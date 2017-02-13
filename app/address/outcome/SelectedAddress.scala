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

package address.outcome

import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.address.v2._


case class SelectedAddress(normativeAddress: Option[AddressRecord] = None,
                           userSuppliedAddress: Option[Address] = None,
                           international: Option[International] = None,
                           bfpo: Option[International] = None,
                           noFixedAddress: Boolean = false) {

  def toDefaultOutcomeFormat: DefaultOutcomeFormat = {
    international.map(int => mapInternational(int))
      .getOrElse(bfpo.map(b => mapBfpo(b))
        .getOrElse(userSuppliedAddress.map(addr => mapUserSupplied(addr))
          .getOrElse(normativeAddress.map(norm => mapNormativeAddress(norm))
            .getOrElse(DefaultOutcomeFormat(None, None)))))
  }

  private def mapInternational(int: International): DefaultOutcomeFormat = {
    DefaultOutcomeFormat(
      None,
      Some(DefaultOutcomeFormatAddress(
        Some(int.lines.take(4)),
        int.postcode,
        mapCountry(int.country)
      ))
    )
  }

  private def mapBfpo(bfpo: International): DefaultOutcomeFormat = {
    mapInternational(bfpo)
  }

  private def mapUserSupplied(addr: Address): DefaultOutcomeFormat = {
    DefaultOutcomeFormat(
      None,
      Some(DefaultOutcomeFormatAddress(
        Some(addr.lines.take(3) ++ addr.town.map(t => List(t)).getOrElse(List.empty)),
        Some(addr.postcode),
        mapCountry(Some(addr.country))
      ))
    )
  }

  private def mapNormativeAddress(norm: AddressRecord): DefaultOutcomeFormat = {
    DefaultOutcomeFormat(
      Some(norm.id),
      Some(DefaultOutcomeFormatAddress(
        Some(norm.address.lines.take(3) ++ norm.address.town.map(t => List(t)).getOrElse(List.empty)),
        Some(norm.address.postcode),
        mapCountry(Some(norm.address.country))
      ))
    )
  }

  private def mapCountry(country: Option[Country]): Option[DefaultOutcomeFormatAddressCountry] = {
    country.map(c => DefaultOutcomeFormatAddressCountry(Some(c.code), Some(c.name)))
  }

}

object SelectedAddress {
  implicit val format0: OFormat[Country] = Json.format[Country]
  implicit val format1: OFormat[LocalCustodian] = Json.format[LocalCustodian]
  implicit val format2: OFormat[Address] = Json.format[Address]
  implicit val format3: OFormat[AddressRecord] = Json.format[AddressRecord]
  implicit val format4: OFormat[International] = Json.format[International]
  implicit val format5: OFormat[SelectedAddress] = Json.format[SelectedAddress]
}

object DefaultOutcomeFormat {
  implicit val format1: OFormat[DefaultOutcomeFormatAddressCountry] = Json.format[DefaultOutcomeFormatAddressCountry]
  implicit val format2: OFormat[DefaultOutcomeFormatAddress] = Json.format[DefaultOutcomeFormatAddress]
  implicit val format3: OFormat[DefaultOutcomeFormat] = Json.format[DefaultOutcomeFormat]
}

case class DefaultOutcomeFormat(id: Option[String], address: Option[DefaultOutcomeFormatAddress])

case class DefaultOutcomeFormatAddress(lines: Option[List[String]], postcode: Option[String], country: Option[DefaultOutcomeFormatAddressCountry])

case class DefaultOutcomeFormatAddressCountry(code: Option[String], name: Option[String])

