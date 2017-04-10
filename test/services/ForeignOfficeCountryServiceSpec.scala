package services

import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}

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

}
