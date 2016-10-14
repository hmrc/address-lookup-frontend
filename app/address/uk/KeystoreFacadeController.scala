package address.uk

import config.FrontendGlobal
import keystore.KeystoreService
import uk.gov.hmrc.play.frontend.controller.FrontendController

import scala.concurrent.ExecutionContext


object KeystoreFacadeController extends KeystoreFacadeController(
  Services.metricatedKeystoreService,
  FrontendGlobal.executionContext)


class KeystoreFacadeController(keystore: KeystoreService, val ec: ExecutionContext) extends FrontendController {

}
