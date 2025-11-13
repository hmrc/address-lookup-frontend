/*
 * Copyright 2024 HM Revenue & Customs
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
import model.Lookup
import model.v2.JourneyDataV2
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import utils.TestConstants._
import views.html.abp.{lookup, select}

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

  class Setup(journeyData: JourneyDataV2, proposals: Proposals, lookup: Lookup, firstSearch: Boolean, welshEnabled: Boolean = false)(implicit frontendAppConfig: FrontendAppConfig) {
    implicit val lang: Lang = if (welshEnabled) Lang("cy") else Lang("en")

    val messages: Messages = implicitly[Messages]

    val testPage: HtmlFormat.Appendable = select("testId", journeyData, selectForm(), proposals, lookup.postcode, lookup.filter, firstSearch, welshEnabled)
    val doc: Document = Jsoup.parse(testPage.body)
  }

  "Select Page" should {

    "render the a11y link in the footer" in new Setup(testSelectPageConfig, testProposal, testLookup, firstSearch = true) {
      doc.select("ul.govuk-footer__inline-list li:nth-of-type(2) a").attr("href").shouldBe("/a11y/url")
    }

    "render the back button" when {
      "the config is provided as true for back links" in new Setup(testSelectPageConfig, testProposal, testLookup, firstSearch = true) {
        doc.getBackLinkText.shouldBe(messages("constants.back"))
      }

      "the config is not provided" in new Setup(testSelectPageConfigMinimal, testProposal, testLookup, firstSearch = true) {
        doc.getBackLinkText.shouldBe(messages("constants.back"))
      }
    }

    "render the edit address link" when {
      "the config is provided as false for show none of these option" in new Setup(testSelectPageConfig, testProposal, testLookup, firstSearch = true) {
        doc.getEditAddressLinkAsText.shouldBe(messages("selectPage.editAddressLinkText"))
      }
    }

    "not render the edit address link" when {
      "the config is provided as true for show none of these option" in new Setup(testSelectPageConfigWithNoneOfTheseOption, testProposal, testLookup, firstSearch = true) {
        doc.getEditAddressLinkAsText.shouldBe(Content.empty)
      }
    }

    "render the none of these option" when {
      "the config is provided as true for show none of these option" in new Setup(testSelectPageConfigWithNoneOfTheseOption, testProposal, testLookup, firstSearch = true) {
        doc.select("#addressId-none + label").text().shouldBe(messages("selectPage.noneOfThese"))
      }
    }

    "not render the none of these option" when {
      "the config is provided as false for show none of these option" in new Setup(testSelectPageConfig, testProposal, testLookup, firstSearch = true) {
        doc.select("#addressId-none + label").text().shouldBe(Content.empty)
      }
    }

    "not render the back button" when {
      "the config is provided as false for back links" in new Setup(testJourneyDataNoBackButtons, testProposal, testLookup, firstSearch = true) {
        doc.getBackLinkText.shouldBe(Content.empty)
      }
    }

    "render the heading without a postcode" when {
      "a lookup is provided with a postcode but it is still the first search" in new Setup(testSelectPageConfig, testProposal, testLookup, firstSearch = true) {
        doc.getH1ElementAsText.shouldBe(messages("selectPage.heading"))
      }
    }

    "render the heading with a postcode" when {
      "it is not the first search" in new Setup(testSelectPageConfig, testProposal, testLookup, firstSearch = false) {
        doc.getH1ElementAsText.shouldBe(s"${messages("selectPage.headingWithPostcode")} ${testLookup.postcode}")
      }
    }
  }

  "render the no results message" when {
    "it is not the first search" in new Setup(testSelectPageConfig, testProposal, testLookup, firstSearch = false) {
      doc.select("#no-results").text.shouldBe(s"${messages("constants.noResults")} 'testFilter'.")
    }
  }

  "not render the no results message" when {
    "it is the first search" in new Setup(testSelectPageConfig, testProposal, testLookup, firstSearch = true) {
      doc.select("#no-results").text.shouldBe(Content.empty)
    }
  }

  "render the try a different name or number link" when {
    "it is not the first search" in new Setup(testSelectPageConfig, testProposal, testLookup, firstSearch = false) {
      doc.getALinkText(id = "differentAddress").shouldBe(messages("constants.differentSearch"))
      doc.getLinkHrefAsText(id = "differentAddress").shouldBe(routes.AbpAddressLookupController.lookup("testId", Some(testLookup.postcode), testLookup.filter).url)
    }
  }

  "not render the try a different name or number link" when {
    "it is the first search" in new Setup(testSelectPageConfig, testProposal, testLookup, firstSearch = true) {
      doc.getALinkText(id = "differentAddress").shouldBe(Content.empty)
      doc.getLinkHrefAsText(id = "differentAddress").shouldBe(Content.empty)
    }
  }

  "render proposals" when {
    "there is 1 proposal" in new Setup(testSelectPageConfig, testProposal, testLookup, firstSearch = true) {
      doc.select(s"input[name^=addressId]").size.shouldBe(testProposal.proposals.get.size)
      doc.select("input[id^=addressId]").size.shouldBe(1)
      doc.select("label[for^=addressId]").size.shouldBe(1)
      doc.select("label[for^=addressId]").text.shouldBe(testProposal.proposals.get.head.toDescription)
      doc.getElementById("searchAgainLink") should not be null
    }
  }

  "there are many proposals" in new Setup(testSelectPageConfig, testProposalMany, testLookup, firstSearch = true) {
    doc.select(s"input[name^=addressId]").size.shouldBe(testProposalMany.proposals.get.size)
    for ((proposal, count) <- testProposalMany.proposals.get.zipWithIndex) {
      if (count == 0) {
        doc.select("input[id=addressId]").size.shouldBe(1)
        doc.select("label[for=addressId]").size.shouldBe(1)
        doc.select("label[for=addressId]").text.shouldBe(proposal.toDescription)
      } else {
        doc.select(s"input[id=addressId-${count}]").size.shouldBe(1)
        doc.select(s"label[for=addressId-${count}]").size.shouldBe(1)
        doc.select(s"label[for=addressId-${count}]").text.shouldBe(proposal.toDescription)
      }
    }
  }

  "not render any proposals" when {
    "there are none" in new Setup(testSelectPageConfig, testProposalNone, testLookup, firstSearch = true) {
      doc.select("input[id^=addressId]").size().shouldBe(testProposalNone.proposals.get.size)
    }
  }
}
