package controllers

import itutil.IntegrationSpecBase
import play.api.test.FakeApplication
import play.api.http.Status.{BAD_REQUEST, OK}
import itutil.config.IntegrationTestConstants._
import play.api.http.HeaderNames
import play.api.libs.json.Json
import itutil.config.PageElementConstants.LookupPage
import controllers.routes

class NoResultsFoundPageISpec extends IntegrationSpecBase {

  override implicit lazy val app = FakeApplication(additionalConfiguration = fakeConfig())

  object messages {
    val title = "Can't find any addresses"
    def heading(pc: String) = s"We can not find any addresses for $pc"
    val manualEntry = "Enter the address manually"
    val submitButton = "Try a different postcode"
  }

  "No results page GET" should {
    "with the default config" should {
      "Render the 'No results' page" in {
        stubKeystore(testJourneyId, testConfigDefaultAsJson, OK)
        stubKeystoreSave(testJourneyId, testConfigDefaultAsJson, OK)
        stubALBEGET(addressJson = Json.toJson(Json.arr()))

        val fResponse = buildClientLookupAddress(path = s"select?${LookupPage.postcodeId}=$testPostCode&${LookupPage.filterId}=")
          .withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()
        val res = await(fResponse)
        val doc = getDocFromResponse(res)

        res.status shouldBe OK

        doc.title shouldBe messages.title
        doc.h1.text() shouldBe messages.heading(testPostCode)

        doc.select("a[class=back-link]") should have(
          text("Back")
        )

        doc.link("enterManual") should have(
          href(routes.AddressLookupController.edit(testJourneyId, None, Some(true)).url),
          text(messages.manualEntry)
        )

        doc.submitButton.text() shouldBe messages.submitButton
      }
    }

    "with the back button disabled in config" should {
      "Render the 'No results' page without a back button" in {
        stubKeystore(testJourneyId, testLookupConfigNoBackButtons, OK)
        stubKeystoreSave(testJourneyId, testLookupConfigNoBackButtons, OK)
        stubALBEGET(addressJson = Json.toJson(Json.arr()))

        val fResponse = buildClientLookupAddress(path = s"select?${LookupPage.postcodeId}=$testPostCode&${LookupPage.filterId}=")
          .withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()
        val res = await(fResponse)
        val doc = getDocFromResponse(res)

        res.status shouldBe OK

        doc.title shouldBe messages.title
        doc.h1.text() shouldBe messages.heading(testPostCode)

        doc.select("a[class=back-link]") should not have(
          text("Back")
        )

        doc.link("enterManual") should have(
          href(routes.AddressLookupController.edit(testJourneyId, None, Some(true)).url),
          text(messages.manualEntry)
        )

        doc.submitButton.text() shouldBe messages.submitButton
      }
    }
  }
}
