/*
 * Copyright 2020 HM Revenue & Customs
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
