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
import play.api.Logger
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.objectstore.client.Path
import uk.gov.hmrc.objectstore.client.play.Implicits._
import uk.gov.hmrc.objectstore.client.play.PlayObjectStoreClient

import javax.inject.{Inject, Singleton}
import scala.collection.immutable.SortedMap
import scala.concurrent.ExecutionContext
import scala.io.Source
import scala.jdk.javaapi.CollectionConverters.asScala
import scala.util.{Failure, Success};

@Singleton
class WelshCountryNamesDataSource @Inject() (english: EnglishCountryNamesDataSource) extends CountryNamesDataSource {

  protected val mutable: java.util.Deque[CachedData] = new java.util.concurrent.ConcurrentLinkedDeque()
  mutable.add(CachedData("", Source.fromInputStream(getClass.getResourceAsStream("/welsh-country-names.csv"), "UTF-8")))

  def updateCache(): Unit = {}

  def retrieveAndStoreData(): Unit = {}

  private val allAliasesCY = allAliases("/countryAliasesCY.csv")

  private val allWCORows = CSVReader.open(Source.fromInputStream(getClass.getResourceAsStream("/wco-countries.csv"), "UTF-8"))
    .allWithOrderedHeaders._2.sortBy(x => x("Country"))
    .groupBy(_("Country"))
    .view.mapValues(v => v.head)

  private def allGovWalesRows(govWalesData: Source) = CSVReader.open(govWalesData)
    .allWithOrderedHeaders._2.sortBy(x => x("Cod gwlad (Country code)"))
    .groupBy(_("Cod gwlad (Country code)"))
    .view.mapValues(v => v.head)
    .map { case (k, m) => k -> Map("Country" -> m("Cod gwlad (Country code)"), "Name" -> m("Enw yn Gymraeg (Name in Welsh)")) }

  private def countriesCYFull(govWalesData: Source): Seq[Country] =
    SortedMap.from(english.allISORows ++ english.allFCDORows ++ english.allFCDOTRows ++ allWCORows ++ allGovWalesRows(govWalesData))
      .map(Country.apply)
      .toSeq.sortWith { case (a, b) => utfSorter.compare(a.name, b.name) < 0 }

  protected def countriesWithAliases(govWalesData: Source) = {
    countriesCYFull(govWalesData).flatMap { country =>
      if (allAliasesCY.contains(country.code)) country +: allAliasesCY(country.code)
      else Seq(country)
    }
  }

  var countriesCY: Seq[Country] = countriesWithAliases(mutable.peekFirst().data)
}


@Singleton
class WelshCountryNamesObjectStoreDataSource  @Inject() (englishCountryNamesDataSource: EnglishCountryNamesDataSource, objectStore: PlayObjectStoreClient, implicit val ec: ExecutionContext,
                                                         implicit val materializer: Materializer) extends WelshCountryNamesDataSource(englishCountryNamesDataSource) {
  val logger = Logger(this.getClass)

  private val objectStorePath = Path.Directory("govwales").file("country-names.csv")

  override def retrieveAndStoreData: Unit = {
    try {
      val browser = new HtmlUnitBrowser()
      val x = browser.underlying.getPage[HtmlPage]("https://www.gov.wales/bydtermcymru/international-place-names")

      var count = 0
      var link: Option[HtmlAnchor] = None

      while (count < 60 && link.isEmpty) {
        link = asScala(x.getAnchors).toSeq.find(a => a.asNormalizedText().startsWith("Enwau gwledydd"))
        count = count + 1

        try {
          // This is blocking but usually completes on the first try
          Thread.sleep(500)
        }
      }

      val download = link.get.click[HtmlPage]();
      implicit val hc = new HeaderCarrier();

      val data = Source.fromString("")
      val csv = CSVReader.open(data)

      // Check the integrity of the data file before caching it
      if (csv.allWithHeaders().length > 1) {
          mutable.addFirst(CachedData("", data))
          if (mutable.size() > 1) mutable.removeLast()
          logger.info("Refreshed welsh country name data from third party source")

          objectStore.putObject(
            path = objectStorePath, content = "", contentType = Some("text/plain")
          ).onComplete {
            case Failure(e) => logger.error("Could not write welsh country name data to object-store", e)
            case Success(_) => logger.info("Wrote welsh country name data to object-store successfully")
          }
      }
      else {
        logger.info(s"Error parsing welsh country name data from third party, unexpected file contents")
      }

    } catch {
      case e: Exception =>
        logger.error("Welsh country name data retrieval and storage failed", e)
    }
  }

  override def updateCache(): Unit = {
    try {
      implicit val hc = new HeaderCarrier();

      import uk.gov.hmrc.objectstore.client.play.Implicits.InMemoryReads._

      objectStore.getObject[String](objectStorePath).map {
        case Some(obj) =>
          val data = Source.fromString(obj.content)
          val csv = CSVReader.open(data)

          if (csv.allWithHeaders().length > 1) {
            mutable.addFirst(CachedData("", data))
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
    }
  }
}

case class CachedData(checksum: String, data: Source)
