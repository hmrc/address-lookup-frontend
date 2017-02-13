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

package address.outcome

import address.uk.Services
import config.FrontendGlobal
import keystore.MemoService
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, Result}
import uk.gov.hmrc.play.frontend.controller.FrontendController

import scala.concurrent.{ExecutionContext, Future}


object OutcomeController extends OutcomeController(
  Services.metricatedKeystoreService,
  FrontendGlobal.executionContext)


class OutcomeController(keystore: MemoService, val ec: ExecutionContext) extends FrontendController {

  private implicit val xec = ec

  // response contains JSON representation of AddressRecordWithEdits
  def outcome(tag: String, id: String): Action[AnyContent] = Action.async {
    request =>
      require(tag.nonEmpty)
      require(id.nonEmpty)
      fetch(tag, id)
  }

  private def fetch(tag: String, id: String): Future[Result] = {
    keystore.fetchSingleResponse(tag, id) map {
      address =>
        if (address.isEmpty)
          NotFound
        else
          Ok(Json.toJson(address.get.as[SelectedAddress].toDefaultOutcomeFormat))
    }
  }
}
