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

import address.v2.Country
import com.github.tototoshi.csv.CSVReader
import net.ruippeixotog.scalascraper.browser.HtmlUnitBrowser
import org.apache.pekko.stream.Materializer
import org.htmlunit.html.{HtmlAnchor, HtmlPage}
import org.htmlunit.{ProxyConfig, UnexpectedPage}
import play.api.Logger
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.objectstore.client.Path
import uk.gov.hmrc.objectstore.client.play.Implicits._
import uk.gov.hmrc.objectstore.client.play.PlayObjectStoreClient

import java.io.InputStream
import javax.inject.{Inject, Singleton}
import scala.collection.immutable.SortedMap
import scala.concurrent.{ExecutionContext, Future}
import scala.io.Source
import scala.jdk.javaapi.CollectionConverters.asScala



@Singleton
class WelshCountryNamesDataSource @Inject() (english: EnglishCountryNamesDataSource) extends CountryNamesDataSource {

  protected def streamToString(stream: InputStream): String = {
    Source.fromInputStream(stream).getLines().toList.mkString("\n")
  }

  protected val mutable: java.util.Deque[CachedData] = new java.util.concurrent.ConcurrentLinkedDeque()

  val localData = streamToString(getClass.getResourceAsStream("/welsh-country-names.csv"))
  mutable.add(CachedData("", localData))

  def isCacheReady: Boolean = Option(mutable.peekFirst()).isDefined

  def updateCache(): Future[Unit] = Future.successful()

  def retrieveAndStoreData(): Future[Unit] = Future.successful()

  private val allAliasesCY = allAliases("/countryAliasesCY.csv")

  private val allWCORows = CSVReader.open(Source.fromInputStream(getClass.getResourceAsStream("/wco-countries.csv"), "UTF-8"))
    .allWithOrderedHeaders._2.sortBy(x => x("Country"))
    .groupBy(_("Country"))
    .view.mapValues(v => v.head)

  private def allGovWalesRows(govWalesData: String) = CSVReader.open(Source.fromString(govWalesData))
    .allWithOrderedHeaders._2.sortBy(x => x("Cod gwlad (Country code)"))
    .groupBy(_("Cod gwlad (Country code)"))
    .view.mapValues(v => v.head)
    .map { case (k, m) => k -> Map("Country" -> m("Cod gwlad (Country code)"), "Name" -> m("Enw yn Gymraeg (Name in Welsh)")) }

  private def countriesCYFull(govWalesData: String): Seq[Country] =
    SortedMap.from(english.allISORows ++ english.allFCDORows ++ english.allFCDOTRows ++ allWCORows ++ allGovWalesRows(govWalesData))
      .map(Country.apply)
      .toSeq.sortWith { case (a, b) => utfSorter.compare(a.name, b.name) < 0 }

  protected def countriesWithAliases(govWalesData: String) = {
    countriesCYFull(govWalesData).flatMap { country =>
      if (allAliasesCY.contains(country.code)) country +: allAliasesCY(country.code)
      else Seq(country)
    }
  }

  def countriesCY: Seq[Country] =
    countriesWithAliases(mutable.peekFirst().data)
}


@Singleton
class WelshCountryNamesObjectStoreDataSource  @Inject() (
    englishCountryNamesDataSource: EnglishCountryNamesDataSource, objectStore: PlayObjectStoreClient,
    proxyConfig: Option[ProxyConfig], implicit val ec: ExecutionContext, implicit val materializer: Materializer)
  extends WelshCountryNamesDataSource(englishCountryNamesDataSource) {

  val logger = Logger(this.getClass)

  private val objectStorePath = Path.Directory("govwales").file("country-names.csv")

  override def retrieveAndStoreData: Future[Unit] = {
    try {
      val browser = new HtmlUnitBrowser(proxy = proxyConfig)
      val page = browser.underlying.getPage[HtmlPage](
        "https://www.gov.wales/bydtermcymru/international-place-names")

      var count = 0
      var link: Option[HtmlAnchor] = None

      while (count < 60 && link.isEmpty) {
        link = asScala(page.getAnchors).toSeq.find(a => a.asNormalizedText().startsWith("Enwau gwledydd"))
        count = count + 1

        try {
          // This is blocking but usually completes on the first try
          Thread.sleep(500)
        }
      }

      val href = s"https://www.gov.wales/${link.get.getHrefAttribute}"
      val download = browser.underlying.getPage[UnexpectedPage](href)
      implicit val hc: HeaderCarrier = new HeaderCarrier()

      val d = download.getInputStream
      val content = streamToString(d)

      val csv = CSVReader.open(Source.fromString(content))

      // Check the integrity of the data file before caching it
      if (csv.allWithHeaders().length > 1) {
          mutable.addFirst(CachedData("", content))
          if (mutable.size() > 1) mutable.removeLast()
          logger.info("Refreshed welsh country name data from third party source")

          objectStore.putObject(path = objectStorePath, content, contentType = Some("text/plain"))
            .map(_ => logger.info("Wrote welsh country name data to object-store successfully"))
            .recoverWith { case e =>
              logger.error("Could not write welsh country name data to object-store", e)
              Future.successful()
            }
      }
      else {
        logger.info(s"Error parsing welsh country name data from third party, unexpected file contents")
        Future.successful()
      }

    } catch {
      case e: Exception =>
        logger.error("Welsh country name data retrieval and storage failed", e)
        Future.successful()
    }
  }

  override def updateCache(): Future[Unit] = {
    try {
      implicit val hc = new HeaderCarrier();

      import uk.gov.hmrc.objectstore.client.play.Implicits.InMemoryReads._

      objectStore.getObject[String](objectStorePath).map {
        case Some(obj) =>
          val csv = CSVReader.open(Source.fromString(obj.content))

          if (csv.allWithHeaders().length > 1) {
            mutable.addFirst(CachedData("", obj.content))
            if (mutable.size() > 1) mutable.removeLast()
            logger.info("Refreshed welsh country name data cache from object-store")
          }
          else {
            logger.info(s"Error parsing welsh country name data cache from object-store, unexpected file contents")
          }
        case None => logger.info("Did not find welsh country name data in object-store (it may not have been initialised yet)")
      }

    } catch {
      case e: Exception =>
        logger.error("Welsh country name data cache initialisation failed", e)
        Future.successful()
    }
  }
}

case class CachedData(checksum: String, data: String)
