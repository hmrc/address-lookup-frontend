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

package address.international

import address.outcome.SelectedAddress
import address.uk._
import address.uk.service.AddressLookupService
import com.fasterxml.uuid.{EthernetAddress, Generators}
import config.FrontendGlobal
import keystore.MemoService
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.libs.json.JsValue
import play.api.mvc.{Action, AnyContent, Request, Result}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.addressint._
import views.html.addressuk.confirmationPage

import scala.concurrent.{ExecutionContext, Future}

object IntAddressLookupController extends IntAddressLookupController(
  Services.configuredAddressLookupService,
  Services.metricatedKeystoreService,
  FrontendGlobal.executionContext)


class IntAddressLookupController(lookup: AddressLookupService, memo: MemoService, val ec: ExecutionContext) extends FrontendController {

  private implicit val xec = ec

  import IntAddressForm.addressForm
  import address.ViewConfig._

  private val uuidGenerator = Generators.timeBasedGenerator(EthernetAddress.fromInterface())

  //-----------------------------------------------------------------------------------------------

  def getEmptyForm(tag: String, guid: Option[String], continue: Option[String], backUrl: Option[String], backText: Option[String]): Action[AnyContent] =
    TaggedAction.withTag(tag).apply {
      implicit request =>
        if (cfg(tag).allowInternationalAddress) {
          Ok(basicBlankForm(tag, guid, continue, backUrl, backText))
        } else {
          BadRequest("International addresses are not available")
        }
    }

  private def basicBlankForm(tag: String, guid: Option[String], continue: Option[String], backUrl: Option[String], backText: Option[String])(implicit request: Request[_]) = {
    val actualGuid = guid.getOrElse(uuidGenerator.generate.toString)
    val cu = continue.getOrElse(defaultContinueUrl)
    val ad = IntAddressData(guid = actualGuid, continue = cu, backUrl = backUrl, backText = backText)
    val bound = addressForm.fill(ad)
    blankIntForm(tag, cfg(tag), bound, noMatchesWereFound = false, exceededLimit = false)
  }

  //-----------------------------------------------------------------------------------------------

  def postSelected(tag: String): Action[AnyContent] =
    TaggedAction.withTag(tag).async {
      implicit request =>
        val bound = addressForm.bindFromRequest()(request)
        if (bound.errors.nonEmpty) {
          Future.successful(BadRequest(blankIntForm(tag, cfg(tag), bound, noMatchesWereFound = false, exceededLimit = false)))

        } else {
          continueToCompletion(tag, bound.get, request)
        }
    }

  private def continueToCompletion(tag: String, addressData: IntAddressData, request: Request[_]): Future[Result] = {
    val international = addressData.asInternational
    val selected = SelectedAddress(international = Some(international))
    memo.storeSingleResponse(tag, addressData.guid, selected) map {
      httpResponse =>
        SeeOther(addressData.continue + "?id=" + addressData.guid)
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
              val emptyFormRoute = routes.IntAddressLookupController.getEmptyForm(tag, Some(id), None, None, None)
              TemporaryRedirect(emptyFormRoute.url)
            } else {
              import SelectedAddress._
              val addressRecord = response.get.as[SelectedAddress]
              Ok(confirmationPage(tag, cfg(tag), addressRecord.normativeAddress, addressRecord.userSuppliedAddress, addressRecord.international))
            }
        }
    }
}
