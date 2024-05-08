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
import com.amazonaws.services.s3.AmazonS3
import com.github.tototoshi.csv.CSVReader
import play.api.Logger

import javax.inject.{Inject, Singleton}
import scala.collection.immutable.SortedMap
import scala.io.{BufferedSource, Source}

@Singleton
class WelshCountryNamesDataSource @Inject() (english: EnglishCountryNamesDataSource) extends CountryNamesDataSource {

  protected val mutable: java.util.Deque[S3Cache] = new java.util.concurrent.ConcurrentLinkedDeque()
  mutable.add(S3Cache("", Source.fromInputStream(getClass.getResourceAsStream("/welsh-country-names.csv"), "UTF-8")))

  def updateCache(): Unit = {}

  private val allAliasesCY = allAliases("/countryAliasesCY.csv")

  private val allWCORows = CSVReader.open(Source.fromInputStream(getClass.getResourceAsStream("/wco-countries.csv"), "UTF-8"))
    .allWithOrderedHeaders._2.sortBy(x => x("Country"))
    .groupBy(_("Country"))
    .view.mapValues(v => v.head)

  private def allGovWalesRows(govWalesData: BufferedSource) = CSVReader.open(govWalesData)
    .allWithOrderedHeaders._2.sortBy(x => x("Cod gwlad (Country code)"))
    .groupBy(_("Cod gwlad (Country code)"))
    .view.mapValues(v => v.head)
    .map { case (k, m) => k -> Map("Country" -> m("Cod gwlad (Country code)"), "Name" -> m("Enw yn Gymraeg (Name in Welsh)")) }

  private def countriesCYFull(govWalesData: BufferedSource): Seq[Country] =
    SortedMap.from(english.allISORows ++ english.allFCDORows ++ english.allFCDOTRows ++ allWCORows ++ allGovWalesRows(govWalesData))
      .map(Country.apply)
      .toSeq.sortWith { case (a, b) => utfSorter.compare(a.name, b.name) < 0 }

  protected def countriesWithAliases(govWalesData: BufferedSource) = {
    countriesCYFull(govWalesData).flatMap { country =>
      if (allAliasesCY.contains(country.code)) country +: allAliasesCY(country.code)
      else Seq(country)
    }
  }

  var countriesCY: Seq[Country] = countriesWithAliases(mutable.peekFirst().data)
}


@Singleton
class WelshCountryS3DataSource  @Inject() (englishCountryNamesDataSource: EnglishCountryNamesDataSource, client: AmazonS3, bucket: String, key: String) extends WelshCountryNamesDataSource(englishCountryNamesDataSource) {
  val logger = Logger(this.getClass)

  def cache: Option[S3Cache] = {
    val head = mutable.peekFirst()
    Option(head)
  }

  override def updateCache(): Unit = {
    try {
      fetchDataFromAmazon match {
        case Some(c) =>
          mutable.addFirst(c)
          if (mutable.size() > 1) {
            mutable.removeLast()
          }

          countriesCY = countriesWithAliases(mutable.peekFirst().data)
        }
    } catch {
      case e: Exception =>
        logger.error("Cache initialisation failed", e)
    }
  }

  private def fetchDataFromAmazon: Option[S3Cache] = {
    val remoteMd5 = getMetadata("MD5")
    val localMd5 = if (cache == null || cache.isEmpty) "" else cache.get.checksum
    if (remoteMd5 != localMd5) {
      logger.info("New Welsh country data available - will refresh cache")
      val file = client.getObject(bucket, key)
      Some(S3Cache(remoteMd5, Source.fromInputStream(file.getObjectContent)))
    } else {
      logger.info("No new Welsh country data available - cache will not be refreshed")
      None
    }
  }

  private def getMetadata: Map[String, String] = {
    val metadata = client.getObjectMetadata(bucket, key)
    val eTag = metadata.getETag
    val uploadTime = metadata.getUserMetadata.get("upload-datetime")
    Map[String, String]("upload-datetime" -> uploadTime, "MD5" -> eTag)
  }

}

case class S3Cache(checksum: String, data: BufferedSource)