package model

import fixtures.ALFEFixtures
import model.JourneyConfigDefaults.LOOKUP_PAGE_MANUAL_ADDRESS_LINK_TEXT
import model.JourneyData._
import org.scalatest.{MustMatchers, WordSpec}
import play.api.libs.json.{JsResultException, Json}
import services.ForeignOfficeCountryService

class ALFESpec extends WordSpec with MustMatchers with ALFEFixtures {

  "stripEmptyLines" should{
    "strip empty address lines (all 4)" in {
      val res = ConfirmableAddress("foo",Some("bar"),ConfirmableAddressDetails(Some(List("","","","")),Some("wizz"),None)).stripEmptyLines
      res mustBe ConfirmableAddress("foo",Some("bar"),ConfirmableAddressDetails(Some(List.empty),Some("wizz"),None))
    }
    "not strip non empty address lines (all 4)" in {
      val add = ConfirmableAddress("foo",Some("bar"),ConfirmableAddressDetails(Some(List("1","2","3","4")),Some("wizz"),None))
      add.stripEmptyLines mustBe add
    }
    "strip one address line if empty string" in {
      val res = ConfirmableAddress("foo",Some("bar"),ConfirmableAddressDetails(Some(List("1","","3","4")),Some("wizz"),None)).stripEmptyLines
      res mustBe ConfirmableAddress("foo",Some("bar"),ConfirmableAddressDetails(Some(List("1","3","4")), Some("wizz"),None))
    }
  }
  "an edit" should {

    "transform to a confirmable address using toConfirmableAddressUk" in {
      val edit = Edit("line1", Some("line2"), Some("line3"), "town", "postcode", Some(ForeignOfficeCountryService.find("GB").get.code))
      val conf = edit.toConfirmableAddressUk("audit ref")
      val expected = ConfirmableAddress(
        "audit ref",
        None,
        ConfirmableAddressDetails(
          Some(List("line1", "line2", "town", "line3")),
          Some("postcode"),
          ForeignOfficeCountryService.find("GB")
        )
      )
      conf must be (expected)
    }

    "transform to a confirmable address and back again where isukMode == false" in {
      val edit = Edit("line1", Some("line2"), Some("line3"), "town", "postcode", Some(ForeignOfficeCountryService.find("GB").get.code))
      val conf = edit.toConfirmableAddressNonUk("audit ref")
      val expected = ConfirmableAddress(
        "audit ref",
        None,
        ConfirmableAddressDetails(
          Some(List("line1", "line2", "line3", "town")),
          Some("postcode"),
          ForeignOfficeCountryService.find("GB")
        )
      )
      conf must be (expected)
      val ed2 = conf.toEdit
      ed2 must be (edit)
      ed2.toConfirmableAddressNonUk("audit ref") must be (expected)
    }

    "transform to a confirmable address and back again given less than three lines where isukMode == false" in {
      val edit = Edit("line1", None, None, "town", "postcode", Some(ForeignOfficeCountryService.find("GB").get.code))
      val conf = edit.toConfirmableAddressNonUk("audit ref")
      val expected = ConfirmableAddress(
        "audit ref",
        None,
        ConfirmableAddressDetails(
          Some(List("line1", "town")),
          Some("postcode"),
          ForeignOfficeCountryService.find("GB")
        )
      )
      conf must be (expected)
      val ed2 = conf.toEdit
      ed2 must be (edit)
      ed2.toConfirmableAddressNonUk("audit ref") must be (expected)
    }

    "transform to a confirmable address and back again given less than three lines where isukMode == true" in {
      val edit = Edit("line1", None, None, "town", "foo", Some("GB"))
      val conf = edit.toConfirmableAddressNonUk("audit ref")
      val expected = ConfirmableAddress(
        "audit ref",
        None,
        ConfirmableAddressDetails(
          Some(List("line1", "town")),
          postcode = Some("foo"),
          ForeignOfficeCountryService.find("GB")
        )
      )
      conf must be (expected)
      val ed2 = conf.toEdit
      ed2 must be (edit)
      ed2.toConfirmableAddressNonUk("audit ref") must be (expected)
    }
    "transform to a confirmable address and back where postcode is empty isukMode == true" in {
      val edit = Edit("line1", None, None, "town", "", Some("FR"))
      val conf = edit.toConfirmableAddressNonUk("audit ref")
      val expected = ConfirmableAddress(
        "audit ref",
        None,
        ConfirmableAddressDetails(
          Some(List("line1", "town")),
          postcode = None,
          ForeignOfficeCountryService.find("FR")
        )
      )
      conf must be (expected)
      val ed2 = conf.toEdit
      ed2 must be (edit)
      ed2.toConfirmableAddressNonUk("audit ref") must be (expected)
    }
  }

  "a proposal" should {

    "transform to a confirmable address where county is ignored AND LINE 4 if town exists" in {
      val auditRef = "audit ref"
      val prop = ProposedAddress("GB1234567890", "postcode", List("line1", "line2", "line3", "line4"), Some("town"), Some("county"), ForeignOfficeCountryService.find("GB").get)
      val conf = prop.toConfirmableAddress(auditRef)
      val expected = ConfirmableAddress(
        auditRef,
        Some(prop.addressId),
        address = ConfirmableAddressDetails(
          Some(prop.lines.take(3) ++ List(prop.town.get)),
          Some(prop.postcode),
          Some(prop.country)
        )
      )
      conf must be (expected)

    }
    "transform to a confirmable address where town is ignored AND LINE 4 if county exists" in {
      val auditRef = "audit ref"
      val prop = ProposedAddress("GB1234567890", "postcode", List("line1", "line2", "line3", "line4"), None, Some("county"), ForeignOfficeCountryService.find("GB").get)
      val conf = prop.toConfirmableAddress(auditRef)
      val expected = ConfirmableAddress(
        auditRef,
        Some(prop.addressId),
        address = ConfirmableAddressDetails(
          Some(prop.lines.take(3) ++ List(prop.county.get)),
          Some(prop.postcode),
          Some(prop.country)
        )
      )

      conf must be (expected)
    }
    "transform to a confirmable address With all 4 address lines as county and town are None" in {
      val auditRef = "audit ref"
      val prop = ProposedAddress("GB1234567890", "postcode", List("line1", "line2", "line3", "line4"), None, None, ForeignOfficeCountryService.find("GB").get)
      val conf = prop.toConfirmableAddress(auditRef)
      val expected = ConfirmableAddress(
        auditRef,
        Some(prop.addressId),
        address = ConfirmableAddressDetails(
          Some(prop.lines.take(4)),
          Some(prop.postcode),
          Some(prop.country)
        )
      )

      conf must be (expected)
    }

    "be able to describe itself" in {
      val prop = ProposedAddress("GB1234567890", "postcode", List("line1", "line2", "line3"), Some("town"), Some("county"), ForeignOfficeCountryService.find("GB").get)
      val desc = prop.toDescription
      desc must be ("line1, line2, line3, town, county, postcode")
    }
  }

  "a confirmable address" should {
    "default country to GB" in {
      ConfirmableAddress("auditRef").address.country must be (ForeignOfficeCountryService.find("GB"))
    }
  }

  "a journey config" should {
    "be creatable with only continueUrl" in {
      val json = "{\"continueUrl\":\"http://google.com\"}"
      val config = Json.parse(json).as[JourneyConfig]
      config.continueUrl must be ("http://google.com")
    }

  }

  "a resolved config" should {

    val c = JourneyConfig("http://google.com")

    val cfg = ResolvedJourneyConfig(c)

    "have a default home nav href" in {
      cfg.homeNavHref must be ("http://www.hmrc.gov.uk")
    }

    "not show phase banner by default" in {
      cfg.showPhaseBanner must be (false)
    }

    "turn alpha phase off by default" in {
      cfg.alphaPhase must be (false)
    }

    "have empty phase name by default" in {
      cfg.phase must be ("")
    }

    "have beta phase name when phase banner on and alpha phase off" in {
      cfg.copy(c.copy(showPhaseBanner = Some(true))).phase must be ("beta")
    }

    "have alpha phase name when phase banner on and alpha phase on" in {
      cfg.copy(c.copy(showPhaseBanner = Some(true), alphaPhase = Some(true))).phase must be ("alpha")
    }

    "have default help link" in {
      cfg.phaseFeedbackLink must be ("/help/")
    }

    "have beta help link" in {
      cfg.copy(c.copy(showPhaseBanner = Some(true))).phaseFeedbackLink must be ("/help/beta")
    }

    "have alpha help link" in {
      cfg.copy(c.copy(showPhaseBanner = Some(true), alphaPhase = Some(true))).phaseFeedbackLink must be ("/help/alpha")
    }

    "have default phase banner html" in {
      cfg.phaseBannerHtml must be (JourneyConfigDefaults.defaultPhaseBannerHtml(cfg.phaseFeedbackLink))
    }

    "show back buttons by default" in {
      cfg.showBackButtons must be (true)
    }

    "include HMRC branding by default" in {
      cfg.includeHMRCBranding must be (true)
    }

  }

  "A timeout" should {
    "throw error" when {
      "timeoutAmount is less than 120 seconds" in {
        val errorMsgContent = "Timeout duration must be greater than 120 seconds"
        def parseJson = Json.parse(
          """
            |{
            | "continueUrl" : "continue",
            | "timeout" : {
            |   "timeoutAmount" : 80,
            |   "timeoutUrl" : "timeout"
            | }
            |}
          """.stripMargin).as[JourneyConfig]

        intercept[JsResultException](parseJson)
      }
    }

    "create journey config with a timeout" when {
      "timeoutAmount is less than 120 seconds" in {

        val parsedJson = Json.parse(
          """
            |{
            | "continueUrl" : "continue",
            | "timeout" : {
            |   "timeoutAmount" : 120,
            |   "timeoutUrl" : "timeout"
            | }
            |}
          """.stripMargin).as[JourneyConfig]

        parsedJson.timeout mustBe Some(Timeout(120,"timeout"))
      }
    }
  }

  "edit page" should {
    "not use default values when json contains all fields in edit block including search again fields which will be ignored" in {
      val parsedJson = Json.parse(
        """
          |{
          |"continueUrl" : "cont",
          |"editPage" : {
          |    "title" : "Enter Address",
          |    "heading" : "Enter Address",
          |    "line1Label" : "1 Whooo Lane",
          |    "line2Label" : "Whooo Land",
          |    "townLabel" : "City World",
          |    "postcodeLabel" : "AA1 99ZZ",
          |    "countryLabel" : "Home Country",
          |    "submitLabel" : "Cont",
          |    "showSearchAgainLink" : true,
          |    "searchAgainLinkText" : "Search"
          |     }
          |    }
        """.stripMargin).as[JourneyConfig]
      val journeyExpected = JourneyConfig(continueUrl = "cont",editPage = Some(EditPage(title = Some("Enter Address"), heading = Some("Enter Address"),
        line1Label = Some("1 Whooo Lane"), line2Label = Some("Whooo Land"), townLabel = Some("City World"),
        postcodeLabel = Some("AA1 99ZZ"), countryLabel = Some("Home Country"), submitLabel = Some("Cont"))))
      parsedJson.editPage.get mustBe journeyExpected.editPage.get

    }
    "use all default values whilst passing in just search again fields which will be ignored" in {
      val parsedJson = Json.parse(
        """
          |{
          |"continueUrl" : "cont",
          |"editPage" : {
          |    "showSearchAgainLink" : true,
          |    "searchAgainLinkText" : "Search"
          |     }
          |    }
        """.stripMargin).as[JourneyConfig]
      val journeyExpected = JourneyConfig(continueUrl = "cont",editPage = Some(EditPage()))
      parsedJson.editPage.get mustBe journeyExpected.editPage.get
    }
  }
  "ResolvedJourneyConfig" should {
    "default text when ukMode == true for ResolvedLookupPage.manualAddressLinkText" in {
      val res = ResolvedJourneyConfig(basicJourney(Some(true)).config)
      res.lookupPage.manualAddressLinkText mustBe(LOOKUP_PAGE_MANUAL_ADDRESS_LINK_TEXT)
    }
    "default text when ukMode == false for ResolvedLookupPage.manualAddressLinkText" in {
      val res = ResolvedJourneyConfig(basicJourney().config)
      res.lookupPage.manualAddressLinkText mustBe(LOOKUP_PAGE_MANUAL_ADDRESS_LINK_TEXT)
    }
  }
}