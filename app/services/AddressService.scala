
package services

import javax.inject.{Inject, Singleton}
import com.google.inject.ImplementedBy
import config.FrontendAppConfig
import model.ProposedAddress
import play.api.libs.json.{Json, OFormat}
import services.AddressReputationFormats._
import uk.gov.hmrc.address.uk.Postcode
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
      }.filterNot( a => isukMode && a.country.code != "GB")
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
