/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package services

import address.v2._
import config.FrontendAppConfig
import model.ProposedAddress
import org.mockito.Mockito._
import org.mockito.ArgumentMatchers._
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.Json
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.io.Source
import scala.util.Random

class AddressLookupAddressServiceSpec extends PlaySpec with GuiceOneAppPerSuite with MockitoSugar with ScalaFutures {

  class Scenario(resp: List[AddressRecord]) {
    implicit val hc = HeaderCarrier()
    val end = "http://localhost:42"

    val httpClient = mock[HttpClient]
    val frontendAppConfig = app.injector.instanceOf[FrontendAppConfig]

    when(httpClient.POST[LookupAddressByPostcode, List[AddressRecord]](anyString(), any(), any())(any(), any(), any()
      , any())).thenReturn(Future.successful(resp))

    val service = new AddressLookupAddressService(frontendAppConfig, httpClient) {
      override val endpoint = end
    }
  }

  "find" should {

    "find addresses by postcode & isukMode == false" in new Scenario(oneAddress) {
      service.find("ZZ11 1ZZ", isukMode = false).futureValue must be(toProposals(oneAddress))
    }

    "map UK to GB & isukMode == false" in new Scenario(List(addr(Some("UK"))(1000L))) {
      service.find("ZZ11 1ZZ", isukMode = false).futureValue.head.country.code must be("GB")
    }

    "return multiple addresses with diverse country codes when isukMode == false" in new Scenario(
      manyAddresses(0)(Some("foo")) ::: manyAddresses(1)(Some("UK"))) {
      service.find("ZZ11 1ZZ", isukMode = false).futureValue.map(a => a.country.code) must contain("foo")
    }

    "return no addresses where ukMode == true and all addresses are non UK addresses" in new Scenario(
      manyAddresses(2)(Some("foobar"))) {
      service.find("ZZ11 1ZZ", isukMode = true).futureValue.headOption must be(None)
    }

    "return 2 addresses where ukMode == true and 2 out of 3 addresses are UK" in new Scenario(
      manyAddresses(0)(Some("foo")) ::: manyAddresses(1)(Some("UK"))) {

      service.find("ZZ11 1ZZ", isukMode = true).futureValue.map(a => a.country.code) mustBe Seq("GB", "GB")
    }

    "sort the addresses intelligently based on street/flat numbers as well as string comparisons" in new Scenario(cannedAddresses) {
      val listOfLines = service.find("ZZ11 1ZZ", isukMode = true).futureValue.map(pa => pa.lines.mkString(" "))
      val listOfOrgs = service.find("ZZ11 1ZZ", isukMode = true).futureValue.map(pa => pa.organisation).flatten

      listOfLines mustBe Seq("1 Malvern Court", "3b Malvern Court", "3c Malvern Court", "Flat 2a stuff 4 Malvern Court")

      listOfOrgs mustBe Seq("malvern-organisation")
    }

    "sort complex addresses intelligently based on street/flat numbers as well as string comparisons" in new Scenario(cannedComplexAddresses) {
      val listOfLines = service.find("ZZ11 1ZZ", isukMode = true).futureValue.map(pa => pa.lines.mkString(" "))

      listOfLines mustBe Seq(
        "Flat 1 The Curtains Up Comeragh Road",
        "Flat 2 The Curtains Up Comeragh Road",
        "Flat 1 70 Comeragh Road",
        "72a Comeragh Road",
        "Flat 1 74 Comeragh Road",
        "Flat 2 74 Comeragh Road",
        "Flat B 78 Comeragh Road"
      )
    }

    "sort the dodgy addresses without failing" in new Scenario(dodgyAddressess) {
      service.find("SK15 2BT", isukMode = true).futureValue.map(pa => pa.lines.mkString(" "))
    }

    "sort the suspect addresses without failing" in new Scenario(suspectAddresses) {
      service.find("SK15 2BT", isukMode = true).futureValue.map(pa => pa.lines.mkString(" "))
    }

    "sort the questionable addresses without failing" in new Scenario(questionableAddresses) {
      service.find("SK15 2BT", isukMode = true).futureValue.map(pa => pa.lines.mkString(" "))
    }

    "sort the dubious addresses without failing" in new Scenario(dubiousAddresses) {
      service.find("SK15 2BT", isukMode = true).futureValue.map(pa => pa.lines.mkString(" "))
    }
  }

  import services.AddressReputationFormats._

  private val dodgyAddressess = Json.parse(Source.fromResource("dodgy.json").mkString).as[List[AddressRecord]]
  private val suspectAddresses = Json.parse(Source.fromResource("suspect.json").mkString).as[List[AddressRecord]]
  private val questionableAddresses = Json.parse(Source.fromResource("questionable.json").mkString)
                                          .as[List[AddressRecord]]
  private val dubiousAddresses = Json.parse(Source.fromResource("dubious.json").mkString).as[List[AddressRecord]]

  private val cannedAddresses = List(
    cannedAddress(1000L, List("3c", "Malvern Court"), "ZZ11 1ZZ", Some("malvern-organisation")),
    cannedAddress(2000L, List("Flat 2a stuff 4", "Malvern Court"), "ZZ11 1ZZ"),
    cannedAddress(3000L, List("3b", "Malvern Court"), "ZZ11 1ZZ"),
    cannedAddress(4000L, List("1", "Malvern Court"), "ZZ11 1ZZ"))

  private val cannedComplexAddresses = List(
    cannedAddress(5000L, List("Flat 1", "74 Comeragh Road"), "ZZ11 1ZZ"),
    cannedAddress(6000L, List("Flat 2", "The Curtains Up", "Comeragh Road"), "ZZ11 1ZZ"),
    cannedAddress(7000L, List("Flat 1", "70 Comeragh Road"), "ZZ11 1ZZ"),
    cannedAddress(8000L, List("Flat 2", "74 Comeragh Road"), "ZZ11 1ZZ"),
    cannedAddress(9000L, List("Flat B", "78 Comeragh Road"), "ZZ11 1ZZ"),
    cannedAddress(10000L, List("72a", "Comeragh Road"), "ZZ11 1ZZ"),
    cannedAddress(11000L, List("Flat 1", "The Curtains Up", "Comeragh Road"), "ZZ11 1ZZ"))

  private val manyAddresses = (numberOfAddresses: Int) =>
    (code: Option[String]) => someAddresses(numberOfAddresses, addr(code)(1000L))

  private val oneAddress = someAddresses(1, addr(Some("GB"))(1000L))

  private def someAddresses(num: Int = 1, addr: AddressRecord): List[AddressRecord] = {
    (0 to num).map { i =>
      addr
    }.toList
  }

  private def cannedAddress(uprn: Long, lines: List[String], postCode: String, organisation: Option[String] = None) =
    AddressRecord(uprn.toString, Some(uprn), None, None, organisation, Address(lines, "some-town", postCode, Some(Countries.England),
      Country("GB", rndstr(32))), "en", Some(LocalCustodian(123, "Tyne & Wear")), None, None, None, None)

  private def addr(code: Option[String])(uprn: Long, organisation: Option[String] = None): AddressRecord = {
    AddressRecord(
      uprn.toString,
      Some(uprn),
      Some(uprn+100),
      Some(uprn+20),
      organisation,
      Address(
        List(rndstr(16), rndstr(16), rndstr(8)),
        rndstr(16),
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
        uprn = addr.uprn,
        parentUprn = addr.parentUprn,
        usrn = addr.usrn,
        organisation = addr.organisation,
        addr.address.postcode,
        addr.address.town,
        addr.address.lines,
        addr.address.country
      )
    }
  }
}
