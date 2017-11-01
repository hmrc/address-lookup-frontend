
package controllers

import com.gu.scalatest.JsoupShouldMatchers
import controllers.api.ApiController
import model._
import model.JourneyData._
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.http.HeaderNames
import play.api.i18n.Messages.Implicits._
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{AddressService, CountryService, JourneyRepository}
import uk.gov.hmrc.address.v2.Country

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}
import uk.gov.hmrc.http.HeaderCarrier

class AddressLookupControllerSpec
  extends PlaySpec
    with OneAppPerSuite
    with JsoupShouldMatchers
    with ScalaFutures {

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
      override def find(postcode: String, filter: Option[String])(implicit hc: HeaderCarrier) = {
        Future.successful(proposals)
      }
    }

    val countryService = new CountryService {
      override val GB = Country("GB", "United Kingdom")
      override val findAll = Seq(GB, Country("DE", "Germany"))
      override def find(code: String) = findAll.find{ case Country(cc, _) => cc == code }
    }

    val controller = new AddressLookupController(journeyRepository, addressService, countryService)

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

  }

  "initializing a journey" should {

    "fail given an invalid journey name" in new Scenario {
      val res = call(api.init("foo"), req.withJsonBody(Json.toJson(Init(None))))
      status(res) must be (404)
    }

    "return the 'on-ramp' URL given a legit journey name" in new Scenario(
      journeyConfig = Map("foo" -> basicJourney),
      id = Some("bar")
    ) {
      val res = call(api.init("foo"), req.withJsonBody(Json.toJson(Init(None))))
      status(res) must be (ACCEPTED)
      header(HeaderNames.LOCATION, res) must be (Some(s"$endpoint/lookup-address/bar/lookup"))
    }

    "permit user to supply custom continueUrl" in new Scenario(
      journeyConfig = Map("foo" -> basicJourney),
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

    "return a form which permits input of building name/number and postcode" in new Scenario(
      journeyData = Map("foo" -> basicJourney)
    ) {
      val res = call(controller.lookup("foo"), req)
      val html = contentAsString(res).asBodyFragment
      html should include element withName("title").withValue("Lookup Address")
      html should include element withName("h1").withValue("Your Address")
      html should include element withName("form").withAttrValue("action", routes.AddressLookupController.select("foo").url)
      html should include element withName("label").withAttrValue("for", "filter").withValue("Building name or number")
      html should include element withName("input").withAttrValue("name", "filter")
      html should include element withName("label").withAttrValue("for", "postcode").withValue("Postcode")
      html should include element withName("input").withAttrValue("name", "postcode")
      html should include element withName("button").withAttrValue("type", "submit").withValue("Find my address")
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

    "display an error if no addresses were found" in new Scenario(
      journeyData = Map("foo" -> basicJourney),
      proposals = Seq()
    ) {
      val res = controller.select("foo").apply(req.withFormUrlEncodedBody("postcode" -> "ZZ11 1ZZ"))
      val html = contentAsString(res).asBodyFragment
      html should include element withClass("highlight-message").withValue("Sorry, we couldn't find anything for that postcode.")
      html should include element withName("button").withAttrValue("type", "submit").withValue("Find my address")
    }

    "display a single address on confirmation page" in new Scenario(
      journeyData = Map("foo" -> basicJourney),
      proposals = Seq(ProposedAddress("GB1234567890", "ZZ11 1ZZ", lines = List("line1", "line2"), town = Some("town"), county = Some("county")))
    ) {
      val res = controller.select("foo").apply(req.withFormUrlEncodedBody("postcode" -> "ZZ11 1ZZ"))

      status(res) must be (303)
      header(HeaderNames.LOCATION, res) must be (Some(routes.AddressLookupController.confirm("foo").url))
    }

    "display a list of proposals given postcode and filter parameters" in new Scenario(
      journeyData = Map("foo" -> basicJourney),
      proposals = Seq(ProposedAddress("GB1234567890", "ZZ11 1ZZ"), ProposedAddress("GB1234567891", "ZZ11 1ZZ"))
    ) {
      val res = controller.select("foo").apply(req.withFormUrlEncodedBody("postcode" -> "ZZ11 1ZZ"))
      val html = contentAsString(res).asBodyFragment
      html should include element withName("input").withAttrValue("type", "radio").withAttrValue("name", "addressId").withAttrValue("value", "GB1234567890")
      html should include element withName("input").withAttrValue("type", "radio").withAttrValue("name", "addressId").withAttrValue("value", "GB1234567891")
      html should include element withName("button").withAttrValue("type", "submit").withValue("Next")
    }

  }

  "handle select" should {

    "redirect to confirm page" in new Scenario(
      journeyData = Map("foo" -> basicJourney.copy(proposals = Some(Seq(ProposedAddress("GB1234567890", "AA1 BB2")))))
    ) {
      val res = controller.handleSelect("foo").apply(req.withFormUrlEncodedBody("addressId" -> "GB1234567890"))
      status(res) must be (303)
      header(HeaderNames.LOCATION, res) must be (Some(routes.AddressLookupController.confirm("foo").url))
    }

  }

  "Calling addressOrDefault" should {
    "return an address when called with a defined option" in new Scenario(
      journeyData = Map("foo" -> basicJourney.copy(proposals = Some(Seq(ProposedAddress("GB1234567890", "AA1 BB2")))))
    ) {
      val tstAddress = ConfirmableAddress("auditRef", Some("id"), ConfirmableAddressDetails(postcode = Some("postCode")))
      val tstEdit = Edit("", None, None, "", "postCode", Some("GB"))
      controller.addressOrDefault(Some(tstAddress)) must be (tstEdit)
    }
    "return an address when called with an empty option" in new Scenario(
      journeyData = Map("foo" -> basicJourney.copy(proposals = Some(Seq(ProposedAddress("GB1234567890", "AA1 BB2")))))
    ) {
      val tstEdit = Edit("", None, None, "", "", Some("GB"))
      controller.addressOrDefault(None) must be (tstEdit)
    }
  }

  "edit" should {

    "show all countries if no allowedCountryCodes configured" in new Scenario(
      journeyData = Map("foo" -> basicJourney.copy(config = basicJourney.config.copy(allowedCountryCodes = None)))
    ) {
      val res = controller.edit("foo").apply(req)
      val html = contentAsString(res).asBodyFragment
      html should include element withName("option").withAttrValue("value", "GB")
      html should include element withName("option").withAttrValue("value", "DE")
    }

    "show selection of countries given by allowedCountryCodes if configured" in new Scenario(
        journeyData = Map("foo" -> basicJourney.copy(config = basicJourney.config.copy(allowedCountryCodes = Some(Set("DE", "ZY")))))
    ) {
      val res = controller.edit("foo").apply(req)
      val html = contentAsString(res).asBodyFragment

      html should not include element(withName("option").withAttrValue("value", "GB"))
      html should include element withName("option").withAttrValue("value", "DE")
      html should not include element(withName("option").withAttrValue("value", "ZY"))
    }

    "editing an existing address with a country code that is not in the allowedCountryCodes config" in new Scenario(
      journeyData = Map("foo" -> basicJourney.copy(
        selectedAddress = Some(ConfirmableAddress("someAuditRef", None, ConfirmableAddressDetails(None, None, Some(Country("FR", "France"))))),
        config = basicJourney.config.copy(allowedCountryCodes = Some(Set("DE"))))
      )
    ) {
      val res = controller.edit("foo").apply(req)
      val html = contentAsString(res).asBodyFragment

      html should not include element(withName("option").withAttrValue("value", "FR"))
      html should not include element(withName("option").withAttrValue("value", "GB"))
      html should include element withName("option").withAttrValue("value", "DE")
    }
  }

  private def basicJourney: JourneyData = JourneyData(JourneyConfig("continue"))

}
