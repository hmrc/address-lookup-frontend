package utils

import java.util.UUID

import model._
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.address.v2.Country

object TestConstants {
  val testContinueUrl = "continueUrl"
  val testHomeNavRef = Some("homeNavRef")
  val testNavTitle = Some("navTitle")
  val testAdditionalStylesheetUrl = Some("additionalStylesheetUrl")
  val testShowPhaseBanner = Some(true)
  val testAlphaPhase = Some(true)
  val testPhaseFeedbackLink = Some("phaseFeedbackLink")
  val testPhaseBannerHtml = Some("phaseBannerHtml")
  val testShowBackButtons = Some(true)
  val testIncludeHmrcBranding = Some(true)
  val testDeskproServiceName = Some("deskproServiceName")
  val testAllowedCountryCodes = Some(Set("GB", "UK"))
  val testTimeoutAmount = 20
  val testTimeoutUrl = "timeoutUrl"
  val testUkMode = Some(true)

  val testAuditRef = "Jid123"
  val testAddressDetails = ConfirmableAddressDetails(Some(List("1 High Street", "Line 2", "Line 3")), Some("TF1 1NT"), Some(Country("UK", "United Kingdom")))
  val testAddress = ConfirmableAddress(testAuditRef, Some("1"), testAddressDetails)
  val testProposedAddressSeq = Seq(
    ProposedAddress("1", "TF1 1NT", List("1 High Street", "Line 2", "Line 3"), Some("Telford"), Some("United Kingdom")),
    ProposedAddress("2", "TF2 2NT", List("2 High Street", "Line2", "Line3"), Some("Shrewsbury"), Some("United Kingdom"))
  )

  object Lookup {
    val title = Some("lookupTitle")
    val heading = Some("lookupHeading")
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

  object Edit {
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

  // V1 model constants
  val fullV1LookupConfig = Some(LookupPage(Lookup.title, Lookup.heading, Lookup.filter, Lookup.postcode, Lookup.submit,
    Lookup.limitExceeded, Lookup.noResultsFound, Lookup.enterManually))

  val fullV1SelectConfig = Some(SelectPage(Select.title, Select.heading, Select.headingWithPostcode,
    Select.proposalListLabel, Select.submitLabel, Select.proposalListLimit, Select.showSearchagainLink,
    Select.searchAgainLinkText, Select.editAddressLinkText))

  val fullV1EditConfig = Some(EditPage(Edit.title, Edit.heading, Edit.line1, Edit.line2, Edit.line3, Edit.town,
    Edit.postcode, Edit.country, Edit.submit))

  val fullV1ConfirmConfig = Some(ConfirmPage(Confirm.title, Confirm.heading, Confirm.showSubHeading, Confirm.infoSubheading,
    Confirm.infoMessage, Confirm.submitLabel, Confirm.showSearchAgainLink, Confirm.searchAgainLinkText, Confirm.showChangeLink,
    Confirm.changeLinkText, Confirm.showConfirmChangeLink, Confirm.confirmChangeText))

  val fullV1TimeoutConfig = Some(Timeout(testTimeoutAmount, testTimeoutUrl))

  val fullV1JourneyConfig = JourneyConfig(
    continueUrl = testContinueUrl,
    lookupPage = fullV1LookupConfig,
    selectPage = fullV1SelectConfig,
    confirmPage = fullV1ConfirmConfig,
    editPage = fullV1EditConfig,
    homeNavHref = testHomeNavRef,
    navTitle = testNavTitle,
    additionalStylesheetUrl = testAdditionalStylesheetUrl,
    showPhaseBanner = testShowPhaseBanner,
    alphaPhase = testAlphaPhase,
    phaseFeedbackLink = testPhaseFeedbackLink,
    phaseBannerHtml = testPhaseBannerHtml,
    showBackButtons = testShowBackButtons,
    includeHMRCBranding = testIncludeHmrcBranding,
    deskProServiceName = testDeskproServiceName,
    allowedCountryCodes = testAllowedCountryCodes,
    timeout = fullV1TimeoutConfig,
    ukMode = testUkMode
  )

  val fullV1JourneyData = JourneyData(fullV1JourneyConfig, Some(testProposedAddressSeq), Some(testAddress), Some(testAddress))

  // V2 model constants
  val fullV2AppLabels = Some(AppLevelLabels(testNavTitle, testPhaseBannerHtml))

  val fullV2SelectLabels = Some(SelectPageLabels(Select.title, Select.heading, Select.headingWithPostcode,
    Select.proposalListLabel, Select.submitLabel, Select.searchAgainLinkText, Select.editAddressLinkText))

  val fullV2LookupLabels = Some(LookupPageLabels(Lookup.title, Lookup.heading, Lookup.filter, Lookup.postcode,
    Lookup.submit, Lookup.noResultsFound, Lookup.limitExceeded, Lookup.enterManually))

  val fullV2EditLabels = Some(EditPageLabels(Edit.title, Edit.heading, Edit.line1, Edit.line2, Edit.line3, Edit.town,
    Edit.postcode, Edit.country, Edit.submit))

  val fullV2ConfirmLabels = Some(ConfirmPageLabels(Confirm.title, Confirm.heading, Confirm.infoSubheading,
    Confirm.infoMessage, Confirm.submitLabel, Confirm.searchAgainLinkText, Confirm.changeLinkText, Confirm.confirmChangeText))

  val fullV2SelectPageConfig = Some(SelectPageConfig(Select.proposalListLimit, Select.showSearchagainLink))

  val fullV2ConfirmPageConfig = Some(ConfirmPageConfig(Confirm.showSearchAgainLink, Confirm.showSubHeading, Confirm.showChangeLink, Confirm.showConfirmChangeLink))

  val fullV2TimeoutConfig = Some(TimeoutConfig(testTimeoutAmount, testTimeoutUrl))

  val fullV2JourneyOptions = JourneyOptions(testContinueUrl, testHomeNavRef, testAdditionalStylesheetUrl,
    testPhaseFeedbackLink, testDeskproServiceName, testShowPhaseBanner, testAlphaPhase, testShowBackButtons,
    testIncludeHmrcBranding, testUkMode, testAllowedCountryCodes, fullV2SelectPageConfig, fullV2ConfirmPageConfig,
    fullV2TimeoutConfig)

  val fullV2LanguageLabelsEn = LanguageLabels(
    appLevelLabels = fullV2AppLabels,
    selectPageLabels = fullV2SelectLabels,
    lookupPageLabels = fullV2LookupLabels,
    editPageLabels = fullV2EditLabels,
    confirmPageLabels = fullV2ConfirmLabels)

  val fullV2JourneyLabelsEn = Some(JourneyLabels(en = Some(fullV2LanguageLabelsEn)))

  val fullV2JourneyConfig = JourneyConfigV2(2, fullV2JourneyOptions, fullV2JourneyLabelsEn)

  val fullV2JourneyData = JourneyDataV2(fullV2JourneyConfig, Some(testProposedAddressSeq), Some(testAddress), Some(testAddress))

  val emptyJson: JsValue = Json.parse("{}")

  val confirmPageLabelsMinimal = ConfirmPageLabels(None, None, None, None, None, None, None, None)

  val editPageLabelsMinimal = EditPageLabels(None, None, None, None, None, None, None, None, None)

  val lookupPageLabelsMinimal = LookupPageLabels(None, None, None, None, None, None, None, None)

  val selectPageLabelsMinimal = SelectPageLabels(None, None, None, None, None, None, None)

  val appLevelLabelsMinimal = AppLevelLabels(None, None)

  val languageLabelsMinimal = LanguageLabels(None, None, None, None, None)

  val journeyLabelsMinimal = JourneyLabels(None, None)

  val timeoutConfigLessThanMinJson: JsValue = Json.parse("""{"timeoutAmount":119, "timeoutUrl": "testTimeoutUrl"}""")
  val timeoutConfigMissingAmountJson: JsValue = Json.parse("""{"timeoutUrl": "testTimeoutUrl"}""")
  val timeoutConfigMissingUrlJson: JsValue = Json.parse("""{"timeoutAmount":120}""")

  val confirmPageConfigMinimal = ConfirmPageConfig(None, None, None, None)

  val selectPageConfigMinimal = SelectPageConfig(None, None)

  val journeyOptionsMinimal = JourneyOptions("testUrl", None, None, None, None, None, None, None, None, None, None, None, None, None)
  val journeyOptionsMinimalJson: JsValue = Json.parse("""{"continueUrl":"testUrl"}""")

  val journeyConfigV2 = JourneyConfigV2(2, journeyOptionsMinimal, Some(journeyLabelsMinimal))
  val journeyConfigV2Json: JsValue = Json.parse(s"""{"version":2, "options":$journeyOptionsMinimalJson, "labels":$emptyJson}""")

  val journeyConfigV2Minimal = JourneyConfigV2(2, journeyOptionsMinimal, None)
  val journeyConfigV2MinimalJson: JsValue = Json.parse(s"""{"version":2, "options":$journeyOptionsMinimalJson}""")
  val journeyConfigV2MissingVersionJson: JsValue = Json.parse(s"""{"options":$journeyOptionsMinimalJson}""")
  val journeyConfigV2MissingConfigJson: JsValue = Json.parse(s"""{"version":2}""")

  val journeyDataV2Full = JourneyDataV2(
    config = JourneyConfigV2(
      version = 2,
      options = JourneyOptions(
        continueUrl = "testContinueUrl",
        homeNavHref = Some("tesNavtHref"),
        additionalStylesheetUrl = Some("testStylesheetUrl"),
        phaseFeedbackLink = Some("testFeedbackLink"),
        deskProServiceName = Some("testDeskproName"),
        showPhaseBanner = Some(true),
        alphaPhase = Some(true),
        showBackButtons = Some(true),
        includeHMRCBranding = Some(true),
        ukMode = Some(true),
        allowedCountryCodes = Some(Set("UK", "FR")),
        selectPageConfig = Some(SelectPageConfig(
          proposalListLimit = Some(30),
          showSearchAgainLink = Some(true)
        )),
        confirmPageConfig = Some(ConfirmPageConfig(
          showSearchAgainLink = Some(true),
          showSubHeadingAndInfo = Some(true),
          showChangeLink = Some(true),
          showConfirmChangeText = Some(true)
        )),
        timeoutConfig = Some(TimeoutConfig(
          timeoutAmount = 120,
          timeoutUrl = "testTimeoutUrl"
        ))
      ),
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
            heading = Some("enLookupPageHeading"),
            filterLabel = Some("enFilterLabel"),
            postcodeLabel = Some("enPostcodeLabel"),
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
            heading = Some("cyLookupPageHeading"),
            filterLabel = Some("cyFilterLabel"),
            postcodeLabel = Some("cyPostcodeLabel"),
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
      |         "homeNavHref":"tesNavtHref",
      |         "additionalStylesheetUrl":"testStylesheetUrl",
      |         "phaseFeedbackLink":"testFeedbackLink",
      |         "deskProServiceName":"testDeskproName",
      |         "showPhaseBanner":true,
      |         "alphaPhase":true,
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
      |            "timeoutUrl":"testTimeoutUrl"
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
      |               "heading":"enLookupPageHeading",
      |               "filterLabel":"enFilterLabel",
      |               "postcodeLabel":"enPostcodeLabel",
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
      |               "heading":"cyLookupPageHeading",
      |               "filterLabel":"cyFilterLabel",
      |               "postcodeLabel":"cyPostcodeLabel",
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
      options = JourneyOptions(
        continueUrl = "testContinueUrl"
      )
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
    """.stripMargin)

  val testId = UUID.randomUUID().toString

  val testAppLevelJourneyConfigV2 =
    JourneyDataV2(
      config = JourneyConfigV2(
        version = 2,
        options = JourneyOptions(
          continueUrl = "testContinueUrl",
          homeNavHref = Some("testNavHref"),
          additionalStylesheetUrl = Some("testStylesheetUrl"),
          phaseFeedbackLink = None,
          deskProServiceName = Some("testDeskproName"),
          showPhaseBanner = Some(true),
          alphaPhase = Some(true),
          showBackButtons = None,
          includeHMRCBranding = Some(true),
          ukMode = None,
          allowedCountryCodes = None,
          selectPageConfig = None,
          confirmPageConfig = None,
          timeoutConfig = Some(TimeoutConfig(
            timeoutAmount = 120,
            timeoutUrl = "testTimeoutUrl"
          ))
        ),
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
}
