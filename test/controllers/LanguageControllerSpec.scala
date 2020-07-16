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

import com.codahale.metrics.SharedMetricRegistries
import config.FrontendAppConfig
import fixtures.ALFEFixtures
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.Play
import play.api.i18n.MessagesApi
import play.api.mvc.{AnyContent, Cookie, MessagesControllerComponents, Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.play.language.LanguageUtils

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class LanguageControllerSpec extends PlaySpec with GuiceOneAppPerSuite with ALFEFixtures {
  SharedMetricRegistries.clear()

  implicit lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

  val frontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
  val components = app.injector.instanceOf[MessagesControllerComponents]
  val languageUtils = app.injector.instanceOf[LanguageUtils]

  class TestLanguageController extends LanguageController(frontendAppConfig, components, languageUtils)

  "switchToLanguage" must {

    val referer: String = "testReferrer"

    "set the language cookie and redirect back to the referer" when {
      val request: Request[AnyContent] = FakeRequest().withHeaders(REFERER -> referer)

      "the language is english" in new TestLanguageController {
        val result: Future[Result] = switchToLanguage("english")(request)
        status(result) mustBe 303
        redirectLocation(result) mustBe Some(referer)
        cookies(result).get(Play.langCookieName).get.value mustBe "en"
      }

      "the language is cymraeg (welsh)" in new TestLanguageController {
        val result: Future[Result] = switchToLanguage("cymraeg")(request)
        status(result) mustBe 303
        redirectLocation(result) mustBe Some(referer)
        cookies(result).get(Play.langCookieName).get.value mustBe "cy"
      }

      "the language is not in the language map and the request does not have a language cookie already" in new TestLanguageController {
        val result: Future[Result] = switchToLanguage("unknown")(request)
        status(result) mustBe 303
        redirectLocation(result) mustBe Some(referer)
        cookies(result).get(Play.langCookieName).get.value mustBe "en"
      }
    }

    "keep the language the same" when {
      "the language is not in the language map" in new TestLanguageController {
        val request: Request[AnyContent] = FakeRequest().withHeaders(REFERER -> referer).withCookies(Cookie(Play.langCookieName, "cy"))
        val result: Future[Result] = switchToLanguage("unknown")(request)
        status(result) mustBe 303
        redirectLocation(result) mustBe Some(referer)
        cookies(result).get(Play.langCookieName).get.value mustBe "cy"
      }
    }

    "throw an internal server exception" when {
      "the referer header does not have a value" in new TestLanguageController {
        val request: Request[AnyContent] = FakeRequest().withCookies(Cookie(Play.langCookieName, "cy"))
        intercept[InternalServerException](switchToLanguage("english")(request))
      }
    }
  }

}