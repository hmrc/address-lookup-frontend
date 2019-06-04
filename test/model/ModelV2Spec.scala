package model

import org.scalatest.{MustMatchers, WordSpecLike}
import play.api.data.validation.ValidationError
import play.api.libs.json._
import utils.TestConstants._

class ModelV2Spec extends WordSpecLike with MustMatchers {

  "JourneyDataV2" should {
    "read successfully from full json" in {
      Json.fromJson[JourneyDataV2](journeyDataV2FullJson) mustBe JsSuccess(journeyDataV2Full)
    }
    "read successfully from minimal json" in {
      Json.fromJson[JourneyDataV2](journeyDataV2MinimalJson) mustBe JsSuccess(journeyDataV2Minimal)
    }
    "fail to read when the journey config is missing from the json" in {
      Json.fromJson[JourneyDataV2](emptyJson) mustBe JsError(JsPath \ "config", ValidationError("error.path.missing"))
    }

    "write to json from full model" in {
      Json.toJson(journeyDataV2Full) mustBe journeyDataV2FullJson
    }
    "write to json from minimal model" in {
      Json.toJson(journeyDataV2Minimal) mustBe journeyDataV2MinimalJson
    }
  }

  "JourneyConfigV2" should {
    "read successfully with no journey labels" in {
      Json.fromJson[JourneyConfigV2](journeyConfigV2MinimalJson) mustBe JsSuccess(journeyConfigV2Minimal)
    }
    "fail to read from json with version missing" in {
      Json.fromJson[JourneyConfigV2](journeyConfigV2MissingVersionJson) mustBe JsError(JsPath \ "version", ValidationError("error.path.missing"))
    }
    "fail to read from json with journey options missing" in {
      Json.fromJson[JourneyConfigV2](journeyConfigV2MissingConfigJson) mustBe JsError(JsPath \ "options", ValidationError("error.path.missing"))
    }

    "write to json with minimal data" in {
      Json.toJson(journeyConfigV2Minimal) mustBe journeyConfigV2MinimalJson
    }
  }

  "JourneyOptions" should {
    "read successfully with minimal json" in {
      Json.fromJson[JourneyOptions](journeyOptionsMinimalJson) mustBe JsSuccess(journeyOptionsMinimal)
    }
    "fail to read from json with continue url missing" in {
      Json.fromJson[JourneyOptions](emptyJson) mustBe JsError(JsPath \ "continueUrl", ValidationError("error.path.missing"))
    }

    "write to json with minimal data" in {
      Json.toJson(journeyOptionsMinimal) mustBe journeyOptionsMinimalJson
    }
  }

  "SelectPageConfig" should {
    "read successfully from minimal json" in {
      Json.fromJson[SelectPageConfig](emptyJson) mustBe JsSuccess(selectPageConfigMinimal)
    }

    "write to json with minimal data" in {
      Json.toJson(selectPageConfigMinimal) mustBe emptyJson
    }
  }

  "ConfirmPageConfig" should {
    "read successfully from minimal json" in {
      Json.fromJson[ConfirmPageConfig](emptyJson) mustBe JsSuccess(confirmPageConfigMinimal)
    }

    "write to json with minimal data" in {
      Json.toJson(confirmPageConfigMinimal) mustBe emptyJson
    }
  }

  "TimeoutConfig" should {
    "fail to read from json with timeout amount missing" in {
      Json.fromJson[TimeoutConfig](timeoutConfigLessThanMinJson) mustBe JsError(JsPath \ "timeoutAmount", ValidationError("error.min", 120))
    }
    "fail to read from json with timeout url missing" in {
      Json.fromJson[TimeoutConfig](timeoutConfigMissingAmountJson) mustBe JsError(JsPath \ "timeoutAmount", ValidationError("error.path.missing"))
    }
    "fail to read from json when timeout amount is less than 120" in {
      Json.fromJson[TimeoutConfig](timeoutConfigMissingUrlJson) mustBe JsError(JsPath \ "timeoutUrl", ValidationError("error.path.missing"))
    }
  }

  "JourneyLabels" should {
    "read successfully from minimal json" in {
      Json.fromJson[JourneyLabels](emptyJson) mustBe JsSuccess(journeyLabelsMinimal)
    }

    "write to json with minimal data" in {
      Json.toJson(journeyLabelsMinimal) mustBe emptyJson
    }
  }

  "LanguageLabels" should {
    "read successfully from minimal json" in {
      Json.fromJson[LanguageLabels](emptyJson) mustBe JsSuccess(languageLabelsMinimal)
    }

    "write to json with minimal data" in {
      Json.toJson(languageLabelsMinimal) mustBe emptyJson
    }
  }

  "AppLevelLabels" should {
    "read successfully from minimal json" in {
      Json.fromJson[AppLevelLabels](emptyJson) mustBe JsSuccess(appLevelLabelsMinimal)
    }

    "write to json with minimal data" in {
      Json.toJson(appLevelLabelsMinimal) mustBe emptyJson
    }
  }

  "SelectPageLabels" should {
    "read successfully from minimal json" in {
      Json.fromJson[SelectPageLabels](emptyJson) mustBe JsSuccess(selectPageLabelsMinimal)
    }

    "write to json with minimal data" in {
      Json.toJson(selectPageLabelsMinimal) mustBe emptyJson
    }
  }

  "LookupPageLabels" should {
    "read successfully from minimal json" in {
      Json.fromJson[LookupPageLabels](emptyJson) mustBe JsSuccess(lookupPageLabelsMinimal)
    }

    "write to json with minimal data" in {
      Json.toJson(lookupPageLabelsMinimal) mustBe emptyJson
    }
  }

  "EditPageLabels" should {
    "read successfully from minimal json" in {
      Json.fromJson[EditPageLabels](emptyJson) mustBe JsSuccess(editPageLabelsMinimal)
    }

    "write to json with minimal data" in {
      Json.toJson(editPageLabelsMinimal) mustBe emptyJson
    }
  }

  "ConfirmPageLabels" should {
    "read successfully from minimal json" in {
      Json.fromJson[ConfirmPageLabels](emptyJson) mustBe JsSuccess(confirmPageLabelsMinimal)
    }

    "write to json with minimal data" in {
      Json.toJson(confirmPageLabelsMinimal) mustBe emptyJson
    }
  }


  "ResolvedJourneyConfigV2" should {
    "return a full model without defaulting any values" in {
      val originalJourneyConfig: JourneyConfigV2 = journeyDataV2Full.config
      val resolvedJourneyConfig: ResolvedJourneyConfigV2 = ResolvedJourneyConfigV2(originalJourneyConfig)

      originalJourneyConfig.version mustBe resolvedJourneyConfig.version

      originalJourneyConfig.options.continueUrl mustBe resolvedJourneyConfig.options.continueUrl
      originalJourneyConfig.options.homeNavHref must contain(resolvedJourneyConfig.options.homeNavHref)
      originalJourneyConfig.options.additionalStylesheetUrl mustBe resolvedJourneyConfig.options.additionalStylesheetUrl
      originalJourneyConfig.options.phaseFeedbackLink must contain(resolvedJourneyConfig.options.phaseFeedbackLink)
      originalJourneyConfig.options.deskProServiceName mustBe resolvedJourneyConfig.options.deskProServiceName
      originalJourneyConfig.options.showPhaseBanner must contain(resolvedJourneyConfig.options.showPhaseBanner)
      originalJourneyConfig.options.alphaPhase must contain(resolvedJourneyConfig.options.alphaPhase)
      resolvedJourneyConfig.options.phase mustBe "alpha"
      originalJourneyConfig.options.showBackButtons must contain(resolvedJourneyConfig.options.showBackButtons)
      originalJourneyConfig.options.includeHMRCBranding must contain(resolvedJourneyConfig.options.includeHMRCBranding)
      originalJourneyConfig.options.ukMode must contain(resolvedJourneyConfig.options.isUkMode)
      originalJourneyConfig.options.allowedCountryCodes mustBe resolvedJourneyConfig.options.allowedCountryCodes

      originalJourneyConfig.options.selectPageConfig.get.proposalListLimit mustBe resolvedJourneyConfig.options.selectPageConfig.proposalListLimit
      originalJourneyConfig.options.selectPageConfig.get.showSearchAgainLink must contain(resolvedJourneyConfig.options.selectPageConfig.showSearchAgainLink)

      originalJourneyConfig.options.confirmPageConfig.get.showChangeLink must contain(resolvedJourneyConfig.options.confirmPageConfig.showChangeLink)
      originalJourneyConfig.options.confirmPageConfig.get.showSubHeadingAndInfo must contain(resolvedJourneyConfig.options.confirmPageConfig.showSubHeadingAndInfo)
      originalJourneyConfig.options.confirmPageConfig.get.showSearchAgainLink must contain(resolvedJourneyConfig.options.confirmPageConfig.showSearchAgainLink)
      originalJourneyConfig.options.confirmPageConfig.get.showConfirmChangeText must contain(resolvedJourneyConfig.options.confirmPageConfig.showConfirmChangeText)

      originalJourneyConfig.options.timeoutConfig mustBe resolvedJourneyConfig.options.timeoutConfig

      originalJourneyConfig.labels.get.en.get.appLevelLabels.get.navTitle mustBe resolvedJourneyConfig.labels.en.appLevelLabels.navTitle
      originalJourneyConfig.labels.get.en.get.appLevelLabels.get.phaseBannerHtml must contain(resolvedJourneyConfig.labels.en.appLevelLabels.phaseBannerHtml)

      originalJourneyConfig.labels.get.en.get.selectPageLabels.get.title must contain(resolvedJourneyConfig.labels.en.selectPageLabels.title)
      originalJourneyConfig.labels.get.en.get.selectPageLabels.get.heading must contain(resolvedJourneyConfig.labels.en.selectPageLabels.heading)
      originalJourneyConfig.labels.get.en.get.selectPageLabels.get.headingWithPostcode must contain(resolvedJourneyConfig.labels.en.selectPageLabels.headingWithPostcode)
      originalJourneyConfig.labels.get.en.get.selectPageLabels.get.proposalListLabel must contain(resolvedJourneyConfig.labels.en.selectPageLabels.proposalListLabel)
      originalJourneyConfig.labels.get.en.get.selectPageLabels.get.submitLabel must contain(resolvedJourneyConfig.labels.en.selectPageLabels.submitLabel)
      originalJourneyConfig.labels.get.en.get.selectPageLabels.get.searchAgainLinkText must contain(resolvedJourneyConfig.labels.en.selectPageLabels.searchAgainLinkText)
      originalJourneyConfig.labels.get.en.get.selectPageLabels.get.editAddressLinkText must contain(resolvedJourneyConfig.labels.en.selectPageLabels.editAddressLinkText)

      originalJourneyConfig.labels.get.en.get.lookupPageLabels.get.title must contain(resolvedJourneyConfig.labels.en.lookupPageLabels.title)
      originalJourneyConfig.labels.get.en.get.lookupPageLabels.get.heading must contain(resolvedJourneyConfig.labels.en.lookupPageLabels.heading)
      originalJourneyConfig.labels.get.en.get.lookupPageLabels.get.filterLabel must contain(resolvedJourneyConfig.labels.en.lookupPageLabels.filterLabel)
      originalJourneyConfig.labels.get.en.get.lookupPageLabels.get.postcodeLabel must contain(resolvedJourneyConfig.labels.en.lookupPageLabels.postcodeLabel)
      originalJourneyConfig.labels.get.en.get.lookupPageLabels.get.submitLabel must contain(resolvedJourneyConfig.labels.en.lookupPageLabels.submitLabel)
      originalJourneyConfig.labels.get.en.get.lookupPageLabels.get.noResultsFoundMessage mustBe resolvedJourneyConfig.labels.en.lookupPageLabels.noResultsFoundMessage
      originalJourneyConfig.labels.get.en.get.lookupPageLabels.get.resultLimitExceededMessage mustBe resolvedJourneyConfig.labels.en.lookupPageLabels.resultLimitExceededMessage
      originalJourneyConfig.labels.get.en.get.lookupPageLabels.get.manualAddressLinkText must contain(resolvedJourneyConfig.labels.en.lookupPageLabels.manualAddressLinkText)

      originalJourneyConfig.labels.get.en.get.editPageLabels.get.title must contain(resolvedJourneyConfig.labels.en.editPageLabels.title)
      originalJourneyConfig.labels.get.en.get.editPageLabels.get.heading must contain(resolvedJourneyConfig.labels.en.editPageLabels.heading)
      originalJourneyConfig.labels.get.en.get.editPageLabels.get.line1Label must contain(resolvedJourneyConfig.labels.en.editPageLabels.line1Label)
      originalJourneyConfig.labels.get.en.get.editPageLabels.get.line2Label must contain(resolvedJourneyConfig.labels.en.editPageLabels.line2Label)
      originalJourneyConfig.labels.get.en.get.editPageLabels.get.line3Label must contain(resolvedJourneyConfig.labels.en.editPageLabels.line3Label)
      originalJourneyConfig.labels.get.en.get.editPageLabels.get.townLabel must contain(resolvedJourneyConfig.labels.en.editPageLabels.townLabel)
      originalJourneyConfig.labels.get.en.get.editPageLabels.get.postcodeLabel must contain(resolvedJourneyConfig.labels.en.editPageLabels.postcodeLabel)
      originalJourneyConfig.labels.get.en.get.editPageLabels.get.countryLabel must contain(resolvedJourneyConfig.labels.en.editPageLabels.countryLabel)
      originalJourneyConfig.labels.get.en.get.editPageLabels.get.submitLabel must contain(resolvedJourneyConfig.labels.en.editPageLabels.submitLabel)

      originalJourneyConfig.labels.get.en.get.confirmPageLabels.get.title must contain(resolvedJourneyConfig.labels.en.confirmPageLabels.title)
      originalJourneyConfig.labels.get.en.get.confirmPageLabels.get.heading must contain(resolvedJourneyConfig.labels.en.confirmPageLabels.heading)
      originalJourneyConfig.labels.get.en.get.confirmPageLabels.get.infoSubheading must contain(resolvedJourneyConfig.labels.en.confirmPageLabels.infoSubheading)
      originalJourneyConfig.labels.get.en.get.confirmPageLabels.get.infoMessage must contain(resolvedJourneyConfig.labels.en.confirmPageLabels.infoMessage)
      originalJourneyConfig.labels.get.en.get.confirmPageLabels.get.submitLabel must contain(resolvedJourneyConfig.labels.en.confirmPageLabels.submitLabel)
      originalJourneyConfig.labels.get.en.get.confirmPageLabels.get.searchAgainLinkText must contain(resolvedJourneyConfig.labels.en.confirmPageLabels.searchAgainLinkText)
      originalJourneyConfig.labels.get.en.get.confirmPageLabels.get.changeLinkText must contain(resolvedJourneyConfig.labels.en.confirmPageLabels.changeLinkText)
      originalJourneyConfig.labels.get.en.get.confirmPageLabels.get.confirmChangeText must contain(resolvedJourneyConfig.labels.en.confirmPageLabels.confirmChangeText)

      resolvedJourneyConfig.labels.cy mustBe None

    }

    "return a full model with all possible default values" in {
      val originalJourneyConfig: JourneyConfigV2 = journeyDataV2Minimal.config
      val resolvedJourneyConfig: ResolvedJourneyConfigV2 = ResolvedJourneyConfigV2(originalJourneyConfig)

      resolvedJourneyConfig.version mustBe originalJourneyConfig.version

      resolvedJourneyConfig.options.continueUrl mustBe originalJourneyConfig.options.continueUrl
      resolvedJourneyConfig.options.homeNavHref mustBe "http://www.hmrc.gov.uk"
      resolvedJourneyConfig.options.additionalStylesheetUrl mustBe originalJourneyConfig.options.additionalStylesheetUrl
      resolvedJourneyConfig.options.phaseFeedbackLink mustBe "https://www.tax.service.gov.uk/contact/beta-feedback-unauthenticated?service=ALF"
      resolvedJourneyConfig.options.deskProServiceName mustBe Some("AddressLookupFrontend")
      resolvedJourneyConfig.options.showPhaseBanner mustBe false
      resolvedJourneyConfig.options.alphaPhase mustBe false
      resolvedJourneyConfig.options.phase mustBe ""
      resolvedJourneyConfig.options.showBackButtons mustBe true
      resolvedJourneyConfig.options.includeHMRCBranding mustBe true
      resolvedJourneyConfig.options.isUkMode mustBe false
      resolvedJourneyConfig.options.allowedCountryCodes mustBe None

      resolvedJourneyConfig.options.selectPageConfig.proposalListLimit mustBe None
      resolvedJourneyConfig.options.selectPageConfig.showSearchAgainLink mustBe false

      resolvedJourneyConfig.options.confirmPageConfig.showChangeLink mustBe true
      resolvedJourneyConfig.options.confirmPageConfig.showSubHeadingAndInfo mustBe false
      resolvedJourneyConfig.options.confirmPageConfig.showSearchAgainLink mustBe false
      resolvedJourneyConfig.options.confirmPageConfig.showConfirmChangeText mustBe false

      resolvedJourneyConfig.options.timeoutConfig mustBe None

      resolvedJourneyConfig.labels.en.appLevelLabels.navTitle mustBe None
      resolvedJourneyConfig.labels.en.appLevelLabels.phaseBannerHtml mustBe JourneyConfigDefaults.defaultPhaseBannerHtml("https://www.tax.service.gov.uk/contact/beta-feedback-unauthenticated?service=ALF")

      resolvedJourneyConfig.labels.en.selectPageLabels.title mustBe JourneyConfigDefaults.SELECT_PAGE_TITLE
      resolvedJourneyConfig.labels.en.selectPageLabels.heading mustBe JourneyConfigDefaults.SELECT_PAGE_HEADING
      resolvedJourneyConfig.labels.en.selectPageLabels.headingWithPostcode mustBe JourneyConfigDefaults.SELECT_PAGE_HEADING_WITH_POSTCODE
      resolvedJourneyConfig.labels.en.selectPageLabels.proposalListLabel mustBe JourneyConfigDefaults.SELECT_PAGE_PROPOSAL_LIST_LABEL
      resolvedJourneyConfig.labels.en.selectPageLabels.submitLabel mustBe JourneyConfigDefaults.SELECT_PAGE_SUBMIT_LABEL
      resolvedJourneyConfig.labels.en.selectPageLabels.searchAgainLinkText mustBe JourneyConfigDefaults.SEARCH_AGAIN_LINK_TEXT
      resolvedJourneyConfig.labels.en.selectPageLabels.editAddressLinkText mustBe JourneyConfigDefaults.EDIT_LINK_TEXT

      resolvedJourneyConfig.labels.en.lookupPageLabels.title mustBe JourneyConfigDefaults.LOOKUP_PAGE_TITLE
      resolvedJourneyConfig.labels.en.lookupPageLabels.heading mustBe JourneyConfigDefaults.LOOKUP_PAGE_HEADING
      resolvedJourneyConfig.labels.en.lookupPageLabels.filterLabel mustBe JourneyConfigDefaults.LOOKUP_PAGE_FILTER_LABEL
      resolvedJourneyConfig.labels.en.lookupPageLabels.postcodeLabel mustBe JourneyConfigDefaults.LOOKUP_PAGE_POSTCODE_LABEL
      resolvedJourneyConfig.labels.en.lookupPageLabels.submitLabel mustBe JourneyConfigDefaults.LOOKUP_PAGE_SUBMIT_LABEL
      resolvedJourneyConfig.labels.en.lookupPageLabels.noResultsFoundMessage mustBe None
      resolvedJourneyConfig.labels.en.lookupPageLabels.resultLimitExceededMessage mustBe None
      resolvedJourneyConfig.labels.en.lookupPageLabels.manualAddressLinkText mustBe JourneyConfigDefaults.LOOKUP_PAGE_MANUAL_ADDRESS_LINK_TEXT

      resolvedJourneyConfig.labels.en.editPageLabels.title mustBe JourneyConfigDefaults.EDIT_PAGE_TITLE
      resolvedJourneyConfig.labels.en.editPageLabels.heading mustBe JourneyConfigDefaults.EDIT_PAGE_HEADING
      resolvedJourneyConfig.labels.en.editPageLabels.line1Label mustBe JourneyConfigDefaults.EDIT_PAGE_LINE1_LABEL
      resolvedJourneyConfig.labels.en.editPageLabels.line2Label mustBe JourneyConfigDefaults.EDIT_PAGE_LINE2_LABEL
      resolvedJourneyConfig.labels.en.editPageLabels.line3Label mustBe JourneyConfigDefaults.EDIT_PAGE_LINE3_LABEL
      resolvedJourneyConfig.labels.en.editPageLabels.townLabel mustBe JourneyConfigDefaults.EDIT_PAGE_TOWN_LABEL
      resolvedJourneyConfig.labels.en.editPageLabels.postcodeLabel mustBe JourneyConfigDefaults.EDIT_PAGE_POSTCODE_LABEL
      resolvedJourneyConfig.labels.en.editPageLabels.countryLabel mustBe JourneyConfigDefaults.EDIT_PAGE_COUNTRY_LABEL
      resolvedJourneyConfig.labels.en.editPageLabels.submitLabel mustBe JourneyConfigDefaults.EDIT_PAGE_SUBMIT_LABEL

      resolvedJourneyConfig.labels.en.confirmPageLabels.title mustBe JourneyConfigDefaults.CONFIRM_PAGE_TITLE
      resolvedJourneyConfig.labels.en.confirmPageLabels.heading mustBe JourneyConfigDefaults.CONFIRM_PAGE_HEADING
      resolvedJourneyConfig.labels.en.confirmPageLabels.infoSubheading mustBe JourneyConfigDefaults.CONFIRM_PAGE_INFO_SUBHEADING
      resolvedJourneyConfig.labels.en.confirmPageLabels.infoMessage mustBe JourneyConfigDefaults.CONFIRM_PAGE_INFO_MESSAGE_HTML
      resolvedJourneyConfig.labels.en.confirmPageLabels.submitLabel mustBe JourneyConfigDefaults.CONFIRM_PAGE_SUBMIT_LABEL
      resolvedJourneyConfig.labels.en.confirmPageLabels.searchAgainLinkText mustBe JourneyConfigDefaults.SEARCH_AGAIN_LINK_TEXT
      resolvedJourneyConfig.labels.en.confirmPageLabels.changeLinkText mustBe JourneyConfigDefaults.CONFIRM_PAGE_EDIT_LINK_TEXT
      resolvedJourneyConfig.labels.en.confirmPageLabels.confirmChangeText mustBe JourneyConfigDefaults.CONFIRM_CHANGE_TEXT

      resolvedJourneyConfig.labels.cy mustBe None

    }
  }
  "ResolvedJourneyOptions" should {
    //TODO: isUKMode, provided and false
    "set the isUkMode to true" in {
      ResolvedJourneyOptions(journeyDataV2Full.config.options.copy(ukMode = Some(true))).isUkMode mustBe true
    }
    "set the isUkMode to false" when {
      "ukMode is missing" in {
        ResolvedJourneyOptions(journeyDataV2Full.config.options.copy(ukMode = None)).isUkMode mustBe false
      }
      "ukMode is set to false" in {
        ResolvedJourneyOptions(journeyDataV2Full.config.options.copy(ukMode = Some(false))).isUkMode mustBe false
      }
    }

    "set the phase value to alpha" in {
      ResolvedJourneyOptions(journeyDataV2Full.config.options.copy(alphaPhase = Some(true),showPhaseBanner = Some(true))).phase mustBe "alpha"
    }
    "set the phase value to beta" in {
      ResolvedJourneyOptions(journeyDataV2Full.config.options.copy(alphaPhase = Some(false),showPhaseBanner = Some(true))).phase mustBe "beta"
    }
    "set the phase value to empty string" in {
      ResolvedJourneyOptions(journeyDataV2Full.config.options.copy(alphaPhase = Some(false),showPhaseBanner = Some(false))).phase mustBe ""
    }
  }
}
