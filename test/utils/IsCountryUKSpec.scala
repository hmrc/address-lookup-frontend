package utils

import fixtures.ALFEFixtures
import model.{ConfirmableAddress, ConfirmableAddressDetails}
import org.scalatest.{MustMatchers, WordSpec}
import uk.gov.hmrc.address.v2.Country

class IsCountryUKSpec extends WordSpec with MustMatchers with ALFEFixtures {

  "countryCheck" should {
    "Return a false if there is no selected address" in {
      IsCountryUK.countryCheck(None) mustBe false
    }
    "Return a true if there is a selected address and country is GB" in {
      IsCountryUK.countryCheck(Some(ConfirmableAddress("audit", None, ConfirmableAddressDetails(None, None, Some(Country("GB", "United Kingdom")))))) mustBe true
    }
    "Return a false if there is a selected address and country is not GB" in {
      IsCountryUK.countryCheck(Some(ConfirmableAddress("audit", None, ConfirmableAddressDetails(None, None, Some(Country("FR", "France")))))) mustBe false
    }
  }
}
