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

package controllers

import java.nio.charset.StandardCharsets

import address.uk.AddressRecordWithEdits
import com.pyruby.stubserver.StubMethod
import helper.{AppServerTestApi, IntegrationTest}
import keystore.{KeystoreResponse, KeystoreService}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.SequentialNestedSuiteExecution
import org.scalatestplus.play._
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import uk.gov.hmrc.address.v2.Countries._
import uk.gov.hmrc.address.v2._
import uk.gov.hmrc.logging.StubLogger
import uk.gov.hmrc.util.JacksonMapper._

//-------------------------------------------------------------------------------------------------
// This is a long test file to ensure that everything runs in sequence, not overlapping.
// It is also important to start/stop embedded stubs cleanly.
//
// Use the Folds, Luke!!!
//-------------------------------------------------------------------------------------------------

class UnigrationTest extends PlaySpec with IntegrationTest with AppServerTestApi with SequentialNestedSuiteExecution {

  private val en = "en"
  private val NewcastleUponTyne = Some("Newcastle upon Tyne")
  private val Northumberland = Some("Northumberland")
  private val NE1_6JN = "NE1 6JN"
  private val lcc = LocalCustodian(123, "Town")

  val se1_9py = AddressRecord("GB10091836674", Some(10091836674L), Address(List("Dorset House 27-45", "Stamford Street"), Some("London"), None, "SE1 9PY", Some(England), UK), Some(lcc), en)

  // This sample is a length-2 postcode
  val ne1_6jn_a = AddressRecord("GB4510737202", Some(4510737202L), Address(List("11 Market Street"), NewcastleUponTyne, Northumberland, NE1_6JN, Some(England), UK), Some(lcc), en)
  val ne1_6jn_b = AddressRecord("GB4510141231", Some(4510141231L), Address(List("Royal House 5-7", "Market Street"), NewcastleUponTyne, Northumberland, NE1_6JN, Some(England), UK), Some(lcc), en)

  val ne15xdLike = AddressRecord("GB4510123533", Some(4510123533L),
    Address(List("10 Taylors Court", "Monk Street", "Byker"),
      Some("Newcastle upon Tyne"), Some("Northumberland"), "NE1 5XD", Some(Countries.England), Countries.UK),
    Some(LocalCustodian(123, "Tyne & Wear")), "en")
  val edited = Address(List("10b Taylors Court", "Monk Street", "Byker"),
    Some("Newcastle upon Tyne"), Some("Northumberland"), "NE1 5XD", Some(Countries.England), Countries.UK)
  val sr = AddressRecordWithEdits(Some(ne15xdLike), Some(edited), false)

  implicit private val ec = scala.concurrent.ExecutionContext.Implicits.global

  "keystore" must {

    "fetchResponse" must {
      "return an address record when matched" in {
        val logger = new StubLogger(true)
        keystoreStub.clearExpectations()
        val service = new KeystoreService(keystoreEndpoint, "foo", logger)
        val stubMethod = StubMethod.get("/keystore/address-lookup/id12345")
        val ksr = writeValueAsString(KeystoreResponse("id12345", Map("response3" -> sr)))
        keystoreStub.expect(stubMethod) thenReturn(200, "application/json", ksr)

        val actual = await(service.fetchSingleResponse("id12345", 3))

        assert(actual === Some(sr))
        keystoreStub.verify()
      }

      "return none when not matched" in {
        val logger = new StubLogger(true)
        keystoreStub.clearExpectations()
        val service = new KeystoreService(keystoreEndpoint, "foo", logger)
        val stubMethod = StubMethod.get("/keystore/address-lookup/id12345")
        keystoreStub.expect(stubMethod) thenReturn(404, "text/plain", "")

        val actual = await(service.fetchSingleResponse("id12345", 3))

        assert(actual === None)
        keystoreStub.verify()
      }
    }

    "storeResponse" must {
      """
      send the address record to the keystore
      """ in {
        val logger = new StubLogger(true)
        keystoreStub.clearExpectations()
        val service = new KeystoreService(keystoreEndpoint, "foo", logger)
        val stubMethod = StubMethod.put("/keystore/address-lookup/id12345/data/response3")
        keystoreStub.expect(stubMethod) thenReturn(204, "application/json", "")

        val actual = await(service.storeSingleResponse("id12345", 3, sr))

        assert(actual.status === 204)
        keystoreStub.verify()
        assert(stubMethod.body === writeValueAsString(sr).getBytes(StandardCharsets.UTF_8))
      }
    }
  }


  "uk address happy-path journeys" must {

    "get form without params, post form with no-fixed-address" ignore {
      keystoreStub.clearExpectations()
      val nfaWithoutEdits = AddressRecordWithEdits(None, None, true)
      keystoreStub.expect(StubMethod.get("/keystore/address-lookup/abc123/data/response0")) thenReturn(200, "application/json", writeValueAsString(nfaWithoutEdits))

      val (cookies, doc1) = step1EntryForm("0")
      val csrfToken = hiddenCsrfTokenValue(doc1)
      val guid: String = hiddenGuidValue(doc1)

      val response2 = request("POST", s"$appContext/uk/addresses/0/propose",
        Map("csrfToken" -> csrfToken, "guid" -> guid, "continue-url" -> "confirmation", "country-code" -> "UK", "no-fixed-address" -> "true", "house-name-number" -> "", "postcode" -> ""),
        cookies: _*
      )
      assert(response2.status === 200) // note that redirection has been followed
      val doc2 = Jsoup.parse(response2.body)
      assert(doc2.select("body.no-fixed-address-page").size === 1, response2.body)
    }


    "get form without params, post form with address that has a single match, post selection to reach confirmation page" in {
      for (ix <- 0 to 2) {
        addressLookupStub.clearExpectations()
        keystoreStub.clearExpectations()
        val sel9pyList = List(se1_9py)
        val se19pyWithoutEdits = AddressRecordWithEdits(Some(se1_9py), None, false)

        val (cookies, doc1) = step1EntryForm(s"$ix?guid=abc123")
        val csrfToken = hiddenCsrfTokenValue(doc1)
        val guid = hiddenGuidValue(doc1)
        assert(guid === "abc123")

        addressLookupStub.expect(StubMethod.get("/v2/uk/addresses?postcode=SE1%209PY")) thenReturn(200, "application/json", writeValueAsString(sel9pyList))

        val response2 = request("POST", s"$appContext/uk/addresses/$ix/propose",
          Map("csrfToken" -> csrfToken, "guid" -> guid, "continue-url" -> "confirmation", "country-code" -> "UK", "house-name-number" -> "", "postcode" -> "SE19PY"),
          cookies: _*
        )
        assert(response2.status === 200) // note that redirection has been followed
        val doc2 = Jsoup.parse(response2.body)
        assert(doc2.select("body.proposal-form").size === 1, response2.body)
        assert(hiddenGuidValue(doc2) === guid)

        addressLookupStub.expect(StubMethod.get("/v2/uk/addresses?uprn=10091836674")) thenReturn(200, "application/json", writeValueAsString(sel9pyList))
        keystoreStub.expect(StubMethod.get(s"/keystore/address-lookup/abc123")) thenReturn(200, "application/json",
          writeValueAsString(KeystoreResponse("", Map(s"response$ix" -> se19pyWithoutEdits))))

        val response3 = request("POST", s"$appContext/uk/addresses/$ix/select",
          Map("csrfToken" -> csrfToken, "guid" -> guid, "continue-url" -> "confirmation", "country-code" -> "UK", "house-name-number" -> "", "postcode" -> "SE19PY", "radio-inline-group" -> "10091836674"),
          cookies: _*
        )
        assert(response3.status === 200) // note that redirection has been followed
        val doc3 = Jsoup.parse(response3.body)
        assert(doc3.select("body.confirmation-page").size === 1, response3.body)
        //      assert(doc3.select("body.user-supplied-address-page").size === 1, response3.body)
      }
    }

  }


  "uk address error journeys" must {
    "landing unexpectedly on the confirmation page causes redirection to the blank form" in {
      for (ix <- 0 to 2) {
        keystoreStub.clearExpectations()
        keystoreStub.expect(StubMethod.get(s"/keystore/address-lookup/a1c5d2ba")) thenReturn(404, "text/plain", "Not found")

        getHtmlForm(s"/uk/addresses/$ix/confirmation?id=a1c5d2ba", "entry-form")
      }
    }
  }


  private def step1EntryForm(params: String = ""): (Seq[(String, String)], Document) = {
    getHtmlForm(s"/uk/addresses/" + params, "entry-form")
  }

  private def getHtmlForm(path: String, expectedBodyClass: String): (Seq[(String, String)], Document) = {
    val response = get(appContext + path)
    assert(response.status === 200)
    val cookies = newCookies(response)
    val doc = Jsoup.parse(response.body)
    assert(doc.select("body." + expectedBodyClass).size === 1, response.body)
    (cookies, doc)
  }

  private def newCookies(response: WSResponse) = response.cookies.map(c => c.name.get + "=" + c.value.get).map("cookie" -> _)

  private def hiddenCsrfTokenValue(doc: Document) = doc.select("input[type=hidden][name=csrfToken]").attr("value")

  private def hiddenGuidValue(doc: Document) = doc.select("input[type=hidden][name=guid]").attr("value")

}
