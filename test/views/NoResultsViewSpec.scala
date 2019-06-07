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

  object Content {
    val title = "Can't find any addresses"
    def heading(postcode: String) = s"We can not find any addresses for $postcode"
    val back = "Back"
    val tryAgainButton = "Try a different postcode"
    val enterManualLink = "Enter the address manually"
  }

  "The 'No results' view" when {
    "rendered with the default config" should {
      "Render the view and display the Back button" in {
        val noResultsView = no_results(id = testJourneyId, journeyData = journeyDataV2Minimal, postcode = testPostCode)
        val doc = Jsoup.parse(noResultsView.body)

        doc.title shouldBe Content.title
        doc.getBackLinkText shouldBe Content.back
        doc.getH1ElementAsText shouldBe Content.heading(testPostCode)
        doc.getButtonContentAsText shouldBe Content.tryAgainButton
        doc.getALinkText("enterManual") shouldBe Content.enterManualLink
        doc.getLinkHrefAsText("enterManual") shouldBe routes.AddressLookupController.edit(testJourneyId, None, Some(true)).url
      }
    }

    "rendered with custom config" should {
      "Render the view without the Back button" in {
        val noResultsView = no_results(id = testJourneyId, journeyData = testNoResultsConfig, postcode = testPostCode)
        val doc = Jsoup.parse(noResultsView.body)

        doc.title shouldBe Content.title
        doc.getBackLinkText shouldBe empty
        doc.getH1ElementAsText shouldBe Content.heading(testPostCode)
        doc.getButtonContentAsText shouldBe Content.tryAgainButton
        doc.getALinkText("enterManual") shouldBe Content.enterManualLink
        doc.getLinkHrefAsText("enterManual") shouldBe routes.AddressLookupController.edit(testJourneyId, None, Some(true)).url
      }
    }
  }

}
