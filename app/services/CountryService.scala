/*
 * Copyright 2022 HM Revenue & Customs
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

package services

import javax.inject.Singleton
import com.github.tototoshi.csv._
import com.google.inject.ImplementedBy
import play.api.libs.json.Json
import address.v2.Country

import scala.io.Source

@ImplementedBy(classOf[ForeignOfficeCountryService])
trait CountryService {

  def findAll(welshFlag: Boolean = false): Seq[Country]

  // to match uk.gov.hmrc.address.v2.Countries and serve as a comprehensive replacement
  def find(welshFlag: Boolean = false, code: String): Option[Country]

}

@Singleton
class ForeignOfficeCountryService extends CountryService {

  implicit val fcoCountryFormat = Json.format[FcoCountry]

  private val countriesENJson: Seq[Country] = Json.parse(getClass.getResourceAsStream("/countriesEN.json")).as[Map[String, FcoCountry]].map { country =>
    Country(country._2.country, country._2.name)
  }.toSeq.sortWith(_.name < _.name)

  private val mappings = Map(
    "independent" -> None,
    "alpha_3_code" -> None,
    "full_name_en" -> Some("Official Name"),
    "status" -> None,
    "short_name_uppercase_en" -> None,
    "numeric_code" -> None,
    "short_name_en" -> Some("Name"),
    "alpha_2_code" -> Some("Country")
  )

  private def renameFields(m: Map[String, String]): Map[String, String] = {
    mappings.flatMap { case (o, nm) => nm.map(n => n -> m(o)) }
  }

  private val utfSorter = java.text.Collator.getInstance()

  private val countriesENFull: Seq[Country] = {
    val allISORows = CSVReader.open(Source.fromInputStream(getClass.getResourceAsStream("/iso-countries.csv"), "UTF-8"))
      .allWithOrderedHeaders._2.sortBy(x => x("alpha_2_code"))
      .map(renameFields)
      .groupBy(_ ("Country"))
      .mapValues(v => v.head)

    val allFCDORows = CSVReader.open(Source.fromInputStream(getClass.getResourceAsStream("/fcdo_countries.csv"), "UTF-8"))
      .allWithOrderedHeaders._2.sortBy(x => x("Country"))
      .groupBy(_ ("Country"))
      .mapValues(v => v.head)

    val allFCDOTRows = CSVReader.open(Source.fromInputStream(getClass.getResourceAsStream("/fcdo_territories.csv"), "UTF-8"))
      .allWithOrderedHeaders._2.sortBy(x => x("Country"))
      .filterNot(m => m("Country") == "Not applicable")
      .groupBy(_ ("Country"))
      .mapValues(v => v.head)

    (allISORows ++ allFCDORows ++ allFCDOTRows)
      .map(Country.apply)
      .toSeq.sortWith{ case (a, b) => utfSorter.compare(a.name, b.name) < 0 }
  }

  private val countriesEN = countriesENFull

  private val countriesCYJson: Seq[Country] = Json.parse(getClass.getResourceAsStream("/countriesCY.json")).as[Map[String, FcoCountry]].map { country =>
    Country(country._2.country, country._2.name)
  }.toSeq.sortWith(_.name < _.name)

  private val countriesCYFull: Seq[Country] = {
    val allISORows = CSVReader.open(Source.fromInputStream(getClass.getResourceAsStream("/iso-countries.csv"), "UTF-8"))
      .allWithOrderedHeaders._2.sortBy(x => x("alpha_2_code"))
      .map(renameFields)
      .groupBy(_ ("Country"))
      .mapValues(v => v.head)

    val countriesCYJsonMaps = countriesCYJson.map(Country.toMap).toMap
    val countriesCYCodeSet = countriesCYJsonMaps.keySet

    val allFCDORows = CSVReader.open(Source.fromInputStream(getClass.getResourceAsStream("/fcdo_countries.csv"), "UTF-8"))
      .allWithOrderedHeaders._2.sortBy(x => x("Country"))
      .groupBy(_ ("Country"))
      .mapValues(v => v.head)

    val allFCDOTRows = CSVReader.open(Source.fromInputStream(getClass.getResourceAsStream("/fcdo_territories.csv"), "UTF-8"))
      .allWithOrderedHeaders._2.sortBy(x => x("Country"))
      .filterNot(m => m("Country") == "Not applicable")
      .groupBy(_ ("Country"))
      .mapValues(v => v.head)

    ((allISORows ++ allFCDORows ++ allFCDOTRows)
      .filterNot {
        case (code, _) => countriesCYCodeSet.contains(code)
      } ++ countriesCYJsonMaps)
      .map(Country.apply)
      .toSeq.sortWith{ case (a, b) => utfSorter.compare(a.name, b.name) < 0 }
  }

  private val countriesCY = countriesCYFull

  override def findAll(welshFlag: Boolean = false): Seq[Country] =
    if (!welshFlag) countriesEN
    else countriesCY

  override def find(welshFlag: Boolean = false, code: String): Option[Country] = {
    if (!welshFlag) {
      val filtered = countriesEN.filter(_.code == code)
      filtered.headOption
    }
    else {
      val filtered = countriesCY.filter(_.code == code)
      filtered.headOption
    }

  }

}

case class FcoCountry(country: String, name: String)

object ForeignOfficeCountryService extends ForeignOfficeCountryService
