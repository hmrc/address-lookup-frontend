
package controllers

import config.FrontendAppConfig
import javax.inject.{Inject, Singleton}
import play.api.i18n.{I18nSupport, Lang, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.play.language.LanguageUtils

import scala.concurrent.ExecutionContext

@Singleton
class LanguageController @Inject()(config: FrontendAppConfig)(implicit val ec: ExecutionContext, implicit val messagesApi: MessagesApi)
  extends FrontendController with I18nSupport {

  def switchToLanguage(language: String): Action[AnyContent] = Action { implicit request =>
    val lang: Lang = config.languageMap.getOrElse(language, LanguageUtils.getCurrentLang)
    val redirectURL = request.headers.get(REFERER).getOrElse(throw new InternalServerException(s"[LanguageController][switchToLanguage] Header: $REFERER did not have a value"))
    Redirect(redirectURL).withLang(Lang.apply(lang.code)).flashing(LanguageUtils.FlashWithSwitchIndicator)
  }

}