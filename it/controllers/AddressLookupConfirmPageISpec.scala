package controllers

import itutil.IntegrationSpecBase
import itutil.config.IntegrationTestConstants._
import play.api.http.HeaderNames
import play.api.http.Status._
import play.api.libs.json.Json
import play.api.test.FakeApplication


class AddressLookupConfirmPageISpec extends IntegrationSpecBase {

  override implicit lazy val app = FakeApplication(additionalConfiguration = fakeConfig())

  "The confirm page GET" should {
    "pre-pop with an address and all elements are correct for an empty journey config model" in {
      stubKeystore(testJourneyId, testConfigWithAddressNotUkModeAsJson, OK)

      val fResponse = buildClientLookupAddress(path = "confirm")
        .withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
        .get()
      val res = await(fResponse)
      val doc = getDocFromResponse(fResponse)

      doc.getElementsByClass("back-link").first().text() shouldBe "Back"
      doc.getElementsByTag("title").first().text() shouldBe "Confirm the address"

      doc.getElementById("pageHeading").text() shouldBe "Review and confirm"
      doc.getElementById("changeLink").text() shouldBe "Edit this address"
      doc.getElementById("continue").text() shouldBe "Confirm and continue"
      doc.getElementById("line1").text() shouldBe "1 High Street"
      doc.getElementById("line2").text() shouldBe "Line 2"
      doc.getElementById("line3").text() shouldBe "Line 3"
      doc.getElementById("line4").text() shouldBe "Telford"
      doc.getElementById("postCode").text() shouldBe "AB11 1AB"
      doc.getElementById("country").text() shouldBe "France"
      doc.getElementsByTag("h2").select(":containsOwn(Your selected address)").size() shouldBe 0
      doc.getElementsByTag("p").select(":containsOwn(This is how your address will look. Please double-check it and, if accurate, click on the Confirm button.)").size() shouldBe 0
      testElementDoesntExist(res,"searchAgainLink")
      testElementDoesntExist(res,"confirmChangeText")

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
      stubKeystore(testJourneyId, journeyDataWithSelectedAddressJson(), OK)

      val fResponse = buildClientLookupAddress(path = "confirm")
        .withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
        .get()
      val res = await(fResponse)
      val doc = getDocFromResponse(fResponse)

      doc.getElementsByClass("back-link").first().text() shouldBe "Back"
      doc.getElementsByTag("title").first().text() shouldBe "confirm-title"

      doc.getElementById("pageHeading").text() shouldBe "confirm-heading"
      doc.getElementById("changeLink").text() shouldBe "confirm-changeLinkText"
      doc.getElementById("continue").text() shouldBe "confirm-submitLabel"
      doc.getElementById("line1").text() shouldBe "1 High Street"
      doc.getElementById("line2").text() shouldBe "Telford"
      doc.getElementById("postCode").text() shouldBe "AB11 1AB"
      doc.getElementById("country").text() shouldBe "France"
      doc.getElementsByTag("h2").select(":containsOwn(confirm-infoSubheading)").size() shouldBe 1
      doc.getElementsByTag("p").select(":containsOwn(confirm-infoMessage)").size() shouldBe 1
      doc.getElementById("searchAgainLink").text() shouldBe "confirm-searchAgainLinkText"
      doc.getElementById("changeLink").text() shouldBe "confirm-changeLinkText"

      testCustomPartsOfGovWrapperElementsForFullConfigAllTrue(fResponse, navTitle = "NAV_TITLE")
      res.status shouldBe OK
    }

    "pre-pop with an address and all elements are correct for FULL journey config model with all booleans as FALSE for page" in {
      stubKeystore(testJourneyId, journeyDataWithSelectedAddressJson(fullDefaultJourneyConfigModelWithAllBooleansSet(false)), OK)

      val fResponse = buildClientLookupAddress(path = "confirm")
        .withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
        .get()
      val res = await(fResponse)
      val doc = getDocFromResponse(fResponse)

      doc.getElementsByTag("title").first().text() shouldBe "confirm-title"

      doc.getElementById("pageHeading").text() shouldBe "confirm-heading"
      doc.getElementById("continue").text() shouldBe "confirm-submitLabel"
      doc.getElementById("line1").text() shouldBe "1 High Street"
      doc.getElementById("line2").text() shouldBe "Telford"
      doc.getElementById("postCode").text() shouldBe "AB11 1AB"
      doc.getElementById("country").text() shouldBe "France"
      doc.getElementsByTag("h2").select(":containsOwn(Your selected address)").size() shouldBe 0
      doc.getElementsByTag("p").select(":containsOwn(This is how your address will look. Please double-check it and, if accurate, click on the Confirm button.)").size() shouldBe 0
      testElementDoesntExist(res,"searchAgainLink")
      testElementDoesntExist(res,"confirmChangeText")

      testCustomPartsOfGovWrapperElementsForFullConfigWithAllTopConfigAsNoneAndAllBooleansFalse(fResponse)
      res.status shouldBe OK
    }
    "pre-pop with an address and all elements are correct for almost full journey config model (missing field in confirm page) with all booleans as FALSE for page" in {
      val jc = fullDefaultJourneyConfigModelWithAllBooleansSet(false)

      stubKeystore(testJourneyId, journeyDataWithSelectedAddressJson(jc.copy(confirmPage = Some(jc.confirmPage.get.copy(heading = None)))), OK)

      val fResponse = buildClientLookupAddress(path = "confirm")
        .withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
        .get()
      val res = await(fResponse)
      val doc = getDocFromResponse(fResponse)

      doc.getElementsByTag("title").first().text() shouldBe "confirm-title"

      doc.getElementById("pageHeading").text() shouldBe "Review and confirm"
      doc.getElementById("continue").text() shouldBe "confirm-submitLabel"
      doc.getElementById("line1").text() shouldBe "1 High Street"
      doc.getElementById("line2").text() shouldBe "Telford"
      doc.getElementById("postCode").text() shouldBe "AB11 1AB"
      doc.getElementById("country").text() shouldBe "France"
      doc.getElementsByTag("h2").select(":containsOwn(Your selected address)").size() shouldBe 0
      doc.getElementsByTag("p").select(":containsOwn(This is how your address will look. Please double-check it and, if accurate, click on the Confirm button.)").size() shouldBe 0
      testElementDoesntExist(res,"searchAgainLink")
      testElementDoesntExist(res,"confirmChangeText")

      testCustomPartsOfGovWrapperElementsForFullConfigWithAllTopConfigAsNoneAndAllBooleansFalse(fResponse)
      res.status shouldBe OK
    }
  }

  "The confirm page POST" should {
    "use the correct continue url when user clicks Confirm the address" in {
      stubKeystore(testJourneyId, testConfigWithAddressNotUkModeAsJson, OK)

      stubKeystoreSave(testJourneyId,Json.obj(),OK)
      val fResponse = buildClientLookupAddress(path = "confirm")
        .withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
        .post(Map("csrfToken" -> Seq("xxx-ignored-xxx")))

      val res = await(fResponse)
      res.status shouldBe SEE_OTHER
      res.header(HeaderNames.LOCATION).get shouldBe "Aurl?id=Jid123"
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