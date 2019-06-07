package services

import config.WSHttp
import model._
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.libs.json.{JsValue, Json, Reads, Writes}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.cache.client.{CacheMap, HttpCaching}
import utils.TestConstants._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class KeystoreJourneyRepositorySpec extends PlaySpec with OneAppPerSuite with ScalaFutures {

  implicit val hc = HeaderCarrier()

  val journeyData = JourneyData(JourneyConfig("continue"))
  val someJourneyDataJson = Some(Json.toJson(journeyData))

  val journeyDataV2 = JourneyDataV2(JourneyConfigV2(2, JourneyOptions("continue")))
  val someJourneyDataV2Json = Some(Json.toJson(journeyDataV2))

  val journeyDataWithTimeout = JourneyData(JourneyConfig("continue", timeout = Some(Timeout(120,"testUrl"))))
  val someJourneyDataWithTimeoutJson = Some(Json.toJson(journeyDataWithTimeout))

  val cached = CacheMap("id", Map("id" -> Json.toJson(journeyData)))

  val cachedV2 = CacheMap("id", Map("id" -> Json.toJson(journeyDataV2)))

  val cachedWithTimeout = CacheMap("id", Map("id" -> Json.toJson(journeyDataWithTimeout)))

  class Scenario(cacheResponse: Option[CacheMap] = None, getResponse: Option[JsValue] = None) {

    val sessionCache = new HttpCaching {

      override def cache[A](source: String, cacheId: String, formId: String, body: A)(implicit wts: Writes[A], hc: HeaderCarrier, ec: ExecutionContext): Future[CacheMap] = {
        cacheResponse match {
          case Some(resp) => Future.successful(resp)
          case None => Future.failed(new Exception("Caching failed"))
        }
      }

      override def fetchAndGetEntry[T](source: String, cacheId: String, key: String)(implicit hc: HeaderCarrier, rds: Reads[T], ec: ExecutionContext): Future[Option[T]] = {
        getResponse match {
          case Some(resp) => Future.successful(Some(resp.as[T]))
          case None => Future.successful(None)
        }
      }

      override def baseUri = "http://localhost:9000/keystore"

      override def domain = "keystore"

      override def defaultSource = "address-lookup-frontend"

      override def http = WSHttp

    }

    val repo = new KeystoreJourneyRepository {

      override val cache = sessionCache

    }
  }

  "get" should {

    "fetch entry" in new Scenario(getResponse = someJourneyDataJson) {
      repo.get("any id").futureValue must be (Some(journeyData))
    }

    "fetch entry with a timeout" in new Scenario(getResponse = someJourneyDataWithTimeoutJson) {
      repo.get("any id").futureValue must be (Some(journeyDataWithTimeout))
    }


  }

  "getV2" should {

    "fetch entry as a v2 model" when {
      "stored as a v2 model" in new Scenario(getResponse = someJourneyDataV2Json) {
        repo.getV2("any id").futureValue must be (Some(journeyDataV2))
      }
      "stored as a v1 model" in new Scenario(getResponse = someJourneyDataJson) {
        repo.getV2("any id").futureValue must be (Some(repo.convertToV2Model(journeyData)))
      }
    }

  }

  "put" should {

    "cache given entry" in new Scenario(cacheResponse = Some(cached)) {
      repo.put("id", journeyData).futureValue must be (true)
    }

  }

  "putV2" should {
    "cache given entry" in new Scenario(cacheResponse = Some(cachedV2)) {
      repo.putV2("id", journeyDataV2).futureValue must be (true)
    }
  }

  "init" should {

    "know about continue URL" in new Scenario() {
      repo.init("j0").config.continueUrl must be ("/api/confirmed")
    }

    "know about homeNavRef" in new Scenario() {
      repo.init("j0").config.homeNavHref must be (Some("http://www.hmrc.gov.uk/"))
    }

    "know about navTitle" in new Scenario() {
      repo.init("j0").config.navTitle must be (Some("Address Lookup"))
    }

    "know about showBannerPhase" in new Scenario() {
      repo.init("j0").config.showPhaseBanner must be (Some(false))
    }

    "know about alphaPhase" in new Scenario() {
      repo.init("j0").config.alphaPhase must be (Some(false))
    }

    "know about lookup page title" in new Scenario() {
      repo.init("j0").config.lookupPage.get.title must be (Some("Lookup Address"))
    }

    "know about lookup page heading" in new Scenario() {
      repo.init("j0").config.lookupPage.get.heading must be (Some("Your Address"))
    }

    "know about lookup page filter label" in new Scenario() {
      repo.init("j0").config.lookupPage.get.filterLabel must be (Some("Building name or number"))
    }

    "know about lookup page postcode label" in new Scenario() {
      repo.init("j0").config.lookupPage.get.postcodeLabel must be (Some("Postcode"))
    }

    "know about lookup page submit label" in new Scenario() {
      repo.init("j0").config.lookupPage.get.submitLabel must be (Some("Find my address"))
    }

    "know about lookup page no results message" in new Scenario() {
      repo.init("j0").config.lookupPage.get.noResultsFoundMessage must be (Some("Sorry, we couldn't find anything for that postcode."))
    }

    "know about lookup page result limit exceeded message" in new Scenario() {
      repo.init("j0").config.lookupPage.get.resultLimitExceededMessage must be (Some("There were too many results. Please add additional details to limit the number of results."))
    }

    "know about select page title" in new Scenario() {
      repo.init("j0").config.selectPage.get.title must be (Some("Select Address"))
    }

    "know about select page heading" in new Scenario() {
      repo.init("j0").config.selectPage.get.heading must be (Some("Select Address"))
    }

    "know about select page proposal list labe" in new Scenario() {
      repo.init("j0").config.selectPage.get.proposalListLabel must be (Some("Please select one of the following addresses"))
    }

    "know about select page submit label" in new Scenario() {
      repo.init("j0").config.selectPage.get.submitLabel must be (Some("Next"))
    }

    "know about select page proposal list limit" in new Scenario() {
      repo.init("j0").config.selectPage.get.proposalListLimit must be (Some(50))
    }

    "know about confirm page title" in new Scenario() {
      repo.init("j0").config.confirmPage.get.title must be (Some("Confirm Address"))
    }

    "know about confirm page heading" in new Scenario() {
      repo.init("j0").config.confirmPage.get.heading must be (Some("Confirm Address"))
    }

    "know about confirm page info subheading" in new Scenario() {
      repo.init("j0").config.confirmPage.get.infoSubheading must be (Some("Your selected address"))
    }

    "know about confirm page info message" in new Scenario() {
      repo.init("j0").config.confirmPage.get.infoMessage must be (Some("This is how your address will look. Please double-check it and, if accurate, click on the <kbd>Confirm</kbd> button."))
    }

    "know about confirm page submit label" in new Scenario() {
      repo.init("j0").config.confirmPage.get.submitLabel must be (Some("Confirm"))
    }

    "know about edit page title" in new Scenario() {
      repo.init("j0").config.editPage.get.title must be (Some("Edit Address"))
    }

    "know about edit page heading" in new Scenario() {
      repo.init("j0").config.editPage.get.heading must be (Some("Edit Address"))
    }

    "know about edit page line 1 label" in new Scenario() {
      repo.init("j0").config.editPage.get.line1Label must be (Some("Line 1"))
    }

    "know about edit page line 2 label" in new Scenario() {
      repo.init("j0").config.editPage.get.line2Label must be (Some("Line 2"))
    }
    "know about edit page line 3 label" in new Scenario() {
      repo.init("j0").config.editPage.get.line3Label must be (Some("Line 3"))
    }

    "know about edit page town label" in new Scenario() {
      repo.init("j0").config.editPage.get.townLabel must be (Some("Town"))
    }

    "know about edit page postcode label" in new Scenario() {
      repo.init("j0").config.editPage.get.postcodeLabel must be (Some("Postcode"))
    }

    "know about edit page country label" in new Scenario() {
      repo.init("j0").config.editPage.get.countryLabel must be (Some("Country"))
    }

    "know about edit page submit label" in new Scenario() {
      repo.init("j0").config.editPage.get.submitLabel must be (Some("Next"))
    }

    "know about phase feedback link" in new Scenario() {
      repo.init("j0").config.phaseFeedbackLink must be (Some("#"))
    }

    "know about select page search again link option" in new Scenario() {
      repo.init("j0").config.selectPage.get.showSearchAgainLink must be (Some(true))
    }

    "know about select page search again link test" in new Scenario() {
      repo.init("j0").config.selectPage.get.searchAgainLinkText must be (Some("Search again"))
    }

    "know about confirm page search again link option" in new Scenario() {
      repo.init("j0").config.confirmPage.get.showSearchAgainLink must be (Some(true))
    }

    "know about confirm page search again link test" in new Scenario() {
      repo.init("j0").config.confirmPage.get.searchAgainLinkText must be (Some("Search again"))
    }

    "know about show back buttons option" in new Scenario() {
      repo.init("j0").config.showBackButtons must be (Some(true))
    }

    "know about HMRC branding option" in new Scenario() {
      repo.init("j0").config.includeHMRCBranding must be (Some(true))
    }

  }

  "Converting a v1 model to a v2 model" when {
    "all options set" should {
      "Create a valid v2 model" in new Scenario() {
        val actual = repo.convertToV2Model(fullV1JourneyData)
        val expected = fullV2JourneyData

        actual mustEqual expected
      }
    }

    "without a selected address" should {
      "return a V2 model without a selected address" in new Scenario() {
        val actual = repo.convertToV2Model(fullV1JourneyData.copy(selectedAddress = None))
        val expected = fullV2JourneyData.copy(selectedAddress = None)

        actual mustEqual expected
      }
    }

    "without a confirmed address" should {
      "return a V2 model without a confirmed address" in new Scenario() {
        val actual = repo.convertToV2Model(fullV1JourneyData.copy(confirmedAddress = None))
        val expected = fullV2JourneyData.copy(confirmedAddress = None)

        actual mustEqual expected
      }
    }

    "without a list of proposed addresses" should {
      "return a V2 model without a list of proposed addresses" in new Scenario() {
        val actual = repo.convertToV2Model(fullV1JourneyData.copy(proposals = None))
        val expected = fullV2JourneyData.copy(proposals = None)

        actual mustEqual expected
      }
    }

    "without lookup page config" should {
      "return a V2 model without lookup page config" in new Scenario() {
        val v1Config = fullV1JourneyConfig.copy(lookupPage = None)
        val v1JourneyData = fullV1JourneyData.copy(config = v1Config)
        val v2Config = fullV2JourneyConfig.copy(labels = Some(JourneyLabels(
          en = Some(fullV2LanguageLabelsEn.copy(lookupPageLabels = None))
        )))

        val actual = repo.convertToV2Model(v1JourneyData)
        val expected = fullV2JourneyData.copy(config = v2Config)

        actual mustEqual expected
      }
    }

    "without select page config" should {
      "return a V2 model without select page config" in new Scenario() {
        val v1Config = fullV1JourneyConfig.copy(selectPage = None)
        val v1JourneyData = fullV1JourneyData.copy(config = v1Config)
        val v2Config = fullV2JourneyConfig.copy(
          options = fullV2JourneyOptions.copy(selectPageConfig = None),
          labels = Some(JourneyLabels(
            en = Some(fullV2LanguageLabelsEn.copy(selectPageLabels = None))
          )))

        val actual = repo.convertToV2Model(v1JourneyData)
        val expected = fullV2JourneyData.copy(config = v2Config)

        actual mustEqual expected
      }
    }

    "without edit page config" should {
      "return a V2 model without edit page config" in new Scenario() {
        val v1Config = fullV1JourneyConfig.copy(editPage = None)
        val v1JourneyData = fullV1JourneyData.copy(config = v1Config)
        val v2Config = fullV2JourneyConfig.copy(
          labels = Some(JourneyLabels(
            en = Some(fullV2LanguageLabelsEn.copy(editPageLabels = None))
          )))

        val actual = repo.convertToV2Model(v1JourneyData)
        val expected = fullV2JourneyData.copy(config = v2Config)

        actual mustEqual expected
      }
    }

    "without confirm page config" should {
      "return a V2 model without confirm page config" in new Scenario() {
        val v1Config = fullV1JourneyConfig.copy(confirmPage = None)
        val v1JourneyData = fullV1JourneyData.copy(config = v1Config)
        val v2Config = fullV2JourneyConfig.copy(
          options = fullV2JourneyOptions.copy(confirmPageConfig = None),
          labels = Some(JourneyLabels(
            en = Some(fullV2LanguageLabelsEn.copy(confirmPageLabels = None))
          )))

        val actual = repo.convertToV2Model(v1JourneyData)
        val expected = fullV2JourneyData.copy(config = v2Config)

        actual mustEqual expected
      }
    }

    "without journey options" should {
      "return a V2 model with all journey options set to none" in new Scenario() {
        val v1Config = fullV1JourneyConfig.copy(
          continueUrl = "testUrl",
          homeNavHref = None,
          navTitle = None,
          additionalStylesheetUrl = None,
          deskProServiceName = None,
          showPhaseBanner = None,
          phaseFeedbackLink = None,
          phaseBannerHtml = None,
          includeHMRCBranding = None,
          alphaPhase = None,
          showBackButtons = None,
          ukMode = None,
          allowedCountryCodes = None)

        val v1JourneyData = fullV1JourneyData.copy(config = v1Config)
        val v2Config = fullV2JourneyConfig.copy(
          options = journeyOptionsMinimal.copy(
            selectPageConfig = fullV2SelectPageConfig,
            confirmPageConfig = fullV2ConfirmPageConfig,
            timeoutConfig = fullV2TimeoutConfig
          ),
          labels = Some(JourneyLabels(
            en = Some(fullV2LanguageLabelsEn.copy(appLevelLabels = None))
          )))

        val actual = repo.convertToV2Model(v1JourneyData)
        val expected = fullV2JourneyData.copy(config = v2Config)

        actual mustEqual expected
      }
    }

    "without language labels" should {
      "return a V2 model without language labels" in new Scenario() {
        val v1Config = fullV1JourneyConfig.copy(
          lookupPage = None,
          selectPage = None,
          editPage = None,
          confirmPage = None)

        val v1JourneyData = fullV1JourneyData.copy(config = v1Config)

        val v2Config = fullV2JourneyConfig.copy(
          options = fullV2JourneyOptions.copy(
            confirmPageConfig = None,
            selectPageConfig = None
          ),
          labels = Some(JourneyLabels(
            en = Some(fullV2LanguageLabelsEn.copy(
              lookupPageLabels = None,
              selectPageLabels = None,
              editPageLabels = None,
              confirmPageLabels = None
            ))
          )))

        val actual = repo.convertToV2Model(v1JourneyData)
        val expected = fullV2JourneyData.copy(config = v2Config)

        actual mustEqual expected
      }
    }
  }
}
