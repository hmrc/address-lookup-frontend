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

import address.ViewConfig
import com.typesafe.config.Config
import net.ceedubs.ficus.Ficus._
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.mvc.{EssentialFilter, Request}
import play.api.{Application, Configuration, Logger, Play}
import play.twirl.api.Html
import uk.gov.hmrc.crypto.ApplicationCrypto
import uk.gov.hmrc.logging.LoggerFacade
import uk.gov.hmrc.play.audit.filters.FrontendAuditFilter
import uk.gov.hmrc.play.config.{AppName, ControllerConfig, RunMode}
import uk.gov.hmrc.play.filters.MicroserviceFilterSupport
import uk.gov.hmrc.play.frontend.bootstrap.DefaultFrontendGlobal
import uk.gov.hmrc.play.http.logging.filters.FrontendLoggingFilter


object FrontendGlobal
  extends DefaultFrontendGlobal {

  override val auditConnector = FrontendAuditConnector
  override val loggingFilter = LoggingFilter
  override val frontendAuditFilter = AuditFilter

  override def onStart(app: Application) {
    super.onStart(app)
    ApplicationCrypto.verifyConfiguration()
  }

  override def standardErrorTemplate(pageTitle: String, heading: String, message: String)(implicit rh: Request[_]): Html = {
    // ideally, we would pass this value in
    val view = ViewConfig.alpha1.copy(pageTitle = pageTitle)
    views.html.error_template(view, heading, message)
  }

  override def microserviceMetricsConfig(implicit app: Application): Option[Configuration] = app.configuration.getConfig(s"microservice.metrics")

  val logger = new LoggerFacade(Logger.logger)

  lazy val whitelistFilter = {
    Play.configuration.getBoolean("enableWhitelist") match {
      case Some(true) => Seq(WhitelistFilter)
      case _ => Seq.empty
    }
  }

  override def filters: Seq[EssentialFilter] =  {
    whitelistFilter ++ super.filters
  }
}


object ControllerConfiguration extends ControllerConfig with CurrentApp {
  lazy val controllerConfigs: Config = app.configuration.underlying.as[Config]("controllers")
}


object LoggingFilter extends FrontendLoggingFilter with MicroserviceFilterSupport {
  override def controllerNeedsLogging(controllerName: String): Boolean = ControllerConfiguration.paramsForController(controllerName).needsLogging
}


object AuditFilter extends FrontendAuditFilter with RunMode with AppName with MicroserviceFilterSupport with CurrentApp {

  override lazy val maskedFormFields = Seq("password")

  override lazy val applicationPort = None

  override lazy val auditConnector = FrontendAuditConnector

  override def controllerNeedsAuditing(controllerName: String) = false
   // TODO should be -- ControllerConfiguration.paramsForController(controllerName).needsAuditing
}
