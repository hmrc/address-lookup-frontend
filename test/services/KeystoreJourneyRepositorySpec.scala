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

package services

import config.FrontendAppConfig
import model._
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.{JsValue, Json, Reads, Writes}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.cache.client.{CacheMap, HttpCaching}
import uk.gov.hmrc.http.HttpClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class KeystoreJourneyRepositorySpec extends PlaySpec with GuiceOneAppPerSuite with ScalaFutures {

  implicit val hc = HeaderCarrier()

  val journeyDataV2 = JourneyDataV2(JourneyConfigV2(2, JourneyOptions("continue")))
  val someJourneyDataV2Json = Some(Json.toJson(journeyDataV2))
  val cachedV2 = CacheMap("id", Map("id" -> Json.toJson(journeyDataV2)))

  class Scenario(cacheResponse: Option[CacheMap] = None, getResponse: Option[JsValue] = None, useNewCache: Boolean = true, useOldCache: Boolean = false) {

    val sessionCache = new HttpCaching {

      override def cache[A](source: String, cacheId: String, formId: String, body: A)(implicit wts: Writes[A], hc: HeaderCarrier, ec: ExecutionContext): Future[CacheMap] = {
        cacheResponse match {
          case Some(resp) => Future.successful(resp)
          case None => Future.failed(new Exception("Caching failed"))
        }
      }

      override def fetchAndGetEntry[T](source: String, cacheId: String, key: String)(implicit hc: HeaderCarrier, rds: Reads[T], ec: ExecutionContext): Future[Option[T]] = {
        val newCache: Map[String, Option[JsValue]] = if (useNewCache) Map(s"$key-$cacheId" -> getResponse) else Map()
        val oldCache: Map[String, Option[JsValue]] = if (useOldCache) Map(s"$cacheId-$key" -> getResponse) else Map()
        newCache.get(s"$key-$cacheId").flatten match {
          case Some(resp) => Future.successful(Some(resp.as[T]))
          case None => oldCache.get(s"$cacheId-$key").flatten match {
            case Some(resp) => Future.successful(Some(resp.as[T]))
            case None => Future.successful(None)
          }
        }
      }

      override def baseUri = "http://localhost:9000/keystore"

      override def domain = "keystore"

      override def defaultSource = "address-lookup-frontend"

      override def http = app.injector.instanceOf[HttpClient]

    }

    val frontendConfig = app.injector.instanceOf[FrontendAppConfig]

    val repo = new KeystoreJourneyRepository(sessionCache, frontendConfig)
  }

  "getV2" should {

    "fetch entry as a v2 model" when {
      "stored as a v2 model" in new Scenario(getResponse = someJourneyDataV2Json) {
        repo.getV2("any id").futureValue must be(Some(journeyDataV2))
      }

      "stored as a v2 model from New Cache" in new Scenario(getResponse = someJourneyDataV2Json, useNewCache = true, useOldCache = false) {
        repo.getV2("any id").futureValue must be(Some(journeyDataV2))
      }

      "stored as a v2 model from Old Cache" in new Scenario(getResponse = someJourneyDataV2Json, useNewCache = false, useOldCache = true) {
        repo.getV2("any id").futureValue must be(Some(journeyDataV2))
      }
    }

    "fetch v2 model entry as None" when {
      "when both New Cache and Old Cache are empty" in new Scenario(getResponse = someJourneyDataV2Json, useNewCache = false, useOldCache = false) {
        repo.getV2("any id").futureValue must be(None)
      }
    }

  }

  "putV2" should {
    "cache given entry" in new Scenario(cacheResponse = Some(cachedV2)) {
      repo.putV2("id", journeyDataV2).futureValue must be(true)
    }
  }
}
