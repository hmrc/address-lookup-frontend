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
  val testAddress = ConfirmableAddressDetails(Some(List("1 High Street", "Line 2","Line 3","Telford")), Some(testPostCode), Some(Country("FR", "France")))
  val testConfirmedAddress = ConfirmableAddress(testAuditRef, testAddressId, testAddress)

  val testJourneyDataWithMinimalJourneyConfig = JourneyData(JourneyConfig(continueUrl = "Aurl"))
  val testConfigWithAddress = testJourneyDataWithMinimalJourneyConfig.copy(selectedAddress = Some(ConfirmableAddress(testAuditRef, testAddressId, testAddress)))
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

  val testConfigWithAddressNotUkMode = testConfigWithAddress.copy(config = testConfigNotUkMode)
  val testConfigWithAddressNotUkModeCustomEditConfig = testConfigWithAddress.copy(config = testConfigNotUkModeCustomEditConfig)

  val testConfigWithAddressNotUkModeAsJson = Json.toJson(testConfigWithAddressNotUkMode).as[JsObject]
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
    proposalListLimit = Some(1),
    showSearchAgainLink = Some(true),
    searchAgainLinkText = Some("select-searchAgainLinkText"),
    editAddressLinkText = Some("select-editAddressLinkText")
  )

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

    def returnNoneOrConfig[A](configOption: Option[A]) = if(allBooleanSetAndAppropriateOptions) configOption else Option.empty[A]
      JourneyConfig (
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

  def journeyDataWithSelectedAddressJson(journeyConfig: JourneyConfig = fullDefaultJourneyConfigModelWithAllBooleansSet(true)) = Json.toJson(
    JourneyData(
      journeyConfig,
      selectedAddress = Some(ConfirmableAddress(testAuditRef, testAddressId, testAddress))
    )).as[JsObject]
}



object AddressRecordConstants {
  val addressRecordSeqJson: JsValue = Json.arr(
    addressRecordJson(
      id = "id1",
      lines = Seq("line1", "line2"),
      town = "town1",
      postcode = "AB1 1AB",
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

}

object PageElementConstants {
  object LookupPage {
    val postcodeId  = "postcode"
    val filterId    = "filter"
    val manualAddressLink = "manualAddress"
  }

  object EditPage {
    val ukEditId      = "ukEdit"
    val nonUkEditId   = "nonUkEdit"
  }
}
