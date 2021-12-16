/*
 * Copyright 2021 HM Revenue & Customs
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

package controllers.api

import akka.stream.Materializer
import com.codahale.metrics.SharedMetricRegistries
import fixtures.ALFEFixtures
import model.{JourneyConfigV2, JourneyOptions, TimeoutConfig}
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.{Matchers, WordSpec}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.http.Status
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.{defaultAwaitTimeout, status}
import services.JourneyRepository
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import scala.concurrent.Future
import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.language.postfixOps

class ApiControllerSpec extends WordSpec with MockitoSugar  with Matchers with GuiceOneAppPerSuite with ALFEFixtures {
  val mockJourneyRepository: JourneyRepository = mock[JourneyRepository]
  when(mockJourneyRepository.putV2(any(), any())(any(), any())).thenReturn(Future.successful(true))

  override implicit lazy val app: Application = {
    SharedMetricRegistries.clear()

    new GuiceApplicationBuilder()
      .overrides(bind[JourneyRepository].toInstance(mockJourneyRepository))
      .build()
  }

  private val injector = app.injector
  private val controller = injector.instanceOf[ApiController]

  implicit val timeout: FiniteDuration = 1 second
  implicit val mat: Materializer = injector.instanceOf[Materializer]

  "initialising a new journey" should {

    "Succeed with relative urls in timeout config" in {
      val journeyOptions = JourneyConfigV2(2,
        JourneyOptions(continueUrl = "ignoreme", timeoutConfig = Some(TimeoutConfig(
          timeoutAmount = 300, timeoutUrl = "/timeout", timeoutKeepAliveUrl = Some("/keepalive")))))

      val result = init(journeyOptions)
      status(result) shouldBe Status.ACCEPTED
    }

    "Fail for non-relative timeoutUrl" in {
      val journeyOptions = JourneyConfigV2(2, JourneyOptions(continueUrl = "ignoreme",
        timeoutConfig = Some(TimeoutConfig(timeoutAmount = 300, timeoutUrl = "http://www.google.com/"))))

      val result = init(journeyOptions)
      status(result) shouldBe Status.BAD_REQUEST
    }

    "Return bad request for non-relative timeoutKeepAliveUrl" in {
      val journeyOptions = JourneyConfigV2(2, JourneyOptions(continueUrl = "ignoreme",
        timeoutConfig = Some(TimeoutConfig(timeoutAmount = 300, timeoutUrl = "/timeout", timeoutKeepAliveUrl = Some("http://www.google.com")))))

      val result = init(journeyOptions)
      status(result) shouldBe Status.BAD_REQUEST
    }
  }

  private def init(journeyOptions: JourneyConfigV2) = {
    val headers = Seq("Content-Type" -> "application/json", "User-Agent" -> "test-user-agent")

    val fakeRequest = FakeRequest("POST", s"/api/v2/init/")
      .withBody[JourneyConfigV2](journeyOptions)
      .withHeaders(headers: _*)

    val result = controller.initWithConfigV2().apply(fakeRequest)
    result
  }
}
