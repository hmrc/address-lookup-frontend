package keystore

import com.pyruby.stubserver.StubMethod._
import config.JacksonMapper._
import org.scalatest.SequentialNestedSuiteExecution
import org.scalatestplus.play.OneAppPerSuite
import stub.StubbedKeystoreService
import uk.gov.hmrc.address.v2.{Address, AddressRecord, Countries, LocalCustodian}
import uk.gov.hmrc.play.test.UnitSpec

class KeystoreTest extends UnitSpec with OneAppPerSuite
  with SequentialNestedSuiteExecution
  with StubbedKeystoreService {

  implicit private val ec = scala.concurrent.ExecutionContext.Implicits.global

  class Context {
    keystoreStub.clearExpectations()
    val service = new Keystore(keystoreEndpoint, "foo")
  }

  val ne15xdLike = AddressRecord("GB4510123533", Some(4510123533L),
    Address(List("10 Taylors Court", "Monk Street", "Byker"),
      Some("Newcastle upon Tyne"), Some("Northumberland"), "NE1 5XD", Some(Countries.England), Countries.UK),
    Some(LocalCustodian(123, "Tyne & Wear")), "en")

  "storeResponse" should {
    """
      serialise the list of addresses as JSON
      and then PUT the data to the keystore micrservice endpoint
      and the URL must include the specified ID and variant
    """ in new Context {
      keystoreStub.expect(put("/keystore/address-lookup/id12345/data/response3")) thenReturn(204, "application/json", writeValueAsString(List(ne15xdLike)))
      val actual = await(service.storeResponse("id12345", 3, List(ne15xdLike)))
      actual.status shouldBe 204
    }
  }
}
