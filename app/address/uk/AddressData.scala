package address.uk

import play.api.data.Form
import play.api.data.Forms._
import uk.gov.hmrc.address.v2.{Address, Countries}


object AddressForm {
  val addressForm = Form[AddressData] {
    mapping(
      "guid" -> text,
      "continue-url" -> text,
      "no-fixed-address" -> boolean,
      "house-name-number" -> optional(text),
      "postcode" -> optional(text),
      "prev-house-name-number" -> optional(text),
      "prev-postcode" -> optional(text),
      "radio-inline-group" -> optional(text),
      "address-lines" -> optional(text),
      "town" -> optional(text),
      "county" -> optional(text),
      "country-code" -> optional(text)
    )(AddressData.apply)(AddressData.unapply)
  }
}


case class AddressData(
                        guid: String,
                        continue: String,
                        noFixedAddress: Boolean = false,
                        nameNo: Option[String] = None,
                        postcode: Option[String] = None,
                        prevNameNo: Option[String] = None,
                        prevPostcode: Option[String] = None,
                        uprn: Option[String] = None,
                        editedLines: Option[String] = None,
                        editedTown: Option[String] = None,
                        editedCounty: Option[String] = None,
                        countryCode: Option[String] = None
                      ) {

  def hasBeenUpdated =
    (prevNameNo.isDefined && prevNameNo.get != nameNo.get) ||
    (prevPostcode.isDefined && prevPostcode.get != postcode.get)

  def editedAddress: Option[Address] =
    if (editedLines.isDefined || editedTown.isDefined || editedCounty.isDefined) {
      Some(Address(
        editedLines.toList.flatMap(_.split("\n").map(_.trim)),
        editedTown.map(_.trim),
        editedCounty.map(_.trim),
        postcode.get.trim,
        None, Countries.find(countryCode.get).get))
    } else None
}
