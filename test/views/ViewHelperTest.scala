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

package views

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import services.CountryService

class ViewHelperTest extends AnyWordSpec with GuiceOneAppPerSuite with Matchers {
  val countryService = app.injector.instanceOf[CountryService]

  "ViewHelper" should {
    val allCountries = countryService.findAll()
    val selectItems = ViewHelper.countriesToSelectItems(allCountries)

    "create list of country select items for all countries provided" in {
      selectItems should have length (allCountries.length)
    }

    "create list of country select items with unique ids" in {
      val groupedById = selectItems.groupBy(_.attributes("id"))

      groupedById.keySet.size shouldBe allCountries.length
    }

    "create list of country select items with unique values" in {
      val groupedById = selectItems.groupBy(_.value)

      groupedById.keySet.size shouldBe allCountries.length
    }

    "encodeCountryCode predictably" in {
      val gb = allCountries.find(_.code == "GB")
      val encoded = ViewHelper.encodeCountryCode(gb.get)
      encoded should not contain " "
      encoded shouldBe gb.map(gb => s"GB-United_Kingdom").getOrElse("GB_NOT_FOUND")
    }

    "decodeCountryCode predictably" in {
      val decoded = ViewHelper.decodeCountryCode("GB-United Kingdom")
      decoded shouldBe "GB"
    }
  }
}
