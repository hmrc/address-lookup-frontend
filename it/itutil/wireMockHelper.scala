/*
 * Copyright 2017 HM Revenue & Customs
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
package itutil

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import org.scalatestplus.play.OneServerPerSuite
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.WSClient

object WiremockHelper {
  val wiremockPort = 11111
  val wiremockHost = "localhost"
  val url = s"http://$wiremockHost:$wiremockPort"
}

trait WiremockHelper {
  self: OneServerPerSuite =>

  import WiremockHelper._

  lazy val ws = app.injector.instanceOf(classOf[WSClient])

  val wmConfig = wireMockConfig().port(wiremockPort)
  val wireMockServer = new WireMockServer(wmConfig)

  def startWiremock() = {
    wireMockServer.start()
    WireMock.configureFor(wiremockHost, wiremockPort)
  }

  def stopWiremock() = wireMockServer.stop()

  def resetWiremock() = WireMock.reset()

  def buildClient(path: String, journeyID:String = "Jid123", lookUpOrApi: String = "lookup-address") = ws.url(s"http://localhost:$port/$lookUpOrApi/$journeyID$path").withFollowRedirects(false)

  def listAllStubs = listAllStubMappings

  def stubKeystore(session: String, theData: JsObject, status: Int = 200) = {
    val keystoreUrl = s"/keystore/address-lookup-frontend/journey-data"
    stubFor(get(urlMatching(keystoreUrl))
      .willReturn(aResponse().
        withStatus(status).
        withBody(
          Json.obj("id" -> "journey-data","data" -> Json.obj(session -> theData)).toString()
        )
      )
    )
  }

  def stubKeystoreSave(session: String, theData: JsObject, status: Int) = {
    val keystoreUrl = s"/keystore/address-lookup-frontend/journey-data/data/$session"
    stubFor(put(urlMatching(keystoreUrl))
      .willReturn(aResponse().
        withStatus(status).
        withBody(
          Json.obj("id" -> "journey-data", "data" -> Json.obj(session -> theData)).toString()
        )
      )
    )
  }

}