/*
 * Copyright 2020 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package views

import config.FrontendAppConfig
import model.{JourneyConfigDefaults, _}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.{Lang, MessagesApi}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import utils.TestConstants._
import views.html.v2.confirm

class ConfirmViewSpec extends ViewSpec {
  implicit val testRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
  implicit val frontendConfig = app.injector.instanceOf[FrontendAppConfig]
  val messagesApi = app.injector.instanceOf[MessagesApi]
  val confirm = app.injector.instanceOf[confirm]

  "ConfirmView" should {
    "render English default content" when {
      val journeyConfigDefaults = JourneyConfigDefaults.EnglishConstants(false)
      val infoText: String = journeyConfigDefaults.CONFIRM_PAGE_INFO_MESSAGE_HTML.replace("<kbd>", "").replace("</kbd>", "")
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
        implicit val lang: Lang = Lang("en")

        "show back button is true" in {
          val testPage = confirm("", testJourneyConfig, Some(testAddress), isWelsh = false)
          val doc: Document = Jsoup.parse(testPage.body)

          doc.getBackLinkText shouldBe messages.back
          doc.getH1ElementAsText shouldBe journeyConfigDefaults.CONFIRM_PAGE_HEADING
          doc.getFirstH2ElementAsText shouldBe journeyConfigDefaults.CONFIRM_PAGE_INFO_SUBHEADING
          doc.getElementById("infoMessage").text shouldBe infoText
          doc.getElementById("searchAgainLink").text shouldBe journeyConfigDefaults.SEARCH_AGAIN_LINK_TEXT
          doc.getElementById("changeLink").text shouldBe journeyConfigDefaults.CONFIRM_PAGE_EDIT_LINK_TEXT
          doc.getElementById("confirmChangeText").text shouldBe journeyConfigDefaults.CONFIRM_PAGE_CONFIRM_CHANGE_TEXT
          doc.getButtonContentAsText shouldBe journeyConfigDefaults.CONFIRM_PAGE_SUBMIT_LABEL
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
            testDisableTranslations = Some(false),
            testShowBackButtons = Some(false),
            testIncludeHMRCBranding = None,
            testUkMode = None,
            testAllowedCountryCodes = None,
            testSelectPage = None,
            testTimeoutConfig = None,
            testLabels = None
          )
          val testPage = confirm("", testJourneyConfig, Some(testAddress), isWelsh = false)
          val doc: Document = Jsoup.parse(testPage.body)

          doc.getBackLinkText shouldBe ""
          doc.getH1ElementAsText shouldBe journeyConfigDefaults.CONFIRM_PAGE_HEADING
          doc.getFirstH2ElementAsText shouldBe journeyConfigDefaults.CONFIRM_PAGE_INFO_SUBHEADING
          doc.getElementById("infoMessage").text shouldBe infoText
          doc.getElementById("searchAgainLink").text shouldBe journeyConfigDefaults.SEARCH_AGAIN_LINK_TEXT
          doc.getElementById("changeLink").text shouldBe journeyConfigDefaults.CONFIRM_PAGE_EDIT_LINK_TEXT
          doc.getElementById("confirmChangeText").text shouldBe journeyConfigDefaults.CONFIRM_PAGE_CONFIRM_CHANGE_TEXT
          doc.getButtonContentAsText shouldBe journeyConfigDefaults.CONFIRM_PAGE_SUBMIT_LABEL
        }

        "a confirmed address has been defined" in {
          val testPage = confirm("", testJourneyConfig, Some(testAddress), isWelsh = false)
          val doc: Document = Jsoup.parse(testPage.body)

          doc.getBackLinkText shouldBe messages.back
          doc.getH1ElementAsText shouldBe journeyConfigDefaults.CONFIRM_PAGE_HEADING
          doc.getFirstH2ElementAsText shouldBe journeyConfigDefaults.CONFIRM_PAGE_INFO_SUBHEADING
          doc.getElementById("infoMessage").text shouldBe infoText
          doc.getElementById("searchAgainLink").text shouldBe journeyConfigDefaults.SEARCH_AGAIN_LINK_TEXT
          doc.getElementById("changeLink").text shouldBe journeyConfigDefaults.CONFIRM_PAGE_EDIT_LINK_TEXT
          doc.getElementById("confirmChangeText").text shouldBe journeyConfigDefaults.CONFIRM_PAGE_CONFIRM_CHANGE_TEXT
          doc.getButtonContentAsText shouldBe journeyConfigDefaults.CONFIRM_PAGE_SUBMIT_LABEL
        }

        "a confirmed address has not been defined" in {
          val testPage = confirm("", testJourneyConfig, None, isWelsh = false)
          val doc: Document = Jsoup.parse(testPage.body)
          val errorText = s"${messages.confirmSelectedAddressError1} ${messages.confirmSelectedAddressError2}."

          doc.getBackLinkText shouldBe messages.back
          doc.getH1ElementAsText shouldBe journeyConfigDefaults.CONFIRM_PAGE_HEADING
//          doc.getFirstH2ElementAsText shouldBe ""
          Option(doc.getElementById("infoMessage")) shouldBe None
          Option(doc.getElementById("searchAgainLink")) shouldBe None
          Option(doc.getElementById("changeLink")) shouldBe None
          Option(doc.getElementById("confirmChangeText")) shouldBe None
          doc.getElementById("lookupLink").parent().text() shouldBe errorText
        }

        "show subheading and info is true" in {
          val testPage = confirm("", testJourneyConfig, Some(testAddress), isWelsh = false)
          val doc: Document = Jsoup.parse(testPage.body)

          doc.getBackLinkText shouldBe messages.back
          doc.getH1ElementAsText shouldBe journeyConfigDefaults.CONFIRM_PAGE_HEADING
          doc.getFirstH2ElementAsText shouldBe journeyConfigDefaults.CONFIRM_PAGE_INFO_SUBHEADING
          doc.getElementById("infoMessage").text shouldBe infoText
          doc.getElementById("searchAgainLink").text shouldBe journeyConfigDefaults.SEARCH_AGAIN_LINK_TEXT
          doc.getElementById("changeLink").text shouldBe journeyConfigDefaults.CONFIRM_PAGE_EDIT_LINK_TEXT
          doc.getElementById("confirmChangeText").text shouldBe journeyConfigDefaults.CONFIRM_PAGE_CONFIRM_CHANGE_TEXT
          doc.getButtonContentAsText shouldBe journeyConfigDefaults.CONFIRM_PAGE_SUBMIT_LABEL
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
          val testPage = confirm("", testJourneyConfig, Some(testAddress), isWelsh = false)
          val doc: Document = Jsoup.parse(testPage.body)

          doc.getBackLinkText shouldBe messages.back
          doc.getH1ElementAsText shouldBe journeyConfigDefaults.CONFIRM_PAGE_HEADING
//          doc.getFirstH2ElementAsText shouldBe ""
          Option(doc.getElementById("infoMessage")) shouldBe None
          doc.getElementById("searchAgainLink").text shouldBe journeyConfigDefaults.SEARCH_AGAIN_LINK_TEXT
          doc.getElementById("changeLink").text shouldBe journeyConfigDefaults.CONFIRM_PAGE_EDIT_LINK_TEXT
          doc.getElementById("confirmChangeText").text shouldBe journeyConfigDefaults.CONFIRM_PAGE_CONFIRM_CHANGE_TEXT
          doc.getButtonContentAsText shouldBe journeyConfigDefaults.CONFIRM_PAGE_SUBMIT_LABEL
        }

        "show search again link is true " in {
          val testPage = confirm("", testJourneyConfig, Some(testAddress), isWelsh = false)
          val doc: Document = Jsoup.parse(testPage.body)

          doc.getBackLinkText shouldBe messages.back
          doc.getH1ElementAsText shouldBe journeyConfigDefaults.CONFIRM_PAGE_HEADING
          doc.getFirstH2ElementAsText shouldBe journeyConfigDefaults.CONFIRM_PAGE_INFO_SUBHEADING
          doc.getElementById("infoMessage").text shouldBe infoText
          doc.getElementById("searchAgainLink").text shouldBe journeyConfigDefaults.SEARCH_AGAIN_LINK_TEXT
          doc.getElementById("changeLink").text shouldBe journeyConfigDefaults.CONFIRM_PAGE_EDIT_LINK_TEXT
          doc.getElementById("confirmChangeText").text shouldBe journeyConfigDefaults.CONFIRM_PAGE_CONFIRM_CHANGE_TEXT
          doc.getButtonContentAsText shouldBe journeyConfigDefaults.CONFIRM_PAGE_SUBMIT_LABEL
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
          val testPage = confirm("", testJourneyConfig, Some(testAddress), isWelsh = false)
          val doc: Document = Jsoup.parse(testPage.body)

          doc.getBackLinkText shouldBe messages.back
          doc.getH1ElementAsText shouldBe journeyConfigDefaults.CONFIRM_PAGE_HEADING
          doc.getFirstH2ElementAsText shouldBe journeyConfigDefaults.CONFIRM_PAGE_INFO_SUBHEADING
          doc.getElementById("infoMessage").text shouldBe infoText
          Option(doc.getElementById("searchAgainLink")) shouldBe None
          doc.getElementById("changeLink").text shouldBe journeyConfigDefaults.CONFIRM_PAGE_EDIT_LINK_TEXT
          doc.getElementById("confirmChangeText").text shouldBe journeyConfigDefaults.CONFIRM_PAGE_CONFIRM_CHANGE_TEXT
          doc.getButtonContentAsText shouldBe journeyConfigDefaults.CONFIRM_PAGE_SUBMIT_LABEL
        }

        "show change link is true " in {
          val testPage = confirm("", testJourneyConfig, Some(testAddress), isWelsh = false)
          val doc: Document = Jsoup.parse(testPage.body)

          doc.getBackLinkText shouldBe messages.back
          doc.getH1ElementAsText shouldBe journeyConfigDefaults.CONFIRM_PAGE_HEADING
          doc.getFirstH2ElementAsText shouldBe journeyConfigDefaults.CONFIRM_PAGE_INFO_SUBHEADING
          doc.getElementById("infoMessage").text shouldBe infoText
          doc.getElementById("searchAgainLink").text shouldBe journeyConfigDefaults.SEARCH_AGAIN_LINK_TEXT
          doc.getElementById("changeLink").text shouldBe journeyConfigDefaults.CONFIRM_PAGE_EDIT_LINK_TEXT
          doc.getElementById("confirmChangeText").text shouldBe journeyConfigDefaults.CONFIRM_PAGE_CONFIRM_CHANGE_TEXT
          doc.getButtonContentAsText shouldBe journeyConfigDefaults.CONFIRM_PAGE_SUBMIT_LABEL
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
          val testPage = confirm("", testJourneyConfig, Some(testAddress), isWelsh = false)
          val doc: Document = Jsoup.parse(testPage.body)

          doc.getBackLinkText shouldBe messages.back
          doc.getH1ElementAsText shouldBe journeyConfigDefaults.CONFIRM_PAGE_HEADING
          doc.getFirstH2ElementAsText shouldBe journeyConfigDefaults.CONFIRM_PAGE_INFO_SUBHEADING
          doc.getElementById("infoMessage").text shouldBe infoText
          doc.getElementById("searchAgainLink").text shouldBe journeyConfigDefaults.SEARCH_AGAIN_LINK_TEXT
          Option(doc.getElementById("changeLink")) shouldBe None
          doc.getElementById("confirmChangeText").text shouldBe journeyConfigDefaults.CONFIRM_PAGE_CONFIRM_CHANGE_TEXT
          doc.getButtonContentAsText shouldBe journeyConfigDefaults.CONFIRM_PAGE_SUBMIT_LABEL
        }

        "show confirm change options is true" in {
          val testPage = confirm("", testJourneyConfig, Some(testAddress), isWelsh = false)
          val doc: Document = Jsoup.parse(testPage.body)

          doc.getBackLinkText shouldBe messages.back
          doc.getH1ElementAsText shouldBe journeyConfigDefaults.CONFIRM_PAGE_HEADING
          doc.getFirstH2ElementAsText shouldBe journeyConfigDefaults.CONFIRM_PAGE_INFO_SUBHEADING
          doc.getElementById("infoMessage").text shouldBe infoText
          doc.getElementById("searchAgainLink").text shouldBe journeyConfigDefaults.SEARCH_AGAIN_LINK_TEXT
          doc.getElementById("changeLink").text shouldBe journeyConfigDefaults.CONFIRM_PAGE_EDIT_LINK_TEXT
          doc.getElementById("confirmChangeText").text shouldBe journeyConfigDefaults.CONFIRM_PAGE_CONFIRM_CHANGE_TEXT
          doc.getButtonContentAsText shouldBe journeyConfigDefaults.CONFIRM_PAGE_SUBMIT_LABEL
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
          val testPage = confirm("", testJourneyConfig, Some(testAddress), isWelsh = false)
          val doc: Document = Jsoup.parse(testPage.body)

          doc.getBackLinkText shouldBe messages.back
          doc.getH1ElementAsText shouldBe journeyConfigDefaults.CONFIRM_PAGE_HEADING
          doc.getFirstH2ElementAsText shouldBe journeyConfigDefaults.CONFIRM_PAGE_INFO_SUBHEADING
          doc.getElementById("infoMessage").text shouldBe infoText
          doc.getElementById("searchAgainLink").text shouldBe journeyConfigDefaults.SEARCH_AGAIN_LINK_TEXT
          doc.getElementById("changeLink").text shouldBe journeyConfigDefaults.CONFIRM_PAGE_EDIT_LINK_TEXT
          Option(doc.getElementById("confirmChangeText")) shouldBe None
          doc.getButtonContentAsText shouldBe journeyConfigDefaults.CONFIRM_PAGE_SUBMIT_LABEL
        }
      }
    }

    "render Welsh default content" when {
      val journeyConfigDefaults = JourneyConfigDefaults.WelshConstants(false)
      val infoText: String = journeyConfigDefaults.CONFIRM_PAGE_INFO_MESSAGE_HTML.replace("<kbd>", "").replace("</kbd>", "")
      val testJourneyConfig = fullV2JourneyDataCustomConfig(
        testContinueUrl = testContinueUrl,
        testHomeNavHref = None,
        testAdditionalStylesheetUrl = None,
        testPhaseFeedbackLink = None,
        testDeskProServiceName = None,
        testShowPhaseBanner = None,
        testAlphaPhase = None,
        testDisableTranslations = None,
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
        implicit val lang: Lang = Lang("cy")

        "show back button is true" in {
          val testPage = confirm("", testJourneyConfig, Some(testAddress), isWelsh = true)
          val doc: Document = Jsoup.parse(testPage.body)

          doc.getBackLinkText shouldBe messages.back
          doc.getH1ElementAsText shouldBe journeyConfigDefaults.CONFIRM_PAGE_HEADING
          doc.getFirstH2ElementAsText shouldBe journeyConfigDefaults.CONFIRM_PAGE_INFO_SUBHEADING
          doc.getElementById("infoMessage").text shouldBe infoText
          doc.getElementById("searchAgainLink").text shouldBe journeyConfigDefaults.SEARCH_AGAIN_LINK_TEXT
          doc.getElementById("changeLink").text shouldBe journeyConfigDefaults.CONFIRM_PAGE_EDIT_LINK_TEXT
          doc.getElementById("confirmChangeText").text shouldBe journeyConfigDefaults.CONFIRM_PAGE_CONFIRM_CHANGE_TEXT
          doc.getButtonContentAsText shouldBe journeyConfigDefaults.CONFIRM_PAGE_SUBMIT_LABEL
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
            testDisableTranslations = Some(false),
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
          val testPage = confirm("", testJourneyConfig, Some(testAddress), isWelsh = true)
          val doc: Document = Jsoup.parse(testPage.body)

          doc.getBackLinkText shouldBe ""
          doc.getH1ElementAsText shouldBe journeyConfigDefaults.CONFIRM_PAGE_HEADING
          doc.getFirstH2ElementAsText shouldBe journeyConfigDefaults.CONFIRM_PAGE_INFO_SUBHEADING
          doc.getElementById("infoMessage").text shouldBe infoText
          doc.getElementById("searchAgainLink").text shouldBe journeyConfigDefaults.SEARCH_AGAIN_LINK_TEXT
          doc.getElementById("changeLink").text shouldBe journeyConfigDefaults.CONFIRM_PAGE_EDIT_LINK_TEXT
          doc.getElementById("confirmChangeText").text shouldBe journeyConfigDefaults.CONFIRM_PAGE_CONFIRM_CHANGE_TEXT
          doc.getButtonContentAsText shouldBe journeyConfigDefaults.CONFIRM_PAGE_SUBMIT_LABEL
        }

        "a confirmed address has been defined" in {
          val testPage = confirm("", testJourneyConfig, Some(testAddress), isWelsh = true)
          val doc: Document = Jsoup.parse(testPage.body)

          doc.getBackLinkText shouldBe messages.back
          doc.getH1ElementAsText shouldBe journeyConfigDefaults.CONFIRM_PAGE_HEADING
          doc.getFirstH2ElementAsText shouldBe journeyConfigDefaults.CONFIRM_PAGE_INFO_SUBHEADING
          doc.getElementById("infoMessage").text shouldBe infoText
          doc.getElementById("searchAgainLink").text shouldBe journeyConfigDefaults.SEARCH_AGAIN_LINK_TEXT
          doc.getElementById("changeLink").text shouldBe journeyConfigDefaults.CONFIRM_PAGE_EDIT_LINK_TEXT
          doc.getElementById("confirmChangeText").text shouldBe journeyConfigDefaults.CONFIRM_PAGE_CONFIRM_CHANGE_TEXT
          doc.getButtonContentAsText shouldBe journeyConfigDefaults.CONFIRM_PAGE_SUBMIT_LABEL
        }

        "a confirmed address has not been defined" in {
          val testPage = confirm("", testJourneyConfig, None, isWelsh = true)
          val doc: Document = Jsoup.parse(testPage.body)
          val errorText = s"${messages.confirmSelectedAddressError1} ${messages.confirmSelectedAddressError2}."

          doc.getBackLinkText shouldBe messages.back
          doc.getH1ElementAsText shouldBe journeyConfigDefaults.CONFIRM_PAGE_HEADING
//          doc.getFirstH2ElementAsText shouldBe ""
          Option(doc.getElementById("infoMessage")) shouldBe None
          Option(doc.getElementById("searchAgainLink")) shouldBe None
          Option(doc.getElementById("changeLink")) shouldBe None
          Option(doc.getElementById("confirmChangeText")) shouldBe None
          doc.getElementById("lookupLink").parent().text() shouldBe errorText
        }

        "show subheading and info is true" in {
          val testPage = confirm("", testJourneyConfig, Some(testAddress), isWelsh = true)
          val doc: Document = Jsoup.parse(testPage.body)

          doc.getBackLinkText shouldBe messages.back
          doc.getH1ElementAsText shouldBe journeyConfigDefaults.CONFIRM_PAGE_HEADING
          doc.getFirstH2ElementAsText shouldBe journeyConfigDefaults.CONFIRM_PAGE_INFO_SUBHEADING
          doc.getElementById("infoMessage").text shouldBe infoText
          doc.getElementById("searchAgainLink").text shouldBe journeyConfigDefaults.SEARCH_AGAIN_LINK_TEXT
          doc.getElementById("changeLink").text shouldBe journeyConfigDefaults.CONFIRM_PAGE_EDIT_LINK_TEXT
          doc.getElementById("confirmChangeText").text shouldBe journeyConfigDefaults.CONFIRM_PAGE_CONFIRM_CHANGE_TEXT
          doc.getButtonContentAsText shouldBe journeyConfigDefaults.CONFIRM_PAGE_SUBMIT_LABEL
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
          val testPage = confirm("", testJourneyConfig, Some(testAddress), isWelsh = true)
          val doc: Document = Jsoup.parse(testPage.body)

          doc.getBackLinkText shouldBe messages.back
          doc.getH1ElementAsText shouldBe journeyConfigDefaults.CONFIRM_PAGE_HEADING
//          doc.getFirstH2ElementAsText shouldBe ""
          Option(doc.getElementById("infoMessage")) shouldBe None
          doc.getElementById("searchAgainLink").text shouldBe journeyConfigDefaults.SEARCH_AGAIN_LINK_TEXT
          doc.getElementById("changeLink").text shouldBe journeyConfigDefaults.CONFIRM_PAGE_EDIT_LINK_TEXT
          doc.getElementById("confirmChangeText").text shouldBe journeyConfigDefaults.CONFIRM_PAGE_CONFIRM_CHANGE_TEXT
          doc.getButtonContentAsText shouldBe journeyConfigDefaults.CONFIRM_PAGE_SUBMIT_LABEL
        }

        "show search again link is true " in {
          val testPage = confirm("", testJourneyConfig, Some(testAddress), isWelsh = true)
          val doc: Document = Jsoup.parse(testPage.body)

          doc.getBackLinkText shouldBe messages.back
          doc.getH1ElementAsText shouldBe journeyConfigDefaults.CONFIRM_PAGE_HEADING
          doc.getFirstH2ElementAsText shouldBe journeyConfigDefaults.CONFIRM_PAGE_INFO_SUBHEADING
          doc.getElementById("infoMessage").text shouldBe infoText
          doc.getElementById("searchAgainLink").text shouldBe journeyConfigDefaults.SEARCH_AGAIN_LINK_TEXT
          doc.getElementById("changeLink").text shouldBe journeyConfigDefaults.CONFIRM_PAGE_EDIT_LINK_TEXT
          doc.getElementById("confirmChangeText").text shouldBe journeyConfigDefaults.CONFIRM_PAGE_CONFIRM_CHANGE_TEXT
          doc.getButtonContentAsText shouldBe journeyConfigDefaults.CONFIRM_PAGE_SUBMIT_LABEL
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
          val testPage = confirm("", testJourneyConfig, Some(testAddress), isWelsh = true)
          val doc: Document = Jsoup.parse(testPage.body)

          doc.getBackLinkText shouldBe messages.back
          doc.getH1ElementAsText shouldBe journeyConfigDefaults.CONFIRM_PAGE_HEADING
          doc.getFirstH2ElementAsText shouldBe journeyConfigDefaults.CONFIRM_PAGE_INFO_SUBHEADING
          doc.getElementById("infoMessage").text shouldBe infoText
          Option(doc.getElementById("searchAgainLink")) shouldBe None
          doc.getElementById("changeLink").text shouldBe journeyConfigDefaults.CONFIRM_PAGE_EDIT_LINK_TEXT
          doc.getElementById("confirmChangeText").text shouldBe journeyConfigDefaults.CONFIRM_PAGE_CONFIRM_CHANGE_TEXT
          doc.getButtonContentAsText shouldBe journeyConfigDefaults.CONFIRM_PAGE_SUBMIT_LABEL
        }

        "show change link is true " in {
          val testPage = confirm("", testJourneyConfig, Some(testAddress), isWelsh = true)
          val doc: Document = Jsoup.parse(testPage.body)

          doc.getBackLinkText shouldBe messages.back
          doc.getH1ElementAsText shouldBe journeyConfigDefaults.CONFIRM_PAGE_HEADING
          doc.getFirstH2ElementAsText shouldBe journeyConfigDefaults.CONFIRM_PAGE_INFO_SUBHEADING
          doc.getElementById("infoMessage").text shouldBe infoText
          doc.getElementById("searchAgainLink").text shouldBe journeyConfigDefaults.SEARCH_AGAIN_LINK_TEXT
          doc.getElementById("changeLink").text shouldBe journeyConfigDefaults.CONFIRM_PAGE_EDIT_LINK_TEXT
          doc.getElementById("confirmChangeText").text shouldBe journeyConfigDefaults.CONFIRM_PAGE_CONFIRM_CHANGE_TEXT
          doc.getButtonContentAsText shouldBe journeyConfigDefaults.CONFIRM_PAGE_SUBMIT_LABEL
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
          val testPage = confirm("", testJourneyConfig, Some(testAddress), isWelsh = true)
          val doc: Document = Jsoup.parse(testPage.body)

          doc.getBackLinkText shouldBe messages.back
          doc.getH1ElementAsText shouldBe journeyConfigDefaults.CONFIRM_PAGE_HEADING
          doc.getFirstH2ElementAsText shouldBe journeyConfigDefaults.CONFIRM_PAGE_INFO_SUBHEADING
          doc.getElementById("infoMessage").text shouldBe infoText
          doc.getElementById("searchAgainLink").text shouldBe journeyConfigDefaults.SEARCH_AGAIN_LINK_TEXT
          Option(doc.getElementById("changeLink")) shouldBe None
          doc.getElementById("confirmChangeText").text shouldBe journeyConfigDefaults.CONFIRM_PAGE_CONFIRM_CHANGE_TEXT
          doc.getButtonContentAsText shouldBe journeyConfigDefaults.CONFIRM_PAGE_SUBMIT_LABEL
        }

        "show confirm change options is true" in {
          val testPage = confirm("", testJourneyConfig, Some(testAddress), isWelsh = true)
          val doc: Document = Jsoup.parse(testPage.body)

          doc.getBackLinkText shouldBe messages.back
          doc.getH1ElementAsText shouldBe journeyConfigDefaults.CONFIRM_PAGE_HEADING
          doc.getFirstH2ElementAsText shouldBe journeyConfigDefaults.CONFIRM_PAGE_INFO_SUBHEADING
          doc.getElementById("infoMessage").text shouldBe infoText
          doc.getElementById("searchAgainLink").text shouldBe journeyConfigDefaults.SEARCH_AGAIN_LINK_TEXT
          doc.getElementById("changeLink").text shouldBe journeyConfigDefaults.CONFIRM_PAGE_EDIT_LINK_TEXT
          doc.getElementById("confirmChangeText").text shouldBe journeyConfigDefaults.CONFIRM_PAGE_CONFIRM_CHANGE_TEXT
          doc.getButtonContentAsText shouldBe journeyConfigDefaults.CONFIRM_PAGE_SUBMIT_LABEL
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
          val testPage = confirm("", testJourneyConfig, Some(testAddress), isWelsh = true)
          val doc: Document = Jsoup.parse(testPage.body)

          doc.getBackLinkText shouldBe messages.back
          doc.getH1ElementAsText shouldBe journeyConfigDefaults.CONFIRM_PAGE_HEADING
          doc.getFirstH2ElementAsText shouldBe journeyConfigDefaults.CONFIRM_PAGE_INFO_SUBHEADING
          doc.getElementById("infoMessage").text shouldBe infoText
          doc.getElementById("searchAgainLink").text shouldBe journeyConfigDefaults.SEARCH_AGAIN_LINK_TEXT
          doc.getElementById("changeLink").text shouldBe journeyConfigDefaults.CONFIRM_PAGE_EDIT_LINK_TEXT
          Option(doc.getElementById("confirmChangeText")) shouldBe None
          doc.getButtonContentAsText shouldBe journeyConfigDefaults.CONFIRM_PAGE_SUBMIT_LABEL
        }
      }
    }
    "render configured content" when {
      implicit val lang: Lang = Lang("en")

      "English content is provided" when {
        val messages = MessageConstants.EnglishMessageConstants(true)

        "a confirmed address has been defined" in {
          val testPage = confirm("", journeyDataV2Full, Some(testAddress), isWelsh = false)
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
          val testPage = confirm("", journeyDataV2Full, None, isWelsh = false)
          val doc: Document = Jsoup.parse(testPage.body)
          val errorText = s"${messages.confirmSelectedAddressError1} ${messages.confirmSelectedAddressError2}."

          doc.getBackLinkText shouldBe messages.back
          doc.getH1ElementAsText shouldBe testEnglishConfirmPageLabels.heading
//          doc.getFirstH2ElementAsText shouldBe ""
          Option(doc.getElementById("infoMessage")) shouldBe None
          Option(doc.getElementById("searchAgainLink")) shouldBe None
          Option(doc.getElementById("changeLink")) shouldBe None
          Option(doc.getElementById("confirmChangeText")) shouldBe None
          doc.getElementById("lookupLink").parent().text() shouldBe errorText
        }
      }
    }
    "Welsh content is provided" when {
      implicit val lang: Lang = Lang("cy")

      val messages = MessageConstants.WelshMessageConstants(true)

      "a confirmed address has been defined" in {
        val testPage = confirm("", journeyDataV2Full, Some(testAddress), isWelsh = true)
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
        val testPage = confirm("", journeyDataV2Full, None, isWelsh = true)
        val doc: Document = Jsoup.parse(testPage.body)
        val errorText = s"${messages.confirmSelectedAddressError1} ${messages.confirmSelectedAddressError2}."

        doc.getBackLinkText shouldBe messages.back
        doc.getH1ElementAsText shouldBe testWelshConfirmPageLabels.heading
//        doc.getFirstH2ElementAsText shouldBe ""
        Option(doc.getElementById("infoMessage")) shouldBe None
        Option(doc.getElementById("searchAgainLink")) shouldBe None
        Option(doc.getElementById("changeLink")) shouldBe None
        Option(doc.getElementById("confirmChangeText")) shouldBe None
        doc.getElementById("lookupLink").parent().text() shouldBe errorText
      }
    }
  }

}
