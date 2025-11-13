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

package controllers.abp

import controllers.routes
import itutil.config.AddressRecordConstants.*
import itutil.config.IntegrationTestConstants.*
import itutil.{IntegrationSpecBase, PageContentHelper}
import model.v2.{JourneyConfigV2, JourneyDataV2, JourneyOptions, SelectPageConfig}
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.http.Status.*
import play.api.libs.json.Json
import play.api.libs.ws.WSResponse
import services.JourneyDataV2Cache
import uk.gov.hmrc.http.HeaderCarrier

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global

class TooManyResultsISpec extends IntegrationSpecBase with PageContentHelper {
  val cache = app.injector.instanceOf[JourneyDataV2Cache]
  implicit val hc: HeaderCarrier = HeaderCarrier()

  object tooManyResultsMessages {
    val title = "No results found"
    val heading1 = "Too many results, enter more details"
    val heading2 = "We couldn't find any results for that property name or number"

    def bullet1(postcode: String) = s"$postcode for postcode"

    val bullet2NoFilter = "nothing for property name or number"

    def bullet2WithFilter(filter: String) = s"'$filter' for name or number"

    val line1 = "You entered:"
    val button = "Try a new search"
    val back = "back"
  }

  object welshTooManyResultsMessages {
    val title = "Dim canlyniadau wedi’u darganfod"
    val heading1 = "Mae yna ormod o ganlyniadau"
    val heading2 = "Ni allem ddod o hyd i unrhyw ganlyniadau ar gyfer enw neu rif yr eiddo hwnnw"

    def bullet1(postcode: String) = s"$postcode am y cod post"

    val bullet2NoFilter = "ddim byd ar gyfer enw neu rif eiddo"

    def bullet2WithFilter(filter: String) = s"'$filter' ar gyfer enw neu rif"

    val line1 = "Nodoch:"
    val button = "Rhowch gynnig ar chwiliad newydd"
    val back = "Yn ôl"
  }

  object otherPageMessages {
    val noResultsPageTitle = "We cannot find any addresses for"
  }

  //  val EnglishConstantsUkMode = EnglishConstants(true)

  //  import EnglishConstantsUkMode._

  "The 'Too Many Results' page" should {
    "be rendered" when {
      "the back buttons are enabled in the journey config" when {
        "no filter has been entered" when {
          "the backend service returns too many addresses" in {
            val testJourneyId = UUID.randomUUID().toString
            await(cache.putV2(testJourneyId, journeyDataV2ResultLimit))
            stubGetAddressFromBE(addressJson = addressResultsListBySize(numberOfRepeats = 51))

            val res = buildClientLookupAddress(path = "select?postcode=AB11+1AB", testJourneyId)
              .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
              .get()

            await(res).status.shouldBe(OK)

            val doc = getDocFromResponse(res)

            doc.select("a[class=govuk-back-link]") should have(
              text("Back")
            )
            doc.title.shouldBe(tooManyResultsMessages.title)
            doc.h1.text.shouldBe(tooManyResultsMessages.heading1)
            doc.paras should have(elementWithValue(tooManyResultsMessages.line1))
            doc.bulletPointList.select("li").first.text.shouldBe(tooManyResultsMessages.bullet1(testPostCode))
            doc.bulletPointList.select("li").last.text.shouldBe(tooManyResultsMessages.bullet2NoFilter)
            doc.link("anotherSearch").text().shouldBe(tooManyResultsMessages.button)
            doc.link("enterManual") should have(
              href(routes.AbpAddressLookupController.edit(testJourneyId, Some(testPostCode)).url),
              text("Enter the address manually")
            )
          }
        }

        "a filter has been entered" when {
          "the backend returns too many addresses" in {
            val testJourneyId = UUID.randomUUID().toString
            await(cache.putV2(testJourneyId, journeyDataV2ResultLimit))
            stubGetAddressFromBEWithFilter(addressJson = Json.arr())
            stubGetAddressFromBE(addressJson = addressResultsListBySize(51))

            val res = buildClientLookupAddress(path = s"select?postcode=AB11+1AB&filter=$testFilterValue", testJourneyId)
              .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
              .get()

            await(res).status.shouldBe(OK)

            val doc = getDocFromResponse(res)

            doc.select("a[class=govuk-back-link]") should have(
              text("Back")
            )
            doc.title.shouldBe(tooManyResultsMessages.title)
            doc.h1.text.shouldBe(tooManyResultsMessages.heading2)
            doc.paras should have(elementWithValue(tooManyResultsMessages.line1))
            doc.bulletPointList.select("li").first.text.shouldBe(tooManyResultsMessages.bullet1(testPostCode))
            doc.bulletPointList.select("li").last.text.shouldBe(tooManyResultsMessages.bullet2WithFilter(testFilterValue))
            doc.link("anotherSearch").text().shouldBe(tooManyResultsMessages.button)
            doc.link("enterManual") should have(
              href(routes.AbpAddressLookupController.edit(testJourneyId, Some(testPostCode)).url),
              text("Enter the address manually")
            )
          }
        }
      }

      "the back buttons are not enabled in the journey config" when {
        "no filter has been entered" when {
          "the backend service returns too many addresses " in {
            val testJourneyId = UUID.randomUUID().toString
            await(cache.putV2(testJourneyId, journeyDataV2SelectLabelsNoBack))
            stubGetAddressFromBE(addressJson = addressResultsListBySize(numberOfRepeats = 51))

            val res = buildClientLookupAddress(path = "select?postcode=AB11+1AB", testJourneyId)
              .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
              .get()

            await(res).status.shouldBe(OK)

            val doc = getDocFromResponse(res)

            doc.select("a[class=govuk-back-link]") shouldNot have(
              text("Back")
            )
            doc.title.shouldBe(tooManyResultsMessages.title)
            doc.h1.text.shouldBe(tooManyResultsMessages.heading1)
            doc.paras should have(elementWithValue(tooManyResultsMessages.line1))
            doc.bulletPointList.select("li").first.text.shouldBe(tooManyResultsMessages.bullet1(testPostCode))
            doc.bulletPointList.select("li").last.text.shouldBe(tooManyResultsMessages.bullet2NoFilter)
            doc.link("anotherSearch").text().shouldBe(tooManyResultsMessages.button)
            doc.link("enterManual") should have(
              href(routes.AbpAddressLookupController.edit(testJourneyId, Some(testPostCode)).url),
              text("Enter the address manually")
            )
          }
        }

        "a filter has been entered" when {
          "the backend returns too many addresses" in {
            val testJourneyId = UUID.randomUUID().toString
            await(cache.putV2(testJourneyId, journeyDataV2SelectLabelsNoBack))
            stubGetAddressFromBEWithFilter(addressJson = Json.arr())
            stubGetAddressFromBE(addressJson = addressResultsListBySize(51))

            val res = buildClientLookupAddress(path = s"select?postcode=AB11+1AB&filter=$testFilterValue", testJourneyId)
              .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
              .get()

            await(res).status.shouldBe(OK)

            val doc = getDocFromResponse(res)

            doc.select("a[class=govuk-back-link]") shouldNot have(
              text("Back")
            )
            doc.title.shouldBe(tooManyResultsMessages.title)
            doc.h1.text.shouldBe(tooManyResultsMessages.heading2)
            doc.paras should have(elementWithValue(tooManyResultsMessages.line1))
            doc.bulletPointList.select("li").first.text.shouldBe(tooManyResultsMessages.bullet1(testPostCode))
            doc.bulletPointList.select("li").last.text.shouldBe(tooManyResultsMessages.bullet2WithFilter(testFilterValue))
            doc.link("anotherSearch").text().shouldBe(tooManyResultsMessages.button)
            doc.link("enterManual") should have(
              href(routes.AbpAddressLookupController.edit(testJourneyId, Some(testPostCode)).url),
              text("Enter the address manually")
            )
          }
        }
      }
    }

    "allow the initialising service to override the header size" when {
      "provided with a pageHeadingStyle option" in {
        val testJourneyId = UUID.randomUUID().toString
        val journeyData = JourneyDataV2(JourneyConfigV2(2, JourneyOptions(
          testContinueUrl,
          selectPageConfig = Some(SelectPageConfig(proposalListLimit = Some(50))),
          pageHeadingStyle = Some("govuk-heading-l"))))

          await(cache.putV2(testJourneyId, journeyData))
        stubGetAddressFromBE(addressJson = addressResultsListBySize(numberOfRepeats = 51))

        val fResponse = buildClientLookupAddress(path = "select?postcode=AB111AB", testJourneyId)
          .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()
        val res: WSResponse = await(fResponse)

        res.status.shouldBe(OK)
        val document = Jsoup.parse(res.body)
        document.getElementById("pageHeading").classNames().should(contain("govuk-heading-l"))
      }
    }

    "not be rendered" when {
      "the backend service returns enough addresses to be displayed on the select page" in {
        val addressAmount = 25
        val testJourneyId = UUID.randomUUID().toString

        stubGetAddressFromBE(addressJson = addressResultsListBySize(numberOfRepeats = addressAmount))
        await(cache.putV2(testJourneyId,
          journeyDataV2ResultLimit.copy(proposals = Some(testProposedAddresses(addressAmount)))))

        val res = buildClientLookupAddress(path = "select?postcode=AB111AB", testJourneyId)
          .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()

        await(res).status.shouldBe(OK)

        val doc = getDocFromResponse(res)

        doc.title.shouldBe(messages("selectPage.title"))
      }

      "the backend service returns 1 address and redirects to the confirm page" in {
        val testJourneyId = UUID.randomUUID().toString
        stubGetAddressFromBE(addressJson = addressResultsListBySize(1))
        await(cache.putV2(testJourneyId,
          journeyDataV2ResultLimit.copy(selectedAddress = Some(testConfirmedAddress(testJourneyId)))))

        val res = buildClientLookupAddress(path = "select?postcode=AB111AB", testJourneyId)
          .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()

        val completedResponse = await(res)

        completedResponse.status.shouldBe(SEE_OTHER)
        completedResponse.header(HeaderNames.LOCATION).get.shouldBe(s"/lookup-address/$testJourneyId/confirm")

      }

      "the backend service returns no addresses and renders the no results found page" in {
        val testJourneyId = UUID.randomUUID().toString
        await(cache.putV2(testJourneyId, journeyDataV2Minimal))
        stubGetAddressFromBE(addressJson = Json.arr())

        val res = buildClientLookupAddress(path = "select?postcode=AB111AB", testJourneyId)
          .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()

        await(res).status.shouldBe(OK)

        val doc = getDocFromResponse(res)

        doc.title.shouldBe(s"${otherPageMessages.noResultsPageTitle} AB11 1AB")
      }
    }
  }
}
