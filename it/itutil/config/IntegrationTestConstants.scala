package itutil.config

import java.util.UUID

import itutil.config.IntegrationTestConstants._
import model._
import play.api.libs.json._
import address.v2.Country

object IntegrationTestConstants {
  val testApiVersion = 2
  val testJourneyId = "Jid123"
  val testCsrfToken = () => UUID.randomUUID().toString


  val testContinueUrl = "test-continue-url"
  val testPostCode = "AB11 1AB"
  val testFilterValue = "bar"
  val testAuditRef = testJourneyId
  val testAddressIdRaw = "addressId"
  val testAddressId = Some(testAddressIdRaw)
  val testAddressLine1 = "1 High Street"
  val testAddressLine2 = "Line 2"
  val testAddressLine3 = "Line 3"
  val testAddressTown = "Telford"
  val testCountry = Country("GB", "United Kingdom")
  val useWelshCookieName = "Use-Welsh"

  val testNonUKAddress = ConfirmableAddressDetails(Some(List(testAddressLine1, testAddressTown)), Some(testPostCode), Some(Country("FR", "France")))
  val testFullNonUKAddress = ConfirmableAddressDetails(Some(List(testAddressLine1, testAddressLine2, testAddressLine3, testAddressTown)), Some(testPostCode), Some(Country("FR", "France")))
  val testUKAddress = ConfirmableAddressDetails(Some(List(testAddressLine1, testAddressLine2, testAddressTown)), Some(testPostCode), Some(Country("GB", "United Kingdom")))
  val testConfirmedAddress = ConfirmableAddress(testAuditRef, testAddressId, testUKAddress)
  val testFullNonUKConfirmedAddress = ConfirmableAddress(testAuditRef, testAddressId, testFullNonUKAddress)

  def testProposedAddresses(amount: Int): Seq[ProposedAddress] = (1 to amount) map { _ =>
    ProposedAddress(
      addressId = testAddressIdRaw,
      postcode = testPostCode,
      lines = List(testAddressLine1, testAddressLine2),
      town = testAddressTown,
      country = testCountry
    )
  }

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
      |         "timeoutUrl":"timeoutUrl",
      |         "timeoutKeepAliveUrl":"testTimeoutKeepAliveUrl"
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
      |            "timeoutUrl":"testTimeoutUrl",
      |            "timeoutKeepAliveUrl":"testTimeoutKeepAliveUrl"
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
  lazy val journeyDataV2Full: JourneyDataV2 = journeyDataV2FullJson.as[JourneyDataV2]

  val testJourneyDataWithMinimalJourneyConfigV2 = JourneyDataV2(config = JourneyConfigV2(2, JourneyOptions(continueUrl = testContinueUrl)))
  val testConfigWithFullNonUKAddressV2 = testJourneyDataWithMinimalJourneyConfigV2.copy(selectedAddress = Some(testFullNonUKConfirmedAddress))
  val testConfigNotUkModeV2 = testJourneyDataWithMinimalJourneyConfigV2.config.copy(options = JourneyOptions(ukMode = Some(false), continueUrl = testContinueUrl))

  val testConfigWithAddressNotUkModeV2 = testConfigWithFullNonUKAddressV2.copy(config = testConfigNotUkModeV2)

  val testConfigwithAddressNotUkModeAsJsonV2 = Json.toJson(testConfigWithAddressNotUkModeV2)
  val testConfigDefaultAsJsonV2 = Json.toJson(testJourneyDataWithMinimalJourneyConfigV2).as[JsObject]

  val fullSelectPageConfigV2 = SelectPageConfig(
    proposalListLimit = Some(50),
    showSearchAgainLink = Some(true)
  )

  val fullConfirmPageConfigV2 = ConfirmPageConfig(
    showSubHeadingAndInfo = Some(true),
    showSearchAgainLink = Some(true),
    showConfirmChangeText = Some(true),
    showChangeLink = Some(true)
  )

  val testMinimalLevelJourneyConfigV2 = Json.toJson(JourneyDataV2(
    config = JourneyConfigV2(
      version = 2,
      options = JourneyOptions(
        continueUrl = "testContinueUrl"
      ),
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
  )).as[JsValue]

  val testDefaultLookupPageJourneyConfigV2 = Json.toJson(JourneyDataV2(
    config = JourneyConfigV2(
      version = 2,
      options = JourneyOptions(
        continueUrl = "testContinueUrl",
        showBackButtons = Some(false),
        homeNavHref = Some("NAV_TITLE")
      ),
      labels = Some(JourneyLabels(
        en = Some(LanguageLabels(
          appLevelLabels = Some(AppLevelLabels(
            navTitle = Some("NAV_TITLE")
          )),
          selectPageLabels = None,
          lookupPageLabels = None,
          editPageLabels = None,
          confirmPageLabels = None
        )),
        cy = None
      ))
    )
  )).as[JsValue]

  def testCustomLookupPageJourneyConfigV2Json = Json.toJson(testCustomLookupPageJourneyConfigV2).as[JsValue]

  def testCustomLookupPageJourneyConfigV2 = JourneyDataV2(
    config = JourneyConfigV2(
      version = 2,
      options = JourneyOptions(
        continueUrl = "testContinueUrl",
        homeNavHref = Some("NAV_TITLE"),
        additionalStylesheetUrl = Some("ADDITIONAL_STYLESHEET_URL"),
        phaseFeedbackLink = Some("testFeedbackLink"),
        deskProServiceName = Some("DESKPRO_SERVICE_NAME"),
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
          timeoutUrl = "TIMEOUT_URL",
          timeoutKeepAliveUrl = Some("KEEP_ALIVE_URL")
        ))
      ),
      labels = Some(JourneyLabels(
        en = Some(LanguageLabels(
          appLevelLabels = Some(AppLevelLabels(
            navTitle = Some("NAV_TITLE"),
            phaseBannerHtml = Some("PHASE_BANNER_HTML")
          )),
          selectPageLabels = None,
          lookupPageLabels = Some(LookupPageLabels(
            title = Some("lookup-title"),
            heading = Some("lookup-heading"),
            filterLabel = Some("lookup-filterLabel"),
            postcodeLabel = Some("lookup-postcodeLabel"),
            submitLabel = Some("lookup-submitLabel"),
            resultLimitExceededMessage = Some("lookup-resultLimitExceededMessage"),
            noResultsFoundMessage = Some("lookup-noResultsFoundMessage"),
            manualAddressLinkText = Some("lookup-manualAddressLinkText")
          )),
          editPageLabels = None,
          confirmPageLabels = None
        )),
        cy = None
      ))
    )
  )

  def testOtherCustomLookupPageJourneyConfigV2Json = Json.toJson(testOtherCustomLookupPageJourneyConfigV2).as[JsValue]

  def testOtherCustomLookupPageJourneyConfigV2 = JourneyDataV2(
    config = JourneyConfigV2(
      version = 2,
      options = JourneyOptions(
        continueUrl = "testContinueUrl",
        phaseFeedbackLink = Some("testFeedbackLink"),
        showPhaseBanner = Some(false),
        alphaPhase = Some(false),
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
        ))
      ),
      labels = Some(JourneyLabels(
        en = Some(LanguageLabels(
          appLevelLabels = None,
          selectPageLabels = None,
          lookupPageLabels = Some(LookupPageLabels(
            title = Some("lookup-title"),
            heading = Some("lookup-heading"),
            filterLabel = Some("lookup-filterLabel"),
            postcodeLabel = Some("lookup-postcodeLabel"),
            submitLabel = Some("lookup-submitLabel"),
            resultLimitExceededMessage = Some("lookup-resultLimitExceededMessage"),
            noResultsFoundMessage = Some("lookup-noResultsFoundMessage"),
            manualAddressLinkText = Some("lookup-manualAddressLinkText")
          )),
          editPageLabels = None,
          confirmPageLabels = None
        )),
        cy = None
      ))
    ))

  def journeyV2Labels(heading: Option[String] = Some("confirm-heading")): Option[JourneyLabels] = {
    Some(JourneyLabels(
      Some(LanguageLabels(
        confirmPageLabels = Some(ConfirmPageLabels(
          title = Some("confirm-title"),
          heading = heading,
          infoSubheading = Some("confirm-infoSubheading"),
          infoMessage = Some("confirm-infoMessage"),
          submitLabel = Some("confirm-submitLabel"),
          searchAgainLinkText = Some("confirm-searchAgainLinkText"),
          changeLinkText = Some("confirm-changeLinkText"),
          confirmChangeText = Some("confirm-confirmChangeText")
        ))
      ))
    ))
  }

  def fullDefaultJourneyConfigModelV2WithAllBooleansSet(allBooleanSetAndAppropriateOptions: Boolean = true, isWelsh: Boolean = false) = {

    def returnNoneOrConfig[A](configOption: Option[A]) = if (allBooleanSetAndAppropriateOptions) configOption else Option.empty[A]

    JourneyConfigV2(
      2,
      options = JourneyOptions(
        continueUrl = "continueUrl",
        selectPageConfig = Some(fullSelectPageConfigV2.copy(showSearchAgainLink = Some(allBooleanSetAndAppropriateOptions))),
        confirmPageConfig = Some(fullConfirmPageConfigV2.copy(showConfirmChangeText = Some(allBooleanSetAndAppropriateOptions), showChangeLink = Some(allBooleanSetAndAppropriateOptions), showSearchAgainLink = Some(allBooleanSetAndAppropriateOptions), showSubHeadingAndInfo = Some(allBooleanSetAndAppropriateOptions))),
        homeNavHref = returnNoneOrConfig(Some("HOME_NAV_REF")),
        additionalStylesheetUrl = returnNoneOrConfig(Some("ADDITIONAL_STYLESHEET_URL")),
        showPhaseBanner = Some(allBooleanSetAndAppropriateOptions),
        alphaPhase = Some(allBooleanSetAndAppropriateOptions),
        phaseFeedbackLink = returnNoneOrConfig(Some("PHASE_FEEDBACK_LINK")),
        showBackButtons = returnNoneOrConfig(Some(allBooleanSetAndAppropriateOptions)),
        includeHMRCBranding = Some(allBooleanSetAndAppropriateOptions),
        deskProServiceName = returnNoneOrConfig(Some("DESKPRO_SERVICE_NAME")),
        allowedCountryCodes = returnNoneOrConfig(Some(Set("GB", "AB", "CD"))),
        timeoutConfig = returnNoneOrConfig(Some(TimeoutConfig(
          timeoutAmount = 120,
          timeoutUrl = "TIMEOUT_URL",
          timeoutKeepAliveUrl = Some("KEEP_ALIVE_URL")
        ))),
        ukMode = Some(allBooleanSetAndAppropriateOptions)
      ),
      Some(JourneyLabels(
        en = Some(LanguageLabels(
          confirmPageLabels = Some(ConfirmPageLabels(
            title = Some("confirm-title"),
            heading = Some("confirm-heading"),
            infoSubheading = Some("confirm-infoSubheading"),
            infoMessage = Some("confirm-infoMessage"),
            submitLabel = Some("confirm-submitLabel"),
            searchAgainLinkText = Some("confirm-searchAgainLinkText"),
            changeLinkText = Some("confirm-changeLinkText"),
            confirmChangeText = Some("confirm-confirmChangeText")
          )),
          appLevelLabels = Some(AppLevelLabels(
            navTitle = returnNoneOrConfig(Some("NAV_TITLE")),
            phaseBannerHtml = returnNoneOrConfig(Some("PHASE_BANNER_HTML"))))
        )),
        cy = if (isWelsh) {
          Some(LanguageLabels(
            confirmPageLabels = Some(ConfirmPageLabels(
              title = Some("cy-confirm-title"),
              heading = Some("cy-confirm-heading"),
              infoSubheading = Some("cy-confirm-infoSubheading"),
              infoMessage = Some("cy-confirm-infoMessage"),
              submitLabel = Some("cy-confirm-submitLabel"),
              searchAgainLinkText = Some("cy-confirm-searchAgainLinkText"),
              changeLinkText = Some("cy-confirm-changeLinkText"),
              confirmChangeText = Some("cy-confirm-confirmChangeText")
            )),
            appLevelLabels = Some(AppLevelLabels(
              navTitle = returnNoneOrConfig(Some("cy-NAV_TITLE")),
              phaseBannerHtml = returnNoneOrConfig(Some("cy-PHASE_BANNER_HTML"))))
          ))
        } else None
      ))
    )
  }

  def journeyDataV2WithNoSelectedAddressJson(journeyConfig: JourneyConfigV2 = fullDefaultJourneyConfigModelV2WithAllBooleansSet(true)) =
    Json.toJson(
      JourneyDataV2(
        journeyConfig
      ))

  def journeyDataV2WithSelectedAddressJson(journeyConfigV2: JourneyConfigV2 = JourneyConfigV2(2, JourneyOptions(testContinueUrl, ukMode = Some(false))),
                                           selectedAddress: ConfirmableAddressDetails = testFullNonUKAddress) =
    Json.toJson(
      JourneyDataV2(
        journeyConfigV2,
        selectedAddress = Some(ConfirmableAddress(testAuditRef, testAddressId, selectedAddress))
      ))

  val journeyDataV2ResultLimitUkMode: JourneyDataV2 = JourneyDataV2(JourneyConfigV2(2, JourneyOptions(testContinueUrl, selectPageConfig = Some(SelectPageConfig(proposalListLimit = Some(50))), ukMode = Some(true))))
  val journeyDataV2MinimalUkMode: JourneyDataV2 = JourneyDataV2(JourneyConfigV2(2, JourneyOptions(testContinueUrl, ukMode = Some(true))))
  val journeyDataV2ResultLimit: JourneyDataV2 = JourneyDataV2(JourneyConfigV2(2, JourneyOptions(testContinueUrl, selectPageConfig = Some(SelectPageConfig(proposalListLimit = Some(50))))))
  val journeyDataV2Minimal: JourneyDataV2 = JourneyDataV2(JourneyConfigV2(2, JourneyOptions(testContinueUrl)))

  def journeyDataV2EditLabels(ukModeBool: Option[Boolean]): JourneyDataV2 = JourneyDataV2(JourneyConfigV2(
    2,
    JourneyOptions(
      testContinueUrl, ukMode = ukModeBool
    ),
    Some(JourneyLabels(
      Some(LanguageLabels(
        editPageLabels = Some(EditPageLabels(
          title = Some("edit-title"),
          heading = Some("edit-heading"),
          line1Label = Some("Custom Line1"),
          line2Label = Some("Custom Line2"),
          line3Label = Some("Custom Line3"),
          townLabel = Some("Custom Town"),
          postcodeLabel = Some("Custom Postcode"),
          countryLabel = Some("Custom Country"),
          submitLabel = Some("edit-submitLabel")
        ))
      ))
    ))
  ))

  val journeyDataV2SelectLabels: JourneyDataV2 = JourneyDataV2(
    JourneyConfigV2(
      2,
      JourneyOptions(
        testContinueUrl,
        selectPageConfig = Some(SelectPageConfig(
          proposalListLimit = Some(50)
        ))
      ),
      Some(JourneyLabels(
        Some(LanguageLabels(
          selectPageLabels = Some(SelectPageLabels(
            title = Some("select-title"),
            heading = Some("select-heading"),
            headingWithPostcode = Some("select-headingWithPostcode"),
            proposalListLabel = Some("select-proposalListLabel"),
            submitLabel = Some("select-submitLabel"),
            searchAgainLinkText = Some("select-searchAgainLinkText"),
            editAddressLinkText = Some("select-editAddressLinkText")
          ))
        ))
      ))
    )
  )

  val journeyDataV2DefaultWelshLabels: JourneyDataV2 = JourneyDataV2(
    JourneyConfigV2(
      2,
      JourneyOptions(
        testContinueUrl,
        selectPageConfig = Some(SelectPageConfig(
          proposalListLimit = Some(50)
        ))
      ),
      Some(JourneyLabels(
        None,
        cy = Some(LanguageLabels())
      ))
    )
  )

  val journeyDataV2SelectLabelsNoBack: JourneyDataV2 = journeyDataV2SelectLabels.copy(config = journeyDataV2SelectLabels.config.copy(options = journeyDataV2SelectLabels.config.options.copy(showBackButtons = Some(false))))

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

  def addressResultsModelListBySize(numberOfRepeats: Int): List[ProposedAddress] =
    (1 to numberOfRepeats).map(n =>
      ProposedAddress(
        addressId = testAddressIdRaw,
        postcode = testPostCode,
        lines = List(testAddressLine1, testAddressLine2),
        town = testAddressTown,
        country = testCountry
      ))
      .toList

  def addressResultsListBySize(numberOfRepeats: Int): JsValue = {
    Json.toJson(
      (1 to numberOfRepeats) map {
        _ =>
          addressRecordJson(
            id = s"$testAddressIdRaw",
            lines = Seq(testAddressLine1, testAddressLine2),
            town = testAddressTown,
            postcode = testPostCode,
            country = testCountry
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
        "code" -> "GB",
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
