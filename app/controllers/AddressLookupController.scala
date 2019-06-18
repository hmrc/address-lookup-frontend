
package controllers

import java.io.File

import config.{FrontendAuditConnector, FrontendServicesConfig}
import controllers.countOfResults._
import forms.ALFForms._
import javax.inject.{Inject, Singleton}
import model._
import play.api.Play
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import services.{AddressService, CountryService, JourneyRepository}
import spray.http.Uri
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.HeaderCarrierConverter
import uk.gov.hmrc.play.audit.AuditExtensions._
import uk.gov.hmrc.play.audit.model.{DataEvent, EventTypes}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import utils.PostcodeHelper

import scala.concurrent.{ExecutionContext, Future}

object countOfResults {

  sealed trait ResultsCount

  case class OneResult(res: ProposedAddress) extends ResultsCount

  case class ResultsList(res: Seq[ProposedAddress], firstLookup: Boolean) extends ResultsCount

  case class TooManyResults(res: Seq[ProposedAddress], firstLookup: Boolean) extends ResultsCount

  case object NoResults extends ResultsCount

}

@Singleton
class AddressLookupController @Inject()(journeyRepository: JourneyRepository, addressService: AddressService, countryService: CountryService)
                                       (override implicit val ec: ExecutionContext, override implicit val messagesApi: MessagesApi)
  extends AlfController(journeyRepository) {

  def countries(welshFlag: Boolean = false): Seq[(String, String)] = countryService.findAll(welshFlag).map { c =>
    (c.code -> c.name)
  }

  def getWelshContent(journeyData: JourneyDataV2)(implicit request: Request[_]): Boolean = {
    journeyData.config.labels.flatMap(_.cy).isDefined && request.cookies.exists(kv => kv.name == "PLAY_LANG" && kv.value == "cy")
  }

  // GET  /no-journey
  // display an error page when a required journey is not available
  def noJourney() = Action { implicit req =>
    Ok(views.html.no_journey())
  }

  // GET  /:id/lookup
  def lookup(id: String, postcode: Option[String] = None, filter: Option[String] = None) = Action.async { implicit req =>
    withJourneyV2(id) { journeyData =>
      val formPrePopped = lookupForm.fill(Lookup(filter, PostcodeHelper.displayPostcode(postcode)))
      (Some(journeyData.copy(selectedAddress = None)), Ok(views.html.v2.lookup(id, journeyData, formPrePopped, isWelsh = getWelshContent(journeyData))))
    }
  }

  // GET  /:id/select
  def select(id: String) = Action.async { implicit req =>
    withFutureJourneyV2(id) { journeyData =>
      lookupForm.bindFromRequest().fold(
        errors => Future.successful((None, BadRequest(views.html.v2.lookup(id, journeyData, errors,isWelsh = getWelshContent(journeyData))))),
        lookup => {
          val lookupWithFormattedPostcode = lookup.copy(postcode = PostcodeHelper.displayPostcode(lookup.postcode))
          handleLookup(id, journeyData, lookup) map {
            case OneResult(address) => Some(journeyData.copy(selectedAddress = Some(address.toConfirmableAddress(id)))) -> Redirect(routes.AddressLookupController.confirm(id))
            case ResultsList(addresses, firstLookup) => Some(journeyData.copy(proposals = Some(addresses))) ->
              Ok(views.html.v2.select(id, journeyData, selectForm, Proposals(Some(addresses)), Some(lookupWithFormattedPostcode), firstLookup))
            case TooManyResults(addresses, firstLookup) => None -> Ok(views.html.v2.too_many_results(id, journeyData, lookupWithFormattedPostcode, firstLookup))
            case NoResults => None -> Ok(views.html.v2.no_results(id, journeyData, lookupWithFormattedPostcode.postcode))
          }
        }
      )
    }
  }

  private[controllers] def handleLookup(id: String, journeyData: JourneyDataV2, lookup: Lookup, firstLookup: Boolean = true)(implicit hc: HeaderCarrier): Future[ResultsCount] = {
    val addressLimit = journeyData.config.options.selectPageConfig.getOrElse(SelectPageConfig()).proposalListLimit
    addressService.find(lookup.postcode, lookup.filter, journeyData.config.options.isUkMode).flatMap {
      case noneFound if noneFound.isEmpty =>
        if (lookup.filter.isDefined) {
          handleLookup(id: String, journeyData, lookup.copy(filter = None), firstLookup = false) //TODO Pass a boolean through to show no results were found and this is a retry?
        } else {
          Future.successful(NoResults)
        }
      case oneFound if oneFound.size == 1 => Future.successful(OneResult(oneFound.head))
      case tooManyFound if tooManyFound.size > addressLimit.getOrElse(tooManyFound.size) => Future.successful(TooManyResults(tooManyFound.take(addressLimit.get), firstLookup))
      case displayProposals => Future.successful(ResultsList(displayProposals, firstLookup))
    }
  }

  // TODO enable journey-configurable limit on proposal list size
  // POST /:id/select
  def handleSelect(id: String) = Action.async { implicit req =>
    withJourneyV2(id) { journeyData =>
      val bound = selectForm.bindFromRequest()
      bound.fold(
        errors => (None, BadRequest(views.html.v2.select(id, journeyData, errors, Proposals(journeyData.proposals), None, firstSearch = true))),
        selection => {
          journeyData.proposals match {
            case Some(props) => {
              props.find(_.addressId == selection.addressId) match {
                case Some(addr) => (Some(journeyData.copy(selectedAddress = Some(addr.toConfirmableAddress(id)))), Redirect(routes.AddressLookupController.confirm(id)))
                case None => (None, BadRequest(views.html.v2.select(id, journeyData, bound, Proposals(Some(props)), None, firstSearch = true)))
              }
            }
            case None => (None, Redirect(routes.AddressLookupController.lookup(id)))
          }
        }
      )
    }
  }

  private[controllers] def allowedCountries(countries: Seq[(String, String)], countryCodesOpt: Option[Set[String]]): Seq[(String, String)] = {
    countryCodesOpt match {
      case None => countries
      case Some(countryCodes) => countries filter {
        case (code, _) => countryCodes.contains(code)
      }
    }
  }

  // GET  /:id/edit
  def edit(id: String, lookUpPostCode: Option[String], uk: Option[Boolean]) = Action.async {
    implicit req =>
      withJourneyV2(id) {
        journeyData =>
          val editAddress = addressOrDefault(journeyData.selectedAddress, lookUpPostCode)
          val allowedSeqCountries = (s: Seq[(String, String)]) => allowedCountries(s, journeyData.config.options.allowedCountryCodes)
          val isWelsh = getWelshContent(journeyData)
          if (journeyData.config.options.isUkMode|| uk.contains(true)) {
            (None, Ok(views.html.v2.uk_mode_edit(id, journeyData, ukEditForm(isWelsh).fill(editAddress), allowedSeqCountries(Seq.empty), isWelsh)))

          } else {
            (None, Ok(views.html.v2.non_uk_mode_edit(id, journeyData, nonUkEditForm(isWelsh).fill(editAddress), allowedSeqCountries(countries(isWelsh)),isWelsh = isWelsh)))
          }
      }
  }

  private[controllers] def addressOrDefault(oAddr: Option[ConfirmableAddress], lookUpPostCode: Option[String] = None): Edit = {
    oAddr.map(_.toEdit).getOrElse(Edit("", None, None, "", PostcodeHelper.displayPostcode(lookUpPostCode), Some("GB")))
  }

  def handleUkEdit(id: String): Action[AnyContent] = Action.async {
    implicit req =>
      withJourneyV2(id) {
        journeyData =>
          val isWelsh = getWelshContent(journeyData)
          val validatedForm = isValidPostcode(ukEditForm(isWelsh).bindFromRequest(), isWelsh)
            validatedForm.fold(
            errors => (None, BadRequest(views.html.v2.uk_mode_edit(id, journeyData, errors, allowedCountries(countries(isWelsh), journeyData.config.options.allowedCountryCodes), isWelsh))),
            edit => (Some(journeyData.copy(selectedAddress = Some(edit.toConfirmableAddress(id)))), Redirect(routes.AddressLookupController.confirm(id)))

      )
      }
  }

  // POST /:id/edit
  def handleNonUkEdit(id: String): Action[AnyContent] = Action.async {
    implicit req =>
      withJourneyV2(id) {
        journeyData =>
          val isWelsh = getWelshContent(journeyData)
          val validatedForm = isValidPostcode(nonUkEditForm(isWelsh).bindFromRequest())
          validatedForm.fold(
            errors => (None, BadRequest(views.html.v2.non_uk_mode_edit(id, journeyData, errors, allowedCountries(countries(isWelsh), journeyData.config.options.allowedCountryCodes), isWelsh = isWelsh))),
            edit => (Some(journeyData.copy(selectedAddress = Some(edit.toConfirmableAddress(id)))), Redirect(routes.AddressLookupController.confirm(id)))
          )
      }
  }

  // GET  /:id/confirm
  def confirm(id: String) = Action.async {
    implicit req =>
      withJourneyV2(id) {
        journeyData =>
          journeyData.selectedAddress
            .map(_ => (None, Ok(views.html.v2.confirm(id, journeyData, journeyData.selectedAddress, isWelsh = getWelshContent(journeyData)))))
            .getOrElse((None, Redirect(routes.AddressLookupController.lookup(id))))
      }
  }

  // POST /:id/confirm
  def handleConfirm(id: String) = Action.async {
    implicit req =>
      withJourneyV2(id) {
        journeyData =>
          if (journeyData.selectedAddress.isDefined) {
            val jd = journeyData.copy(confirmedAddress = journeyData.selectedAddress)
            FrontendAuditConnector.sendEvent(new DataEvent("address-lookup-frontend", EventTypes.Succeeded, tags = hc.toAuditTags("ConfirmAddress", req.uri), detail = Map(
              "auditRef" -> id,
              "confirmedAddress" -> jd.confirmedAddress.get.toDescription,
              "confirmedAddressId" -> jd.confirmedAddress.get.id.getOrElse("-")
            )))
            (Some(jd), Redirect(Uri(journeyData.config.options.continueUrl).withQuery("id" -> id).toString()))
          } else {
            (None, Redirect(routes.AddressLookupController.confirm(id)))
          }
      }
  }

  // GET /renewSession
  def renewSession: Action[AnyContent] = Action {
    implicit req =>
      Ok.sendFile(new File("conf/renewSession.jpg")).as("image/jpeg")
  }

  // GET /destroySession
  def destroySession(timeoutUrl: String): Action[AnyContent] = Action {
    implicit req =>
      Redirect(timeoutUrl).withNewSession
  }
}

abstract class AlfController @Inject()(journeyRepository: JourneyRepository)
                                      (implicit val ec: ExecutionContext, implicit val messagesApi: MessagesApi)
  extends FrontendController with I18nSupport with FrontendServicesConfig {

  protected def withJourney(id: String, noJourney: Result = Redirect(routes.AddressLookupController.noJourney()))(action: JourneyData => (Option[JourneyData], Result))(implicit request: Request[AnyContent]): Future[Result] = {
    implicit val hc = HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))
    journeyRepository.get(id).flatMap {
      case Some(journeyData) => {
        val outcome = action(journeyData)
        outcome._1.fold(Future.successful(outcome._2))(modifiedJourneyData => journeyRepository.put(id, modifiedJourneyData).map(success => outcome._2))
      }
      case None => Future.successful(noJourney)
    }
  }

  protected def withJourneyV2(id: String, noJourney: Result = Redirect(routes.AddressLookupController.noJourney()))(action: JourneyDataV2 => (Option[JourneyDataV2], Result))(implicit request: Request[AnyContent]): Future[Result] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))
    journeyRepository.getV2(id).flatMap {
      case Some(journeyData) =>
        val outcome = action(journeyData)
        outcome._1.fold(Future.successful(outcome._2))(modifiedJourneyData => journeyRepository.putV2(id, modifiedJourneyData).map(_ => outcome._2))
      case None => Future.successful(noJourney)
    }
  }


  protected def withFutureJourney(id: String, noJourney: Result = Redirect(routes.AddressLookupController.noJourney()))(action: JourneyData => Future[(Option[JourneyData], Result)])(implicit request: Request[AnyContent]): Future[Result] = {
    implicit val hc = HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))
    journeyRepository.get(id).flatMap {
      case Some(journeyData) => {
        action(journeyData).flatMap { outcome =>
          outcome._1 match {
            case Some(modifiedJourneyData) => journeyRepository.put(id, modifiedJourneyData).map(success => outcome._2)
            case None => Future.successful(outcome._2)
          }
        }
      }
      case None => Future.successful(noJourney)
    }
  }

  protected def withFutureJourneyV2(id: String, noJourney: Result = Redirect(routes.AddressLookupController.noJourney()))(action: JourneyDataV2 => Future[(Option[JourneyDataV2], Result)])(implicit request: Request[AnyContent]): Future[Result] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))
    journeyRepository.getV2(id).flatMap {
      case Some(journeyData) =>
        action(journeyData).flatMap { outcome =>
          outcome._1 match {
            case Some(modifiedJourneyData) => journeyRepository.putV2(id, modifiedJourneyData).map(_ => outcome._2)
            case None => Future.successful(outcome._2)
          }
        }
      case None => Future.successful(noJourney)
    }
  }
}

case class Proposals(proposals: Option[Seq[ProposedAddress]]) {

  def toHtmlOptions: Seq[(String, String)] = {
    proposals.map { props =>
      props.map { addr =>
        (addr.addressId, addr.toDescription)
      }
    }.getOrElse(Seq.empty)
  }
}

case class Confirmed(id: String)