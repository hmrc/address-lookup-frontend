/*
 * Copyright 2021 HM Revenue & Customs
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
import com.google.inject.ImplementedBy
import play.api.libs.json.{Format, JsResult, JsValue, Json, JsonConfiguration, JsonNaming, OFormat}
import address.v2.Country
import play.api.data.format.Formats
import play.api.libs.json.JsonNaming.SnakeCase

import java.time.LocalDate

@ImplementedBy(classOf[ForeignOfficeCountryService])
trait CountryService {

  def findAll(welshFlag: Boolean = false): Seq[Country]

  // to match uk.gov.hmrc.address.v2.Countries and serve as a comprehensive replacement
  def find(welshFlag: Boolean = false, code: String): Option[Country]

}

object KebabCase extends JsonNaming {
  def apply(property: String): String = {
    val length = property.length
    val result = new StringBuilder(length * 2)
    var resultLength = 0
    var wasPrevTranslated = false
    for (i <- 0 until length) {
      var c = property.charAt(i)
      if (i > 0 || i != '-') {
        if (Character.isUpperCase(c)) {
          // append a underscore if the previous result wasn't translated
          if (!wasPrevTranslated && resultLength > 0 && result.charAt(resultLength - 1) != '-') {
            result.append('-')
            resultLength += 1
          }
          c = Character.toLowerCase(c)
          wasPrevTranslated = true
        } else {
          wasPrevTranslated = false
        }
        result.append(c)
        resultLength += 1
      }
    }

    // builds the final string
    result.toString()
  }

  override def toString = "KebabCase"
}


@Singleton
class ForeignOfficeCountryService extends CountryService {

  implicit val config = JsonConfiguration(KebabCase)
  implicit val fcoCountryFormat: Format[FcoCountry] = Json.format[FcoCountry]

  private val countriesEN: Seq[Country] =
    Json.parse(getClass.getResourceAsStream("/countriesEN.json")).as[Map[String, FcoCountry]]
        .filter(validCountry)
        .map { country => Country(country._2.country, country._2.name) }
        .toSeq.sortWith(_.name < _.name)

  private val countriesCY: Seq[Country] =
    Json.parse(getClass.getResourceAsStream("/countriesCY.json")).as[Map[String, FcoCountry]]
        .filter(validCountry)
        .map { country => Country(country._2.country, country._2.name) }
        .toSeq.sortWith(_.name < _.name)

  final lazy val startupDate = LocalDate.now
  private def validCountry(country: (String, FcoCountry)): Boolean = {
    country._2.endDate.map(ed => ed.isEqual(startupDate) || ed.isAfter(startupDate)).getOrElse(true)
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

//case class FcoCountry(country: String, name: String)
case class FcoCountry(country: String, name: String, endDate: Option[LocalDate])

object ForeignOfficeCountryService extends ForeignOfficeCountryService