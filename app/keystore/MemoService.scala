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
import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.address.v2.{Country, International}
import uk.gov.hmrc.logging.SimpleLogger
import uk.gov.hmrc.play.http.hooks.HttpHook
import uk.gov.hmrc.play.http.ws.{WSGet, WSPut}
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.{ExecutionContext, Future}

trait MemoService {
  def fetchSingleUkResponse(tag: String, id: String): Future[Option[AddressRecordWithEdits]]

  def storeSingleUkResponse(tag: String, id: String, address: AddressRecordWithEdits): Future[HttpResponse]

  def fetchSingleIntResponse(tag: String, id: String): Future[Option[International]]

  def storeSingleIntResponse(tag: String, id: String, address: International): Future[HttpResponse]
}


class KeystoreServiceImpl(endpoint: String, applicationName: String, logger: SimpleLogger, ec: ExecutionContext) extends MemoService {

  private implicit val xec = ec

  private implicit val hc = HeaderCarrier()

  private val http = new WSGet with WSPut {
    override val hooks = Seq[HttpHook]()
    val appName: String = applicationName
  }

  def fetchSingleUkResponse(tag: String, id: String): Future[Option[AddressRecordWithEdits]] = {
    fetchSingleResponse[AddressRecordWithEdits](tag, id, parseUkAddress)
  }

  private def parseUkAddress(tag: String, body: String) = {
    val ks = LenientJacksonMapper.readValue(body, classOf[UkKeystoreResponse])
    ks.data.get(tag)
  }

  def fetchSingleIntResponse(tag: String, id: String): Future[Option[International]] = {
    fetchSingleResponse[International](tag, id, parseIntAddress)
  }

  private def parseIntAddress(tag: String, body: String) = {
    val ks = LenientJacksonMapper.readValue(body, classOf[IntKeystoreResponse])
    ks.data.get(tag)
  }

  private def fetchSingleResponse[T](tag: String, id: String, fn: (String, String) => Option[T]): Future[Option[T]] = {
    require(id.nonEmpty)
    require(tag.nonEmpty)

    val url = s"$endpoint/keystore/address-lookup/$id"
    // Not using 'GET' because the status and response entity processing would not be appropriate.
    http.doGet(url) map {
      parse(_, tag, url, fn)
    }
  }

  private def parse[T](response: HttpResponse, tag: String, url: String, fn: (String, String) => Option[T]): Option[T] = {
    response.status match {
      case 200 =>
        try {
          fn(tag, response.body)
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


  def storeSingleUkResponse(tag: String, id: String, address: AddressRecordWithEdits): Future[HttpResponse] = {
    require(id.nonEmpty)
    require(tag.nonEmpty)

    import AddressRecordWithEdits._

    val url = s"$endpoint/keystore/address-lookup/$id/data/$tag"
    http.PUT[AddressRecordWithEdits, HttpResponse](url, address)
  }


  def storeSingleIntResponse(tag: String, id: String, address: International): Future[HttpResponse] = {
    require(id.nonEmpty)
    require(tag.nonEmpty)

    implicit val f0: OFormat[Country] = Json.format[Country]
    implicit val f1: OFormat[International] = Json.format[International]

    val url = s"$endpoint/keystore/address-lookup/$id/data/$tag"
    http.PUT[International, HttpResponse](url, address)
  }
}


case class UkKeystoreResponse(data: Map[String, AddressRecordWithEdits])

case class IntKeystoreResponse(data: Map[String, International])


object LenientJacksonMapper extends ObjectMapper {
  configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
  registerModule(DefaultScalaModule)
  setSerializationInclusion(JsonInclude.Include.NON_ABSENT)
}
