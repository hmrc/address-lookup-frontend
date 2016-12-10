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
                           noFixedAddress: Boolean = false)


object SelectedAddress {
  implicit val format0: OFormat[Country] = Json.format[Country]
  implicit val format1: OFormat[LocalCustodian] = Json.format[LocalCustodian]
  implicit val format2: OFormat[Address] = Json.format[Address]
  implicit val format3: OFormat[AddressRecord] = Json.format[AddressRecord]
  implicit val format4: OFormat[International] = Json.format[International]
  implicit val format5: OFormat[SelectedAddress] = Json.format[SelectedAddress]
}

