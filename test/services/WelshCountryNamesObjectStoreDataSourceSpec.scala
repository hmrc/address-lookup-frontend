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

import org.apache.pekko.stream.Materializer
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{verify, verifyNoInteractions, when}
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import uk.gov.hmrc.objectstore.client.{ObjectSummaryWithMd5, Path}
import uk.gov.hmrc.objectstore.client.play.PlayObjectStoreClient

import scala.concurrent.{ExecutionContext, Future}

class WelshCountryNamesObjectStoreDataSourceSpec
    extends PlaySpec
    with GuiceOneAppPerSuite
    with MockitoSugar
    with ScalaFutures {

  private val validGovWalesCsv: String =
    """
      |Cod gwlad (Country code),Enw yn Saesneg (Name in English),Enw yn Gymraeg (Name in Welsh),Enw swyddogol yn Saesneg (Official name in English),Enw swyddogol yn Gymraeg (Official name in Welsh)
      |AF,Afghanistan,Affganistan Newydd,The Islamic Republic of Afghanistan,Gweriniaeth Islamaidd Affganistan
      |AL,Albania,Albania Newydd,The Republic of Albania,Gweriniaeth Albania
      |""".stripMargin

  private val singleRowGovWalesCsv: String =
    "Cod gwlad (Country code),Enw yn Saesneg (Name in English),Enw yn Gymraeg (Name in Welsh),Enw swyddogol yn Saesneg (Official name in English),Enw swyddogol yn Gymraeg (Official name in Welsh)"

  class Scenario(
    resolveDownloadUrl: () => String = () => "https://example.com/country-names.csv",
    downloadContentFromUrl: String => String = _ => validGovWalesCsv,
    putObjectResult: Future[ObjectSummaryWithMd5] = Future.successful(null.asInstanceOf[ObjectSummaryWithMd5])
  ) {
    implicit val ec: ExecutionContext = ExecutionContext.global
    implicit val materializer: Materializer = app.materializer

    val english = new EnglishCountryNamesDataSource()
    val objectStore: PlayObjectStoreClient = mock[PlayObjectStoreClient]
    val expectedPath: Path.File = Path.Directory("govwales").file("country-names.csv")

    when(objectStore.putObject(any[Path.File], any[String], any(), any(), any(), any())(any(), any()))
      .thenReturn(putObjectResult)

    val service: WelshCountryNamesObjectStoreDataSource = new WelshCountryNamesObjectStoreDataSource(english, objectStore, ec, materializer) {
      override protected[services] def resolveGovWalesDownloadUrl(): String = resolveDownloadUrl()
      override protected[services] def downloadContent(url: String): String = downloadContentFromUrl(url)
    }
  }

  "retrieveAndStoreData" should {
    "refresh the Welsh country cache when the downloaded CSV is valid" in new Scenario {
      service.retrieveAndStoreData().futureValue

      service.countriesCY.find(_.code == "AF").get.name mustBe "Affganistan Newydd"
      service.countriesCY.find(_.code == "AL").get.name mustBe "Albania Newydd"
      verify(objectStore).putObject(eqTo(expectedPath), eqTo(validGovWalesCsv), any(), eqTo(Some("text/plain")), any(), any())(any(), any())
    }

    "leave the existing cache unchanged when the downloaded CSV does not contain enough rows" in new Scenario(
      downloadContentFromUrl = _ => singleRowGovWalesCsv
    ) {
      val originalAfghanistanName: String = service.countriesCY.find(_.code == "AF").get.name

      service.retrieveAndStoreData().futureValue

      service.countriesCY.find(_.code == "AF").get.name mustBe originalAfghanistanName
    }

    "keep refreshed Welsh country names when writing to object-store fails" in new Scenario(
      putObjectResult = Future.failed(new RuntimeException("object-store unavailable"))
    ) {
      service.retrieveAndStoreData().futureValue

      service.countriesCY.find(_.code == "AF").get.name mustBe "Affganistan Newydd"
    }

    "leave the existing cache unchanged when the data cannot be downloaded" in new Scenario(
      resolveDownloadUrl = () => throw new RuntimeException("download link missing")
    ) {
      val originalAfghanistanName: String = service.countriesCY.find(_.code == "AF").get.name

      service.retrieveAndStoreData().futureValue

      service.countriesCY.find(_.code == "AF").get.name mustBe originalAfghanistanName
      verifyNoInteractions(objectStore)
    }
  }
}





