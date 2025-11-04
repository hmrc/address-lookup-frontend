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

package controllers.api

import com.codahale.metrics.SharedMetricRegistries
import fixtures.ALFEFixtures
import model.v2.{JourneyConfigV2, JourneyOptions, TimeoutConfig}
import org.apache.pekko.stream.Materializer
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.{defaultAwaitTimeout, status}
import play.api.{Application, Mode}
import services.JourneyRepository

import scala.concurrent.Future
import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.language.postfixOps

abstract class ApiControllerSpecBase extends AnyWordSpec with MockitoSugar with Matchers with GuiceOneAppPerSuite with ALFEFixtures {
  val mockJourneyRepository: JourneyRepository = mock[JourneyRepository]
  when(mockJourneyRepository.putV2(any(), any())(any(), any())).thenReturn(Future.successful(true))

  private val injector = app.injector
  private val controller = injector.instanceOf[ApiController]

  implicit val timeout: FiniteDuration = 1 second
  implicit val mat: Materializer = injector.instanceOf[Materializer]

  protected def init(journeyOptions: JourneyConfigV2) = {
    val headers = Seq("Content-Type" -> "application/json", "User-Agent" -> "test-user-agent")

    val fakeRequest = FakeRequest("POST", s"/api/v2/init/")
      .withBody[JourneyConfigV2](journeyOptions)
      .withHeaders(headers*)

    val result = controller.initWithConfigV2.apply(fakeRequest)
    result
  }
}

class ApiControllerSpec extends ApiControllerSpecBase {
  override implicit lazy val app: Application = {
    SharedMetricRegistries.clear()

    new GuiceApplicationBuilder()
      .in(Mode.Test)
      .overrides(bind[JourneyRepository].toInstance(mockJourneyRepository))
      .build()
  }

  "initialising a new journey" should {

    "Succeed with relative urls in timeout config" in {
      val journeyOptions = JourneyConfigV2(2,
        JourneyOptions(continueUrl = "ignoreme", signOutHref = Some("/signout"), timeoutConfig = Some(TimeoutConfig(
          timeoutAmount = 300, timeoutUrl = "/timeout", timeoutKeepAliveUrl = Some("/keepalive")))))

      val result = init(journeyOptions)
      status(result).shouldBe(Status.ACCEPTED)
    }

    "Return bad request for non-relative signoutUrl" in {
      val journeyOptions = JourneyConfigV2(2, JourneyOptions(continueUrl = "ignoreme",
        signOutHref = Some("http://www.google.com"),
        timeoutConfig = Some(TimeoutConfig(timeoutAmount = 300, timeoutUrl = "/timeout"))))

      val result = init(journeyOptions)
      status(result).shouldBe(Status.BAD_REQUEST)
    }

    "Return bad request for non-relative timeoutUrl" in {
      val journeyOptions = JourneyConfigV2(2, JourneyOptions(continueUrl = "ignoreme",
        timeoutConfig = Some(TimeoutConfig(timeoutAmount = 300, timeoutUrl = "http://www.google.com/"))))

      val result = init(journeyOptions)
      status(result).shouldBe(Status.BAD_REQUEST)
    }

    "Return bad request for non-relative timeoutKeepAliveUrl" in {
      val journeyOptions = JourneyConfigV2(2, JourneyOptions(continueUrl = "ignoreme",
        timeoutConfig = Some(TimeoutConfig(timeoutAmount = 300, timeoutUrl = "/timeout", timeoutKeepAliveUrl = Some("http://www.google.com")))))

      val result = init(journeyOptions)
      status(result).shouldBe(Status.BAD_REQUEST)
    }
  }
}

class ApiControllerDevSpec extends ApiControllerSpecBase {
  override implicit lazy val app: Application = {
    SharedMetricRegistries.clear()

    new GuiceApplicationBuilder()
      .in(Mode.Dev)
      .overrides(bind[JourneyRepository].toInstance(mockJourneyRepository))
      .build()
  }

  "initialising a new journey" should {

    "Succeed with relative urls in timeout config" in {
      val journeyOptions = JourneyConfigV2(2,
        JourneyOptions(continueUrl = "ignoreme", signOutHref = Some("/signout"), timeoutConfig = Some(TimeoutConfig(
          timeoutAmount = 300, timeoutUrl = "/timeout", timeoutKeepAliveUrl = Some("/keepalive")))))

      val result = init(journeyOptions)
      status(result).shouldBe(Status.ACCEPTED)
    }

    "Succeed for non-relative signoutUrl" in {
      val journeyOptions = JourneyConfigV2(2, JourneyOptions(continueUrl = "ignoreme",
        signOutHref = Some("http://www.google.com"),
        timeoutConfig = Some(TimeoutConfig(timeoutAmount = 300, timeoutUrl = "/timeout"))))

      val result = init(journeyOptions)

      status(result).shouldBe(Status.ACCEPTED)
    }

    "Succeed for non-relative timeoutUrl" in {
      val journeyOptions = JourneyConfigV2(2, JourneyOptions(continueUrl = "ignoreme",
        timeoutConfig = Some(TimeoutConfig(timeoutAmount = 300, timeoutUrl = "http://www.google.com/"))))

      val result = init(journeyOptions)
      status(result).shouldBe(Status.ACCEPTED)
    }

    "Succeed for non-relative timeoutKeepAliveUrl" in {
      val journeyOptions = JourneyConfigV2(2, JourneyOptions(continueUrl = "ignoreme",
        timeoutConfig = Some(TimeoutConfig(timeoutAmount = 300, timeoutUrl = "/timeout", timeoutKeepAliveUrl = Some("http://www.google.com")))))

      val result = init(journeyOptions)
      status(result).shouldBe(Status.ACCEPTED)
    }
  }
}
