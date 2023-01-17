/*
 * Copyright 2023 HM Revenue & Customs
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

package utils

import java.util.UUID

import controllers.Proposals
import model._
import play.api.libs.json.{JsValue, Json}
import address.v2.Country

object TestConstants {
  val testJourneyId: String = UUID.randomUUID().toString
  val testContinueUrl = "continueUrl"
  val testFilterValue = "bar"
  val testPostCode = "AA1 1AA"
  val testHomeNavRef = Some("homeNavRef")
  val testSignOutHref = Some("signOutHref")
  val testAccessibilityFooterUrl = Some("accessibilityFooterUrl")
  val testNavTitle = Some("navTitle")
  val testShowPhaseBanner = Some(true)
  val testAlphaPhase = Some(true)
  val testPhaseFeedbackLink = Some("phaseFeedbackLink")
  val testPhaseBannerHtml = Some("phaseBannerHtml")
  val testDisableTranslations = Some(true)
  val testShowBackButtons = Some(true)
  val testIncludeHmrcBranding = Some(true)
  val testDeskproServiceName = Some("deskproServiceName")
  val testAllowedCountryCodes = Some(Set("GB", "UK"))
  val testTimeoutAmount = 20
  val testTimeoutUrl = "timeoutUrl"
  val testTimeoutKeepAliveUrl = Some("testTimeoutKeepAliveUrl")
  val testUkMode = Some(true)

  val testAuditRef = "Jid123"
  val testAddressDetails = ConfirmableAddressDetails(None, List("1 High Street", "Line 2", "Line 3"), Some("Town"), Some("TF1 1NT"), Some(Country("UK", "United Kingdom")))
  val testAddress = ConfirmableAddress(testAuditRef, Some("1"), None, None, None, None, testAddressDetails)
  val testProposedAddressSeq = Seq(
    ProposedAddress("1",       uprn = None, parentUprn = None, usrn = None, organisation = None, "TF1 1NT", "Telford", List("1 High Street", "Line 2", "Line 3"), Country("UK", "United Kingdom")),
    ProposedAddress("2",       uprn = None, parentUprn = None, usrn = None, organisation = None, "TF2 2NT", "Shrewsbury", List("2 High Street", "Line2", "Line3"), Country("UK", "United Kingdom"))
  )

  object Lookup {
    val title = Some("lookupTitle")
    val heading = Some("lookupHeading")
    val afterHeadingText = Some("lookupAfterHeadingText")
    val filter = Some("filterLabel")
    val postcode = Some("postcodeLabel")
    val submit = Some("lookupSubmitLabel")
    val limitExceeded = Some("resultsLimitExceeded")
    val noResultsFound = Some("noResultsFound")
    val enterManually = Some("enterAddressManually")
  }

  object Select {
    val title = Some("selectTitle")
    val heading = Some("selectHeading")
    val headingWithPostcode = Some("selectHeadingWithPostcode")
    val proposalListLabel = Some("proposalListLabel")
    val submitLabel = Some("selectSubmitLabel")
    val proposalListLimit = Some(50)
    val showSearchagainLink = Some(true)
    val searchAgainLinkText = Some("selectSearchAgainLinkText")
    val editAddressLinkText = Some("editAddressLinkText")
  }

  object EditConst {
    val title = Some("editTitle")
    val heading = Some("editHeading")
    val line1 = Some("editLine1")
    val line2 = Some("editLine2")
    val line3 = Some("editLine3")
    val town = Some("editLine4")
    val postcode = Some("editPostcode")
    val country = Some("editCountry")
    val submit = Some("editSubmit")
  }

  object Confirm {
    val title = Some("confirmTitle")
    val heading = Some("confirmHeading")
    val showSubHeading = Some(true)
    val infoSubheading = Some("infoSubHeading")
    val infoMessage = Some("infoMessage")
    val submitLabel = Some("confirmSubmitLabel")
    val showSearchAgainLink = Some(true)
    val searchAgainLinkText = Some("confirmSearchAgainLinkText")
    val showChangeLink = Some(true)
    val changeLinkText = Some("changeLinkText")
    val showConfirmChangeLink = Some(true)
    val confirmChangeText = Some("confirmChangeText")
  }

  object testWelshConfirmPageLabels {
    val title = "cyConfirmPageTitle"
    val heading = "cyConfirmPageHeading"
    val infoSubheading = "cyConfirmPageInfoSubheading"
    val infoMessage = "cyConfirmPageInfoMessage"
    val submitLabel = "cyConfirmPageSubmitLabel"
    val searchAgainLinkText = "cyConfirmPageSearchAgainLinkText"
    val changeLinkText = "cyConfirmPageChangeLinkText"
    val confirmChangeText = "cyConfirmPageConfirmChangeText"
  }

  object testEnglishConfirmPageLabels {
    val title = "enConfirmPageTitle"
    val heading = "enConfirmPageHeading"
    val infoSubheading = "enConfirmPageInfoSubheading"
    val infoMessage = "enConfirmPageInfoMessage"
    val submitLabel = "enConfirmPageSubmitLabel"
    val searchAgainLinkText = "enConfirmPageSearchAgainLinkText"
    val changeLinkText = "enConfirmPageChangeLinkText"
    val confirmChangeText = "enConfirmPageConfirmChangeText"
  }

  object ALFHeaders {
    val useWelsh = "Use-Welsh"
  }

  val fullV1TimeoutConfig = Some(Timeout(testTimeoutAmount, testTimeoutUrl, testTimeoutKeepAliveUrl))

  // V2 model constants
  val fullV2AppLabels = Some(AppLevelLabels(testNavTitle, testPhaseBannerHtml))

  val fullV2SelectLabels = Some(SelectPageLabels(Select.title, Select.heading, Select.headingWithPostcode,
    Select.proposalListLabel, Select.submitLabel, Select.searchAgainLinkText, Select.editAddressLinkText))

  val fullV2LookupLabels = Some(LookupPageLabels(Lookup.title, Lookup.heading, Lookup.afterHeadingText, Lookup.filter, Lookup.postcode,
    Lookup.submit, Lookup.noResultsFound, Lookup.limitExceeded, Lookup.enterManually))

  val fullV2EditLabels = Some(EditPageLabels(EditConst.title, EditConst.heading, EditConst.line1, EditConst.line2, EditConst.line3, EditConst.town,
    EditConst.postcode, EditConst.country, EditConst.submit))

  val fullV2ConfirmLabels = Some(ConfirmPageLabels(Confirm.title, Confirm.heading, Confirm.infoSubheading,
    Confirm.infoMessage, Confirm.submitLabel, Confirm.searchAgainLinkText, Confirm.changeLinkText, Confirm.confirmChangeText))

  val fullV2SelectPageConfig = Some(SelectPageConfig(Select.proposalListLimit, Select.showSearchagainLink))

  val fullV2ConfirmPageConfig = Some(ConfirmPageConfig(Confirm.showSearchAgainLink, Confirm.showSubHeading, Confirm.showChangeLink, Confirm.showConfirmChangeLink))

  val fullV2TimeoutConfig = Some(TimeoutConfig(testTimeoutAmount, testTimeoutUrl, testTimeoutKeepAliveUrl))

  val fullV2JourneyOptions = JourneyOptions(testContinueUrl, testHomeNavRef, testSignOutHref, testAccessibilityFooterUrl, testPhaseFeedbackLink, testDeskproServiceName, testShowPhaseBanner, testAlphaPhase, testDisableTranslations, testShowBackButtons, testIncludeHmrcBranding, testUkMode, testAllowedCountryCodes, fullV2SelectPageConfig, fullV2ConfirmPageConfig, fullV2TimeoutConfig)

  val fullV2LanguageLabelsEn = LanguageLabels(
    appLevelLabels = fullV2AppLabels,
    selectPageLabels = fullV2SelectLabels,
    lookupPageLabels = fullV2LookupLabels,
    editPageLabels = fullV2EditLabels,
    confirmPageLabels = fullV2ConfirmLabels)

  val fullV2JourneyLabelsEn = Some(JourneyLabels(en = Some(fullV2LanguageLabelsEn)))

  val fullV2JourneyConfig = JourneyConfigV2(2, fullV2JourneyOptions, fullV2JourneyLabelsEn)

  val fullV2JourneyData = JourneyDataV2(fullV2JourneyConfig, Some(testProposedAddressSeq), Some(testAddress), Some(testAddress))

  val fullV2JourneyDataNonUkMode = JourneyDataV2(fullV2JourneyConfig.copy(options = fullV2JourneyOptions.copy(ukMode = Some(false))), Some(testProposedAddressSeq), Some(testAddress), Some(testAddress))

  val fullV2JourneyDataFromV1 = JourneyDataV2(fullV2JourneyConfig.copy(requestedVersion = Some(1), options = fullV2JourneyOptions.copy(disableTranslations = Some(false))), Some(testProposedAddressSeq), Some(testAddress), Some(testAddress))

  val emptyJson: JsValue = Json.parse("{}")

  val confirmPageLabelsMinimal = ConfirmPageLabels(None, None, None, None, None, None, None, None)

  val editPageLabelsMinimal = EditPageLabels(None, None, None, None, None, None, None, None, None)

  val lookupPageLabelsMinimal = LookupPageLabels(None, None, None, None, None, None, None, None)

  val selectPageLabelsMinimal = SelectPageLabels(None, None, None, None, None, None, None)

  val appLevelLabelsMinimal = AppLevelLabels(None, None)

  val languageLabelsMinimal = LanguageLabels(None, None, None, None, None)

  val internationalLanguageLabelsMinimal = InternationalLanguageLabels(None, None, None, None)

  val journeyLabelsMinimal = JourneyLabels(None, None)

  val timeoutConfigMissingKeepAliveUrlJson: JsValue = Json.parse("""{"timeoutAmount":119, "timeoutUrl": "testTimeoutUrl", "timeoutKeepAliveUrl": "testTimeoutKeepAliveUrl"}""")
  val timeoutConfigLessThanMinJson: JsValue = Json.parse("""{"timeoutAmount":119, "timeoutUrl": "testTimeoutUrl", "timeoutKeepAliveUrl": "testTimeoutKeepAliveUrl"}""")
  val timeoutConfigMissingAmountJson: JsValue = Json.parse("""{"timeoutUrl": "testTimeoutUrl", "timeoutKeepAliveUrl": "testTimeoutKeepAliveUrl"}""")
  val timeoutConfigMissingUrlJson: JsValue = Json.parse("""{"timeoutAmount":120, "timeoutKeepAliveUrl": "testTimeoutKeepAliveUrl"}""")

  val confirmPageConfigMinimal = ConfirmPageConfig(None, None, None, None)

  val confirmPageConfigFull = Some(ConfirmPageConfig(
    Confirm.showSearchAgainLink,
    Confirm.showSubHeading,
    Confirm.showChangeLink,
    Confirm.showConfirmChangeLink
  ))

  val selectPageConfigMinimal = SelectPageConfig(None, None)

  val journeyOptionsMinimal = JourneyOptions("testUrl", None, None, None, None, None, None, None, None, None, None, None, None, None, None, None)
  val journeyOptionsMinimalJson: JsValue = Json.parse("""{"continueUrl":"testUrl"}""")

  val journeyConfigV2 = JourneyConfigV2(2, journeyOptionsMinimal, Some(journeyLabelsMinimal))
  val journeyConfigV2Json: JsValue = Json.parse(s"""{"version":2, "options":$journeyOptionsMinimalJson, "labels":$emptyJson}""")

  val journeyConfigV2Minimal = JourneyConfigV2(2, journeyOptionsMinimal, None)
  val journeyConfigV2MinimalJson: JsValue = Json.parse(s"""{"version":2, "options":$journeyOptionsMinimalJson}""")
  val journeyConfigV2MissingVersionJson: JsValue = Json.parse(s"""{"options":$journeyOptionsMinimalJson}""")
  val journeyConfigV2MissingConfigJson: JsValue = Json.parse(s"""{"version":2}""")

  def fullV2JourneyDataCustomConfig(testContinueUrl: String = testContinueUrl,
                                    testHomeNavHref: Option[String] = testHomeNavRef,
                                    testAccessibilityFooterUrl: Option[String] = testAccessibilityFooterUrl,
                                    testPhaseFeedbackLink: Option[String] = testPhaseFeedbackLink,
                                    testDeskProServiceName: Option[String] = testDeskproServiceName,
                                    testShowPhaseBanner: Option[Boolean] = testShowPhaseBanner,
                                    testAlphaPhase: Option[Boolean] = testAlphaPhase,
                                    testDisableTranslations: Option[Boolean] = testDisableTranslations,
                                    testShowBackButtons: Option[Boolean] = testShowBackButtons,
                                    testIncludeHMRCBranding: Option[Boolean] = testIncludeHmrcBranding,
                                    testUkMode: Option[Boolean] = testUkMode,
                                    testAllowedCountryCodes: Option[Set[String]] = testAllowedCountryCodes,
                                    testSelectPage: Option[SelectPageConfig] = fullV2SelectPageConfig,
                                    testTimeoutConfig: Option[TimeoutConfig] = fullV2TimeoutConfig,
                                    testConfirmPageConfig: Option[ConfirmPageConfig] = confirmPageConfigFull,
                                    testLabels: Option[JourneyLabels] = fullV2JourneyLabelsEn
                                   ): JourneyDataV2 = {

    val journeyOptions = JourneyOptions(testContinueUrl, testHomeNavHref, testSignOutHref, testAccessibilityFooterUrl, testPhaseFeedbackLink, testDeskProServiceName, testShowPhaseBanner, testAlphaPhase, testShowBackButtons, testDisableTranslations, testIncludeHMRCBranding, testUkMode, testAllowedCountryCodes, testSelectPage, testConfirmPageConfig, testTimeoutConfig)

    JourneyDataV2(
      JourneyConfigV2(
        2,
        journeyOptions,
        testLabels
      ),
      Some(testProposedAddressSeq),
      Some(testAddress),
      Some(testAddress)
    )

  }

  val journeyDataV2Full = JourneyDataV2(
    config = JourneyConfigV2(
      version = 2,
      options = JourneyOptions(continueUrl = "testContinueUrl", homeNavHref = Some("testNavHref"), accessibilityFooterUrl = Some("testAccessibilityFooterUrl"), phaseFeedbackLink = Some("testFeedbackLink"), deskProServiceName = Some("testDeskproName"), showPhaseBanner = Some(true), alphaPhase = Some(true), showBackButtons = Some(true), disableTranslations = Some(false), includeHMRCBranding = Some(true), ukMode = Some(true), allowedCountryCodes = Some(Set("UK", "FR")), selectPageConfig = Some(SelectPageConfig(
                proposalListLimit = Some(30),
                showSearchAgainLink = Some(true)
              )), confirmPageConfig = Some(ConfirmPageConfig(
                showSearchAgainLink = Some(true),
                showSubHeadingAndInfo = Some(true),
                showChangeLink = Some(true),
                showConfirmChangeText = Some(true)
              )), timeoutConfig = Some(TimeoutConfig(
                timeoutAmount = 120,
                timeoutUrl = "testTimeoutUrl",
                timeoutKeepAliveUrl = Some("testTimeoutKeepAliveUrl")
              ))),
      labels = Some(JourneyLabels(
        en = Some(LanguageLabels(
          appLevelLabels = Some(AppLevelLabels(
            navTitle = Some("enNavTitle"),
            phaseBannerHtml = Some("enPhaseBannerHtml")
          )),
          selectPageLabels = Some(SelectPageLabels(
            title = Some("enSelectPageTitle"),
            heading = Some("enSelectPageHeading"),
            headingWithPostcode = Some("enSelectPageHeadingWithPostcode"),
            proposalListLabel = Some("enProposalListLabel"),
            submitLabel = Some("enSubmitLabel"),
            searchAgainLinkText = Some("enSearchAgainLinkText"),
            editAddressLinkText = Some("enEditAddressLinkText")
          )),
          lookupPageLabels = Some(LookupPageLabels(
            title = Some("enLookupPageTitle"),
            titleUkMode = Some("enLookupPageTitle"),
            heading = Some("enLookupPageHeading"),
            headingUkMode = Some("enLookupPageHeading"),
            afterHeadingText = Some("enAfterHeadingText"),
            filterLabel = Some("enFilterLabel"),
            postcodeLabel = Some("enPostcodeLabel"),
            postcodeLabelUkMode = Some("enPostcodeLabel"),
            submitLabel = Some("enSubmitLabel"),
            noResultsFoundMessage = Some("enNoResultsFoundMessage"),
            resultLimitExceededMessage = Some("enResultLimitExceededMessage"),
            manualAddressLinkText = Some("enManualAddressLinkText")
          )),
          editPageLabels = Some(EditPageLabels(
            title = Some("enEditPageTitle"),
            heading = Some("enEditPageHeading"),
            line1Label = Some("enEditPageLine1Label"),
            line2Label = Some("enEditPageLine2Label"),
            line3Label = Some("enEditPageLine3Label"),
            townLabel = Some("enEditPageTownLabel"),
            postcodeLabel = Some("enEditPagePostcodeLabel"),
            postcodeLabelUkMode = Some("enEditPagePostcodeLabel"),
            countryLabel = Some("enEditPageCountryLabel"),
            submitLabel = Some("enEditPageSubmitLabel")
          )),
          confirmPageLabels = Some(ConfirmPageLabels(
            title = Some("enConfirmPageTitle"),
            heading = Some("enConfirmPageHeading"),
            infoSubheading = Some("enConfirmPageInfoSubheading"),
            infoMessage = Some("enConfirmPageInfoMessage"),
            submitLabel = Some("enConfirmPageSubmitLabel"),
            searchAgainLinkText = Some("enConfirmPageSearchAgainLinkText"),
            changeLinkText = Some("enConfirmPageChangeLinkText"),
            confirmChangeText = Some("enConfirmPageConfirmChangeText")
          ))
        )),
        cy = Some(LanguageLabels(
          appLevelLabels = Some(AppLevelLabels(
            navTitle = Some("cyNavTitle"),
            phaseBannerHtml = Some("cyPhaseBannerHtml")
          )),
          selectPageLabels = Some(SelectPageLabels(
            title = Some("cySelectPageTitle"),
            heading = Some("cySelectPageHeading"),
            headingWithPostcode = Some("cySelectPageHeadingWithPostcode"),
            proposalListLabel = Some("cyProposalListLabel"),
            submitLabel = Some("cySubmitLabel"),
            searchAgainLinkText = Some("cySearchAgainLinkText"),
            editAddressLinkText = Some("cyEditAddressLinkText")
          )),
          lookupPageLabels = Some(LookupPageLabels(
            title = Some("cyLookupPageTitle"),
            titleUkMode = Some("cyLookupPageTitle"),
            heading = Some("cyLookupPageHeading"),
            headingUkMode = Some("cyLookupPageHeading"),
            afterHeadingText = Some("cyAfterHeadingText"),
            filterLabel = Some("cyFilterLabel"),
            postcodeLabel = Some("cyPostcodeLabel"),
            postcodeLabelUkMode = Some("cyPostcodeLabel"),
            submitLabel = Some("cySubmitLabel"),
            noResultsFoundMessage = Some("cyNoResultsFoundMessage"),
            resultLimitExceededMessage = Some("cyResultLimitExceededMessage"),
            manualAddressLinkText = Some("cyManualAddressLinkText")
          )),
          editPageLabels = Some(EditPageLabels(
            title = Some("cyEditPageTitle"),
            heading = Some("cyEditPageHeading"),
            line1Label = Some("cyEditPageLine1Label"),
            line2Label = Some("cyEditPageLine2Label"),
            line3Label = Some("cyEditPageLine3Label"),
            townLabel = Some("cyEditPageTownLabel"),
            postcodeLabel = Some("cyEditPagePostcodeLabel"),
            postcodeLabelUkMode = Some("cyEditPagePostcodeLabel"),
            countryLabel = Some("cyEditPageCountryLabel"),
            submitLabel = Some("cyEditPageSubmitLabel")
          )),
          confirmPageLabels = Some(ConfirmPageLabels(
            title = Some("cyConfirmPageTitle"),
            heading = Some("cyConfirmPageHeading"),
            infoSubheading = Some("cyConfirmPageInfoSubheading"),
            infoMessage = Some("cyConfirmPageInfoMessage"),
            submitLabel = Some("cyConfirmPageSubmitLabel"),
            searchAgainLinkText = Some("cyConfirmPageSearchAgainLinkText"),
            changeLinkText = Some("cyConfirmPageChangeLinkText"),
            confirmChangeText = Some("cyConfirmPageConfirmChangeText")
          ))
        ))
      ))
    )
  )

  val journeyDataV2FullJson: JsValue = Json.parse(
    """{
      |   "config":{
      |      "version":2,
      |      "options":{
      |         "continueUrl":"testContinueUrl",
      |         "homeNavHref":"testNavHref",
      |         "accessibilityFooterUrl": "testAccessibilityFooterUrl",
      |         "phaseFeedbackLink":"testFeedbackLink",
      |         "deskProServiceName":"testDeskproName",
      |         "showPhaseBanner":true,
      |         "alphaPhase":true,
      |         "disableTranslations": false,
      |         "showBackButtons":true,
      |         "includeHMRCBranding":true,
      |         "ukMode":true,
      |         "allowedCountryCodes":[
      |            "UK",
      |            "FR"
      |         ],
      |         "selectPageConfig":{
      |            "proposalListLimit":30,
      |            "showSearchAgainLink":true
      |         },
      |         "confirmPageConfig":{
      |            "showSearchAgainLink":true,
      |            "showSubHeadingAndInfo":true,
      |            "showChangeLink":true,
      |            "showConfirmChangeText":true
      |         },
      |         "timeoutConfig":{
      |            "timeoutAmount":120,
      |            "timeoutUrl":"testTimeoutUrl",
      |            "timeoutKeepAliveUrl": "testTimeoutKeepAliveUrl"
      |         }
      |      },
      |      "labels":{
      |         "en":{
      |            "appLevelLabels":{
      |               "navTitle":"enNavTitle",
      |               "phaseBannerHtml":"enPhaseBannerHtml"
      |            },
      |            "selectPageLabels":{
      |               "title":"enSelectPageTitle",
      |               "heading":"enSelectPageHeading",
      |               "headingWithPostcode":"enSelectPageHeadingWithPostcode",
      |               "proposalListLabel":"enProposalListLabel",
      |               "submitLabel":"enSubmitLabel",
      |               "searchAgainLinkText":"enSearchAgainLinkText",
      |               "editAddressLinkText":"enEditAddressLinkText"
      |            },
      |            "lookupPageLabels":{
      |               "title":"enLookupPageTitle",
      |               "titleUkMode":"enLookupPageTitle",
      |               "heading":"enLookupPageHeading",
      |               "headingUkMode":"enLookupPageHeading",
      |               "afterHeadingText":"enAfterHeadingText",
      |               "filterLabel":"enFilterLabel",
      |               "postcodeLabel":"enPostcodeLabel",
      |               "postcodeLabelUkMode":"enPostcodeLabel",
      |               "submitLabel":"enSubmitLabel",
      |               "noResultsFoundMessage":"enNoResultsFoundMessage",
      |               "resultLimitExceededMessage":"enResultLimitExceededMessage",
      |               "manualAddressLinkText":"enManualAddressLinkText"
      |            },
      |            "editPageLabels":{
      |               "title":"enEditPageTitle",
      |               "heading":"enEditPageHeading",
      |               "line1Label":"enEditPageLine1Label",
      |               "line2Label":"enEditPageLine2Label",
      |               "line3Label":"enEditPageLine3Label",
      |               "townLabel":"enEditPageTownLabel",
      |               "postcodeLabel":"enEditPagePostcodeLabel",
      |               "postcodeLabelUkMode":"enEditPagePostcodeLabel",
      |               "countryLabel":"enEditPageCountryLabel",
      |               "submitLabel":"enEditPageSubmitLabel"
      |            },
      |            "confirmPageLabels":{
      |               "title":"enConfirmPageTitle",
      |               "heading":"enConfirmPageHeading",
      |               "infoSubheading":"enConfirmPageInfoSubheading",
      |               "infoMessage":"enConfirmPageInfoMessage",
      |               "submitLabel":"enConfirmPageSubmitLabel",
      |               "searchAgainLinkText":"enConfirmPageSearchAgainLinkText",
      |               "changeLinkText":"enConfirmPageChangeLinkText",
      |               "confirmChangeText":"enConfirmPageConfirmChangeText"
      |            }
      |         },
      |         "cy":{
      |            "appLevelLabels":{
      |               "navTitle":"cyNavTitle",
      |               "phaseBannerHtml":"cyPhaseBannerHtml"
      |            },
      |            "selectPageLabels":{
      |               "title":"cySelectPageTitle",
      |               "heading":"cySelectPageHeading",
      |               "headingWithPostcode":"cySelectPageHeadingWithPostcode",
      |               "proposalListLabel":"cyProposalListLabel",
      |               "submitLabel":"cySubmitLabel",
      |               "searchAgainLinkText":"cySearchAgainLinkText",
      |               "editAddressLinkText":"cyEditAddressLinkText"
      |            },
      |            "lookupPageLabels":{
      |               "title":"cyLookupPageTitle",
      |               "titleUkMode":"cyLookupPageTitle",
      |               "heading":"cyLookupPageHeading",
      |               "headingUkMode":"cyLookupPageHeading",
      |               "afterHeadingText":"cyAfterHeadingText",
      |               "filterLabel":"cyFilterLabel",
      |               "postcodeLabel":"cyPostcodeLabel",
      |               "postcodeLabelUkMode":"cyPostcodeLabel",
      |               "submitLabel":"cySubmitLabel",
      |               "noResultsFoundMessage":"cyNoResultsFoundMessage",
      |               "resultLimitExceededMessage":"cyResultLimitExceededMessage",
      |               "manualAddressLinkText":"cyManualAddressLinkText"
      |            },
      |            "editPageLabels":{
      |               "title":"cyEditPageTitle",
      |               "heading":"cyEditPageHeading",
      |               "line1Label":"cyEditPageLine1Label",
      |               "line2Label":"cyEditPageLine2Label",
      |               "line3Label":"cyEditPageLine3Label",
      |               "townLabel":"cyEditPageTownLabel",
      |               "postcodeLabel":"cyEditPagePostcodeLabel",
      |               "postcodeLabelUkMode":"cyEditPagePostcodeLabel",
      |               "countryLabel":"cyEditPageCountryLabel",
      |               "submitLabel":"cyEditPageSubmitLabel"
      |            },
      |            "confirmPageLabels":{
      |               "title":"cyConfirmPageTitle",
      |               "heading":"cyConfirmPageHeading",
      |               "infoSubheading":"cyConfirmPageInfoSubheading",
      |               "infoMessage":"cyConfirmPageInfoMessage",
      |               "submitLabel":"cyConfirmPageSubmitLabel",
      |               "searchAgainLinkText":"cyConfirmPageSearchAgainLinkText",
      |               "changeLinkText":"cyConfirmPageChangeLinkText",
      |               "confirmChangeText":"cyConfirmPageConfirmChangeText"
      |            }
      |         }
      |      }
      |   }
      |}
    """.stripMargin)

  val journeyDataV2Minimal = JourneyDataV2(
    config = JourneyConfigV2(
      version = 2,
      options = JourneyOptions(continueUrl = "testContinueUrl")
    )
  )

  val journeyDataV2MinimalExpected = JourneyDataV2(
    config = JourneyConfigV2(
      version = 2,
      options = JourneyOptions(continueUrl = "testContinueUrl")
    )
  )

  val journeyDataV2MinimalUKMode = JourneyDataV2(
    config = JourneyConfigV2(
      version = 2,
      options = JourneyOptions(continueUrl = "testContinueUrl", ukMode = Some(true))
    )
  )

  val journeyDataV2EnglishAndWelshMinimal = JourneyDataV2(
    config = JourneyConfigV2(
      version = 2,
      options = JourneyOptions(continueUrl = "testContinueUrl"),
      labels = Some(JourneyLabels(
        en = Some(LanguageLabels()),
        cy = Some(LanguageLabels())
      ))
    )
  )

  val journeyDataV2EnglishAndWelshMinimalUKMode = JourneyDataV2(
    config = JourneyConfigV2(
      version = 2,
      options = JourneyOptions(continueUrl = "testContinueUrl", ukMode = Some(true)),
      labels = Some(JourneyLabels(
        en = Some(LanguageLabels()),
        cy = Some(LanguageLabels())
      ))
    )
  )

  val journeyDataV2MinimalJson: JsValue = Json.parse(
    """{
      |   "config":{
      |      "version":2,
      |      "options":{
      |         "continueUrl":"testContinueUrl"
      |      }
      |   }
      |}
    """.stripMargin
  )

  val testId: String = UUID.randomUUID().toString

  val testLookup =
    model.Lookup(
      filter = Some("testFilter"),
      postcode = "TF3 4NT"
    )

  val testProposal =
    Proposals(
      proposals = Some(Seq(ProposedAddress(
        uprn = None,
        parentUprn = None,
        usrn = None,
        organisation = None,
        town = "Telford",
        addressId = "testAddressId",
        postcode = "TF3 4NT",
        lines = List("1 High Street", "Line 2", "Line 3")
      )))
    )

  val testProposalMany =
    Proposals(
      proposals = Some(Seq(
        ProposedAddress(
          uprn = None,
          parentUprn = None,
          usrn = None,
          organisation = None,
          town = "Telford",
          addressId = "testAddressId0",
          postcode = "TF3 4NT",
          lines = List("1 High Street", "Line 2", "Line 3")
        ),

        ProposedAddress(
          uprn = None,
          parentUprn = None,
          usrn = None,
          organisation = None,
          town = "Telford",
          addressId = "testAddressId1",
          postcode = "TF3 4NT",
          lines = List("2 High Street", "Line 2", "Line 3")
        ),

        ProposedAddress(
          uprn = None,
          parentUprn = None,
          usrn = None,
          organisation = None,
          town = "Telford",
          addressId = "testAddressId2",
          postcode = "TF3 4NT",
          lines = List("3 High Street", "Line 2", "Line 3")
        )
      ))
    )

  val testProposalNone = Proposals(proposals = Some(Seq()))

  val testBasicLevelJourneyConfigV2 =
    JourneyDataV2(
      config = JourneyConfigV2(
        version = 2,
        options = JourneyOptions(continueUrl = "testContinueUrl"),
        labels = Some(JourneyLabels(
          en = Some(LanguageLabels(
            appLevelLabels = None,
            selectPageLabels = None,
            lookupPageLabels = None,
            editPageLabels = None,
            confirmPageLabels = None
          )),
          cy = None
        ))
      )
    )

  val testSelectPageConfig =
    JourneyDataV2(
      config = JourneyConfigV2(
        version = 2,
        options = JourneyOptions(continueUrl = "testContinueUrl", showBackButtons = Some(true), selectPageConfig = Some(SelectPageConfig(Some(10), Some(true)))),
        labels = Some(JourneyLabels(en = Some(LanguageLabels(selectPageLabels = Some(SelectPageLabels(
          title = Some("testTitle"),
          heading = Some("testHeading"),
          headingWithPostcode = Some("testHeadingWithPostcode "),
          proposalListLabel = Some("testProposalListLabel"),
          submitLabel = Some("testSubmitLabel"),
          searchAgainLinkText = Some("testSearchAgainLinkText"),
          editAddressLinkText = Some("testEditAddressLinkText")
        ))))))
      )
    )

  val testSelectPageConfigWelshEmpty =
    JourneyDataV2(
      config = JourneyConfigV2(
        version = 2,
        options = JourneyOptions(continueUrl = "testContinueUrl", showBackButtons = Some(true), selectPageConfig = Some(SelectPageConfig(Some(10), Some(true)))),
        labels = Some(JourneyLabels(en = Some(LanguageLabels(selectPageLabels = Some(SelectPageLabels(
          title = Some("testTitle"),
          heading = Some("testHeading"),
          headingWithPostcode = Some("testHeadingWithPostcode "),
          proposalListLabel = Some("testProposalListLabel"),
          submitLabel = Some("testSubmitLabel"),
          searchAgainLinkText = Some("testSearchAgainLinkText"),
          editAddressLinkText = Some("testEditAddressLinkText")
        )))), cy = Some(LanguageLabels())))
      )
    )

  val testWelshSelectPageConfig =
    JourneyDataV2(
      config = JourneyConfigV2(
        version = 2,
        options = JourneyOptions(continueUrl = "testContinueUrl", showBackButtons = Some(true), selectPageConfig = Some(SelectPageConfig(Some(10), Some(true)))),
        labels = Some(JourneyLabels(
          en = Some(LanguageLabels(
            selectPageLabels = Some(SelectPageLabels(
              title = Some("testTitle"),
              heading = Some("testHeading"),
              headingWithPostcode = Some("testHeadingWithPostcode "),
              proposalListLabel = Some("testProposalListLabel"),
              submitLabel = Some("testSubmitLabel"),
              searchAgainLinkText = Some("testSearchAgainLinkText"),
              editAddressLinkText = Some("testEditAddressLinkText")
            ))
          )),
          cy = Some(LanguageLabels(
            selectPageLabels = Some(SelectPageLabels(
              title = Some("cyTestTitle"),
              heading = Some("cyTestHeading"),
              headingWithPostcode = Some("cyTestHeadingWithPostcode "),
              proposalListLabel = Some("cyTestProposalListLabel"),
              submitLabel = Some("cyTestSubmitLabel"),
              searchAgainLinkText = Some("cyTestSearchAgainLinkText"),
              editAddressLinkText = Some("cyTestEditAddressLinkText")
            ))
          ))
        ))
      )
    )

  val testJourneyDataNoBackButtons =
    JourneyDataV2(
      config = JourneyConfigV2(
        version = 2,
        options = JourneyOptions(continueUrl = "testContinueUrl", showBackButtons = Some(false))
      )
    )

  val testSelectPageConfigMinimal =
    JourneyDataV2(
      config = JourneyConfigV2(
        version = 2,
        options = JourneyOptions(continueUrl = "testContinueUrl")
      )
    )

  val testSelectPageConfigNoLabel =
    JourneyDataV2(
      config = JourneyConfigV2(
        version = 2,
        options = JourneyOptions(continueUrl = "testContinueUrl", showBackButtons = Some(true), selectPageConfig = Some(SelectPageConfig(Some(10), Some(true))))
      )
    )

  val testAppLevelJourneyConfigV2 =
    JourneyDataV2(
      config = JourneyConfigV2(
        version = 2,
        options = JourneyOptions(continueUrl = "testContinueUrl", homeNavHref = Some("testNavHref"), accessibilityFooterUrl = Some("testAccessibilityFooterUrl"), phaseFeedbackLink = None, deskProServiceName = Some("testDeskproName"), showPhaseBanner = Some(true), alphaPhase = Some(true), showBackButtons = None, includeHMRCBranding = Some(true), ukMode = None, allowedCountryCodes = None, selectPageConfig = None, confirmPageConfig = None, timeoutConfig = Some(TimeoutConfig(
                    timeoutAmount = 120,
                    timeoutUrl = "testTimeoutUrl",
                    timeoutKeepAliveUrl = Some("testTimeoutKeepAliveUrl")
                  ))),
        labels = Some(JourneyLabels(
          en = Some(LanguageLabels(
            appLevelLabels = Some(AppLevelLabels(
              navTitle = Some("enNavTitle"),
              phaseBannerHtml = Some("enPhaseBannerHtml")
            )),
            selectPageLabels = None,
            lookupPageLabels = None,
            editPageLabels = None,
            confirmPageLabels = None
          )),
          cy = None
        ))
      )
    )

  val testAppLevelJourneyConfigV2WithWelsh =
    JourneyDataV2(
      config = JourneyConfigV2(
        version = 2,
        options = JourneyOptions(continueUrl = "testContinueUrl", homeNavHref = Some("testNavHref"), accessibilityFooterUrl = Some("testAccessibilityFooterUrl"), phaseFeedbackLink = None, deskProServiceName = Some("testDeskproName"), showPhaseBanner = Some(true), alphaPhase = Some(true), showBackButtons = None, includeHMRCBranding = Some(true), ukMode = None, allowedCountryCodes = None, selectPageConfig = None, confirmPageConfig = None, timeoutConfig = Some(TimeoutConfig(
                    timeoutAmount = 120,
                    timeoutUrl = "testTimeoutUrl",
                    timeoutKeepAliveUrl = Some("testTimeoutKeepAliveUrl")
                  ))),
        labels = Some(JourneyLabels(
          en = Some(LanguageLabels(
            appLevelLabels = Some(AppLevelLabels(
              navTitle = Some("enNavTitle"),
              phaseBannerHtml = Some("enPhaseBannerHtml")
            )),
            selectPageLabels = None,
            lookupPageLabels = None,
            editPageLabels = None,
            confirmPageLabels = None
          )),
          cy = Some(LanguageLabels(
            appLevelLabels = Some(AppLevelLabels(
              navTitle = Some("cyNavTitle"),
              phaseBannerHtml = Some("cyPhaseBannerHtml")
            ))
          ))
        ))
      )
    )

  val testAppLevelJourneyConfigV2WithWelshDisabled =
    JourneyDataV2(
      config = JourneyConfigV2(
        version = 2,
        options = JourneyOptions(continueUrl = "testContinueUrl", homeNavHref = Some("testNavHref"), accessibilityFooterUrl = Some("testAccessibilityFooterUrl"), phaseFeedbackLink = None, deskProServiceName = Some("testDeskproName"), showPhaseBanner = Some(true), alphaPhase = Some(true), showBackButtons = None, disableTranslations = Some(true), includeHMRCBranding = Some(true), ukMode = None, allowedCountryCodes = None, selectPageConfig = None, confirmPageConfig = None, timeoutConfig = Some(TimeoutConfig(
                    timeoutAmount = 120,
                    timeoutUrl = "testTimeoutUrl",
                    timeoutKeepAliveUrl = Some("testTimeoutKeepAliveUrl")
                  ))),
        labels = Some(JourneyLabels(
          en = Some(LanguageLabels(
            appLevelLabels = Some(AppLevelLabels(
              navTitle = Some("enNavTitle"),
              phaseBannerHtml = Some("enPhaseBannerHtml")
            )),
            selectPageLabels = None,
            lookupPageLabels = None,
            editPageLabels = None,
            confirmPageLabels = None
          )),
          cy = Some(LanguageLabels(
            appLevelLabels = Some(AppLevelLabels(
              navTitle = Some("cyNavTitle"),
              phaseBannerHtml = Some("cyPhaseBannerHtml")
            ))
          ))
        ))
      )
    )

  val testLookupLevelJourneyConfigV2 =
    JourneyDataV2(
      config = JourneyConfigV2(
        version = 2,
        options = JourneyOptions(continueUrl = "testContinueUrl", homeNavHref = Some("testNavHref"), accessibilityFooterUrl = Some("testAccessibilityFooterUrl"), phaseFeedbackLink = None, deskProServiceName = Some("testDeskproName"), showPhaseBanner = Some(true), alphaPhase = Some(true), showBackButtons = Some(true), includeHMRCBranding = Some(true), ukMode = None, allowedCountryCodes = None, selectPageConfig = None, confirmPageConfig = None, timeoutConfig = Some(TimeoutConfig(
                    timeoutAmount = 120,
                    timeoutUrl = "testTimeoutUrl",
                    timeoutKeepAliveUrl = Some("testTimeoutKeepAliveUrl")
                  ))),
        labels = Some(JourneyLabels(
          en = Some(LanguageLabels(
            appLevelLabels = None,
            selectPageLabels = None,
            lookupPageLabels = Some(LookupPageLabels(
              title = Some("enLookupPageTitle"),
              heading = Some("enLookupPageHeading"),
              filterLabel = Some("enFilterLabel"),
              postcodeLabel = Some("enPostcodeLabel"),
              submitLabel = Some("enSubmitLabel"),
              noResultsFoundMessage = Some("enNoResultsFoundMessage"),
              resultLimitExceededMessage = Some("enResultLimitExceededMessage"),
              manualAddressLinkText = Some("enManualAddressLinkText")
            )),
            editPageLabels = None,
            confirmPageLabels = None,
            international = Some(InternationalLanguageLabels(
              lookupPageLabels = Some(InternationalLookupPageLabels(
                title = Some("international-enLookupPageTitle"),
                heading = Some("international-enLookupPageHeading"),
                filterLabel = Some("international-enFilterLabel"),
                submitLabel = Some("international-enSubmitLabel"),
                noResultsFoundMessage = Some("international-enNoResultsFoundMessage"),
                resultLimitExceededMessage = Some("international-enResultLimitExceededMessage"),
                manualAddressLinkText = Some("international-enManualAddressLinkText")
              ))
            ))
          )),
          cy = None
        ))
      )
    )

  val testLookupLevelCYJourneyConfigV2 =
    JourneyDataV2(
      config = JourneyConfigV2(
        version = 2,
        options = JourneyOptions(continueUrl = "testContinueUrl", homeNavHref = Some("testNavHref"), accessibilityFooterUrl = Some("testAccessibilityFooterUrl"), phaseFeedbackLink = None, deskProServiceName = Some("testDeskproName"), showPhaseBanner = Some(true), alphaPhase = Some(true), showBackButtons = Some(true), includeHMRCBranding = Some(true), ukMode = Some(false), allowedCountryCodes = None, selectPageConfig = None, confirmPageConfig = None, timeoutConfig = Some(TimeoutConfig(
                    timeoutAmount = 120,
                    timeoutUrl = "testTimeoutUrl",
                    timeoutKeepAliveUrl = Some("testTimeoutKeepAliveUrl")
                  ))),
        labels = Some(JourneyLabels(
          en = None,
          cy = Some(LanguageLabels(
            appLevelLabels = None,
            selectPageLabels = None,
            lookupPageLabels = Some(LookupPageLabels(
              title = Some("cyLookupPageTitle"),
              heading = Some("cyLookupPageHeading"),
              filterLabel = Some("cyFilterLabel"),
              postcodeLabel = Some("cyPostcodeLabel"),
              submitLabel = Some("cySubmitLabel"),
              noResultsFoundMessage = Some("cyNoResultsFoundMessage"),
              resultLimitExceededMessage = Some("cyResultLimitExceededMessage"),
              manualAddressLinkText = Some("cyManualAddressLinkText")
            )),
            editPageLabels = None,
            confirmPageLabels = None
          ))
        ))
      )
    )


  val testDefaultCYJourneyConfigV2 =
    JourneyDataV2(
      config = JourneyConfigV2(
        version = 2,
        options = JourneyOptions(continueUrl = "testContinueUrl", homeNavHref = Some("testNavHref"), accessibilityFooterUrl = Some("testAccessibilityFooterUrl"), phaseFeedbackLink = None, deskProServiceName = Some("testDeskproName"), showPhaseBanner = Some(true), alphaPhase = Some(true), showBackButtons = Some(true), includeHMRCBranding = Some(true), ukMode = Some(false), allowedCountryCodes = None, selectPageConfig = None, confirmPageConfig = None, timeoutConfig = Some(TimeoutConfig(
                    timeoutAmount = 120,
                    timeoutUrl = "testTimeoutUrl",
                    timeoutKeepAliveUrl = Some("testTimeoutKeepAliveUrl")
                  ))),
        labels = Some(JourneyLabels(
          en = None,
          cy = Some(LanguageLabels(
            appLevelLabels = None,
            selectPageLabels = None,
            lookupPageLabels = None,
            editPageLabels = None,
            confirmPageLabels = None
          ))
        ))
      ),
      countryCode = Some("GB")
    )

  val testNoResultsConfig = JourneyDataV2(
    config = JourneyConfigV2(
      2,
      options = JourneyOptions(continueUrl = "", showBackButtons = Some(false))
    )
  )
}
