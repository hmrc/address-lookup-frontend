package views

import model._
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages.Implicits._
import play.api.i18n.MessagesApi
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import utils.TestConstants._

class ConfirmViewSpec extends ViewSpec {

  implicit val testRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
  val messagesApi = app.injector.instanceOf[MessagesApi]

  "ConfirmView" should {
    "render English default content" when {
      val infoText: String = JourneyConfigDefaults.EnglishConstants.CONFIRM_PAGE_INFO_MESSAGE_HTML.replace("<kbd>", "").replace("</kbd>", "")
      val testJourneyConfig = fullV2JourneyDataCustomConfig(
        testContinueUrl = testContinueUrl,
        testHomeNavHref = None,
        testAdditionalStylesheetUrl = None,
        testPhaseFeedbackLink = None,
        testDeskProServiceName = None,
        testShowPhaseBanner = None,
        testAlphaPhase = None,
        testShowBackButtons = None,
        testIncludeHMRCBranding = None,
        testUkMode = None,
        testAllowedCountryCodes = None,
        testSelectPage = None,
        testTimeoutConfig = None,
        testLabels = None
      )
      val messages = MessageConstants.EnglishMessageConstants(true)


      "isWelsh is false" when {
        "show back button is true" in {
          val testPage = views.html.v2.confirm("", testJourneyConfig, Some(testAddress), isWelsh = false)
          val doc: Document = Jsoup.parse(testPage.body)

          doc.getBackLinkText shouldBe messages.back
          doc.getH1ElementAsText shouldBe JourneyConfigDefaults.EnglishConstants.CONFIRM_PAGE_HEADING
          doc.getFirstH2ElementAsText shouldBe JourneyConfigDefaults.EnglishConstants.CONFIRM_PAGE_INFO_SUBHEADING
          doc.getElementById("infoMessage").text shouldBe infoText
          doc.getElementById("searchAgainLink").text shouldBe JourneyConfigDefaults.EnglishConstants.SEARCH_AGAIN_LINK_TEXT
          doc.getElementById("changeLink").text shouldBe JourneyConfigDefaults.EnglishConstants.CONFIRM_PAGE_EDIT_LINK_TEXT
          doc.getElementById("confirmChangeText").text shouldBe JourneyConfigDefaults.EnglishConstants.CONFIRM_CHANGE_TEXT
          doc.getButtonContentAsText shouldBe JourneyConfigDefaults.EnglishConstants.CONFIRM_PAGE_SUBMIT_LABEL
        }

        "show back button is false" in {
          val testJourneyConfig = fullV2JourneyDataCustomConfig(
            testContinueUrl = testContinueUrl,
            testHomeNavHref = None,
            testAdditionalStylesheetUrl = None,
            testPhaseFeedbackLink = None,
            testDeskProServiceName = None,
            testShowPhaseBanner = None,
            testAlphaPhase = None,
            testShowBackButtons = Some(false),
            testIncludeHMRCBranding = None,
            testUkMode = None,
            testAllowedCountryCodes = None,
            testSelectPage = None,
            testTimeoutConfig = None,
            testLabels = None
          )
          val testPage = views.html.v2.confirm("", testJourneyConfig, Some(testAddress), isWelsh = false)
          val doc: Document = Jsoup.parse(testPage.body)

          doc.getBackLinkText shouldBe ""
          doc.getH1ElementAsText shouldBe JourneyConfigDefaults.EnglishConstants.CONFIRM_PAGE_HEADING
          doc.getFirstH2ElementAsText shouldBe JourneyConfigDefaults.EnglishConstants.CONFIRM_PAGE_INFO_SUBHEADING
          doc.getElementById("infoMessage").text shouldBe infoText
          doc.getElementById("searchAgainLink").text shouldBe JourneyConfigDefaults.EnglishConstants.SEARCH_AGAIN_LINK_TEXT
          doc.getElementById("changeLink").text shouldBe JourneyConfigDefaults.EnglishConstants.CONFIRM_PAGE_EDIT_LINK_TEXT
          doc.getElementById("confirmChangeText").text shouldBe JourneyConfigDefaults.EnglishConstants.CONFIRM_CHANGE_TEXT
          doc.getButtonContentAsText shouldBe JourneyConfigDefaults.EnglishConstants.CONFIRM_PAGE_SUBMIT_LABEL
        }

        "a confirmed address has been defined" in {
          val testPage = views.html.v2.confirm("", testJourneyConfig, Some(testAddress), isWelsh = false)
          val doc: Document = Jsoup.parse(testPage.body)

          doc.getBackLinkText shouldBe messages.back
          doc.getH1ElementAsText shouldBe JourneyConfigDefaults.EnglishConstants.CONFIRM_PAGE_HEADING
          doc.getFirstH2ElementAsText shouldBe JourneyConfigDefaults.EnglishConstants.CONFIRM_PAGE_INFO_SUBHEADING
          doc.getElementById("infoMessage").text shouldBe infoText
          doc.getElementById("searchAgainLink").text shouldBe JourneyConfigDefaults.EnglishConstants.SEARCH_AGAIN_LINK_TEXT
          doc.getElementById("changeLink").text shouldBe JourneyConfigDefaults.EnglishConstants.CONFIRM_PAGE_EDIT_LINK_TEXT
          doc.getElementById("confirmChangeText").text shouldBe JourneyConfigDefaults.EnglishConstants.CONFIRM_CHANGE_TEXT
          doc.getButtonContentAsText shouldBe JourneyConfigDefaults.EnglishConstants.CONFIRM_PAGE_SUBMIT_LABEL
        }

        "a confirmed address has not been defined" in {
          val testPage = views.html.v2.confirm("", testJourneyConfig, None, isWelsh = false)
          val doc: Document = Jsoup.parse(testPage.body)
          val errorText = s"${messages.confirmSelectedAddressError1} ${messages.confirmSelectedAddressError2}."

          doc.getBackLinkText shouldBe messages.back
          doc.getH1ElementAsText shouldBe JourneyConfigDefaults.EnglishConstants.CONFIRM_PAGE_HEADING
          doc.getFirstH2ElementAsText shouldBe ""
          Option(doc.getElementById("infoMessage")) shouldBe None
          Option(doc.getElementById("searchAgainLink")) shouldBe None
          Option(doc.getElementById("changeLink")) shouldBe None
          Option(doc.getElementById("confirmChangeText")) shouldBe None
          doc.getElementById("lookupLink").parent().text() shouldBe errorText
        }

        "show subheading and info is true" in {
          val testPage = views.html.v2.confirm("", testJourneyConfig, Some(testAddress), isWelsh = false)
          val doc: Document = Jsoup.parse(testPage.body)

          doc.getBackLinkText shouldBe messages.back
          doc.getH1ElementAsText shouldBe JourneyConfigDefaults.EnglishConstants.CONFIRM_PAGE_HEADING
          doc.getFirstH2ElementAsText shouldBe JourneyConfigDefaults.EnglishConstants.CONFIRM_PAGE_INFO_SUBHEADING
          doc.getElementById("infoMessage").text shouldBe infoText
          doc.getElementById("searchAgainLink").text shouldBe JourneyConfigDefaults.EnglishConstants.SEARCH_AGAIN_LINK_TEXT
          doc.getElementById("changeLink").text shouldBe JourneyConfigDefaults.EnglishConstants.CONFIRM_PAGE_EDIT_LINK_TEXT
          doc.getElementById("confirmChangeText").text shouldBe JourneyConfigDefaults.EnglishConstants.CONFIRM_CHANGE_TEXT
          doc.getButtonContentAsText shouldBe JourneyConfigDefaults.EnglishConstants.CONFIRM_PAGE_SUBMIT_LABEL
        }

        "show subheading and info is false" in {
          val testJourneyConfig = fullV2JourneyDataCustomConfig(
            testContinueUrl = testContinueUrl,
            testHomeNavHref = None,
            testAdditionalStylesheetUrl = None,
            testPhaseFeedbackLink = None,
            testDeskProServiceName = None,
            testShowPhaseBanner = None,
            testAlphaPhase = None,
            testShowBackButtons = None,
            testIncludeHMRCBranding = None,
            testUkMode = None,
            testAllowedCountryCodes = None,
            testSelectPage = None,
            testTimeoutConfig = None,
            testConfirmPageConfig = Some(ConfirmPageConfig(
              showSearchAgainLink = Some(true),
              showSubHeadingAndInfo = Some(false),
              showChangeLink = Some(true),
              showConfirmChangeText = Some(true)
            )),
            testLabels = None
          )
          val testPage = views.html.v2.confirm("", testJourneyConfig, Some(testAddress), isWelsh = false)
          val doc: Document = Jsoup.parse(testPage.body)

          doc.getBackLinkText shouldBe messages.back
          doc.getH1ElementAsText shouldBe JourneyConfigDefaults.EnglishConstants.CONFIRM_PAGE_HEADING
          doc.getFirstH2ElementAsText shouldBe ""
          Option(doc.getElementById("infoMessage")) shouldBe None
          doc.getElementById("searchAgainLink").text shouldBe JourneyConfigDefaults.EnglishConstants.SEARCH_AGAIN_LINK_TEXT
          doc.getElementById("changeLink").text shouldBe JourneyConfigDefaults.EnglishConstants.CONFIRM_PAGE_EDIT_LINK_TEXT
          doc.getElementById("confirmChangeText").text shouldBe JourneyConfigDefaults.EnglishConstants.CONFIRM_CHANGE_TEXT
          doc.getButtonContentAsText shouldBe JourneyConfigDefaults.EnglishConstants.CONFIRM_PAGE_SUBMIT_LABEL
        }

        "show search again link is true " in {
          val testPage = views.html.v2.confirm("", testJourneyConfig, Some(testAddress), isWelsh = false)
          val doc: Document = Jsoup.parse(testPage.body)

          doc.getBackLinkText shouldBe messages.back
          doc.getH1ElementAsText shouldBe JourneyConfigDefaults.EnglishConstants.CONFIRM_PAGE_HEADING
          doc.getFirstH2ElementAsText shouldBe JourneyConfigDefaults.EnglishConstants.CONFIRM_PAGE_INFO_SUBHEADING
          doc.getElementById("infoMessage").text shouldBe infoText
          doc.getElementById("searchAgainLink").text shouldBe JourneyConfigDefaults.EnglishConstants.SEARCH_AGAIN_LINK_TEXT
          doc.getElementById("changeLink").text shouldBe JourneyConfigDefaults.EnglishConstants.CONFIRM_PAGE_EDIT_LINK_TEXT
          doc.getElementById("confirmChangeText").text shouldBe JourneyConfigDefaults.EnglishConstants.CONFIRM_CHANGE_TEXT
          doc.getButtonContentAsText shouldBe JourneyConfigDefaults.EnglishConstants.CONFIRM_PAGE_SUBMIT_LABEL
        }

        "show search again link is false " in {
          val testJourneyConfig = fullV2JourneyDataCustomConfig(
            testContinueUrl = testContinueUrl,
            testHomeNavHref = None,
            testAdditionalStylesheetUrl = None,
            testPhaseFeedbackLink = None,
            testDeskProServiceName = None,
            testShowPhaseBanner = None,
            testAlphaPhase = None,
            testShowBackButtons = None,
            testIncludeHMRCBranding = None,
            testUkMode = None,
            testAllowedCountryCodes = None,
            testSelectPage = None,
            testTimeoutConfig = None,
            testConfirmPageConfig = Some(ConfirmPageConfig(
              showSearchAgainLink = Some(false),
              showSubHeadingAndInfo = Some(true),
              showChangeLink = Some(true),
              showConfirmChangeText = Some(true)
            )),
            testLabels = None
          )
          val testPage = views.html.v2.confirm("", testJourneyConfig, Some(testAddress), isWelsh = false)
          val doc: Document = Jsoup.parse(testPage.body)

          doc.getBackLinkText shouldBe messages.back
          doc.getH1ElementAsText shouldBe JourneyConfigDefaults.EnglishConstants.CONFIRM_PAGE_HEADING
          doc.getFirstH2ElementAsText shouldBe JourneyConfigDefaults.EnglishConstants.CONFIRM_PAGE_INFO_SUBHEADING
          doc.getElementById("infoMessage").text shouldBe infoText
          Option(doc.getElementById("searchAgainLink")) shouldBe None
          doc.getElementById("changeLink").text shouldBe JourneyConfigDefaults.EnglishConstants.CONFIRM_PAGE_EDIT_LINK_TEXT
          doc.getElementById("confirmChangeText").text shouldBe JourneyConfigDefaults.EnglishConstants.CONFIRM_CHANGE_TEXT
          doc.getButtonContentAsText shouldBe JourneyConfigDefaults.EnglishConstants.CONFIRM_PAGE_SUBMIT_LABEL
        }

        "show change link is true " in {
          val testPage = views.html.v2.confirm("", testJourneyConfig, Some(testAddress), isWelsh = false)
          val doc: Document = Jsoup.parse(testPage.body)

          doc.getBackLinkText shouldBe messages.back
          doc.getH1ElementAsText shouldBe JourneyConfigDefaults.EnglishConstants.CONFIRM_PAGE_HEADING
          doc.getFirstH2ElementAsText shouldBe JourneyConfigDefaults.EnglishConstants.CONFIRM_PAGE_INFO_SUBHEADING
          doc.getElementById("infoMessage").text shouldBe infoText
          doc.getElementById("searchAgainLink").text shouldBe JourneyConfigDefaults.EnglishConstants.SEARCH_AGAIN_LINK_TEXT
          doc.getElementById("changeLink").text shouldBe JourneyConfigDefaults.EnglishConstants.CONFIRM_PAGE_EDIT_LINK_TEXT
          doc.getElementById("confirmChangeText").text shouldBe JourneyConfigDefaults.EnglishConstants.CONFIRM_CHANGE_TEXT
          doc.getButtonContentAsText shouldBe JourneyConfigDefaults.EnglishConstants.CONFIRM_PAGE_SUBMIT_LABEL
        }

        "show change link is false " in {
          val testJourneyConfig = fullV2JourneyDataCustomConfig(
            testContinueUrl = testContinueUrl,
            testHomeNavHref = None,
            testAdditionalStylesheetUrl = None,
            testPhaseFeedbackLink = None,
            testDeskProServiceName = None,
            testShowPhaseBanner = None,
            testAlphaPhase = None,
            testShowBackButtons = None,
            testIncludeHMRCBranding = None,
            testUkMode = None,
            testAllowedCountryCodes = None,
            testSelectPage = None,
            testTimeoutConfig = None,
            testConfirmPageConfig = Some(ConfirmPageConfig(
              showSearchAgainLink = Some(true),
              showSubHeadingAndInfo = Some(true),
              showChangeLink = Some(false),
              showConfirmChangeText = Some(true)
            )),
            testLabels = None
          )
          val testPage = views.html.v2.confirm("", testJourneyConfig, Some(testAddress), isWelsh = false)
          val doc: Document = Jsoup.parse(testPage.body)

          doc.getBackLinkText shouldBe messages.back
          doc.getH1ElementAsText shouldBe JourneyConfigDefaults.EnglishConstants.CONFIRM_PAGE_HEADING
          doc.getFirstH2ElementAsText shouldBe JourneyConfigDefaults.EnglishConstants.CONFIRM_PAGE_INFO_SUBHEADING
          doc.getElementById("infoMessage").text shouldBe infoText
          doc.getElementById("searchAgainLink").text shouldBe JourneyConfigDefaults.EnglishConstants.SEARCH_AGAIN_LINK_TEXT
          Option(doc.getElementById("changeLink")) shouldBe None
          doc.getElementById("confirmChangeText").text shouldBe JourneyConfigDefaults.EnglishConstants.CONFIRM_CHANGE_TEXT
          doc.getButtonContentAsText shouldBe JourneyConfigDefaults.EnglishConstants.CONFIRM_PAGE_SUBMIT_LABEL
        }

        "show confirm change options is true" in {
          val testPage = views.html.v2.confirm("", testJourneyConfig, Some(testAddress), isWelsh = false)
          val doc: Document = Jsoup.parse(testPage.body)

          doc.getBackLinkText shouldBe messages.back
          doc.getH1ElementAsText shouldBe JourneyConfigDefaults.EnglishConstants.CONFIRM_PAGE_HEADING
          doc.getFirstH2ElementAsText shouldBe JourneyConfigDefaults.EnglishConstants.CONFIRM_PAGE_INFO_SUBHEADING
          doc.getElementById("infoMessage").text shouldBe infoText
          doc.getElementById("searchAgainLink").text shouldBe JourneyConfigDefaults.EnglishConstants.SEARCH_AGAIN_LINK_TEXT
          doc.getElementById("changeLink").text shouldBe JourneyConfigDefaults.EnglishConstants.CONFIRM_PAGE_EDIT_LINK_TEXT
          doc.getElementById("confirmChangeText").text shouldBe JourneyConfigDefaults.EnglishConstants.CONFIRM_CHANGE_TEXT
          doc.getButtonContentAsText shouldBe JourneyConfigDefaults.EnglishConstants.CONFIRM_PAGE_SUBMIT_LABEL
        }

        "show confirm change options is false" in {
          val testJourneyConfig = fullV2JourneyDataCustomConfig(
            testContinueUrl = testContinueUrl,
            testHomeNavHref = None,
            testAdditionalStylesheetUrl = None,
            testPhaseFeedbackLink = None,
            testDeskProServiceName = None,
            testShowPhaseBanner = None,
            testAlphaPhase = None,
            testShowBackButtons = None,
            testIncludeHMRCBranding = None,
            testUkMode = None,
            testAllowedCountryCodes = None,
            testSelectPage = None,
            testTimeoutConfig = None,
            testConfirmPageConfig = Some(ConfirmPageConfig(
              showSearchAgainLink = Some(true),
              showSubHeadingAndInfo = Some(true),
              showChangeLink = Some(true),
              showConfirmChangeText = Some(false)
            )),
            testLabels = None
          )
          val testPage = views.html.v2.confirm("", testJourneyConfig, Some(testAddress), isWelsh = false)
          val doc: Document = Jsoup.parse(testPage.body)

          doc.getBackLinkText shouldBe messages.back
          doc.getH1ElementAsText shouldBe JourneyConfigDefaults.EnglishConstants.CONFIRM_PAGE_HEADING
          doc.getFirstH2ElementAsText shouldBe JourneyConfigDefaults.EnglishConstants.CONFIRM_PAGE_INFO_SUBHEADING
          doc.getElementById("infoMessage").text shouldBe infoText
          doc.getElementById("searchAgainLink").text shouldBe JourneyConfigDefaults.EnglishConstants.SEARCH_AGAIN_LINK_TEXT
          doc.getElementById("changeLink").text shouldBe JourneyConfigDefaults.EnglishConstants.CONFIRM_PAGE_EDIT_LINK_TEXT
          Option(doc.getElementById("confirmChangeText")) shouldBe None
          doc.getButtonContentAsText shouldBe JourneyConfigDefaults.EnglishConstants.CONFIRM_PAGE_SUBMIT_LABEL
        }
      }
    }

    "render Welsh default content" when {
      val infoText: String = JourneyConfigDefaults.WelshConstants.CONFIRM_PAGE_INFO_MESSAGE_HTML.replace("<kbd>", "").replace("</kbd>", "")
      val testJourneyConfig = fullV2JourneyDataCustomConfig(
        testContinueUrl = testContinueUrl,
        testHomeNavHref = None,
        testAdditionalStylesheetUrl = None,
        testPhaseFeedbackLink = None,
        testDeskProServiceName = None,
        testShowPhaseBanner = None,
        testAlphaPhase = None,
        testShowBackButtons = None,
        testIncludeHMRCBranding = None,
        testUkMode = None,
        testAllowedCountryCodes = None,
        testSelectPage = None,
        testTimeoutConfig = None,
        testLabels = Some(JourneyLabels(
          cy = Some(LanguageLabels())
        ))
      )
      val messages = MessageConstants.WelshMessageConstants(true)

      "isWelsh is true" when {
        "show back button is true" in {
          val testPage = views.html.v2.confirm("", testJourneyConfig, Some(testAddress), isWelsh = true)
          val doc: Document = Jsoup.parse(testPage.body)

          doc.getBackLinkText shouldBe messages.back
          doc.getH1ElementAsText shouldBe JourneyConfigDefaults.WelshConstants.CONFIRM_PAGE_HEADING
          doc.getFirstH2ElementAsText shouldBe JourneyConfigDefaults.WelshConstants.CONFIRM_PAGE_INFO_SUBHEADING
          doc.getElementById("infoMessage").text shouldBe infoText
          doc.getElementById("searchAgainLink").text shouldBe JourneyConfigDefaults.WelshConstants.SEARCH_AGAIN_LINK_TEXT
          doc.getElementById("changeLink").text shouldBe JourneyConfigDefaults.WelshConstants.CONFIRM_PAGE_EDIT_LINK_TEXT
          doc.getElementById("confirmChangeText").text shouldBe JourneyConfigDefaults.WelshConstants.CONFIRM_CHANGE_TEXT
          doc.getButtonContentAsText shouldBe JourneyConfigDefaults.WelshConstants.CONFIRM_PAGE_SUBMIT_LABEL
        }

        "show back button is false" in {
          val testJourneyConfig = fullV2JourneyDataCustomConfig(
            testContinueUrl = testContinueUrl,
            testHomeNavHref = None,
            testAdditionalStylesheetUrl = None,
            testPhaseFeedbackLink = None,
            testDeskProServiceName = None,
            testShowPhaseBanner = None,
            testAlphaPhase = None,
            testShowBackButtons = Some(false),
            testIncludeHMRCBranding = None,
            testUkMode = None,
            testAllowedCountryCodes = None,
            testSelectPage = None,
            testTimeoutConfig = None,
            testLabels = Some(JourneyLabels(
              cy = Some(LanguageLabels())
            ))
          )
          val testPage = views.html.v2.confirm("", testJourneyConfig, Some(testAddress), isWelsh = true)
          val doc: Document = Jsoup.parse(testPage.body)

          doc.getBackLinkText shouldBe ""
          doc.getH1ElementAsText shouldBe JourneyConfigDefaults.WelshConstants.CONFIRM_PAGE_HEADING
          doc.getFirstH2ElementAsText shouldBe JourneyConfigDefaults.WelshConstants.CONFIRM_PAGE_INFO_SUBHEADING
          doc.getElementById("infoMessage").text shouldBe infoText
          doc.getElementById("searchAgainLink").text shouldBe JourneyConfigDefaults.WelshConstants.SEARCH_AGAIN_LINK_TEXT
          doc.getElementById("changeLink").text shouldBe JourneyConfigDefaults.WelshConstants.CONFIRM_PAGE_EDIT_LINK_TEXT
          doc.getElementById("confirmChangeText").text shouldBe JourneyConfigDefaults.WelshConstants.CONFIRM_CHANGE_TEXT
          doc.getButtonContentAsText shouldBe JourneyConfigDefaults.WelshConstants.CONFIRM_PAGE_SUBMIT_LABEL
        }

        "a confirmed address has been defined" in {
          val testPage = views.html.v2.confirm("", testJourneyConfig, Some(testAddress), isWelsh = true)
          val doc: Document = Jsoup.parse(testPage.body)

          doc.getBackLinkText shouldBe messages.back
          doc.getH1ElementAsText shouldBe JourneyConfigDefaults.WelshConstants.CONFIRM_PAGE_HEADING
          doc.getFirstH2ElementAsText shouldBe JourneyConfigDefaults.WelshConstants.CONFIRM_PAGE_INFO_SUBHEADING
          doc.getElementById("infoMessage").text shouldBe infoText
          doc.getElementById("searchAgainLink").text shouldBe JourneyConfigDefaults.WelshConstants.SEARCH_AGAIN_LINK_TEXT
          doc.getElementById("changeLink").text shouldBe JourneyConfigDefaults.WelshConstants.CONFIRM_PAGE_EDIT_LINK_TEXT
          doc.getElementById("confirmChangeText").text shouldBe JourneyConfigDefaults.WelshConstants.CONFIRM_CHANGE_TEXT
          doc.getButtonContentAsText shouldBe JourneyConfigDefaults.WelshConstants.CONFIRM_PAGE_SUBMIT_LABEL
        }

        "a confirmed address has not been defined" in {
          val testPage = views.html.v2.confirm("", testJourneyConfig, None, isWelsh = true)
          val doc: Document = Jsoup.parse(testPage.body)
          val errorText = s"${messages.confirmSelectedAddressError1} ${messages.confirmSelectedAddressError2}."

          doc.getBackLinkText shouldBe messages.back
          doc.getH1ElementAsText shouldBe JourneyConfigDefaults.WelshConstants.CONFIRM_PAGE_HEADING
          doc.getFirstH2ElementAsText shouldBe ""
          Option(doc.getElementById("infoMessage")) shouldBe None
          Option(doc.getElementById("searchAgainLink")) shouldBe None
          Option(doc.getElementById("changeLink")) shouldBe None
          Option(doc.getElementById("confirmChangeText")) shouldBe None
          doc.getElementById("lookupLink").parent().text() shouldBe errorText
        }

        "show subheading and info is true" in {
          val testPage = views.html.v2.confirm("", testJourneyConfig, Some(testAddress), isWelsh = true)
          val doc: Document = Jsoup.parse(testPage.body)

          doc.getBackLinkText shouldBe messages.back
          doc.getH1ElementAsText shouldBe JourneyConfigDefaults.WelshConstants.CONFIRM_PAGE_HEADING
          doc.getFirstH2ElementAsText shouldBe JourneyConfigDefaults.WelshConstants.CONFIRM_PAGE_INFO_SUBHEADING
          doc.getElementById("infoMessage").text shouldBe infoText
          doc.getElementById("searchAgainLink").text shouldBe JourneyConfigDefaults.WelshConstants.SEARCH_AGAIN_LINK_TEXT
          doc.getElementById("changeLink").text shouldBe JourneyConfigDefaults.WelshConstants.CONFIRM_PAGE_EDIT_LINK_TEXT
          doc.getElementById("confirmChangeText").text shouldBe JourneyConfigDefaults.WelshConstants.CONFIRM_CHANGE_TEXT
          doc.getButtonContentAsText shouldBe JourneyConfigDefaults.WelshConstants.CONFIRM_PAGE_SUBMIT_LABEL
        }

        "show subheading and info is false" in {
          val testJourneyConfig = fullV2JourneyDataCustomConfig(
            testContinueUrl = testContinueUrl,
            testHomeNavHref = None,
            testAdditionalStylesheetUrl = None,
            testPhaseFeedbackLink = None,
            testDeskProServiceName = None,
            testShowPhaseBanner = None,
            testAlphaPhase = None,
            testShowBackButtons = None,
            testIncludeHMRCBranding = None,
            testUkMode = None,
            testAllowedCountryCodes = None,
            testSelectPage = None,
            testTimeoutConfig = None,
            testConfirmPageConfig = Some(ConfirmPageConfig(
              showSearchAgainLink = Some(true),
              showSubHeadingAndInfo = Some(false),
              showChangeLink = Some(true),
              showConfirmChangeText = Some(true)
            )),
            testLabels = Some(JourneyLabels(
              cy = Some(LanguageLabels())
            ))
          )
          val testPage = views.html.v2.confirm("", testJourneyConfig, Some(testAddress), isWelsh = true)
          val doc: Document = Jsoup.parse(testPage.body)

          doc.getBackLinkText shouldBe messages.back
          doc.getH1ElementAsText shouldBe JourneyConfigDefaults.WelshConstants.CONFIRM_PAGE_HEADING
          doc.getFirstH2ElementAsText shouldBe ""
          Option(doc.getElementById("infoMessage")) shouldBe None
          doc.getElementById("searchAgainLink").text shouldBe JourneyConfigDefaults.WelshConstants.SEARCH_AGAIN_LINK_TEXT
          doc.getElementById("changeLink").text shouldBe JourneyConfigDefaults.WelshConstants.CONFIRM_PAGE_EDIT_LINK_TEXT
          doc.getElementById("confirmChangeText").text shouldBe JourneyConfigDefaults.WelshConstants.CONFIRM_CHANGE_TEXT
          doc.getButtonContentAsText shouldBe JourneyConfigDefaults.WelshConstants.CONFIRM_PAGE_SUBMIT_LABEL
        }

        "show search again link is true " in {
          val testPage = views.html.v2.confirm("", testJourneyConfig, Some(testAddress), isWelsh = true)
          val doc: Document = Jsoup.parse(testPage.body)

          doc.getBackLinkText shouldBe messages.back
          doc.getH1ElementAsText shouldBe JourneyConfigDefaults.WelshConstants.CONFIRM_PAGE_HEADING
          doc.getFirstH2ElementAsText shouldBe JourneyConfigDefaults.WelshConstants.CONFIRM_PAGE_INFO_SUBHEADING
          doc.getElementById("infoMessage").text shouldBe infoText
          doc.getElementById("searchAgainLink").text shouldBe JourneyConfigDefaults.WelshConstants.SEARCH_AGAIN_LINK_TEXT
          doc.getElementById("changeLink").text shouldBe JourneyConfigDefaults.WelshConstants.CONFIRM_PAGE_EDIT_LINK_TEXT
          doc.getElementById("confirmChangeText").text shouldBe JourneyConfigDefaults.WelshConstants.CONFIRM_CHANGE_TEXT
          doc.getButtonContentAsText shouldBe JourneyConfigDefaults.WelshConstants.CONFIRM_PAGE_SUBMIT_LABEL
        }

        "show search again link is false " in {
          val testJourneyConfig = fullV2JourneyDataCustomConfig(
            testContinueUrl = testContinueUrl,
            testHomeNavHref = None,
            testAdditionalStylesheetUrl = None,
            testPhaseFeedbackLink = None,
            testDeskProServiceName = None,
            testShowPhaseBanner = None,
            testAlphaPhase = None,
            testShowBackButtons = None,
            testIncludeHMRCBranding = None,
            testUkMode = None,
            testAllowedCountryCodes = None,
            testSelectPage = None,
            testTimeoutConfig = None,
            testConfirmPageConfig = Some(ConfirmPageConfig(
              showSearchAgainLink = Some(false),
              showSubHeadingAndInfo = Some(true),
              showChangeLink = Some(true),
              showConfirmChangeText = Some(true)
            )),
            testLabels = Some(JourneyLabels(
              cy = Some(LanguageLabels())
            ))
          )
          val testPage = views.html.v2.confirm("", testJourneyConfig, Some(testAddress), isWelsh = true)
          val doc: Document = Jsoup.parse(testPage.body)

          doc.getBackLinkText shouldBe messages.back
          doc.getH1ElementAsText shouldBe JourneyConfigDefaults.WelshConstants.CONFIRM_PAGE_HEADING
          doc.getFirstH2ElementAsText shouldBe JourneyConfigDefaults.WelshConstants.CONFIRM_PAGE_INFO_SUBHEADING
          doc.getElementById("infoMessage").text shouldBe infoText
          Option(doc.getElementById("searchAgainLink")) shouldBe None
          doc.getElementById("changeLink").text shouldBe JourneyConfigDefaults.WelshConstants.CONFIRM_PAGE_EDIT_LINK_TEXT
          doc.getElementById("confirmChangeText").text shouldBe JourneyConfigDefaults.WelshConstants.CONFIRM_CHANGE_TEXT
          doc.getButtonContentAsText shouldBe JourneyConfigDefaults.WelshConstants.CONFIRM_PAGE_SUBMIT_LABEL
        }

        "show change link is true " in {
          val testPage = views.html.v2.confirm("", testJourneyConfig, Some(testAddress), isWelsh = true)
          val doc: Document = Jsoup.parse(testPage.body)

          doc.getBackLinkText shouldBe messages.back
          doc.getH1ElementAsText shouldBe JourneyConfigDefaults.WelshConstants.CONFIRM_PAGE_HEADING
          doc.getFirstH2ElementAsText shouldBe JourneyConfigDefaults.WelshConstants.CONFIRM_PAGE_INFO_SUBHEADING
          doc.getElementById("infoMessage").text shouldBe infoText
          doc.getElementById("searchAgainLink").text shouldBe JourneyConfigDefaults.WelshConstants.SEARCH_AGAIN_LINK_TEXT
          doc.getElementById("changeLink").text shouldBe JourneyConfigDefaults.WelshConstants.CONFIRM_PAGE_EDIT_LINK_TEXT
          doc.getElementById("confirmChangeText").text shouldBe JourneyConfigDefaults.WelshConstants.CONFIRM_CHANGE_TEXT
          doc.getButtonContentAsText shouldBe JourneyConfigDefaults.WelshConstants.CONFIRM_PAGE_SUBMIT_LABEL
        }

        "show change link is false " in {
          val testJourneyConfig = fullV2JourneyDataCustomConfig(
            testContinueUrl = testContinueUrl,
            testHomeNavHref = None,
            testAdditionalStylesheetUrl = None,
            testPhaseFeedbackLink = None,
            testDeskProServiceName = None,
            testShowPhaseBanner = None,
            testAlphaPhase = None,
            testShowBackButtons = None,
            testIncludeHMRCBranding = None,
            testUkMode = None,
            testAllowedCountryCodes = None,
            testSelectPage = None,
            testTimeoutConfig = None,
            testConfirmPageConfig = Some(ConfirmPageConfig(
              showSearchAgainLink = Some(true),
              showSubHeadingAndInfo = Some(true),
              showChangeLink = Some(false),
              showConfirmChangeText = Some(true)
            )),
            testLabels = Some(JourneyLabels(
              cy = Some(LanguageLabels())
            ))
          )
          val testPage = views.html.v2.confirm("", testJourneyConfig, Some(testAddress), isWelsh = true)
          val doc: Document = Jsoup.parse(testPage.body)

          doc.getBackLinkText shouldBe messages.back
          doc.getH1ElementAsText shouldBe JourneyConfigDefaults.WelshConstants.CONFIRM_PAGE_HEADING
          doc.getFirstH2ElementAsText shouldBe JourneyConfigDefaults.WelshConstants.CONFIRM_PAGE_INFO_SUBHEADING
          doc.getElementById("infoMessage").text shouldBe infoText
          doc.getElementById("searchAgainLink").text shouldBe JourneyConfigDefaults.WelshConstants.SEARCH_AGAIN_LINK_TEXT
          Option(doc.getElementById("changeLink")) shouldBe None
          doc.getElementById("confirmChangeText").text shouldBe JourneyConfigDefaults.WelshConstants.CONFIRM_CHANGE_TEXT
          doc.getButtonContentAsText shouldBe JourneyConfigDefaults.WelshConstants.CONFIRM_PAGE_SUBMIT_LABEL
        }

        "show confirm change options is true" in {
          val testPage = views.html.v2.confirm("", testJourneyConfig, Some(testAddress), isWelsh = true)
          val doc: Document = Jsoup.parse(testPage.body)

          doc.getBackLinkText shouldBe messages.back
          doc.getH1ElementAsText shouldBe JourneyConfigDefaults.WelshConstants.CONFIRM_PAGE_HEADING
          doc.getFirstH2ElementAsText shouldBe JourneyConfigDefaults.WelshConstants.CONFIRM_PAGE_INFO_SUBHEADING
          doc.getElementById("infoMessage").text shouldBe infoText
          doc.getElementById("searchAgainLink").text shouldBe JourneyConfigDefaults.WelshConstants.SEARCH_AGAIN_LINK_TEXT
          doc.getElementById("changeLink").text shouldBe JourneyConfigDefaults.WelshConstants.CONFIRM_PAGE_EDIT_LINK_TEXT
          doc.getElementById("confirmChangeText").text shouldBe JourneyConfigDefaults.WelshConstants.CONFIRM_CHANGE_TEXT
          doc.getButtonContentAsText shouldBe JourneyConfigDefaults.WelshConstants.CONFIRM_PAGE_SUBMIT_LABEL
        }

        "show confirm change options is false" in {
          val testJourneyConfig = fullV2JourneyDataCustomConfig(
            testContinueUrl = testContinueUrl,
            testHomeNavHref = None,
            testAdditionalStylesheetUrl = None,
            testPhaseFeedbackLink = None,
            testDeskProServiceName = None,
            testShowPhaseBanner = None,
            testAlphaPhase = None,
            testShowBackButtons = None,
            testIncludeHMRCBranding = None,
            testUkMode = None,
            testAllowedCountryCodes = None,
            testSelectPage = None,
            testTimeoutConfig = None,
            testConfirmPageConfig = Some(ConfirmPageConfig(
              showSearchAgainLink = Some(true),
              showSubHeadingAndInfo = Some(true),
              showChangeLink = Some(true),
              showConfirmChangeText = Some(false)
            )),
            testLabels = Some(JourneyLabels(
              cy = Some(LanguageLabels())
            ))
          )
          val testPage = views.html.v2.confirm("", testJourneyConfig, Some(testAddress), isWelsh = true)
          val doc: Document = Jsoup.parse(testPage.body)

          doc.getBackLinkText shouldBe messages.back
          doc.getH1ElementAsText shouldBe JourneyConfigDefaults.WelshConstants.CONFIRM_PAGE_HEADING
          doc.getFirstH2ElementAsText shouldBe JourneyConfigDefaults.WelshConstants.CONFIRM_PAGE_INFO_SUBHEADING
          doc.getElementById("infoMessage").text shouldBe infoText
          doc.getElementById("searchAgainLink").text shouldBe JourneyConfigDefaults.WelshConstants.SEARCH_AGAIN_LINK_TEXT
          doc.getElementById("changeLink").text shouldBe JourneyConfigDefaults.WelshConstants.CONFIRM_PAGE_EDIT_LINK_TEXT
          Option(doc.getElementById("confirmChangeText")) shouldBe None
          doc.getButtonContentAsText shouldBe JourneyConfigDefaults.WelshConstants.CONFIRM_PAGE_SUBMIT_LABEL
        }
      }
    }
    "render configured content" when {
      "English content is provided" when {
        val messages = MessageConstants.EnglishMessageConstants(true)

        "a confirmed address has been defined" in {
          val testPage = views.html.v2.confirm("", journeyDataV2Full, Some(testAddress), isWelsh = false)
          val doc: Document = Jsoup.parse(testPage.body)

          doc.getBackLinkText shouldBe messages.back
          doc.getH1ElementAsText shouldBe testEnglishConfirmPageLabels.heading
          doc.getFirstH2ElementAsText shouldBe testEnglishConfirmPageLabels.infoSubheading
          doc.getElementById("infoMessage").text shouldBe testEnglishConfirmPageLabels.infoMessage
          doc.getElementById("searchAgainLink").text shouldBe testEnglishConfirmPageLabels.searchAgainLinkText
          doc.getElementById("changeLink").text shouldBe testEnglishConfirmPageLabels.changeLinkText
          doc.getElementById("confirmChangeText").text shouldBe testEnglishConfirmPageLabels.confirmChangeText
          doc.getButtonContentAsText shouldBe testEnglishConfirmPageLabels.submitLabel
        }

        "a confirmed address has not been defined" in {
          val testPage = views.html.v2.confirm("", journeyDataV2Full, None, isWelsh = false)
          val doc: Document = Jsoup.parse(testPage.body)
          val errorText = s"${messages.confirmSelectedAddressError1} ${messages.confirmSelectedAddressError2}."

          doc.getBackLinkText shouldBe messages.back
          doc.getH1ElementAsText shouldBe testEnglishConfirmPageLabels.heading
          doc.getFirstH2ElementAsText shouldBe ""
          Option(doc.getElementById("infoMessage")) shouldBe None
          Option(doc.getElementById("searchAgainLink")) shouldBe None
          Option(doc.getElementById("changeLink")) shouldBe None
          Option(doc.getElementById("confirmChangeText")) shouldBe None
          doc.getElementById("lookupLink").parent().text() shouldBe errorText
        }
      }
    }
    "Welsh content is provided" when {
      val messages = MessageConstants.WelshMessageConstants(true)

      "a confirmed address has been defined" in {
        val testPage = views.html.v2.confirm("", journeyDataV2Full, Some(testAddress), isWelsh = true)
        val doc: Document = Jsoup.parse(testPage.body)

        doc.getBackLinkText shouldBe messages.back
        doc.getH1ElementAsText shouldBe testWelshConfirmPageLabels.heading
        doc.getFirstH2ElementAsText shouldBe testWelshConfirmPageLabels.infoSubheading
        doc.getElementById("infoMessage").text shouldBe testWelshConfirmPageLabels.infoMessage
        doc.getElementById("searchAgainLink").text shouldBe testWelshConfirmPageLabels.searchAgainLinkText
        doc.getElementById("changeLink").text shouldBe testWelshConfirmPageLabels.changeLinkText
        doc.getElementById("confirmChangeText").text shouldBe testWelshConfirmPageLabels.confirmChangeText
        doc.getButtonContentAsText shouldBe testWelshConfirmPageLabels.submitLabel
      }

      "a confirmed address has not been defined" in {
        val testPage = views.html.v2.confirm("", journeyDataV2Full, None, isWelsh = true)
        val doc: Document = Jsoup.parse(testPage.body)
        val errorText = s"${messages.confirmSelectedAddressError1} ${messages.confirmSelectedAddressError2}."

        doc.getBackLinkText shouldBe messages.back
        doc.getH1ElementAsText shouldBe testWelshConfirmPageLabels.heading
        doc.getFirstH2ElementAsText shouldBe ""
        Option(doc.getElementById("infoMessage")) shouldBe None
        Option(doc.getElementById("searchAgainLink")) shouldBe None
        Option(doc.getElementById("changeLink")) shouldBe None
        Option(doc.getElementById("confirmChangeText")) shouldBe None
        doc.getElementById("lookupLink").parent().text() shouldBe errorText
      }
    }
  }

}
