package model

import config.FrontendAppConfig
import javax.security.auth.login.AppConfigurationEntry
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
                            options: JourneyOptions,
                            labels: Option[JourneyLabels] = None
                          )

case class ResolvedJourneyConfigV2(journeyConfig: JourneyConfigV2) {
  val version: Int = journeyConfig.version
  val options: ResolvedJourneyOptions = ResolvedJourneyOptions(journeyConfig.options)
  val labels: ResolvedJourneyLabels = ResolvedJourneyLabels(journeyConfig.labels.getOrElse(JourneyLabels()), options.phaseFeedbackLink)
}

object JourneyConfigV2 {
  implicit val format: Format[JourneyConfigV2] = Json.format[JourneyConfigV2]
}

case class JourneyOptions(
                           continueUrl: String,
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
                         )

case class ResolvedJourneyOptions(journeyOptions: JourneyOptions) {
  val continueUrl: String = journeyOptions.continueUrl
  val homeNavHref: String = journeyOptions.homeNavHref.getOrElse(FrontendAppConfig.homeUrl)
  val additionalStylesheetUrl: Option[String] = journeyOptions.additionalStylesheetUrl
  val phaseFeedbackLink: String = journeyOptions.phaseFeedbackLink.getOrElse(FrontendAppConfig.feedbackUrl)
  val deskProServiceName: Option[String] = journeyOptions.deskProServiceName.fold(Some(FrontendAppConfig.contactFormServiceIdentifier))(Some(_))
  val showPhaseBanner: Boolean = journeyOptions.showPhaseBanner.getOrElse(false)
  val alphaPhase: Boolean = journeyOptions.alphaPhase.getOrElse(false)
  val phase: String = if (showPhaseBanner) {
    if (alphaPhase) "alpha" else "beta"
  } else ""
  val showBackButtons: Boolean = journeyOptions.showBackButtons.getOrElse(true)
  val includeHMRCBranding: Boolean = journeyOptions.includeHMRCBranding.getOrElse(true)
  val isUkMode: Boolean = journeyOptions.ukMode.contains(true)
  val allowedCountryCodes: Option[Set[String]] = journeyOptions.allowedCountryCodes
  val selectPageConfig: ResolvedSelectPageConfig = ResolvedSelectPageConfig(journeyOptions.selectPageConfig.getOrElse(SelectPageConfig()))
  val confirmPageConfig: ResolvedConfirmPageConfig = ResolvedConfirmPageConfig(journeyOptions.confirmPageConfig.getOrElse(ConfirmPageConfig()))
  val timeoutConfig: Option[TimeoutConfig] = journeyOptions.timeoutConfig
}

object JourneyOptions {
  implicit val format: Format[JourneyOptions] = Json.format[JourneyOptions]
}

case class SelectPageConfig(
                             proposalListLimit: Option[Int] = None,
                             showSearchAgainLink: Option[Boolean] = None
                           )

case class ResolvedSelectPageConfig(selectPageConfig: SelectPageConfig) {
  val proposalListLimit: Option[Int] = selectPageConfig.proposalListLimit
  val showSearchAgainLink: Boolean = selectPageConfig.showSearchAgainLink.getOrElse(false)
}

object SelectPageConfig {
  implicit val format: Format[SelectPageConfig] = Json.format[SelectPageConfig]
}

case class ConfirmPageConfig(
                              showSearchAgainLink: Option[Boolean] = None,
                              showSubHeadingAndInfo: Option[Boolean] = None,
                              showChangeLink: Option[Boolean] = None,
                              showConfirmChangeText: Option[Boolean] = None
                            )

case class ResolvedConfirmPageConfig(confirmPageConfig: ConfirmPageConfig) {
  val showChangeLink: Boolean = confirmPageConfig.showChangeLink.getOrElse(true)
  val showSubHeadingAndInfo: Boolean = confirmPageConfig.showSubHeadingAndInfo.getOrElse(false)
  val showSearchAgainLink: Boolean = confirmPageConfig.showSearchAgainLink.getOrElse(false)
  val showConfirmChangeText: Boolean = confirmPageConfig.showConfirmChangeText.getOrElse(false)
}

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
                          en: Option[LanguageLabels] = None,
                          cy: Option[LanguageLabels] = None
                        )

case class ResolvedJourneyLabels(journeyLabels: JourneyLabels, phaseFeedbackLink: String) {
  val en: ResolvedLanguageLabels = ResolvedLanguageLabels(journeyLabels.en.getOrElse(LanguageLabels()), phaseFeedbackLink)
  val cy: Option[ResolvedLanguageLabels] = None
}

object JourneyLabels {
  implicit val format: Format[JourneyLabels] = Json.format[JourneyLabels]
}

case class LanguageLabels(
                           appLevelLabels: Option[AppLevelLabels] = None,
                           selectPageLabels: Option[SelectPageLabels] = None,
                           lookupPageLabels: Option[LookupPageLabels] = None,
                           editPageLabels: Option[EditPageLabels] = None,
                           confirmPageLabels: Option[ConfirmPageLabels] = None
                         )

case class ResolvedLanguageLabels(languageLabels: LanguageLabels, phaseFeedbackLink: String) {
  val appLevelLabels: ResolvedAppLevelLabels = ResolvedAppLevelLabels(languageLabels.appLevelLabels.getOrElse(AppLevelLabels()), phaseFeedbackLink)
  val selectPageLabels: ResolvedSelectPageLabels = ResolvedSelectPageLabels(languageLabels.selectPageLabels.getOrElse(SelectPageLabels()))
  val lookupPageLabels: ResolvedLookupPageLabels = ResolvedLookupPageLabels(languageLabels.lookupPageLabels.getOrElse(LookupPageLabels()))
  val editPageLabels: ResolvedEditPageLabels = ResolvedEditPageLabels(languageLabels.editPageLabels.getOrElse(EditPageLabels()))
  val confirmPageLabels: ResolvedConfirmPageLabels = ResolvedConfirmPageLabels(languageLabels.confirmPageLabels.getOrElse(ConfirmPageLabels()))
}

object LanguageLabels {
  implicit val format: Format[LanguageLabels] = Json.format[LanguageLabels]
}

case class AppLevelLabels(
                           navTitle: Option[String] = None,
                           phaseBannerHtml: Option[String] = None
                         )

case class ResolvedAppLevelLabels(appLevelLabels: AppLevelLabels, phaseFeedbackLink: String) {
  val navTitle: Option[String] = appLevelLabels.navTitle
  val phaseBannerHtml: String = appLevelLabels.phaseBannerHtml.getOrElse(JourneyConfigDefaults.defaultPhaseBannerHtml(phaseFeedbackLink))
}

object AppLevelLabels {
  implicit val format: Format[AppLevelLabels] = Json.format[AppLevelLabels]
}

case class SelectPageLabels(
                             title: Option[String] = None,
                             heading: Option[String] = None,
                             headingWithPostcode: Option[String] = None,
                             proposalListLabel: Option[String] = None,
                             submitLabel: Option[String] = None,
                             searchAgainLinkText: Option[String] = None,
                             editAddressLinkText: Option[String] = None
                           )

case class ResolvedSelectPageLabels(selectPageLabels: SelectPageLabels) {
  val title: String = selectPageLabels.title.getOrElse(JourneyConfigDefaults.SELECT_PAGE_TITLE)
  val heading: String = selectPageLabels.heading.getOrElse(JourneyConfigDefaults.SELECT_PAGE_HEADING)
  val headingWithPostcode: String = selectPageLabels.headingWithPostcode.getOrElse(JourneyConfigDefaults.SELECT_PAGE_HEADING_WITH_POSTCODE)
  val proposalListLabel: String = selectPageLabels.proposalListLabel.getOrElse(JourneyConfigDefaults.SELECT_PAGE_PROPOSAL_LIST_LABEL)
  val submitLabel: String = selectPageLabels.submitLabel.getOrElse(JourneyConfigDefaults.SELECT_PAGE_SUBMIT_LABEL)
  val searchAgainLinkText: String = selectPageLabels.searchAgainLinkText.getOrElse(JourneyConfigDefaults.SEARCH_AGAIN_LINK_TEXT)
  val editAddressLinkText: String = selectPageLabels.editAddressLinkText.getOrElse(JourneyConfigDefaults.EDIT_LINK_TEXT)
}

object SelectPageLabels {
  implicit val format: Format[SelectPageLabels] = Json.format[SelectPageLabels]
}

case class LookupPageLabels(
                             title: Option[String] = None,
                             heading: Option[String] = None,
                             filterLabel: Option[String] = None,
                             postcodeLabel: Option[String] = None,
                             submitLabel: Option[String] = None,
                             noResultsFoundMessage: Option[String] = None,
                             resultLimitExceededMessage: Option[String] = None,
                             manualAddressLinkText: Option[String] = None
                           )

case class ResolvedLookupPageLabels(lookupPageLabels: LookupPageLabels) {
  val title: String = lookupPageLabels.title.getOrElse(JourneyConfigDefaults.LOOKUP_PAGE_TITLE)
  val heading: String = lookupPageLabels.heading.getOrElse(JourneyConfigDefaults.LOOKUP_PAGE_HEADING)
  val filterLabel: String = lookupPageLabels.filterLabel.getOrElse(JourneyConfigDefaults.LOOKUP_PAGE_FILTER_LABEL)
  val postcodeLabel: String = lookupPageLabels.postcodeLabel.getOrElse(JourneyConfigDefaults.LOOKUP_PAGE_POSTCODE_LABEL)
  val submitLabel: String = lookupPageLabels.submitLabel.getOrElse(JourneyConfigDefaults.LOOKUP_PAGE_SUBMIT_LABEL)
  val noResultsFoundMessage: Option[String] = lookupPageLabels.noResultsFoundMessage
  val resultLimitExceededMessage: Option[String] = lookupPageLabels.resultLimitExceededMessage
  val manualAddressLinkText: String = lookupPageLabels.manualAddressLinkText.getOrElse(JourneyConfigDefaults.LOOKUP_PAGE_MANUAL_ADDRESS_LINK_TEXT)
}

object LookupPageLabels {
  implicit val format: Format[LookupPageLabels] = Json.format[LookupPageLabels]
}

case class EditPageLabels(
                           title: Option[String] = None,
                           heading: Option[String] = None,
                           line1Label: Option[String] = None,
                           line2Label: Option[String] = None,
                           line3Label: Option[String] = None,
                           townLabel: Option[String] = None,
                           postcodeLabel: Option[String] = None,
                           countryLabel: Option[String] = None,
                           submitLabel: Option[String] = None
                         )

case class ResolvedEditPageLabels(editPageLabels: EditPageLabels) {
  val title: String = editPageLabels.title.getOrElse(JourneyConfigDefaults.EDIT_PAGE_TITLE)
  val heading: String = editPageLabels.heading.getOrElse(JourneyConfigDefaults.EDIT_PAGE_HEADING)
  val line1Label: String = editPageLabels.line1Label.getOrElse(JourneyConfigDefaults.EDIT_PAGE_LINE1_LABEL)
  val line2Label: String = editPageLabels.line2Label.getOrElse(JourneyConfigDefaults.EDIT_PAGE_LINE2_LABEL)
  val line3Label: String = editPageLabels.line3Label.getOrElse(JourneyConfigDefaults.EDIT_PAGE_LINE3_LABEL)
  val townLabel: String = editPageLabels.townLabel.getOrElse(JourneyConfigDefaults.EDIT_PAGE_TOWN_LABEL)
  val postcodeLabel: String = editPageLabels.postcodeLabel.getOrElse(JourneyConfigDefaults.EDIT_PAGE_POSTCODE_LABEL)
  val countryLabel: String = editPageLabels.countryLabel.getOrElse(JourneyConfigDefaults.EDIT_PAGE_COUNTRY_LABEL)
  val submitLabel: String = editPageLabels.submitLabel.getOrElse(JourneyConfigDefaults.EDIT_PAGE_SUBMIT_LABEL)
}

object EditPageLabels {
  implicit val format: Format[EditPageLabels] = Json.format[EditPageLabels]
}

case class ConfirmPageLabels(
                              title: Option[String] = None,
                              heading: Option[String] = None,
                              infoSubheading: Option[String] = None,
                              infoMessage: Option[String] = None,
                              submitLabel: Option[String] = None,
                              searchAgainLinkText: Option[String] = None,
                              changeLinkText: Option[String] = None,
                              confirmChangeText: Option[String] = None
                            )

case class ResolvedConfirmPageLabels(confirmPageLabels: ConfirmPageLabels) {
  val title: String = confirmPageLabels.title.getOrElse(JourneyConfigDefaults.CONFIRM_PAGE_TITLE)
  val heading: String = confirmPageLabels.heading.getOrElse(JourneyConfigDefaults.CONFIRM_PAGE_HEADING)
  val infoSubheading: String = confirmPageLabels.infoSubheading.getOrElse(JourneyConfigDefaults.CONFIRM_PAGE_INFO_SUBHEADING)
  val infoMessage: String = confirmPageLabels.infoMessage.getOrElse(JourneyConfigDefaults.CONFIRM_PAGE_INFO_MESSAGE_HTML)
  val submitLabel: String = confirmPageLabels.submitLabel.getOrElse(JourneyConfigDefaults.CONFIRM_PAGE_SUBMIT_LABEL)
  val searchAgainLinkText: String = confirmPageLabels.searchAgainLinkText.getOrElse(JourneyConfigDefaults.SEARCH_AGAIN_LINK_TEXT)
  val changeLinkText: String = confirmPageLabels.changeLinkText.getOrElse(JourneyConfigDefaults.CONFIRM_PAGE_EDIT_LINK_TEXT)
  val confirmChangeText: String = confirmPageLabels.confirmChangeText.getOrElse(JourneyConfigDefaults.CONFIRM_CHANGE_TEXT)
}

object ConfirmPageLabels {
  implicit val format: Format[ConfirmPageLabels] = Json.format[ConfirmPageLabels]
}