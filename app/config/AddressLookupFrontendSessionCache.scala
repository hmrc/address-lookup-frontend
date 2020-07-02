package config

import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.http.cache.client.HttpCaching
import uk.gov.hmrc.play.bootstrap.http.HttpClient

@Singleton
class AddressLookupFrontendSessionCache @Inject()(val http: HttpClient, frontendAppConfig: FrontendAppConfig) extends HttpCaching {

  override def defaultSource: String = frontendAppConfig.appName

  override def baseUri: String = frontendAppConfig.keyStoreUri

  override def domain: String = frontendAppConfig.cacheDomain
}
