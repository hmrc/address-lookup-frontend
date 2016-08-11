package address.uk.service

import address.uk.{Address, AddressRecord}
import address.uk.Countries.UK
import org.scalatest.{BeforeAndAfterAll, SequentialNestedSuiteExecution}
import org.scalatestplus.play.OneAppPerSuite
import stub.AddRepStub
import uk.gov.hmrc.play.test.UnitSpec

class AddressLookupServiceTest extends UnitSpec with OneAppPerSuite
  with SequentialNestedSuiteExecution
  with BeforeAndAfterAll {

  implicit private val ec = scala.concurrent.ExecutionContext.Implicits.global

  val addRep = new AddRepStub()

  override def beforeAll() {
    addRep.start()
  }

  override def afterAll() {
    addRep.stop()
  }

  class Context {
    addRep.clearExpectations()
    val service = new AddressLookupService(addRep.endpoint, "foo")
  }

  val ne15xdLike = AddressRecord("GB4510123533", Some(4510123533L),
    Address(List("10 Taylors Court", "Monk Street", "Byker"),
      Some("Newcastle upon Tyne"), Some("Northumberland"), "NE1 5XD", Some("GB-ENG"), UK),
    "en")

  "AddressLookupService" should {
    "fetch an empty list ok" in new Context {
      addRep.givenAddressResponse("/uk/addresses?postcode=NE12AB", Nil)
      val actual = await(service.findAddresses("NE12AB", None))
      actual shouldBe Nil
    }

    "fetch a list ok" in new Context {
      addRep.givenAddressResponse("/uk/addresses?postcode=NE15XD", List(ne15xdLike))
      val actual = await(service.findAddresses("NE15XD", None))
      actual shouldBe List(ne15xdLike)
    }
  }

}
