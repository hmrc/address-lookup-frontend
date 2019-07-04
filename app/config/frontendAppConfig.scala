package config

import play.api.Play.{configuration, current}
import play.api.i18n.Lang

trait AppConfig {
  val analyticsToken: String
  val analyticsHost: String
  val reportAProblemPartialUrl: String
  val reportAProblemNonJSUrl: String

  def languageMap: Map[String, Lang]

  def buildReportAProblemPartialUrl(service: Option[String]): String
  def buildReportAProblemNonJSUrl(service: Option[String]): String
}

object FrontendAppConfig extends AppConfig with FrontendServicesConfig {

  private def loadConfig(key: String) = configuration.getString(key).getOrElse(throw new Exception(s"Missing configuration key: $key"))

  val contactFormServiceIdentifier = "AddressLookupFrontend"
  val homeUrl = "http://www.hmrc.gov.uk"
  val feedbackUrl = "https://www.tax.service.gov.uk/contact/beta-feedback-unauthenticated?service=ALF"
  val apiVersion2 = 2

  val languageMap: Map[String, Lang] = Map(
    "english" -> Lang("en"),
    "cymraeg" -> Lang("cy")
  )

  object ALFCookieNames {
    val useWelsh = "Use-Welsh"
  }

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
