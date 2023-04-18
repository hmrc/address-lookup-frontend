package controllers.abp

import controllers.routes
import itutil.IntegrationSpecBase
import itutil.config.IntegrationTestConstants._
import itutil.config.PageElementConstants.LookupPage
import model._
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.http.Status.OK
import play.api.libs.json.Json

class NoResultsFoundPageISpec extends IntegrationSpecBase {

  object EnglishContent {
    def title(postcode: String) = s"We cannot find any addresses for $postcode"

    def heading(postcode: String) = s"We cannot find any addresses for $postcode"

    val manualEntry = "Enter the address manually"
    val submitButton = "Try a different postcode"
  }

  object WelshContent {
    def title(postcode: String) = s"Ni allwn ddod o hyd i unrhyw gyfeiriadau ar gyfer $postcode"

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

        val fResponse = buildClientLookupAddress(path = s"select?${LookupPage.postcodeId}=$testPostCode")
          .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()
        val res = await(fResponse)
        val doc = getDocFromResponse(res)

        res.status shouldBe OK

        testCustomPartsOfGovWrapperElementsForDefaultConfig(fResponse)

        doc.title shouldBe EnglishContent.title(testPostCode)
        doc.h1.text() shouldBe EnglishContent.heading(testPostCode)

        doc.select("a[class=govuk-back-link]") should have(
          text("Back")
        )

        doc.link("enterManual") should have(
          href(routes.AbpAddressLookupController.edit(testJourneyId, None).url),
          text(EnglishContent.manualEntry)
        )

        doc.select("a[class=govuk-button]") should have(
          text(EnglishContent.submitButton)
        )
      }
    }

    "With full journey config model with all booleans set to true" should {
      "Render the page with expected custom English elements" in {
        stubKeystore(testJourneyId, journeyDataV2WithSelectedAddressJson(journeyConfigV2 = fullDefaultJourneyConfigModelV2WithAllBooleansSet()), OK)
        stubKeystoreSave(testJourneyId, journeyDataV2WithSelectedAddressJson(journeyConfigV2 = fullDefaultJourneyConfigModelV2WithAllBooleansSet()), OK)
        stubGetAddressFromBE(addressJson = Json.toJson(Json.arr()))

        val fResponse = buildClientLookupAddress(path = s"select?${LookupPage.postcodeId}=$testPostCode")
          .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()
        val res = await(fResponse)
        val doc = getDocFromResponse(res)

        res.status shouldBe OK

        testCustomPartsOfGovWrapperElementsForFullConfigAllTrue(fResponse, "NAV_TITLE")

        doc.title shouldBe EnglishContent.title(testPostCode) + " - NAV_TITLE - GOV.UK"
        doc.h1.text() shouldBe EnglishContent.heading(testPostCode)

        doc.select("a[class=govuk-back-link]") should have(
          text("Back")
        )

        doc.link("enterManual") should have(
          href(routes.AbpAddressLookupController.edit(testJourneyId, None).url),
          text(EnglishContent.manualEntry)
        )

        doc.select("a[class=govuk-button]") should have(
          text(EnglishContent.submitButton)
        )
      }
    }

    "With full journey config model with top level config set to None all booleans set to true" should {
      "Render the page with expected custom English elements" in {
        stubKeystore(testJourneyId, journeyDataV2WithSelectedAddressJson(
          fullDefaultJourneyConfigModelV2WithAllBooleansSet(false)), OK)
        stubKeystoreSave(testJourneyId, journeyDataV2WithSelectedAddressJson(
          fullDefaultJourneyConfigModelV2WithAllBooleansSet(false)), OK)
        stubGetAddressFromBE(addressJson = Json.toJson(Json.arr()))

        val fResponse = buildClientLookupAddress(path = s"select?${LookupPage.postcodeId}=$testPostCode")
          .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()
        val res = await(fResponse)
        val doc = getDocFromResponse(res)

        res.status shouldBe OK

        testCustomPartsOfGovWrapperElementsForFullConfigWithAllTopConfigAsNoneAndAllBooleansFalse(fResponse)

        doc.title shouldBe EnglishContent.title(testPostCode)
        doc.h1.text() shouldBe EnglishContent.heading(testPostCode)

        doc.select("a[class=govuk-back-link]") should have(
          text("Back")
        )

        doc.link("enterManual") should have(
          href(routes.AbpAddressLookupController.edit(testJourneyId, None).url),
          text(EnglishContent.manualEntry)
        )

        doc.select("a[class=govuk-button]") should have(
          text(EnglishContent.submitButton)
        )
      }
    }

    "With full journey config model with top level config set to None all booleans set to true" should {
      "Render the page with expected custom Welsh elements" in {
        stubKeystore(testJourneyId, journeyDataV2WithSelectedAddressJson(
          fullDefaultJourneyConfigModelV2WithAllBooleansSet(false, isWelsh = true)), OK)
        stubKeystoreSave(testJourneyId, journeyDataV2WithSelectedAddressJson(
          fullDefaultJourneyConfigModelV2WithAllBooleansSet(false, isWelsh = true)), OK)
        stubGetAddressFromBE(addressJson = Json.toJson(Json.arr()))

        val fResponse = buildClientLookupAddress(path = s"select?${LookupPage.postcodeId}=$testPostCode")
          .withHttpHeaders(HeaderNames.COOKIE -> (sessionCookieWithCSRF + ";PLAY_LANG=cy;"), "Csrf-Token" -> "nocheck")
          .get()
        val res = await(fResponse)
        val doc = getDocFromResponse(res)

        res.status shouldBe OK

        testCustomPartsOfGovWrapperElementsForFullConfigWithAllTopConfigAsNoneAndAllBooleansFalse(fResponse)

        doc.title shouldBe WelshContent.title(testPostCode)
        doc.h1.text() shouldBe WelshContent.heading(testPostCode)

        doc.select("a[class=govuk-back-link]") should have(
          text("Yn ôl")
        )

        doc.link("enterManual") should have(
          href(routes.AbpAddressLookupController.edit(testJourneyId, None).url),
          text(WelshContent.manualEntry)
        )

        doc.select("a[class=govuk-button]") should have(
          text(WelshContent.submitButton)
        )
      }
    }

    "With the back button disabled in config" should {
      "Render the 'No results' page without a back button" in {
        val testJson = Json.toJson(
          journeyDataV2Minimal.copy(
            JourneyConfigV2(
              version = 2,
              options = JourneyOptions(continueUrl = testContinueUrl, showBackButtons = Some(false)),
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

        val fResponse = buildClientLookupAddress(path = s"select?${LookupPage.postcodeId}=$testPostCode")
          .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()
        val res = await(fResponse)
        val doc = getDocFromResponse(res)

        res.status shouldBe OK

        doc.title shouldBe EnglishContent.title(testPostCode)
        doc.h1.text() shouldBe EnglishContent.heading(testPostCode)

        doc.select("a[class=govuk-back-link]") should not have (
          text("Back")
          )

        doc.link("enterManual") should have(
          href(routes.AbpAddressLookupController.edit(testJourneyId, None).url),
          text(EnglishContent.manualEntry)
        )

        doc.select("a[class=govuk-button]") should have(
          text(EnglishContent.submitButton)
        )
      }
    }

    "when provided with a pageHeadingStyle option" should {
      "allow the initialising service to override the header size" in {
        val testJson = Json.toJson(
          journeyDataV2Minimal.copy(
            JourneyConfigV2(
              version = 2,
              options = JourneyOptions(continueUrl = testContinueUrl, pageHeadingStyle = Some("govuk-heading-l")),
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

        val fResponse = buildClientLookupAddress(path = s"select?${LookupPage.postcodeId}=$testPostCode")
          .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()

        val res = await(fResponse)

        res.status shouldBe OK
        val document = Jsoup.parse(res.body)
        document.getElementById("pageHeading").classNames() should contain("govuk-heading-l")
      }
    }
  }
}
