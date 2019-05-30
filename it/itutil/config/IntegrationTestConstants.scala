package itutil.config

import java.util.UUID

import model._
import play.api.libs.json._
import uk.gov.hmrc.address.v2.LocalCustodian
import uk.gov.hmrc.address.v2.{Address, AddressRecord, Country}

object IntegrationTestConstants {
  val testJourneyId = "Jid123"
  val testCsrfToken = () => UUID.randomUUID().toString

  val testPostCode = "AB11 1AB"
  val testFilterValue = "bar"
  val testAuditRef = "auditRef"
  val testAddressId = Some("addressId")
  val testAddress = ConfirmableAddressDetails(Some(List("1 High Street", "Telford")), Some(testPostCode), Some(Country("FR", "France")))
  val testConfirmedAddress = ConfirmableAddress(testAuditRef, testAddressId, testAddress)

  val testJourneyDataWithMinimalJourneyConfig = JourneyData(JourneyConfig(continueUrl = "A url"))
  val testConfigWithAddress = testJourneyDataWithMinimalJourneyConfig.copy(selectedAddress = Some(ConfirmableAddress(testAuditRef, testAddressId, testAddress)))
  val testConfigWithoutAddress = testJourneyDataWithMinimalJourneyConfig.copy(selectedAddress = None)
  val testConfigDefaultAsJson = Json.toJson(testJourneyDataWithMinimalJourneyConfig).as[JsObject]
  val testConfigWithoutAddressAsJson = Json.toJson(testConfigWithoutAddress).as[JsObject]
  val testConfigNotUkMode = testJourneyDataWithMinimalJourneyConfig.config.copy(ukMode = Some(false))
  val testConfigWithAddressNotUkMode = testConfigWithAddress.copy(config = testConfigNotUkMode)
  val testConfigWithAddressNotUkModeAsJson = Json.toJson(testConfigWithAddressNotUkMode).as[JsObject]

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

  val fullDefaultJourneyConfigModel = JourneyConfig(
    continueUrl = "continueUrl",
    lookupPage = Some(fullLookupPageConfig),
    selectPage = Some(fullSelectPageConfig),
    confirmPage = Some(fullConfirmPageConfig),
    editPage = Some(fullEditPageConfig),
    homeNavHref = Some("HOME_NAV_REF"),
    navTitle = Some("NAV_TITLE"),
    additionalStylesheetUrl = Some("ADDITIONAL_STYLESHEET_URL"),
    showPhaseBanner = Some(true),
    alphaPhase = Some(true),
    phaseFeedbackLink = Some("PHASE_FEEDBACK_LINK"),
    phaseBannerHtml = Some("PHASE_BANNER_HTML"),
    showBackButtons = Some(true),
    includeHMRCBranding = Some(true),
    deskProServiceName = Some("DESKPRO_SERVICE_NAME"),
    allowedCountryCodes = Some(Set("GB", "AB", "CD")),
    timeout = Some(Timeout(
      timeoutAmount = 1,
      timeoutUrl = "TIMEOUT_URL"
    )),
    ukMode = Some(true)
  )
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
      "id" -> 1,
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
