package model

object JourneyConfigDefaults {

  sealed trait JourneyConfigDefaults {
    def CONFIRM_PAGE_TITLE: String

    def CONFIRM_PAGE_HEADING: String

    def CONFIRM_PAGE_INFO_SUBHEADING: String

    def CONFIRM_PAGE_INFO_MESSAGE_HTML: String

    def CONFIRM_PAGE_SUBMIT_LABEL: String

    def CONFIRM_PAGE_EDIT_LINK_TEXT: String

    def EDIT_PAGE_TITLE: String

    def EDIT_PAGE_HEADING: String

    def EDIT_PAGE_LINE1_LABEL: String

    def EDIT_PAGE_LINE2_LABEL: String

    def EDIT_PAGE_LINE3_LABEL: String

    def EDIT_PAGE_TOWN_LABEL: String

    def EDIT_PAGE_POSTCODE_LABEL: String

    def EDIT_PAGE_COUNTRY_LABEL: String

    def EDIT_PAGE_SUBMIT_LABEL: String

    def LOOKUP_PAGE_TITLE: String

    def LOOKUP_PAGE_HEADING: String

    def LOOKUP_PAGE_FILTER_LABEL: String

    def LOOKUP_PAGE_POSTCODE_LABEL: String

    def LOOKUP_PAGE_SUBMIT_LABEL: String

    def LOOKUP_PAGE_MANUAL_ADDRESS_LINK_TEXT: String

    def SELECT_PAGE_TITLE: String

    def SELECT_PAGE_HEADING: String

    def SELECT_PAGE_HEADING_WITH_POSTCODE: String

    def SELECT_PAGE_PROPOSAL_LIST_LABEL: String

    def SELECT_PAGE_SUBMIT_LABEL: String

    def EDIT_LINK_TEXT: String

    def SEARCH_AGAIN_LINK_TEXT: String

    def CONFIRM_CHANGE_TEXT: String

    def defaultPhaseBannerHtml(link: String): String
  }

  case class EnglishConstants(isUkMode: Boolean) extends JourneyConfigDefaults {
    val CONFIRM_PAGE_TITLE = "Confirm the address"
    val CONFIRM_PAGE_HEADING = "Review and confirm"
    val CONFIRM_PAGE_INFO_SUBHEADING = "Your selected address"
    val CONFIRM_PAGE_INFO_MESSAGE_HTML = "This is how your address will look. Please double-check it and, if accurate, click on the <kbd>Confirm</kbd> button."
    val CONFIRM_PAGE_SUBMIT_LABEL = "Confirm address"
    val CONFIRM_PAGE_EDIT_LINK_TEXT = "Change address"

    val EDIT_PAGE_TITLE = "Enter the address"
    val EDIT_PAGE_HEADING = "Enter the address"
    val EDIT_PAGE_LINE1_LABEL = "Address line 1"
    val EDIT_PAGE_LINE2_LABEL = "Address line 2 (optional)"
    val EDIT_PAGE_LINE3_LABEL = "Address line 3 (optional)"
    val EDIT_PAGE_TOWN_LABEL = "Town/city"
    val EDIT_PAGE_POSTCODE_LABEL = "Postal code (optional)"
    val EDIT_PAGE_COUNTRY_LABEL = "Country"
    val EDIT_PAGE_SUBMIT_LABEL = "Continue"

    val LOOKUP_PAGE_TITLE = if (isUkMode) "Find UK address" else "Find address"
    val LOOKUP_PAGE_HEADING = if (isUkMode) "Find UK address" else "Find address"
    val LOOKUP_PAGE_FILTER_LABEL = "Property name or number (optional)"
    val LOOKUP_PAGE_POSTCODE_LABEL = "UK postcode"
    val LOOKUP_PAGE_SUBMIT_LABEL = "Find address"
    val LOOKUP_PAGE_MANUAL_ADDRESS_LINK_TEXT = "The address does not have a UK postcode"

    val SELECT_PAGE_TITLE = "Choose address"
    val SELECT_PAGE_HEADING = "Choose address"
    val SELECT_PAGE_HEADING_WITH_POSTCODE = "Showing all results for "
    val SELECT_PAGE_PROPOSAL_LIST_LABEL = "Please select one of the following addresses"
    val SELECT_PAGE_SUBMIT_LABEL = "Continue"

    val EDIT_LINK_TEXT = "Enter the address manually"
    val SEARCH_AGAIN_LINK_TEXT = "Search again"

    val CONFIRM_CHANGE_TEXT = "By confirming this change, you agree that the information you have given is complete and correct."

    def defaultPhaseBannerHtml(link: String) = s"This is a new service – your <a href='$link}'>feedback</a> will help us to improve it."
  }

  case class WelshConstants(isUkMode: Boolean) extends JourneyConfigDefaults {
    val CONFIRM_PAGE_TITLE = "Cadarnhewch y cyfeiriad"
    val CONFIRM_PAGE_HEADING = "Adolygu a chadarnhau"
    val CONFIRM_PAGE_INFO_SUBHEADING = "Y cyfeiriad rydych wedi’i ddewis"
    val CONFIRM_PAGE_INFO_MESSAGE_HTML = "Bydd eich cyfeiriad yn edrych fel hyn. Gwiriwch eto ac, os yw’n gywir, cliciwch y botwm <kbd>Cadarnhau</kbd>."
    val CONFIRM_PAGE_SUBMIT_LABEL = "Cadarnhewch gyfeiriad"
    val CONFIRM_PAGE_EDIT_LINK_TEXT = "Newidiwch gyfeiriad"

    val EDIT_PAGE_TITLE = "Nodwch y cyfeiriad"
    val EDIT_PAGE_HEADING = "Nodwch y cyfeiriad"
    val EDIT_PAGE_LINE1_LABEL = "Llinell cyfeiriad 1"
    val EDIT_PAGE_LINE2_LABEL = "Llinell cyfeiriad 2 (dewisol)"
    val EDIT_PAGE_LINE3_LABEL = "Llinell cyfeiriad 3 (dewisol)"
    val EDIT_PAGE_TOWN_LABEL = "Tref/dinas"
    val EDIT_PAGE_POSTCODE_LABEL = "Cod post (dewisol)"
    val EDIT_PAGE_COUNTRY_LABEL = "Gwlad"
    val EDIT_PAGE_SUBMIT_LABEL = "Yn eich blaen"

    val LOOKUP_PAGE_TITLE = if (isUkMode) "Dod o hyd i gyfeiriad yn y DU" else "Dod o hyd i gyfeiriad"
    val LOOKUP_PAGE_HEADING = if (isUkMode) "Dod o hyd i gyfeiriad yn y DU" else "Dod o hyd i gyfeiriad"
    val LOOKUP_PAGE_FILTER_LABEL = "Enw neu rif yr eiddo (dewisol)"
    val LOOKUP_PAGE_POSTCODE_LABEL = "Cod post yn y DU"
    val LOOKUP_PAGE_SUBMIT_LABEL = "Chwiliwch am y cyfeiriad"
    val LOOKUP_PAGE_MANUAL_ADDRESS_LINK_TEXT = "Nid oes gan y cyfeiriad god post yn y DU"

    val SELECT_PAGE_TITLE = "Dewiswch cyfeiriad"
    val SELECT_PAGE_HEADING = "Dewiswch cyfeiriad"
    val SELECT_PAGE_HEADING_WITH_POSTCODE = "Yn dangos pob canlyniad ar gyfer "
    val SELECT_PAGE_PROPOSAL_LIST_LABEL = "Dewiswch un o’r cyfeiriadau canlynol"
    val SELECT_PAGE_SUBMIT_LABEL = "Yn eich blaen"

    val EDIT_LINK_TEXT = "Nodwch y cyfeiriad â llaw"
    val SEARCH_AGAIN_LINK_TEXT = "Chwilio eto"

    val CONFIRM_CHANGE_TEXT = "Drwy gadarnhau’r newid hwn, rydych yn cytuno bod yr wybodaeth a roesoch yn gyflawn ac yn gywir."

    def defaultPhaseBannerHtml(link: String) = s"Mae hwn yn wasanaeth newydd – bydd eich <a href='$link}'>adborth</a> yn ein helpu i’w wella."
  }

}
