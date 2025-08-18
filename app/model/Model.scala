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

package model

import forms.Postcode
import play.api.libs.json._
import address.v2.Country
import utils.PostcodeHelper

case class CountryPicker(countryCode: String)

case class Lookup(filter: Option[String], postcode: String)

case class NonAbpLookup(filter: String)

case class Timeout(timeoutAmount: Int,
                   timeoutUrl: String,
                   timeoutKeepAliveUrl: Option[String])

case class Select(addressId: String)

case class Edit(organisation: Option[String],
                line1: Option[String],
                line2: Option[String],
                line3: Option[String],
                town: Option[String],
                postcode: String,
                countryCode: String = "GB") {

  def toConfirmableAddress(auditRef: String, findCountry: String => Option[Country]): ConfirmableAddress =
    ConfirmableAddress(
      auditRef,
      None,
      None, None, None, organisation,
      ConfirmableAddressDetails(
        organisation,
        List(line1, line2, line3).flatten,
        town,
        if (postcode.isEmpty) None
        else if (countryCode == "GB") Postcode.cleanupPostcode(postcode).map(_.toString)
        else Some(postcode),
        findCountry(countryCode)
      )
    )
}

case class ProposedAddress(addressId: String,
                           uprn: Option[Long],
                           parentUprn: Option[Long],
                           usrn: Option[Long],
                           organisation: Option[String],
                           postcode: Option[String],
                           town: Option[String],
                           lines: List[String] = List.empty,
                           country: Country = Country("GB", "United Kingdom"),
                           poBox: Option[String] = None) {

  def toConfirmableAddress(auditRef: String): ConfirmableAddress =
    ConfirmableAddress(
      auditRef,
      Some(addressId),
      uprn, parentUprn, usrn, organisation,
      ConfirmableAddressDetails(organisation, lines, town, postcode, Some(country), poBox)
    )

  def toDescription: String = {
    val addressDescription = (lines.take(3).map(Some(_)) :+ town :+ postcode).flatten.mkString(", ")
    organisation.fold(addressDescription)(org => s"$org, $addressDescription")
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
  organisation: Option[String] = None,
  lines: Seq[String] = Seq(),
  town: Option[String] = None,
  postcode: Option[String] = None,
  country: Option[Country] = Some(Country("GB", "United Kingdom")),
  poBox: Option[String] = None
) {

  def toDescription: String = {
    (organisation ++ lines ++ postcode.toList ++ country.toList.map(
      _.name
    )).mkString(", ") + "."
  }

  def toEdit: Edit = {
    Edit(
      organisation,
      lines.headOption,
      lines.lift(1),
      lines.lift(2),
      town,
      PostcodeHelper.displayPostcode(postcode),
      country.map(_.code).getOrElse("GB")
    )
  }
}

object CountryFormat {
  implicit val countryFormat: Format[Country] = Json.format[Country]
}

object ConfirmableAddressDetails {
  import CountryFormat._
  implicit val confirmableAddressDetailsFormat: OFormat[ConfirmableAddressDetails] =
    Json.format[ConfirmableAddressDetails]
}

object ConfirmableAddress {
  implicit val confirmableAddressFormat: OFormat[ConfirmableAddress] = Json.format[ConfirmableAddress]
}

object ProposedAddress {
  import CountryFormat._
  implicit val proposedAddressFormat: OFormat[ProposedAddress] = Json.format[ProposedAddress]

  def apply(addressId: String, uprn: Option[Long], parentUprn: Option[Long], usrn: Option[Long],
            organisation: Option[String], postcode: String, town: String): ProposedAddress =

    ProposedAddress(addressId = addressId, uprn = uprn, parentUprn = parentUprn, usrn = usrn,
      organisation = organisation, postcode = Some(postcode), town = Some(town))

  def apply(addressId: String, uprn: Option[Long], parentUprn: Option[Long], usrn: Option[Long],
            organisation: Option[String], postcode: String, town: String, lines: List[String]): ProposedAddress =

    ProposedAddress(addressId = addressId, uprn = uprn, parentUprn = parentUprn, usrn = usrn,
      organisation = organisation, postcode = Some(postcode), town = Some(town), lines = lines)

  def apply(addressId: String, uprn: Option[Long], parentUprn: Option[Long], usrn: Option[Long],
            organisation: Option[String], postcode: String, town: String, lines: List[String], country: Country): ProposedAddress =

    ProposedAddress(addressId = addressId, uprn = uprn, parentUprn = parentUprn, usrn = usrn,
      organisation = organisation, postcode = Some(postcode), town = Some(town), lines = lines, country = country)
}
