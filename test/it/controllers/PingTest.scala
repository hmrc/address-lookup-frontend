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
import play.api.test.Helpers._

class PingTest extends PlaySpec with AppServerUnderTest {

  "ping resource" must {
    val pingPage = s"$baseURL/ping"

    "give a successful response" in {
      get(pingPage).status mustBe OK
    }

    "give version information in the response body" in {
      (get(pingPage).json \ "version").as[String] must not be empty
    }
  }

}
