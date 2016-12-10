package address.outcome

import address.ViewConfig
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import keystore.MemoService
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.libs.json.Json
import play.api.mvc.Security
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.address.v2.Countries._
import uk.gov.hmrc.address.v2._

import scala.concurrent.Future

class OutcomeControllerTest extends PlaySpec with MockitoSugar with OneAppPerSuite {

  val ec = scala.concurrent.ExecutionContext.Implicits.global

  implicit val system = ActorSystem("AddressLookupControllerTest")

  implicit def mat: Materializer = ActorMaterializer()

  private val allTags = ViewConfig.cfg.filter(_._2.allowInternationalAddress).keys.toList.sorted
  private val tag = allTags.head

  val ne15xdLike = AddressRecord("GB4510123533", Some(4510123533L),
    Address(List("10 Taylors Court", "Monk Street", "Byker"),
      Some("Newcastle upon Tyne"), Some("Northumberland"), "NE1 5XD", Some(England), UK),
    Some(LocalCustodian(123, "Tyne & Wear")), "en")
  val edited = Address(List("10b Taylors Court", "Monk Street", "Byker"),
    Some("Newcastle upon Tyne"), Some("Northumberland"), "NE1 5XD", Some(England), UK)
  val sr = SelectedAddress(Some(ne15xdLike), Some(edited), None)

  trait action {
    val keystore = mock[MemoService]
    val controller = new OutcomeController(keystore, ec)
    val req = FakeRequest().withSession(Security.username -> "user")
  }

  "outcome" should {

    "return successful JSON response for known parameters" in new action {
      val srj = Json.toJson(sr)
      when(keystore.fetchSingleResponse(tag, "abc123")) thenReturn Future.successful(Some(srj))

      val result = call(controller.outcome(tag, "abc123"), req)

      status(result) mustBe 200
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) mustBe srj
      verify(keystore).fetchSingleResponse(tag, "abc123")
    }

    "return not-found response for unknown parameters" in new action {
      val srj = Json.toJson(sr)
      when(keystore.fetchSingleResponse(tag, "abc123")) thenReturn Future.successful(None)

      val result = call(controller.outcome(tag, "abc123"), req)

      status(result) mustBe 404
      verify(keystore).fetchSingleResponse(tag, "abc123")
    }
  }

  private def memoResponseJson(tag: String, sa: SelectedAddress) = Json.toJson(Map("data" -> Map(tag -> sa)))
}
