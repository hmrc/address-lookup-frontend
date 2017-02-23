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

import address.outcome.SelectedAddress
import play.api.libs.json.JsValue
import uk.gov.hmrc.logging.SimpleLogger
import uk.gov.hmrc.play.http.HttpResponse

import scala.concurrent.{ExecutionContext, Future}

// TODO this should be using the normal metrics APIs

class MemoMetrics(peer: MemoService, logger: SimpleLogger)(implicit val ec: ExecutionContext) extends MemoService {

  override def storeSingleResponse(tag: String, id: String, address: SelectedAddress): Future[HttpResponse] = {
    val now = System.currentTimeMillis
    peer.storeSingleResponse(tag, id, address) map {
      response =>
        val took = System.currentTimeMillis - now
        val uprn = address.normativeAddress.flatMap(_.uprn.map(_.toString)) getOrElse "unknown"
        logger.info(s"Keystore put $tag $id uprn=$uprn took {}ms", took.toString)
        response
    }
  }

  def fetchSingleResponse(tag: String, id: String): Future[Option[JsValue]] = {
    val now = System.currentTimeMillis
    peer.fetchSingleResponse(tag, id) map {
      response =>
        val took = System.currentTimeMillis - now
        logger.info(s"Keystore get $tag $id took {}ms", took.toString)
        response
    }
  }
}
