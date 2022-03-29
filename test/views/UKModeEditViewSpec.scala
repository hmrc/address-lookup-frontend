/*
 * Copyright 2022 HM Revenue & Customs
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

package views

import config.FrontendAppConfig
import play.api.i18n.MessagesApi
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import views.html.abp.non_uk_mode_edit

class UKModeEditViewSpec extends ViewSpec {
  val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit val testRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
  implicit val frontendAppConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]

  val non_uk_mode_edit: non_uk_mode_edit = app.injector.instanceOf[non_uk_mode_edit]

  "UK Mode Page" should {
    //implicit val lang: Lang = Lang("en")

    //TODO: TESTS_REQUIRED
    //would be nice to have some tests around the rendering of errors perhaps
    //do we need to test for the back button?
  }
}
