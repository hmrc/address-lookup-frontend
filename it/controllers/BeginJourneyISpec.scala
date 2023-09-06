package controllers

import itutil.IntegrationSpecBase
import itutil.config.IntegrationTestConstants.{journeyDataV2MinimalUkMode, testJourneyDataWithMinimalJourneyConfigV2, testJourneyId}
import play.api.http.HeaderNames
import play.api.http.Status.SEE_OTHER
import services.JourneyDataV2Cache
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global

class BeginJourneyISpec extends IntegrationSpecBase {
  val cache = app.injector.instanceOf[JourneyDataV2Cache]
  implicit val hc: HeaderCarrier = HeaderCarrier()

  "The begin journey endpoint" when {
    "in uk mode" should {
      "redirect to the lookup page" in {
        cache.putV2(testJourneyId, journeyDataV2MinimalUkMode)

        val fResponse = buildClientLookupAddress(path = s"begin")
          .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()

        val res = await(fResponse)
        res.status shouldBe SEE_OTHER

        res.header(HeaderNames.LOCATION).get shouldBe "/lookup-address/Jid123/lookup"
      }
    }

    "in non-uk mode" should {
      "redirect to the country picker page" in {
        cache.putV2(testJourneyId, testJourneyDataWithMinimalJourneyConfigV2)

        val fResponse = buildClientLookupAddress(path = s"begin")
          .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()

        val res = await(fResponse)
        res.status shouldBe SEE_OTHER

        res.header(HeaderNames.LOCATION).get shouldBe "/lookup-address/Jid123/country-picker"
      }
    }
  }
}
