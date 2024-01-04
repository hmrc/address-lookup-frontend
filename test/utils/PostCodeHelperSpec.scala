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

package utils

import fixtures.ALFEFixtures
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class PostCodeHelperSpec extends AnyWordSpec with Matchers with ALFEFixtures {

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
  "displayPostcode string for edge case postcode 'GIR 0AA'" should {
    "return formatted postcode" in {
      PostcodeHelper.displayPostcode("GIR 0AA") mustBe "GIR 0AA"
    }
  }
}
