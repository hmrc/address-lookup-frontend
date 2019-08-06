
package config

import com.typesafe.config.Config
import config.FrontendAppConfig.ALFCookieNames
import model.MessageConstants.{EnglishMessageConstants => englishContent, WelshMessageConstants => welshContent}
import net.ceedubs.ficus.Ficus._
import play.api.mvc.Request
import play.api.{Application, Configuration, Play}
import play.twirl.api.Html
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import uk.gov.hmrc.crypto.ApplicationCrypto
import uk.gov.hmrc.play.config.{AppName, ControllerConfig}
import uk.gov.hmrc.play.frontend.bootstrap.DefaultFrontendGlobal
import uk.gov.hmrc.play.frontend.filters.{FrontendAuditFilter, FrontendLoggingFilter, MicroserviceFilterSupport}

object FrontendGlobal extends DefaultFrontendGlobal {

  override val auditConnector = FrontendAuditConnector
  override val loggingFilter = LoggingFilter
  override val frontendAuditFilter = AuditFilter

  override def onStart(app: Application) {
    super.onStart(app)

    val applicationCrypto = new ApplicationCrypto(app.configuration.underlying)
    applicationCrypto.verifyConfiguration()
  }

  override def standardErrorTemplate(pageTitle: String, heading: String, message: String)(implicit rh: Request[_]): Html = {
    val optWelshContentCookie = rh.cookies.get(ALFCookieNames.useWelsh)

    val langSpecificMessages = optWelshContentCookie collect {
      case welshCookie if welshCookie.value.toBoolean == true => welshContent
    } getOrElse(englishContent)

    views.html.error_template(
      pageTitle = langSpecificMessages.intServerErrorTitle,
      heading = langSpecificMessages.intServerErrorTitle,
      message = langSpecificMessages.intServerErrorTryAgain
    )
  }

  override def notFoundTemplate(implicit request: Request[_]): Html = {
    val optWelshContentCookie = request.cookies.get(ALFCookieNames.useWelsh)

    val langSpecificMessages = optWelshContentCookie collect {
      case welshCookie if welshCookie.value.toBoolean == true => welshContent
    } getOrElse(englishContent)

    views.html.error_template(
      pageTitle = langSpecificMessages.notFoundErrorTitle,
      heading = langSpecificMessages.notFoundErrorHeading,
      message = langSpecificMessages.notFoundErrorBody
    )
  }

  override def microserviceMetricsConfig(implicit app: Application): Option[Configuration] = app.configuration.getConfig(s"microservice.metrics")
}

object ControllerConfiguration extends ControllerConfig {
  lazy val controllerConfigs = Play.current.configuration.underlying.as[Config]("controllers")
}

object LoggingFilter extends FrontendLoggingFilter with MicroserviceFilterSupport {
  override def controllerNeedsLogging(controllerName: String) = ControllerConfiguration.paramsForController(controllerName).needsLogging
}

object AuditFilter extends FrontendAuditFilter with AppName with MicroserviceFilterSupport {

  override lazy val maskedFormFields = Seq("password")

  override lazy val applicationPort = None

  override lazy val auditConnector = FrontendAuditConnector

  override def controllerNeedsAuditing(controllerName: String) = ControllerConfiguration.paramsForController(controllerName).needsAuditing

  override protected def appNameConfiguration: Configuration = Play.current.configuration
}
