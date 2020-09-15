/*
 * Copyright 2020 HM Revenue & Customs
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

case class ResolvedJourneyConfigV2(
  journeyConfig: JourneyConfigV2,
  appConfig: FrontendAppConfig
) {
  val version: Int = journeyConfig.version
  val options: ResolvedJourneyOptions =
    ResolvedJourneyOptions(journeyConfig.options, appConfig)
  val labels = journeyConfig.labels
}

case class ResolvedJourneyOptions(
  journeyOptions: JourneyOptions,
  appConfig: FrontendAppConfig
) {
  val continueUrl: String = journeyOptions.continueUrl
  val homeNavHref: Option[String] = journeyOptions.homeNavHref
  val signOutHref: Option[String] = journeyOptions.signOutHref
  val serviceHref: Option[String] = journeyOptions.serviceHref
  val accessibilityFooterUrl: Option[String] =
    journeyOptions.accessibilityFooterUrl
  val additionalStylesheetUrl: Option[String] =
    journeyOptions.additionalStylesheetUrl
  // This should never resolve to None here
  val phaseFeedbackLink: String =
    journeyOptions.phaseFeedbackLink.getOrElse(appConfig.feedbackUrl)
  val deskProServiceName: Option[String] = journeyOptions.deskProServiceName
    .orElse(Some(appConfig.contactFormServiceIdentifier))
  val showPhaseBanner: Boolean = journeyOptions.showPhaseBanner.getOrElse(false)
  val alphaPhase: Boolean = journeyOptions.alphaPhase.getOrElse(false)
  val phase: String = if (showPhaseBanner) {
    if (alphaPhase) "alpha" else "beta"
  } else ""
  val disableTranslations: Boolean =
    journeyOptions.disableTranslations.getOrElse(false)
  val showBackButtons: Boolean = journeyOptions.showBackButtons.getOrElse(true)
  val includeHMRCBranding: Boolean =
    journeyOptions.includeHMRCBranding.getOrElse(true)
  val isUkMode: Boolean = journeyOptions.ukMode.contains(true)
  val allowedCountryCodes: Option[Set[String]] =
    journeyOptions.allowedCountryCodes
  val selectPageConfig: ResolvedSelectPageConfig = ResolvedSelectPageConfig(
    journeyOptions.selectPageConfig.getOrElse(SelectPageConfig())
  )
  val confirmPageConfig: ResolvedConfirmPageConfig = ResolvedConfirmPageConfig(
    journeyOptions.confirmPageConfig.getOrElse(ConfirmPageConfig())
  )
  val timeoutConfig: Option[TimeoutConfig] = journeyOptions.timeoutConfig
}

case class ResolvedSelectPageConfig(selectPageConfig: SelectPageConfig) {
  val proposalListLimit: Option[Int] = selectPageConfig.proposalListLimit
  val showSearchAgainLink: Boolean =
    selectPageConfig.showSearchAgainLink.getOrElse(false)
}

case class ResolvedConfirmPageConfig(confirmPageConfig: ConfirmPageConfig) {
  val showChangeLink: Boolean = confirmPageConfig.showChangeLink.getOrElse(true)
  val showSubHeadingAndInfo: Boolean =
    confirmPageConfig.showSubHeadingAndInfo.getOrElse(false)
  val showSearchAgainLink: Boolean =
    confirmPageConfig.showSearchAgainLink.getOrElse(false)
  val showConfirmChangeText: Boolean =
    confirmPageConfig.showConfirmChangeText.getOrElse(false)
}
