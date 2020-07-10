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
