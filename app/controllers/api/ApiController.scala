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

package controllers.api

import config.FrontendAppConfig
import controllers.AlfController
import forms.ALFForms
import javax.inject.{Inject, Singleton}
import model.JourneyData._
import model._
import play.api.i18n.{Langs, Messages, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, MessagesControllerComponents}
import play.mvc.Http.HeaderNames
import services.{IdGenerationService, JourneyRepository}
import utils.V2ModelConverter

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ApiController @Inject()(journeyRepository: JourneyRepository,
                              idGenerationService: IdGenerationService,
                              config: FrontendAppConfig,
                              converter: V2ModelConverter,
                              controllerComponents: MessagesControllerComponents)
                             (override implicit val ec: ExecutionContext, override implicit val messages: Messages, implicit val lang: Langs)
  extends AlfController(journeyRepository, controllerComponents) {

  val addressLookupEndpoint = config.addressLookupEndpoint
  protected def uuid: String = idGenerationService.uuid

  private implicit val initFormat = Json.format[Init]

  // POST /init
  def initWithConfig = Action.async(parse.json[JourneyConfig]) { implicit req =>
    import converter.V2ModelConverter
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