package model

import org.scalatest.{MustMatchers, WordSpecLike}
import play.api.data.validation.ValidationError
import play.api.libs.json._

class ModelV2Spec extends WordSpecLike with MustMatchers {

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
        continueUrl = "testContinueUrl",
        None, None, None, None, None, None, None, None, None, None, None, None, None
      ),
      labels = None
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

}
