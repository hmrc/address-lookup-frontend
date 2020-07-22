package itutil

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import org.scalatest.{Matchers, WordSpec}
import org.scalatest.matchers.{HavePropertyMatchResult, HavePropertyMatcher}
import play.api.libs.ws.WSResponse
import play.api.test.Helpers.defaultAwaitTimeout

import scala.concurrent.{Await, Future}

trait PageContentHelper {
  unitSpec: WordSpec with Matchers =>

  implicit class ViewTestDoc(doc: Document) {
    def title: Elements = doc.select("title")

    def h1: Elements = doc.select("h1")

    def h2s = doc.select("h2")

    def link(id: String): Elements = doc.select(s"a[id=$id")

    def submitButton: Elements = doc.select("button[type=submit]")

    def input(id: String) = doc.select(s"input[id=$id]")

    def radio(id: String) = doc.select(s"input[type=radio][id=$id]")

    def paras = doc.select("p")

    def address = doc.select("div[id=address]")

    def errorSummary = doc.select("div.govuk-error-summary__body")

    def bulletPointList = doc.select("ul[class=list list-bullet]")
  }

  def value(value: String): HavePropertyMatcher[Elements, String] =
    new HavePropertyMatcher[Elements, String] {
      def apply(element: Elements) =
        HavePropertyMatchResult(
          element.`val`() == value,
          "value",
          value,
          element.`val`()
        )
    }

  def href(url: String): HavePropertyMatcher[Elements, String] =
    new HavePropertyMatcher[Elements, String] {
      def apply(element: Elements) =
        HavePropertyMatchResult(
          element.attr("href") == url,
          "href",
          url,
          element.attr("href")
        )
    }

  def text(text: String): HavePropertyMatcher[Elements, String] =
    new HavePropertyMatcher[Elements, String] {
      def apply(element: Elements) =
        HavePropertyMatchResult(
          element.text() == text,
          "text",
          text,
          element.text()
        )
    }

  def label(label: String): HavePropertyMatcher[Elements, String] =
    new HavePropertyMatcher[Elements, String] {
      def apply(element: Elements) = {
        val labelElem = element.parents().get(0).tagName("label")
        val labelForAttr = labelElem.attr("for")

        HavePropertyMatchResult(
          labelForAttr == element.attr("id"),
          "label for attr",
          element.attr("id"),
          labelForAttr
        )

        HavePropertyMatchResult(
          labelElem.text() == label,
          "label text",
          label,
          labelElem.text()
        )
      }
    }

  def elementWithValue(value: String): HavePropertyMatcher[Elements, String] =
    new HavePropertyMatcher[Elements, String] {
      def apply(element: Elements) = {
        val elem = element.select(s":contains($value)")

        HavePropertyMatchResult(
          elem.text() == value,
          "paragraph",
          value,
          elem.text()
        )
      }
    }

  def addressLine(id: String, value: String): HavePropertyMatcher[Elements, String] =
    new HavePropertyMatcher[Elements, String] {
      def apply(element: Elements) = {
        val span = element.select(s"span[id=$id]")

        HavePropertyMatchResult(
          span.text() == value,
          s"address line $id",
          value,
          span.text()
        )
      }
    }

  def errorSummaryMessage(id: String, message: String): HavePropertyMatcher[Elements, String] =
    new HavePropertyMatcher[Elements, String] {
      def apply(element: Elements) = {
        val errorMessage = element.select("a")

        HavePropertyMatchResult(
          errorMessage.text() == message,
          "error summary errors",
          message,
          errorMessage.text()
        )
      }
    }

  def errorMessage(message: String): HavePropertyMatcher[Elements, String] =
    new HavePropertyMatcher[Elements, String] {
      def apply(element: Elements) = {
        val errorMessage = element.parents.first.select("span[class=error-notification")

        HavePropertyMatchResult(
          errorMessage.text() == message,
          "input error message",
          message,
          errorMessage.text()
        )
      }
    }

  def getDocFromResponse(response: Future[WSResponse]): Document =
    Jsoup.parse(Await.result(response, defaultAwaitTimeout.duration).body)

  def testFormElementValuesMatch(response: Future[WSResponse], idValueMapping: Map[String, String]): Unit = {
    val doc = getDocFromResponse(response)

    idValueMapping.foreach { case (elementId: String, expectedValue: String) =>
      doc.getElementById(elementId).`val`() shouldBe expectedValue
    }
  }

  def labelForFieldsMatch(response: Future[WSResponse], idOfFieldExpectedLabelTextForFieldMapping: Map[String, String]): Unit = {
    val elems = getDocFromResponse(response).getElementsByTag("label")
    idOfFieldExpectedLabelTextForFieldMapping.foreach { case (fieldId: String, expectedtextOfLabel: String) =>
      elems.select(s"[for=$fieldId]").get(0).text() shouldBe expectedtextOfLabel
    }
  }

  def testElementExists(response: Future[WSResponse], elementId: String): Unit = {
    val doc = getDocFromResponse(response)
    doc.getElementsByClass(elementId) should not be null
  }

  def testElementDoesntExist(response: Future[WSResponse], elementId: String): Unit = {
    val doc = getDocFromResponse(response)
    doc.getElementById(elementId) shouldBe null
  }

  def testCustomPartsOfGovWrapperElementsForDefaultConfig(response: Future[WSResponse]): Unit = {
    val doc = getDocFromResponse(response)
//    doc.getElementsByClass("header__menu__proposition-name").first().text() shouldBe ""
    testElementDoesntExist(response, "govuk-phase-banner")
    // TODO: Add additionalStyleSheet option support - see v1
    //testElementDoesntExist(response, "customStyleSheet")
    doc.select(".govuk-link").last().attr("href") shouldBe "/contact/problem_reports_nonjs?newTab=true&service=address-lookup-frontend"
    doc.getElementsByClass("govuk-link").last().text().contains("""Get help with this page (opens in a new window or tab)""")
  }

  def testCustomPartsOfGovWrapperElementsForFullConfigAllTrue(response: Future[WSResponse], navTitle: String): Unit = {
    val doc = getDocFromResponse(response)
//    doc.getElementsByClass("header__menu__proposition-name").first().text() shouldBe navTitle
    doc.getElementsByClass("govuk-phase-banner__content__tag").text() shouldBe "alpha"
    doc.getElementsByClass("govuk-phase-banner__content").text() shouldBe "alpha PHASE_BANNER_HTML"
    testElementExists(response, "govuk-phase-banner")
    // TODO: Add additionalStyleSheet option support - see v1
    //doc.getElementById("customStyleSheet").attr("href") shouldBe "ADDITIONAL_STYLESHEET_URL"
//    doc.select(".report-error__toggle.js-hidden").first().attr("href") shouldBe "/contact/problem_reports_nonjs?service=DESKPRO_SERVICE_NAME"
    // /contact/problem_reports_nonjs?newTab=true&service=address-lookup-frontend
    doc.getElementsByClass("govuk-link").last().text().contains("""/contact/problem_reports_ajax?service=deskpro_service_name""")
    // TODO: Re-introduce timeout script support
//    doc.getElementById("timeoutScript").html().contains("timeout: 120") shouldBe true
//    doc.getElementById("timeoutScript").html().contains("/lookup-address/destroySession?timeoutUrl=TIMEOUT_URL") shouldBe true
//    doc.getElementsByClass("copyright").first().child(0).attr("href") shouldBe "https://www.nationalarchives.gov.uk/information-management/our-services/crown-copyright.htm"
  }

  def testCustomPartsOfGovWrapperElementsForFullConfigWithAllTopConfigAsNoneAndAllBooleansFalse(response: Future[WSResponse]): Unit = {
    val doc = getDocFromResponse(response)
//    doc.getElementsByClass("header__menu__proposition-name").first().text() shouldBe ""
    doc.getElementsByClass("govuk-phase-banner__content__tag").first() shouldBe null
    doc.getElementsByClass("govuk-phase-banner__content").first() shouldBe null
    testElementDoesntExist(response, "govuk-phase-banner")
    // TODO: Add additionalStyleSheet option support - see v1
    // testElementDoesntExist(response, "customStyleSheet")
//    doc.select(".report-error__toggle.js-hidden").first().attr("href") shouldBe "/contact/problem_reports_nonjs?service=AddressLookupFrontend"
    doc.getElementsByClass("govuk-link").last().text().contains("""/contact/problem_reports_ajax?service=address_lookup_frontend""")
//    doc.getElementsByTag("script").last().html().contains("timeout: 120") shouldBe false
//    doc.getElementsByTag("script").last().html().contains("/lookup-address/destroySession?timeoutUrl=TIMEOUT_URL") shouldBe false
//    doc.getElementsByClass("copyright").first().child(0).attr("href") shouldBe "https://www.nationalarchives.gov.uk/information-management/our-services/crown-copyright.htm"
  }
}