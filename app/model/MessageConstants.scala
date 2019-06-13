package model

object MessageConstants {

  sealed trait MessageConstants {
    def home: String
    def back: String
    def timeoutSeconds: String
    def timeoutMinutes: String
    def timeoutMinute: String
    def timeoutSignedOut: String
    def timeoutSignYouOut: String
    def timeoutResumeSession: String
    def timeoutInactive: String
    def confirmSelectedAddressError1: String
    def confirmSelectedAddressError2: String
    def errorText: String
    def noResults: String
    def differentSearch: String
  }

  object EnglishMessageConstants extends MessageConstants {
    val home = "Home"
    val back = "Back"
    val timeoutSeconds = "seconds"
    val timeoutMinutes = "minutes"
    val timeoutMinute = "minute"
    val timeoutSignedOut = "You're about to be signed out"
    val timeoutSignYouOut = "For your security, we'll sign you out in"
    val timeoutResumeSession = "Resume your session"
    val timeoutInactive = "You've been inactive for a while."
    val confirmSelectedAddressError1 = "You have not selected any address. Please"
    val confirmSelectedAddressError2 = "search for and select your address"
    val errorText = "This page has errors"
    val noResults = "We could not find a match with"
    val differentSearch = "Try a different name or number"
  }

  object WelshMessageConstants extends MessageConstants {
    val home = "Cartref"
    val back = "Yn ôl"
    val timeoutSeconds = "eiliad"
    val timeoutMinutes = "o funudau"
    val timeoutMinute = "munud"
    val timeoutSignedOut = "Rydych ar fin cael eich allgofnodi"
    val timeoutSignYouOut = "Er eich diogelwch, byddwn yn eich allgofnodi cyn pen"
    val timeoutResumeSession = "Ailddechrau eich sesiwn"
    val timeoutInactive = "Rydych wedi bod yn anweithredol am sbel."
    val confirmSelectedAddressError1 = "Nid ydych wedi dewis unrhyw gyfeiriad."
    val confirmSelectedAddressError2 = "Chwiliwch am eich cyfeiriad a’i ddewis"
    val errorText = "Mae gwallau ar y dudalen hon"
    val noResults = "Nid oeddem yn gallu dod o hyd i rywbeth sy’n cydweddu â"
    val differentSearch = "Rhowch gynnig ar enw neu rif gwahanol"
  }

}
