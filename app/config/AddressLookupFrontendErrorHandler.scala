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

import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.RequestHeader
import play.twirl.api.Html
import uk.gov.hmrc.play.bootstrap.frontend.http.FrontendErrorHandler

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AddressLookupFrontendErrorHandler @Inject()(val messagesApi: MessagesApi,
                                                  implicit val frontendAppConfig: FrontendAppConfig,
                                                  val ec: ExecutionContext,
                                                  error_template: views.html.error_template) extends FrontendErrorHandler {

  override def standardErrorTemplate(pageTitle: String, heading: String, message: String)(implicit request: RequestHeader): Future[Html] = {
    val messages = implicitly[Messages]

    Future.successful(
      error_template(
        title = messages("constants.intServerErrorTitle"),
        heading = messages("constants.intServerErrorTitle"),
        message = messages("constants.intServerErrorTryAgain")
      )
    )
  }

  override def notFoundTemplate(implicit request: RequestHeader): Future[Html] = {
    val messages = implicitly[Messages]

    Future.successful(
      error_template(
        title = messages("constants.notFoundErrorTitle"),
        heading = messages("constants.notFoundErrorHeading"),
        message = messages("constants.notFoundErrorBody")
      )
    )
  }

}
