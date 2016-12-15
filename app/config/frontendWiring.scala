/*
 * Copyright 2016 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package config

import play.api.Play
import play.api.Play.{configuration, current}
import play.api.mvc.Call
import uk.gov.hmrc.http.cache.client.SessionCache
import uk.gov.hmrc.play.audit.http.config.LoadAuditingConfig
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector => Auditing}
import uk.gov.hmrc.play.config.{AppName, RunMode, ServicesConfig}
import uk.gov.hmrc.play.filters.MicroserviceFilterSupport
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import uk.gov.hmrc.play.http.ws.{WSDelete, WSGet, WSPost, WSPut}
import uk.gov.hmrc.whitelist.AkamaiWhitelistFilter

object FrontendAuditConnector extends Auditing with AppName {
  override lazy val auditingConfig = LoadAuditingConfig(s"auditing")
}


object WSHttp extends WSGet with WSPut with WSPost with WSDelete with AppName with RunMode {
  override val hooks = NoneRequired
}


object FrontendAuthConnector extends AuthConnector with ServicesConfig {
  val serviceUrl = baseUrl("auth")
  lazy val http = WSHttp
}


object FrontendSessionCache extends SessionCache with AppName with ServicesConfig {

  import ConfigHelper._

  override lazy val http = WSHttp
  override lazy val defaultSource = appName
  override lazy val baseUri = baseUrl("keystore")
  override lazy val domain = mustGetConfigString(configuration, "cachable.session-cache.domain")
}

object WhitelistFilter extends AkamaiWhitelistFilter with RunMode with MicroserviceFilterSupport {

  private def whitelistConfig(key: String): Seq[String] = Play.configuration.getString(key).getOrElse("").split(",").toSeq

  override def whitelist: Seq[String] = whitelistConfig("whitelist")

  override def excludedPaths: Seq[Call] = whitelistConfig("whitelistExcludedCalls").map {
    path => Call("GET", path)
  }

  override def destination: Call = Call("GET", "https://www.tax.service.gov.uk/")

}