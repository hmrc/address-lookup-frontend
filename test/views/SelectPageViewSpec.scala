/*
 * Copyright 2021 HM Revenue & Customs
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
import play.api.i18n.Messages
import model.{JourneyDataV2, Lookup}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.{Lang, MessagesApi}
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

  implicit val testRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
  implicit val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit val frontendAppConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
  val lookup: lookup = app.injector.instanceOf[lookup]
  val select: select = app.injector.instanceOf[select]
  val uk_mode_edit: uk_mode_edit = app.injector.instanceOf[uk_mode_edit]
  val non_uk_mode_edit: non_uk_mode_edit = app.injector.instanceOf[non_uk_mode_edit]

  class Setup(journeyData: JourneyDataV2, proposals: Proposals, lookup: Lookup, firstSearch: Boolean, welshEnabled: Boolean = false)(implicit frontendAppConfig: FrontendAppConfig) {
    implicit val lang: Lang = if (welshEnabled) Lang("cy") else Lang("en")

    val messages = implicitly[Messages]

    val testPage: HtmlFormat.Appendable = select("testId", journeyData, selectForm(), proposals, lookup, firstSearch, welshEnabled)
    val doc: Document = Jsoup.parse(testPage.body)
  }

  "Select Page" should {
    "render the back button" when {
      "the config is provided as true for back links" in new Setup(testSelectPageConfig, testProposal, testLookup, firstSearch = true) {
        doc.getBackLinkText shouldBe messages("constants.back")
      }

      "the config is not provided" in new Setup(testSelectPageConfigMinimal, testProposal, testLookup, firstSearch = true) {
        doc.getBackLinkText shouldBe messages("constants.back")
      }
    }

    "not render the back button" when {
      "the config is provided as false for back links" in new Setup(testJourneyDataNoBackButtons, testProposal, testLookup, firstSearch = true) {
        doc.getBackLinkText shouldBe Content.empty
      }
    }

    "render the heading without a postcode" when {
      "a lookup is provided with a postcode but it is still the first search" in new Setup(testSelectPageConfig, testProposal, testLookup, firstSearch = true) {
        doc.getH1ElementAsText shouldBe messages("selectPage.heading")
      }
    }

    "render the heading with a postcode" when {
      "it is not the first search" in new Setup(testSelectPageConfig, testProposal, testLookup, firstSearch = false) {
        doc.getH1ElementAsText shouldBe s"${messages("selectPage.headingWithPostcode")} ${testLookup.postcode}"
      }
    }
  }

  "render the no results message" when {
    "it is not the first search" in new Setup(testSelectPageConfig, testProposal, testLookup, firstSearch = false) {
      doc.select("#no-results").text shouldBe s"${messages("constants.noResults")} 'testFilter'."
    }
  }

  "not render the no results message" when {
    "it is the first search" in new Setup(testSelectPageConfig, testProposal, testLookup, firstSearch = true) {
      doc.select("#no-results").text shouldBe Content.empty
    }
  }

  "render the try a different name or number link" when {
    "it is not the first search" in new Setup(testSelectPageConfig, testProposal, testLookup, firstSearch = false) {
      doc.getALinkText(id = "differentAddress") shouldBe messages("constants.differentSearch")
      doc.getLinkHrefAsText(id = "differentAddress") shouldBe routes.AddressLookupController.lookup("testId", Some(testLookup.postcode), testLookup.filter).url
    }
  }

  "not render the try a different name or number link" when {
    "it is the first search" in new Setup(testSelectPageConfig, testProposal, testLookup, firstSearch = true) {
      doc.getALinkText(id = "differentAddress") shouldBe Content.empty
      doc.getLinkHrefAsText(id = "differentAddress") shouldBe Content.empty
    }
  }

  "render proposals" when {
    "there is 1 proposal" in new Setup(testSelectPageConfig, testProposal, testLookup, firstSearch = true) {
      doc.select(s"input[name^=addressId]").size shouldBe testProposal.proposals.get.size
      doc.select("input[id^=addressId]").size shouldBe 1
      doc.select("label[for^=addressId]").size shouldBe 1
      doc.select("label[for^=addressId]").text shouldBe testProposal.proposals.get.head.toDescription
      doc.getElementById("searchAgainLink") should not be null
    }
  }

  "there are many proposals" in new Setup(testSelectPageConfig, testProposalMany, testLookup, firstSearch = true) {
    doc.select(s"input[name^=addressId]").size shouldBe testProposalMany.proposals.get.size
    for ((proposal, count) <- testProposalMany.proposals.get.zipWithIndex) {
      if (count == 0) {
        doc.select("input[id=addressId]").size shouldBe 1
        doc.select("label[for=addressId]").size shouldBe 1
        doc.select("label[for=addressId]").text shouldBe proposal.toDescription
      } else {
        doc.select(s"input[id=addressId-${count}]").size shouldBe 1
        doc.select(s"label[for=addressId-${count}]").size shouldBe 1
        doc.select(s"label[for=addressId-${count}]").text shouldBe proposal.toDescription
      }
    }
  }

  "not render any proposals" when {
    "there are none" in new Setup(testSelectPageConfig, testProposalNone, testLookup, firstSearch = true) {
      doc.select("input[id^=addressId]").size() shouldBe testProposalNone.proposals.get.size
    }
  }
}

