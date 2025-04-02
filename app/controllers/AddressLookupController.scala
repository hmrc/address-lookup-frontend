/*
 * Copyright 2024 HM Revenue & Customs
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

import address.v2.{Countries, Country}
import config.{ALFCookieNames, FrontendAppConfig}
import forms.ALFForms._
import model._
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.mvc._
import services.{CountryService, JourneyRepository}
import uk.gov.hmrc.play.bootstrap.binders.RedirectUrl
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.RelativeOrAbsoluteWithHostnameFromAllowlist
import views.ViewHelper

import java.io.File
import java.net.URI
import javax.inject.{Inject, Singleton}
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
                                         implicit val frontendAppConfig: FrontendAppConfig,
                                         messagesControllerComponents: MessagesControllerComponents,
                                         remoteMessagesApiProvider: RemoteMessagesApiProvider,
                                         countryService: CountryService,
                                         error_template: views.html.error_template,
                                         country_picker: views.html.country_picker)(override implicit val ec: ExecutionContext)
  extends AlfController(journeyRepository, messagesControllerComponents) {

  private def countries(welshFlag: Boolean): Seq[Country] =
    countryService.findAll(welshFlag)

  // GET  /no-journey
  // display an error page when a required journey is not available
  def noJourney(): Action[AnyContent] = Action { implicit req =>
    implicit val messages: Messages = messagesApi.preferred(req)
    Ok(error_template(messages("no.journey.title.text"), messages("no.journey.heading.text"), ""))
  }

  def begin(id: String): Action[AnyContent] = Action.async {
    implicit req =>
      journeyRepository.getV2(id).map {
        case Some(journeyData) =>


          val isUKMode = journeyData.config.options.isUkMode

          if (isUKMode) {
            Redirect(routes.AbpAddressLookupController.lookup(id, None, None))
          }
          else {
            Redirect(routes.AddressLookupController.countryPicker(id))
          }

        case None => Redirect(routes.AddressLookupController.noJourney())
      }
  }

  def countryPicker(id: String): Action[AnyContent] = Action.async {
    implicit req =>
      journeyRepository.getV2(id).map {
        case Some(journeyData) =>
          import LanguageLabelsForMessages._

          val remoteMessagesApi = remoteMessagesApiProvider.getRemoteMessagesApi(
            journeyData.config.labels.map(ls => Json.toJsObject(ls)).orElse(Some(Json.obj())))

          implicit val messages: Messages = remoteMessagesApi.preferred(req)

          val isWelsh = getWelshContent(journeyData)
          val isUKMode = journeyData.config.options.isUkMode

          if (isUKMode) {
            Redirect(routes.AbpAddressLookupController.lookup(id, None, None))
          }
          else {
            val allowedSeqCountries = (cs: Seq[Country]) =>
              allowedCountries(cs, journeyData.config.options.allowedCountryCodes)

            requestWithWelshHeader(isWelsh) {
              Ok(country_picker(id, journeyData, countryPickerForm().fill(CountryPicker("")), isWelsh,
                allowedSeqCountries(countries(isWelsh)))(req, messages, frontendAppConfig))
            }
          }

        case None => Redirect(routes.AddressLookupController.noJourney())
      }
  }

  def handleCountryPicker(id: String): Action[AnyContent] = Action.async {
    implicit req =>
      withJourneyV2(id) { journeyData =>
        import LanguageLabelsForMessages._

        val remoteMessagesApi = remoteMessagesApiProvider.getRemoteMessagesApi(
          journeyData.config.labels.map(ls => Json.toJsObject(ls)).orElse(Some(Json.obj())))

        implicit val messages: Messages = remoteMessagesApi.preferred(req)

        val isWelsh = getWelshContent(journeyData)
        val bound = countryPickerForm().bindFromRequest()

        bound.fold(
          errors => {
            val allowedSeqCountries = (cs: Seq[Country]) =>
              allowedCountries(cs, journeyData.config.options.allowedCountryCodes)

            None -> {
              requestWithWelshHeader(isWelsh) {
                BadRequest(country_picker(id, journeyData, errors, isWelsh,
                  allowedSeqCountries(countries(isWelsh)))(req, messages, frontendAppConfig))
              }
            }
          },
          selection => {
            val selectedCountryCode = ViewHelper.decodeCountryCode(selection.countryCode)
            val updatedJourney = journeyData.copy(countryCode = Some(selectedCountryCode), selectedAddress = None)

            val country = Countries.find(selectedCountryCode)
            if (country.isDefined) {
              (Some(updatedJourney), Redirect(routes.AbpAddressLookupController.lookup(id)))
            }
            else {
              val countryWithData = Countries.findCountryWithData(selectedCountryCode)
              if (countryWithData.isDefined) {
                (Some(updatedJourney), Redirect(routes.InternationalAddressLookupController.lookup(id, None)))
              }
              else {
                (Some(updatedJourney), Redirect(routes.InternationalAddressLookupController.edit(id)))
              }
            }
          }
        )

      }
  }

  //   GET /renewSession
  def renewSession: Action[AnyContent] =
    Action { _ =>
      Ok.sendFile(new File("conf/renewSession.jpg")).as("image/jpeg")
    }

  // GET /destroySession
  private val policy = new RelativeOrAbsoluteWithHostnameFromAllowlist(frontendAppConfig.allowedHosts, frontendAppConfig.environment)

  def destroySession(timeoutUrl: RedirectUrl): Action[AnyContent] = Action {
    _ => Redirect(policy.url(timeoutUrl)).withNewSession
  }
}

abstract class AlfController @Inject()(journeyRepository: JourneyRepository,
                                       messagesControllerComponents: MessagesControllerComponents
                                      )(implicit val ec: ExecutionContext)
  extends FrontendController(messagesControllerComponents) {

  protected def allowedCountries(countries: Seq[Country], countryCodesOpt: Option[Set[String]]): Seq[Country] = {
    countryCodesOpt match {
      case None => countries
      case Some(countryCodes) =>
        countries filter (c => countryCodes.contains(c.code))
    }
  }

  def getWelshContent(journeyData: JourneyDataV2)(implicit request: Request[_]): Boolean = {
    journeyData.welshEnabled && request.cookies.exists(
      kv => kv.name == "PLAY_LANG" && kv.value == "cy"
    )
  }

  def requestWithWelshHeader(useWelsh: Boolean)(req: => Result): Result = {
    req.withCookies(
      Cookie(
        name = ALFCookieNames.useWelsh,
        value = useWelsh.toString,
        httpOnly = false,
        sameSite = Some(Cookie.SameSite.Lax)
      )
    )
  }

  protected def urlWithQuery(url: String, appendQuery: String): URI = {
    val uri = new URI(url)
    val queryString = uri.getQuery match {
      case null => appendQuery
      case qs => s"$qs&$appendQuery"
    }

    new URI(uri.getScheme, uri.getAuthority, uri.getPath, queryString, uri.getFragment)
  }

  protected def withJourneyV2(id: String, noJourney: Result = Redirect(routes.AddressLookupController.noJourney()))
                             (action: JourneyDataV2 => (Option[JourneyDataV2], Result))
                             (implicit request: Request[AnyContent]): Future[Result] = {
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

  protected def withFutureJourneyV2(id: String,
                                    noJourney: Result = Redirect(
                                      routes.AddressLookupController.noJourney()
                                    ))(
                                     action: JourneyDataV2 => Future[(Option[JourneyDataV2], Result)]
                                   )(implicit request: Request[AnyContent]): Future[Result] = {

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
        props.map { addr => {
          (addr.addressId, addr.toDescription)
        }
        }.sorted
      }
      .getOrElse(Seq.empty)
  }
}

case class Confirmed(id: String)
