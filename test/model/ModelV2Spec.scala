/*
 * Copyright 2021 HM Revenue & Customs
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

import com.codahale.metrics.SharedMetricRegistries
import config.FrontendAppConfig
import org.scalatest.{MustMatchers, WordSpecLike}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json._
import utils.TestConstants._

class ModelV2Spec extends WordSpecLike with MustMatchers with GuiceOneAppPerSuite {
  override implicit lazy val app: Application = {
    SharedMetricRegistries.clear()
    new GuiceApplicationBuilder().build()
  }

  val appConfig = app.injector.instanceOf[FrontendAppConfig]

  "JourneyDataV2" should {
    "read successfully from full json" in {
      Json.fromJson[JourneyDataV2](journeyDataV2FullJson) mustBe JsSuccess(journeyDataV2Full)
    }
    "read successfully from minimal json" in {
      Json.fromJson[JourneyDataV2](journeyDataV2MinimalJson) mustBe JsSuccess(journeyDataV2MinimalExpected)
    }
    "fail to read when the journey config is missing from the json" in {
      Json.fromJson[JourneyDataV2](emptyJson) mustBe JsError(JsPath \ "config", JsonValidationError("error.path.missing"))
    }

    "write to json from full model" in {
      Json.toJson(journeyDataV2Full) mustBe journeyDataV2FullJson
    }
    "write to json from minimal model" in {
      Json.toJson(journeyDataV2MinimalExpected) mustBe journeyDataV2MinimalJson
    }
  }

  "JourneyConfigV2" should {
    "read successfully with no journey labels" in {
      Json.fromJson[JourneyConfigV2](journeyConfigV2MinimalJson) mustBe JsSuccess(journeyConfigV2Minimal)
    }
    "fail to read from json with version missing" in {
      Json.fromJson[JourneyConfigV2](journeyConfigV2MissingVersionJson) mustBe JsError(JsPath \ "version", JsonValidationError("error.path.missing"))
    }
    "fail to read from json with journey options missing" in {
      Json.fromJson[JourneyConfigV2](journeyConfigV2MissingConfigJson) mustBe JsError(JsPath \ "options", JsonValidationError("error.path.missing"))
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
      Json.fromJson[JourneyOptions](emptyJson) mustBe JsError(JsPath \ "continueUrl", JsonValidationError("error.path.missing"))
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
      Json.fromJson[TimeoutConfig](timeoutConfigLessThanMinJson) mustBe JsError(JsPath \ "timeoutAmount", JsonValidationError("error.min", 120))
    }
    "fail to read from json with timeout url missing" in {
      Json.fromJson[TimeoutConfig](timeoutConfigMissingAmountJson) mustBe JsError(JsPath \ "timeoutAmount", JsonValidationError("error.path.missing"))
    }
    "fail to read from json when timeout amount is less than 120" in {
      Json.fromJson[TimeoutConfig](timeoutConfigMissingUrlJson) mustBe JsError(JsPath \ "timeoutUrl", JsonValidationError("error.path.missing"))
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
    import JourneyLabels.{languageLabelsReads, languageLabelsWrites}

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
      val resolvedJourneyConfig: ResolvedJourneyConfigV2 = ResolvedJourneyConfigV2(originalJourneyConfig, appConfig)

      originalJourneyConfig.version mustBe resolvedJourneyConfig.version

      originalJourneyConfig.options.continueUrl mustBe resolvedJourneyConfig.options.continueUrl
      originalJourneyConfig.options.homeNavHref mustBe resolvedJourneyConfig.options.homeNavHref
      originalJourneyConfig.options.accessibilityFooterUrl mustBe resolvedJourneyConfig.options.accessibilityFooterUrl
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

      // TODO: Add tests for message resolution
      //      originalJourneyConfig.labels.get.cy.get.appLevelLabels.get.navTitle mustBe resolvedJourneyConfig.labels.appLevelLabels.navTitle
      //      originalJourneyConfig.labels.get.cy.get.appLevelLabels.get.phaseBannerHtml must contain(resolvedJourneyConfig.labels.appLevelLabels.phaseBannerHtml)
      //
      //      originalJourneyConfig.labels.get.cy.get.selectPageLabels.get.title must contain(resolvedJourneyConfig.labels.selectPageLabels.title)
      //      originalJourneyConfig.labels.get.cy.get.selectPageLabels.get.heading must contain(resolvedJourneyConfig.labels.selectPageLabels.heading)
      //      originalJourneyConfig.labels.get.cy.get.selectPageLabels.get.headingWithPostcode must contain(resolvedJourneyConfig.labels.selectPageLabels.headingWithPostcode)
      //      originalJourneyConfig.labels.get.cy.get.selectPageLabels.get.proposalListLabel must contain(resolvedJourneyConfig.labels.selectPageLabels.proposalListLabel)
      //      originalJourneyConfig.labels.get.cy.get.selectPageLabels.get.submitLabel must contain(resolvedJourneyConfig.labels.selectPageLabels.submitLabel)
      //      originalJourneyConfig.labels.get.cy.get.selectPageLabels.get.searchAgainLinkText must contain(resolvedJourneyConfig.labels.selectPageLabels.searchAgainLinkText)
      //      originalJourneyConfig.labels.get.cy.get.selectPageLabels.get.editAddressLinkText must contain(resolvedJourneyConfig.labels.selectPageLabels.editAddressLinkText)
      //
      //      originalJourneyConfig.labels.get.cy.get.lookupPageLabels.get.title must contain(resolvedJourneyConfig.labels.lookupPageLabels.title)
      //      originalJourneyConfig.labels.get.cy.get.lookupPageLabels.get.heading must contain(resolvedJourneyConfig.labels.lookupPageLabels.heading)
      //      originalJourneyConfig.labels.get.cy.get.lookupPageLabels.get.filterLabel must contain(resolvedJourneyConfig.labels.lookupPageLabels.filterLabel)
      //      originalJourneyConfig.labels.get.cy.get.lookupPageLabels.get.postcodeLabel must contain(resolvedJourneyConfig.labels.lookupPageLabels.postcodeLabel)
      //      originalJourneyConfig.labels.get.cy.get.lookupPageLabels.get.submitLabel must contain(resolvedJourneyConfig.labels.lookupPageLabels.submitLabel)
      //      originalJourneyConfig.labels.get.cy.get.lookupPageLabels.get.noResultsFoundMessage mustBe resolvedJourneyConfig.labels.lookupPageLabels.noResultsFoundMessage
      //      originalJourneyConfig.labels.get.cy.get.lookupPageLabels.get.resultLimitExceededMessage mustBe resolvedJourneyConfig.labels.lookupPageLabels.resultLimitExceededMessage
      //      originalJourneyConfig.labels.get.cy.get.lookupPageLabels.get.manualAddressLinkText must contain(resolvedJourneyConfig.labels.lookupPageLabels.manualAddressLinkText)
      //
      //      originalJourneyConfig.labels.get.cy.get.editPageLabels.get.title must contain(resolvedJourneyConfig.labels.editPageLabels.title)
      //      originalJourneyConfig.labels.get.cy.get.editPageLabels.get.heading must contain(resolvedJourneyConfig.labels.editPageLabels.heading)
      //      originalJourneyConfig.labels.get.cy.get.editPageLabels.get.line1Label must contain(resolvedJourneyConfig.labels.editPageLabels.line1Label)
      //      originalJourneyConfig.labels.get.cy.get.editPageLabels.get.line2Label must contain(resolvedJourneyConfig.labels.editPageLabels.line2Label)
      //      originalJourneyConfig.labels.get.cy.get.editPageLabels.get.line3Label must contain(resolvedJourneyConfig.labels.editPageLabels.line3Label)
      //      originalJourneyConfig.labels.get.cy.get.editPageLabels.get.townLabel must contain(resolvedJourneyConfig.labels.editPageLabels.townLabel)
      //      originalJourneyConfig.labels.get.cy.get.editPageLabels.get.postcodeLabel must contain(resolvedJourneyConfig.labels.editPageLabels.postcodeLabel)
      //      originalJourneyConfig.labels.get.cy.get.editPageLabels.get.countryLabel must contain(resolvedJourneyConfig.labels.editPageLabels.countryLabel)
      //      originalJourneyConfig.labels.get.cy.get.editPageLabels.get.submitLabel must contain(resolvedJourneyConfig.labels.editPageLabels.submitLabel)
      //
      //      originalJourneyConfig.labels.get.cy.get.confirmPageLabels.get.title must contain(resolvedJourneyConfig.labels.confirmPageLabels.title)
      //      originalJourneyConfig.labels.get.cy.get.confirmPageLabels.get.heading must contain(resolvedJourneyConfig.labels.confirmPageLabels.heading)
      //      originalJourneyConfig.labels.get.cy.get.confirmPageLabels.get.infoSubheading must contain(resolvedJourneyConfig.labels.confirmPageLabels.infoSubheading)
      //      originalJourneyConfig.labels.get.cy.get.confirmPageLabels.get.infoMessage must contain(resolvedJourneyConfig.labels.confirmPageLabels.infoMessage)
      //      originalJourneyConfig.labels.get.cy.get.confirmPageLabels.get.submitLabel must contain(resolvedJourneyConfig.labels.confirmPageLabels.submitLabel)
      //      originalJourneyConfig.labels.get.cy.get.confirmPageLabels.get.searchAgainLinkText must contain(resolvedJourneyConfig.labels.confirmPageLabels.searchAgainLinkText)
      //      originalJourneyConfig.labels.get.cy.get.confirmPageLabels.get.changeLinkText must contain(resolvedJourneyConfig.labels.confirmPageLabels.changeLinkText)
      //      originalJourneyConfig.labels.get.cy.get.confirmPageLabels.get.confirmChangeText must contain(resolvedJourneyConfig.labels.confirmPageLabels.confirmChangeText)
    }

    "return a full model with all possible English default values" in {
      val originalJourneyConfig: JourneyConfigV2 = journeyDataV2Minimal.config
      val resolvedJourneyConfig: ResolvedJourneyConfigV2 = ResolvedJourneyConfigV2(originalJourneyConfig, appConfig)

      resolvedJourneyConfig.version mustBe originalJourneyConfig.version

      resolvedJourneyConfig.options.continueUrl mustBe originalJourneyConfig.options.continueUrl
      resolvedJourneyConfig.options.homeNavHref mustBe originalJourneyConfig.options.homeNavHref
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

      //      resolvedJourneyConfig.labels.appLevelLabels.navTitle mustBe None
      //      resolvedJourneyConfig.labels.appLevelLabels.phaseBannerHtml mustBe EnglishConstantsNonUkMode.defaultPhaseBannerHtml("https://www.tax.service.gov.uk/contact/beta-feedback-unauthenticated?service=ALF")
      //
      //      resolvedJourneyConfig.labels.selectPageLabels.title mustBe EnglishConstantsNonUkMode.SELECT_PAGE_TITLE
      //      resolvedJourneyConfig.labels.selectPageLabels.heading mustBe EnglishConstantsNonUkMode.SELECT_PAGE_HEADING
      //      resolvedJourneyConfig.labels.selectPageLabels.headingWithPostcode mustBe EnglishConstantsNonUkMode.SELECT_PAGE_HEADING_WITH_POSTCODE
      //      resolvedJourneyConfig.labels.selectPageLabels.proposalListLabel mustBe EnglishConstantsNonUkMode.SELECT_PAGE_PROPOSAL_LIST_LABEL
      //      resolvedJourneyConfig.labels.selectPageLabels.submitLabel mustBe EnglishConstantsNonUkMode.SELECT_PAGE_SUBMIT_LABEL
      //      resolvedJourneyConfig.labels.selectPageLabels.searchAgainLinkText mustBe EnglishConstantsNonUkMode.SEARCH_AGAIN_LINK_TEXT
      //      resolvedJourneyConfig.labels.selectPageLabels.editAddressLinkText mustBe EnglishConstantsNonUkMode.SELECT_PAGE_EDIT_ADDRESS_LINK_TEXT
      //
      //      resolvedJourneyConfig.labels.lookupPageLabels.title mustBe EnglishConstantsNonUkMode.LOOKUP_PAGE_TITLE
      //      resolvedJourneyConfig.labels.lookupPageLabels.heading mustBe EnglishConstantsNonUkMode.LOOKUP_PAGE_HEADING
      //      resolvedJourneyConfig.labels.lookupPageLabels.filterLabel mustBe EnglishConstantsNonUkMode.LOOKUP_PAGE_FILTER_LABEL
      //      resolvedJourneyConfig.labels.lookupPageLabels.postcodeLabel mustBe EnglishConstantsNonUkMode.LOOKUP_PAGE_POSTCODE_LABEL
      //      resolvedJourneyConfig.labels.lookupPageLabels.submitLabel mustBe EnglishConstantsNonUkMode.LOOKUP_PAGE_SUBMIT_LABEL
      //      resolvedJourneyConfig.labels.lookupPageLabels.noResultsFoundMessage mustBe None
      //      resolvedJourneyConfig.labels.lookupPageLabels.resultLimitExceededMessage mustBe None
      //      resolvedJourneyConfig.labels.lookupPageLabels.manualAddressLinkText mustBe EnglishConstantsNonUkMode.LOOKUP_PAGE_MANUAL_ADDRESS_LINK_TEXT
      //
      //      resolvedJourneyConfig.labels.editPageLabels.title mustBe EnglishConstantsNonUkMode.EDIT_PAGE_TITLE
      //      resolvedJourneyConfig.labels.editPageLabels.heading mustBe EnglishConstantsNonUkMode.EDIT_PAGE_HEADING
      //      resolvedJourneyConfig.labels.editPageLabels.line1Label mustBe EnglishConstantsNonUkMode.EDIT_PAGE_LINE1_LABEL
      //      resolvedJourneyConfig.labels.editPageLabels.line2Label mustBe EnglishConstantsNonUkMode.EDIT_PAGE_LINE2_LABEL
      //      resolvedJourneyConfig.labels.editPageLabels.line3Label mustBe EnglishConstantsNonUkMode.EDIT_PAGE_LINE3_LABEL
      //      resolvedJourneyConfig.labels.editPageLabels.townLabel mustBe EnglishConstantsNonUkMode.EDIT_PAGE_TOWN_LABEL
      //      resolvedJourneyConfig.labels.editPageLabels.postcodeLabel mustBe EnglishConstantsNonUkMode.EDIT_PAGE_POSTCODE_LABEL
      //      resolvedJourneyConfig.labels.editPageLabels.countryLabel mustBe EnglishConstantsNonUkMode.EDIT_PAGE_COUNTRY_LABEL
      //      resolvedJourneyConfig.labels.editPageLabels.submitLabel mustBe EnglishConstantsNonUkMode.EDIT_PAGE_SUBMIT_LABEL
      //
      //      resolvedJourneyConfig.labels.confirmPageLabels.title mustBe EnglishConstantsNonUkMode.CONFIRM_PAGE_TITLE
      //      resolvedJourneyConfig.labels.confirmPageLabels.heading mustBe EnglishConstantsNonUkMode.CONFIRM_PAGE_HEADING
      //      resolvedJourneyConfig.labels.confirmPageLabels.infoSubheading mustBe EnglishConstantsNonUkMode.CONFIRM_PAGE_INFO_SUBHEADING
      //      resolvedJourneyConfig.labels.confirmPageLabels.infoMessage mustBe EnglishConstantsNonUkMode.CONFIRM_PAGE_INFO_MESSAGE_HTML
      //      resolvedJourneyConfig.labels.confirmPageLabels.submitLabel mustBe EnglishConstantsNonUkMode.CONFIRM_PAGE_SUBMIT_LABEL
      //      resolvedJourneyConfig.labels.confirmPageLabels.searchAgainLinkText mustBe EnglishConstantsNonUkMode.SEARCH_AGAIN_LINK_TEXT
      //      resolvedJourneyConfig.labels.confirmPageLabels.changeLinkText mustBe EnglishConstantsNonUkMode.CONFIRM_PAGE_EDIT_LINK_TEXT
      //      resolvedJourneyConfig.labels.confirmPageLabels.confirmChangeText mustBe EnglishConstantsNonUkMode.CONFIRM_PAGE_CONFIRM_CHANGE_TEXT
    }

    "return a full model with all possible default values including English and Welsh content" in {
      val originalJourneyConfig: JourneyConfigV2 = journeyDataV2EnglishAndWelshMinimal.config
      val resolvedJourneyConfig: ResolvedJourneyConfigV2 = ResolvedJourneyConfigV2(originalJourneyConfig, appConfig)

      resolvedJourneyConfig.version mustBe originalJourneyConfig.version

      resolvedJourneyConfig.options.continueUrl mustBe originalJourneyConfig.options.continueUrl
      resolvedJourneyConfig.options.homeNavHref mustBe originalJourneyConfig.options.homeNavHref
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

      //      resolvedJourneyConfig.labels.appLevelLabels.navTitle mustBe None
      //      resolvedJourneyConfig.labels.appLevelLabels.phaseBannerHtml mustBe WelshConstantsNonUkMode.defaultPhaseBannerHtml("https://www.tax.service.gov.uk/contact/beta-feedback-unauthenticated?service=ALF")
      //
      //      resolvedJourneyConfig.labels.selectPageLabels.title mustBe WelshConstantsNonUkMode.SELECT_PAGE_TITLE
      //      resolvedJourneyConfig.labels.selectPageLabels.heading mustBe WelshConstantsNonUkMode.SELECT_PAGE_HEADING
      //      resolvedJourneyConfig.labels.selectPageLabels.headingWithPostcode mustBe WelshConstantsNonUkMode.SELECT_PAGE_HEADING_WITH_POSTCODE
      //      resolvedJourneyConfig.labels.selectPageLabels.proposalListLabel mustBe WelshConstantsNonUkMode.SELECT_PAGE_PROPOSAL_LIST_LABEL
      //      resolvedJourneyConfig.labels.selectPageLabels.submitLabel mustBe WelshConstantsNonUkMode.SELECT_PAGE_SUBMIT_LABEL
      //      resolvedJourneyConfig.labels.selectPageLabels.searchAgainLinkText mustBe WelshConstantsNonUkMode.SEARCH_AGAIN_LINK_TEXT
      //      resolvedJourneyConfig.labels.selectPageLabels.editAddressLinkText mustBe WelshConstantsNonUkMode.SELECT_PAGE_EDIT_ADDRESS_LINK_TEXT
      //
      //      resolvedJourneyConfig.labels.lookupPageLabels.title mustBe WelshConstantsNonUkMode.LOOKUP_PAGE_TITLE
      //      resolvedJourneyConfig.labels.lookupPageLabels.heading mustBe WelshConstantsNonUkMode.LOOKUP_PAGE_HEADING
      //      resolvedJourneyConfig.labels.lookupPageLabels.filterLabel mustBe WelshConstantsNonUkMode.LOOKUP_PAGE_FILTER_LABEL
      //      resolvedJourneyConfig.labels.lookupPageLabels.postcodeLabel mustBe WelshConstantsNonUkMode.LOOKUP_PAGE_POSTCODE_LABEL
      //      resolvedJourneyConfig.labels.lookupPageLabels.submitLabel mustBe WelshConstantsNonUkMode.LOOKUP_PAGE_SUBMIT_LABEL
      //      resolvedJourneyConfig.labels.lookupPageLabels.noResultsFoundMessage mustBe None
      //      resolvedJourneyConfig.labels.lookupPageLabels.resultLimitExceededMessage mustBe None
      //      resolvedJourneyConfig.labels.lookupPageLabels.manualAddressLinkText mustBe WelshConstantsNonUkMode.LOOKUP_PAGE_MANUAL_ADDRESS_LINK_TEXT
      //
      //      resolvedJourneyConfig.labels.editPageLabels.title mustBe WelshConstantsNonUkMode.EDIT_PAGE_TITLE
      //      resolvedJourneyConfig.labels.editPageLabels.heading mustBe WelshConstantsNonUkMode.EDIT_PAGE_HEADING
      //      resolvedJourneyConfig.labels.editPageLabels.line1Label mustBe WelshConstantsNonUkMode.EDIT_PAGE_LINE1_LABEL
      //      resolvedJourneyConfig.labels.editPageLabels.line2Label mustBe WelshConstantsNonUkMode.EDIT_PAGE_LINE2_LABEL
      //      resolvedJourneyConfig.labels.editPageLabels.line3Label mustBe WelshConstantsNonUkMode.EDIT_PAGE_LINE3_LABEL
      //      resolvedJourneyConfig.labels.editPageLabels.townLabel mustBe WelshConstantsNonUkMode.EDIT_PAGE_TOWN_LABEL
      //      resolvedJourneyConfig.labels.editPageLabels.postcodeLabel mustBe WelshConstantsNonUkMode.EDIT_PAGE_POSTCODE_LABEL
      //      resolvedJourneyConfig.labels.editPageLabels.countryLabel mustBe WelshConstantsNonUkMode.EDIT_PAGE_COUNTRY_LABEL
      //      resolvedJourneyConfig.labels.editPageLabels.submitLabel mustBe WelshConstantsNonUkMode.EDIT_PAGE_SUBMIT_LABEL
      //
      //      resolvedJourneyConfig.labels.confirmPageLabels.title mustBe WelshConstantsNonUkMode.CONFIRM_PAGE_TITLE
      //      resolvedJourneyConfig.labels.confirmPageLabels.heading mustBe WelshConstantsNonUkMode.CONFIRM_PAGE_HEADING
      //      resolvedJourneyConfig.labels.confirmPageLabels.infoSubheading mustBe WelshConstantsNonUkMode.CONFIRM_PAGE_INFO_SUBHEADING
      //      resolvedJourneyConfig.labels.confirmPageLabels.infoMessage mustBe WelshConstantsNonUkMode.CONFIRM_PAGE_INFO_MESSAGE_HTML
      //      resolvedJourneyConfig.labels.confirmPageLabels.submitLabel mustBe WelshConstantsNonUkMode.CONFIRM_PAGE_SUBMIT_LABEL
      //      resolvedJourneyConfig.labels.confirmPageLabels.searchAgainLinkText mustBe WelshConstantsNonUkMode.SEARCH_AGAIN_LINK_TEXT
      //      resolvedJourneyConfig.labels.confirmPageLabels.changeLinkText mustBe WelshConstantsNonUkMode.CONFIRM_PAGE_EDIT_LINK_TEXT
      //      resolvedJourneyConfig.labels.confirmPageLabels.confirmChangeText mustBe WelshConstantsNonUkMode.CONFIRM_PAGE_CONFIRM_CHANGE_TEXT
    }
  }

  "welshEnabled" should {
    "return true" when {
      "there is welsh config provided" in {
        journeyDataV2Full.welshEnabled mustBe true
      }
      "there is no config provided" in {
        journeyDataV2Minimal.welshEnabled mustBe true
      }
    }
  }

  "ResolvedJourneyOptions" should {
    //TODO: isUKMode, provided and false
    "set the isUkMode to true" in {
      ResolvedJourneyOptions(journeyDataV2Full.config.options.copy(ukMode = Some(true)), appConfig).isUkMode mustBe true
    }
    "set the isUkMode to false" when {
      "ukMode is missing" in {
        ResolvedJourneyOptions(journeyDataV2Full.config.options.copy(ukMode = None), appConfig).isUkMode mustBe false
      }
      "ukMode is set to false" in {
        ResolvedJourneyOptions(journeyDataV2Full.config.options.copy(ukMode = Some(false)), appConfig).isUkMode mustBe false
      }
    }

    "set the phase value to alpha" in {
      ResolvedJourneyOptions(journeyDataV2Full.config.options.copy(alphaPhase = Some(true), showPhaseBanner = Some(true)), appConfig).phase mustBe "alpha"
    }
    "set the phase value to beta" in {
      ResolvedJourneyOptions(journeyDataV2Full.config.options.copy(alphaPhase = Some(false), showPhaseBanner = Some(true)), appConfig).phase mustBe "beta"
    }
    "set the phase value to empty string" in {
      ResolvedJourneyOptions(journeyDataV2Full.config.options.copy(alphaPhase = Some(false), showPhaseBanner = Some(false)), appConfig).phase mustBe ""
    }
  }
}
