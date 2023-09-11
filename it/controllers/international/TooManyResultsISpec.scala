package controllers.international

import controllers.routes
import itutil.config.AddressRecordConstants._
import itutil.config.IntegrationTestConstants._
import itutil.{IntegrationSpecBase, PageContentHelper}
import model.{JourneyConfigV2, JourneyDataV2, JourneyOptions, SelectPageConfig}
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.http.Status._
import play.api.libs.json.Json
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

  "The 'Too Many Results' page" should {
    "be rendered" when {
      "the back buttons are enabled in the journey config" when {
        "a filter has been entered" when {
          "the backend returns too many addresses" in {
            val testJourneyId = UUID.randomUUID().toString
            cache.putV2(testJourneyId, journeyDataV2ResultLimit.copy(countryCode = Some("BM")))

            stubGetAddressByCountry(addressJson = internationalAddressResultsListBySize(51), countryCode = "BM")

            val res = buildClientLookupAddress(path = s"international/select?filter=$testFilterValue", testJourneyId)
              .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
              .get()

            await(res).status shouldBe OK

            val doc = getDocFromResponse(res)

            doc.select("a[class=govuk-back-link]") should have(
              text("Back")
            )

            doc.title shouldBe tooManyResultsMessages.title
            doc.h1.text shouldBe tooManyResultsMessages.heading1
            doc.paras.not(".language-select").get(1).text shouldBe tooManyResultsMessages.line1
            doc.bulletPointList.select("li").first.text shouldBe tooManyResultsMessages.bullet2WithFilter(testFilterValue)
            doc.link("anotherSearch").text() shouldBe tooManyResultsMessages.button
            doc.link("enterManual") should have(
              href(routes.InternationalAddressLookupController.edit(testJourneyId).url),
              text("Enter the address manually")
            )
          }
        }
      }

      "the back buttons are not enabled in the journey config" when {
        "a filter has been entered" when {
          "the backend returns too many addresses" in {
            val testJourneyId = UUID.randomUUID().toString
            cache.putV2(testJourneyId, journeyDataV2SelectLabelsNoBack.copy(countryCode = Some("BM")))

            stubGetAddressByCountry(addressJson = internationalAddressResultsListBySize(51), countryCode = "BM")

            val res = buildClientLookupAddress(path = s"international/select?filter=$testFilterValue", testJourneyId)
              .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
              .get()

            await(res).status shouldBe OK

            val doc = getDocFromResponse(res)

            doc.select("a[class=govuk-back-link]") shouldNot have(
              text("Back")
            )
            doc.title shouldBe tooManyResultsMessages.title
            doc.h1.text shouldBe tooManyResultsMessages.heading1
            doc.paras should have(elementWithValue(tooManyResultsMessages.line1))
            doc.bulletPointList.select("li").first.text shouldBe tooManyResultsMessages.bullet2WithFilter(testFilterValue)
            doc.link("anotherSearch").text() shouldBe tooManyResultsMessages.button
            doc.link("enterManual") should have(
              href(routes.InternationalAddressLookupController.edit(testJourneyId).url),
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
          pageHeadingStyle = Some("govuk-heading-l"))), countryCode = Some("BM"))

        cache.putV2(testJourneyId, journeyData)
        stubGetAddressByCountry(addressJson = internationalAddressResultsListBySize(numberOfRepeats = 51), countryCode = "BM")

        val fResponse = buildClientLookupAddress(path = s"international/select?filter=$testFilterValue", testJourneyId)
          .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()
        val res = await(fResponse)

        res.status shouldBe OK
        val document = Jsoup.parse(res.body)
        document.getElementById("pageHeading").classNames() should contain("govuk-heading-l")
      }
    }

    "not be rendered" when {
      "the backend service returns enough addresses to be displayed on the select page" in {
        val addressAmount = 25
        val testJourneyId = UUID.randomUUID().toString

        stubGetAddressByCountry(addressJson = internationalAddressResultsListBySize(numberOfRepeats = addressAmount), countryCode = "BM")
        cache.putV2(testJourneyId,
          journeyDataV2ResultLimit.copy(proposals = Some(testInternationalProposedAddresses(addressAmount, "BM")), countryCode = Some("BM")))

        val res = buildClientLookupAddress(path = s"international/select?filter=$testFilterValue", testJourneyId)
          .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()

        await(res).status shouldBe OK

        val doc = getDocFromResponse(res)

        doc.title shouldBe messages("international.selectPage.title")
      }

      "the backend service returns 1 address and redirects to the confirm page" in {
        val testJourneyId = UUID.randomUUID().toString
        stubGetAddressByCountry(addressJson = internationalAddressResultsListBySize(1), countryCode = "BM")
        cache.putV2(testJourneyId,
          journeyDataV2ResultLimit.copy(selectedAddress = Some(testInternationalConfirmedAddress(testJourneyId)), countryCode = Some("BM")))

        val res = buildClientLookupAddress(path = s"international/select?filter=$testFilterValue", testJourneyId)
          .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()

        val completedResponse = await(res)
        completedResponse.status shouldBe SEE_OTHER
        completedResponse.header(HeaderNames.LOCATION).get shouldBe s"/lookup-address/$testJourneyId/international/confirm"

      }

      "the backend service returns no addresses and renders the no results found page" in {
        val testJourneyId = UUID.randomUUID().toString
        cache.putV2(testJourneyId, journeyDataV2ResultLimit.copy(countryCode = Some("BM")))
        stubGetAddressByCountry(addressJson = Json.arr(), countryCode = "BM")

        val res = buildClientLookupAddress(path = s"international/select?filter=$testFilterValue", testJourneyId)
          .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()

        await(res).status shouldBe OK

        val doc = getDocFromResponse(res)

        doc.title shouldBe s"${otherPageMessages.noResultsPageTitle} $testFilterValue"
      }
    }
  }
}
