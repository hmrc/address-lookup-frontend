
package controllers


import com.gu.scalatest.JsoupShouldMatchers
import controllers.api.ApiController
import controllers.countOfResults.ResultsCount
import fixtures.ALFEFixtures
import model._
import model.JourneyData._
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.http.HeaderNames
import play.api.i18n.Messages.Implicits._
import play.api.test.Helpers._
//import play.api.mvc.Result
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{AddressService, CountryService, JourneyRepository}
import uk.gov.hmrc.address.v2.Country

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

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
                 proposals: Seq[ProposedAddress] = Seq.empty,
                 id: Option[String] = None) {

    val req = FakeRequest()

    val endpoint = "http://localhost:9000"

    val journeyRepository = new JourneyRepository {

      override def init(journeyName: String): JourneyData = {
        journeyConfig
          .get(journeyName)
          .getOrElse(throw new IllegalArgumentException(s"Invalid journey name: $journeyName"))
      }

      override def get(id: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[JourneyData]] = {
        Future.successful(journeyData.get(id))
      }

      override def put(id: String, data: JourneyData)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Boolean] = {
        journeyData = journeyData ++ Map((id -> data))
        Future.successful(true)
      }
    }

    val addressService = new AddressService {
      override def find(postcode: String, filter: Option[String],isUkMode:Boolean)(implicit hc: HeaderCarrier) = {
        Future.successful(proposals)
      }
    }

    val countryService = new CountryService {
      override val GB = Country("GB", "United Kingdom")
      override val findAll = Seq(GB, Country("DE", "Germany"), Country("FR", "France"))
      override def find(code: String) = findAll.find{ case Country(cc, _) => cc == code }
    }

    val controller = new AddressLookupController(journeyRepository, addressService, countryService)
    def controllerOveridinghandleLookup(resOfHandleLookup: Future[countOfResults.ResultsCount]) = new AddressLookupController(journeyRepository, addressService, countryService) {
      override private[controllers] def handleLookup(id: String, journeyData: JourneyData, lookup: Lookup, firstLookup: Boolean)(implicit hc: HeaderCarrier): Future[ResultsCount] = resOfHandleLookup
    }

    val api = new ApiController(journeyRepository) {
      override val addressLookupEndpoint = endpoint
      override protected def uuid: String = id.getOrElse("random-id")
    }

  }

  "init journey with config" should {

    "create journey and return the 'on-ramp' URL" in new Scenario(id = Some("quix")) {
      val config = JourneyConfig(continueUrl = "http://google.com", showPhaseBanner = Some(true))
      val res = call(api.initWithConfig, req.withJsonBody(Json.toJson(config)))
      status(res) must be (ACCEPTED)
      header(HeaderNames.LOCATION, res) must be (Some(s"$endpoint/lookup-address/quix/lookup"))
      journeyRepository.get("quix").futureValue.get.config must be (config)
    }

    "handle a call to init without confirm change fields" in new Scenario(id = Some("quix")) {
      //the optional confirm change fields were not in the original release
      val config = JourneyConfig(
        continueUrl = "http://google.com",
        confirmPage = Some(ConfirmPage(
          showConfirmChangeText = None,
          confirmChangeText = None))
      )
      val res = call(api.initWithConfig, req.withJsonBody(Json.toJson(config)))
      status(res) must be (ACCEPTED)
      journeyRepository.get("quix").futureValue.get.config must be (config)
    }
  }

  "initializing a journey" should {

    "fail given an invalid journey name" in new Scenario {
      val res = call(api.init("foo"), req.withJsonBody(Json.toJson(Init(None))))
      status(res) must be (404)
    }

    "return the 'on-ramp' URL given a legit journey name" in new Scenario(
      journeyConfig = Map("foo" -> basicJourney()),
      id = Some("bar")
    ) {
      val res = call(api.init("foo"), req.withJsonBody(Json.toJson(Init(None))))
      status(res) must be (ACCEPTED)
      header(HeaderNames.LOCATION, res) must be (Some(s"$endpoint/lookup-address/bar/lookup"))
    }

    "permit user to supply custom continueUrl" in new Scenario(
      journeyConfig = Map("foo" -> basicJourney()),
      id = Some("bar")
    ) {
      val res = call(api.init("foo"), req.withJsonBody(Json.toJson(Init(Some("http://google.com")))))
      status(res) must be (ACCEPTED)
      header(HeaderNames.LOCATION, res) must be (Some(s"$endpoint/lookup-address/bar/lookup"))
      journeyRepository.get("bar").futureValue.get.config.continueUrl must be ("http://google.com")

    }

  }

  "no journey" should {

    "return a 'no journey' view" in new Scenario {
      val res = call(controller.noJourney(), req)
      contentAsString(res).asBodyFragment should include element withName("title").withValue("No Journey")
    }
  }

  "lookup" should {

    "return a form which permits input of building name/number and postcode and should pre pop values" in new Scenario(
      journeyData = Map("foo" -> basicJourney())
    ) {
      val res = call(controller.lookup("foo",Some("ZZ1 1ZZ"),Some("The House")), req)
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
      journeyData = Map("foo" -> basicJourney(ukModeBool=Some(true)))
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
      journeyData = Map("foo" -> basicJourney())
    ) {
      val res = call(controller.lookup("foo",postcode = Some("AB11 1AB")), req)
      val html = contentAsString(res).asBodyFragment
      html.getElementById("postcode").`val` mustBe "AB11 1AB"
      html.getElementById("filter").`val` mustBe ""
    }

    "goes to the no results page if postcode not found" in new Scenario(
      journeyData = Map("foo" -> basicJourney()),
      proposals = Seq()
      ) {
      val result = controllerOveridinghandleLookup(Future.successful(countOfResults.NoResults)).select("foo").apply(req.withFormUrlEncodedBody("postcode" -> "ZZ11 1ZZ"))
      val html = contentAsString(result).asBodyFragment

      status(result) must be (200)
      html.getElementById("pageHeading").html mustBe "We can not find any addresses for ZZ11 1ZZ"
    }

    "goes to the confirmation page if exact postcode and number is matched" in new Scenario(
          journeyData = Map("foo" -> basicJourney()),
          proposals = Seq()
          ) {
          val result = controllerOveridinghandleLookup(Future.successful(countOfResults.OneResult(ProposedAddress("10", "ZZ1 1ZZ")))).select("foo").apply(req.withFormUrlEncodedBody("postcode" -> "ZZ1 1ZZ", "filter" -> "10"))
          val html = contentAsString(result).asBodyFragment

          status(result) must be (303)
          redirectLocation(result) mustBe Some("/lookup-address/foo/confirm")
        }


    "allow page title to be configured" in new Scenario(
      journeyData = Map("foo" -> JourneyData(JourneyConfig("continue", lookupPage = Some(LookupPage(title = Some("Hello!"))))))
    ) {
      val res = call(controller.lookup("foo"), req)
      val html = contentAsString(res).asBodyFragment
      html should include element withName("title").withValue("Hello!")
    }

    "allow page heading to be configured" in new Scenario(
      journeyData = Map("foo" -> JourneyData(JourneyConfig("continue", lookupPage = Some(LookupPage(heading = Some("World!"))))))
    ) {
      val res = call(controller.lookup("foo"), req)
      val html = contentAsString(res).asBodyFragment
      html should include element withName("h1").withValue("World!")
    }

    "allow filter label to be configured" in new Scenario(
      journeyData = Map("foo" -> JourneyData(JourneyConfig("continue", lookupPage = Some(LookupPage(filterLabel = Some("Your digs no."))))))
    ) {
      val res = call(controller.lookup("foo"), req)
      val html = contentAsString(res).asBodyFragment
      html should include element withName("label").withAttrValue("for", "filter").withValue("Your digs no.")
    }

    "allow postcode label to be configured" in new Scenario(
      journeyData = Map("foo" -> JourneyData(JourneyConfig("continue", lookupPage = Some(LookupPage(postcodeLabel = Some("Your PO, bro"))))))
    ) {
      val res = call(controller.lookup("foo"), req)
      val html = contentAsString(res).asBodyFragment
      html should include element withName("label").withAttrValue("for", "postcode").withValue("Your PO, bro")
    }

    "allow submit label to be configured" in new Scenario(
      journeyData = Map("foo" -> JourneyData(JourneyConfig("continue", lookupPage = Some(LookupPage(submitLabel = Some("Make it so"))))))
    ) {
      val res = call(controller.lookup("foo"), req)
      val html = contentAsString(res).asBodyFragment
      html should include element withName("button").withAttrValue("type", "submit").withValue("Make it so")
    }
  }

  "configuring phase banner should" should {

    val noBannerJourney = JourneyData(JourneyConfig(continueUrl="cont", showPhaseBanner = Some(false)))
    "show no phase banner when deactivated" in new Scenario(
      journeyData = Map("foo" -> noBannerJourney)
    ) {
      val res = call(controller.lookup("foo"), req)
      val html = contentAsString(res).asBodyFragment
      html should include element withClass("service-info").withValue("")
    }

    val betaBannerJourney = JourneyData(JourneyConfig(continueUrl="cont", showPhaseBanner = Some(true)))
    "show a default beta phase banner when activated" in new Scenario(
      journeyData = Map("foo" -> betaBannerJourney)
    ) {
      val res = call(controller.lookup("foo"), req)
      val html = contentAsString(res).asBodyFragment
      html should include element withClass("phase-tag").withValue("BETA")
      html should include element withAttrValue("id", "phase-banner-content")
        .withValue("This is a new service – your feedback will help us to improve it.")
    }

    val customBetaBannerJourney = JourneyData(JourneyConfig(continueUrl="cont", showPhaseBanner = Some(true), phaseBannerHtml = Some("html content")))
    "show a custom beta phase banner when supplied with Html" in new Scenario(
      journeyData = Map("foo" -> customBetaBannerJourney)
    ) {
      val res = call(controller.lookup("foo"), req)
      val html = contentAsString(res).asBodyFragment
      html should include element withClass("phase-tag").withValue("BETA")
      html should include element withAttrValue("id", "phase-banner-content")
        .withValue("html content")
    }

    val alphaBannerJourney = JourneyData(JourneyConfig(continueUrl="cont", showPhaseBanner = Some(true), alphaPhase = Some(true)))
    "show a default alpha phase banner when specified" in new Scenario(
      journeyData = Map("foo" -> alphaBannerJourney)
    ) {
      val res = call(controller.lookup("foo"), req)
      val html = contentAsString(res).asBodyFragment
      html should include element withClass("phase-tag").withValue("ALPHA")
      html should include element withAttrValue("id", "phase-banner-content")
        .withValue("This is a new service – your feedback will help us to improve it.")
    }

    val customAlphaBannerJourney = JourneyData(JourneyConfig(continueUrl="cont", showPhaseBanner = Some(true), alphaPhase = Some(true), phaseBannerHtml = Some("more html content")))
    "show a custom alpha phase banner when specified and supplied with Html" in new Scenario(
      journeyData = Map("foo" -> customAlphaBannerJourney)
    ) {
      val res = call(controller.lookup("foo"), req)
      val html = contentAsString(res).asBodyFragment
      html should include element withClass("phase-tag").withValue("ALPHA")
      html should include element withAttrValue("id", "phase-banner-content")
        .withValue("more html content")
    }
  }

  "select" should {

    "display the too many addresses page" in new Scenario(
      journeyData = Map("foo" -> basicJourney().copy(config = basicJourney().config.copy(selectPage = Some(SelectPage(proposalListLimit = Some(1)))))),
      proposals = Seq(ProposedAddress("1", "ZZ11 1ZZ"), ProposedAddress("2", "ZZ11 1ZZ"))
    ) {
      val res = controller.select("foo").apply(req.withFormUrlEncodedBody("postcode" -> "ZZ11     1ZZ"))
      val html = contentAsString(res).asBodyFragment
      html.getElementById("pageHeading").html mustBe "There are too many results"
    }

    "display the no results page if no addresses were found" in new Scenario(
      journeyData = Map("foo" -> basicJourney()),
      proposals = Seq()
    ) {
      val res = controller.select("foo").apply(req.withFormUrlEncodedBody("postcode" -> "ZZ11 1ZZ"))
      val html = contentAsString(res).asBodyFragment

      status(res) must be (200)
      html.getElementById("pageHeading").html mustBe "We can not find any addresses for ZZ11 1ZZ"
    }

    "display a single address on confirmation page" in new Scenario(
      journeyData = Map("foo" -> basicJourney()),
      proposals = Seq(ProposedAddress("GB1234567890", "ZZ11 1ZZ", lines = List("line1", "line2"), town = Some("town"), county = Some("county")))
    ) {
      val res = controller.select("foo").apply(req.withFormUrlEncodedBody("postcode" -> "ZZ11 1ZZ"))

      status(res) must be (303)
      header(HeaderNames.LOCATION, res) must be (Some(routes.AddressLookupController.confirm("foo").url))
    }

    "display a list of proposals given postcode and filter parameters" in new Scenario(
      journeyData = Map("foo" -> basicJourney()),
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
      journeyData = Map("foo" -> basicJourney(None).copy(proposals = Some(Seq(ProposedAddress("GB1234567890", "AA1 BB2")))))
    ) {
      val res = controller.handleSelect("foo").apply(req.withFormUrlEncodedBody("addressId" -> "GB1234567890"))
      status(res) must be (303)
      header(HeaderNames.LOCATION, res) must be (Some(routes.AddressLookupController.confirm("foo").url))
    }

  }

  "confirm" should {

    "redirect to lookup if selected address doesnt exist" in new Scenario(
      journeyData = Map("foo" -> basicJourney().copy(
        selectedAddress = None
      ))
    ) {
      val res = controller.confirm("foo").apply(req)
      await(res).header.headers(HeaderNames.LOCATION) mustBe routes.AddressLookupController.lookup("foo").url
    }
    "allow confirmChangeText to be configured" in new Scenario(
      journeyData = Map("foo" -> JourneyData(
        config = JourneyConfig("continue",confirmPage = Some(ConfirmPage(showConfirmChangeText = Some(true), confirmChangeText = Some("I confirm")))),
        selectedAddress = Some(ConfirmableAddress(auditRef = "", id = Some("GB1234567890"), address = ConfirmableAddressDetails(lines = Some(List("line1", "line2")), Some("ZZ11 1ZZ"))))
      ))
    ) {
      val res = controller.confirm("foo").apply(req)
      val html = contentAsString(res).asBodyFragment
      html should include element withAttrValue("id","confirmChangeText")
    }
    "render address with blank string in lines correctly" in new Scenario(
      journeyData = Map("foo" -> JourneyData(
        config = JourneyConfig("continue",confirmPage = Some(ConfirmPage(showConfirmChangeText = Some(true), confirmChangeText = Some("I confirm")))),
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
      controller.addressOrDefault(Some(tstAddress)) must be (tstEdit)
    }

    "return an address when called with a defined option - and long postcode" in new Scenario(
      journeyData = Map("foo" -> basicJourney().copy(proposals = Some(Seq(ProposedAddress("GB1234567890", "AA11 BB2")))))
    ) {
      val tstAddress = ConfirmableAddress("auditRef", Some("id"), ConfirmableAddressDetails(postcode = Some("postCode")))
      val tstEdit = Edit("", None, None, "", "", Some("GB"))
      controller.addressOrDefault(Some(tstAddress)) must be (tstEdit)
    }

    "return an address with a normalised postcode when called with no option and a long lookup postcode " in new Scenario(
      journeyData = Map("foo" -> basicJourney().copy(proposals = Some(Seq(ProposedAddress("GB1234567890", "AA11 BB2")))))
    ) {
      val spacesInPostcode = Some("AA11     2BB")
      val tstAddress = ConfirmableAddress("auditRef", Some("id"), ConfirmableAddressDetails())

      val tstEdit = Edit("", None, None, "", "AA11 2BB", Some("GB"))
      controller.addressOrDefault(None,spacesInPostcode) must be (tstEdit)
    }

    "return an address with a normalised postcode when called with no option and no spaces in lookup postcode " in new Scenario(
      journeyData = Map("foo" -> basicJourney().copy(proposals = Some(Seq(ProposedAddress("GB1234567890", "AA11 BB2")))))
    ) {
      val lookUpPostcode = Some("AA112BB")
      val tstAddress = ConfirmableAddress("auditRef", Some("id"), ConfirmableAddressDetails())

      val tstEdit = Edit("", None, None, "", "AA11 2BB", Some("GB"))
      controller.addressOrDefault(None,lookUpPostcode) must be (tstEdit)
    }

    "return an address with a blank postcode when called with no option and a lookup postcode with incorrect format" in new Scenario(
      journeyData = Map("foo" -> basicJourney().copy(proposals = Some(Seq(ProposedAddress("GB1234567890", "AA11 BB2")))))
    ) {
      val lookUpPostcode = Some("AA11     BB2")
      val tstAddress = ConfirmableAddress("auditRef", Some("id"), ConfirmableAddressDetails())

      val tstEdit = Edit("", None, None, "", "", Some("GB"))
      controller.addressOrDefault(None,lookUpPostcode) must be (tstEdit)
    }

    "return an address with a blank postcode when called with no option and a lookup postcode with invalid characters" in new Scenario(
      journeyData = Map("foo" -> basicJourney().copy(proposals = Some(Seq(ProposedAddress("GB1234567890", "AA11 BB2")))))
    ) {
      val lookUpPostcode = Some("TF(3@r")
      val tstAddress = ConfirmableAddress("auditRef", Some("id"), ConfirmableAddressDetails())

      val tstEdit = Edit("", None, None, "", "", Some("GB"))
      controller.addressOrDefault(None,lookUpPostcode) must be (tstEdit)
    }

    "return an address with a blank postcode when called with no option and a no lookup postcode" in new Scenario(
      journeyData = Map("foo" -> basicJourney().copy(proposals = Some(Seq(ProposedAddress("GB1234567890", "AA11 BB2")))))
    ) {

      val tstAddress = ConfirmableAddress("auditRef", Some("id"), ConfirmableAddressDetails())

      val tstEdit = Edit("", None, None, "", "", Some("GB"))
      controller.addressOrDefault(None,None) must be (tstEdit)
    }

    "return an address when called with a defined option - postcode with no space, postcode confirmable gets removed" in new Scenario(
      journeyData = Map("foo" -> basicJourney().copy(proposals = Some(Seq(ProposedAddress("GB1234567890", "AA11BB2")))))
    ) {
      val tstAddress = ConfirmableAddress("auditRef", Some("id"), ConfirmableAddressDetails(postcode = Some("postCode")))
      val tstEdit = Edit("", None, None, "", "", Some("GB"))
      controller.addressOrDefault(Some(tstAddress)) must be (tstEdit)
    }

    "return an address when called with an empty option" in new Scenario(
      journeyData = Map("foo" -> basicJourney().copy(proposals = Some(Seq(ProposedAddress("GB1234567890", "AA1 BB2")))))
    ) {
      val tstEdit = Edit("", None, None, "", "", Some("GB"))
      controller.addressOrDefault(None) must be (tstEdit)
    }
  }

  "edit" should {

    "show all countries if no allowedCountryCodes configured whereby isukMode == false" in new Scenario(
      journeyData = Map("foo" -> basicJourney().copy(config = basicJourney().config.copy(allowedCountryCodes = None)))
    ) {
      val res = controller.edit("foo",Some("ZZ1 1ZZ"), None).apply(req)
      val html = contentAsString(res).asBodyFragment
      html should include element withName("option").withAttrValue("value", "GB")
      html should include element withName("option").withAttrValue("value", "DE")
      html should include element(withName("input").withAttrValue("name","postcode"))
      html should include element(withName("select").withAttrValue("name","countryCode"))
      html.getElementById("continue").html mustBe "Continue"

    }

    "show dropdown of countries given by allowedCountryCodes if allowedCountryCodes is configured with several codes" in new Scenario(
      journeyData = Map("foo" -> basicJourney().copy(config = basicJourney().config.copy(allowedCountryCodes = Some(Set("GB", "FR")))))
    ) {
      val res = controller.edit("foo",Some("ZZ1 1ZZ"),Some(false)).apply(req)
      val html = contentAsString(res).asBodyFragment

      html should not include element(withName("option").withAttrValue("value", "DE"))
      html should include element withName("option").withAttrValue("value", "GB")
      html should include element withName("option").withAttrValue("value", "FR")
    }

    "show single country without dropdown if allowedCountryCodes is configured with a single country code" in new Scenario(
      journeyData = Map("foo" -> basicJourney().copy(config = basicJourney().config.copy(allowedCountryCodes = Some(Set("GB")))))
    ) {
      val res = controller.edit("foo",Some("ZZ1 1ZZ"), Some(false)).apply(req)
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
        journeyData = Map("foo" -> basicJourney().copy(
          selectedAddress = Some(ConfirmableAddress("someAuditRef", None, ConfirmableAddressDetails(None, None, Some(Country("FR", "France"))))),
          config = basicJourney().config.copy(allowedCountryCodes = Some(Set("DE", "GB"))))
        )
      ) {
        val res = controller.edit("foo",Some("ZZ1 1ZZ"),None).apply(req)
        val html = contentAsString(res).asBodyFragment

        html should not include element(withName("option").withAttrValue("value", "FR"))
        html should include element(withName("option").withAttrValue("value", "GB"))
        html should include element withName("option").withAttrValue("value", "DE")
      }
      "allowedCountryCodes contains a single country" in new Scenario(
        journeyData = Map("foo" -> basicJourney().copy(
          selectedAddress = Some(ConfirmableAddress("someAuditRef", None, ConfirmableAddressDetails(None, None, Some(Country("FR", "France"))))),
          config = basicJourney().config.copy(allowedCountryCodes = Some(Set("DE"))))
        )
      ) {
        val res = controller.edit("foo",Some("ZZ1 1ZZ"),None).apply(req)
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
      journeyData = Map("foo" -> basicJourney(Some(true)))
    ){
      val res = controller.edit("foo",Some("ZZ1 1ZZ"),Some(false)).apply(req)
      status(res) must be(200)
      val html = contentAsString(res).asBodyFragment
      html should include element(withName("input").withAttrValue("name","postcode"))
      html.getElementById("postcode").attr("value") mustBe "ZZ1 1ZZ"
      html should not include element(withName("select").withAttrValue("name","countryCode"))
    }
  }

  "handleUkEdit" should {
    "return 303 with valid request" in new Scenario(
      journeyData = Map("foo" -> basicJourney(Some(true)))
    ){
      val res = controller.handleUkEdit("foo").apply(req.withFormUrlEncodedBody(editFormConstructor():_*))
      status(res) must be(303)
    }
    "return 400 with empty request" in new Scenario(
      journeyData = Map("foo" -> basicJourney(Some(true)))
    ){
      val res = controller.handleUkEdit("foo").apply(req)
      status(res) must be(400)
    }
    "return 303 with country code == GB and no postcode provided" in new Scenario(
      journeyData = Map("foo" -> basicJourney(Some(true)))
    ){
      val res = controller.handleUkEdit("foo").apply(
        req.withFormUrlEncodedBody(editFormConstructor(Edit("foo",Some("bar"), Some("wizz"),"bar","", Some("GB"))):_*))
      status(res) must be(303)
    }
  }
  "handleNonUkEdit" should {
    "return a 400 with empty request, uk mode == true" in new Scenario (
      journeyData = Map("foo" -> basicJourney(Some(true)))
    ){
      val res = controller.handleNonUkEdit("foo").apply(req)
      status(res) must be(400)
    }
    "return a 303 with request containing postcode countrycode when ukMode == false" in new Scenario (
      journeyData = Map("foo" -> basicJourney(Some(false)))
    ) {
      val res = controller.handleNonUkEdit("foo")
        .apply(req.withFormUrlEncodedBody(editFormConstructor():_*))
      status(res) must be(303)
    }
    "return a 400 with empty request when ukMode == false" in new Scenario (
      journeyData = Map("foo" -> basicJourney(Some(false)))
    ){
      val res = controller.handleNonUkEdit("foo").apply(req)
      status(res) must be(400)
      val html = contentAsString(res).asBodyFragment
      html should include element(withName("input").withAttrValue("name","postcode"))
      html should include element(withName("select").withAttrValue("name","countryCode"))
    }
    "return 303 with country code == GB and no postcode provided" in new Scenario(
      journeyData = Map("foo" -> basicJourney())
    ){
      val res = controller.handleUkEdit("foo").apply(
        req.withFormUrlEncodedBody(editFormConstructor(Edit("foo",Some("bar"), Some("wizz"),"bar","", Some("GB"))):_*))
      status(res) must be(303)
    }

    "return a 303 with request containing valid data but blank postcode and countryCode when ukMode == true" in new Scenario (
      journeyData = Map("foo" -> basicJourney(Some(true)))
    ) {
      val res = controller.handleNonUkEdit("foo")
        .apply(req.withFormUrlEncodedBody(editFormConstructor(Edit("foo",None,None,"fooBar","",None)):_*))
      status(res) must be(303)
    }
  }
  "renewSession" should {
    "return 200 when hit" in new Scenario{
      val result = controller.renewSession()(req)
      status(result) mustBe 200
      contentType(result) mustBe Some("image/jpeg")
      await(result).body.dataStream.toString.contains("""renewSession.jpg""") mustBe true
    }
  }
  "destroySession" should {
    "redirect to timeout url and get rid of headers" in new Scenario{
      val fakeRequest = req.withHeaders("testSession" -> "present")

      val result = controller.destroySession("timeoutUrl")(fakeRequest)

      status(result) mustBe 303
      headers(result) contains "testSession" mustBe false
      redirectLocation(result) mustBe Some("timeoutUrl")
    }
  }
}