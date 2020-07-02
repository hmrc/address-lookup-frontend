package controllers.testonly

import controllers.api.ApiController
import javax.inject.{Inject, Singleton}
import model.JourneyData._
import model._
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent, Request}
import play.mvc.Http.HeaderNames
import services.JourneyRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

import scala.concurrent.{ExecutionContext, Future}

object TestSetupForm {
  val form = Form(single(
    "journeyConfig" -> text
  ))
}

object StubHelper {
  val regexPatternForId = """(?<=lookup-address\/)(.*)(?=/lookup)""".r
  val getJourneyIDFromURL = (url:String) => regexPatternForId.findFirstIn(url).getOrElse(throw new Exception("id not in url"))

  def changeContinueUrlFromUserInputToStubV2(journeyconfigV2: JourneyConfigV2, id: String): JourneyConfigV2 =
    journeyconfigV2.copy(
      options = journeyconfigV2.options.copy(
        continueUrl = controllers.testonly.routes.StubController.showResultOfJourney(id).url)
    )

  val defaultJourneyConfigV2JsonAsString: JsValue =
    Json.toJson(JourneyConfigV2(
      version = 2,
      options = JourneyOptions(
        continueUrl = "This will be ignored"
      ),
      labels = Some(JourneyLabels()))
    )
}

@Singleton
class StubController @Inject()(apiController: ApiController,journeyRepository: JourneyRepository)(implicit val ec: ExecutionContext, implicit val messagesApi: MessagesApi) extends FrontendController with I18nSupport
    with FrontendServicesConfig {

  def showResultOfJourney(id: String): Action[AnyContent] = Action.async { implicit request =>
    journeyRepository.getV2(id).map { j =>
      Ok(Json.prettyPrint(Json.toJson(j.get))) }
  }

  def resolvedFormWithJourneyConfig =  {
    val jConfigDefaults = JourneyConfig(continueUrl = "will be ignored")

    TestSetupForm.form.fill(
      Json.prettyPrint(
        Json.toJson(
          ResolvedJourneyConfig(
            jConfigDefaults, JourneyConfigDefaults.EnglishConstants(jConfigDefaults.ukMode.contains(true))).cfg)
      ).toString)
  }

  def showStubPageForJourneyInit = Action { implicit request =>
    Ok(views.html.testonly.setup_journey_stub_page(resolvedFormWithJourneyConfig))
  }

  def showStubPageForJourneyInitV2: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.testonly.setup_journey_v2_stub_page(TestSetupForm.form.fill(
      Json.prettyPrint(
          StubHelper.defaultJourneyConfigV2JsonAsString
      ))))
  }
  def submitStubForNewJourneyV2 = Action.async { implicit request =>
    TestSetupForm.form.bindFromRequest().fold(
      errors => {
        Future.successful(BadRequest(views.html.testonly.setup_journey_v2_stub_page(errors)))},
      valid => {
        val jConfigV2 = Json.parse(valid).as[JourneyConfigV2]
        val reqForInit: Request[JourneyConfigV2] = request.map(_ => jConfigV2)

        apiController.initWithConfigV2()(reqForInit).flatMap { resOfInit => {

          val redirectLocation = resOfInit.header.headers(HeaderNames.LOCATION)
          val id = StubHelper.getJourneyIDFromURL(redirectLocation)
          val updatedJConfigWIthNewContinueUrl = StubHelper.changeContinueUrlFromUserInputToStubV2(jConfigV2,id)

          journeyRepository.putV2(id,JourneyDataV2(updatedJConfigWIthNewContinueUrl))
            .map(_ => Redirect(redirectLocation))
        }
       }
      })
  }

  def submitStubForNewJourney = Action.async{ implicit request =>
    TestSetupForm.form.bindFromRequest().fold(
      errors => Future.successful(BadRequest(views.html.testonly.setup_journey_stub_page(errors))),
      valid => {
        val jConfig = Json.parse(valid).as[JourneyConfig]
        val req = request.map(_ => jConfig)
        apiController.initWithConfig()(req).flatMap { res => {
          val id = StubHelper.getJourneyIDFromURL(res.header.headers(HeaderNames.LOCATION))
          for {
            jd <- journeyRepository.getV2(id)
            _ <- journeyRepository.putV2(id,
              jd.get.copy(
                config = jd.get.config.copy(
                  options = jd.get.config.options.copy(
                    continueUrl = controllers.testonly.routes.StubController.showResultOfJourney(id).url)
                  )
                )
              )
          } yield Redirect(res.header.headers(HeaderNames.LOCATION))
        }
        }
      }
    )
  }
}