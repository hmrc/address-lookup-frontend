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
import model._
import forms.ALFForms._
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.{Lang, MessagesApi}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import utils.TestConstants._
import views.html.v2.{lookup, non_uk_mode_edit, select, uk_mode_edit}


class NonUKModeEditViewSpec extends ViewSpec {

  object defaultContent {
    val title = "Enter address"
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
    val title = "editTitle"
    val heading = "editHeading"
    val additionalStylesheet = "testStylesheetUrl"
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
  val non_uk_mode_edit = app.injector.instanceOf[non_uk_mode_edit]

  val configWithoutLabels = fullV2JourneyConfig.copy(
    options = fullV2JourneyOptions.copy(ukMode = Some(false)),
    labels = Some(JourneyLabels(
      en = Some(fullV2LanguageLabelsEn.copy(editPageLabels = None))
    )))

  "Non UK Mode Page" should {
    implicit val lang: Lang = Lang("en")

    "when provided with no page config" in {
      val testPage = non_uk_mode_edit(
        id = testId,
        journeyData = fullV2JourneyDataNonUkMode.copy(config = configWithoutLabels),
        editForm = nonUkEditForm(false),
        countries = Seq("FR" -> "France"),
        isWelsh = false
      )
      val doc: Document = Jsoup.parse(testPage.body)

      doc.title shouldBe defaultContent.title
      doc.getH1ElementAsText shouldBe defaultContent.heading
      doc.getTextFieldLabel("line1") shouldBe defaultContent.addressLine1
      doc.getTextFieldInput("line1").`val`() shouldBe ""

      doc.getTextFieldLabel("line2") shouldBe defaultContent.addressLine2
      doc.getTextFieldInput("line2").`val`() shouldBe ""

      doc.getTextFieldLabel("line3") shouldBe defaultContent.addressLine3
      doc.getTextFieldInput("line3").`val`() shouldBe ""

      doc.getTextFieldLabel("town") shouldBe defaultContent.townCity
      doc.getTextFieldInput("town").`val`() shouldBe ""

      doc.getTextFieldLabel("postcode") shouldBe defaultContent.postcodeInternational
      doc.getTextFieldInput("postcode").`val`() shouldBe ""

      doc.getTextFieldLabel("France") shouldBe defaultContent.country
      doc.getTextFieldInput("countryCode").`val`() shouldBe "FR"

      doc.getElementById("continue").text() shouldBe defaultContent.continue
    }

    "when provided with page config" in {
      implicit val lang: Lang = Lang("en")

      val testPage = non_uk_mode_edit(
        id = testId,
        journeyData = fullV2JourneyDataNonUkMode,
        editForm = nonUkEditForm(false),
        countries = Seq("FR" -> "France"),
        isWelsh = false
      )
      val doc: Document = Jsoup.parse(testPage.body)

      doc.title shouldBe configuredContent.title
      doc.getH1ElementAsText shouldBe configuredContent.heading
      doc.getTextFieldLabel("line1") shouldBe configuredContent.addressLine1
      doc.getTextFieldInput("line1").`val`() shouldBe ""

      doc.getTextFieldLabel("line2") shouldBe configuredContent.addressLine2
      doc.getTextFieldInput("line2").`val`() shouldBe ""

      doc.getTextFieldLabel("line3") shouldBe configuredContent.addressLine3
      doc.getTextFieldInput("line3").`val`() shouldBe ""

      doc.getTextFieldLabel("town") shouldBe configuredContent.townCity
      doc.getTextFieldInput("town").`val`() shouldBe ""

      doc.getTextFieldLabel("postcode") shouldBe configuredContent.postcodeInternational
      doc.getTextFieldInput("postcode").`val`() shouldBe ""

      doc.getTextFieldLabel("France") shouldBe configuredContent.country
      doc.getTextFieldInput("countryCode").`val`() shouldBe "FR"

      doc.getElementById("continue").text() shouldBe configuredContent.continue
    }
    "When there is > 1 country" in {
      val testPage = non_uk_mode_edit(
        id = testId,
        journeyData = fullV2JourneyDataNonUkMode.copy(config = configWithoutLabels),
        editForm = nonUkEditForm(false),
        countries = Seq("FR" -> "France", "AL" -> "Albanian"),
        isWelsh = false
      )
      val doc: Document = Jsoup.parse(testPage.body)

      doc.title shouldBe defaultContent.title
      doc.getH1ElementAsText shouldBe defaultContent.heading
      doc.getTextFieldLabel("line1") shouldBe defaultContent.addressLine1
      doc.getTextFieldInput("line1").`val`() shouldBe ""

      doc.getTextFieldLabel("line2") shouldBe defaultContent.addressLine2
      doc.getTextFieldInput("line2").`val`() shouldBe ""

      doc.getTextFieldLabel("line3") shouldBe defaultContent.addressLine3
      doc.getTextFieldInput("line3").`val`() shouldBe ""

      doc.getTextFieldLabel("town") shouldBe defaultContent.townCity
      doc.getTextFieldInput("town").`val`() shouldBe ""

      doc.getTextFieldLabel("postcode") shouldBe defaultContent.postcodeInternational
      doc.getTextFieldInput("postcode").`val`() shouldBe ""

      doc.testElementExists("countryCode")
      doc.getDropList("countryCode").select("option").size() shouldBe 3
      doc.getDropList("countryCode") should have(
        option("countryCode-placeholder", "Select a country"),
        option("countryCode-FR", "France"),
        option("countryCode-AL", "Albanian")
      )

      doc.getElementById("continue").text() shouldBe defaultContent.continue
    }
  }


}
