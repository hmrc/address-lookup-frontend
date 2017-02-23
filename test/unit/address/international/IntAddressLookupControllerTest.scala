package address.international

import address.ViewConfig
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import keystore.MemoService
import org.jsoup.Jsoup
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.{Configuration, Environment}
import play.api.mvc.Security
import play.api.test.FakeRequest
import play.api.test.Helpers._

class IntAddressLookupControllerTest extends PlaySpec with MockitoSugar with OneAppPerSuite {

  import scala.concurrent.ExecutionContext.Implicits.global
  import play.api.i18n.Messages.Implicits._


  implicit val system = ActorSystem("AddressLookupControllerTest")
  implicit def mat: Materializer = ActorMaterializer()

  private val allTags = ViewConfig.cfg.filter(_._2.allowInternationalAddress).keys.toList.sorted
  private val tag = allTags.head

  trait action {
    private val keystore = mock[MemoService]
    val controller = new IntAddressLookupController(Environment.simple(), Configuration.load(Environment.simple()) )(global, applicationMessagesApi) {
      override lazy val memo = keystore
    }
    val req = FakeRequest().withSession(Security.username -> "user")
  }

  "getEmptyForm" should {

    "display new empty form including the supplied guid, continue URL and country code" in new action {
      private val result = call(controller.getEmptyForm(tag, Some("abc123"), Some("/here/there/everywhere"), Some("/back"), Some("back")), req)
      status(result) mustBe 200
      private val doc = Jsoup.parse(contentAsString(result))
      doc.select("body.entry-form").size mustBe 1
      doc.select("#guid").`val`() mustBe "abc123"
      doc.select("#continue-url").`val`() mustBe "/here/there/everywhere"
    }

    "display new empty form including a generated guid if no guid is supplied" in new action {
      private val result = call(controller.getEmptyForm(tag, None, Some("/here/there/everywhere"), Some("/back"), Some("back")), req)
      status(result) mustBe 200
      private val doc = Jsoup.parse(contentAsString(result))
      doc.select("body.entry-form").size mustBe 1
      doc.select("#guid").`val`().nonEmpty mustBe true
    }

    "give bad-request if the tag is unknown" in new action {
      private val result = call(controller.getEmptyForm("no-such-tag", Some("abc123"), Some("/here/there/everywhere"), Some("/back"), Some("back")), req)
      status(result) mustBe 400
    }
  }
}
