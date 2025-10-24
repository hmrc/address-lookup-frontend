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

package model

import address.v2.Country
import fixtures.ALFEFixtures
import model.v2.JourneyConfigV2
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsResultException, Json}

class ALFESpec extends AnyWordSpec with Matchers with ALFEFixtures {

  val gb = Country("GB", "United Kingdom")
  val fr = Country("FR", "France")

  "an edit" should {
    "transform to a confirmable address with a formatted postcode when countrycode is GB" in {
      val edit = Edit(None, Some("line1"), Some("line2"), Some("line3"), Some("town"), "Z Z 1 1 Z Z", gb.code)
      val conf = edit.toConfirmableAddress("audit ref", _ => Some(gb))
      val expected = ConfirmableAddress(
        "audit ref",
        None, None, None, None, None,
        ConfirmableAddressDetails(
          None,
          List("line1", "line2", "line3"),
          Some("town"),
          Some("ZZ1 1ZZ"),
          Some(Country("GB", "United Kingdom"))))

      conf must be (expected)
    }

    "transform to a confirmable address leaving postcode as is when countrycode is not GB" in {
      val edit = Edit(None, Some("line1"), Some("line2"), Some("line3"), Some("town"), "Z Z 1 1 Z Z", fr.code)
      val conf = edit.toConfirmableAddress("audit ref", _ => Some(fr))
      val expected = ConfirmableAddress(
        "audit ref",
        None, None, None, None, None,
        ConfirmableAddressDetails(
          None,
          List("line1", "line2", "line3"),
          Some("town"),
          Some("Z Z 1 1 Z Z"),
          Some(fr)))

      conf must be (expected)
    }

    "transform to a confirmable address and back again where isukMode == false" in {
      val edit = Edit(None, Some("line1"), Some("line2"), Some("line3"), Some("town"), "ZZ1 1ZZ", gb.code)
      val conf = edit.toConfirmableAddress("audit ref", _ => Some(gb))
      val expected = ConfirmableAddress(
        "audit ref",
        None, None, None, None, None,
        ConfirmableAddressDetails(
          None,
          List("line1", "line2", "line3"),
          Some("town"),
          Some("ZZ1 1ZZ"),
          Some(gb)))

      conf must be (expected)
      val ed2 = conf.toEdit
      ed2 must be (edit)
      ed2.toConfirmableAddress("audit ref", _ => Some(gb)) must be (expected)
    }

    "transform to a confirmable address and back again given less than three lines where isukMode == false" in {
      val edit = Edit(None, Some("line1"), None, None, Some("town"), "ZZ1 1ZZ", gb.code)
      val conf = edit.toConfirmableAddress("audit ref", _ => Some(gb))
      val expected = ConfirmableAddress(
        "audit ref",
        None, None, None, None, None,
        ConfirmableAddressDetails(
          None,
          List("line1"),
          Some("town"),
          Some("ZZ1 1ZZ"),
          Some(gb)))

      conf must be (expected)
      val ed2 = conf.toEdit
      ed2 must be (edit)
      ed2.toConfirmableAddress("audit ref", _ => Some(gb)) must be (expected)
    }

    "transform to a confirmable address and back again given less than three lines where isukMode == true" in {
      val edit = Edit(None, Some("line1"), None, None, Some("town"), "ZZ1 1ZZ", "GB")
      val conf = edit.toConfirmableAddress("audit ref", _ => Some(gb))
      val expected = ConfirmableAddress(
        "audit ref",
        None, None, None, None, None,
        ConfirmableAddressDetails(
          None,
          List("line1"), Some("town"),
          postcode = Some("ZZ1 1ZZ"),
          Some(gb)))

      conf must be (expected)
      val ed2 = conf.toEdit
      ed2 must be (edit)
      ed2.toConfirmableAddress("audit ref", _ => Some(gb)) must be (expected)
    }

    "transform to a confirmable address and back where postcode is empty isukMode == true" in {
      val edit = Edit(None, Some("line1"), None, None, Some("town"), "", "FR")
      val conf = edit.toConfirmableAddress("audit ref", _ => Some(fr))
      val expected = ConfirmableAddress(
        "audit ref",
        None, None, None, None, None,
        ConfirmableAddressDetails(
          None,
          List("line1"), Some("town"),
          postcode = None,
          Some(fr)))

      conf must be (expected)
      val ed2 = conf.toEdit
      ed2 must be (edit)
      ed2.toConfirmableAddress("audit ref", _ => Some(fr)) must be (expected)
    }
  }

  "a proposal" should {

    "transform to a confirmable address where town is ignored" in {
      val auditRef = "audit ref"
      val prop = ProposedAddress("GB1234567890",       uprn = None, parentUprn = None, usrn = None, organisation = None, "postcode", "some-town", List("line1", "line2", "line3"), gb)
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
      val prop = ProposedAddress("GB1234567890",      uprn = None, parentUprn = None, usrn = None, organisation = None, "postcode", "some-town", List("line1", "line2", "line3"), gb)
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
      val prop = ProposedAddress("GB1234567890",      uprn = None, parentUprn = None, usrn = None, organisation = None, "postcode", "some-town", List("line1", "line2", "line3"),  gb)
      val desc = prop.toDescription
      desc must be ("line1, line2, line3, some-town, postcode")
    }
  }

  "a confirmable address" should {
    "default country to GB" in {
      ConfirmableAddress("auditRef").address.country must be (Some(gb))
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
