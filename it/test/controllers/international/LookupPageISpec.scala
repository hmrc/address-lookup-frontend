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

package controllers.international

import com.codahale.metrics.SharedMetricRegistries
import controllers.routes
import itutil.IntegrationSpecBase
import itutil.config.IntegrationTestConstants.*
import itutil.config.PageElementConstants.LookupPage
import model.v2.{JourneyConfigV2, JourneyOptions}
import org.jsoup.Jsoup
import play.api.Application
import play.api.Mode.Test
import play.api.http.HeaderNames
import play.api.http.Status.*
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.WSBodyWritables.writeableOf_urlEncodedSimpleForm
import play.api.libs.ws.WSResponse
import services.JourneyDataV2Cache
import uk.gov.hmrc.http.HeaderCarrier

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps
import scala.util.Random

class LookupPageISpec extends IntegrationSpecBase {
  val cache = app.injector.instanceOf[JourneyDataV2Cache]
  implicit val hc: HeaderCarrier = HeaderCarrier()

  def longFilterValue = (1 to 257) map (_ => Random.alphanumeric.head) mkString

  // TODO: Make hint configurable as part of welsh translation
  val hardCodedFormHint = " For example, The Mill, 116 or Flat 37a"

  override lazy val app: Application = {
    SharedMetricRegistries.clear()
    new GuiceApplicationBuilder()
      .configure(fakeConfig())
      .configure("error.required" -> "Postcode is required")
      .in(Test)
      .build()
  }

  "The lookup page" when {
    "when provided with no page config" should {
      "Render the default content" in {
        val testJourneyId = UUID.randomUUID().toString
        await(cache.putV2(testJourneyId, testMinimalLevelJourneyDataV2))

        val fResponse = buildClientLookupAddress(path = s"international/lookup?filter=$testFilterValue", testJourneyId)
          .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()

        val res: WSResponse = await(fResponse)
        val doc = getDocFromResponse(res)

        res.status.shouldBe(OK)

        testCustomPartsOfGovWrapperElementsForDefaultConfig(fResponse)

        doc.title.shouldBe(messages("international.lookupPage.title"))
        doc.h1.text().shouldBe(messages("international.lookupPage.heading"))

        doc.getElementById("afterHeadingText").shouldBe(null)

        doc.select("a[class=govuk-back-link]") should have(
          text("Back")
        )

        doc.input(LookupPage.filterId) should have(
          label(messages("international.lookupPage.filterLabel") + hardCodedFormHint),
          value(testFilterValue)
        )

        doc.link(LookupPage.manualAddressLink) should have(
          href(routes.InternationalAddressLookupController.edit(testJourneyId).url),
          text(messages("international.lookupPage.manualAddressLinkText"))
        )

        doc.submitButton.text().shouldBe("Continue")
      }

      "Show the default 'filter invalid' error messages" in {
        val testJourneyId = UUID.randomUUID().toString
        await(cache.putV2(testJourneyId, testMinimalLevelJourneyDataV2))

        val filterValue = longFilterValue
        val fResponse = buildClientLookupAddress(path = s"international/lookup", testJourneyId)
          .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .post(Map("filter" -> filterValue))

        val res: WSResponse = await(fResponse)
        val doc = getDocFromResponse(res)

        res.status.shouldBe(BAD_REQUEST)

        val message = "The house name or number needs to be fewer than 256 characters"

        doc.errorSummary should have(
          errorSummaryMessage(LookupPage.filterId, message)
        )

        doc.input(LookupPage.filterId) should have(
          errorMessage(s"Error: $message"),
          value(filterValue)
        )
      }
    }

    "when provided with a pageHeadingStyle option" should {
      "allow the initialising service to override the header size" in {
        val testJourneyId = UUID.randomUUID().toString
        await(cache.putV2(testJourneyId, journeyDataV2WithSelectedAddress(
          testJourneyId,
          journeyConfigV2 = JourneyConfigV2(2, JourneyOptions(testContinueUrl, pageHeadingStyle = Some("govuk-heading-l"))))))

        val fResponse = buildClientLookupAddress(path = s"international/lookup?filter=$testFilterValue", testJourneyId)
          .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()
        val res: WSResponse = await(fResponse)

        res.status.shouldBe(OK)
        val document = Jsoup.parse(res.body)
        document.getElementById("pageHeading").classNames().should(contain("govuk-heading-l"))
      }
    }

    "Provided with custom content" should {
      "Render the page with custom content" in {
        val testJourneyId = UUID.randomUUID().toString
        await(cache.putV2(testJourneyId, testCustomLookupPageJourneyConfigV2))

        val fResponse = buildClientLookupAddress(path = s"international/lookup?filter=$testFilterValue", testJourneyId)
          .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()

        val res: WSResponse = await(fResponse)
        val doc = getDocFromResponse(res)

        res.status.shouldBe(OK)

        for {
          l <- testCustomLookupPageJourneyConfigV2.config.labels
          en <- l.en
          international <- en.international
          lookupPage <- international.lookupPageLabels
        } yield {

          doc.title.shouldBe(lookupPage.title.get + " - NAV_TITLE - GOV.UK")
          doc.h1.text().shouldBe(lookupPage.heading.get)

          doc.getElementById("afterHeadingText").html.shouldBe("international-after-heading-text")

          doc.select("a[class=govuk-back-link]") should have(
            text("Back")
          )

          doc.input(LookupPage.filterId) should have(
            label(lookupPage.filterLabel.get + hardCodedFormHint),
            value(testFilterValue)
          )

          doc.link(LookupPage.manualAddressLink) should have(
            href(routes.InternationalAddressLookupController.edit(testJourneyId).url),
            text(lookupPage.manualAddressLinkText.get)
          )

          doc.submitButton.text().shouldBe(lookupPage.submitLabel.get)

        }

      }

      "not display the back button if disabled" in {
        val testJourneyId = UUID.randomUUID().toString
        await(cache.putV2(testJourneyId, testDefaultLookupPageJourneyDataV2))

        val fResponse = buildClientLookupAddress(path = s"international/lookup?filter=$testFilterValue", testJourneyId)
          .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()
        val res: WSResponse = await(fResponse)
        val doc = getDocFromResponse(res)

        res.status.shouldBe(OK)

        doc.select("a[class=govuk-back-link]") should not have (
          text("Back")
          )
      }
    }

    "Provided with config with all booleans set to true" should {
      "Render the page correctly with custom elements" in {
        val testJourneyId = UUID.randomUUID().toString
        await(cache.putV2(testJourneyId, testCustomLookupPageJourneyConfigV2))

        val fResponse = buildClientLookupAddress(path = s"international/lookup?filter=$testFilterValue", testJourneyId)
          .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()
        val res: WSResponse = await(fResponse)
        val doc = getDocFromResponse(res)

        res.status.shouldBe(OK)

        testCustomPartsOfGovWrapperElementsForFullConfigAllTrue(fResponse, "NAV_TITLE")

        for {
          l <- testCustomLookupPageJourneyConfigV2.config.labels
          en <- l.en
          international <- en.international
          lookupPage <- international.lookupPageLabels
        } yield {
          doc.title.shouldBe(lookupPage.title.get + " - NAV_TITLE - GOV.UK")
          doc.h1.text().shouldBe(lookupPage.heading.get)

          doc.getElementById("afterHeadingText").html.shouldBe("international-after-heading-text")

          doc.select("a[class=govuk-back-link]") should have(
            text("Back")
          )

          doc.input(LookupPage.filterId) should have(
            label(lookupPage.filterLabel.get + hardCodedFormHint),
            value(testFilterValue)
          )

          doc.link(LookupPage.manualAddressLink) should have(
            href(routes.InternationalAddressLookupController.edit(testJourneyId).url),
            text(lookupPage.manualAddressLinkText.get)
          )

          doc.submitButton.text().shouldBe(lookupPage.submitLabel.get)
        }
      }
    }

    "Provided with config where all the default values are overriden with the default values" should {
      "Render " in {
        val testJourneyId = UUID.randomUUID().toString
        await(cache.putV2(testJourneyId, testOtherCustomLookupPageJourneyConfigV2))

        val fResponse = buildClientLookupAddress(path = s"international/lookup?filter=$testFilterValue", testJourneyId)
          .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()

        val res: WSResponse = await(fResponse)
        val doc = getDocFromResponse(res)

        res.status.shouldBe(OK)

        testCustomPartsOfGovWrapperElementsForFullConfigWithAllTopConfigAsNoneAndAllBooleansFalse(fResponse)

        for {
          l <- testOtherCustomLookupPageJourneyConfigV2.config.labels
          en <- l.en
          international <- en.international
          lookupPage <- international.lookupPageLabels
        } yield {
          doc.title.shouldBe(lookupPage.title.get)
          doc.h1.text().shouldBe(lookupPage.heading.get)
          doc.getElementById("afterHeadingText").html.shouldBe("international-after-heading-text")
          doc.select("a[class=govuk-back-link]") should have(text("Back"))
          doc.input(LookupPage.filterId) should have(label(lookupPage.filterLabel.get + hardCodedFormHint), value(testFilterValue))
          doc.link(LookupPage.manualAddressLink) should have(
            href(routes.InternationalAddressLookupController.edit(testJourneyId).url),
            text(lookupPage.manualAddressLinkText.get)
          )

          doc.submitButton.text().shouldBe(lookupPage.submitLabel.get)
        }
      }
    }
  }

  "Show the default 'filter invalid' error messages" in {
    val testJourneyId = UUID.randomUUID().toString
    await(cache.putV2(testJourneyId, testMinimalLevelJourneyDataV2))

    val filterValue = longFilterValue
    val fResponse = buildClientLookupAddress(path = s"international/lookup", testJourneyId)
      .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRFAndLang(), "Csrf-Token" -> "nocheck")
      .post(Map("filter" -> filterValue))

    val res: WSResponse = await(fResponse)
    val doc = getDocFromResponse(res)

    res.status.shouldBe(BAD_REQUEST)

    val message = "Rhaid i enw/rhif eich tŷ fod yn llai na 256 o gymeriadau"

    doc.errorSummary should have(
      errorSummaryMessage(LookupPage.filterId, message)
    )

    doc.input(LookupPage.filterId) should have(
      errorMessage(s"Gwall: $message"),
      value(filterValue)
    )
  }
}
