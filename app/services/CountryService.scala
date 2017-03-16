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

  def findAll: Future[Seq[Country]]

}

@Singleton
class ForeignOfficeCountryService extends CountryService with ServicesConfig {

  implicit val fcoCountryFormat = Json.format[FcoCountry]

  val endpoint = baseUrl("country-register")

  val http: WSGet = WSHttp

  // TODO can't imagine list changes often - should we cache results or even keep a local copy?
  override def findAll: Future[Seq[Country]] = {
    implicit val hc = HeaderCarrier()
    http.GET[Map[String, FcoCountry]](s"$endpoint/records", Seq("page-size" -> "5000")).map { countries =>
      countries.map { country =>
        Country(country._2.country, country._2.name)
      }.toSeq.sortWith(_.name < _.name)
    }
  }

}

case class FcoCountry(country: String, name: String)
