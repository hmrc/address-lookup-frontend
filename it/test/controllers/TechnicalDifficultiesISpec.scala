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

import com.codahale.metrics.SharedMetricRegistries
import config.ALFCookieNames
import itutil.IntegrationSpecBase
import itutil.config.IntegrationTestConstants.testMinimalLevelJourneyDataV2
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.Mode.Test
import play.api.http.HeaderNames
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.i18n.Lang
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.{Application, inject}
import services.{JourneyDataV2Cache, JourneyRepository}
import uk.gov.hmrc.http.HeaderCarrier

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global

class TechnicalDifficultiesISpec extends IntegrationSpecBase {
  // Deliberately avoid setting up the mock to cause an exception
  val mockJourneyRepository = mock[JourneyRepository]

  implicit override lazy val app: Application = {
    SharedMetricRegistries.clear()
    new GuiceApplicationBuilder()
      .overrides(inject.bind[JourneyRepository].toInstance(mockJourneyRepository))
      .configure(fakeConfig())
      .in(Test)
      .build()
  }

  val cache = app.injector.instanceOf[JourneyDataV2Cache]
  implicit val hc: HeaderCarrier = HeaderCarrier()

  "technical difficulties" when {
    "the welsh content header isn't set and welsh object isn't provided in config" should {
      "render in English" in {
        val testJourneyId = UUID.randomUUID().toString
        await(cache.putV2(testJourneyId, testMinimalLevelJourneyDataV2))

        val fResponse = buildClientLookupAddress(path = s"country-picker", testJourneyId)
          .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()

        val res = await(fResponse)
        res.status shouldBe INTERNAL_SERVER_ERROR
        res.cookie(ALFCookieNames.useWelsh) shouldBe None

        val doc = getDocFromResponse(res)
        doc.title shouldBe messages("constants.intServerErrorTitle")
        doc.h1 should have(text(messages("constants.intServerErrorTitle")))
        doc.paras should have(elementWithValue(messages("constants.intServerErrorTryAgain")))
      }
    }

    "the welsh content header is set to false and welsh object isn't provided in config" should {
      "render in English" in {
        val testJourneyId = UUID.randomUUID().toString
        await(cache.putV2(testJourneyId, testMinimalLevelJourneyDataV2))

        val fResponse = buildClientLookupAddress(s"country-picker", testJourneyId)
          .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithWelshCookie(useWelsh = false), "Csrf-Token" -> "nocheck")
          .get()

        val res = await(fResponse)
        res.status shouldBe INTERNAL_SERVER_ERROR
        res.cookie(ALFCookieNames.useWelsh) shouldBe None

        val doc = getDocFromResponse(res)
        doc.title shouldBe messages("constants.intServerErrorTitle")
        doc.h1 should have(text(messages("constants.intServerErrorTitle")))
        doc.paras should have(elementWithValue(messages("constants.intServerErrorTryAgain")))
      }
    }

    "the welsh content header is set to false and welsh object is provided in config" should {
      "render in English" in {
        val testJourneyId = UUID.randomUUID().toString
        await(cache.putV2(testJourneyId, testMinimalLevelJourneyDataV2))

        val fResponse = buildClientLookupAddress(s"country-picker", testJourneyId)
          .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithWelshCookie(useWelsh = false), "Csrf-Token" -> "nocheck")
          .get()

        val res = await(fResponse)
        res.status shouldBe INTERNAL_SERVER_ERROR

        val doc = getDocFromResponse(res)
        doc.title shouldBe messages("constants.intServerErrorTitle")
        doc.h1 should have(text(messages("constants.intServerErrorTitle")))
        doc.paras should have(elementWithValue(messages("constants.intServerErrorTryAgain")))
      }
    }

    "the welsh content header is set to true and welsh object provided in config" should {
      "render in Welsh" in {
        val testJourneyId = UUID.randomUUID().toString
        await(cache.putV2(testJourneyId, testMinimalLevelJourneyDataV2))

        val fResponse = buildClientLookupAddress(s"country-picker", testJourneyId)
          .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRFAndLang(), "Csrf-Token" -> "nocheck")
          .get()

        val res = await(fResponse)
        res.status shouldBe INTERNAL_SERVER_ERROR

        val doc = getDocFromResponse(res)
        doc.title shouldBe messages(Lang("cy"), "constants.intServerErrorTitle")
        doc.h1 should have(text(messages(Lang("cy"), "constants.intServerErrorTitle")))
        doc.paras should have(elementWithValue(messages(Lang("cy"), "constants.intServerErrorTryAgain")))
      }
    }
  }
}
