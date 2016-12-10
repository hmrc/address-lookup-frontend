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

package address.international

import address.PathElements
import play.api.data.Form
import play.api.data.Forms._
import uk.gov.hmrc.address.v2.{Country, International}


object IntAddressForm {
  val addressForm = Form[IntAddressData] {
    mapping(
      "guid" -> text,
      "continue-url" -> text,
      "address" -> optional(text),
      "country" -> optional(text),
      "code" -> optional(text)
    )(IntAddressData.apply)(IntAddressData.unapply)
  }
}


case class IntAddressData(
                           guid: String,
                           continue: String,
                           address: Option[String] = None,
                           country: Option[String] = None,
                           code: Option[String] = None
                         ) extends PathElements {

  def asInternational: International = {
    val lines = if (address.isEmpty) Nil else address.get.split('\n').map(_.trim).filterNot(_ == "").toList
    val c = if (country.isEmpty) None else Some(Country(code.get, country.get))
    International(lines, None, c)
    // TODO postcode
  }
}
