package services

import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import uk.gov.hmrc.address.v2.Country

class ForeignOfficeCountryServiceSpec extends PlaySpec with OneAppPerSuite {

  class Scenario {
    val service = new ForeignOfficeCountryService
  }

  "find all" should {

    "return list of countries ordered by name" in new Scenario {
      val found = service.findAll
      found.head.name must be ("Afghanistan")
      found.last.name must be ("Zimbabwe")
    }

  }

  "UK" should {

    "keep reference to UK" in new Scenario {
      service.GB must be (Country("GB", "United Kingdom"))
    }

  }

}
