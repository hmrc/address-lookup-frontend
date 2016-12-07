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
import com.pyruby.stubserver.StubMethod
import it.helper.{AppServerTestApi, Context}
import keystore.IntKeystoreResponse
import keystore.LenientJacksonMapper._
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatestplus.play._
import play.api.Application
import play.api.libs.ws.WSResponse
import uk.gov.hmrc.address.v2._

class IntSuite(val context: Context)(implicit val app: Application) extends PlaySpec with AppServerTestApi {

  private def keystoreStub = context.keystoreStub

  private def appContext = context.appContext

  private val en = "en"
  private val allTags = ViewConfig.cfg.filter(_._2.allowInternationalAddress).keys.toList.sorted

  val i1 = International(List("The Metropolitan Museum of Art", "1000 5th Ave", "New York"), Some("NY 10028"), Some(Country("US", "United States")))

  implicit private val ec = scala.concurrent.ExecutionContext.Implicits.global


  "international address happy-path journeys" must {

    "journey 1: country and address entered and submitted" in {
      for (tag <- allTags) {
        keystoreStub.clearExpectations()
        //        val ne1_6jn_withoutEdits = AddressRecordWithEdits(Some(ne1_6jn_a), None, false)

        //---------- entry form ----------
        val (cookies, doc1) = step1EntryForm(s"$tag?id=abc123")
        val csrfToken = hiddenCsrfTokenValue(doc1)
        val guid = hiddenGuidValue(doc1)
        assert(guid === "abc123")

        //---------- confirmation ----------
        keystoreStub.expect(StubMethod.get(s"/keystore/address-lookup/abc123")) thenReturn(200, "application/json",
          writeValueAsString(IntKeystoreResponse(Map(tag -> i1))))

        val form2PostcodeOnly = Map("csrfToken" -> csrfToken, "guid" -> guid, "continue-url" -> "confirmation",
          "country" -> "United States", "code" -> "US", "address" -> "A\nB\nC")
        val response3 = request("POST", s"$appContext/int/addresses/$tag/submit", form2PostcodeOnly, cookies: _*)

        keystoreStub.verify()
        expectConfirmationPage(response3)

        //        keystoreStub.expect(StubMethod.get(s"/keystore/address-lookup/abc123")) thenReturn(200, "application/json",
        //          writeValueAsString(IntKeystoreResponse(Map(tag -> ne1_6jn_withoutEdits))))
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

  private def expectConfirmationPage(response: WSResponse) = {
    assert(response.status === 200)
    val doc = Jsoup.parse(response.body)
    assert(doc.select("body.user-supplied-address-page").size === 1, response.body)
    doc
  }

  private def step1EntryForm(params: String = ""): (Seq[(String, String)], Document) = {
    val response = get(context.appContext + "/int/addresses/" + params)
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
