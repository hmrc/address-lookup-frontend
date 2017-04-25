
package services

import javax.inject.Singleton

import com.google.inject.ImplementedBy
import config.WSHttp
import model.ProposedAddress
import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.address.v2._
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpGet}
import services.AddressReputationFormats._
import uk.gov.hmrc.address.uk.Postcode

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

@ImplementedBy(classOf[AddressLookupAddressService])
trait AddressService {

  def find(postcode: String, filter: Option[String] = None)(implicit hc: HeaderCarrier): Future[Seq[ProposedAddress]]

}

@Singleton
class AddressLookupAddressService extends AddressService with ServicesConfig {

  val endpoint = baseUrl("address-reputation")

  val http: HttpGet = WSHttp

  override def find(postcode: String, filter: Option[String] = None)(implicit hc: HeaderCarrier): Future[Seq[ProposedAddress]] = {
    http.GET[List[AddressRecord]](s"$endpoint/v2/uk/addresses", Seq("postcode" -> Postcode.cleanupPostcode(postcode).get.toString, "filter" -> filter.getOrElse(""))).map { found =>
      found.map { addr =>
        ProposedAddress(
          addr.id,
          addr.address.postcode,
          addr.address.lines,
          addr.address.town,
          addr.address.county,
          if ("UK" == addr.address.country.code) Country("GB", "United Kingdom")
          else addr.address.country
        )
      }
    }
  }

}

object AddressReputationFormats {

  implicit val format0: OFormat[Country] = Json.format[Country]
  implicit val format1: OFormat[LocalCustodian] = Json.format[LocalCustodian]
  implicit val format2: OFormat[Address] = Json.format[Address]
  implicit val format3: OFormat[AddressRecord] = Json.format[AddressRecord]
  implicit val format4: OFormat[International] = Json.format[International]

}
