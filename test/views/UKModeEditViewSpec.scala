package views

import model._
import forms.ALFForms._
import org.jsoup.Jsoup
import play.api.i18n.Messages.Implicits._
import org.jsoup.nodes.Document
import play.api.i18n.MessagesApi
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.twirl.api.Html
import utils.TestConstants._


class UKModeEditViewSpec extends ViewSpec {

  object defaultContent {
    val title = "Enter address"
    val heading = "Enter address"
    val addressLine1 = "Address line 1"
    val addressLine2 = "Address line 2 (optional)"
    val addressLine3 = "Address line 3 (optional)"
    val townCity = "Town/city"
    val postcodeUk ="UK postcode (optional)"
    val continue = "Continue"
  }

  object configuredContent {
    val title = "editTitle"
    val heading = "editHeading"
    val additionalStylesheet = "testStylesheetUrl"
    val addressLine1 = "editLine1"
    val addressLine2 = "editLine2"
    val addressLine3 = "editLine3"
    val townCity = "editLine4"
    val continue = "editSubmit"
  }

  implicit val testRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
  val messages = app.injector.instanceOf[MessagesApi]
  val configWithoutLabels = fullV2JourneyConfig.copy(
    labels = Some(JourneyLabels(
      en = Some(fullV2LanguageLabelsEn.copy(editPageLabels = None))
    )))

  "UK Mode Page" should {
    "when provided with no page config" in {

      val testPage = views.html.v2.uk_mode_edit(
        id = testId,
        journeyData = fullV2JourneyData.copy(config = configWithoutLabels),
        editForm = ukEditForm(false),
        countries = Seq("FR" -> "France"),
        false
      )
      val doc: Document = Jsoup.parse(testPage.body)

      doc.title shouldBe defaultContent.title
      doc.getH1ElementAsText shouldBe defaultContent.heading
      doc.getTextFieldLabel("line1") shouldBe defaultContent.addressLine1
      doc.getTextFieldInput("line1").`val`() shouldBe ""

      doc.getTextFieldLabel("line2") shouldBe defaultContent.addressLine2
      doc.getTextFieldInput("line2").`val`() shouldBe ""

      doc.getTextFieldLabel("line3") shouldBe defaultContent.addressLine3
      doc.getTextFieldInput("line3").`val`() shouldBe ""

      doc.getTextFieldLabel("town") shouldBe defaultContent.townCity
      doc.getTextFieldInput("town").`val`() shouldBe ""

      doc.getTextFieldLabel("postcode") shouldBe defaultContent.postcodeUk
      doc.getTextFieldInput("postcode").`val`() shouldBe ""

      doc.getElementById("continue").text() shouldBe defaultContent.continue
    }
  }

  "when provided with page config" in {
    val testPage = views.html.v2.uk_mode_edit(
      id = testId,
      journeyData = fullV2JourneyData,
      editForm = ukEditForm(false),
      countries = Seq("FR" -> "France"),
      false
    )
    val doc: Document = Jsoup.parse(testPage.body)

    doc.title shouldBe configuredContent.title
    doc.getH1ElementAsText shouldBe configuredContent.heading
    doc.getTextFieldLabel("line1") shouldBe configuredContent.addressLine1
    doc.getTextFieldInput("line1").`val`() shouldBe ""

    doc.getTextFieldLabel("line2") shouldBe configuredContent.addressLine2
    doc.getTextFieldInput("line2").`val`() shouldBe ""

    doc.getTextFieldLabel("line3") shouldBe configuredContent.addressLine3
    doc.getTextFieldInput("line3").`val`() shouldBe ""

    doc.getTextFieldLabel("town") shouldBe configuredContent.townCity
    doc.getTextFieldInput("town").`val`() shouldBe ""

    doc.getElementById("continue").text() shouldBe configuredContent.continue
  }

}
