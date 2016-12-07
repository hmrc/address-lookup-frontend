package address.uk

import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.address.v2.{Address, AddressRecord, Country, LocalCustodian}


case class AddressRecordWithEdits(normativeAddress: Option[AddressRecord],
                                  userSuppliedAddress: Option[Address],
                                  noFixedAddress: Boolean)


object AddressRecordWithEdits {
  implicit val f0: OFormat[Country] = Json.format[Country]
  implicit val f1: OFormat[LocalCustodian] = Json.format[LocalCustodian]
  implicit val f2: OFormat[Address] = Json.format[Address]
  implicit val f3: OFormat[AddressRecord] = Json.format[AddressRecord]
  implicit val f4: OFormat[AddressRecordWithEdits] = Json.format[AddressRecordWithEdits]
}

