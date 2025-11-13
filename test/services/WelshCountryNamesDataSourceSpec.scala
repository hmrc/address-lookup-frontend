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

import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite

class WelshCountryNamesDataSourceSpec extends PlaySpec with GuiceOneAppPerSuite {

  lazy val service: WelshCountryNamesDataSource = app.injector.instanceOf[WelshCountryNamesDataSource]

  ".allGovWalesRows()" should {

    val dummyFileWithCommas: String =
      """
        |Cod gwlad (Country code),Enw yn Saesneg (Name in English),Enw yn Gymraeg (Name in Welsh),Enw swyddogol yn Saesneg (Official name in English),Enw swyddogol yn Gymraeg (Official name in Welsh)
        |AF,Afghanistan,Affganistan,The Islamic Republic of Afghanistan,Gweriniaeth Islamaidd Affganistan
        |AL,Albania,Albania,The Republic of Albania,Gweriniaeth Albania
        |""".stripMargin

    val dummyFileWithSemicolon: String =
      """
        |Column1;Column2;Column3;Column4;Column5
        |Cod gwlad (Country code);Enw yn Saesneg (Name in English);Enw yn Gymraeg (Name in Welsh);Enw swyddogol yn Saesneg (Official name in English);Enw swyddogol yn Gymraeg (Official name in Welsh)
        |AF;Afghanistan;Affganistan;The Islamic Republic of Afghanistan;Gweriniaeth Islamaidd Affganistan
        |AL;Albania;Albania;The Republic of Albania;Gweriniaeth Albania
        |""".stripMargin

    "Be able to process a file with comma delimiter" in {
      val actual = service.allGovWalesRows(dummyFileWithCommas).toMap
      val expected = Map(
        "AF" -> Map(
          "Country" -> "AF",
          "Name" -> "Affganistan"
        ),
        "AL" -> Map(
          "Country" -> "AL",
          "Name" -> "Albania"
        )
      )
      actual.mustBe(expected)
    }

    "Be able to process a file with semicolon delimiter (and wrong headers)" in {
      val actual = service.allGovWalesRows(dummyFileWithSemicolon).toMap
      val expected = Map(
        "AF" -> Map(
          "Country" -> "AF",
          "Name" -> "Affganistan"
        ),
        "AL" -> Map(
          "Country" -> "AL",
          "Name" -> "Albania"
        )
      )
      actual.mustBe(expected)
    }
  }
}
