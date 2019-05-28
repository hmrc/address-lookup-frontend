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
import com.github.tomakehurst.wiremock.admin.model.ListStubMappingsResult
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import itutil.config.{AddressRecordConstants, IntegrationTestConstants}
import org.scalatestplus.play.OneServerPerSuite
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.libs.ws.WSClient

object WireMockHelper {
  val wiremockPort = 11111
  val wiremockHost = "localhost"
  val url = s"http://$wiremockHost:$wiremockPort"
}

trait WireMockHelper {
  self: OneServerPerSuite =>

  import WireMockHelper._

  lazy val ws: WSClient = app.injector.instanceOf(classOf[WSClient])

  val wmConfig: WireMockConfiguration = wireMockConfig().port(wiremockPort)
  val wireMockServer: WireMockServer = new WireMockServer(wmConfig)

  def startWiremock(): Unit = {
    wireMockServer.start()
    WireMock.configureFor(wiremockHost, wiremockPort)
  }

  def stopWiremock(): Unit = wireMockServer.stop()

  def resetWiremock(): Unit = WireMock.reset()

  def buildClientLookupAddress(path: String, journeyID: String = "Jid123") = ws.url(s"http://localhost:$port/lookup-address/$journeyID/$path").withFollowRedirects(false)

  def buildClientAPI(path: String) = ws.url(s"http://localhost:$port/api/$path").withFollowRedirects(false)

  def listAllStubs: ListStubMappingsResult = listAllStubMappings

  def stubKeystore(session: String, theData: JsObject, status: Int = 200): StubMapping = {
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

  def stubKeystoreSave(session: String, theData: JsObject, status: Int): StubMapping = {
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

  def stubALBEGET(
    postcode: String = IntegrationTestConstants.testPostCode,
    filter: Option[String] = None,
    expectedStatus: Int = 200,
    addressJson: JsValue = AddressRecordConstants.addressRecordSeqJson): StubMapping = {

    val alfBackendURL = s"/address-lookup/v2/uk/addresses?postcode=$postcode" + filter.fold("")(f => s"&filter=$f")
    stubFor(get(urlMatching(alfBackendURL))
      .willReturn(aResponse().
        withStatus(expectedStatus).
        withBody(
          addressJson.toString()
        )
      )
    )
  }

}