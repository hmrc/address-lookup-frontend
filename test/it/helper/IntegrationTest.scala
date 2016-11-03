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

package helper

import config.FrontendGlobal
import org.scalatest._
import org.scalatestplus.play.ServerProvider
import play.api.{Application, Mode}
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceableModule}
import play.api.mvc.{Action, Results}
import play.api.test.{Helpers, TestServer}
import stub.{StubbedAddressService2, StubbedKeystoreService2}

trait IntegrationTest extends SuiteMixin with ServerProvider with StubbedAddressService2 with StubbedKeystoreService2 {
  this: Suite =>

  def appConfiguration: Map[String, String] = Map(
    "addressReputation.endpoint" -> addressLookupEndpoint,
    "keystore.endpoint" -> keystoreEndpoint
  )

  private val bindModules: Seq[GuiceableModule] = Seq()

  implicit override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(appConfiguration)
    .bindings(bindModules:_*).in(Mode.Test)
    .routes({
      case ("GET", "/test-only/assets/javascripts/vendor/modernizr.js") => Action {
        Results.Ok
      }})
    .build()

  final def port: Int = Helpers.testServerPort

  abstract override def run(testName: Option[String], args: Args): Status = {
    beforeAll()
    stabilise()
    val myApp = app
    val testServer = TestServer(port, myApp)
    testServer.start()
    stabilise()
    try {
      val newConfigMap = args.configMap + ("org.scalatestplus.play.app" -> myApp) + ("org.scalatestplus.play.port" -> port)
      val newArgs = args.copy(configMap = newConfigMap)
      val status = super.run(testName, newArgs)
      status.waitUntilCompleted()
      status
    }
    finally {
      testServer.stop()
      afterAll()
      stabilise()
    }
  }

  // starting the stubs takes finite time; there is a race condition and we need to wait. Sigh.
  private def stabilise() {
    Thread.sleep(100)
  }

  def appEndpoint = s"http://localhost:$port"

  def appContext = "/" + FrontendGlobal.appName
}
