package model

import com.fasterxml.jackson.core.JsonParseException
import fixtures.ALFEFixtures
import model.JourneyData._
import org.scalatest.{MustMatchers, WordSpec}
import play.api.libs.json.{JsPath, JsResultException, Json}
import services.ForeignOfficeCountryService
import JourneyConfigDefaults.{UK_LOOKUP_PAGE_MANUAL_ADDRESS_LINK_TEXT, LOOKUP_PAGE_MANUAL_ADDRESS_LINK_TEXT}

class ALFESpec extends WordSpec with MustMatchers with ALFEFixtures {

  "an edit" should {

    "transform to a confirmable address and back again where isukMode == false" in {
      val edit = Edit("line1", Some("line2"), Some("line3"), "town", "postcode", Some(ForeignOfficeCountryService.find("GB").get.code))
      val conf = edit.toConfirmableAddress("audit ref",false)
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
      ed2.toConfirmableAddress("audit ref",false) must be (expected)
    }

    "transform to a confirmable address and back again given less than three lines where isukMode == false" in {
      val edit = Edit("line1", None, None, "town", "postcode", Some(ForeignOfficeCountryService.find("GB").get.code))
      val conf = edit.toConfirmableAddress("audit ref",false)
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
      ed2.toConfirmableAddress("audit ref",false) must be (expected)
    }

    "transform to a confirmable address and back again given less than three lines where isukMode == true" in {
      val edit = Edit("line1", None, None, "town", "", Some("GB"))
      val conf = edit.toConfirmableAddress("audit ref",true)
      val expected = ConfirmableAddress(
        "audit ref",
        None,
        ConfirmableAddressDetails(
          Some(List("line1", "town")),
          postcode = None,
          ForeignOfficeCountryService.find("GB")
        )
      )
      conf must be (expected)
      val ed2 = conf.toEdit
      ed2 must be (edit)
      ed2.toConfirmableAddress("audit ref",true) must be (expected)
    }



    // #1 Scenario: International Address - no postcode
    "accept international address with no postcode where ukMode == false" in {
      Edit("", None, None, "", "", Some("FR")).isValidPostcode(false) must be (true)
    }
    // #2 Scenario Outline: International Address with postcode
 Seq(("case 1", "MN 99555"),
      ("case 2","A"),
      ("case 3","1"),
      ("case 4","999999999999"),
      ("case 5","ABC123XYZ123"),
      ("case 6", "SW778 2BH"),
      ("case 7", "SW1A 1AA")).foreach{
      case (caseNum,postcode) =>
        s"accept international address with any postcode where ukMode == false ($caseNum)" in {
          Edit("", None, None, "", postcode, Some("FR")).isValidPostcode(false) must be (true)
        }
    }

    // #3 Scenario: UK Address no postcode
    "accept a UK address with no postcode where ukMode == false" in {
      Edit("", None, None, "", "", Some("GB")).isValidPostcode(false) must be (true)
    }
    // #4 Scenario Outline: UK Address with Invalid PostCode
    Seq(
      ("case 1","MN 99555"),
      ("case 2","A"),
      ("case 3","1"),
      ("case 4","999999999999"),
      ("case 5","ABC123XYZ123"),
      ("case 6","SW778 2BH")).foreach {
      case (caseNum, postcode) =>
        s"not accept a UK address with an invalid postcode where ukMode == false ($caseNum)" in {
          Edit("", None, None, "", postcode, Some("GB")).isValidPostcode(false) must be(false)
        }
    }

    // #5 Scenario Outline: UK Address with Valid PostCode
    Seq(
      ("case 1","SW1A 1AA"),
      ("case 2","SW11 2BB"),
      ("case 3","SW7 9YY"),
      ("case 4","B1 1AA"),
      ("case 5","E1W 3CC"),
      ("case 6","B11 6HJ")).foreach{
      case  (caseNum,postcode) =>
    s"accept a UK address with a valid postcode where ukMode == false ($caseNum)" in {
      Edit("", None, None, "", postcode, Some("GB")).isValidPostcode(false) must be(true)
    }
    }

    // #6 Scenario Outline: UK Address with Valid PostCode, ukMode == true
    "not accept valid postcode as countrycode & postcode cannot be provided (someone has changed the html to add more form elements on client side) where ukMode == true" in {
      Edit("", None, None, "", "B11 6HJ", Some("GB")).isValidPostcode(true) must be(false)
    }
    // #7 Scenario Outline: No CountryCode or PostCode ukMode == true
    "accept blank postcode and no CountryCode where ukMode == true" in {
      Edit("", None, None, "", "", None).isValidPostcode(true) must be(true)
    }
  }

  "a proposal" should {

    "transform to a confirmable address" in {
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

    "be able to describe itself" in {
      val prop = ProposedAddress("GB1234567890", "postcode", List("line1", "line2", "line3"), Some("town"), Some("county"), ForeignOfficeCountryService.find("GB").get)
      val desc = prop.toDescription
      desc must be ("line1, line2, line3, town, county, postcode, United Kingdom")
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

    "not show back buttons by default" in {
      cfg.showBackButtons must be (false)
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
  "ResolvedJourneyConfig" should {
    "default text when ukMode == true for ResolvedLookupPage.manualAddressLinkText" in {
      val res = ResolvedJourneyConfig(basicJourney(Some(true)).config)
      println(res)
      res.lookupPage.manualAddressLinkText mustBe(UK_LOOKUP_PAGE_MANUAL_ADDRESS_LINK_TEXT)
    }
    "default text when ukMode == false for ResolvedLookupPage.manualAddressLinkText" in {
      val res = ResolvedJourneyConfig(basicJourney().config)
      res.lookupPage.manualAddressLinkText mustBe(LOOKUP_PAGE_MANUAL_ADDRESS_LINK_TEXT)
    }
  }

}
