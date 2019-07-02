package services

import javax.inject.Singleton
import com.google.inject.ImplementedBy
import com.typesafe.config.{ConfigObject, ConfigValue}
import config.{AddressLookupFrontendSessionCache, FrontendServicesConfig}
import model._
import play.api.libs.json.JsValue
import uk.gov.hmrc.http.cache.client.HttpCaching

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}
import uk.gov.hmrc.http.HeaderCarrier
import utils.V2ModelConverter._

@ImplementedBy(classOf[KeystoreJourneyRepository])
trait JourneyRepository {

  def init(journeyName: String): JourneyData

  def initV2(journeyName: String): JourneyDataV2

  def get(id: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[JourneyData]]

  def getV2(id: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[JourneyDataV2]]

  def put(id: String, data: JourneyData)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Boolean]

  def putV2(id: String, data: JourneyDataV2)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Boolean]

}

@Singleton
class KeystoreJourneyRepository extends JourneyRepository with FrontendServicesConfig {

  val cacheId = "journey-data"

  private val cfg: Map[String, JourneyData] = config("address-lookup-frontend").getObject("journeys").map { journeys =>
    journeys.keySet().asScala.map { key =>
      (key -> journey(key, journeys))
    }.toMap
  }.getOrElse(Map.empty)

  private val journeyConfigAsV2: Map[String, JourneyDataV2] =
    config("address-lookup-frontend")
      .getObject("journeys")
      .map (journeys => journeys.keySet().asScala
        .map (key => (key -> convertToV2Model(journey(key, journeys))))
        .toMap
      )
      .getOrElse(Map.empty)

  val cache: HttpCaching = AddressLookupFrontendSessionCache

  override def init(journeyName: String): JourneyData = {
    try {
      cfg.get(journeyName).get
    } catch {
      case none: NoSuchElementException => throw new IllegalArgumentException(s"Invalid journey name: '$journeyName'", none)
    }
  }

  override def initV2(journeyName: String): JourneyDataV2 =
    journeyConfigAsV2(journeyName) match {
      case foundJourney: JourneyDataV2 => foundJourney
      case failure: IllegalArgumentException =>
        throw new IllegalArgumentException(s"Invalid journey name: $journeyName", failure)
    }

  override def get(id: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[JourneyData]] = {
      cache.fetchAndGetEntry[JourneyData](cache.defaultSource, cacheId, id)
  }

  override def getV2(id: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[JourneyDataV2]] = {
    cache.fetchAndGetEntry[JsValue](cache.defaultSource, cacheId, id).map(_.map(json =>
      (json \ "config" \ "version").asOpt[Int] match {
        case Some(_) => json.as[JourneyDataV2]
        case None => convertToV2Model(json.as[JourneyData])
      }
    ))
  }

  override def put(id: String, data: JourneyData)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Boolean] = {
    cache.cache(cache.defaultSource, cacheId, id, data).map { res =>
      true
    }
  }

  override def putV2(id: String, data: JourneyDataV2)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Boolean] =
    cache.cache(cache.defaultSource, cacheId, id, data) map (_ => true)

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

  private def maybeBoolean(v: ConfigValue, default: Boolean): Option[Boolean] = {
    if (v == null) Some(default)
    else Some(v.unwrapped().asInstanceOf[Boolean])
  }

  private def maybeSetOfStrings(v: ConfigValue, key: String): Option[Set[String]] = {
    if (v == null) None
    else v.unwrapped() match {
      case list: java.util.List[_] => Some(list.asScala.map(_.toString).toSet)
      case item: String => Some(Set(item))
      case _ => throw new IllegalArgumentException(s"$key must be a list of strings")
    }
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
      case Some(s) => SelectPage(maybeString(s.get("title")), maybeString(s.get("heading")), maybeString(s.get("headingWithPostcode")), maybeString(s.get("proposalListLabel")), maybeString(s.get("submitLabel")), maybeInt(s.get("proposalListLimit")), maybeBoolean(s.get("showSearchAgainLink"), false), maybeString(s.get("searchAgainLinkText")), maybeString(s.get("editAddressLinkText")))
      case None => SelectPage()
    }
    val confirm = c match {
      case Some(c) => ConfirmPage(maybeString(c.get("title")), maybeString(c.get("heading")), maybeBoolean(c.get("showSubHeadingAndInfo"), false), maybeString(c.get("infoSubheading")), maybeString(c.get("infoMessage")), maybeString(c.get("submitLabel")), maybeBoolean(c.get("showSearchAgainLink"), false), maybeString(c.get("searchAgainLinkText")), maybeBoolean(c.get("showChangeLink"), true), maybeString(c.get("changeLinkText")))
      case None => ConfirmPage()
    }
    val edit = e match {
      case Some(e) => EditPage(maybeString(e.get("title")), maybeString(e.get("heading")), maybeString(e.get("line1Label")), maybeString(e.get("line2Label")), maybeString(e.get("line3Label")), maybeString(e.get("townLabel")), maybeString(e.get("postcodeLabel")), maybeString(e.get("countryLabel")), maybeString(e.get("submitLabel")))
      case None => EditPage()
    }
    JourneyData(
      config = JourneyConfig(
        continueUrl = mustBeString(j.get("continueUrl"), "continueUrl"),
        homeNavHref = maybeString(j.get("homeNavHref")),
        navTitle = maybeString(j.get("navTitle")),
        additionalStylesheetUrl = maybeString(j.get("additionalStylesheetUrl")),
        lookupPage = Some(lookup),
        selectPage = Some(select),
        confirmPage = Some(confirm),
        editPage = Some(edit),
        showPhaseBanner = maybeBoolean(j.get("showPhaseBanner"), false),
        alphaPhase = maybeBoolean(j.get("alphaPhase"), false),
        phaseFeedbackLink = maybeString(j.get("phaseFeedbackLink")),
        phaseBannerHtml = maybeString(j.get("phaseBannerHtml")),
        showBackButtons = maybeBoolean(j.get("showBackButtons"), false),
        includeHMRCBranding = maybeBoolean(j.get("includeHMRCBranding"), true),
        deskProServiceName = maybeString(j.get("deskProServiceName")),
        allowedCountryCodes = maybeSetOfStrings(j.get("allowedCountryCodes"), "allowedCountryCodes")
      )
    )
  }

}
