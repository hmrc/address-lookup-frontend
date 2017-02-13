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

  private val en = "en"
  val ne15xdLike = AddressRecord("GB4510123533", Some(4510123533L),
    Address(List("10 Taylors Court", "Monk Street", "Byker"),
      Some("Newcastle upon Tyne"), Some("Northumberland"), "NE1 5XD", Some(England), UK),
    en, Some(LocalCustodian(123, "Tyne & Wear")), None, None, None, None)
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
      contentAsJson(result) mustBe Json.toJson(sr.toDefaultOutcomeFormat)
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

  "default outcome format" should {

    val usa = SelectedAddress(userSuppliedAddress = Some(Address(
      List("line 1", "line 2", "line 3", "line 4 removed"), Some("town"), Some("county"), "postcode", None, Country("code", "name")
    )))

    val int = SelectedAddress(international = Some(International(
      List("line 1", "line 2", "line 3", "line 4", "line 5 removed"), Some("postcode"), Some(Country("code", "name"))
    )))

    val bfpo = SelectedAddress(bfpo = Some(International(
      List("line 1", "line 2", "line 3", "line 4", "line 5 removed"), Some("postcode"), Some(Country("code", "name"))
    )))

    val norm = SelectedAddress(normativeAddress = Some(AddressRecord(
      "id", None, Address(
        lines = List("line 1", "line 2", "line 3", "line 4 removed"),
        town = Some("town"),
        county = Some("county"),
        postcode = "postcode",
        subdivision = None,
        country = Country("code", "name")
      ),
      "language", None, None, None, None, None
    )))

    "map normative address id" in {
      norm.toDefaultOutcomeFormat.id must be (Some("id"))
    }

    "map normative address lines" in {
      norm.toDefaultOutcomeFormat.address.get.lines must be (Some(List("line 1", "line 2", "line 3", "town")))
    }

    "map normative address postcode" in {
      norm.toDefaultOutcomeFormat.address.get.postcode must be (Some("postcode"))
    }

    "map normative address country" in {
      norm.toDefaultOutcomeFormat.address.get.country must be (Some(DefaultOutcomeFormatAddressCountry(Some("code"), Some("name"))))
    }

    "map user supplied address lines" in {
      usa.toDefaultOutcomeFormat.address.get.lines must be (Some(List("line 1", "line 2", "line 3", "town")))
    }

    "map user supplied address postcode" in {
      usa.toDefaultOutcomeFormat.address.get.postcode must be (Some("postcode"))
    }

    "map user supplied address country" in {
      usa.toDefaultOutcomeFormat.address.get.country must be (Some(DefaultOutcomeFormatAddressCountry(Some("code"), Some("name"))))
    }

    "map international address lines" in {
      int.toDefaultOutcomeFormat.address.get.lines must be (Some(List("line 1", "line 2", "line 3", "line 4")))
    }

    "map international address postcode" in {
      int.toDefaultOutcomeFormat.address.get.postcode must be (Some("postcode"))
    }

    "map international address country" in {
      int.toDefaultOutcomeFormat.address.get.country must be (Some(DefaultOutcomeFormatAddressCountry(Some("code"), Some("name"))))
    }

    "map bfpo address lines" in {
      bfpo.toDefaultOutcomeFormat.address.get.lines must be (Some(List("line 1", "line 2", "line 3", "line 4")))
    }

    "map bfpo address postcode" in {
      bfpo.toDefaultOutcomeFormat.address.get.postcode must be (Some("postcode"))
    }

    "map bfpo address country" in {
      bfpo.toDefaultOutcomeFormat.address.get.country must be (Some(DefaultOutcomeFormatAddressCountry(Some("code"), Some("name"))))
    }

  }

  private def memoResponseJson(tag: String, sa: SelectedAddress) = Json.toJson(Map("data" -> Map(tag -> sa)))
}
