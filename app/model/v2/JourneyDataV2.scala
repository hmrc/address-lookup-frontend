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

import config.FrontendAppConfig
import model.{ConfirmableAddress, ProposedAddress, ResolvedJourneyConfigV2}
import play.api.libs.json._

case class JourneyDataV2(
                          config: JourneyConfigV2,
                          proposals: Option[Seq[ProposedAddress]] = None,
                          selectedAddress: Option[ConfirmableAddress] = None,
                          confirmedAddress: Option[ConfirmableAddress] = None,
                          countryCode: Option[String] = None
                        ) {

  def resolveConfigV2(appConfig: FrontendAppConfig): ResolvedJourneyConfigV2 = ResolvedJourneyConfigV2(config, appConfig)

  val welshEnabled
  : Boolean = !config.requestedVersion.contains(1) && !(config.options.disableTranslations.isDefined && (config
    .options.disableTranslations exists (_ != false)))
}

object JourneyDataV2 {
  implicit val format: Format[JourneyDataV2] = Json.format[JourneyDataV2]
}
