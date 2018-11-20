package controllers.testonly

import com.google.inject.ImplementedBy
import controllers.api.ApiController
import javax.inject.Inject
import model.JourneyData._
import model.{JourneyConfig, ResolvedJourneyConfig}
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent}
import play.mvc.Http.HeaderNames
import services.JourneyRepository
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.frontend.controller.FrontendController

import scala.concurrent.{ExecutionContext, Future}


class StubControllerImpl @Inject()(val apiController: ApiController,val  journeyRepository: JourneyRepository)(implicit val ec: ExecutionContext, implicit val messagesApi: MessagesApi) extends StubController {

}
object TestSetupForm {
  val form = Form(single(
    "journeyConfig" -> text
  ))
}
@ImplementedBy(classOf[StubControllerImpl])
trait StubController extends FrontendController with I18nSupport with ServicesConfig {
  val apiController: ApiController
  val journeyRepository: JourneyRepository

  def showResultOfJourney(id: String): Action[AnyContent] = Action.async { implicit request =>
    journeyRepository.get(id).map { j =>
      Ok(Json.prettyPrint(Json.toJson(j.get))) }
  }

  def resolvedFormWithJourneyConfig =  {
    val jConfigDefaults = JourneyConfig(continueUrl = "will be ignored")
    TestSetupForm.form.fill(Json.prettyPrint(Json.toJson(ResolvedJourneyConfig(jConfigDefaults).cfg)).toString)
  }

  def showStubPageForJourneyInit = Action { implicit request =>
    Ok(views.html.testonly.setup_journey_stub_page(resolvedFormWithJourneyConfig))
  }

  def submitStubForNewJourney = Action.async{ implicit request =>
    TestSetupForm.form.bindFromRequest().fold(
      errors => Future.successful(BadRequest(views.html.testonly.setup_journey_stub_page(errors))),
      valid => {
        val jConfig = Json.parse(valid).as[JourneyConfig]

        val req = request.map(_ => jConfig)
        apiController.initWithConfig()(req).flatMap { res =>
          val pattern = """(?<=lookup-address\/)(.*)(?=/lookup)""".r
          val id = pattern.findFirstIn(res.header.headers(HeaderNames.LOCATION)).getOrElse(throw new Exception("id not in url in header"))
          for {
            jd <- journeyRepository.get(id)
            _ <- journeyRepository.put(id, jd.get.copy(jConfig.copy(continueUrl = controllers.testonly.routes.StubController.showResultOfJourney(id).url)))
          } yield Redirect(res.header.headers(HeaderNames.LOCATION))

        }
      }
    )
  }
}