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
import play.api.libs.json.JsValue
import uk.gov.hmrc.logging.SimpleLogger
import uk.gov.hmrc.play.http.hooks.HttpHook
import uk.gov.hmrc.play.http.ws.{WSGet, WSPut}
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.{ExecutionContext, Future}

trait MemoService {
  def storeSingleResponse(tag: String, id: String, address: SelectedAddress): Future[HttpResponse]

  def fetchSingleResponse(tag: String, id: String): Future[Option[JsValue]]
}


class KeystoreServiceImpl(endpoint: String, applicationName: String, logger: SimpleLogger, ec: ExecutionContext) extends MemoService {

  private implicit val xec = ec

  private implicit val hc = HeaderCarrier()

  private val http = new WSGet with WSPut {
    override val hooks = Seq[HttpHook]()
    val appName: String = applicationName
  }

  def fetchSingleResponse(tag: String, id: String): Future[Option[JsValue]] = {
    require(id.nonEmpty)
    require(tag.nonEmpty)

    val url = s"$endpoint/keystore/address-lookup/$id"
    // Not using 'GET' because the status and response entity processing would not be appropriate.
    http.doGet(url) map {
      parse(_, tag, url)
    }
  }

  private def parse(response: HttpResponse, tag: String, url: String): Option[JsValue] = {
    response.status match {
      case 200 =>
        try {
          Some((response.json \ "data" \ tag).get)
        } catch {
          case e: Exception =>
            logger.warn(s"keystore error GET $url ${response.status}", e)
            None
        }
      case 404 =>
        None
      case _ =>
        logger.info("keystore error GET {} {}", url, response.status.toString)
        None
    }
  }


  def storeSingleResponse(tag: String, id: String, address: SelectedAddress): Future[HttpResponse] = {
    require(id.nonEmpty)
    require(tag.nonEmpty)

    import SelectedAddress._

    val url = s"$endpoint/keystore/address-lookup/$id/data/$tag"
    http.PUT[SelectedAddress, HttpResponse](url, address)
  }
}
