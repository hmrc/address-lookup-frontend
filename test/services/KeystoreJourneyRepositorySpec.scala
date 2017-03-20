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

    "know about j0" in new Scenario() {
      repo.init("j0") must be (JourneyData(
        continueUrl = "/lookup-address/confirmed",
        homeNavHref = Some("http://www.hmrc.gov.uk/"),
        navTitle = Some("Address Lookup"),
        lookupPage = LookupPage(
          title = Some("Lookup Address"),
          heading = Some("Your Address"),
          filterLabel = Some("Building name or number"),
          postcodeLabel = Some("Postcode"),
          submitLabel = Some("Find my address"),
          noResultsFoundMessage = Some("Sorry, we couldn't find anything for that postcode."),
          resultLimitExceededMessage = Some("There were too many results. Please add additional details to limit the number of results.")
        ),
        selectPage = SelectPage(
          title = Some("Select Address"),
          heading = Some("Select Address"),
          proposalListLabel = Some("Please select one of the following addresses"),
          submitLabel = Some("Next"),
          proposalListLimit = Some(50)
        ),
        confirmPage = ConfirmPage(
          title = Some("Confirm Address"),
          heading = Some("Confirm Address"),
          infoSubheading =  Some("Your selected address"),
          infoMessage = Some("This is how your address will look. Please double-check it and, if accurate, click on the <kbd>Confirm</kbd> button."),
          submitLabel = Some("Confirm")
        )
      ))
    }

  }

}
