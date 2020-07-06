package views

import controllers.routes
import model.JourneyConfigDefaults
import org.jsoup.Jsoup
import play.api.i18n.Messages.Implicits._
import play.api.i18n.MessagesApi
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.twirl.api.Html
import utils.TestConstants._

class TooManyResultsV1ViewSpec extends ViewSpec {
  implicit val testRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
  implicit val messages = app.injector.instanceOf[MessagesApi]

  val testHtml = Html("")

  object tooManyResultsMessages {
    val title = "No results found"
    val heading1 = "There are too many results"
    val heading2 = "We couldn't find any results for that property name or number"

    def bullet1(postcode: String) = s"$postcode for postcode"

    val bullet2NoFilter = "nothing for property name or number"

    def bullet2WithFilter(filter: String) = s"'$filter' for name or number"

    val line1 = "You entered:"
    val back = "Back"
  }

  val EnglishConstantsUkMode = JourneyConfigDefaults.EnglishConstants(true)

  "The English 'Too Many Results' page" should {
    "be rendered" when {
      "the back buttons are enabled in the journey config" when {
        "no filter has been entered" when {
          "there are too many addresses" in {
            val doc = Jsoup.parse(views.html.too_many_results(
              id = testJourneyId,
              journeyData = fullV1JourneyData,
              lookup = model.Lookup(
                None,
                testPostCode
              ),
              firstLookup = true
            ).body)

            doc.getBackLinkText shouldBe tooManyResultsMessages.back
            doc.title shouldBe tooManyResultsMessages.title
            doc.getH1ElementAsText shouldBe tooManyResultsMessages.heading1
            doc.paras.not(".language-select").get(2).text shouldBe tooManyResultsMessages.line1
            System.out.println(doc)
            doc.bulletPointList.select("li").first.text shouldBe tooManyResultsMessages.bullet1(testPostCode)
            doc.bulletPointList.select("li").last.text shouldBe tooManyResultsMessages.bullet2NoFilter
            doc.getALinkText("enterManual") shouldBe EnglishConstantsUkMode.EDIT_LINK_TEXT
            doc.getLinkHrefAsText("enterManual") shouldBe routes.AddressLookupController.edit(testJourneyId, Some(testPostCode)).url
          }
        }

        "a filter has been entered with ukMode = false" when {
          "there are too many addresses" in {
            val doc = Jsoup.parse(views.html.too_many_results(
              id = testJourneyId,
              journeyData = fullV1JourneyData.copy(config = fullV1JourneyConfig.copy(ukMode = Some(false))),
              lookup = model.Lookup(
                Some(testFilterValue),
                testPostCode
              ),
              firstLookup = false
            ).body)

            doc.getBackLinkText shouldBe tooManyResultsMessages.back
            doc.title shouldBe tooManyResultsMessages.title
            doc.getH1ElementAsText shouldBe tooManyResultsMessages.heading2
            doc.paras.not(".language-select").get(2).text shouldBe tooManyResultsMessages.line1
            doc.bulletPointList.select("li").first.text shouldBe tooManyResultsMessages.bullet1(testPostCode)
            doc.bulletPointList.select("li").last.text shouldBe tooManyResultsMessages.bullet2WithFilter(testFilterValue)
            doc.getALinkText("enterManual") shouldBe EnglishConstantsUkMode.EDIT_LINK_TEXT
            doc.getLinkHrefAsText("enterManual") shouldBe routes.AddressLookupController.edit(testJourneyId, Some(testPostCode)).url
          }
        }

        "a filter has been entered with ukMode = true" when {
          "there are too many addresses" in {
            val doc = Jsoup.parse(views.html.too_many_results(
              id = testJourneyId,
              journeyData = fullV1JourneyData,
              lookup = model.Lookup(
                Some(testFilterValue),
                testPostCode
              ),
              firstLookup = false
            ).body)

            doc.getBackLinkText shouldBe tooManyResultsMessages.back
            doc.title shouldBe tooManyResultsMessages.title
            doc.getH1ElementAsText shouldBe tooManyResultsMessages.heading2
            doc.paras.not(".language-select").get(2).text shouldBe tooManyResultsMessages.line1
            doc.bulletPointList.select("li").first.text shouldBe tooManyResultsMessages.bullet1(testPostCode)
            doc.bulletPointList.select("li").last.text shouldBe tooManyResultsMessages.bullet2WithFilter(testFilterValue)
            doc.getALinkText("enterManual") shouldBe EnglishConstantsUkMode.EDIT_LINK_TEXT
            doc.getLinkHrefAsText("enterManual") shouldBe routes.AddressLookupController.edit(testJourneyId, Some(testPostCode)).url
          }
        }
      }
    }
  }

}
