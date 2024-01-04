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

package controllers.testonly

import config.FrontendAppConfig
import controllers.api.ApiController
import javax.inject.{Inject, Singleton}
import model._
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.I18nSupport
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request}
import play.mvc.Http.HeaderNames
import services.JourneyRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.testonly.setup_journey_v2_stub_page

import scala.concurrent.{ExecutionContext, Future}

object TestSetupForm {
  val form = Form(single("journeyConfig" -> text))
}

object StubHelper {
  val regexPatternForId = """(?<=lookup-address\/)(.*)(?=/begin)""".r
  val getJourneyIDFromURL = (url: String) =>
    regexPatternForId
      .findFirstIn(url)
      .getOrElse(throw new Exception("id not in url"))

  def changeContinueUrlFromUserInputToStubV2(journeyconfigV2: JourneyConfigV2,
                                             id: String): JourneyConfigV2 =
    journeyconfigV2.copy(
      options = journeyconfigV2.options.copy(continueUrl =
                controllers.testonly.routes.StubController.showResultOfJourney(id).url)
    )

  val defaultJourneyConfigV2JsonAsString: JsValue =
    Json.toJson(
      JourneyConfigV2(
        version = 2,
        options = JourneyOptions(continueUrl = "This will be ignored"),
        labels = Some(JourneyLabels())
      )
    )
}

@Singleton
class StubController @Inject()(
                                apiController: ApiController,
                                journeyRepository: JourneyRepository,
                                implicit val frontendAppConfig: FrontendAppConfig,
                                controllerComponents: MessagesControllerComponents,
                                setup_journey_v2_stub_page: setup_journey_v2_stub_page
                              )(implicit val ec: ExecutionContext)
  extends FrontendController(controllerComponents)
    with I18nSupport {

  def showResultOfJourney(id: String): Action[AnyContent] = Action.async {
    implicit request =>
      journeyRepository.getV2(id).map { j =>
        Ok(Json.prettyPrint(Json.toJson(j.get)))
      }
  }

  def showStubPageForJourneyInitV2: Action[AnyContent] = Action {
    implicit request =>
      Ok(setup_journey_v2_stub_page(TestSetupForm.form.fill(Json.prettyPrint(StubHelper.defaultJourneyConfigV2JsonAsString))))
  }

  def submitStubForNewJourneyV2 = Action.async { implicit request =>
    TestSetupForm.form
      .bindFromRequest()
      .fold(
        errors => {
          Future.successful(BadRequest(setup_journey_v2_stub_page(errors)))
        },
        valid => {
          val jConfigV2 = Json.parse(valid).as[JourneyConfigV2]
          val reqForInit: Request[JourneyConfigV2] = request.map(_ => jConfigV2)

          apiController.initWithConfigV2()(reqForInit).flatMap {
            resOfInit => {
              val redirectLocation = resOfInit.header.headers(HeaderNames.LOCATION)
              val id = StubHelper.getJourneyIDFromURL(redirectLocation)
              val updatedJConfigWIthNewContinueUrl = StubHelper.changeContinueUrlFromUserInputToStubV2(jConfigV2, id)

              journeyRepository
                .putV2(id, JourneyDataV2(updatedJConfigWIthNewContinueUrl))
                .map(_ => Redirect(redirectLocation))
            }
          }
        }
      )
  }
}
