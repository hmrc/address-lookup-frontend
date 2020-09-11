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

package services

import com.google.inject.ImplementedBy
import config.FrontendAppConfig
import javax.inject.{Inject, Singleton}
import model._
import play.api.libs.json.{JsValue, Reads, Writes}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.cache.client.HttpCaching

import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[KeystoreJourneyRepository])
trait JourneyRepository {
  def getV2(sessionId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[JourneyDataV2]]

  def putV2(sessionId: String, data: JourneyDataV2)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Boolean]
}

@Singleton
class KeystoreJourneyRepository @Inject()(cache: HttpCaching,
                                          frontendAppConfig: FrontendAppConfig) extends JourneyRepository {
  val keyId = "journey-data"

  override def getV2(sessionId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[JourneyDataV2]] = {
    fetchCache[JsValue](sessionId).map(_.map(json =>
      (json \ "config" \ "version").asOpt[Int] match {
        case Some(_) => json.as[JourneyDataV2]
        case None => throw new IllegalStateException("V1 is no longer supported")
      }
    ))
  }

  private def fetchCache[A](sessionId: String)(implicit reads: Reads[A], hc: HeaderCarrier, ec: ExecutionContext): Future[Option[A]] = {
    for {
      newCachedDoc <- cache.fetchAndGetEntry[A](cache.defaultSource, sessionId, keyId)
      cachedDoc <- if (newCachedDoc.isDefined) Future.successful(newCachedDoc) else cache.fetchAndGetEntry[A](cache.defaultSource, keyId, sessionId)
    } yield cachedDoc
  }

  override def putV2(sessionId: String, data: JourneyDataV2)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Boolean] = {
    updateCache(sessionId, data)
  }

  private def updateCache[A](sessionId: String, data: A)(implicit wts: Writes[A], hc: HeaderCarrier, ec: ExecutionContext): Future[Boolean] = {
    cache.cache(cache.defaultSource, sessionId, keyId, data) map (_ => true)
  }
}
