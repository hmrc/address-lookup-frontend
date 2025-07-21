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

import config.FrontendAppConfig
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads.{min, max}
import play.api.libs.json._

case class JourneyDataV2(config: JourneyConfigV2,
                         proposals: Option[Seq[ProposedAddress]] = None,
                         selectedAddress: Option[ConfirmableAddress] = None,
                         confirmedAddress: Option[ConfirmableAddress] = None,
                         countryCode: Option[String] = None) {

  def resolveConfigV2(appConfig: FrontendAppConfig): ResolvedJourneyConfigV2 = ResolvedJourneyConfigV2(config, appConfig)

  val welshEnabled
  : Boolean = !config.requestedVersion.contains(1) && !(config.options.disableTranslations.isDefined && (config
    .options.disableTranslations exists (_ != false)))
}

case class JourneyConfigV2(version: Int,
                           options: JourneyOptions,
                           labels: Option[JourneyLabels] = None, //messages
                           requestedVersion: Option[Int] = None)

case class JourneyOptions(continueUrl: String,
                          homeNavHref: Option[String] = None,
                          signOutHref: Option[String] = None,
                          accessibilityFooterUrl: Option[String] = None,
                          phaseFeedbackLink: Option[String] = None,
                          deskProServiceName: Option[String] = None,
                          showPhaseBanner: Option[Boolean] = None,
                          alphaPhase: Option[Boolean] = None,
                          showBackButtons: Option[Boolean] = None,
                          disableTranslations: Option[Boolean] = None,
                          includeHMRCBranding: Option[Boolean] = None,
                          ukMode: Option[Boolean] = None,
                          allowedCountryCodes: Option[Set[String]] = None,
                          selectPageConfig: Option[SelectPageConfig] = None,
                          confirmPageConfig: Option[ConfirmPageConfig] = None,
                          manualAddressEntryConfig: Option[ManualAddressEntryConfig] = None,
                          timeoutConfig: Option[TimeoutConfig] = None,
                          serviceHref: Option[String] = None,
                          pageHeadingStyle: Option[String] = None) {

  val isUkMode: Boolean = ukMode contains true

}

case class SelectPageConfig(proposalListLimit: Option[Int] = Some(100),
                            showSearchAgainLink: Option[Boolean] = None)

case class ConfirmPageConfig(showSearchAgainLink: Option[Boolean] = None,
                             showSubHeadingAndInfo: Option[Boolean] = None,
                             showChangeLink: Option[Boolean] = None,
                             showConfirmChangeText: Option[Boolean] = None)

case class ManualAddressEntryConfig(line1MaxLength: Int = ManualAddressEntryConfig.defaultMax,
                                    line2MaxLength: Int = ManualAddressEntryConfig.defaultMax,
                                    line3MaxLength: Int = ManualAddressEntryConfig.defaultMax,
                                    townMaxLength: Int = ManualAddressEntryConfig.defaultMax)

case class TimeoutConfig(timeoutAmount: Int,
                         timeoutUrl: String,
                         timeoutKeepAliveUrl: Option[String] = None)

case class JourneyLabels(en: Option[LanguageLabels] = None,
                         cy: Option[LanguageLabels] = None)

object JourneyLabels {
  implicit val writes: OWrites[JourneyLabels] = Json.writes[JourneyLabels]
  implicit val reads: Reads[JourneyLabels] = Json.reads[JourneyLabels]
}

object JourneyDataV2 {
  implicit val format: Format[JourneyDataV2] = Json.format[JourneyDataV2]
}

object JourneyConfigV2 {
  implicit val labelsFormat: Format[JourneyLabels] = OFormat(JourneyLabels.reads, JourneyLabels.writes)

  implicit val format: Format[JourneyConfigV2] = Json.format[JourneyConfigV2]
}

object JourneyOptions {
  implicit val format: Format[JourneyOptions] = Json.format[JourneyOptions]
}

object SelectPageConfig {
  implicit val format: Format[SelectPageConfig] = Json.format[SelectPageConfig]
}

object ManualAddressEntryConfig {

  val defaultMax: Int = 255
  private val minLength: Int = 35

  val constraints: Reads[Int] = min(minLength) ~> max(defaultMax)

  implicit val reads: Reads[ManualAddressEntryConfig] = (
    (__ \ "line1MaxLength").readWithDefault[Int](defaultMax)(constraints) and
      (__ \ "line2MaxLength").readWithDefault[Int](defaultMax)(constraints) and
      (__ \ "line3MaxLength").readWithDefault[Int](defaultMax)(constraints) and
      (__ \ "townMaxLength").readWithDefault[Int](defaultMax)(constraints)
  )(ManualAddressEntryConfig.apply _)

  implicit val writes: Writes[ManualAddressEntryConfig] = Json.writes[ManualAddressEntryConfig]
}

object ConfirmPageConfig {
  implicit val format: Format[ConfirmPageConfig] =
    Json.format[ConfirmPageConfig]
}

object TimeoutConfig {
  implicit val timeoutFormat: Format[TimeoutConfig] = (
    (JsPath \ "timeoutAmount").format[Int](min(120)) and
      (JsPath \ "timeoutUrl").format[String] and
      (JsPath \ "timeoutKeepAliveUrl").formatNullable[String]
    )(TimeoutConfig.apply, unlift(TimeoutConfig.unapply))
}
