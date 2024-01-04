/*
 * Copyright 2024 HM Revenue & Customs
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

import play.api.libs.functional.syntax.{toFunctionalBuilderOps, unlift}
import play.api.libs.json._

case class InternationalLanguageLabels(selectPageLabels: Option[InternationalSelectPageLabels] = None,
                                       lookupPageLabels: Option[InternationalLookupPageLabels] = None,
                                       editPageLabels: Option[InternationalEditPageLabels] = None,
                                       confirmPageLabels: Option[InternationalConfirmPageLabels] = None)

case class InternationalSelectPageLabels(title: Option[String] = None,
                                         heading: Option[String] = None,
                                         headingWithPostcode: Option[String] = None,
                                         proposalListLabel: Option[String] = None,
                                         submitLabel: Option[String] = None,
                                         searchAgainLinkText: Option[String] = None,
                                         editAddressLinkText: Option[String] = None)

case class InternationalLookupPageLabels(title: Option[String] = None,
                                         heading: Option[String] = None,
                                         afterHeadingText: Option[String] = None,
                                         filterLabel: Option[String] = None,
                                         submitLabel: Option[String] = None,
                                         noResultsFoundMessage: Option[String] = None,
                                         resultLimitExceededMessage: Option[String] = None,
                                         manualAddressLinkText: Option[String] = None)

case class InternationalEditPageLabels(title: Option[String] = None,
                                       heading: Option[String] = None,
                                       line1Label: Option[String] = None,
                                       line2Label: Option[String] = None,
                                       line3Label: Option[String] = None,
                                       townLabel: Option[String] = None,
                                       postcodeLabel: Option[String] = None,
                                       countryLabel: Option[String] = None,
                                       submitLabel: Option[String] = None,
                                       organisationLabel: Option[String] = None)

case class InternationalConfirmPageLabels(title: Option[String] = None,
                                          heading: Option[String] = None,
                                          infoSubheading: Option[String] = None,
                                          infoMessage: Option[String] = None,
                                          submitLabel: Option[String] = None,
                                          searchAgainLinkText: Option[String] = None,
                                          changeLinkText: Option[String] = None,
                                          confirmChangeText: Option[String] = None)

object InternationalLanguageLabels {
  implicit val internationalSelectPageWrites: Writes[InternationalSelectPageLabels] = Json.writes[InternationalSelectPageLabels]
  implicit val internationalLookupPageWrites: Writes[InternationalLookupPageLabels] = Json.writes[InternationalLookupPageLabels]
  implicit val internationalEditPageWrites: Writes[InternationalEditPageLabels] = Json.writes[InternationalEditPageLabels]
  implicit val internationalConfirmPageWrites: Writes[InternationalConfirmPageLabels] = Json.writes[InternationalConfirmPageLabels]
  implicit val internationalLanguageLabelsWrites: Writes[InternationalLanguageLabels] = Json.writes[InternationalLanguageLabels]

  implicit val internationalSelectPageReads: Reads[InternationalSelectPageLabels] = Json.reads[InternationalSelectPageLabels]
  implicit val internationalLookupPageReads: Reads[InternationalLookupPageLabels] = Json.reads[InternationalLookupPageLabels]
  implicit val internationalEditPageReads: Reads[InternationalEditPageLabels] = Json.reads[InternationalEditPageLabels]
  implicit val internationalConfirmPageReads: Reads[InternationalConfirmPageLabels] = Json.reads[InternationalConfirmPageLabels]
  implicit val internationalLanguageLabelsReads: Reads[InternationalLanguageLabels] = Json.reads[InternationalLanguageLabels]
}

object InternationalLanguageLabelsForMessages {
  implicit def internationalSelectPageLabelsWrites: Writes[InternationalSelectPageLabels] = {
    (__ \ "international.selectPage.title").writeNullable[String]
      .and((__ \ "international.selectPage.heading").writeNullable[String])
      .and((__ \ "international.selectPage.headingWithPostcode").writeNullable[String])
      .and((__ \ "international.selectPage.proposalListLabel").writeNullable[String])
      .and((__ \ "international.selectPage.submitLabel").writeNullable[String])
      .and((__ \ "international.selectPage.searchAgainLinkText").writeNullable[String])
      .and((__ \ "international.selectPage.editAddressLinkText").writeNullable[String])(
        unlift(InternationalSelectPageLabels.unapply)
      )
  }

  implicit def internationalLookupPageLabelsWrites = {
    (__ \ "international.lookupPage.title").writeNullable[String]
      .and((__ \ "international.lookupPage.heading").writeNullable[String])
      .and((__ \ "international.lookupPage.afterHeadingText").writeNullable[String])
      .and((__ \ "international.lookupPage.filterLabel").writeNullable[String])
      .and((__ \ "international.lookupPage.submitLabel").writeNullable[String])
      .and((__ \ "international.lookupPage.noResultsFoundMessage").writeNullable[String])
      .and((__ \ "international.lookupPage.resultLimitExceededMessage").writeNullable[String])
      .and((__ \ "international.lookupPage.manualAddressLinkText").writeNullable[String])(
        unlift(InternationalLookupPageLabels.unapply)
      )
  }

  implicit def internationalEditPageLabelsWrites = {
    (__ \ "international.editPage.title").writeNullable[String]
      .and((__ \ "international.editPage.heading").writeNullable[String])
      .and((__ \ "international.editPage.line1Label").writeNullable[String])
      .and((__ \ "international.editPage.line2Label").writeNullable[String])
      .and((__ \ "international.editPage.line3Label").writeNullable[String])
      .and((__ \ "international.editPage.townLabel").writeNullable[String])
      .and((__ \ "international.editPage.postcodeLabel").writeNullable[String])
      .and((__ \ "international.editPage.countryLabel").writeNullable[String])
      .and((__ \ "international.editPage.submitLabel").writeNullable[String])
      .and((__ \ "international.editPage.organisationLabel").writeNullable[String])(
        unlift(InternationalEditPageLabels.unapply)
      )
  }

  implicit def internationalConfirmPageLabelsWrites: OWrites[InternationalConfirmPageLabels] = {
    (__ \ "international.confirmPage.title").writeNullable[String]
      .and((__ \ "international.confirmPage.heading").writeNullable[String])
      .and((__ \ "international.confirmPage.infoSubheading").writeNullable[String])
      .and((__ \ "international.confirmPage.infoMessage").writeNullable[String])
      .and((__ \ "international.confirmPage.submitLabel").writeNullable[String])
      .and((__ \ "international.confirmPage.searchAgainLinkText").writeNullable[String])
      .and((__ \ "international.confirmPage.changeLinkText").writeNullable[String])
      .and((__ \ "international.confirmPage.confirmChangeText").writeNullable[String])(
        unlift(InternationalConfirmPageLabels.unapply)
      )
  }

  implicit def internationalLanguageLabelsWrites: OWrites[InternationalLanguageLabels] = {
    ((__).writeNullable[InternationalSelectPageLabels])
      .and((__).writeNullable[InternationalLookupPageLabels])
      .and((__).writeNullable[InternationalEditPageLabels])
      .and((__).writeNullable[InternationalConfirmPageLabels])(
        unlift(InternationalLanguageLabels.unapply)
      )
  }
}