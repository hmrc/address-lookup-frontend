package controllers.international

import itutil.IntegrationSpecBase
import itutil.config.IntegrationTestConstants._
import model._
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.http.Status._
import play.api.i18n.Lang
import play.api.libs.json.{JsObject, Json}

class EditPageISpec extends IntegrationSpecBase {

  "The edit page" should {
    "when provided with no page config for english and welsh" should {
      "return edit page" in {
        stubKeystore(testJourneyId, journeyDataV2WithSelectedAddressJson(countryCode = Some("BM")), OK)

        val fResponse = buildClientLookupAddress(path = "international/edit")
          .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF,
            "Csrf-Token" -> "nocheck")
          .get()
        val res = await(fResponse)

        res.status shouldBe OK
        val document = Jsoup.parse(res.body)
        //testElementExists(res, EditPage.nonUkEditId)
        document.title() shouldBe "Enter address"
        document.getElementById("pageHeading").text() shouldBe "Enter address"
        document.getElementById("pageHeading").classNames() should contain("govuk-heading-xl")
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
          "town" -> "Town or city",
          "postcode" -> "Postcode (optional)",
          "countryName" -> "Country"
        ))
      }

      "return edit page with default values where the 'PLAY_LANG' is set to cy but welsh config is not provided" in {
        val jc = fullDefaultJourneyConfigModelV2WithAllBooleansSet(false)
        val configWIthWelshEmptyBlock = journeyDataV2WithSelectedAddressJson(jc.copy(labels =
          Some(jc.labels.get.copy(cy = Some(LanguageLabels())))), countryCode = Some("BM"))

        stubKeystore(testJourneyId, configWIthWelshEmptyBlock, OK)

        val fResponse = buildClientLookupAddress(path = "international/edit")
          .withHttpHeaders(
            HeaderNames.COOKIE -> sessionCookieWithCSRFAndLang(Some("cy")),
            "Csrf-Token" -> "nocheck")
          .get()

        val res = await(fResponse)

        res.status shouldBe OK
        val document = Jsoup.parse(res.body)
        //testElementExists(res, EditPage.nonUkEditId)
        document.title() shouldBe "Nodwch gyfeiriad"
        document.getElementById("pageHeading").text() shouldBe "Nodwch gyfeiriad"
        document.getElementById("continue").text() shouldBe "Yn eich blaen"

        document.getElementById("line1").`val` shouldBe "1 High Street"
        document.getElementById("line2").`val` shouldBe "Line 2"
        document.getElementById("line3").`val` shouldBe "Line 3"
        document.getElementById("town").`val` shouldBe "Telford"
        document.getElementById("postcode").`val` shouldBe "AB11 1AB"

        labelForFieldsMatch(res, idOfFieldExpectedLabelTextForFieldMapping = Map(
          "line1" -> "Cyfeiriad – llinell 1",
          "line2" -> "Cyfeiriad – llinell 2",
          "line3" -> "Cyfeiriad – llinell 3",
          "town" -> "Tref/dinas",
          "postcode" -> "Cod post (dewisol)",
          "countryName" -> "Gwlad"
        ))
      }

      "return edit page and not error with showSearchAgainLink and searchAgainLinkText in the json should not error" in {
        val config = (Json.toJson(journeyDataV2Minimal.copy(countryCode = Some("BM"))).as[JsObject] - "editPage") ++
          Json.obj("editPage" -> Json.obj("showSearchAgainLink" -> true, "searchAgainLinkText" -> "foo"))
        stubKeystore(testJourneyId, Json.toJson(config).as[JsObject], OK)

        val fResponse = buildClientLookupAddress(path = "international/edit")
          .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF,
            "Csrf-Token" -> "nocheck")
          .get()
        val res = await(fResponse)

        res.status shouldBe OK
        //testElementExists(res, EditPage.nonUkEditId)
      }

      "allow the initialising service to override the header size" in {
        stubKeystore(testJourneyId, journeyDataV2WithSelectedAddressJson(journeyConfigV2 =
          JourneyConfigV2(2, JourneyOptions(testContinueUrl, pageHeadingStyle = Some("govuk-heading-l"))), countryCode = Some("BM")), OK)

        val fResponse = buildClientLookupAddress(path = "international/edit")
          .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF,
            "Csrf-Token" -> "nocheck")
          .get()
        val res = await(fResponse)

        res.status shouldBe OK
        val document = Jsoup.parse(res.body)
        document.getElementById("pageHeading").classNames() should contain("govuk-heading-l")
      }
    }

    "provided with only custom content that has welsh block" should {
      "return edit page and should display all default values from the welsh constants with the 'PLAY_LANG' set to cy" in {
        val jc = fullDefaultJourneyConfigModelV2WithAllBooleansSet(false)
        val configWIthWelshEmptyBlock = journeyDataV2WithSelectedAddressJson(jc.copy(labels =
          Some(jc.labels.get.copy(cy =
            Some(LanguageLabels(
            ))
          ))), countryCode = Some("BM"))

        stubKeystore(testJourneyId, configWIthWelshEmptyBlock, OK)

        val fResponse = buildClientLookupAddress(path = "international/edit")
          .withHttpHeaders(
            HeaderNames.COOKIE -> sessionCookieWithCSRFAndLang(Some("cy")),
            "Csrf-Token" -> "nocheck")
          .get()
        val res = await(fResponse)

        res.status shouldBe OK
        val document = Jsoup.parse(res.body)
        //        //testElementExists(res, EditPage.nonUkEditId)
        document.title() shouldBe "Nodwch gyfeiriad"
        document.getElementById("pageHeading").text() shouldBe "Nodwch gyfeiriad"
        document.getElementById("continue").text() shouldBe "Yn eich blaen"
        Option(document.getElementById("countryName")).isDefined shouldBe true

        document.getElementById("line1").`val` shouldBe "1 High Street"
        document.getElementById("line2").`val` shouldBe "Line 2"
        document.getElementById("line3").`val` shouldBe "Line 3"
        document.getElementById("town").`val` shouldBe "Telford"
        document.getElementById("postcode").`val` shouldBe "AB11 1AB"

        labelForFieldsMatch(res, idOfFieldExpectedLabelTextForFieldMapping = Map(
          "line1" -> "Cyfeiriad – llinell 1",
          "line2" -> "Cyfeiriad – llinell 2",
          "line3" -> "Cyfeiriad – llinell 3",
          "town" -> "Tref/dinas",
          "postcode" -> "Cod post (dewisol)"
        ))
        testCustomPartsOfGovWrapperElementsForDefaultConfig(fResponse)
      }
    }

    "provided with custom content" should {

      "return edit page" in {
        stubKeystore(testJourneyId, journeyDataV2WithSelectedAddressJson(journeyDataV2EditLabels(Some(false)).config, countryCode = Some("BM")), OK)

        val fResponse = buildClientLookupAddress(path = "international/edit")
          .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()
        val res = await(fResponse)

        res.status shouldBe OK
        val document = Jsoup.parse(res.body)
        document.title() shouldBe "international-edit-title"
        document.getElementById("pageHeading").text() shouldBe "international-edit-heading"
        document.getElementById("continue").text() shouldBe "international-edit-submitLabel"

        document.getElementById("line1").`val` shouldBe "1 High Street"
        document.getElementById("line2").`val` shouldBe "Line 2"
        document.getElementById("line3").`val` shouldBe "Line 3"
        document.getElementById("town").`val` shouldBe "Telford"
        document.getElementById("postcode").`val` shouldBe "AB11 1AB"

        labelForFieldsMatch(res, idOfFieldExpectedLabelTextForFieldMapping = Map(
          "line1" -> "International Custom Line1",
          "line2" -> "International Custom Line2",
          "line3" -> "International Custom Line3",
          "town" -> "International Custom Town",
          "postcode" -> "International Custom Postcode",
          "countryName" -> "International Custom Country"
        ))
      }

      "return edit page WITH NO 'PLAY_LANG' set" in {
        stubKeystore(testJourneyId, journeyDataV2WithSelectedAddressJson(journeyDataV2EditLabels(Some(false)).config, countryCode = Some("BM")), OK)

        val fResponse = buildClientLookupAddress(path = "international/edit")
          .withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(Map("csrfToken" -> testCsrfToken())),
            "Csrf-Token" -> "nocheck")
          .get()
        val res = await(fResponse)

        res.status shouldBe OK
        val document = Jsoup.parse(res.body)
        document.title() shouldBe "international-edit-title"
        document.getElementById("pageHeading").text() shouldBe "international-edit-heading"
        document.getElementById("continue").text() shouldBe "international-edit-submitLabel"
        Option(document.getElementById("countryName")).isDefined shouldBe true

        document.getElementById("line1").`val` shouldBe "1 High Street"
        document.getElementById("line2").`val` shouldBe "Line 2"
        document.getElementById("line3").`val` shouldBe "Line 3"
        document.getElementById("town").`val` shouldBe "Telford"
        document.getElementById("postcode").`val` shouldBe "AB11 1AB"

        labelForFieldsMatch(res, idOfFieldExpectedLabelTextForFieldMapping = Map(
          "line1" -> "International Custom Line1",
          "line2" -> "International Custom Line2",
          "line3" -> "International Custom Line3",
          "town" -> "International Custom Town",
          "postcode" -> "International Custom Postcode"
        ))
      }

      "return edit page WITH 'PLAY_LANG' set to cy AND welsh content provided" in {
        val jc = fullDefaultJourneyConfigModelV2WithAllBooleansSet(false)
        val configWithWelsh = journeyDataV2WithSelectedAddressJson(jc.copy(labels =
          Some(jc.labels.get.copy(cy =
            Some(LanguageLabels(
              international = Some(InternationalLanguageLabels(
                editPageLabels = Some(InternationalEditPageLabels(
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
            ))))), countryCode = Some("BM"))

        stubKeystore(testJourneyId, configWithWelsh, OK)

        val fResponse = buildClientLookupAddress(path = "international/edit")
          .withHttpHeaders(
            HeaderNames.COOKIE -> sessionCookieWithCSRFAndLang(Some("cy")),
            "Csrf-Token" -> "nocheck")
          .get()
        val res = await(fResponse)

        res.status shouldBe OK
        val document = Jsoup.parse(res.body)
        //testElementExists(res, EditPage.nonUkEditId)
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
    }
  }

  "handleEdit" should {
    "return 400 if all fields are missing and return edit page with english text" in {
      stubKeystore(testJourneyId, Json.toJson(journeyDataV2Minimal.copy(config = journeyDataV2Minimal.config.copy(options = journeyDataV2Minimal.config.options.copy(ukMode = Some(false))), countryCode = Some("BM"))).as[JsObject], OK)

      val fResponse = buildClientLookupAddress(path = "international/edit")
        .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
        .post(Map("csrfToken" -> Seq("xxx-ignored-xxx")))
      val res = await(fResponse)

      res.status shouldBe BAD_REQUEST
      labelForFieldsMatch(res, idOfFieldExpectedLabelTextForFieldMapping = Map(
        "line1" -> "Address line 1",
        "line2" -> "Address line 2",
        "line3" -> "Address line 3",
        "town" -> "Town or city",
        "postcode" -> "Postcode (optional)",
        "countryName" -> "Country"))
    }

    "return 400 if all fields are missing and return edit page with welsh text" in {
      stubKeystore(
        session = testJourneyId,
        theData = Json.toJson(journeyDataV2Minimal.copy(
          config = journeyDataV2Minimal.config.copy(
            options = journeyDataV2Minimal.config.options.copy(ukMode = Some(false)),
            labels = Some(JourneyLabels(cy = Some(LanguageLabels())))), countryCode = Some("BM"))).as[JsObject], OK)

      val fResponse = buildClientLookupAddress(path = "international/edit").
        withHttpHeaders(
          HeaderNames.COOKIE -> sessionCookieWithCSRFAndLang(Some("cy")),
          "Csrf-Token" -> "nocheck").
        post(Map("csrfToken" -> Seq("xxx-ignored-xxx")))

      val res = await(fResponse)
      val document = Jsoup.parse(res.body)

      document.title shouldBe "Gwall: Nodwch gyfeiriad"
      document.h1.text shouldBe "Nodwch gyfeiriad"
      document.submitButton.text shouldBe "Yn eich blaen"
      Option(document.getElementById("countryName")).isDefined shouldBe true

      document.input("line1") should have(value(""))
      document.input("line2") should have(value(""))
      document.input("line3") should have(value(""))
      document.input("town") should have(value(""))
      document.input("postcode") should have(value(""))

      labelForFieldsMatch(res, idOfFieldExpectedLabelTextForFieldMapping = Map(
        "line1" -> "Cyfeiriad – llinell 1",
        "line2" -> "Cyfeiriad – llinell 2",
        "line3" -> "Cyfeiriad – llinell 3",
        "town" -> "Tref/dinas",
        "postcode" -> "Cod post (dewisol)"
      ))

      res.status shouldBe BAD_REQUEST
    }

    "return 400 if postcode is invalid and return edit page with welsh text" in {
      stubKeystore(
        session = testJourneyId,
        theData = Json.toJson(journeyDataV2Minimal.copy(
          config = journeyDataV2Minimal.config.copy(
            options = journeyDataV2Minimal.config.options.copy(ukMode = Some(false)),
            labels = Some(JourneyLabels(cy = Some(LanguageLabels())))), countryCode = Some("BM"))).as[JsObject], OK)

      val fResponse = buildClientLookupAddress(path = "international/edit").
        withHttpHeaders(
          HeaderNames.COOKIE -> sessionCookieWithCSRFAndLang(Some("cy")),
          "Csrf-Token" -> "nocheck").
        post(Map("csrfToken" -> Seq("xxx-ignored-xxx"), "postcode" -> Seq("eebb")))
      val res = await(fResponse)
      val document = Jsoup.parse(res.body)

      document.input("postcode") should have(value("eebb"))
      //      document.getElementById("postcode-error-summary").text() shouldBe "Nodwch god post sy’n ddilys"

      labelForFieldsMatch(res, idOfFieldExpectedLabelTextForFieldMapping = Map(
        "line1" -> "Cyfeiriad – llinell 1",
        "line2" -> "Cyfeiriad – llinell 2",
        "line3" -> "Cyfeiriad – llinell 3",
        "town" -> "Tref/dinas",
        "postcode" -> "Cod post (dewisol)"
      ))

      res.status shouldBe BAD_REQUEST
      //testElementExists(res, EditPage.nonUkEditId)
    }

    s"return 303 if form is valid and redirect to ${controllers.routes.InternationalAddressLookupController.confirm("")}" in {
      stubKeystore(testJourneyId, Json.toJson(journeyDataV2Minimal.copy(config = journeyDataV2Minimal.config.copy(options = journeyDataV2Minimal.config.options.copy(ukMode = Some(false))), countryCode = Some("BM"))).as[JsObject], OK)
      stubKeystoreSave(
        testJourneyId,
        Json.toJson(journeyDataV2Minimal.copy(
          config = journeyDataV2Minimal.config.copy(options = journeyDataV2Minimal.config.options.copy(ukMode = Some(false))),
          selectedAddress = Some(testConfirmedAddress.copy(id = None)),
          countryCode = Some("BM")
        )),
        OK
      )

      val fResponse = buildClientLookupAddress(path = "international/edit")
        .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
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


  "technical difficulties" when {
    "the welsh content header isn't set and welsh object isn't provided in config" should {
      "render in English" in {
        stubKeystore(testJourneyId, Json.toJson(testMinimalLevelJourneyDataV2.copy(countryCode = Some("BM"))), INTERNAL_SERVER_ERROR)
        stubKeystoreSave(testJourneyId, Json.toJson(testMinimalLevelJourneyDataV2.copy(countryCode = Some("BM"))), INTERNAL_SERVER_ERROR)

        val fResponse = buildClientLookupAddress("edit")
          .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()

        val res = await(fResponse)
        res.status shouldBe INTERNAL_SERVER_ERROR

        val doc = getDocFromResponse(res)
        doc.title shouldBe messages("constants.intServerErrorTitle")
        doc.h1 should have(text(messages("constants.intServerErrorTitle")))
        doc.paras should have(elementWithValue(messages("constants.intServerErrorTryAgain")))
      }
    }

    "the welsh content header is set to false and welsh object isn't provided in config" should {
      "render in English" in {
        stubKeystore(testJourneyId, Json.toJson(testMinimalLevelJourneyDataV2.copy(countryCode = Some("BM"))), INTERNAL_SERVER_ERROR)
        stubKeystoreSave(testJourneyId, Json.toJson(testMinimalLevelJourneyDataV2.copy(countryCode = Some("BM"))), INTERNAL_SERVER_ERROR)

        val fResponse = buildClientLookupAddress("edit")
          .withHttpHeaders(
            HeaderNames.COOKIE -> sessionCookieWithWelshCookie(useWelsh = false),
            "Csrf-Token" -> "nocheck"
          )
          .get()

        val res = await(fResponse)
        res.status shouldBe INTERNAL_SERVER_ERROR

        val doc = getDocFromResponse(res)
        doc.title shouldBe messages("constants.intServerErrorTitle")
        doc.h1 should have(text(messages("constants.intServerErrorTitle")))
        doc.paras should have(elementWithValue(messages("constants.intServerErrorTryAgain")))
      }
    }

    "the welsh content header is set to false and welsh object is provided in config" should {
      "render in English" in {
        val v2Config = Json.toJson(fullDefaultJourneyConfigModelV2WithAllBooleansSet(allBooleanSetAndAppropriateOptions = true, isWelsh = true))
        stubKeystore(testJourneyId, v2Config, INTERNAL_SERVER_ERROR)
        stubKeystoreSave(testJourneyId, v2Config, INTERNAL_SERVER_ERROR)

        val fResponse = buildClientLookupAddress("edit")
          .withHttpHeaders(
            HeaderNames.COOKIE -> sessionCookieWithWelshCookie(useWelsh = false),
            "Csrf-Token" -> "nocheck"
          )
          .get()

        val res = await(fResponse)
        res.status shouldBe INTERNAL_SERVER_ERROR

        val doc = getDocFromResponse(res)
        doc.title shouldBe messages("constants.intServerErrorTitle")
        doc.h1 should have(text(messages("constants.intServerErrorTitle")))
        doc.paras should have(elementWithValue(messages("constants.intServerErrorTryAgain")))
      }
    }

    "the welsh content header is set to true and welsh object provided in config" should {
      "render in Welsh" in {
        val v2Config = Json.toJson(fullDefaultJourneyConfigModelV2WithAllBooleansSet(allBooleanSetAndAppropriateOptions = true, isWelsh = true))
        stubKeystore(testJourneyId, v2Config, INTERNAL_SERVER_ERROR)
        stubKeystoreSave(testJourneyId, v2Config, INTERNAL_SERVER_ERROR)

        val fResponse = buildClientLookupAddress("edit")
          .withHttpHeaders(
            HeaderNames.COOKIE -> sessionCookieWithCSRFAndLang(),
            "Csrf-Token" -> "nocheck"
          )
          .get()

        val res = await(fResponse)
        res.status shouldBe INTERNAL_SERVER_ERROR

        val doc = getDocFromResponse(res)
        doc.title shouldBe messages(Lang("cy"), "constants.intServerErrorTitle")
        doc.h1 should have(text(messages(Lang("cy"), "constants.intServerErrorTitle")))
        doc.paras should have(elementWithValue(messages(Lang("cy"), "constants.intServerErrorTryAgain")))
      }
    }
  }
}
