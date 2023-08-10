/*
 * Copyright 2023 HM Revenue & Customs
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
import uk.gov.hmrc.govukfrontend.views.Aliases.SelectItem

object ViewHelper {
  def countryToSelectItem(c: Country, form: Form[_]): SelectItem = {
    SelectItem(
      value = Some(encodeCountryCode(c)),
      text = c.name,
      selected = c.code == form("countryCode").value.getOrElse(""),
      attributes = Map("id" -> s"countryCode-${c.code}"))
  }

  def countriesToSelectItems(cs: Seq[Country], form: Form[_]): Seq[SelectItem] = {
    SelectItem(Some(""), "Select a country") +: cs.map(c => countryToSelectItem(c, form))
  }

  private val countryCodeEncodingChar = "-"
  def encodeCountryCode(c: Country): String = s"${c.code}${countryCodeEncodingChar}${c.name}"
  def decodeCountryCode(c: String): String = c.replaceAll(s"${countryCodeEncodingChar}.*", "")
}
