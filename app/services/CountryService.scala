package services

import javax.inject.Singleton

import com.google.inject.ImplementedBy
import config.WSHttp
import play.api.libs.json.Json
import uk.gov.hmrc.address.v2.Country
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.http.ws.WSGet

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@ImplementedBy(classOf[ForeignOfficeCountryService])
trait CountryService {

  def findAll: Seq[Country]

  // to match uk.gov.hmrc.address.v2.Countries and serve as a comprehensive replacement
  def find(code: String): Option[Country]

}

@Singleton
class ForeignOfficeCountryService extends CountryService {

  implicit val fcoCountryFormat = Json.format[FcoCountry]

  private val countries: Seq[Country] = Json.parse(getClass.getResourceAsStream("/countries.json")).as[Map[String, FcoCountry]].map { country =>
    Country(country._2.country, country._2.name)
  }.toSeq.sortWith(_.name < _.name)

  override def findAll: Seq[Country] = countries

  override def find(code: String): Option[Country] = {
    val filtered = countries.filter(_.code == code)
    filtered.headOption
  }

}

case class FcoCountry(country: String, name: String)

object ForeignOfficeCountryService extends ForeignOfficeCountryService