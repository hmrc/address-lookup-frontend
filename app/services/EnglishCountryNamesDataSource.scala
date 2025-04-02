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

package services

import address.v2.Country
import com.github.tototoshi.csv.CSVReader

import javax.inject.Singleton
import scala.collection.MapView
import scala.collection.immutable.SortedMap
import scala.io.Source

@Singleton
class EnglishCountryNamesDataSource extends CountryNamesDataSource {
  private val allAliasesEN = allAliases("/countryAliasesEN.csv")

  val allISORows: MapView[String, Map[String, String]] =
    CSVReader.open(Source.fromInputStream(getClass.getResourceAsStream("/iso-countries.csv"), "UTF-8"))
      .allWithOrderedHeaders()._2.sortBy(x => x("alpha_2_code"))
      .map(renameFields)
      .groupBy(_("Country")).view
      .mapValues(v => v.head)

  val allFCDORows: MapView[String, Map[String, String]] =
    CSVReader.open(Source.fromInputStream(getClass.getResourceAsStream("/fcdo_countries.csv"), "UTF-8"))
      .allWithOrderedHeaders()._2.sortBy(x => x("Country"))
      .groupBy(_("Country")).view
      .mapValues(v => v.head)

  val allFCDOTRows: MapView[String, Map[String, String]] =
    CSVReader.open(Source.fromInputStream(getClass.getResourceAsStream("/fcdo_territories.csv"), "UTF-8"))
      .allWithOrderedHeaders()._2.sortBy(x => x("Country"))
      .filterNot(m => m("Country") == "Not applicable")
      .groupBy(_("Country")).view
      .mapValues(v => v.head)

  private val countriesENFull: Seq[Country] = {
    SortedMap.from(allISORows ++ allFCDORows ++ allFCDOTRows)
      .map(Country.apply)
      .toSeq.sortWith { case (a, b) => utfSorter.compare(a.name, b.name) < 0 }
  }

  val countriesEN: Seq[Country] = countriesENFull.flatMap { country =>
    if (allAliasesEN.contains(country.code)) country +: allAliasesEN(country.code)
    else Seq(country)
  }


}