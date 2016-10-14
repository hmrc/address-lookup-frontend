package address.uk

import play.api.mvc.Request
import play.twirl.api.Html
import uk.gov.hmrc.address.v2.{AddressRecord, Countries}
import views.html.addressuk.proposalForm

object DisplayProposalsPage {

  import AddressForm.addressForm
  import address.ViewConfig._

  def showAddressListProposalForm(tag: String, nameNo: Option[String], postcode: String,
                                  guid: String, continue: Option[String],
                                  matchingAddresses: List[AddressRecord], edit: Option[Long],
                                  request: Request[_]): Html = {
    val ar = if (edit.isDefined) matchingAddresses.find(_.uprn == edit).getOrElse(matchingAddresses.head) else matchingAddresses.head
    val ad = ar.address
    val lines = if (ad.lines.nonEmpty) Some(ad.lines.mkString("\n")) else None
    val cu = continue.getOrElse(defaultContinueUrl)
    val selectedUprn =
      if (matchingAddresses.size == 1) {
        ar.uprn.getOrElse(-1L)
      } else if (edit.isDefined) {
        edit.get
      } else {
        -1L
      }
    val country = matchingAddresses.headOption.map(_.address.country).getOrElse(Countries.UK)
    val updatedDetails = AddressData(guid, cu, false, nameNo, Some(postcode), ar.uprn.map(_.toString), lines, ad.town, None, country.code)
    val editUrl = routes.AddressLookupController.getProposals(tag, nameNo.getOrElse("-"), postcode, guid, continue, None)
    val filledInForm = addressForm.fill(updatedDetails)
    proposalForm(tag, cfg(tag).copy(indicator = Some(postcode)), filledInForm, matchingAddresses, selectedUprn, edit.isDefined, editUrl.url)(request)
  }
}
