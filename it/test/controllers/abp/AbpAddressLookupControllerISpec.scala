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

package controllers.abp

import address.v2.Country
import controllers.api.{ConfirmedResponseAddress, ConfirmedResponseAddressDetails}
import itutil.IntegrationSpecBase
import itutil.config.IntegrationTestConstants._
import itutil.config.PageElementConstants._
import play.api.http.HeaderNames
import play.api.http.Status._
import play.api.libs.json.Json
import services.JourneyDataV2Cache
import uk.gov.hmrc.http.HeaderCarrier

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global

class AbpAddressLookupControllerISpec extends IntegrationSpecBase {
  val cache = app.injector.instanceOf[JourneyDataV2Cache]
  implicit val hc: HeaderCarrier = HeaderCarrier()

  "The lookup page" should {
    "pre-pop the postcode and filter on the view when they are passed in as query parameters and drop selected address on load" in {
      val testJourneyId = UUID.randomUUID().toString
      await(cache.putV2(testJourneyId, testMinimalLevelJourneyDataV2))

      val fResponse = buildClientLookupAddress(path = "lookup?postcode=AB11+1AB&filter=bar", testJourneyId)
        .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
        .get()
      val res = await(fResponse)

      res.status shouldBe OK
      testFormElementValuesMatch(res, Map(LookupPage.postcodeId -> testPostCode, LookupPage.filterId -> testFilterValue))
    }

    "pre-pop the postcode only on the view when it is passed in as a query parameters" in {
      val testJourneyId = UUID.randomUUID().toString
      await(cache.putV2(testJourneyId, testMinimalLevelJourneyDataV2))

      val fResponse = buildClientLookupAddress(path = "lookup?postcode=AB11 1AB", testJourneyId)
        .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
        .get()
      val res = await(fResponse)

      res.status shouldBe OK
      testFormElementValuesMatch(res, Map(LookupPage.postcodeId -> testPostCode, LookupPage.filterId -> ""))
    }

    "pre-pop the filter only on the view when it is passed in as a query parameters" in {
      val testJourneyId = UUID.randomUUID().toString
      await(cache.putV2(testJourneyId, testMinimalLevelJourneyDataV2))

      val fResponse = buildClientLookupAddress(path = "lookup?filter=bar", testJourneyId)
        .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
        .get()
      val res = await(fResponse)

      res.status shouldBe OK
      testFormElementValuesMatch(res, Map(LookupPage.postcodeId -> "", LookupPage.filterId -> testFilterValue))
    }

    "not pre-pop the filter or postcode fields when no query parameters are used " in {
      val testJourneyId = UUID.randomUUID().toString
      await(cache.putV2(testJourneyId, testMinimalLevelJourneyDataV2))

      val fResponse = buildClientLookupAddress(path = "lookup", testJourneyId)
        .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
        .get()
      val res = await(fResponse)

      res.status shouldBe OK
      testFormElementValuesMatch(res, Map(LookupPage.postcodeId -> "", LookupPage.filterId -> ""))
    }
  }

  "confirmed" should {
    "return correct address with jid" in {
      val testJourneyId = UUID.randomUUID().toString
      val configWithConfirmedAddress = testJourneyDataWithMinimalJourneyConfigV2.copy(confirmedAddress = Some(testFullNonUKConfirmedAddress(testJourneyId)))
      await(cache.putV2(testJourneyId, configWithConfirmedAddress))

      val fResponse = buildClientAPI(s"v2/confirmed?id=$testJourneyId")
        .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
        .get()

      val res = await(fResponse)

      res.status shouldBe OK
      res.json shouldBe Json.toJson(ConfirmedResponseAddress(
        auditRef = testJourneyId,
        id = Some(testAddressIdRaw),
        address = ConfirmedResponseAddressDetails(None, Some(Seq(testAddressLine1, testAddressLine2, testAddressLine3, testAddressTown)), Some(testPostCode), Some(Country("FR", "France"))))
      )
    }
  }
}
