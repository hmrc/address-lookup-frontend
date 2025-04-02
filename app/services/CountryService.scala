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
import com.google.inject.ImplementedBy
import play.api.libs.json.{Json, OFormat}

import javax.inject.{Inject, Singleton}

@ImplementedBy(classOf[ForeignOfficeCountryService])
trait CountryService {

  def findAll(welshFlag: Boolean = false): Seq[Country]

  // to match uk.gov.hmrc.address.v2.Countries and serve as a comprehensive replacement
  def find(welshFlag: Boolean = false, code: String): Option[Country]
}

@Singleton
class ForeignOfficeCountryService @Inject() (english: EnglishCountryNamesDataSource, welsh: WelshCountryNamesDataSource) extends CountryService {
  implicit val fcoCountryFormat: OFormat[FcoCountry] = Json.format[FcoCountry]

  override def findAll(welshFlag: Boolean = false): Seq[Country] =
    if (!welshFlag) english.countriesEN
    else welsh.countriesCY

  override def find(welshFlag: Boolean = false, code: String): Option[Country] = {
    if (!welshFlag) {
      val filtered = english.countriesEN.filter(_.code == code)
      filtered.headOption
    }
    else {
      val filtered = welsh.countriesCY.filter(_.code == code)
      filtered.headOption
    }
  }
}

case class FcoCountry(country: String, name: String)