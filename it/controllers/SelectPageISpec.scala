package controllers

import itutil.IntegrationSpecBase
import itutil.config.AddressRecordConstants._
import itutil.config.IntegrationTestConstants._
import model.JourneyConfigDefaults._
import itutil.config.PageElementConstants.SelectPage
import play.api.http.HeaderNames
import play.api.http.Status._
import play.api.test.FakeApplication

class SelectPageISpec extends IntegrationSpecBase {
  override implicit lazy val app = FakeApplication(additionalConfiguration = fakeConfig())


  "The select page GET" should {
    "be shown with default text" when {
      "there is a result list between 2 and 50 results" in {
        val testResultsList = addressResultsListBySize(numberOfRepeats = 50)
        stubKeystore(session = testJourneyId, testConfigDefaultWithResultsLimitAsJson, OK)
        stubKeystoreSave(testJourneyId, testConfigWithoutAddressAsJson, OK)
        stubGetAddressFromBE(addressJson = testResultsList)

        val res = buildClientLookupAddress(path = "select?postcode=AB111AB&filter=")
          .withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()

        await(res).status shouldBe OK

        val doc = getDocFromResponse(res)

        doc.title shouldBe SELECT_PAGE_TITLE
        doc.h1.text() shouldBe SELECT_PAGE_HEADING
        doc.submitButton.text() shouldBe SELECT_PAGE_SUBMIT_LABEL
        doc.link("editAddress") should have(
          href(routes.AddressLookupController.edit(id = "Jid123", lookUpPostCode = Some(testPostCode), uk = Some(true)).url),
          text(EDIT_LINK_TEXT)
        )

        val testIds = (testResultsList \\ "id").map {
          testId => testId.as[String]
        }

        testIds.foreach {
          id => {
            val fieldId = (s"addressId-$id")
            doc.radio(fieldId) should have(
              value(id),
              label("line1, line2, town1, AB1 1AB")
            )
          }
        }
      }
    }
    "be shown with configured text" when {
      "there is a result list between 2 and 50 results" in {
        val testResultsList = addressResultsListBySize(numberOfRepeats = 30)
        stubKeystore(session = testJourneyId, testConfigSelectPageAsJson, OK)
        stubKeystoreSave(testJourneyId, testConfigSelectPageAsJson, OK)
        stubGetAddressFromBE(addressJson = testResultsList)

        val res = buildClientLookupAddress(path = "select?postcode=AB111AB&filter=")
          .withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()

        val doc = getDocFromResponse(res)

        await(res).status shouldBe OK

        doc.title shouldBe fullSelectPageConfig.title.get
        doc.h1.text() shouldBe fullSelectPageConfig.heading.get
        doc.submitButton.text() shouldBe fullSelectPageConfig.submitLabel.get
        doc.link("editAddress") should have(
          href(routes.AddressLookupController.edit(id = "Jid123", lookUpPostCode = Some(testPostCode), uk = Some(true)).url),
          text(fullSelectPageConfig.editAddressLinkText.get)
        )

        val testIds = (testResultsList \\ "id").map {
          testId => testId.as[String]
        }

        testIds.foreach {
          id => {
            val fieldId = (s"addressId-$id")
            doc.radio(fieldId) should have(
              value(id),
              label("line1, line2, town1, AB1 1AB")
            )
          }
        }
      }
    }
    "be not shown" when {
      "there are 0 results" in {
        stubKeystore(session = testJourneyId, testConfigDefaultWithResultsLimitAsJson, OK)
        stubKeystoreSave(testJourneyId, testConfigWithoutAddressAsJson, OK)
        stubGetAddressFromBE(addressJson = addressResultsListBySize(numberOfRepeats = 0))

        val res = buildClientLookupAddress(path = "select?postcode=AB111AB&filter=")
          .withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()

        val doc = getDocFromResponse(res)

        await(res).status shouldBe OK

      }
      "there is 1 result" in {
        stubKeystore(session = testJourneyId, testConfigDefaultWithResultsLimitAsJson, OK)
        stubKeystoreSave(testJourneyId, testConfigWithoutAddressAsJson, OK)
        stubGetAddressFromBE(addressJson = addressResultsListBySize(numberOfRepeats = 1))

        val res = buildClientLookupAddress(path = "select?postcode=AB111AB&filter=")
          .withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()

        val doc = getDocFromResponse(res)

        await(res).status shouldBe SEE_OTHER


      }
      "there are 50 results" in {
        stubKeystore(session = testJourneyId, testConfigDefaultWithResultsLimitAsJson, OK)
        stubKeystoreSave(testJourneyId, testConfigWithoutAddressAsJson, OK)
        stubGetAddressFromBE(addressJson = addressResultsListBySize(numberOfRepeats = 100))

        val res = buildClientLookupAddress(path = "select?postcode=AB111AB&filter=")
          .withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()

        val doc = getDocFromResponse(res)

        await(res).status shouldBe OK

      }
    }
  }

  "The select page POST" should {
    "Redirects to Confirm page if option is selected" in {
      val testResultsList = addressResultsListBySize(numberOfRepeats = 2)
      stubKeystore(session = testJourneyId, testConfigDefaultWithResultsLimitAsJson, OK)
      stubKeystoreSave(testJourneyId, testConfigWithoutAddressAsJson, OK)
      stubGetAddressFromBE(addressJson = testResultsList)

      val testIds = (testResultsList \\ "id").map {
        testId => testId.as[String]
      }

      val fRes = buildClientLookupAddress(path = "select")
        .withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
        .post(Map(
          "csrfToken" -> Seq("xxx-ignored-xxx"),
          "addressId" -> Seq(testIds.head)
        ))
      val res = await(fRes)
      res.status shouldBe SEE_OTHER
    }
    "Returns errors when no option has been selected" in {
      val testResultsList = addressResultsListBySize(numberOfRepeats = 50)
      stubKeystore(session = testJourneyId, testConfigDefaultWithResultsLimitAsJson, OK)
      stubKeystoreSave(testJourneyId, testConfigWithoutAddressAsJson, OK)
      stubGetAddressFromBE(addressJson = testResultsList)

      val fRes = buildClientLookupAddress(path = "select")
        .withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
        .post(Map(
          "csrfToken" -> Seq("xxx-ignored-xxx")
        ))

      fRes.status shouldBe BAD_REQUEST

      val doc = getDocFromResponse(fRes)

      val message = "This field is required"

      doc.errorSummary should have (
        errorSummaryMessage(SelectPage.addressId, message)
      )
    }
    "Redirect to Lookup page if there are no data or incorrect data is posted" in {
      val testResultsList = addressResultsListBySize(numberOfRepeats = 0)
      stubKeystore(session = testJourneyId, testConfigDefaultWithResultsLimitAsJson, OK)
      stubKeystoreSave(testJourneyId, testConfigWithoutAddressAsJson, OK)
      stubGetAddressFromBE(addressJson = testResultsList)

      val fRes = buildClientLookupAddress(path = "select")
        .withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
        .post(Map(
          "csrfToken" -> Seq("xxx-ignored-xxx"),
          "addressId" -> Seq("wrong-id")
        ))
      val res = await(fRes)
      res.status shouldBe SEE_OTHER
    }
  }



}
