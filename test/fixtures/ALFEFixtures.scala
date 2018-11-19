package fixtures

import model.{Edit, JourneyConfig, JourneyData}


trait ALFEFixtures {

   def basicJourney(ukModeBool: Option[Boolean] = Some(false)): JourneyData = JourneyData(JourneyConfig("continue", ukMode = ukModeBool))

   def editFormConstructor(a: Edit = Edit("foo", Some("bar"), Some("wizz"), "bang","B11 6HJ", Some("GB")))
   = Seq(("line1", a.line1),
      a.line2.map(b => ("line2", b)).getOrElse(("", "")),
      a.line3.map(c => ("line3", c)).getOrElse(("", "")),
      ("town", a.town),
      ("postcode", a.postcode),
      a.countryCode.map(d => ("countryCode", d)).getOrElse(("", "")))
}