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
import org.scalatest.Ignore
import play.api.i18n.{Lang, MessagesApi}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import utils.TestConstants._
import views.html.no_results

@Ignore
class NoResultsV1ViewSpec extends ViewSpec {
  implicit val testRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
  implicit val messagesApi = app.injector.instanceOf[MessagesApi]
  val frontendAppConfig = app.injector.instanceOf[FrontendAppConfig]

  object EnglishContent {
    val title = "We cannot find any addresses"
    def heading(postcode: String) = s"We cannot find any addresses for $postcode"
    val back = "Back"
    val tryAgainButton = "Try a different postcode"
    val enterManualLink = "Enter the address manually"
  }

  "The 'No results' view" when {
    "rendered with the default English config" should {
      implicit val lang: Lang = Lang("en")

      "Render the view and display the Back button with UK Mode = false" in {
        val noResultsView = no_results(frontendAppConfig, id = testJourneyId, journeyData = fullV1JourneyData.copy(config = fullV1JourneyConfig.copy(ukMode = Some(false))), postcode = testPostCode)
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
        val noResultsView = no_results(frontendAppConfig, id = testJourneyId, journeyData = fullV1JourneyData, postcode = testPostCode)
        val doc = Jsoup.parse(noResultsView.body)

        doc.title shouldBe EnglishContent.title
        doc.getBackLinkText shouldBe EnglishContent.back
        doc.getH1ElementAsText shouldBe EnglishContent.heading(testPostCode)
        doc.getButtonContentAsText shouldBe EnglishContent.tryAgainButton
        doc.getALinkText("enterManual") shouldBe EnglishContent.enterManualLink
        doc.getLinkHrefAsText("enterManual") shouldBe routes.AddressLookupController.edit(testJourneyId, None).url
      }
    }
  }

}
