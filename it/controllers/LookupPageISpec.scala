package controllers

import config.ALFCookieNames
import itutil.IntegrationSpecBase
import itutil.config.IntegrationTestConstants._
import itutil.config.PageElementConstants.LookupPage
import model.JourneyConfigDefaults.EnglishConstants
import play.api.http.Status._
import play.api.http.HeaderNames
import play.api.libs.json.Json
import play.api.test.FakeApplication

import scala.util.Random
import model.MessageConstants.{EnglishMessageConstants => EnglishMessages, WelshMessageConstants => WelshMessages}

class LookupPageISpec extends IntegrationSpecBase {

  override implicit lazy val app = FakeApplication(additionalConfiguration = fakeConfig())
  val EnglishMessageConstants = EnglishMessages(true)
  val WelshMessageConstants = WelshMessages(true)
  val EnglishConstantsNonUkMode = EnglishConstants(false)

  import EnglishConstantsNonUkMode._

  def longFilterValue = (1 to 257) map (_ => Random.alphanumeric.head) mkString

  // TODO: Make hint configurable as part of welsh translation
  val hardCodedFormHint = " For example, The Mill, 116 or Flat 37a"

  "The lookup page" when {
    "when provided with no page config" should {
      "Render the default content" in {
        stubKeystore(testJourneyId, testMinimalLevelJourneyConfigV2, OK)
        stubKeystoreSave(testJourneyId, testMinimalLevelJourneyConfigV2, OK)

        val fResponse = buildClientLookupAddress(path = s"lookup?postcode=$testPostCode&filter=$testFilterValue")
          .withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()
        val res = await(fResponse)
        val doc = getDocFromResponse(res)

        res.status shouldBe OK

        testCustomPartsOfGovWrapperElementsForDefaultConfig(fResponse)

        doc.title shouldBe LOOKUP_PAGE_TITLE
        doc.h1.text() shouldBe LOOKUP_PAGE_HEADING

        doc.select("a[class=back-link]") should have(
          text("Back")
        )

        doc.input(LookupPage.postcodeId) should have(
          label(LOOKUP_PAGE_POSTCODE_LABEL),
          value(testPostCode)
        )

        doc.input(LookupPage.filterId) should have(
          label(LOOKUP_PAGE_FILTER_LABEL + hardCodedFormHint),
          value(testFilterValue)
        )

        doc.link(LookupPage.manualAddressLink) should have(
          href(routes.AddressLookupController.edit(testJourneyId).url),
          text(LOOKUP_PAGE_MANUAL_ADDRESS_LINK_TEXT)
        )

        doc.submitButton.text() shouldBe "Find address"
      }

      "Show the default 'postcode not entered' error message" in {
        stubKeystore(testJourneyId, testMinimalLevelJourneyConfigV2, OK)
        stubKeystoreSave(testJourneyId, testMinimalLevelJourneyConfigV2, OK)

        val fResponse = buildClientLookupAddress(path = "select")
          .withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()
        val res = await(fResponse)
        val doc = getDocFromResponse(res)

        res.status shouldBe BAD_REQUEST

        val message = "This field is required"

        doc.errorSummary should have(
          errorSummaryMessage(LookupPage.postcodeId, message)
        )

        doc.input(LookupPage.postcodeId) should have(
          errorMessage(message),
          value("")
        )
      }

      "Show the default 'invalid postcode' error message" in {
        stubKeystore(testJourneyId, testMinimalLevelJourneyConfigV2, OK)
        stubKeystoreSave(testJourneyId, testMinimalLevelJourneyConfigV2, OK)

        val fResponse = buildClientLookupAddress(path = s"select?postcode=QQ")
          .withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()
        val res = await(fResponse)
        val doc = getDocFromResponse(res)

        res.status shouldBe BAD_REQUEST

        val message = "Enter a real Postcode e.g. AA1 1AA"

        doc.errorSummary should have(
          errorSummaryMessage(LookupPage.postcodeId, message)
        )

        doc.input(LookupPage.postcodeId) should have(
          errorMessage(message),
          value("QQ")
        )
      }

      "Show the default 'filter invalid' error messages" in {
        stubKeystore(testJourneyId, testMinimalLevelJourneyConfigV2, OK)
        stubKeystoreSave(testJourneyId, testMinimalLevelJourneyConfigV2, OK)

        val filterValue = longFilterValue
        val fResponse = buildClientLookupAddress(path = s"select?filter=$filterValue")
          .withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()
        val res = await(fResponse)
        val doc = getDocFromResponse(res)

        res.status shouldBe BAD_REQUEST

        val message = "Your house name/number needs to be fewer than 256 characters"

        doc.errorSummary should have(
          errorSummaryMessage(LookupPage.filterId, message)
        )

        doc.input(LookupPage.filterId) should have(
          errorMessage(message),
          value(filterValue)
        )
      }
    }

    "Provided with custom content" should {
      "Render the page with custom content" in {
        stubKeystore(testJourneyId, testCustomLookupPageJourneyConfigV2, OK)
        stubKeystoreSave(testJourneyId, testCustomLookupPageJourneyConfigV2, OK)

        val fResponse = buildClientLookupAddress(path = s"lookup?postcode=$testPostCode&filter=$testFilterValue")
          .withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()
        val res = await(fResponse)
        val doc = getDocFromResponse(res)

        res.status shouldBe OK

        doc.title shouldBe fullLookupPageConfig.title.get + " - NAV_TITLE - GOV.UK"
        doc.h1.text() shouldBe fullLookupPageConfig.heading.get

        doc.select("a[class=back-link]") should have(
          text("Back")
        )

        doc.input(LookupPage.postcodeId) should have(
          label(fullLookupPageConfig.postcodeLabel.get),
          value(testPostCode)
        )

        doc.input(LookupPage.filterId) should have(
          label(fullLookupPageConfig.filterLabel.get + hardCodedFormHint),
          value(testFilterValue)
        )

        doc.link(LookupPage.manualAddressLink) should have(
          href(routes.AddressLookupController.edit(testJourneyId).url),
          text(fullLookupPageConfig.manualAddressLinkText.get)
        )

        doc.submitButton.text() shouldBe fullLookupPageConfig.submitLabel.get
      }

      "not display the back button if disabled" in {
        stubKeystore(testJourneyId, testDefaultLookupPageJourneyConfigV2, OK)
        stubKeystoreSave(testJourneyId, testDefaultLookupPageJourneyConfigV2, OK)

        val fResponse = buildClientLookupAddress(path = s"lookup?postcode=$testPostCode&filter=$testFilterValue")
          .withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()
        val res = await(fResponse)
        val doc = getDocFromResponse(res)

        res.status shouldBe OK

        doc.select("a[class=back-link]") should not have (
          text("Back")
          )
      }
    }

    "Provided with config with all booleans set to true" should {
      "Render the page correctly with custom elements" in {
        stubKeystore(testJourneyId, testCustomLookupPageJourneyConfigV2, OK)
        stubKeystoreSave(testJourneyId, testCustomLookupPageJourneyConfigV2, OK)

        val fResponse = buildClientLookupAddress(path = s"lookup?postcode=$testPostCode&filter=$testFilterValue")
          .withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()
        val res = await(fResponse)
        val doc = getDocFromResponse(res)

        res.status shouldBe OK

        testCustomPartsOfGovWrapperElementsForFullConfigAllTrue(fResponse, "NAV_TITLE")

        doc.title shouldBe fullLookupPageConfig.title.get + " - NAV_TITLE - GOV.UK"
        doc.h1.text() shouldBe fullLookupPageConfig.heading.get

        doc.select("a[class=back-link]") should have(
          text("Back")
        )

        doc.input(LookupPage.postcodeId) should have(
          label(fullLookupPageConfig.postcodeLabel.get),
          value(testPostCode)
        )

        doc.input(LookupPage.filterId) should have(
          label(fullLookupPageConfig.filterLabel.get + hardCodedFormHint),
          value(testFilterValue)
        )

        doc.link(LookupPage.manualAddressLink) should have(
          href(routes.AddressLookupController.edit(testJourneyId).url),
          text(fullLookupPageConfig.manualAddressLinkText.get)
        )

        doc.submitButton.text() shouldBe fullLookupPageConfig.submitLabel.get
      }
    }

    "Provided with config where all the default values are overriden with the default values" should {
      "Render " in {
        stubKeystore(testJourneyId, testOtherCustomLookupPageJourneyConfigV2, OK)
        stubKeystoreSave(testJourneyId, testOtherCustomLookupPageJourneyConfigV2, OK)

        val fResponse = buildClientLookupAddress(path = s"lookup?postcode=$testPostCode&filter=$testFilterValue")
          .withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()
        val res = await(fResponse)
        val doc = getDocFromResponse(res)

        res.status shouldBe OK

        testCustomPartsOfGovWrapperElementsForFullConfigWithAllTopConfigAsNoneAndAllBooleansFalse(fResponse)

        doc.title shouldBe fullLookupPageConfig.title.get
        doc.h1.text() shouldBe fullLookupPageConfig.heading.get

        doc.select("a[class=back-link]") should have(
          text("Back")
        )

        doc.input(LookupPage.postcodeId) should have(
          label(fullLookupPageConfig.postcodeLabel.get),
          value(testPostCode)
        )

        doc.input(LookupPage.filterId) should have(
          label(fullLookupPageConfig.filterLabel.get + hardCodedFormHint),
          value(testFilterValue)
        )

        doc.link(LookupPage.manualAddressLink) should have(
          href(routes.AddressLookupController.edit(testJourneyId).url),
          text(fullLookupPageConfig.manualAddressLinkText.get)
        )

        doc.submitButton.text() shouldBe fullLookupPageConfig.submitLabel.get
      }
    }
  }

  "technical difficulties" when {
    "the welsh content header isn't set and welsh object isn't provided in config" should {
      "render in English" in {
        stubKeystore(testJourneyId, testMinimalLevelJourneyConfigV2, INTERNAL_SERVER_ERROR)
        stubKeystoreSave(testJourneyId, testMinimalLevelJourneyConfigV2, INTERNAL_SERVER_ERROR)

        val fResponse = buildClientLookupAddress(s"lookup?postcode=$testPostCode&filter=$testFilterValue")
          .withHeaders(
            HeaderNames.COOKIE -> sessionCookieWithCSRF,
            "Csrf-Token" -> "nocheck"
          )
          .get()

        val res = await(fResponse)
        res.status shouldBe INTERNAL_SERVER_ERROR

        val doc = getDocFromResponse(res)
        doc.title shouldBe EnglishMessageConstants.intServerErrorTitle
        doc.h1 should have (text(EnglishMessageConstants.intServerErrorTitle))
        doc.paras should have (elementWithValue(EnglishMessageConstants.intServerErrorTryAgain))
      }
    }
    "the welsh content header is set to false and welsh object isn't provided in config" should {
      "render in English" in {
        stubKeystore(testJourneyId, testMinimalLevelJourneyConfigV2, INTERNAL_SERVER_ERROR)
        stubKeystoreSave(testJourneyId, testMinimalLevelJourneyConfigV2, INTERNAL_SERVER_ERROR)

        val fResponse = buildClientLookupAddress(s"lookup?postcode=$testPostCode&filter=$testFilterValue")
          .withHeaders(
            HeaderNames.COOKIE -> sessionCookieWithWelshCookie(useWelsh = false),
            "Csrf-Token" -> "nocheck"
          )
          .get()

        val res = await(fResponse)
        res.status shouldBe INTERNAL_SERVER_ERROR

        val doc = getDocFromResponse(res)
        doc.title shouldBe EnglishMessageConstants.intServerErrorTitle
        doc.h1 should have (text(EnglishMessageConstants.intServerErrorTitle))
        doc.paras should have (elementWithValue(EnglishMessageConstants.intServerErrorTryAgain))
      }
    }
    "the welsh content header is set to false and welsh object is provided in config" should {
      "render in English" in {
        val v2Config = Json.toJson(fullDefaultJourneyConfigModelV2WithAllBooleansSet(allBooleanSetAndAppropriateOptions = true, isWelsh = true))
        stubKeystore(testJourneyId, v2Config, INTERNAL_SERVER_ERROR)
        stubKeystoreSave(testJourneyId, v2Config, INTERNAL_SERVER_ERROR)

        val fResponse = buildClientLookupAddress(s"lookup?postcode=$testPostCode&filter=$testFilterValue")
          .withHeaders(
            HeaderNames.COOKIE -> sessionCookieWithWelshCookie(useWelsh = false),
            "Csrf-Token" -> "nocheck"
          )
          .get()

        val res = await(fResponse)
        res.status shouldBe INTERNAL_SERVER_ERROR

        val doc = getDocFromResponse(res)
        doc.title shouldBe EnglishMessageConstants.intServerErrorTitle
        doc.h1 should have (text(EnglishMessageConstants.intServerErrorTitle))
        doc.paras should have (elementWithValue(EnglishMessageConstants.intServerErrorTryAgain))
      }
    }
    "the welsh content header is set to true and welsh object provided in config" should {
      "render in Welsh" in {
        val v2Config = Json.toJson(fullDefaultJourneyConfigModelV2WithAllBooleansSet(allBooleanSetAndAppropriateOptions = true, isWelsh = true))
        stubKeystore(testJourneyId, v2Config, INTERNAL_SERVER_ERROR)
        stubKeystoreSave(testJourneyId, v2Config, INTERNAL_SERVER_ERROR)

        val fResponse = buildClientLookupAddress(s"lookup?postcode=$testPostCode&filter=$testFilterValue")
          .withHeaders(
            HeaderNames.COOKIE -> sessionCookieWithWelshCookie(useWelsh = true),
            "Csrf-Token" -> "nocheck"
          )
          .get()

        val res = await(fResponse)
        res.status shouldBe INTERNAL_SERVER_ERROR

        val doc = getDocFromResponse(res)
        doc.title shouldBe WelshMessageConstants.intServerErrorTitle
        doc.h1 should have (text(WelshMessageConstants.intServerErrorTitle))
        doc.paras should have (elementWithValue(WelshMessageConstants.intServerErrorTryAgain))
      }
    }
  }
}
