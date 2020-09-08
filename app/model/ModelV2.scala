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

import java.time.ZonedDateTime

import config.FrontendAppConfig
import play.api.i18n.Messages
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads.min
import play.api.libs.json.{Format, JsObject, JsPath, Json, OWrites, __}
import uk.gov.hmrc.auth.core.syntax.retrieved.authSyntaxForRetrieved

case class JourneyDataV2(config: JourneyConfigV2,
                         proposals: Option[Seq[ProposedAddress]] = None,
                         selectedAddress: Option[ConfirmableAddress] = None,
                         confirmedAddress: Option[ConfirmableAddress] = None) {

  def resolveConfigV2(isWelsh: Boolean = false, appConfig: FrontendAppConfig)(
    implicit messages: Messages
  ) = ResolvedJourneyConfigV2(config, isWelsh, appConfig)

  val welshEnabled
    : Boolean = !config.requestedVersion.contains(1) && !(config.options.disableTranslations.isDefined && (config.options.disableTranslations exists (_ != false)))
}

case class JourneyConfigV2(version: Int,
                           options: JourneyOptions,
                           labels: Option[JourneyLabels] = None, //messages
                           requestedVersion: Option[Int] = None)

case class JourneyOptions(continueUrl: String,
                          homeNavHref: Option[String] = None,
                          signOutHref: Option[String] = None,
                          accessibilityFooterUrl: Option[String] = None,
                          additionalStylesheetUrl: Option[String] = None,
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
                          timeoutConfig: Option[TimeoutConfig] = None,
                          serviceHref: Option[String] = None) {

  val isUkMode: Boolean = ukMode contains true

}

case class SelectPageConfig(proposalListLimit: Option[Int] = None,
                            showSearchAgainLink: Option[Boolean] = None)

case class ConfirmPageConfig(showSearchAgainLink: Option[Boolean] = None,
                             showSubHeadingAndInfo: Option[Boolean] = None,
                             showChangeLink: Option[Boolean] = None,
                             showConfirmChangeText: Option[Boolean] = None)

case class TimeoutConfig(timeoutAmount: Int,
                         timeoutUrl: String,
                         timeoutKeepAliveUrl: Option[String] = None)

case class JourneyLabels(en: Option[LanguageLabels] = None,
                         cy: Option[LanguageLabels] = None)

case class LanguageLabels(appLevelLabels: Option[AppLevelLabels] = None,
                          selectPageLabels: Option[SelectPageLabels] = None,
                          lookupPageLabels: Option[LookupPageLabels] = None,
                          editPageLabels: Option[EditPageLabels] = None,
                          confirmPageLabels: Option[ConfirmPageLabels] = None)
object LanguageLabels {
  implicit val selectPageLablesWrites: OWrites[SelectPageLabels] = Json.writes[SelectPageLabels]
  implicit val lookupPageLablesWrites: OWrites[LookupPageLabels] = Json.writes[LookupPageLabels]

  implicit val languageLabelsWrites: OWrites[LanguageLabels] = Json.writes[LanguageLabels]

  implicit val languageLabelsWrites: OWrites[LanguageLabels] = {
    (__ \ "selectPage.title")
      .writeNullable[String]
      .and((__ \ "selectPage.heading").writeNullable[String])
      .and((__ \ "selectPage.headingWithPostcode").writeNullable[String])
      .and((__ \ "selectPage.proposalListLabel").writeNullable[String])
      .and((__ \ "selectPage.submitLabel").writeNullable[String])
      .and((__ \ "selectPage.searchAgainLable").writeNullable[String])
      .and((__ \ "selectPage.editAddressLinkText").writeNullable[String])
      .and((__ \ "lookupPage.title").writeNullable[String])
      .and((__ \ "lookupPage.heading").writeNullable[String])
      .and((__ \ "lookupPage.filterLabel").writeNullable[String])
      .and((__ \ "lookupPage.postcodeLabel").writeNullable[String])
      .and((__ \ "lookupPage.submitLabel").writeNullable[String])
      .and((__ \ "lookupPage.noResultsFoundMessage").writeNullable[String])
      .and((__ \ "lookupPage.resultLimitExceededMessage").writeNullable[String])
      .and((__ \ "lookupPage.manualAddressLinkText").writeNullable[String])
//      .and((__ \ "editPage.title").writeNullable[String])
//      .and((__ \ "editPage.heading").writeNullable[String])
//      .and((__ \ "editPage.line1Label").writeNullable[String])
//      .and((__ \ "editPage.line2Label").writeNullable[String])
//      .and((__ \ "editPage.line3Label").writeNullable[String])
//      .and((__ \ "editPage.townLabel").writeNullable[String])
//      .and((__ \ "editPage.postcodeLabel").writeNullable[String])
//      .and((__ \ "editPage.countryLabel").writeNullable[String])
//      .and((__ \ "editPage.submitLabel").writeNullable[String])
         (
        unlift(LanguageLabels.unapply)
      )
  }

//  def unapplyFlat(languageLabels: LanguageLabels): Option[
//    (Option[String],
//     Option[String],
//     Option[String],
//     Option[String],
//     Option[String],
//     Option[String],
//     Option[String],
//     Option[String],
//     Option[String],
//     Option[String],
//     Option[String],
//     Option[String],
//     Option[String],
//     Option[String],
//     Option[String],
//     Option[String],
//     Option[String],
//     Option[String],
//     Option[String],
//     Option[String],
//     Option[String],
//     Option[String],
//     Option[String],
//     Option[String])
//  ] = {
//    Some(
//      (
//        languageLabels.selectPageLabels.flatMap(_.title),
//        languageLabels.selectPageLabels.flatMap(_.heading),
//        languageLabels.selectPageLabels.flatMap(_.headingWithPostcode),
//        languageLabels.selectPageLabels.flatMap(_.proposalListLabel),
//        languageLabels.selectPageLabels.flatMap(_.submitLabel),
//        languageLabels.selectPageLabels.flatMap(_.searchAgainLinkText),
//        languageLabels.selectPageLabels.flatMap(_.editAddressLinkText),
//        languageLabels.lookupPageLabels.flatMap(_.title),
//        languageLabels.lookupPageLabels.flatMap(_.heading),
//        languageLabels.lookupPageLabels.flatMap(_.filterLabel),
//        languageLabels.lookupPageLabels.flatMap(_.postcodeLabel),
//        languageLabels.lookupPageLabels.flatMap(_.submitLabel),
//        languageLabels.lookupPageLabels.flatMap(_.noResultsFoundMessage),
//        languageLabels.lookupPageLabels.flatMap(_.resultLimitExceededMessage),
//        languageLabels.lookupPageLabels.flatMap(_.manualAddressLinkText),
//        languageLabels.editPageLabels.flatMap(_.title),
//        languageLabels.editPageLabels.flatMap(_.heading),
//        languageLabels.editPageLabels.flatMap(_.line1Label),
//        languageLabels.editPageLabels.flatMap(_.line2Label),
//        languageLabels.editPageLabels.flatMap(_.line3Label),
//        languageLabels.editPageLabels.flatMap(_.townLabel),
//        languageLabels.editPageLabels.flatMap(_.postcodeLabel),
//        languageLabels.editPageLabels.flatMap(_.countryLabel),
//        languageLabels.editPageLabels.flatMap(_.submitLabel)
//      )
//    )
  }
}

case class AppLevelLabels(navTitle: Option[String] = None,
                          phaseBannerHtml: Option[String] = None)

case class SelectPageLabels(title: Option[String] = None,
                            heading: Option[String] = None,
                            headingWithPostcode: Option[String] = None,
                            proposalListLabel: Option[String] = None,
                            submitLabel: Option[String] = None,
                            searchAgainLinkText: Option[String] = None,
                            editAddressLinkText: Option[String] = None)


case class LookupPageLabels(title: Option[String] = None,
                            heading: Option[String] = None,
                            filterLabel: Option[String] = None,
                            postcodeLabel: Option[String] = None,
                            submitLabel: Option[String] = None,
                            noResultsFoundMessage: Option[String] = None,
                            resultLimitExceededMessage: Option[String] = None,
                            manualAddressLinkText: Option[String] = None)

case class EditPageLabels(title: Option[String] = None,
                          heading: Option[String] = None,
                          line1Label: Option[String] = None,
                          line2Label: Option[String] = None,
                          line3Label: Option[String] = None,
                          townLabel: Option[String] = None,
                          postcodeLabel: Option[String] = None,
                          countryLabel: Option[String] = None,
                          submitLabel: Option[String] = None)

case class ConfirmPageLabels(title: Option[String] = None,
                             heading: Option[String] = None,
                             infoSubheading: Option[String] = None,
                             infoMessage: Option[String] = None,
                             submitLabel: Option[String] = None,
                             searchAgainLinkText: Option[String] = None,
                             changeLinkText: Option[String] = None,
                             confirmChangeText: Option[String] = None)

object JourneyDataV2 {
  implicit val format: Format[JourneyDataV2] = Json.format[JourneyDataV2]
}

object JourneyConfigV2 {
  implicit val format: Format[JourneyConfigV2] = Json.format[JourneyConfigV2]
}

object JourneyOptions {
  implicit val format: Format[JourneyOptions] = Json.format[JourneyOptions]
}

object SelectPageConfig {
  implicit val format: Format[SelectPageConfig] = Json.format[SelectPageConfig]
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

object JourneyLabels {
  implicit val format: Format[JourneyLabels] = Json.format[JourneyLabels]
}

object LanguageLabels {
  implicit val format: Format[LanguageLabels] = Json.format[LanguageLabels]
}

object AppLevelLabels {
  implicit val format: Format[AppLevelLabels] = Json.format[AppLevelLabels]
}

object SelectPageLabels {
  implicit val format: Format[SelectPageLabels] = Json.format[SelectPageLabels]
}

object LookupPageLabels {
  implicit val format: Format[LookupPageLabels] = Json.format[LookupPageLabels]
}

object EditPageLabels {
  implicit val format: Format[EditPageLabels] = Json.format[EditPageLabels]
}

object ConfirmPageLabels {
  implicit val format: Format[ConfirmPageLabels] =
    Json.format[ConfirmPageLabels]
}
