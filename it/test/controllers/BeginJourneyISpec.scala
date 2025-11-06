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

package controllers

import itutil.IntegrationSpecBase
import itutil.config.IntegrationTestConstants.{journeyDataV2MinimalUkMode, testJourneyDataWithMinimalJourneyConfigV2}
import play.api.http.HeaderNames
import play.api.http.Status.SEE_OTHER
import play.api.libs.ws.WSResponse
import services.JourneyDataV2Cache
import uk.gov.hmrc.http.HeaderCarrier

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global

class BeginJourneyISpec extends IntegrationSpecBase {
  val cache = app.injector.instanceOf[JourneyDataV2Cache]
  implicit val hc: HeaderCarrier = HeaderCarrier()

  "The begin journey endpoint" when {
    "in uk mode" should {
      "redirect to the lookup page" in {
        val testJourneyId = UUID.randomUUID().toString
        await(cache.putV2(testJourneyId, journeyDataV2MinimalUkMode))

        val fResponse = buildClientLookupAddress(path = s"begin", testJourneyId)
          .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()

        val res: WSResponse = await(fResponse)
        res.status.shouldBe(SEE_OTHER)

        res.header(HeaderNames.LOCATION).get.shouldBe(s"/lookup-address/$testJourneyId/lookup")
      }
    }

    "in non-uk mode" should {
      "redirect to the country picker page" in {
        val testJourneyId = UUID.randomUUID().toString
        await(cache.putV2(testJourneyId, testJourneyDataWithMinimalJourneyConfigV2))

        val fResponse = buildClientLookupAddress(path = s"begin", testJourneyId)
          .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()

        val res: WSResponse = await(fResponse)
        res.status.shouldBe(SEE_OTHER)

        res.header(HeaderNames.LOCATION).get.shouldBe(s"/lookup-address/$testJourneyId/country-picker")
      }
    }
  }
}
