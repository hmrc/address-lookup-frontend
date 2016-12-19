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

import address.PathElements
import play.api.data.Form
import play.api.data.Forms._
import uk.gov.hmrc.address.v2.{Address, Countries}


object UkAddressForm {
  val addressForm = Form[UkAddressData] {
    mapping(
      "guid" -> text,
      "continue-url" -> text,
      "back-url" -> optional(text),
      "back-text" -> optional(text),
      "no-fixed-address" -> boolean,
      "house-name-number" -> optional(text),
      "postcode" -> optional(text),
      "prev-house-name-number" -> optional(text),
      "prev-postcode" -> optional(text),
      "radio-inline-group" -> optional(text),
      "address-lines" -> optional(text),
      "town" -> optional(text),
      "county" -> optional(text),
      "country-code" -> optional(text)
    )(UkAddressData.apply)(UkAddressData.unapply)
  }
}


case class UkAddressData(
                          guid: String,
                          continue: String,
                          backUrl: Option[String],
                          backText: Option[String],
                          noFixedAddress: Boolean = false,
                          nameNo: Option[String] = None,
                          postcode: Option[String] = None,
                          prevNameNo: Option[String] = None,
                          prevPostcode: Option[String] = None,
                          uprnId: Option[String] = None,
                          editedLines: Option[String] = None,
                          editedTown: Option[String] = None,
                          editedCounty: Option[String] = None,
                          countryCode: Option[String] = None
                      ) extends PathElements {

  def hasBeenUpdated: Boolean = (prevNameNo != nameNo) || (prevPostcode != postcode)

  def editedAddress: Option[Address] =
    if (editedLines.isDefined || editedTown.isDefined || editedCounty.isDefined) {
      Some(Address(
        editedLines.toList.flatMap(_.split("\n").map(_.trim)),
        editedTown.map(_.trim),
        editedCounty.map(_.trim),
        postcode.get.trim,
        None, Countries.find(countryCode.get).get))
    } else {
      None
    }
}
