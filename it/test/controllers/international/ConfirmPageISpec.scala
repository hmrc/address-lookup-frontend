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
import itutil.config.IntegrationTestConstants._
import model._
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.http.Status._
import play.api.i18n.Lang
import services.JourneyDataV2Cache
import uk.gov.hmrc.http.HeaderCarrier

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global

class ConfirmPageISpec extends IntegrationSpecBase {
  val cache = app.injector.instanceOf[JourneyDataV2Cache]
  implicit val hc: HeaderCarrier = HeaderCarrier()

  "The confirm page GET" should {
    "pre-pop with an address and all elements are correct for an empty journey config model" in {
      val testJourneyId = UUID.randomUUID().toString
      val json = journeyDataV2WithSelectedAddress(
        testJourneyId,
        JourneyConfigV2(2, JourneyOptions(continueUrl = testContinueUrl)))

      await(cache.putV2(testJourneyId, json))

      val fResponse = buildClientLookupAddress(path = "international/confirm", testJourneyId)
        .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
        .get()

      val res = await(fResponse)
      val doc = getDocFromResponse(fResponse)

      doc.select("a[class=govuk-back-link]") should have(text("Back"))
      doc.title shouldBe messages("confirmPage.title")
      doc.h1.text() shouldBe messages("confirmPage.heading")
      doc.submitButton.text() shouldBe "Confirm address"
      doc.link("changeLink") should have(text(messages("confirmPage.changeLinkText")))
      doc.address should have(
        addressLine("line1", "1 High Street"),
        addressLine("line2", "Line 2"),
        addressLine("line3", "Line 3"),
        addressLine("line4", "Telford"),
        addressLine("postCode", "AB11 1AB"),
        addressLine("country", "France"))

      doc.paras should not have elementWithValue(
        "This is how your address will look. Please double-check it and, if accurate, click on the Confirm button.")
      doc.h2s should not have elementWithValue("Your selected address")

      testElementDoesntExist(res, "searchAgainLink")
      testElementDoesntExist(res, "confirmChangeText")

      testCustomPartsOfGovWrapperElementsForDefaultConfig(fResponse)
      res.status shouldBe OK
    }

    "redirect to the international lookup page if no selected address exists in keystore" in {
      val testJourneyId = UUID.randomUUID().toString
      await(cache.putV2(testJourneyId, testJourneyDataWithMinimalJourneyConfigV2))

      val fResponse = buildClientLookupAddress(path = "international/confirm", testJourneyId)
        .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
        .get()

      val res = await(fResponse)
      res.status shouldBe SEE_OTHER
      res.header(HeaderNames.LOCATION).get shouldBe s"/lookup-address/$testJourneyId/international/lookup"
    }

    "pre-pop with an address and all elements are correct for FULL journey config model with all booleans as TRUE for page" in {
      val testJourneyId = UUID.randomUUID().toString
      val json = journeyDataV2WithSelectedAddress(
        testJourneyId,
        fullDefaultJourneyConfigModelV2WithAllBooleansSet(true)
      )

      await(cache.putV2(testJourneyId, json))

      val fResponse = buildClientLookupAddress(path = "international/confirm", testJourneyId)
        .withHttpHeaders(
          HeaderNames.COOKIE -> sessionCookieWithCSRF,
          "Csrf-Token" -> "nocheck")
        .get()

      val res = await(fResponse)
      val doc = getDocFromResponse(fResponse)

      doc.select("a[class=govuk-back-link]") should have(text("Back"))
      doc.title shouldBe "international-confirm-title - NAV_TITLE - GOV.UK"
      doc.h1.text() shouldBe "international-confirm-heading"
      doc.submitButton.text() shouldBe "international-confirm-submitLabel"
      doc.address should have(
        addressLine("line1", "1 High Street"),
        addressLine("line2", "Line 2"),
        addressLine("line3", "Line 3"),
        addressLine("line4", "Telford"),
        addressLine("postCode", "AB11 1AB"),
        addressLine("country", "France")
      )

      doc.link("changeLink") should have(text("international-confirm-changeLinkText"))
      doc.h2s should have(elementWithValue("international-confirm-infoSubheading"))
      doc.paras should have(elementWithValue("international-confirm-infoMessage"))
      doc.link("searchAgainLink") should have(
        text("international-confirm-searchAgainLinkText")
      )

      doc.link("changeLink") should have(text("international-confirm-changeLinkText"))

      testCustomPartsOfGovWrapperElementsForFullConfigAllTrue(
        fResponse,
        navTitle = "NAV_TITLE"
      )

      res.status shouldBe OK
    }

    "pre-pop with an address and all elements are correct for FULL journey config model with all booleans as FALSE for page" in {
      val testJourneyId = UUID.randomUUID().toString
      await(cache.putV2(
        testJourneyId,
        journeyDataV2WithSelectedAddress(
          testJourneyId,
          fullDefaultJourneyConfigModelV2WithAllBooleansSet(false)
        )
      ))

      val fResponse = buildClientLookupAddress(path = "international/confirm", testJourneyId)
        .withHttpHeaders(
          HeaderNames.COOKIE -> sessionCookieWithCSRF,
          "Csrf-Token" -> "nocheck")
        .get()

      val res = await(fResponse)
      val doc = getDocFromResponse(fResponse)

      doc.select("a[class=govuk-back-link]") should have(text("Back"))
      doc.title shouldBe "international-confirm-title"
      doc.h1.text() shouldBe "international-confirm-heading"
      doc.submitButton.text() shouldBe "international-confirm-submitLabel"
      doc.address should have(
        addressLine("line1", "1 High Street"),
        addressLine("line2", "Line 2"),
        addressLine("line3", "Line 3"),
        addressLine("line4", "Telford"),
        addressLine("postCode", "AB11 1AB"),
        addressLine("country", "France")
      )

      doc.paras should not have elementWithValue(
        "This is how your address will look. Please double-check it and, if accurate, click on the Confirm button."
      )

      doc.h2s should not have elementWithValue("Your selected address")
      testElementDoesntExist(res, "searchAgainLink")
      testElementDoesntExist(res, "confirmChangeText")

      testCustomPartsOfGovWrapperElementsForFullConfigWithAllTopConfigAsNoneAndAllBooleansFalse(
        fResponse
      )

      res.status shouldBe OK
    }

    "pre-pop with an address and all elements are correct for almost full journey config model (missing field in confirm page) with all booleans as FALSE for page" in {
      val testJourneyId = UUID.randomUUID().toString
      val jc = fullDefaultJourneyConfigModelV2WithAllBooleansSet(false)

      await(cache.putV2(
        testJourneyId,
        journeyDataV2WithSelectedAddress(
          testJourneyId,
          jc.copy(labels = journeyV2Labels(None))
        )))

      val fResponse = buildClientLookupAddress(path = "international/confirm", testJourneyId)
        .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
        .get()

      val res = await(fResponse)
      val doc = getDocFromResponse(fResponse)

      doc.select("a[class=govuk-back-link]") should have(text("Back"))
      doc.title shouldBe "international-confirm-title"
      doc.h1.text() shouldBe "Review and confirm"
      doc.submitButton.text() shouldBe "international-confirm-submitLabel"

      doc.address should have(
        addressLine("line1", "1 High Street"),
        addressLine("line2", "Line 2"),
        addressLine("line3", "Line 3"),
        addressLine("line4", "Telford"),
        addressLine("postCode", "AB11 1AB"),
        addressLine("country", "France"))

      doc.paras should not have elementWithValue(
        "This is how your address will look. Please double-check it and, if accurate, click on the Confirm button.")

      doc.h2s should not have elementWithValue("Your selected address")
      testElementDoesntExist(res, "searchAgainLink")
      testElementDoesntExist(res, "confirmChangeText")

      testCustomPartsOfGovWrapperElementsForFullConfigWithAllTopConfigAsNoneAndAllBooleansFalse(fResponse)
      res.status shouldBe OK
    }

    "pre-pop with an address and all elements are correct for a minimal Welsh journey config model" in {
      val testJourneyId = UUID.randomUUID().toString
      val json = journeyDataV2WithSelectedAddress(
        testJourneyId,
        JourneyConfigV2(
          version = 2,
          options = JourneyOptions(continueUrl = testContinueUrl),
          labels = Some(JourneyLabels(cy = Some(LanguageLabels())))))

      await(cache.putV2(testJourneyId, json))

      val fResponse = buildClientLookupAddress(path = "international/confirm", testJourneyId)
        .withHttpHeaders(HeaderNames.COOKIE -> (sessionCookieWithCSRF + ";PLAY_LANG=cy;"), "Csrf-Token" -> "nocheck")
        .get()

      val res = await(fResponse)
      val doc = getDocFromResponse(fResponse)

      doc.select("a[class=govuk-back-link]") should have(text(messages(Lang("cy"), "constants.back")))
      doc.title shouldBe messages(Lang("cy"), "international.confirmPage.title")
      doc.h1.text() shouldBe messages(Lang("cy"), "international.confirmPage.heading")
      doc.submitButton.text() shouldBe messages(Lang("cy"), "international.confirmPage.submitLabel")
      doc.link("changeLink") should have(text(messages(Lang("cy"), "international.confirmPage.changeLinkText")))

      doc.address should have(
        addressLine("line1", "1 High Street"),
        addressLine("line2", "Line 2"),
        addressLine("line3", "Line 3"),
        addressLine("line4", "Telford"),
        addressLine("postCode", "AB11 1AB"),
        addressLine("country", "France")
      )

      testElementDoesntExist(res, "searchAgainLink")
      testElementDoesntExist(res, "confirmChangeText")

      testCustomPartsOfGovWrapperElementsForDefaultConfig(fResponse)
      res.status shouldBe OK
    }

    "pre-pop with an address and all elements are correct for FULL Welsh journey config model with all booleans as FALSE for page" in {
      val testJourneyId = UUID.randomUUID().toString
      await(cache.putV2(testJourneyId, journeyDataV2WithSelectedAddress(
        testJourneyId,
        fullDefaultJourneyConfigModelV2WithAllBooleansSet(allBooleanSetAndAppropriateOptions = false, isWelsh = true))))

      val fResponse = buildClientLookupAddress(path = "international/confirm", testJourneyId)
        .withHttpHeaders(HeaderNames.COOKIE -> (sessionCookieWithCSRF + ";PLAY_LANG=cy;"), "Csrf-Token" -> "nocheck")
        .get()

      val res = await(fResponse)
      val doc = getDocFromResponse(fResponse)


      doc.select("a[class=govuk-back-link]") should have(text(messages(Lang("cy"), "constants.back")))
      doc.title shouldBe "cy-international-confirm-title"
      doc.h1.text() shouldBe "cy-international-confirm-heading"
      doc.submitButton.text() shouldBe "cy-international-confirm-submitLabel"
      doc.address should have(
        addressLine("line1", "1 High Street"),
        addressLine("line2", "Line 2"),
        addressLine("line3", "Line 3"),
        addressLine("line4", "Telford"),
        addressLine("postCode", "AB11 1AB"),
        addressLine("country", "France")
      )

      testElementDoesntExist(res, "searchAgainLink")
      testElementDoesntExist(res, "confirmChangeText")

      testCustomPartsOfGovWrapperElementsForFullConfigWithAllTopConfigAsNoneAndAllBooleansFalse(
        fResponse
      )

      res.status shouldBe OK
    }

    "allow the initialising service to override the header size" in {
      val testJourneyId = UUID.randomUUID().toString
      await(cache.putV2(testJourneyId, journeyDataV2WithSelectedAddress(
        testJourneyId,
        journeyConfigV2 = JourneyConfigV2(2, JourneyOptions(testContinueUrl, pageHeadingStyle = Some("govuk-heading-l"))))))

      val fResponse = buildClientLookupAddress(path = "international/confirm", testJourneyId)
        .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF,
          "Csrf-Token" -> "nocheck")
        .get()

      val res = await(fResponse)
      res.status shouldBe OK

      val document = Jsoup.parse(res.body)
      document.getElementById("pageHeading").classNames() should contain("govuk-heading-l")
    }
  }

  "The confirm page POST" should {
    "use the correct continue url when user clicks Confirm the address" in {
      val testJourneyId = UUID.randomUUID().toString
      await(cache.putV2(testJourneyId, testConfigWithAddressNotUkModeV2(testJourneyId).copy(
        confirmedAddress = Some(testFullNonUKConfirmedAddress(testJourneyId)))))

      val fResponse = buildClientLookupAddress(path = "international/confirm", testJourneyId)
        .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
        .post(Map("csrfToken" -> Seq("xxx-ignored-xxx")))

      val res = await(fResponse)
      res.status shouldBe SEE_OTHER
      res.header(HeaderNames.LOCATION).get shouldBe s"$testContinueUrl?id=$testJourneyId"
    }

    "should redirect to the confirm page if incorrect data in keystore" in {
      val testJourneyId = UUID.randomUUID().toString
      await(cache.putV2(testJourneyId, testJourneyDataWithMinimalJourneyConfigV2))

      val fResponse = buildClientLookupAddress(path = "international/confirm", testJourneyId)
        .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
        .post(Map("csrfToken" -> Seq("xxx-ignored-xxx")))

      val res = await(fResponse)
      res.status shouldBe SEE_OTHER
      res.header(HeaderNames.LOCATION).get shouldBe s"/lookup-address/$testJourneyId/international/confirm"
    }
  }
}
