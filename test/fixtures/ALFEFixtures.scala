/*
 * Copyright 2020 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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