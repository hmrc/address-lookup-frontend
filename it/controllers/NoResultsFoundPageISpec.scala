package controllers

import itutil.IntegrationSpecBase
import itutil.config.IntegrationTestConstants._
import itutil.config.PageElementConstants.LookupPage
import model._
import play.api.http.HeaderNames
import play.api.http.Status.OK
import play.api.libs.json.Json
import play.api.test.FakeApplication

class NoResultsFoundPageISpec extends IntegrationSpecBase {

  override implicit lazy val app = FakeApplication(additionalConfiguration = fakeConfig())

  object EnglishContent {
    val title = "We can not find any addresses"

    def heading(postcode: String) = s"We can not find any addresses for $postcode"

    val manualEntry = "Enter the address manually"
    val submitButton = "Try a different postcode"
  }

  object WelshContent {
    val title = "Ni allwn ddod o hyd i unrhyw gyfeiriadau"

    def heading(postcode: String) = s"Ni allwn ddod o hyd i unrhyw gyfeiriadau ar gyfer $postcode"

    val manualEntry = "Nodwch y cyfeiriad â llaw"
    val submitButton = "Rhowch gynnig ar god post gwahanol"
  }

  "No results page GET" should {
    "with the default config" should {
      "Render the 'No results' page" in {
        stubKeystore(testJourneyId, testMinimalLevelJourneyConfigV2, OK)
        stubKeystoreSave(testJourneyId, testMinimalLevelJourneyConfigV2, OK)
        stubGetAddressFromBE(addressJson = Json.toJson(Json.arr()))

        val fResponse = buildClientLookupAddress(path = s"select?${LookupPage.postcodeId}=$testPostCode&${LookupPage.filterId}=")
          .withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()
        val res = await(fResponse)
        val doc = getDocFromResponse(res)

        res.status shouldBe OK

        testCustomPartsOfGovWrapperElementsForDefaultConfig(fResponse)

        doc.title shouldBe EnglishContent.title
        doc.h1.text() shouldBe EnglishContent.heading(testPostCode)

        doc.select("a[class=back-link]") should have(
          text("Back")
        )

        doc.link("enterManual") should have(
          href(routes.AddressLookupController.edit(testJourneyId, None).url),
          text(EnglishContent.manualEntry)
        )

        doc.submitButton.text() shouldBe EnglishContent.submitButton
      }
    }

    "With full journey config model with all booleans set to true" should {
      "Render the page with expected custom English elements" in {
        stubKeystore(testJourneyId, journeyDataV2WithSelectedAddressJson(journeyConfigV2 = fullDefaultJourneyConfigModelV2WithAllBooleansSet()), OK)
        stubKeystoreSave(testJourneyId, journeyDataV2WithSelectedAddressJson(journeyConfigV2 = fullDefaultJourneyConfigModelV2WithAllBooleansSet()), OK)
        stubGetAddressFromBE(addressJson = Json.toJson(Json.arr()))

        val fResponse = buildClientLookupAddress(path = s"select?${LookupPage.postcodeId}=$testPostCode&${LookupPage.filterId}=")
          .withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()
        val res = await(fResponse)
        val doc = getDocFromResponse(res)

        res.status shouldBe OK

        testCustomPartsOfGovWrapperElementsForFullConfigAllTrue(fResponse, "NAV_TITLE")

        doc.title shouldBe EnglishContent.title + " - NAV_TITLE - GOV.UK"
        doc.h1.text() shouldBe EnglishContent.heading(testPostCode)

        doc.select("a[class=back-link]") should have(
          text("Back")
        )

        doc.link("enterManual") should have(
          href(routes.AddressLookupController.edit(testJourneyId, None).url),
          text(EnglishContent.manualEntry)
        )

        doc.submitButton.text() shouldBe EnglishContent.submitButton
      }
    }

    "With full journey config model with top level config set to None all booleans set to true" should {
      "Render the page with expected custom English elements" in {
        stubKeystore(testJourneyId, journeyDataV2WithSelectedAddressJson(
          fullDefaultJourneyConfigModelV2WithAllBooleansSet(false)), OK)
        stubKeystoreSave(testJourneyId, journeyDataV2WithSelectedAddressJson(
          fullDefaultJourneyConfigModelV2WithAllBooleansSet(false)), OK)
        stubGetAddressFromBE(addressJson = Json.toJson(Json.arr()))

        val fResponse = buildClientLookupAddress(path = s"select?${LookupPage.postcodeId}=$testPostCode&${LookupPage.filterId}=")
          .withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()
        val res = await(fResponse)
        val doc = getDocFromResponse(res)

        res.status shouldBe OK

        testCustomPartsOfGovWrapperElementsForFullConfigWithAllTopConfigAsNoneAndAllBooleansFalse(fResponse)

        doc.title shouldBe EnglishContent.title
        doc.h1.text() shouldBe EnglishContent.heading(testPostCode)

        doc.select("a[class=back-link]") should have(
          text("Back")
        )

        doc.link("enterManual") should have(
          href(routes.AddressLookupController.edit(testJourneyId, None).url),
          text(EnglishContent.manualEntry)
        )

        doc.submitButton.text() shouldBe EnglishContent.submitButton
      }
    }

    "With full journey config model with top level config set to None all booleans set to true" should {
      "Render the page with expected custom Welsh elements" in {
        stubKeystore(testJourneyId, journeyDataV2WithSelectedAddressJson(
          fullDefaultJourneyConfigModelV2WithAllBooleansSet(false, isWelsh = true)), OK)
        stubKeystoreSave(testJourneyId, journeyDataV2WithSelectedAddressJson(
          fullDefaultJourneyConfigModelV2WithAllBooleansSet(false, isWelsh = true)), OK)
        stubGetAddressFromBE(addressJson = Json.toJson(Json.arr()))

        val fResponse = buildClientLookupAddress(path = s"select?${LookupPage.postcodeId}=$testPostCode&${LookupPage.filterId}=")
          .withHeaders(HeaderNames.COOKIE -> (sessionCookieWithCSRF + ";PLAY_LANG=cy;"), "Csrf-Token" -> "nocheck")
          .get()
        val res = await(fResponse)
        val doc = getDocFromResponse(res)

        res.status shouldBe OK

        testCustomPartsOfGovWrapperElementsForFullConfigWithAllTopConfigAsNoneAndAllBooleansFalse(fResponse)

        doc.title shouldBe WelshContent.title
        doc.h1.text() shouldBe WelshContent.heading(testPostCode)

        doc.select("a[class=back-link]") should have(
          text("Yn ôl")
        )

        doc.link("enterManual") should have(
          href(routes.AddressLookupController.edit(testJourneyId, None).url),
          text(WelshContent.manualEntry)
        )

        doc.submitButton.text() shouldBe WelshContent.submitButton
      }
    }

    "With the back button disabled in config" should {
      "Render the 'No results' page without a back button" in {
        val testJson = Json.toJson(
          journeyDataV2Minimal.copy(
            JourneyConfigV2(
              version = 2,
              options = JourneyOptions(
                continueUrl = testContinueUrl,
                showBackButtons = Some(false)
              ),
              labels = Some(JourneyLabels(
                en = Some(LanguageLabels()),
                cy = Some(LanguageLabels())
              ))
            )
          )
        )

        stubKeystore(testJourneyId, testJson, OK)
        stubKeystoreSave(testJourneyId, testJson, OK)
        stubGetAddressFromBE(addressJson = Json.toJson(Json.arr()))

        val fResponse = buildClientLookupAddress(path = s"select?${LookupPage.postcodeId}=$testPostCode&${LookupPage.filterId}=")
          .withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()
        val res = await(fResponse)
        val doc = getDocFromResponse(res)

        res.status shouldBe OK

        doc.title shouldBe EnglishContent.title
        doc.h1.text() shouldBe EnglishContent.heading(testPostCode)

        doc.select("a[class=back-link]") should not have (
          text("Back")
          )

        doc.link("enterManual") should have(
          href(routes.AddressLookupController.edit(testJourneyId, None).url),
          text(EnglishContent.manualEntry)
        )

        doc.submitButton.text() shouldBe EnglishContent.submitButton
      }
    }
  }
}
