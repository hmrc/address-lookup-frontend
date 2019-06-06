package views

import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import uk.gov.hmrc.play.test.UnitSpec

trait ViewSpec extends UnitSpec with GuiceOneAppPerSuite {
  unitSpec: UnitSpec =>

  implicit class DocumentTest(doc: Document) {

    val getBackLinkText: String =  doc.select(".back-link").text()

    val getParagraphAsText: String = doc.getElementsByTag("p").text()

    val getBulletPointsAsText: String = doc.getElementsByTag("li").text()

    val getH1ElementAsText: String = doc.getElementsByTag("h1").text()

    val getH2ElementAsText: String = doc.getElementsByTag("h2").text()

    val getFormElements: Elements = doc.getElementsByClass("form-field-group")

    val getErrorSummaryMessage: String = doc.select("#error-summary-display ul").text()

    val getButtonContentAsText: String = doc.select("button[type=submit]").text()

    val getHintAsText: String = doc.select(s"""span[class=form-hint]""").text()

    val getFieldErrorMessageHeading: String = doc.select("#error-summary-heading").text()

    def getSpanAsText: String = doc.select("span").text()

    def getSpanAsText(id: String): String = doc.select(s"""span[id=$id]""").text()

    def getALinkText(id: String): String = doc.select(s"""a[id=$id]""").text()

    def getLinkHrefAsText(id: String): String = doc.select(s"""a[id=$id]""").attr("href")

    def getStyleLinkHrefAsText(id: String): String = doc.select(s"""link[id=$id]""").attr("href")

    def hasTextFieldInput(name: String): Boolean = doc.select(s"input[id=$name]").hasAttr("name")

    def getTextFieldLabel(name: String): String = doc.select(s"label[for=$name]").select("span").text()

    def getFieldErrorMessageContent(fieldName: String): String = doc.select(s"""a[id=$fieldName-error-summary]""").text()
  }

}
