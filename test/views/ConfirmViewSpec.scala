package views

import model.{ConfirmPageLabels, ConfirmableAddress, JourneyDataV2}
import org.jsoup.Jsoup
import play.twirl.api.Html
import org.jsoup.nodes.Document
import utils.TestConstants.testAddress
import utils.TestConstants.fullV2JourneyDataCustomConfig
import play.api.i18n.Messages.Implicits._
import play.api.i18n.MessagesApi
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest

class ConfirmViewSpec  extends ViewSpec {

  class Setup(journeyDataV2: JourneyDataV2, selectedAddress: Option[ConfirmableAddress] = Some(testAddress)) {

    implicit val testRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
    val messages = app.injector.instanceOf[MessagesApi]
    val testHtml = Html("")
    val journeyDataV2Expected = journeyDataV2

    val testPage = views.html.v2.confirm("", journeyDataV2, selectedAddress)

    val doc: Document = Jsoup.parse(testPage.body)
  }

  "ConfirmView" should {

    "render" when {
      "show back button is true" in new Setup(fullV2JourneyDataCustomConfig(testShowBackButtons = Some(true))) {
        doc.getLinkTextFromHref("javascript:history.back()") shouldBe "Back"
        doc.getH1ElementAsText shouldBe journeyDataV2Expected.config.labels.get.en.get.confirmPageLabels.get.heading.get
        doc.getFirstH2ElementAsText shouldBe journeyDataV2Expected.config.labels.get.en.get.confirmPageLabels.get.infoSubheading.get
        doc.getElementById("infoMessage").text shouldBe journeyDataV2Expected.config.labels.get.en.get.confirmPageLabels.get.infoMessage.get
        doc.getElementById("searchAgainLink").text shouldBe journeyDataV2Expected.config.labels.get.en.get.confirmPageLabels.get.searchAgainLinkText.get
        doc.getElementById("changeLink").text shouldBe journeyDataV2Expected.config.labels.get.en.get.confirmPageLabels.get.changeLinkText.get
        doc.getElementById("confirmChangeText").text shouldBe journeyDataV2Expected.config.labels.get.en.get.confirmPageLabels.get.confirmChangeText.get
        doc.getButtonContentAsText shouldBe journeyDataV2Expected.config.labels.get.en.get.confirmPageLabels.get.submitLabel.get

      }

      "show back button is false" in  new Setup(fullV2JourneyDataCustomConfig(testShowBackButtons = Some(false))){
        doc.getLinkTextFromHref("javascript:history.back()") shouldBe ""
        doc.getH1ElementAsText shouldBe journeyDataV2Expected.config.labels.get.en.get.confirmPageLabels.get.heading.get
        doc.getFirstH2ElementAsText shouldBe journeyDataV2Expected.config.labels.get.en.get.confirmPageLabels.get.infoSubheading.get
        doc.getElementById("infoMessage").text shouldBe journeyDataV2Expected.config.labels.get.en.get.confirmPageLabels.get.infoMessage.get
        doc.getElementById("searchAgainLink").text shouldBe journeyDataV2Expected.config.labels.get.en.get.confirmPageLabels.get.searchAgainLinkText.get
        doc.getElementById("changeLink").text shouldBe journeyDataV2Expected.config.labels.get.en.get.confirmPageLabels.get.changeLinkText.get
        doc.getElementById("confirmChangeText").text shouldBe journeyDataV2Expected.config.labels.get.en.get.confirmPageLabels.get.confirmChangeText.get
        doc.getButtonContentAsText shouldBe journeyDataV2Expected.config.labels.get.en.get.confirmPageLabels.get.submitLabel.get
      }


      "a confirmed address has been defined" in new Setup(fullV2JourneyDataCustomConfig()) {
        doc.getLinkTextFromHref("javascript:history.back()") shouldBe "Back"
        doc.getH1ElementAsText shouldBe journeyDataV2Expected.config.labels.get.en.get.confirmPageLabels.get.heading.get
        doc.getFirstH2ElementAsText shouldBe journeyDataV2Expected.config.labels.get.en.get.confirmPageLabels.get.infoSubheading.get
        doc.getElementById("infoMessage").text shouldBe journeyDataV2Expected.config.labels.get.en.get.confirmPageLabels.get.infoMessage.get
        doc.getElementById("searchAgainLink").text shouldBe journeyDataV2Expected.config.labels.get.en.get.confirmPageLabels.get.searchAgainLinkText.get
        doc.getElementById("changeLink").text shouldBe journeyDataV2Expected.config.labels.get.en.get.confirmPageLabels.get.changeLinkText.get
        doc.getElementById("confirmChangeText").text shouldBe journeyDataV2Expected.config.labels.get.en.get.confirmPageLabels.get.confirmChangeText.get
        doc.getButtonContentAsText shouldBe journeyDataV2Expected.config.labels.get.en.get.confirmPageLabels.get.submitLabel.get
      }

      "a confirmed address has not been defined" in new Setup(fullV2JourneyDataCustomConfig(), None) {
        doc.getLinkTextFromHref("javascript:history.back()") shouldBe "Back"
        doc.getH1ElementAsText shouldBe journeyDataV2Expected.config.labels.get.en.get.confirmPageLabels.get.heading.get
        doc.getFirstH2ElementAsText shouldBe ""
        Option(doc.getElementById("infoMessage")) shouldBe None
        Option(doc.getElementById("searchAgainLink")) shouldBe None
        Option(doc.getElementById("changeLink")) shouldBe None
        Option(doc.getElementById("confirmChangeText")) shouldBe None
        doc.getElementById("lookupLink").parent().text() shouldBe  "You have not selected any address. Please search for and select your address."
      }

      "show subheading and info is true" in new Setup(fullV2JourneyDataCustomConfig(testShowSubHeading = Some(true))) {
        doc.getLinkTextFromHref("javascript:history.back()") shouldBe "Back"
        doc.getH1ElementAsText shouldBe journeyDataV2Expected.config.labels.get.en.get.confirmPageLabels.get.heading.get
        doc.getFirstH2ElementAsText shouldBe journeyDataV2Expected.config.labels.get.en.get.confirmPageLabels.get.infoSubheading.get
        doc.getElementById("infoMessage").text shouldBe journeyDataV2Expected.config.labels.get.en.get.confirmPageLabels.get.infoMessage.get
        doc.getElementById("searchAgainLink").text shouldBe journeyDataV2Expected.config.labels.get.en.get.confirmPageLabels.get.searchAgainLinkText.get
        doc.getElementById("changeLink").text shouldBe journeyDataV2Expected.config.labels.get.en.get.confirmPageLabels.get.changeLinkText.get
        doc.getElementById("confirmChangeText").text shouldBe journeyDataV2Expected.config.labels.get.en.get.confirmPageLabels.get.confirmChangeText.get
        doc.getButtonContentAsText shouldBe journeyDataV2Expected.config.labels.get.en.get.confirmPageLabels.get.submitLabel.get
      }

      "show subheading and info is false" in new Setup(fullV2JourneyDataCustomConfig(testShowSubHeading = Some(false))){
        doc.getLinkTextFromHref("javascript:history.back()") shouldBe "Back"
        doc.getH1ElementAsText shouldBe journeyDataV2Expected.config.labels.get.en.get.confirmPageLabels.get.heading.get
        doc.getFirstH2ElementAsText shouldBe ""
        Option(doc.getElementById("infoMessage")) shouldBe None
        doc.getElementById("searchAgainLink").text shouldBe journeyDataV2Expected.config.labels.get.en.get.confirmPageLabels.get.searchAgainLinkText.get
        doc.getElementById("changeLink").text shouldBe journeyDataV2Expected.config.labels.get.en.get.confirmPageLabels.get.changeLinkText.get
        doc.getElementById("confirmChangeText").text shouldBe journeyDataV2Expected.config.labels.get.en.get.confirmPageLabels.get.confirmChangeText.get
        doc.getButtonContentAsText shouldBe journeyDataV2Expected.config.labels.get.en.get.confirmPageLabels.get.submitLabel.get
      }

      "show search again link is true " in new Setup(fullV2JourneyDataCustomConfig(testShowSearchAgainLink = Some(true))){
        doc.getLinkTextFromHref("javascript:history.back()") shouldBe "Back"
        doc.getH1ElementAsText shouldBe journeyDataV2Expected.config.labels.get.en.get.confirmPageLabels.get.heading.get
        doc.getFirstH2ElementAsText shouldBe journeyDataV2Expected.config.labels.get.en.get.confirmPageLabels.get.infoSubheading.get
        doc.getElementById("infoMessage").text shouldBe journeyDataV2Expected.config.labels.get.en.get.confirmPageLabels.get.infoMessage.get
        doc.getElementById("searchAgainLink").text shouldBe journeyDataV2Expected.config.labels.get.en.get.confirmPageLabels.get.searchAgainLinkText.get
        doc.getElementById("changeLink").text shouldBe journeyDataV2Expected.config.labels.get.en.get.confirmPageLabels.get.changeLinkText.get
        doc.getElementById("confirmChangeText").text shouldBe journeyDataV2Expected.config.labels.get.en.get.confirmPageLabels.get.confirmChangeText.get
        doc.getButtonContentAsText shouldBe journeyDataV2Expected.config.labels.get.en.get.confirmPageLabels.get.submitLabel.get
      }

      "show search again link is false " in new Setup(fullV2JourneyDataCustomConfig(testShowSearchAgainLink = Some(false))){
        doc.getLinkTextFromHref("javascript:history.back()") shouldBe "Back"
        doc.getH1ElementAsText shouldBe journeyDataV2Expected.config.labels.get.en.get.confirmPageLabels.get.heading.get
        doc.getFirstH2ElementAsText shouldBe journeyDataV2Expected.config.labels.get.en.get.confirmPageLabels.get.infoSubheading.get
        doc.getElementById("infoMessage").text shouldBe journeyDataV2Expected.config.labels.get.en.get.confirmPageLabels.get.infoMessage.get
        Option(doc.getElementById("searchAgainLink")) shouldBe  None
        doc.getElementById("changeLink").text shouldBe journeyDataV2Expected.config.labels.get.en.get.confirmPageLabels.get.changeLinkText.get
        doc.getElementById("confirmChangeText").text shouldBe journeyDataV2Expected.config.labels.get.en.get.confirmPageLabels.get.confirmChangeText.get
        doc.getButtonContentAsText shouldBe journeyDataV2Expected.config.labels.get.en.get.confirmPageLabels.get.submitLabel.get
      }

      "show change link is true " in new Setup(fullV2JourneyDataCustomConfig(testShowChangeLink = Some(true))){
        doc.getLinkTextFromHref("javascript:history.back()") shouldBe "Back"
        doc.getH1ElementAsText shouldBe journeyDataV2Expected.config.labels.get.en.get.confirmPageLabels.get.heading.get
        doc.getFirstH2ElementAsText shouldBe journeyDataV2Expected.config.labels.get.en.get.confirmPageLabels.get.infoSubheading.get
        doc.getElementById("infoMessage").text shouldBe journeyDataV2Expected.config.labels.get.en.get.confirmPageLabels.get.infoMessage.get
        doc.getElementById("searchAgainLink").text shouldBe journeyDataV2Expected.config.labels.get.en.get.confirmPageLabels.get.searchAgainLinkText.get
        doc.getElementById("changeLink").text shouldBe journeyDataV2Expected.config.labels.get.en.get.confirmPageLabels.get.changeLinkText.get
        doc.getElementById("confirmChangeText").text shouldBe journeyDataV2Expected.config.labels.get.en.get.confirmPageLabels.get.confirmChangeText.get
        doc.getButtonContentAsText shouldBe journeyDataV2Expected.config.labels.get.en.get.confirmPageLabels.get.submitLabel.get
      }

      "show change link is false " in new Setup(fullV2JourneyDataCustomConfig(testShowChangeLink = Some(false))){
        doc.getLinkTextFromHref("javascript:history.back()") shouldBe "Back"
        doc.getH1ElementAsText shouldBe journeyDataV2Expected.config.labels.get.en.get.confirmPageLabels.get.heading.get
        doc.getFirstH2ElementAsText shouldBe journeyDataV2Expected.config.labels.get.en.get.confirmPageLabels.get.infoSubheading.get
        doc.getElementById("infoMessage").text shouldBe journeyDataV2Expected.config.labels.get.en.get.confirmPageLabels.get.infoMessage.get
        doc.getElementById("searchAgainLink").text shouldBe journeyDataV2Expected.config.labels.get.en.get.confirmPageLabels.get.searchAgainLinkText.get
        Option(doc.getElementById("changeLink")) shouldBe None
        doc.getElementById("confirmChangeText").text shouldBe journeyDataV2Expected.config.labels.get.en.get.confirmPageLabels.get.confirmChangeText.get
        doc.getButtonContentAsText shouldBe journeyDataV2Expected.config.labels.get.en.get.confirmPageLabels.get.submitLabel.get
      }

      "show confirm change options is true" in new Setup(fullV2JourneyDataCustomConfig(testshowConfirmChangeLink = Some(true))) {
        doc.getLinkTextFromHref("javascript:history.back()") shouldBe "Back"
        doc.getH1ElementAsText shouldBe journeyDataV2Expected.config.labels.get.en.get.confirmPageLabels.get.heading.get
        doc.getFirstH2ElementAsText shouldBe journeyDataV2Expected.config.labels.get.en.get.confirmPageLabels.get.infoSubheading.get
        doc.getElementById("infoMessage").text shouldBe journeyDataV2Expected.config.labels.get.en.get.confirmPageLabels.get.infoMessage.get
        doc.getElementById("searchAgainLink").text shouldBe journeyDataV2Expected.config.labels.get.en.get.confirmPageLabels.get.searchAgainLinkText.get
        doc.getElementById("changeLink").text shouldBe journeyDataV2Expected.config.labels.get.en.get.confirmPageLabels.get.changeLinkText.get
        doc.getElementById("confirmChangeText").text shouldBe journeyDataV2Expected.config.labels.get.en.get.confirmPageLabels.get.confirmChangeText.get
        doc.getButtonContentAsText shouldBe journeyDataV2Expected.config.labels.get.en.get.confirmPageLabels.get.submitLabel.get
      }

      "show confirm change options is false" in new Setup(fullV2JourneyDataCustomConfig(testshowConfirmChangeLink = Some(false))){
        doc.getLinkTextFromHref("javascript:history.back()") shouldBe "Back"
        doc.getH1ElementAsText shouldBe journeyDataV2Expected.config.labels.get.en.get.confirmPageLabels.get.heading.get
        doc.getFirstH2ElementAsText shouldBe journeyDataV2Expected.config.labels.get.en.get.confirmPageLabels.get.infoSubheading.get
        doc.getElementById("infoMessage").text shouldBe journeyDataV2Expected.config.labels.get.en.get.confirmPageLabels.get.infoMessage.get
        doc.getElementById("searchAgainLink").text shouldBe journeyDataV2Expected.config.labels.get.en.get.confirmPageLabels.get.searchAgainLinkText.get
        doc.getElementById("changeLink").text shouldBe journeyDataV2Expected.config.labels.get.en.get.confirmPageLabels.get.changeLinkText.get
        Option(doc.getElementById("confirmChangeText")) shouldBe None
        doc.getButtonContentAsText shouldBe journeyDataV2Expected.config.labels.get.en.get.confirmPageLabels.get.submitLabel.get
      }

    }
  }

}
