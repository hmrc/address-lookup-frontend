package services

import java.util.UUID
import javax.inject.Singleton

import com.google.inject.ImplementedBy
import com.typesafe.config.{ConfigObject, ConfigValue}
import config.AddressLookupFrontendSessionCache
import model._
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

  val cacheId = "journey-data"

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

  private def maybeInt(v: ConfigValue): Option[Int] = {
    if (v == null) None
    else Some(v.unwrapped().asInstanceOf[Int])
  }

  private def mustBeString(v: ConfigValue, key: String): String = {
    if (v == null) throw new IllegalArgumentException(s"$key must not be null")
    else v.unwrapped().toString
  }

  private def mustBeBoolean(v: ConfigValue, default: Boolean): Boolean = {
    if (v == null) default
    else v.unwrapped().asInstanceOf[Boolean]
  }

  // TODO ensure all potential config values are mapped
  private def journey(key: String, journeys: ConfigObject): JourneyData = {
    val j = journeys.get(key).asInstanceOf[ConfigObject]
    val l = Option(j.get("lookupPage").asInstanceOf[ConfigObject])
    val s = Option(j.get("selectPage").asInstanceOf[ConfigObject])
    val c = Option(j.get("confirmPage").asInstanceOf[ConfigObject])
    val e = Option(j.get("editPage").asInstanceOf[ConfigObject])
    val lookup = l match {
      case Some(l) => LookupPage(maybeString(l.get("title")), maybeString(l.get("heading")), maybeString(l.get("filterLabel")), maybeString(l.get("postcodeLabel")), maybeString(l.get("submitLabel")), maybeString(l.get("resultLimitExceededMessage")), maybeString(l.get("noResultsFoundMessage")), maybeString(l.get("manualAddress")))
      case None => LookupPage()
    }
    val select = s match {
      case Some(s) => SelectPage(maybeString(s.get("title")), maybeString(s.get("heading")), maybeString(s.get("proposalListLabel")), maybeString(s.get("submitLabel")), maybeInt(s.get("proposalListLimit")), mustBeBoolean(s.get("showSearchAgainLink"), false), maybeString(s.get("searchAgainLinkText")), maybeString(s.get("editAddressLinkText")))
      case None => SelectPage()
    }
    val confirm = c match {
      case Some(c) => ConfirmPage(maybeString(c.get("title")), maybeString(c.get("heading")), mustBeBoolean(c.get("showSubHeadingAndInfo"), false), maybeString(c.get("infoSubheading")), maybeString(c.get("infoMessage")), maybeString(c.get("submitLabel")), mustBeBoolean(c.get("showSearchAgainLink"), false), maybeString(c.get("searchAgainLinkText")))
      case None => ConfirmPage()
    }
    val edit = e match {
      case Some(e) => EditPage(maybeString(e.get("title")), maybeString(e.get("heading")), maybeString(e.get("line1Label")), maybeString(e.get("line2Label")), maybeString(e.get("line3Label")), maybeString(e.get("townLabel")), maybeString(e.get("postcodeLabel")), maybeString(e.get("countryLabel")), maybeString(e.get("submitLabel")), mustBeBoolean(e.get("showSearchAgainLink"), false), maybeString(e.get("searchAgainLinkText")))
      case None => EditPage()
    }
    JourneyData(
      continueUrl = mustBeString(j.get("continueUrl"), "continueUrl"),
      homeNavHref = maybeString(j.get("homeNavHref")),
      navTitle = maybeString(j.get("navTitle")),
      additionalStylesheetUrl = maybeString(j.get("additionalStylesheetUrl")),
      lookupPage = lookup,
      selectPage = select,
      confirmPage = confirm,
      editPage = edit,
      showPhaseBanner = mustBeBoolean(j.get("showPhaseBanner"), false),
      alphaPhase = mustBeBoolean(j.get("alphaPhase"), false),
      phaseFeedbackLink = maybeString(j.get("phaseFeedbackLink")),
      showBackButtons = mustBeBoolean(j.get("showBackButtons"), false)
    )
  }

}
