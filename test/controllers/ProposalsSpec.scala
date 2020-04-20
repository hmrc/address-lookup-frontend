package controllers

import model.ProposedAddress
import uk.gov.hmrc.play.test.UnitSpec

class ProposalsSpec extends UnitSpec {

  "proposals" should {
    "naturally sort proposed addresses by addressId" in {
      val unsortedProposals = Seq(ProposedAddress(addressId = "GB990091234525", lines = List("1 main road"), postcode = "ZZ11 1ZZ"),
                                  ProposedAddress(addressId = "GB990091234530", lines = List("10 main road"), postcode = "ZZ11 1ZZ"),
                                  ProposedAddress(addressId = "GB990091234526", lines = List("2 main road"), postcode = "ZZ11 1ZZ"))

      val proposals = Proposals(Some(unsortedProposals)).toHtmlOptions

      val expectedProposals = Seq(("GB990091234525", "1 main road, ZZ11 1ZZ"),
                                  ("GB990091234526", "2 main road, ZZ11 1ZZ"),
                                  ("GB990091234530", "10 main road, ZZ11 1ZZ"))

      proposals shouldBe expectedProposals
    }
  }
}
