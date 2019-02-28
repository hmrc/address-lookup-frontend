package config

import com.typesafe.config.Config
import play.api.{Configuration, Play}
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.hooks.HttpHooks
import uk.gov.hmrc.play.audit.http.HttpAuditing
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.http.cache.client.HttpCaching
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector => Auditing}
import uk.gov.hmrc.play.config.AppName
import uk.gov.hmrc.play.http.ws.{WSDelete, WSGet, WSPost, WSPut}
import uk.gov.hmrc.http.{HttpDelete, HttpGet, HttpPut}
import uk.gov.hmrc.play.frontend.config.LoadAuditingConfig

object FrontendAuditConnector extends Auditing with AppName {
  override lazy val auditingConfig = LoadAuditingConfig(s"auditing")

  override protected def appNameConfiguration: Configuration = Play.current.configuration
}

trait Hooks extends HttpHooks with HttpAuditing {
  override val hooks = Seq(AuditingHook)
  override lazy val auditConnector: AuditConnector = FrontendAuditConnector
}

trait WSHttp extends HttpGet with WSGet with HttpPut with WSPut with HttpPost with WSPost with HttpDelete with WSDelete with Hooks with AppName {
  override protected def appNameConfiguration: Configuration = Play.current.configuration

  override protected def configuration: Option[Config] = Some(Play.current.configuration.underlying)
}
object WSHttp extends WSHttp


object AddressLookupFrontendSessionCache extends HttpCaching with AppName with FrontendServicesConfig {

  override def defaultSource: String = appName

  override def baseUri: String = baseUrl("keystore")

  override def domain: String = getConfString("cachable.session-cache.domain", throw new Exception(s"Could not find config 'cachable.session-cache.domain'"))

  override def http: HttpGet with HttpPut with HttpDelete = WSHttp

  override protected def appNameConfiguration: Configuration = Play.current.configuration
}
