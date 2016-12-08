package address.outcome

import address.uk.Services
import config.FrontendGlobal
import keystore.MemoService
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
          Ok(address.get) //.withHeaders(CONTENT_TYPE -> JSON)
    }
  }
}
