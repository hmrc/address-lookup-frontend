package services

import config.WSHttp
import model.{ConfirmPage, JourneyData, LookupPage, SelectPage}
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.libs.json.{Json, Reads, Writes}
import uk.gov.hmrc.http.cache.client.{CacheMap, HttpCaching, SessionCache}
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class KeystoreJourneyRepositorySpec extends PlaySpec with OneAppPerSuite with ScalaFutures {

  implicit val hc = HeaderCarrier()

  val someJourneyData = Some(JourneyData("continue"))

  val cached = CacheMap("id", Map("id" -> Json.toJson(someJourneyData)))

  class Scenario(cacheResponse: Option[CacheMap] = None, getResponse: Option[JourneyData] = None) {

    val sessionCache = new HttpCaching {

      override def cache[A](source: String, cacheId: String, formId: String, body: A)(implicit wts: Writes[A], hc: HeaderCarrier): Future[CacheMap] = {
        cacheResponse match {
          case Some(resp) => Future.successful(resp)
          case None => Future.failed(new Exception("Caching failed"))
        }
      }

      override def fetchAndGetEntry[T](source: String, cacheId: String, key: String)(implicit hc: HeaderCarrier, rds: Reads[T]): Future[Option[T]] = {
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

  }

  "put" should {

    "cache given entry" in new Scenario(cacheResponse = Some(cached)) {
      repo.put("id", someJourneyData.get).futureValue must be (true)
    }

  }

  "init" should {

    "know about continue URL" in new Scenario() {
      repo.init("j0").continueUrl must be ("/api/confirmed")
    }

    "know about homeNavRef" in new Scenario() {
      repo.init("j0").homeNavHref must be (Some("http://www.hmrc.gov.uk/"))
    }

    "know about navTitle" in new Scenario() {
      repo.init("j0").navTitle must be (Some("Address Lookup"))
    }

    "know about showBannerPhase" in new Scenario() {
      repo.init("j0").showPhaseBanner must be (false)
    }

    "know about alphaPhase" in new Scenario() {
      repo.init("j0").alphaPhase must be (false)
    }

    "know about lookup page title" in new Scenario() {
      repo.init("j0").lookupPage.title must be (Some("Lookup Address"))
    }

    "know about lookup page heading" in new Scenario() {
      repo.init("j0").lookupPage.heading must be (Some("Your Address"))
    }

    "know about lookup page filter label" in new Scenario() {
      repo.init("j0").lookupPage.filterLabel must be (Some("Building name or number"))
    }

    "know about lookup page postcode label" in new Scenario() {
      repo.init("j0").lookupPage.postcodeLabel must be (Some("Postcode"))
    }

    "know about lookup page submit label" in new Scenario() {
      repo.init("j0").lookupPage.submitLabel must be (Some("Find my address"))
    }

    "know about lookup page no results message" in new Scenario() {
      repo.init("j0").lookupPage.noResultsFoundMessage must be (Some("Sorry, we couldn't find anything for that postcode."))
    }

    "know about lookup page result limit exceeded message" in new Scenario() {
      repo.init("j0").lookupPage.resultLimitExceededMessage must be (Some("There were too many results. Please add additional details to limit the number of results."))
    }

    "know about select page title" in new Scenario() {
      repo.init("j0").selectPage.title must be (Some("Select Address"))
    }

    "know about select page heading" in new Scenario() {
      repo.init("j0").selectPage.heading must be (Some("Select Address"))
    }

    "know about select page proposal list labe" in new Scenario() {
      repo.init("j0").selectPage.proposalListLabel must be (Some("Please select one of the following addresses"))
    }

    "know about select page submit label" in new Scenario() {
      repo.init("j0").selectPage.submitLabel must be (Some("Next"))
    }

    "know about select page proposal list limit" in new Scenario() {
      repo.init("j0").selectPage.proposalListLimit must be (Some(50))
    }

    "know about confirm page title" in new Scenario() {
      repo.init("j0").confirmPage.title must be (Some("Confirm Address"))
    }

    "know about confirm page heading" in new Scenario() {
      repo.init("j0").confirmPage.heading must be (Some("Confirm Address"))
    }

    "know about confirm page info subheading" in new Scenario() {
      repo.init("j0").confirmPage.infoSubheading must be (Some("Your selected address"))
    }

    "know about confirm page info message" in new Scenario() {
      repo.init("j0").confirmPage.infoMessage must be (Some("This is how your address will look. Please double-check it and, if accurate, click on the <kbd>Confirm</kbd> button."))
    }

    "know about confirm page submit label" in new Scenario() {
      repo.init("j0").confirmPage.submitLabel must be (Some("Confirm"))
    }

    "know about edit page title" in new Scenario() {
      repo.init("j0").editPage.title must be (Some("Edit Address"))
    }

    "know about edit page heading" in new Scenario() {
      repo.init("j0").editPage.heading must be (Some("Edit Address"))
    }

    "know about edit page line 1 label" in new Scenario() {
      repo.init("j0").editPage.line1Label must be (Some("Line 1"))
    }

    "know about edit page line 2 label" in new Scenario() {
      repo.init("j0").editPage.line2Label must be (Some("Line 2"))
    }

    "know about edit page line 3 label" in new Scenario() {
      repo.init("j0").editPage.line3Label must be (Some("Line 3"))
    }

    "know about edit page town label" in new Scenario() {
      repo.init("j0").editPage.townLabel must be (Some("Town"))
    }

    "know about edit page postcode label" in new Scenario() {
      repo.init("j0").editPage.postcodeLabel must be (Some("Postcode"))
    }

    "know about edit page country label" in new Scenario() {
      repo.init("j0").editPage.countryLabel must be (Some("Country"))
    }

    "know about edit page submit label" in new Scenario() {
      repo.init("j0").editPage.submitLabel must be (Some("Next"))
    }

  }

}
