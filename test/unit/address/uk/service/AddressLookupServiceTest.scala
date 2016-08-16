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
import uk.gov.hmrc.addresses.{Address, AddressRecord}
import uk.gov.hmrc.addresses.Countries.UK
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
      Some("Newcastle upon Tyne"), Some("Northumberland"), "NE1 5XD", Some("GB-ENG"), UK),
    "en")

  "AddressLookupService" should {
    "fetch an empty list ok" in new Context {
      addRep.givenAddressResponse("/uk/addresses?postcode=NE12AB", Nil)
      val actual = await(service.findAddresses("NE12AB", None))
      actual shouldBe Nil
    }

    "fetch a list ok" in new Context {
      addRep.givenAddressResponse("/uk/addresses?postcode=NE15XD", List(ne15xdLike))
      val actual = await(service.findAddresses("NE15XD", None))
      actual shouldBe List(ne15xdLike)
    }
  }

}
