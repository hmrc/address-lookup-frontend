package config

import akka.actor.ActorSystem
import com.typesafe.config.Config
import javax.inject.{Inject, Singleton}
import play.api.{Configuration, Environment, Mode, Play}
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.hooks.HttpHooks
import uk.gov.hmrc.play.audit.http.HttpAuditing
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.http.cache.client.HttpCaching
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector ⇒ Auditing}
import uk.gov.hmrc.play.config.{AppName, RunMode, ServicesConfig}
import uk.gov.hmrc.play.http.ws.{WSDelete, WSGet, WSPost, WSPut}
import uk.gov.hmrc.http.{HttpDelete, HttpGet, HttpPut}
import uk.gov.hmrc.play.bootstrap.http.{DefaultHttpClient, HttpClient}
//import uk.gov.hmrc.play.frontend.config.LoadAuditingConfig

//object FrontendAuditConnector extends Auditing with AppName {
////  override lazy val auditingConfig = LoadAuditingConfig(s"auditing")
//
//  override protected def appNameConfiguration: Configuration = Play.current.configuration
//}

//trait Hooks extends HttpHooks with HttpAuditing {
//  override val hooks = Seq(AuditingHook)
//  override lazy val auditConnector: AuditConnector = FrontendAuditConnector
//}

//trait WSHttp extends HttpGet with WSGet with HttpPut with WSPut with HttpPost with WSPost with HttpDelete with WSDelete with Hooks with AppName {
//  override protected def appNameConfiguration: Configuration = Play.current.configuration
//
//  override protected def configuration: Option[Config] = Some(Play.current.configuration.underlying)
//
//  override protected def actorSystem: ActorSystem = Play.current.actorSystem
//}
//object WSHttp extends WSHttp

@Singleton
class AddressLookupFrontendSessionCache @Inject()(val http: HttpClient, frontendAppConfig: FrontendAppConfig) extends HttpCaching {

  override def defaultSource: String = frontendAppConfig.appName

  override def baseUri: String = frontendAppConfig.keyStoreUri

  override def domain: String = frontendAppConfig.cacheDomain
}
