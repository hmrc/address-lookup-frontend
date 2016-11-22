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
import config.FrontendGlobal
import keystore.KeystoreService
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.mvc.{Action, AnyContent, Request, Result}
import uk.gov.hmrc.address.uk.Postcode
import uk.gov.hmrc.address.v2.{Address, Countries}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.util.JacksonMapper
import views.html.addressuk._

import scala.concurrent.{ExecutionContext, Future}


object AddressLookupController extends AddressLookupController(
  Services.configuredAddressLookupService,
  Services.metricatedKeystoreService,
  FrontendGlobal.executionContext)


class AddressLookupController(lookup: AddressLookupService, keystore: KeystoreService, val ec: ExecutionContext) extends FrontendController {

  private implicit val xec = ec

  import AddressForm.addressForm
  import address.ViewConfig._

  private val uuidGenerator = Generators.timeBasedGenerator(EthernetAddress.fromInterface())

  // not strictly needed
  def start: Action[AnyContent] =
  Action {
    implicit request =>
      Redirect(routes.AddressLookupController.getEmptyForm("j0", None, None))
  }

  //-----------------------------------------------------------------------------------------------

  def getEmptyForm(tag: String, guid: Option[String], continue: Option[String]): Action[AnyContent] =
    TaggedAction.withTag(tag).apply {
      implicit request =>
        val actualGuid = guid.getOrElse(uuidGenerator.generate.toString)
        val cu = continue.getOrElse(defaultContinueUrl)
        val ad = AddressData(guid = actualGuid, continue = cu, countryCode = Some(UkCode))
        val bound = addressForm.fill(ad)
        Ok(blankForm(tag, cfg(tag), bound, noMatchesWereFound = false, exceededLimit = false))
    }

  //-----------------------------------------------------------------------------------------------

  def postForm(tag: String): Action[AnyContent] =
    TaggedAction.withTag(tag).async {
      implicit request =>
        val bound = addressForm.bindFromRequest()(request)
        if (bound.errors.nonEmpty) {
          Future.successful(BadRequest(blankForm(tag, cfg(tag), bound, noMatchesWereFound = false, exceededLimit = false)))

        } else {
          val addressData = bound.get
          if (addressData.noFixedAddress) {
            completion(tag, addressData, request)

          } else {
            Future.successful(fixedAddress(tag, addressData))
          }
        }
    }

  private def fixedAddress(tag: String, formData: AddressData)(implicit request: Request[_]) = {
    if (formData.postcode.isEmpty) {
      val formWithError = addressForm.fill(formData).withError("postcode", "A post code is required")
      BadRequest(blankForm(tag, cfg(tag), formWithError, noMatchesWereFound = false, exceededLimit = false))

    } else {
      val pc = Postcode.cleanupPostcode(formData.postcode.get)
      if (pc.isEmpty) {
        val formWithError = addressForm.fill(formData).withError("postcode", "A valid post code is required")
        BadRequest(blankForm(tag, cfg(tag), formWithError, noMatchesWereFound = false, exceededLimit = false))

      } else {
        val cu = Some(formData.continue)
        val nameOrNumber = formData.nameNo.getOrElse("-")
        SeeOther(routes.AddressLookupController.getProposals(tag, nameOrNumber, pc.get.toString, formData.guid, cu, None).url + "#found-addresses")
      }
    }
  }

  //-----------------------------------------------------------------------------------------------

  def getProposals(tag: String, nameNo: String, postcode: String, guid: String, continue: Option[String], edit: Option[Long]): Action[AnyContent] =
    TaggedAction.withTag(tag).async {
      implicit request =>
        val optNameNo = if (nameNo.isEmpty || nameNo == "-") None else Some(nameNo)
        val uPostcode = Postcode.normalisePostcode(postcode)
        lookup.findByPostcode(uPostcode, optNameNo) map {
          list =>
            val cu = continue.getOrElse(defaultContinueUrl)
            val exceededLimit = list.size > cfg(tag).maxAddressesToShow
            if (list.isEmpty || exceededLimit) {
              val ad = AddressData(guid = guid, continue = cu,
                nameNo = optNameNo, postcode = Some(uPostcode),
                prevNameNo = optNameNo, prevPostcode = Some(uPostcode),
                countryCode = Some(UkCode)
              )
              val filledInForm = addressForm.fill(ad)
              Ok(blankForm(tag, cfg(tag), filledInForm, noMatchesWereFound = list.isEmpty, exceededLimit = exceededLimit))

            } else {
              Ok(showAddressListProposalForm(tag, optNameNo, uPostcode, guid, continue, list, edit))
            }
        }
    }

  //-----------------------------------------------------------------------------------------------

  def postSelected(tag: String): Action[AnyContent] =
    TaggedAction.withTag(tag).async {
      implicit request =>
        val bound = addressForm.bindFromRequest()(request)
        if (bound.errors.nonEmpty) {
          Future.successful(BadRequest(blankForm(tag, cfg(tag), bound, noMatchesWereFound = false, exceededLimit = false)))

        } else {
          val addressData = bound.get
          if (addressData.hasBeenUpdated) {
            Future(SeeOther(routes.AddressLookupController.getProposals(tag, addressData.nameNo.get, addressData.postcode.get, addressData.guid, Some(addressData.continue), None).url))

          } else {
            completion(tag, addressData, request)
          }
        }
    }


  private def completion(tag: String, addressData: AddressData, request: Request[_]): Future[Result] = {
    val nfa = if (addressData.noFixedAddress) "nfa=1&" else ""
    val ea = if (addressData.editedAddress.isDefined) "edit=" + encJson(addressData.editedAddress.get) else ""

    if (addressData.uprn.isEmpty) {
      val response = AddressRecordWithEdits(None, addressData.editedAddress, addressData.noFixedAddress)
      keystore.storeSingleResponse(tag, addressData.guid, response) map {
        httpResponse =>
          SeeOther(addressData.continue + "?id=" + addressData.guid)
      }

    } else {
      val uprn = s"uprn=${addressData.uprn.get}&"
      lookup.findByUprn(addressData.uprn.get.toLong) flatMap {
        list =>
          val response = AddressRecordWithEdits(list.headOption, addressData.editedAddress, addressData.noFixedAddress)
          keystore.storeSingleResponse(tag, addressData.guid, response) map {
            httpResponse =>
              SeeOther(addressData.continue + "?tag=" + tag + "&id=" + addressData.guid)
          }
      }
    }
  }

  //-----------------------------------------------------------------------------------------------

  def confirmation(tag: String, id: String): Action[AnyContent] =
    TaggedAction.withTag(tag).async {
      implicit request =>
        require(id.nonEmpty)
        val fuResponse = keystore.fetchSingleResponse(tag, id)
        fuResponse.map {
          response: Option[AddressRecordWithEdits] =>
            if (response.isEmpty) {
              TemporaryRedirect(routes.AddressLookupController.getEmptyForm(tag, None, None).url)
            } else {
              val addressRecord = response.get
              if (addressRecord.normativeAddress.isDefined) {
                Ok(confirmationPage(tag, cfg(tag), addressRecord.normativeAddress.get, addressRecord.userSuppliedAddress))
              } else {
                Ok(userSuppliedAddressPage(tag, cfg(tag), addressRecord.userSuppliedAddress.getOrElse(noFixedAbodeAddress)))
              }
            }
        }
    }

  private def encJson(value: AnyRef): String = URLEncoder.encode(JacksonMapper.writeValueAsString(value), "ASCII")

  private val noFixedAbodeAddress = Address(List("No fixed abode"), None, None, "", None, Countries.UK)

  private val UkCode = Countries.UK.code

}
