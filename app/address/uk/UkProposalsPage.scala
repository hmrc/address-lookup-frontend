/*
 * Copyright 2016 HM Revenue & Customs
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

package address.uk

import play.api.i18n.Messages
import play.api.mvc.Request
import play.twirl.api.Html
import uk.gov.hmrc.address.v2.{AddressRecord, Country}
import views.html.addressuk.proposalForm

object UkProposalsPage {

  import UkAddressForm.addressForm
  import address.ViewConfig._

  def showAddressListProposalForm(tag: String, data: UkAddressData, matchingAddresses: List[AddressRecord], editId: Option[String])
                                 (implicit request: Request[_], messages: Messages): Html = {
    val ar =
      if (editId.isDefined) matchingAddresses.find(_.id == editId.get).getOrElse(matchingAddresses.head)
      else matchingAddresses.head

    val selectedUprnId =
      if (matchingAddresses.size == 1) {
        ar.id
      } else if (editId.isDefined) {
        editId.get
      } else {
        ""
      }

    val country: Option[Country] = matchingAddresses.headOption.map(_.address.country)

    val updatedDetails = data.copy(
      uprnId = Some(ar.id),
      editedLines = if (ar.address.lines.nonEmpty) Some(ar.address.lines.mkString("\n")) else None,
      editedTown = ar.address.town,
      editedCounty = ar.address.county,
      countryCode = country.map(_.code)
    )

    val editUrl = routes.UkAddressLookupController.getProposals(tag,
      data.nameNo.getOrElse("-"), data.postcode.get, data.guid, Some(data.continue),
      editId, data.backUrl, data.backText)

    val filledInForm = addressForm.fill(updatedDetails)

    proposalForm(tag, cfg(tag).copy(indicator = Some(data.postcode.get.toString)), filledInForm, matchingAddresses,
      selectedUprnId, editId.nonEmpty, editUrl.url)
  }
}
