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

import address.v2.Country
import itutil.IntegrationSpecBase
import itutil.config.IntegrationTestConstants._
import model._
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.http.Status._
import play.api.i18n.Lang
import play.api.libs.json.Json
import services.JourneyDataV2Cache
import uk.gov.hmrc.http.HeaderCarrier

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global

class EditPageISpec extends IntegrationSpecBase {

  val cache = app.injector.instanceOf[JourneyDataV2Cache]
  implicit val hc: HeaderCarrier = HeaderCarrier()

  "The edit page" should {
    "when provided with no page config for english and welsh" should {
      "return Non UK edit page if UK mode is false" in {
        val testJourneyId = UUID.randomUUID().toString
        await(cache.putV2(testJourneyId, journeyDataV2WithSelectedAddress(testJourneyId)))

        val fResponse = buildClientLookupAddress(path = "edit", testJourneyId)
          .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF,
            "Csrf-Token" -> "nocheck")
          .get()

        val res = await(fResponse)
        res.status shouldBe OK

        val document = Jsoup.parse(res.body)
        document.title() shouldBe messages("editPage.title")
        document.h1.first.text() shouldBe messages("editPage.heading")
        document.h1.first.classNames() should contain("govuk-heading-xl")
        document.getElementById("continue").text() shouldBe "Continue"

        document.getElementById("line1").`val` shouldBe "1 High Street"
        document.getElementById("line2").`val` shouldBe "Line 2"
        document.getElementById("line3").`val` shouldBe "Line 3"
        document.getElementById("town").`val` shouldBe "Telford"
        document.getElementById("postcode").`val` shouldBe "AB11 1AB"

        labelForFieldsMatch(res, idOfFieldExpectedLabelTextForFieldMapping = Map(
          "organisation" -> "Organisation (optional)",
          "line1" -> "Address line 1",
          "line2" -> "Address line 2",
          "line3" -> "Address line 3",
          "town" -> "Town or city",
          "postcode" -> "Postcode (optional)",
          "countryCode" -> "Country or territory"
        ))
      }

      "return Non Uk edit page with default values where the 'PLAY_LANG' is set to cy but welsh config is not provided" in {
        val testJourneyId = UUID.randomUUID().toString
        val jc = fullDefaultJourneyConfigModelV2WithAllBooleansSet(false)
        val configWIthWelshEmptyBlock = journeyDataV2WithSelectedAddress(
          testJourneyId,
          jc.copy(labels = Some(jc.labels.get.copy(cy = Some(LanguageLabels())))))

        await(cache.putV2(testJourneyId, configWIthWelshEmptyBlock))

        val fResponse = buildClientLookupAddress(path = "edit", testJourneyId)
          .withHttpHeaders(
            HeaderNames.COOKIE -> sessionCookieWithCSRFAndLang(Some("cy")),
            "Csrf-Token" -> "nocheck")
          .get()

        val res = await(fResponse)
        res.status shouldBe OK

        val document = Jsoup.parse(res.body)
        document.title() shouldBe messages(Lang("cy"), "editPage.title")
        document.h1.first.text() shouldBe messages(Lang("cy"), "editPage.heading")
        document.getElementById("continue").text() shouldBe "Yn eich blaen"

        document.getElementById("line1").`val` shouldBe "1 High Street"
        document.getElementById("line2").`val` shouldBe "Line 2"
        document.getElementById("line3").`val` shouldBe "Line 3"
        document.getElementById("town").`val` shouldBe "Telford"
        document.getElementById("postcode").`val` shouldBe "AB11 1AB"

        labelForFieldsMatch(res, idOfFieldExpectedLabelTextForFieldMapping = Map(
          "line1" -> "Llinell cyfeiriad 1",
          "line2" -> "Llinell cyfeiriad 2",
          "line3" -> "Llinell cyfeiriad 3",
          "town" -> "Tref/dinas",
          "postcode" -> "Cod post (dewisol)",
          "countryCode" -> "Gwlad"
        ))
      }

      "allow the initialising service to override the header size" when {
        "uk Mode is false" in {
          val testJourneyId = UUID.randomUUID().toString
          await(cache.putV2(testJourneyId, journeyDataV2WithSelectedAddress(
            testJourneyId,
            journeyConfigV2 = JourneyConfigV2(2, JourneyOptions(testContinueUrl, pageHeadingStyle = Some("govuk-heading-l"))))))

          val fResponse = buildClientLookupAddress(path = "edit", testJourneyId)
            .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF,
              "Csrf-Token" -> "nocheck")
            .get()

          val res = await(fResponse)
          res.status shouldBe OK

          val document = Jsoup.parse(res.body)
          document.h1.first.classNames() should contain("govuk-heading-l")
        }

        "uk Mode is true" in {
          val testJourneyId = UUID.randomUUID().toString
          await(cache.putV2(testJourneyId, journeyDataV2WithSelectedAddress(
            testJourneyId,
            journeyConfigV2 = JourneyConfigV2(2, JourneyOptions(testContinueUrl, ukMode = Some(true),
              pageHeadingStyle = Some("govuk-heading-l"))))))

          val fResponse = buildClientLookupAddress(path = "edit", testJourneyId)
            .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF,
              "Csrf-Token" -> "nocheck")
            .get()

          val res = await(fResponse)
          res.status shouldBe OK

          val document = Jsoup.parse(res.body)
          document.h1.first.classNames() should contain("govuk-heading-l")
        }
      }

      "redirect to the UK edit page if uk UK mode is true" in {
        val testJourneyId = UUID.randomUUID().toString
        val testConfigWithAddressAndUkMode = journeyDataV2Minimal.copy(
          selectedAddress = Some(
            ConfirmableAddress("foo", Some("bar"), None, None, None, None, ConfirmableAddressDetails(None, List("wizz"), Some("bang"), Some("fooP"), Some(Country("GB", "United Kingdom"))))
          ), config = journeyDataV2Minimal.config.copy(options = journeyDataV2Minimal.config.options.copy(ukMode = Some(true)))
        )

        await(cache.putV2(testJourneyId, testConfigWithAddressAndUkMode))

        val fResponse = buildClientLookupAddress(path = "edit", testJourneyId)
          .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF,
            "Csrf-Token" -> "nocheck")
          .get()

        val res = await(fResponse)
        res.status shouldBe OK
      }

      "redirect to the UK edit page if country doesn't exist in selected address and AND UK mode is true" in {
        val testJourneyId = UUID.randomUUID().toString
        val testConfigWithAddressAndUkMode = journeyDataV2Minimal.copy(
          selectedAddress = Some(
            ConfirmableAddress("foo", Some("bar"), None, None, None, None, ConfirmableAddressDetails(None, List("wizz"), Some("bang"), Some("fooP")))
          ), config = journeyDataV2Minimal.config.copy(options = journeyDataV2Minimal.config.options.copy(ukMode = Some(true)))
        )

        await(cache.putV2(testJourneyId, testConfigWithAddressAndUkMode))

        val fResponse = buildClientLookupAddress(path = "edit", testJourneyId)
          .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()

        val res = await(fResponse)
        res.status shouldBe OK
      }

      "redirect to the International edit page if Uk mode is false but selected address in keystore" in {
        val testJourneyId = UUID.randomUUID().toString
        await(cache.putV2(testJourneyId, journeyDataV2WithSelectedAddress(testJourneyId)))

        val fResponse = buildClientLookupAddress(path = "edit", testJourneyId)
          .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()
        val res = await(fResponse)

        res.status shouldBe OK
      }
    }

    "provided with only custom content that has welsh block" should {
      "return non UK edit page if uk mode is false and should display all default values from the welsh constants with the 'PLAY_LANG' set to cy" in {
        val testJourneyId = UUID.randomUUID().toString
        val jc = fullDefaultJourneyConfigModelV2WithAllBooleansSet(false)
        val configWIthWelshEmptyBlock = journeyDataV2WithSelectedAddress(
          testJourneyId,
          jc.copy(labels = Some(jc.labels.get.copy(cy = Some(LanguageLabels())))))

        await(cache.putV2(testJourneyId, configWIthWelshEmptyBlock))

        val fResponse = buildClientLookupAddress(path = "edit", testJourneyId)
          .withHttpHeaders(
            HeaderNames.COOKIE -> sessionCookieWithCSRFAndLang(Some("cy")),
            "Csrf-Token" -> "nocheck")
          .get()

        val res = await(fResponse)
        res.status shouldBe OK

        val document = Jsoup.parse(res.body)
        document.title() shouldBe messages(Lang("cy"), "editPage.title")
        document.h1.first.text() shouldBe messages(Lang("cy"), "editPage.heading")
        document.getElementById("continue").text() shouldBe "Yn eich blaen"
        Option(document.getElementById("countryCode")).isDefined shouldBe true

        document.getElementById("line1").`val` shouldBe "1 High Street"
        document.getElementById("line2").`val` shouldBe "Line 2"
        document.getElementById("line3").`val` shouldBe "Line 3"
        document.getElementById("town").`val` shouldBe "Telford"
        document.getElementById("postcode").`val` shouldBe "AB11 1AB"

        labelForFieldsMatch(res, idOfFieldExpectedLabelTextForFieldMapping = Map(
          "line1" -> "Llinell cyfeiriad 1",
          "line2" -> "Llinell cyfeiriad 2",
          "line3" -> "Llinell cyfeiriad 3",
          "town" -> "Tref/dinas",
          "postcode" -> "Cod post (dewisol)"
        ))

        testCustomPartsOfGovWrapperElementsForDefaultConfig(fResponse)
      }
    }

    "provided with custom content" should {

      "return UK edit page if UK mode is false" in {
        val testJourneyId = UUID.randomUUID().toString
        await(cache.putV2(testJourneyId, journeyDataV2WithSelectedAddress(
          testJourneyId, journeyDataV2EditLabels(Some(false)).config)))

        val fResponse = buildClientLookupAddress(path = "edit", testJourneyId)
          .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()

        val res = await(fResponse)
        res.status shouldBe OK

        val document = Jsoup.parse(res.body)
        document.title() shouldBe "edit-title"
        document.h1.first.text() shouldBe "edit-heading"
        document.getElementById("continue").text() shouldBe "edit-submitLabel"

        document.getElementById("line1").`val` shouldBe "1 High Street"
        document.getElementById("line2").`val` shouldBe "Line 2"
        document.getElementById("line3").`val` shouldBe "Line 3"
        document.getElementById("town").`val` shouldBe "Telford"
        document.getElementById("postcode").`val` shouldBe "AB11 1AB"

        labelForFieldsMatch(res, idOfFieldExpectedLabelTextForFieldMapping = Map(
          "organisation" -> "edit-organisationLabel",
          "line1" -> "Custom Line1",
          "line2" -> "Custom Line2",
          "line3" -> "Custom Line3",
          "town" -> "Custom Town",
          "postcode" -> "Custom Postcode",
          "countryCode" -> "Custom Country"
        ))
      }

      "return non UK edit page if UK mode is false WITH NO 'PLAY_LANG' set" in {
        val testJourneyId = UUID.randomUUID().toString
        await(cache.putV2(testJourneyId, journeyDataV2WithSelectedAddress(
          testJourneyId, journeyDataV2EditLabels(Some(false)).config)))

        val fResponse = buildClientLookupAddress(path = "edit", testJourneyId)
          .withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(Map("csrfToken" -> testCsrfToken())),
            "Csrf-Token" -> "nocheck")
          .get()

        val res = await(fResponse)
        res.status shouldBe OK

        val document = Jsoup.parse(res.body)
        document.title() shouldBe "edit-title"
        document.h1.first.text() shouldBe "edit-heading"
        document.getElementById("continue").text() shouldBe "edit-submitLabel"
        Option(document.getElementById("countryCode")).isDefined shouldBe true

        document.getElementById("line1").`val` shouldBe "1 High Street"
        document.getElementById("line2").`val` shouldBe "Line 2"
        document.getElementById("line3").`val` shouldBe "Line 3"
        document.getElementById("town").`val` shouldBe "Telford"
        document.getElementById("postcode").`val` shouldBe "AB11 1AB"

        labelForFieldsMatch(res, idOfFieldExpectedLabelTextForFieldMapping = Map(
          "organisation" -> "edit-organisationLabel",
          "line1" -> "Custom Line1",
          "line2" -> "Custom Line2",
          "line3" -> "Custom Line3",
          "town" -> "Custom Town",
          "postcode" -> "Custom Postcode"
        ))
      }

      "return non UK edit page if UK mode is false WITH 'PLAY_LANG' set to cy AND welsh content provided" in {
        val testJourneyId = UUID.randomUUID().toString
        val jc = fullDefaultJourneyConfigModelV2WithAllBooleansSet(false)
        val configWithWelsh = journeyDataV2WithSelectedAddress(
          testJourneyId,
          jc.copy(labels =
            Some(jc.labels.get.copy(cy =
              Some(LanguageLabels(
                editPageLabels = Some(EditPageLabels(
                  title = Some("edit-title welsh"),
                  heading = Some("edit-heading welsh"),
                  line1Label = Some("Custom Line1 welsh"),
                  line2Label = Some("Custom Line2 welsh"),
                  line3Label = Some("Custom Line3 welsh"),
                  townLabel = Some("Custom Town welsh"),
                  postcodeLabel = Some("Custom Postcode welsh"),
                  countryLabel = Some("Custom Country welsh"),
                  submitLabel = Some("edit-submitLabel welsh"),
                  organisationLabel = Some("edit-organisationLabel welsh")
                ))
              ))
            ))))

        await(cache.putV2(testJourneyId, configWithWelsh))

        val fResponse = buildClientLookupAddress(path = "edit", testJourneyId)
          .withHttpHeaders(
            HeaderNames.COOKIE -> sessionCookieWithCSRFAndLang(Some("cy")),
            "Csrf-Token" -> "nocheck")
          .get()

        val res = await(fResponse)
        res.status shouldBe OK

        val document = Jsoup.parse(res.body)
        document.title() shouldBe "edit-title welsh"
        document.h1.first.text() shouldBe "edit-heading welsh"
        document.getElementById("continue").text() shouldBe "edit-submitLabel welsh"

        document.getElementById("line1").`val` shouldBe "1 High Street"
        document.getElementById("line2").`val` shouldBe "Line 2"
        document.getElementById("line3").`val` shouldBe "Line 3"
        document.getElementById("town").`val` shouldBe "Telford"
        document.getElementById("postcode").`val` shouldBe "AB11 1AB"

        labelForFieldsMatch(res, idOfFieldExpectedLabelTextForFieldMapping = Map(
          "organisation" -> "edit-organisationLabel welsh",
          "line1" -> "Custom Line1 welsh",
          "line2" -> "Custom Line2 welsh",
          "line3" -> "Custom Line3 welsh",
          "town" -> "Custom Town welsh",
          "postcode" -> "Custom Postcode welsh"
        ))
      }

      "return non - UK edit page if UK mode is false WITH NO 'PLAY_LANG' set" in {
        val testJourneyId = UUID.randomUUID().toString
        await(cache.putV2(testJourneyId, journeyDataV2WithSelectedAddress(
          testJourneyId, journeyDataV2EditLabels(Some(false)).config)))

        val fResponse = buildClientLookupAddress(path = "edit", testJourneyId)
          .withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(Map("csrfToken" -> testCsrfToken())),
            "Csrf-Token" -> "nocheck")
          .get()

        val res = await(fResponse)
        res.status shouldBe OK

        val document = Jsoup.parse(res.body)
        document.title() shouldBe "edit-title"
        document.h1.first.text() shouldBe "edit-heading"
        document.getElementById("continue").text() shouldBe "edit-submitLabel"
        Option(document.getElementById("countryCode")).isDefined shouldBe true

        document.getElementById("line1").`val` shouldBe "1 High Street"
        document.getElementById("line2").`val` shouldBe "Line 2"
        document.getElementById("line3").`val` shouldBe "Line 3"
        document.getElementById("town").`val` shouldBe "Telford"
        document.getElementById("postcode").`val` shouldBe "AB11 1AB"

        labelForFieldsMatch(res, idOfFieldExpectedLabelTextForFieldMapping = Map(
          "organisation" -> "edit-organisationLabel",
          "line1" -> "Custom Line1",
          "line2" -> "Custom Line2",
          "line3" -> "Custom Line3",
          "town" -> "Custom Town",
          "postcode" -> "Custom Postcode"
        ))
      }

      "return non - UK edit page if UK mode is false WITH 'PLAY_LANG' set to cy AND welsh content provided" in {
        val testJourneyId = UUID.randomUUID().toString
        val jc = fullDefaultJourneyConfigModelV2WithAllBooleansSet(false)
        val configWithWelsh = journeyDataV2WithSelectedAddress(
          testJourneyId,
          jc.copy(labels =
            Some(jc.labels.get.copy(cy =
              Some(LanguageLabels(
                editPageLabels = Some(EditPageLabels(
                  title = Some("edit-title welsh"),
                  heading = Some("edit-heading welsh"),
                  line1Label = Some("Custom Line1 welsh"),
                  line2Label = Some("Custom Line2 welsh"),
                  line3Label = Some("Custom Line3 welsh"),
                  townLabel = Some("Custom Town welsh"),
                  postcodeLabel = Some("Custom Postcode welsh"),
                  countryLabel = Some("Custom Country welsh"),
                  submitLabel = Some("edit-submitLabel welsh"),
                  organisationLabel = Some("edit-organisationLabel welsh")
                ))
              ))
            ))))

        await(cache.putV2(testJourneyId, configWithWelsh))

        val fResponse = buildClientLookupAddress(path = "edit", testJourneyId)
          .withHttpHeaders(
            HeaderNames.COOKIE -> sessionCookieWithCSRFAndLang(Some("cy")),
            "Csrf-Token" -> "nocheck")
          .get()

        val res = await(fResponse)
        res.status shouldBe OK

        val document = Jsoup.parse(res.body)
        document.title() shouldBe "edit-title welsh"
        document.h1.first.text() shouldBe "edit-heading welsh"
        document.getElementById("continue").text() shouldBe "edit-submitLabel welsh"

        document.getElementById("line1").`val` shouldBe "1 High Street"
        document.getElementById("line2").`val` shouldBe "Line 2"
        document.getElementById("line3").`val` shouldBe "Line 3"
        document.getElementById("town").`val` shouldBe "Telford"
        document.getElementById("postcode").`val` shouldBe "AB11 1AB"

        labelForFieldsMatch(res, idOfFieldExpectedLabelTextForFieldMapping = Map(
          "organisation" -> "edit-organisationLabel welsh",
          "line1" -> "Custom Line1 welsh",
          "line2" -> "Custom Line2 welsh",
          "line3" -> "Custom Line3 welsh",
          "town" -> "Custom Town welsh",
          "postcode" -> "Custom Postcode welsh",
          "countryCode" -> "Custom Country welsh"
        ))
      }
    }
  }

  "handleEditNonUk" should {
    "return 400 if all fields are missing and return nonUkEdit page with english text" in {
      val testJourneyId = UUID.randomUUID().toString
      await(cache.putV2(testJourneyId, journeyDataV2Minimal.copy(config = journeyDataV2Minimal.config.copy(options = journeyDataV2Minimal.config.options.copy(ukMode = Some(false))))))

      val fResponse = buildClientLookupAddress(path = "edit", testJourneyId)
        .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
        .post(Map("csrfToken" -> Seq("xxx-ignored-xxx")))

      val res = await(fResponse)
      res.status shouldBe BAD_REQUEST

      labelForFieldsMatch(res, idOfFieldExpectedLabelTextForFieldMapping = Map(
        "organisation" -> "Organisation (optional)",
        "line1" -> "Address line 1",
        "line2" -> "Address line 2",
        "line3" -> "Address line 3",
        "town" -> "Town or city",
        "postcode" -> "Postcode (optional)",
        "countryCode" -> "Country or territory"))
    }

    "return 400 if all fields are missing and return nonUkEdit page with welsh text" in {
      val testJourneyId = UUID.randomUUID().toString
      await(cache.putV2(testJourneyId, journeyDataV2Minimal.copy(
        config = journeyDataV2Minimal.config.copy(
          options = journeyDataV2Minimal.config.options.copy(ukMode = Some(false)),
          labels = Some(JourneyLabels(cy = Some(LanguageLabels())))))))

      val fResponse = buildClientLookupAddress(path = "edit", testJourneyId).
        withHttpHeaders(
          HeaderNames.COOKIE -> sessionCookieWithCSRFAndLang(Some("cy")),
          "Csrf-Token" -> "nocheck").
        post(Map("csrfToken" -> Seq("xxx-ignored-xxx")))

      val res = await(fResponse)

      val document = Jsoup.parse(res.body)
      document.title shouldBe "Gwall: Nodwch eich cyfeiriad"
      document.h1.first.text() shouldBe "Nodwch eich cyfeiriad"
      document.submitButton.text shouldBe "Yn eich blaen"
      Option(document.getElementById("countryCode")).isDefined shouldBe true

      document.input("line1") should have(value(""))
      document.input("line2") should have(value(""))
      document.input("line3") should have(value(""))
      document.input("town") should have(value(""))
      document.input("postcode") should have(value(""))

      labelForFieldsMatch(res, idOfFieldExpectedLabelTextForFieldMapping = Map(
        "line1" -> "Llinell cyfeiriad 1",
        "line2" -> "Llinell cyfeiriad 2",
        "line3" -> "Llinell cyfeiriad 3",
        "town" -> "Tref/dinas",
        "postcode" -> "Cod post (dewisol)"
      ))

      res.status shouldBe BAD_REQUEST
    }

    "return 400 if postcode is invalid and return nonUkEdit page with welsh text" in {
      val testJourneyId = UUID.randomUUID().toString
      await(cache.putV2(
        testJourneyId,
        journeyDataV2Minimal.copy(
          config = journeyDataV2Minimal.config.copy(
            options = journeyDataV2Minimal.config.options.copy(ukMode = Some(false)),
            labels = Some(JourneyLabels(cy = Some(LanguageLabels())))))))

      val fResponse = buildClientLookupAddress(path = "edit", testJourneyId).
        withHttpHeaders(
          HeaderNames.COOKIE -> sessionCookieWithCSRFAndLang(Some("cy")),
          "Csrf-Token" -> "nocheck").
        post(Map("csrfToken" -> Seq("xxx-ignored-xxx"), "postcode" -> Seq("eebb")))

      val res = await(fResponse)

      val document = Jsoup.parse(res.body)
      document.input("postcode") should have(value("eebb"))

      labelForFieldsMatch(res, idOfFieldExpectedLabelTextForFieldMapping = Map(
        "line1" -> "Llinell cyfeiriad 1",
        "line2" -> "Llinell cyfeiriad 2",
        "line3" -> "Llinell cyfeiriad 3",
        "town" -> "Tref/dinas",
        "postcode" -> "Cod post (dewisol)"
      ))

      res.status shouldBe BAD_REQUEST
    }

    // TODO: This setup doesn't look right
    s"return 303 if form is valid and redirect to ${controllers.routes.AbpAddressLookupController.confirm("")}" in {
      val testJourneyId = UUID.randomUUID().toString

      await(cache.putV2(
        testJourneyId,
        journeyDataV2Minimal.copy(
          config = journeyDataV2Minimal.config.copy(options = journeyDataV2Minimal.config.options.copy(ukMode = Some(false))))))

      await(cache.putV2(
        testJourneyId,
        journeyDataV2Minimal.copy(
          config = journeyDataV2Minimal.config.copy(options = journeyDataV2Minimal.config.options.copy(ukMode = Some(false))),
          selectedAddress = Some(testConfirmedAddress(testJourneyId).copy(id = None))
        )
      ))

      val fResponse = buildClientLookupAddress(path = "edit", testJourneyId)
        .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
        .post(Map(
          "csrfToken" -> Seq("xxx-ignored-xxx"),
          "line1" -> Seq(testAddressLine1),
          "line2" -> Seq(testAddressLine2),
          "town" -> Seq(testAddressTown),
          "postcode" -> Seq(testPostCode),
          "countryCode" -> Seq("GB")
        ))

      val res = await(fResponse)
      res.status shouldBe SEE_OTHER
    }
  }

  "handleEditUkMode" should {
    "return 400 if postcode is missing and return uk edit mode page with english text" in {
      val testJourneyId = UUID.randomUUID().toString
      await(cache.putV2(testJourneyId, journeyDataV2Minimal.copy(
        config = journeyDataV2Minimal.config.copy(
          options = journeyDataV2Minimal.config.options.copy(ukMode = Some(true))))))

      val fResponse = buildClientLookupAddress(path = "edit", testJourneyId).
        withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck").
        post(Map("csrfToken" -> Seq("xxx-ignored-xxx")))

      val res = await(fResponse)

      val document = Jsoup.parse(res.body)
      document.title shouldBe s"Error: ${messages("editPage.title")}"
      document.h1.first.text() shouldBe messages("editPage.heading")
      document.getElementById("line1-error").text() shouldBe s"Error: ${messages("constants.editPageAtLeastOneLineOrTown")}"

      document.submitButton.text shouldBe "Continue"
      testElementDoesntExist(res, "countryCode")

      document.input("line1") should have(value(""))
      document.input("line2") should have(value(""))
      document.input("line3") should have(value(""))
      document.input("town") should have(value(""))
      document.input("postcode") should have(value(""))

      labelForFieldsMatch(res, idOfFieldExpectedLabelTextForFieldMapping = Map(
        "organisation" -> "Organisation (optional)",
        "line1" -> "Address line 1",
        "line2" -> "Address line 2",
        "line3" -> "Address line 3",
        "town" -> "Town or city",
        "postcode" -> "UK postcode (optional)"
      ))

      res.status shouldBe BAD_REQUEST
    }

    "return 400 if postcode is missing and return uk edit mode page with english text and custom error messages" in {
      val testJourneyId = UUID.randomUUID().toString
      await(cache.putV2(testJourneyId, journeyDataV2Minimal.copy(
        config = journeyDataV2Minimal.config.copy(
          options = journeyDataV2Minimal.config.options.copy(ukMode = Some(true)),
          labels = Some(JourneyLabels(en = Some(LanguageLabels(otherLabels = Some(Json.parse(s"""{"constants.editPageAtLeastOneLineOrTown": "CUSTOM"}""")))))))
      )))

      val fResponse = buildClientLookupAddress(path = "edit", testJourneyId).
        withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck").
        post(Map("csrfToken" -> Seq("xxx-ignored-xxx")))

      val res = await(fResponse)

      val document = Jsoup.parse(res.body)
      document.getElementById("line1-error").text() shouldBe s"Error: CUSTOM"
      document.submitButton.text shouldBe "Continue"

      res.status shouldBe BAD_REQUEST
    }

    "return 400 if postcode is missing and return uk edit mode page with welsh text" in {
      val testJourneyId = UUID.randomUUID().toString
      await(cache.putV2(
        testJourneyId,
        journeyDataV2Minimal.copy(
          config = journeyDataV2Minimal.config.copy(
            options = journeyDataV2Minimal.config.options.copy(ukMode = Some(true)),
            labels = Some(JourneyLabels(cy = Some(LanguageLabels())))))))

      val fResponse = buildClientLookupAddress(path = "edit", testJourneyId).
        withHttpHeaders(
          HeaderNames.COOKIE -> sessionCookieWithCSRFAndLang(Some("cy")),
          "Csrf-Token" -> "nocheck").
        post(Map("csrfToken" -> Seq("xxx-ignored-xxx")))

      val res = await(fResponse)

      val document = Jsoup.parse(res.body)
      document.title shouldBe s"Gwall: ${messages(Lang("cy"), "editPage.title")}"
      document.h1.first.text() shouldBe messages(Lang("cy"), "editPage.title")
      document.submitButton.text shouldBe "Yn eich blaen"
      testElementDoesntExist(res, "countryCode")

      document.input("line1") should have(value(""))
      document.input("line2") should have(value(""))
      document.input("line3") should have(value(""))
      document.input("town") should have(value(""))
      document.input("postcode") should have(value(""))

      labelForFieldsMatch(res, idOfFieldExpectedLabelTextForFieldMapping = Map(
        "line1" -> "Llinell cyfeiriad 1",
        "line2" -> "Llinell cyfeiriad 2",
        "line3" -> "Llinell cyfeiriad 3",
        "town" -> "Tref/dinas",
        "postcode" -> "Cod post y DU (dewisol)"
      ))

      res.status shouldBe BAD_REQUEST
    }

    "return 400 if postcode is missing and return uk edit mode page with welsh text and custom error messages" in {
      val testJourneyId = UUID.randomUUID().toString
      await(cache.putV2(
        testJourneyId,
        journeyDataV2Minimal.copy(
          config = journeyDataV2Minimal.config.copy(
            options = journeyDataV2Minimal.config.options.copy(ukMode = Some(true)),
            labels = Some(JourneyLabels(cy = Some(LanguageLabels(otherLabels = Some(Json.parse(s"""{"constants.editPageAtLeastOneLineOrTown": "W CUSTOM"}""")))))))
        )))

      val fResponse = buildClientLookupAddress(path = "edit", testJourneyId).
        withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRFAndLang(Some("cy")), "Csrf-Token" -> "nocheck").
        post(Map("csrfToken" -> Seq("xxx-ignored-xxx")))

      val res = await(fResponse)

      val document = Jsoup.parse(res.body)
      document.getElementById("line1-error").text() shouldBe s"Gwall: W CUSTOM"
      document.submitButton.text shouldBe "Yn eich blaen"

      res.status shouldBe BAD_REQUEST
    }


    "return 303 if form is valid and redirect to Confirm" in {
      val testJourneyId = UUID.randomUUID().toString
      await(cache.putV2(
        testJourneyId,
        journeyDataV2Minimal.copy(
          config = journeyDataV2Minimal.config.copy(options = journeyDataV2Minimal.config.options.copy(ukMode = Some(false))))))

      val fResponse = buildClientLookupAddress(path = "edit", testJourneyId).
        withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck").
        post(Map(
          "csrfToken" -> Seq("xxx-ignored-xxx"),
          "line1" -> Seq(testAddressLine1),
          "line2" -> Seq(testAddressLine2),
          "town" -> Seq(testAddressTown),
          "postcode" -> Seq(testPostCode),
          "countryCode" -> Seq("GB")
        ))

      val res = await(fResponse)

      res.status shouldBe SEE_OTHER
    }
  }
}
