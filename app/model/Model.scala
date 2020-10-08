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

import forms.Postcode
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
        if (postcode.isEmpty) None
        else if (countryCode == "GB") Postcode.cleanupPostcode(postcode).map(_.toString)
        else Some(postcode),
        ForeignOfficeCountryService.find(code = countryCode)
      )
    )
}

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
