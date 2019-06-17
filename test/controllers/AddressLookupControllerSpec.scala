
package controllers


import com.gu.scalatest.JsoupShouldMatchers
import controllers.api.ApiController
import controllers.countOfResults.ResultsCount
import fixtures.ALFEFixtures
import model.JourneyData._
import model._
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.Play
import play.api.http.HeaderNames
import play.api.i18n.Messages.Implicits._
import play.api.libs.json.Json
import play.api.mvc.Cookie
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{AddressService, CountryService, IdGenerationService, KeystoreJourneyRepository}
import uk.gov.hmrc.address.v2.Country
import uk.gov.hmrc.http.HeaderCarrier
import utils.TestConstants._
import utils.V2ModelConverter._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class AddressLookupControllerSpec
  extends PlaySpec
    with OneAppPerSuite
    with JsoupShouldMatchers
    with ScalaFutures with ALFEFixtures {

  implicit lazy val materializer = app.materializer

  implicit val hc = HeaderCarrier()

  implicit val f = Json.format[Init]

  class Scenario(journeyConfig: Map[String, JourneyData] = Map.empty,
                 var journeyData: Map[String, JourneyData] = Map.empty,
                 journeyConfigV2: Map[String, JourneyDataV2] = Map.empty,
                 var journeyDataV2: Map[String, JourneyDataV2] = Map.empty,
                 proposals: Seq[ProposedAddress] = Seq.empty,
                 id: Option[String] = None) {

    val req = FakeRequest()
    val reqWelsh = FakeRequest().withCookies(Cookie(Play.langCookieName, "cy"))

    val endpoint = "http://localhost:9000"

    val journeyRepository = new KeystoreJourneyRepository {

      override def init(journeyName: String): JourneyData = {
        journeyConfig
          .get(journeyName)
          .getOrElse(throw new IllegalArgumentException(s"Invalid journey name: $journeyName"))
      }

      override def get(id: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[JourneyData]] = {
        Future.successful(journeyData.get(id))
      }

      override def getV2(id: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[JourneyDataV2]] = {
        Future.successful(journeyDataV2.get(id))
      }

      override def put(id: String, data: JourneyData)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Boolean] = {
        journeyData = journeyData ++ Map((id -> data))
        Future.successful(true)
      }

      override def putV2(id: String, data: JourneyDataV2)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Boolean] = {
        journeyDataV2 = journeyDataV2 ++ Map(id -> data)
        Future.successful(true)
      }
    }

    val addressService = new AddressService {
      override def find(postcode: String, filter: Option[String], isUkMode: Boolean)(implicit hc: HeaderCarrier) = {
        Future.successful(proposals)
      }
    }

    val countryService = new CountryService {

      override def findAll(enFlag: Boolean = true) = Seq(Country("GB", "United Kingdom"), Country("DE", "Germany"), Country("FR", "France"))

      override def find(enFlag: Boolean = true, code: String) = findAll().find { case Country(cc, _) => cc == code }
    }

    val controller = new AddressLookupController(journeyRepository, addressService, countryService)

    def controllerOveridinghandleLookup(resOfHandleLookup: Future[countOfResults.ResultsCount]) = new AddressLookupController(journeyRepository, addressService, countryService) {
      override private[controllers] def handleLookup(id: String, journeyData: JourneyDataV2, lookup: Lookup, firstLookup: Boolean)(implicit hc: HeaderCarrier): Future[ResultsCount] = resOfHandleLookup
    }

    object MockIdGenerationService extends IdGenerationService {
      override def uuid = id.getOrElse(testJourneyId)
    }

    val api = new ApiController(journeyRepository, MockIdGenerationService) {
      override val addressLookupEndpoint = endpoint
    }

  }

  "init journey with config" should {

    "create journey and return the 'on-ramp' URL" in new Scenario(id = Some("quix")) {
      val v1Data = JourneyData(
        config = JourneyConfig(
          continueUrl = testContinueUrl,
          showPhaseBanner = Some(true)
        )
      )
      val v2Data = v1Data.toV2Model

      val res = call(api.initWithConfig, req.withJsonBody(Json.toJson(v1Data.config)))
      status(res) must be(ACCEPTED)
      header(HeaderNames.LOCATION, res) must be(Some(s"$endpoint/lookup-address/quix/lookup"))
      journeyRepository.getV2("quix").futureValue.get must be(v2Data)
    }

    "handle a call to init without confirm change fields" in new Scenario(id = Some("quix")) {
      //the optional confirm change fields were not in the original release
      val v1Data = JourneyData(
        JourneyConfig(
          continueUrl = testContinueUrl,
          confirmPage = Some(ConfirmPage(
            showConfirmChangeText = None,
            confirmChangeText = None))
        )
      )
      val v2Data = v1Data.toV2Model

      val res = call(api.initWithConfig, req.withJsonBody(Json.toJson(v1Data.config)))
      status(res) must be(ACCEPTED)
      journeyRepository.getV2("quix").futureValue.get must be(v2Data)
    }
  }

  "initializing a journey" should {

    "fail given an invalid journey name" in new Scenario {
      val res = call(api.init("foo"), req.withJsonBody(Json.toJson(Init(None))))
      status(res) must be(404)
    }

    "return the 'on-ramp' URL given a legit journey name" in new Scenario(
      journeyConfig = Map("foo" -> basicJourney()),
      id = Some("bar")
    ) {
      val res = call(api.init("foo"), req.withJsonBody(Json.toJson(Init(None))))
      status(res) must be(ACCEPTED)
      header(HeaderNames.LOCATION, res) must be(Some(s"$endpoint/lookup-address/bar/lookup"))
    }

    "permit user to supply custom continueUrl" in new Scenario(
      journeyConfig = Map("foo" -> basicJourney()),
      id = Some("bar")
    ) {
      val res = call(api.init("foo"), req.withJsonBody(Json.toJson(Init(Some("http://google.com")))))
      status(res) must be(ACCEPTED)
      header(HeaderNames.LOCATION, res) must be(Some(s"$endpoint/lookup-address/bar/lookup"))
      journeyRepository.get("bar").futureValue.get.config.continueUrl must be("http://google.com")

    }

  }

  "no journey" should {

    "return a 'no journey' view" in new Scenario {
      val res = call(controller.noJourney(), req)
      contentAsString(res).asBodyFragment should include element withName("title").withValue("No Journey")
    }
  }

  "lookup" should {
    "isWelsh is false" should {
      "return a form which permits input of building name/number and postcode and should pre pop values" in new Scenario(
        journeyDataV2 = Map("foo" -> basicJourneyV2())
      ) {
        val res = call(controller.lookup("foo", Some("ZZ1 1ZZ"), Some("The House")), req)
        val html = contentAsString(res).asBodyFragment
        html should include element withName("title").withValue("Find the address")
        html should include element withName("h1").withValue("Find the address")
        html should include element withName("form").withAttrValue("action", routes.AddressLookupController.select("foo").url)
        html should include element withName("label").withAttrValue("for", "filter").withValue("Property name or number")
        html should include element withName("input").withAttrValue("name", "filter")
        html should include element withName("label").withAttrValue("for", "postcode").withValue("UK postcode")
        html should include element withName("input").withAttrValue("name", "postcode")
        html should include element withName("button").withAttrValue("type", "submit").withValue("Search for the address")
        html should include element withAttrValue("id", "manualAddress").withValue("The address does not have a UK Postcode")
        html.getElementById("postcode").`val` mustBe "ZZ1 1ZZ"
        html.getElementById("filter").`val` mustBe "The House"
      }

      "return a form which permits input of building name/number and postcode when set to UK mode and should not pre pop" in new Scenario(
        journeyDataV2 = Map("foo" -> basicJourneyV2(ukModeBool = Some(true)))
      ) {
        val res = call(controller.lookup("foo"), req)
        val html = contentAsString(res).asBodyFragment
        html should include element withName("title").withValue("Find the address")
        html should include element withName("h1").withValue("Find the address")
        html should include element withName("form").withAttrValue("action", routes.AddressLookupController.select("foo").url)
        html should include element withName("label").withAttrValue("for", "filter").withValue("Property name or number")
        html should include element withName("input").withAttrValue("name", "filter")
        html should include element withName("label").withAttrValue("for", "postcode").withValue("UK postcode")
        html should include element withName("input").withAttrValue("name", "postcode")
        html should include element withName("button").withAttrValue("type", "submit").withValue("Search for the address")
        html should include element withAttrValue("id", "manualAddress").withValue("The address does not have a UK Postcode")
        html.getElementById("postcode").`val` mustBe ""
        html.getElementById("filter").`val` mustBe ""
      }
      "return a form that pre pops just post code" in new Scenario(
        journeyDataV2 = Map("foo" -> basicJourneyV2())
      ) {
        val res = call(controller.lookup("foo", postcode = Some("AB11 1AB")), req)
        val html = contentAsString(res).asBodyFragment
        html.getElementById("postcode").`val` mustBe "AB11 1AB"
        html.getElementById("filter").`val` mustBe ""
      }

      "allow page title to be configured" in new Scenario(
        journeyDataV2 = Map("foo" -> testLookupLevelJourneyConfigV2)
      ) {
        val res = call(controller.lookup("foo"), req)
        val html = contentAsString(res).asBodyFragment
        html should include element withName("title").withValue("enLookupPageTitle")
      }

      "allow page heading to be configured" in new Scenario(
        journeyDataV2 = Map("foo" -> testLookupLevelJourneyConfigV2)
      ) {
        val res = call(controller.lookup("foo"), req)
        val html = contentAsString(res).asBodyFragment
        html should include element withName("h1").withValue("enLookupPageHeading")
      }

      "allow filter label to be configured" in new Scenario(
        journeyDataV2 = Map("foo" -> testLookupLevelJourneyConfigV2)
      ) {
        val res = call(controller.lookup("foo"), req)
        val html = contentAsString(res).asBodyFragment
        html should include element withName("label").withAttrValue("for", "filter").withValue("enFilterLabel")
      }

      "allow postcode label to be configured" in new Scenario(
        journeyDataV2 = Map("foo" -> testLookupLevelJourneyConfigV2)
      ) {
        val res = call(controller.lookup("foo"), req)
        val html = contentAsString(res).asBodyFragment
        html should include element withName("label").withAttrValue("for", "postcode").withValue("enPostcodeLabel")
      }

      "allow submit label to be configured" in new Scenario(
        journeyDataV2 = Map("foo" -> testLookupLevelJourneyConfigV2)
      ) {
        val res = call(controller.lookup("foo"), req)
        val html = contentAsString(res).asBodyFragment
        html should include element withName("button").withAttrValue("type", "submit").withValue("enSubmitLabel")
      }
    }
    "isWelsh is true" should {
      "Welsh default content is available" should {
        "return Welsh default content" in new Scenario(
          journeyDataV2 = Map("foo" -> testDefaultCYJourneyConfigV2)
        ) {
          val res = call(controller.lookup("foo", Some("ZZ1 1ZZ"), Some("The House")), reqWelsh)
          val html = contentAsString(res).asBodyFragment
          html should include element withName("title").withValue("Dod o hyd i’r cyfeiriad")
          html should include element withName("h1").withValue("Dod o hyd i’r cyfeiriad")
          html should include element withName("form").withAttrValue("action", routes.AddressLookupController.select("foo").url)
          html should include element withName("label").withAttrValue("for", "filter").withValue("Enw neu rif yr eiddo")
          html should include element withName("input").withAttrValue("name", "filter")
          html should include element withName("label").withAttrValue("for", "postcode").withValue("Cod post yn y DU")
          html should include element withName("input").withAttrValue("name", "postcode")
          html should include element withName("button").withAttrValue("type", "submit").withValue("Chwiliwch am y cyfeiriad")
          html should include element withAttrValue("id", "manualAddress").withValue("Nid oes gan y cyfeiriad god post yn y DU")
          html.getElementById("postcode").`val` mustBe "ZZ1 1ZZ"
          html.getElementById("filter").`val` mustBe "The House"
        }
      }
      "Welsh default content is not available" should {
        "return English default content" in new Scenario(
          journeyDataV2 = Map("foo" -> basicJourneyV2(ukModeBool = Some(true)))
        ) {
          val res = call(controller.lookup("foo"), reqWelsh)
          val html = contentAsString(res).asBodyFragment
          html should include element withName("title").withValue("Find the address")
          html should include element withName("h1").withValue("Find the address")
          html should include element withName("form").withAttrValue("action", routes.AddressLookupController.select("foo").url)
          html should include element withName("label").withAttrValue("for", "filter").withValue("Property name or number")
          html should include element withName("input").withAttrValue("name", "filter")
          html should include element withName("label").withAttrValue("for", "postcode").withValue("UK postcode")
          html should include element withName("input").withAttrValue("name", "postcode")
          html should include element withName("button").withAttrValue("type", "submit").withValue("Search for the address")
          html should include element withAttrValue("id", "manualAddress").withValue("The address does not have a UK Postcode")
          html.getElementById("postcode").`val` mustBe ""
          html.getElementById("filter").`val` mustBe ""
        }
      }
      "return a form that pre pops just post code" in new Scenario(
        journeyDataV2 = Map("foo" -> basicJourneyV2())
      ) {
        val res = call(controller.lookup("foo", postcode = Some("AB11 1AB")), reqWelsh)
        val html = contentAsString(res).asBodyFragment
        html.getElementById("postcode").`val` mustBe "AB11 1AB"
        html.getElementById("filter").`val` mustBe ""
      }

      "allow page title to be configured" in new Scenario(
        journeyDataV2 = Map("foo" -> testLookupLevelCYJourneyConfigV2)
      ) {
        val res = call(controller.lookup("foo"), reqWelsh)
        val html = contentAsString(res).asBodyFragment
        html should include element withName("title").withValue("cyLookupPageTitle")
      }

      "allow page heading to be configured" in new Scenario(
        journeyDataV2 = Map("foo" -> testLookupLevelCYJourneyConfigV2)
      ) {
        val res = call(controller.lookup("foo"), reqWelsh)
        val html = contentAsString(res).asBodyFragment
        html should include element withName("h1").withValue("cyLookupPageHeading")
      }

      "allow filter label to be configured" in new Scenario(
        journeyDataV2 = Map("foo" -> testLookupLevelCYJourneyConfigV2)
      ) {
        val res = call(controller.lookup("foo"), reqWelsh)
        val html = contentAsString(res).asBodyFragment
        html should include element withName("label").withAttrValue("for", "filter").withValue("cyFilterLabel")
      }

      "allow postcode label to be configured" in new Scenario(
        journeyDataV2 = Map("foo" -> testLookupLevelCYJourneyConfigV2)
      ) {
        val res = call(controller.lookup("foo"), reqWelsh)
        val html = contentAsString(res).asBodyFragment
        html should include element withName("label").withAttrValue("for", "postcode").withValue("cyPostcodeLabel")
      }

      "allow submit label to be configured" in new Scenario(
        journeyDataV2 = Map("foo" -> testLookupLevelCYJourneyConfigV2)
      ) {
        val res = call(controller.lookup("foo"), reqWelsh)
        val html = contentAsString(res).asBodyFragment
        html should include element withName("button").withAttrValue("type", "submit").withValue("cySubmitLabel")
      }
    }
  }

  "configuring phase banner should" should {

    val noBannerJourneyV2 = basicJourneyV2()
    "show no phase banner when deactivated" in new Scenario(
      journeyDataV2 = Map("foo" -> noBannerJourneyV2)
    ) {
      val res = call(controller.lookup("foo"), req)
      val html = contentAsString(res).asBodyFragment
      html should include element withClass("service-info").withValue("")
    }

    val betaBannerJourneyV2 =
      JourneyDataV2(
        config = JourneyConfigV2(
          version = 2,
          options = JourneyOptions(
            continueUrl = "testContinueUrl",
            showPhaseBanner = Some(true)
          )
        )
      )
    "show a default beta phase banner when activated" in new Scenario(
      journeyDataV2 = Map("foo" -> betaBannerJourneyV2)
    ) {
      val res = call(controller.lookup("foo"), req)
      val html = contentAsString(res).asBodyFragment
      html should include element withClass("phase-tag").withValue("BETA")
      html should include element withAttrValue("id", "phase-banner-content")
        .withValue("This is a new service – your feedback will help us to improve it.")
    }

    val customBetaBannerJourneyV2 =
      JourneyDataV2(
        config = JourneyConfigV2(
          version = 2,
          options = JourneyOptions(
            continueUrl = "testContinueUrl",
            showPhaseBanner = Some(true),
            alphaPhase = Some(false)
          ),
          labels = Some(JourneyLabels(
            en = Some(LanguageLabels(
              appLevelLabels = Some(AppLevelLabels(
                navTitle = Some("enNavTitle"),
                phaseBannerHtml = Some("enPhaseBannerHtml"))))))
          )
        )
      )
    "show a custom beta phase banner when supplied with Html" in new Scenario(
      journeyDataV2 = Map("foo" -> customBetaBannerJourneyV2)
    ) {
      val res = call(controller.lookup("foo"), req)
      val html = contentAsString(res).asBodyFragment
      html should include element withClass("phase-tag").withValue("BETA")
      html should include element withAttrValue("id", "phase-banner-content")
        .withValue("enPhaseBannerHtml")
    }

    val alphaBannerJourneyV2 = testLookupLevelJourneyConfigV2
    "show a default alpha phase banner when specified" in new Scenario(
      journeyDataV2 = Map("foo" -> alphaBannerJourneyV2)
    ) {
      val res = call(controller.lookup("foo"), req)
      val html = contentAsString(res).asBodyFragment
      html should include element withClass("phase-tag").withValue("ALPHA")
      html should include element withAttrValue("id", "phase-banner-content")
        .withValue("This is a new service – your feedback will help us to improve it.")
    }

    val customAlphaBannerJourneyV2 = testAppLevelJourneyConfigV2
    "show a custom alpha phase banner when specified and supplied with Html" in new Scenario(
      journeyDataV2 = Map("foo" -> customAlphaBannerJourneyV2)
    ) {
      val res = call(controller.lookup("foo"), req)
      val html = contentAsString(res).asBodyFragment
      html should include element withClass("phase-tag").withValue("ALPHA")
      html should include element withAttrValue("id", "phase-banner-content")
        .withValue("enPhaseBannerHtml")
    }
  }

  "select" should {

    "display the too many addresses page" in new Scenario(
      journeyDataV2 = Map("foo" -> basicJourneyV2().copy(config = JourneyConfigV2(2, JourneyOptions(continueUrl = "continue", selectPageConfig = Some(SelectPageConfig(Some(1))))))),
      proposals = Seq(ProposedAddress("1", "ZZ11 1ZZ"), ProposedAddress("2", "ZZ11 1ZZ"))
    ) {
      val res = controller.select("foo").apply(req.withFormUrlEncodedBody("postcode" -> "ZZ11     1ZZ"))
      val html = contentAsString(res).asBodyFragment
      html.getElementById("pageHeading").html mustBe "There are too many results"
    }

    "display the no results page if no addresses were found" in new Scenario(
      journeyDataV2 = Map("foo" -> basicJourneyV2()),
      proposals = Seq()
    ) {
      val res = controller.select("foo").apply(req.withFormUrlEncodedBody("postcode" -> "ZZ11 1ZZ"))
      val html = contentAsString(res).asBodyFragment

      status(res) must be(200)
      html.getElementById("pageHeading").html mustBe "We can not find any addresses for ZZ11 1ZZ"
    }

    "display a single address on confirmation page" in new Scenario(
      journeyDataV2 = Map("foo" -> basicJourneyV2()),
      proposals = Seq(ProposedAddress("GB1234567890", "ZZ11 1ZZ", lines = List("line1", "line2"), town = Some("town"), county = Some("county")))
    ) {
      val res = controller.select("foo").apply(req.withFormUrlEncodedBody("postcode" -> "ZZ11 1ZZ"))

      status(res) must be(303)
      header(HeaderNames.LOCATION, res) must be(Some(routes.AddressLookupController.confirm("foo").url))
    }

    "display a list of proposals given postcode and filter parameters" in new Scenario(
      journeyDataV2 = Map("foo" -> basicJourneyV2()),
      proposals = Seq(ProposedAddress("GB1234567890", "ZZ11 1ZZ"), ProposedAddress("GB1234567891", "ZZ11 1ZZ"))
    ) {
      val res = controller.select("foo").apply(req.withFormUrlEncodedBody("postcode" -> "ZZ11 1ZZ"))
      val html = contentAsString(res).asBodyFragment
      html should include element withName("input").withAttrValue("type", "radio").withAttrValue("name", "addressId").withAttrValue("value", "GB1234567890")
      html should include element withName("input").withAttrValue("type", "radio").withAttrValue("name", "addressId").withAttrValue("value", "GB1234567891")
      html should include element withName("button").withAttrValue("type", "submit").withValue("Continue")
    }
  }

  "handle select" should {

    "redirect to confirm page" in new Scenario(
      journeyDataV2 = Map("foo" -> basicJourneyV2(None).copy(proposals = Some(Seq(ProposedAddress("GB1234567890", "AA1 BB2")))))
    ) {
      val res = controller.handleSelect("foo").apply(req.withFormUrlEncodedBody("addressId" -> "GB1234567890"))
      status(res) must be(303)
      header(HeaderNames.LOCATION, res) must be(Some(routes.AddressLookupController.confirm("foo").url))
    }

  }

  "confirm" should {

    "redirect to lookup if selected address doesnt exist" in new Scenario(
      journeyDataV2 = Map("foo" -> basicJourneyV2().copy(
        selectedAddress = None
      ))
    ) {
      val res = controller.confirm("foo").apply(req)
      await(res).header.headers(HeaderNames.LOCATION) mustBe routes.AddressLookupController.lookup("foo").url
    }
    "allow confirmChangeText to be configured" in new Scenario(
      journeyDataV2 = Map("foo" -> JourneyDataV2(
        config = JourneyConfigV2(2, JourneyOptions("continue", confirmPageConfig = Some(ConfirmPageConfig(showConfirmChangeText = Some(true)))), Some(JourneyLabels(Some(LanguageLabels(confirmPageLabels = Some(ConfirmPageLabels(confirmChangeText = Some("I confirm")))))))),
        selectedAddress = Some(ConfirmableAddress(auditRef = "", id = Some("GB1234567890"), address = ConfirmableAddressDetails(lines = Some(List("line1", "line2")), Some("ZZ11 1ZZ"))))
      ))
    ) {
      val res = controller.confirm("foo").apply(req)
      val html = contentAsString(res).asBodyFragment
      html should include element withAttrValue("id", "confirmChangeText")
    }
    "render address with blank string in lines correctly" in new Scenario(
      journeyDataV2 = Map("foo" -> JourneyDataV2(
        config = JourneyConfigV2(2, JourneyOptions("continue", confirmPageConfig = Some(ConfirmPageConfig(showConfirmChangeText = Some(true)))), Some(JourneyLabels(Some(LanguageLabels(confirmPageLabels = Some(ConfirmPageLabels(confirmChangeText = Some("I confirm")))))))),
        selectedAddress = Some(ConfirmableAddress(auditRef = "", id = Some("GB1234567890"), address = ConfirmableAddressDetails(lines = Some(List("line1", "", "line3")), Some("ZZ11 1ZZ"))))
      ))
    ) {
      val res = controller.confirm("foo").apply(req)
      val html = contentAsString(res).asBodyFragment
      html.getElementById("line1").html mustBe "line1"
      html.getElementById("line2").html mustBe ""
      html.getElementById("line3").html mustBe "line3"
      intercept[Exception](html.getElementById("line0").html)
      html.getElementById("postCode").html mustBe "ZZ11 1ZZ"
    }
  }

  "Calling addressOrDefault" should {

    "return an address when called with a defined option removing postcode as its invalid" in new Scenario(
      journeyData = Map("foo" -> basicJourney().copy(proposals = Some(Seq(ProposedAddress("GB1234567890", "AA1 BB2")))))
    ) {
      val tstAddress = ConfirmableAddress("auditRef", Some("id"), ConfirmableAddressDetails(postcode = Some("postCode")))
      val tstEdit = Edit("", None, None, "", "", Some("GB"))
      controller.addressOrDefault(Some(tstAddress)) must be(tstEdit)
    }

    "return an address when called with a defined option - and long postcode" in new Scenario(
      journeyData = Map("foo" -> basicJourney().copy(proposals = Some(Seq(ProposedAddress("GB1234567890", "AA11 BB2")))))
    ) {
      val tstAddress = ConfirmableAddress("auditRef", Some("id"), ConfirmableAddressDetails(postcode = Some("postCode")))
      val tstEdit = Edit("", None, None, "", "", Some("GB"))
      controller.addressOrDefault(Some(tstAddress)) must be(tstEdit)
    }

    "return an address with a normalised postcode when called with no option and a long lookup postcode " in new Scenario(
      journeyData = Map("foo" -> basicJourney().copy(proposals = Some(Seq(ProposedAddress("GB1234567890", "AA11 BB2")))))
    ) {
      val spacesInPostcode = Some("AA11     2BB")
      val tstAddress = ConfirmableAddress("auditRef", Some("id"), ConfirmableAddressDetails())

      val tstEdit = Edit("", None, None, "", "AA11 2BB", Some("GB"))
      controller.addressOrDefault(None, spacesInPostcode) must be(tstEdit)
    }

    "return an address with a normalised postcode when called with no option and no spaces in lookup postcode " in new Scenario(
      journeyData = Map("foo" -> basicJourney().copy(proposals = Some(Seq(ProposedAddress("GB1234567890", "AA11 BB2")))))
    ) {
      val lookUpPostcode = Some("AA112BB")
      val tstAddress = ConfirmableAddress("auditRef", Some("id"), ConfirmableAddressDetails())

      val tstEdit = Edit("", None, None, "", "AA11 2BB", Some("GB"))
      controller.addressOrDefault(None, lookUpPostcode) must be(tstEdit)
    }

    "return an address with a blank postcode when called with no option and a lookup postcode with incorrect format" in new Scenario(
      journeyData = Map("foo" -> basicJourney().copy(proposals = Some(Seq(ProposedAddress("GB1234567890", "AA11 BB2")))))
    ) {
      val lookUpPostcode = Some("AA11     BB2")
      val tstAddress = ConfirmableAddress("auditRef", Some("id"), ConfirmableAddressDetails())

      val tstEdit = Edit("", None, None, "", "", Some("GB"))
      controller.addressOrDefault(None, lookUpPostcode) must be(tstEdit)
    }

    "return an address with a blank postcode when called with no option and a lookup postcode with invalid characters" in new Scenario(
      journeyData = Map("foo" -> basicJourney().copy(proposals = Some(Seq(ProposedAddress("GB1234567890", "AA11 BB2")))))
    ) {
      val lookUpPostcode = Some("TF(3@r")
      val tstAddress = ConfirmableAddress("auditRef", Some("id"), ConfirmableAddressDetails())

      val tstEdit = Edit("", None, None, "", "", Some("GB"))
      controller.addressOrDefault(None, lookUpPostcode) must be(tstEdit)
    }

    "return an address with a blank postcode when called with no option and a no lookup postcode" in new Scenario(
      journeyData = Map("foo" -> basicJourney().copy(proposals = Some(Seq(ProposedAddress("GB1234567890", "AA11 BB2")))))
    ) {

      val tstAddress = ConfirmableAddress("auditRef", Some("id"), ConfirmableAddressDetails())

      val tstEdit = Edit("", None, None, "", "", Some("GB"))
      controller.addressOrDefault(None, None) must be(tstEdit)
    }

    "return an address when called with a defined option - postcode with no space, postcode confirmable gets removed" in new Scenario(
      journeyData = Map("foo" -> basicJourney().copy(proposals = Some(Seq(ProposedAddress("GB1234567890", "AA11BB2")))))
    ) {
      val tstAddress = ConfirmableAddress("auditRef", Some("id"), ConfirmableAddressDetails(postcode = Some("postCode")))
      val tstEdit = Edit("", None, None, "", "", Some("GB"))
      controller.addressOrDefault(Some(tstAddress)) must be(tstEdit)
    }

    "return an address when called with an empty option" in new Scenario(
      journeyData = Map("foo" -> basicJourney().copy(proposals = Some(Seq(ProposedAddress("GB1234567890", "AA1 BB2")))))
    ) {
      val tstEdit = Edit("", None, None, "", "", Some("GB"))
      controller.addressOrDefault(None) must be(tstEdit)
    }
  }

  "edit" should {
    "show the uk edit page for english" in new Scenario(

      journeyDataV2 = Map("foo" -> basicJourneyV2().copy(config = basicJourneyV2(Some(true)).config.copy(
        options = basicJourneyV2(Some(true)).config.options.copy(allowedCountryCodes = None),
        labels = Some(JourneyLabels(cy = Some(LanguageLabels()))))))
    ) {
      val reqOther = FakeRequest().withCookies(Cookie(Play.langCookieName, "en"))
      val res = controller.edit("foo", Some("ZZ1 1ZZ"), None).apply(reqOther)
      val html = contentAsString(res).asBodyFragment
      html.getElementById("back-link").html mustBe "Back"

    }
    "show the uk edit page for welsh" in new Scenario(

      journeyDataV2 = Map("foo" -> basicJourneyV2().copy(config = basicJourneyV2(Some(true)).config.copy(
        options = basicJourneyV2(Some(true)).config.options.copy(allowedCountryCodes = None),
        labels = Some(JourneyLabels(cy = Some(LanguageLabels()))))))
    ) {
      val reqOther = FakeRequest().withCookies(Cookie(Play.langCookieName, "cy"))
      val res = controller.edit("foo", Some("ZZ1 1ZZ"), None).apply(reqOther)
      val html = contentAsString(res).asBodyFragment
      html.getElementById("back-link").html mustBe "Yn ôl"

    }


    "show all countries if no allowedCountryCodes configured whereby isukMode == false" in new Scenario(
      journeyDataV2 = Map("foo" -> basicJourneyV2().copy(config = basicJourneyV2().config.copy(options = basicJourneyV2().config.options.copy(allowedCountryCodes = None))))
    ) {
      val res = controller.edit("foo", Some("ZZ1 1ZZ"), None).apply(req)
      val html = contentAsString(res).asBodyFragment
      html should include element withName("option").withAttrValue("value", "GB")
      html should include element withName("option").withAttrValue("value", "DE")
      html should include element (withName("input").withAttrValue("name", "postcode"))
      html should include element (withName("select").withAttrValue("name", "countryCode"))
      html.getElementById("continue").html mustBe "Continue"

    }

    "show dropdown of countries given by allowedCountryCodes if allowedCountryCodes is configured with several codes" in new Scenario(
      journeyDataV2 = Map("foo" -> basicJourneyV2().copy(config = basicJourneyV2().config.copy(options = basicJourneyV2().config.options.copy(allowedCountryCodes = Some(Set("GB", "FR"))))))
    ) {
      val res = controller.edit("foo", Some("ZZ1 1ZZ"), Some(false)).apply(req)
      val html = contentAsString(res).asBodyFragment

      html should not include element(withName("option").withAttrValue("value", "DE"))
      html should include element withName("option").withAttrValue("value", "GB")
      html should include element withName("option").withAttrValue("value", "FR")
    }

    "show single country without dropdown if allowedCountryCodes is configured with a single country code" in new Scenario(
      journeyDataV2 = Map("foo" -> basicJourneyV2().copy(config = basicJourneyV2().config.copy(options = basicJourneyV2().config.options.copy(allowedCountryCodes = Some(Set("GB"))))))
    ) {
      val res = controller.edit("foo", Some("ZZ1 1ZZ"), Some(false)).apply(req)
      val html = contentAsString(res).asBodyFragment

      html should not include element(withName("option").withAttrValue("value", "GB"))

      html should include element withName("input")
        .withAttrValue("type", "hidden")
        .withAttrValue("value", "GB")
        .withAttrValue("name", "countryCode")
      html should include element withName("input")
        .withAttrValue("type", "text")
        .withAttrValue("value", "United Kingdom")
        .withAttr("readonly")
        .withAttr("disabled")
    }

    "editing an existing address with a country code that is not in the allowedCountryCodes config" when {
      "allowedCountryCodes contains multiple countries" in new Scenario(
        journeyDataV2 = Map("foo" -> basicJourneyV2().copy(
          selectedAddress = Some(ConfirmableAddress("someAuditRef", None, ConfirmableAddressDetails(None, None, Some(Country("FR", "France"))))),
          config = basicJourneyV2().config.copy(options = basicJourneyV2().config.options.copy(allowedCountryCodes = Some(Set("DE", "GB")))))
        )) {

        val res = controller.edit("foo", Some("ZZ1 1ZZ"), None).apply(req)

        val html = contentAsString(res).asBodyFragment

        html should not include element(withName("option").withAttrValue("value", "FR"))
        html should include element (withName("option").withAttrValue("value", "GB"))
        html should include element withName("option").withAttrValue("value", "DE")
      }
      "allowedCountryCodes contains a single country" in new Scenario(
        journeyDataV2 = Map("foo" -> basicJourneyV2().copy(
          selectedAddress = Some(ConfirmableAddress("someAuditRef", None, ConfirmableAddressDetails(None, None, Some(Country("FR", "France"))))),
          config = basicJourneyV2().config.copy(options = basicJourneyV2().config.options.copy(allowedCountryCodes = Some(Set("DE")))))
        )
      ) {
        val res = controller.edit("foo", Some("ZZ1 1ZZ"), None).apply(req)
        val html = contentAsString(res).asBodyFragment

        html should include element withName("input")
          .withAttrValue("type", "hidden")
          .withAttrValue("value", "DE")
          .withAttrValue("name", "countryCode")
        html should include element withName("input")
          .withAttrValue("type", "text")
          .withAttrValue("value", "Germany")
          .withAttr("readonly")
          .withAttr("disabled")

        html should not include element(withName("option").withAttrValue("value", "DE"))
      }
    }

    "editing an address whereby isukMode == true returns ukEditMode page" in new Scenario(
      journeyDataV2 = Map("foo" -> basicJourneyV2(Some(true)))
    ) {
      val res = controller.edit("foo", Some("ZZ1 1ZZ"), Some(false)).apply(req)
      status(res) must be(200)
      val html = contentAsString(res).asBodyFragment
      html should include element (withName("input").withAttrValue("name", "postcode"))
      html.getElementById("postcode").attr("value") mustBe "ZZ1 1ZZ"
      html should not include element(withName("select").withAttrValue("name", "countryCode"))
    }
  }

  "handleUkEdit" should {
    "return 303 with valid request" in new Scenario(
      journeyDataV2 = Map("foo" -> basicJourneyV2(Some(true)))
    ) {
      val res = controller.handleUkEdit("foo").apply(req.withFormUrlEncodedBody(editFormConstructor(): _*))
      status(res) must be(303)
    }
    "return 400 with empty request" in new Scenario(
      journeyDataV2 = Map("foo" -> basicJourneyV2(Some(true)))
    ) {
      val res = controller.handleUkEdit("foo").apply(req)
      status(res) must be(400)
    }
    "return 303 with country code == GB and no postcode provided" in new Scenario(
      journeyDataV2 = Map("foo" -> basicJourneyV2(Some(true)))
    ) {
      val res = controller.handleUkEdit("foo").apply(
        req.withFormUrlEncodedBody(editFormConstructor(Edit("foo", Some("bar"), Some("wizz"), "bar", "", Some("GB"))): _*))
      status(res) must be(303)
    }
  }
  "handleNonUkEdit" should {
    "return a 400 with empty request, uk mode == true" in new Scenario(
      journeyDataV2 = Map("foo" -> basicJourneyV2(Some(true)))
    ) {
      val res = controller.handleNonUkEdit("foo").apply(req)
      status(res) must be(400)
    }
    "return a 303 with request containing postcode countrycode when ukMode == false" in new Scenario(
      journeyDataV2 = Map("foo" -> basicJourneyV2(Some(false)))
    ) {
      val res = controller.handleNonUkEdit("foo")
        .apply(req.withFormUrlEncodedBody(editFormConstructor(): _*))
      status(res) must be(303)
    }
    "return a 400 with empty request when ukMode == false" in new Scenario(
      journeyDataV2 = Map("foo" -> basicJourneyV2(Some(false)))
    ) {
      val res = controller.handleNonUkEdit("foo").apply(req)
      status(res) must be(400)
      val html = contentAsString(res).asBodyFragment
      html should include element (withName("input").withAttrValue("name", "postcode"))
      html should include element (withName("select").withAttrValue("name", "countryCode"))
    }
    "return 303 with country code == GB and no postcode provided" in new Scenario(
      journeyData = Map("foo" -> basicJourney())
    ) {
      val res = controller.handleUkEdit("foo").apply(
        req.withFormUrlEncodedBody(editFormConstructor(Edit("foo", Some("bar"), Some("wizz"), "bar", "", Some("GB"))): _*))
      status(res) must be(303)
    }

    "return a 303 with request containing valid data but blank postcode and countryCode when ukMode == true" in new Scenario(
      journeyData = Map("foo" -> basicJourney(Some(true)))
    ) {
      val res = controller.handleNonUkEdit("foo")
        .apply(req.withFormUrlEncodedBody(editFormConstructor(Edit("foo", None, None, "fooBar", "", None)): _*))
      status(res) must be(303)
    }
  }
  "renewSession" should {
    "return 200 when hit" in new Scenario {
      val result = controller.renewSession()(req)
      status(result) mustBe 200
      contentType(result) mustBe Some("image/jpeg")
      headers(result).get("Content-Disposition").exists(_.contains("renewSession.jpg")) mustBe true
    }
  }
  "destroySession" should {
    "redirect to timeout url and get rid of headers" in new Scenario {
      val fakeRequest = req.withHeaders("testSession" -> "present")

      val result = controller.destroySession("timeoutUrl")(fakeRequest)

      status(result) mustBe 303
      headers(result) contains "testSession" mustBe false
      redirectLocation(result) mustBe Some("timeoutUrl")
    }
  }

  "getWelshContent" should {
    "return true" when {
      "there is a welsh language cookie in the request and welsh is setup in the journey without labels" in new Scenario {
        controller.getWelshContent(testDefaultCYJourneyConfigV2)(reqWelsh) mustBe true
      }
      "there is a welsh language cookie in the request and welsh is setup in the journey with labels" in new Scenario {
        controller.getWelshContent(testLookupLevelCYJourneyConfigV2)(reqWelsh) mustBe true
      }
    }
    "return false" when {
      "there is no welsh language cookie but welsh labels are provided" in new Scenario {
        val reqOther = FakeRequest().withCookies(Cookie(Play.langCookieName, "en"))
        controller.getWelshContent(testLookupLevelCYJourneyConfigV2)(reqOther) mustBe false
      }
      "there is a welsh language cookie in the request and welsh is not setup in the journey" in new Scenario {
        controller.getWelshContent(testLookupLevelJourneyConfigV2)(reqWelsh) mustBe false
      }
    }
  }
}