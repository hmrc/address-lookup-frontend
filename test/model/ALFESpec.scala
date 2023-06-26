/*
 * Copyright 2023 HM Revenue & Customs
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

package model

import fixtures.ALFEFixtures
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsResultException, Json}
import services.ForeignOfficeCountryService

class ALFESpec extends AnyWordSpec with Matchers with ALFEFixtures {

  "an edit" should {
    "transform to a confirmable address with a formatted postcode when countrycode is GB" in {
      val edit = Edit(None, Some("line1"), Some("line2"), Some("line3"), Some("town"), "Z Z 1 1 Z Z", ForeignOfficeCountryService.find(code = "GB").get.code)
      val conf = edit.toConfirmableAddress("audit ref")
      val expected = ConfirmableAddress(
        "audit ref",
        None, None, None, None, None,
        ConfirmableAddressDetails(
          None,
          List("line1", "line2", "line3"),
          Some("town"),
          Some("ZZ1 1ZZ"),
          ForeignOfficeCountryService.find(code = "GB")))

      conf must be (expected)
    }

    "transform to a confirmable address leaving postcode as is when countrycode is not GB" in {
      val edit = Edit(None, Some("line1"), Some("line2"), Some("line3"), Some("town"), "Z Z 1 1 Z Z", ForeignOfficeCountryService.find(code = "FR").get.code)
      val conf = edit.toConfirmableAddress("audit ref")
      val expected = ConfirmableAddress(
        "audit ref",
        None, None, None, None, None,
        ConfirmableAddressDetails(
          None,
          List("line1", "line2", "line3"),
          Some("town"),
          Some("Z Z 1 1 Z Z"),
          ForeignOfficeCountryService.find(code = "FR")))

      conf must be (expected)
    }

    "transform to a confirmable address and back again where isukMode == false" in {
      val edit = Edit(None, Some("line1"), Some("line2"), Some("line3"), Some("town"), "ZZ1 1ZZ", ForeignOfficeCountryService.find(code = "GB").get.code)
      val conf = edit.toConfirmableAddress("audit ref")
      val expected = ConfirmableAddress(
        "audit ref",
        None, None, None, None, None,
        ConfirmableAddressDetails(
          None,
          List("line1", "line2", "line3"),
          Some("town"),
          Some("ZZ1 1ZZ"),
          ForeignOfficeCountryService.find(code = "GB")))

      conf must be (expected)
      val ed2 = conf.toEdit
      ed2 must be (edit)
      ed2.toConfirmableAddress("audit ref") must be (expected)
    }

    "transform to a confirmable address and back again given less than three lines where isukMode == false" in {
      val edit = Edit(None, Some("line1"), None, None, Some("town"), "ZZ1 1ZZ", ForeignOfficeCountryService.find(code = "GB").get.code)
      val conf = edit.toConfirmableAddress("audit ref")
      val expected = ConfirmableAddress(
        "audit ref",
        None, None, None, None, None,
        ConfirmableAddressDetails(
          None,
          List("line1"),
          Some("town"),
          Some("ZZ1 1ZZ"),
          ForeignOfficeCountryService.find(code = "GB")))

      conf must be (expected)
      val ed2 = conf.toEdit
      ed2 must be (edit)
      ed2.toConfirmableAddress("audit ref") must be (expected)
    }

    "transform to a confirmable address and back again given less than three lines where isukMode == true" in {
      val edit = Edit(None, Some("line1"), None, None, Some("town"), "ZZ1 1ZZ", "GB")
      val conf = edit.toConfirmableAddress("audit ref")
      val expected = ConfirmableAddress(
        "audit ref",
        None, None, None, None, None,
        ConfirmableAddressDetails(
          None,
          List("line1"), Some("town"),
          postcode = Some("ZZ1 1ZZ"),
          ForeignOfficeCountryService.find(code = "GB")))

      conf must be (expected)
      val ed2 = conf.toEdit
      ed2 must be (edit)
      ed2.toConfirmableAddress("audit ref") must be (expected)
    }

    "transform to a confirmable address and back where postcode is empty isukMode == true" in {
      val edit = Edit(None, Some("line1"), None, None, Some("town"), "", "FR")
      val conf = edit.toConfirmableAddress("audit ref")
      val expected = ConfirmableAddress(
        "audit ref",
        None, None, None, None, None,
        ConfirmableAddressDetails(
          None,
          List("line1"), Some("town"),
          postcode = None,
          ForeignOfficeCountryService.find(code = "FR")))

      conf must be (expected)
      val ed2 = conf.toEdit
      ed2 must be (edit)
      ed2.toConfirmableAddress("audit ref") must be (expected)
    }
  }

  "a proposal" should {

    "transform to a confirmable address where town is ignored" in {
      val auditRef = "audit ref"
      val prop = ProposedAddress("GB1234567890",       uprn = None, parentUprn = None, usrn = None, organisation = None, "postcode", "some-town", List("line1", "line2", "line3"), ForeignOfficeCountryService.find(code = "GB").get)
      val conf = prop.toConfirmableAddress(auditRef)
      val expected = ConfirmableAddress(
        auditRef,
        Some(prop.addressId), None, None, None, None,
        address = ConfirmableAddressDetails(
          None,
          prop.lines.take(3),
          Some("some-town"),
          prop.postcode,
          Some(prop.country)))

      conf must be (expected)
    }

    "transform to a confirmable address With all 4 address lines as town is None" in {
      val auditRef = "audit ref"
      val prop = ProposedAddress("GB1234567890",      uprn = None, parentUprn = None, usrn = None, organisation = None, "postcode", "some-town", List("line1", "line2", "line3"), ForeignOfficeCountryService.find(code = "GB").get)
      val conf = prop.toConfirmableAddress(auditRef)
      val expected = ConfirmableAddress(
        auditRef,
        Some(prop.addressId), None, None, None, None,
        address = ConfirmableAddressDetails(
          None,
          prop.lines.take(3),
          Some("some-town"),
          prop.postcode,
          Some(prop.country)))

      conf must be (expected)
    }

    "be able to describe itself" in {
      val prop = ProposedAddress("GB1234567890",      uprn = None, parentUprn = None, usrn = None, organisation = None, "postcode", "some-town", List("line1", "line2", "line3"),  ForeignOfficeCountryService.find(code = "GB").get)
      val desc = prop.toDescription
      desc must be ("line1, line2, line3, some-town, postcode")
    }
  }

  "a confirmable address" should {
    "default country to GB" in {
      ConfirmableAddress("auditRef").address.country must be (ForeignOfficeCountryService.find(code = "GB"))
    }
  }

  "A timeout" should {
    "throw error" when {
      "timeoutAmount is less than 120 seconds" in {
        def parseJson = Json.parse(
          """
            |{
            | "continueUrl" : "continue",
            | "timeout" : {
            |   "timeoutAmount" : 80,
            |   "timeoutUrl" : "timeout"
            | }
            |}
          """.stripMargin).as[JourneyConfigV2]

        intercept[JsResultException](parseJson)
      }
    }
  }
}
