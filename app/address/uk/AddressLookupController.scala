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

import java.net.URLEncoder

import address.uk.service.AddressLookupService
import config.JacksonMapper
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc.{Action, AnyContent, Request}
import uk.gov.hmrc.address.uk.Postcode
import uk.gov.hmrc.address.v2.{Address, AddressRecord, Countries}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.addressuk._

import scala.concurrent.Future


object AddressLookupController extends AddressLookupController(AddressLookupService)


class AddressLookupController(lookup: AddressLookupService) extends FrontendController {

  import AddressLookupForm.addressForm
  import ViewConfig.cfg

  val defaultContinueUrl = "confirmation"

  // not strictly needed
  def start: Action[AnyContent] = Action {
    implicit request =>
      Redirect(routes.AddressLookupController.getEmptyForm(0, None))
  }

  //-----------------------------------------------------------------------------------------------

  def getEmptyForm(ix: Int, continue: Option[String]): Action[AnyContent] = Action {
    implicit request =>
      val cu = continue.getOrElse(defaultContinueUrl)
      val bound = addressForm.fill(AddressForm(cu, false, None, None, None, None, None, None, Countries.UK.code))
      Ok(blankForm(ix, cfg(ix), bound, noMatchesWereFound = false, exceededLimit = false))
  }

  //-----------------------------------------------------------------------------------------------

  def postForm(ix: Int): Action[AnyContent] = Action {
    implicit request =>
      val bound = addressForm.bindFromRequest()
      if (bound.errors.nonEmpty) {
        BadRequest(blankForm(ix, cfg(ix), bound, noMatchesWereFound = false, exceededLimit = false))
      } else {
        val formData = bound.get
        if (formData.noFixedAddress) {
          completion(ix, bound.get, noFixedAddress = true)
        } else if (formData.postcode.isEmpty) {
          BadRequest(blankForm(ix, cfg(ix), addressForm.fill(formData).withError("postcode", "A post code is required"), noMatchesWereFound = false, exceededLimit = false))
        } else {
          val pc = Postcode.cleanupPostcode(formData.postcode.get)
          if (pc.isEmpty) {
            BadRequest(blankForm(ix, cfg(ix), addressForm.fill(formData).withError("postcode", "A valid post code is required"), noMatchesWereFound = false, exceededLimit = false))
          } else {
            val cu = Some(formData.continue)
            SeeOther(routes.AddressLookupController.getProposals(ix, formData.nameNo.getOrElse("-"), pc.get.toString, cu, None).url + "#found-addresses")
          }
        }
      }
  }

  //-----------------------------------------------------------------------------------------------

  def getProposals(ix: Int, nameNo: String, postcode: String, continue: Option[String], edit: Option[Long]): Action[AnyContent] = Action.async {
    implicit request =>
      val optNameNo = if (nameNo.isEmpty || nameNo == "-") None else Some(nameNo)
      val uPostcode = postcode.toUpperCase
      lookup.findByPostcode(uPostcode, optNameNo) map {
        list =>
          val cu = continue.getOrElse(defaultContinueUrl)
          val exceededLimit = list.size > cfg(ix).maxAddressesToShow
          if (list.isEmpty || exceededLimit) {
            val bound = addressForm.fill(AddressForm(cu, false, optNameNo, Some(uPostcode), None, None, None, None, Countries.UK.code))
            Ok(blankForm(ix, cfg(ix), bound, noMatchesWereFound = list.isEmpty, exceededLimit = exceededLimit))
          } else {
            Ok(showAddressList(ix, optNameNo, uPostcode, continue, list, edit))
          }
      }
  }

  private def showAddressList(ix: Int, nameNo: Option[String], postcode: String, continue: Option[String],
                              matchingAddresses: List[AddressRecord], edit: Option[Long])
                             (implicit request: Request[_]) = {
    val ar = if (edit.isDefined) matchingAddresses.find(_.uprn == edit).getOrElse(matchingAddresses.head) else matchingAddresses.head
    val ad = ar.address
    val lines = if (ad.lines.nonEmpty) Some(ad.lines.mkString("\n")) else None
    val cu = continue.getOrElse(defaultContinueUrl)
    val selectedUprn =
      if (matchingAddresses.size == 1) {
        ar.uprn.getOrElse(-1L)
      } else if (edit.isDefined) {
        edit.get
      } else {
        -1L
      }
    val country = matchingAddresses.headOption.map(_.address.country).getOrElse(Countries.UK)
    val updatedDetails = AddressForm(cu, false, nameNo, Some(postcode), ar.uprn.map(_.toString), lines, ad.town, None, country.code)
    val editUrl = routes.AddressLookupController.getProposals(ix, nameNo.getOrElse("-"), postcode, continue, None)
    proposalForm(ix, cfg(ix).copy(indicator = Some(postcode)), addressForm.fill(updatedDetails), matchingAddresses, selectedUprn, edit.isDefined, editUrl.url)
  }

  //-----------------------------------------------------------------------------------------------

  def postSelected(ix: Int): Action[AnyContent] = Action { implicit request =>
    val bound = addressForm.bindFromRequest()
    if (bound.errors.nonEmpty) {
      BadRequest(blankForm(ix, cfg(ix), bound, noMatchesWereFound = false, exceededLimit = false))
    } else {
      val formData = bound.get
      completion(ix, addressForm.bindFromRequest().get, noFixedAddress = false)
    }
  }

  private def completion(ix: Int, address: AddressForm, noFixedAddress: Boolean)
                        (implicit request: Request[_]) = {
    val nfa = if (noFixedAddress) "nfa=1&" else ""
    val uprn = if (address.uprn.isDefined) s"uprn=${address.uprn.get}&" else ""
    val ed = address.editedAddress
    val ea = if (ed.isDefined) "edit=" + URLEncoder.encode(JacksonMapper.writeValueAsString(ed.get), "ASCII") else ""
    SeeOther(address.continue + "?" + nfa + uprn + ea)
  }

  //-----------------------------------------------------------------------------------------------

  def confirmation(ix: Int, nfa: Option[Int], uprn: Option[String], edit: Option[String]): Action[AnyContent] = Action.async {
    implicit request =>
      if (nfa.contains(1)) {
        Future.successful(Ok(noFixedAddressPage(ix, cfg(ix))))
      } else if (uprn.isEmpty) {
        Future.successful(Redirect(routes.AddressLookupController.getEmptyForm(ix, None)))
      } else {
        lookup.findByUprn(uprn.get.toLong) map {
          list =>
            val editedAddress = edit.map(json => JacksonMapper.readValue(json, classOf[Address]))
            Ok(confirmationPage(ix, cfg(ix), list.head, editedAddress))
        }
      }
  }
}


case class AddressForm(
                        continue: String,
                        noFixedAddress: Boolean,
                        nameNo: Option[String],
                        postcode: Option[String],
                        uprn: Option[String],
                        editedLines: Option[String], editedTown: Option[String],
                        editedCounty: Option[String],
                        countryCode: String
                      ) {

  def editedAddress: Option[Address] =
    if (editedLines.isDefined || editedTown.isDefined || editedCounty.isDefined) {
      Some(Address(
        editedLines.toList.flatMap(_.split("\n").map(_.trim)),
        editedTown.map(_.trim),
        editedCounty.map(_.trim),
        postcode.get.trim,
        None, Countries.find(countryCode).get))
    } else None
}


object AddressLookupForm {
  val addressForm = Form[AddressForm] {
    mapping(
      "continue-url" -> text,
      "no-fixed-address" -> boolean,
      "house-name-number" -> optional(text),
      "postcode" -> optional(text),
      "radio-inline-group" -> optional(text),
      "address-lines" -> optional(text),
      "town" -> optional(text),
      "county" -> optional(text),
      "country-code" -> text
    )(AddressForm.apply)(AddressForm.unapply)
  }
}
