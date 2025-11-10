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

import play.api.libs.json.{Format, Json, OFormat}

case class JourneyConfigV2(
                            version: Int, 
                            options: JourneyOptions, 
                            labels: Option[JourneyLabels] = None, 
                            requestedVersion: Option[Int] = None
                          )

object JourneyConfigV2 {
  implicit val labelsFormat: Format[JourneyLabels] = OFormat(JourneyLabels.reads, JourneyLabels.writes)

  implicit val format: Format[JourneyConfigV2] = Json.format[JourneyConfigV2]
}
