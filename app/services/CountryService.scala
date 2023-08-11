/*
 * Copyright 2023 HM Revenue & Customs
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

import address.v2.Country
import com.github.tototoshi.csv._
import com.google.inject.ImplementedBy
import play.api.libs.json.Json

import javax.inject.Singleton
import scala.collection.immutable.SortedMap
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

  private def allAliases(aliasFileClasspath: String) =
    CSVReader.open(Source.fromInputStream(getClass.getResourceAsStream(aliasFileClasspath), "UTF-8"))
      .allWithOrderedHeaders()._2.sortBy(x => x("countryCode"))
      .map { x => x("countryCode") -> (x("aliases").split("\\|").map(_.trim).toList) }
      .map { case (c, as) => c -> as.map(a => Country(c, a)) }
      .toMap
  private val allAliasesEN = allAliases("/countryAliasesEN.csv")
  private val allAliasesCY = allAliases("/countryAliasesCY.csv")

  private val allISORows = CSVReader.open(Source.fromInputStream(getClass.getResourceAsStream("/iso-countries.csv"), "UTF-8"))
    .allWithOrderedHeaders._2.sortBy(x => x("alpha_2_code"))
    .map(renameFields)
    .groupBy(_ ("Country")).view
    .mapValues(v => v.head)

  private val allFCDORows =  CSVReader.open(Source.fromInputStream(getClass.getResourceAsStream("/fcdo_countries.csv"), "UTF-8"))
    .allWithOrderedHeaders._2.sortBy(x => x("Country"))
    .groupBy(_ ("Country")).view
    .mapValues(v => v.head)

  private val allFCDOTRows = CSVReader.open(Source.fromInputStream(getClass.getResourceAsStream("/fcdo_territories.csv"), "UTF-8"))
    .allWithOrderedHeaders._2.sortBy(x => x("Country"))
    .filterNot(m => m("Country") == "Not applicable")
    .groupBy(_ ("Country")).view
    .mapValues(v => v.head)

  private val allWCORows = CSVReader.open(Source.fromInputStream(getClass.getResourceAsStream("/wco-countries.csv"), "UTF-8"))
    .allWithOrderedHeaders._2.sortBy(x => x("Country"))
    .groupBy(_ ("Country"))
    .view.mapValues(v => v.head)

  private val countriesENFull: Seq[Country] = {
    SortedMap.from(allISORows ++ allFCDORows ++ allFCDOTRows)
      .map(Country.apply)
      .toSeq.sortWith{ case (a, b) => utfSorter.compare(a.name, b.name) < 0 }
  }

  private val countriesEN = countriesENFull.flatMap { country =>
    if(allAliasesEN.contains(country.code)) country +: allAliasesEN(country.code)
    else Seq(country)
  }

  private val countriesCYFull: Seq[Country] = {
    SortedMap.from(allISORows ++ allFCDORows ++ allFCDOTRows ++ allWCORows)
      .map(Country.apply)
      .toSeq.sortWith{ case (a, b) => utfSorter.compare(a.name, b.name) < 0 }
  }

  private val countriesCY = countriesCYFull.flatMap { country =>
    if (allAliasesCY.contains(country.code)) country +: allAliasesCY(country.code)
    else Seq(country)
  }

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
