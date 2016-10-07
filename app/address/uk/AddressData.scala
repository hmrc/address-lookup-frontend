package address.uk

import play.api.data.Form
import play.api.data.Forms._
import uk.gov.hmrc.address.v2.{Address, Countries, LocalCustodian}


object AddressForm {
  val addressForm = Form[AddressData] {
    mapping(
      "continue-url" -> text,
      "no-fixed-address" -> boolean,
      "house-name-number" -> optional(text),
      "postcode" -> optional(text),
      "radio-inline-group" -> optional(text),
      "address-lines" -> optional(text),
      "town" -> optional(text),
      "county" -> optional(text),
      "country-code" -> text
    )(AddressData.apply)(AddressData.unapply)
  }
}


case class AddressData(
                        continue: String,
                        noFixedAddress: Boolean,
                        nameNo: Option[String],
                        postcode: Option[String],
                        uprn: Option[String],
                        editedLines: Option[String], editedTown: Option[String],
                        editedCounty: Option[String],
                        countryCode: String
                      ) {

  def editedAddress: Option[Address] =
    if (editedLines.isDefined || editedTown.isDefined || editedCounty.isDefined) {
      Some(Address(
        editedLines.toList.flatMap(_.split("\n").map(_.trim)),
        editedTown.map(_.trim),
        editedCounty.map(_.trim),
        postcode.get.trim,
        None, Countries.find(countryCode).get))
    } else None
}


case class AddressRecordWithEdits(id: String,
                                  uprn: Option[Long],
                                  address: Address,
                                  userEdited: Address,
                                  localCustodian: Option[LocalCustodian],
                                  // ISO639-1 code, e.g. 'en' for English
                                  // see https://en.wikipedia.org/wiki/List_of_ISO_639-1_codes
                                  language: String)
