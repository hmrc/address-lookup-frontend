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
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import uk.gov.hmrc.logging.SimpleLogger
import uk.gov.hmrc.play.http.hooks.HttpHook
import uk.gov.hmrc.play.http.ws.{WSGet, WSPut}
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.{ExecutionContext, Future}

trait MemoService {
  def fetchSingleResponse(tag: String, id: String): Future[Option[AddressRecordWithEdits]]

  def storeSingleResponse(tag: String, id: String, address: AddressRecordWithEdits): Future[HttpResponse]
}


class KeystoreServiceImpl(endpoint: String, applicationName: String, logger: SimpleLogger, ec: ExecutionContext) extends MemoService {

  private implicit val xec = ec

  private implicit val hc = HeaderCarrier()

  private val http = new WSGet with WSPut {
    override val hooks = Seq[HttpHook]()
    val appName = applicationName
  }

  def fetchSingleResponse(tag: String, id: String): Future[Option[AddressRecordWithEdits]] = {
    require(id.nonEmpty)
    require(tag.nonEmpty)

    val url = s"$endpoint/keystore/address-lookup/$id"
    // Not using 'GET' because the status and response entity processing would not be appropriate.
    http.doGet(url) map {
      parse(_, tag, url)
    }
  }

  private def parse(response: HttpResponse, tag: String, url: String): Option[AddressRecordWithEdits] = {
    response.status match {
      case 200 =>
        try {
          val ks = LenientJacksonMapper.readValue(response.body, classOf[KeystoreResponse])
          ks.data.get(tag)
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


  def storeSingleResponse(tag: String, id: String, address: AddressRecordWithEdits): Future[HttpResponse] = {
    require(id.nonEmpty)
    require(tag.nonEmpty)

    import ResponseWriteable._
    val url = s"$endpoint/keystore/address-lookup/$id/data/$tag"
    http.PUT[AddressRecordWithEdits, HttpResponse](url, address)
  }
}


case class KeystoreResponse(data: Map[String, AddressRecordWithEdits])


object LenientJacksonMapper extends ObjectMapper {
  configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
  registerModule(DefaultScalaModule)
  setSerializationInclusion(JsonInclude.Include.NON_ABSENT)
}
