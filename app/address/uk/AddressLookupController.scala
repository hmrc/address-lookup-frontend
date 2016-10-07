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

import address.uk.DisplayProposalsPage.showAddressListProposalForm
import address.uk.service.AddressLookupService
import com.fasterxml.uuid.{EthernetAddress, Generators}
import config.JacksonMapper
import play.api.mvc.{Action, AnyContent, Request, Result}
import uk.gov.hmrc.address.uk.Postcode
import uk.gov.hmrc.address.v2.{Address, Countries}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.addressuk._

import scala.concurrent.ExecutionContext.global
import scala.concurrent.{ExecutionContext, Future}


object AddressLookupController extends AddressLookupController(AddressLookupService, global)


class AddressLookupController(lookup: AddressLookupService, val ec: ExecutionContext) extends FrontendController {

  private implicit val xec = ec

  import AddressForm.addressForm
  import ViewConfig._

  private val uuidGenerator = Generators.timeBasedGenerator(EthernetAddress.fromInterface())

  // not strictly needed
  def start: Action[AnyContent] = Action {
    implicit request =>
      Redirect(routes.AddressLookupController.getEmptyForm(0, None, None))
  }

  //-----------------------------------------------------------------------------------------------

  def getEmptyForm(ix: Int, guid: Option[String], continue: Option[String]): Action[AnyContent] = Action {
    request =>
      val actualGuid = guid.getOrElse(uuidGenerator.generate.toString)
      val cu = continue.getOrElse(defaultContinueUrl)
      val bound = addressForm.fill(AddressData(actualGuid, cu, false, None, None, None, None, None, None, Countries.UK.code))
      Ok(blankForm(ix, cfg(ix), bound, noMatchesWereFound = false, exceededLimit = false)(request))
  }

  //-----------------------------------------------------------------------------------------------

  def postForm(ix: Int): Action[AnyContent] = Action {
    request =>
      val bound = addressForm.bindFromRequest()(request)
      if (bound.errors.nonEmpty) {
        BadRequest(blankForm(ix, cfg(ix), bound, noMatchesWereFound = false, exceededLimit = false)(request))

      } else {
        val formData = bound.get
        if (formData.noFixedAddress) {
          completion(ix, bound.get, noFixedAddress = true, request)

        } else if (formData.postcode.isEmpty) {
          val formWithError = addressForm.fill(formData).withError("postcode", "A post code is required")
          BadRequest(blankForm(ix, cfg(ix), formWithError, noMatchesWereFound = false, exceededLimit = false)(request))

        } else {
          val pc = Postcode.cleanupPostcode(formData.postcode.get)
          if (pc.isEmpty) {
            val formWithError = addressForm.fill(formData).withError("postcode", "A valid post code is required")
            BadRequest(blankForm(ix, cfg(ix), formWithError, noMatchesWereFound = false, exceededLimit = false)(request))

          } else {
            val cu = Some(formData.continue)
            val nameOrNumber = formData.nameNo.getOrElse("-")
            SeeOther(routes.AddressLookupController.getProposals(ix, nameOrNumber, pc.get.toString, formData.guid, cu, None).url + "#found-addresses")
          }
        }
      }
  }

  //-----------------------------------------------------------------------------------------------

  def getProposals(ix: Int, nameNo: String, postcode: String, guid: String, continue: Option[String], edit: Option[Long]): Action[AnyContent] = Action.async {
    request =>
      val optNameNo = if (nameNo.isEmpty || nameNo == "-") None else Some(nameNo)
      val uPostcode = postcode.toUpperCase
      lookup.findByPostcode(uPostcode, optNameNo) map {
        list =>
          val cu = continue.getOrElse(defaultContinueUrl)
          val exceededLimit = list.size > cfg(ix).maxAddressesToShow
          if (list.isEmpty || exceededLimit) {
            val filledInForm = addressForm.fill(AddressData(guid, cu, noFixedAddress = false, optNameNo, Some(uPostcode), None, None, None, None, Countries.UK.code))
            Ok(blankForm(ix, cfg(ix), filledInForm, noMatchesWereFound = list.isEmpty, exceededLimit = exceededLimit)(request))

          } else {
            Ok(showAddressListProposalForm(ix, optNameNo, uPostcode, guid, continue, list, edit, request))
          }
      }
  }

  //-----------------------------------------------------------------------------------------------

  def postSelected(ix: Int): Action[AnyContent] = Action { request =>
    val bound = addressForm.bindFromRequest()(request)
    if (bound.errors.nonEmpty) {
      BadRequest(blankForm(ix, cfg(ix), bound, noMatchesWereFound = false, exceededLimit = false)(request))

    } else {
      completion(ix, bound.get, noFixedAddress = false, request)
    }
  }

  private def completion(ix: Int, address: AddressData, noFixedAddress: Boolean, request: Request[_]): Result = {
    val nfa = if (noFixedAddress) "nfa=1&" else ""
    val uprn = if (address.uprn.isDefined) s"uprn=${address.uprn.get}&" else ""
    val ea = if (address.editedAddress.isDefined) "edit=" + encJson(address.editedAddress.get) else ""
    SeeOther(address.continue + "?" + nfa + uprn + ea)
  }

  //-----------------------------------------------------------------------------------------------

  def confirmation(ix: Int, nfa: Option[Int], uprn: Option[String], edit: Option[String]): Action[AnyContent] = Action.async {
    request =>
      if (nfa.contains(1)) {
        Future.successful(Ok(noFixedAddressPage(ix, cfg(ix))(request)))

      } else if (uprn.isEmpty) {
        Future.successful(Redirect(routes.AddressLookupController.getEmptyForm(ix, None, None)))

      } else {
        lookup.findByUprn(uprn.get.toLong) map {
          list =>
            val normative = list.head
            val editedAddress = edit.map(json => JacksonMapper.readValue(json, classOf[Address]))
            val address = AddressRecordWithEdits(normative, editedAddress)
            Ok(confirmationPage(ix, cfg(ix), address)(request))
        }
      }
  }

  private def encJson(value: AnyRef): String = URLEncoder.encode(JacksonMapper.writeValueAsString(value), "ASCII")

}
