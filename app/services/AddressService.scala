/*
 * Copyright 2022 HM Revenue & Customs
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

import address.v2._
import com.google.inject.ImplementedBy
import config.FrontendAppConfig
import forms.Postcode
import model.ProposedAddress
import play.api.libs.json.{Format, Json, Writes}
import services.AddressReputationFormats._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

@ImplementedBy(classOf[AddressLookupAddressService])
trait AddressService {
  def find(postcode: String, filter: Option[String] = None, isukMode: Boolean)(implicit hc: HeaderCarrier)
  : Future[Seq[ProposedAddress]]
}

@Singleton
class AddressLookupAddressService @Inject()(frontendAppConfig: FrontendAppConfig, http: HttpClient)(implicit val
ec: ExecutionContext) extends AddressService {

  val endpoint = frontendAppConfig.addressReputationEndpoint

  override def find(postcode: String, filter: Option[String] = None, isukMode: Boolean)(implicit hc: HeaderCarrier)
  : Future[Seq[ProposedAddress]] = {
    val lookupAddressByPostcode = LookupAddressByPostcode(Postcode.cleanupPostcode(postcode).get.toString, filter)
    http.POST[LookupAddressByPostcode, List[AddressRecord]](s"$endpoint/lookup", lookupAddressByPostcode)
      .map { found =>
        val results = found.map { addr =>
          ProposedAddress(
            addr.id,
            addr.uprn,
            addr.parentUprn,
            addr.usrn,
            addr.organisation,
            addr.address.postcode,
            addr.address.town,
            addr.address.lines,
            if ("UK" == addr.address.country.code) Country("GB", "United Kingdom")
            else addr.address.country,
            addr.poBox
          )
        }.filterNot(a => isukMode && a.country.code != "GB")

        results.sortWith((a, b) => {
          def sort(zipped: Seq[(Option[Int], Option[Int])]): Boolean = zipped match {
            case (Some(nA), Some(nB)) :: tail =>
              if (nA == nB) sort(tail) else nA < nB
            case (Some(_), None) :: _ => true
            case (None, Some(_)) :: _ => false
            case _ => mkString(a) < mkString(b)
          }

          sort(numbersIn(a).zipAll(numbersIn(b), None, None).toList)
        })
      }
  }

  def mkString(p: ProposedAddress) = p.lines.mkString(" ").toLowerCase()

  // Find numbers in proposed address in order of significance, from rightmost to leftmost.
  // Pad with None to ensure we never return an empty sequence
  def numbersIn(p: ProposedAddress): Seq[Option[Int]] =
    "([0-9]+)".r.findAllIn(mkString(p)).map(n => Try(n.toInt).toOption).toSeq.reverse :+ None
}

object AddressReputationFormats {
  import play.api.libs.functional.syntax._
  import play.api.libs.json.{JsPath, Reads}

  implicit val format0: Format[Country] = Json.format[Country]
  implicit val format1: Format[LocalCustodian] = Json.format[LocalCustodian]
  implicit val format2: Format[Address] = Json.format[Address]

  implicit val addressRecordReads: Reads[AddressRecord] = (
      (JsPath \ "id").read[String] and
          (JsPath \ "uprn").readNullable[Long] and
          (JsPath \ "parentUprn").readNullable[Long] and
          (JsPath \ "usrn").readNullable[Long] and
          (JsPath \ "organisation").readNullable[String] and
          (JsPath \ "address").read[Address] and
          (JsPath \ "language").read[String] and
          (JsPath \ "localCustodian").readNullable[LocalCustodian] and
          (JsPath \ "location").readNullable[Seq[BigDecimal]] and
          (JsPath \ "blpuState").readNullable[String] and
          (JsPath \ "logicalState").readNullable[String] and
          (JsPath \ "streetClassification").readNullable[String] and
          (JsPath \ "administrativeArea").readNullable[String] and
          (JsPath \ "poBox").readNullable[String]
      ) (AddressRecord.apply _)

  implicit val format3: Format[AddressRecord] = Format(addressRecordReads, Json.writes[AddressRecord])
  implicit val format4: Format[International] = Json.format[International]
}

case class LookupAddressByPostcode(postcode: String, filter: Option[String])

object LookupAddressByPostcode {
  implicit val writes: Writes[LookupAddressByPostcode] = Json.writes[LookupAddressByPostcode]
}
