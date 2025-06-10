/*
 * Copyright 2024 HM Revenue & Customs
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

package address.v2

import address.v1
import play.api.libs.json.{Json, OFormat}

/** Represents a country as per ISO3166. */
case class Country(code: String, name: String) {

  def asV1 = v1.Country(code, name)

  def toMap: Map[String, String] = Map(
    "Country" -> code,
    "Name" -> name
  )
}

object Country {
  implicit val format: OFormat[Country] = Json.format[Country]

  def apply(codeCountryMap: (String, Map[String, String])): Country = codeCountryMap match {
    case (code, countryMap) => new Country(countryMap("Country"), countryMap("Name"))
  }

  def toMap(country: Country): (String, Map[String, String]) = {
    (country.code -> country.toMap)
  }
}


object Countries {
  // note that "GB" is the official ISO code for UK, although "UK" is a reserved synonym and is less confusing
  val UK = Country("UK", "United Kingdom")
  val GB = Country("GB", "United Kingdom") // special case provided for in ISO-3166
  val GG = Country("GG", "Guernsey")
  val IM = Country("IM", "Isle of Man")
  val JE = Country("JE", "Jersey")

  val England = Country("GB-ENG", "England")
  val Scotland = Country("GB-SCT", "Scotland")
  val Wales = Country("GB-WLS", "Wales")
  val Cymru = Country("GB-CYM", "Cymru")
  val NorthernIreland = Country("GB-NIR", "Northern Ireland")

  val Bermuda = Country("BM", "Bermuda")
  val Netherlands = Country("NL", "Netherlands")
  val BritishVirginIslands = Country("VG", "British Virgin Islands")

  private val all = List(UK, GB, GG, IM, JE, England, Scotland, Wales, Cymru, NorthernIreland)

  private val countriesWithLookupData = List(Bermuda, Netherlands, BritishVirginIslands)

  def find(code: String): Option[Country] = all.find(_.code == code)

  def findCountryWithData(code: String): Option[Country] = countriesWithLookupData.find(_.code == code)

  def findByName(name: String): Option[Country] = all.find(_.name == name)

  // TODO this is possibly not good enough - should consult a reference HMG-approved list of countries
}
