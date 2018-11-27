package utils

import model.ConfirmableAddress

object IsCountryUK {
  def countryCheck(selectedAddress: Option[ConfirmableAddress]): Boolean = {
   def isConfirmableAddressInUk(confirmableAddress: ConfirmableAddress): Boolean = {
     confirmableAddress.address.country
    .map (_.code == "GB")
    .getOrElse (true)
   }
    selectedAddress.map(isConfirmableAddressInUk).getOrElse(false)
  }
}