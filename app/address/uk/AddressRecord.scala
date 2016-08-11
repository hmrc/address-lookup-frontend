package address.uk

/**
  * Represents one address record. Arrays of these are returned from the address-lookup microservice.
  */
case class AddressRecord(
                          id: String,
                          uprn: Option[Long],
                          address: Address,
                          // ISO639-1 code, e.g. 'en' for English
                          // see https://en.wikipedia.org/wiki/List_of_ISO_639-1_codes
                          language: String) {

  def isValid = address.isValid && language.length == 2

  def truncatedAddress =
    if (address.longestLineLength <= Address.maxLineLength) this
    else AddressRecord(id, uprn, address.truncatedAddress, language)
}
