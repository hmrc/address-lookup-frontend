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

import config.FrontendGlobal
import config.ConfigHelper._
import play.api.Play
import uk.gov.hmrc.address.v2.AddressRecord
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.http.hooks.HttpHook
import uk.gov.hmrc.play.http.ws.WSGet
import uk.gov.hmrc.play.http._

import scala.concurrent.{ExecutionContext, Future}


class AddressLookupService(endpoint: String, applicationName: String)(implicit val ec: ExecutionContext) {

  private val url = s"$endpoint/v2/uk/addresses"

  private implicit val hc = HeaderCarrier()

  private val http = new WSGet {
    override val hooks = Seq[HttpHook]()
    val appName = applicationName
  }

  def findById(id: String): Future[Option[AddressRecord]] = {
    val uq = "/" + enc(id)
    import osgb.outmodel.v2.AddressReadable._
    http.GET[Option[AddressRecord]](url + uq).recover {
      case e: NotFoundException => None
    }
  }

  def findByUprn(uprn: Long): Future[List[AddressRecord]] = {
    val uq = "?uprn=" + uprn.toString
    import osgb.outmodel.v2.AddressReadable._
    http.GET[List[AddressRecord]](url + uq)
  }

  def findByPostcode(postcode: String, filter: Option[String]): Future[List[AddressRecord]] = {
    val pq = "?postcode=" + enc(postcode)
    val fq = filter.map(fi => "&filter=" + enc(fi)).getOrElse("")
    import osgb.outmodel.v2.AddressReadable._
    http.GET[List[AddressRecord]](url + pq + fq)
  }

  //TODO
  def searchFuzzy = ???

  private def enc(s: String) = URLEncoder.encode(s, "UTF-8")
}
