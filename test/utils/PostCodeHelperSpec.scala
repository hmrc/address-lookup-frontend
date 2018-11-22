package utils

import fixtures.ALFEFixtures
import org.scalatest.{MustMatchers, WordSpec}

class PostCodeHelperSpec extends WordSpec with MustMatchers with ALFEFixtures {

  "displayPostcode (option string)" should {
    "return formatted postcode" in {
      PostcodeHelper.displayPostcode(Some("  ZZ1   1Zz    ")) mustBe "ZZ1 1ZZ"
    }
    "return nothing for invalid postcode" in {
      PostcodeHelper.displayPostcode(Some("fbbfjebfje")) mustBe ""
    }
  }
  "displayPostcode string" should {
    "return formatted postcode" in {
      PostcodeHelper.displayPostcode("  ZZ1   1Zz    ") mustBe "ZZ1 1ZZ"
    }
    "return nothing for invalid postcode" in {
      PostcodeHelper.displayPostcode("fbbfjebfje") mustBe ""
    }
  }
}
