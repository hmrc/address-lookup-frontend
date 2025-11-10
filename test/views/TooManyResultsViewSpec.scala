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
import model.v2.{JourneyConfigV2, JourneyDataV2, JourneyOptions}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.{Lang, MessagesApi}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import utils.TestConstants._
import views.html.abp.too_many_results

class TooManyResultsViewSpec extends ViewSpec {
  implicit val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit private val testRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
  implicit private val frontendAppConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
  implicit private val lang: Lang = Lang("en")

  private val too_many_results = app.injector.instanceOf[too_many_results]

  object tooManyResultsMessages {
    def bullet1(postcode: String) = s"$postcode for postcode"

    val bullet2NoFilter = "nothing for property name or number"

    def bullet2WithFilter(filter: String) = s"'$filter' for name or number"

    val tooManyResultsText = "Too many results, enter more details"
    val cannotFindText = "We couldn't find any results for that property name or number"
  }

  private def render(showBackButtons: Boolean = true, firstLookup: Boolean = false, filter: Option[String] = None): Document = {
    val journeyData = JourneyDataV2(JourneyConfigV2(2, JourneyOptions(continueUrl = testContinueUrl, showBackButtons = Some(showBackButtons))))

    Jsoup.parse(too_many_results(id = testJourneyId, journeyData = journeyData, testPostCode, filter, firstLookup = firstLookup).body)
  }

  "The 'Too Many Results' page" when {

    "the back buttons are enabled in the journey config" should {
      val doc = render()

      "not display back button" in {
        doc.getBackLinkText should not be empty
      }
    }

    "the back buttons are not enabled in the journey config" should {
      val doc = render(showBackButtons = false)

      "display back button" in {
        doc.getBackLinkText shouldBe empty
      }
    }

    "when firstLookup" should {
      val doc = render(firstLookup = true)

      "render too many results found text" in {
        doc.getH1ElementAsText shouldBe tooManyResultsMessages.tooManyResultsText
      }
    }

    "when not firstLookup" should {
      val doc = render()

      "render cannot find text" in {
        doc.getH1ElementAsText shouldBe tooManyResultsMessages.cannotFindText
      }
    }

    "no filter has been entered" should {
      val doc = render(filter = None)

      "render the no filter text" in {
        doc.bulletPointList.select("li").first.text shouldBe tooManyResultsMessages.bullet1(testPostCode)
        doc.bulletPointList.select("li").last.text shouldBe tooManyResultsMessages.bullet2NoFilter
      }
    }

    "a filter has been entered" should {
      val doc = render(filter = Some(testFilterValue))

      "render the filter text" in {
        doc.bulletPointList.select("li").first.text shouldBe tooManyResultsMessages.bullet1(testPostCode)
        doc.bulletPointList.select("li").last.text shouldBe tooManyResultsMessages.bullet2WithFilter(testFilterValue)
      }
    }
  }
}
