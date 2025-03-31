/*
 * Copyright 2024 HM Revenue & Customs
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

package views

import config.FrontendAppConfig
import model._
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import utils.TestConstants._
import views.html.abp.confirm

class ConfirmViewSpec extends ViewSpec {
  implicit val testRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
  implicit val frontendConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
  val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  val confirm: confirm = app.injector.instanceOf[confirm]

  "ConfirmView" when {
   implicit val lang: Lang = Lang("en")
    val messages = implicitly[Messages]

    "show back button is true" should {
      val testJourneyConfig = fullV2JourneyDataCustomConfig(testContinueUrl = testContinueUrl)

      val testPage = confirm("", testJourneyConfig, Some(testAddress))
      val doc: Document = Jsoup.parse(testPage.body)

      "show the back button" in {
        doc.getBackLinkText shouldBe messages("constants.back")
      }
    }

    "show back button is false" in {
      val testJourneyConfig = fullV2JourneyDataCustomConfig(testContinueUrl = testContinueUrl, testShowBackButtons = Some(false))
      val testPage = confirm("", testJourneyConfig, Some(testAddress))
      val doc: Document = Jsoup.parse(testPage.body)

      doc.getBackLinkText shouldBe empty
    }

    "show subheading and info is true" in {
      val infoText: String = messages("confirmPage.infoMessage").replace("<kbd>", "").replace("</kbd>", "")
      val testJourneyConfig = fullV2JourneyDataCustomConfig(testContinueUrl = testContinueUrl)

      val testPage = confirm("", testJourneyConfig, Some(testAddress))
      val doc: Document = Jsoup.parse(testPage.body)

      doc.getElementById("infoSubheading").text shouldBe messages("confirmPage.infoSubheading")
      doc.getElementById("infoMessage").text shouldBe infoText
    }

    "show subheading and info is false" in {
      val testJourneyConfig = fullV2JourneyDataCustomConfig(
        testConfirmPageConfig = Some(ConfirmPageConfig(showSubHeadingAndInfo = Some(false))))

      val testPage = confirm("", testJourneyConfig, Some(testAddress))
      val doc: Document = Jsoup.parse(testPage.body)

      Option(doc.getElementById("infoSubheading")) shouldBe None
      Option(doc.getElementById("infoMessage")) shouldBe None
    }

    "show search again link is true " in {
      val testJourneyConfig = fullV2JourneyDataCustomConfig(testContinueUrl = testContinueUrl)

      val testPage = confirm("", testJourneyConfig, Some(testAddress))
      val doc: Document = Jsoup.parse(testPage.body)

      doc.getElementById("searchAgainLink").text shouldBe messages("confirmPage.searchAgainLinkText")
    }

    "show search again link is false " in {
      val testJourneyConfig = fullV2JourneyDataCustomConfig(testContinueUrl = testContinueUrl,
        testConfirmPageConfig = Some(ConfirmPageConfig(showSearchAgainLink = Some(false))))

      val testPage = confirm("", testJourneyConfig, Some(testAddress))
      val doc: Document = Jsoup.parse(testPage.body)

      Option(doc.getElementById("searchAgainLink")) shouldBe None
    }

    "show change link is true " in {
      val testJourneyConfig = fullV2JourneyDataCustomConfig(testContinueUrl = testContinueUrl)

      val testPage = confirm("", testJourneyConfig, Some(testAddress))
      val doc: Document = Jsoup.parse(testPage.body)

      doc.getElementById("changeLink").text shouldBe messages("confirmPage.changeLinkText")
    }

    "show change link is false " in {
      val testJourneyConfig = fullV2JourneyDataCustomConfig(testContinueUrl = testContinueUrl,
        testConfirmPageConfig = Some(ConfirmPageConfig(showChangeLink = Some(false))))

      val testPage = confirm("", testJourneyConfig, Some(testAddress))
      val doc: Document = Jsoup.parse(testPage.body)

      Option(doc.getElementById("changeLink")) shouldBe None
    }

    "show confirm change options is true" in {
      val testJourneyConfig = fullV2JourneyDataCustomConfig(testContinueUrl = testContinueUrl)

      val testPage = confirm("", testJourneyConfig, Some(testAddress))
      val doc: Document = Jsoup.parse(testPage.body)

      doc.getElementById("confirmChangeText").text shouldBe messages("confirmPage.confirmChangeText")
    }

    "show confirm change options is false" in {
      val testJourneyConfig = fullV2JourneyDataCustomConfig(testContinueUrl = testContinueUrl,
        testConfirmPageConfig = Some(ConfirmPageConfig(showConfirmChangeText = Some(false))))

      val testPage = confirm("", testJourneyConfig, Some(testAddress))
      val doc: Document = Jsoup.parse(testPage.body)

      Option(doc.getElementById("confirmChangeText")) shouldBe None
    }
  }
}
