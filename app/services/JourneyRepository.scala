package services

import java.util.UUID
import javax.inject.Singleton

import com.google.inject.ImplementedBy
import com.typesafe.config.{ConfigObject, ConfigValue}
import config.AddressLookupFrontendSessionCache
import model.{JourneyData, LookupPage}
import uk.gov.hmrc.http.cache.client.HttpCaching
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[KeystoreJourneyRepository])
trait JourneyRepository {

  def init(journeyName: String): JourneyData

  def get(id: String)(implicit hc: HeaderCarrier): Future[Option[JourneyData]]

  def put(id: String, data: JourneyData)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Boolean]

}

@Singleton
class KeystoreJourneyRepository extends JourneyRepository with ServicesConfig {

  val cacheId = UUID.randomUUID().toString

  private val cfg: Map[String, JourneyData] = config("address-lookup-frontend").getObject("journeys").map { journeys =>
    journeys.keySet().asScala.map { key =>
      (key -> journey(key, journeys))
    }.toMap
  }.getOrElse(Map.empty)

  val cache: HttpCaching = AddressLookupFrontendSessionCache

  override def init(journeyName: String): JourneyData = {
    try {
      cfg.get(journeyName).get
    } catch {
      case none: NoSuchElementException => throw new IllegalArgumentException(s"Invalid journey name: '$journeyName'", none)
    }
  }

  override def get(id: String)(implicit hc: HeaderCarrier): Future[Option[JourneyData]] = {
    cache.fetchAndGetEntry[JourneyData](cache.defaultSource, cacheId, id)
  }

  override def put(id: String, data: JourneyData)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Boolean] = {
    cache.cache(cache.defaultSource, cacheId, id, data).map { res =>
      true
    }
  }

  private def maybeString(v: ConfigValue): Option[String] = {
    if (v == null) None
    else Some(v.unwrapped().toString)
  }

  private def mustBeString(v: ConfigValue, key: String): String = {
    if (v == null) throw new IllegalArgumentException(s"$key must not be null")
    else v.unwrapped().toString
  }

  private def journey(key: String, journeys: ConfigObject): JourneyData = {
    val j = journeys.get(key).asInstanceOf[ConfigObject]
    val l = Option(j.get("lookupPage").asInstanceOf[ConfigObject])
    val lookup = l match {
      case Some(l) => LookupPage(maybeString(l.get("title")), maybeString(l.get("heading")), maybeString(l.get("filterLabel")), maybeString(l.get("postcodeLabel")), maybeString(l.get("submitLabel")))
      case None => LookupPage()
    }
    JourneyData(
      continueUrl = mustBeString(j.get("continueUrl"), "continueUrl"),
      lookupPage = lookup
    )
  }

}
