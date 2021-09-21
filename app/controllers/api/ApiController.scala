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
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.MessagesControllerComponents
import play.mvc.Http.HeaderNames
import services.{IdGenerationService, JourneyRepository}
import utils.RelativeOrAbsoluteWithHostnameFromAllowlist

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

@Singleton
class ApiController @Inject()(journeyRepository: JourneyRepository,
                              idGenerationService: IdGenerationService,
                              config: FrontendAppConfig,
                              controllerComponents: MessagesControllerComponents,
                              frontendAppConfig: FrontendAppConfig)
                             (override implicit val ec: ExecutionContext)
  extends AlfController(journeyRepository, controllerComponents) {

  val logger = Logger(this.getClass)
  val addressLookupEndpoint = config.addressLookupEndpoint

  protected def uuid: String = idGenerationService.uuid

  private val policy = new RelativeOrAbsoluteWithHostnameFromAllowlist(frontendAppConfig.allowedHosts)

  case class InitFailure(reason: String)
  object InitFailure {
    implicit val writes = Json.writes[InitFailure]
  }

  def initWithConfigV2 = Action.async(parse.json[JourneyConfigV2]) { implicit req =>
    val id = uuid

    val timeoutRedirectUrl = req.body.options.timeoutConfig.map(t => t.timeoutUrl)
    val timeoutKeepAliveUrl = req.body.options.timeoutConfig.flatMap(t => t.timeoutKeepAliveUrl)

    val redirectPolicyResult = timeoutRedirectUrl.map { url => Try { policy.url(url) } }
    val keepAlivePolicyResult = timeoutKeepAliveUrl.map { url => Try { policy.url(url) } }

    val userAgent = req.headers.get(USER_AGENT).getOrElse("unknown")

    redirectPolicyResult match {
      case Some(Failure(_)) =>
        logger.warn(s"Timeout redirect url from ${userAgent} is not valid according to the security policy: '${timeoutRedirectUrl}'")
    }

    keepAlivePolicyResult match {
      case Some(Failure(_)) =>
        logger.warn(s"Timeout keepalive url from ${userAgent} is not valid according to the security policy: '${timeoutKeepAliveUrl}'")
    }

//    policyResult match {
//      case Success(_) =>
    journeyRepository.putV2(id, JourneyDataV2(config = req.body))
      .map(_ => Accepted.withHeaders(HeaderNames.LOCATION -> s"$addressLookupEndpoint/lookup-address/$id/lookup"))
//      case _ =>
//        import InitFailure.writes
//        Future.successful(BadRequest(Json.toJson(InitFailure("Timeout config only allows relative urls"))))
//    }
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