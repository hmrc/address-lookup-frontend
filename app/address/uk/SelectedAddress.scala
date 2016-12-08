package address.uk

import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.address.v2._


case class SelectedAddress(normativeAddress: Option[AddressRecord],
                           userSuppliedAddress: Option[Address],
                           international: Option[International],
                           noFixedAddress: Boolean = false)


object SelectedAddress {
  implicit val format0: OFormat[Country] = Json.format[Country]
  implicit val format1: OFormat[LocalCustodian] = Json.format[LocalCustodian]
  implicit val format2: OFormat[Address] = Json.format[Address]
  implicit val format3: OFormat[AddressRecord] = Json.format[AddressRecord]
  implicit val format4: OFormat[International] = Json.format[International]
  implicit val format5: OFormat[SelectedAddress] = Json.format[SelectedAddress]
}

