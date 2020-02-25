package views

import controllers.routes
import forms.ALFForms.lookupForm
import model.JourneyConfigDefaults.{EnglishConstants, WelshConstants}
import model.MessageConstants.{EnglishMessageConstants, WelshMessageConstants}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.Play
import play.api.i18n.Messages.Implicits._
import play.api.i18n.MessagesApi
import play.api.mvc.{AnyContentAsEmpty, Cookie}
import play.api.test.FakeRequest
import play.twirl.api.Html
import utils.TestConstants._

class LookupViewSpec extends ViewSpec {

  object content {
    val title = "enLookupPageTitle"
    val heading = "enLookupPageHeading"
    val filterLabel = "enFilterLabel"
    val postcodeLabel = "enPostcodeLabel"
    val submitLabel = "enSubmitLabel"
    val manualAddressLinkText = "enManualAddressLinkText"

    val cyTitle = "cyLookupPageTitle"
    val cyHeading = "cyLookupPageHeading"
    val cyFilterLabel = "cyFilterLabel"
    val cyPostcodeLabel = "cyPostcodeLabel"
    val cySubmitLabel = "cySubmitLabel"
    val cyManualAddressLinkText = "cyManualAddressLinkText"

  }

  val messages = app.injector.instanceOf[MessagesApi]
  val testHtml = Html("")
  val testForm = lookupForm()
  val EnglishMessagesUKMode = EnglishMessageConstants(true)
  val EnglishMessagesNonUKMode = EnglishMessageConstants(false)
  val WelshMessagesUKMode = WelshMessageConstants(true)
  val WelshMessagesNonUKMode = WelshMessageConstants(false)
  val EnglishConstantsUKMode = EnglishConstants(true)
  val EnglishConstantsNonUKMode = EnglishConstants(false)
  val WelshConstantsUKMode = WelshConstants(true)
  val WelshConstantsNonUKMode = WelshConstants(false)

  "Lookup view page" should {
    "renders" when {
      "Welsh is disabled" when {
        import EnglishConstantsNonUKMode._
        import EnglishMessagesUKMode._

        implicit val testRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

        "default content" in {
          val testPage = views.html.v2.lookup(testId, testBasicLevelJourneyConfigV2, lookupForm(false), false)
          val doc: Document = Jsoup.parse(testPage.body)

          doc.title shouldBe LOOKUP_PAGE_TITLE
          doc.getH1ElementAsText shouldBe LOOKUP_PAGE_HEADING
          doc.hasTextFieldInput("postcode") shouldBe true
          doc.getTextFieldLabel("postcode") shouldBe LOOKUP_PAGE_POSTCODE_LABEL
          doc.hasTextFieldInput("filter") shouldBe true
          doc.getHintAsText shouldBe lookupFilterHint
          doc.getTextFieldLabel("filter") shouldBe LOOKUP_PAGE_FILTER_LABEL + " " + lookupFilterHint
          doc.getALinkText("manualAddress") shouldBe LOOKUP_PAGE_MANUAL_ADDRESS_LINK_TEXT
          doc.getLinkHrefAsText("manualAddress") shouldBe routes.AddressLookupController.edit(testId).url
          doc.getButtonContentAsText shouldBe LOOKUP_PAGE_SUBMIT_LABEL
        }
        "configured content" in {
          val testPage = views.html.v2.lookup(testId, testLookupLevelJourneyConfigV2, lookupForm(false), false)
          val doc: Document = Jsoup.parse(testPage.body)

          doc.title shouldBe content.title
          doc.getH1ElementAsText shouldBe content.heading
          doc.hasTextFieldInput("postcode") shouldBe true
          doc.getTextFieldLabel("postcode") shouldBe content.postcodeLabel
          doc.hasTextFieldInput("filter") shouldBe true
          doc.getHintAsText shouldBe lookupFilterHint
          doc.getTextFieldLabel("filter") shouldBe content.filterLabel + " " + lookupFilterHint
          doc.getALinkText("manualAddress") shouldBe content.manualAddressLinkText
          doc.getLinkHrefAsText("manualAddress") shouldBe routes.AddressLookupController.edit(testId).url
          doc.getButtonContentAsText shouldBe content.submitLabel
        }
        "the non-UK postcode field empty error" in {
          val testPage = views.html.v2.lookup(testId, testBasicLevelJourneyConfigV2, lookupForm(false).withError("postcode", EnglishMessagesNonUKMode.lookupPostcodeEmptyError), false)
          val doc: Document = Jsoup.parse(testPage.body)

          doc.getFieldErrorMessageHeading shouldBe EnglishMessagesNonUKMode.lookupErrorHeading
          doc.getFieldErrorMessageContent("postcode") shouldBe EnglishMessagesNonUKMode.lookupPostcodeEmptyError
        }
        "the UK postcode field empty error" in {
          val testPage = views.html.v2.lookup(testId, testBasicLevelJourneyConfigV2, lookupForm(false).withError("postcode", EnglishMessagesUKMode.lookupPostcodeEmptyError), false)
          val doc: Document = Jsoup.parse(testPage.body)

          doc.getFieldErrorMessageHeading shouldBe lookupErrorHeading
          doc.getFieldErrorMessageContent("postcode") shouldBe lookupPostcodeEmptyError
        }
        "the non-UK postcode field invalid error" in {
          val testPage = views.html.v2.lookup(testId, testBasicLevelJourneyConfigV2, lookupForm(false).withError("postcode", EnglishMessagesNonUKMode.lookupPostcodeInvalidError), false)
          val doc: Document = Jsoup.parse(testPage.body)

          doc.getFieldErrorMessageHeading shouldBe EnglishMessagesNonUKMode.lookupErrorHeading
          doc.getFieldErrorMessageContent("postcode") shouldBe EnglishMessagesNonUKMode.lookupPostcodeInvalidError
        }
        "the UK postcode field invalid error" in {
          val testPage = views.html.v2.lookup(testId, testBasicLevelJourneyConfigV2, lookupForm(false).withError("postcode", EnglishMessagesUKMode.lookupPostcodeInvalidError), false)
          val doc: Document = Jsoup.parse(testPage.body)

          doc.getFieldErrorMessageHeading shouldBe lookupErrorHeading
          doc.getFieldErrorMessageContent("postcode") shouldBe lookupPostcodeInvalidError
        }
        "the non-UK postcode field error" in {
          val testPage = views.html.v2.lookup(testId, testBasicLevelJourneyConfigV2, lookupForm(false).withError("postcode", EnglishMessagesNonUKMode.lookupPostcodeError), false)
          val doc: Document = Jsoup.parse(testPage.body)

          doc.getFieldErrorMessageHeading shouldBe lookupErrorHeading
          doc.getFieldErrorMessageContent("postcode") shouldBe EnglishMessagesNonUKMode.lookupPostcodeError
        }
        "the UK postcode field error" in {
          val testPage = views.html.v2.lookup(testId, testBasicLevelJourneyConfigV2, lookupForm(false).withError("postcode", EnglishMessagesUKMode.lookupPostcodeError), false)
          val doc: Document = Jsoup.parse(testPage.body)

          doc.getFieldErrorMessageHeading shouldBe lookupErrorHeading
          doc.getFieldErrorMessageContent("postcode") shouldBe EnglishMessagesUKMode.lookupPostcodeError
        }
        "the filter field error" in {
          val testPage = views.html.v2.lookup(testId, testBasicLevelJourneyConfigV2, lookupForm(false).withError("filter", EnglishMessagesUKMode.lookupFilterError), false)
          val doc: Document = Jsoup.parse(testPage.body)

          doc.getFieldErrorMessageHeading shouldBe lookupErrorHeading
          doc.getFieldErrorMessageContent("filter") shouldBe lookupFilterError
        }
      }
      "Welsh is enabled" when {
        import WelshConstantsNonUKMode._
        import WelshMessagesUKMode._

        implicit val testRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withCookies(Cookie(Play.langCookieName, "cy"))

        "default content exists in Welsh" in {
          val testPage = views.html.v2.lookup(testId, testDefaultCYJourneyConfigV2, lookupForm(true), true)
          val doc: Document = Jsoup.parse(testPage.body)

          doc.title shouldBe LOOKUP_PAGE_TITLE
          doc.getH1ElementAsText shouldBe LOOKUP_PAGE_HEADING
          doc.hasTextFieldInput("postcode") shouldBe true
          doc.getTextFieldLabel("postcode") shouldBe LOOKUP_PAGE_POSTCODE_LABEL
          doc.hasTextFieldInput("filter") shouldBe true
          doc.getHintAsText shouldBe lookupFilterHint
          doc.getTextFieldLabel("filter") shouldBe LOOKUP_PAGE_FILTER_LABEL + " " + lookupFilterHint
          doc.getALinkText("manualAddress") shouldBe LOOKUP_PAGE_MANUAL_ADDRESS_LINK_TEXT
          doc.getLinkHrefAsText("manualAddress") shouldBe routes.AddressLookupController.edit(testId).url
          doc.getButtonContentAsText shouldBe LOOKUP_PAGE_SUBMIT_LABEL
        }
        "default content doesn't exist in Welsh" in {
          import EnglishConstantsNonUKMode._

          val testPage = views.html.v2.lookup(testId, testBasicLevelJourneyConfigV2, lookupForm(false), false)
          val doc: Document = Jsoup.parse(testPage.body)

          doc.title shouldBe LOOKUP_PAGE_TITLE
          doc.getH1ElementAsText shouldBe LOOKUP_PAGE_HEADING
          doc.hasTextFieldInput("postcode") shouldBe true
          doc.getTextFieldLabel("postcode") shouldBe LOOKUP_PAGE_POSTCODE_LABEL
          doc.hasTextFieldInput("filter") shouldBe true
          doc.getHintAsText shouldBe EnglishMessagesUKMode.lookupFilterHint
          doc.getTextFieldLabel("filter") shouldBe LOOKUP_PAGE_FILTER_LABEL + " " + EnglishMessagesUKMode.lookupFilterHint
          doc.getALinkText("manualAddress") shouldBe LOOKUP_PAGE_MANUAL_ADDRESS_LINK_TEXT
          doc.getLinkHrefAsText("manualAddress") shouldBe routes.AddressLookupController.edit(testId).url
          doc.getButtonContentAsText shouldBe LOOKUP_PAGE_SUBMIT_LABEL
        }
        "configured content" in {
          val testPage = views.html.v2.lookup(testId, testLookupLevelCYJourneyConfigV2, lookupForm(true), true)
          val doc: Document = Jsoup.parse(testPage.body)

          doc.title shouldBe content.cyTitle
          doc.getH1ElementAsText shouldBe content.cyHeading
          doc.hasTextFieldInput("postcode") shouldBe true
          doc.getTextFieldLabel("postcode") shouldBe content.cyPostcodeLabel
          doc.hasTextFieldInput("filter") shouldBe true
          doc.getHintAsText shouldBe lookupFilterHint
          doc.getTextFieldLabel("filter") shouldBe content.cyFilterLabel + " " + lookupFilterHint
          doc.getALinkText("manualAddress") shouldBe content.cyManualAddressLinkText
          doc.getLinkHrefAsText("manualAddress") shouldBe routes.AddressLookupController.edit(testId).url
          doc.getButtonContentAsText shouldBe content.cySubmitLabel
        }
        "the non-UK postcode field empty error" in {
          val testPage = views.html.v2.lookup(testId, testBasicLevelJourneyConfigV2, lookupForm(true).withError("postcode", WelshMessagesNonUKMode.lookupPostcodeEmptyError), true)
          val doc: Document = Jsoup.parse(testPage.body)

          doc.getFieldErrorMessageHeading shouldBe lookupErrorHeading
          doc.getFieldErrorMessageContent("postcode") shouldBe WelshMessagesNonUKMode.lookupPostcodeEmptyError
        }
        "the UK postcode field empty error" in {
          val testPage = views.html.v2.lookup(testId, testBasicLevelJourneyConfigV2, lookupForm(true).withError("postcode", WelshMessagesUKMode.lookupPostcodeEmptyError), true)
          val doc: Document = Jsoup.parse(testPage.body)

          doc.getFieldErrorMessageHeading shouldBe lookupErrorHeading
          doc.getFieldErrorMessageContent("postcode") shouldBe WelshMessagesUKMode.lookupPostcodeEmptyError
        }
        "the non-UK postcode field invalid error" in {
          val testPage = views.html.v2.lookup(testId, testBasicLevelJourneyConfigV2, lookupForm(true).withError("postcode", WelshMessagesNonUKMode.lookupPostcodeInvalidError), true)
          val doc: Document = Jsoup.parse(testPage.body)

          doc.getFieldErrorMessageHeading shouldBe lookupErrorHeading
          doc.getFieldErrorMessageContent("postcode") shouldBe WelshMessagesNonUKMode.lookupPostcodeInvalidError
        }
        "the UK postcode field invalid error" in {
          val isUkMode = true
          val testPage = views.html.v2.lookup(testId, testBasicLevelJourneyConfigV2, lookupForm(true).withError("postcode", WelshMessagesUKMode.lookupPostcodeInvalidError), true)
          val doc: Document = Jsoup.parse(testPage.body)

          doc.getFieldErrorMessageHeading shouldBe lookupErrorHeading
          doc.getFieldErrorMessageContent("postcode") shouldBe WelshMessagesUKMode.lookupPostcodeInvalidError
        }
        "the non-UK postcode field error" in {
          val testPage = views.html.v2.lookup(testId, testBasicLevelJourneyConfigV2, lookupForm(true).withError("postcode", WelshMessagesNonUKMode.lookupPostcodeError), true)
          val doc: Document = Jsoup.parse(testPage.body)

          doc.getFieldErrorMessageHeading shouldBe lookupErrorHeading
          doc.getFieldErrorMessageContent("postcode") shouldBe WelshMessagesNonUKMode.lookupPostcodeError
        }
        "the UK postcode field error" in {
          val testPage = views.html.v2.lookup(testId, testBasicLevelJourneyConfigV2, lookupForm(true).withError("postcode", WelshMessagesUKMode.lookupPostcodeError), true)
          val doc: Document = Jsoup.parse(testPage.body)

          doc.getFieldErrorMessageHeading shouldBe lookupErrorHeading
          doc.getFieldErrorMessageContent("postcode") shouldBe WelshMessagesUKMode.lookupPostcodeError
        }
        "the filter field error" in {
          val testPage = views.html.v2.lookup(testId, testBasicLevelJourneyConfigV2, lookupForm(true).withError("filter", WelshMessagesUKMode.lookupFilterError), true)
          val doc: Document = Jsoup.parse(testPage.body)

          doc.getFieldErrorMessageHeading shouldBe lookupErrorHeading
          doc.getFieldErrorMessageContent("filter") shouldBe lookupFilterError
        }
      }
    }
  }
}
