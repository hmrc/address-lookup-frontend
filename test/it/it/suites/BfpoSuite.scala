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

import address.ViewConfig
import address.outcome.SelectedAddress
import com.pyruby.stubserver.StubMethod
import it.helper.{AppServerTestApi, Context}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatestplus.play._
import play.api.Application
import play.api.libs.json.Json
import play.api.libs.ws.WSResponse
import uk.gov.hmrc.address.v2.Countries._
import uk.gov.hmrc.address.v2._
import uk.gov.hmrc.util.JacksonMapper._

//-------------------------------------------------------------------------------------------------
// This is a long test file to ensure that everything runs in sequence, not overlapping.
// It is also important to start/stop embedded stubs cleanly.
//
// Use the Folds, Luke!!!
//-------------------------------------------------------------------------------------------------

class BfpoSuite(val context: Context)(implicit val app: Application) extends PlaySpec with AppServerTestApi {

  private def addressLookupStub = context.addressLookupStub

  private def keystoreStub = context.keystoreStub

  private def appContext = context.appContext

  private val en = "en"
  private val BF1_3AA = "BF1 3AA"
  private val lcc = LocalCustodian(123, "Town")
  private val bfpoTags = ViewConfig.cfg.filter(_._2.allowBfpo).keys.toList.sorted

  val bf1_3aa = AddressRecord("GB10092787052", Some(10092787052L), Address(List("Bfpo 2"), Some("Bfpo"), None, "BF1 3AA", None, UK), en, Some(lcc), None, None, None)
  val bf1_3ah = AddressRecord("GB10092786554", Some(10092786554L), Address(List("Bfpo 12"), Some("Bfpo"), None, "BF1 3AH", None, UK), en, Some(lcc), None, None, None)

  val edited = Address(List("10b Taylors Court", "Monk Street", "Byker"),
    Some("Newcastle upon Tyne"), Some("Northumberland"), "NE1 5XD", Some(Countries.England), Countries.UK)
  val sr = SelectedAddress(Some(bf1_3aa), Some(edited), None)

  implicit private val ec = scala.concurrent.ExecutionContext.Implicits.global

  "entry form errors" must {
    "when postcode is left blank, remain on the entry form" in {
      keystoreStub.clearExpectations()
      val se1_9py_withoutEdits = SelectedAddress(Some(bf1_3aa), None, None)
      val nfaWithoutEdits = SelectedAddress(None, None, None, None, true)

      for (tag <- bfpoTags) {
        //---------- entry form ----------
        val (cookies, doc1) = step1EntryForm(tag)
        val csrfToken = hiddenCsrfTokenValue(doc1)
        val guid: String = hiddenGuidValue(doc1)

        //---------- confirmation ----------
        val form1NoFixedAddress = Map("csrfToken" -> csrfToken, "guid" -> guid, "continue-url" -> "confirmation", "country-code" -> "UK",
          "number" -> "", "postcode" -> "")
        val response2 = request("POST", s"$appContext/bfpo/addresses/$tag/propose", form1NoFixedAddress, cookies: _*)

        keystoreStub.verify()
        addressLookupStub.verify()
        verifyEntryForm(response2, 400)
      }
    }
  }


  "bfpo address happy-path journeys" must {

    "journey 1: postcode entered; one proposal seen; it is picked without editing" in {
      for (tag <- bfpoTags) {
        addressLookupStub.clearExpectations()
        keystoreStub.clearExpectations()
        val bf1_3aa_withoutEdits = SelectedAddress(Some(bf1_3aa), None, None)

        //---------- entry form ----------
        val (cookies, doc1) = step1EntryForm(s"$tag?id=abc123")
        val csrfToken = hiddenCsrfTokenValue(doc1)
        val guid = hiddenGuidValue(doc1)
        assert(guid === "abc123")

        //---------- proposal form ----------
        addressLookupStub.expect(StubMethod.get("/v2/uk/addresses?postcode=BF1+3AA")) thenReturn(200, "application/json", writeValueAsString(List(bf1_3aa)))

        val form1PostcodeOnly = Map("csrfToken" -> csrfToken, "guid" -> guid, "continue-url" -> "confirmation", "country-code" -> "UK",
          "number" -> "", "postcode" -> "BF1 3AA")
        val response2 = request("POST", s"$appContext/bfpo/addresses/$tag/propose", form1PostcodeOnly, cookies: _*)

        addressLookupStub.verify()
        keystoreStub.verify()
        expectProposalForm(response2, 1, guid, "", "BF1 3AA")

        //---------- confirmation ----------
        addressLookupStub.expect(StubMethod.get("/v2/uk/addresses/GB10092787052")) thenReturn(200, "application/json", writeValueAsString(bf1_3aa))
        keystoreStub.expect(StubMethod.get(s"/keystore/address-lookup/abc123")) thenReturn(200, "application/json", keystoreResponseString(tag, bf1_3aa_withoutEdits))

        val form2PostcodeOnly = Map("csrfToken" -> csrfToken, "guid" -> guid, "continue-url" -> "confirmation", "country-code" -> "UK",
          "number" -> "", "prev-number" -> "", "postcode" -> "BF1 3AA", "prev-postcode" -> "BF1 3AA", "radio-inline-group" -> "GB10092787052")
        val response3 = request("POST", s"$appContext/bfpo/addresses/$tag/select", form2PostcodeOnly, cookies: _*)

        addressLookupStub.verify()
        keystoreStub.verify()
        expectConfirmationPage(response3)

        keystoreStub.expect(StubMethod.get(s"/keystore/address-lookup/abc123")) thenReturn(200, "application/json", keystoreResponseString(tag, bf1_3aa_withoutEdits))

        val outcomeResponse = get(s"$appContext/outcome/$tag/$guid")

        keystoreStub.verify()
        assert(outcomeResponse.status === 200)
        val outcome = readValue(outcomeResponse.body, classOf[SelectedAddress])
        assert(outcome === bf1_3aa_withoutEdits)
      }
    }


    "journey 2: both bfpo number and postcode entered; single proposal seen (based on postcode) and accepted without editing" in {
      for (tag <- bfpoTags) {
        addressLookupStub.clearExpectations()
        keystoreStub.clearExpectations()
        val bf1_3aa_withoutEdits = SelectedAddress(Some(bf1_3aa), None, None)

        //---------- entry form ----------
        val (cookies, doc1) = step1EntryForm(s"$tag?id=abc123")
        val csrfToken = hiddenCsrfTokenValue(doc1)
        val guid = hiddenGuidValue(doc1)
        assert(guid === "abc123")

        //---------- proposal form ----------
        addressLookupStub.expect(StubMethod.get("/v2/uk/addresses?postcode=BF1+3AA")) thenReturn(200, "application/json", writeValueAsString(List(bf1_3aa)))

        val form1NameAndPostcode = Map("csrfToken" -> csrfToken, "guid" -> guid, "continue-url" -> "confirmation", "country-code" -> "UK",
          "number" -> "111", "postcode" -> "BF13AA")
        val response2 = request("POST", s"$appContext/bfpo/addresses/$tag/propose", form1NameAndPostcode, cookies: _*)

        addressLookupStub.verify()
        expectProposalForm(response2, 1, guid, "111", "BF1 3AA")

        //---------- confirmation ----------
        addressLookupStub.expect(StubMethod.get("/v2/uk/addresses/GB10092787052")) thenReturn(200, "application/json", writeValueAsString(bf1_3aa))
        keystoreStub.expect(StubMethod.get(s"/keystore/address-lookup/abc123")) thenReturn(200, "application/json", keystoreResponseString(tag, bf1_3aa_withoutEdits))

        val form2PostcodeAndRadio = Map("csrfToken" -> csrfToken, "guid" -> guid, "continue-url" -> "confirmation", "country-code" -> "UK",
          "number" -> "111", "prev-number" -> "111", "postcode" -> "BF1 3AA", "prev-postcode" -> "BF1 3AA", "radio-inline-group" -> "GB10092787052")
        val response3 = request("POST", s"$appContext/bfpo/addresses/$tag/select", form2PostcodeAndRadio, cookies: _*)

        addressLookupStub.verify()
        keystoreStub.verify()
        val page = expectConfirmationPage(response3)
        assert(page.select("#confirmation .addr .norm").text.trim === bf1_3aa.address.line1, response3.body)
        assert(page.select("#confirmation .town .norm").text.trim === bf1_3aa.address.town.get, response3.body)
        assert(page.select("#confirmation .postcode .norm").text.trim === bf1_3aa.address.postcode, response3.body)
        assert(page.select("#confirmation .country .norm").text.trim === bf1_3aa.address.country.code, response3.body)

        assert(page.select("#confirmation .addr .user").text.trim === "", response3.body)
        assert(page.select("#confirmation .county .user").text.trim === "", response3.body)

        assert(page.select("#confirmation .addr .int").text.trim === "", response3.body)
        assert(page.select("#confirmation .county .int").text.trim === "", response3.body)

        keystoreStub.expect(StubMethod.get(s"/keystore/address-lookup/abc123")) thenReturn(200, "application/json", keystoreResponseString(tag, bf1_3aa_withoutEdits))

        val outcomeResponse = get(s"$appContext/outcome/$tag/$guid")

        keystoreStub.verify()
        assert(outcomeResponse.status === 200)
        val outcome = readValue(outcomeResponse.body, classOf[SelectedAddress])
        assert(outcome === bf1_3aa_withoutEdits)
      }
    }


    "journey 3: bfpo number only entered; single proposal accepted and edited" in {
      for (tag <- bfpoTags) {
        addressLookupStub.clearExpectations()
        keystoreStub.clearExpectations()
        val bf1_3aa_edited = bf1_3aa.address.copy(lines = List("Major Mann", "BFPO 2"))
        val bf1_3aa_withEdits = SelectedAddress(normativeAddress = Some(bf1_3aa), bfpo = Some(international(bf1_3aa_edited)))

        //---------- entry form ----------
        val (cookies, doc1) = step1EntryForm(s"$tag?id=abc123")
        val csrfToken = hiddenCsrfTokenValue(doc1)
        val guid = hiddenGuidValue(doc1)
        assert(guid === "abc123")

        //---------- proposal form ----------
        addressLookupStub.expect(StubMethod.get("/v2/uk/addresses?outcode=BF1&filter=12")) thenReturn(200, "application/json", writeValueAsString(List(bf1_3ah)))

        val form1NameAndPostcode = Map("csrfToken" -> csrfToken, "guid" -> guid, "continue-url" -> "confirmation", "country-code" -> "UK",
          "number" -> "12", "postcode" -> "")
        val response2 = request("POST", s"$appContext/bfpo/addresses/$tag/propose", form1NameAndPostcode, cookies: _*)

        addressLookupStub.verify()
        expectProposalForm(response2, 1, guid, "12", "")

        //---------- make edit request ----------
        addressLookupStub.expect(StubMethod.get("/v2/uk/addresses?outcode=BF1&filter=2")) thenReturn(200, "application/json", writeValueAsString(List(bf1_3aa)))

        val response3 = request("GET", s"$appContext/bfpo/addresses/$tag/get-proposals/2/-/$guid?continue=confirmation&editId=GB10092787052", cookies: _*)

        addressLookupStub.verify()
        keystoreStub.verify()
        expectProposalForm(response3, 1, guid, "2", "")

        //---------- confirmation ----------
        addressLookupStub.expect(StubMethod.get("/v2/uk/addresses/GB10092787052")) thenReturn(200, "application/json", writeValueAsString(bf1_3aa))
        keystoreStub.expect(StubMethod.get(s"/keystore/address-lookup/abc123")) thenReturn(200, "application/json", keystoreResponseString(tag, bf1_3aa_withEdits))

        val form2PostcodeAndRadio = Map("csrfToken" -> csrfToken, "guid" -> guid, "continue-url" -> "confirmation", "country-code" -> "UK",
          "number" -> "2", "prev-number" -> "2", "postcode" -> "", "prev-postcode" -> "", "radio-inline-group" -> "GB10092787052",
          "address-lines" -> "Major Mann\nBFPO 2")

        val response4 = request("POST", s"$appContext/bfpo/addresses/$tag/select", form2PostcodeAndRadio, cookies: _*)

        addressLookupStub.verify()
        keystoreStub.verify()
        val page = expectConfirmationPage(response4)
        assert(page.select("#confirmation .addr .norm").text.trim === "Bfpo 2", response4.body)
        assert(page.select("#confirmation .addr .int").text.trim === "Major Mann BFPO 2", response4.body)
        assert(page.select("#confirmation .postcode .norm").text.trim === "BF1 3AA", response4.body)

        keystoreStub.expect(StubMethod.get(s"/keystore/address-lookup/abc123")) thenReturn(200, "application/json", keystoreResponseString(tag, bf1_3aa_withEdits))

        val outcomeResponse = get(s"$appContext/outcome/$tag/$guid")

        keystoreStub.verify()
        assert(outcomeResponse.status === 200)
        val outcome = readValue(outcomeResponse.body, classOf[SelectedAddress])
        assert(outcome === bf1_3aa_withEdits)
      }
    }


    "journey 4: bfpo number only entered; single proposal seen; bfpo number changed; single proposal seen and accepted without editing" in {
      for (tag <- bfpoTags) {
        addressLookupStub.clearExpectations()
        keystoreStub.clearExpectations()
        val bf1_3aa_withoutEdits = SelectedAddress(Some(bf1_3aa), None, None)

        //---------- entry form ----------
        val (cookies, doc1) = step1EntryForm(s"$tag?id=abc123")
        val csrfToken = hiddenCsrfTokenValue(doc1)
        val guid = hiddenGuidValue(doc1)
        assert(guid === "abc123")

        //---------- proposal form 1 ----------
        addressLookupStub.expect(StubMethod.get("/v2/uk/addresses?outcode=BF1&filter=2")) thenReturn(200, "application/json", writeValueAsString(List(bf1_3aa)))

        val form1HouseAndPostcode = Map("csrfToken" -> csrfToken, "guid" -> guid, "continue-url" -> "confirmation", "country-code" -> "UK",
          "number" -> "2", "postcode" -> "")
        val response2 = request("POST", s"$appContext/bfpo/addresses/$tag/propose", form1HouseAndPostcode, cookies: _*)

        addressLookupStub.verify()
        expectProposalForm(response2, 1, guid, "2", "")

        //---------- proposal form 2 ----------
        addressLookupStub.expect(StubMethod.get("/v2/uk/addresses?outcode=BF1&filter=12")) thenReturn(200, "application/json", writeValueAsString(List(bf1_3ah)))

        val form2APostcodeOnly = Map("csrfToken" -> csrfToken, "guid" -> guid, "continue-url" -> "confirmation", "country-code" -> "UK",
          "number" -> "12", "prev-number" -> "2", "postcode" -> "", "prev-postcode" -> "")
        val response3 = request("POST", s"$appContext/bfpo/addresses/$tag/select", form2APostcodeOnly, cookies: _*)

        addressLookupStub.verify()
        expectProposalForm(response3, 1, guid, "12", "")

        //---------- confirmation ----------
        addressLookupStub.expect(StubMethod.get("/v2/uk/addresses/GB10092786554")) thenReturn(200, "application/json", writeValueAsString(bf1_3ah))
        keystoreStub.expect(StubMethod.get(s"/keystore/address-lookup/abc123")) thenReturn(200, "application/json", keystoreResponseString(tag, bf1_3aa_withoutEdits))

        val form2BPostcodeOnly = Map("csrfToken" -> csrfToken, "guid" -> guid, "continue-url" -> "confirmation", "country-code" -> "UK",
          "number" -> "12", "prev-number" -> "12", "postcode" -> "", "prev-postcode" -> "", "radio-inline-group" -> "GB10092786554")
        val response4 = request("POST", s"$appContext/bfpo/addresses/$tag/select", form2BPostcodeOnly, cookies: _*)

        addressLookupStub.verify()
        keystoreStub.verify()
        expectConfirmationPage(response4)

        keystoreStub.expect(StubMethod.get(s"/keystore/address-lookup/abc123")) thenReturn(200, "application/json", keystoreResponseString(tag, bf1_3aa_withoutEdits))

        val outcomeResponse = get(s"$appContext/outcome/$tag/$guid")

        keystoreStub.verify()
        assert(outcomeResponse.status === 200)
        val outcome = readValue(outcomeResponse.body, classOf[SelectedAddress])
        assert(outcome === bf1_3aa_withoutEdits)
      }
    }
  }


  "bfpo address error journeys" must {
    "landing unexpectedly on the proposal form causes redirection to the blank form" in {
      addressLookupStub.clearExpectations()
      keystoreStub.clearExpectations()

      for (tag <- bfpoTags) {
        val response = get(appContext + s"/bfpo/addresses/$tag/get-proposals/-/-/abc123")
        verifyEntryForm(response, 400)

        keystoreStub.verify()
        addressLookupStub.verify()
      }
    }

    "landing unexpectedly on the confirmation page causes redirection to the blank form" in {
      addressLookupStub.clearExpectations()

      for (tag <- bfpoTags) {
        keystoreStub.clearExpectations()
        keystoreStub.expect(StubMethod.get(s"/keystore/address-lookup/a1c5d2ba")) thenReturn(404, "text/plain", "Not found")

        val response = get(appContext + s"/bfpo/addresses/$tag/confirmation?id=a1c5d2ba")
        verifyEntryForm(response)

        keystoreStub.verify()
        addressLookupStub.verify()
      }
    }
  }


  private def expectProposalForm(response: WSResponse, expectedSize: Int, expectedGuid: String, expectedNumber: String, expectedPostcode: String) {
    assert(response.status === 200, response.body)
    val doc = Jsoup.parse(response.body)
    assert(doc.select("body.proposal-form").size === 1, response.body)
    assert(doc.select("table#Address-table tbody tr").size === expectedSize, response.body)
    assert(hiddenGuidValue(doc) === expectedGuid)
    assert(textBoxValue(doc, "number") === expectedNumber)
    assert(textBoxValue(doc, "postcode") === expectedPostcode)
    assert(hiddenValue(doc, "prev-number") === expectedNumber)
    assert(hiddenValue(doc, "prev-postcode") === expectedPostcode)
  }

  private def expectConfirmationPage(response: WSResponse) = {
    assert(response.status === 200)
    val doc = Jsoup.parse(response.body)
    assert(doc.select("body.confirmation-page").size === 1, response.body)
    doc
  }

  private def step1EntryForm(params: String = ""): (Seq[(String, String)], Document) = {
    val response = get(context.appContext + "/bfpo/addresses/" + params)
    verifyEntryForm(response)
  }

  private def verifyEntryForm(response: WSResponse, expectedCode: Int = 200): (Seq[(String, String)], Document) = {
    assert(response.status === expectedCode)
    val cookies = newCookies(response)
    val doc = Jsoup.parse(response.body)
    assert(doc.select("body.entry-form").size === 1, response.body)
    (cookies, doc)
  }

  private def keystoreResponseJson(tag: String, sa: SelectedAddress) = Json.toJson(Map("data" -> Map(tag -> sa)))

  private def keystoreResponseString(tag: String, sa: SelectedAddress) = Json.stringify(keystoreResponseJson(tag, sa))

  private def i1Json(tag: String, i: International) = keystoreResponseString(tag, SelectedAddress(international = Some(i)))

  private def newCookies(response: WSResponse) = response.cookies.map(c => c.name.get + "=" + c.value.get).map("cookie" -> _)

  private def hiddenCsrfTokenValue(doc: Document) = hiddenValue(doc, "csrfToken")

  private def hiddenGuidValue(doc: Document) = hiddenValue(doc, "guid")

  private def hiddenValue(doc: Document, name: String) = doc.select(s"input[type=hidden][name=$name]").attr("value")

  private def textBoxValue(doc: Document, name: String) = doc.select(s"input[type=text][name=$name]").attr("value")

  private def international(a: Address) = International(a.lines, Some(a.postcode), Some(a.country))
}
