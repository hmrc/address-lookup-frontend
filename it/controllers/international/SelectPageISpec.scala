package controllers.international

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
        val addressAmount = 50
        val testJourneyId = UUID.randomUUID().toString
        val testResultsList = internationalAddressResultsListBySize(numberOfRepeats = addressAmount)

        stubGetAddressByCountry(addressJson = testResultsList, countryCode = "BM")
        cache.putV2(testJourneyId,
          journeyDataV2ResultLimit.copy(proposals = Some(testInternationalProposedAddresses(addressAmount, "BM")), countryCode = Some("BM")))

        val res = buildClientLookupAddress(path = s"international/select?filter=$testFilterValue", testJourneyId)
          .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()

        res.status shouldBe OK

        val doc = getDocFromResponse(res)
        doc.title shouldBe messages("international.selectPage.title")
        doc.h1.text() shouldBe messages("international.selectPage.heading")
        doc.submitButton.text() shouldBe messages("international.selectPage.submitLabel")
        doc.link("editAddress") should have(
          href(routes.InternationalAddressLookupController.edit(id = testJourneyId).url),
          text(messages("international.selectPage.editAddressLinkText"))
        )

        val testIds = (testResultsList \\ "id").map {
          testId => testId.as[String]
        }

        testIds.zipWithIndex.foreach {
          case (id, idx) => {
            val i = idx + 1
            val fieldId = if (idx == 0) s"addressId" else s"addressId-$idx"
            doc.radio(fieldId) should have(
              value(id),
              label(s"Unit $i $i Street $i, District $i, City $i, City $i, Postcode $i")
            )
          }
        }
      }
    }

    "be shown with configured text" when {
      "there is a result list between 2 and 50 results" in {
        val addressAmount = 30
        val testJourneyId = UUID.randomUUID().toString
        val testResultsList = internationalAddressResultsListBySize(numberOfRepeats = addressAmount)

        stubGetAddressByCountry(addressJson = testResultsList, countryCode = "BM")
        cache.putV2(testJourneyId,
          journeyDataV2SelectLabels.copy(proposals = Some(testInternationalProposedAddresses(addressAmount, "BM")), countryCode = Some("BM")))

        val res = buildClientLookupAddress(path = s"international/select?filter=$testFilterValue", testJourneyId)
          .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()

        val doc = getDocFromResponse(res)

        await(res).status shouldBe OK
        for {
          l <- journeyDataV2SelectLabels.config.labels
          en <- l.en
          international <- en.international
          selectPage <- international.selectPageLabels
        } yield {
          doc.title shouldBe selectPage.title.get
          doc.h1.text() shouldBe selectPage.heading.get
          doc.submitButton.text() shouldBe selectPage.submitLabel.get
          doc.link("editAddress") should have(
            href(routes.InternationalAddressLookupController.edit(id = testJourneyId).url),
            text(selectPage.editAddressLinkText.get)
          )
        }

        val testIds = (testResultsList \\ "id").map {
          testId => testId.as[String]
        }

        testIds.zipWithIndex.foreach {
          case (id, idx) => {
            val i = idx + 1
            val fieldId = if (idx == 0) s"addressId" else s"addressId-$idx"
            doc.radio(fieldId) should have(
              value(id),
              label(s"Unit $i $i Street $i, District $i, City $i, City $i, Postcode $i")
            )
          }
        }
      }
    }

    "be not shown" when {
      "there are 0 results" in {
        val testJourneyId = UUID.randomUUID().toString
        stubGetAddressByCountry(addressJson = internationalAddressResultsListBySize(numberOfRepeats = 0), countryCode = "BM")
        cache.putV2(testJourneyId, journeyDataV2ResultLimit.copy(countryCode = Some("BM")))

        val res = buildClientLookupAddress(path = s"international/select?filter=$testFilterValue", testJourneyId)
          .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()

        getDocFromResponse(res)

        await(res).status shouldBe OK
      }

      "there is 1 result" in {
        val testJourneyId = UUID.randomUUID().toString
        stubGetAddressByCountry(addressJson = internationalAddressResultsListBySize(numberOfRepeats = 1), countryCode = "BM")
        cache.putV2(testJourneyId,
          journeyDataV2ResultLimit.copy(selectedAddress = Some(testInternationalConfirmedAddress(testJourneyId)), countryCode = Some("BM")))

        val res = buildClientLookupAddress(path = s"international/select?filter=$testFilterValue", testJourneyId)
          .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()

        getDocFromResponse(res)

        await(res).status shouldBe SEE_OTHER
      }

      "there are 50 results" in {
        val testJourneyId = UUID.randomUUID().toString
        stubGetAddressByCountry(addressJson = internationalAddressResultsListBySize(numberOfRepeats = 100), countryCode = "BM")
        cache.putV2(testJourneyId, journeyDataV2ResultLimit.copy(countryCode = Some("BM")))

        val res = buildClientLookupAddress(path = s"international/select?filter=$testFilterValue", testJourneyId)
          .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()

        getDocFromResponse(res)

        await(res).status shouldBe OK

      }
    }

    "be shown with welsh content" when {
      "the journey was setup with welsh enabled and the welsh cookie is present" in {
        val addressAmount = 50
        val testJourneyId = UUID.randomUUID().toString
        val testResultsList = internationalAddressResultsListBySize(numberOfRepeats = addressAmount)

        stubGetAddressByCountry(addressJson = testResultsList, countryCode = "BM")
        cache.putV2(testJourneyId,
          journeyDataV2DefaultWelshLabels.copy(proposals = Some(testInternationalProposedAddresses(addressAmount, "BM")), countryCode = Some("BM")))

        val res = buildClientLookupAddress(path = s"international/select?filter=$testFilterValue", testJourneyId)
          .withHttpHeaders(
            HeaderNames.COOKIE -> (getSessionCookie(Map("csrfToken" -> testCsrfToken())) + ";PLAY_LANG=cy;"),
            "Csrf-Token" -> "nocheck")
          .get()

        val doc = getDocFromResponse(res)
        await(res).status shouldBe OK

        doc.title() shouldBe messages(Lang("cy"), "international.selectPage.title")
        doc.h1.text() shouldBe messages(Lang("cy"), "international.selectPage.heading")
      }
    }

    "allow the initialising service to override the header size" when {
      "provided with a pageHeadingStyle option" in {
        val testJourneyId = UUID.randomUUID().toString

        val journeyData = JourneyDataV2(JourneyConfigV2(2, JourneyOptions(
          testContinueUrl,
          pageHeadingStyle = Some("govuk-heading-l"),
          selectPageConfig = Some(SelectPageConfig(proposalListLimit = Some(50))))),
          countryCode = Some("BM"))

        val addressAmount = 50
        val testResultsList = internationalAddressResultsListBySize(numberOfRepeats = addressAmount)

        stubGetAddressByCountry(addressJson = testResultsList, countryCode = "BM")
        cache.putV2(testJourneyId,
          journeyData.copy(proposals = Some(testInternationalProposedAddresses(addressAmount, "BM")), countryCode = Some("BM")))

        val fResponse = buildClientLookupAddress(path = s"international/select?filter=$testFilterValue", testJourneyId)
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
        val testResultsList = internationalAddressResultsListBySize(numberOfRepeats = 50)

        stubGetAddressByCountry(addressJson = testResultsList, countryCode = "BM")
        cache.putV2(testJourneyId, journeyDataV2DefaultWelshLabels)

        val res = buildClientLookupAddress(path = s"international/select?filter=$testFilterValue", testJourneyId)
          .withHttpHeaders(HeaderNames.COOKIE -> (getSessionCookie(Map("csrfToken" -> testCsrfToken())) + ";PLAY_LANG=cy;"), "Csrf-Token" -> "nocheck")
          .post(Map(
            "csrfToken" -> Seq("xxx-ignored-xxx")
          ))

        res.status shouldBe BAD_REQUEST

        val doc = getDocFromResponse(res)

        doc.title shouldBe s"Gwall: ${messages(Lang("cy"), "international.selectPage.title")}"
      }
    }
    "Redirects to Confirm page if option is selected" in {
      val testJourneyId = UUID.randomUUID().toString
      val testResultsList = internationalAddressResultsListBySize(numberOfRepeats = 2)

      stubGetAddressByCountry(addressJson = testResultsList, countryCode = "BM")
      cache.putV2(testJourneyId, journeyDataV2Minimal)

      val testIds = (testResultsList \\ "id").map {
        testId => testId.as[String]
      }

      val fRes = buildClientLookupAddress(path = s"international/select?filter=$testFilterValue", testJourneyId)
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
      val testResultsList = internationalAddressResultsListBySize(numberOfRepeats = 50)

      stubGetAddressByCountry(addressJson = testResultsList, countryCode = "BM")
      cache.putV2(testJourneyId, journeyDataV2Minimal)

      val fRes = buildClientLookupAddress(path = s"international/select?filter=$testFilterValue", testJourneyId)
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
      val testResultsList = internationalAddressResultsListBySize(numberOfRepeats = 0)

      stubGetAddressByCountry(addressJson = testResultsList, countryCode = "BM")
      cache.putV2(testJourneyId, journeyDataV2Minimal)

      val fRes = buildClientLookupAddress(path = s"international/select?filter=$testFilterValue", testJourneyId)
        .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
        .post(Map(
          "csrfToken" -> Seq("xxx-ignored-xxx"),
          "addressId" -> Seq("wrong-id")
        ))
      val res = await(fRes)
      res.status shouldBe SEE_OTHER
    }
  }

//  "technical difficulties" when {
//    "the welsh content header isn't set and welsh object isn't provided in config" should {
//      "render in English" in {
//        val testJourneyId = UUID.randomUUID().toString
//        val fResponse = buildClientLookupAddress(s"international/select?filter=$testFilterValue", testJourneyId)
//          .withHttpHeaders(
//            HeaderNames.COOKIE -> sessionCookieWithCSRF,
//            "Csrf-Token" -> "nocheck")
//          .get()
//
//        val res = await(fResponse)
//        res.status shouldBe INTERNAL_SERVER_ERROR
//
//        val doc = getDocFromResponse(res)
//        doc.title shouldBe messages("constants.intServerErrorTitle")
//        doc.h1 should have(text(messages("constants.intServerErrorTitle")))
//        doc.paras should have(elementWithValue(messages("constants.intServerErrorTryAgain")))
//      }
//    }
//
//    "the welsh content header is set to false and welsh object isn't provided in config" should {
//      "render in English" in {
//        val testJourneyId = UUID.randomUUID().toString
//        val fResponse = buildClientLookupAddress(s"international/select?filter=$testFilterValue", testJourneyId)
//          .withHttpHeaders(
//            HeaderNames.COOKIE -> sessionCookieWithWelshCookie(useWelsh = false),
//            "Csrf-Token" -> "nocheck")
//          .get()
//
//        val res = await(fResponse)
//        res.status shouldBe INTERNAL_SERVER_ERROR
//
//        val doc = getDocFromResponse(res)
//        doc.title shouldBe messages("constants.intServerErrorTitle")
//        doc.h1 should have(text(messages("constants.intServerErrorTitle")))
//        doc.paras should have(elementWithValue(messages("constants.intServerErrorTryAgain")))
//      }
//    }
//
//    "the welsh content header is set to false and welsh object is provided in config" should {
//      "render in English" in {
//        val testJourneyId = UUID.randomUUID().toString
//        val fResponse = buildClientLookupAddress(s"international/select?filter=$testFilterValue", testJourneyId)
//          .withHttpHeaders(
//            HeaderNames.COOKIE -> sessionCookieWithWelshCookie(useWelsh = false),
//            "Csrf-Token" -> "nocheck")
//          .get()
//
//        val res = await(fResponse)
//        res.status shouldBe INTERNAL_SERVER_ERROR
//
//        val doc = getDocFromResponse(res)
//        doc.title shouldBe messages("constants.intServerErrorTitle")
//        doc.h1 should have(text(messages("constants.intServerErrorTitle")))
//        doc.paras should have(elementWithValue(messages("constants.intServerErrorTryAgain")))
//      }
//    }
//
//    "the welsh content header is set to true and welsh object provided in config" should {
//      "render in Welsh" in {
//        val testJourneyId = UUID.randomUUID().toString
//        val fResponse = buildClientLookupAddress(s"international/select?filter=$testFilterValue", testJourneyId)
//          .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRFAndLang(), "Csrf-Token" -> "nocheck")
//          .get()
//
//        val res = await(fResponse)
//        res.status shouldBe INTERNAL_SERVER_ERROR
//
//        val doc = getDocFromResponse(res)
//        doc.title shouldBe messages(Lang("cy"), "constants.intServerErrorTitle")
//        doc.h1 should have(text(messages(Lang("cy"), "constants.intServerErrorTitle")))
//        doc.paras should have(elementWithValue(messages(Lang("cy"), "constants.intServerErrorTryAgain")))
//      }
//    }
//  }
}
