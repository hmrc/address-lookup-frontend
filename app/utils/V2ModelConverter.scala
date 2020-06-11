package utils

import config.FrontendAppConfig
import model._

object V2ModelConverter {
  implicit class V2ModelConverter(journeyData: JourneyData) {
    val toV2Model: JourneyDataV2 = convertToV2Model(journeyData)
  }

  def convertToV2Model(v1: JourneyData): JourneyDataV2 = {
    val journeyConfig = JourneyConfigV2 (
      version = FrontendAppConfig.apiVersion2,
      options = resolveJourneyOptions(v1.config),
      labels = resolveLabels(v1.config.navTitle, v1.config.phaseBannerHtml, v1.config.lookupPage, v1.config.selectPage,
        v1.config.editPage, v1.config.confirmPage),
      requestedVersion = Some(1)
    )

    JourneyDataV2(journeyConfig, v1.proposals, v1.selectedAddress, v1.confirmedAddress)
  }

  private def resolveJourneyOptions(v1: JourneyConfig): JourneyOptions = {
    JourneyOptions(
      continueUrl = v1.continueUrl,
      homeNavHref = v1.homeNavHref,
      signOutHref = v1.signOutHref,
      accessibilityFooterUrl = v1.accessibilityFooterUrl,
      additionalStylesheetUrl =  v1.additionalStylesheetUrl,
      phaseFeedbackLink = v1.phaseFeedbackLink,
      deskProServiceName = v1.deskProServiceName,
      showPhaseBanner = v1.showPhaseBanner,
      alphaPhase = v1.alphaPhase,
      disableTranslations = Some(false),
      showBackButtons = v1.showBackButtons,
      includeHMRCBranding = v1.includeHMRCBranding,
      ukMode = v1.ukMode,
      allowedCountryCodes = v1.allowedCountryCodes,
      selectPageConfig = resolveSelectPageConfig(v1.selectPage),
      confirmPageConfig = resolveConfirmPageConfig(v1.confirmPage),
      timeoutConfig = resolveTimeoutConfig(v1.timeout)
    )
  }

  private def resolveLabels(optNavTitle: Option[String],
                            optPhaseBannerHtml: Option[String],
                            optLookupPage: Option[LookupPage],
                            optSelectPage: Option[SelectPage],
                            optEditPage: Option[EditPage],
                            optConfirmPage: Option[ConfirmPage]): Option[JourneyLabels] = {

    val appLabels = (optNavTitle, optPhaseBannerHtml) match {
      case (None, None) => None
      case _ => Some(AppLevelLabels(optNavTitle, optPhaseBannerHtml))
    }

    val lookupLabels = optLookupPage map (v1 => LookupPageLabels(
      title = v1.title,
      heading = v1.heading,
      filterLabel = v1.filterLabel,
      postcodeLabel = v1.postcodeLabel,
      submitLabel = v1.submitLabel,
      noResultsFoundMessage = v1.noResultsFoundMessage,
      resultLimitExceededMessage = v1.resultLimitExceededMessage,
      manualAddressLinkText = v1.manualAddressLinkText
    ))

    val selectLabels = optSelectPage map (v1 => SelectPageLabels(
      title = v1.title,
      heading = v1.heading,
      headingWithPostcode = v1.headingWithPostcode,
      proposalListLabel = v1.proposalListLabel,
      submitLabel = v1.submitLabel,
      searchAgainLinkText = v1.searchAgainLinkText,
      editAddressLinkText = v1.editAddressLinkText
    ))

    val editLabels = optEditPage map (v1 => EditPageLabels(
      title = v1.title,
      heading = v1.heading,
      line1Label = v1.line1Label,
      line2Label = v1.line2Label,
      line3Label = v1.line3Label,
      townLabel = v1.townLabel,
      postcodeLabel = v1.postcodeLabel,
      countryLabel = v1.countryLabel,
      submitLabel = v1.submitLabel
    ))

    val confirmLabels = optConfirmPage map (v1 => ConfirmPageLabels(
      title = v1.title,
      heading = v1.heading,
      infoSubheading = v1.infoSubheading,
      infoMessage = v1.infoMessage,
      submitLabel = v1.submitLabel,
      searchAgainLinkText = v1.searchAgainLinkText,
      changeLinkText = v1.changeLinkText,
      confirmChangeText = v1.confirmChangeText
    ))

    (appLabels, lookupLabels, selectLabels, editLabels, confirmLabels) match {
      case (None, None, None, None, None) =>
        None
      case _ =>
        Some(JourneyLabels(
          en = Some(LanguageLabels(appLabels, selectLabels, lookupLabels, editLabels, confirmLabels)))
        )
    }
  }

  private def resolveSelectPageConfig(optSelectPage: Option[SelectPage]): Option[SelectPageConfig] =
    optSelectPage map (v1 => SelectPageConfig(v1.proposalListLimit, v1.showSearchAgainLink))

  private def resolveConfirmPageConfig(optConfirmPage: Option[ConfirmPage]): Option[ConfirmPageConfig] =
    optConfirmPage map (
      v1 => ConfirmPageConfig(v1.showSearchAgainLink, v1.showSubHeadingAndInfo, v1.showChangeLink, v1.showConfirmChangeText)
      )

  private def resolveTimeoutConfig(optTimeout: Option[Timeout]): Option[TimeoutConfig] =
    optTimeout map (v1 => TimeoutConfig(v1.timeoutAmount, v1.timeoutUrl))
}
