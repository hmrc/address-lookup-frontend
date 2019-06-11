package controllers.api

import controllers.AlfController
import forms.ALFForms
import javax.inject.{Inject, Singleton}
import model.JourneyData._
import model.{Init, JourneyConfig, JourneyData}
import play.api.i18n.MessagesApi
import play.api.libs.json.Json
import play.api.mvc.Action
import play.mvc.Http.HeaderNames
import services.{IdGenerationService, JourneyRepository}
import utils.V2ModelConverter._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ApiController @Inject()(journeyRepository: JourneyRepository, idGenerationService: IdGenerationService)
                             (override implicit val ec: ExecutionContext, override implicit val messagesApi: MessagesApi)
  extends AlfController(journeyRepository) {

  val addressLookupEndpoint = baseUrl("address-lookup-frontend")

  protected def uuid: String = idGenerationService.uuid

  private implicit val initFormat = Json.format[Init]

  // POST /init
  def initWithConfig = Action.async(parse.json[JourneyConfig]) { implicit req =>
    val id = uuid
    journeyRepository.putV2(id, JourneyData(req.body).toV2Model).map(success =>
      Accepted.withHeaders(HeaderNames.LOCATION -> s"$addressLookupEndpoint/lookup-address/$id/lookup")
    )
  }

  // GET  /init/:journeyName
  // initialize a new journey and return the "on ramp" URL
  def init(journeyName: String) = Action.async(parse.json[Init]) { implicit req =>
    val id = uuid
    try {
      // TODO init should do put, too?
      val journey = journeyRepository.init(journeyName)
      val j = req.body.continueUrl match {
        case Some(url) => journey.copy(config = journey.config.copy(continueUrl = url))
        case None => journey
      }
      journeyRepository.put(id, j).map(success => Accepted.withHeaders(HeaderNames.LOCATION -> s"$addressLookupEndpoint/lookup-address/$id/lookup"))
    } catch {
      case e: IllegalArgumentException => Future.successful(NotFound(e.getMessage))
    }
  }

  // GET  /confirmed?id=:id
  def confirmed = Action.async { implicit req =>
    ALFForms.confirmedForm.bindFromRequest().fold(
      _ => Future.successful(BadRequest),
      confirmed => {
        withJourney(confirmed.id, NotFound) { journeyData =>
          if (journeyData.confirmedAddress.isDefined) {
            (None, Ok(Json.toJson(journeyData.confirmedAddress.get)))
          } else {
            (None, NotFound)
          }
        }
      }
    )
  }

}