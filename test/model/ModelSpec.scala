package model

import org.scalatest.{MustMatchers, WordSpec}
import uk.gov.hmrc.address.v2.Countries

class ModelSpec extends WordSpec with MustMatchers {

  "an edit" should {

    // TODO this test is not yet complete, obviously!
    "transform to a confirmable address" in {
      val edit = Edit("line1", Some("line2"), Some("line3"), "town", "postcode", Some(Countries.UK.code))
      val conf = edit.toConfirmableAddress("audit ref")
      val expected = ConfirmableAddress(
        "audit ref",
        None,
        ConfirmableAddressDetails(
          Some(List("line1", "line2", "line3", "town")),
          Some("postcode"),
          Some(Countries.UK)
        )
      )
      conf must be (expected)
    }

  }

  "a proposal" should {

    "transform to a confirmable address" in {
      val auditRef = "audit ref"
      val prop = ProposedAddress("GB1234567890", "postcode", List("line1", "line2", "line3", "line4"), Some("town"), Some("county"), Countries.England)
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
      val prop = ProposedAddress("GB1234567890", "postcode", List("line1", "line2", "line3"), Some("town"), Some("county"), Countries.England)
      val desc = prop.toDescription
      desc must be ("line1, line2, line3, town, county, postcode, England")
    }

  }

}
