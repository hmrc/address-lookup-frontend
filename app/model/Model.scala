
package model

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import services.ForeignOfficeCountryService
import uk.gov.hmrc.address.uk.Postcode
import uk.gov.hmrc.address.v2.Country
import JourneyConfigDefaults._

case class Lookup(filter: Option[String], postcode: String)

case class Timeout(timeoutAmount: Int, timeoutUrl: String)

case class Select(addressId: String)

case class Edit(line1: String, line2: Option[String], line3: Option[String], town: String, postcode: String, countryCode: Option[String]) {

  def toConfirmableAddress(auditRef: String): ConfirmableAddress = ConfirmableAddress(
    auditRef,
    None,
    ConfirmableAddressDetails(
      Some(List(line1) ++ line2.map(_.toString).toList ++ line3.map(_.toString).toList ++ List(town)),
      Some(postcode),
      countryCode.flatMap(code => ForeignOfficeCountryService.find(code))
    )
  )

  def isValidPostcode(): Boolean = {
    countryCode.flatMap(code => Some(ForeignOfficeCountryService.GB.code != code || postcode.length == 0 || Postcode.cleanupPostcode(postcode).isDefined)).get
  }

}

object JourneyConfigDefaults {

  val CONFIRM_PAGE_TITLE = "Confirm Address"
  val CONFIRM_PAGE_HEADING = "Confirm Address"
  val CONFIRM_PAGE_INFO_SUBHEADING = "Your selected address"
  val CONFIRM_PAGE_INFO_MESSAGE_HTML = "This is how your address will look. Please double-check it and, if accurate, click on the <kbd>Confirm</kbd> button."
  val CONFIRM_PAGE_SUBMIT_LABEL = "Confirm"

  val EDIT_PAGE_TITLE = "Edit Address"
  val EDIT_PAGE_HEADING = "Edit Address"
  val EDIT_PAGE_LINE1_LABEL = "Line 1"
  val EDIT_PAGE_LINE2_LABEL = "Line 2"
  val EDIT_PAGE_LINE3_LABEL = "Line 3"
  val EDIT_PAGE_TOWN_LABEL = "Town"
  val EDIT_PAGE_POSTCODE_LABEL = "Postcode"
  val EDIT_PAGE_COUNTRY_LABEL = "Country"
  val EDIT_PAGE_SUBMIT_LABEL = "Next"

  val LOOKUP_PAGE_TITLE = "Lookup Address"
  val LOOKUP_PAGE_HEADING = "Your Address"
  val LOOKUP_PAGE_FILTER_LABEL = "Building name or number"
  val LOOKUP_PAGE_POSTCODE_LABEL = "Postcode"
  val LOOKUP_PAGE_SUBMIT_LABEL = "Find my address"
  val LOOKUP_PAGE_MANUAL_ADDRESS_LINK_TEXT = "Enter address manually"

  val SELECT_PAGE_TITLE = "Select Address"
  val SELECT_PAGE_HEADING = "Select Address"
  val SELECT_PAGE_PROPOSAL_LIST_LABEL = "Please select one of the following addresses"
  val SELECT_PAGE_SUBMIT_LABEL = "Next"

  val EDIT_LINK_TEXT = "Edit this address"
  val SEARCH_AGAIN_LINK_TEXT = "Search again"
  def defaultPhaseBannerHtml(link: String) = s"This is a new service – your <a href='$link}'>feedback</a> will help us to improve it."

}

// decorator providing default config values; genuinely optional options are not decorated, only those that are required
// but which have fallbacks so that client apps do not need to specify a value except to override the default are decorated
case class ResolvedJourneyConfig(cfg: JourneyConfig) {
  val continueUrl: String = cfg.continueUrl
  val lookupPage: ResolvedLookupPage = ResolvedLookupPage(cfg.lookupPage.getOrElse(LookupPage()))
  val selectPage: ResolvedSelectPage = ResolvedSelectPage(cfg.selectPage.getOrElse(SelectPage()))
  val confirmPage: ResolvedConfirmPage = ResolvedConfirmPage(cfg.confirmPage.getOrElse(ConfirmPage()))
  val editPage: ResolvedEditPage = ResolvedEditPage(cfg.editPage.getOrElse(EditPage()))
  val homeNavHref: String = cfg.homeNavHref.getOrElse("http://www.hmrc.gov.uk")
  val showPhaseBanner: Boolean = cfg.showPhaseBanner.getOrElse(false)
  val alphaPhase: Boolean = cfg.alphaPhase.getOrElse(false)
  val phase: String = cfg.showPhaseBanner match {
    case Some(true) => if (alphaPhase) "alpha" else "beta"
    case _ => ""
  }
  val phaseFeedbackLink: String = cfg.phaseFeedbackLink.getOrElse(s"/help/${phase}")
  val phaseBannerHtml: String = cfg.phaseBannerHtml.getOrElse(defaultPhaseBannerHtml(phaseFeedbackLink))
  val showBackButtons: Boolean = cfg.showBackButtons.getOrElse(false)
  val includeHMRCBranding: Boolean = cfg.includeHMRCBranding.getOrElse(true)
}

case class ResolvedConfirmPage(p: ConfirmPage) {
  val title: String = p.title.getOrElse(CONFIRM_PAGE_TITLE)
  val heading: String = p.heading.getOrElse(CONFIRM_PAGE_HEADING)
  val showSubHeadingAndInfo: Boolean = p.showSubHeadingAndInfo.getOrElse(false)
  val infoSubheading: String = p.infoSubheading.getOrElse(CONFIRM_PAGE_INFO_SUBHEADING)
  val infoMessage: String = p.infoMessage.getOrElse(CONFIRM_PAGE_INFO_MESSAGE_HTML)
  val submitLabel: String = p.submitLabel.getOrElse(CONFIRM_PAGE_SUBMIT_LABEL)
  val showSearchAgainLink: Boolean = p.showSearchAgainLink.getOrElse(false)
  val searchAgainLinkText: String = p.searchAgainLinkText.getOrElse(SEARCH_AGAIN_LINK_TEXT)
  val showChangeLink: Boolean = p.showChangeLink.getOrElse(false)
  val changeLinkText: String = p.changeLinkText.getOrElse(EDIT_LINK_TEXT)
}

case class ConfirmPage(title: Option[String] = None,
                       heading: Option[String] = None,
                       showSubHeadingAndInfo: Option[Boolean] = Some(false),
                       infoSubheading: Option[String] = None,
                       infoMessage: Option[String] = None,
                       submitLabel: Option[String] = None,
                       showSearchAgainLink: Option[Boolean] = Some(false),
                       searchAgainLinkText: Option[String] = None,
                       showChangeLink: Option[Boolean] = Some(false),
                       changeLinkText: Option[String] = None)

case class ResolvedLookupPage(p: LookupPage) {
  val title: String = p.title.getOrElse(LOOKUP_PAGE_TITLE)
  val heading: String = p.heading.getOrElse(LOOKUP_PAGE_HEADING)
  val filterLabel: String = p.filterLabel.getOrElse(LOOKUP_PAGE_FILTER_LABEL)
  val postcodeLabel: String = p.postcodeLabel.getOrElse(LOOKUP_PAGE_POSTCODE_LABEL)
  val submitLabel: String = p.submitLabel.getOrElse(LOOKUP_PAGE_SUBMIT_LABEL)
  // TODO
  val resultLimitExceededMessage: Option[String] = None
  val noResultsFoundMessage: Option[String] = None
  val manualAddressLinkText: String = p.manualAddressLinkText.getOrElse(LOOKUP_PAGE_MANUAL_ADDRESS_LINK_TEXT)
}

case class LookupPage(title: Option[String] = None,
                      heading: Option[String] = None,
                      filterLabel: Option[String] = None,
                      postcodeLabel: Option[String] = None,
                      submitLabel: Option[String] = None,
                      resultLimitExceededMessage: Option[String] = None,
                      noResultsFoundMessage: Option[String] = None,
                      manualAddressLinkText: Option[String] = None)

case class ResolvedSelectPage(p: SelectPage) {
  val title: String = p.title.getOrElse(SELECT_PAGE_TITLE)
  val heading: String = p.heading.getOrElse(SELECT_PAGE_HEADING)
  val proposalListLabel: String = p.proposalListLabel.getOrElse(SELECT_PAGE_PROPOSAL_LIST_LABEL)
  val submitLabel: String = p.submitLabel.getOrElse(SELECT_PAGE_SUBMIT_LABEL)
  val showSearchAgainLink: Boolean = p.showSearchAgainLink.getOrElse(false)
  val searchAgainLinkText: String = p.searchAgainLinkText.getOrElse(SEARCH_AGAIN_LINK_TEXT)
  val editAddressLinkText: String = p.editAddressLinkText.getOrElse(EDIT_LINK_TEXT)
}

case class SelectPage(title: Option[String] = None,
                      heading: Option[String] = None,
                      proposalListLabel: Option[String] = None,
                      submitLabel: Option[String] = None,
                      proposalListLimit: Option[Int] = None,
                      showSearchAgainLink: Option[Boolean] = Some(false),
                      searchAgainLinkText: Option[String] = None,
                      editAddressLinkText: Option[String] = None)

case class ResolvedEditPage(p: EditPage) {
  val title: String = p.title.getOrElse(EDIT_PAGE_TITLE)
  val heading: String = p.heading.getOrElse(EDIT_PAGE_HEADING)
  val line1Label: String = p.line1Label.getOrElse(EDIT_PAGE_LINE1_LABEL)
  val line2Label: String = p.line2Label.getOrElse(EDIT_PAGE_LINE2_LABEL)
  val line3Label: String = p.line3Label.getOrElse(EDIT_PAGE_LINE3_LABEL)
  val townLabel: String = p.townLabel.getOrElse(EDIT_PAGE_TOWN_LABEL)
  val postcodeLabel: String = p.postcodeLabel.getOrElse(EDIT_PAGE_POSTCODE_LABEL)
  val countryLabel: String = p.countryLabel.getOrElse(EDIT_PAGE_COUNTRY_LABEL)
  val submitLabel: String = p.submitLabel.getOrElse(EDIT_PAGE_SUBMIT_LABEL)
  val showSearchAgainLink: Boolean = p.showSearchAgainLink.getOrElse(false)
  val searchAgainLinkText: String = p.searchAgainLinkText.getOrElse(SEARCH_AGAIN_LINK_TEXT)
}

case class EditPage(title: Option[String] = None,
                    heading: Option[String] = None,
                    line1Label: Option[String] = None,
                    line2Label: Option[String] = None,
                    line3Label: Option[String] = None,
                    townLabel: Option[String] = None,
                    postcodeLabel: Option[String] = None,
                    countryLabel: Option[String] = None,
                    submitLabel: Option[String] = None,
                    showSearchAgainLink: Option[Boolean] = Some(false),
                    searchAgainLinkText: Option[String] = None)

case class JourneyData(config: JourneyConfig,
                       proposals: Option[Seq[ProposedAddress]] = None,
                       selectedAddress: Option[ConfirmableAddress] = None,
                       confirmedAddress: Option[ConfirmableAddress] = None) {

  def resolvedConfig = ResolvedJourneyConfig(config)

}

case class Init(continueUrl: Option[String])

case class JourneyConfig(continueUrl: String,
                         lookupPage: Option[LookupPage] = Some(LookupPage()),
                         selectPage: Option[SelectPage] = Some(SelectPage()),
                         confirmPage: Option[ConfirmPage] = Some(ConfirmPage()),
                         editPage: Option[EditPage] = Some(EditPage()),
                         homeNavHref: Option[String] = None,
                         navTitle: Option[String] = None,
                         additionalStylesheetUrl: Option[String] = None,
                         showPhaseBanner: Option[Boolean] = Some(false), // if phase banner is shown, it will default to "beta" unless ...
                         alphaPhase: Option[Boolean] = Some(false), // ... you set "alpha" to be true,
                         phaseFeedbackLink: Option[String] = None,
                         phaseBannerHtml: Option[String] = None,
                         showBackButtons: Option[Boolean] = Some(false),
                         includeHMRCBranding: Option[Boolean] = Some(true),
                         deskProServiceName: Option[String] = None,
                         allowedCountryCodes: Option[Set[String]] = None,
                         timeout: Option[Timeout] = None)

case class ProposedAddress(addressId: String,
                           postcode: String,
                           lines: List[String] = List.empty,
                           town: Option[String] = None,
                           county: Option[String] = None,
                           country: Country = ForeignOfficeCountryService.find("GB").getOrElse(Country("GB", "United Kingdom"))) {

  def toConfirmableAddress(auditRef: String): ConfirmableAddress = ConfirmableAddress(
    auditRef,
    Some(addressId),
    ConfirmableAddressDetails(
      Some(toLines),
      Some(postcode),
      Some(country)
    )
  )

  private def toLines: List[String] = {
    town match {
      case Some(town) => lines.take(3) ++ List(town)
      case None => county match {
        case Some(county) => lines.take(3) ++ List(county)
        case None => lines.take(4)
      }
    }
  }

  // TODO verify description format
  def toDescription: String = {
    lines.take(3).mkString(", ") + ", " +
      town.map(_ + ", ").getOrElse("") +
      county.map(_ + ", ").getOrElse("") +
      postcode + ", " +
      country.name
  }

}

case class ConfirmableAddress(auditRef: String,
                              id: Option[String] = None,
                              address: ConfirmableAddressDetails = ConfirmableAddressDetails()) {

  def toEdit: Edit = address.toEdit

  def toDescription: String = address.toDescription

}

case class ConfirmableAddressDetails(lines: Option[List[String]] = None,
                                     postcode: Option[String] = None,
                                     country: Option[Country] = ForeignOfficeCountryService.find("GB")) {

  def toDescription: String = {
    (lines.getOrElse(List.empty) ++ postcode.toList ++ country.toList.map(_.name)).mkString(", ") + "."
  }

  def toEdit: Edit = {
    val el = editLines
    Edit(el._1, el._2, el._3, el._4, postcode.getOrElse(""), country.map(_.code))
  }

  def editLines: (String, Option[String], Option[String], String) = {
    val l1 = lines.map { lines =>
      lines.lift(0).getOrElse("")
    }.getOrElse("")
    val l2 = lines.flatMap(l => {
      if (l.length > 2) l.lift(1) else None
    })
    val l3 = lines.flatMap(l => {
      if (l.length > 3) l.lift(2) else None
    })
    val l4 = lines.flatMap(l => {
      if (l.length > 1) l.lastOption else None
    }).getOrElse("")
    (l1, l2, l3, l4)
  }

}

// JSON serialization companions

object JourneyData {

  implicit val countryFormat = Json.format[Country]
  implicit val confirmPageFormat = Json.format[ConfirmPage]
  implicit val selectPageFormat = Json.format[SelectPage]
  implicit val lookupPageFormat = Json.format[LookupPage]
  implicit val editPageFormat = Json.format[EditPage]
  implicit val confirmableAddressDetailsFormat = Json.format[ConfirmableAddressDetails]
  implicit val confirmableAddressFormat = Json.format[ConfirmableAddress]
  implicit val proposedAddressFormat = Json.format[ProposedAddress]
  implicit val timeoutFormat: Format[Timeout] = (
    (JsPath \ "timeoutAmount").format[Int](min(120)) and
      (JsPath \ "timeoutUrl").format[String]
    )(Timeout.apply, unlift(Timeout.unapply))
  implicit val journeyConfigFormat = Json.format[JourneyConfig]
  implicit val journeyDataFormat = Json.format[JourneyData]

}

object ConfirmableAddress {

  implicit val countryFormat = Json.format[Country]
  implicit val confirmableAddressDetailsFormat = Json.format[ConfirmableAddressDetails]
  implicit val confirmableAddressFormat = Json.format[ConfirmableAddress]

}
