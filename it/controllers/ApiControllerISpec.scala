package controllers

import controllers.api.ApiController
import itutil.IntegrationSpecBase
import itutil.config.IntegrationTestConstants._
import model.JourneyData._
import model._
import play.api.http.HeaderNames
import play.api.http.Status._
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.{Application, Environment, Mode}
import services.IdGenerationService
import utils.V2ModelConverter._

class ApiControllerISpec extends IntegrationSpecBase {

  object MockIdGenerationService extends IdGenerationService {
    override def uuid: String = testJourneyId
  }

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .in(Environment.simple(mode = Mode.Dev))
    .bindings(bind[IdGenerationService].toInstance(MockIdGenerationService))
    .configure(fakeConfig())
    .build

  lazy val addressLookupEndpoint = app.injector.instanceOf[ApiController].addressLookupEndpoint

  "/api/init" should {
    "convert a v1 model into v2 and store in keystore" in {
      val v1Model = JourneyData(
        config = JourneyConfig(
          continueUrl = testContinueUrl
        )
      )

      val v2Model = v1Model.toV2Model

      stubKeystoreSave(testJourneyId, Json.toJson(v2Model), OK)

      val res = buildClientAPI(path = "init")
        .withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
        .post(Json.toJson(v1Model.config))

      res.status shouldBe ACCEPTED
      res.header(HeaderNames.LOCATION) should contain(s"$addressLookupEndpoint/lookup-address/$testJourneyId/lookup")
    }
  }

  "/api/v2/confirmed" when {
    "provided with a valid journey ID" should {
      "return OK with a confirmed address" in {
        val v2Model = testJourneyDataWithMinimalJourneyConfigV2.copy(confirmedAddress = Some(testConfirmedAddress))

        stubKeystore(testJourneyId, Json.toJson(v2Model), OK)

        val res = await(buildClientAPI(s"confirmed?id=$testJourneyId")
          .withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get())

        res.status shouldBe OK
        Json.parse(res.body) shouldBe Json.toJson(testConfirmedAddress)
      }
    }
  }

}
