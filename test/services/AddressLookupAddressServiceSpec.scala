package services

import config.FrontendAppConfig
import model.ProposedAddress
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import uk.gov.hmrc.address.v2._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.collection.immutable.Seq
import scala.concurrent.Future
import scala.util.Random

class AddressLookupAddressServiceSpec extends PlaySpec with GuiceOneAppPerSuite with MockitoSugar with ScalaFutures {

  class Scenario(resp: List[AddressRecord]) {
    private implicit val ec = play.api.libs.concurrent.Execution.Implicits.defaultContext
    implicit val hc = HeaderCarrier()
    val end = "http://localhost:42"

    val httpClient = mock[HttpClient]
    val frontendAppConfig = app.injector.instanceOf[FrontendAppConfig]

    when(httpClient.GET[List[AddressRecord]](any(), any())(any(), any(), any())).thenReturn(Future.successful(resp))

    val service = new AddressLookupAddressService(frontendAppConfig, httpClient)(ec) {
      override val endpoint = end
    }
  }

  "find" should {

    "find addresses by postcode & isukMode == false" in new Scenario(oneAddress) {
      service.find("ZZ11 1ZZ", isukMode = false).futureValue must be(toProposals(oneAddress))
    }

    "map UK to GB & isukMode == false" in new Scenario(List(addr(Some("UK")))) {
      service.find("ZZ11 1ZZ", isukMode = false).futureValue.head.country.code must be("GB")
    }

    "return multiple addresses with diverse country codes when isukMode == false" in new Scenario(
      manyAddresses(0)(Some("foo")) ::: manyAddresses(1)(Some("UK"))) {
      service.find("ZZ11 1ZZ", isukMode = false).futureValue.map(a => a.country.code) mustBe Seq("foo", "GB", "GB")
    }

    "return no addresses where ukMode == true and all addresses are non UK addresses" in new Scenario(
      manyAddresses(2)(Some("foobar"))) {
      service.find("ZZ11 1ZZ", isukMode = true).futureValue.headOption must be(None)
    }
    "return 2 addresses where ukMode == true and 2 out of 3 addresses are UK" in new Scenario(
      manyAddresses(0)(Some("foo")) ::: manyAddresses(1)(Some("UK"))) {

      service.find("ZZ11 1ZZ", isukMode = true).futureValue.map(a => a.country.code) mustBe Seq("GB", "GB")
    }
  }

  private val manyAddresses = (numberOfAddresses: Int) =>
    (code: Option[String]) => someAddresses(numberOfAddresses, addr(code))

  private val oneAddress = someAddresses(1, addr(Some("GB")))

  private def someAddresses(num: Int = 1, addr: AddressRecord): List[AddressRecord] = {
    (0 to num).map { i =>
      addr
    }.toList
  }

  private def addr(code: Option[String]): AddressRecord = {
    AddressRecord(
      rndstr(16),
      Some(Random.nextLong()),
      Address(
        List(rndstr(16), rndstr(16), rndstr(8)),
        Some(rndstr(16)),
        Some(rndstr(8)),
        rndstr(8),
        Some(Countries.England),
        Country(code.get, rndstr(32))
      ),
      "en",
      Some(LocalCustodian(123, "Tyne & Wear")), None, None, None, None
    )
  }

  private def rndstr(i: Int): String = Random.alphanumeric.take(i).mkString

  private def toProposals(found: List[AddressRecord]): Seq[ProposedAddress] = {
    found.map { addr =>
      ProposedAddress(
        addr.id,
        addr.address.postcode,
        addr.address.lines,
        addr.address.town,
        addr.address.county,
        addr.address.country
      )
    }
  }

}
