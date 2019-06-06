package views

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.MessagesApi
import play.api.i18n.Messages.Implicits._
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.twirl.api.Html
import utils.TestConstants.testAppLevelJourneyConfigV2

class MainTemplateViewSpec extends ViewSpec {

  object content {
    val title = "testTitle"
    val additionalStylesheet = "testStylesheetUrl"
    val continueUrl = "testContinueUrl"
    val navHref = "testNavHref"
    val navText = "Home"
    val phaseBannerHtml = "enPhaseBannerHtml"
  }

  implicit val testRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
  val messages = app.injector.instanceOf[MessagesApi]
  val testHtml = Html("")

  "MainTemplate" should {
    "render" when {
      "only the title is passed in" in {
        val testPage = views.html.v2.main_template(title = content.title)(testHtml)
        val doc: Document = Jsoup.parse(testPage.body)

        doc.title shouldBe content.title
      }

      "title and journeyData are passed in" in {
        val testPage = views.html.v2.main_template(
          title = content.title,
          journeyData = Some(testAppLevelJourneyConfigV2)
        )(testHtml)
        val doc: Document = Jsoup.parse(testPage.body)

        doc.title shouldBe content.title
        doc.getStyleLinkHrefAsText("customStyleSheet")  shouldBe content.additionalStylesheet
        doc.getLinkHrefAsText("homeNavHref") shouldBe content.navHref
        doc.getALinkText("homeNavHref") shouldBe content.navText
        doc.getSpanAsText("phase-banner-content") shouldBe content.phaseBannerHtml

      }
    }
  }

}
