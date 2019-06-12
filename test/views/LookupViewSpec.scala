package views

import controllers.routes
import forms.ALFForms.lookupForm
import model.JourneyConfigDefaults.EnglishConstants._
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages.Implicits._
import play.api.i18n.MessagesApi
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.twirl.api.Html
import utils.TestConstants._

class LookupViewSpec extends ViewSpec {

  object content {
    val testHintLabel = "For example, The Mill, 116 or Flat 37a"
    val title = "enLookupPageTitle"
    val heading = "enLookupPageHeading"
    val filterLabel = "enFilterLabel"
    val postcodeLabel = "enPostcodeLabel"
    val submitLabel = "enSubmitLabel"
    val manualAddressLinkText = "enManualAddressLinkText"
    val errorHeading = "This page has errors"
    val postcodeErrorMessage = "The postcode you entered appears to be incomplete or invalid. Please check and try again."
    val filterErrorMessage = "Your house name/number needs to be fewer than 256 characters"
  }

  implicit val testRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
  val messages = app.injector.instanceOf[MessagesApi]
  val testHtml = Html("")
  val testForm = lookupForm

  "Lookup view page" should {
    "renders" when {
      "default content" in {
        val testPage = views.html.v2.lookup(testId, testBasicLevelJourneyConfigV2, lookupForm)
        val doc: Document = Jsoup.parse(testPage.body)

        doc.title shouldBe LOOKUP_PAGE_TITLE
        doc.getH1ElementAsText shouldBe LOOKUP_PAGE_HEADING
        doc.hasTextFieldInput("postcode") shouldBe true
        doc.getTextFieldLabel("postcode") shouldBe LOOKUP_PAGE_POSTCODE_LABEL
        doc.hasTextFieldInput("filter") shouldBe true
        doc.getHintAsText shouldBe content.testHintLabel
        doc.getTextFieldLabel("filter") shouldBe LOOKUP_PAGE_FILTER_LABEL + " " + content.testHintLabel
        doc.getALinkText("manualAddress") shouldBe LOOKUP_PAGE_MANUAL_ADDRESS_LINK_TEXT
        doc.getLinkHrefAsText("manualAddress") shouldBe routes.AddressLookupController.edit(testId).url
        doc.getButtonContentAsText shouldBe LOOKUP_PAGE_SUBMIT_LABEL
      }
      "configured content" in {
        val testPage = views.html.v2.lookup(testId, testLookupLevelJourneyConfigV2, lookupForm)
        val doc: Document = Jsoup.parse(testPage.body)

        doc.title shouldBe content.title
        doc.getH1ElementAsText shouldBe content.heading
        doc.hasTextFieldInput("postcode") shouldBe true
        doc.getTextFieldLabel("postcode") shouldBe content.postcodeLabel
        doc.hasTextFieldInput("filter") shouldBe true
        doc.getHintAsText shouldBe content.testHintLabel
        doc.getTextFieldLabel("filter") shouldBe content.filterLabel + " " + content.testHintLabel
        doc.getALinkText("manualAddress") shouldBe content.manualAddressLinkText
        doc.getLinkHrefAsText("manualAddress") shouldBe routes.AddressLookupController.edit(testId).url
        doc.getButtonContentAsText shouldBe content.submitLabel
      }
      "the postcode field error" in {
        val testPage = views.html.v2.lookup(testId, testBasicLevelJourneyConfigV2, lookupForm.withError("postcode", content.postcodeErrorMessage))
        val doc: Document = Jsoup.parse(testPage.body)

        doc.getFieldErrorMessageHeading shouldBe content.errorHeading
        doc.getFieldErrorMessageContent("postcode") shouldBe content.postcodeErrorMessage
      }
      "the filter field error" in {
        val testPage = views.html.v2.lookup(testId, testBasicLevelJourneyConfigV2, lookupForm.withError("filter", content.filterErrorMessage))
        val doc: Document = Jsoup.parse(testPage.body)

        doc.getFieldErrorMessageHeading shouldBe content.errorHeading
        doc.getFieldErrorMessageContent("filter") shouldBe content.filterErrorMessage
      }
    }
  }
}
