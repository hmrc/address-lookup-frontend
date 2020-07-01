package controllers

import itutil.IntegrationSpecBase
import itutil.config.IntegrationTestConstants._
import itutil.config.PageElementConstants.{EditPage, _}
import model.MessageConstants.{EnglishMessageConstants => EnglishMessages, WelshMessageConstants => WelshMessages}
import model.{EditPage => _, LookupPage => _, _}
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.http.Status._
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeApplication
import uk.gov.hmrc.address.v2.Country

class EditPageISpec extends IntegrationSpecBase {

  override implicit lazy val app = FakeApplication(additionalConfiguration = fakeConfig())
  val EnglishMessageConstants = EnglishMessages(true)
  val WelshMessageConstants = WelshMessages(true)

  "The edit page" should {
    "when provided with no page config for english and welsh" should {
      "return Non UK edit page if UK mode is false" in {
        stubKeystore(testJourneyId, journeyDataV2WithSelectedAddressJson(), OK)

        val fResponse = buildClientLookupAddress(path = "edit")
          .withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF,
            "Csrf-Token" -> "nocheck")
          .get()
        val res = await(fResponse)

        res.status shouldBe OK
        val document = Jsoup.parse(res.body)
        testElementExists(res, EditPage.nonUkEditId)
        document.title() shouldBe "Enter address"
        document.getElementById("pageHeading").text() shouldBe "Enter address"
        document.getElementById("continue").text() shouldBe "Continue"

        document.getElementById("line1").`val` shouldBe "1 High Street"
        document.getElementById("line2").`val` shouldBe "Line 2"
        document.getElementById("line3").`val` shouldBe "Line 3"
        document.getElementById("town").`val` shouldBe "Telford"
        document.getElementById("postcode").`val` shouldBe "AB11 1AB"

        labelForFieldsMatch(res, idOfFieldExpectedLabelTextForFieldMapping = Map(
          "line1" -> "Address line 1",
          "line2" -> "Address line 2 (optional)",
          "line3" -> "Address line 3 (optional)",
          "town" -> "Town/city",
          "postcode" -> "Postcode (optional)",
          "countryCode" -> "Country"
        ))
      }

      "return Non Uk edit page with default values where the 'PLAY_LANG' is set to cy but welsh config is not provided" in {
        val jc = fullDefaultJourneyConfigModelV2WithAllBooleansSet(false)
        val configWIthWelshEmptyBlock = journeyDataV2WithSelectedAddressJson(jc.copy(labels =
          Some(jc.labels.get.copy(cy =
            Some(LanguageLabels(
            ))
          ))))

        stubKeystore(testJourneyId, configWIthWelshEmptyBlock, OK)

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
        document.title() shouldBe "Nodwch cyfeiriad"
        document.getElementById("pageHeading").text() shouldBe "Nodwch cyfeiriad"
        document.getElementById("continue").text() shouldBe "Yn eich blaen"

        document.getElementById("line1").`val` shouldBe "1 High Street"
        document.getElementById("line2").`val` shouldBe "Line 2"
        document.getElementById("line3").`val` shouldBe "Line 3"
        document.getElementById("town").`val` shouldBe "Telford"
        document.getElementById("postcode").`val` shouldBe "AB11 1AB"

        labelForFieldsMatch(res, idOfFieldExpectedLabelTextForFieldMapping = Map(
          "line1" -> "Llinell cyfeiriad 1",
          "line2" -> "Llinell cyfeiriad 2 (dewisol)",
          "line3" -> "Llinell cyfeiriad 3 (dewisol)",
          "town" -> "Tref/dinas",
          "postcode" -> "Cod post (dewisol)",
          "countryCode" -> "Gwlad"
        ))
      }

      "return non uk edit and not error with showSearchAgainLink and searchAgainLinkText in the json should not error" in {
        val config = (Json.toJson(journeyDataV2Minimal).as[JsObject] - "editPage") ++
          Json.obj("editPage" -> Json.obj("showSearchAgainLink" -> true, "searchAgainLinkText" -> "foo"))
        stubKeystore(testJourneyId, Json.toJson(config).as[JsObject], OK)

        val fResponse = buildClientLookupAddress(path = "edit")
          .withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF,
            "Csrf-Token" -> "nocheck")
          .get()
        val res = await(fResponse)

        res.status shouldBe OK
        testElementExists(res, EditPage.nonUkEditId)
      }

      "redirect to the UK edit page if uk UK mode is true" in {
        val testConfigWithAddressAndUkMode = journeyDataV2Minimal.copy(
          selectedAddress = Some(
            ConfirmableAddress("foo", Some("bar"), ConfirmableAddressDetails(Some(List("wizz", "bang")), Some("fooP"), Some(Country("GB", "United Kingdom"))))
          ), config = journeyDataV2Minimal.config.copy(options = journeyDataV2Minimal.config.options.copy(ukMode = Some(true)))
        )
        stubKeystore(testJourneyId, Json.toJson(testConfigWithAddressAndUkMode).as[JsObject], OK)

        val fResponse = buildClientLookupAddress(path = "edit")
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

      "redirect to the International edit page if Uk mode is false but selected address in keystore" in {
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
      "return non UK edit page if uk mode is false and should display all default values from the welsh constants with the 'PLAY_LANG' set to cy" in {
        val jc = fullDefaultJourneyConfigModelV2WithAllBooleansSet(false)
        val configWIthWelshEmptyBlock = journeyDataV2WithSelectedAddressJson(jc.copy(labels =
          Some(jc.labels.get.copy(cy =
            Some(LanguageLabels(
            ))
          ))))

        stubKeystore(testJourneyId, configWIthWelshEmptyBlock, OK)

        val fResponse = buildClientLookupAddress(path = "edit")
          .withHeaders(
            HeaderNames.COOKIE -> sessionCookieWithCSRFAndLang(Some("cy")),
            "Csrf-Token" -> "nocheck")
          .get()
        val res = await(fResponse)

        res.status shouldBe OK
        val document = Jsoup.parse(res.body)
        testElementExists(res, EditPage.nonUkEditId)
        document.title() shouldBe "Nodwch cyfeiriad"
        document.getElementById("pageHeading").text() shouldBe "Nodwch cyfeiriad"
        document.getElementById("continue").text() shouldBe "Yn eich blaen"
        Option(document.getElementById("countryCode")).isDefined shouldBe true

        document.getElementById("line1").`val` shouldBe "1 High Street"
        document.getElementById("line2").`val` shouldBe "Line 2"
        document.getElementById("line3").`val` shouldBe "Line 3"
        document.getElementById("town").`val` shouldBe "Telford"
        document.getElementById("postcode").`val` shouldBe "AB11 1AB"

        labelForFieldsMatch(res, idOfFieldExpectedLabelTextForFieldMapping = Map(
          "line1" -> "Llinell cyfeiriad 1",
          "line2" -> "Llinell cyfeiriad 2 (dewisol)",
          "line3" -> "Llinell cyfeiriad 3 (dewisol)",
          "town" -> "Tref/dinas",
          "postcode" -> "Cod post (dewisol)"
        ))
        testCustomPartsOfGovWrapperElementsForDefaultConfig(fResponse)
      }
    }

    "provided with custom content" should {

      "return UK edit page if UK mode is false" in {
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

      "return non UK edit page if UK mode is false WITH NO 'PLAY_LANG' set" in {
        stubKeystore(testJourneyId, journeyDataV2WithSelectedAddressJson(journeyDataV2EditLabels(Some(false)).config), OK)

        val fResponse = buildClientLookupAddress(path = "edit")
          .withHeaders(HeaderNames.COOKIE -> getSessionCookie(Map("csrfToken" -> testCsrfToken())),
            "Csrf-Token" -> "nocheck")
          .get()
        val res = await(fResponse)

        res.status shouldBe OK
        val document = Jsoup.parse(res.body)
        testElementExists(res, EditPage.nonUkEditId)
        document.title() shouldBe "edit-title"
        document.getElementById("pageHeading").text() shouldBe "edit-heading"
        document.getElementById("continue").text() shouldBe "edit-submitLabel"
        Option(document.getElementById("countryCode")).isDefined shouldBe true

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
          "postcode" -> "Custom Postcode"
        ))
      }

      "return non UK edit page if UK mode is false WITH 'PLAY_LANG' set to cy AND welsh content provided" in {
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

        val fResponse = buildClientLookupAddress(path = "edit")
          .withHeaders(
            HeaderNames.COOKIE -> sessionCookieWithCSRFAndLang(Some("cy")),
            "Csrf-Token" -> "nocheck")
          .get()
        val res = await(fResponse)

        res.status shouldBe OK
        val document = Jsoup.parse(res.body)
        testElementExists(res, EditPage.nonUkEditId)
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
          "postcode" -> "Custom Postcode welsh"
        ))
      }

      "return non - UK edit page if UK mode is false WITH NO 'PLAY_LANG' set" in {
        stubKeystore(testJourneyId, journeyDataV2WithSelectedAddressJson(journeyDataV2EditLabels(Some(false)).config), OK)

        val fResponse = buildClientLookupAddress(path = "edit")
          .withHeaders(HeaderNames.COOKIE -> getSessionCookie(Map("csrfToken" -> testCsrfToken())),
            "Csrf-Token" -> "nocheck")
          .get()
        val res = await(fResponse)

        res.status shouldBe OK
        val document = Jsoup.parse(res.body)
        testElementExists(res, EditPage.nonUkEditId)
        document.title() shouldBe "edit-title"
        document.getElementById("pageHeading").text() shouldBe "edit-heading"
        document.getElementById("continue").text() shouldBe "edit-submitLabel"
        Option(document.getElementById("countryCode")).isDefined shouldBe true

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
          "postcode" -> "Custom Postcode"
        ))
      }

      "return non - UK edit page if UK mode is false WITH 'PLAY_LANG' set to cy AND welsh content provided" in {
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

        val fResponse = buildClientLookupAddress(path = "edit")
          .withHeaders(
            HeaderNames.COOKIE -> sessionCookieWithCSRFAndLang(Some("cy")),
            "Csrf-Token" -> "nocheck")
          .get()
        val res = await(fResponse)

        res.status shouldBe OK
        val document = Jsoup.parse(res.body)
        testElementExists(res, EditPage.nonUkEditId)
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
          "postcode" -> "Custom Postcode welsh",
          "countryCode" -> "Custom Country welsh"
        ))
      }
    }
  }

  "handleEditNonUk" should {
    "return 400 if all fields are missing and return nonUkEdit page with english text" in {
      stubKeystore(testJourneyId, Json.toJson(journeyDataV2Minimal.copy(config = journeyDataV2Minimal.config.copy(options = journeyDataV2Minimal.config.options.copy(ukMode = Some(false))))).as[JsObject], OK)

      val fResponse = buildClientLookupAddress(path = "edit")
        .withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
        .post(Map("csrfToken" -> Seq("xxx-ignored-xxx")))
      val res = await(fResponse)

      res.status shouldBe BAD_REQUEST
      labelForFieldsMatch(res, idOfFieldExpectedLabelTextForFieldMapping = Map(
        "line1" -> "Address line 1 Enter first line of address",
        "line2" -> "Address line 2 (optional)",
        "line3" -> "Address line 3 (optional)",
        "town" -> "Town/city Enter town or city of the address",
        "postcode" -> "Postcode (optional)",
        "countryCode" -> "Country Enter country of the address"))
      testElementExists(res, EditPage.nonUkEditId)
    }

    "return 400 if all fields are missing and return nonUkEdit page with welsh text" in {
      stubKeystore(
        session = testJourneyId,
        theData = Json.toJson(journeyDataV2Minimal.copy(
          config = journeyDataV2Minimal.config.copy(
            options = journeyDataV2Minimal.config.options.copy(ukMode = Some(false)),
            labels = Some(JourneyLabels(cy = Some(LanguageLabels())))))).as[JsObject], OK)

      val fResponse = buildClientLookupAddress(path = "edit").
        withHeaders(
          HeaderNames.COOKIE -> sessionCookieWithCSRFAndLang(Some("cy")),
          "Csrf-Token" -> "nocheck").
        post(Map("csrfToken" -> Seq("xxx-ignored-xxx")))
      val res = await(fResponse)
      val document = Jsoup.parse(res.body)

      document.title shouldBe "Gwall: Nodwch cyfeiriad"
      document.h1.text shouldBe "Nodwch cyfeiriad"
      document.submitButton.text shouldBe "Yn eich blaen"
      Option(document.getElementById("countryCode")).isDefined shouldBe true

      document.input("line1") should have (value(""))
      document.input("line2") should have (value(""))
      document.input("line3") should have (value(""))
      document.input("town") should have (value(""))
      document.input("postcode") should have (value(""))

      labelForFieldsMatch(res, idOfFieldExpectedLabelTextForFieldMapping = Map(
        "line1" -> "Llinell cyfeiriad 1 Nodwch linell gyntaf y cyfeiriad",
        "line2" -> "Llinell cyfeiriad 2 (dewisol)",
        "line3" -> "Llinell cyfeiriad 3 (dewisol)",
        "town" -> "Tref/dinas Nodwch dref neu ddinas y cyfeiriad",
        "postcode" -> "Cod post (dewisol)"
      ))

      res.status shouldBe BAD_REQUEST
      testElementExists(res, EditPage.nonUkEditId)
    }

    "return 400 if postcode is invalid and return nonUkEdit page with welsh text" in {
      stubKeystore(
        session = testJourneyId,
        theData = Json.toJson(journeyDataV2Minimal.copy(
          config = journeyDataV2Minimal.config.copy(
            options = journeyDataV2Minimal.config.options.copy(ukMode = Some(false)),
            labels = Some(JourneyLabels(cy = Some(LanguageLabels())))))).as[JsObject], OK)

      val fResponse = buildClientLookupAddress(path = "edit").
        withHeaders(
          HeaderNames.COOKIE -> sessionCookieWithCSRFAndLang(Some("cy")),
          "Csrf-Token" -> "nocheck").
        post(Map("csrfToken" -> Seq("xxx-ignored-xxx"), "postcode" -> Seq("eebb")))
      val res = await(fResponse)
      val document = Jsoup.parse(res.body)

      document.input("postcode") should have (value("eebb"))
      document.getElementById("postcode-error-summary").text() shouldBe "Nodwch god post sy’n ddilys"

      labelForFieldsMatch(res, idOfFieldExpectedLabelTextForFieldMapping = Map(
        "line1" -> "Llinell cyfeiriad 1 Nodwch linell gyntaf y cyfeiriad",
        "line2" -> "Llinell cyfeiriad 2 (dewisol)",
        "line3" -> "Llinell cyfeiriad 3 (dewisol)",
        "town" -> "Tref/dinas Nodwch dref neu ddinas y cyfeiriad",
        "postcode" -> "Cod post (dewisol) Nodwch god post sy’n ddilys"
      ))

      res.status shouldBe BAD_REQUEST
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
          config = journeyDataV2Minimal.config.copy(options = journeyDataV2Minimal.config.options.copy(ukMode = Some(true))))).as[JsObject], OK)

      val fResponse = buildClientLookupAddress(path = "edit").
        withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck").
        post(Map("csrfToken" -> Seq("xxx-ignored-xxx")))
      val res = await(fResponse)

      val document = Jsoup.parse(res.body)
      testElementExists(res, EditPage.ukEditId)

      document.title shouldBe "Error: Enter address"
      document.h1.text shouldBe "Enter address"
      document.submitButton.text shouldBe "Continue"
      testElementDoesntExist(res,"countryCode")

      document.input("line1") should have (value(""))
      document.input("line2") should have (value(""))
      document.input("line3") should have (value(""))
      document.input("town") should have (value(""))
      document.input("postcode") should have (value(""))

      labelForFieldsMatch(res, idOfFieldExpectedLabelTextForFieldMapping = Map(
        "line1" -> "Address line 1 Enter first line of address",
        "line2" -> "Address line 2 (optional)",
        "line3" -> "Address line 3 (optional)",
        "town" -> "Town/city Enter town or city of the address",
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
            options = journeyDataV2Minimal.config.options.copy(ukMode = Some(true)),
            labels = Some(JourneyLabels(cy = Some(LanguageLabels())))))).as[JsObject], OK)

      val fResponse = buildClientLookupAddress(path = "edit").
        withHeaders(
          HeaderNames.COOKIE -> sessionCookieWithCSRFAndLang(Some("cy")),
          "Csrf-Token" -> "nocheck").
        post(Map("csrfToken" -> Seq("xxx-ignored-xxx")))
      val res = await(fResponse)
      val document = Jsoup.parse(res.body)

      document.title shouldBe "Gwall: Nodwch cyfeiriad"
      document.h1.text shouldBe "Nodwch cyfeiriad"
      document.submitButton.text shouldBe "Yn eich blaen"
      testElementDoesntExist(res,"countryCode")

      document.input("line1") should have (value(""))
      document.input("line2") should have (value(""))
      document.input("line3") should have (value(""))
      document.input("town") should have (value(""))
      document.input("postcode") should have (value(""))

      labelForFieldsMatch(res, idOfFieldExpectedLabelTextForFieldMapping = Map(
        "line1" -> "Llinell cyfeiriad 1 Nodwch linell gyntaf y cyfeiriad",
        "line2" -> "Llinell cyfeiriad 2 (dewisol)",
        "line3" -> "Llinell cyfeiriad 3 (dewisol)",
        "town" -> "Tref/dinas Nodwch dref neu ddinas y cyfeiriad",
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

      val fResponse = buildClientLookupAddress(path = "edit").
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

  "technical difficulties" when {
    "the welsh content header isn't set and welsh object isn't provided in config" should {
      "render in English" in {
        stubKeystore(testJourneyId, testMinimalLevelJourneyConfigV2, INTERNAL_SERVER_ERROR)
        stubKeystoreSave(testJourneyId, testMinimalLevelJourneyConfigV2, INTERNAL_SERVER_ERROR)

        val fResponse = buildClientLookupAddress("edit")
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

        val fResponse = buildClientLookupAddress("edit")
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

        val fResponse = buildClientLookupAddress("edit")
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

        val fResponse = buildClientLookupAddress("edit")
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