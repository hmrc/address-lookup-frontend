package model

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads.min
import play.api.libs.json.{Format, JsPath, Json}

case class JourneyDataV2(config: JourneyConfigV2,
                         proposals: Option[Seq[ProposedAddress]] = None,
                         selectedAddress: Option[ConfirmableAddress] = None,
                         confirmedAddress: Option[ConfirmableAddress] = None
                        ) {

  def resolveConfigV2(isWelsh: Boolean = false) = ResolvedJourneyConfigV2(config, isWelsh)

  val welshEnabled: Boolean = (config.version != 1) || (config.labels exists (_.cy.isDefined))
}

case class JourneyConfigV2(version: Int,
                           options: JourneyOptions,
                           labels: Option[JourneyLabels] = None
                          )

case class JourneyOptions(continueUrl: String,
                          homeNavHref: Option[String] = None,
                          additionalStylesheetUrl: Option[String] = None,
                          phaseFeedbackLink: Option[String] = None,
                          deskProServiceName: Option[String] = None,
                          showPhaseBanner: Option[Boolean] = None,
                          alphaPhase: Option[Boolean] = None,
                          showBackButtons: Option[Boolean] = None,
                          includeHMRCBranding: Option[Boolean] = None,
                          ukMode: Option[Boolean] = None,
                          allowedCountryCodes: Option[Set[String]] = None,
                          selectPageConfig: Option[SelectPageConfig] = None,
                          confirmPageConfig: Option[ConfirmPageConfig] = None,
                          timeoutConfig: Option[TimeoutConfig] = None
                         ) {
  val isUkMode: Boolean = ukMode contains true

}

case class SelectPageConfig(proposalListLimit: Option[Int] = None,
                            showSearchAgainLink: Option[Boolean] = None
                           )

case class ConfirmPageConfig(showSearchAgainLink: Option[Boolean] = None,
                             showSubHeadingAndInfo: Option[Boolean] = None,
                             showChangeLink: Option[Boolean] = None,
                             showConfirmChangeText: Option[Boolean] = None
                            )

case class TimeoutConfig(timeoutAmount: Int,
                         timeoutUrl: String
                        )

case class JourneyLabels(en: Option[LanguageLabels] = None,
                         cy: Option[LanguageLabels] = None
                        )

case class LanguageLabels(appLevelLabels: Option[AppLevelLabels] = None,
                          selectPageLabels: Option[SelectPageLabels] = None,
                          lookupPageLabels: Option[LookupPageLabels] = None,
                          editPageLabels: Option[EditPageLabels] = None,
                          confirmPageLabels: Option[ConfirmPageLabels] = None
                         )

case class AppLevelLabels(navTitle: Option[String] = None,
                          phaseBannerHtml: Option[String] = None
                         )

case class SelectPageLabels(title: Option[String] = None,
                            heading: Option[String] = None,
                            headingWithPostcode: Option[String] = None,
                            proposalListLabel: Option[String] = None,
                            submitLabel: Option[String] = None,
                            searchAgainLinkText: Option[String] = None,
                            editAddressLinkText: Option[String] = None
                           )

case class LookupPageLabels(title: Option[String] = None,
                            heading: Option[String] = None,
                            filterLabel: Option[String] = None,
                            postcodeLabel: Option[String] = None,
                            submitLabel: Option[String] = None,
                            noResultsFoundMessage: Option[String] = None,
                            resultLimitExceededMessage: Option[String] = None,
                            manualAddressLinkText: Option[String] = None
                           )

case class EditPageLabels(title: Option[String] = None,
                          heading: Option[String] = None,
                          line1Label: Option[String] = None,
                          line2Label: Option[String] = None,
                          line3Label: Option[String] = None,
                          townLabel: Option[String] = None,
                          postcodeLabel: Option[String] = None,
                          countryLabel: Option[String] = None,
                          submitLabel: Option[String] = None
                         )

case class ConfirmPageLabels(title: Option[String] = None,
                             heading: Option[String] = None,
                             infoSubheading: Option[String] = None,
                             infoMessage: Option[String] = None,
                             submitLabel: Option[String] = None,
                             searchAgainLinkText: Option[String] = None,
                             changeLinkText: Option[String] = None,
                             confirmChangeText: Option[String] = None
                            )

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
  implicit val format: Format[ConfirmPageConfig] = Json.format[ConfirmPageConfig]
}

object TimeoutConfig {
  implicit val timeoutFormat: Format[TimeoutConfig] = (
    (JsPath \ "timeoutAmount").format[Int](min(120)) and
      (JsPath \ "timeoutUrl").format[String]
    ) (TimeoutConfig.apply, unlift(TimeoutConfig.unapply))
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
  implicit val format: Format[ConfirmPageLabels] = Json.format[ConfirmPageLabels]
}