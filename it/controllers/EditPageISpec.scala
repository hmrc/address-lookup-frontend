package controllers

import itutil.IntegrationSpecBase
import itutil.config.IntegrationTestConstants._
import itutil.config.PageElementConstants._
import model.{EditPage => _, LookupPage => _, _}
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.http.Status._
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeApplication
import uk.gov.hmrc.address.v2.Country

class EditPageISpec extends IntegrationSpecBase {

  override implicit lazy val app = FakeApplication(additionalConfiguration = fakeConfig())

  "The edit page" should {

    "when provided with no page config for english and welsh" should {

      "return UK edit page if uk param is true AND UK mode is false" in {
        stubKeystore(testJourneyId, journeyDataV2WithSelectedAddressJson(), OK)

        val fResponse = buildClientLookupAddress(path = "edit?uk=true")
          .withHeaders(
            HeaderNames.COOKIE -> sessionCookieWithCSRFAndLang(Some("en")),
            "Csrf-Token" -> "nocheck")
          .get()
        val res = await(fResponse)

        res.status shouldBe OK
        val document = Jsoup.parse(res.body)
        testElementExists(res, EditPage.ukEditId)
        document.title shouldBe "Enter the address"
        document.h1.text shouldBe "Enter the address"
        document.submitButton.text shouldBe "Continue"
        testElementDoesntExist(res,"countryCode")

        document.input("line1") should have (value("1 High Street"))
        document.input("line2") should have (value("Line 2"))
        document.input("line3") should have (value("Line 3"))
        document.input("town") should have (value("Telford"))
        document.input("postcode") should have (value("AB11 1AB"))

        labelForFieldsMatch(res, idOfFieldExpectedLabelTextForFieldMapping = Map(
          "line1" -> "Address line 1",
          "line2" -> "Address line 2",
          "line3" -> "Address line 3",
          "town" -> "Town/city",
          "postcode" -> "UK postcode (optional)"
        ))
      }

      "return Non UK edit page if no uk parameter provided AND UK mode is false" in {
        stubKeystore(testJourneyId, journeyDataV2WithSelectedAddressJson(), OK)

        val fResponse = buildClientLookupAddress(path = "edit")
          .withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF,
            "Csrf-Token" -> "nocheck")
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
      "return Non Uk edit page with default values where the 'PLAY_LANG' is set to cy but welsh config is not provided" in {
        stubKeystore(testJourneyId, journeyDataV2WithSelectedAddressJson(), OK)

        val fResponse = buildClientLookupAddress(path = "edit")
          .withHeaders(
            HeaderNames.COOKIE -> sessionCookieWithCSRFAndLang(Some("cy")),
            "Csrf-Token" -> "nocheck"
            )
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

      "return non uk edit and not error with showSearchAgainLink and searchAgainLinkText in the json should not error when uk param not provided" in {
        val config = (Json.toJson(journeyDataV2Minimal).as[JsObject] - "editPage") ++
          Json.obj("editPage" -> Json.obj("showSearchAgainLink" -> true, "searchAgainLinkText" -> "foo"))
        stubKeystore(testJourneyId, testConfigDefaultAsJson, OK)

        val fResponse = buildClientLookupAddress(path = "edit")
          .withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF,
            "Csrf-Token" -> "nocheck")
          .get()
        val res = await(fResponse)

        res.status shouldBe OK
        testElementExists(res, EditPage.nonUkEditId)
      }


      "return the UK edit page with no pre-popped postcode if param not provided AND UK mode is false but uk param provided" in {
        val testConfigWithAddressAndNotUkMode = journeyDataV2Minimal.copy(
          selectedAddress = None, config = journeyDataV2Minimal.config.copy(options = journeyDataV2Minimal.config.options.copy(ukMode = Some(false))))

        stubKeystore(testJourneyId, journeyDataV2WithSelectedAddressJson(), OK)
        val fResponse = buildClientLookupAddress(path = "edit?uk=true")
          .withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF,
            "Csrf-Token" -> "nocheck")
          .get()
        val res = await(fResponse)

        res.status shouldBe OK
        testElementExists(res, EditPage.ukEditId)
        testFormElementValuesMatch(res, Map(LookupPage.postcodeId -> testPostCode))
      }

      "redirect to the UK edit page if uk param provided and UK mode is true" in {
        val testConfigWithAddressAndUkMode = journeyDataV2Minimal.copy(
          selectedAddress = Some(
            ConfirmableAddress("foo", Some("bar"), ConfirmableAddressDetails(Some(List("wizz", "bang")), Some("fooP"), Some(Country("GB", "United Kingdom"))))
          ), config = journeyDataV2Minimal.config.copy(options = journeyDataV2Minimal.config.options.copy(ukMode = Some(true)))
        )
        stubKeystore(testJourneyId, Json.toJson(testConfigWithAddressAndUkMode).as[JsObject], OK)

        val fResponse = buildClientLookupAddress(path = "edit?uk=true")
          .withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF,
            "Csrf-Token" -> "nocheck")
          .get()
        val res = await(fResponse)

        res.status shouldBe OK
        testElementExists(res, EditPage.ukEditId)
      }

      "redirect to the UK edit page if country doesn't exist in selected address and AND UK mode is true" in {
        val testConfigWithAddressAndUkMode = journeyDataV2Minimal.copy(
          selectedAddress = Some(
            ConfirmableAddress("foo", Some("bar"), ConfirmableAddressDetails(Some(List("wizz", "bang")), Some("fooP")))
          ), config = journeyDataV2Minimal.config.copy(options = journeyDataV2Minimal.config.options.copy(ukMode = Some(true)))
        )
        stubKeystore(testJourneyId, Json.toJson(testConfigWithAddressAndUkMode).as[JsObject], OK)

        val fResponse = buildClientLookupAddress(path = "edit")
          .withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()
        val res = await(fResponse)

        res.status shouldBe OK
        testElementExists(res, EditPage.ukEditId)
      }

      "redirect to the International edit page if Uk mode is false and uk param not provided but selected address in keystore" in {
        stubKeystore(testJourneyId, journeyDataV2WithSelectedAddressJson(), OK)

        val fResponse = buildClientLookupAddress(path = "edit")
          .withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()
        val res = await(fResponse)

        res.status shouldBe OK
        testElementExists(res, EditPage.nonUkEditId)
      }
    }

    "provided with only custom content that has welsh block" should {
      "return UK edit page if uk param is true and should display all default values from the welsh constants with the 'PLAY_LANG' set to cy" in {
        val jc = fullDefaultJourneyConfigModelV2WithAllBooleansSet(false)
        val configWIthWelshEmptyBlock = journeyDataV2WithSelectedAddressJson(jc.copy(labels =
          Some(jc.labels.get.copy(cy =
            Some(LanguageLabels(
            ))
          ))))

        stubKeystore(testJourneyId, configWIthWelshEmptyBlock, OK)

        val fResponse = buildClientLookupAddress(path = "edit?uk=true")
          .withHeaders(
            HeaderNames.COOKIE -> sessionCookieWithCSRFAndLang(Some("cy")),
            "Csrf-Token" -> "nocheck")
          .get()
        val res = await(fResponse)

        res.status shouldBe OK
        val document = Jsoup.parse(res.body)
        testElementExists(res, EditPage.ukEditId)
        document.title() shouldBe "Nodwch y cyfeiriad"
        document.getElementById("pageHeading").text() shouldBe "Nodwch y cyfeiriad"
        document.getElementById("continue").text() shouldBe "Yn eich blaen"
        Option(document.getElementById("countryCode")).isDefined shouldBe false

        document.getElementById("line1").`val` shouldBe "1 High Street"
        document.getElementById("line2").`val` shouldBe "Line 2"
        document.getElementById("line3").`val` shouldBe "Line 3"
        document.getElementById("town").`val` shouldBe "Telford"
        document.getElementById("postcode").`val` shouldBe "AB11 1AB"


        labelForFieldsMatch(res, idOfFieldExpectedLabelTextForFieldMapping = Map(
          "line1" -> "Llinell cyfeiriad 1",
          "line2" -> "Llinell cyfeiriad 2",
          "line3" -> "Llinell cyfeiriad 3",
          "town" -> "Tref/dinas",
          "postcode" -> "Cod post y DU (dewisol)"
        ))
        testCustomPartsOfGovWrapperElementsForDefaultConfig(fResponse)
      }
    }
    "provided with custom content" should {

      "return UK edit page if uk param is true AND UK mode is false WITH NO 'PLAY_LANG' set" in {
        stubKeystore(testJourneyId, journeyDataV2WithSelectedAddressJson(journeyDataV2EditLabels(Some(false)).config), OK)

        val fResponse = buildClientLookupAddress(path = "edit?uk=true")
          .withHeaders(HeaderNames.COOKIE -> getSessionCookie(Map("csrfToken" -> testCsrfToken())),
            "Csrf-Token" -> "nocheck")
          .get()
        val res = await(fResponse)

        res.status shouldBe OK
        val document = Jsoup.parse(res.body)
        testElementExists(res, EditPage.ukEditId)
        document.title() shouldBe "edit-title"
        document.getElementById("pageHeading").text() shouldBe "edit-heading"
        document.getElementById("continue").text() shouldBe "edit-submitLabel"
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
        stubKeystore(testJourneyId, journeyDataV2WithSelectedAddressJson(journeyDataV2EditLabels(Some(false)).config), OK)

        val fResponse = buildClientLookupAddress(path = "edit")
          .withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()
        val res = await(fResponse)

        res.status shouldBe OK
        val document = Jsoup.parse(res.body)
        testElementExists(res, EditPage.nonUkEditId)
        document.title() shouldBe "edit-title"
        document.getElementById("pageHeading").text() shouldBe "edit-heading"
        document.getElementById("continue").text() shouldBe "edit-submitLabel"

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
      "return UK edit page if uk parameter provided AND UK mode is false WITH 'PLAY_LANG' set to cy AND welsh content provided" in {
        val jc = fullDefaultJourneyConfigModelV2WithAllBooleansSet(false)
        val configWithWelsh = journeyDataV2WithSelectedAddressJson(jc.copy(labels =
          Some(jc.labels.get.copy(cy =
            Some(LanguageLabels(
              editPageLabels = Some(EditPageLabels(
                title = Some("edit-title welsh"),
                heading = Some("edit-heading welsh"),
                line1Label = Some("Custom Line1 welsh"),
                line2Label = Some("Custom Line2 welsh"),
                line3Label = Some("Custom Line3 welsh"),
                townLabel = Some("Custom Town welsh"),
                postcodeLabel = Some("Custom Postcode welsh"),
                countryLabel = Some("Custom Country welsh"),
                submitLabel = Some("edit-submitLabel welsh")
              ))
            ))
          ))))


        stubKeystore(testJourneyId, configWithWelsh, OK)

        val fResponse = buildClientLookupAddress(path = "edit?uk=true")
          .withHeaders(
            HeaderNames.COOKIE -> sessionCookieWithCSRFAndLang(Some("cy")),
            "Csrf-Token" -> "nocheck")
          .get()
        val res = await(fResponse)

        res.status shouldBe OK
        val document = Jsoup.parse(res.body)
        testElementExists(res, EditPage.ukEditId)
        document.title() shouldBe "edit-title welsh"
        document.getElementById("pageHeading").text() shouldBe "edit-heading welsh"
        document.getElementById("continue").text() shouldBe "edit-submitLabel welsh"

        document.getElementById("line1").`val` shouldBe "1 High Street"
        document.getElementById("line2").`val` shouldBe "Line 2"
        document.getElementById("line3").`val` shouldBe "Line 3"
        document.getElementById("town").`val` shouldBe "Telford"
        document.getElementById("postcode").`val` shouldBe "AB11 1AB"


        labelForFieldsMatch(res, idOfFieldExpectedLabelTextForFieldMapping = Map(
          "line1" -> "Custom Line1 welsh",
          "line2" -> "Custom Line2 welsh",
          "line3" -> "Custom Line3 welsh",
          "town" -> "Custom Town welsh",
          "postcode" -> "Cod post y DU (dewisol)"
        ))
      }
    }
  }

  "handleEditNonUk" should {
    "return 400 if all fields are missing and return nonUkEdit page" in {
      stubKeystore(testJourneyId, Json.toJson(journeyDataV2Minimal.copy(config = journeyDataV2Minimal.config.copy(options = journeyDataV2Minimal.config.options.copy(ukMode = Some(false))))).as[JsObject], OK)

      val fResponse = buildClientLookupAddress(path = "edit")
        .withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
        .post(Map("csrfToken" -> Seq("xxx-ignored-xxx")))
      val res = await(fResponse)

      res.status shouldBe BAD_REQUEST
      labelForFieldsMatch(res, idOfFieldExpectedLabelTextForFieldMapping = Map(
        "line1" -> "This field is required Address line 1",
        "line2" -> "Address line 2"))
      testElementExists(res, EditPage.nonUkEditId)
    }
    s"return 303 if form is valid and redirect to ${controllers.routes.AddressLookupController.confirm("")}" in {
      stubKeystore(testJourneyId, Json.toJson(journeyDataV2Minimal.copy(config = journeyDataV2Minimal.config.copy(options = journeyDataV2Minimal.config.options.copy(ukMode = Some(false))))).as[JsObject], OK)
      stubKeystoreSave(
        testJourneyId,
        Json.toJson(journeyDataV2Minimal.copy(
          config = journeyDataV2Minimal.config.copy(options = journeyDataV2Minimal.config.options.copy(ukMode = Some(false))),
          selectedAddress = Some(testConfirmedAddress.copy(id = None))
        )),
        OK
      )
      val fResponse = buildClientLookupAddress(path = "edit")
        .withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
        .post(Map(
          "csrfToken" -> Seq("xxx-ignored-xxx"),
          "line1" -> Seq(testAddressLine1),
          "line2" -> Seq(testAddressLine2),
          "town" -> Seq(testAddressTown),
          "postcode" -> Seq(testPostCode),
          "countryCode" -> Seq("GB")
        ))
      val res = await(fResponse)

      res.status shouldBe SEE_OTHER
    }
  }

  "handleEditUkMode" should {
    "return 400 if postcode is missing and return uk edit mode page with english text" in {
      stubKeystore(
        session = testJourneyId,
        theData = Json.toJson(journeyDataV2Minimal.copy(
          config = journeyDataV2Minimal.config.copy(options = journeyDataV2Minimal.config.options.copy(ukMode = Some(false))))).as[JsObject], OK)

      val fResponse = buildClientLookupAddress("ukEdit").
        withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck").
        post(Map("csrfToken" -> Seq("xxx-ignored-xxx")))
      val res = await(fResponse)

      val document = Jsoup.parse(res.body)
      testElementExists(res, EditPage.ukEditId)

      document.title shouldBe "Enter the address"
      document.h1.text shouldBe "Enter the address"
      document.submitButton.text shouldBe "Continue"
      testElementDoesntExist(res,"countryCode")

      document.input("line1") should have (value(""))
      document.input("line2") should have (value(""))
      document.input("line3") should have (value(""))
      document.input("town") should have (value(""))
      document.input("postcode") should have (value(""))

      labelForFieldsMatch(res, idOfFieldExpectedLabelTextForFieldMapping = Map(
        "line1" -> "This field is required Address line 1",
        "line2" -> "Address line 2",
        "line3" -> "Address line 3",
        "town" -> "This field is required Town/city",
        "postcode" -> "UK postcode (optional)"
      ))

      res.status shouldBe BAD_REQUEST
      testElementExists(res, EditPage.ukEditId)
    }
    "return 400 if postcode is missing and return uk edit mode page with welsh text" in {
      stubKeystore(
        session = testJourneyId,
        theData = Json.toJson(journeyDataV2Minimal.copy(
          config = journeyDataV2Minimal.config.copy(
            options = journeyDataV2Minimal.config.options.copy(ukMode = Some(false)),
            labels = Some(JourneyLabels(cy = Some(LanguageLabels())))))).as[JsObject], OK)

      val fResponse = buildClientLookupAddress("ukEdit").
        withHeaders(
          HeaderNames.COOKIE -> sessionCookieWithCSRFAndLang(Some("cy")),
          "Csrf-Token" -> "nocheck").
        post(Map("csrfToken" -> Seq("xxx-ignored-xxx")))
      val res = await(fResponse)
      val document = Jsoup.parse(res.body)

      document.title shouldBe "Nodwch y cyfeiriad"
      document.h1.text shouldBe "Nodwch y cyfeiriad"
      document.submitButton.text shouldBe "Yn eich blaen"
      testElementDoesntExist(res,"countryCode")

      document.input("line1") should have (value(""))
      document.input("line2") should have (value(""))
      document.input("line3") should have (value(""))
      document.input("town") should have (value(""))
      document.input("postcode") should have (value(""))

      labelForFieldsMatch(res, idOfFieldExpectedLabelTextForFieldMapping = Map(
        "line1" -> "This field is required Llinell cyfeiriad 1",
        "line2" -> "Llinell cyfeiriad 2",
        "line3" -> "Llinell cyfeiriad 3",
        "town" -> "This field is required Tref/dinas",
        "postcode" -> "Cod post y DU (dewisol)"
      ))

      res.status shouldBe BAD_REQUEST
      testElementExists(res, EditPage.ukEditId)
    }

    "return 303 if form is valid and redirect to Confirm" in {
      stubKeystore(
        session = testJourneyId,
        theData = Json.toJson(journeyDataV2Minimal.copy(
          config = journeyDataV2Minimal.config.copy(options = journeyDataV2Minimal.config.options.copy(ukMode = Some(false))))),
        status = OK)

      stubKeystore(
        testJourneyId,
        Json.toJson(journeyDataV2Minimal.copy(
          config = journeyDataV2Minimal.config.copy(options = journeyDataV2Minimal.config.options.copy(ukMode = Some(false))))),
        OK
      )
      stubKeystoreSave(
        testJourneyId,
        Json.toJson(journeyDataV2Minimal.copy(
          config = journeyDataV2Minimal.config.copy(options = journeyDataV2Minimal.config.options.copy(ukMode = Some(false))),
          selectedAddress = Some(testConfirmedAddress.copy(id = None))
        )),
        OK
      )

      val fResponse = buildClientLookupAddress("ukEdit").
        withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck").
        post(Map(
          "csrfToken" -> Seq("xxx-ignored-xxx"),
          "line1" -> Seq(testAddressLine1),
          "line2" -> Seq(testAddressLine2),
          "town" -> Seq(testAddressTown),
          "postcode" -> Seq(testPostCode),
          "countryCode" -> Seq("GB")
        ))
      val res = await(fResponse)

      res.status shouldBe SEE_OTHER
    }
  }

}