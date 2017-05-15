package model

import org.scalatest.{MustMatchers, WordSpec}
import services.ForeignOfficeCountryService
import uk.gov.hmrc.address.v2.Countries

class ModelSpec extends WordSpec with MustMatchers {

  "an edit" should {

    "transform to a confirmable address and back again" in {
      val edit = Edit("line1", Some("line2"), Some("line3"), "town", "postcode", Some(ForeignOfficeCountryService.find("GB").get.code))
      val conf = edit.toConfirmableAddress("audit ref")
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
      ed2.toConfirmableAddress("audit ref") must be (expected)
    }

    "transform to a confirmable address and back again given less than three lines" in {
      val edit = Edit("line1", None, None, "town", "postcode", Some(ForeignOfficeCountryService.find("GB").get.code))
      val conf = edit.toConfirmableAddress("audit ref")
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
      ed2.toConfirmableAddress("audit ref") must be (expected)
    }

    // #1 Scenario: International Address - no postcode
    "accept international address with no postcode" in {
      Edit("", None, None, "", "", Some("FR")).isValidPostcode() must be (true)
    }

    // #2 Scenario Outline: International Address with postcode
    "accept international address with any postcode (case 1)" in {
      Edit("", None, None, "", "MN 99555", Some("FR")).isValidPostcode() must be (true)
    }
    "accept international address with any postcode (case 2)" in {
      Edit("", None, None, "", "A", Some("FR")).isValidPostcode() must be (true)
    }
    "accept international address with any postcode (case 3)" in {
      Edit("", None, None, "", "1", Some("FR")).isValidPostcode() must be (true)
    }
    "accept international address with any postcode (case 4)" in {
      Edit("", None, None, "", "999999999999", Some("FR")).isValidPostcode() must be (true)
    }
    "accept international address with any postcode (case 5)" in {
      Edit("", None, None, "", "ABC123XYZ123", Some("FR")).isValidPostcode() must be (true)
    }
    "accept international address with any postcode (case 6)" in {
      Edit("", None, None, "", "SW778 2BH", Some("FR")).isValidPostcode() must be (true)
    }
    "accept international address with any postcode (case 7)" in {
      Edit("", None, None, "", "SW1A 1AA", Some("FR")).isValidPostcode() must be (true)
    }

    // #3 Scenario: UK Address no postcode
    "accept a UK address with no postcode" in {
      Edit("", None, None, "", "", Some("GB")).isValidPostcode() must be (true)
    }

    // #4 Scenario Outline: UK Address with Invalid PostCode
    "not accept a UK address with an invalid postcode (case 1)" in {
      Edit("", None, None, "", "MN 99555", Some("GB")).isValidPostcode() must be (false)
    }
    "not accept a UK address with an invalid postcode (case 2)" in {
      Edit("", None, None, "", "A", Some("GB")).isValidPostcode() must be (false)
    }
    "not accept a UK address with an invalid postcode (case 3)" in {
      Edit("", None, None, "", "1", Some("GB")).isValidPostcode() must be (false)
    }
    "not accept a UK address with an invalid postcode (case 4)" in {
      Edit("", None, None, "", "999999999999", Some("GB")).isValidPostcode() must be (false)
    }
    "not accept a UK address with an invalid postcode (case 5)" in {
      Edit("", None, None, "", "ABC123XYZ123", Some("GB")).isValidPostcode() must be (false)
    }
    "not accept a UK address with an invalid postcode (case 6)" in {
      Edit("", None, None, "", "SW778 2BH", Some("GB")).isValidPostcode() must be (false)
    }

    // #5 Scenario Outline: UK Address with Valid PostCode
    "accept a UK address with a valid postcode (case 1)" in {
      Edit("", None, None, "", "SW1A 1AA", Some("GB")).isValidPostcode() must be (true)
    }
    "accept a UK address with a valid postcode (case 2)" in {
      Edit("", None, None, "", "SW11 2BB", Some("GB")).isValidPostcode() must be (true)
    }
    "accept a UK address with a valid postcode (case 3)" in {
      Edit("", None, None, "", "SW7 9YY", Some("GB")).isValidPostcode() must be (true)
    }
    "accept a UK address with a valid postcode (case 4)" in {
      Edit("", None, None, "", "B1 1AA", Some("GB")).isValidPostcode() must be (true)
    }
    "accept a UK address with a valid postcode (case 5)" in {
      Edit("", None, None, "", "E1W 3CC", Some("GB")).isValidPostcode() must be (true)
    }
    "accept a UK address with a valid postcode (case 6)" in {
      Edit("", None, None, "", "B11 6HJ", Some("GB")).isValidPostcode() must be (true)
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

}
