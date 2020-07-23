/*
 * Copyright 2020 HM Revenue & Customs
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

import javax.inject.{Inject, Singleton}
import play.api.{Configuration, Environment, Mode}
import play.api.i18n.Lang
import uk.gov.hmrc.hmrcfrontend.views.viewmodels.language.{Cy, En, Language}
import uk.gov.hmrc.play.bootstrap.config.{RunMode, ServicesConfig}

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
class FrontendAppConfig @Inject()(runModeConfiguration: Configuration, runMode: RunMode) extends ServicesConfig(runModeConfiguration, runMode) with AppConfig {

  private def loadConfig(key: String) = runModeConfiguration.getString(key).getOrElse(throw new Exception(s"Missing configuration key: $key"))

  val appName = runModeConfiguration.getString("appName").get
  val contactFormServiceIdentifier = "AddressLookupFrontend"
  val homeUrl = "http://www.hmrc.gov.uk"
  val feedbackUrl = "https://www.tax.service.gov.uk/contact/beta-feedback-unauthenticated?service=ALF"
  val apiVersion2 = 2
  val addressLookupEndpoint = baseUrl("address-lookup-frontend")
  val addressReputationEndpoint = baseUrl("address-reputation")

  val languageMap: Map[String, Lang] = Map(
    "english" -> Lang("en"),
    "cymraeg" -> Lang("cy")
  )

  val footerLinkItems: Seq[String] = runModeConfiguration.getOptional[Seq[String]]("footerLinkItems").getOrElse(Seq())

  //  override def mode: RunMode = environment.mode

  override def config(serviceName: String): Configuration = super.config(serviceName)

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

  def langToLanguage(langCode: String): Language = langCode match {
    case "en" ⇒ En
    case "cy" ⇒ Cy
    case _ => En
  }
}

object ALFCookieNames {
  val useWelsh = "Use-Welsh"
}


