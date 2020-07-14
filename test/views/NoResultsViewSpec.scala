/*
 * Copyright 2020 HM Revenue & Customs
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
import controllers.routes
import org.jsoup.Jsoup
import play.api.i18n.{Lang, MessagesApi}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import utils.TestConstants._
import views.html.v2.no_results

class NoResultsViewSpec extends ViewSpec {

  implicit val testRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
  implicit val messagesApi = app.injector.instanceOf[MessagesApi]
  implicit val frontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
  val no_results = app.injector.instanceOf[no_results]

  object EnglishContent {
    val title = "We can not find any addresses"
    def heading(postcode: String) = s"We can not find any addresses for $postcode"
    val back = "Back"
    val tryAgainButton = "Try a different postcode"
    val enterManualLink = "Enter the address manually"
  }

  object WelshContent {
    val title = "Ni allwn ddod o hyd i unrhyw gyfeiriadau"
    def heading(postcode: String) = s"Ni allwn ddod o hyd i unrhyw gyfeiriadau ar gyfer $postcode"
    val back = "Yn ôl"
    val tryAgainButton = "Rhowch gynnig ar god post gwahanol"
    val enterManualLink = "Nodwch y cyfeiriad â llaw"
  }

  "The 'No results' view" when {
    "rendered with the default English config" should {
      implicit val lang: Lang = Lang("en")

      "Render the view and display the Back button" in {
        val noResultsView = no_results(id = testJourneyId, journeyData = journeyDataV2Minimal, postcode = testPostCode)
        val doc = Jsoup.parse(noResultsView.body)

        doc.title shouldBe EnglishContent.title
        doc.getBackLinkText shouldBe EnglishContent.back
        doc.getH1ElementAsText shouldBe EnglishContent.heading(testPostCode)
        doc.getButtonContentAsText shouldBe EnglishContent.tryAgainButton
        doc.getALinkText("enterManual") shouldBe EnglishContent.enterManualLink
        doc.getLinkHrefAsText("enterManual") shouldBe routes.AddressLookupController.edit(testJourneyId, None).url
      }
    }

    "rendered with the default English config (UK Mode = true)" should {
      implicit val lang: Lang = Lang("en")

      "Render the view and display the Back button with UK Mode = true" in {
        val noResultsView = no_results(id = testJourneyId, journeyData = journeyDataV2MinimalUKMode, postcode = testPostCode)
        val doc = Jsoup.parse(noResultsView.body)

        doc.title shouldBe EnglishContent.title
        doc.getBackLinkText shouldBe EnglishContent.back
        doc.getH1ElementAsText shouldBe EnglishContent.heading(testPostCode)
        doc.getButtonContentAsText shouldBe EnglishContent.tryAgainButton
        doc.getALinkText("enterManual") shouldBe EnglishContent.enterManualLink
        doc.getLinkHrefAsText("enterManual") shouldBe routes.AddressLookupController.edit(testJourneyId, None).url
      }
    }

    "rendered with custom English config" should {
      implicit val lang: Lang = Lang("en")

      "Render the view without the Back button" in {
        val noResultsView = no_results(id = testJourneyId, journeyData = testNoResultsConfig, postcode = testPostCode)
        val doc = Jsoup.parse(noResultsView.body)

        doc.title shouldBe EnglishContent.title
        doc.getBackLinkText shouldBe empty
        doc.getH1ElementAsText shouldBe EnglishContent.heading(testPostCode)
        doc.getButtonContentAsText shouldBe EnglishContent.tryAgainButton
        doc.getALinkText("enterManual") shouldBe EnglishContent.enterManualLink
        doc.getLinkHrefAsText("enterManual") shouldBe routes.AddressLookupController.edit(testJourneyId, None).url
      }
    }

    "rendered with the default Welsh config" should {
      implicit val lang: Lang = Lang("cy")

      "Render the view and display the Back button" in {
        val noResultsView = no_results(id = testJourneyId, journeyData = journeyDataV2EnglishAndWelshMinimal, postcode = testPostCode, isWelsh = true)
        val doc = Jsoup.parse(noResultsView.body)

        doc.title shouldBe WelshContent.title
        doc.getBackLinkText shouldBe WelshContent.back
        doc.getH1ElementAsText shouldBe WelshContent.heading(testPostCode)
        doc.getButtonContentAsText shouldBe WelshContent.tryAgainButton
        doc.getALinkText("enterManual") shouldBe WelshContent.enterManualLink
        doc.getLinkHrefAsText("enterManual") shouldBe routes.AddressLookupController.edit(testJourneyId, None).url
      }

      "Render the view and display the Back button with UK Mode = true" in {
        val noResultsView = no_results(id = testJourneyId, journeyData = journeyDataV2EnglishAndWelshMinimalUKMode, postcode = testPostCode, isWelsh = true)
        val doc = Jsoup.parse(noResultsView.body)

        doc.title shouldBe WelshContent.title
        doc.getBackLinkText shouldBe WelshContent.back
        doc.getH1ElementAsText shouldBe WelshContent.heading(testPostCode)
        doc.getButtonContentAsText shouldBe WelshContent.tryAgainButton
        doc.getALinkText("enterManual") shouldBe WelshContent.enterManualLink
        doc.getLinkHrefAsText("enterManual") shouldBe routes.AddressLookupController.edit(testJourneyId, None).url
      }
    }
  }

}
