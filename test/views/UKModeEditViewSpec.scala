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

import address.v2.Country
import config.FrontendAppConfig
import model._
import forms.ALFForms._
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.{Lang, MessagesApi}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import utils.TestConstants._
import views.html.abp.{lookup, select, uk_mode_edit}

class UKModeEditViewSpec extends ViewSpec {

  object defaultContent {
    val title = "Enter address - navTitle - GOV.UK"
    val heading = "Enter address"
    val addressLine1 = "Address line 1"
    val addressLine2 = "Address line 2 (optional)"
    val addressLine3 = "Address line 3 (optional)"
    val townCity = "Town/city"
    val postcodeInternational = "Postcode (optional)"
    val country = "Country"
    val continue = "Continue"
  }

  object configuredContent {
    val title = "editTitle - navTitle - GOV.UK"
    val heading = "editHeading"
    val addressLine1 = "editLine1"
    val addressLine2 = "editLine2"
    val addressLine3 = "editLine3"
    val townCity = "editLine4"
    val postcodeInternational = "editPostcode"
    val country = "editCountry"
    val continue = "editSubmit"
  }

  implicit val testRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
  val messagesApi = app.injector.instanceOf[MessagesApi]
  implicit val frontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
  val lookup = app.injector.instanceOf[lookup]
  val select = app.injector.instanceOf[select]
  val uk_mode_edit = app.injector.instanceOf[uk_mode_edit]

  val configWithoutLabels = fullV2JourneyConfig.copy(
    options = fullV2JourneyOptions.copy(ukMode = Some(false)),
    labels = Some(JourneyLabels(
      en = Some(fullV2LanguageLabelsEn.copy(editPageLabels = None))
    )))

  "UK Mode Page" should {
    implicit val lang: Lang = Lang("en")

    "the town field should have autocomplete attribute of address-level1" in {
      val testPage = uk_mode_edit(
        id = testId,
        journeyData = fullV2JourneyDataNonUkMode.copy(config = configWithoutLabels),
        editForm = ukEditForm(),
        countries = Seq(Country("FR", "France"), Country("AL", "Albanian")),
        isWelsh = false
      )
      val doc: Document = Jsoup.parse(testPage.body)

      doc.getElementById("town").attr("autocomplete") shouldBe "address-level1"
    
    }
  }
}
