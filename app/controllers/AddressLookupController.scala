/*
 * Copyright 2020 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers

import java.io.File

import config.{ALFCookieNames, FrontendAppConfig}
import controllers.countOfResults._
import forms.ALFForms._
import javax.inject.{Inject, Singleton}
import model._
import play.api.i18n.{I18nSupport, Lang, Messages, MessagesApi}
import play.api.libs.json.{JsObject, Json}
import play.api.mvc._
import services.{AddressService, CountryService, JourneyRepository}
import spray.http.Uri
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.HeaderCarrierConverter
import uk.gov.hmrc.play.audit.AuditExtensions._
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.audit.model.{DataEvent, EventTypes}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.PostcodeHelper

import scala.concurrent.{ExecutionContext, Future}

object countOfResults {

  sealed trait ResultsCount

  case class OneResult(res: ProposedAddress) extends ResultsCount

  case class ResultsList(res: Seq[ProposedAddress], firstLookup: Boolean)
      extends ResultsCount

  case class TooManyResults(res: Seq[ProposedAddress], firstLookup: Boolean)
      extends ResultsCount

  case object NoResults extends ResultsCount

}

@Singleton
class AddressLookupController @Inject()(
  journeyRepository: JourneyRepository,
  addressService: AddressService,
  countryService: CountryService,
  auditConnector: AuditConnector,
  implicit val frontendAppConfig: FrontendAppConfig,
  messagesControllerComponents: MessagesControllerComponents,
  remoteMessagesApiProvider: RemoteMessagesApiProvider,
  lookup: views.html.v2.lookup,
  select: views.html.v2.select,
  uk_mode_edit: views.html.v2.uk_mode_edit,
  non_uk_mode_edit: views.html.v2.non_uk_mode_edit,
  confirm: views.html.v2.confirm,
  no_results: views.html.v2.no_results,
  too_many_results: views.html.v2.too_many_results
)(override implicit val ec: ExecutionContext)
    extends AlfController(journeyRepository, messagesControllerComponents) {

  def countries(welshFlag: Boolean = false): Seq[(String, String)] =
    countryService.findAll(welshFlag).map { c =>
      (c.code -> c.name)
    }

  def getWelshContent(
    journeyData: JourneyDataV2
  )(implicit request: Request[_]): Boolean = {
    journeyData.welshEnabled && request.cookies.exists(
      kv => kv.name == "PLAY_LANG" && kv.value == "cy"
    )
  }

  def requestWithWelshHeader(useWelsh: Boolean)(req: => Result) = {
    req.withCookies(Cookie(ALFCookieNames.useWelsh, useWelsh.toString))
  }

  // GET  /no-journey
  // display an error page when a required journey is not available
  def noJourney() = Action { implicit req =>
    Ok(views.html.no_journey(frontendAppConfig))
  }

  // GET  /:id/lookup
  def lookup(id: String,
             postcode: Option[String] = None,
             filter: Option[String] = None): Action[AnyContent] = Action.async {
    implicit req =>
      withJourneyV2(id) { journeyData =>
        import JourneyLabelsForMessages._

        val remoteMessagesApi =
          remoteMessagesApiProvider.getRemoteMessagesApi(
            journeyData.config.labels
              .map(ls => Json.toJsObject(ls))
              .orElse(Some(Json.obj())))

        implicit val messages: Messages = remoteMessagesApi.preferred(req)

        val isWelsh = getWelshContent(journeyData)
        implicit val permittedLangs: Seq[Lang] =
          if (isWelsh) Seq(Lang("cy")) else Seq(Lang("en"))

        val isUKMode = journeyData.config.options.isUkMode
        val formPrePopped = lookupForm(isWelsh)(messages).fill(
          Lookup(filter, PostcodeHelper.displayPostcode(postcode))
        )

        (
          Some(journeyData.copy(selectedAddress = None)),
          requestWithWelshHeader(isWelsh) {
            Ok(lookup(id, journeyData, formPrePopped, isWelsh, isUKMode)(req, messages, frontendAppConfig))
          }
        )
      }
  }

  // GET  /:id/select
  def select(id: String): Action[AnyContent] = Action.async { implicit req =>
    withFutureJourneyV2(id) { journeyData =>
      val isWelsh = getWelshContent(journeyData)
      implicit val lang: Lang = if (isWelsh) Lang("cy") else Lang("en")
      implicit val permittedLangs: Seq[Lang] =
        if (isWelsh) Seq(Lang("cy")) else Seq(Lang("en"))

      val isUKMode = journeyData.config.options.isUkMode

      lookupForm(journeyData.config.options.isUkMode)
        .bindFromRequest()
        .fold(
          errors =>
            Future.successful((None -> requestWithWelshHeader(isWelsh) {
              BadRequest(lookup(id, journeyData, errors, isWelsh, isUKMode))
            })),
          lookup => {
            val lookupWithFormattedPostcode = lookup
              .copy(postcode = PostcodeHelper.displayPostcode(lookup.postcode))

            handleLookup(id, journeyData, lookup) map {
              case OneResult(address) =>
                val journeyDataWithSelectedAddress = journeyData.copy(
                  selectedAddress = Some(address.toConfirmableAddress(id))
                )

                Some(journeyDataWithSelectedAddress) -> requestWithWelshHeader(
                  isWelsh
                ) {
                  Redirect(routes.AddressLookupController.confirm(id))
                }
              case ResultsList(addresses, firstLookup) =>
                val journeyDataWithProposals =
                  journeyData.copy(proposals = Some(addresses))

                Some(journeyDataWithProposals) -> requestWithWelshHeader(
                  isWelsh
                ) {
                  Ok(
                    select(
                      id,
                      journeyData,
                      selectForm(),
                      Proposals(Some(addresses)),
                      lookupWithFormattedPostcode,
                      firstLookup,
                      isWelsh,
                      isUKMode
                    )
                  )
                }
              case TooManyResults(_, firstLookup) =>
                None -> requestWithWelshHeader(isWelsh) {
                  Ok(
                    too_many_results(
                      id,
                      journeyData,
                      lookupWithFormattedPostcode,
                      firstLookup,
                      isWelsh,
                      isUKMode
                    )
                  )
                }
              case NoResults =>
                None -> requestWithWelshHeader(isWelsh) {
                  Ok(
                    no_results(
                      id,
                      journeyData,
                      lookupWithFormattedPostcode.postcode,
                      isWelsh,
                      isUKMode
                    )
                  )
                }
            }
          }
        )
    }
  }

  private[controllers] def handleLookup(
    id: String,
    journeyData: JourneyDataV2,
    lookup: Lookup,
    firstLookup: Boolean = true
  )(implicit hc: HeaderCarrier): Future[ResultsCount] = {

    val addressLimit = journeyData.config.options.selectPageConfig
      .getOrElse(SelectPageConfig())
      .proposalListLimit

    addressService
      .find(lookup.postcode, lookup.filter, journeyData.config.options.isUkMode)
      .flatMap {
        case noneFound if noneFound.isEmpty =>
          if (lookup.filter.isDefined) {
            handleLookup(
              id: String,
              journeyData,
              lookup.copy(filter = None),
              firstLookup = false
            ) //TODO Pass a boolean through to show no results were found and this is a retry?
          } else {
            Future.successful(NoResults)
          }
        case oneFound if oneFound.size == 1 =>
          Future.successful(OneResult(oneFound.head))
        case tooManyFound
            if tooManyFound.size > addressLimit.getOrElse(tooManyFound.size) =>
          Future.successful(
            TooManyResults(tooManyFound.take(addressLimit.get), firstLookup)
          )
        case displayProposals =>
          Future.successful(ResultsList(displayProposals, firstLookup))
      }
  }

  // TODO enable journey-configurable limit on proposal list size
  // POST /:id/select
  def handleSelect(id: String,
                   filter: Option[String],
                   postcode: String): Action[AnyContent] = Action.async {
    implicit req =>
      withJourneyV2(id) { journeyData =>
        val isWelsh = getWelshContent(journeyData)
        implicit val lang: Lang = if (isWelsh) Lang("cy") else Lang("en")

        val isUKMode = journeyData.config.options.isUkMode
        val bound = selectForm().bindFromRequest()

        bound.fold(
          errors => {
            (None -> requestWithWelshHeader(isWelsh) {
              BadRequest(
                select(
                  id,
                  journeyData,
                  errors,
                  Proposals(journeyData.proposals),
                  Lookup(filter, postcode),
                  firstSearch = true,
                  isWelsh = isWelsh,
                  isUKMode = isUKMode
                )
              )
            })
          },
          selection => {
            journeyData.proposals match {
              case Some(props) => {
                props.find(_.addressId == selection.addressId) match {
                  case Some(addr) =>
                    val journeyDataWithConfirmableAddress = journeyData.copy(
                      selectedAddress = Some(addr.toConfirmableAddress(id))
                    )
                    (
                      Some(journeyDataWithConfirmableAddress),
                      requestWithWelshHeader(isWelsh) {
                        Redirect(routes.AddressLookupController.confirm(id))
                      }
                    )
                  case None =>
                    (None, requestWithWelshHeader(isWelsh) {
                      BadRequest(
                        select(
                          id,
                          journeyData,
                          bound,
                          Proposals(Some(props)),
                          Lookup(filter, postcode),
                          firstSearch = true,
                          isWelsh = isWelsh,
                          isUKMode = isUKMode
                        )
                      )
                    })
                }
              }
              case None =>
                (None, Redirect(routes.AddressLookupController.lookup(id)))
            }
          }
        )
      }
  }

  private[controllers] def allowedCountries(
    countries: Seq[(String, String)],
    countryCodesOpt: Option[Set[String]]
  ): Seq[(String, String)] = {
    countryCodesOpt match {
      case None => countries
      case Some(countryCodes) =>
        countries filter {
          case (code, _) => countryCodes.contains(code)
        }
    }
  }

  // GET  /:id/edit
  def edit(id: String, lookUpPostCode: Option[String]): Action[AnyContent] =
    Action.async { implicit req =>
      withJourneyV2(id) { journeyData =>
        {
          val editAddress =
            addressOrDefault(journeyData.selectedAddress, lookUpPostCode)
          val allowedSeqCountries = (s: Seq[(String, String)]) =>
            allowedCountries(s, journeyData.config.options.allowedCountryCodes)

          val isWelsh = getWelshContent(journeyData)
          implicit val permittedLangs: Seq[Lang] =
            if (isWelsh) Seq(Lang("cy")) else Seq(Lang("en"))

          val isUKMode = journeyData.config.options.isUkMode

          if (isUKMode) {
            (None, requestWithWelshHeader(isWelsh) {
              Ok(
                uk_mode_edit(
                  id,
                  journeyData,
                  ukEditForm().fill(editAddress),
                  allowedSeqCountries(Seq.empty),
                  isWelsh,
                  isUKMode
                )
              )
            })
          } else {
            val defaultAddress =
              addressOrEmpty(journeyData.selectedAddress, lookUpPostCode)
            (None, requestWithWelshHeader(isWelsh) {
              Ok(
                non_uk_mode_edit(
                  id,
                  journeyData,
                  nonUkEditForm().fill(defaultAddress),
                  allowedSeqCountries(countries(isWelsh)),
                  isWelsh = isWelsh,
                  isUKMode = isUKMode
                )
              )
            })
          }
        }
      }
    }

  private[controllers] def addressOrDefault(oAddr: Option[ConfirmableAddress],
                                            lookUpPostCode: Option[String] =
                                              None): Edit = {
    oAddr
      .map(_.toEdit)
      .getOrElse(
        Edit(
          "",
          None,
          None,
          "",
          PostcodeHelper.displayPostcode(lookUpPostCode),
          "GB"
        )
      )
  }

  private[controllers] def addressOrEmpty(
    oAddr: Option[ConfirmableAddress],
    lookUpPostCode: Option[String] = None
  ): Edit = {
    oAddr
      .map(_.toEdit)
      .getOrElse(
        Edit(
          "",
          None,
          None,
          "",
          PostcodeHelper.displayPostcode(lookUpPostCode),
          ""
        )
      )
  }

  // POST /:id/edit
  def handleEdit(id: String): Action[AnyContent] = Action.async {
    implicit req =>
      withJourneyV2(id) { journeyData =>
        {
          val isWelsh = getWelshContent(journeyData)
          implicit val permittedLangs: Seq[Lang] =
            if (isWelsh) Seq(Lang("cy")) else Seq(Lang("en"))

          val isUKMode = journeyData.config.options.isUkMode
          if (isUKMode) {
            val validatedForm = isValidPostcode(ukEditForm().bindFromRequest())

            validatedForm.fold(
              errors =>
                (None, requestWithWelshHeader(isWelsh) {
                  BadRequest(
                    uk_mode_edit(
                      id,
                      journeyData,
                      errors,
                      allowedCountries(
                        countries(isWelsh),
                        journeyData.config.options.allowedCountryCodes
                      ),
                      isWelsh,
                      isUKMode
                    )
                  )
                }),
              edit =>
                (
                  Some(
                    journeyData.copy(
                      selectedAddress = Some(edit.toConfirmableAddress(id))
                    )
                  ),
                  requestWithWelshHeader(isWelsh) {
                    Redirect(routes.AddressLookupController.confirm(id))
                  }
              )
            )
          } else {
            val validatedForm =
              isValidPostcode(nonUkEditForm().bindFromRequest())

            validatedForm.fold(
              errors =>
                (None, requestWithWelshHeader(isWelsh) {
                  BadRequest(
                    non_uk_mode_edit(
                      id,
                      journeyData,
                      errors,
                      allowedCountries(
                        countries(isWelsh),
                        journeyData.config.options.allowedCountryCodes
                      ),
                      isWelsh = isWelsh,
                      isUKMode = isUKMode
                    )
                  )
                }),
              edit =>
                (
                  Some(
                    journeyData.copy(
                      selectedAddress = Some(edit.toConfirmableAddress(id))
                    )
                  ),
                  requestWithWelshHeader(isWelsh) {
                    Redirect(routes.AddressLookupController.confirm(id))
                  }
              )
            )
          }
        }
      }
  }

  // GET  /:id/confirm
  def confirm(id: String): Action[AnyContent] = Action.async { implicit req =>
    withJourneyV2(id) { journeyData =>
      {
        val isWelsh = getWelshContent(journeyData)
        implicit val permittedLangs: Seq[Lang] =
          if (isWelsh) Seq(Lang("cy")) else Seq(Lang("en"))

        val isUKMode = journeyData.config.options.isUkMode

        journeyData.selectedAddress
          .map(
            _ =>
              (None, requestWithWelshHeader(isWelsh) {
                Ok(
                  confirm(
                    id,
                    journeyData,
                    journeyData.selectedAddress,
                    isWelsh,
                    isUKMode
                  )
                )
              })
          )
          .getOrElse((None, requestWithWelshHeader(isWelsh) {
            Redirect(routes.AddressLookupController.lookup(id))
          }))
      }
    }
  }

  // POST /:id/confirm
  def handleConfirm(id: String): Action[AnyContent] = Action.async {
    implicit req =>
      withJourneyV2(id) { journeyData =>
        {
          val isWelsh = getWelshContent(journeyData)
          implicit val permittedLangs: Seq[Lang] =
            if (isWelsh) Seq(Lang("cy")) else Seq(Lang("en"))

          if (journeyData.selectedAddress.isDefined) {
            val jd =
              journeyData.copy(confirmedAddress = journeyData.selectedAddress)

            auditConnector.sendEvent(
              new DataEvent(
                "address-lookup-frontend",
                EventTypes.Succeeded,
                tags = hc.toAuditTags("ConfirmAddress", req.uri),
                detail = Map(
                  "auditRef" -> id,
                  "confirmedAddress" -> jd.confirmedAddress.get.toDescription,
                  "confirmedAddressId" -> jd.confirmedAddress.get.id
                    .getOrElse("-")
                )
              )
            )

            (Some(jd), requestWithWelshHeader(isWelsh) {
              Redirect(
                Uri(journeyData.config.options.continueUrl)
                  .withQuery("id" -> id)
                  .toString()
              )
            })
          } else {
            (None, requestWithWelshHeader(isWelsh) {
              Redirect(routes.AddressLookupController.confirm(id))
            })
          }
        }
      }
  }

//   GET /renewSession
  def renewSession: Action[AnyContent] =
    Action { implicit req =>
    Ok.sendFile(new File("conf/renewSession.jpg")).as("image/jpeg")
  }

  // GET /destroySession
  def destroySession(timeoutUrl: String): Action[AnyContent] = Action {
    implicit req =>
      Redirect(timeoutUrl).withNewSession
  }
}

abstract class AlfController @Inject()(
  journeyRepository: JourneyRepository,
  messagesControllerComponents: MessagesControllerComponents
)(implicit val ec: ExecutionContext)
    extends FrontendController(messagesControllerComponents)
    with AlfI18nSupport {

  protected def withJourney(id: String,
                            noJourney: Result = Redirect(
                              routes.AddressLookupController.noJourney()
                            ))(
    action: JourneyData => (Option[JourneyData], Result)
  )(implicit request: Request[AnyContent]): Future[Result] = {
    implicit val hc = HeaderCarrierConverter.fromHeadersAndSession(
      request.headers,
      Some(request.session)
    )
    journeyRepository.get(id).flatMap {
      case Some(journeyData) => {
        val outcome = action(journeyData)
        outcome._1.fold(Future.successful(outcome._2))(
          modifiedJourneyData =>
            journeyRepository
              .put(id, modifiedJourneyData)
              .map(success => outcome._2)
        )
      }
      case None => Future.successful(noJourney)
    }
  }

  protected def withJourneyV2(id: String,
                              noJourney: Result = Redirect(
                                routes.AddressLookupController.noJourney()
                              ))(
    action: JourneyDataV2 => (Option[JourneyDataV2], Result)
  )(implicit request: Request[AnyContent]): Future[Result] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter
      .fromHeadersAndSession(request.headers, Some(request.session))
    journeyRepository.getV2(id).flatMap {
      case Some(journeyData) =>
        val outcome = action(journeyData)
        outcome._1.fold(Future.successful(outcome._2))(
          modifiedJourneyData =>
            journeyRepository
              .putV2(id, modifiedJourneyData)
              .map(_ => outcome._2)
        )
      case None => Future.successful(noJourney)
    }
  }

  protected def withFutureJourney(id: String,
                                  noJourney: Result = Redirect(
                                    routes.AddressLookupController.noJourney()
                                  ))(
    action: JourneyData => Future[(Option[JourneyData], Result)]
  )(implicit request: Request[AnyContent]): Future[Result] = {
    implicit val hc = HeaderCarrierConverter.fromHeadersAndSession(
      request.headers,
      Some(request.session)
    )
    journeyRepository.get(id).flatMap {
      case Some(journeyData) => {
        action(journeyData).flatMap { outcome =>
          outcome._1 match {
            case Some(modifiedJourneyData) =>
              journeyRepository
                .put(id, modifiedJourneyData)
                .map(success => outcome._2)
            case None => Future.successful(outcome._2)
          }
        }
      }
      case None => Future.successful(noJourney)
    }
  }

  protected def withFutureJourneyV2(id: String,
                                    noJourney: Result = Redirect(
                                      routes.AddressLookupController.noJourney()
                                    ))(
    action: JourneyDataV2 => Future[(Option[JourneyDataV2], Result)]
  )(implicit request: Request[AnyContent]): Future[Result] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter
      .fromHeadersAndSession(request.headers, Some(request.session))
    journeyRepository.getV2(id).flatMap {
      case Some(journeyData) =>
        action(journeyData).flatMap { outcome =>
          outcome._1 match {
            case Some(modifiedJourneyData) =>
              journeyRepository
                .putV2(id, modifiedJourneyData)
                .map(_ => outcome._2)
            case None => Future.successful(outcome._2)
          }
        }
      case None => Future.successful(noJourney)
    }
  }
}

case class Proposals(proposals: Option[Seq[ProposedAddress]]) {

  def toHtmlOptions: Seq[(String, String)] = {
    proposals
      .map { props =>
        props.map { addr =>
          {
            (addr.addressId, addr.toDescription)
          }
        }.sorted
      }
      .getOrElse(Seq.empty)
  }
}

case class Confirmed(id: String)
