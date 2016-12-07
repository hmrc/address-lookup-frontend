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

package it.suites

import java.nio.charset.StandardCharsets

import address.ViewConfig
import address.uk.AddressRecordWithEdits
import com.pyruby.stubserver.{StubMethod, StubServer}
import it.helper.{AppServerTestApi, Context, Stub}
import keystore.LenientJacksonMapper._
import keystore.{IntKeystoreResponse, KeystoreServiceImpl, MemoMetrics, UkKeystoreResponse}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatestplus.play._
import play.api.Application
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import uk.gov.hmrc.address.v2.Countries._
import uk.gov.hmrc.address.v2._
import uk.gov.hmrc.logging.StubLogger

//-------------------------------------------------------------------------------------------------
// This is a long test file to ensure that everything runs in sequence, not overlapping.
// It is also important to start/stop embedded stubs cleanly.
//
// Use the Folds, Luke!!!
//-------------------------------------------------------------------------------------------------

class KeystoreSuite(val context: Context)(implicit val app: Application) extends PlaySpec with AppServerTestApi {

  private def keystoreStub = context.keystoreStub

  private val en = "en"
  private val NewcastleUponTyne = Some("Newcastle upon Tyne")
  private val TyneAndWear = Some("Tyne & Wear")
  private val NE1_6JN = "NE1 6JN"
  private val lcc = LocalCustodian(123, "Town")
  private val allTags = ViewConfig.cfg.keys.toList.sorted

  val se1_9py = AddressRecord("GB10091836674", Some(10091836674L), Address(List("Dorset House 27-45", "Stamford Street"), Some("London"), None, "SE1 9PY", Some(England), UK), Some(lcc), en)

  // This sample is a length-2 postcode
  val ne1_6jn_a = AddressRecord("GB4510737202", Some(4510737202L), Address(List("11 Market Street"), NewcastleUponTyne, TyneAndWear, NE1_6JN, Some(England), UK), Some(lcc), en)
  val ne1_6jn_b = AddressRecord("GB4510141231", Some(4510141231L), Address(List("Royal House 5-7", "Market Street"), NewcastleUponTyne, TyneAndWear, NE1_6JN, Some(England), UK), Some(lcc), en)

  val ne15xdLike = AddressRecord("GB4510123533", Some(4510123533L),
    Address(List("10 Taylors Court", "Monk Street", "Byker"),
      Some("Newcastle upon Tyne"), Some("Northumberland"), "NE1 5XD", Some(Countries.England), Countries.UK),
    Some(LocalCustodian(123, "Tyne & Wear")), "en")
  val edited = Address(List("10b Taylors Court", "Monk Street", "Byker"),
    Some("Newcastle upon Tyne"), Some("Northumberland"), "NE1 5XD", Some(Countries.England), Countries.UK)
  val sr = AddressRecordWithEdits(Some(ne15xdLike), Some(edited), false)
  val i1 = International(ne15xdLike.address.lines, Some(ne15xdLike.address.postcode), Some(UK))

  implicit private val ec = scala.concurrent.ExecutionContext.Implicits.global

  "keystore" must {

    "fetchSingleUkResponse" must {
      "return an address record when matched" in {
        val logger = new StubLogger(true)
        keystoreStub.clearExpectations()
        val service = new KeystoreServiceImpl(keystoreStub.endpoint, "foo", logger, ec)
        val stubMethod = StubMethod.get("/keystore/address-lookup/id12345")
        val ksr = writeValueAsString(UkKeystoreResponse(Map("j3" -> sr)))
        keystoreStub.expect(stubMethod) thenReturn(200, "application/json", ksr)

        val actual = await(service.fetchSingleUkResponse("j3", "id12345"))

        assert(actual === Some(sr))
        keystoreStub.verify()
        assert(logger.isEmpty, logger.all)
      }

      "return none when not matched" in {
        val logger = new StubLogger(true)
        keystoreStub.clearExpectations()
        val service = new KeystoreServiceImpl(keystoreStub.endpoint, "foo", logger, ec)
        val stubMethod = StubMethod.get("/keystore/address-lookup/id12345")
        keystoreStub.expect(stubMethod) thenReturn(404, "text/plain", "")

        val actual = await(service.fetchSingleUkResponse("j3", "id12345"))

        assert(actual === None)
        keystoreStub.verify()
        assert(logger.isEmpty, logger.all)
      }
    }

    "fetchSingleIntResponse" must {
      "return an address record when matched" in {
        val logger = new StubLogger(true)
        keystoreStub.clearExpectations()
        val service = new KeystoreServiceImpl(keystoreStub.endpoint, "foo", logger, ec)
        val stubMethod = StubMethod.get("/keystore/address-lookup/id12345")
        val ksr = writeValueAsString(IntKeystoreResponse(Map("j3" -> i1)))
        keystoreStub.expect(stubMethod) thenReturn(200, "application/json", ksr)

        val actual = await(service.fetchSingleIntResponse("j3", "id12345"))

        assert(actual === Some(i1))
        keystoreStub.verify()
        assert(logger.isEmpty, logger.all)
      }

      "return none when not matched" in {
        val logger = new StubLogger(true)
        keystoreStub.clearExpectations()
        val service = new KeystoreServiceImpl(keystoreStub.endpoint, "foo", logger, ec)
        val stubMethod = StubMethod.get("/keystore/address-lookup/id12345")
        keystoreStub.expect(stubMethod) thenReturn(404, "text/plain", "")

        val actual = await(service.fetchSingleIntResponse("j3", "id12345"))

        assert(actual === None)
        keystoreStub.verify()
        assert(logger.isEmpty, logger.all)
      }
    }

    "storeSingleUkResponse" must {
      "send the address record to the keystore" in {
        val logger = new StubLogger(true)
        keystoreStub.clearExpectations()
        val service = new KeystoreServiceImpl(keystoreStub.endpoint, "foo", logger, ec)
        val stubMethod = StubMethod.put("/keystore/address-lookup/id12345/data/j3")
        keystoreStub.expect(stubMethod) thenReturn(204, "application/json", "")

        val actual = await(service.storeSingleUkResponse("j3", "id12345", sr))

        assert(actual.status === 204)
        keystoreStub.verify()
        assert(stubMethod.body === writeValueAsString(sr).getBytes(StandardCharsets.UTF_8))
        assert(logger.isEmpty, logger.all)
      }
    }

    "fetchSingleUkResponse with metrics" must {
      "return an address record when matched" in {
        val logger = new StubLogger(true)
        keystoreStub.clearExpectations()
        val peer = new KeystoreServiceImpl(keystoreStub.endpoint, "foo", logger, ec)
        val service = new MemoMetrics(peer, logger, ec)
        val stubMethod = StubMethod.get("/keystore/address-lookup/id12345")
        val ksr = writeValueAsString(UkKeystoreResponse(Map("j3" -> sr)))
        keystoreStub.expect(stubMethod) thenReturn(200, "application/json", ksr)

        val actual = await(service.fetchSingleUkResponse("j3", "id12345"))

        assert(actual === Some(sr))
        keystoreStub.verify()
        assert(logger.infos.map(_.message) === List(s"Keystore get j3 id12345 took {}ms"))
      }
    }

    "storeResponse with metrics" must {
      "send the address record to the keystore" in {
        val logger = new StubLogger(true)
        keystoreStub.clearExpectations()
        val peer = new KeystoreServiceImpl(keystoreStub.endpoint, "foo", logger, ec)
        val service = new MemoMetrics(peer, logger, ec)
        val stubMethod = StubMethod.put("/keystore/address-lookup/id12345/data/j3")
        keystoreStub.expect(stubMethod) thenReturn(204, "application/json", "")

        val actual = await(service.storeSingleUkResponse("j3", "id12345", sr))

        assert(actual.status === 204)
        keystoreStub.verify()
        assert(stubMethod.body === writeValueAsString(sr).getBytes(StandardCharsets.UTF_8))
        assert(logger.infos.map(_.message) === List(s"Keystore put j3 id12345 uprn=4510123533 took {}ms"))
      }
    }

  }


  private def expectProposalForm(response: WSResponse, expectedSize: Int, expectedGuid: String, expectedHouse: String, expectedPostcode: String) {
    assert(response.status === 200, response.body)
    val doc = Jsoup.parse(response.body)
    assert(doc.select("body.proposal-form").size === 1, response.body)
    assert(doc.select("table#Address-table tbody tr").size === expectedSize, response.body)
    assert(hiddenGuidValue(doc) === expectedGuid)
    assert(textBoxValue(doc, "house-name-number") === expectedHouse)
    assert(textBoxValue(doc, "postcode") === expectedPostcode)
    assert(hiddenValue(doc, "prev-house-name-number") === expectedHouse)
    assert(hiddenValue(doc, "prev-postcode") === expectedPostcode)
  }

  private def expectConfirmationPage(response: WSResponse) = {
    assert(response.status === 200)
    val doc = Jsoup.parse(response.body)
    assert(doc.select("body.confirmation-page").size === 1, response.body)
    doc
  }

  private def step1EntryForm(params: String = ""): (Seq[(String, String)], Document) = {
    val response = get(context.appContext + s"/uk/addresses/" + params)
    verifyEntryForm(response)
  }

  private def verifyEntryForm(response: WSResponse, expectedCode: Int = 200): (Seq[(String, String)], Document) = {
    assert(response.status === expectedCode)
    val cookies = newCookies(response)
    val doc = Jsoup.parse(response.body)
    assert(doc.select("body.entry-form").size === 1, response.body)
    (cookies, doc)
  }

  private def newCookies(response: WSResponse) = response.cookies.map(c => c.name.get + "=" + c.value.get).map("cookie" -> _)

  private def hiddenCsrfTokenValue(doc: Document) = hiddenValue(doc, "csrfToken")

  private def hiddenGuidValue(doc: Document) = hiddenValue(doc, "guid")

  private def hiddenValue(doc: Document, name: String) = doc.select(s"input[type=hidden][name=$name]").attr("value")

  private def textBoxValue(doc: Document, name: String) = doc.select(s"input[type=text][name=$name]").attr("value")

}
