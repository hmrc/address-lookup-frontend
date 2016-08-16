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

import helper.{AppServerTestApi, IntegrationTest}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.SequentialNestedSuiteExecution
import org.scalatestplus.play._
import play.api.libs.ws.WSResponse

class AddressukTest extends PlaySpec with IntegrationTest with AppServerTestApi with SequentialNestedSuiteExecution {

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
