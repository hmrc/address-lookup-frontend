package address.uk

import address.uk.service.AddressLookupService
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import keystore.KeystoreService
import org.jsoup.Jsoup
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import org.scalatest.mock.MockitoSugar
import play.api.mvc.Security
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.SessionCache

class AddressLookupControllerTest extends PlaySpec with MockitoSugar with OneAppPerSuite {

  val ec = scala.concurrent.ExecutionContext.Implicits.global

  implicit val system = ActorSystem("AddressLookupControllerTest")
  implicit def mat: Materializer = ActorMaterializer()

  trait action {
    val cache = mock[SessionCache]
    val lookup = mock[AddressLookupService]
    val keystore = mock[KeystoreService]
    val controller = new AddressLookupController(lookup, keystore, ec)
    val req = FakeRequest().withSession(Security.username -> "user")
  }

  "getEmptyForm" should {

    "display new empty form including the supplied guid, continue URL and country code" in new action {
      val result = call(controller.getEmptyForm("j0", Some("abc123"), Some("/here/there/everywhere")), req)
      status(result) mustBe 200
      val doc = Jsoup.parse(contentAsString(result))
      doc.select("body.entry-form").size mustBe 1
      doc.select("#guid").`val`() mustBe "abc123"
      doc.select("#continue-url").`val`() mustBe "/here/there/everywhere"
      doc.select("#country-code").`val`() mustBe "UK"
    }

    "display new empty form including a generated guid if no guid is supplied" in new action {
      val result = call(controller.getEmptyForm("j0", None, Some("/here/there/everywhere")), req)
      status(result) mustBe 200
      val doc = Jsoup.parse(contentAsString(result))
      doc.select("body.entry-form").size mustBe 1
      doc.select("#guid").`val`().nonEmpty mustBe true
    }

    "give bad-request if the tag is unknown" in new action {
      val result = call(controller.getEmptyForm("no-such-tag", Some("abc123"), Some("/here/there/everywhere")), req)
      status(result) mustBe 400
    }
  }


  "getProposals" should {

    "give bad-request if the tag is unknown" in new action {
      val result = call(controller.getProposals("no-such-tag", "", "SE1 9PY", "abc123", None, None), req)
      status(result) mustBe 400
    }
  }

  //  "next" should {
  //
  //    "redirect to assessment page" in new action {
  //      val formReq: FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest(POST, "/bank-account-reputation-frontend/bankAccountDetails").withSession(Security.username -> "user").withFormUrlEncodedBody("sortCode" -> sortCode, "accountNumber" -> accountNumber)
  //      val res = call(controller.postNext(), formReq)
  //      status(res) must be (303)
  //      header(LOCATION, res) must be (Some(routes.RiskAssessmentResultController.assess.toString))
  //      verify(cache).cache[BankAccountDetails](Matchers.eq("bankAccountDetails"), Matchers.eq(bd))(any[Writes[BankAccountDetails]], any[HeaderCarrier])
  //    }
  //
  //    "return bad request given invalid form" in new action {
  //      val formReq: FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest(POST, "/bank-account-reputation-frontend/bankAccountDetails").withSession(Security.username -> "user").withFormUrlEncodedBody("sortCode" -> sortCode)
  //      val res = call(controller.postNext(), formReq)
  //      status(res) must be(400)
  //      css("form input[name=sortCode]", res).`val`() must be(bd.sortCode)
  //      css("form input[name=accountNumber]", res).`val`() must be(empty) // no account number
  //    }
  //
  //  }
}
