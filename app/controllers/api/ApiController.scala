/*
 * Copyright 2021 HM Revenue & Customs
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
import model._
import play.api.libs.json.Json
import play.api.mvc.MessagesControllerComponents
import play.mvc.Http.HeaderNames
import services.{IdGenerationService, JourneyRepository}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ApiController @Inject()(journeyRepository: JourneyRepository,
                              idGenerationService: IdGenerationService,
                              config: FrontendAppConfig,
                              controllerComponents: MessagesControllerComponents)
                             (override implicit val ec: ExecutionContext)
  extends AlfController(journeyRepository, controllerComponents) {

  val addressLookupEndpoint = config.addressLookupEndpoint

  protected def uuid: String = idGenerationService.uuid

  def initWithConfigV2 = Action.async(parse.json[JourneyConfigV2]) { implicit req =>
    val id = uuid
    journeyRepository.putV2(id, JourneyDataV2(config = req.body))
      .map(_ => Accepted.withHeaders(HeaderNames.LOCATION -> s"$addressLookupEndpoint/lookup-address/$id/lookup"))
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