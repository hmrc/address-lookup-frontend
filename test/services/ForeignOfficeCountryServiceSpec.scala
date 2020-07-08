package services

import com.codahale.metrics.SharedMetricRegistries
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import uk.gov.hmrc.address.v2.Country

class ForeignOfficeCountryServiceSpec extends PlaySpec with OneAppPerSuite {

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
  }

  "UK in Welsh" should {
    "keep reference to UK" in new Scenario {
      val found = service.find(welshFlag = true, code = "GB")
      found.get.name must be ("Y Deyrnas Unedig")
    }

  }
}
