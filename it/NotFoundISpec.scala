import itutil.IntegrationSpecBase
import itutil.config.IntegrationTestConstants._
import play.api.http.HeaderNames
import play.api.http.Status._
import play.api.i18n.Lang
import play.api.libs.json.Json

class NotFoundISpec extends IntegrationSpecBase {

  "Not Found" when {
    "the welsh content header isn't set and welsh object isn't provided in config" should {
      "render in English" in {
        val fResponse = buildClientLookupAddress(s"notfound")
          .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()

        val res = await(fResponse)
        res.status shouldBe NOT_FOUND

        val doc = getDocFromResponse(res)
        doc.title shouldBe messages("constants.notFoundErrorTitle")
        doc.h1 should have(text(messages("constants.notFoundErrorHeading")))
        doc.paras should have(elementWithValue(messages("constants.notFoundErrorBody")))
      }
    }

    "the welsh content header is set and welsh object isn't provided in config" should {
      "render in English" in {
        stubKeystore(testJourneyId, testMinimalLevelJourneyConfigV2, OK)
        stubKeystoreSave(testJourneyId, testMinimalLevelJourneyConfigV2, OK)

        val fResponse = buildClientLookupAddress(s"notfound")
          .withHttpHeaders(
            HeaderNames.COOKIE -> sessionCookieWithWelshCookie(useWelsh = false),
            "Csrf-Token" -> "nocheck"
          )
          .get()

        val res = await(fResponse)
        res.status shouldBe NOT_FOUND

        val doc = getDocFromResponse(res)
        doc.title shouldBe messages("constants.notFoundErrorTitle")
        doc.h1 should have(text(messages("constants.notFoundErrorHeading")))
        doc.paras should have(elementWithValue(messages("constants.notFoundErrorBody")))
      }
    }
    "the welsh content header is set and welsh object is provided in config" should {
      "render in English" in {
        val v2Config = Json.toJson(fullDefaultJourneyConfigModelV2WithAllBooleansSet(allBooleanSetAndAppropriateOptions = true, isWelsh = true))
        stubKeystore(testJourneyId, v2Config, OK)
        stubKeystoreSave(testJourneyId, v2Config, OK)

        val fResponse = buildClientLookupAddress(s"notfound")
          .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithWelshCookie(useWelsh = false), "Csrf-Token" -> "nocheck")
          .get()

        val res = await(fResponse)
        res.status shouldBe NOT_FOUND


        val doc = getDocFromResponse(res)
        doc.title shouldBe messages("constants.notFoundErrorTitle")
        doc.h1 should have(text(messages("constants.notFoundErrorHeading")))
        doc.paras should have(elementWithValue(messages("constants.notFoundErrorBody")))
      }
    }

    "the welsh content header is set and welsh object provided in config" should {
      "render in Welsh" in {
        val v2Config = Json.toJson(fullDefaultJourneyConfigModelV2WithAllBooleansSet(allBooleanSetAndAppropriateOptions = true, isWelsh = true))
        stubKeystore(testJourneyId, v2Config, OK)
        stubKeystoreSave(testJourneyId, v2Config, OK)

        val fResponse = buildClientLookupAddress(s"notfound")
          .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRFAndLang(), "Csrf-Token" -> "nocheck")
          .get()

        val res = await(fResponse)
        res.status shouldBe NOT_FOUND

        val doc = getDocFromResponse(res)
        doc.title shouldBe messages(Lang("cy"), "constants.notFoundErrorTitle")
        doc.h1 should have(text(messages(Lang("cy"), "constants.notFoundErrorHeading")))
        doc.paras should have(elementWithValue(messages(Lang("cy"), "constants.notFoundErrorBody")))
      }
    }
  }
}
