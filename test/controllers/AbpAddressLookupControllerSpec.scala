/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers


import address.v2.Country
import com.codahale.metrics.SharedMetricRegistries
import com.gu.scalatest.JsoupShouldMatchers
import config.FrontendAppConfig
import controllers.api.ApiController
import controllers.countOfResults.ResultsCount
import fixtures.ALFEFixtures
import model._
import org.apache.pekko.stream.Materializer
import org.jsoup.nodes.Element
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.HeaderNames
import play.api.http.Status.BAD_REQUEST
import play.api.i18n.{Lang, MessagesApi, MessagesImpl}
import play.api.mvc.{AnyContentAsEmpty, Cookie, MessagesControllerComponents, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{AddressService, CountryService, IdGenerationService, JourneyRepository}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import utils.TestConstants.{Lookup => _, _}
import views.html.abp._
import views.html.{country_picker, error_template}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class AbpAddressLookupControllerSpec
  extends PlaySpec
    with GuiceOneAppPerSuite
    with JsoupShouldMatchers
    with ScalaFutures with ALFEFixtures {

  SharedMetricRegistries.clear()

  implicit lazy val materializer: Materializer = app.materializer

  implicit val hc: HeaderCarrier = HeaderCarrier()

  class Scenario(var journeyDataV2: Map[String, JourneyDataV2] = Map.empty,
                 proposals: Seq[ProposedAddress] = Seq.empty,
                 id: Option[String] = None) {

    implicit lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

    implicit val lang: Lang = Lang("en")
    implicit lazy val messages: MessagesImpl = MessagesImpl(lang, messagesApi)

    val req: FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withMethod("POST")
    // TODO: Do we need this and the tests that depend on it?
    val reqWelsh: FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withCookies(Cookie(messagesApi.langCookieName, "cy")).withMethod("POST")

    val endpoint = "http://localhost:9000"

    val frontendAppConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
    val auditConnector: AuditConnector = app.injector.instanceOf[AuditConnector]

    val components: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]

    val lookup: lookup = app.injector.instanceOf[lookup]
    val select: select = app.injector.instanceOf[select]
    val uk_mode_edit: uk_mode_edit = app.injector.instanceOf[uk_mode_edit]
    val non_uk_mode_edit: non_uk_mode_edit = app.injector.instanceOf[non_uk_mode_edit]
    val confirm: confirm = app.injector.instanceOf[confirm]
    val no_results: no_results = app.injector.instanceOf[no_results]
    val too_many_results: too_many_results = app.injector.instanceOf[too_many_results]
    val error_template: error_template = app.injector.instanceOf[error_template]
    val country_picker: country_picker = app.injector.instanceOf[country_picker]
    val remoteMessagesApiProvider: RemoteMessagesApiProvider = app.injector.instanceOf[RemoteMessagesApiProvider]

    val journeyRepository: JourneyRepository = new JourneyRepository {
      override def getV2(id: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[JourneyDataV2]] = {
        Future.successful(journeyDataV2.get(id))
      }

      override def putV2(id: String, data: JourneyDataV2)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Boolean] = {
        journeyDataV2 = journeyDataV2 ++ Map(id -> data)
        Future.successful(true)
      }
    }

    val addressService: AddressService = new AddressService {
      override def find(postcode: String, filter: Option[String], isUkMode: Boolean)(implicit hc: HeaderCarrier): Future[Seq[ProposedAddress]] = {
        Future.successful(proposals)
      }

      override def findByCountry(countryCode: String, filter: String)(implicit hc: HeaderCarrier): Future[Seq[ProposedAddress]] = ???
    }

    val countryService: CountryService = new CountryService {
      override def findAll(enFlag: Boolean = true): Seq[Country] = Seq(Country("GB", "United Kingdom"), Country("DE", "Germany"), Country("FR", "France"))

      override def find(enFlag: Boolean = true, code: String): Option[Country] = findAll().find { c: Country => c.code == code }
    }

    val controller = new AbpAddressLookupController(journeyRepository, addressService, auditConnector,
      frontendAppConfig, components, remoteMessagesApiProvider, countryService, lookup, select, uk_mode_edit, non_uk_mode_edit, confirm,
      no_results, too_many_results)

    def controllerOveridinghandleLookup(resOfHandleLookup: Future[countOfResults.ResultsCount]): AbpAddressLookupController =
      new AbpAddressLookupController(journeyRepository, addressService, auditConnector, frontendAppConfig,
        components, remoteMessagesApiProvider, countryService, lookup, select, uk_mode_edit, non_uk_mode_edit, confirm, no_results,
        too_many_results) {
        override private[controllers] def handleLookup(id: String, journeyData: JourneyDataV2, postCode: String, filter: Option[String], firstLookup: Boolean)(implicit hc: HeaderCarrier): Future[ResultsCount] = resOfHandleLookup
      }

    object MockIdGenerationService extends IdGenerationService {
      override def uuid: String = id.getOrElse(testJourneyId)
    }

    val api: ApiController = new ApiController(journeyRepository, MockIdGenerationService, frontendAppConfig, components) {
      override val addressLookupEndpoint: String = endpoint
    }

  }

  // TODO: Do we need to move this to ApiControllerSpec and update to v2?

  //  "initializing a journey" should {
  //    "fail given an invalid journey name" in new Scenario {
  //      val res = call(api.init("foo"), req.withJsonBody(Json.toJson(Init(None))))
  //      status(res) must be(404)
  //    }
  //
  //    "return the 'on-ramp' URL given a legit journey name" in new Scenario(
  //      journeyConfig = Map("foo" -> basicJourney()),
  //      id = Some("bar")
  //    ) {
  //      val res = call(api.init("foo"), req.withJsonBody(Json.toJson(Init(None))))
  //      status(res) must be(ACCEPTED)
  //      header(HeaderNames.LOCATION, res) must be(Some(s"$endpoint/lookup-address/bar/lookup"))
  //    }
  //
  //    "permit user to supply custom continueUrl" in new Scenario(
  //      journeyConfig = Map("foo" -> basicJourney()),
  //      id = Some("bar")
  //    ) {
  //      val res = call(api.init("foo"), req.withJsonBody(Json.toJson(Init(Some("http://google.com")))))
  //      status(res) must be(ACCEPTED)
  //      header(HeaderNames.LOCATION, res) must be(Some(s"$endpoint/lookup-address/bar/lookup"))
  //      journeyRepository.get("bar").futureValue.get.config.continueUrl must be("http://google.com")
  //    }
  //  }

  //  "no journey" should {
  //    "return a 'no journey' view" in new Scenario {
  //      val res = call(controller.noJourney(), req)
  //      contentAsString(res).asBodyFragment should include element withName("title").withValue("No Journey")
  //    }
  //  }

  "lookup" should {
    "isWelsh is false" should {
      "return a form which permits input of building name/number and postcode and should pre pop values" in new Scenario(
        journeyDataV2 = Map("foo" -> basicJourneyV2())
      ) {
        val res: Future[Result] = call(controller.lookup("foo", Some("ZZ1 1ZZ"), Some("The House")), req)
        val html: Element = contentAsString(res).asBodyFragment
        html should include element withName("title").withValue(messages("lookupPage.title"))
        html should include element withName("h1").withValue(messages("lookupPage.heading"))
        html should include element withName("form").withAttrValue("action", routes.AbpAddressLookupController.lookup("foo").url)
        html should include element withName("label").withAttrValue("for", "filter").withValue("Property name or number (optional)")
        html should include element withName("input").withAttrValue("name", "filter")
        html should include element withName("label").withAttrValue("for", "postcode").withValue("Postcode")
        html should include element withName("input").withAttrValue("name", "postcode")
        html should include element withName("button").withAttrValue("type", "submit").withValue("Continue")
        html should include element withAttrValue("id", "manualAddress").withValue("Enter the address manually")
        html.getElementById("postcode").`val` mustBe "ZZ1 1ZZ"
        html.getElementById("filter").`val` mustBe "The House"
      }

      "return a form which permits input of building name/number and postcode when set to UK mode and should not pre pop" in new Scenario(
        journeyDataV2 = Map("foo" -> basicJourneyV2(ukModeBool = Some(true)))
      ) {
        val res: Future[Result] = call(controller.lookup("foo"), req)
        val html: Element = contentAsString(res).asBodyFragment
        html should include element withName("title").withValue(messages("lookupPage.title.ukMode"))
        html should include element withName("h1").withValue(messages("lookupPage.heading.ukMode"))
        html should include element withName("form").withAttrValue("action", routes.AbpAddressLookupController.lookup("foo").url)
        html should include element withName("label").withAttrValue("for", "filter").withValue("Property name or number (optional)")
        html should include element withName("input").withAttrValue("name", "filter")
        html should include element withName("label").withAttrValue("for", "postcode").withValue("UK postcode")
        html should include element withName("input").withAttrValue("name", "postcode")
        html should include element withName("button").withAttrValue("type", "submit").withValue("Continue")
        html should include element withAttrValue("id", "manualAddress").withValue("Enter the address manually")
        html.getElementById("postcode").`val` mustBe ""
        html.getElementById("filter").`val` mustBe ""
      }
      "return a form that pre pops just post code" in new Scenario(
        journeyDataV2 = Map("foo" -> basicJourneyV2())
      ) {
        val res: Future[Result] = call(controller.lookup("foo", postcode = Some("AB11 1AB")), req)
        val html: Element = contentAsString(res).asBodyFragment
        html.getElementById("postcode").`val` mustBe "AB11 1AB"
        html.getElementById("filter").`val` mustBe ""
      }

      "allow page title to be configured" in new Scenario(
        journeyDataV2 = Map("foo" -> testLookupLevelJourneyConfigV2)
      ) {
        val res: Future[Result] = call(controller.lookup("foo"), req)
        val html: Element = contentAsString(res).asBodyFragment
        html should include element withName("title").withValue("enLookupPageTitle")
      }

      "allow page heading to be configured" in new Scenario(
        journeyDataV2 = Map("foo" -> testLookupLevelJourneyConfigV2)
      ) {
        val res: Future[Result] = call(controller.lookup("foo"), req)
        val html: Element = contentAsString(res).asBodyFragment
        html should include element withName("h1").withValue("enLookupPageHeading")
      }

      "allow filter label to be configured" in new Scenario(
        journeyDataV2 = Map("foo" -> testLookupLevelJourneyConfigV2)
      ) {
        val res: Future[Result] = call(controller.lookup("foo"), req)
        val html: Element = contentAsString(res).asBodyFragment
        html should include element withName("label").withAttrValue("for", "filter").withValue("enFilterLabel")
      }

      "allow postcode label to be configured" in new Scenario(
        journeyDataV2 = Map("foo" -> testLookupLevelJourneyConfigV2)
      ) {
        val res: Future[Result] = call(controller.lookup("foo"), req)
        val html: Element = contentAsString(res).asBodyFragment
        html should include element withName("label").withAttrValue("for", "postcode").withValue("enPostcodeLabel")
      }

      "allow submit label to be configured" in new Scenario(
        journeyDataV2 = Map("foo" -> testLookupLevelJourneyConfigV2)
      ) {
        val res: Future[Result] = call(controller.lookup("foo"), req)
        val html: Element = contentAsString(res).asBodyFragment
        html should include element withName("button").withAttrValue("type", "submit").withValue("enSubmitLabel")
      }
    }
  }

  "configuring phase banner should" should {

    val noBannerJourneyV2 = basicJourneyV2()
    "show no phase banner when deactivated" in new Scenario(
      journeyDataV2 = Map("foo" -> noBannerJourneyV2)
    ) {
      val res: Future[Result] = call(controller.lookup("foo"), req)
      val html: Element = contentAsString(res).asBodyFragment
      private val maybeBannerTextElement = html.getElementsByClass("govuk-phase-banner__text")

      maybeBannerTextElement.size() mustBe 0
    }

    val betaBannerJourneyV2 =
      JourneyDataV2(
        config = JourneyConfigV2(
          version = 2,
          options = JourneyOptions(continueUrl = "testContinueUrl", showPhaseBanner = Some(true)),
          labels = Some(
            JourneyLabels(en = Some(LanguageLabels()), cy = None)
          )
        )
      )
    "show a default beta phase banner when activated" in new Scenario(
      journeyDataV2 = Map("foo" -> betaBannerJourneyV2)
    ) {
      val res: Future[Result] = call(controller.lookup("foo"), req)
      val html: Element = contentAsString(res).asBodyFragment
      html should include element withClass("govuk-phase-banner__text")
      html should include element withClass("govuk-phase-banner").withValue("BETA")
      html should include element withAttrValue("class", "govuk-phase-banner__content")
        .withValue("This is a new service – your feedback will help us to improve it.")
    }

    val customBetaBannerJourneyV2 =
      JourneyDataV2(
        config = JourneyConfigV2(
          version = 2,
          options = JourneyOptions(continueUrl = "testContinueUrl", showPhaseBanner = Some(true), alphaPhase = Some(false)),
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
      val res: Future[Result] = call(controller.lookup("foo"), req)
      val html: Element = contentAsString(res).asBodyFragment
      html should include element withClass("govuk-phase-banner__text")
      html should include element withClass("govuk-phase-banner").withValue("BETA")
      html should include element withAttrValue("class", "govuk-phase-banner__content")
        .withValue("enPhaseBannerHtml")
    }

    val alphaBannerJourneyV2 = testLookupLevelJourneyConfigV2
    "show a default alpha phase banner when specified" in new Scenario(
      journeyDataV2 = Map("foo" -> alphaBannerJourneyV2)
    ) {
      val res: Future[Result] = call(controller.lookup("foo"), req)
      val html: Element = contentAsString(res).asBodyFragment
      html should include element withClass("govuk-phase-banner__text")
      html should include element withClass("govuk-phase-banner").withValue("ALPHA")
      html should include element withAttrValue("class", "govuk-phase-banner__content")
        .withValue("This is a new service – your feedback will help us to improve it.")
    }

    val customAlphaBannerJourneyV2 = testAppLevelJourneyConfigV2
    "show a custom alpha phase banner when specified and supplied with Html" in new Scenario(
      journeyDataV2 = Map("foo" -> customAlphaBannerJourneyV2)
    ) {
      val res: Future[Result] = call(controller.lookup("foo"), req)
      val html: Element = contentAsString(res).asBodyFragment
      html should include element withClass("govuk-phase-banner__text")
      html should include element withClass("govuk-phase-banner").withValue("ALPHA")
        .withValue("enPhaseBannerHtml")
    }
  }

  "select" should {

    "display the too many addresses page" in new Scenario(
      journeyDataV2 = Map("foo" -> basicJourneyV2().copy(config = JourneyConfigV2(2, JourneyOptions(continueUrl = "continue", selectPageConfig = Some(SelectPageConfig(Some(1))))))),
      proposals = Seq(ProposedAddress("1", uprn = None, parentUprn = None, usrn = None, organisation = None, "ZZ11 1ZZ", "some-town"), ProposedAddress("2", uprn = None, parentUprn = None, usrn = None, organisation = None, "ZZ11 1ZZ", "some-town"))
    ) {
      val res: Future[Result] = controller.select("foo", "ZZ11     1ZZ").apply(req)
      val html: Element = contentAsString(res).asBodyFragment
      html.getElementById("pageHeading").html mustBe "Too many results, enter more details"
    }

    "display the no results page if no addresses were found" in new Scenario(
      journeyDataV2 = Map("foo" -> basicJourneyV2()),
      proposals = Seq()
    ) {
      val res: Future[Result] = controller.select("foo", "ZZ11 1ZZ").apply(req)
      val html: Element = contentAsString(res).asBodyFragment

      status(res) must be(200)
      html.getElementById("pageHeading").html mustBe "We cannot find any addresses for ZZ11 1ZZ"
    }

    "display a single address on confirmation page" in new Scenario(
      journeyDataV2 = Map("foo" -> basicJourneyV2()),
      proposals = Seq(ProposedAddress("GB1234567890", uprn = None, parentUprn = None, usrn = None, organisation = None, "ZZ11 1ZZ", lines = List("line1", "line2"), town = "town"))
    ) {
      val res: Future[Result] = controller.select("foo", "ZZ11 1ZZ").apply(req)

      status(res) must be(303)
      header(HeaderNames.LOCATION, res) must be(Some(routes.AbpAddressLookupController.confirm("foo").url))
    }

    "display a list of  english proposals given postcode and filter parameters" in new Scenario(
      journeyDataV2 = Map("foo" -> basicJourneyV2()),
      proposals = Seq(ProposedAddress("GB1234567890", uprn = None, parentUprn = None, usrn = None, organisation = None, "ZZ11 1ZZ", "some-town"), ProposedAddress("GB1234567891", uprn = None, parentUprn = None, usrn = None, organisation = None, "ZZ11 1ZZ", "some-town"))
    ) {
      val res: Future[Result] = controller.select("foo", "ZZ11 1ZZ").apply(req)
      val html: Element = contentAsString(res).asBodyFragment
      html should include element withName("h1").withValue("Choose your address")
      html should include element withName("input").withAttrValue("type", "radio").withAttrValue("name", "addressId").withAttrValue("value", "GB1234567890")
      html should include element withName("input").withAttrValue("type", "radio").withAttrValue("name", "addressId").withAttrValue("value", "GB1234567891")
      html should include element withName("button").withAttrValue("type", "submit").withValue("Continue")
    }

    "display a list of welsh proposals given postcode and filter parameters" in new Scenario(
      journeyDataV2 = Map("foo" -> testDefaultCYJourneyConfigV2),
      proposals = Seq(ProposedAddress("GB1234567890", uprn = None, parentUprn = None, usrn = None, organisation = None, "ZZ11 1ZZ", "some-town"), ProposedAddress("GB1234567891", uprn = None, parentUprn = None, usrn = None, organisation = None, "ZZ11 1ZZ", "some-town"))
    ) {
      val res: Future[Result] = controller.select("foo", "ZZ11 1ZZ").apply(reqWelsh)
      val html: Element = contentAsString(res).asBodyFragment
      html should include element withName("h1").withValue("Dewiswch eich cyfeiriad")
      html should include element withName("input").withAttrValue("type", "radio").withAttrValue("name", "addressId").withAttrValue("value", "GB1234567890")
      html should include element withName("input").withAttrValue("type", "radio").withAttrValue("name", "addressId").withAttrValue("value", "GB1234567891")
      html should include element withName("button").withAttrValue("type", "submit").withValue("Yn eich blaen")
    }
  }

  "handle select" should {

    "redirect to confirm page when a proposal is selected" in new Scenario(
      journeyDataV2 = Map("foo" -> basicJourneyV2(None).copy(proposals = Some(Seq(ProposedAddress("GB1234567890", uprn = None, parentUprn = None, usrn = None, organisation = None, "AA1 BB2", "some-town")))))
    ) {
      val res: Future[Result] = controller.handleSelect("foo", None, testPostCode).apply(req.withFormUrlEncodedBody("addressId" -> "GB1234567890"))
      status(res) must be(303)
      header(HeaderNames.LOCATION, res) must be(Some(routes.AbpAddressLookupController.confirm("foo").url))
    }

    "redirect to select page when an address hasn't been selected" in new Scenario(
      journeyDataV2 = Map("foo" -> basicJourneyV2(None).copy(proposals = Some(Seq(ProposedAddress("GB1234567890", uprn = None, parentUprn = None, usrn = None, organisation = None, "AA1 BB2", "some-town")))))
    ) {
      val res: Future[Result] = controller.handleSelect("foo", None, testPostCode).apply(req)
      status(res) must be(BAD_REQUEST)
      val html: Element = contentAsString(res).asBodyFragment
      html should include element withAttrValue("action", routes.AbpAddressLookupController.handleSelect("foo", None, testPostCode).url)
    }

    "redirect to the lookup page when there are no proposals in the journey" in new Scenario(
      journeyDataV2 = Map("foo" -> basicJourneyV2(None))
    ) {
      val res: Future[Result] = controller.handleSelect("foo", None, testPostCode)(req.withFormUrlEncodedBody("addressId" -> "GB1234567890"))
      status(res) mustBe 303
      redirectLocation(res) mustBe Some(routes.AbpAddressLookupController.lookup("foo").url)
    }

    "display the select page" when {
      "the form had an error" when {
        "nothing was selected" in new Scenario(
          journeyDataV2 = Map("foo" -> basicJourneyV2(None))
        ) {
          val res: Future[Result] = controller.handleSelect("foo", None, testPostCode)(req.withFormUrlEncodedBody("addressId" -> ""))
          status(res) mustBe 400
          val html: Element = contentAsString(res).asBodyFragment
          html should include element withName("h1").withValue(messages("selectPage.heading"))
          html should include element withAttrValue("class", "govuk-error-summary").withValue(messages("constants.errorRequired"))
        }

        // TODO: Test at the form level rather than here
        //        "something was selected which was more than the maximum length allowed" in new Scenario(
        //          journeyDataV2 = Map("foo" -> basicJourneyV2(None))
        //        ) {
        //          val res: Future[Result] = controller.handleSelect("foo", None, testPostCode)(req.withFormUrlEncodedBody("addressId" -> "A" * 256))
        //          status(res) mustBe 400
        //          val html: Element = contentAsString(res).asBodyFragment
        //          html should include element withName("h1").withValue("??? EnglishConstantsUkMode.SELECT_PAGE_HEADING")
        //          html should include element withAttrValue("class", "govuk-error-summary").withValue("??? EnglishMessageConstants.errorMax(255)")
        //        }
      }
      "the proposals in the journey data don't contain the proposal the user selected" in new Scenario(
        journeyDataV2 = Map("foo" -> basicJourneyV2(None).copy(
          proposals = Some(Seq(ProposedAddress("GB1234567890", uprn = None, parentUprn = None, usrn = None, organisation = None, "AA1 BB2", "some-town")))))
      ) {
        val res: Future[Result] = controller.handleSelect("foo", None, testPostCode)(req.withFormUrlEncodedBody("addressId" -> "GB1234567891"))
        status(res) mustBe 400
        val html: Element = contentAsString(res).asBodyFragment
        html should include element withName("h1").withValue(messages("selectPage.heading"))
      }
    }
  }

  "confirm" should {
    "redirect to lookup if selected address doesn't exist" in new Scenario(
      journeyDataV2 = Map("foo" -> basicJourneyV2().copy(
        selectedAddress = None
      ))
    ) {
      val res: Future[Result] = controller.confirm("foo").apply(req)
      await(res).header.headers(HeaderNames.LOCATION) mustBe routes.AbpAddressLookupController.lookup("foo").url
    }
    "display English content" when {
      "allow confirmChangeText to be configured" in new Scenario(
        journeyDataV2 = Map("foo" -> JourneyDataV2(
          config = JourneyConfigV2(2, JourneyOptions("continue", confirmPageConfig = Some(ConfirmPageConfig(showConfirmChangeText = Some(true)))), Some(JourneyLabels(Some(LanguageLabels(confirmPageLabels = Some(ConfirmPageLabels(confirmChangeText = Some("I confirm")))))))),
          selectedAddress = Some(ConfirmableAddress(auditRef = "", id = Some("GB1234567890"), None, None, None, None,
            address = ConfirmableAddressDetails(organisation = None, lines = List("line1", "line2"), town = Some("town"), Some("ZZ11 1ZZ"))))
        ))
      ) {
        val res: Future[Result] = controller.confirm("foo").apply(req)
        val html: Element = contentAsString(res).asBodyFragment
        html should include element withAttrValue("id", "confirmChangeText")
      }
      "render address with blank string in lines correctly" in new Scenario(
        journeyDataV2 = Map("foo" -> JourneyDataV2(
          config = JourneyConfigV2(2, JourneyOptions("continue", confirmPageConfig = Some(ConfirmPageConfig(showConfirmChangeText = Some(true)))), Some(JourneyLabels(Some(LanguageLabels(confirmPageLabels = Some(ConfirmPageLabels(confirmChangeText = Some("I confirm")))))))),
          selectedAddress = Some(ConfirmableAddress(auditRef = "", id = Some("GB1234567890"), None, None, None, None,
            address = ConfirmableAddressDetails(organisation = None, lines = List("line1", "", "line3"), town = Some("town"), Some("ZZ11 1ZZ"))))
        ))
      ) {
        val res: Future[Result] = controller.confirm("foo").apply(req)
        val html: Element = contentAsString(res).asBodyFragment
        html.getElementById("line1").html mustBe "line1"
        html.getElementById("line2").html mustBe ""
        html.getElementById("line3").html mustBe "line3"
        intercept[Exception](html.getElementById("line0").html)
        html.getElementById("postCode").html mustBe "ZZ11 1ZZ"
      }
    }
    "display Welsh content" when {
      "allow confirmChangeText to be configured" in new Scenario(
        journeyDataV2 = Map("foo" -> JourneyDataV2(
          config = JourneyConfigV2(
            version = 2,
            options = JourneyOptions(continueUrl = "continue", confirmPageConfig = Some(ConfirmPageConfig(
              showConfirmChangeText = Some(true)
            ))),
            labels = Some(JourneyLabels(
              cy = Some(LanguageLabels(
                confirmPageLabels = Some(ConfirmPageLabels(
                  confirmChangeText = Some("Welsh Content")
                ))
              ))
            ))
          ),
          selectedAddress = Some(ConfirmableAddress(
            auditRef = "",
            id = Some("GB1234567890"),
            None, None, None, None,
            address = ConfirmableAddressDetails(
              lines = List("line1", "line2"), town = Some("town"),
              postcode = Some("ZZ11 1ZZ")
            )
          ))
        ))
      ) {
        val res: Future[Result] = controller.confirm("foo").apply(reqWelsh)
        val html: Element = contentAsString(res).asBodyFragment
        html should include element withAttrValue("id", "confirmChangeText")
      }
      "render address with blank string in lines correctly" in new Scenario(
        journeyDataV2 = Map("foo" -> JourneyDataV2(
          config = JourneyConfigV2(
            version = 2,
            options = JourneyOptions(continueUrl = "continue", confirmPageConfig = Some(ConfirmPageConfig(
              showConfirmChangeText = Some(true)
            ))),
            labels = Some(JourneyLabels(
              cy = Some(LanguageLabels(
                confirmPageLabels = Some(ConfirmPageLabels(
                  confirmChangeText = Some("I confirm")
                ))
              ))
            ))
          ),
          selectedAddress = Some(ConfirmableAddress(
            auditRef = "",
            id = Some("GB1234567890"),
            None, None, None, None,
            address = ConfirmableAddressDetails(
              lines = List("cyLine1", "", "cyLine3"), town = Some("town"),
              postcode = Some("ZZ11 1ZZ")
            )
          ))
        ))
      ) {
        val res: Future[Result] = controller.confirm("foo").apply(reqWelsh)
        val html: Element = contentAsString(res).asBodyFragment
        html.getElementById("line1").html mustBe "cyLine1"
        html.getElementById("line2").html mustBe ""
        html.getElementById("line3").html mustBe "cyLine3"
        intercept[Exception](html.getElementById("line0").html)
        html.getElementById("postCode").html mustBe "ZZ11 1ZZ"
      }
    }
  }

  "Calling addressOrDefault" should {

    "return an address when called with a defined option removing postcode as its invalid" in new Scenario(
      journeyDataV2 = Map("foo" -> basicJourneyV2().copy(proposals = Some(Seq(ProposedAddress("GB1234567890", uprn = None, parentUprn = None, usrn = None, organisation = None, "AA1 BB2", "some-town")))))
    ) {
      val tstAddress: ConfirmableAddress = ConfirmableAddress("auditRef", Some("id"), None, None, None, None, ConfirmableAddressDetails(postcode = Some("postCode")))
      val tstEdit: Edit = Edit(None, None, None, None, None, "")
      controller.addressOrDefault(Some(tstAddress)) must be(tstEdit)
    }

    "return an address when called with a defined option - and long postcode" in new Scenario(
      journeyDataV2 = Map("foo" -> basicJourneyV2().copy(proposals = Some(Seq(ProposedAddress("GB1234567890", uprn = None, parentUprn = None, usrn = None, organisation = None, "AA11 BB2", "some-town")))))
    ) {
      val tstAddress: ConfirmableAddress = ConfirmableAddress("auditRef", Some("id"), None, None, None, None, ConfirmableAddressDetails(postcode = Some("postCode")))
      val tstEdit: Edit = Edit(None, None, None, None, None, "")
      controller.addressOrDefault(Some(tstAddress)) must be(tstEdit)
    }

    "return an address with a normalised postcode when called with no option and a long lookup postcode " in new Scenario(
      journeyDataV2 = Map("foo" -> basicJourneyV2().copy(proposals = Some(Seq(ProposedAddress("GB1234567890", uprn = None, parentUprn = None, usrn = None, organisation = None, "AA11 BB2", "some-town")))))
    ) {
      val spacesInPostcode: Some[String] = Some("AA11     2BB")

      val tstEdit: Edit = Edit(None, None, None, None, None, "AA11 2BB")
      controller.addressOrDefault(None, spacesInPostcode) must be(tstEdit)
    }

    "return an address with a normalised postcode when called with no option and no spaces in lookup postcode " in new Scenario(
      journeyDataV2 = Map("foo" -> basicJourneyV2().copy(proposals = Some(Seq(ProposedAddress("GB1234567890", uprn = None, parentUprn = None, usrn = None, organisation = None, "AA11 BB2", "some-town")))))
    ) {
      val lookUpPostcode: Some[String] = Some("AA112BB")

      val tstEdit: Edit = Edit(None, None, None, None, None, "AA11 2BB")
      controller.addressOrDefault(None, lookUpPostcode) must be(tstEdit)
    }

    "return an address with a blank postcode when called with no option and a lookup postcode with incorrect format" in new Scenario(
      journeyDataV2 = Map("foo" -> basicJourneyV2().copy(proposals = Some(Seq(ProposedAddress("GB1234567890", uprn = None, parentUprn = None, usrn = None, organisation = None, "AA11 BB2", "some-town")))))
    ) {
      val lookUpPostcode: Some[String] = Some("AA11     BB2")

      val tstEdit: Edit = Edit(None, None, None, None, None, "")
      controller.addressOrDefault(None, lookUpPostcode) must be(tstEdit)
    }

    "return an address with a blank postcode when called with no option and a lookup postcode with invalid characters" in new Scenario(
      journeyDataV2 = Map("foo" -> basicJourneyV2().copy(proposals = Some(Seq(ProposedAddress("GB1234567890", uprn = None, parentUprn = None, usrn = None, organisation = None, "AA11 BB2", "some-town")))))
    ) {
      val lookUpPostcode: Some[String] = Some("TF(3@r")

      val tstEdit: Edit = Edit(None, None, None, None, None, "")
      controller.addressOrDefault(None, lookUpPostcode) must be(tstEdit)
    }

    "return an address with a blank postcode when called with no option and a no lookup postcode" in new Scenario(
      journeyDataV2 = Map("foo" -> basicJourneyV2().copy(proposals = Some(Seq(ProposedAddress("GB1234567890", uprn = None, parentUprn = None, usrn = None, organisation = None, "AA11 BB2", "some-town")))))
    ) {
      val tstEdit: Edit = Edit(None, None, None, None, None, "")
      controller.addressOrDefault(None, None) must be(tstEdit)
    }

    "return an address when called with a defined option - postcode with no space, postcode confirmable gets removed" in new Scenario(
      journeyDataV2 = Map("foo" -> basicJourneyV2().copy(proposals = Some(Seq(ProposedAddress("GB1234567890", uprn = None, parentUprn = None, usrn = None, organisation = None, "AA11BB2", "some-town")))))
    ) {
      val tstAddress: ConfirmableAddress = ConfirmableAddress("auditRef", Some("id"), None, None, None, None, ConfirmableAddressDetails(postcode = Some("postCode")))
      val tstEdit: Edit = Edit(None, None, None, None, None, "")
      controller.addressOrDefault(Some(tstAddress)) must be(tstEdit)
    }

    "return an address when called with an empty option" in new Scenario(
      journeyDataV2 = Map("foo" -> basicJourneyV2().copy(proposals = Some(Seq(ProposedAddress("GB1234567890", uprn = None, parentUprn = None, usrn = None, organisation = None, "AA1 BB2", "some-town")))))
    ) {
      val tstEdit: Edit = Edit(None, None, None, None, None, "")
      controller.addressOrDefault(None) must be(tstEdit)
    }
  }

  "edit" should {
    "show the uk edit page for english" in new Scenario(
      journeyDataV2 = Map("foo" -> basicJourneyV2().copy(config = basicJourneyV2(Some(true)).config.copy(
        options = basicJourneyV2(Some(true)).config.options.copy(allowedCountryCodes = None),
        labels = Some(JourneyLabels(cy = Some(LanguageLabels()))))))
    ) {
      val reqOther: FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withCookies(Cookie(messagesApi.langCookieName, "en"))
      val res: Future[Result] = controller.edit("foo", Some("ZZ1 1ZZ")).apply(reqOther)
      val html: Element = contentAsString(res).asBodyFragment
      html.getElementsByClass("govuk-back-link").html mustBe "Back"
    }

    "show the uk edit page for welsh" in new Scenario(
      journeyDataV2 = Map("foo" -> basicJourneyV2().copy(config = basicJourneyV2(Some(true)).config.copy(
        options = basicJourneyV2(Some(true)).config.options.copy(allowedCountryCodes = None),
        labels = Some(JourneyLabels(cy = Some(LanguageLabels()))))))
    ) {
      val reqOther: FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withCookies(Cookie(messagesApi.langCookieName, "cy"))
      val res: Future[Result] = controller.edit("foo", Some("ZZ1 1ZZ")).apply(reqOther)
      val html: Element = contentAsString(res).asBodyFragment
      html.getElementsByClass("govuk-back-link").html mustBe "Yn ôl"

    }

    "show read-only country without dropdown" in new Scenario(
      journeyDataV2 = Map("foo" -> basicJourneyV2())
    ) {
      val res: Future[Result] = controller.edit("foo", Some("ZZ1 1ZZ")).apply(req)
      val html: Element = contentAsString(res).asBodyFragment

      html should not include element(withName("option").withAttrValue("value", "GB"))

      html should include element withName("input")
        .withAttrValue("type", "hidden")
        .withAttrValue("value", "GB")
        .withAttrValue("name", "countryCode")

      html should include element withName("input")
        .withAttrValue("type", "text")
        .withAttrValue("value", "United Kingdom")
        .withAttr("disabled")
    }

    "editing an address whereby isukMode == true returns ukEditMode page" in new Scenario(
      journeyDataV2 = Map("foo" -> basicJourneyV2(Some(true)))
    ) {
      val res: Future[Result] = controller.edit("foo", Some("ZZ1 1ZZ")).apply(req)
      status(res) must be(200)
      val html: Element = contentAsString(res).asBodyFragment
      html should include element withName("input").withAttrValue("name", "postcode")
      html.getElementById("postcode").attr("value") mustBe "ZZ1 1ZZ"
      html should not include element(withName("select").withAttrValue("name", "countryCode"))
    }
  }

  "handleUkEdit" should {
    "return 303 with valid request" in new Scenario(
      journeyDataV2 = Map("foo" -> basicJourneyV2(Some(true)))
    ) {
      val res: Future[Result] = controller.handleEdit("foo").apply(req.withFormUrlEncodedBody(editFormConstructor(): _*))
      status(res) must be(303)
    }

    "return 400 with empty request" in new Scenario(
      journeyDataV2 = Map("foo" -> basicJourneyV2(Some(true)))
    ) {
      val res: Future[Result] = controller.handleEdit("foo").apply(req)
      status(res) must be(400)
    }

    "return 303 with country code == GB and no postcode provided" in new Scenario(
      journeyDataV2 = Map("foo" -> basicJourneyV2(Some(true)))
    ) {
      val res: Future[Result] = controller.handleEdit("foo").apply(
        req.withFormUrlEncodedBody(editFormConstructor(Edit(None, Some("foo"), Some("bar"), Some("wizz"), Some("bar"), "")): _*))
      status(res) must be(303)
    }
  }

  "handleNonUkEdit" should {
    "return a 400 with empty request, uk mode == true" in new Scenario(
      journeyDataV2 = Map("foo" -> basicJourneyV2(Some(true)))
    ) {
      val res: Future[Result] = controller.handleEdit("foo").apply(req)
      status(res) must be(400)
    }

    "return a 303 with request containing postcode countrycode when ukMode == false" in new Scenario(
      journeyDataV2 = Map("foo" -> basicJourneyV2(Some(false)))
    ) {
      val res: Future[Result] = controller.handleEdit("foo")
        .apply(req.withFormUrlEncodedBody(editFormConstructor(): _*))
      status(res) must be(303)
    }

    "return a 400 with empty request when ukMode == false" in new Scenario(
      journeyDataV2 = Map("foo" -> basicJourneyV2(Some(false)))
    ) {
      val res: Future[Result] = controller.handleEdit("foo").apply(req)
      status(res) must be(400)
      val html: Element = contentAsString(res).asBodyFragment
      html should include element withName("input").withAttrValue("name", "postcode")
    }

    "return 303 with country code == GB and no postcode provided" in new Scenario(
      journeyDataV2 = Map("foo" -> basicJourneyV2())
    ) {
      val res: Future[Result] = controller.handleEdit("foo").apply(
        req.withFormUrlEncodedBody(editFormConstructor(Edit(None, Some("foo"), Some("bar"), Some("wizz"), Some("bar"), "")): _*))
      status(res) must be(303)
    }

    "return 303 when country code is not set for journey and is submitted by the User from the Edit page" in new Scenario(
      journeyDataV2 = Map("foo" -> basicJourneyV2().copy(countryCode = None))
    ) {
      val res: Future[Result] = controller.handleEdit("foo").apply(
        req.withFormUrlEncodedBody(editFormConstructor(Edit(
          organisation = None,
          line1 = Some("foo"),
          line2 = Some("bar"),
          line3 = Some("wizz"),
          town = Some("bar"),
          postcode = "",
          countryCode = "GB-UnitedKingdom"
        )): _*))
      status(res) must be(303)

      await(journeyRepository.getV2("foo")) mustBe Some(basicJourneyV2().copy(
        countryCode = Some("GB"),
        selectedAddress = Some(ConfirmableAddress(
          auditRef = "foo",
          id = None,
          address = ConfirmableAddressDetails(
            organisation = None,
            lines = List("foo", "bar", "wizz"),
            town = Some("bar"),
            postcode = None,
            country = Some(Country("GB", "United Kingdom"))
          )
        ))
      ))
    }

    "return a 303 with request containing valid data but blank postcode and countryCode when ukMode == true" in new Scenario(
      journeyDataV2 = Map("foo" -> basicJourneyV2(Some(true)))
    ) {
      val res: Future[Result] = controller.handleEdit("foo")
        .apply(req.withFormUrlEncodedBody(editFormConstructor(Edit(None, Some("foo"), None, None, Some("fooBar"), "", "")): _*))
      status(res) must be(303)
    }
  }

  //  "renewSession" should {
  //    "return 200 when hit" in new Scenario {
  //      val result = controller.renewSession()(req)
  //      status(result) mustBe 200
  //      contentType(result) mustBe Some("image/jpeg")
  //      headers(result).get("Content-Disposition").exists(_.contains("renewSession.jpg")) mustBe true
  //    }
  //  }
  //
  //  "destroySession" should {
  //    "redirect to timeout url and get rid of headers" in new Scenario {
  //      val fakeRequest = req.withHeaders("testSession" -> "present")
  //
  //      val result = controller.destroySession(RedirectUrl("https://www.tax.service.gov.uk/timeoutUrl"))(fakeRequest)
  //
  //      status(result) mustBe 303
  //      headers(result) contains "testSession" mustBe false
  //      redirectLocation(result) mustBe Some("https://www.tax.service.gov.uk/timeoutUrl")
  //    }
  //  }

  "getWelshContent" should {
    "return true" when {
      "there is a welsh language cookie in the request and welsh is setup in the journey without labels" in new Scenario {
        controller.getWelshContent(testDefaultCYJourneyConfigV2)(reqWelsh) mustBe true
      }

      "there is a welsh language cookie in the request and welsh is setup in the journey with labels" in new Scenario {
        controller.getWelshContent(testLookupLevelCYJourneyConfigV2)(reqWelsh) mustBe true
      }

      "there is a welsh language cookie in the request and welsh is not setup in the journey" in new Scenario {
        controller.getWelshContent(testLookupLevelJourneyConfigV2)(reqWelsh) mustBe true
      }
    }

    "return false" when {
      "there is a welsh language cookie but welsh translations are disabled" in new Scenario {
        controller.getWelshContent(testAppLevelJourneyConfigV2WithWelshDisabled)(reqWelsh) mustBe false
      }

      "there is no welsh language cookie but welsh labels are provided" in new Scenario {
        val reqOther: FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withCookies(Cookie(messagesApi.langCookieName, "en"))
        controller.getWelshContent(testLookupLevelCYJourneyConfigV2)(reqOther) mustBe false
      }
    }
  }
}
