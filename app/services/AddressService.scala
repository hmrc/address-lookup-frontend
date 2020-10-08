/*
 * Copyright 2020 HM Revenue & Customs
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

package services

import javax.inject.{Inject, Singleton}
import com.google.inject.ImplementedBy
import config.FrontendAppConfig
import forms.Postcode
import model.ProposedAddress
import play.api.libs.json.{Json, OFormat}
import services.AddressReputationFormats._
import uk.gov.hmrc.address.v2._

import scala.concurrent.{ExecutionContext, Future}
import uk.gov.hmrc.http.{HeaderCarrier, HttpGet}
import uk.gov.hmrc.play.bootstrap.http.HttpClient

@ImplementedBy(classOf[AddressLookupAddressService])
trait AddressService {
  def find(postcode: String, filter: Option[String] = None,isukMode:Boolean)(implicit hc: HeaderCarrier): Future[Seq[ProposedAddress]]
}

@Singleton
class AddressLookupAddressService @Inject()(frontendAppConfig: FrontendAppConfig, http: HttpClient)(implicit val ec: ExecutionContext) extends AddressService {

  val endpoint = frontendAppConfig.addressReputationEndpoint

  override def find(postcode: String, filter: Option[String] = None,isukMode:Boolean)(implicit hc: HeaderCarrier): Future[Seq[ProposedAddress]] = {
    http.GET[List[AddressRecord]](s"$endpoint/v2/uk/addresses", Seq("postcode" ->
      Postcode.cleanupPostcode(postcode).get.toString,
      "filter" -> filter.getOrElse(""))).map { found =>
      val results = found.map { addr =>
        ProposedAddress(
          addr.id,
          addr.address.postcode,
          addr.address.lines,
          addr.address.town,
          addr.address.county,
          if ("UK" == addr.address.country.code) Country("GB", "United Kingdom")
          else addr.address.country
        )
      }.filterNot( a => isukMode && a.country.code != "GB")

      sortAddresses(results)
    }
  }

  private def sortAddresses(proposedAddresses: Seq[ProposedAddress]) = proposedAddresses.sortWith((a, b) => {
    val aString = a.lines.mkString(" ").toLowerCase()
    val bString = b.lines.mkString(" ").toLowerCase()

    val pattern = "([0-9]+)".r

    val d1 = pattern.findAllIn(aString).toSeq.take(2)
    val d2 = pattern.findAllIn(bString).toSeq.take(2)

    (d1, d2) match {
      case (Seq(a1, a2), Seq(b1, b2)) =>
        if (a2.toInt != b2.toInt) a2 < b2
        else if (a1.toInt != b1.toInt) a1 < b1
        else aString < bString
      case (sa, sb) if sa.nonEmpty && sb.nonEmpty =>
        if (sa.last.toInt != sb.last.toInt) sa.last.toInt < sb.last.toInt
        else aString < bString
      case _ =>
        aString < bString
    }
  })
}

object AddressReputationFormats {

  implicit val format0: OFormat[Country] = Json.format[Country]
  implicit val format1: OFormat[LocalCustodian] = Json.format[LocalCustodian]
  implicit val format2: OFormat[Address] = Json.format[Address]
  implicit val format3: OFormat[AddressRecord] = Json.format[AddressRecord]
  implicit val format4: OFormat[International] = Json.format[International]

}
