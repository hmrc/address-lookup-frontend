/*
 * Copyright 2023 HM Revenue & Customs
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

package controllers

import java.net.URI

import config.FrontendAppConfig
import javax.inject.{Inject, Singleton}
import play.api.i18n.{I18nSupport, Lang}
import play.api.mvc.{Action, AnyContent, Flash, MessagesControllerComponents}
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.play.language.LanguageUtils

import scala.concurrent.ExecutionContext
import scala.util.Try

@Singleton
class LanguageController @Inject()(config: FrontendAppConfig, controllerComponents: MessagesControllerComponents, languageUtils: LanguageUtils)(implicit val ec: ExecutionContext)
  extends FrontendController(controllerComponents) with I18nSupport {

  def switchToLanguage(language: String): Action[AnyContent] = Action { implicit request =>
    val enabled: Boolean = config.languageMap.get(language).exists(languageUtils.isLangAvailable)
    val lang: Lang =
      if (enabled) config.languageMap.getOrElse(language, languageUtils.getCurrentLang)
      else languageUtils.getCurrentLang

    val redirectURL: String = request.headers.get(REFERER)
      .flatMap(asRelativeUrl)
      .getOrElse(throw new InternalServerException(s"[LanguageController][switchToLanguage] Header: $REFERER did not have a value"))

    Redirect(redirectURL).withLang(Lang.apply(lang.code)).flashing(FlashWithSwitchIndicator)
  }

  private def asRelativeUrl(url:String): Option[String] =
    for {
      uri      <- Try(new URI(url)).toOption
      path     <- Option(uri.getPath)
      query    <- Option(uri.getQuery).map("?" + _).orElse(Some(""))
      fragment <- Option(uri.getRawFragment).map("#" + _).orElse(Some(""))
    } yield  s"$path$query$fragment"

  private val SwitchIndicatorKey = "switching-language"
  private val FlashWithSwitchIndicator = Flash(Map(SwitchIndicatorKey -> "true"))
}
