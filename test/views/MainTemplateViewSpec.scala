package views

import model.MessageConstants._
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
    val phaseBannerHtml = "enPhaseBannerHtml"
  }

  implicit val testRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
  val testHtml = Html("")

  "MainTemplate" should {
    "render" when {
      "only the title is passed in" in {
        val testPage = views.html.v2.main_template(title = content.title)(testHtml)
        val doc: Document = Jsoup.parse(testPage.body)

        doc.title shouldBe content.title
      }

      "title and journeyData are passed in" when {
        "welsh is enabled" in {
          val testPage = views.html.v2.main_template(
            title = content.title,
            journeyData = Some(testAppLevelJourneyConfigV2),
            welshEnabled = true
          )(testHtml)
          val doc: Document = Jsoup.parse(testPage.body)

          doc.title shouldBe content.title
          doc.getStyleLinkHrefAsText("customStyleSheet") shouldBe content.additionalStylesheet
          doc.getLinkHrefAsText("homeNavHref") shouldBe content.navHref
          doc.getALinkText("homeNavHref") shouldBe WelshMessageConstants.home
          doc.getSpanAsText("phase-banner-content") shouldBe content.phaseBannerHtml
        }
        "welsh is not enabled" in {
          val testPage = views.html.v2.main_template(
            title = content.title,
            journeyData = Some(testAppLevelJourneyConfigV2)
          )(testHtml)
          val doc: Document = Jsoup.parse(testPage.body)

          doc.title shouldBe content.title
          doc.getStyleLinkHrefAsText("customStyleSheet") shouldBe content.additionalStylesheet
          doc.getLinkHrefAsText("homeNavHref") shouldBe content.navHref
          doc.getALinkText("homeNavHref") shouldBe EnglishMessageConstants.home
          doc.getSpanAsText("phase-banner-content") shouldBe content.phaseBannerHtml
        }
      }

      "timeout" should {
        "should display correct text in Welsh mode" in {
          val testPage = views.html.v2.main_template(
            title = content.title,
            journeyData = Some(testAppLevelJourneyConfigV2),
            welshEnabled = true
          )(testHtml)
          val doc: Document = Jsoup.parse(testPage.body)

          doc.title shouldBe content.title
          val textOfScript: String = doc.getElementById("timeoutScript").html()

         textOfScript shouldBe
            """$.timeoutDialog({timeout: 120, countdown: 30, """ +
              """time: "eiliad", """ +
              """title: "Rydych ar fin cael eich allgofnodi", """ +
              """message: "Er eich diogelwch, byddwn yn eich allgofnodi cyn pen", """ +
              """minute_text: "munud", """ +
              """minutes_text: "o funudau", """ +
              """keep_alive_button_text: "Ailddechrau eich sesiwn", """ +
              """heading_text: "Rydych wedi bod yn anweithredol am sbel.", """ +
              """keep_alive_url: '/lookup-address/renewSession',logout_url: '/lookup-address/destroySession?timeoutUrl=testTimeoutUrl'});var dialogOpen;"""
        }

        "should display correct text in english mode" in {
          val testPage = views.html.v2.main_template(
            title = content.title,
            journeyData = Some(testAppLevelJourneyConfigV2)
          )(testHtml)
          val doc: Document = Jsoup.parse(testPage.body)

          doc.title shouldBe content.title
          val textOfScript: String = doc.getElementById("timeoutScript").html()

          textOfScript shouldBe
            """$.timeoutDialog({timeout: 120, countdown: 30, """ +
            """time: "seconds", """ +
             """title: "You're about to be signed out", """ +
            """message: "For your security, we'll sign you out in", """ +
            """minute_text: "minute", """ +
            """minutes_text: "minutes", """ +
            """keep_alive_button_text: "Resume your session", """ +
            """heading_text: "You've been inactive for a while.", """ +
              """keep_alive_url: '/lookup-address/renewSession',logout_url: '/lookup-address/destroySession?timeoutUrl=testTimeoutUrl'});var dialogOpen;"""
        }
      }
    }
  }

}
