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

import config.FrontendGlobal
import org.openqa.selenium.WebDriver
import org.scalatest.{BeforeAndAfterAll, SequentialNestedSuiteExecution, Suite}
import org.scalatestplus.play.{HtmlUnitFactory, OneBrowserPerSuite, OneServerPerSuite}
import play.api.libs.ws.WS
import play.api.mvc.{Action, Results}
import play.api.test.FakeApplication
import play.api.test.Helpers._

trait AppServerUnderTest extends OneServerPerSuite with OneBrowserPerSuite with HtmlUnitFactory with BeforeAndAfterAll with SequentialNestedSuiteExecution {
  this: Suite =>

  val stub = new AddRepStub()

  def appConfiguration: Map[String, String] = Map("addressReputation.endpoint" -> stub.endpoint)

  override def createWebDriver(): WebDriver = HtmlUnitFactory.createWebDriver(false)

  implicit override final lazy val app: FakeApplication = new FakeApplication(additionalConfiguration = appConfiguration, withRoutes = {
    case ("GET", "/test-only/assets/javascripts/vendor/modernizr.js") => Action {
      Results.Ok
    }
  })

  lazy val baseURL = s"http://localhost:$port"

  lazy val appBaseURL = s"$baseURL/${FrontendGlobal.appName}"

  def get(url: String) = await(WS.url(url).withMethod("GET").withHeaders("User-Agent" -> "xyz").execute())

  override def beforeAll() {
    stub.start()
  }

  override def afterAll() {
    stub.stop()
  }

}
