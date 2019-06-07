package views

import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import org.scalatest.matchers.{HavePropertyMatchResult, HavePropertyMatcher}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.ExecutionContext

trait ViewSpec extends UnitSpec with GuiceOneAppPerSuite {
  unitSpec: UnitSpec =>

  implicit class DocumentTest(doc: Document) {

    val getParagraphAsText: String = doc.getElementsByTag("p").text()

    val getBulletPointsAsText: String = doc.getElementsByTag("li").text()

    val getH1ElementAsText: String = doc.getElementsByTag("h1").text()

    val getH2ElementAsText: String = doc.getElementsByTag("h2").text()

    val getFormElements: Elements = doc.getElementsByClass("form-field-group")

    val getErrorSummaryMessage: String = doc.select("#error-summary-display ul").text

    def getSpanAsText(id: String): String = doc.select(s"""span[id=$id]""").text()

    def getALinkText(id: String): String = doc.select(s"""a[id=$id]""").text()

    def getLinkHrefAsText(id: String): String = doc.select(s"""a[id=$id]""").attr("href")

    def getStyleLinkHrefAsText(id: String): String = doc.select(s"""link[id=$id]""").attr("href")

    def getTextFieldInput(name: String): Elements = doc.select(s"""input[name=$name]""")

    def getTextFieldLabel(name: String): String = doc.select(s"label[for=$name]").select("span").text()

    def getButtonElements(implicit ec: ExecutionContext): Elements = doc.getElementsByTag("input").filter(_.attr("type") == "submit")

    def getFieldErrorMessage(fieldName: String): String = doc.select(s"#error-message-$fieldName").text

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
