/*
 * Copyright 2024 HM Revenue & Customs
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

import play.api.{Configuration, Environment}
import play.api.i18n.Lang
import uk.gov.hmrc.hmrcfrontend.views.viewmodels.language.{Cy, En, Language}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}
import scala.collection.JavaConverters.asScalaBufferConverter
import scala.concurrent.duration.Duration

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
class FrontendAppConfig @Inject()(config: Configuration, servicesConfig: ServicesConfig, val environment: Environment) {
  private def loadConfig(key: String) = config.getOptional[String](key).getOrElse(throw new Exception(s"Missing configuration key: $key"))

  val appName: String = config.get[String]("appName")
  val cacheTtl: Duration = config.get[Duration]("mongodb.ttl")

  val contactFormServiceIdentifier = "AddressLookupFrontend"
  val homeUrl = "http://www.hmrc.gov.uk"
  val feedbackUrl = "https://www.tax.service.gov.uk/contact/beta-feedback-unauthenticated?service=ALF"
  val apiVersion2 = 2

  val addressLookupEndpoint = servicesConfig.baseUrl("address-lookup-frontend")
  val addressReputationEndpoint = servicesConfig.baseUrl("address-reputation")
  val allowedHosts = config.underlying.getStringList("microservice.hosts.allowList").asScala.toSet

  val languageMap: Map[String, Lang] = Map(
    "english" -> Lang("en"),
    "cymraeg" -> Lang("cy")
  )

  val footerLinkItems: Seq[String] = config.getOptional[Seq[String]]("footerLinkItems").getOrElse(Seq())

  lazy val analyticsToken = loadConfig(s"google-analytics.token")
  lazy val analyticsHost = loadConfig(s"google-analytics.host")
  lazy val reportAProblemPartialUrl = s"/contact/problem_reports_ajax?service=$contactFormServiceIdentifier"
  lazy val reportAProblemNonJSUrl = s"/contact/report-technical-problem?service=$contactFormServiceIdentifier"

  def buildReportAProblemPartialUrl(service: Option[String]): String = {
    s"/contact/problem_reports_ajax?service=${service.getOrElse(contactFormServiceIdentifier)}"
  }

  def buildReportAProblemNonJSUrl(service: Option[String]): String = {
    s"/contact/report-technical-problem?service=${service.getOrElse(contactFormServiceIdentifier)}"
  }

  def langToLanguage(langCode: String): Language = langCode match {
    case "en" => En
    case "cy" => Cy
    case _ => En
  }
}

object ALFCookieNames {
  val useWelsh = "Use-Welsh"
}
