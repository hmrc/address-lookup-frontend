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
import controllers.{Proposals, routes}
import forms.ALFForms.selectForm
import model.{JourneyConfigDefaults, JourneyDataV2, Lookup}
import model.MessageConstants.{EnglishMessageConstants ⇒ EnglishMessages, WelshMessageConstants ⇒ WelshMessages}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import utils.TestConstants._
import views.html.v2.{lookup, non_uk_mode_edit, select, uk_mode_edit}

class SelectPageViewSpec extends ViewSpec {

  object Content {
    val empty = ""
    val backLink = "Back"

    val title = "testTitle"
    val heading = "testHeading"
    val headingWithPostcode = "testHeadingWithPostcode "
    val proposalListLabel = "testProposalListLabel"
    val submitLabel = "testSubmitLabel"
    val searchAgainLinkText = "testSearchAgainLinkText"
    val editAddressLinkText = "testEditAddressLinkText"
  }

  object WelshContent {
    val backLink = "Yn ôl"
    val title = "cyTestTitle"
    val heading = "cyTestHeading"
    val headingWithPostcode = "cyTestHeadingWithPostcode"
    val submitLabel = "cyTestSubmitLabel"
    val editAddressLinkText = "cyTestEditAddressLinkText"
  }

  implicit val testRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
  implicit val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit val frontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
  val lookup = app.injector.instanceOf[lookup]
  val select = app.injector.instanceOf[select]
  val uk_mode_edit = app.injector.instanceOf[uk_mode_edit]
  val non_uk_mode_edit = app.injector.instanceOf[non_uk_mode_edit]

  class Setup(journeyData: JourneyDataV2, proposals: Proposals, lookup: Lookup, firstSearch: Boolean, welshEnabled: Boolean = false)(implicit frontendAppConfig: FrontendAppConfig) {
    implicit val lang: Lang = if (welshEnabled) Lang("cy") else Lang("en")

    val testPage: HtmlFormat.Appendable = select("testId", journeyData, selectForm(welshEnabled), proposals, lookup, firstSearch, welshEnabled)
    val doc: Document = Jsoup.parse(testPage.body)
  }

  val EnglishMessageConstants = EnglishMessages(true)
  val WelshMessageConstants = WelshMessages(true)

  val EnglishDefaultConstants = JourneyConfigDefaults.EnglishConstants(true)
  val WelshDefaultConstants = JourneyConfigDefaults.WelshConstants(true)

  "Select Page" should {
    "render the back button in english" when {
      "the config is provided as true for back links" in new Setup(testSelectPageConfig, testProposal, testLookup, firstSearch = true) {
        doc.getBackLinkText shouldBe Content.backLink
      }
      "the config is not provided" in new Setup(testSelectPageConfigMinimal, testProposal, testLookup, firstSearch = true) {
        doc.getBackLinkText shouldBe Content.backLink
      }
    }
    "not render the back button" when {
      "the config is provided as false for back links" in new Setup(testJourneyDataNoBackButtons, testProposal, testLookup, firstSearch = true) {
        doc.getBackLinkText shouldBe Content.empty
      }
    }

    "render the title" when {
      "the title is provided" in new Setup(testSelectPageConfig, testProposal, testLookup, firstSearch = true) {
        doc.title shouldBe Content.title
      }

      "the title isn't provided" in new Setup(testSelectPageConfigNoLabel, testProposal, testLookup, firstSearch = true) {
        doc.title shouldBe EnglishDefaultConstants.SELECT_PAGE_TITLE
      }
    }

    "render the heading without a postcode" when {
      "the heading is provided" in new Setup(testSelectPageConfig, testProposal, testLookup, firstSearch = true) {
        doc.getH1ElementAsText shouldBe Content.heading
      }
      "the heading isn't provided" in new Setup(testSelectPageConfigNoLabel, testProposal, testLookup, firstSearch = true) {
        doc.getH1ElementAsText shouldBe EnglishDefaultConstants.SELECT_PAGE_HEADING
      }
      "a lookup is provided with a postcode but it is still the first search" in new Setup(testSelectPageConfig, testProposal, testLookup, firstSearch = true) {
        doc.getH1ElementAsText shouldBe Content.heading
      }
    }

    "render the heading with a postcode" that {
      "is in english" when {
        "the heading is provided and it is not the first search" in new Setup(testSelectPageConfig, testProposal, testLookup, firstSearch = false) {
          doc.getH1ElementAsText shouldBe Content.headingWithPostcode + testLookup.postcode
        }
        "the heading is not provided and it is not the first search" in new Setup(testSelectPageConfigNoLabel, testProposal, testLookup, firstSearch = false) {
          doc.getH1ElementAsText shouldBe EnglishDefaultConstants.SELECT_PAGE_HEADING_WITH_POSTCODE + testLookup.postcode
        }
      }
      "is in welsh" when {
        "the heading is provided and it is not the first search" in new Setup(testWelshSelectPageConfig, testProposal, testLookup, firstSearch = false, welshEnabled = true) {
          doc.getH1ElementAsText shouldBe s"${WelshContent.headingWithPostcode} ${testLookup.postcode}"
        }
        "the heading is not provided and it is not the first search" in new Setup(testSelectPageConfigWelshEmpty, testProposal, testLookup, firstSearch = false, welshEnabled = true) {
          doc.getH1ElementAsText shouldBe WelshDefaultConstants.SELECT_PAGE_HEADING_WITH_POSTCODE + testLookup.postcode
        }
      }
    }

    "render the no results message" when {
      "in english and it is not the first search" in new Setup(testSelectPageConfig, testProposal, testLookup, firstSearch = false) {
        doc.select("#no-results").text shouldBe s"${EnglishMessageConstants.noResults} 'testFilter'."
      }
      "in welsh and it is not the first search" in new Setup(testWelshSelectPageConfig, testProposal, testLookup, firstSearch = false, welshEnabled = true) {
        doc.select("#no-results").text shouldBe s"${WelshMessageConstants.noResults} 'testFilter'."
      }
    }

    "not render the no results message" when {
      "it is the first search" in new Setup(testSelectPageConfig, testProposal, testLookup, firstSearch = true) {
        doc.select("#no-results").text shouldBe Content.empty
      }

      "it is not the first search" in new Setup(testSelectPageConfig, testProposal, testLookup, firstSearch = false) {
        doc.select("#no-results").text shouldBe s"${EnglishMessageConstants.noResults} 'testFilter'."
      }
    }

    "render the try a different name or number link" when {
      "it is not the first search" in new Setup(testSelectPageConfig, testProposal, testLookup, firstSearch = false) {
        doc.getALinkText(id = "differentAddress") shouldBe EnglishMessageConstants.differentSearch
        doc.getLinkHrefAsText(id = "differentAddress") shouldBe routes.AddressLookupController.lookup("testId", Some(testLookup.postcode), testLookup.filter).url
      }
    }

    "render the try a different name or number link in welsh" when {
      "it is not the first search" in new Setup(testSelectPageConfig, testProposal, testLookup, firstSearch = false, welshEnabled = true) {
        doc.getALinkText(id = "differentAddress") shouldBe WelshMessageConstants.differentSearch
      }
    }

    "not render the try a different name or number link" when {
      "it is the first search" in new Setup(testSelectPageConfig, testProposal, testLookup, firstSearch = true) {
        doc.getALinkText(id = "differentAddress") shouldBe Content.empty
        doc.getLinkHrefAsText(id = "differentAddress") shouldBe Content.empty
      }

      "it is not the first search" in new Setup(testSelectPageConfig, testProposal, testLookup, firstSearch = false) {
        doc.getALinkText(id = "differentAddress") shouldBe EnglishMessageConstants.differentSearch
        doc.getLinkHrefAsText(id = "differentAddress") shouldBe routes.AddressLookupController.lookup("testId", Some(testLookup.postcode), testLookup.filter).url
      }
    }

    "render the submit label" when {
      "the submit label is provided" in new Setup(testSelectPageConfig, testProposal, testLookup, firstSearch = true) {
        doc.select("button").text shouldBe Content.submitLabel
      }
      "the submit label is not provided" in new Setup(testSelectPageConfigNoLabel, testProposal, testLookup, firstSearch = true) {
        doc.select("button").text shouldBe EnglishDefaultConstants.SELECT_PAGE_SUBMIT_LABEL
      }
    }

    "render the edit address link" when {
      "the edit address link text is provided" in new Setup(testSelectPageConfig, testProposal, testLookup, firstSearch = true) {
        doc.getALinkText(id = "editAddress") shouldBe Content.editAddressLinkText
        doc.getLinkHrefAsText(id = "editAddress") shouldBe routes.AddressLookupController.edit("testId", Some(testLookup.postcode)).url
      }
      "the edit address link text is not provided" in new Setup(testSelectPageConfigNoLabel, testProposal, testLookup, firstSearch = true) {
        doc.getALinkText(id = "editAddress") shouldBe EnglishDefaultConstants.EDIT_LINK_TEXT
        doc.getLinkHrefAsText(id = "editAddress") shouldBe routes.AddressLookupController.edit("testId", Some(testLookup.postcode)).url
      }
    }

    "render proposals" when {
      "there is 1 proposal" in new Setup(testSelectPageConfig, testProposal, testLookup, firstSearch = true) {
        doc.select("input[id^=addressId]").size shouldBe testProposal.proposals.get.size
        doc.select("label[for^=addressId]").size shouldBe testProposal.proposals.get.size
        doc.select("label[for^=addressId]").text shouldBe testProposal.proposals.get.head.toDescription
      }
    }
    "there are many proposals" in new Setup(testSelectPageConfig, testProposalMany, testLookup, firstSearch = true) {
      doc.select("input[id^=addressId]").size() shouldBe testProposalMany.proposals.get.size
      doc.select("label[for^=addressId]").size shouldBe testProposalMany.proposals.get.size
      doc.select("label[for^=addressId]").text shouldBe testProposalMany.proposals.get.map(_.toDescription).mkString(" ")
    }
    "not render any proposals" when {
      "there are none" in new Setup(testSelectPageConfig, testProposalNone, testLookup, firstSearch = true) {
        doc.select("input[id^=addressId]").size() shouldBe testProposalNone.proposals.get.size
      }
    }

    "render the page in welsh" when {
      "there is custom welsh provided and the welsh flag is true" in new Setup(testWelshSelectPageConfig, testProposalNone, testLookup, firstSearch = true, welshEnabled = true) {
        doc.getBackLinkText shouldBe WelshContent.backLink
        doc.title shouldBe WelshContent.title
        doc.getH1ElementAsText shouldBe WelshContent.heading
        doc.getALinkText(id = "editAddress") shouldBe WelshContent.editAddressLinkText
        doc.select("button").text shouldBe WelshContent.submitLabel

      }
      "welsh is configured but not provided and the welsh flag is true" in new Setup(testSelectPageConfigWelshEmpty, testProposalNone, testLookup, firstSearch = true, welshEnabled = true) {
        doc.getBackLinkText shouldBe WelshContent.backLink
        doc.title shouldBe WelshDefaultConstants.SELECT_PAGE_TITLE
        doc.getH1ElementAsText shouldBe WelshDefaultConstants.SELECT_PAGE_HEADING
        doc.getALinkText(id = "editAddress") shouldBe WelshDefaultConstants.EDIT_LINK_TEXT
        doc.select("button").text shouldBe WelshDefaultConstants.SELECT_PAGE_SUBMIT_LABEL
      }
    }
    "render the page in english" when {
      "there is welsh provided but the welsh flag is false" in new Setup(testWelshSelectPageConfig, testProposalNone, testLookup, firstSearch = true, welshEnabled = false) {
        doc.getBackLinkText shouldBe Content.backLink
        doc.title shouldBe Content.title
        doc.getH1ElementAsText shouldBe Content.heading
        doc.getALinkText(id = "editAddress") shouldBe Content.editAddressLinkText
        doc.select("button").text shouldBe Content.submitLabel
      }
    }
  }
}