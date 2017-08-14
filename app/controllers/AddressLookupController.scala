
package controllers

import javax.inject.{Inject, Singleton}

import config.FrontendAuditConnector
import model._
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import services.{AddressService, CountryService, JourneyRepository}
import uk.gov.hmrc.address.uk.Postcode
import uk.gov.hmrc.play.audit.model.{DataEvent, EventTypes}
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.audit.AuditExtensions._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AddressLookupController @Inject()(journeyRepository: JourneyRepository, addressService: AddressService, countryService: CountryService)
                                       (override implicit val ec: ExecutionContext, override implicit val messagesApi: MessagesApi)
  extends AlfController(journeyRepository) {

  val countries: Seq[(String, String)] = countryService.findAll.map { c =>
    (c.code -> c.name)
  }

  val lookupForm = Form(
    mapping(
      "filter" -> optional(text.verifying("Your house name/number needs to be fewer than 256 characters", txt => txt.length < 256)),
      "postcode" -> text.verifying("The postcode you entered appears to be incomplete or invalid. Please check and try again.", p => Postcode.cleanupPostcode(p).isDefined)
    )(Lookup.apply)(Lookup.unapply)
  )

  val selectForm = Form(
    mapping(
      "addressId" -> text(1, 255)
    )(Select.apply)(Select.unapply)
  )

  val editForm = Form(
    mapping(
      "line1" -> text
        .verifying("The first line of your address needs to be fewer than 256 characters", _.length < 256)
        .verifying("This field is required", _.length > 0),
      "line2" -> optional(text.verifying("The second line of your address needs to be fewer than 256 characters", _.length < 256)),
      "line3" -> optional(text.verifying("The third line of your address needs to be fewer than 256 characters", _.length < 256)),
      "town" -> text
        .verifying("The fourth line of your address needs to be fewer than 256 characters", _.length < 256)
        .verifying("This field is required", _.length > 0),
      "postcode" -> text,
      "countryCode" -> optional(text(2))
    )(Edit.apply)(Edit.unapply)
      .verifying("The postcode is invalid", edit => edit.isValidPostcode())
  )

  val confirmedForm = Form(
    mapping(
      "id" -> text(1, 255)
    )(Confirmed.apply)(Confirmed.unapply)
  )

  // GET  /no-journey
  // display an error page when a required journey is not available
  def noJourney() = Action { implicit req =>
    Ok(views.html.no_journey())
  }

  // GET  /:id/lookup
  // show the lookup form
  // we could potentially make a debatable minor "improvement" here by pre-populating with previously entered values stored in journeyData
  def lookup(id: String) = Action.async { implicit req =>
    withJourney(id) { journeyData =>
      (None, Ok(views.html.lookup(id, journeyData, lookupForm)))
    }
  }

  // GET  /:id/select
  // show a list of proposals from lookup parameters; always do the remote lookup as the parameters may have changed
  // go back to the lookup form on form binding error
  // we could optimize this to check whether or not parameters have changed but not really worth the effort at present
  def select(id: String) = Action.async { implicit req =>
    withFutureJourney(id) { journeyData =>
      lookupForm.bindFromRequest().fold(
        errors => Future.successful((None, BadRequest(views.html.lookup(id, journeyData, errors)))),
        lookup => {
          addressService.find(lookup.postcode, lookup.filter).map { props =>
            if (props.isEmpty) (None, Ok(views.html.lookup(id, journeyData, lookupForm.fill(lookup), Some(journeyData.config.lookupPage.noResultsFoundMessage.getOrElse("Sorry, we couldn't find anything for that postcode.")))))
            else if (props.size > journeyData.config.selectPage.proposalListLimit.getOrElse(props.size)) (None, Ok(views.html.lookup(id, journeyData, lookupForm.fill(lookup), Some(journeyData.config.lookupPage.resultLimitExceededMessage.getOrElse("There were too many results. Please add additional details to limit the number of results.")))))
            else (Some(journeyData.copy(proposals = Some(props))), Ok(views.html.select(id, journeyData, selectForm, Proposals(Some(props)))))
          }
        }
      )
    }
  }

  // TODO enable journey-configurable limit on proposal list size
  // POST /:id/select
  def handleSelect(id: String) = Action.async { implicit req =>
    withJourney(id) { journeyData =>
      val bound = selectForm.bindFromRequest()
      bound.fold(
        errors => (None, BadRequest(views.html.select(id, journeyData, errors, Proposals(journeyData.proposals)))),
        selection => {
          journeyData.proposals match {
            case Some(props) => {
              props.filter(_.addressId == selection.addressId).headOption match {
                case Some(addr) => (Some(journeyData.copy(selectedAddress = Some(addr.toConfirmableAddress(id)))), Redirect(routes.AddressLookupController.confirm(id)))
                case None => (None, BadRequest(views.html.select(id, journeyData, bound, Proposals(Some(props)))))
              }
            }
            case None => (None, Redirect(routes.AddressLookupController.lookup(id)))
          }
        }
      )
    }
  }

  // GET  /:id/edit
  def edit(id: String) = Action.async { implicit req =>
    withJourney(id) { journeyData =>
      val f = addressOrDefault(journeyData.selectedAddress)
      (None, Ok(views.html.edit(id, journeyData, editForm.fill(f), countries)))
    }
  }

  private[controllers] def addressOrDefault(oAddr: Option[ConfirmableAddress]): Edit = {
    oAddr map (_.toEdit) getOrElse Edit("", None, None, "", "", Some("GB"))
  }

  // POST /:id/edit
  def handleEdit(id: String) = Action.async { implicit req =>
    withJourney(id) { journeyData =>
      val bound = editForm.bindFromRequest()
      bound.fold(
        errors => (None, BadRequest(views.html.edit(id, journeyData, errors, countries))),
        edit => (Some(journeyData.copy(selectedAddress = Some(edit.toConfirmableAddress(id)))), Redirect(routes.AddressLookupController.confirm(id)))
      )
    }
  }

  // GET  /:id/confirm
  def confirm(id: String) = Action.async { implicit req =>
    withJourney(id) { journeyData =>
      (None, Ok(views.html.confirm(id, journeyData, journeyData.selectedAddress)))
    }
  }

  // POST /:id/confirm
  def handleConfirm(id: String) = Action.async { implicit req =>
    withJourney(id) { journeyData =>
      if (journeyData.selectedAddress.isDefined) {
        val jd = journeyData.copy(confirmedAddress = journeyData.selectedAddress)
        FrontendAuditConnector.sendEvent(new DataEvent("address-lookup-frontend", EventTypes.Succeeded, tags = hc.toAuditTags("ConfirmAddress", req.uri), detail = Map(
          "auditRef" -> id,
          "confirmedAddress" -> jd.confirmedAddress.get.toDescription,
          "confirmedAddressId" -> jd.confirmedAddress.get.id.getOrElse("-")
        )))
        (Some(jd), Redirect(s"${journeyData.config.continueUrl}?id=${id}"))
      } else {
        (None, Redirect(routes.AddressLookupController.confirm(id)))
      }
    }
  }

}

abstract class AlfController @Inject()(journeyRepository: JourneyRepository)
                                      (implicit val ec: ExecutionContext, implicit val messagesApi: MessagesApi)
  extends FrontendController with I18nSupport with ServicesConfig {

  protected def withJourney(id: String, noJourney: Result = Redirect(routes.AddressLookupController.noJourney()))(action: JourneyData => (Option[JourneyData], Result))(implicit request: Request[AnyContent]): Future[Result] = {
    implicit val hc = HeaderCarrier.fromHeadersAndSession(request.headers, Some(request.session))
    journeyRepository.get(id).flatMap { maybeJournalData =>
      maybeJournalData match {
        case Some(journeyData) => {
          val outcome = action(journeyData)
          outcome._1 match {
            case Some(modifiedJourneyData) => journeyRepository.put(id, modifiedJourneyData).map(success => outcome._2)
            case None => Future.successful(outcome._2)
          }
        }
        case None => Future.successful(noJourney)
      }
    }
  }

  protected def withFutureJourney(id: String, noJourney: Result = Redirect(routes.AddressLookupController.noJourney()))(action: JourneyData => Future[(Option[JourneyData], Result)])(implicit request: Request[AnyContent]): Future[Result] = {
    implicit val hc = HeaderCarrier.fromHeadersAndSession(request.headers, Some(request.session))
    journeyRepository.get(id).flatMap { maybeJournalData =>
      maybeJournalData match {
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


