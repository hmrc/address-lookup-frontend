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

import com.codahale.metrics.SharedMetricRegistries
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite

class ForeignOfficeCountryServiceSpec extends PlaySpec with GuiceOneAppPerSuite {

  class Scenario {
    SharedMetricRegistries.clear()
    val service = new ForeignOfficeCountryService
  }

  "find all in English" should {
    "return list of countries ordered by name" in new Scenario {
      val found = service.findAll()
      found.head.name must be ("Afghanistan")
      found.last.name must be ("Zimbabwe")
    }
    "return list of countries must have unique elements" in new Scenario {
      service.findAll().size mustBe  service.findAll().distinct.size
    }

  }

  "UK in English" should {

    "keep reference to UK" in new Scenario {
      val found = service.find(code = "GB")
      found.get.name must be ("United Kingdom")
    }

  }

  "find all in Welsh" should {
    "return list of countries ordered by name" in new Scenario {
      val found = service.findAll(welshFlag = true)
      found.head.name must be ("Affganistan")
      found.last.name must be ("Zimbabwe")
    }
    "return list of countries must have unique elements" in new Scenario {
      service.findAll(welshFlag = true).size mustBe  service.findAll(welshFlag = true).distinct.size
    }
  }

  "UK in Welsh" should {
    "keep reference to UK" in new Scenario {
      val found = service.find(welshFlag = true, code = "GB")
      found.get.name must be ("Y Deyrnas Unedig")
    }

  }
}
