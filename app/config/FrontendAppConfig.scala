/*
 * Copyright 2025 HM Revenue & Customs
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

import play.api.i18n.Lang
import play.api.{Configuration, Environment}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.Duration
import scala.jdk.CollectionConverters._

@Singleton
class FrontendAppConfig @Inject()(config: Configuration, servicesConfig: ServicesConfig, val environment: Environment) {

  val appName: String = config.get[String]("appName")
  val cacheTtl: Duration = config.get[Duration]("mongodb.ttl")

  val contactFormServiceIdentifier = "AddressLookupFrontend"
  val feedbackUrl = "https://www.tax.service.gov.uk/contact/beta-feedback-unauthenticated?service=ALF"

  val addressLookupEndpoint: String = servicesConfig.baseUrl("address-lookup-frontend")
  val addressReputationEndpoint: String = servicesConfig.baseUrl("address-reputation")
  val allowedHosts: Set[String] = config.underlying.getStringList("microservice.hosts.allowList").asScala.toSet
  val showNoneOfTheseOptionOnSelectPage: Boolean = config.get[Boolean]("microservice.selectPageConfig.showNoneOfTheseOption")
  val newGovUkServiceNavigationEnabled: Boolean = config.get[Boolean]("microservice.newGovUkServiceNavigationEnabled")

  val languageMap: Map[String, Lang] = Map(
    "english" -> Lang("en"),
    "cymraeg" -> Lang("cy")
  )
}

object ALFCookieNames {
  val useWelsh = "Use-Welsh"
}
