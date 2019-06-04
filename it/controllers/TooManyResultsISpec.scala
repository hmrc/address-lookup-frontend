package controllers

import itutil.config.AddressRecordConstants._
import itutil.config.IntegrationTestConstants._
import itutil.{IntegrationSpecBase, PageContentHelper}
import model._
import play.api.http.HeaderNames
import play.api.http.Status._
import play.api.libs.json.{JsArray, JsObject, Json}
import play.api.test.FakeApplication
import uk.gov.hmrc.address.v2.Country

class TooManyResultsISpec extends IntegrationSpecBase with PageContentHelper {

  override implicit lazy val app = FakeApplication(additionalConfiguration = fakeConfig())

  object tooManyResultsMessages {
    val title = "No results found"
    val heading1 = "There are too many results"
    val heading2 = "We couldn't find any results for that property name or number"

    def bullet1(postcode: String) = s"$postcode for postcode"

    val bullet2NoFilter = "nothing for property name or number"

    def bullet2WithFilter(filter: String) = s"'$filter' for name or number"

    val line1 = "You entered:"
    val button = "Try a new search"
  }

  object otherPageMessages {
    val noResultsPageTitle = "Can't find any addresses"

  }

  "The 'Too Many Results' page" should {
    "be rendered" when {
      "the back buttons are enabled in the journey config" when {
        "no filter has been entered" when {
          "the backend service returns too many addresses" in {
            stubKeystore(testJourneyId, testConfigDefaultWithResultsLimitAsJson, OK)
            stubGetAddressFromBE(addressJson = addressRecordJsonList(numberOfRepeats = 51))

            val res = buildClientLookupAddress(path = "select?postcode=AB11+1AB&filter=")
              .withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
              .get()

            await(res).status shouldBe OK

            val doc = getDocFromResponse(res)

            doc.select("a[class=back-link]") should have(
              text("Back")
            )
            doc.title shouldBe tooManyResultsMessages.title
            doc.h1.text shouldBe tooManyResultsMessages.heading1
            doc.select("t").text shouldBe tooManyResultsMessages.line1
            doc.bulletPointList.select("li").first.text shouldBe tooManyResultsMessages.bullet1(testPostCode)
            doc.bulletPointList.select("li").last.text shouldBe tooManyResultsMessages.bullet2NoFilter
            doc.submitButton.text shouldBe tooManyResultsMessages.button
            doc.link("enterManual").text shouldBe JourneyConfigDefaults.EDIT_LINK_TEXT
          }
        }

        "a filter has been entered" when {
          "the backend returns too many addresses" in {
            stubKeystore(testJourneyId, testConfigDefaultWithResultsLimitAsJson, OK)
            stubKeystoreSave(testJourneyId, testConfigWithoutAddressAsJson, OK)
            stubGetAddressFromBEWithFilter(addressJson = Json.arr())
            stubGetAddressFromBE(addressJson = addressRecordJsonList(51))

            val res = buildClientLookupAddress(path = s"select?postcode=AB11+1AB&filter=$testFilterValue")
              .withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
              .get()

            await(res).status shouldBe OK

            val doc = getDocFromResponse(res)

            doc.select("a[class=back-link]") should have(
              text("Back")
            )
            doc.title shouldBe tooManyResultsMessages.title
            doc.h1.text shouldBe tooManyResultsMessages.heading2
            doc.select("t").text shouldBe tooManyResultsMessages.line1
            doc.bulletPointList.select("li").first.text shouldBe tooManyResultsMessages.bullet1(testPostCode)
            doc.bulletPointList.select("li").last.text shouldBe tooManyResultsMessages.bullet2WithFilter(testFilterValue)
            doc.submitButton.text shouldBe tooManyResultsMessages.button
            doc.link("enterManual").text shouldBe JourneyConfigDefaults.EDIT_LINK_TEXT
          }
        }
      }

      "the back buttons are not enabled in the journey config" when {
        "no filter has been entered" when {
          "the backend service returns too many addresses " in {
            stubKeystore(testJourneyId, testSelectConfigNoBackButtons, OK)
            stubGetAddressFromBE(addressJson = addressRecordJsonList(numberOfRepeats = 51))

            val res = buildClientLookupAddress(path = "select?postcode=AB11+1AB&filter=")
              .withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
              .get()

            await(res).status shouldBe OK

            val doc = getDocFromResponse(res)

            doc.select("a[class=back-link]") shouldNot have(
              text("Back")
            )
            doc.title shouldBe tooManyResultsMessages.title
            doc.h1.text shouldBe tooManyResultsMessages.heading1
            doc.select("t").text shouldBe tooManyResultsMessages.line1
            doc.bulletPointList.select("li").first.text shouldBe tooManyResultsMessages.bullet1(testPostCode)
            doc.bulletPointList.select("li").last.text shouldBe tooManyResultsMessages.bullet2NoFilter
            doc.submitButton.text shouldBe tooManyResultsMessages.button
            doc.link("enterManual").text shouldBe JourneyConfigDefaults.EDIT_LINK_TEXT
          }
        }

        "a filter has been entered" when {
          "the backend returns too many addresses" in {
            stubKeystore(testJourneyId, testSelectConfigNoBackButtons, OK)
            stubKeystoreSave(testJourneyId, testConfigWithoutAddressAsJson, OK)
            stubGetAddressFromBEWithFilter(addressJson = Json.arr())
            stubGetAddressFromBE(addressJson = addressRecordJsonList(51))

            val res = buildClientLookupAddress(path = s"select?postcode=AB11+1AB&filter=$testFilterValue")
              .withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
              .get()

            await(res).status shouldBe OK

            val doc = getDocFromResponse(res)

            doc.select("a[class=back-link]") shouldNot have(
              text("Back")
            )
            doc.title shouldBe tooManyResultsMessages.title
            doc.h1.text shouldBe tooManyResultsMessages.heading2
            doc.select("t").text shouldBe tooManyResultsMessages.line1
            doc.bulletPointList.select("li").first.text shouldBe tooManyResultsMessages.bullet1(testPostCode)
            doc.bulletPointList.select("li").last.text shouldBe tooManyResultsMessages.bullet2WithFilter(testFilterValue)
            doc.submitButton.text shouldBe tooManyResultsMessages.button
            doc.link("enterManual").text shouldBe JourneyConfigDefaults.EDIT_LINK_TEXT
          }
        }
      }
    }

    "not be rendered" when {
      "the backend service returns enough addresses to be displayed on the select page" in {
        stubKeystore(testJourneyId, testConfigDefaultWithResultsLimitAsJson, OK)
        stubKeystoreSave(testJourneyId, testConfigWithoutAddressAsJson, OK)
        stubGetAddressFromBE(addressJson = addressRecordJsonList(numberOfRepeats = 25))

        val res = buildClientLookupAddress(path = "select?postcode=AB111AB&filter=")
          .withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()

        await(res).status shouldBe OK

        val doc = getDocFromResponse(res)

        doc.title shouldBe JourneyConfigDefaults.SELECT_PAGE_TITLE
      }

      "the backend service returns 1 address and redirects to the confirm page" in {
        stubKeystore(testJourneyId, testConfigDefaultWithResultsLimitAsJson, OK)
        stubGetAddressFromBE(addressJson = addressRecordJsonList(1))
        stubKeystoreSave(testJourneyId, Json.toJson(testConfigWithUKAddress).as[JsObject], OK)

        val res = buildClientLookupAddress(path = "select?postcode=AB111AB&filter=")
          .withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()

        val completedResponse = await(res)

        completedResponse.status shouldBe SEE_OTHER
        completedResponse.header(HeaderNames.LOCATION).get shouldBe s"/lookup-address/$testJourneyId/confirm"

      }

      "the backend service returns no addresses and renders the no results found page" in {
        stubKeystore(testJourneyId, testConfigDefaultWithResultsLimitAsJson, OK)
        stubKeystoreSave(testJourneyId, testConfigWithoutAddressAsJson, OK)
        stubGetAddressFromBE(addressJson = Json.arr())

        val res = buildClientLookupAddress(path = "select?postcode=AB111AB&filter=")
          .withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()

        await(res).status shouldBe OK

        val doc = getDocFromResponse(res)

        doc.title shouldBe otherPageMessages.noResultsPageTitle // Todo: should be mo results page title
      }
    }

  }
}
