package address.uk

import com.fasterxml.uuid.{EthernetAddress, Generators}
import config.FrontendGlobal
import keystore.KeystoreService
import play.api.mvc.{Action, AnyContent, Result}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.util.JacksonMapper._

import scala.concurrent.{ExecutionContext, Future}


object KeystoreFacadeController extends KeystoreFacadeController(
  Services.metricatedKeystoreService,
  FrontendGlobal.executionContext)


class KeystoreFacadeController(keystore: KeystoreService, val ec: ExecutionContext) extends FrontendController {

  private implicit val xec = ec

  private val uuidGenerator = Generators.timeBasedGenerator(EthernetAddress.fromInterface())

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
          Ok(writeValueAsString(address)).withHeaders(CONTENT_TYPE -> JSON)
    }
  }
}
