/*
 * Copyright 2022 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.HeaderNames
import play.api.libs.json.Json
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.JourneyRepository
import uk.gov.hmrc.http.HeaderCarrier
import views.html.testonly.setup_journey_v2_stub_page

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

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
      val id = StubHelper.getJourneyIDFromURL("/lookup-address/FOOBarWizzID/begin")
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
        options = JourneyOptions(continueUrl = "testContinueUrl")
      )
      val expectedJourneyConfigV2 = JourneyConfigV2(
        version = 2,
        options = JourneyOptions(continueUrl = s"/end-of-journey/$journeyId")
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
  val components = app.injector.instanceOf[MessagesControllerComponents]

  val setup_journey_v2_stub_page = app.injector.instanceOf[setup_journey_v2_stub_page]

  class Setup {
    val controller = new StubController(mockAPIController, mockJourneyRepository, frontendAppConfig, components, setup_journey_v2_stub_page)
    
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
      val cc: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]
      def Action: ActionBuilder[MessagesRequest, AnyContent] = {
        cc.messagesActionBuilder.compose(cc.actionBuilder)
      }

      when(mockAPIController.initWithConfigV2).thenReturn(Action.async(cc.parsers.json[JourneyConfigV2])(
        _ => Future.successful(Results.Ok("foo").withHeaders(HeaderNames.LOCATION -> "/lookup-address/bar/begin"))))

      when(mockJourneyRepository.putV2(Matchers.eq("bar"), Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(true))

      val res: Future[Result] = controller.submitStubForNewJourneyV2()(FakeRequest().withMethod("POST").withFormUrlEncodedBody(
        "journeyConfig" -> basicJourney
      ))

      redirectLocation(res).get mustBe "/lookup-address/bar/begin"
    }

    "return 400 if journeyConfig is not provided" in new Setup {
      val res: Future[Result] = controller.submitStubForNewJourneyV2()(FakeRequest())

      status(res) mustBe BAD_REQUEST
      Jsoup.parse(contentAsString(res)).getElementsByTag("title").first().text() mustBe titleForV2StubPage
    }
  }
}
