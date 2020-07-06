package config

import javax.inject.{Inject, Singleton}
import play.api.i18n.MessagesApi
import play.api.mvc.Request
import play.twirl.api.Html
import uk.gov.hmrc.play.bootstrap.http.FrontendErrorHandler
import model.MessageConstants.{EnglishMessageConstants => englishContent, WelshMessageConstants => welshContent}

@Singleton
class AddressLookupFrontendErrorHandler @Inject()(val messagesApi: MessagesApi,
                                                  frontendAppConfig: FrontendAppConfig) extends FrontendErrorHandler {

  override def standardErrorTemplate(pageTitle: String, heading: String, message: String)(implicit rh: Request[_]): Html = {
    val optWelshContentCookie = rh.cookies.get(ALFCookieNames.useWelsh)

    val langSpecificMessages = optWelshContentCookie collect {
      case welshCookie if welshCookie.value.toBoolean == true => welshContent(true)
    } getOrElse (englishContent(true))

    views.html.error_template(
      appConfig = frontendAppConfig,
      pageTitle = langSpecificMessages.intServerErrorTitle,
      heading = langSpecificMessages.intServerErrorTitle,
      message = langSpecificMessages.intServerErrorTryAgain
    )
  }

  override def notFoundTemplate(implicit request: Request[_]): Html = {
    val optWelshContentCookie = request.cookies.get(ALFCookieNames.useWelsh)

    val langSpecificMessages = optWelshContentCookie collect {
      case welshCookie if welshCookie.value.toBoolean == true => welshContent(true)
    } getOrElse(englishContent(true))

    views.html.error_template(
      appConfig = frontendAppConfig,
      pageTitle = langSpecificMessages.notFoundErrorTitle,
      heading = langSpecificMessages.notFoundErrorHeading,
      message = langSpecificMessages.notFoundErrorBody
    )
  }

}
