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

package controllers

import model.ProposedAddress
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ProposalsSpec extends AnyWordSpec with Matchers {

  "proposals" should {
    "naturally sort proposed addresses by addressId" in {
      val unsortedProposals = Seq(
        ProposedAddress(addressId = "GB990091234525", uprn = None, parentUprn = None, usrn = None, organisation = None, postcode = "ZZ11 1ZZ", town = "some-town", lines = List("1 main road")),
        ProposedAddress(addressId = "GB990091234530", uprn = None, parentUprn = None, usrn = None, organisation = None, postcode = "ZZ11 1ZZ", town = "some-town", lines = List("10 main road")),
        ProposedAddress(addressId = "GB990091234526", uprn = None, parentUprn = None, usrn = None, organisation = None, postcode = "ZZ11 1ZZ", town = "some-town", lines = List("2 main road"))
      )

      val proposals = Proposals(Some(unsortedProposals)).toHtmlOptions

      val expectedProposals = Seq(("GB990091234525", "1 main road, some-town, ZZ11 1ZZ"),
        ("GB990091234526", "2 main road, some-town, ZZ11 1ZZ"),
        ("GB990091234530", "10 main road, some-town, ZZ11 1ZZ"))

      proposals.shouldBe(expectedProposals)
    }
  }
}
