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

import com.google.inject.ImplementedBy
import config.FrontendAppConfig
import forms.Postcode
import javax.inject.{Inject, Singleton}
import model.ProposedAddress
import play.api.libs.json.{Json, OFormat}
import services.AddressReputationFormats._
import uk.gov.hmrc.address.v2._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[AddressLookupAddressService])
trait AddressService {
  def find(postcode: String, filter: Option[String] = None, isukMode: Boolean)(implicit hc: HeaderCarrier): Future[Seq[ProposedAddress]]
}

@Singleton
class AddressLookupAddressService @Inject()(frontendAppConfig: FrontendAppConfig, http: HttpClient)(implicit val ec: ExecutionContext) extends AddressService {

  val endpoint = frontendAppConfig.addressReputationEndpoint

  override def find(postcode: String, filter: Option[String] = None, isukMode: Boolean)(implicit hc: HeaderCarrier): Future[Seq[ProposedAddress]] = {
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
      }.filterNot(a => isukMode && a.country.code != "GB")

      results.sortWith(superCleverAlgorithm)
    }
  }

  private def superCleverAlgorithm(addressA: ProposedAddress, addressB: ProposedAddress) =
    chooseComparisonMethod(addressA, addressB) match {
      case CompareAddressesAsStrings(a, b) => a < b
      case CompareAddressesWithOneNumber(a, b, a1, b1) =>
        if (a1 != b1) a1 < b1 else a < b
      case CompareAddressesWithTwoNumbers(a, b, a1, a2, b1, b2) =>
        if (a2 != b2) a2 < b2
        else if (a1 != b1) a1 < b1
        else a < b
      case CompareWhenOnlyOneAddressContainsANumber(aSortsBeforeB) => aSortsBeforeB
    }

  private def chooseComparisonMethod(a: ProposedAddress, b: ProposedAddress): AddressComparisonMethod = {
    val pattern = "([0-9]+)".r
    val aString = a.lines.mkString(" ").toLowerCase()
    val bString = b.lines.mkString(" ").toLowerCase()

    val numbersInA = pattern.findAllIn(aString).toSeq.take(2)
    val numbersInB = pattern.findAllIn(bString).toSeq.take(2)

    (numbersInA, numbersInB) match {
      case (Seq(), Seq()) =>
        CompareAddressesAsStrings(aString, bString)
      case (Seq(a1, a2), Seq(b1, b2)) =>
        CompareAddressesWithTwoNumbers(aString, bString, a1.toInt, a2.toInt, b1.toInt, b2.toInt)
      case (sa, sb) if sa.nonEmpty && sb.nonEmpty =>
        CompareAddressesWithOneNumber(aString, bString, sa.last.toInt, sb.last.toInt)
      case (sa, _) =>
        // We only reach this case when either one side or the other contains one number, but not both
        // We want the address containing a number to sort first, so if sa is nonempty we consider this a < b
        CompareWhenOnlyOneAddressContainsANumber(sa.nonEmpty)
    }
  }

  trait AddressComparisonMethod

  case class CompareAddressesWithTwoNumbers(a: String, b: String, a1: Int, a2: Int, b1: Int, b2: Int) extends AddressComparisonMethod

  case class CompareAddressesWithOneNumber(a: String, b: String, a1: Int, b1: Int) extends AddressComparisonMethod

  case class CompareAddressesAsStrings(a: String, b: String) extends AddressComparisonMethod

  case class CompareWhenOnlyOneAddressContainsANumber(aSortsBeforeB: Boolean) extends AddressComparisonMethod
}

object AddressReputationFormats {
  implicit val format0: OFormat[Country] = Json.format[Country]
  implicit val format1: OFormat[LocalCustodian] = Json.format[LocalCustodian]
  implicit val format2: OFormat[Address] = Json.format[Address]
  implicit val format3: OFormat[AddressRecord] = Json.format[AddressRecord]
  implicit val format4: OFormat[International] = Json.format[International]
}
