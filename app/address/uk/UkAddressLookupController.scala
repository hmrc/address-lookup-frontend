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
import keystore.MemoService
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.mvc.{Action, AnyContent, Request, Result}
import uk.gov.hmrc.address.uk.Postcode
import uk.gov.hmrc.address.v2.{Address, Countries}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.util.JacksonMapper
import views.html.addressuk._

import scala.concurrent.{ExecutionContext, Future}


object UkAddressLookupController extends UkAddressLookupController(
  Services.configuredAddressLookupService,
  Services.metricatedKeystoreService,
  FrontendGlobal.executionContext)


class UkAddressLookupController(lookup: AddressLookupService, memo: MemoService, val ec: ExecutionContext) extends FrontendController {

  private implicit val xec = ec

  import UkAddressForm.addressForm
  import address.ViewConfig._

  private val uuidGenerator = Generators.timeBasedGenerator(EthernetAddress.fromInterface())

  // not strictly needed
  def start: Action[AnyContent] =
    Action {
      implicit request =>
        Redirect(routes.UkAddressLookupController.getEmptyForm("j0", None, None))
    }

  //-----------------------------------------------------------------------------------------------

  def getEmptyForm(tag: String, guid: Option[String], continue: Option[String]): Action[AnyContent] =
    TaggedAction.withTag(tag).apply {
      implicit request =>
        Ok(basicBlankForm(tag, guid, continue))
    }

  private def basicBlankForm(tag: String, guid: Option[String], continue: Option[String])(implicit request: Request[_]) = {
    val actualGuid = guid.getOrElse(uuidGenerator.generate.toString)
    val cu = continue.getOrElse(defaultContinueUrl)
    val ad = UkAddressData(guid = actualGuid, continue = cu, countryCode = Some(UkCode))
    val bound = addressForm.fill(ad)
    blankUkForm(tag, cfg(tag), bound, noMatchesWereFound = false, exceededLimit = false)
  }

  //-----------------------------------------------------------------------------------------------

  def postUkForm(tag: String): Action[AnyContent] =
    TaggedAction.withTag(tag).async {
      implicit request =>
        //        println("form1: " + PrettyMapper.writeValueAsString(request.body))
        val bound = addressForm.bindFromRequest()(request)
        if (bound.errors.nonEmpty) {
          Future.successful(BadRequest(blankUkForm(tag, cfg(tag), bound, noMatchesWereFound = false, exceededLimit = false)))

        } else {
          val addressData = bound.get
          if (addressData.noFixedAddress) {
            continueToCompletion(tag, addressData, request)

          } else {
            Future.successful(fixedAddress(tag, addressData))
          }
        }
    }

  private def fixedAddress(tag: String, formData: UkAddressData)(implicit request: Request[_]) = {
    if (formData.postcode.isEmpty) {
      val formWithError = addressForm.fill(formData).withError("postcode", "A post code is required")
      BadRequest(blankUkForm(tag, cfg(tag), formWithError, noMatchesWereFound = false, exceededLimit = false))

    } else {
      val pc = Postcode.cleanupPostcode(formData.postcode.get)
      if (pc.isEmpty) {
        val formWithError = addressForm.fill(formData).withError("postcode", "A valid post code is required")
        BadRequest(blankUkForm(tag, cfg(tag), formWithError, noMatchesWereFound = false, exceededLimit = false))

      } else {
        val cu = Some(formData.continue)
        val nameOrNumber = formData.nameNo.getOrElse("-")
        val proposalsRoute = routes.UkAddressLookupController.getProposals(tag, nameOrNumber, pc.get.toString, formData.guid, cu, None)
        SeeOther(proposalsRoute.url)
      }
    }
  }

  //-----------------------------------------------------------------------------------------------

  def getProposals(tag: String, nameNo: String, postcode: String, guid: String, continue: Option[String], edit: Option[Long]): Action[AnyContent] =
    TaggedAction.withTag(tag).async {
      implicit request =>
        val optNameNo = if (nameNo.isEmpty || nameNo == "-") None else Some(nameNo)
        val uPostcode = Postcode.cleanupPostcode(postcode)
        if (uPostcode.isEmpty) {
          val bound = addressForm.bindFromRequest()(request)
          Future.successful(BadRequest(basicBlankForm(tag, Some(guid), continue)))

        } else {
          lookup.findByPostcode(uPostcode.get, optNameNo) map {
            list =>
              val cu = continue.getOrElse(defaultContinueUrl)
              val exceededLimit = list.size > cfg(tag).maxAddressesToShow
              if (list.isEmpty || exceededLimit) {
                val pc = uPostcode.map(_.toString)
                val ad = UkAddressData(guid = guid, continue = cu,
                  nameNo = optNameNo, postcode = pc,
                  prevNameNo = optNameNo, prevPostcode = pc,
                  countryCode = Some(UkCode)
                )
                val filledInForm = addressForm.fill(ad)
                Ok(blankUkForm(tag, cfg(tag), filledInForm, noMatchesWereFound = list.isEmpty, exceededLimit = exceededLimit))

              } else {
                Ok(showAddressListProposalForm(tag, optNameNo, uPostcode.get.toString, guid, continue, list, edit))
              }
          }
        }
    }

  //-----------------------------------------------------------------------------------------------

  def postSelected(tag: String): Action[AnyContent] =
    TaggedAction.withTag(tag).async {
      implicit request =>
        //println("form2: " + PrettyMapper.writeValueAsString(request.body))
        val bound = addressForm.bindFromRequest()(request)
        if (bound.errors.nonEmpty) {
          Future.successful(BadRequest(blankUkForm(tag, cfg(tag), bound, noMatchesWereFound = false, exceededLimit = false)))

        } else {
          userSelection(tag, bound.get, request)
        }
    }


  private def userSelection(tag: String, addressData: UkAddressData, request: Request[_]): Future[Result] = {
    if (addressData.hasBeenUpdated) {
      val proposalsRoute = routes.UkAddressLookupController.getProposals(tag, addressData.nameNo.getOrElse("-"),
        addressData.postcode.getOrElse("-"), addressData.guid, Some(addressData.continue), None)
      Future(SeeOther(proposalsRoute.url))

    } else {
      continueToCompletion(tag, addressData, request)
    }
  }


  private def continueToCompletion(tag: String, addressData: UkAddressData, request: Request[_]): Future[Result] = {
    val nfa = if (addressData.noFixedAddress) "nfa=1&" else ""
    val ea = if (addressData.editedAddress.isDefined) "edit=" + encJson(addressData.editedAddress.get) else ""

    if (addressData.uprn.isEmpty) {
      val response = AddressRecordWithEdits(None, addressData.editedAddress, addressData.noFixedAddress)
      memo.storeSingleUkResponse(tag, addressData.guid, response) map {
        httpResponse =>
          SeeOther(addressData.continue + "?id=" + addressData.guid)
      }

    } else {
      val uprn = s"uprn=${addressData.uprn.get}&"
      lookup.findByUprn(addressData.uprn.get.toLong) flatMap {
        list =>
          val response = AddressRecordWithEdits(list.headOption, addressData.editedAddress, addressData.noFixedAddress)
          memo.storeSingleUkResponse(tag, addressData.guid, response) map {
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
        val fuResponse = memo.fetchSingleUkResponse(tag, id)
        fuResponse.map {
          response: Option[AddressRecordWithEdits] =>
            if (response.isEmpty) {
              val emptyFormRoute = routes.UkAddressLookupController.getEmptyForm(tag, None, None)
              TemporaryRedirect(emptyFormRoute.url)
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
