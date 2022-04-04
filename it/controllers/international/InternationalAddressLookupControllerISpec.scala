package controllers.international

import address.v2.Country
import controllers.api.{ConfirmedResponseAddress, ConfirmedResponseAddressDetails}
import itutil.IntegrationSpecBase
import itutil.config.IntegrationTestConstants._
import itutil.config.PageElementConstants._
import play.api.http.HeaderNames
import play.api.http.Status._
import play.api.libs.json.{JsObject, Json}

class InternationalAddressLookupControllerISpec extends IntegrationSpecBase {

  "The lookup page" should {
    "pre-pop the filter only on the view when it is passed in as a query parameters" in {
      stubKeystore(testJourneyId, testMinimalLevelJourneyConfigV2, OK)
      stubKeystoreSave(testJourneyId, testMinimalLevelJourneyConfigV2, OK)

      val fResponse = buildClientLookupAddress(path = "international/lookup?filter=bar")
        .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
        .get()
      val res = await(fResponse)

      res.status shouldBe OK
      testFormElementValuesMatch(res, Map(LookupPage.filterId -> testFilterValue))
    }

    "not pre-pop the filter or postcode fields when no query parameters are used " in {
      stubKeystore(testJourneyId, testMinimalLevelJourneyConfigV2, OK)
      stubKeystoreSave(testJourneyId, testMinimalLevelJourneyConfigV2, OK)

      val fResponse = buildClientLookupAddress(path = "international/lookup")
        .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
        .get()
      val res = await(fResponse)

      res.status shouldBe OK
      testFormElementValuesMatch(res, Map(LookupPage.filterId -> ""))
    }
  }
}
