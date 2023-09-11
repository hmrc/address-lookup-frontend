import itutil.IntegrationSpecBase
import itutil.config.IntegrationTestConstants._
import play.api.http.HeaderNames
import play.api.http.Status._
import play.api.i18n.Lang

import java.util.UUID

class NotFoundISpec extends IntegrationSpecBase {

  "Not Found" when {
    "the welsh content header isn't set and welsh object isn't provided in config" should {
      "render in English" in {
        val testJourneyId = UUID.randomUUID().toString
        val fResponse = buildClientLookupAddress(s"notfound", testJourneyId)
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
        val testJourneyId = UUID.randomUUID().toString
        val fResponse = buildClientLookupAddress(s"notfound", testJourneyId)
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
        val testJourneyId = UUID.randomUUID().toString
        val fResponse = buildClientLookupAddress(s"notfound", testJourneyId)
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
        val testJourneyId = UUID.randomUUID().toString
        val fResponse = buildClientLookupAddress(s"notfound", testJourneyId)
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
