package config

import javax.inject.Inject
import model.MessageConstants.MessageConstants
import play.api.Configuration
import play.api.i18n.MessagesApi
import play.api.mvc.Request
import play.twirl.api.Html
import uk.gov.hmrc.play.bootstrap.http.FrontendErrorHandler
import model.MessageConstants.{EnglishMessageConstants => englishContent, WelshMessageConstants => welshContent}

class AddressLookupFrontendErrorHandler @Inject()(val messagesApi: MessagesApi, val configuration: Configuration) extends FrontendErrorHandler {
  override def standardErrorTemplate(pageTitle: String, heading: String, message: String)(implicit rh: Request[_]): Html = {
    val optWelshContentCookie = rh.cookies.get(ALFCookieNames.useWelsh)

    val langSpecificMessages = optWelshContentCookie collect {
      case welshCookie if welshCookie.value.toBoolean == true => welshContent(true)
    } getOrElse (englishContent(true))

    views.html.error_template(
      pageTitle = langSpecificMessages.intServerErrorTitle,
      heading = langSpecificMessages.intServerErrorTitle,
      message = langSpecificMessages.intServerErrorTryAgain
    )
  }
}
