/*
 * Copyright 2016 HM Revenue & Customs
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

package address.uk.service

import org.scalatest.{BeforeAndAfterAll, SequentialNestedSuiteExecution}
import org.scalatestplus.play.OneAppPerSuite
import stub.AddRepStub
import uk.gov.hmrc.address.v2.{Address, AddressRecord, Countries, LocalCustodian}
import uk.gov.hmrc.play.test.UnitSpec

class AddressLookupServiceTest extends UnitSpec with OneAppPerSuite
  with SequentialNestedSuiteExecution
  with BeforeAndAfterAll {

  implicit private val ec = scala.concurrent.ExecutionContext.Implicits.global

  val addRep = new AddRepStub()

  override def beforeAll() {
    addRep.start()
  }

  override def afterAll() {
    addRep.stop()
  }

  class Context {
    addRep.clearExpectations()
    val service = new AddressLookupService(addRep.endpoint, "foo")
  }

  val ne15xdLike = AddressRecord("GB4510123533", Some(4510123533L),
    Address(List("10 Taylors Court", "Monk Street", "Byker"),
      Some("Newcastle upon Tyne"), Some("Northumberland"), "NE1 5XD", Some(Countries.England), Countries.UK),
    Some(LocalCustodian(123, "Tyne & Wear")), "en")

  "Find By Postcode" should {
    "fetch an empty list ok" in new Context {
      addRep.givenAddressResponse("/v2/uk/addresses?postcode=NE12AB", Nil)
      val actual = await(service.findByPostcode("NE12AB", None))
      actual shouldBe Nil
    }

    "fetch a list ok" in new Context {
      addRep.givenAddressResponse("/v2/uk/addresses?postcode=NE15XD", List(ne15xdLike))
      val actual = await(service.findByPostcode("NE15XD", None))
      actual shouldBe List(ne15xdLike)
    }
  }

  "Find By UPRN" should {
    "fetch an empty list ok" in new Context {
      addRep.givenAddressResponse("/v2/uk/addresses?uprn=9510123533", Nil)
      val actual = await(service.findByUprn(9510123533L))
      actual shouldBe Nil
    }

    "fetch a list ok" in new Context {
      addRep.givenAddressResponse("/v2/uk/addresses?uprn=4510123533", List(ne15xdLike))
      val actual = await(service.findByUprn(4510123533L))
      actual shouldBe List(ne15xdLike)
    }
  }

  "Find By Id" should {
    "return 404" in new Context {
      addRep.givenAddressError("/v2/uk/addresses/GB9510123533", 404, "Not found")
      val actual = await(service.findById("GB9510123533"))
      actual shouldBe None
    }

    "fetch a list ok" in new Context {
      addRep.givenAddressResponse("/v2/uk/addresses/GB4510123533", ne15xdLike)
      val actual = await(service.findById("GB4510123533"))
      actual shouldBe Some(ne15xdLike)
    }
  }

  "Search Fuzzy" should {
    //TODO
  }

}
