package controllers

import java.util.UUID

import itutil.IntegrationSpecBase
import model.{ConfirmableAddress, ConfirmableAddressDetails, JourneyConfig, JourneyData}
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeApplication
import uk.gov.hmrc.address.v2.Country

class AddressLookupControllerISpec extends IntegrationSpecBase {
  override implicit lazy val app = FakeApplication(additionalConfiguration = fakeConfig())

  val testConfig = JourneyData(JourneyConfig(continueUrl = "A url"))
  val csrfToken = () => UUID.randomUUID().toString

  "The lookup page" should {
    "pre-pop the postcode and filter on the view when they are passed in as query parameters and drop selected address on load" in {

      stubKeystore("Jid123", Json.toJson(testConfig).as[JsObject], 200)
      stubKeystoreSave("Jid123", Json.toJson(testConfig.copy(selectedAddress = None)).as[JsObject], 200)
      val sessionCookie = getSessionCookie(Map("csrfToken" -> csrfToken()))
      val fResponse = buildClient("/lookup?postcode=AB11+1AB&filter=bar").
        withHeaders(HeaderNames.COOKIE -> sessionCookie, "Csrf-Token" -> "nocheck").
        get()
      val res = await(fResponse)
      val doc = Jsoup.parse(res.body)
      res.status shouldBe 200
      doc.getElementById("postcode").`val` shouldBe "AB11 1AB"
      doc.getElementById("filter").`val` shouldBe "bar"

    }

    "pre-pop the postcode only on the view when it is passed in as a query parameters" in {

      stubKeystore("Jid123", Json.toJson(testConfig).as[JsObject], 200)
      stubKeystoreSave("Jid123", Json.toJson(testConfig.copy(selectedAddress = None)).as[JsObject], 200)
      val sessionCookie = getSessionCookie(Map("csrfToken" -> csrfToken()))
      val fResponse = buildClient("/lookup?postcode=AB11 1AB").
        withHeaders(HeaderNames.COOKIE -> sessionCookie, "Csrf-Token" -> "nocheck").
        get()
      val res = await(fResponse)
      val doc = Jsoup.parse(res.body)
      res.status shouldBe 200
      doc.getElementById("postcode").`val` shouldBe "AB11 1AB"
      doc.getElementById("filter").`val` shouldBe ""

    }

    "pre-pop the filter only on the view when it is passed in as a query parameters" in {

      stubKeystore("Jid123", Json.toJson(testConfig).as[JsObject], 200)
      stubKeystoreSave("Jid123", Json.toJson(testConfig.copy(selectedAddress = None)).as[JsObject], 200)
      val sessionCookie = getSessionCookie(Map("csrfToken" -> csrfToken()))
      val fResponse = buildClient("/lookup?filter=bar").
        withHeaders(HeaderNames.COOKIE -> sessionCookie, "Csrf-Token" -> "nocheck").
        get()
      val res = await(fResponse)
      val doc = Jsoup.parse(res.body)
      res.status shouldBe 200
      doc.getElementById("postcode").`val` shouldBe ""
      doc.getElementById("filter").`val` shouldBe "bar"

    }


    "not pre-pop the filter or postcode fields when no query parameters are used " in {

      stubKeystore("Jid123", Json.toJson(testConfig).as[JsObject], 200)
      stubKeystoreSave("Jid123", Json.toJson(testConfig.copy(selectedAddress = None)).as[JsObject], 200)
      val sessionCookie = getSessionCookie(Map("csrfToken" -> csrfToken()))
      val fResponse = buildClient("/lookup").
        withHeaders(HeaderNames.COOKIE -> sessionCookie, "Csrf-Token" -> "nocheck").
        get()
      val res = await(fResponse)
      val doc = Jsoup.parse(res.body)
      res.status shouldBe 200
      doc.getElementById("postcode").`val` shouldBe ""
      doc.getElementById("filter").`val` shouldBe ""

    }
  }

  "The edit page" should {
    "return UK edit page if uk param is true AND UK mode is false" in {
      val testConfigWithAddressAndNotUkMode = testConfig.copy(
        selectedAddress = Some(
          ConfirmableAddress("foo", Some("bar"), ConfirmableAddressDetails(Some(List("wizz", "bang")), Some("fooP"), Some(Country("FR", "France"))))
        ), config = testConfig.config.copy(ukMode = Some(false))
      )

      stubKeystore("Jid123", Json.toJson(testConfigWithAddressAndNotUkMode).as[JsObject], 200)
      val sessionCookie = getSessionCookie(Map("csrfToken" -> csrfToken()))
      val fResponse = buildClient("/edit?uk=true").
        withHeaders(HeaderNames.COOKIE -> sessionCookie, "Csrf-Token" -> "nocheck").
        get()
      val res = await(fResponse)
      val doc = Jsoup.parse(res.body)
      res.status shouldBe 200
      doc.getElementById("ukEdit")
    }

    "return non uk edit and not error with showSearchAgainLink and searchAgainLinkText in the json should not error when uk param not provided" in {

      val config = (Json.toJson(testConfig).as[JsObject] - "editPage") ++ Json.obj("editPage" -> Json.obj("showSearchAgainLink" -> true, "searchAgainLinkText" -> "foo"))
      stubKeystore("Jid123", Json.toJson(config).as[JsObject], 200)
      val sessionCookie = getSessionCookie(Map("csrfToken" -> csrfToken()))
      val fResponse = buildClient("/edit").
        withHeaders(HeaderNames.COOKIE -> sessionCookie, "Csrf-Token" -> "nocheck").
        get()
      val res = await(fResponse)
      res.status shouldBe 200
      val doc = Jsoup.parse(res.body)
      doc.getElementById("nonUkEdit")
    }

    "return the UK edit page with the lookup postcode uk param true AND UK mode is false" in {
      val testConfigWithAddressAndNotUkMode = testConfig.copy(
        selectedAddress = None, config = testConfig.config.copy(ukMode = Some(false))
      )

      stubKeystore("Jid123", Json.toJson(testConfigWithAddressAndNotUkMode).as[JsObject], 200)
      val sessionCookie = getSessionCookie(Map("csrfToken" -> csrfToken()))
      val fResponse = buildClient("/edit?lookUpPostCode=ZZ1+++1ZZ&uk=true").
        withHeaders(HeaderNames.COOKIE -> sessionCookie, "Csrf-Token" -> "nocheck").
        get()
      val res = await(fResponse)
      val doc = Jsoup.parse(res.body)
      res.status shouldBe 200
      doc.getElementById("ukEdit")
      doc.getElementById("postcode").`val` shouldBe "ZZ1 1ZZ"
    }

    "return the UK edit page with no pre-popped postcode if param not provided AND UK mode is false but uk param provided" in {
      val testConfigWithAddressAndNotUkMode = testConfig.copy(
        selectedAddress = None, config = testConfig.config.copy(ukMode = Some(false))
      )

      stubKeystore("Jid123", Json.toJson(testConfigWithAddressAndNotUkMode).as[JsObject], 200)
      val sessionCookie = getSessionCookie(Map("csrfToken" -> csrfToken()))
      val fResponse = buildClient("/edit").
        withHeaders(HeaderNames.COOKIE -> sessionCookie, "Csrf-Token" -> "nocheck").
        get()
      val res = await(fResponse)
      val doc = Jsoup.parse(res.body)
      res.status shouldBe 200
      doc.getElementById("ukEdit")
      doc.getElementById("postcode").`val` shouldBe ""
    }

    "redirect to the UK edit page if uk param provided and UK mode is true" in {

      val testConfigWithAddressAndUkMode = testConfig.copy(
        selectedAddress = Some(
          ConfirmableAddress("foo", Some("bar"), ConfirmableAddressDetails(Some(List("wizz", "bang")), Some("fooP"), Some(Country("GB", "United Kingdom"))))
        ), config = testConfig.config.copy(ukMode = Some(true))
      )
      stubKeystore("Jid123", Json.toJson(testConfigWithAddressAndUkMode).as[JsObject], 200)
      val sessionCookie = getSessionCookie(Map("csrfToken" -> csrfToken()))
      val fResponse = buildClient("/edit?uk=true").
        withHeaders(HeaderNames.COOKIE -> sessionCookie, "Csrf-Token" -> "nocheck").
        get()
      val res = await(fResponse)
      val doc = Jsoup.parse(res.body)
      res.status shouldBe 200
      doc.getElementById("nonUkEdit")
    }

    "redirect to the UK edit page if country doesn't exist in selected address and AND UK mode is true" in {
      val testConfigWithAddressAndUkMode = testConfig.copy(
        selectedAddress = Some(
          ConfirmableAddress("foo", Some("bar"), ConfirmableAddressDetails(Some(List("wizz", "bang")), Some("fooP")))
        ), config = testConfig.config.copy(ukMode = Some(true))
      )
      stubKeystore("Jid123", Json.toJson(testConfigWithAddressAndUkMode).as[JsObject], 200)
      val sessionCookie = getSessionCookie(Map("csrfToken" -> csrfToken()))
      val fResponse = buildClient("/edit").
        withHeaders(HeaderNames.COOKIE -> sessionCookie, "Csrf-Token" -> "nocheck").
        get()
      val res = await(fResponse)
      val doc = Jsoup.parse(res.body)
      res.status shouldBe 200
      doc.getElementById("ukEdit")
    }

    "redirect to the International edit page if Uk mode is false and uk param not provided but selected address in keystore" in {
      val testConfigWithAddressAndNotUkMode = testConfig.copy(
        selectedAddress = Some(
          ConfirmableAddress("foo", Some("bar"), ConfirmableAddressDetails(Some(List("wizz", "bang")), Some("fooP"), Some(Country("FR", "France"))))
        ), config = testConfig.config.copy(ukMode = Some(false))
      )
      stubKeystore("Jid123", Json.toJson(testConfigWithAddressAndNotUkMode).as[JsObject], 200)
      val sessionCookie = getSessionCookie(Map("csrfToken" -> csrfToken()))
      val fResponse = buildClient("/edit").
        withHeaders(HeaderNames.COOKIE -> sessionCookie, "Csrf-Token" -> "nocheck").
        get()
      val res = await(fResponse)
      val doc = Jsoup.parse(res.body)
      res.status shouldBe 200
      doc.getElementById("nonUkEdit")
    }
  }
  "handleEditNonUk" should {
    "return 400 if all fields are missing and return nonUkEdit page" in {
      stubKeystore("Jid123", Json.toJson(testConfig.copy(config = testConfig.config.copy(ukMode = Some(false)))).as[JsObject], 200)

      val sessionCookie = getSessionCookie(Map("csrfToken" -> csrfToken()))

      val fResponse = buildClient("/edit").
        withHeaders(HeaderNames.COOKIE -> sessionCookie, "Csrf-Token" -> "nocheck").
        post(Map("csrfToken" -> Seq("xxx-ignored-xxx")))
      val res = await(fResponse)
      val doc = Jsoup.parse(res.body)
      doc.getElementById("nonUkEdit")

      res.status shouldBe 400
    }
    s"return 303 if form is valid and redirect to ${controllers.routes.AddressLookupController.confirm("")}" in {

      stubKeystore("Jid123", Json.toJson(testConfig.copy(config = testConfig.config.copy(ukMode = Some(false)))).as[JsObject], 200)
      stubKeystoreSave("Jid123", Json.obj(), 200)
      val sessionCookie = getSessionCookie(Map("csrfToken" -> csrfToken()))

      val fResponse = buildClient("/edit").
        withHeaders(HeaderNames.COOKIE -> sessionCookie, "Csrf-Token" -> "nocheck").
        post(Map(
          "csrfToken" -> Seq("xxx-ignored-xxx"),
          "line1" -> Seq("One Line"),
          "line2" -> Seq("Two Line"),
          "town" -> Seq("Home Town"),
          "postcode" -> Seq(""),
          "countryCode" -> Seq("FR")
        ))

      val res = await(fResponse)
      val doc = Jsoup.parse(res.body)
      doc.getElementById("nonUkEdit")

      res.status shouldBe 303

    }
  }
  "handleEditUkMode" should {
    "return 400 if postcode is missing and return uk edit mode page" in {
      stubKeystore("Jid123", Json.toJson(testConfig.copy(config = testConfig.config.copy(ukMode = Some(false)))).as[JsObject], 200)

      val sessionCookie = getSessionCookie(Map("csrfToken" -> csrfToken()))

      val fResponse = buildClient("/ukEdit").
        withHeaders(HeaderNames.COOKIE -> sessionCookie, "Csrf-Token" -> "nocheck").
        post(Map("csrfToken" -> Seq("xxx-ignored-xxx")))
      val res = await(fResponse)
      val doc = Jsoup.parse(res.body)
      doc.getElementById("ukEdit")

      res.status shouldBe 400
    }
    s"return 303 if form is valid and redirect to ${controllers.routes.AddressLookupController.confirm("Jid123")}" in {

      stubKeystore("Jid123", Json.toJson(testConfig.copy(config = testConfig.config.copy(ukMode = Some(false)))).as[JsObject], 200)
      stubKeystoreSave("Jid123", Json.obj(), 200)
      val sessionCookie = getSessionCookie(Map("csrfToken" -> csrfToken()))

      val fResponse = buildClient("/ukEdit").
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
      val doc = Jsoup.parse(res.body)
      doc.getElementById("ukEdit")
      res.status shouldBe 303
    }
  }
  "confirmed" should {
    "return correct address with jid" in {
      val sessionCookie = getSessionCookie(Map("csrfToken" -> csrfToken()))
      val address = ConfirmableAddress("foo",Some("bar"),ConfirmableAddressDetails(Some(List("1","","3","4")),Some("wizz"),None))
      val configWithConfirmedAddress = testConfig.copy(confirmedAddress = Some(address))
      stubKeystore("Jid123", Json.toJson(configWithConfirmedAddress).as[JsObject], 200)
      val fResponse = buildClient("confirmed?id=Jid123", journeyID = "", lookUpOrApi = "api").
        withHeaders(HeaderNames.COOKIE -> sessionCookie, "Csrf-Token" -> "nocheck").
        get()
      val res = await(fResponse)
      res.status shouldBe 200
      res.json shouldBe Json.toJson(ConfirmableAddress("foo",Some("bar"),ConfirmableAddressDetails(Some(List("1","3","4")),Some("wizz"),None)))
    }
  }
}