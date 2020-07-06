package controllers

import com.codahale.metrics.SharedMetricRegistries
import com.gu.scalatest.JsoupShouldMatchers
import config.FrontendAppConfig
import controllers.api.ApiController
import controllers.testonly.{StubController, StubHelper}
import fixtures.ALFEFixtures
import model._
import org.jsoup.Jsoup
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.http.HeaderNames
import play.api.i18n.MessagesApi
import play.api.libs.json.Json
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.JourneyRepository
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class StubControllerSpec extends PlaySpec
  with GuiceOneAppPerSuite
  with JsoupShouldMatchers
  with ScalaFutures with ALFEFixtures with MockitoSugar {

  SharedMetricRegistries.clear()

  implicit val hc = HeaderCarrier()
  implicit lazy val materializer = app.materializer
  val titleForV2StubPage = "Stub For starting new journey in ALF with V2 config"

  "StubHelper getJourneyIDFromURL" should {
    "return id if url matches regex" in {
      val id = StubHelper.getJourneyIDFromURL("/lookup-address/FOOBarWizzID/lookup")
      id mustBe "FOOBarWizzID"
    }
    "return exception if url does not match regex" in {
      intercept[Exception](StubHelper.getJourneyIDFromURL("foo"))
    }
  }

  "StubHelper changeContinueUrlFromUserInputToStubV2" should {
    "return journeyConfigV2 with an amended continue url of showResultOfJourneyV2 route" in {
      val journeyId: String = "11345fgh"
      val journeyConfigV2 = JourneyConfigV2(
        version = 2,
        options = JourneyOptions(continueUrl = "testContinueUrl",
          feedbackUrl = Some("PLACEHOLDER"),
          contactFormServiceIdentifier = Some("PLACEHOLDER"))
      )
      val expectedJourneyConfigV2 = JourneyConfigV2(
        version = 2,
        options = JourneyOptions(continueUrl = s"/end-of-journey/$journeyId",
          feedbackUrl = Some("PLACEHOLDER"),
          contactFormServiceIdentifier = Some("PLACEHOLDER"))
      )

      val res = StubHelper.changeContinueUrlFromUserInputToStubV2(journeyConfigV2, journeyId)
      res mustBe expectedJourneyConfigV2
    }
  }

  "StubHelper defaultJourneyConfigV2JsonAsString" should {
    "return default journeyConfig v2 string and is a valid JourneyConfigV2 model" in {
      val res = StubHelper.defaultJourneyConfigV2JsonAsString
      res mustBe Json.parse(
        """{
           "version": 2,
           "options":{
                "continueUrl": "This will be ignored"
                },
           "labels" : {}
      }""".stripMargin)
      res.as[JourneyConfigV2]
    }
  }

  val mockJourneyRepository = mock[JourneyRepository]
  val mockAPIController = mock[ApiController]

  val frontendAppConfig = app.injector.instanceOf[FrontendAppConfig]

  class Setup {
    val controller = new StubController(
      mockAPIController, mockJourneyRepository, frontendAppConfig
    )(app.injector.instanceOf[ExecutionContext], app.injector.instanceOf[MessagesApi])
    reset(mockAPIController)
    reset(mockJourneyRepository)
  }

  "showStubPageForJourneyInitV2" should {
    "return 200" in new Setup {
      val res = controller.showStubPageForJourneyInitV2()(FakeRequest())
      status(res) mustBe OK
      val doc = Jsoup.parse(contentAsString(res))
      doc.getElementsByTag("title").first().text() mustBe titleForV2StubPage

      Json.parse(doc.getElementById("journeyConfig").text()) mustBe Json.parse(
        """{
           "version": 2,
           "options":{
              "continueUrl": "This will be ignored"
              },
            "labels" : {}
        }""".stripMargin)

      doc.getElementsByTag("form").first().attr("action") mustBe "/v2/test-setup"
    }
  }

  "submitStubForNewJourneyV2" should {
    val basicJourney =
      """{
        |  "version": 2,
        |     "options":{
        |         "continueUrl":"testContinueUrl"
        |     }
        |
        |}""".stripMargin

    "return 303 to location returned from LOCATION header in initv2 on APIController" in new Setup {
      val jcv2 = JourneyConfigV2(
        version = 2,
        options = JourneyOptions(
          continueUrl = "/end-of-journey/bar",
          feedbackUrl = Some("PLACEHOLDER"),
          contactFormServiceIdentifier = Some("PLACEHOLDER")
        )
      )
      val matchedRequestContainingJourneyConfig = FakeRequest().map(_ => jcv2)

      when(mockAPIController.initWithConfigV2).thenReturn(Action.async(BodyParsers.parse.json[JourneyConfigV2])(
        _ => Future.successful(Results.Ok("foo").withHeaders(HeaderNames.LOCATION -> "/lookup-address/bar/lookup"))))

      when(mockJourneyRepository.putV2(Matchers.eq("bar"), Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(true))

      val res: Future[Result] = controller.submitStubForNewJourneyV2()(FakeRequest().withFormUrlEncodedBody(
        "journeyConfig" -> basicJourney
      ))

      redirectLocation(res).get mustBe "/lookup-address/bar/lookup"
    }
    "return 400 if journeyConfig is not provided" in new Setup {
      val res = controller.submitStubForNewJourneyV2()(FakeRequest())

      status(res) mustBe BAD_REQUEST
      Jsoup.parse(contentAsString(res)).getElementsByTag("title").first().text() mustBe titleForV2StubPage
    }
  }
}