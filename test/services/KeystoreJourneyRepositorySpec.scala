package services

import config.WSHttp
import model._
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.libs.json.{Json, Reads, Writes}
import uk.gov.hmrc.http.cache.client.{CacheMap, HttpCaching, SessionCache}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import uk.gov.hmrc.http.HeaderCarrier

class KeystoreJourneyRepositorySpec extends PlaySpec with OneAppPerSuite with ScalaFutures {

  implicit val hc = HeaderCarrier()

  val someJourneyData = Some(JourneyData(JourneyConfig("continue")))

  val someJourneyDataWithTimeout = Some(JourneyData(JourneyConfig("continue", timeout = Some(Timeout(120,"testUrl")))))

  val cached = CacheMap("id", Map("id" -> Json.toJson(someJourneyData)))

  val cachedWithTimeout = CacheMap("id", Map("id" -> Json.toJson(someJourneyDataWithTimeout)))

  class Scenario(cacheResponse: Option[CacheMap] = None, getResponse: Option[JourneyData] = None) {

    val sessionCache = new HttpCaching {

      override def cache[A](source: String, cacheId: String, formId: String, body: A)(implicit wts: Writes[A], hc: HeaderCarrier, ec: ExecutionContext): Future[CacheMap] = {
        cacheResponse match {
          case Some(resp) => Future.successful(resp)
          case None => Future.failed(new Exception("Caching failed"))
        }
      }

      override def fetchAndGetEntry[T](source: String, cacheId: String, key: String)(implicit hc: HeaderCarrier, rds: Reads[T], ec: ExecutionContext): Future[Option[T]] = {
        getResponse match {
          case Some(resp) => Future.successful(Some(resp.asInstanceOf[T]))
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

    "fetch entry" in new Scenario(getResponse = someJourneyData) {
      repo.get("any id").futureValue must be (someJourneyData)
    }

    "fetch entry with a timeout" in new Scenario(getResponse = someJourneyDataWithTimeout) {
      repo.get("any id").futureValue must be (someJourneyDataWithTimeout)
    }


  }

  "put" should {

    "cache given entry" in new Scenario(cacheResponse = Some(cached)) {
      repo.put("id", someJourneyData.get).futureValue must be (true)
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

}
