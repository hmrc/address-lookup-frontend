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

package model.v2

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads.{max, min}
import play.api.libs.json._

case class ManualAddressEntryConfig(
                                     line1MaxLength: Int = ManualAddressEntryConfig.defaultMax,
                                     line2MaxLength: Int = ManualAddressEntryConfig.defaultMax,
                                     line3MaxLength: Int = ManualAddressEntryConfig.defaultMax,
                                     townMaxLength: Int = ManualAddressEntryConfig.defaultMax,
                                     mandatoryFields: Option[MandatoryFieldsConfigModel] = None,
                                     showOrganisationName: Boolean = true
                                   )

object ManualAddressEntryConfig {

  val defaultMax: Int = 255
  private val minLength: Int = 35

  val constraints: Reads[Int] = min(minLength) ~> max(defaultMax)

  implicit val reads: Reads[ManualAddressEntryConfig] = (
    (__ \ "line1MaxLength").readWithDefault[Int](defaultMax)(constraints) and
      (__ \ "line2MaxLength").readWithDefault[Int](defaultMax)(constraints) and
      (__ \ "line3MaxLength").readWithDefault[Int](defaultMax)(constraints) and
      (__ \ "townMaxLength").readWithDefault[Int](defaultMax)(constraints) and
      (__ \ "mandatoryFields").readNullable[MandatoryFieldsConfigModel] and
      (__ \ "showOrganisationName").readWithDefault[Boolean](true)
  )(ManualAddressEntryConfig.apply _)

  implicit val writes: Writes[ManualAddressEntryConfig] = Json.writes[ManualAddressEntryConfig]
}
