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

import address.outcome.SelectedAddress
import address.uk.UkProposalsPage.showAddressListProposalForm
import address.uk.service.AddressLookupService
import com.fasterxml.uuid.{EthernetAddress, Generators}
import com.google.inject.{Inject, Singleton}
import keystore.MemoService
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.JsValue
import play.api.mvc.{Action, AnyContent, Request, Result}
import play.twirl.api.Html
import uk.gov.hmrc.address.uk.Postcode
import uk.gov.hmrc.address.v2.Countries
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.addressuk._

import scala.concurrent.{ExecutionContext, Future}

import scala.concurrent.ExecutionContext.Implicits.global



@Singleton
class UkAddressLookupController @Inject()(environment: play.api.Environment, configuration: play.api.Configuration)
                                         (implicit val ec: ExecutionContext, val messagesApi: MessagesApi)
  extends FrontendController with I18nSupport {


  import UkAddressForm.addressForm
  import address.ViewConfig._

  lazy val lookup: AddressLookupService = Services.configuredAddressLookupService(environment, configuration)
  lazy val memo: MemoService = Services.metricatedKeystoreService(environment, configuration)


  private val uuidGenerator = Generators.timeBasedGenerator(EthernetAddress.fromInterface())

  // not strictly needed
  def start: Action[AnyContent] =
    Action {
      implicit request =>
        Redirect(routes.UkAddressLookupController.getEmptyForm("j0", None, None, None, None))
    }

  //-----------------------------------------------------------------------------------------------

  def getEmptyForm(tag: String, guid: Option[String], continue: Option[String],
                   backUrl: Option[String], backText: Option[String]): Action[AnyContent] =
    TaggedAction.withTag(tag).apply {
      implicit request =>
        Ok(basicBlankForm(tag, guid, continue, backUrl, backText))
    }

  private def basicBlankForm(tag: String, guid: Option[String], continue: Option[String],
                             backUrl: Option[String], backText: Option[String])(implicit request: Request[_]): Html = {
    val actualGuid = guid.getOrElse(uuidGenerator.generate.toString)
    val cu = continue.getOrElse(defaultContinueUrl)
    val ad = UkAddressData(guid = actualGuid, continue = cu, backUrl = backUrl, backText = backText, countryCode = UkCode)
    val bound = addressForm.fill(ad)
    blankUkForm(tag, cfg(tag), bound, noMatchesWereFound = false, exceededLimit = false)
  }

  //-----------------------------------------------------------------------------------------------

  def postFirstForm(tag: String): Action[AnyContent] =
    TaggedAction.withTag(tag).async {
      implicit request =>
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
      val pc = formData.postcode.flatMap(Postcode.cleanupPostcode)
      if (pc.isEmpty) {
        val formWithError = addressForm.fill(formData).withError("postcode", "A valid post code is required")
        BadRequest(blankUkForm(tag, cfg(tag), formWithError, noMatchesWereFound = false, exceededLimit = false))

      } else {
        val cu = Some(formData.continue)
        val nameOrNumber = formData.nameNo.getOrElse("-")
        val proposalsRoute =
          routes.UkAddressLookupController.getProposals(tag, nameOrNumber, pc.get.toString, formData.guid, cu, None, formData.backUrl, formData.backText)
        SeeOther(proposalsRoute.url)
      }
    }
  }

  //-----------------------------------------------------------------------------------------------

  def getProposals(tag: String, nameNo: String, postcode: String, guid: String, continue: Option[String],
                   editId: Option[String], backUrl: Option[String], backText: Option[String]): Action[AnyContent] =
    TaggedAction.withTag(tag).async {
      implicit request =>
        val optNameNo = normaliseNumber(nameNo)
        val uPostcode = Postcode.cleanupPostcode(postcode)
        if (uPostcode.isEmpty) {
          Future.successful(BadRequest(basicBlankForm(tag, Some(guid), continue, backUrl, backText)))

        } else {
          lookup.findByPostcode(uPostcode.get, optNameNo).map {
            list =>
              val cu = continue.getOrElse(defaultContinueUrl)
              val exceededLimit = list.size > cfg(tag).maxAddressesToShow
              val pc = uPostcode.map(_.toString)
              val data = UkAddressData(guid = guid, continue = cu,
                backUrl = backUrl, backText = backText,
                nameNo = optNameNo, postcode = pc,
                prevNameNo = optNameNo, prevPostcode = pc,
                countryCode = UkCode
              )
              if (list.isEmpty || exceededLimit) {
                val filledInForm = addressForm.fill(data)
                Ok(blankUkForm(tag, cfg(tag), filledInForm, noMatchesWereFound = list.isEmpty, exceededLimit = exceededLimit))

              } else {
                Ok(showAddressListProposalForm(tag, data, list, editId))
              }
          }
        }
    }

  private def normaliseNumber(number: String): Option[String] = {
    val t = number.trim
    if (number.isEmpty || number == "-") {
      None
    } else {
      Some(t)
    }
  }

  //-----------------------------------------------------------------------------------------------

  def postSelected(tag: String): Action[AnyContent] =
    TaggedAction.withTag(tag).async {
      implicit request =>
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
        addressData.postcode.getOrElse("-"), addressData.guid, Some(addressData.continue), None, addressData.backUrl, addressData.backText)
      Future(SeeOther(proposalsRoute.url))

    } else {
      continueToCompletion(tag, addressData, request)
    }
  }


  private def continueToCompletion(tag: String, addressData: UkAddressData, request: Request[_]): Future[Result] = {
    if (addressData.uprnId.isEmpty) {
      val response = SelectedAddress(userSuppliedAddress = addressData.editedAddress, noFixedAddress = addressData.noFixedAddress)
      memo.storeSingleResponse(tag, addressData.guid, response).map {
        _ => SeeOther(addressData.continue + "?id=" + addressData.guid)
      }

    } else {
      lookup.findById(addressData.uprnId.get).flatMap {
        normativeAddress =>
          val response = SelectedAddress(
            normativeAddress = normativeAddress,
            userSuppliedAddress = addressData.editedAddress,
            noFixedAddress = addressData.noFixedAddress)
          memo.storeSingleResponse(tag, addressData.guid, response).map {
            _ => SeeOther(addressData.continue + "?tag=" + tag + "&id=" + addressData.guid)
          }
      }
    }
  }

  //-----------------------------------------------------------------------------------------------

  def confirmation(tag: String, id: String): Action[AnyContent] =
    TaggedAction.withTag(tag).async {
      implicit request =>
        require(id.nonEmpty)
        val fuResponse = memo.fetchSingleResponse(tag, id)
        fuResponse.map {
          response: Option[JsValue] =>
            if (response.isEmpty) {
              val emptyFormRoute = routes.UkAddressLookupController.getEmptyForm(tag, Some(id), None, None, None)
              TemporaryRedirect(emptyFormRoute.url)
            } else {
              import SelectedAddress._
              val addressRecord = response.get.as[SelectedAddress]
              Ok(confirmationPage(id, tag, cfg(tag), addressRecord.normativeAddress, addressRecord.userSuppliedAddress, addressRecord.international))
            }
        }
    }

  private val UkCode = Some(Countries.UK.code)
}
