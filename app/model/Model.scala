/*
 * Copyright 2020 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package model

import model.JourneyConfigDefaults._
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import services.ForeignOfficeCountryService
import uk.gov.hmrc.address.v2.Country
import utils.PostcodeHelper

case class Lookup(filter: Option[String], postcode: String)

case class Timeout(timeoutAmount: Int,
                   timeoutUrl: String,
                   timeoutKeepAliveUrl: Option[String])

case class Select(addressId: String)

case class Edit(line1: String,
                line2: Option[String],
                line3: Option[String],
                town: String,
                postcode: String,
                countryCode: String = "GB") {

  def toConfirmableAddress(auditRef: String): ConfirmableAddress =
    ConfirmableAddress(
      auditRef,
      None,
      ConfirmableAddressDetails(
        Some(
          List(line1) ++ line2.map(_.toString).toList ++ line3
            .map(_.toString)
            .toList ++ List(town)
        ),
        if (postcode.isEmpty) None else Some(postcode),
        ForeignOfficeCountryService.find(code = countryCode)
      )
    )
}

//// decorator providing default config values; genuinely optional options are not decorated, only those that are required
//// but which have fallbacks so that client apps do not need to specify a value except to override the default are decorated
//case class ResolvedJourneyConfig(cfg: JourneyConfig) {
//  val continueUrl: String = cfg.continueUrl
//  val lookupPage = cfg.lookupPage.getOrElse(LookupPage())
//  val selectPage = cfg.selectPage.getOrElse(SelectPage())
//  val confirmPage = cfg.confirmPage.getOrElse(ConfirmPage())
//  val editPage = cfg.editPage.getOrElse(EditPage())
//  val isUKMode = cfg.ukMode.contains(true)
//
//  val homeNavHref: String = cfg.homeNavHref.getOrElse("http://www.hmrc.gov.uk")
//  val showPhaseBanner: Boolean = cfg.showPhaseBanner.getOrElse(false)
//  val alphaPhase: Boolean = cfg.alphaPhase.getOrElse(false)
//  val phase: String = cfg.showPhaseBanner match {
//    case Some(true) => if (alphaPhase) "alpha" else "beta"
//    case _          => ""
//  }
//  val phaseFeedbackLink: String = cfg.phaseFeedbackLink.getOrElse(
//    s"https://www.tax.service.gov.uk/contact/beta-feedback-unauthenticated?service=ALF"
//  )
//  val phaseBannerHtml: Option[String] = cfg.phaseBannerHtml
//  val showBackButtons: Boolean = cfg.showBackButtons.getOrElse(true)
//  val includeHMRCBranding: Boolean = cfg.includeHMRCBranding.getOrElse(true)
//  val allowedCountryCodes: Option[Set[String]] = cfg.allowedCountryCodes
//}
//
//case class ConfirmPage(title: Option[String] = None,
//                       heading: Option[String] = None,
//                       showSubHeadingAndInfo: Option[Boolean] = Some(false),
//                       infoSubheading: Option[String] = None,
//                       infoMessage: Option[String] = None,
//                       submitLabel: Option[String] = None,
//                       showSearchAgainLink: Option[Boolean] = Some(false),
//                       searchAgainLinkText: Option[String] = None,
//                       showChangeLink: Option[Boolean] = Some(true),
//                       changeLinkText: Option[String] = None,
//                       showConfirmChangeText: Option[Boolean] = Some(false),
//                       confirmChangeText: Option[String] = None)
//
//case class LookupPage(title: Option[String] = None,
//                      heading: Option[String] = None,
//                      filterLabel: Option[String] = None,
//                      postcodeLabel: Option[String] = None,
//                      submitLabel: Option[String] = None,
//                      resultLimitExceededMessage: Option[String] = None,
//                      noResultsFoundMessage: Option[String] = None,
//                      manualAddressLinkText: Option[String] = None)
//
//case class SelectPage(title: Option[String] = None,
//                      heading: Option[String] = None,
//                      headingWithPostcode: Option[String] = None,
//                      proposalListLabel: Option[String] = None,
//                      submitLabel: Option[String] = None,
//                      proposalListLimit: Option[Int] = None,
//                      showSearchAgainLink: Option[Boolean] = Some(false),
//                      searchAgainLinkText: Option[String] = None,
//                      editAddressLinkText: Option[String] = None)
//
//case class EditPage(title: Option[String] = None,
//                    heading: Option[String] = None,
//                    line1Label: Option[String] = None,
//                    line2Label: Option[String] = None,
//                    line3Label: Option[String] = None,
//                    townLabel: Option[String] = None,
//                    postcodeLabel: Option[String] = None,
//                    countryLabel: Option[String] = None,
//                    submitLabel: Option[String] = None)
//
//case class JourneyData(config: JourneyConfig,
//                       proposals: Option[Seq[ProposedAddress]] = None,
//                       selectedAddress: Option[ConfirmableAddress] = None,
//                       confirmedAddress: Option[ConfirmableAddress] = None) {
//
//  def resolvedConfig = ResolvedJourneyConfig(config)
//
//}
//
//case class Init(continueUrl: Option[String])
//
//case class JourneyConfig(
//  continueUrl: String,
//  lookupPage: Option[LookupPage] = Some(LookupPage()),
//  selectPage: Option[SelectPage] = Some(SelectPage()),
//  confirmPage: Option[ConfirmPage] = Some(ConfirmPage()),
//  editPage: Option[EditPage] = Some(EditPage()),
//  homeNavHref: Option[String] = None,
//  signOutHref: Option[String] = None,
//  serviceHref: Option[String] = None,
//  navTitle: Option[String] = None,
//  accessibilityFooterUrl: Option[String] = None,
//  additionalStylesheetUrl: Option[String] = None,
//  showPhaseBanner: Option[Boolean] = Some(false), // if phase banner is shown, it will default to "beta" unless ...
//  alphaPhase: Option[Boolean] = Some(false), // ... you set "alpha" to be true,
//  phaseFeedbackLink: Option[String] = None,
//  phaseBannerHtml: Option[String] = None,
//  showBackButtons: Option[Boolean] = Some(true),
//  includeHMRCBranding: Option[Boolean] = Some(true),
//  deskProServiceName: Option[String] = None,
//  allowedCountryCodes: Option[Set[String]] = None,
//  timeout: Option[Timeout] = None,
//  ukMode: Option[Boolean] = None
//) {
//
//  def isukMode: Boolean = ukMode.contains(true)
//
//}

case class ProposedAddress(addressId: String,
                           postcode: String,
                           lines: List[String] = List.empty,
                           town: Option[String] = None,
                           county: Option[String] = None,
                           country: Country = ForeignOfficeCountryService
                             .find(code = "GB")
                             .getOrElse(Country("GB", "United Kingdom"))) {

  def toConfirmableAddress(auditRef: String): ConfirmableAddress =
    ConfirmableAddress(
      auditRef,
      Some(addressId),
      ConfirmableAddressDetails(Some(toLines), Some(postcode), Some(country))
    )

  private def toLines: List[String] = {
    town match {
      case Some(town) => lines.take(3) ++ List(town)
      case None =>
        county match {
          case Some(county) => lines.take(3) ++ List(county)
          case None         => lines.take(4)
        }
    }
  }

  // TODO verify description format
  def toDescription: String = {
    lines.take(3).mkString(", ") + ", " +
      town.map(_ + ", ").getOrElse("") +
      county.map(_ + ", ").getOrElse("") +
      postcode
  }

}

case class ConfirmableAddress(auditRef: String,
                              id: Option[String] = None,
                              address: ConfirmableAddressDetails =
                                ConfirmableAddressDetails()) {

  def toEdit: Edit = address.toEdit

  def toDescription: String = address.toDescription

}

case class ConfirmableAddressDetails(
  lines: Option[List[String]] = None,
  postcode: Option[String] = None,
  country: Option[Country] = ForeignOfficeCountryService.find(code = "GB")
) {

  def toDescription: String = {
    (lines.getOrElse(List.empty) ++ postcode.toList ++ country.toList.map(
      _.name
    )).mkString(", ") + "."
  }

  def toEdit: Edit = {
    val el = editLines
    Edit(
      el._1,
      el._2,
      el._3,
      el._4,
      PostcodeHelper.displayPostcode(postcode),
      country.map(_.code).get
    )
  }

  def editLines: (String, Option[String], Option[String], String) = {
    val l1 = lines
      .map { lines =>
        lines.lift(0).getOrElse("")
      }
      .getOrElse("")
    val l2 = lines.flatMap(l => {
      if (l.length > 2) l.lift(1) else None
    })
    val l3 = lines.flatMap(l => {
      if (l.length > 3) l.lift(2) else None
    })
    val l4 = lines
      .flatMap(l => {
        if (l.length > 1) l.lastOption else None
      })
      .getOrElse("")
    (l1, l2, l3, l4)
  }

}

object CountryFormat {
  implicit val countryFormat: Format[Country] = Json.format[Country]
}

object ConfirmableAddressDetails {
  import CountryFormat._
  implicit val confirmableAddressDetailsFormat =
    Json.format[ConfirmableAddressDetails]
}

object ConfirmableAddress {
  implicit val confirmableAddressFormat = Json.format[ConfirmableAddress]
}

object ProposedAddress {
  import CountryFormat._
  implicit val proposedAddressFormat = Json.format[ProposedAddress]
}
//
//// JSON serialization companions
//object JourneyData {
//
//  implicit val confirmPageFormat = Json.format[ConfirmPage]
//  implicit val selectPageFormat = Json.format[SelectPage]
//  implicit val lookupPageFormat = Json.format[LookupPage]
//  implicit val editPageFormat = Json.format[EditPage]
//
//  implicit val timeoutFormat: Format[Timeout] = (
//    (JsPath \ "timeoutAmount").format[Int](min(120)) and
//      (JsPath \ "timeoutUrl").format[String] and
//      (JsPath \ "timeoutKeepAliveUrl").formatNullable[String]
//  )(Timeout.apply, unlift(Timeout.unapply))
//  implicit val journeyConfigFormat = Json.format[JourneyConfig]
//  implicit val journeyDataFormat = Json.format[JourneyData]
//
//}
