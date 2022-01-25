/*
 * Copyright 2022 HM Revenue & Customs
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

package controllers.api

import address.v2.Country
import play.api.libs.json.{Format, Json}

case class ConfirmedResponseAddress(auditRef: String,
                              id: Option[String] = None,
                              address: ConfirmedResponseAddressDetails =
                              ConfirmedResponseAddressDetails())

case class ConfirmedResponseAddressDetails(lines: Option[Seq[String]] = None,
                                          postcode: Option[String] = None,
                                          country: Option[Country] = None,
                                          poBox: Option[String] = None)

object ConfirmedResponseAddressDetails {
  implicit val countryFormat: Format[Country] = Json.format[Country]
  implicit val confirmableAddressDetailsFormat: Format[ConfirmedResponseAddressDetails] =
     Json.format[ConfirmedResponseAddressDetails]
}

object ConfirmedResponseAddress {
  implicit val confirmableAddressFormat: Format[ConfirmedResponseAddress] = Json.format[ConfirmedResponseAddress]
}
