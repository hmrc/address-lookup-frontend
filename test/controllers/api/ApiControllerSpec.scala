package controllers.api

import akka.stream.Materializer
import com.codahale.metrics.SharedMetricRegistries
import fixtures.ALFEFixtures
import model.{JourneyConfigV2, JourneyOptions, TimeoutConfig}
import org.scalatest.{Matchers, WordSpec}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers.{defaultAwaitTimeout, status}

import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.language.postfixOps

class ApiControllerSpec extends WordSpec with Matchers with GuiceOneAppPerSuite with ALFEFixtures {
  SharedMetricRegistries.clear()

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
