import itutil.IntegrationSpecBase
import itutil.config.IntegrationTestConstants._
import model.MessageConstants
import play.api.http.HeaderNames
import play.api.http.Status._
import play.api.libs.json.Json
import config.FrontendAppConfig.ALFCookieNames

class NotFoundISpec extends IntegrationSpecBase {

  "Not Found" when {
    "the welsh content header isn't set and welsh object isn't provided in config" should {
      "render in English" in {
        val fResponse = buildClientLookupAddress(s"notfound")
          .withHeaders(
            HeaderNames.COOKIE -> sessionCookieWithCSRF,
            "Csrf-Token" -> "nocheck"
          )
          .get()

        val res = await(fResponse)
        res.status shouldBe NOT_FOUND

        val doc = getDocFromResponse(res)
        doc.title shouldBe MessageConstants.EnglishMessageConstants.notFoundErrorTitle
        doc.h1 should have(text(MessageConstants.EnglishMessageConstants.notFoundErrorHeading))
        doc.paras should have(elementWithValue(MessageConstants.EnglishMessageConstants.notFoundErrorBody))
      }
    }
    "the welsh content header is set and welsh object isn't provided in config" should {
      "render in English" in {
        stubKeystore(testJourneyId, testMinimalLevelJourneyConfigV2, OK)
        stubKeystoreSave(testJourneyId, testMinimalLevelJourneyConfigV2, OK)

        val fResponse = buildClientLookupAddress(s"notfound")
          .withHeaders(
            HeaderNames.COOKIE -> sessionCookieWithWelshCookie(useWelsh = false),
            "Csrf-Token" -> "nocheck"
          )
          .get()

        val res = await(fResponse)
        res.status shouldBe NOT_FOUND

        val doc = getDocFromResponse(res)
        doc.title shouldBe MessageConstants.EnglishMessageConstants.notFoundErrorTitle
        doc.h1 should have(text(MessageConstants.EnglishMessageConstants.notFoundErrorHeading))
        doc.paras should have(elementWithValue(MessageConstants.EnglishMessageConstants.notFoundErrorBody))
      }
    }
    "the welsh content header is set and welsh object is provided in config" should {
      "render in English" in {
        val v2Config = Json.toJson(fullDefaultJourneyConfigModelV2WithAllBooleansSet(allBooleanSetAndAppropriateOptions = true, isWelsh = true))
        stubKeystore(testJourneyId, v2Config, OK)
        stubKeystoreSave(testJourneyId, v2Config, OK)

        val fResponse = buildClientLookupAddress(s"notfound")
          .withHeaders(
            HeaderNames.COOKIE -> sessionCookieWithWelshCookie(useWelsh = false),
            "Csrf-Token" -> "nocheck"
          )
          .get()

        val res = await(fResponse)
        res.status shouldBe NOT_FOUND


        val doc = getDocFromResponse(res)
        doc.title shouldBe MessageConstants.EnglishMessageConstants.notFoundErrorTitle
        doc.h1 should have(text(MessageConstants.EnglishMessageConstants.notFoundErrorHeading))
        doc.paras should have(elementWithValue(MessageConstants.EnglishMessageConstants.notFoundErrorBody))
      }
    }
    "the welsh content header is set and welsh object provided in config" should {
      "render in Welsh" in {
        val v2Config = Json.toJson(fullDefaultJourneyConfigModelV2WithAllBooleansSet(allBooleanSetAndAppropriateOptions = true, isWelsh = true))
        stubKeystore(testJourneyId, v2Config, OK)
        stubKeystoreSave(testJourneyId, v2Config, OK)

        val fResponse = buildClientLookupAddress(s"notfound")
          .withHeaders(
            HeaderNames.COOKIE -> sessionCookieWithWelshCookie(useWelsh = true),
            "Csrf-Token" -> "nocheck"
          )
          .get()

        val res = await(fResponse)
        res.status shouldBe NOT_FOUND

        val doc = getDocFromResponse(res)
        doc.title shouldBe MessageConstants.WelshMessageConstants.notFoundErrorTitle
        doc.h1 should have(text(MessageConstants.WelshMessageConstants.notFoundErrorHeading))
        doc.paras should have(elementWithValue(MessageConstants.WelshMessageConstants.notFoundErrorBody))
      }
    }
  }
}
