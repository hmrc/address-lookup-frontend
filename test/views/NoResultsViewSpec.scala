package views

import controllers.routes
import model.{JourneyConfigV2, JourneyDataV2, JourneyOptions}
import org.jsoup.Jsoup
import play.api.test.FakeRequest
import play.api.i18n.Messages.Implicits._
import play.api.i18n.MessagesApi
import play.api.mvc.AnyContentAsEmpty
import utils.TestConstants._
import views.html.v2.no_results

class NoResultsViewSpec extends ViewSpec {

  implicit val testRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
  implicit val messages = app.injector.instanceOf[MessagesApi]

  object EnglishContent {
    val title = "We can not find any addresses"
    def heading(postcode: String) = s"We can not find any addresses for $postcode"
    val back = "Back"
    val tryAgainButton = "Try a different postcode"
    val enterManualLink = "Enter the address manually"
  }

  object WelshContent {
    val title = "Ni allwn ddod o hyd i unrhyw gyfeiriadau"
    def heading(postcode: String) = s"Ni allwn ddod o hyd i unrhyw gyfeiriadau ar gyfer $postcode"
    val back = "Yn ôl"
    val tryAgainButton = "Rhowch gynnig ar god post gwahanol"
    val enterManualLink = "Nodwch y cyfeiriad â llaw"
  }

  "The 'No results' view" when {
    "rendered with the default English config" should {
      "Render the view and display the Back button" in {
        val noResultsView = no_results(id = testJourneyId, journeyData = journeyDataV2Minimal, postcode = testPostCode)
        val doc = Jsoup.parse(noResultsView.body)

        doc.title shouldBe EnglishContent.title
        doc.getBackLinkText shouldBe EnglishContent.back
        doc.getH1ElementAsText shouldBe EnglishContent.heading(testPostCode)
        doc.getButtonContentAsText shouldBe EnglishContent.tryAgainButton
        doc.getALinkText("enterManual") shouldBe EnglishContent.enterManualLink
        doc.getLinkHrefAsText("enterManual") shouldBe routes.AddressLookupController.edit(testJourneyId, None, Some(true)).url
      }
    }

    "rendered with custom English config" should {
      "Render the view without the Back button" in {
        val noResultsView = no_results(id = testJourneyId, journeyData = testNoResultsConfig, postcode = testPostCode)
        val doc = Jsoup.parse(noResultsView.body)

        doc.title shouldBe EnglishContent.title
        doc.getBackLinkText shouldBe empty
        doc.getH1ElementAsText shouldBe EnglishContent.heading(testPostCode)
        doc.getButtonContentAsText shouldBe EnglishContent.tryAgainButton
        doc.getALinkText("enterManual") shouldBe EnglishContent.enterManualLink
        doc.getLinkHrefAsText("enterManual") shouldBe routes.AddressLookupController.edit(testJourneyId, None, Some(true)).url
      }
    }

    "rendered with the default Welsh config" should {
      "Render the view and display the Back button" in {
        val noResultsView = no_results(id = testJourneyId, journeyData = journeyDataV2EnglishAndWelshMinimal, postcode = testPostCode, isWelsh = true)
        val doc = Jsoup.parse(noResultsView.body)

        doc.title shouldBe WelshContent.title
        doc.getBackLinkText shouldBe WelshContent.back
        doc.getH1ElementAsText shouldBe WelshContent.heading(testPostCode)
        doc.getButtonContentAsText shouldBe WelshContent.tryAgainButton
        doc.getALinkText("enterManual") shouldBe WelshContent.enterManualLink
        doc.getLinkHrefAsText("enterManual") shouldBe routes.AddressLookupController.edit(testJourneyId, None, Some(true)).url
      }
    }
  }

}
