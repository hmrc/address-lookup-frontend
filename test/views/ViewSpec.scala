package views

import com.codahale.metrics.SharedMetricRegistries
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import org.scalatest.{Matchers, WordSpec}
import org.scalatest.matchers.{HavePropertyMatchResult, HavePropertyMatcher}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite

trait ViewSpec extends WordSpec with Matchers with GuiceOneAppPerSuite {
  unitSpec: WordSpec =>
  SharedMetricRegistries.clear()


  implicit class DocumentTest(doc: Document) {

    val getBackLinkText: String =  doc.select(".back-link").text()

    val getParagraphAsText: String = doc.getElementsByTag("p").text()

    val getBulletPointsAsText: String = doc.getElementsByTag("li").text()

    val getH1ElementAsText: String = doc.getElementsByTag("h1").text()

    val getH2ElementAsText: String = doc.getElementsByTag("h2").text()

    val getFirstH2ElementAsText: String = doc.getElementsByTag("h2").first().text()

    val getFormElements: Elements = doc.getElementsByClass("form-field-group")

    val getErrorSummaryMessage: String = doc.select("#error-summary-display ul").text()

    val getButtonContentAsText: String = doc.select("button[type=submit]").text()

    val getHintAsText: String = doc.select(s"""span[class=form-hint]""").text()

    val getFieldErrorMessageHeading: String = doc.select("#error-summary-heading").text()

    def getSpanAsText: String = doc.select("span").text()

    def getSpanAsText(id: String): String = doc.select(s"""span[id=$id]""").text()

    def getALinkText(id: String): String = doc.select(s"""a[id=$id]""").text()

    def getALinkHrefTextByDataJourney(dataJourneyText: String): String = doc.select(s"""a[data-journey-click=$dataJourneyText]""").attr("href")

    def getLinkHrefAsText(id: String): String = doc.select(s"""a[id=$id]""").attr("href")

    def getLinkTextFromHref(href: String): String = doc.select(s"""a[href=$href]""").text()

    def getStyleLinkHrefAsText(id: String): String = doc.select(s"""link[id=$id]""").attr("href")

    def hasTextFieldInput(name: String): Boolean = doc.select(s"input[id=$name]").hasAttr("name")

    def getTextFieldInput(name: String): Elements = doc.select(s"""input[name=$name]""")

    def getTextFieldLabel(name: String): String = doc.select(s"label[for=$name]").select("span").text()

    def getFieldErrorMessageContent(fieldName: String): String = doc.select(s"""a[id=$fieldName-error-summary]""").text()

    def paras: Elements = doc.select("p")

    def bulletPointList: Elements = doc.select("ul[class=list list-bullet]")

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
