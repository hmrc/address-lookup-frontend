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

package address.bfpo

import address.bfpo.BfpoForm.bfpoForm
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.mvc.Request
import play.twirl.api.Html
import uk.gov.hmrc.address.v2.{AddressRecord, Country}
import views.html.bfpo.proposalForm

object BfpoProposalsPage {

  import address.ViewConfig._

  def showAddressListProposalForm(tag: String, number: Option[String], postcode: String,
                                  guid: String, continue: Option[String],
                                  backUrl: Option[String], backText: Option[String],
                                  matchingAddresses: List[AddressRecord], editId: Option[String])
                                 (implicit request: Request[_]): Html = {
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

    val ad = ar.address
    val updatedDetails = BfpoData(
      guid = guid,
      continue = continue.getOrElse(defaultContinueUrl),
      backUrl = backUrl,
      backText = backText,
      lines = if (ad.lines.nonEmpty) Some(ad.lines.mkString("\n")) else None,
      postcode = Some(postcode),
      prevPostcode = Some(postcode),
      number = number,
      prevNumber = number,
      uprnId = Some(ar.id)
    )

    val editUrl = routes.BfpoAddressLookupController.getProposals(tag, number.getOrElse("-"), postcode, guid, continue, None, backUrl, backText)
    val filledInForm = bfpoForm.fill(updatedDetails)
    proposalForm(tag, cfg(tag).copy(indicator = Some(postcode)), filledInForm, matchingAddresses, selectedUprnId, editId.nonEmpty, editUrl.url)
  }
}
