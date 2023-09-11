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
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSClient, WSRequest}
import services.{LookupAddressByCountry, LookupAddressByPostcode}

object WireMockHelper {
  val wiremockPort = 11111
  val wiremockHost = "localhost"
  val url = s"http://$wiremockHost:$wiremockPort"
}

trait WireMockHelper {
  self: GuiceOneServerPerSuite =>

  import WireMockHelper._

  lazy val ws: WSClient = app.injector.instanceOf(classOf[WSClient])

  val wmConfig: WireMockConfiguration = wireMockConfig().port(wiremockPort)
  val wireMockServer: WireMockServer = new WireMockServer(wmConfig)

  val keyId = "journey-data"

  def startWiremock(): Unit = {
    wireMockServer.start()
    WireMock.configureFor(wiremockHost, wiremockPort)
  }

  def stopWiremock(): Unit = wireMockServer.stop()

  def resetWiremock(): Unit = WireMock.reset()

  def buildClientLookupAddress(path: String, journeyID: String) =
    ws.url(s"http://localhost:$port/lookup-address/$journeyID/$path").withFollowRedirects(false)

  def buildClientAPI(path: String) = ws.url(s"http://localhost:$port/api/$path").withFollowRedirects(false)

  def buildClientLanguage(language: String, referer: String): WSRequest =
    ws.url(s"http://localhost:$port/lookup-address/language/$language").withHttpHeaders("Referer" -> referer)
      .withFollowRedirects(false)

  def buildClientTestOnlyRoutes(path: String) =
    ws.url(s"http://localhost:$port/lookup-address/test-only/$path").withFollowRedirects(false)

  def listAllStubs: ListStubMappingsResult = listAllStubMappings

  def stubGetAddressFromBEWithFilter(postcode: String = IntegrationTestConstants.testPostCode,
                                     expectedStatus: Int = 200,
                                     addressJson: JsValue = AddressRecordConstants.addressRecordSeqJson): StubMapping
  = {

    val alfBackendURL = "/lookup"

    val lookupAddressByPostcode = LookupAddressByPostcode(postcode, Some("bar"))
    stubFor(post(urlPathEqualTo(alfBackendURL))
      .withRequestBody(equalTo(Json.toJson(lookupAddressByPostcode).toString()))
      .willReturn(
        aResponse()
          .withStatus(expectedStatus)
          .withBody(addressJson.toString())
      )
    )
  }

  def stubGetAddressFromBE(postcode: String = IntegrationTestConstants.testPostCode,
                           expectedStatus: Int = 200,
                           addressJson: JsValue = AddressRecordConstants.addressRecordSeqJson): StubMapping = {
    val alfBackendURL = "/lookup"

    val lookupAddressByPostcode = LookupAddressByPostcode(postcode, None)
    stubFor(post(urlPathEqualTo(alfBackendURL))
      .withRequestBody(equalTo(Json.toJson(lookupAddressByPostcode).toString()))
      .willReturn(
        aResponse()
          .withStatus(expectedStatus)
          .withBody(addressJson.toString())
      )
    )
  }

  def stubGetAddressByCountry(countryCode: String, filter: String = IntegrationTestConstants.testFilterValue,
                           expectedStatus: Int = 200,
                           addressJson: JsValue = AddressRecordConstants.internationalAddressRecordSeqJson): StubMapping = {
    val alfBackendURL = s"/country/$countryCode/lookup"

    val lookupAddressByCountry = LookupAddressByCountry(filter)
    stubFor(post(urlPathEqualTo(alfBackendURL))
      .withRequestBody(equalTo(Json.toJson(lookupAddressByCountry).toString()))
      .willReturn(
        aResponse()
          .withStatus(expectedStatus)
          .withBody(addressJson.toString())
      )
    )
  }
}