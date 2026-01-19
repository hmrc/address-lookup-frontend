/*
 * Copyright 2026 HM Revenue & Customs
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

import address.v2.Country
import config.FrontendAppConfig
import forms.ALFForms.countryPickerForm
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.{Lang, MessagesApi}
import play.api.test.FakeRequest
import play.twirl.api.Html
import utils.TestConstants.*
import views.html.country_picker

class CountryPickerViewSpec extends ViewSpec {

  implicit val lang: Lang = Lang("en")
  val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  val countryPickerView: country_picker = app.injector.instanceOf[country_picker]
  implicit val frontendAppConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]

  private def renderPage(showPhaseBanner: Option[Boolean], includeHMRCBranding: Option[Boolean]): Document = {
    val testJourneyConfig = fullV2JourneyDataCustomConfig(
      testShowPhaseBanner = showPhaseBanner,
      testIncludeHMRCBranding = includeHMRCBranding
    )
    val testPage: Html = countryPickerView("", testJourneyConfig, countryPickerForm(), isWelsh = false, countries = Seq.empty[Country])(FakeRequest())
    Jsoup.parse(testPage.body)
  }

  "the phase banner is ENABLED" should {
    "show the HMRC branding when explicitly set to true" in {
      renderPage(Some(true), Some(true)).select("div.hmrc-banner").size.shouldBe(1)
    }

    "NOT show the HMRC branding when explicitly set to false" in {
      renderPage(Some(true), Some(false)).select("div.hmrc-banner").size.shouldBe(0)
    }
  }

  "the phase banner is DISABLED" should {
    "show the HMRC branding when explicitly set to true" in {
      renderPage(Some(false), Some(true)).select("div.hmrc-banner").size.shouldBe(1)
    }

    "NOT show the HMRC branding when explicitly set to false" in {
      renderPage(Some(false), Some(false)).select("div.hmrc-banner").size.shouldBe(0)
    }
  }

}
