/*
 * Copyright 2021 HM Revenue & Customs
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

package model

import forms.Postcode
import play.api.libs.json._
import services.ForeignOfficeCountryService
import address.v2.Country
import utils.PostcodeHelper

case class Lookup(filter: Option[String], postcode: String)

case class Timeout(timeoutAmount: Int,
                   timeoutUrl: String,
                   timeoutKeepAliveUrl: Option[String])

case class Select(addressId: String)

case class Edit(line1: Option[String],
                line2: Option[String],
                line3: Option[String],
                town: Option[String],
                postcode: String,
                countryCode: String = "GB") {

  def toConfirmableAddress(auditRef: String): ConfirmableAddress =
    ConfirmableAddress(
      auditRef,
      None,
      None, None, None, None,
      ConfirmableAddressDetails(
        List(line1, line2, line3).flatten,
        town,
        if (postcode.isEmpty) None
        else if (countryCode == "GB") Postcode.cleanupPostcode(postcode).map(_.toString)
        else Some(postcode),
        ForeignOfficeCountryService.find(code = countryCode)
      )
    )
}

case class ProposedAddress(addressId: String,
                           uprn: Option[Long],
                           parentUprn: Option[Long],
                           usrn: Option[Long],
                           organisation: Option[String],
                           postcode: String,
                           town: String,
                           lines: List[String] = List.empty,
                           country: Country = ForeignOfficeCountryService
                             .find(code = "GB")
                             .getOrElse(Country("GB", "United Kingdom")),
                           poBox: Option[String] = None) {

  def toConfirmableAddress(auditRef: String): ConfirmableAddress =
    ConfirmableAddress(
      auditRef,
      Some(addressId),
      uprn, parentUprn, usrn, organisation,
      ConfirmableAddressDetails(lines, Some(town), Some(postcode), Some(country), poBox)
    )

  // TODO verify description format
  def toDescription: String = {
    lines.take(3).mkString(", ") + ", " + town + ", " + postcode
  }

}

case class ConfirmableAddress(auditRef: String,
                              id: Option[String] = None,
                              uprn: Option[Long] = None,
                              parentUprn: Option[Long] = None,
                              usrn: Option[Long] = None,
                              organisation: Option[String] = None,
                              address: ConfirmableAddressDetails =
                                ConfirmableAddressDetails()) {

  def toEdit: Edit = address.toEdit

  def toDescription: String = address.toDescription

}

case class ConfirmableAddressDetails(
  lines: Seq[String] = Seq(),
  town: Option[String] = None,
  postcode: Option[String] = None,
  country: Option[Country] = ForeignOfficeCountryService.find(code = "GB"),
  poBox: Option[String] = None
) {

  def toDescription: String = {
    (lines ++ postcode.toList ++ country.toList.map(
      _.name
    )).mkString(", ") + "."
  }

  def toEdit: Edit = {
    Edit(
      lines.headOption,
      lines.lift(1),
      lines.lift(2),
      town,
      PostcodeHelper.displayPostcode(postcode),
      country.map(_.code).get
    )
  }
}

object CountryFormat {
  implicit val countryFormat: Format[Country] = Json.format[Country]
}

object ConfirmableAddressDetails {
  import CountryFormat._
  implicit val confirmableAddressDetailsFormat =
    Json.format[ConfirmableAddressDetails]
}

object ConfirmableAddress {
  implicit val confirmableAddressFormat = Json.format[ConfirmableAddress]
}

object ProposedAddress {
  import CountryFormat._
  implicit val proposedAddressFormat = Json.format[ProposedAddress]
}
