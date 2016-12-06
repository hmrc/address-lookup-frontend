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
import address.uk.AddressRecordWithEdits
import com.pyruby.stubserver.StubMethod
import it.helper.{AppServerTestApi, Context}
import keystore.KeystoreResponse
import keystore.LenientJacksonMapper._
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatestplus.play._
import play.api.Application
import play.api.libs.ws.WSResponse
import uk.gov.hmrc.address.v2.Countries._
import uk.gov.hmrc.address.v2._

class IntSuite(val context: Context)(implicit val app: Application) extends PlaySpec with AppServerTestApi {

  private def addressLookupStub = context.addressLookupStub

  private def keystoreStub = context.keystoreStub

  private def appContext = context.appContext

  private val en = "en"
  private val allTags = ViewConfig.cfg.filter(_._2.allowInternationalAddress).keys.toList.sorted

//  val se1_9py = AddressRecord("GB10091836674", Some(10091836674L), Address(List("Dorset House 27-45", "Stamford Street"), Some("London"), None, "SE1 9PY", Some(England), UK), Some(lcc), en)

  // This sample is a length-2 postcode
//  val ne1_6jn_a = AddressRecord("GB4510737202", Some(4510737202L), Address(List("11 Market Street"), NewcastleUponTyne, TyneAndWear, NE1_6JN, Some(England), UK), Some(lcc), en)
//  val ne1_6jn_b = AddressRecord("GB4510141231", Some(4510141231L), Address(List("Royal House 5-7", "Market Street"), NewcastleUponTyne, TyneAndWear, NE1_6JN, Some(England), UK), Some(lcc), en)

//  val ne15xdLike = AddressRecord("GB4510123533", Some(4510123533L),
//    Address(List("10 Taylors Court", "Monk Street", "Byker"),
//      Some("Newcastle upon Tyne"), Some("Northumberland"), "NE1 5XD", Some(Countries.England), Countries.UK),
//    Some(LocalCustodian(123, "Tyne & Wear")), "en")
//  val edited = Address(List("10b Taylors Court", "Monk Street", "Byker"),
//    Some("Newcastle upon Tyne"), Some("Northumberland"), "NE1 5XD", Some(Countries.England), Countries.UK)
//  val sr = AddressRecordWithEdits(Some(ne15xdLike), Some(edited), false)

  implicit private val ec = scala.concurrent.ExecutionContext.Implicits.global


  "international address happy-path journeys" must {

    "journey 1: country and address entered and submitted" in {
      for (tag <- allTags) {
        addressLookupStub.clearExpectations()
        keystoreStub.clearExpectations()
//        val ne1_6jn_withoutEdits = AddressRecordWithEdits(Some(ne1_6jn_a), None, false)

        //---------- entry form ----------
        val (cookies, doc1) = step1EntryForm(s"$tag?id=abc123")
        val csrfToken = hiddenCsrfTokenValue(doc1)
        val guid = hiddenGuidValue(doc1)
        assert(guid === "abc123")

        //---------- confirmation ----------
//        addressLookupStub.expect(StubMethod.get("/v2/uk/addresses?uprn=4510737202")) thenReturn(200, "application/json", writeValueAsString(List(ne1_6jn_a)))
//        keystoreStub.expect(StubMethod.get(s"/keystore/address-lookup/abc123")) thenReturn(200, "application/json",
//          writeValueAsString(KeystoreResponse(Map(tag -> ne1_6jn_withoutEdits))))
//
//        val form2PostcodeOnly = Map("csrfToken" -> csrfToken, "guid" -> guid, "continue-url" -> "confirmation", "country-code" -> "UK",
//          "house-name-number" -> "", "prev-house-name-number" -> "", "postcode" -> "NE1 6JN", "prev-postcode" -> "NE1 6JN", "radio-inline-group" -> "4510737202")
//        val response3 = request("POST", s"$appContext/uk/addresses/$tag/select", form2PostcodeOnly, cookies: _*)
//
//        addressLookupStub.verify()
//        keystoreStub.verify()
//        expectConfirmationPage(response3)
//
//        keystoreStub.expect(StubMethod.get(s"/keystore/address-lookup/abc123")) thenReturn(200, "application/json",
//          writeValueAsString(KeystoreResponse(Map(tag -> ne1_6jn_withoutEdits))))
//
//        val outcomeResponse = get(s"$appContext/outcome/$tag/$guid")
//
//        keystoreStub.verify()
//        assert(outcomeResponse.status === 200)
//        val outcome = readValue(outcomeResponse.body, classOf[AddressRecordWithEdits])
//        assert(outcome === ne1_6jn_withoutEdits)
      }
    }
  }


//  private def expectProposalForm(response: WSResponse, expectedSize: Int, expectedGuid: String, expectedHouse: String, expectedPostcode: String) {
//    assert(response.status === 200, response.body)
//    val doc = Jsoup.parse(response.body)
//    assert(doc.select("body.proposal-form").size === 1, response.body)
//    assert(doc.select("table#Address-table tbody tr").size === expectedSize, response.body)
//    assert(hiddenGuidValue(doc) === expectedGuid)
//    assert(textBoxValue(doc, "house-name-number") === expectedHouse)
//    assert(textBoxValue(doc, "postcode") === expectedPostcode)
//    assert(hiddenValue(doc, "prev-house-name-number") === expectedHouse)
//    assert(hiddenValue(doc, "prev-postcode") === expectedPostcode)
//  }

//  private def expectConfirmationPage(response: WSResponse) = {
//    assert(response.status === 200)
//    val doc = Jsoup.parse(response.body)
//    assert(doc.select("body.confirmation-page").size === 1, response.body)
//    doc
//  }

  private def step1EntryForm(params: String = ""): (Seq[(String, String)], Document) = {
    val response = get(context.appContext + s"/int/addresses/" + params)
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
