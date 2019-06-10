package itutil.config

import java.util.UUID

import model._
import play.api.libs.json._
import uk.gov.hmrc.address.v2.Country

object IntegrationTestConstants {
  val testJourneyId = "Jid123"
  val testCsrfToken = () => UUID.randomUUID().toString

  val testPostCode = "AB11 1AB"
  val testFilterValue = "bar"
  val testAuditRef = "auditRef"
  val testAddressId = Some("addressId")
  val testNonUKAddress = ConfirmableAddressDetails(Some(List("1 High Street", "Telford")), Some(testPostCode), Some(Country("FR", "France")))
  val testFullNonUKAddress = ConfirmableAddressDetails(Some(List("1 High Street", "Line 2", "Line 3", "Telford")), Some(testPostCode), Some(Country("FR", "France")))
  val testUKAddress = ConfirmableAddressDetails(Some(List("1 High Street", "Telford")), Some(testPostCode), Some(Country("GB", "United Kingdom")))
  val testConfirmedAddress = ConfirmableAddress(testAuditRef, testAddressId, testNonUKAddress)
  val testFullNonUKConfirmedAddress = ConfirmableAddress(testAuditRef, testAddressId, testFullNonUKAddress)

  val journeyDataV1FullJson: JsValue = Json.parse(
    """
      |{
      |   "config":{
      |      "continueUrl":"continueUrl",
      |      "lookupPage":{
      |         "title":"lookupTitle",
      |         "heading":"lookupHeading",
      |         "filterLabel":"filterLabel",
      |         "postcodeLabel":"postcodeLabel",
      |         "submitLabel":"lookupSubmitLabel",
      |         "resultLimitExceededMessage":"resultsLimitExceeded",
      |         "noResultsFoundMessage":"noResultsFound",
      |         "manualAddressLinkText":"enterAddressManually"
      |      },
      |      "selectPage":{
      |         "title":"selectTitle",
      |         "heading":"selectHeading",
      |         "headingWithPostcode":"selectHeadingWithPostcode",
      |         "proposalListLabel":"proposalListLabel",
      |         "submitLabel":"selectSubmitLabel",
      |         "proposalListLimit":50,
      |         "showSearchAgainLink":true,
      |         "searchAgainLinkText":"selectSearchAgainLinkText",
      |         "editAddressLinkText":"editAddressLinkText"
      |      },
      |      "confirmPage":{
      |         "title":"confirmTitle",
      |         "heading":"confirmHeading",
      |         "showSubHeadingAndInfo":true,
      |         "infoSubheading":"infoSubHeading",
      |         "infoMessage":"infoMessage",
      |         "submitLabel":"confirmSubmitLabel",
      |         "showSearchAgainLink":true,
      |         "searchAgainLinkText":"confirmSearchAgainLinkText",
      |         "showChangeLink":true,
      |         "changeLinkText":"changeLinkText",
      |         "showConfirmChangeText":true,
      |         "confirmChangeText":"confirmChangeText"
      |      },
      |      "editPage":{
      |         "title":"editTitle",
      |         "heading":"editHeading",
      |         "line1Label":"editLine1",
      |         "line2Label":"editLine2",
      |         "line3Label":"editLine3",
      |         "townLabel":"editLine4",
      |         "postcodeLabel":"editPostcode",
      |         "countryLabel":"editCountry",
      |         "submitLabel":"editSubmit"
      |      },
      |      "homeNavHref":"homeNavRef",
      |      "navTitle":"navTitle",
      |      "additionalStylesheetUrl":"additionalStylesheetUrl",
      |      "showPhaseBanner":true,
      |      "alphaPhase":true,
      |      "phaseFeedbackLink":"phaseFeedbackLink",
      |      "phaseBannerHtml":"phaseBannerHtml",
      |      "showBackButtons":true,
      |      "includeHMRCBranding":true,
      |      "deskProServiceName":"deskproServiceName",
      |      "allowedCountryCodes":[
      |         "GB",
      |         "UK"
      |      ],
      |      "timeout":{
      |         "timeoutAmount":120,
      |         "timeoutUrl":"timeoutUrl"
      |      },
      |      "ukMode":true
      |   },
      |   "proposals":[
      |      {
      |         "addressId":"1",
      |         "postcode":"TF1 1NT",
      |         "lines":[
      |            "1 High Street",
      |            "Line 2",
      |            "Line 3"
      |         ],
      |         "town":"Telford",
      |         "county":"United Kingdom",
      |         "country":{
      |            "code":"GB",
      |            "name":"United Kingdom"
      |         }
      |      },
      |      {
      |         "addressId":"2",
      |         "postcode":"TF2 2NT",
      |         "lines":[
      |            "2 High Street",
      |            "Line2",
      |            "Line3"
      |         ],
      |         "town":"Shrewsbury",
      |         "county":"United Kingdom",
      |         "country":{
      |            "code":"GB",
      |            "name":"United Kingdom"
      |         }
      |      }
      |   ],
      |   "selectedAddress":{
      |      "auditRef":"Jid123",
      |      "id":"1",
      |      "address":{
      |         "lines":[
      |            "1 High Street",
      |            "Line 2",
      |            "Line 3"
      |         ],
      |         "postcode":"TF1 1NT",
      |         "country":{
      |            "code":"UK",
      |            "name":"United Kingdom"
      |         }
      |      }
      |   },
      |   "confirmedAddress":{
      |      "auditRef":"Jid123",
      |      "id":"1",
      |      "address":{
      |         "lines":[
      |            "1 High Street",
      |            "Line 2",
      |            "Line 3"
      |         ],
      |         "postcode":"TF1 1NT",
      |         "country":{
      |            "code":"UK",
      |            "name":"United Kingdom"
      |         }
      |      }
      |   }
      |}
    """.stripMargin)

  lazy val journeyDataV1Full: JourneyData = journeyDataV1FullJson.as[JourneyData]

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
  lazy val journeyDataV2Full: JourneyDataV2 = journeyDataV2FullJson.as[JourneyDataV2]

  val testJourneyDataWithMinimalJourneyConfig = JourneyData(JourneyConfig(continueUrl = "Aurl"))
  val testConfigWithNonUKAddress = testJourneyDataWithMinimalJourneyConfig.copy(selectedAddress = Some(ConfirmableAddress(testAuditRef, testAddressId, testNonUKAddress)))
  val testConfigWithFullNonUKAddress = testJourneyDataWithMinimalJourneyConfig.copy(selectedAddress = Some(ConfirmableAddress(testAuditRef, testAddressId, testFullNonUKAddress)))
  val testConfigWithUKAddress = testJourneyDataWithMinimalJourneyConfig.copy(selectedAddress = Some(ConfirmableAddress(testAuditRef, testAddressId, testUKAddress)))
  val testConfigWithoutAddress = testJourneyDataWithMinimalJourneyConfig.copy(selectedAddress = None)
  val testConfigDefaultAsJson = Json.toJson(testJourneyDataWithMinimalJourneyConfig).as[JsObject]
  val testConfigWithoutAddressAsJson = Json.toJson(testConfigWithoutAddress).as[JsObject]
  val testConfigNotUkMode = testJourneyDataWithMinimalJourneyConfig.config.copy(ukMode = Some(false))
  val testConfigNotUkModeCustomEditConfig = testJourneyDataWithMinimalJourneyConfig.config.copy(ukMode = Some(false),
          editPage = Some(EditPage(Some("Custom Title"),
                     Some("Custom Heading"),
                     Some("Custom Line1"),
                     Some("Custom Line2"),
                     Some("Custom Line3"),
                     Some("Custom Town"),
                     Some("Custom Postcode"),
                     Some("Custom Country"),
                     Some("Custom Continue")
          )))

  val testConfigWithAddressNotUkMode = testConfigWithFullNonUKAddress.copy(config = testConfigNotUkMode)
  val testConfigWithAddressNotUkModeCustomEditConfig = testConfigWithFullNonUKAddress.copy(config = testConfigNotUkModeCustomEditConfig)

  val testConfigWithAddressNotUkModeAsJson = Json.toJson(testConfigWithAddressNotUkMode).as[JsObject]
  val testConfigDefaultWithResultsLimitAsJson = Json.toJson(JourneyData(JourneyConfig(continueUrl = "A url", selectPage = Some(SelectPage(proposalListLimit = Some(50)))))).as[JsObject]
  val testConfigWithAddressNotUkModeCustomEditConfigAsJson = Json.toJson(testConfigWithAddressNotUkModeCustomEditConfig).as[JsObject]

  val fullLookupPageConfig = LookupPage(
    title = Some("lookup-title"),
    heading = Some("lookup-heading"),
    filterLabel = Some("lookup-filterLabel"),
    postcodeLabel = Some("lookup-postcodeLabel"),
    submitLabel = Some("lookup-submitLabel"),
    resultLimitExceededMessage = Some("lookup-resultLimitExceededMessage"),
    noResultsFoundMessage = Some("lookup-noResultsFoundMessage"),
    manualAddressLinkText = Some("lookup-manualAddressLinkText")
  )

  val testLookupConfig = Json.toJson(JourneyData(JourneyConfig(continueUrl = "A url", lookupPage = Some(fullLookupPageConfig)))).as[JsObject]

  val testLookupConfigNoBackButtons = Json.toJson(
    JourneyData(
      JourneyConfig(continueUrl = "A url", lookupPage = Some(fullLookupPageConfig), showBackButtons = Some(false))
    )
  ).as[JsObject]

  val fullSelectPageConfig = SelectPage(
    title = Some("select-title"),
    heading = Some("select-heading"),
    headingWithPostcode = Some("select-headingWithPostcode"),
    proposalListLabel = Some("select-proposalListLabel"),
    submitLabel = Some("select-submitLabel"),
    proposalListLimit = Some(50),
    showSearchAgainLink = Some(true),
    searchAgainLinkText = Some("select-searchAgainLinkText"),
    editAddressLinkText = Some("select-editAddressLinkText")
  )
  val testConfigSelectPageAsJson = Json.toJson(JourneyData(JourneyConfig(continueUrl = "A url", selectPage = Some(fullSelectPageConfig)))).as[JsObject]

  val testSelectConfigNoBackButtons = Json.toJson(
    JourneyData(
      JourneyConfig(continueUrl = "A url", selectPage = Some(fullSelectPageConfig), showBackButtons = Some(false))
    )
  ).as[JsObject]

  val fullConfirmPageConfig = ConfirmPage(
    title = Some("confirm-title"),
    heading = Some("confirm-heading"),
    showSubHeadingAndInfo = Some(true),
    infoSubheading = Some("confirm-infoSubheading"),
    infoMessage = Some("confirm-infoMessage"),
    submitLabel = Some("confirm-submitLabel"),
    showSearchAgainLink = Some(true),
    showConfirmChangeText = Some(true),
    searchAgainLinkText = Some("confirm-searchAgainLinkText"),
    showChangeLink = Some(true),
    changeLinkText = Some("confirm-changeLinkText"),
    confirmChangeText = Some("confirm-confirmChangeText")
  )

  val fullEditPageConfig = EditPage(
    title = Some("edit-title"),
    heading = Some("edit-heading"),
    line1Label = Some("edit-line1Label"),
    line2Label = Some("edit-line2Label"),
    line3Label = Some("edit-line3Label"),
    townLabel = Some("edit-townLabel"),
    postcodeLabel = Some("edit-postcodeLabel"),
    countryLabel = Some("edit-countryLabel"),
    submitLabel = Some("edit-submitLabel")
  )

  def fullDefaultJourneyConfigModelWithAllBooleansSet(allBooleanSetAndAppropriateOptions: Boolean = true) = {

    def returnNoneOrConfig[A](configOption: Option[A]) = if (allBooleanSetAndAppropriateOptions) configOption else Option.empty[A]

    JourneyConfig(
      continueUrl = "continueUrl",
      lookupPage = Some(fullLookupPageConfig),
      selectPage = Some(fullSelectPageConfig.copy(showSearchAgainLink = Some(allBooleanSetAndAppropriateOptions))),
      confirmPage = Some(fullConfirmPageConfig.copy(showConfirmChangeText = Some(allBooleanSetAndAppropriateOptions), showChangeLink = Some(allBooleanSetAndAppropriateOptions), showSearchAgainLink = Some(allBooleanSetAndAppropriateOptions), showSubHeadingAndInfo = Some(allBooleanSetAndAppropriateOptions))),
      editPage = Some(fullEditPageConfig),
      homeNavHref = returnNoneOrConfig(Some("HOME_NAV_REF")),
      navTitle = returnNoneOrConfig(Some("NAV_TITLE")),
      additionalStylesheetUrl = returnNoneOrConfig(Some("ADDITIONAL_STYLESHEET_URL")),
      showPhaseBanner = Some(allBooleanSetAndAppropriateOptions),
      alphaPhase = Some(allBooleanSetAndAppropriateOptions),
      phaseFeedbackLink = returnNoneOrConfig(Some("PHASE_FEEDBACK_LINK")),
      phaseBannerHtml = returnNoneOrConfig(Some("PHASE_BANNER_HTML")),
      showBackButtons = returnNoneOrConfig(Some(allBooleanSetAndAppropriateOptions)),
      includeHMRCBranding = Some(allBooleanSetAndAppropriateOptions),
      deskProServiceName = returnNoneOrConfig(Some("DESKPRO_SERVICE_NAME")),
      allowedCountryCodes = returnNoneOrConfig(Some(Set("GB", "AB", "CD"))),
      timeout = returnNoneOrConfig(Some(Timeout(
        timeoutAmount = 120,
        timeoutUrl = "TIMEOUT_URL"
      ))),
      ukMode = Some(allBooleanSetAndAppropriateOptions)
    )
  }

  def journeyDataWithSelectedAddressJson(
                                          journeyConfig: JourneyConfig = fullDefaultJourneyConfigModelWithAllBooleansSet(true),
                                          selectedAddress: ConfirmableAddressDetails = testNonUKAddress) = Json.toJson(
    JourneyData(
      journeyConfig,
      selectedAddress = Some(ConfirmableAddress(testAuditRef, testAddressId, selectedAddress))
    )).as[JsObject]
}


object AddressRecordConstants {
  val addressRecordSeqJson: JsValue = Json.arr(
    addressRecordJson(
      id = "id1",
      lines = Seq("line1", "line2"),
      town = "town1",
      postcode = "AB11 1AB",
      country = Country("GB", "Great Britain")
    ),
    addressRecordJson(
      id = "id2",
      lines = Seq("line3", "line4"),
      town = "town2",
      postcode = "ZZ1 1ZZ",
      country = Country("UK", "United Kingdom")
    )
  )

  def addressResultsListBySize(numberOfRepeats: Int): JsValue = {
    Json.toJson(
      (1 to numberOfRepeats) map {
        _ =>
          addressRecordJson(
            id = s"id-${UUID.randomUUID()}",
            lines = Seq("line1", "line2"),
            town = "town1",
            postcode = "AB1 1AB",
            country = Country("GB", "Great Britain")
          )
      } toList
    )
  }
  def addressRecordJson(id: String, lines: Seq[String], town: String, postcode: String, country: Country): JsValue = Json.obj(
    "id" -> id,
    "upurn" -> "",
    "address" -> Json.obj(
      "lines" -> Json.toJson(lines),
      "town" -> town,
      "postcode" -> postcode,
      "subdivision" -> Json.obj(
        "code" -> "GC",
        "name" -> "United Kingdom"
      ),
      "country" -> Json.obj(
        "code" -> country.code,
        "name" -> country.name
      )
    ),
    "language" -> "language2",
    "localCustodian" -> Json.obj(
      "code" -> 1,
      "name" -> "Custodian"
    ),
    "location" -> Json.toJson(Seq(1.2, 2.1)),
    "blpuState" -> "blpuState",
    "logicalState" -> Json.toJson("logicalState"),
    "streetClassification" -> "streetClassification"
  )

  def addressRecordJsonList(numberOfRepeats: Int): JsValue = {
    Json.toJson(
      (1 to numberOfRepeats) map {
        _ =>
          addressRecordJson(
            id = s"id-${UUID.randomUUID().toString}",
            lines = Seq("line1", "line2"),
            town = "town1",
            postcode = "AB11 1AB",
            country = Country("GB", "Great Britain")
          )
      } toList
    )
  }

}

object PageElementConstants {

  object LookupPage {
    val postcodeId = "postcode"
    val filterId = "filter"
    val manualAddressLink = "manualAddress"
  }

  object EditPage {
    val ukEditId = "ukEdit"
    val nonUkEditId = "nonUkEdit"
  }

  object SelectPage {
    val addressId = "addressId"
  }

}
