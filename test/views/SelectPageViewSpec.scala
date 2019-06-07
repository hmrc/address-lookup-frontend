package views

import controllers.{Proposals, routes}
import forms.ALFForms.selectForm
import model.{JourneyConfigDefaults, JourneyDataV2, Lookup}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import utils.TestConstants._

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

    def noResults(filter: String) = s"We could not find a match with '$filter'."

    val differentSearch = "Try a different name or number"
  }

  class Setup(journeyData: JourneyDataV2, proposals: Proposals, lookup: Option[Lookup], firstSearch: Boolean) {
    val testPage: HtmlFormat.Appendable = views.html.v2.select("testId", journeyData, selectForm, proposals, lookup, firstSearch)
    val doc: Document = Jsoup.parse(testPage.body)
  }

  implicit val testRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
  implicit val messages: Messages = app.injector.instanceOf[MessagesApi].preferred(testRequest)

  "Select Page" should {
    "render the back button" when {
      "the config is provided as true for back links" in new Setup(testSelectPageConfig, testProposal, None, firstSearch = true) {
        doc.getBackLinkText shouldBe Content.backLink
      }
      "the config is not provided" in new Setup(testSelectPageConfigMinimal, testProposal, None, firstSearch = true) {
        doc.getBackLinkText shouldBe Content.backLink
      }
    }
    "not render the back button" when {
      "the config is provided as false for back links" in new Setup(testJourneyDataNoBackButtons, testProposal, None, firstSearch = true) {
        doc.getBackLinkText shouldBe Content.empty
      }
    }

    "render the title" when {
      "the title is provided" in new Setup(testSelectPageConfig, testProposal, None, firstSearch = true) {
        doc.title shouldBe Content.title
      }

      "the title isn't provided" in new Setup(testSelectPageConfigNoLabel, testProposal, None, firstSearch = true) {
        doc.title shouldBe JourneyConfigDefaults.SELECT_PAGE_TITLE
      }
    }

    "render the heading without a postcode" when {
      "the heading is provided" in new Setup(testSelectPageConfig, testProposal, None, firstSearch = true) {
        doc.getH1ElementAsText shouldBe Content.heading
      }
      "the heading isn't provided" in new Setup(testSelectPageConfigNoLabel, testProposal, None, firstSearch = true) {
        doc.getH1ElementAsText shouldBe JourneyConfigDefaults.SELECT_PAGE_HEADING
      }
      "a lookup is provided with a postcode but it is still the first search" in new Setup(testSelectPageConfig, testProposal, Some(testLookup), firstSearch = true) {
        doc.getH1ElementAsText shouldBe Content.heading
      }
    }

    "render the heading with a postcode" when {
      "the heading is provided, a lookup is provided and it is not the first search" in new Setup(testSelectPageConfig, testProposal, Some(testLookup), firstSearch = false) {
        doc.getH1ElementAsText shouldBe Content.headingWithPostcode + testLookup.postcode
      }
      "the heading is not provided, a lookup is provided and it is not the first search" in new Setup(testSelectPageConfigNoLabel, testProposal, Some(testLookup), firstSearch = false) {
        doc.getH1ElementAsText shouldBe JourneyConfigDefaults.SELECT_PAGE_HEADING_WITH_POSTCODE + testLookup.postcode
      }
    }

    "render the no results message" when {
      "the lookup is provided and it is not the first search" in new Setup(testSelectPageConfig, testProposal, Some(testLookup), firstSearch = false) {
        doc.select("#no-results").text shouldBe Content.noResults(testLookup.filter.get)
      }
    }

    "not render the no results message" when {
      "the lookup is provided and it is the first search" in new Setup(testSelectPageConfig, testProposal, Some(testLookup), firstSearch = true) {
        doc.select("#no-results").text shouldBe Content.empty
      }

      "the lookup is not provided and it is not the first search" in new Setup(testSelectPageConfig, testProposal, None, firstSearch = false) {
        doc.select("#no-results").text shouldBe Content.empty
      }
    }

    "render the try a different name or number link" when {
      "the lookup is provided and it is not the first search" in new Setup(testSelectPageConfig, testProposal, Some(testLookup), firstSearch = false) {
        doc.getALinkText(id = "differentAddress") shouldBe Content.differentSearch
        doc.getLinkHrefAsText(id = "differentAddress") shouldBe routes.AddressLookupController.lookup("testId", Some(testLookup.postcode), testLookup.filter).url
      }
    }

    "not render the try a different name or number link" when {
      "the lookup is provided and it is the first search" in new Setup(testSelectPageConfig, testProposal, Some(testLookup), firstSearch = true) {
        doc.getALinkText(id = "differentAddress") shouldBe Content.empty
        doc.getLinkHrefAsText(id = "differentAddress") shouldBe Content.empty
      }

      "the lookup is not provided and it is not the first search" in new Setup(testSelectPageConfig, testProposal, None, firstSearch = false) {
        doc.getALinkText(id = "differentAddress") shouldBe Content.empty
        doc.getLinkHrefAsText(id = "differentAddress") shouldBe Content.empty
      }
    }

    "render the submit label" when {
      "the submit label is provided" in new Setup(testSelectPageConfig, testProposal, None, firstSearch = true) {
        doc.select("button").text shouldBe Content.submitLabel
      }
      "the submit label is not provided" in new Setup(testSelectPageConfigNoLabel, testProposal, None, firstSearch = true) {
        doc.select("button").text shouldBe JourneyConfigDefaults.SELECT_PAGE_SUBMIT_LABEL
      }
    }

    "render the edit address link" when {
      "the edit address link text is provided" in new Setup(testSelectPageConfig, testProposal, Some(testLookup), firstSearch = true) {
        doc.getALinkText(id = "editAddress") shouldBe Content.editAddressLinkText
        doc.getLinkHrefAsText(id = "editAddress") shouldBe routes.AddressLookupController.edit("testId", Some(testLookup.postcode), Some(true)).url
      }
      "the edit address link text is not provided" in new Setup(testSelectPageConfigNoLabel, testProposal, Some(testLookup), firstSearch = true) {
        doc.getALinkText(id = "editAddress") shouldBe JourneyConfigDefaults.EDIT_LINK_TEXT
        doc.getLinkHrefAsText(id = "editAddress") shouldBe routes.AddressLookupController.edit("testId", Some(testLookup.postcode), Some(true)).url
      }
    }

    "render proposals" when {
      "there is 1 proposal" in new Setup(testSelectPageConfig, testProposal, None, firstSearch = true) {
        doc.select("input[id^=addressId-]").size shouldBe testProposal.proposals.get.size
        doc.select("label[for^=addressId-]").size shouldBe testProposal.proposals.get.size
        doc.select("label[for^=addressId-]").text shouldBe testProposal.proposals.get.head.toDescription
      }
    }
    "there are many proposals" in new Setup(testSelectPageConfig, testProposalMany, None, firstSearch = true) {
      doc.select("input[id^=addressId-]").size() shouldBe testProposalMany.proposals.get.size
      doc.select("label[for^=addressId-]").size shouldBe testProposalMany.proposals.get.size
      doc.select("label[for^=addressId-]").text shouldBe testProposalMany.proposals.get.map(_.toDescription).mkString(" ")

    }
  }

  "not render any proposals" when {
    "there are none" in new Setup(testSelectPageConfig, testProposalNone, None, firstSearch = true) {
      doc.select("input[id^=addressId-]").size() shouldBe testProposalNone.proposals.get.size
    }
  }
}