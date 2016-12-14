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

import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.mvc.Request
import play.twirl.api.Html
import uk.gov.hmrc.address.v2.{AddressRecord, Country}
import views.html.addressuk.proposalForm

object UkProposalsPage {

  import UkAddressForm.addressForm
  import address.ViewConfig._

  def showAddressListProposalForm(tag: String, nameNo: Option[String], postcode: String,
                                  guid: String, continue: Option[String], backUrl: Option[String], backText: Option[String],
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
    val updatedDetails = UkAddressData(
      guid = guid,
      continue = continue.getOrElse(defaultContinueUrl),
      backUrl = backUrl,
      backText = backText,
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

    val editUrl = routes.UkAddressLookupController.getProposals(tag, nameNo.getOrElse("-"), postcode, guid, continue, None, backUrl, backText)
    val filledInForm = addressForm.fill(updatedDetails)
    proposalForm(tag, cfg(tag).copy(indicator = Some(postcode)), filledInForm, matchingAddresses, selectedUprn, editUprn.isDefined, editUrl.url)
  }
}
