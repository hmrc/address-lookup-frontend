package itutil

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.Assertion
import play.api.libs.ws.WSResponse

import scala.concurrent.{Await, Future}
import play.api.test.Helpers.defaultAwaitTimeout
import uk.gov.hmrc.play.test.UnitSpec

trait PageContentHelper { unitSpec: UnitSpec =>

  def getDocFromResponse(response: Future[WSResponse]): Document =
    Jsoup.parse(Await.result(response, defaultAwaitTimeout.duration).body)

  def testOnPageValuesMatch(response: Future[WSResponse], idValueMapping: Map[String,String]): Unit = {
    val doc = getDocFromResponse(response)

    idValueMapping.foreach { case (elementId: String, expectedValue: String) =>
      doc.getElementById(elementId).`val`() shouldBe expectedValue
    }
  }
  def labelForFieldsMatch(response: Future[WSResponse], idOfFieldExpectedLabelTextForFieldMapping: Map[String,String]): Unit = {
    val doc = getDocFromResponse(response)

    idOfFieldExpectedLabelTextForFieldMapping.foreach { case (fieldId: String, expectedtextOfLabel: String) =>
     await(doc.getElementsByTag("label").filter(_.attr("for") == fieldId)).get(0).text() shouldBe expectedtextOfLabel
    }
  }

  def testElementExists(response: Future[WSResponse], elementId: String): Unit = {
    val doc = getDocFromResponse(response)
    doc.getElementById(elementId)
  }
}
