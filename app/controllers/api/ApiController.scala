package controllers.api

import java.util.UUID
import javax.inject.{Inject, Singleton}

import controllers.{AlfController, Confirmed, Init}
import play.api.data.Form
import play.api.data.Forms.{mapping, text}
import play.api.i18n.MessagesApi
import play.api.libs.json.Json
import play.api.mvc.Action
import play.mvc.Http.HeaderNames
import services.JourneyRepository

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by christopher on 04/04/17.
  */
@Singleton
class ApiController @Inject()(journeyRepository: JourneyRepository)
                             (override implicit val ec: ExecutionContext, override implicit val messagesApi: MessagesApi)
  extends AlfController(journeyRepository) {

  val addressLookupEndpoint = baseUrl("address-lookup-frontend")

  protected def uuid: String = UUID.randomUUID().toString

  private implicit val initFormat = Json.format[Init]

  val confirmedForm = Form(
    mapping(
      "id" -> text(1, 255)
    )(Confirmed.apply)(Confirmed.unapply)
  )

  // GET  /init/:journeyName
  // initialize a new journey and return the "on ramp" URL
  def init(journeyName: String) = Action.async(parse.json[Init]) { implicit req =>
    val id = uuid
    try {
      // TODO init should do put, too?
      val journey = journeyRepository.init(journeyName)
      val j = req.body.continueUrl match {
        case Some(url) => journey.copy(continueUrl = url)
        case None => journey
      }
      journeyRepository.put(id, j).map(success => Accepted.withHeaders(HeaderNames.LOCATION -> s"$addressLookupEndpoint/lookup-address/$id/lookup"))
    } catch {
      case e: IllegalArgumentException => Future.successful(NotFound(e.getMessage))
    }
  }

  // GET  /confirmed?id=:id
  def confirmed = Action.async { implicit req =>
    confirmedForm.bindFromRequest().fold(
      errors => Future.successful(BadRequest),
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
