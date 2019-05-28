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
    doc.getElementById(elementId)
  }
}
