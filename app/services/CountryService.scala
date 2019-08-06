package services

import javax.inject.Singleton

import com.google.inject.ImplementedBy
import play.api.libs.json.Json
import uk.gov.hmrc.address.v2.Country

@ImplementedBy(classOf[ForeignOfficeCountryService])
trait CountryService {

  def findAll(welshFlag: Boolean = false): Seq[Country]

  // to match uk.gov.hmrc.address.v2.Countries and serve as a comprehensive replacement
  def find(welshFlag: Boolean = false, code: String): Option[Country]

}

@Singleton
class ForeignOfficeCountryService extends CountryService {

  implicit val fcoCountryFormat = Json.format[FcoCountry]

  private val countriesEN: Seq[Country] = Json.parse(getClass.getResourceAsStream("/countriesEN.json")).as[Map[String, FcoCountry]].map { country =>
    Country(country._2.country, country._2.name)
  }.toSeq.sortWith(_.name < _.name)

  private val countriesCY: Seq[Country] = Json.parse(getClass.getResourceAsStream("/countriesCY.json")).as[Map[String, FcoCountry]].map { country =>
    Country(country._2.country, country._2.name)
  }.toSeq.sortWith(_.name < _.name)

  override def findAll(welshFlag: Boolean = false): Seq[Country] =
    if (!welshFlag) countriesEN
    else countriesCY

  override def find(welshFlag: Boolean = false, code: String): Option[Country] = {
    if (!welshFlag) {
    val filtered = countriesEN.filter(_.code == code)
    filtered.headOption
    }
    else {
      val filtered = countriesCY.filter(_.code == code)
      filtered.headOption
    }

  }

}

case class FcoCountry(country: String, name: String)

object ForeignOfficeCountryService extends ForeignOfficeCountryService