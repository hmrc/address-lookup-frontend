package controllers.international

import controllers.routes
import itutil.IntegrationSpecBase
import itutil.config.IntegrationTestConstants._
import itutil.config.PageElementConstants.LookupPage
import model._
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.http.Status.OK
import play.api.libs.json.Json
import services.JourneyDataV2Cache
import uk.gov.hmrc.http.HeaderCarrier

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global

class NoResultsFoundPageISpec extends IntegrationSpecBase {
  val cache = app.injector.instanceOf[JourneyDataV2Cache]
  implicit val hc: HeaderCarrier = HeaderCarrier()

  object EnglishContent {
    def title(postcode: String) = s"We cannot find any addresses for $postcode"

    def heading(postcode: String) = s"We cannot find any addresses for $postcode"

    val manualEntry = "Enter the address manually"
    val submitButton = "Try a different postcode"
  }

  object WelshContent {
    def title(postcode: String) = s"Ni allwn ddod o hyd i unrhyw gyfeiriadau ar gyfer $postcode"

    def heading(postcode: String) = s"Ni allwn ddod o hyd i unrhyw gyfeiriadau ar gyfer $postcode"

    val manualEntry = "Nodwch y cyfeiriad â llaw"
    val submitButton = "Rhowch gynnig ar god post gwahanol"
  }

  "No results page GET" should {
    "with the default config" should {
      "Render the 'No results' page" in {
        val testJourneyId = UUID.randomUUID().toString
        await(cache.putV2(testJourneyId, testMinimalLevelJourneyDataV2.copy(countryCode = Some("BM"))))
        stubGetAddressByCountry(addressJson = Json.toJson(Json.arr()), countryCode = "BM")

        val fResponse = buildClientLookupAddress(
          path = s"international/select?${LookupPage.filterId}=$testFilterValue", testJourneyId)
          .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()

        val res = await(fResponse)
        res.status shouldBe OK

        testCustomPartsOfGovWrapperElementsForDefaultConfig(fResponse)

        val doc = getDocFromResponse(res)
        doc.title shouldBe EnglishContent.title(testFilterValue)
        doc.h1.text() shouldBe EnglishContent.heading(testFilterValue)
        doc.select("a[class=govuk-back-link]") should have(
          text("Back")
        )

        doc.link("enterManual") should have(
          href(routes.InternationalAddressLookupController.edit(testJourneyId).url),
          text(EnglishContent.manualEntry)
        )

        doc.submitButton.text() shouldBe EnglishContent.submitButton
      }
    }

    "With full journey config model with all booleans set to true" should {
      "Render the page with expected custom English elements" in {
        val testJourneyId = UUID.randomUUID().toString
        await(cache.putV2(testJourneyId, journeyDataV2WithSelectedAddress(
          testJourneyId,
          journeyConfigV2 = fullDefaultJourneyConfigModelV2WithAllBooleansSet(), countryCode = Some("BM"))))

        stubGetAddressByCountry(addressJson = Json.toJson(Json.arr()), countryCode = "BM")

        val fResponse = buildClientLookupAddress(path = s"international/select?${LookupPage.filterId}=$testFilterValue", testJourneyId)
          .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()

        val res = await(fResponse)
        res.status shouldBe OK

        testCustomPartsOfGovWrapperElementsForFullConfigAllTrue(fResponse, "NAV_TITLE")

        val doc = getDocFromResponse(res)
        doc.title shouldBe EnglishContent.title(testFilterValue) + " - NAV_TITLE - GOV.UK"
        doc.h1.text() shouldBe EnglishContent.heading(testFilterValue)
        doc.select("a[class=govuk-back-link]") should have(
          text("Back")
        )

        doc.link("enterManual") should have(
          href(routes.InternationalAddressLookupController.edit(testJourneyId).url),
          text(EnglishContent.manualEntry)
        )

        doc.submitButton.text() shouldBe EnglishContent.submitButton
      }
    }

    "With full journey config model with top level config set to None all booleans set to true" should {
      "Render the page with expected custom English elements" in {
        val testJourneyId = UUID.randomUUID().toString
        await(cache.putV2(testJourneyId, journeyDataV2WithSelectedAddress(
          testJourneyId,
          fullDefaultJourneyConfigModelV2WithAllBooleansSet(false), countryCode = Some("BM"))))
        stubGetAddressByCountry(addressJson = Json.toJson(Json.arr()), countryCode = "BM")

        val fResponse = buildClientLookupAddress(path = s"international/select?${LookupPage.filterId}=$testFilterValue", testJourneyId)
          .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()

        val res = await(fResponse)
        res.status shouldBe OK

        testCustomPartsOfGovWrapperElementsForFullConfigWithAllTopConfigAsNoneAndAllBooleansFalse(fResponse)

        val doc = getDocFromResponse(res)
        doc.title shouldBe EnglishContent.title(testFilterValue)
        doc.h1.text() shouldBe EnglishContent.heading(testFilterValue)
        doc.select("a[class=govuk-back-link]") should have(
          text("Back")
        )

        doc.link("enterManual") should have(
          href(routes.InternationalAddressLookupController.edit(testJourneyId).url),
          text(EnglishContent.manualEntry)
        )

        doc.submitButton.text() shouldBe EnglishContent.submitButton
      }
    }

    "With full journey config model with top level config set to None all booleans set to true" should {
      "Render the page with expected custom Welsh elements" in {
        val testJourneyId = UUID.randomUUID().toString
        await(cache.putV2(testJourneyId, journeyDataV2WithSelectedAddress(
          testJourneyId,
          fullDefaultJourneyConfigModelV2WithAllBooleansSet(false, isWelsh = true), countryCode = Some("BM"))))

        stubGetAddressByCountry(addressJson = Json.toJson(Json.arr()), countryCode = "BM")

        val fResponse = buildClientLookupAddress(path = s"international/select?${LookupPage.filterId}=$testFilterValue", testJourneyId)
          .withHttpHeaders(HeaderNames.COOKIE -> (sessionCookieWithCSRF + ";PLAY_LANG=cy;"), "Csrf-Token" -> "nocheck")
          .get()

        val res = await(fResponse)
        res.status shouldBe OK

        testCustomPartsOfGovWrapperElementsForFullConfigWithAllTopConfigAsNoneAndAllBooleansFalse(fResponse)

        val doc = getDocFromResponse(res)
        doc.title shouldBe WelshContent.title(testFilterValue)
        doc.h1.text() shouldBe WelshContent.heading(testFilterValue)
        doc.select("a[class=govuk-back-link]") should have(
          text("Yn ôl")
        )

        doc.link("enterManual") should have(
          href(routes.InternationalAddressLookupController.edit(testJourneyId).url),
          text(WelshContent.manualEntry)
        )

        doc.submitButton.text() shouldBe WelshContent.submitButton
      }
    }

    "With the back button disabled in config" should {
      "Render the 'No results' page without a back button" in {
        val testJourneyId = UUID.randomUUID().toString
        val testJson = journeyDataV2Minimal.copy(
          JourneyConfigV2(
            version = 2,
            options = JourneyOptions(continueUrl = testContinueUrl, showBackButtons = Some(false)),
            labels = Some(JourneyLabels(
              en = Some(LanguageLabels()),
              cy = Some(LanguageLabels())
            ))
          ),
          countryCode = Some("BM")
        )

        await(cache.putV2(testJourneyId, testJson))
        stubGetAddressByCountry(addressJson = Json.toJson(Json.arr()), countryCode = "BM")

        val fResponse = buildClientLookupAddress(
          path = s"international/select?${LookupPage.filterId}=$testFilterValue", testJourneyId)
          .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()

        val res = await(fResponse)
        res.status shouldBe OK

        val doc = getDocFromResponse(res)
        doc.title shouldBe EnglishContent.title(testFilterValue)
        doc.h1.text() shouldBe EnglishContent.heading(testFilterValue)
        doc.select("a[class=govuk-back-link]") should not have (
          text("Back")
          )

        doc.link("enterManual") should have(
          href(routes.InternationalAddressLookupController.edit(testJourneyId).url),
          text(EnglishContent.manualEntry)
        )

        doc.submitButton.text() shouldBe EnglishContent.submitButton
      }
    }

    "when provided with a pageHeadingStyle option" should {
      "allow the initialising service to override the header size" in {
        val testJourneyId = UUID.randomUUID().toString
        val testJson = journeyDataV2Minimal.copy(
          JourneyConfigV2(
            version = 2,
            options = JourneyOptions(continueUrl = testContinueUrl, pageHeadingStyle = Some("govuk-heading-l")),
            labels = Some(JourneyLabels(
              en = Some(LanguageLabels()),
              cy = Some(LanguageLabels())
            ))
          ),
          countryCode = Some("BM")
        )

        await(cache.putV2(testJourneyId, testJson))
        stubGetAddressByCountry(addressJson = Json.toJson(Json.arr()), countryCode = "BM")

        val fResponse = buildClientLookupAddress(
          path = s"international/select?${LookupPage.filterId}=$testFilterValue", testJourneyId)
          .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()

        val res = await(fResponse)
        res.status shouldBe OK

        val document = Jsoup.parse(res.body)
        document.getElementById("pageHeading").classNames() should contain("govuk-heading-l")
      }
    }
  }
}
