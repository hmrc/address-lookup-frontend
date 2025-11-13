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

import address.v2.{AddressRecord, NonUkAddressRecord}
import config.FrontendAppConfig
import forms.Postcode
import play.api.Logging
import play.api.libs.json.Json
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import services.AddressReputationFormats.{addressRecordReads, nonUkAddressRecordReads}
import services.{LookupAddressByCountry, LookupAddressByPostcode}
import uk.gov.hmrc.http.HttpReadsInstances.readFromJson
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AddressReputationConnector @Inject() (frontendAppConfig: FrontendAppConfig, httpClient: HttpClientV2)(implicit val
ec: ExecutionContext) extends Logging {

  val endpoint: String = frontendAppConfig.addressReputationEndpoint

  def findByPostcode(postcode: String, filter: Option[String] = None)(implicit hc: HeaderCarrier): Future[Seq[AddressRecord]] = {
    val lookupAddressByPostcode: LookupAddressByPostcode = LookupAddressByPostcode(Postcode.cleanupPostcode(postcode).get.toString, filter)

    httpClient
      .post(url"$endpoint/lookup")
      .withBody(Json.toJson(lookupAddressByPostcode))
      .execute[Seq[AddressRecord]]
  }

  def findByCountry(countryCode: String, filter: String)(implicit hc: HeaderCarrier): Future[Seq[NonUkAddressRecord]] = {
    val lookupAddressByCountry: LookupAddressByCountry = LookupAddressByCountry(filter)

    httpClient
      .post(url"$endpoint/country/$countryCode/lookup")
      .withBody(Json.toJson(lookupAddressByCountry))
      .execute[Seq[NonUkAddressRecord]]
  }
}

