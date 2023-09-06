package controllers

import com.codahale.metrics.SharedMetricRegistries
import itutil.IntegrationSpecBase
import itutil.config.IntegrationTestConstants.testJourneyId
import model.{JourneyConfigV2, JourneyDataV2, JourneyOptions}
import play.api.http.HeaderNames
import play.api.http.Status._
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.{Application, Environment, Mode}
import services.IdGenerationService

class  StubControllerISpec extends IntegrationSpecBase {

  object MockIdGenerationService extends IdGenerationService {
    override def uuid: String = testJourneyId
  }

  override implicit lazy val app: Application = {
    SharedMetricRegistries.clear()
    new GuiceApplicationBuilder()
      .in(Environment.simple(mode = Mode.Dev))
      .bindings(bind[IdGenerationService].toInstance(MockIdGenerationService))
      .configure(fakeConfig("application.router" -> "testOnlyDoNotUseInAppConf.Routes"))
      .build
  }

  s"${controllers.testonly.routes.StubController.showStubPageForJourneyInitV2.url}" should {
    "return 200" in {
      val res = buildClientTestOnlyRoutes(path = "v2/test-setup")
        .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
        .get()
      await(res).status shouldBe OK
    }
  }

  s"${controllers.testonly.routes.StubController.submitStubForNewJourneyV2.url}" should {
    "return 303 and redirect to the begin endpoint" in {
      val basicJourney =
        """{
          |  "version": 2,
          |     "options":{
          |         "continueUrl":"testContinueUrl"
          |     }
          |
          |}""".stripMargin

      val res = buildClientTestOnlyRoutes(path = "v2/test-setup")
        .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
        .post(Map(
          "journeyConfig" -> Seq(basicJourney)))

      val response = await(res)
      response.status shouldBe SEE_OTHER
      response.header(HeaderNames.LOCATION).get shouldBe "http://localhost:9028/lookup-address/Jid123/begin"

    }
  }

}
