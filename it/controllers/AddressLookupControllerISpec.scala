package controllers

import itutil.IntegrationSpecBase
import itutil.config.IntegrationTestConstants._
import itutil.config.PageElementConstants._
import model.{ConfirmableAddress, ConfirmableAddressDetails}
import play.api.http.HeaderNames
import play.api.http.Status._
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeApplication
import uk.gov.hmrc.address.v2.Country

class AddressLookupControllerISpec extends IntegrationSpecBase {

  override implicit lazy val app = FakeApplication(additionalConfiguration = fakeConfig())

  "The lookup page" should {
    "pre-pop the postcode and filter on the view when they are passed in as query parameters and drop selected address on load" in {
      stubKeystore(testJourneyId, testMinimalLevelJourneyConfigV2, OK)
      stubKeystoreSave(testJourneyId, testMinimalLevelJourneyConfigV2, OK)

      val fResponse = buildClientLookupAddress(path = "lookup?postcode=AB11+1AB&filter=bar")
        .withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
        .get()
      val res = await(fResponse)

      res.status shouldBe OK
      testFormElementValuesMatch(res, Map(LookupPage.postcodeId -> testPostCode, LookupPage.filterId -> testFilterValue))
    }

    "pre-pop the postcode only on the view when it is passed in as a query parameters" in {
      stubKeystore(testJourneyId, testMinimalLevelJourneyConfigV2, OK)
      stubKeystoreSave(testJourneyId, testMinimalLevelJourneyConfigV2, OK)

      val fResponse = buildClientLookupAddress(path = "lookup?postcode=AB11 1AB")
        .withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
        .get()
      val res = await(fResponse)

      res.status shouldBe OK
      testFormElementValuesMatch(res, Map(LookupPage.postcodeId -> testPostCode, LookupPage.filterId -> ""))
    }

    "pre-pop the filter only on the view when it is passed in as a query parameters" in {
      stubKeystore(testJourneyId, testMinimalLevelJourneyConfigV2, OK)
      stubKeystoreSave(testJourneyId, testMinimalLevelJourneyConfigV2, OK)

      val fResponse = buildClientLookupAddress(path = "lookup?filter=bar")
        .withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
        .get()
      val res = await(fResponse)

      res.status shouldBe OK
      testFormElementValuesMatch(res, Map(LookupPage.postcodeId -> "", LookupPage.filterId -> testFilterValue))
    }

    "not pre-pop the filter or postcode fields when no query parameters are used " in {
      stubKeystore(testJourneyId, testMinimalLevelJourneyConfigV2, OK)
      stubKeystoreSave(testJourneyId, testMinimalLevelJourneyConfigV2, OK)

      val fResponse = buildClientLookupAddress(path = "lookup")
        .withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
        .get()
      val res = await(fResponse)

      res.status shouldBe OK
      testFormElementValuesMatch(res, Map(LookupPage.postcodeId -> "", LookupPage.filterId -> ""))
    }
  }

  "The edit page" should {
    "return UK edit page if uk param is true AND UK mode is false" in {
      stubKeystore(testJourneyId, testConfigWithAddressNotUkModeAsJson, OK)

      val fResponse = buildClientLookupAddress(path = "edit?uk=true")
        .withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
        .get()
      val res = await(fResponse)

      res.status shouldBe OK
      testElementExists(res, EditPage.ukEditId)
    }

    "return non uk edit and not error with showSearchAgainLink and searchAgainLinkText in the json should not error when uk param not provided" in {
      val config = (Json.toJson(testJourneyDataWithMinimalJourneyConfig).as[JsObject] - "editPage") ++
        Json.obj("editPage" -> Json.obj("showSearchAgainLink" -> true, "searchAgainLinkText" -> "foo"))
      stubKeystore(testJourneyId, testConfigDefaultAsJson, OK)

      val fResponse = buildClientLookupAddress(path = "edit")
        .withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
        .get()
      val res = await(fResponse)

      res.status shouldBe OK
      testElementExists(res, EditPage.nonUkEditId)
    }

    "return the UK edit page with the lookup postcode uk param true AND UK mode is false" in {
      stubKeystore(testJourneyId, testConfigWithAddressNotUkModeAsJson, OK)

      val fResponse = buildClientLookupAddress(path = "edit?lookUpPostCode=AB11+++1AB&uk=true")
        .withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
        .get()
      val res = await(fResponse)

      res.status shouldBe OK

      testElementExists(res, EditPage.ukEditId)
      testFormElementValuesMatch(res, Map(LookupPage.postcodeId -> testPostCode))
    }

    "return the UK edit page with no pre-popped postcode if param not provided AND UK mode is false but uk param provided" in {
      val testConfigWithAddressAndNotUkMode = testJourneyDataWithMinimalJourneyConfig.copy(
        selectedAddress = None, config = testJourneyDataWithMinimalJourneyConfig.config.copy(ukMode = Some(false))
      )

      stubKeystore(testJourneyId, testConfigWithAddressNotUkModeAsJson, OK)
      val fResponse = buildClientLookupAddress(path = "edit?uk=true")
        .withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
        .get()
      val res = await(fResponse)

      res.status shouldBe OK
      testElementExists(res, EditPage.ukEditId)
      testFormElementValuesMatch(res, Map(LookupPage.postcodeId -> testPostCode))
    }

    "redirect to the UK edit page if uk param provided and UK mode is true" in {
      val testConfigWithAddressAndUkMode = testJourneyDataWithMinimalJourneyConfig.copy(
        selectedAddress = Some(
          ConfirmableAddress("foo", Some("bar"), ConfirmableAddressDetails(Some(List("wizz", "bang")), Some("fooP"), Some(Country("GB", "United Kingdom"))))
        ), config = testJourneyDataWithMinimalJourneyConfig.config.copy(ukMode = Some(true))
      )
      stubKeystore(testJourneyId, Json.toJson(testConfigWithAddressAndUkMode).as[JsObject], OK)

      val fResponse = buildClientLookupAddress(path = "edit?uk=true")
        .withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
        .get()
      val res = await(fResponse)

      res.status shouldBe OK
      testElementExists(res, EditPage.ukEditId)
    }

    "redirect to the UK edit page if country doesn't exist in selected address and AND UK mode is true" in {
      val testConfigWithAddressAndUkMode = testJourneyDataWithMinimalJourneyConfig.copy(
        selectedAddress = Some(
          ConfirmableAddress("foo", Some("bar"), ConfirmableAddressDetails(Some(List("wizz", "bang")), Some("fooP")))
        ), config = testJourneyDataWithMinimalJourneyConfig.config.copy(ukMode = Some(true))
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
      stubKeystore(testJourneyId, testConfigWithAddressNotUkModeAsJson, OK)

      val fResponse = buildClientLookupAddress(path = "edit")
        .withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
        .get()
      val res = await(fResponse)

      res.status shouldBe OK
      testElementExists(res, EditPage.nonUkEditId)
    }
  }

  "handleEditNonUk" should {
    "return 400 if all fields are missing and return nonUkEdit page" in {
      stubKeystore(testJourneyId, Json.toJson(testJourneyDataWithMinimalJourneyConfig.copy(config = testJourneyDataWithMinimalJourneyConfig.config.copy(ukMode = Some(false)))).as[JsObject], OK)

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
      stubKeystore(testJourneyId, Json.toJson(testJourneyDataWithMinimalJourneyConfig.copy(config = testJourneyDataWithMinimalJourneyConfig.config.copy(ukMode = Some(false)))).as[JsObject], OK)
      stubKeystoreSave(
        testJourneyId,
        Json.toJson(testJourneyDataWithMinimalJourneyConfig.copy(
          config = testJourneyDataWithMinimalJourneyConfig.config.copy(ukMode = Some(false)),
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
    "return 400 if postcode is missing and return uk edit mode page" in {
      stubKeystore(
        session = testJourneyId,
        theData = Json.toJson(testJourneyDataWithMinimalJourneyConfig.copy(
          config = testJourneyDataWithMinimalJourneyConfig.config.copy(ukMode = Some(false)))).as[JsObject],
        status = OK)

      val fResponse = buildClientLookupAddress("ukEdit").
        withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck").
        post(Map("csrfToken" -> Seq("xxx-ignored-xxx")))
      val res = await(fResponse)

      res.status shouldBe BAD_REQUEST
      testElementExists(res, EditPage.ukEditId)
    }
    "return 303 if form is valid and redirect to Confirm" in {
      stubKeystore(
        session = testJourneyId,
        theData = Json.toJson(testJourneyDataWithMinimalJourneyConfig.copy(
          config = testJourneyDataWithMinimalJourneyConfig.config.copy(ukMode = Some(false)))),
        status = OK)

      stubKeystore(
        testJourneyId,
        Json.toJson(testJourneyDataWithMinimalJourneyConfig.copy(
          config = testJourneyDataWithMinimalJourneyConfig.config.copy(ukMode = Some(false)))),
        OK
      )
      stubKeystoreSave(
        testJourneyId,
        Json.toJson(testJourneyDataWithMinimalJourneyConfig.copy(
          config = testJourneyDataWithMinimalJourneyConfig.config.copy(ukMode = Some(false)),
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

  "confirmed" should {
    "return correct address with jid" in {
      val configWithConfirmedAddress = testJourneyDataWithMinimalJourneyConfig.copy(confirmedAddress = Some(testFullNonUKConfirmedAddress))
      stubKeystore(testJourneyId, Json.toJson(configWithConfirmedAddress).as[JsObject], OK)

      val fResponse = buildClientAPI("confirmed?id=Jid123")
        .withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
        .get()
      val res = await(fResponse)

      res.status shouldBe OK
      res.json shouldBe Json.toJson(ConfirmableAddress(
        auditRef = testAuditRef,
        id = Some(testAddressIdRaw),
        address = ConfirmableAddressDetails(Some(List(testAddressLine1, testAddressLine2, testAddressLine3, testAddressTown)), Some(testPostCode), Some(Country("FR", "France"))))
      )
    }
  }
}