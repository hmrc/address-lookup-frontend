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

import com.pyruby.stubserver.StubMethod._
import uk.gov.hmrc.util.JacksonMapper._
import org.scalatest.SequentialNestedSuiteExecution
import org.scalatestplus.play.OneAppPerSuite
import it.stub.StubbedAddressService
import uk.gov.hmrc.address.uk.{Outcode, Postcode}
import uk.gov.hmrc.address.v2.{Address, AddressRecord, Countries, LocalCustodian}
import uk.gov.hmrc.play.test.UnitSpec

// Warning: this uses a stubbed server and may give rise to spurious test failures due to concurrency
// problems when starting/stopping the stubs.
// The workaround for such problems seems to be to move these tests into UnigrationTest.

class AddressLookupServiceTest extends UnitSpec with OneAppPerSuite
  with SequentialNestedSuiteExecution
  with StubbedAddressService {

  implicit private val ec = scala.concurrent.ExecutionContext.Implicits.global

  class Context {
    addressLookupStub.clearExpectations()
    val service = new AddressLookupService(addressLookupEndpoint, "foo", ec)
  }

  val emptyList = "[]"

  val ne15xdLike = AddressRecord("GB4510123533", Some(4510123533L),
    Address(List("10 Taylors Court", "Monk Street", "Byker"),
      Some("Newcastle upon Tyne"), Some("Northumberland"), "NE1 5XD", Some(Countries.England), Countries.UK),
    "en", Some(LocalCustodian(123, "Tyne & Wear")), None, None, None)

  "Find By Postcode" should {
    "fetch an empty list ok" in new Context {
      addressLookupStub.expect(get("/v2/uk/addresses?postcode=NE1+2AB")) thenReturn(200, "application/json", emptyList)
      val actual = await(service.findByPostcode(Postcode("NE1 2AB"), None))
      actual shouldBe Nil
    }

    "fetch a list ok" in new Context {
      addressLookupStub.expect(get("/v2/uk/addresses?postcode=NE1+5XD")) thenReturn(200, "application/json", writeValueAsString(List(ne15xdLike)))
      val actual = await(service.findByPostcode(Postcode("NE1 5XD"), None))
      actual shouldBe List(ne15xdLike)
    }
  }

  "Find By Outcode" should {
    "fetch an empty list ok" in new Context {
      addressLookupStub.expect(get("/v2/uk/addresses?outcode=NE1&filter=FOO")) thenReturn(200, "application/json", emptyList)
      val actual = await(service.findByOutcode(Outcode("NE1"), "FOO"))
      actual shouldBe Nil
    }

    "fetch a list ok" in new Context {
      addressLookupStub.expect(get("/v2/uk/addresses?outcode=NE1&filter=FOO")) thenReturn(200, "application/json", writeValueAsString(List(ne15xdLike)))
      val actual = await(service.findByOutcode(Outcode("NE1"), "FOO"))
      actual shouldBe List(ne15xdLike)
    }
  }

  "Find By UPRN" should {
    "fetch an empty list ok" in new Context {
      addressLookupStub.expect(get("/v2/uk/addresses?uprn=9510123533")) thenReturn(200, "application/json", emptyList)
      val actual = await(service.findByUprn(9510123533L))
      actual shouldBe Nil
    }

    "fetch a list ok" in new Context {
      addressLookupStub.expect(get("/v2/uk/addresses?uprn=4510123533")) thenReturn(200, "application/json", writeValueAsString(List(ne15xdLike)))
      val actual = await(service.findByUprn(4510123533L))
      actual shouldBe List(ne15xdLike)
    }
  }

  "Find By Id" should {
    "return 404" in new Context {
      addressLookupStub.expect(get("/v2/uk/addresses/GB9510123533")) thenReturn(404, "text/plain", "Not found")
      val actual = await(service.findById("GB9510123533"))
      actual shouldBe None
    }

    "fetch a list ok" in new Context {
      addressLookupStub.expect(get("/v2/uk/addresses/GB4510123533")) thenReturn(200, "application/json", writeValueAsString(ne15xdLike))
      val actual = await(service.findById("GB4510123533"))
      actual shouldBe Some(ne15xdLike)
    }
  }

  "Search Fuzzy" should {
    //TODO
  }
}
