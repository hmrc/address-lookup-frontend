/*
 * Copyright 2025 HM Revenue & Customs
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
import play.api.libs.json.{Json, OWrites, Reads, __}

case class MandatoryFieldsConfigModel(
                                       addressLine1: Boolean = false,
                                       addressLine2: Boolean = false,
                                       addressLine3: Boolean = false,
                                       postcode: Boolean = false,
                                       town: Boolean = false
                                     )

object MandatoryFieldsConfigModel {
  implicit val reads: Reads[MandatoryFieldsConfigModel] = (
    (__ \ "addressLine1").readWithDefault[Boolean](false) and
      (__ \ "addressLine2").readWithDefault[Boolean](false) and
      (__ \ "addressLine3").readWithDefault[Boolean](false) and
      (__ \ "postcode").readWithDefault[Boolean](false) and
      (__ \ "town").readWithDefault[Boolean](false)
  )(MandatoryFieldsConfigModel.apply _)
  
  implicit val writes: OWrites[MandatoryFieldsConfigModel] = Json.writes[MandatoryFieldsConfigModel]
}
