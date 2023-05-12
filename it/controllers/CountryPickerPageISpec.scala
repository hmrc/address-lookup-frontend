package controllers

import address.v2.Countries
import itutil.IntegrationSpecBase
import itutil.config.IntegrationTestConstants.{testCustomCountryPickerPageJourneyConfigV2, testJourneyDataWithMinimalJourneyConfigV2, testJourneyId, testMinimalLevelJourneyConfigV2}
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.http.Status.{BAD_REQUEST, OK, SEE_OTHER}
import play.api.libs.json.Json

class CountryPickerPageISpec extends IntegrationSpecBase {

  "The country picker page" when {
    "provided with default page config" should {
      "render the default content" in {

        stubKeystore(testJourneyId, testMinimalLevelJourneyConfigV2, OK)
        stubKeystoreSave(testJourneyId, testMinimalLevelJourneyConfigV2, OK)

        val fResponse = buildClientLookupAddress(path = s"country-picker")
          .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()

        val res = await(fResponse)
        res.status shouldBe OK

        val document = Jsoup.parse(res.body)
        document.title() shouldBe messages("countryPickerPage.title")
        document.getElementById("pageHeading").text() shouldBe messages("countryPickerPage.heading")
        document.getElementById("pageHeading").classNames() should contain("govuk-heading-xl")
        document.getElementById("continue").text() shouldBe "Continue"

        labelForFieldsMatch(res, idOfFieldExpectedLabelTextForFieldMapping = Map(
          "countryCode" -> "Select country"
        ))
        document.getElementById("accessibilityAutocomplete").text() contains "No results found"

      }

      "render the default welsh content where the 'PLAY_LANG' is set to cy" in {
        stubKeystore(testJourneyId, testMinimalLevelJourneyConfigV2, OK)
        stubKeystoreSave(testJourneyId, testMinimalLevelJourneyConfigV2, OK)

        val fResponse = buildClientLookupAddress(path = s"country-picker")
          .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRFAndLang(Some("cy")), "Csrf-Token" -> "nocheck")
          .get()

        val res = await(fResponse)
        res.status shouldBe OK

        val document = Jsoup.parse(res.body)
        document.title() shouldBe "Dewiswch eich gwlad"
        document.getElementById("pageHeading").text() shouldBe "Dewiswch eich gwlad"
        document.getElementById("pageHeading").classNames() should contain("govuk-heading-xl")
        document.getElementById("continue").text() shouldBe "Yn eich blaen"

        labelForFieldsMatch(res, idOfFieldExpectedLabelTextForFieldMapping = Map(
          "countryCode" -> "Dewiswch wlad"
        ))
        document.getElementById("accessibilityAutocomplete").text() contains "Dim canlyniadau wedi’u darganfod"
      }
    }

    "provided with custom page config" should {
      "render the custom content" in {
        stubKeystore(testJourneyId, testCustomCountryPickerPageJourneyConfigV2, OK)
        stubKeystoreSave(testJourneyId, testCustomCountryPickerPageJourneyConfigV2, OK)

        val fResponse = buildClientLookupAddress(path = s"country-picker")
          .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()

        val res = await(fResponse)
        res.status shouldBe OK

        val document = Jsoup.parse(res.body)
        document.title() shouldBe "countryPicker-title"
        document.getElementById("pageHeading").text() shouldBe "countryPicker-heading"
        document.getElementById("pageHeading").classNames() should contain("govuk-heading-xl")
        document.getElementById("continue").text() shouldBe "Custom Continue"

        labelForFieldsMatch(res, idOfFieldExpectedLabelTextForFieldMapping = Map(
          "countryCode" -> "Custom Country"
        ))
      }

      "render the custom welsh content where the 'PLAY_LANG' is set to cy" in {
        stubKeystore(testJourneyId, testCustomCountryPickerPageJourneyConfigV2, OK)
        stubKeystoreSave(testJourneyId, testCustomCountryPickerPageJourneyConfigV2, OK)

        val fResponse = buildClientLookupAddress(path = s"country-picker")
          .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRFAndLang(Some("cy")), "Csrf-Token" -> "nocheck")
          .get()

        val res = await(fResponse)
        res.status shouldBe OK

        val document = Jsoup.parse(res.body)
        document.title() shouldBe "countryPicker-title-cy"
        document.getElementById("pageHeading").text() shouldBe "countryPicker-heading-cy"
        document.getElementById("pageHeading").classNames() should contain("govuk-heading-xl")
        document.getElementById("continue").text() shouldBe "Custom Continue Cy"

        labelForFieldsMatch(res, idOfFieldExpectedLabelTextForFieldMapping = Map(
          "countryCode" -> "Custom Country Cy"
        ))
      }
    }

    "submitted without a country specified" should {
      "return 400 bad request and display the error" in {
        stubKeystore(testJourneyId, testMinimalLevelJourneyConfigV2, OK)
        stubKeystoreSave(testJourneyId, testMinimalLevelJourneyConfigV2, OK)

        val fResponse = buildClientLookupAddress(path = s"country-picker")
          .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .post(Map("csrfToken" -> Seq("xxx-ignored-xxx")))

        val res = await(fResponse)
        res.status shouldBe BAD_REQUEST

        val document = Jsoup.parse(res.body)
        document.title() shouldBe s"Error: ${messages("countryPickerPage.title")}"
      }
    }

    "submitted with a country that has OS data" should {
      "redirect to the address lookup screen" in {
        stubKeystore(testJourneyId, Json.toJson(testJourneyDataWithMinimalJourneyConfigV2), OK)
        stubKeystoreSave(testJourneyId, Json.toJson(testJourneyDataWithMinimalJourneyConfigV2.copy(countryCode = Some(Countries.GB.code))), OK)

        val fResponse = buildClientLookupAddress(path = s"country-picker")
          .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .post(Map("countryCode" -> Seq("GB"), "csrfToken" -> Seq("xxx-ignored-xxx")))

        val res = await(fResponse)
        res.status shouldBe SEE_OTHER
        res.header(HeaderNames.LOCATION).get shouldBe "/lookup-address/Jid123/lookup"
      }
    }

    "submitted with a country that we hold non-OS data for" should {
      "redirect to the manual entry screen" in {
        stubKeystore(testJourneyId, Json.toJson(testJourneyDataWithMinimalJourneyConfigV2), OK)
        stubKeystoreSave(testJourneyId, Json.toJson(testJourneyDataWithMinimalJourneyConfigV2.copy(countryCode = Some("BM"))), OK)

        val fResponse = buildClientLookupAddress(path = s"country-picker")
          .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .post(Map("countryCode" -> Seq("BM"), "csrfToken" -> Seq("xxx-ignored-xxx")))

        val res = await(fResponse)
        res.status shouldBe SEE_OTHER
        res.header(HeaderNames.LOCATION).get shouldBe "/lookup-address/Jid123/international/lookup"
      }
    }

    "submitted with a country that we do not hold any data for" should {
      "redirect to the manual entry screen" in {
        stubKeystore(testJourneyId, Json.toJson(testJourneyDataWithMinimalJourneyConfigV2), OK)
        stubKeystoreSave(testJourneyId, Json.toJson(testJourneyDataWithMinimalJourneyConfigV2.copy(countryCode = Some("XX"))), OK)

        val fResponse = buildClientLookupAddress(path = s"country-picker")
          .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .post(Map("countryCode" -> Seq("XX"), "csrfToken" -> Seq("xxx-ignored-xxx")))

        val res = await(fResponse)
        res.status shouldBe SEE_OTHER
        res.header(HeaderNames.LOCATION).get shouldBe "/lookup-address/Jid123/international/edit"
      }
    }

    //    "submitted with an invalid country code" should {
    //      "redirect to the manual entry screen" in {
    //        stubKeystore(testJourneyId, Json.toJson(testJourneyDataWithMinimalJourneyConfigV2), OK)
    //        stubKeystoreSave(testJourneyId, Json.toJson(testJourneyDataWithMinimalJourneyConfigV2), OK)
    //
    //        val fResponse = buildClientLookupAddress(path = s"country-picker")
    //          .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
    //          .post(Map("countryCode" -> Seq("12"), "csrfToken" -> Seq("xxx-ignored-xxx")))
    //
    //        val res = await(fResponse)
    //        res.status shouldBe BAD_REQUEST
    //      }
    //    }
  }
}
