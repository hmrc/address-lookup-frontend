/*
 * Copyright 2024 HM Revenue & Customs
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
import itutil.IntegrationSpecBase
import play.api.http.HeaderNames
import play.api.http.Status._
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.{Application, Environment, Mode}
import services.IdGenerationService

import java.util.UUID

class StubControllerISpec extends IntegrationSpecBase {

  val testJourneyId = UUID.randomUUID().toString

  object MockIdGenerationService extends IdGenerationService {
    override def uuid: String = testJourneyId
  }

  override implicit lazy val app: Application = {
    SharedMetricRegistries.clear()
    new GuiceApplicationBuilder()
      .in(Environment.simple(mode = Mode.Dev))
      .bindings(bind[IdGenerationService].toInstance(MockIdGenerationService))
      .configure(fakeConfig("play.http.router" -> "testOnlyDoNotUseInAppConf.Routes"))
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
      response.header(HeaderNames.LOCATION).get shouldBe s"http://localhost:9028/lookup-address/$testJourneyId/begin"

    }
  }

}
