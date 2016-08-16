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

import address.uk.{Address, AddressRecord}
import address.uk.Countries.UK
import helper.{AppServerTestApi, IntegrationTest}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.SequentialNestedSuiteExecution
import org.scalatestplus.play._
import play.api.libs.ws.WSResponse

class AddressUkTest extends PlaySpec with IntegrationTest with AppServerTestApi with SequentialNestedSuiteExecution {

  private val en = "en"
  private val NewcastleUponTyne = Some("Newcastle upon Tyne")
  private val Northumberland = Some("Northumberland")
  private val NE1_6JN = "NE1 6JN"

  val se1_9py = AddressRecord("GB10091836674", Some(10091836674L), Address(List("Dorset House 27-45", "Stamford Street"), Some("London"), None, "SE1 9PY", Some("GB-ENG"), UK), en)

  // This sample is a length-2 postcode
  val ne1_6jn_a = AddressRecord("GB4510737202", Some(4510737202L), Address(List("11 Market Street"), NewcastleUponTyne, Northumberland, NE1_6JN, Some("GB-ENG"), UK), en)
  val ne1_6jn_b = AddressRecord("GB4510141231", Some(4510141231L), Address(List("Royal House 5-7", "Market Street"), NewcastleUponTyne, Northumberland, NE1_6JN, Some("GB-ENG"), UK), en)

  "uk address happy-path journeys" must {

    "get form without params, post form with no-fixed-address" in {
      val (cookies, doc1) = step1EntryForm("")
      val csrfToken = hiddenCsrfTokenValue(doc1)

      val response2 = request("POST", s"$appContext/uk/addresses/0/propose",
        Map("csrfToken" -> csrfToken, "continue-url" -> "confirmation", "no-fixed-address" -> "true", "house-name-number" -> "", "postcode" -> ""),
        cookies:_*
      )
      assert(response2.status === 200)
      val doc2 = Jsoup.parse(response2.body)
      assert(doc2.select("body.no-fixed-address-page").size === 1, response2.body)
    }


    "get form without params, post form with address that has a single match, post selection to reach confirmation page" in {
      val (cookies, doc1) = step1EntryForm("")
      val csrfToken = hiddenCsrfTokenValue(doc1)

      addressRepStub.givenAddressResponse("/uk/addresses?postcode=SE19PY", List(se1_9py))

      val response2 = request("POST", s"$appContext/uk/addresses/0/propose",
        Map("csrfToken" -> csrfToken, "continue-url" -> "confirmation", "house-name-number" -> "", "postcode" -> "SE19PY"),
        cookies:_*
      )
      assert(response2.status === 200)
      val doc2 = Jsoup.parse(response2.body)
      assert(doc2.select("body.proposal-form").size === 1, response2.body)

      addressRepStub.givenAddressResponse("/uk/addresses?uprn=10091836674", List(se1_9py))

      val response3 = request("POST", s"$appContext/uk/addresses/0/select",
        Map("csrfToken" -> csrfToken, "continue-url" -> "confirmation", "house-name-number" -> "", "postcode" -> "SE19PY", "radio-inline-group" -> "10091836674"),
        cookies:_*
      )
      assert(response3.status === 200)
      val doc3 = Jsoup.parse(response3.body)
      assert(doc3.select("body.confirmation-page").size === 1, response3.body)
    }

  }

  private def step1EntryForm(params: String): (Seq[(String, String)], Document) = {
    val response = get(s"$appContext/uk/addresses/0" + params)
    assert(response.status === 200)
    val cookies = newCookies(response)
    val doc = Jsoup.parse(response.body)
    assert(doc.select("body.entry-form").size === 1, response.body)
    (cookies, doc)
  }

  private def newCookies(response: WSResponse) = response.cookies.map(c => c.name.get + "=" + c.value.get).map("cookie" -> _)

  private def hiddenCsrfTokenValue(doc: Document) = doc.select("input[type=hidden][name=csrfToken]").attr("value")

}
