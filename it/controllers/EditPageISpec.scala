package controllers

import itutil.IntegrationSpecBase
import itutil.config.IntegrationTestConstants._
import itutil.config.PageElementConstants._
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.http.Status._
import play.api.test.FakeApplication

class EditPageISpec extends IntegrationSpecBase {

  override implicit lazy val app = FakeApplication(additionalConfiguration = fakeConfig())

  "The edit page" should {

    "when provided with no page config" should {

    "return UK edit page if uk param is true AND UK mode is false" in {
      stubKeystore(testJourneyId, testConfigWithAddressNotUkModeAsJson, OK)

      val fResponse = buildClientLookupAddress(path = "edit?uk=true")
        .withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
        .get()
      val res = await(fResponse)

      res.status shouldBe OK
      val document = Jsoup.parse(res.body)
      testElementExists(res, EditPage.ukEditId)
      document.title() shouldBe "Enter the address"
      document.getElementById("pageHeading").text() shouldBe "Enter the address"
      document.getElementById("continue").text() shouldBe "Continue"
      Option(document.getElementById("countryCode")).isDefined shouldBe false

      document.getElementById("line1").`val` shouldBe "1 High Street"
      document.getElementById("line2").`val` shouldBe "Line 2"
      document.getElementById("line3").`val` shouldBe "Line 3"
      document.getElementById("town").`val` shouldBe "Telford"
      document.getElementById("postcode").`val` shouldBe "AB11 1AB"


      labelForFieldsMatch(res, idOfFieldExpectedLabelTextForFieldMapping = Map(
        "line1" -> "Address line 1",
        "line2" -> "Address line 2",
        "line3" -> "Address line 3",
        "town" -> "Town/city",
        "postcode" -> "UK postcode (optional)"
      ))
    }
    "return UK edit page if no uk parameter provided AND UK mode is false" in {
      stubKeystore(testJourneyId, testConfigWithAddressNotUkModeAsJson, OK)

      val fResponse = buildClientLookupAddress(path = "edit")
        .withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
        .get()
      val res = await(fResponse)

      res.status shouldBe OK
      val document = Jsoup.parse(res.body)
      testElementExists(res, EditPage.nonUkEditId)
      document.title() shouldBe "Enter the address"
      document.getElementById("pageHeading").text() shouldBe "Enter the address"
      document.getElementById("continue").text() shouldBe "Continue"

      document.getElementById("line1").`val` shouldBe "1 High Street"
      document.getElementById("line2").`val` shouldBe "Line 2"
      document.getElementById("line3").`val` shouldBe "Line 3"
      document.getElementById("town").`val` shouldBe "Telford"
      document.getElementById("postcode").`val` shouldBe "AB11 1AB"


      labelForFieldsMatch(res, idOfFieldExpectedLabelTextForFieldMapping = Map(
              "line1" -> "Address line 1",
              "line2" -> "Address line 2",
              "line3" -> "Address line 3",
              "town" -> "Town/city",
              "postcode" -> "Postal code (optional)",
              "countryCode" -> "Country"
      ))
    }

    }

    "provided with custom content" should {

      "return UK edit page if uk param is true AND UK mode is false" in {
        stubKeystore(testJourneyId, testConfigWithAddressNotUkModeCustomEditConfigAsJson, OK)

        val fResponse = buildClientLookupAddress(path = "edit?uk=true")
          .withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()
        val res = await(fResponse)

        res.status shouldBe OK
        val document = Jsoup.parse(res.body)
        testElementExists(res, EditPage.ukEditId)
        document.title() shouldBe "Custom Title"
        document.getElementById("pageHeading").text() shouldBe "Custom Heading"
        document.getElementById("continue").text() shouldBe "Custom Continue"
        Option(document.getElementById("countryCode")).isDefined shouldBe false

        document.getElementById("line1").`val` shouldBe "1 High Street"
        document.getElementById("line2").`val` shouldBe "Line 2"
        document.getElementById("line3").`val` shouldBe "Line 3"
        document.getElementById("town").`val` shouldBe "Telford"
        document.getElementById("postcode").`val` shouldBe "AB11 1AB"


        labelForFieldsMatch(res, idOfFieldExpectedLabelTextForFieldMapping = Map(
          "line1" -> "Custom Line1",
          "line2" -> "Custom Line2",
          "line3" -> "Custom Line3",
          "town" -> "Custom Town",
          "postcode" -> "UK postcode (optional)"
        ))
      }
      "return UK edit page if no uk parameter provided AND UK mode is false" in {
        stubKeystore(testJourneyId, testConfigWithAddressNotUkModeCustomEditConfigAsJson, OK)

        val fResponse = buildClientLookupAddress(path = "edit")
          .withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()
        val res = await(fResponse)

        res.status shouldBe OK
        val document = Jsoup.parse(res.body)
        testElementExists(res, EditPage.nonUkEditId)
        document.title() shouldBe "Custom Title"
        document.getElementById("pageHeading").text() shouldBe "Custom Heading"
        document.getElementById("continue").text() shouldBe "Custom Continue"

        document.getElementById("line1").`val` shouldBe "1 High Street"
        document.getElementById("line2").`val` shouldBe "Line 2"
        document.getElementById("line3").`val` shouldBe "Line 3"
        document.getElementById("town").`val` shouldBe "Telford"
        document.getElementById("postcode").`val` shouldBe "AB11 1AB"


        labelForFieldsMatch(res, idOfFieldExpectedLabelTextForFieldMapping = Map(
          "line1" -> "Custom Line1",
          "line2" -> "Custom Line2",
          "line3" -> "Custom Line3",
          "town" -> "Custom Town",
          "postcode" -> "Custom Postcode",
          "countryCode" -> "Custom Country"
        ))
      }

    }

    }

  }