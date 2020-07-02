package controllers.api

import config.FrontendAppConfig
import controllers.AlfController
import forms.ALFForms
import javax.inject.{Inject, Singleton}
import model.JourneyData._
import model._
import play.api.i18n.MessagesApi
import play.api.libs.json.Json
import play.api.mvc.Action
import play.mvc.Http.HeaderNames
import services.{IdGenerationService, JourneyRepository}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ApiController @Inject()(journeyRepository: JourneyRepository, idGenerationService: IdGenerationService, config: FrontendAppConfig)
                             (override implicit val ec: ExecutionContext, override implicit val messagesApi: MessagesApi)
  extends AlfController(journeyRepository) {

  val addressLookupEndpoint = config.addressLookupEndpoint
  protected def uuid: String = idGenerationService.uuid

  private implicit val initFormat = Json.format[Init]

  // POST /init
  def initWithConfig = Action.async(parse.json[JourneyConfig]) { implicit req =>
    val id = uuid
    journeyRepository.putV2(id, JourneyData(req.body).toV2Model).map(success =>
      Accepted.withHeaders(HeaderNames.LOCATION -> s"$addressLookupEndpoint/lookup-address/$id/lookup")
    )
  }

  def initWithConfigV2 = Action.async(parse.json[JourneyConfigV2]) { implicit req =>
    val id = uuid
    journeyRepository.putV2(id, JourneyDataV2(config = req.body))
      .map(_ => Accepted.withHeaders(HeaderNames.LOCATION -> s"$addressLookupEndpoint/lookup-address/$id/lookup"))
  }

  // POST  /init/:journeyName
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

  def confirmedV2 = Action.async { implicit req =>
    ALFForms.confirmedForm.bindFromRequest().fold(
      _ => Future.successful(BadRequest),
      confirmed => {
        withJourneyV2(confirmed.id, NotFound) { journeyData =>
          journeyData.confirmedAddress match {
            case Some(confirmedAddresss) => (None, Ok(Json.toJson(confirmedAddresss)))
            case _ => (None, NotFound)
          }
        }
      }
    )
  }

}