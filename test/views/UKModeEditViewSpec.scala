package views

import config.FrontendAppConfig
import forms.ALFForms._
import model._
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages.Implicits._
import play.api.i18n.MessagesApi
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import utils.TestConstants._


class UKModeEditViewSpec extends ViewSpec {

  object customContent {
    val title = "Custom Enter address"
    val heading = "Custom Enter address"
    val addressLine1 = "Custom Address line 1"
    val addressLine2 = "Custom Address line 2 (optional)"
    val addressLine3 = "Custom Address line 3 (optional)"
    val townCity = "Custom Town/city"
    val postcodeUk ="Custom UK postcode (optional)"
    val country = "Custom country"
    val continue = "Custom Continue"
  }

  object defaultWelshContent {
    val title = "Nodwch cyfeiriad"
    val heading = "Nodwch cyfeiriad"
    val addressLine1 = "Llinell cyfeiriad 1"          
    val addressLine2 = "Llinell cyfeiriad 2 (dewisol)"
    val addressLine3 = "Llinell cyfeiriad 3 (dewisol)"
    val townCity = "Tref/dinas"
    val postcodeUk ="Cod post y DU (dewisol)"
    val country = "Gwlad"
    val continue = "Yn eich blaen"
  }

  object defaultContent {
    val title = "Enter address - navTitle - GOV.UK"
    val heading = "Enter address"
    val addressLine1 = "Address line 1"
    val addressLine2 = "Address line 2 (optional)"
    val addressLine3 = "Address line 3 (optional)"
    val townCity = "Town/city"
    val postcodeUk ="UK postcode (optional)"
    val continue = "Continue"
  }

  object configuredContent {
    val title = "editTitle - navTitle - GOV.UK"
    val heading = "editHeading"
    val additionalStylesheet = "testStylesheetUrl"
    val addressLine1 = "editLine1"
    val addressLine2 = "editLine2"
    val addressLine3 = "editLine3"
    val townCity = "editLine4"
    val postcodeUk ="editPostcode"
    val continue = "editSubmit"
  }

  implicit val testRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
  val messages = app.injector.instanceOf[MessagesApi]
  val frontendAppConfig = app.injector.instanceOf[FrontendAppConfig]

  val configWithoutLabels = fullV2JourneyConfig.copy(
    labels = Some(JourneyLabels(
      en = Some(fullV2LanguageLabelsEn.copy(editPageLabels = None))
    )))

  val configWithCustomLabels = fullV2JourneyConfig.copy(
    labels = Some(JourneyLabels(
      en = Some(fullV2LanguageLabelsEn.copy(
              editPageLabels = Some(EditPageLabels(Some(customContent.title),
                                                   Some(customContent.heading),
                                                   Some(customContent.addressLine1),
                                                   Some(customContent.addressLine2),
                                                   Some(customContent.addressLine3),
                                                   Some(customContent.townCity),
                                                   Some(customContent.postcodeUk),
                                                   Some(customContent.country),
                                                   Some(customContent.continue))))
    ))))

  "UK Mode Page" should {
    "when provided with no page config" in {

      val testPage = views.html.v2.uk_mode_edit(
        frontendAppConfig,
        id = testId,
        journeyData = fullV2JourneyData.copy(config = configWithoutLabels),
        editForm = ukEditForm(false),
        countries = Seq("FR" -> "France"),
        isWelsh = false
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
      frontendAppConfig,
      id = testId,
      journeyData = fullV2JourneyData,
      editForm = ukEditForm(false),
      countries = Seq("FR" -> "France"),
      isWelsh = false
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

    doc.getTextFieldLabel("postcode") shouldBe configuredContent.postcodeUk
    doc.getTextFieldInput("postcode").`val`() shouldBe ""

    doc.getElementById("continue").text() shouldBe configuredContent.continue
  }

  "when provided with custom page config" in {
    val testPage = views.html.v2.uk_mode_edit(
      frontendAppConfig,
      id = testId,
      journeyData = fullV2JourneyData.copy(config = configWithCustomLabels),
      editForm = ukEditForm(false),
      countries = Seq("FR" -> "France"),
      isWelsh = false
    )
    val doc: Document = Jsoup.parse(testPage.body)

    doc.title shouldBe customContent.title + " - navTitle - GOV.UK"
    doc.getH1ElementAsText shouldBe customContent.heading
    doc.getTextFieldLabel("line1") shouldBe customContent.addressLine1
    doc.getTextFieldInput("line1").`val`() shouldBe ""

    doc.getTextFieldLabel("line2") shouldBe customContent.addressLine2
    doc.getTextFieldInput("line2").`val`() shouldBe ""

    doc.getTextFieldLabel("line3") shouldBe customContent.addressLine3
    doc.getTextFieldInput("line3").`val`() shouldBe ""

    doc.getTextFieldLabel("town") shouldBe customContent.townCity
    doc.getTextFieldInput("town").`val`() shouldBe ""

    doc.getTextFieldLabel("postcode") shouldBe customContent.postcodeUk
    doc.getTextFieldInput("postcode").`val`() shouldBe ""

    doc.getElementById("continue").text() shouldBe customContent.continue
  }

  "when provided with default page config in Welsh" in {
    val testPage = views.html.v2.uk_mode_edit(
      frontendAppConfig,
      id = testId,
      journeyData = fullV2JourneyData,
      editForm = ukEditForm(false),
      countries = Seq("FR" -> "France"),
      isWelsh = true
    )
    val doc: Document = Jsoup.parse(testPage.body)

    doc.title shouldBe defaultWelshContent.title
    doc.getH1ElementAsText shouldBe defaultWelshContent.heading
    doc.getTextFieldLabel("line1") shouldBe defaultWelshContent.addressLine1
    doc.getTextFieldInput("line1").`val`() shouldBe ""

    doc.getTextFieldLabel("line2") shouldBe defaultWelshContent.addressLine2
    doc.getTextFieldInput("line2").`val`() shouldBe ""

    doc.getTextFieldLabel("line3") shouldBe defaultWelshContent.addressLine3
    doc.getTextFieldInput("line3").`val`() shouldBe ""

    doc.getTextFieldLabel("town") shouldBe defaultWelshContent.townCity
    doc.getTextFieldInput("town").`val`() shouldBe ""

    doc.getTextFieldLabel("postcode") shouldBe defaultWelshContent.postcodeUk
    doc.getTextFieldInput("postcode").`val`() shouldBe ""

    doc.getElementById("continue").text() shouldBe defaultWelshContent.continue
  }

}
