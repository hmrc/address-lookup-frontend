/*
 * Copyright 2016 HM Revenue & Customs
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

import org.scalatestplus.play._

class AddressUkControllerTest extends PlaySpec with AppServerUnderTest {

  "uk address happy-path journeys" must {
    val getFormPage = s"$appBaseURL/getForm"

    "get form, post form, redirect to continue" in {
      deleteAllCookies() // ensure we are definitely not logged in
      go to getFormPage
      //      currentUrl mustBe loginPage
    }

  }

}
