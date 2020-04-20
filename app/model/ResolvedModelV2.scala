package model

import config.FrontendAppConfig
import model.JourneyConfigDefaults.{EnglishConstants, JourneyConfigDefaults, WelshConstants}

case class ResolvedJourneyConfigV2(journeyConfig: JourneyConfigV2, isWelsh: Boolean) {
  val version: Int = journeyConfig.version
  val options: ResolvedJourneyOptions = ResolvedJourneyOptions(journeyConfig.options)
  val journeyConfigDefaults : JourneyConfigDefaults = if (isWelsh)  WelshConstants(options.isUkMode) else  EnglishConstants(options.isUkMode)

  val labels: ResolvedLanguageLabels = journeyConfig.labels match {
    case Some(JourneyLabels(_, Some(welshLanguageLabels))) if isWelsh => ResolvedLanguageLabels(welshLanguageLabels, options.phaseFeedbackLink, journeyConfigDefaults)
    case Some(JourneyLabels(Some(englishLanguageLabels), _)) if !isWelsh => ResolvedLanguageLabels(englishLanguageLabels, options.phaseFeedbackLink, journeyConfigDefaults)
    case _ => ResolvedLanguageLabels(LanguageLabels(), options.phaseFeedbackLink, journeyConfigDefaults)
  }
}

case class ResolvedJourneyOptions(journeyOptions: JourneyOptions) {
  val continueUrl: String = journeyOptions.continueUrl
  val homeNavHref: String = journeyOptions.homeNavHref.getOrElse(FrontendAppConfig.homeUrl)
  val accessibilityFooterUrl: Option[String] = journeyOptions.accessibilityFooterUrl
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

case class ResolvedSelectPageConfig(selectPageConfig: SelectPageConfig) {
  val proposalListLimit: Option[Int] = selectPageConfig.proposalListLimit
  val showSearchAgainLink: Boolean = selectPageConfig.showSearchAgainLink.getOrElse(false)
}

case class ResolvedConfirmPageConfig(confirmPageConfig: ConfirmPageConfig) {
  val showChangeLink: Boolean = confirmPageConfig.showChangeLink.getOrElse(true)
  val showSubHeadingAndInfo: Boolean = confirmPageConfig.showSubHeadingAndInfo.getOrElse(false)
  val showSearchAgainLink: Boolean = confirmPageConfig.showSearchAgainLink.getOrElse(false)
  val showConfirmChangeText: Boolean = confirmPageConfig.showConfirmChangeText.getOrElse(false)
}

case class ResolvedLanguageLabels(languageLabels: LanguageLabels, phaseFeedbackLink: String, journeyConfigDefaults: JourneyConfigDefaults) {
  val appLevelLabels: ResolvedAppLevelLabels = ResolvedAppLevelLabels(languageLabels.appLevelLabels.getOrElse(AppLevelLabels()), phaseFeedbackLink)
  val selectPageLabels: ResolvedSelectPageLabels = ResolvedSelectPageLabels(languageLabels.selectPageLabels.getOrElse(SelectPageLabels()))
  val lookupPageLabels: ResolvedLookupPageLabels = ResolvedLookupPageLabels(languageLabels.lookupPageLabels.getOrElse(LookupPageLabels()))
  val editPageLabels: ResolvedEditPageLabels = ResolvedEditPageLabels(languageLabels.editPageLabels.getOrElse(EditPageLabels()))
  val confirmPageLabels: ResolvedConfirmPageLabels = ResolvedConfirmPageLabels(languageLabels.confirmPageLabels.getOrElse(ConfirmPageLabels()))

  case class ResolvedAppLevelLabels(appLevelLabels: AppLevelLabels, phaseFeedbackLink: String) {
    val navTitle: Option[String] = appLevelLabels.navTitle
    val phaseBannerHtml: String = appLevelLabels.phaseBannerHtml.getOrElse(journeyConfigDefaults.defaultPhaseBannerHtml(phaseFeedbackLink))
  }

  case class ResolvedSelectPageLabels(selectPageLabels: SelectPageLabels) {
    val title: String = selectPageLabels.title.getOrElse(journeyConfigDefaults.SELECT_PAGE_TITLE)
    val heading: String = selectPageLabels.heading.getOrElse(journeyConfigDefaults.SELECT_PAGE_HEADING)
    val headingWithPostcode: String = selectPageLabels.headingWithPostcode.getOrElse(journeyConfigDefaults.SELECT_PAGE_HEADING_WITH_POSTCODE)
    val proposalListLabel: String = selectPageLabels.proposalListLabel.getOrElse(journeyConfigDefaults.SELECT_PAGE_PROPOSAL_LIST_LABEL)
    val submitLabel: String = selectPageLabels.submitLabel.getOrElse(journeyConfigDefaults.SELECT_PAGE_SUBMIT_LABEL)
    val searchAgainLinkText: String = selectPageLabels.searchAgainLinkText.getOrElse(journeyConfigDefaults.SEARCH_AGAIN_LINK_TEXT)
    val editAddressLinkText: String = selectPageLabels.editAddressLinkText.getOrElse(journeyConfigDefaults.EDIT_LINK_TEXT)
  }

  case class ResolvedLookupPageLabels(lookupPageLabels: LookupPageLabels) {
    val title: String = lookupPageLabels.title.getOrElse(journeyConfigDefaults.LOOKUP_PAGE_TITLE)
    val heading: String = lookupPageLabels.heading.getOrElse(journeyConfigDefaults.LOOKUP_PAGE_HEADING)
    val filterLabel: String = lookupPageLabels.filterLabel.getOrElse(journeyConfigDefaults.LOOKUP_PAGE_FILTER_LABEL)
    val postcodeLabel: String = lookupPageLabels.postcodeLabel.getOrElse(journeyConfigDefaults.LOOKUP_PAGE_POSTCODE_LABEL)
    val submitLabel: String = lookupPageLabels.submitLabel.getOrElse(journeyConfigDefaults.LOOKUP_PAGE_SUBMIT_LABEL)
    val noResultsFoundMessage: Option[String] = lookupPageLabels.noResultsFoundMessage
    val resultLimitExceededMessage: Option[String] = lookupPageLabels.resultLimitExceededMessage
    val manualAddressLinkText: String = lookupPageLabels.manualAddressLinkText.getOrElse(journeyConfigDefaults.LOOKUP_PAGE_MANUAL_ADDRESS_LINK_TEXT)
  }

  case class ResolvedEditPageLabels(editPageLabels: EditPageLabels) {
    val title: String = editPageLabels.title.getOrElse(journeyConfigDefaults.EDIT_PAGE_TITLE)
    val heading: String = editPageLabels.heading.getOrElse(journeyConfigDefaults.EDIT_PAGE_HEADING)
    val line1Label: String = editPageLabels.line1Label.getOrElse(journeyConfigDefaults.EDIT_PAGE_LINE1_LABEL)
    val line2Label: String = editPageLabels.line2Label.getOrElse(journeyConfigDefaults.EDIT_PAGE_LINE2_LABEL)
    val line3Label: String = editPageLabels.line3Label.getOrElse(journeyConfigDefaults.EDIT_PAGE_LINE3_LABEL)
    val townLabel: String = editPageLabels.townLabel.getOrElse(journeyConfigDefaults.EDIT_PAGE_TOWN_LABEL)
    val postcodeLabel: String = editPageLabels.postcodeLabel.getOrElse(journeyConfigDefaults.EDIT_PAGE_POSTCODE_LABEL)
    val countryLabel: String = editPageLabels.countryLabel.getOrElse(journeyConfigDefaults.EDIT_PAGE_COUNTRY_LABEL)
    val submitLabel: String = editPageLabels.submitLabel.getOrElse(journeyConfigDefaults.EDIT_PAGE_SUBMIT_LABEL)
  }

  case class ResolvedConfirmPageLabels(confirmPageLabels: ConfirmPageLabels) {
    val title: String = confirmPageLabels.title.getOrElse(journeyConfigDefaults.CONFIRM_PAGE_TITLE)
    val heading: String = confirmPageLabels.heading.getOrElse(journeyConfigDefaults.CONFIRM_PAGE_HEADING)
    val infoSubheading: String = confirmPageLabels.infoSubheading.getOrElse(journeyConfigDefaults.CONFIRM_PAGE_INFO_SUBHEADING)
    val infoMessage: String = confirmPageLabels.infoMessage.getOrElse(journeyConfigDefaults.CONFIRM_PAGE_INFO_MESSAGE_HTML)
    val submitLabel: String = confirmPageLabels.submitLabel.getOrElse(journeyConfigDefaults.CONFIRM_PAGE_SUBMIT_LABEL)
    val searchAgainLinkText: String = confirmPageLabels.searchAgainLinkText.getOrElse(journeyConfigDefaults.SEARCH_AGAIN_LINK_TEXT)
    val changeLinkText: String = confirmPageLabels.changeLinkText.getOrElse(journeyConfigDefaults.CONFIRM_PAGE_EDIT_LINK_TEXT)
    val confirmChangeText: String = confirmPageLabels.confirmChangeText.getOrElse(journeyConfigDefaults.CONFIRM_CHANGE_TEXT)
  }

}
