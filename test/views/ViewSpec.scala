/*
 * Copyright 2023 HM Revenue & Customs
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

import com.codahale.metrics.SharedMetricRegistries
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import org.scalatest.matchers.should.Matchers
import org.scalatest.matchers.{HavePropertyMatchResult, HavePropertyMatcher}
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.i18n.LangImplicits
import play.api.inject.guice.GuiceApplicationBuilder

trait ViewSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite with LangImplicits {
  unitSpec: AnyWordSpec =>

  override implicit lazy val app: Application = {
    SharedMetricRegistries.clear()
    new GuiceApplicationBuilder().build()
  }

  implicit class DocumentTest(doc: Document) {

    val getBackLinkText: String =  doc.select(".govuk-back-link").text()

    val getParagraphAsText: String = doc.getElementsByTag("p").text()

    val getBulletPointsAsText: String = doc.getElementsByTag("li").text()

    val getH1ElementAsText: String = doc.getElementsByTag("h1").text()

    val getH2ElementAsText: String = doc.getElementsByTag("h2").text()

    val getFirstH2ElementAsText: String = doc.getElementsByTag("h2").first().text()

    val getFormElements: Elements = doc.getElementsByClass("govuk-form-group")

    val getErrorSummaryMessage: String = doc.select("#error-summary-display ul").text()

    val getButtonContentAsText: String = doc.select("button[type=submit]").text()

    def getHintAsText(hintClass: String = "govuk-hint"): String = doc.select(s"""div[class=$hintClass]""").text()

    val getFieldErrorMessageHeading: String = doc.select("#error-summary-title").text()

    def getSpanAsText: String = doc.select("span").text()

    def getSpanAsText(id: String): String = doc.select(s"""span[id=$id]""").text()

    def getALinkText(id: String): String = doc.select(s"""a[id=$id]""").text()

    def getALinkHrefTextByDataJourney(dataJourneyText: String): String = doc.select(s"""a[data-journey-click=$dataJourneyText]""").attr("href")

    def getLinkHrefAsText(id: String): String = doc.select(s"""a[id=$id]""").attr("href")

    def getLinkTextFromHref(href: String): String = doc.select(s"""a[href=$href]""").text()

    def getStyleLinkHrefAsText(id: String): String = doc.select(s"""link[id=$id]""").attr("href")

    def hasTextFieldInput(name: String): Boolean = doc.select(s"input[id=$name]").hasAttr("name")

    def getTextFieldInput(name: String): Elements = doc.select(s"""input[name=$name]""")

    def getTextFieldLabel(name: String, textElement: String = "label"): String = doc.select(s"label[for=$name]").text()

    def getSelectOption(name: String): Elements = doc.select(s"""option[name=${name}]""")
    def getSelectOptionValue(name: String): String = getSelectOption(name).attr("value")
    def getSelectOptionLabel(name: String): String = getSelectOption(name).text()

    def getFieldErrorMessageContent(fieldName: String): String = doc.select(s"""a[href=#$fieldName]""").text()

    def paras: Elements = doc.select("p")

    def bulletPointList: Elements = doc.select("ul[class=govuk-list govuk-list-bullet]")

    def getDropList(id:String) = doc.select(s"select[id=$id]")

    def testElementExists(elementId: String) = doc.getElementById(elementId) should not be null

  }

  def option(id: String, value: String): HavePropertyMatcher[Elements, String] =
    new HavePropertyMatcher[Elements, String] {
      def apply(element: Elements) = {
        val span = element.select(s"option[id=$id]")

        HavePropertyMatchResult(
          span.text() == value,
          s"option $id",
          value,
          span.text()
        )
      }
    }
}
