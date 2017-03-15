
package model

import play.api.libs.json.Json
import uk.gov.hmrc.address.v2.{Countries, Country}

case class Lookup(filter: Option[String], postcode: String)

case class Select(addressId: String)

case class Edit(line1: String, line2: Option[String], line3: Option[String], town: String, postcode: String, countryCode: Option[String]) {

  def toConfirmableAddress: ConfirmableAddress = ConfirmableAddress("TODO")

}

case class LookupPage(title: Option[String] = None,
                      heading: Option[String] = None,
                      filterLabel: Option[String] = None,
                      postcodeLabel: Option[String] = None,
                      submitLabel: Option[String] = None,
                      resultLimitExceededMessage: Option[String] = None,
                      noResultsFoundMessage: Option[String] = None)

case class SelectPage(title: Option[String] = None,
                      heading: Option[String] = None,
                      proposalListLabel: Option[String] = None,
                      submitLabel: Option[String] = None,
                      proposalListLimit: Option[Int] = None,
                      noProposalsFoundMessage: Option[String] = None)

case class JourneyData(continueUrl: String,
                       proposals: Option[Seq[ProposedAddress]] = None,
                       selectedAddress: Option[ConfirmableAddress] = None,
                       confirmedAddress: Option[ConfirmableAddress] = None,
                       lookupPage: LookupPage = LookupPage(),
                       selectPage: SelectPage = SelectPage())

case class ProposedAddress(addressId: String,
                           postcode: String,
                           lines: List[String] = List.empty,
                           town: Option[String] = None,
                           county: Option[String] = None,
                           country: Country = Countries.UK) {

  def toConfirmableAddress: ConfirmableAddress = ConfirmableAddress("TODO")

  // TODO verify description format
  def toDescription: String = {
    lines.take(3).mkString(", ") + ", " +
      town.map(_ + ", ").getOrElse("") +
      county.map(_ + ", ").getOrElse("") +
      postcode + ", " +
      country.name
  }

}

case class ConfirmableAddress(auditRef: String,
                              id: Option[String] = None) {

  def toEdit: Edit = ???

}

case class ConfirmableAddressDetails(lines: Option[List[String]] = None,
                                     postcode: Option[String] = None,
                                     country: Option[Country] = None)

// JSON serialization companions

object JourneyData {

  implicit val countryFormat = Json.format[Country]
  implicit val selectPageFormat = Json.format[SelectPage]
  implicit val lookupPageFormat = Json.format[LookupPage]
  implicit val confirmableAddressFormat = Json.format[ConfirmableAddress]
  implicit val proposedAddressFormat = Json.format[ProposedAddress]
  implicit val journeyDataFormat = Json.format[JourneyData]

}

object ConfirmableAddress {

  implicit val confirmableAddressFormat = Json.format[ConfirmableAddress]

}
