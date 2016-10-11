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
import config.{FrontendGlobal, JacksonMapper}
import config.ConfigHelper._
import keystore.KeystoreService
import play.api.Play
import play.api.mvc.{Action, AnyContent, Request, Result}
import uk.gov.hmrc.address.uk.Postcode
import uk.gov.hmrc.address.v2.{Address, Countries}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.addressuk._

import scala.concurrent.{ExecutionContext, Future}


object ConfiguredAddressLookupService extends AddressLookupService(
  mustGetConfigString(Play.current.mode, Play.current.configuration, "addressReputation.endpoint"),
  FrontendGlobal.appName)(FrontendGlobal.executionContext)


object ConfiguredKeystoreService extends KeystoreService(
  mustGetConfigString(Play.current.mode, Play.current.configuration, "keystore.endpoint"),
  FrontendGlobal.appName)(FrontendGlobal.executionContext)


object AddressLookupController extends AddressLookupController(
  ConfiguredAddressLookupService,
  ConfiguredKeystoreService,
  FrontendGlobal.executionContext)


class AddressLookupController(lookup: AddressLookupService, keystore: KeystoreService, val ec: ExecutionContext) extends FrontendController {

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

  def postForm(ix: Int): Action[AnyContent] = Action.async {
    request =>
      val bound = addressForm.bindFromRequest()(request)
      if (bound.errors.nonEmpty) {
        Future.successful(BadRequest(blankForm(ix, cfg(ix), bound, noMatchesWereFound = false, exceededLimit = false)(request)))

      } else {
        val formData = bound.get
        if (formData.noFixedAddress) {
          completion(ix, bound.get, noFixedAddress = true, request)

        } else {
          Future.successful(fixedAddress(ix, formData, request))
        }
      }
  }

  private def fixedAddress(ix: Int, formData: AddressData, request: Request[_]) = {
    if (formData.postcode.isEmpty) {
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

  def postSelected(ix: Int): Action[AnyContent] = Action.async { request =>
    val bound = addressForm.bindFromRequest()(request)
    if (bound.errors.nonEmpty) {
      Future.successful(BadRequest(blankForm(ix, cfg(ix), bound, noMatchesWereFound = false, exceededLimit = false)(request)))

    } else {
      completion(ix, bound.get, noFixedAddress = false, request)
    }
  }

  private def completion(ix: Int, addressData: AddressData, noFixedAddress: Boolean, request: Request[_]): Future[Result] = {
    val nfa = if (noFixedAddress) "nfa=1&" else ""
    val ea = if (addressData.editedAddress.isDefined) "edit=" + encJson(addressData.editedAddress.get) else ""

    if (addressData.uprn.isEmpty) {
      val response = AddressRecordWithEdits(None, addressData.editedAddress, noFixedAddress)
      keystore.storeSingleResponse(addressData.guid, ix, response) map {
        httpResponse =>
          SeeOther(addressData.continue + "?id=" + addressData.guid)
      }

    } else {
      val uprn = s"uprn=${addressData.uprn.get}&"
      lookup.findByUprn(addressData.uprn.get.toLong) flatMap {
        list =>
          val response = AddressRecordWithEdits(list.headOption, addressData.editedAddress, noFixedAddress)
          keystore.storeSingleResponse(addressData.guid, ix, response) map {
            httpResponse =>
              SeeOther(addressData.continue + "?ix=" + ix + "&id=" + addressData.guid)
          }
      }
    }
  }

  //-----------------------------------------------------------------------------------------------

  def confirmation(ix: Int, id: String): Action[AnyContent] = Action.async {
    request =>
      val fuResponse = keystore.fetchSingleResponse(id, ix)
      fuResponse.map {
        response: Option[AddressRecordWithEdits] =>
          if (response.isEmpty) {
            TemporaryRedirect(routes.AddressLookupController.getEmptyForm(ix, None, None).url)
          } else {
            val addressRecord = response.get
            if (addressRecord.normativeAddress.isDefined) {
              Ok(confirmationPage(ix, cfg(ix), addressRecord.normativeAddress.get, addressRecord.userSuppliedAddress)(request))
            } else {
              Ok(userSuppliedAddressPage(ix, cfg(ix), addressRecord.userSuppliedAddress.getOrElse(noFixedAbodeAddress))(request))
            }
          }
      }
  }

  private def encJson(value: AnyRef): String = URLEncoder.encode(JacksonMapper.writeValueAsString(value), "ASCII")

  private val noFixedAbodeAddress = Address(List("No fixed abode"), None, None, "", None, Countries.UK)
}
