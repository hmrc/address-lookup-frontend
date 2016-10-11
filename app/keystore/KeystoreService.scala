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

import address.uk._
import uk.gov.hmrc.play.http.hooks.HttpHook
import uk.gov.hmrc.play.http.ws.{WSGet, WSPut}
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpResponse, NotFoundException}

import scala.concurrent.{ExecutionContext, Future}

class KeystoreService(endpoint: String, applicationName: String)(implicit val ec: ExecutionContext) {

  private val url = s"$endpoint/keystore/address-lookup/"

  private implicit val hc = HeaderCarrier()

  private val http = new WSGet with WSPut {
    override val hooks = Seq[HttpHook]()
    val appName = applicationName
  }

  def fetchSingleResponse(id: String, variant: Int): Future[Option[AddressRecordWithEdits]] = {
    import ResponseReadable._
    val url = s"$endpoint/keystore/address-lookup/$id/data/response$variant"
    http.GET[AddressRecordWithEdits](url).map {
      ar => Some(ar)
    }.recover {
      case e: NotFoundException => None
    }
  }

  def storeSingleResponse(id: String, variant: Int, address: AddressRecordWithEdits): Future[HttpResponse] = {
    import ResponseWriteable._
    val url = s"$endpoint/keystore/address-lookup/$id/data/response$variant"
    http.PUT[AddressRecordWithEdits, HttpResponse](url, address)
  }
}
