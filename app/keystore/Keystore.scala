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

package keystore

import uk.gov.hmrc.address.v2.AddressRecord
import uk.gov.hmrc.play.http.hooks.HttpHook
import uk.gov.hmrc.play.http.ws.WSPut
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.{ExecutionContext, Future}

class Keystore(endpoint: String, applicationName: String)(implicit val ec: ExecutionContext) {

  private val url = s"$endpoint/keystore/address-lookup/"

  private implicit val hc = HeaderCarrier()

  private val http = new WSPut {
    override val hooks = Seq[HttpHook]()
    val appName = applicationName
  }

  def storeResponse(id: String, variant: Int, addresses: List[AddressRecord]): Future[HttpResponse] = {
    val url = s"$endpoint/keystore/address-lookup/$id/data/response$variant"
    import osgb.outmodel.v2.AddressWriteable._
    http.PUT[List[AddressRecord], HttpResponse](url, addresses)
  }
}
