package model

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads.min
import play.api.libs.json.{Format, JsPath, Json}

case class JourneyDataV2(
                          config: JourneyConfigV2,
                          proposals: Option[Seq[ProposedAddress]] = None,
                          selectedAddress: Option[ConfirmableAddress] = None,
                          confirmedAddress: Option[ConfirmableAddress] = None
                        )

object JourneyDataV2 {
  implicit val format: Format[JourneyDataV2] = Json.format[JourneyDataV2]
}

case class JourneyConfigV2(
                            version: Int,
                            config: JourneyOptions,
                            labels: Option[JourneyLabels]
                          )

object JourneyConfigV2 {
  implicit val format: Format[JourneyConfigV2] = Json.format[JourneyConfigV2]
}

case class JourneyOptions(
                           continueUrl: String,
                           homeNavHref: Option[String],
                           additionalStylesheetUrl: Option[String],
                           phaseFeedbackLink: Option[String],
                           deskProServiceName: Option[String],
                           showPhaseBanner: Option[Boolean],
                           alphaPhase: Option[Boolean],
                           showBackButtons: Option[Boolean],
                           includeHMRCBranding: Option[Boolean],
                           ukMode: Option[Boolean],
                           allowedCountryCodes: Option[Set[String]],
                           selectPageConfig: Option[SelectPageConfig],
                           confirmPageConfig: Option[ConfirmPageConfig],
                           timeoutConfig: Option[TimeoutConfig]
                         )

object JourneyOptions {
  implicit val format: Format[JourneyOptions] = Json.format[JourneyOptions]
}

case class SelectPageConfig(
                             proposalListLimit: Option[Int],
                             showSearchAgainLink: Option[Boolean]
                           )

object SelectPageConfig {
  implicit val format: Format[SelectPageConfig] = Json.format[SelectPageConfig]
}

case class ConfirmPageConfig(
                              showSearchAgainLink: Option[Boolean],
                              showSubHeadingAndInfo: Option[Boolean],
                              showChangeLink: Option[Boolean],
                              showConfirmChangeText: Option[Boolean]
                            )

object ConfirmPageConfig {
  implicit val format: Format[ConfirmPageConfig] = Json.format[ConfirmPageConfig]
}

case class TimeoutConfig(
                          timeoutAmount: Int,
                          timeoutUrl: String
                        )

object TimeoutConfig {
  implicit val timeoutFormat: Format[TimeoutConfig] = (
    (JsPath \ "timeoutAmount").format[Int](min(120)) and
      (JsPath \ "timeoutUrl").format[String]
    ) (TimeoutConfig.apply, unlift(TimeoutConfig.unapply))
}

case class JourneyLabels(
                          en: Option[LanguageLabels],
                          cy: Option[LanguageLabels]
                        )

object JourneyLabels {
  implicit val format: Format[JourneyLabels] = Json.format[JourneyLabels]
}

case class LanguageLabels(
                           appLevelLabels: Option[AppLevelLabels],
                           selectPageLabels: Option[SelectPageLabels],
                           lookupPageLabels: Option[LookupPageLabels],
                           editPageLabels: Option[EditPageLabels],
                           confirmPageLabels: Option[ConfirmPageLabels]
                         )

object LanguageLabels {
  implicit val format: Format[LanguageLabels] = Json.format[LanguageLabels]
}


case class AppLevelLabels(
                           navTitle: Option[String],
                           phaseBannerHtml: Option[String]
                         )

object AppLevelLabels {
  implicit val format: Format[AppLevelLabels] = Json.format[AppLevelLabels]
}

case class SelectPageLabels(
                             title: Option[String],
                             heading: Option[String],
                             headingWithPostcode: Option[String],
                             proposalListLabel: Option[String],
                             submitLabel: Option[String],
                             searchAgainLinkText: Option[String],
                             editAddressLinkText: Option[String]
                           )

object SelectPageLabels {
  implicit val format: Format[SelectPageLabels] = Json.format[SelectPageLabels]
}

case class LookupPageLabels(
                             title: Option[String],
                             heading: Option[String],
                             filterLabel: Option[String],
                             postcodeLabel: Option[String],
                             submitLabel: Option[String],
                             noResultsFoundMessage: Option[String],
                             resultLimitExceededMessage: Option[String],
                             manualAddressLinkText: Option[String]
                           )

object LookupPageLabels {
  implicit val format: Format[LookupPageLabels] = Json.format[LookupPageLabels]
}

case class EditPageLabels(
                           title: Option[String],
                           heading: Option[String],
                           line1Label: Option[String],
                           line2Label: Option[String],
                           line3Label: Option[String],
                           townLabel: Option[String],
                           postcodeLabel: Option[String],
                           countryLabel: Option[String],
                           submitLabel: Option[String]
                         )

object EditPageLabels {
  implicit val format: Format[EditPageLabels] = Json.format[EditPageLabels]
}

case class ConfirmPageLabels(
                              title: Option[String],
                              heading: Option[String],
                              infoSubheading: Option[String],
                              infoMessage: Option[String],
                              submitLabel: Option[String],
                              searchAgainLinkText: Option[String],
                              changeLinkText: Option[String],
                              confirmChangeText: Option[String]
                            )

object ConfirmPageLabels {
  implicit val format: Format[ConfirmPageLabels] = Json.format[ConfirmPageLabels]
}