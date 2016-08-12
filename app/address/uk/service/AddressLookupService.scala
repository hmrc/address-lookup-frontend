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

package address.uk.service

import java.net.URLEncoder

import address.uk.{Address, AddressRecord, Country, Services}
import config.FrontendGlobal
import config.ConfigHelper._
import play.api.{Logger, Play}
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.{JsPath, Reads}
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.http.hooks.HttpHook
import uk.gov.hmrc.play.http.ws.WSGet

import scala.concurrent.{ExecutionContext, Future}


object AddressLookupService extends AddressLookupService(
  mustGetConfigString(Play.current.mode, Play.current.configuration, "addressReputation.endpoint"),
  FrontendGlobal.appName)(FrontendGlobal.executionContext)


class AddressLookupService(endpoint: String, applicationName: String)(implicit val ec: ExecutionContext) {

  private val url = s"$endpoint/uk/addresses"

  private implicit val hc = HeaderCarrier()

  implicit val CountryReads: Reads[Country] = (
    (JsPath \ "code").read[String](minLength[String](2) keepAnd maxLength[String](2)) and
      (JsPath \ "name").read[String]) (Country.apply _)

  implicit val AddressReads: Reads[Address] = (
    (JsPath \ "lines").read[List[String]] and
      (JsPath \ "town").readNullable[String] and
      (JsPath \ "county").readNullable[String] and
      (JsPath \ "postcode").read[String] and
      (JsPath \ "subdivision").readNullable[String] and
      (JsPath \ "country").read[Country]) (Address.apply _)

  implicit val AddressRecordReads: Reads[AddressRecord] = (
    (JsPath \ "id").read[String] and
      (JsPath \ "uprn").readNullable[Long] and
      (JsPath \ "address").read[Address] and
      (JsPath \ "language").read[String]) (AddressRecord.apply _)

  private val http = new WSGet {
    override val hooks = Seq[HttpHook]()
    val appName = applicationName
  }

  def findUprn(uprn: String): Future[List[AddressRecord]] = {
    val uq = "?uprn=" + enc(uprn)
    http.GET[List[AddressRecord]](url + uq)
  }

  def findAddresses(postcode: String, filter: Option[String]): Future[List[AddressRecord]] = {
    val pq = "?postcode=" + enc(postcode)
    val fq = filter.map(fi => "&filter=" + enc(fi)).getOrElse("")
    http.GET[List[AddressRecord]](url + pq + fq)
  }

  private def enc(s: String) = URLEncoder.encode(s, "UTF-8")
}
