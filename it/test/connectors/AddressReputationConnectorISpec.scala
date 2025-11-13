/*
 * Copyright 2025 HM Revenue & Customs
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

package connectors

import address.v2._
import com.github.tomakehurst.wiremock.client.WireMock._
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers.mustBe
import play.api.libs.json.Json
import services.{LookupAddressByCountry, LookupAddressByPostcode}
import support.{IntegrationBaseSpec, WireMockHelper}

import scala.util.Random

class AddressReputationConnectorISpec extends IntegrationBaseSpec
  with ScalaFutures
  with IntegrationPatience {

  override def serviceConfig: Map[String, Any] = Map(
    "microservice.services.address-reputation.port" -> WireMockHelper.wireMockPort
  )

  class Test {
    lazy val connector: AddressReputationConnector = app.injector.instanceOf[AddressReputationConnector]
  }

  private def addr(code: Option[String])(uprn: Long, organisation: Option[String] = None): AddressRecord = {
    def rndstr(i: Int): String = Random.alphanumeric.take(i).mkString

    AddressRecord(
      uprn.toString,
      Some(uprn),
      Some(uprn+100),
      Some(uprn+20),
      organisation,
      Address(
        List(rndstr(16), rndstr(16), rndstr(8)),
        rndstr(16),
        rndstr(8),
        Some(Countries.England),
        Country(code.get, rndstr(32))
      ),
      "en",
      Some(LocalCustodian(123, "Tyne & Wear")), None, None, None, None
    )
  }


    "findByPostcode" should {
      "return addresses" in new Test {
        val responseBody: Seq[AddressRecord] = Seq(
          addr(Some("GB"))(1000L),
          addr(Some("GB"))(1001L)
        )

        wireMockServer.stubFor(
          post(urlEqualTo("/lookup"))
            .withRequestBody(equalToJson(Json.stringify(Json.toJson(LookupAddressByPostcode("ZZ11 1ZZ", Some("filter"))))))
            .willReturn(aResponse().withStatus(200).withBody(Json.stringify(Json.toJson(responseBody))))
        )

        connector.findByPostcode("ZZ11 1ZZ", Some("filter")).futureValue.mustBe(responseBody)
      }
  }

  "findByCountry" should {
    "return non UK addresses" in new Test {
      val responseBody: Seq[NonUkAddressRecord] = Seq(
        NonUkAddressRecord("1", Some("1234"), Some("Test Street 1"), Some("unit 1"), Some("Test City 1"), Some("district 1"), Some("region 1"), Some("XX1 1YY")),
        NonUkAddressRecord("2", Some("5678"), Some("Test Street 2"), Some("unit 2"), Some("Test City 2"), Some("district 2"), Some("region 2"), Some("XX2 2YY"))
      )

      wireMockServer.stubFor(
        post(urlEqualTo("/country/FR/lookup"))
          .withRequestBody(equalToJson(Json.stringify(Json.toJson(LookupAddressByCountry("filter")))))
          .willReturn(aResponse().withStatus(200).withBody(Json.stringify(Json.toJson(responseBody))))
      )

      connector.findByCountry("FR", "filter").futureValue.mustBe(responseBody)
    }
  }

}
