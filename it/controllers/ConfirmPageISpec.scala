package controllers

import config.ALFCookieNames
import itutil.IntegrationSpecBase
import itutil.config.IntegrationTestConstants._
import play.api.i18n.Lang
import model._
import play.api.http.HeaderNames
import play.api.http.Status._
import play.api.libs.json.Json

class ConfirmPageISpec extends IntegrationSpecBase {

  "The confirm page GET" should {
    "pre-pop with an address and all elements are correct for an empty journey config model" in {

      val json = journeyDataV2WithSelectedAddressJson(
        JourneyConfigV2(2, JourneyOptions(continueUrl = testContinueUrl)))

      stubKeystore(testJourneyId, json, OK)

      val fResponse = buildClientLookupAddress(path = "confirm")
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

    "redirect to the lookup page if no selected address exists in keystore" in {
      stubKeystore(testJourneyId, testConfigDefaultAsJsonV2, OK)

      val fResponse = buildClientLookupAddress(path = "confirm")
        .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
        .get()

      val res = await(fResponse)
      res.status shouldBe SEE_OTHER
      res.header(HeaderNames.LOCATION).get shouldBe "/lookup-address/Jid123/lookup"
    }

    "pre-pop with an address and all elements are correct for FULL journey config model with all booleans as TRUE for page" in {
      val json = journeyDataV2WithSelectedAddressJson(
        fullDefaultJourneyConfigModelV2WithAllBooleansSet(true)
      )
      stubKeystore(testJourneyId, json, OK)

      val fResponse = buildClientLookupAddress(path = "confirm")
        .withHttpHeaders(
          HeaderNames.COOKIE -> sessionCookieWithCSRF,
          "Csrf-Token" -> "nocheck"
        )
        .get()
      val res = await(fResponse)
      val doc = getDocFromResponse(fResponse)

      doc.select("a[class=govuk-back-link]") should have(text("Back"))
      doc.title shouldBe "confirm-title - NAV_TITLE - GOV.UK"
      doc.h1.text() shouldBe "confirm-heading"
      doc.submitButton.text() shouldBe "confirm-submitLabel"
      doc.address should have(
        addressLine("line1", "1 High Street"),
        addressLine("line2", "Line 2"),
        addressLine("line3", "Line 3"),
        addressLine("line4", "Telford"),
        addressLine("postCode", "AB11 1AB"),
        addressLine("country", "France")
      )
      doc.link("changeLink") should have(text("confirm-changeLinkText"))
      doc.h2s should have(elementWithValue("confirm-infoSubheading"))
      doc.paras should have(elementWithValue("confirm-infoMessage"))
      doc.link("searchAgainLink") should have(
        text("confirm-searchAgainLinkText")
      )
      doc.link("changeLink") should have(text("confirm-changeLinkText"))

      testCustomPartsOfGovWrapperElementsForFullConfigAllTrue(
        fResponse,
        navTitle = "NAV_TITLE"
      )
      res.status shouldBe OK
    }

    "pre-pop with an address and all elements are correct for FULL journey config model with all booleans as FALSE for page" in {
      stubKeystore(
        testJourneyId,
        journeyDataV2WithSelectedAddressJson(
          fullDefaultJourneyConfigModelV2WithAllBooleansSet(false)
        ),
        OK
      )

      val fResponse = buildClientLookupAddress(path = "confirm")
        .withHttpHeaders(
          HeaderNames.COOKIE -> sessionCookieWithCSRF,
          "Csrf-Token" -> "nocheck"
        )
        .get()
      val res = await(fResponse)
      val doc = getDocFromResponse(fResponse)

      doc.select("a[class=govuk-back-link]") should have(text("Back"))
      doc.title shouldBe "confirm-title"
      doc.h1.text() shouldBe "confirm-heading"
      doc.submitButton.text() shouldBe "confirm-submitLabel"
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
      val jc = fullDefaultJourneyConfigModelV2WithAllBooleansSet(false)

      stubKeystore(
        testJourneyId,
        journeyDataV2WithSelectedAddressJson(
          jc.copy(labels = journeyV2Labels(None))
        ),
        OK
      )

      val fResponse = buildClientLookupAddress(path = "confirm")
        .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
        .get()

      val res = await(fResponse)
      val doc = getDocFromResponse(fResponse)

      doc.select("a[class=govuk-back-link]") should have(text("Back"))
      doc.title shouldBe "confirm-title"
      doc.h1.text() shouldBe "Review and confirm"
      doc.submitButton.text() shouldBe "confirm-submitLabel"
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
      val json = journeyDataV2WithSelectedAddressJson(
        JourneyConfigV2(
          version = 2,
          options = JourneyOptions(continueUrl = testContinueUrl),
          labels = Some(JourneyLabels(cy = Some(LanguageLabels())))))

      stubKeystore(testJourneyId, json, OK)

      val fResponse = buildClientLookupAddress(path = "confirm")
        .withHttpHeaders(HeaderNames.COOKIE -> (sessionCookieWithCSRF + ";PLAY_LANG=cy;"), "Csrf-Token" -> "nocheck")
        .get()

      val res = await(fResponse)
      val doc = getDocFromResponse(fResponse)

      doc.select("a[class=govuk-back-link]") should have(text(messages(Lang("cy"), "constants.back")))
      doc.title shouldBe messages(Lang("cy"), "confirmPage.title")
      doc.h1.text() shouldBe messages(Lang("cy"), "confirmPage.heading")
      doc.submitButton.text() shouldBe messages(Lang("cy"), "confirmPage.submitLabel")
      doc.link("changeLink") should have(text(messages(Lang("cy"), "confirmPage.changeLinkText")))

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
      stubKeystore(testJourneyId, journeyDataV2WithSelectedAddressJson(
        fullDefaultJourneyConfigModelV2WithAllBooleansSet(allBooleanSetAndAppropriateOptions = false, isWelsh = true)),
        OK)

      val fResponse = buildClientLookupAddress(path = "confirm")
        .withHttpHeaders(HeaderNames.COOKIE -> (sessionCookieWithCSRF + ";PLAY_LANG=cy;"), "Csrf-Token" -> "nocheck")
        .get()

      val res = await(fResponse)
      val doc = getDocFromResponse(fResponse)


      doc.select("a[class=govuk-back-link]") should have(text(messages(Lang("cy"), "constants.back")))
      doc.title shouldBe "cy-confirm-title"
      doc.h1.text() shouldBe "cy-confirm-heading"
      doc.submitButton.text() shouldBe "cy-confirm-submitLabel"
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
  }

  "The confirm page POST" should {
    "use the correct continue url when user clicks Confirm the address" in {
      stubKeystore(testJourneyId, testConfigwithAddressNotUkModeAsJsonV2, OK)
      stubKeystoreSave(testJourneyId, Json.toJson(
        testConfigWithAddressNotUkModeV2.copy(confirmedAddress = Some(testFullNonUKConfirmedAddress))), OK)

      val fResponse = buildClientLookupAddress(path = "confirm")
        .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
        .post(Map("csrfToken" -> Seq("xxx-ignored-xxx")))

      val res = await(fResponse)
      res.status shouldBe SEE_OTHER
      res.header(HeaderNames.LOCATION).get shouldBe s"$testContinueUrl?id=$testJourneyId"
    }

    "should redirect to the confirm page if incorrect data in keystore" in {
      stubKeystore(testJourneyId, testConfigDefaultAsJsonV2, OK)

      val fResponse = buildClientLookupAddress(path = "confirm")
        .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
        .post(Map("csrfToken" -> Seq("xxx-ignored-xxx")))

      val res = await(fResponse)
      res.status shouldBe SEE_OTHER
      res.header(HeaderNames.LOCATION).get shouldBe "/lookup-address/Jid123/confirm"
    }
  }

  "technical difficulties" when {
    "the welsh content header isn't set and welsh object isn't provided in config" should {
      "render in English" in {
        stubKeystore(testJourneyId, testMinimalLevelJourneyConfigV2, INTERNAL_SERVER_ERROR)
        stubKeystoreSave(testJourneyId, testMinimalLevelJourneyConfigV2, INTERNAL_SERVER_ERROR)

        val fResponse = buildClientLookupAddress(s"confirm")
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
        stubKeystore(testJourneyId, testMinimalLevelJourneyConfigV2, INTERNAL_SERVER_ERROR)
        stubKeystoreSave(testJourneyId, testMinimalLevelJourneyConfigV2, INTERNAL_SERVER_ERROR)

        val fResponse = buildClientLookupAddress(s"confirm")
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
        val v2Config = Json.toJson(
          fullDefaultJourneyConfigModelV2WithAllBooleansSet(
            allBooleanSetAndAppropriateOptions = true,
            isWelsh = true))

        stubKeystore(testJourneyId, v2Config, INTERNAL_SERVER_ERROR)
        stubKeystoreSave(testJourneyId, v2Config, INTERNAL_SERVER_ERROR)

        val fResponse = buildClientLookupAddress(s"confirm")
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
        val v2Config = Json.toJson(
          fullDefaultJourneyConfigModelV2WithAllBooleansSet(
            allBooleanSetAndAppropriateOptions = true,
            isWelsh = true))

        stubKeystore(testJourneyId, v2Config, INTERNAL_SERVER_ERROR)
        stubKeystoreSave(testJourneyId, v2Config, INTERNAL_SERVER_ERROR)

        val fResponse = buildClientLookupAddress(s"confirm")
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
