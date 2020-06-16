package views

import controllers.routes
import org.jsoup.Jsoup
import play.api.i18n.Messages.Implicits._
import play.api.i18n.MessagesApi
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import utils.TestConstants._
import views.html.no_results

class NoResultsV1ViewSpec extends ViewSpec {

  implicit val testRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
  implicit val messages = app.injector.instanceOf[MessagesApi]

  object EnglishContent {
    val title = "We can not find any addresses"
    def heading(postcode: String) = s"We can not find any addresses for $postcode"
    val back = "Back"
    val tryAgainButton = "Try a different postcode"
    val enterManualLink = "Enter the address manually"
  }

  "The 'No results' view" when {
    "rendered with the default English config" should {
      "Render the view and display the Back button with UK Mode = false" in {
        val noResultsView = no_results(id = testJourneyId, journeyData = fullV1JourneyData.copy(config = fullV1JourneyConfig.copy(ukMode = Some(false))), postcode = testPostCode)
        val doc = Jsoup.parse(noResultsView.body)

        doc.title shouldBe EnglishContent.title
        doc.getBackLinkText shouldBe EnglishContent.back
        doc.getH1ElementAsText shouldBe EnglishContent.heading(testPostCode)
        doc.getButtonContentAsText shouldBe EnglishContent.tryAgainButton
        doc.getALinkText("enterManual") shouldBe EnglishContent.enterManualLink
        doc.getLinkHrefAsText("enterManual") shouldBe routes.AddressLookupController.edit(testJourneyId, None, Some(false)).url
      }
    }

    "rendered with the default English config (UK Mode = true)" should {
      "Render the view and display the Back button with UK Mode = true" in {
        val noResultsView = no_results(id = testJourneyId, journeyData = fullV1JourneyData, postcode = testPostCode)
        val doc = Jsoup.parse(noResultsView.body)

        doc.title shouldBe EnglishContent.title
        doc.getBackLinkText shouldBe EnglishContent.back
        doc.getH1ElementAsText shouldBe EnglishContent.heading(testPostCode)
        doc.getButtonContentAsText shouldBe EnglishContent.tryAgainButton
        doc.getALinkText("enterManual") shouldBe EnglishContent.enterManualLink
        doc.getLinkHrefAsText("enterManual") shouldBe routes.AddressLookupController.edit(testJourneyId, None, Some(true)).url
      }
    }
  }

}
