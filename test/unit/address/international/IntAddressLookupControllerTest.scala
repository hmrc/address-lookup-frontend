package address.international

import address.ViewConfig
import address.uk.service.AddressLookupService
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import keystore.MemoService
import org.jsoup.Jsoup
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.mvc.Security
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.SessionCache

class IntAddressLookupControllerTest extends PlaySpec with MockitoSugar with OneAppPerSuite {

  val ec = scala.concurrent.ExecutionContext.Implicits.global

  implicit val system = ActorSystem("AddressLookupControllerTest")
  implicit def mat: Materializer = ActorMaterializer()

  private val allTags = ViewConfig.cfg.filter(_._2.allowInternationalAddress).keys.toList.sorted
  private val tag = allTags.head

  trait action {
    val cache = mock[SessionCache]
    val lookup = mock[AddressLookupService]
    val keystore = mock[MemoService]
    val controller = new IntAddressLookupController(lookup, keystore, ec)
    val req = FakeRequest().withSession(Security.username -> "user")
  }

  "getEmptyForm" should {

    "display new empty form including the supplied guid, continue URL and country code" in new action {
      val result = call(controller.getEmptyForm(tag, Some("abc123"), Some("/here/there/everywhere")), req)
      status(result) mustBe 200
      val doc = Jsoup.parse(contentAsString(result))
      doc.select("body.entry-form").size mustBe 1
      doc.select("#guid").`val`() mustBe "abc123"
      doc.select("#continue-url").`val`() mustBe "/here/there/everywhere"
    }

    "display new empty form including a generated guid if no guid is supplied" in new action {
      val result = call(controller.getEmptyForm(tag, None, Some("/here/there/everywhere")), req)
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

//    "give bad-request if the tag is unknown" in new action {
//      val result = call(controller.getProposals("no-such-tag", "", "SE1 9PY", "abc123", None, None), req)
//      status(result) mustBe 400
//    }
  }

}
