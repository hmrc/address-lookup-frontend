package model

object MessageConstants {

  sealed trait MessageConstants {
    def home: String
    def back: String
    def forPostcode: String

    def timeoutSeconds: String
    def timeoutMinutes: String
    def timeoutMinute: String
    def timeoutSignedOut: String
    def timeoutSignYouOut: String
    def timeoutResumeSession: String
    def timeoutInactive: String

    def confirmSelectedAddressError1: String
    def confirmSelectedAddressError2: String

    def lookupErrorHeading: String
    def lookupPostcodeError: String
    def lookupFilterError: String
    def lookupFilterHint: String

    def errorText: String
    def noResults: String
    def differentSearch: String

    def tooManyResultsText: String
    def cannotFindText: String
    def anotherSearchText: String
    def nothingText: String
    def nameNumberText: String
    def youEntered: String
    def tooManyResultsManualLink: String
    def noResultsFoundTitle: String

    def ukModePostcodeLabel: String

    def editPageAddressLine1MaxErrorMessage: String
    def editPageAddressLine2MaxErrorMessage: String
    def editPageAddressLine3MaxErrorMessage: String
    def editPageTownMaxErrorMessage: String
    def editPageAddressLine1MinErrorMessage: String
    def editPageTownMinErrorMessage: String
    def editPagePostcodeErrorMessage: String

    def noResultsPageTitle: String
    def noResultsPageHeading: String
    def noResultsPageEnterManually: String
    def noResultsPageDifferentPostcode: String

    def intServerErrorTitle: String
    def intServerErrorTryAgain: String

    def notFoundErrorTitle: String
    def notFoundErrorHeading: String
    def notFoundErrorBody: String

    val errorRequired: String
    val errorMin: Int => String
    val errorMax: Int => String

  }

  object EnglishMessageConstants extends MessageConstants {
    val home = "Home"
    val back = "Back"
    val forPostcode = "for postcode"

    val timeoutSeconds = "seconds"
    val timeoutMinutes = "minutes"
    val timeoutMinute = "minute"
    val timeoutSignedOut = "You're about to be signed out"
    val timeoutSignYouOut = "For your security, we'll sign you out in"
    val timeoutResumeSession = "Resume your session"
    val timeoutInactive = "You've been inactive for a while."
    val ukModePostcodeLabel = "UK postcode (optional)"

    val editPageAddressLine1MaxErrorMessage = "Enter a first address line using 256 characters or less"
    val editPageAddressLine2MaxErrorMessage = "Enter a second address line using 256 characters or less"
    val editPageAddressLine3MaxErrorMessage = "Enter a third address line using 256 characters or less"
    val editPageTownMaxErrorMessage = "Enter a town or city using 256 characters or less"
    val editPageAddressLine1MinErrorMessage = "Enter first line of address"
    val editPageTownMinErrorMessage = "Enter a town or city"
    val editPagePostcodeErrorMessage = "Enter a valid UK postcode"

    val errorRequired = "This field is required"
    val errorMin: Int => String = min => s"Minimum length is $min"
    val errorMax: Int => String = max => s"Maximum length is $max"

    val confirmSelectedAddressError1 = "You have not selected any address. Please"
    val confirmSelectedAddressError2 = "search for and select your address"

    val lookupErrorHeading = "This page has errors"
    val lookupPostcodeError = "The postcode you entered appears to be incomplete or invalid. Please check and try again."
    val lookupFilterError = "Your house name/number needs to be fewer than 256 characters"
    val lookupFilterHint = "For example, The Mill, 116 or Flat 37a"

    val errorText = "This page has errors"
    val noResults = "We could not find a match with"
    val differentSearch = "Try a different name or number"

    val noResultsPageTitle = "Can't find any addresses"
    val noResultsPageHeading = "We can not find any addresses for"
    val noResultsPageEnterManually = "Enter the address manually"
    val noResultsPageDifferentPostcode = "Try a different postcode"

    val tooManyResultsText = "There are too many results"
    val noResultsFoundTitle = "No results found"
    val cannotFindText = "We couldn't find any results for that property name or number"
    val anotherSearchText = "Try a new search"
    val nothingText = "nothing for property name or number"
    val nameNumberText = "for name or number"
    val youEntered = "You entered:"
    val tooManyResultsManualLink = "Enter the address manually"

    val intServerErrorTitle = "Sorry, there is a problem with the service"
    val intServerErrorTryAgain = "Try again later."

    val notFoundErrorTitle = "Page not found"
    val notFoundErrorHeading = "This page cannot be found"
    val notFoundErrorBody = "Please check that you have entered the correct web address."
  }

  object WelshMessageConstants extends MessageConstants {
    val home = "Cartref"
    val back = "Yn ôl"
    val forPostcode = "am y cod post"

    val timeoutSeconds = "eiliad"
    val timeoutMinutes = "o funudau"
    val timeoutMinute = "munud"
    val timeoutSignedOut = "Rydych ar fin cael eich allgofnodi"
    val timeoutSignYouOut = "Er eich diogelwch, byddwn yn eich allgofnodi cyn pen"
    val timeoutResumeSession = "Ailddechrau eich sesiwn"
    val timeoutInactive = "Rydych wedi bod yn anweithredol am sbel."

    val confirmSelectedAddressError1 = "Nid ydych wedi dewis unrhyw gyfeiriad."
    val confirmSelectedAddressError2 = "Chwiliwch am eich cyfeiriad a’i ddewis"

    val lookupErrorHeading = "Mae gwallau ar y dudalen hon"
    val lookupPostcodeError = "Mae’r cod post a nodoch i’w weld yn anghyflawn neu’n annilys. Gwiriwch ef a rhowch gynnig arall arni."
    val lookupFilterError = "Rhaid i enw/rhif eich tŷ fod yn llai na 256 o gymeriadau"
    val lookupFilterHint = "Er enghraifft, Tegfan, 116 neu Fflat 37a"

    val errorText = "Mae gwallau ar y dudalen hon"
    val noResults = "Nid oeddem yn gallu dod o hyd i rywbeth sy’n cydweddu â"
    val differentSearch = "Rhowch gynnig ar enw neu rif gwahanol"
    val ukModePostcodeLabel = "Cod post y DU (dewisol)"

    val tooManyResultsText = "Mae yna ormod o ganlyniadau"
    val noResultsFoundTitle = "Dim canlyniadau wedi’u darganfod"
    val cannotFindText = "Ni allem ddod o hyd i unrhyw ganlyniadau ar gyfer enw neu rif yr eiddo hwnnw"
    val anotherSearchText = "Rhowch gynnig ar chwiliad newydd"
    val nothingText = "ddim byd ar gyfer enw neu rif eiddo"
    val nameNumberText = "ar gyfer enw neu rif"
    val youEntered = "Nodoch:"
    val tooManyResultsManualLink = "Nodwch y cyfeiriad â llaw"

    val editPageAddressLine1MaxErrorMessage = "Nodwch linell gyntaf y cyfeiriad gan ddefnyddio 256 o gymeriadau neu lai"
    val editPageAddressLine2MaxErrorMessage = "Nodwch ail linell y cyfeiriad gan ddefnyddio 256 o gymeriadau neu lai"
    val editPageAddressLine3MaxErrorMessage = "Nodwch drydedd linell y cyfeiriad gan ddefnyddio 256 o gymeriadau neu lai"
    val editPageAddressLine1MinErrorMessage = "Nodwch linell gyntaf y cyfeiriad"
    val editPageTownMaxErrorMessage = "Nodwch dref neu ddinas ddefnyddio 256 o gymeriadau neu lai"
    val editPageTownMinErrorMessage = "Nodwch dref neu ddinas"
    val editPagePostcodeErrorMessage = "Nodwch god post yn y DU sy’n ddilys"

    val noResultsPageTitle = "Ni allwn ddod o hyd i unrhyw gyfeiriadau"
    val noResultsPageHeading = "Ni allwn ddod o hyd i unrhyw gyfeiriadau ar gyfer "
    val noResultsPageEnterManually = "Nodwch y cyfeiriad â llaw"
    val noResultsPageDifferentPostcode = "Rhowch gynnig ar god post gwahanol"

    val intServerErrorTitle = "Mae’n ddrwg gennym – mae problem gyda’r gwasanaeth"
    val intServerErrorTryAgain = "Rhowch gynnig arall arni yn nes ymlaen."

    val notFoundErrorTitle = "Heb ddod o hyd i’r dudalen"
    val notFoundErrorHeading = "Ni ellir dod o hyd i’r dudalen hon"
    val notFoundErrorBody = "Gwiriwch eich bod wedi nodi’r cyfeiriad gwe cywir."

    val errorRequired = "Mae angen llenwi’r maes hwn"
    val errorMin: Int => String = min => s"$min yw’r isafswm hyd"
    val errorMax: Int => String = max => s"$max yw’r uchafswm hyd"
  }

}
