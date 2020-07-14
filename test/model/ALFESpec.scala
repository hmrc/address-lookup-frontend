package model

import fixtures.ALFEFixtures
import model.JourneyData._
import org.scalatest.{MustMatchers, WordSpec}
import play.api.libs.json.{JsResultException, Json}
import services.ForeignOfficeCountryService

class ALFESpec extends WordSpec with MustMatchers with ALFEFixtures {


  "an edit" should {
    "transform to a confirmable address and back again where isukMode == false" in {
      val edit = Edit("line1", Some("line2"), Some("line3"), "town", "ZZ1 1ZZ", ForeignOfficeCountryService.find(code = "GB").get.code)
      val conf = edit.toConfirmableAddress("audit ref")
      val expected = ConfirmableAddress(
        "audit ref",
        None,
        ConfirmableAddressDetails(
          Some(List("line1", "line2", "line3", "town")),
          Some("ZZ1 1ZZ"),
          ForeignOfficeCountryService.find(code = "GB")
        )
      )
      conf must be (expected)
      val ed2 = conf.toEdit
      ed2 must be (edit)
      ed2.toConfirmableAddress("audit ref") must be (expected)
    }

    "transform to a confirmable address and back again given less than three lines where isukMode == false" in {
      val edit = Edit("line1", None, None, "town", "ZZ1 1ZZ", ForeignOfficeCountryService.find(code = "GB").get.code)
      val conf = edit.toConfirmableAddress("audit ref")
      val expected = ConfirmableAddress(
        "audit ref",
        None,
        ConfirmableAddressDetails(
          Some(List("line1", "town")),
          Some("ZZ1 1ZZ"),
          ForeignOfficeCountryService.find(code = "GB")
        )
      )
      conf must be (expected)
      val ed2 = conf.toEdit
      ed2 must be (edit)
      ed2.toConfirmableAddress("audit ref") must be (expected)
    }

    "transform to a confirmable address and back again given less than three lines where isukMode == true" in {
      val edit = Edit("line1", None, None, "town", "ZZ1 1ZZ", "GB")
      val conf = edit.toConfirmableAddress("audit ref")
      val expected = ConfirmableAddress(
        "audit ref",
        None,
        ConfirmableAddressDetails(
          Some(List("line1", "town")),
          postcode = Some("ZZ1 1ZZ"),
          ForeignOfficeCountryService.find(code = "GB")
        )
      )
      conf must be (expected)
      val ed2 = conf.toEdit
      ed2 must be (edit)
      ed2.toConfirmableAddress("audit ref") must be (expected)
    }
    "transform to a confirmable address and back where postcode is empty isukMode == true" in {
      val edit = Edit("line1", None, None, "town", "", "FR")
      val conf = edit.toConfirmableAddress("audit ref")
      val expected = ConfirmableAddress(
        "audit ref",
        None,
        ConfirmableAddressDetails(
          Some(List("line1", "town")),
          postcode = None,
          ForeignOfficeCountryService.find(code = "FR")
        )
      )
      conf must be (expected)
      val ed2 = conf.toEdit
      ed2 must be (edit)
      ed2.toConfirmableAddress("audit ref") must be (expected)
    }
  }

  "a proposal" should {

    "transform to a confirmable address where county is ignored AND LINE 4 if town exists" in {
      val auditRef = "audit ref"
      val prop = ProposedAddress("GB1234567890", "postcode", List("line1", "line2", "line3", "line4"), Some("town"), Some("county"), ForeignOfficeCountryService.find(code = "GB").get)
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
      val prop = ProposedAddress("GB1234567890", "postcode", List("line1", "line2", "line3", "line4"), None, Some("county"), ForeignOfficeCountryService.find(code = "GB").get)
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
      val prop = ProposedAddress("GB1234567890", "postcode", List("line1", "line2", "line3", "line4"), None, None, ForeignOfficeCountryService.find(code = "GB").get)
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
      val prop = ProposedAddress("GB1234567890", "postcode", List("line1", "line2", "line3"), Some("town"), Some("county"), ForeignOfficeCountryService.find(code = "GB").get)
      val desc = prop.toDescription
      desc must be ("line1, line2, line3, town, county, postcode")
    }
  }

  "a confirmable address" should {
    "default country to GB" in {
      ConfirmableAddress("auditRef").address.country must be (ForeignOfficeCountryService.find(code = "GB"))
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

    val journeyConfigDefaults = JourneyConfigDefaults.EnglishConstants(true)
    val cfg = ResolvedJourneyConfig(c, journeyConfigDefaults)

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
      cfg.phaseFeedbackLink must be ("https://www.tax.service.gov.uk/contact/beta-feedback-unauthenticated?service=ALF")
    }

    "have beta help link" in {
      cfg.copy(c.copy(showPhaseBanner = Some(true))).phaseFeedbackLink must be ("https://www.tax.service.gov.uk/contact/beta-feedback-unauthenticated?service=ALF")
    }

    "have alpha help link" in {
      cfg.copy(c.copy(showPhaseBanner = Some(true), alphaPhase = Some(true))).phaseFeedbackLink must be ("https://www.tax.service.gov.uk/contact/beta-feedback-unauthenticated?service=ALF")
    }

    "have default phase banner html" in {
      cfg.phaseBannerHtml must be (journeyConfigDefaults.defaultPhaseBannerHtml(cfg.phaseFeedbackLink))
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
            |   "timeoutUrl" : "timeout",
            |   "timeoutKeepAliveUrl" : "keepAlive"
            | }
            |}
          """.stripMargin).as[JourneyConfig]

        parsedJson.timeout mustBe Some(Timeout(120,"timeout", "keepAlive"))
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
          |        "line3Label" : "Blank",
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
        line1Label = Some("1 Whooo Lane"), line2Label = Some("Whooo Land"), line3Label = Some("Blank"),  townLabel = Some("City World"),
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
    val journeyConfigDefaults = JourneyConfigDefaults.EnglishConstants(true)

    "default text when ukMode == true for ResolvedLookupPage.manualAddressLinkText" in {
      val res = ResolvedJourneyConfig(basicJourney(Some(true)).config, journeyConfigDefaults)
      res.lookupPage.manualAddressLinkText mustBe(journeyConfigDefaults.LOOKUP_PAGE_MANUAL_ADDRESS_LINK_TEXT)
    }
    "default text when ukMode == false for ResolvedLookupPage.manualAddressLinkText" in {
      val res = ResolvedJourneyConfig(basicJourney().config, journeyConfigDefaults)
      res.lookupPage.manualAddressLinkText mustBe(journeyConfigDefaults.LOOKUP_PAGE_MANUAL_ADDRESS_LINK_TEXT)
    }
  }
}