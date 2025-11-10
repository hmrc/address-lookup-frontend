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

package services

import itutil.config.IntegrationTestConstants._
import model.v2.JourneyDataV2
import org.apache.pekko.util.Timeout
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.Helpers.await
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.mongo.cache.CacheIdType.SimpleCacheId
import uk.gov.hmrc.mongo.cache.{DataKey, MongoCacheRepository}
import uk.gov.hmrc.mongo.{CurrentTimestampSupport, MongoComponent}

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{Duration, FiniteDuration}

class JourneyRepositoryISpec extends AnyWordSpec with ScalaFutures with Matchers with GuiceOneAppPerSuite {

  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val timeout: Timeout = Timeout(FiniteDuration(5, "seconds"))

  class JourneyDataV2Cache @Inject()(mongoComponent: MongoComponent) extends MongoCacheRepository(
    mongoComponent,
    "address-lookup-frontend",
    true,
    Duration("5 minutes"),
    new CurrentTimestampSupport(),
    SimpleCacheId
  )

  val cache = new JourneyDataV2Cache(app.injector.instanceOf[MongoComponent])


  "getV2" should {
    "return a v2 model" when {
      "a v2 model is stored" in {
        val testJourneyId = UUID.randomUUID().toString
        //                             record key             key in data      data in data
        // record key: 123456789
        // record data:
        // {"Jid123": {"config": {"version": new NumberInt("2"), "options": {"continueUrl": "testContinueUrl", "homeNavHref": "tesNavtHref", "phaseFeedbackLink": "testFeedbackLink", "deskProServiceName": "testDeskproName", "showPhaseBanner": true, "alphaPhase": true, "showBackButtons": true, "includeHMRCBranding": true, "ukMode": true, "allowedCountryCodes": ["UK", "FR"], "selectPageConfig": {"proposalListLimit": new NumberInt("30"), "showSearchAgainLink": true}, "confirmPageConfig": {"showSearchAgainLink": true, "showSubHeadingAndInfo": true, "showChangeLink": true, "showConfirmChangeText": true}, "timeoutConfig": {"timeoutAmount": new NumberInt("120"), "timeoutUrl": "testTimeoutUrl", "timeoutKeepAliveUrl": "testTimeoutKeepAliveUrl"}}, "labels": {"en": {"appLevelLabels": {"navTitle": "enNavTitle", "phaseBannerHtml": "enPhaseBannerHtml"}, "selectPageLabels": {"title": "enSelectPageTitle", "heading": "enSelectPageHeading", "headingWithPostcode": "enSelectPageHeadingWithPostcode", "proposalListLabel": "enProposalListLabel", "submitLabel": "enSubmitLabel", "searchAgainLinkText": "enSearchAgainLinkText", "editAddressLinkText": "enEditAddressLinkText"}, "lookupPageLabels": {"title": "enLookupPageTitle", "titleUkMode": "enLookupPageTitle", "heading": "enLookupPageHeading", "headingUkMode": "enLookupPageHeading", "filterLabel": "enFilterLabel", "postcodeLabel": "enPostcodeLabel", "postcodeLabelUkMode": "enPostcodeLabel", "submitLabel": "enSubmitLabel", "noResultsFoundMessage": "enNoResultsFoundMessage", "resultLimitExceededMessage": "enResultLimitExceededMessage", "manualAddressLinkText": "enManualAddressLinkText"}, "editPageLabels": {"title": "enEditPageTitle", "heading": "enEditPageHeading", "line1Label": "enEditPageLine1Label", "line2Label": "enEditPageLine2Label", "line3Label": "enEditPageLine3Label", "townLabel": "enEditPageTownLabel", "postcodeLabel": "enEditPagePostcodeLabel", "postcodeLabelUkMode": "enEditPagePostcodeLabel", "countryLabel": "enEditPageCountryLabel", "submitLabel": "enEditPageSubmitLabel"}, "confirmPageLabels": {"title": "enConfirmPageTitle", "heading": "enConfirmPageHeading", "infoSubheading": "enConfirmPageInfoSubheading", "infoMessage": "enConfirmPageInfoMessage", "submitLabel": "enConfirmPageSubmitLabel", "searchAgainLinkText": "enConfirmPageSearchAgainLinkText", "changeLinkText": "enConfirmPageChangeLinkText", "confirmChangeText": "enConfirmPageConfirmChangeText"}}, "cy": {"appLevelLabels": {"navTitle": "cyNavTitle", "phaseBannerHtml": "cyPhaseBannerHtml"}, "selectPageLabels": {"title": "cySelectPageTitle", "heading": "cySelectPageHeading", "headingWithPostcode": "cySelectPageHeadingWithPostcode", "proposalListLabel": "cyProposalListLabel", "submitLabel": "cySubmitLabel", "searchAgainLinkText": "cySearchAgainLinkText", "editAddressLinkText": "cyEditAddressLinkText"}, "lookupPageLabels": {"title": "cyLookupPageTitle", "titleUkMode": "cyLookupPageTitle", "heading": "cyLookupPageHeading", "headingUkMode": "cyLookupPageHeading", "filterLabel": "cyFilterLabel", "postcodeLabel": "cyPostcodeLabel", "postcodeLabelUkMode": "cyPostcodeLabel", "submitLabel": "cySubmitLabel", "noResultsFoundMessage": "cyNoResultsFoundMessage", "resultLimitExceededMessage": "cyResultLimitExceededMessage", "manualAddressLinkText": "cyManualAddressLinkText"}, "editPageLabels": {"title": "cyEditPageTitle", "heading": "cyEditPageHeading", "line1Label": "cyEditPageLine1Label", "line2Label": "cyEditPageLine2Label", "line3Label": "cyEditPageLine3Label", "townLabel": "cyEditPageTownLabel", "postcodeLabel": "cyEditPagePostcodeLabel", "postcodeLabelUkMode": "cyEditPagePostcodeLabel", "countryLabel": "cyEditPageCountryLabel", "submitLabel": "cyEditPageSubmitLabel"}, "confirmPageLabels": {"title": "cyConfirmPageTitle", "heading": "cyConfirmPageHeading", "infoSubheading": "cyConfirmPageInfoSubheading", "infoMessage": "cyConfirmPageInfoMessage", "submitLabel": "cyConfirmPageSubmitLabel", "searchAgainLinkText": "cyConfirmPageSearchAgainLinkText", "changeLinkText": "cyConfirmPageChangeLinkText", "confirmChangeText": "cyConfirmPageConfirmChangeText"}}}}}}
        await(cache.put[JourneyDataV2](testJourneyId)(DataKey("journey-data"), journeyDataV2Full))
        await(cache.get[JourneyDataV2](testJourneyId)(DataKey("journey-data"))) shouldBe Some(journeyDataV2Full)
      }
    }
  }
}
