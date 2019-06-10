package controllers

import itutil.IntegrationSpecBase
import itutil.config.IntegrationTestConstants._
import model.JourneyConfig
import play.api.http.HeaderNames
import play.api.http.Status._
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.test.FakeApplication
import model.JourneyConfigDefaults._

class AddressLookupConfirmPageISpec extends IntegrationSpecBase {

  override implicit lazy val app = FakeApplication(additionalConfiguration = fakeConfig())

  "The confirm page GET" should {
    "pre-pop with an address and all elements are correct for an empty journey config model" in {

      val json = journeyDataWithSelectedAddressJson(JourneyConfig(continueUrl = testContinueUrl), testFullNonUKAddress)
      stubKeystore(testJourneyId, json, OK)

      val fResponse = buildClientLookupAddress(path = "confirm")
        .withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
        .get()
      val res = await(fResponse)
      val doc = getDocFromResponse(fResponse)


      doc.select("a[class=back-link]") should have(
        text("Back")
      )
      doc.title shouldBe CONFIRM_PAGE_TITLE
      doc.h1.text() shouldBe CONFIRM_PAGE_HEADING
      doc.submitButton.text() shouldBe "Confirm and continue"
      doc.link("changeLink") should have(
        text(CONFIRM_PAGE_EDIT_LINK_TEXT)
      )
      doc.address should have(
        addressLine("line1", "1 High Street"),
        addressLine("line2", "Line 2"),
        addressLine("line3", "Line 3"),
        addressLine("line4", "Telford"),
        addressLine("postCode", "AB11 1AB"),
        addressLine("country", "France")
      )
      doc.paras should not have elementWithValue("This is how your address will look. Please double-check it and, if accurate, click on the Confirm button.")
      doc.h2s should not have elementWithValue("Your selected address")

      testElementDoesntExist(res, "searchAgainLink")
      testElementDoesntExist(res, "confirmChangeText")

      testCustomPartsOfGovWrapperElementsForDefaultConfig(fResponse)
      res.status shouldBe OK
    }
    "redirect to the lookup page if no selected address exists in keystore" in {
      stubKeystore(testJourneyId, testConfigDefaultAsJson, OK)

      val fResponse = buildClientLookupAddress(path = "confirm")
        .withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
        .get()
      val res = await(fResponse)
      res.status shouldBe SEE_OTHER
      res.header(HeaderNames.LOCATION).get shouldBe "/lookup-address/Jid123/lookup"
    }

    "pre-pop with an address and all elements are correct for FULL journey config model with all booleans as TRUE for page" in {
      val json = journeyDataWithSelectedAddressJson(fullDefaultJourneyConfigModelWithAllBooleansSet(true), testFullNonUKAddress)
      stubKeystore(testJourneyId, json, OK)

      val fResponse = buildClientLookupAddress(path = "confirm")
        .withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
        .get()
      val res = await(fResponse)
      val doc = getDocFromResponse(fResponse)

      doc.select("a[class=back-link]") should have(
        text("Back")
      )
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
      doc.link("changeLink") should have(
        text("confirm-changeLinkText")
      )
      doc.h2s should have(elementWithValue("confirm-infoSubheading"))
      doc.paras should have(elementWithValue("confirm-infoMessage"))
      doc.link("searchAgainLink") should have(
        text("confirm-searchAgainLinkText")
      )
      doc.link("changeLink") should have(
        text("confirm-changeLinkText")
      )

      testCustomPartsOfGovWrapperElementsForFullConfigAllTrue(fResponse, navTitle = "NAV_TITLE")
      res.status shouldBe OK
    }

    "pre-pop with an address and all elements are correct for FULL journey config model with all booleans as FALSE for page" in {
      stubKeystore(testJourneyId, journeyDataWithSelectedAddressJson(fullDefaultJourneyConfigModelWithAllBooleansSet(false), testFullNonUKAddress), OK)

      val fResponse = buildClientLookupAddress(path = "confirm")
        .withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
        .get()
      val res = await(fResponse)
      val doc = getDocFromResponse(fResponse)

      doc.select("a[class=back-link]") should have(
        text("Back")
      )
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
      doc.paras should not have elementWithValue("This is how your address will look. Please double-check it and, if accurate, click on the Confirm button.")
      doc.h2s should not have elementWithValue("Your selected address")
      testElementDoesntExist(res, "searchAgainLink")
      testElementDoesntExist(res, "confirmChangeText")

      testCustomPartsOfGovWrapperElementsForFullConfigWithAllTopConfigAsNoneAndAllBooleansFalse(fResponse)
      res.status shouldBe OK
    }
    "pre-pop with an address and all elements are correct for almost full journey config model (missing field in confirm page) with all booleans as FALSE for page" in {
      val jc = fullDefaultJourneyConfigModelWithAllBooleansSet(false)

      stubKeystore(testJourneyId, journeyDataWithSelectedAddressJson(jc.copy(confirmPage = Some(jc.confirmPage.get.copy(heading = None))), testFullNonUKAddress), OK)

      val fResponse = buildClientLookupAddress(path = "confirm")
        .withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
        .get()
      val res = await(fResponse)
      val doc = getDocFromResponse(fResponse)

      doc.select("a[class=back-link]") should have(
        text("Back")
      )
      doc.title shouldBe "confirm-title"
      doc.h1.text() shouldBe "Review and confirm"
      doc.submitButton.text() shouldBe "confirm-submitLabel"
      doc.address should have(
        addressLine("line1", "1 High Street"),
        addressLine("line2", "Line 2"),
        addressLine("line3", "Line 3"),
        addressLine("line4", "Telford"),
        addressLine("postCode", "AB11 1AB"),
        addressLine("country", "France")
      )
      doc.paras should not have elementWithValue("This is how your address will look. Please double-check it and, if accurate, click on the Confirm button.")
      doc.h2s should not have elementWithValue("Your selected address")
      testElementDoesntExist(res, "searchAgainLink")
      testElementDoesntExist(res, "confirmChangeText")

      testCustomPartsOfGovWrapperElementsForFullConfigWithAllTopConfigAsNoneAndAllBooleansFalse(fResponse)
      res.status shouldBe OK
    }
  }

  "The confirm page POST" should {
    "use the correct continue url when user clicks Confirm the address" in {
      stubKeystore(testJourneyId, testConfigWithAddressNotUkModeAsJson, OK)

      stubKeystoreSave(testJourneyId, Json.toJson(testConfigWithAddressNotUkMode.copy(confirmedAddress = Some(testFullNonUKConfirmedAddress))), OK)
      val fResponse = buildClientLookupAddress(path = "confirm")
        .withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
        .post(Map("csrfToken" -> Seq("xxx-ignored-xxx")))

      val res = await(fResponse)
      res.status shouldBe SEE_OTHER
      res.header(HeaderNames.LOCATION).get shouldBe s"$testContinueUrl?id=$testJourneyId"
    }

    "should redirect to the confirm page if incorrect data in keystore" in {
      stubKeystore(testJourneyId, testConfigDefaultAsJson, OK)

      val fResponse = buildClientLookupAddress(path = "confirm")
        .withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
        .post(Map("csrfToken" -> Seq("xxx-ignored-xxx")))

      val res = await(fResponse)
      res.status shouldBe SEE_OTHER
      res.header(HeaderNames.LOCATION).get shouldBe "/lookup-address/Jid123/confirm"
    }

  }
}