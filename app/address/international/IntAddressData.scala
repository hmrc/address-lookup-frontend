package address.international

import play.api.data.Form
import play.api.data.Forms._


object IntAddressForm {
  val addressForm = Form[IntAddressData] {
    mapping(
      "guid" -> text,
      "continue-url" -> text,
      "address" -> optional(text),
      "prev-address" -> optional(text),
      "country" -> optional(text),
      "prev-country" -> optional(text)
    )(IntAddressData.apply)(IntAddressData.unapply)
  }
}


case class IntAddressData(
                           guid: String,
                           continue: String,
                           address: Option[String] = None,
                           previousAddress: Option[String] = None,
                           country: Option[String] = None,
                           previousCountry: Option[String] = None
                         ) {

    def hasBeenUpdated: Boolean = (previousAddress != address) || (previousCountry != country)
}
