package utils

import model.ConfirmableAddress

object IsCountryUK {
  val countryCheck: Option[ConfirmableAddress] => Boolean = (selectedAddress: Option[ConfirmableAddress]) => {
    selectedAddress.fold(false)(
      _.address.country.fold(true)(_.code == "GB")
    )
  }

}
