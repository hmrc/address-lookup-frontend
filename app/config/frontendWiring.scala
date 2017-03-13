package config

import java.util.UUID

import uk.gov.hmrc.http.cache.client.{HttpCaching, SessionCache}
import uk.gov.hmrc.play.audit.http.config.LoadAuditingConfig
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector => Auditing}
import uk.gov.hmrc.play.config.{AppName, RunMode, ServicesConfig}
import uk.gov.hmrc.play.http.{HttpDelete, HttpGet, HttpPut}
import uk.gov.hmrc.play.http.ws.{WSDelete, WSGet, WSPost, WSPut}

object FrontendAuditConnector extends Auditing with AppName {
  override lazy val auditingConfig = LoadAuditingConfig(s"auditing")
}

object WSHttp extends WSGet with WSPut with WSPost with WSDelete with AppName with RunMode {
  override val hooks = NoneRequired
}

object AddressLookupFrontendSessionCache extends HttpCaching with AppName with ServicesConfig {

  override def defaultSource: String = appName

  override def baseUri: String = baseUrl("keystore")

  override def domain: String = getConfString("cachable.session-cache.domain", throw new Exception(s"Could not find config 'cachable.session-cache.domain'"))

  override def http: HttpGet with HttpPut with HttpDelete = WSHttp

}
