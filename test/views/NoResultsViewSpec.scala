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
import model.{JourneyConfigV2, JourneyDataV2, JourneyOptions}
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
  implicit val lang: Lang = Lang("en")


  "The 'No results' view" should {
    "render the view without the back link" in {
      val noResultsView = no_results(id = testJourneyId, journeyData = testNoResultsConfig, postcode = testPostCode)
      val doc = Jsoup.parse(noResultsView.body)

      doc.getBackLinkText shouldBe empty
    }

    "render the view with the back link" in {
      val noResultsView = no_results(id = testJourneyId,
        journeyData = JourneyDataV2(
          config = JourneyConfigV2(2, options = JourneyOptions(continueUrl = "", showBackButtons = Some(true)))),
        postcode = testPostCode)
      val doc = Jsoup.parse(noResultsView.body)

      doc.getBackLinkText should not be empty
    }
  }
}
