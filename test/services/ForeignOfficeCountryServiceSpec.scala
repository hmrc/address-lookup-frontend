package services

import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}

class ForeignOfficeCountryServiceSpec extends PlaySpec with OneAppPerSuite with ScalaFutures {

  class Scenario {
    val service = new ForeignOfficeCountryService
  }

  "find all" should {

    "return list of countries ordered by name" in new Scenario {
      val found = service.findAll.futureValue
      found.head.name must be ("Afghanistan")
      found.last.name must be ("Zimbabwe")
    }

  }

}
