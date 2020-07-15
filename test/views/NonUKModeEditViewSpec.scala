package views

import config.FrontendAppConfig
import model._
import forms.ALFForms._
import org.jsoup.Jsoup
import play.api.i18n.Messages.Implicits._
import org.jsoup.nodes.Document
import play.api.i18n.MessagesApi
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import utils.TestConstants._


class NonUKModeEditViewSpec extends ViewSpec {

  object defaultContent {
    val title = "Enter address - navTitle - GOV.UK"
    val heading = "Enter address"
    val addressLine1 = "Address line 1"
    val addressLine2 = "Address line 2 (optional)"
    val addressLine3 = "Address line 3 (optional)"
    val townCity = "Town/city"
    val postcodeInternational = "Postcode (optional)"
    val country = "Country"
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
    val postcodeInternational = "editPostcode"
    val country = "editCountry"
    val continue = "editSubmit"
  }

  implicit val testRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
  val messages = app.injector.instanceOf[MessagesApi]
  val frontendAppConfig = app.injector.instanceOf[FrontendAppConfig]

  val configWithoutLabels = fullV2JourneyConfig.copy(
    options = fullV2JourneyOptions.copy(ukMode = Some(false)),
    labels = Some(JourneyLabels(
      en = Some(fullV2LanguageLabelsEn.copy(editPageLabels = None))
    )))

  "Non UK Mode Page" should {
    "when provided with no page config" in {

      val testPage = views.html.v2.non_uk_mode_edit(
        frontendAppConfig,
        id = testId,
        journeyData = fullV2JourneyDataNonUkMode.copy(config = configWithoutLabels),
        editForm = nonUkEditForm(false),
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

      doc.getTextFieldLabel("postcode") shouldBe defaultContent.postcodeInternational
      doc.getTextFieldInput("postcode").`val`() shouldBe ""

      doc.getTextFieldLabel("France") shouldBe defaultContent.country
      doc.getTextFieldInput("countryCode").`val`() shouldBe "FR"

      doc.getElementById("continue").text() shouldBe defaultContent.continue
    }
    "when provided with page config" in {
      val testPage = views.html.v2.non_uk_mode_edit(
        frontendAppConfig,
        id = testId,
        journeyData = fullV2JourneyDataNonUkMode,
        editForm = nonUkEditForm(false),
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

      doc.getTextFieldLabel("postcode") shouldBe configuredContent.postcodeInternational
      doc.getTextFieldInput("postcode").`val`() shouldBe ""

      doc.getTextFieldLabel("France") shouldBe configuredContent.country
      doc.getTextFieldInput("countryCode").`val`() shouldBe "FR"

      doc.getElementById("continue").text() shouldBe configuredContent.continue
    }
    "When there is > 1 country" in {
      val testPage = views.html.v2.non_uk_mode_edit(
        frontendAppConfig,
        id = testId,
        journeyData = fullV2JourneyDataNonUkMode.copy(config = configWithoutLabels),
        editForm = nonUkEditForm(false),
        countries = Seq("FR" -> "France", "AL" -> "Albanian"),
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

      doc.getTextFieldLabel("postcode") shouldBe defaultContent.postcodeInternational
      doc.getTextFieldInput("postcode").`val`() shouldBe ""

      doc.testElementExists("countryCode")
      doc.getDropList("countryCode").select("option").size() shouldBe 3
      doc.getDropList("countryCode") should have(
        option("countryCode-placeholder", "Select a country"),
        option("countryCode-FR", "France"),
        option("countryCode-AL", "Albanian")
      )

      doc.getElementById("continue").text() shouldBe defaultContent.continue
    }
  }


}
