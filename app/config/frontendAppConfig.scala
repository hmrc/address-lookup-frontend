package config

import javax.inject.{Inject, Singleton}
import play.api.{Configuration, Environment, Mode}
import play.api.i18n.Lang
import uk.gov.hmrc.play.config.ServicesConfig

trait AppConfig {
  val analyticsToken: String
  val analyticsHost: String
  val reportAProblemPartialUrl: String
  val reportAProblemNonJSUrl: String

  def languageMap: Map[String, Lang]

  def buildReportAProblemPartialUrl(service: Option[String]): String
  def buildReportAProblemNonJSUrl(service: Option[String]): String
}

@Singleton
class FrontendAppConfig @Inject()(val runModeConfiguration: Configuration, env: Environment) extends AppConfig with ServicesConfig {

  private def loadConfig(key: String) = runModeConfiguration.getString(key).getOrElse(throw new Exception(s"Missing configuration key: $key"))

  val appName = runModeConfiguration.getString("appName").get
  val contactFormServiceIdentifier = "AddressLookupFrontend"
  val homeUrl = "http://www.hmrc.gov.uk"
  val feedbackUrl = "https://www.tax.service.gov.uk/contact/beta-feedback-unauthenticated?service=ALF"
  val apiVersion2 = 2
  val addressLookupEndpoint = baseUrl("address-lookup-frontend")

  val languageMap: Map[String, Lang] = Map(
    "english" -> Lang("en"),
    "cymraeg" -> Lang("cy")
  )

  override def mode: Mode.Mode = env.mode

  def keyStoreUri: String = baseUrl("keystore")

  def cacheDomain: String = getConfString("cachable.session-cache.domain", throw new Exception(s"Could not find config 'cachable.session-cache.domain'"))

  override lazy val analyticsToken = loadConfig(s"google-analytics.token")
  override lazy val analyticsHost = loadConfig(s"google-analytics.host")
  override lazy val reportAProblemPartialUrl = s"/contact/problem_reports_ajax?service=$contactFormServiceIdentifier"
  override lazy val reportAProblemNonJSUrl = s"/contact/problem_reports_nonjs?service=$contactFormServiceIdentifier"

  def buildReportAProblemPartialUrl(service: Option[String]): String = {
    s"/contact/problem_reports_ajax?service=${service.getOrElse(contactFormServiceIdentifier)}"
  }

  def buildReportAProblemNonJSUrl(service: Option[String]): String = {
    s"/contact/problem_reports_nonjs?service=${service.getOrElse(contactFormServiceIdentifier)}"
  }
}

object ALFCookieNames {
  val useWelsh = "Use-Welsh"
}


