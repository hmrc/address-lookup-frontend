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

import model.v2.JourneyLabels
import play.api.libs.functional.syntax.{toFunctionalBuilderOps, unlift}
import play.api.libs.json._

case class LanguageLabels(appLevelLabels: Option[AppLevelLabels] = None,
                          selectPageLabels: Option[SelectPageLabels] = None,
                          lookupPageLabels: Option[LookupPageLabels] = None,
                          editPageLabels: Option[EditPageLabels] = None,
                          confirmPageLabels: Option[ConfirmPageLabels] = None,
                          countryPickerLabels: Option[CountryPickerPageLabels] = None,
                          international: Option[InternationalLanguageLabels] = None,
                          otherLabels: Option[JsValue] = None)

case class AppLevelLabels(navTitle: Option[String] = None,
                          phaseBannerHtml: Option[String] = None)

case class SelectPageLabels(title: Option[String] = None,
                            heading: Option[String] = None,
                            headingWithPostcode: Option[String] = None,
                            proposalListLabel: Option[String] = None,
                            submitLabel: Option[String] = None,
                            searchAgainLinkText: Option[String] = None,
                            editAddressLinkText: Option[String] = None)

case class LookupPageLabels(title: Option[String] = None,
                            var titleUkMode: Option[String] = None,
                            heading: Option[String] = None,
                            var headingUkMode: Option[String] = None,
                            afterHeadingText: Option[String] = None,
                            filterLabel: Option[String] = None,
                            postcodeLabel: Option[String] = None,
                            var postcodeLabelUkMode: Option[String] = None,
                            submitLabel: Option[String] = None,
                            noResultsFoundMessage: Option[String] = None,
                            resultLimitExceededMessage: Option[String] = None,
                            manualAddressLinkText: Option[String] = None) {

  titleUkMode = titleUkMode.orElse(title)
  headingUkMode = headingUkMode.orElse(heading)
  postcodeLabelUkMode = postcodeLabelUkMode.orElse(postcodeLabel)
}

case class EditPageLabels(title: Option[String] = None,
                          heading: Option[String] = None,
                          line1Label: Option[String] = None,
                          line2Label: Option[String] = None,
                          line3Label: Option[String] = None,
                          townLabel: Option[String] = None,
                          postcodeLabel: Option[String] = None,
                          var postcodeLabelUkMode: Option[String] = None,
                          countryLabel: Option[String] = None,
                          submitLabel: Option[String] = None,
                          organisationLabel: Option[String] = None) {
  postcodeLabelUkMode = postcodeLabelUkMode.orElse(postcodeLabel)
}

case class ConfirmPageLabels(title: Option[String] = None,
                             heading: Option[String] = None,
                             infoSubheading: Option[String] = None,
                             infoMessage: Option[String] = None,
                             submitLabel: Option[String] = None,
                             searchAgainLinkText: Option[String] = None,
                             changeLinkText: Option[String] = None,
                             confirmChangeText: Option[String] = None)

case class CountryPickerPageLabels(title: Option[String] = None,
                                   heading: Option[String] = None,
                                   countryLabel: Option[String] = None,
                                   submitLabel: Option[String] = None)

object LanguageLabels {

  implicit val appLevelWrites: Writes[AppLevelLabels] = Json.writes[AppLevelLabels]
  implicit val selectPageWrites: Writes[SelectPageLabels] = Json.writes[SelectPageLabels]
  implicit val lookupPageWrites: Writes[LookupPageLabels] = Json.writes[LookupPageLabels]
  implicit val editPageWrites: Writes[EditPageLabels] = Json.writes[EditPageLabels]
  implicit val confirmPageWrites: Writes[ConfirmPageLabels] = Json.writes[ConfirmPageLabels]
  implicit val countryPickerPageWrites: Writes[CountryPickerPageLabels] = Json.writes[CountryPickerPageLabels]
  implicit val languageLabelsWrites: Writes[LanguageLabels] = Json.writes[LanguageLabels]

  implicit val appLevelReads: Reads[AppLevelLabels] = Json.reads[AppLevelLabels]
  implicit val selectPageReads: Reads[SelectPageLabels] = Json.reads[SelectPageLabels]
  implicit val lookupPageReads: Reads[LookupPageLabels] = Json.reads[LookupPageLabels]
  implicit val editPageReads: Reads[EditPageLabels] = Json.reads[EditPageLabels]
  implicit val confirmPageReads: Reads[ConfirmPageLabels] = Json.reads[ConfirmPageLabels]
  implicit val countryPickerPageReads: Reads[CountryPickerPageLabels] = Json.reads[CountryPickerPageLabels]
  implicit val languageLabelsReads: Reads[LanguageLabels] = Json.reads[LanguageLabels]

  def unapply(labels: LanguageLabels): Option[(Option[AppLevelLabels], Option[SelectPageLabels], Option[LookupPageLabels], Option[EditPageLabels], Option[ConfirmPageLabels], Option[CountryPickerPageLabels], Option[InternationalLanguageLabels], Option[JsValue])] = {
    Some((labels.appLevelLabels, labels.selectPageLabels, labels.lookupPageLabels, labels.editPageLabels, labels.confirmPageLabels, labels.countryPickerLabels, labels.international, labels.otherLabels))
  }
}

object LanguageLabelsForMessages {
  implicit def appLevelLabelsWrites: Writes[AppLevelLabels] = {
    (__ \ "navTitle").writeNullable[String]
      .and((__ \ "phaseBannerHtml").writeNullable[String])(
        unlift(AppLevelLabels.unapply)
      )
  }

  implicit def selectPageLabelsWrites: Writes[SelectPageLabels] = {
    (__ \ "selectPage.title").writeNullable[String]
      .and((__ \ "selectPage.heading").writeNullable[String])
      .and((__ \ "selectPage.headingWithPostcode").writeNullable[String])
      .and((__ \ "selectPage.proposalListLabel").writeNullable[String])
      .and((__ \ "selectPage.submitLabel").writeNullable[String])
      .and((__ \ "selectPage.searchAgainLinkText").writeNullable[String])
      .and((__ \ "selectPage.editAddressLinkText").writeNullable[String])(
        unlift(SelectPageLabels.unapply)
      )
  }

  implicit def lookupPageLabelsWrites: OWrites[LookupPageLabels] = {
    (__ \ "lookupPage.title").writeNullable[String]
      .and((__ \ "lookupPage.title.ukMode").writeNullable[String])
      .and((__ \ "lookupPage.heading").writeNullable[String])
      .and((__ \ "lookupPage.heading.ukMode").writeNullable[String])
      .and((__ \ "lookupPage.afterHeadingText").writeNullable[String])
      .and((__ \ "lookupPage.filterLabel").writeNullable[String])
      .and((__ \ "lookupPage.postcodeLabel").writeNullable[String])
      .and((__ \ "lookupPage.postcodeLabel.ukMode").writeNullable[String])
      .and((__ \ "lookupPage.submitLabel").writeNullable[String])
      .and((__ \ "lookupPage.noResultsFoundMessage").writeNullable[String])
      .and((__ \ "lookupPage.resultLimitExceededMessage").writeNullable[String])
      .and((__ \ "lookupPage.manualAddressLinkText").writeNullable[String])(
        unlift(LookupPageLabels.unapply)
      )
  }

  implicit def editPageLabelsWrites: OWrites[EditPageLabels] = {
    (__ \ "editPage.title").writeNullable[String]
      .and((__ \ "editPage.heading").writeNullable[String])
      .and((__ \ "editPage.line1Label").writeNullable[String])
      .and((__ \ "editPage.line2Label").writeNullable[String])
      .and((__ \ "editPage.line3Label").writeNullable[String])
      .and((__ \ "editPage.townLabel").writeNullable[String])
      .and((__ \ "editPage.postcodeLabel").writeNullable[String])
      .and((__ \ "editPage.postcodeLabel.ukMode").writeNullable[String])
      .and((__ \ "editPage.countryLabel").writeNullable[String])
      .and((__ \ "editPage.submitLabel").writeNullable[String])
      .and((__ \ "editPage.organisationLabel").writeNullable[String])(
        unlift(EditPageLabels.unapply)
      )
  }

  implicit def confirmPageLabelsWrites: OWrites[ConfirmPageLabels] = {
    (__ \ "confirmPage.title").writeNullable[String]
      .and((__ \ "confirmPage.heading").writeNullable[String])
      .and((__ \ "confirmPage.infoSubheading").writeNullable[String])
      .and((__ \ "confirmPage.infoMessage").writeNullable[String])
      .and((__ \ "confirmPage.submitLabel").writeNullable[String])
      .and((__ \ "confirmPage.searchAgainLinkText").writeNullable[String])
      .and((__ \ "confirmPage.changeLinkText").writeNullable[String])
      .and((__ \ "confirmPage.confirmChangeText").writeNullable[String])(
        unlift(ConfirmPageLabels.unapply)
      )
  }

  implicit def countryPickerLabelsWrites: OWrites[CountryPickerPageLabels] = {
    (__ \ "countryPickerPage.title").writeNullable[String]
      .and((__ \ "countryPickerPage.heading").writeNullable[String])
      .and((__ \ "countryPickerPage.countryLabel").writeNullable[String])
      .and((__ \ "countryPickerPage.submitLabel").writeNullable[String])(
        unlift(CountryPickerPageLabels.unapply)
      )
  }

  import InternationalLanguageLabelsForMessages._

  implicit def languageLabelsWrites: OWrites[LanguageLabels] = {
    (__).writeNullable[AppLevelLabels]
      .and((__).writeNullable[SelectPageLabels])
      .and((__).writeNullable[LookupPageLabels])
      .and((__).writeNullable[EditPageLabels])
      .and((__).writeNullable[ConfirmPageLabels])
      .and((__).writeNullable[CountryPickerPageLabels])
      .and((__).writeNullable[InternationalLanguageLabels])
      .and((__).writeNullable[JsValue])(
        unlift(LanguageLabels.unapply)
      )
  }

  implicit val writes: OWrites[JourneyLabels] = Json.writes[JourneyLabels]
}

object AppLevelLabels {
  implicit val format: Format[AppLevelLabels] = Json.format[AppLevelLabels]
  
  def unapply(appLevelLabels: AppLevelLabels): Option[(Option[String], Option[String])] = {
    Some((appLevelLabels.navTitle, appLevelLabels.phaseBannerHtml))
  }
}

object SelectPageLabels {
  implicit val format: Format[SelectPageLabels] = Json.format[SelectPageLabels]
  
  def unapply(selectPageLabels: SelectPageLabels): Option[(Option[String], Option[String], Option[String], Option[String], Option[String], Option[String], Option[String])] = {
    Some((
      selectPageLabels.title,
      selectPageLabels.heading,
      selectPageLabels.headingWithPostcode,
      selectPageLabels.proposalListLabel,
      selectPageLabels.submitLabel,
      selectPageLabels.searchAgainLinkText,
      selectPageLabels.editAddressLinkText
    ))
  }
}

object LookupPageLabels {
  implicit val format: Format[LookupPageLabels] = Json.format[LookupPageLabels]
  
  def unapply(lookupPageLabels: LookupPageLabels): Option[(Option[String], Option[String], Option[String], Option[String], Option[String], Option[String], Option[String], Option[String], Option[String], Option[String], Option[String], Option[String])] = {
    Some((
      lookupPageLabels.title,
      lookupPageLabels.titleUkMode,
      lookupPageLabels.heading,
      lookupPageLabels.headingUkMode,
      lookupPageLabels.afterHeadingText,
      lookupPageLabels.filterLabel,
      lookupPageLabels.postcodeLabel,
      lookupPageLabels.postcodeLabelUkMode,
      lookupPageLabels.submitLabel,
      lookupPageLabels.noResultsFoundMessage,
      lookupPageLabels.resultLimitExceededMessage,
      lookupPageLabels.manualAddressLinkText
    ))
  }
}

object EditPageLabels {
  implicit val format: Format[EditPageLabels] = Json.format[EditPageLabels]
  
  def unapply(editPageLabels: EditPageLabels): Option[(Option[String], Option[String], Option[String], Option[String], Option[String], Option[String], Option[String], Option[String], Option[String], Option[String], Option[String])] = {
    Some((
      editPageLabels.title,
      editPageLabels.heading,
      editPageLabels.line1Label,
      editPageLabels.line2Label,
      editPageLabels.line3Label,
      editPageLabels.townLabel,
      editPageLabels.postcodeLabel,
      editPageLabels.postcodeLabelUkMode,
      editPageLabels.countryLabel,
      editPageLabels.submitLabel,
      editPageLabels.organisationLabel
    ))
  }
}

object ConfirmPageLabels {
  implicit val format: Format[ConfirmPageLabels] =
    Json.format[ConfirmPageLabels]
    
  def unapply(confirmPageLabels: ConfirmPageLabels): Option[(Option[String], Option[String], Option[String], Option[String], Option[String], Option[String], Option[String], Option[String])] = {
    Some((
      confirmPageLabels.title,
      confirmPageLabels.heading,
      confirmPageLabels.infoSubheading,
      confirmPageLabels.infoMessage,
      confirmPageLabels.submitLabel,
      confirmPageLabels.searchAgainLinkText,
      confirmPageLabels.changeLinkText,
      confirmPageLabels.confirmChangeText
    ))
  }
}

object CountryPickerPageLabels {
  def unapply(countryPickerPageLabels: CountryPickerPageLabels): Option[(Option[String], Option[String], Option[String], Option[String])] = {
    Some((
      countryPickerPageLabels.title,
      countryPickerPageLabels.heading,
      countryPickerPageLabels.countryLabel,
      countryPickerPageLabels.submitLabel
    ))
  }
}
