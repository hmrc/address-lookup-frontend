package fixtures

import model._


trait ALFEFixtures {

   def basicJourney(ukModeBool: Option[Boolean] = Some(false)): JourneyData = JourneyData(JourneyConfig("continue", ukMode = ukModeBool))
   def basicJourneyV2(ukModeBool: Option[Boolean] = Some(false)): JourneyDataV2 = JourneyDataV2(JourneyConfigV2(2, JourneyOptions("continue", ukMode = ukModeBool)))

   def editFormConstructor(a: Edit = Edit("foo", Some("bar"), Some("wizz"), "bang","B11 6HJ", "GB"))
   = Seq(("line1", a.line1),
      a.line2.map(b => ("line2", b)).getOrElse(("", "")),
      a.line3.map(c => ("line3", c)).getOrElse(("", "")),
      ("town", a.town),
      ("postcode", a.postcode),
      ("countryCode", a.countryCode))
}