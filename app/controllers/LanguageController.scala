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

package controllers

import config.FrontendAppConfig
import javax.inject.{Inject, Singleton}
import play.api.i18n.{I18nSupport, Lang, Messages}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.play.language.LanguageUtils

import scala.concurrent.ExecutionContext

@Singleton
class LanguageController @Inject()(config: FrontendAppConfig, controllerComponents: MessagesControllerComponents, languageUtils: LanguageUtils)(implicit val ec: ExecutionContext, implicit val messages: Messages)
  extends FrontendController(controllerComponents) with I18nSupport {

  def switchToLanguage(language: String): Action[AnyContent] = Action { implicit request =>
    val lang: Lang = config.languageMap.getOrElse(language, languageUtils.getCurrentLang)
    val redirectURL = request.headers.get(REFERER).getOrElse(throw new InternalServerException(s"[LanguageController][switchToLanguage] Header: $REFERER did not have a value"))
    Redirect(redirectURL).withLang(Lang.apply(lang.code)) //.flashing(languageUtils.FlashWithSwitchIndicator)
  }

}