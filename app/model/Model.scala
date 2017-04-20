
package model

import play.api.libs.json.Json
import services.ForeignOfficeCountryService
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
      countryCode.flatMap(code => ForeignOfficeCountryService.find(code))
    )
  )

}

case class ConfirmPage(title: Option[String] = None,
                       heading: Option[String] = None,
                       showSubHeadingAndInfo: Boolean = false,
                       infoSubheading: Option[String] = None,
                       infoMessage: Option[String] = None,
                       submitLabel: Option[String] = None,
                       showSearchAgainLink: Boolean = false,
                       searchAgainLinkText: Option[String] = None)

case class LookupPage(title: Option[String] = None,
                      heading: Option[String] = None,
                      filterLabel: Option[String] = None,
                      postcodeLabel: Option[String] = None,
                      submitLabel: Option[String] = None,
                      resultLimitExceededMessage: Option[String] = None,
                      noResultsFoundMessage: Option[String] = None,
                      manualAddressLinkText: Option[String] = None)

case class SelectPage(title: Option[String] = None,
                      heading: Option[String] = None,
                      proposalListLabel: Option[String] = None,
                      submitLabel: Option[String] = None,
                      proposalListLimit: Option[Int] = None,
                      showSearchAgainLink: Boolean = false,
                      searchAgainLinkText: Option[String] = None,
                      editAddressLinkText: Option[String] = None)

case class EditPage(title: Option[String] = None,
                    heading: Option[String] = None,
                    line1Label: Option[String] = None,
                    line2Label: Option[String] = None,
                    line3Label: Option[String] = None,
                    townLabel: Option[String] = None,
                    postcodeLabel: Option[String] = None,
                    countryLabel: Option[String] = None,
                    submitLabel: Option[String] = None,
                    showSearchAgainLink: Boolean = false,
                    searchAgainLinkText: Option[String] = None)

case class JourneyData(continueUrl: String,
                       proposals: Option[Seq[ProposedAddress]] = None,
                       selectedAddress: Option[ConfirmableAddress] = None,
                       confirmedAddress: Option[ConfirmableAddress] = None,
                       lookupPage: LookupPage = LookupPage(),
                       selectPage: SelectPage = SelectPage(),
                       confirmPage: ConfirmPage = ConfirmPage(),
                       editPage: EditPage = EditPage(),
                       homeNavHref: Option[String] = None,
                       navTitle: Option[String] = None,
                       additionalStylesheetUrl: Option[String] = None,
                       showPhaseBanner: Boolean = false, // if phase banner is shown, it will default to "beta" unless ...
                       alphaPhase: Boolean = false, // ... you set "alpha" to be true
                       phaseFeedbackLink: Option[String] = None,
                       showBackButtons: Boolean = false)

case class ProposedAddress(addressId: String,
                           postcode: String,
                           lines: List[String] = List.empty,
                           town: Option[String] = None,
                           county: Option[String] = None,
                           country: Country = ForeignOfficeCountryService.find("GB").getOrElse(Country("GB", "United Kingdom"))) {

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
                                     country: Option[Country] = ForeignOfficeCountryService.find("GB")) {

  def toDescription: String = {
    (lines.getOrElse(List.empty) ++ postcode.toList ++ country.toList.map(_.name)).mkString(", ") + "."
  }

  def toEdit: Edit = {
    val el = editLines
    Edit(el._1, el._2, el._3, el._4, postcode.getOrElse(""), country.map(_.code))
  }

  def editLines: (String, Option[String], Option[String], String) = {
    val l1 = lines.map { lines =>
      lines.lift(0).getOrElse("")
    }.getOrElse("")
    val l2 = lines.flatMap(l => {
      if (l.length > 2) l.lift(1) else None
    })
    val l3 = lines.flatMap(l => {
      if (l.length > 3) l.lift(2) else None
    })
    val l4 = lines.flatMap(l => {
      if (l.length > 1) l.lastOption else None
    }).getOrElse("")
    (l1, l2, l3, l4)
  }

}

// JSON serialization companions

object JourneyData {

  implicit val countryFormat = Json.format[Country]
  implicit val confirmPageFormat = Json.format[ConfirmPage]
  implicit val selectPageFormat = Json.format[SelectPage]
  implicit val lookupPageFormat = Json.format[LookupPage]
  implicit val editPageFormat = Json.format[EditPage]
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
