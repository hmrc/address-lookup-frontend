package address.international

import address.uk.{Services, TaggedAction, UkAddressForm}
import address.uk.service.AddressLookupService
import com.fasterxml.uuid.{EthernetAddress, Generators}
import config.FrontendGlobal
import keystore.MemoService
import play.api.mvc.{Action, AnyContent, Request}
import uk.gov.hmrc.address.v2.Countries
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.addressuk._
import play.api.i18n.Messages.Implicits._
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
import views.html.addressint._

import scala.concurrent.ExecutionContext

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

  def getEmptyForm(tag: String, guid: Option[String], continue: Option[String]): Action[AnyContent] =
    TaggedAction.withTag(tag).apply {
      implicit request =>
        Ok(basicBlankForm(tag, guid, continue))
    }

  private def basicBlankForm(tag: String, guid: Option[String], continue: Option[String])(implicit request: Request[_]) = {
    val actualGuid = guid.getOrElse(uuidGenerator.generate.toString)
    val cu = continue.getOrElse(defaultContinueUrl)
    val ad = IntAddressData(guid = actualGuid, continue = cu, country = Some(UkCode))
    val bound = addressForm.fill(ad)
    blankIntForm(tag, cfg(tag), bound, noMatchesWereFound = false, exceededLimit = false)
  }

  private val UkCode = Countries.UK.code
}
