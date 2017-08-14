package services

import javax.inject.Singleton

import com.google.inject.ImplementedBy
import play.api.libs.json.Json
import uk.gov.hmrc.address.v2.Country

@ImplementedBy(classOf[ForeignOfficeCountryService])
trait CountryService {

  val GB: Country

  def findAll: Seq[Country]

  // to match uk.gov.hmrc.address.v2.Countries and serve as a comprehensive replacement
  def find(code: String): Option[Country]

}

@Singleton
class ForeignOfficeCountryService extends CountryService {

  implicit val fcoCountryFormat = Json.format[FcoCountry]

  override lazy val GB = find("GB").get

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