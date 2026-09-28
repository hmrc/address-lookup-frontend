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
import play.api.libs.json.*

case class JourneyDataV2(
                          config: JourneyConfigV2,
                          proposals: Option[Seq[ProposedAddress]] = None,
                          selectedAddress: Option[ConfirmableAddress] = None,
                          confirmedAddress: Option[ConfirmableAddress] = None,
                          countryCode: Option[String] = None
                        ):

  def resolveConfigV2(appConfig: FrontendAppConfig): ResolvedJourneyConfigV2 = ResolvedJourneyConfigV2(config, appConfig)

  val welshEnabled: Boolean =
    !config.requestedVersion.contains(1) &&
      !(config.options.disableTranslations.isDefined &&
        (config.options.disableTranslations exists (_ != false)))

  def selectedAddressPassesConstraints(): Boolean =
    config.options.manualAddressEntryConfig -> selectedAddress.map(_.address) match
      case (Some(validationConfig), Some(address)) =>
        lazy val checkMandatoryFields: Boolean = validationConfig.mandatoryFields.fold(true):
          required =>
            /*
              For checking mandatory fields, (!A || B) is a "logical implication operation",
              where A implies B (sometimes written as A ⇒ B).
                If A is True, B must be True
                If B is False, B can be either True or False

                e.g. If Town is required, it must be defined.
                     If Postcode is optional, it doesn't matter if it's defined or empty.
             */
            !required.town || address.town.isDefined &&
            !required.postcode || address.town.isDefined &&
            !required.addressLine1 || address.lines.isDefinedAt(0) &&
            !required.addressLine2 || address.lines.isDefinedAt(1) &&
            !required.addressLine3 || address.lines.isDefinedAt(2)

        lazy val line1ValidLength = address.lines.unapply(0).fold(true)(_.length <= validationConfig.line1MaxLength)
        lazy val line2ValidLength = address.lines.unapply(1).fold(true)(_.length <= validationConfig.line2MaxLength)
        lazy val line3ValidLength = address.lines.unapply(2).fold(true)(_.length <= validationConfig.line3MaxLength)
        lazy val townValidLength = address.town.fold(true)(_.length <= validationConfig.townMaxLength)

        checkMandatoryFields && line1ValidLength && line2ValidLength && line3ValidLength && townValidLength
      case _ => true

object JourneyDataV2:
  given format: Format[JourneyDataV2] = Json.format[JourneyDataV2]
