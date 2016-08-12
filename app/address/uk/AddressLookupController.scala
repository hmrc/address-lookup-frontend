/*
 * Copyright 2016 HM Revenue & Customs
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

package address.uk

import address.uk.service.AddressLookupService
import config.JacksonMapper
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc.{Action, AnyContent, Request}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.addressuk._

import scala.concurrent.Future


object AddressLookupController extends AddressLookupController(
  AddressLookupService,
  ViewConfig(title = "Your address", allowManualEntry = false, maxAddressesToShow = 20)
)


class AddressLookupController(lookup: AddressLookupService, cfg: ViewConfig) extends FrontendController {

  val addressForm = Form[AddressForm] {
    mapping(
      "continue-url" -> optional(text),
      "no-fixed-address" -> boolean,
      "house-name-number" -> optional(text),
      "postcode" -> optional(text),
      "radio-inline-group" -> optional(text),
      "address-line1" -> optional(text),
      "address-line2" -> optional(text),
      "address-line3" -> optional(text),
      "town" -> optional(text),
      "county" -> optional(text)
    )(AddressForm.apply)(AddressForm.unapply)
  }

  def start: Action[AnyContent] = Action { implicit request =>
    Redirect(routes.AddressLookupController.getEmptyForm(None))
  }

  def getEmptyForm(continueUrl: Option[String]): Action[AnyContent] = Action { implicit request =>
    val bound = addressForm.fill(AddressForm(continueUrl, false, None, None, None, None, None, None, None, None))
    Ok(blankForm(cfg, bound, noMatchesWereFound = false))
  }

  def postForm: Action[AnyContent] = Action { implicit request =>
    val bound = addressForm.bindFromRequest()
    if (bound.errors.nonEmpty) {
      BadRequest(blankForm(cfg, bound, noMatchesWereFound = false))
    } else {
      val formData = bound.get
      if (formData.noFixedAddress) {
        completion(cfg, addressForm.bindFromRequest().get, noFixedAddress = true)
      } else if (formData.postcode.isEmpty) {
        BadRequest(blankForm(cfg, addressForm.fill(formData).withError("postcode", "A post code is required"), noMatchesWereFound = false))
      } else {
        SeeOther(routes.AddressLookupController.getProposals(formData.nameNo.getOrElse("-"), formData.postcode.get, formData.continueUrl).url)
      }
    }
  }

  def getProposals(nameNo: String, postcode: String, continueUrl: Option[String]): Action[AnyContent] = Action.async { implicit request =>
    val optNameNo = if (nameNo.isEmpty || nameNo == "-") None else Some(nameNo)
    lookup.findAddresses(postcode, optNameNo) map {
      list => showAddressList(optNameNo, postcode, continueUrl, list)
    }
  }

  private def showAddressList(nameNo: Option[String], postcode: String, continueUrl: Option[String], matchingAddresses: List[AddressRecord])
                             (implicit request: Request[_]) = {
    val updatedDetails =
      if (matchingAddresses.nonEmpty) {
        val a = matchingAddresses.head.address
        addressForm.fill(
          AddressForm(continueUrl, false, nameNo, Some(postcode),
            None,
            Some(a.line1), Some(a.line2), Some(a.line3),
            a.town, None
          ))
      } else {
        addressForm.fill(AddressForm(continueUrl, false, nameNo, Some(postcode), None, None, None, None, None, None))
      }

    Ok(proposalForm(cfg, updatedDetails, matchingAddresses, matchingAddresses.isEmpty))
  }

  def postSelected: Action[AnyContent] = Action { implicit request =>
    val bound = addressForm.bindFromRequest()
    if (bound.errors.nonEmpty) {
      BadRequest(blankForm(cfg, bound, noMatchesWereFound = false))
    } else {
      val formData = bound.get
      completion(cfg, addressForm.bindFromRequest().get, noFixedAddress = false)
    }
  }

  private def completion(cfg: ViewConfig, address: AddressForm, noFixedAddress: Boolean)(implicit request: Request[_]) = {
    val url = if (address.continueUrl.isDefined) {
      val nfa = if (noFixedAddress) "nfa=1&" else ""
      val uprn = if (address.id.isDefined) s"uprn=${address.id.get}&" else ""
      val ed = address.editedAddress
      val ea = if (ed.isDefined) "edit=" + JacksonMapper.writeValueAsString(ed.get) else ""
      address.continueUrl.get + "?" + nfa + uprn + ea

    } else {
      val nfa = if (noFixedAddress) Some(1) else None
      val uprn = if (address.id.isDefined) s"uprn=${address.id.get}&" else ""
      val ed = address.editedAddress
      val ea = ed.map(JacksonMapper.writeValueAsString)
      routes.AddressLookupController.confirmation(nfa, address.id, ea).url
    }

    SeeOther(url)
  }

  def confirmation(nfa: Option[Int], uprn: Option[String], edit: Option[String]): Action[AnyContent] = Action.async { implicit request =>
    if (nfa.contains(1)) {
      Future.successful(Ok(noFixedAddressPage(cfg)))
    } else {
      lookup.findUprn(uprn.get) map {
        list =>
          val editedAddress = edit.map(json => JacksonMapper.readValue(json, classOf[Address]))
          Ok(confirmationPage(cfg, list.head, editedAddress))
      }
    }
  }
}


case class ViewConfig(title: String,
                      allowManualEntry: Boolean = false,
                      allowNoFixedAddress: Boolean = true,
                      maxAddressesToShow: Int = 20)


case class AddressForm(
                        continueUrl: Option[String],
                        noFixedAddress: Boolean,
                        nameNo: Option[String], postcode: Option[String],
                        id: Option[String],
                        editedLine1: Option[String], editedLine2: Option[String], editedLine3: Option[String],
                        editedTown: Option[String], editedCounty: Option[String]
                      ) {

  def editedAddress =
    if (editedLine1.isDefined || editedLine2.isDefined || editedLine3.isDefined || editedTown.isDefined)
      Some(Address(editedLine1.toList ++ editedLine2.toList ++ editedLine3.toList, editedTown, editedCounty, postcode.get, None, Countries.UK))
    else
      None
}
