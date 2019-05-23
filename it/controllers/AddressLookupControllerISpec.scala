package controllers

import itutil.{IntegrationSpecBase, WireMockHelper}
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

  s"The lookup page" should {
    "pre-pop the postcode and filter on the view when they are passed in as query parameters and drop selected address on load" in {
      stubKeystore(testJourneyId, testConfigDefaultAsJson, OK)
      stubKeystoreSave(testJourneyId, testConfigWithoutAddressAsJson, OK)

      val sessionCookie = sessionCookieWithCSRF
      val fResponse = buildClient(path = "lookup?postcode=AB11+1AB&filter=bar")
        .withHeaders(HeaderNames.COOKIE -> sessionCookie, "Csrf-Token" -> "nocheck")
        .get()
      val res = await(fResponse)

      res.status shouldBe OK
      testOnPageValuesMatch(res, Map(LookupPage.postcodeId -> testPostCode, LookupPage.filterId -> testFilterValue))
    }

    "pre-pop the postcode only on the view when it is passed in as a query parameters" in {
      stubKeystore(testJourneyId, testConfigDefaultAsJson, OK)
      stubKeystoreSave(testJourneyId, testConfigWithoutAddressAsJson, OK)

      val sessionCookie = sessionCookieWithCSRF
      val fResponse = buildClient(path = "lookup?postcode=AB11 1AB")
        .withHeaders(HeaderNames.COOKIE -> sessionCookie, "Csrf-Token" -> "nocheck")
        .get()
      val res = await(fResponse)

      res.status shouldBe OK
      testOnPageValuesMatch(res, Map(LookupPage.postcodeId -> testPostCode, LookupPage.filterId -> ""))
    }

    "pre-pop the filter only on the view when it is passed in as a query parameters" in {
      stubKeystore(testJourneyId, testConfigDefaultAsJson, OK)
      stubKeystoreSave(testJourneyId, testConfigWithoutAddressAsJson, OK)

      val sessionCookie = sessionCookieWithCSRF
      val fResponse = buildClient(path = "lookup?filter=bar")
        .withHeaders(HeaderNames.COOKIE -> sessionCookie, "Csrf-Token" -> "nocheck")
        .get()
      val res = await(fResponse)

      res.status shouldBe OK
      testOnPageValuesMatch(res, Map(LookupPage.postcodeId -> "", LookupPage.filterId -> testFilterValue))
    }

    "not pre-pop the filter or postcode fields when no query parameters are used " in {
      stubKeystore(testJourneyId, testConfigDefaultAsJson, OK)
      stubKeystoreSave(testJourneyId, testConfigWithoutAddressAsJson, OK)

      val sessionCookie = sessionCookieWithCSRF
      val fResponse = buildClient(path = "lookup")
        .withHeaders(HeaderNames.COOKIE -> sessionCookie, "Csrf-Token" -> "nocheck")
        .get()
      val res = await(fResponse)

      res.status shouldBe OK
      testOnPageValuesMatch(res, Map(LookupPage.postcodeId -> "", LookupPage.filterId -> ""))
    }
  }

  "The edit page" should {
    "return UK edit page if uk param is true AND UK mode is false" in {
      stubKeystore(testJourneyId, testConfigWithAddressNotUkModeAsJson, OK)

      val sessionCookie = sessionCookieWithCSRF
      val fResponse = buildClient(path = "edit?uk=true").
        withHeaders(HeaderNames.COOKIE -> sessionCookie, "Csrf-Token" -> "nocheck").
        get()
      val res = await(fResponse)

      res.status shouldBe OK
      testElementExists(res, EditPage.ukEditId)
    }

    "return non uk edit and not error with showSearchAgainLink and searchAgainLinkText in the json should not error when uk param not provided" in {
      val config = (Json.toJson(testJourneyDataWithMinimalJourneyConfig).as[JsObject] - "editPage") ++
        Json.obj("editPage" -> Json.obj("showSearchAgainLink" -> true, "searchAgainLinkText" -> "foo"))

      stubKeystore(testJourneyId, testConfigDefaultAsJson, OK)

      val sessionCookie = sessionCookieWithCSRF
      val fResponse = buildClient(path = "edit")
        .withHeaders(HeaderNames.COOKIE -> sessionCookie, "Csrf-Token" -> "nocheck")
        .get()
      val res = await(fResponse)

      res.status shouldBe OK
      testElementExists(res, EditPage.nonUkEditId)
    }

    "return the UK edit page with the lookup postcode uk param true AND UK mode is false" in {
      stubKeystore(testJourneyId, testConfigWithAddressNotUkModeAsJson, OK)

      val sessionCookie = sessionCookieWithCSRF
      val fResponse = buildClient(path = "edit?lookUpPostCode=AB11+++1AB&uk=true")
        .withHeaders(HeaderNames.COOKIE -> sessionCookie, "Csrf-Token" -> "nocheck")
        .get()
      val res = await(fResponse)

      res.status shouldBe OK
      testElementExists(res, EditPage.ukEditId)
      testOnPageValuesMatch(res, Map(LookupPage.postcodeId -> testPostCode))
    }

    "return the UK edit page with no pre-popped postcode if param not provided AND UK mode is false but uk param provided" in {
      stubKeystore(testJourneyId, testConfigWithAddressNotUkModeAsJson, OK)

      val sessionCookie = sessionCookieWithCSRF
      val fResponse = buildClient(path = "edit")
        .withHeaders(HeaderNames.COOKIE -> sessionCookie, "Csrf-Token" -> "nocheck")
        .get()
      val res = await(fResponse)

      res.status shouldBe OK
      testElementExists(res, EditPage.ukEditId)
      testOnPageValuesMatch(res, Map(LookupPage.postcodeId -> testPostCode))
    }

    "redirect to the UK edit page if uk param provided and UK mode is true" in {
      val testConfigWithAddressAndUkMode = testJourneyDataWithMinimalJourneyConfig.copy(
        selectedAddress = Some(
          ConfirmableAddress("foo", Some("bar"), ConfirmableAddressDetails(Some(List("wizz", "bang")), Some("fooP"), Some(Country("GB", "United Kingdom"))))
        ), config = testJourneyDataWithMinimalJourneyConfig.config.copy(ukMode = Some(true))
      )
      stubKeystore(testJourneyId, Json.toJson(testConfigWithAddressAndUkMode).as[JsObject], OK)

      val sessionCookie = sessionCookieWithCSRF
      val fResponse = buildClient(path = "edit?uk=true")
        .withHeaders(HeaderNames.COOKIE -> sessionCookie, "Csrf-Token" -> "nocheck")
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

      val sessionCookie = sessionCookieWithCSRF
      val fResponse = buildClient(path = "edit")
        .withHeaders(HeaderNames.COOKIE -> sessionCookie, "Csrf-Token" -> "nocheck")
        .get()
      val res = await(fResponse)

      res.status shouldBe OK
      testElementExists(res, EditPage.ukEditId)
    }

    "redirect to the International edit page if Uk mode is false and uk param not provided but selected address in keystore" in {
      stubKeystore(testJourneyId, testConfigWithAddressNotUkModeAsJson, OK)

      val sessionCookie = sessionCookieWithCSRF
      val fResponse = buildClient(path = "edit")
        .withHeaders(HeaderNames.COOKIE -> sessionCookie, "Csrf-Token" -> "nocheck")
        .get()
      val res = await(fResponse)

      res.status shouldBe OK
      testElementExists(res, EditPage.nonUkEditId)
    }
  }

  "handleEditNonUk" should {
    "return 400 if all fields are missing and return nonUkEdit page" in {
      stubKeystore(
        session = testJourneyId,
        theData = Json.toJson(testJourneyDataWithMinimalJourneyConfig.copy(
          config = testJourneyDataWithMinimalJourneyConfig.config.copy(ukMode = Some(false)))).as[JsObject],
        status = OK)

      val sessionCookie = sessionCookieWithCSRF
      val fResponse = buildClient(path = "edit")
        .withHeaders(HeaderNames.COOKIE -> sessionCookie, "Csrf-Token" -> "nocheck")
        .post(Map("csrfToken" -> Seq("xxx-ignored-xxx")))
      val res = await(fResponse)

      res.status shouldBe BAD_REQUEST
      testElementExists(res, EditPage.nonUkEditId)
    }
    s"return 303 if form is valid and redirect to ${controllers.routes.AddressLookupController.confirm("")}" in {
      stubKeystore(
        session = testJourneyId,
        theData = Json.toJson(testJourneyDataWithMinimalJourneyConfig.copy(
          config = testJourneyDataWithMinimalJourneyConfig.config.copy(ukMode = Some(false)))).as[JsObject],
        status = OK)

      stubKeystoreSave(testJourneyId, Json.obj(), OK)

      val sessionCookie = sessionCookieWithCSRF
      val fResponse = buildClient(path = "edit")
        .withHeaders(HeaderNames.COOKIE -> sessionCookie, "Csrf-Token" -> "nocheck")
        .post(Map(
          "csrfToken" -> Seq("xxx-ignored-xxx"),
          "line1" -> Seq("One Line"),
          "line2" -> Seq("Two Line"),
          "town" -> Seq("Home Town"),
          "postcode" -> Seq(""),
          "countryCode" -> Seq("FR")
        ))
      val res = await(fResponse)

      res.status shouldBe SEE_OTHER
      testElementExists(res, EditPage.nonUkEditId)
    }
  }

  "handleEditUkMode" should {
    "return 400 if postcode is missing and return uk edit mode page" in {
      stubKeystore(
        session = testJourneyId,
        theData = Json.toJson(testJourneyDataWithMinimalJourneyConfig.copy(
          config = testJourneyDataWithMinimalJourneyConfig.config.copy(ukMode = Some(false)))).as[JsObject],
        status = OK)

      val sessionCookie = sessionCookieWithCSRF
      val fResponse = buildClient("ukEdit").
        withHeaders(HeaderNames.COOKIE -> sessionCookie, "Csrf-Token" -> "nocheck").
        post(Map("csrfToken" -> Seq("xxx-ignored-xxx")))
      val res = await(fResponse)

      res.status shouldBe BAD_REQUEST
      testElementExists(res, EditPage.ukEditId)
    }
    s"return 303 if form is valid and redirect to ${controllers.routes.AddressLookupController.confirm(testJourneyId)}" in {
      stubKeystore(
        session = testJourneyId,
        theData = Json.toJson(testJourneyDataWithMinimalJourneyConfig.copy(
          config = testJourneyDataWithMinimalJourneyConfig.config.copy(ukMode = Some(false)))).as[JsObject],
        status = OK)

      stubKeystoreSave(testJourneyId, Json.obj(), OK)

      val sessionCookie = sessionCookieWithCSRF
      val fResponse = buildClient("ukEdit").
        withHeaders(HeaderNames.COOKIE -> sessionCookie, "Csrf-Token" -> "nocheck").
        post(Map(
          "csrfToken" -> Seq("xxx-ignored-xxx"),
          "line1" -> Seq("One Line"),
          "line2" -> Seq("Two Line"),
          "town" -> Seq("Home Town"),
          "postcode" -> Seq("AA199ZZ"),
          "countryCode" -> Seq("GB")
        ))
      val res = await(fResponse)

      res.status shouldBe SEE_OTHER
      testElementExists(res, EditPage.ukEditId)
    }
  }

  "confirmed" should {
    "return correct address with jid" in {
      val sessionCookie = sessionCookieWithCSRF
      val configWithConfirmedAddress = testJourneyDataWithMinimalJourneyConfig.copy(confirmedAddress = Some(testConfirmedAddress))
      stubKeystore(testJourneyId, Json.toJson(configWithConfirmedAddress).as[JsObject], OK)

      val fResponse = buildClient(path = "confirm?id=Jid123", lookUpOrApi = "api")
        .withHeaders(HeaderNames.COOKIE -> sessionCookie, "Csrf-Token" -> "nocheck")
        .get()
      val res = await(fResponse)

      res.status shouldBe OK
      res.json shouldBe Json.toJson(ConfirmableAddress(
        auditRef = "auditRef",
        id = Some("addressId"),
        address = ConfirmableAddressDetails(Some(List("1 High Street", "Telford")), Some("AB11 1AB"), Some(Country("FR", "France"))))
      )
    }
  }
}