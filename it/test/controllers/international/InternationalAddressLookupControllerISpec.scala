/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers.international

import itutil.IntegrationSpecBase
import itutil.config.IntegrationTestConstants.*
import itutil.config.PageElementConstants.*
import play.api.http.HeaderNames
import play.api.http.Status.*
import play.api.libs.ws.WSResponse
import services.JourneyDataV2Cache
import uk.gov.hmrc.http.HeaderCarrier

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global

class InternationalAddressLookupControllerISpec extends IntegrationSpecBase {
  val cache = app.injector.instanceOf[JourneyDataV2Cache]
  implicit val hc: HeaderCarrier = HeaderCarrier()

  "The lookup page" should {
    "pre-pop the filter only on the view when it is passed in as a query parameters" in {
      val testJourneyId = UUID.randomUUID().toString
      await(cache.putV2(testJourneyId, testMinimalLevelJourneyDataV2))

      val fResponse = buildClientLookupAddress(path = "international/lookup?filter=bar", testJourneyId)
        .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
        .get()
      val res: WSResponse = await(fResponse)

      res.status.shouldBe(OK)
      testFormElementValuesMatch(res, Map(LookupPage.filterId -> testFilterValue))
    }

    "not pre-pop the filter or postcode fields when no query parameters are used " in {
      val testJourneyId = UUID.randomUUID().toString
      await(cache.putV2(testJourneyId, testMinimalLevelJourneyDataV2))

      val fResponse = buildClientLookupAddress(path = "international/lookup", testJourneyId)
        .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
        .get()
      val res: WSResponse = await(fResponse)

      res.status.shouldBe(OK)
      testFormElementValuesMatch(res, Map(LookupPage.filterId -> ""))
    }
  }
}
