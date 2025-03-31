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

package controllers

import itutil.IntegrationSpecBase
import play.api.http.Status._
import play.api.i18n.MessagesApi

class LanguageControllerISpec extends IntegrationSpecBase {

  implicit lazy val messagesAPI: MessagesApi = app.injector.instanceOf[MessagesApi]

  s"${controllers.routes.LanguageController.switchToLanguage("english")}" must {
    "swap to english and redirect back" in {
      val response = await(buildClientLanguage("english", "testRef").get())
      response.status shouldBe SEE_OTHER
      response.cookie(messagesApi.langCookieName).get.value shouldBe "en"
    }
  }

  s"${controllers.routes.LanguageController.switchToLanguage("cymraeg")}" must {
    "swap to welsh and redirect back" in {
      val response = await(buildClientLanguage("cymraeg", "testRef").get())
      response.status shouldBe SEE_OTHER
      response.cookie(messagesApi.langCookieName).get.value shouldBe "cy"
    }
  }

}