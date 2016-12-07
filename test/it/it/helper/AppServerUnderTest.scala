/*
 *
 *  * Copyright 2016 HM Revenue & Customs
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 *
 */

package it.helper

import config.FrontendGlobal
import org.scalatest._
import org.scalatestplus.play.ServerProvider
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceableModule}
import play.api.mvc.{Action, Results}
import play.api.test.{Helpers, TestServer}
import play.api.{Application, Mode}

trait Context {
  def addressLookupStub: Stub

  def keystoreStub: Stub

  def appEndpoint: String

  def appContext: String
}

trait AppServerUnderTest extends SuiteMixin with ServerProvider with AppServerTestApi with Context {
  this: Suite =>

  val addressLookupStub = new Stub
  val keystoreStub = new Stub

  def beforeAppServerStarts() {
    addressLookupStub.start()
    keystoreStub.start()
  }

  def afterAppServerStops() {
    addressLookupStub.stop()
    keystoreStub.stop()
  }

  def appConfiguration: Map[String, String] = Map(
    "addressReputation.endpoint" -> addressLookupStub.endpoint,
    "keystore.endpoint" -> keystoreStub.endpoint
  )

  private val bindModules: Seq[GuiceableModule] = Seq()

  implicit override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(appConfiguration)
    .bindings(bindModules: _*).in(Mode.Test)
    .routes({
      case ("GET", "/test-only/assets/javascripts/vendor/modernizr.js") => Action {
        Results.Ok
      }
    })
    .build()

  /**
    * The port used by the `TestServer`.  By default this will be set to the result returned from
    * `Helpers.testServerPort`. You can override this to provide a different port number.
    */
  lazy val port: Int = Helpers.testServerPort

  override lazy val appEndpoint = s"http://localhost:$port"

  def appContext: String = "/" + FrontendGlobal.appName

  abstract override def run(testName: Option[String], args: Args): Status = {
    beforeAppServerStarts()
    stabilise()
    val testServer = TestServer(port, app)
    testServer.start()
    stabilise()
    try {
      val newConfigMap = args.configMap + ("org.scalatestplus.play.app" -> app) + ("org.scalatestplus.play.port" -> port)
      val newArgs = args.copy(configMap = newConfigMap)
      val status = super.run(testName, newArgs)
      status.waitUntilCompleted()
      status
    }
    finally {
      testServer.stop()
      afterAppServerStops()
      stabilise()
    }
  }

  // starting the stubs takes finite time; there is a race condition and we need to wait. Sigh.
  private def stabilise() {
    Thread.sleep(100)
  }

}

