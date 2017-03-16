
package model

import play.api.libs.json.Json
import uk.gov.hmrc.address.v2.{Countries, Country}

case class Lookup(filter: Option[String], postcode: String)

case class Select(addressId: String)

case class Edit(line1: String, line2: Option[String], line3: Option[String], town: String, postcode: String, countryCode: Option[String]) {

  def toConfirmableAddress(auditRef: String): ConfirmableAddress = ConfirmableAddress(
    auditRef,
    None,
    ConfirmableAddressDetails(
      Some(List(line1) ++ line2.map(_.toString).toList ++ line3.map(_.toString).toList ++ List(town)),
      Some(postcode),
      countryCode.flatMap(code => Countries.find(code))
    )
  )

}

case class ConfirmPage(title: Option[String] = None,
                       heading: Option[String] = None,
                       infoSubheading: Option[String] = None,
                       infoMessage: Option[String] = None,
                       submitLabel: Option[String] = None)

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
                       selectPage: SelectPage = SelectPage(),
                       confirmPage: ConfirmPage = ConfirmPage())

case class ProposedAddress(addressId: String,
                           postcode: String,
                           lines: List[String] = List.empty,
                           town: Option[String] = None,
                           county: Option[String] = None,
                           country: Country = Countries.UK) {

  def toConfirmableAddress(auditRef: String): ConfirmableAddress = ConfirmableAddress(
    auditRef,
    Some(addressId),
    ConfirmableAddressDetails(
      Some(toLines),
      Some(postcode),
      Some(country)
    )
  )

  private def toLines: List[String] = {
    town match {
      case Some(town) => lines.take(3) ++ List(town)
      case None => county match {
        case Some(county) => lines.take(3) ++ List(county)
        case None => lines.take(4)
      }
    }
  }

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
                              id: Option[String] = None,
                              address: ConfirmableAddressDetails = ConfirmableAddressDetails()) {

  def toEdit: Edit = address.toEdit

  def toDescription: String = address.toDescription

}

case class ConfirmableAddressDetails(lines: Option[List[String]] = None,
                                     postcode: Option[String] = None,
                                     country: Option[Country] = None) {

  def toDescription: String = {
    (lines.getOrElse(List.empty) ++ postcode.toList ++ country.toList.map(_.name)).mkString(", ") + "."
  }

  // TODO refine
  def toEdit: Edit = Edit(
    lines.map { lines =>
      lines.lift(0).getOrElse("")
    }.getOrElse(""),
    lines.flatMap(_.lift(1)),
    lines.flatMap(_.lift(2)),
    lines.flatMap(_.lastOption).getOrElse(""),
    postcode.getOrElse(""),
    country.map(_.code)
  )

}

// JSON serialization companions

object JourneyData {

  implicit val countryFormat = Json.format[Country]
  implicit val confirmPageFormat = Json.format[ConfirmPage]
  implicit val selectPageFormat = Json.format[SelectPage]
  implicit val lookupPageFormat = Json.format[LookupPage]
  implicit val confirmableAddressDetailsFormat = Json.format[ConfirmableAddressDetails]
  implicit val confirmableAddressFormat = Json.format[ConfirmableAddress]
  implicit val proposedAddressFormat = Json.format[ProposedAddress]
  implicit val journeyDataFormat = Json.format[JourneyData]

}

object ConfirmableAddress {

  implicit val countryFormat = Json.format[Country]
  implicit val confirmableAddressDetailsFormat = Json.format[ConfirmableAddressDetails]
  implicit val confirmableAddressFormat = Json.format[ConfirmableAddress]

}
