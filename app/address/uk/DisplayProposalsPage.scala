package address.uk

import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.mvc.Request
import play.twirl.api.Html
import uk.gov.hmrc.address.v2.{AddressRecord, Country}
import views.html.addressuk.proposalForm

object DisplayProposalsPage {

  import AddressForm.addressForm
  import address.ViewConfig._

  def showAddressListProposalForm(tag: String, nameNo: Option[String], postcode: String,
                                  guid: String, continue: Option[String],
                                  matchingAddresses: List[AddressRecord], editUprn: Option[Long])
                                 (implicit request: Request[_]): Html = {
    val ar = if (editUprn.isDefined) matchingAddresses.find(_.uprn == editUprn).getOrElse(matchingAddresses.head) else matchingAddresses.head
    val selectedUprn =
      if (matchingAddresses.size == 1) {
        ar.uprn.getOrElse(-1L)
      } else if (editUprn.isDefined) {
        editUprn.get
      } else {
        -1L
      }

    val country: Option[Country] = matchingAddresses.headOption.map(_.address.country)

    val ad = ar.address
    val updatedDetails = AddressData(
      guid = guid,
      continue = continue.getOrElse(defaultContinueUrl),
      nameNo = nameNo,
      prevNameNo = nameNo,
      postcode = Some(postcode),
      prevPostcode = Some(postcode),
      uprn = ar.uprn.map(_.toString),
      editedLines = if (ad.lines.nonEmpty) Some(ad.lines.mkString("\n")) else None,
      editedTown = ad.town,
      editedCounty = ad.county,
      countryCode = country.map(_.code)
    )

    val editUrl = routes.AddressLookupController.getProposals(tag, nameNo.getOrElse("-"), postcode, guid, continue, None)
    val filledInForm = addressForm.fill(updatedDetails)
    proposalForm(tag, cfg(tag).copy(indicator = Some(postcode)), filledInForm, matchingAddresses, selectedUprn, editUprn.isDefined, editUrl.url)
  }
}
