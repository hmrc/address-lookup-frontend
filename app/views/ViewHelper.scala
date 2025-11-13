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

package views

import address.v2.Country
import play.api.data.Form
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.SelectItem

object ViewHelper {

  private def countryToSelectItem(c: Country): SelectItem =
    SelectItem(
      value = Some(encodeCountryCode(c)),
      text = c.name,
      selected = false,
      attributes = Map("id" -> encodeCountryCode(c))
    )

  def countriesToSelectItems(cs: Seq[Country], form: Form[?])(implicit messages: Messages): Seq[SelectItem] =
    SelectItem(Some(""), messages("countryPickerPage.countryLabel")) +: countriesToSelectItems(cs)

  def countriesToSelectItems(cs:Seq[Country]): Seq[SelectItem] = cs.map(c => countryToSelectItem(c))

  private val countryCodeEncodingChar = "-"
  private val whitespaceChar = "_"

  def encodeCountryCode(c: Country): String =
    s"${c.code}${countryCodeEncodingChar}${c.name.replaceAll("\\p{Space}", whitespaceChar)}"
  def decodeCountryCode(c: String): String = c.replaceAll(s"${countryCodeEncodingChar}.*", "")
}
