package controllers.international

import itutil.IntegrationSpecBase
import itutil.config.IntegrationTestConstants._
import itutil.config.PageElementConstants._
import play.api.http.HeaderNames
import play.api.http.Status._
import services.JourneyDataV2Cache
import uk.gov.hmrc.http.HeaderCarrier

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global

class InternationalAddressLookupControllerISpec extends IntegrationSpecBase {
  val cache = app.injector.instanceOf[JourneyDataV2Cache]
  implicit val hc: HeaderCarrier = HeaderCarrier()

  "The lookup page" should {
    "pre-pop the filter only on the view when it is passed in as a query parameters" in {
      val testJourneyId = UUID.randomUUID().toString
      await(cache.putV2(testJourneyId, testMinimalLevelJourneyDataV2))

      val fResponse = buildClientLookupAddress(path = "international/lookup?filter=bar", testJourneyId)
        .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
        .get()
      val res = await(fResponse)

      res.status shouldBe OK
      testFormElementValuesMatch(res, Map(LookupPage.filterId -> testFilterValue))
    }

    "not pre-pop the filter or postcode fields when no query parameters are used " in {
      val testJourneyId = UUID.randomUUID().toString
      await(cache.putV2(testJourneyId, testMinimalLevelJourneyDataV2))

      val fResponse = buildClientLookupAddress(path = "international/lookup", testJourneyId)
        .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
        .get()
      val res = await(fResponse)

      res.status shouldBe OK
      testFormElementValuesMatch(res, Map(LookupPage.filterId -> ""))
    }
  }
}
