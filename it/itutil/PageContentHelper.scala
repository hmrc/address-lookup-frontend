package itutil

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.libs.ws.WSResponse
import play.api.test.Helpers.defaultAwaitTimeout
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.{Await, Future}

trait PageContentHelper { unitSpec: UnitSpec =>

  def getDocFromResponse(response: Future[WSResponse]): Document =
    Jsoup.parse(Await.result(response, defaultAwaitTimeout.duration).body)

  def testFormElementValuesMatch(response: Future[WSResponse], idValueMapping: Map[String,String]): Unit = {
    val doc = getDocFromResponse(response)

    idValueMapping.foreach { case (elementId: String, expectedValue: String) =>
      doc.getElementById(elementId).`val`() shouldBe expectedValue
    }
  }
  def labelForFieldsMatch(response: Future[WSResponse], idOfFieldExpectedLabelTextForFieldMapping: Map[String,String]): Unit = {
    val elems = getDocFromResponse(response).getElementsByTag("label")
    idOfFieldExpectedLabelTextForFieldMapping.foreach { case (fieldId: String, expectedtextOfLabel: String) =>
      elems.select(s"[for=$fieldId]").get(0).text() shouldBe expectedtextOfLabel
    }
  }

  def testElementExists(response: Future[WSResponse], elementId: String): Unit = {
    val doc = getDocFromResponse(response)
    doc.getElementById(elementId) should not be null
  }
  def testElementDoesntExist(response: Future[WSResponse], elementId: String): Unit = {
    val doc = getDocFromResponse(response)
      doc.getElementById(elementId) shouldBe null
  }
  def testCustomPartsOfGovWrapperElementsForDefaultConfig(response: Future[WSResponse]): Unit = {
    val doc = getDocFromResponse(response)
    doc.getElementsByClass("header__menu__proposition-name").first().text() shouldBe ""
    testElementDoesntExist(response,"phase-banner")
    testElementDoesntExist(response,"customStyleSheet")
    doc.select(".report-error__toggle.js-hidden").first().attr("href") shouldBe "/contact/problem_reports_nonjs?service=AddressLookupFrontend"
    doc.getElementsByClass("report-error").first().child(0).text().contains("""/contact/problem_reports_ajax?service=AddressLookupFrontend""")
  }
}
