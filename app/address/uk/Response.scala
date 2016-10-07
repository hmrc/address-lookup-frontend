package address.uk

import uk.gov.hmrc.address.v2.{Address, AddressRecord, LocalCustodian}


case class Response(selected: Option[AddressRecordWithEdits], noFixedAddress: Boolean)


case class AddressRecordWithEdits(id: String,
                                  uprn: Option[Long],
                                  address: Address,
                                  userEdited: Option[Address],
                                  localCustodian: Option[LocalCustodian],
                                  // ISO639-1 code, e.g. 'en' for English
                                  // see https://en.wikipedia.org/wiki/List_of_ISO_639-1_codes
                                  language: String)

object AddressRecordWithEdits {
  def apply(ar: AddressRecord, userEdited: Option[Address]): AddressRecordWithEdits =
    AddressRecordWithEdits(ar.id, ar.uprn, ar.address, userEdited, ar.localCustodian, ar.language)
}
