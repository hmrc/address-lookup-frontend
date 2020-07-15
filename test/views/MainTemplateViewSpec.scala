package views

import config.FrontendAppConfig
import model.MessageConstants.{EnglishMessageConstants => EnglishMessages, WelshMessageConstants => WelshMessages}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.twirl.api.Html
import utils.TestConstants.{testAppLevelJourneyConfigV2, testAppLevelJourneyConfigV2WithWelsh}

class MainTemplateViewSpec extends ViewSpec {

  object enContent {
    val title = "testTitle"
    val additionalStylesheet = "testStylesheetUrl"
    val continueUrl = "testContinueUrl"
    val navHref = "testNavHref"
    val navTitle = "enNavTitle"
    val phaseBannerHtml = "enPhaseBannerHtml"
    val cookies = "Cookies"
    val accessibility = "Accessibility Statement"
    val privacy = "Privacy policy"
    val terms = "Terms and conditions"
    val help = "Help using GOV.UK"
  }

  object cyContent {
    val title = "testTitle"
    val additionalStylesheet = "testStylesheetUrl"
    val continueUrl = "testContinueUrl"
    val navHref = "testNavHref"
    val navTitle = "cyNavTitle"
    val phaseBannerHtml = "cyPhaseBannerHtml"
    val cookies = "Cwcis"
    val accessibility = "Datganiad hygyrchedd"
    val privacy = "Polisi preifatrwydd"
    val terms = "Telerau ac Amodau"
    val help = "Help wrth ddefnyddio GOV.UK"
  }

  implicit val testRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
  val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  val frontendAppConfig = app.injector.instanceOf[FrontendAppConfig]

  implicit val enMessages: Messages = Messages(Lang("en"), messagesApi)
  val testHtml = Html("")
  val EnglishMessageConstants = EnglishMessages(true)
  val WelshMessageConstants = WelshMessages(true)

  "MainTemplate" should {
    "render" when {
      "accessibility footer link when it is passed in the journey data" in {
        val testPage = views.html.v2.main_template(
          frontendAppConfig,title = enContent.title,
          journeyData = Some(testAppLevelJourneyConfigV2)
        )(testHtml)
        val doc: Document = Jsoup.parse(testPage.body)

        doc.getALinkHrefTextByDataJourney("footer:Click:Accessibility") shouldBe "testAccessibilityFooterUrl?service=address-lookup&userAction="
      }

      "accessibility footer link with userAction when it is passed in the journey data" in {
        implicit val testRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "testAction")
        val testPage = views.html.v2.main_template(
          frontendAppConfig,title = enContent.title,
          journeyData = Some(testAppLevelJourneyConfigV2)
        )(testHtml)
        val doc: Document = Jsoup.parse(testPage.body)

        doc.getALinkHrefTextByDataJourney("footer:Click:Accessibility") shouldBe "testAccessibilityFooterUrl?service=address-lookup&userAction=testAction"
      }

      "accessibility footer link with userAction (without the additional query parameters) when it is passed in the journey data" in {
        implicit val testRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "testAction?additionalParams=testParam")
        val testPage = views.html.v2.main_template(
          frontendAppConfig,title = enContent.title,
          journeyData = Some(testAppLevelJourneyConfigV2)
        )(testHtml)
        val doc: Document = Jsoup.parse(testPage.body)

        doc.getALinkHrefTextByDataJourney("footer:Click:Accessibility") shouldBe "testAccessibilityFooterUrl?service=address-lookup&userAction=testAction"
      }

      "only the title is passed in" in {
        val testPage = views.html.v2.main_template(
          frontendAppConfig,title = enContent.title)(testHtml)
        val doc: Document = Jsoup.parse(testPage.body)

        doc.title shouldBe enContent.title
      }

      "title and journeyData without welsh are passed in" when {
        "welsh is enabled" in {
          val testPage = views.html.v2.main_template(
            frontendAppConfig,
            title = enContent.title,
            journeyData = Some(testAppLevelJourneyConfigV2WithWelsh),
            welshEnabled = true
          )(testHtml)
          val doc: Document = Jsoup.parse(testPage.body)

          doc.title shouldBe cyContent.title + " - cyNavTitle - GOV.UK"
          doc.getStyleLinkHrefAsText("customStyleSheet") shouldBe cyContent.additionalStylesheet
          doc.getLinkHrefAsText("homeNavHref") shouldBe cyContent.navHref
          doc.select(".header__menu__proposition-name").text() shouldBe cyContent.navTitle
          doc.getALinkText("homeNavHref") shouldBe WelshMessageConstants.home
          doc.getSpanAsText("phase-banner-content") shouldBe cyContent.phaseBannerHtml
        }
        "welsh is not enabled" in {
          val testPage = views.html.v2.main_template(
            frontendAppConfig,
            title = enContent.title,
            journeyData = Some(testAppLevelJourneyConfigV2)
          )(testHtml)
          val doc: Document = Jsoup.parse(testPage.body)

          doc.title shouldBe enContent.title + " - enNavTitle - GOV.UK"
          doc.getStyleLinkHrefAsText("customStyleSheet") shouldBe enContent.additionalStylesheet
          doc.getLinkHrefAsText("homeNavHref") shouldBe enContent.navHref
          doc.select(".header__menu__proposition-name").text() shouldBe enContent.navTitle
          doc.getALinkText("homeNavHref") shouldBe EnglishMessageConstants.home
          doc.getSpanAsText("phase-banner-content") shouldBe enContent.phaseBannerHtml
          doc.select(".platform-help-links").text shouldBe List(enContent.cookies, enContent.accessibility, enContent.privacy, enContent.terms, enContent.help).mkString(" ")
        }
      }

      "title and journeyData with welsh are passed in" when {
        "welsh is enabled" in {
          val testPage = views.html.v2.main_template(
            frontendAppConfig,
            title = enContent.title,
            journeyData = Some(testAppLevelJourneyConfigV2WithWelsh),
            welshEnabled = true
          )(testHtml)(testRequest, Messages(Lang("cy"), messagesApi))
          val doc: Document = Jsoup.parse(testPage.body)

          doc.title shouldBe enContent.title + " - cyNavTitle - GOV.UK"
          doc.getStyleLinkHrefAsText("customStyleSheet") shouldBe cyContent.additionalStylesheet
          doc.getLinkHrefAsText("homeNavHref") shouldBe cyContent.navHref
          doc.select(".header__menu__proposition-name").text() shouldBe cyContent.navTitle
          doc.getALinkText("homeNavHref") shouldBe WelshMessageConstants.home
          doc.getSpanAsText("phase-banner-content") shouldBe cyContent.phaseBannerHtml
          doc.select(".platform-help-links").text shouldBe List(cyContent.cookies, cyContent.accessibility, cyContent.privacy, cyContent.terms, cyContent.help).mkString(" ")
        }
        "welsh is not enabled" in {
          val testPage = views.html.v2.main_template(
            frontendAppConfig,
            title = enContent.title,
            journeyData = Some(testAppLevelJourneyConfigV2WithWelsh)
          )(testHtml)
          val doc: Document = Jsoup.parse(testPage.body)

          doc.title shouldBe enContent.title + " - enNavTitle - GOV.UK"
          doc.getStyleLinkHrefAsText("customStyleSheet") shouldBe enContent.additionalStylesheet
          doc.getLinkHrefAsText("homeNavHref") shouldBe enContent.navHref
          doc.select(".header__menu__proposition-name").text() shouldBe enContent.navTitle
          doc.getALinkText("homeNavHref") shouldBe EnglishMessageConstants.home
          doc.getSpanAsText("phase-banner-content") shouldBe enContent.phaseBannerHtml
          doc.select(".platform-help-links").text shouldBe List(enContent.cookies, enContent.accessibility, enContent.privacy, enContent.terms, enContent.help).mkString(" ")
        }
      }

      "timeout" should {
        "should display correct text in Welsh mode" in {
          val testPage = views.html.v2.main_template(
            frontendAppConfig,
            title = enContent.title,
            journeyData = Some(testAppLevelJourneyConfigV2),
            welshEnabled = true
          )(testHtml)
          val doc: Document = Jsoup.parse(testPage.body)

          doc.title shouldBe enContent.title
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
            frontendAppConfig,
            title = enContent.title,
            journeyData = Some(testAppLevelJourneyConfigV2)
          )(testHtml)
          val doc: Document = Jsoup.parse(testPage.body)

          doc.title shouldBe enContent.title + " - enNavTitle - GOV.UK"
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
