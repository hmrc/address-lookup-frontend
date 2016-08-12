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


object AddressLookupController extends AddressLookupController(AddressLookupService)


class AddressLookupController(lookup: AddressLookupService) extends FrontendController {
  val cfg = List(
    ViewConfig(title = "Your address", allowManualEntry = true, maxAddressesToShow = 20),
    ViewConfig(title = "Account holder address", allowManualEntry = false, maxAddressesToShow = 10)
  )

  val defaultContinueUrl = "confirmation"

  val addressForm = Form[AddressForm] {
    mapping(
      "continue-url" -> text,
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
    Redirect(routes.AddressLookupController.getEmptyForm(0, None))
  }

  def getEmptyForm(ix: Int, continueUrl: Option[String]): Action[AnyContent] = Action { implicit request =>
    val cu = continueUrl.getOrElse(defaultContinueUrl)
    val bound = addressForm.fill(AddressForm(cu, false, None, None, None, None, None, None, None, None))
    Ok(blankForm(ix, cfg(ix), bound, noMatchesWereFound = false, exceededLimit = false))
  }

  def postForm(ix: Int): Action[AnyContent] = Action { implicit request =>
    val bound = addressForm.bindFromRequest()
    if (bound.errors.nonEmpty) {
      BadRequest(blankForm(ix, cfg(ix), bound, noMatchesWereFound = false, exceededLimit = false))
    } else {
      val formData = bound.get
      if (formData.noFixedAddress) {
        completion(ix, addressForm.bindFromRequest().get, noFixedAddress = true)
      } else if (formData.postcode.isEmpty) {
        BadRequest(blankForm(ix, cfg(ix), addressForm.fill(formData).withError("postcode", "A post code is required"), noMatchesWereFound = false, exceededLimit = false))
      } else {
        val cu = Some(formData.continueUrl)
        SeeOther(routes.AddressLookupController.getProposals(ix, formData.nameNo.getOrElse("-"), formData.postcode.get, cu).url)
      }
    }
  }

  def getProposals(ix: Int, nameNo: String, postcode: String, continueUrl: Option[String]): Action[AnyContent] = Action.async { implicit request =>
    val optNameNo = if (nameNo.isEmpty || nameNo == "-") None else Some(nameNo)
    lookup.findAddresses(postcode, optNameNo) map {
      list =>
        val cu = continueUrl.getOrElse(defaultContinueUrl)
        val exceededLimit = list.size > cfg(ix).maxAddressesToShow
        if (list.isEmpty || exceededLimit) {
          val bound = addressForm.fill(AddressForm(cu, false, optNameNo, Some(postcode), None, None, None, None, None, None))
          Ok(blankForm(ix, cfg(ix), bound, noMatchesWereFound = list.isEmpty, exceededLimit = exceededLimit))
        } else {
          Ok(showAddressList(ix, optNameNo, postcode, cu, list))
        }
    }
  }

  private def showAddressList(ix: Int, nameNo: Option[String], postcode: String, continueUrl: String, matchingAddresses: List[AddressRecord])
                             (implicit request: Request[_]) = {
    val selected = if (matchingAddresses.size == 1) 0 else -1
    val ar = matchingAddresses.head
    val ad = ar.address
    val l1 = ad.lines.headOption
    val l2 = if (ad.lines.size > 1) Some(ad.lines(1)) else None
    val l3 = if (ad.lines.size > 2) Some(ad.lines(2)) else None
    val updatedDetails = AddressForm(continueUrl, false, nameNo, Some(postcode), ar.uprn.map(_.toString), l1, l2, l3, ad.town, None)
    proposalForm(ix, cfg(ix), addressForm.fill(updatedDetails), matchingAddresses, selected)
  }

  def postSelected(ix: Int): Action[AnyContent] = Action { implicit request =>
    val bound = addressForm.bindFromRequest()
    if (bound.errors.nonEmpty) {
      BadRequest(blankForm(ix, cfg(ix), bound, noMatchesWereFound = false, exceededLimit = false))
    } else {
      val formData = bound.get
      completion(ix, addressForm.bindFromRequest().get, noFixedAddress = false)
    }
  }

  private def completion(ix: Int, address: AddressForm, noFixedAddress: Boolean)(implicit request: Request[_]) = {
    val nfa = if (noFixedAddress) "nfa=1&" else ""
    val uprn = if (address.id.isDefined) s"uprn=${address.id.get}&" else ""
    val ed = address.editedAddress
    val ea = if (ed.isDefined) "edit=" + JacksonMapper.writeValueAsString(ed.get) else ""
    SeeOther(address.continueUrl + "?" + nfa + uprn + ea)
  }

  def confirmation(ix: Int, nfa: Option[Int], uprn: Option[String], edit: Option[String]): Action[AnyContent] = Action.async { implicit request =>
    if (nfa.contains(1)) {
      Future.successful(Ok(noFixedAddressPage(cfg(ix))))
    } else if (uprn.isEmpty) {
      Future.successful(Redirect(routes.AddressLookupController.getEmptyForm(ix, None)))
    } else {
      lookup.findUprn(uprn.get) map {
        list =>
          val editedAddress = edit.map(json => JacksonMapper.readValue(json, classOf[Address]))
          Ok(confirmationPage(cfg(ix), list.head, editedAddress))
      }
    }
  }
}


case class ViewConfig(title: String,
                      allowManualEntry: Boolean = false,
                      allowNoFixedAddress: Boolean = true,
                      maxAddressesToShow: Int = 20)


case class AddressForm(
                        continueUrl: String,
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
