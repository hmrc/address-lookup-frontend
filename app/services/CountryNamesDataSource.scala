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

import scala.io.Source

trait CountryNamesDataSource {
  protected def allAliases(aliasFileClasspath: String) =
    CSVReader.open(Source.fromInputStream(getClass.getResourceAsStream(aliasFileClasspath), "UTF-8"))
      .allWithOrderedHeaders()._2.sortBy(x => x("countryCode"))
      .map { x => x("countryCode") -> (x("aliases").split("\\|").map(_.trim).toList) }
      .map { case (c, as) => c -> as.map(a => Country(c, a)) }
      .toMap

  protected def renameFields(m: Map[String, String]): Map[String, String] = {
    mappings.flatMap { case (o, nm) => nm.map(n => n -> m(o)) }
  }

  protected val mappings = Map(
    "independent" -> None,
    "alpha_3_code" -> None,
    "full_name_en" -> Some("Official Name"),
    "status" -> None,
    "short_name_uppercase_en" -> None,
    "numeric_code" -> None,
    "short_name_en" -> Some("Name"),
    "alpha_2_code" -> Some("Country")
  )

  protected val utfSorter = java.text.Collator.getInstance()
}
