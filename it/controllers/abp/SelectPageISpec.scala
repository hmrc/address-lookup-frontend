package controllers.abp

import controllers.routes
import itutil.IntegrationSpecBase
import itutil.config.AddressRecordConstants._
import itutil.config.IntegrationTestConstants._
import itutil.config.PageElementConstants.SelectPage
import model.{JourneyConfigV2, JourneyDataV2, JourneyOptions, SelectPageConfig}
import org.jsoup.Jsoup
import play.api.i18n.Lang
import services.JourneyDataV2Cache
import uk.gov.hmrc.http.HeaderCarrier

import java.util.UUID
//import model.JourneyConfigDefaults.{EnglishConstants, WelshConstants}
//import model.MessageConstants.{EnglishMessageConstants => EnglishMessages, WelshMessageConstants => WelshMessages}
import play.api.http.HeaderNames
import play.api.http.Status._

import scala.concurrent.ExecutionContext.Implicits.global

class SelectPageISpec extends IntegrationSpecBase {
  val cache = app.injector.instanceOf[JourneyDataV2Cache]
  implicit val hc: HeaderCarrier = HeaderCarrier()

  "The select page GET" should {
    "be shown with default text" when {
      "there is a result list between 2 and 50 results" in {
        val testJourneyId = UUID.randomUUID().toString

        val addressAmount = 50
        val testResultsList = addressResultsListBySize(numberOfRepeats = addressAmount)
        stubGetAddressFromBE(addressJson = testResultsList)
        await(cache.putV2(testJourneyId,
          journeyDataV2ResultLimit.copy(proposals = Some(testProposedAddresses(addressAmount)))))

        val res = buildClientLookupAddress(path = "select?postcode=AB111AB", testJourneyId)
          .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()

        res.status shouldBe OK

        val doc = getDocFromResponse(res)

        doc.title shouldBe messages("selectPage.title")
        doc.h1.text() shouldBe messages("selectPage.heading")
        doc.submitButton.text() shouldBe messages("selectPage.submitLabel")
        doc.link("editAddress") should have(
          href(routes.AbpAddressLookupController.edit(id = testJourneyId, lookUpPostCode = Some(testPostCode)).url),
          text(messages("selectPage.editAddressLinkText"))
        )

        val testIds = (testResultsList \\ "id").map {
          testId => testId.as[String]
        }

        testIds.foreach {
          id => {
            val fieldId = (s"addressId")
            doc.radio(fieldId) should have(
              value(id),
              label(s"$testAddressLine1, $testAddressLine2, $testAddressTown, $testPostCode")
            )
          }
        }
      }
    }
    "be shown with configured text" when {
      "there is a result list between 2 and 50 results" in {
        val testJourneyId = UUID.randomUUID().toString
        val addressAmount = 30

        val testResultsList = addressResultsListBySize(numberOfRepeats = addressAmount)
        stubGetAddressFromBE(addressJson = testResultsList)
        await(cache.putV2(testJourneyId,
          journeyDataV2SelectLabels.copy(proposals = Some(testProposedAddresses(addressAmount)))))

        val res = buildClientLookupAddress(path = "select?postcode=AB111AB", testJourneyId)
          .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()

        val doc = getDocFromResponse(res)

        await(res).status shouldBe OK
        for {
          l <- journeyDataV2SelectLabels.config.labels
          en <- l.en
          selectPage <- en.selectPageLabels
        } yield {
          doc.title shouldBe selectPage.title.get
          doc.h1.text() shouldBe selectPage.heading.get
          doc.submitButton.text() shouldBe selectPage.submitLabel.get
          doc.link("editAddress") should have(
            href(routes.AbpAddressLookupController.edit(id = testJourneyId, lookUpPostCode = Some(testPostCode)).url),
            text(selectPage.editAddressLinkText.get)
          )
        }

        val testIds = (testResultsList \\ "id").map {
          testId => testId.as[String]
        }

        testIds.foreach {
          id => {
            val fieldId = (s"addressId")
            doc.radio(fieldId) should have(
              value(id),
              label(s"$testAddressLine1, $testAddressLine2, $testAddressTown, $testPostCode")
            )
          }
        }
      }
    }
    "be not shown" when {
      "there are 0 results" in {
        val testJourneyId = UUID.randomUUID().toString
        stubGetAddressFromBE(addressJson = addressResultsListBySize(numberOfRepeats = 0))
        await(cache.putV2(testJourneyId, journeyDataV2ResultLimit))

        val res = buildClientLookupAddress(path = "select?postcode=AB111AB", testJourneyId)
          .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()

        getDocFromResponse(res)

        await(res).status shouldBe OK

      }
      "there is 1 result" in {
        val testJourneyId = UUID.randomUUID().toString
        stubGetAddressFromBE(addressJson = addressResultsListBySize(numberOfRepeats = 1))
        await(cache.putV2(testJourneyId,
          journeyDataV2ResultLimit.copy(selectedAddress = Some(testConfirmedAddress(testJourneyId)))))

        val res = buildClientLookupAddress(path = "select?postcode=AB111AB", testJourneyId)
          .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()

        getDocFromResponse(res)

        await(res).status shouldBe SEE_OTHER


      }
      "there are 50 results" in {
        val testJourneyId = UUID.randomUUID().toString
        stubGetAddressFromBE(addressJson = addressResultsListBySize(numberOfRepeats = 100))
        await(cache.putV2(testJourneyId, journeyDataV2ResultLimit))

        val res = buildClientLookupAddress(path = "select?postcode=AB111AB", testJourneyId)
          .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()

        getDocFromResponse(res)

        await(res).status shouldBe OK

      }
    }
    "be shown with welsh content" when {
      "the journey was setup with welsh enabled and the welsh cookie is present" in {
        val testJourneyId = UUID.randomUUID().toString
        val addressAmount = 50
        val testResultsList = addressResultsListBySize(numberOfRepeats = addressAmount)

        stubGetAddressFromBE(addressJson = testResultsList)
        await(cache.putV2(testJourneyId,
          journeyDataV2DefaultWelshLabels.copy(proposals = Some(testProposedAddresses(addressAmount)))))

        val res = buildClientLookupAddress(path = "select?postcode=AB111AB", testJourneyId)
          .withHttpHeaders(
            HeaderNames.COOKIE -> (getSessionCookie(Map("csrfToken" -> testCsrfToken())) + ";PLAY_LANG=cy;"),
            "Csrf-Token" -> "nocheck")
          .get()

        val doc = getDocFromResponse(res)
        await(res).status shouldBe OK

        doc.title() shouldBe messages(Lang("cy"), "selectPage.title")
        doc.h1.text() shouldBe messages(Lang("cy"), "selectPage.heading")
      }
    }

    "allow the initialising service to override the header size" when {
      "provided with a pageHeadingStyle option" in {
        val testJourneyId = UUID.randomUUID().toString
        val journeyData = JourneyDataV2(JourneyConfigV2(2, JourneyOptions(
          testContinueUrl,
          pageHeadingStyle = Some("govuk-heading-l"),
          selectPageConfig = Some(SelectPageConfig(proposalListLimit = Some(50))))))

        val addressAmount = 50
        val testResultsList = addressResultsListBySize(numberOfRepeats = addressAmount)

        stubGetAddressFromBE(addressJson = testResultsList)
        await(cache.putV2(testJourneyId,
          journeyData.copy(proposals = Some(testProposedAddresses(addressAmount)))))

        val fResponse = buildClientLookupAddress(path = "select?postcode=AB111AB", testJourneyId)
          .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()
        val res = await(fResponse)

        res.status shouldBe OK
        val document = Jsoup.parse(res.body)
        document.getElementById("pageHeading").classNames() should contain("govuk-heading-l")
      }
    }
  }

  "The select page POST" should {
    "Display the select page in welsh" when {
      "no option was selected, welsh is enabled in the journey and the welsh cookie is present" in {
        val testJourneyId = UUID.randomUUID().toString
        val testResultsList = addressResultsListBySize(numberOfRepeats = 50)
        stubGetAddressFromBE(addressJson = testResultsList)
        await(cache.putV2(testJourneyId, journeyDataV2DefaultWelshLabels))

        val res = buildClientLookupAddress(path = s"select?postcode=$testPostCode", testJourneyId)
          .withHttpHeaders(HeaderNames.COOKIE -> (getSessionCookie(Map("csrfToken" -> testCsrfToken())) + ";PLAY_LANG=cy;"), "Csrf-Token" -> "nocheck")
          .post(Map(
            "csrfToken" -> Seq("xxx-ignored-xxx")
          ))

        res.status shouldBe BAD_REQUEST

        val doc = getDocFromResponse(res)

        doc.title shouldBe s"Gwall: ${messages(Lang("cy"), "selectPage.title")}"
      }
    }

    "Redirects to Confirm page if option is selected" in {
      val testJourneyId = UUID.randomUUID().toString
      val testResultsList = addressResultsListBySize(numberOfRepeats = 2)
      stubGetAddressFromBE(addressJson = testResultsList)
      await(cache.putV2(testJourneyId, journeyDataV2ResultLimit))

      val testIds = (testResultsList \\ "id").map {
        testId => testId.as[String]
      }

      val fRes = buildClientLookupAddress(path = s"select?postcode=$testPostCode", testJourneyId)
        .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
        .post(Map(
          "csrfToken" -> Seq("xxx-ignored-xxx"),
          "addressId" -> Seq(testIds.head)
        ))
      val res = await(fRes)
      res.status shouldBe SEE_OTHER
    }

    "Returns errors when no option has been selected" in {
      val testJourneyId = UUID.randomUUID().toString
      val testResultsList = addressResultsListBySize(numberOfRepeats = 50)
      stubGetAddressFromBE(addressJson = testResultsList)
      await(cache.putV2(testJourneyId, journeyDataV2ResultLimit))

      val fRes = buildClientLookupAddress(path = s"select?postcode=$testPostCode", testJourneyId)
        .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
        .post(Map(
          "csrfToken" -> Seq("xxx-ignored-xxx")
        ))

      fRes.status shouldBe BAD_REQUEST

      val doc = getDocFromResponse(fRes)

      val message = "Select an address"

      doc.errorSummary should have(
        errorSummaryMessage(SelectPage.addressId, message)
      )
    }

    "Redirect to Lookup page if there are no data or incorrect data is posted" in {
      val testJourneyId = UUID.randomUUID().toString
      val testResultsList = addressResultsListBySize(numberOfRepeats = 0)
      stubGetAddressFromBE(addressJson = testResultsList)
      await(cache.putV2(testJourneyId, journeyDataV2ResultLimit))

      val fRes = buildClientLookupAddress(path = s"select?postcode=$testPostCode", testJourneyId)
        .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
        .post(Map(
          "csrfToken" -> Seq("xxx-ignored-xxx"),
          "addressId" -> Seq("wrong-id")
        ))
      val res = await(fRes)
      res.status shouldBe SEE_OTHER
    }
  }s
}
