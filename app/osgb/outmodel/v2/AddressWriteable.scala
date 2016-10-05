package osgb.outmodel.v2

import play.api.libs.functional.syntax._
import play.api.libs.json._
import uk.gov.hmrc.address.v2.{Address, AddressRecord, Country, LocalCustodian}

object AddressWriteable {

  // https://www.playframework.com/documentation/2.3.x/ScalaJsonCombinators

  implicit val CountryWrites: Writes[Country] = (
    (JsPath \ "code").write[String] and
      (JsPath \ "name").write[String]) (unlift(Country.unapply))

  implicit val AddressWrites: Writes[Address] = (
    (JsPath \ "lines").write[Seq[String]] and
      (JsPath \ "town").writeNullable[String] and
      (JsPath \ "county").writeNullable[String] and
      (JsPath \ "postcode").write[String] and
      (JsPath \ "subdivision").writeNullable[Country] and
      (JsPath \ "country").write[Country]) (unlift(Address.unapply))

  implicit val LocalCustodianWrites: Writes[LocalCustodian] = (
    (JsPath \ "code").write[Int] and
      (JsPath \ "name").write[String]) (unlift(LocalCustodian.unapply))

  implicit val AddressRecordWrites: Writes[AddressRecord] = (
    (JsPath \ "id").write[String] and
      (JsPath \ "uprn").writeNullable[Long] and
      (JsPath \ "address").write[Address] and
      (JsPath \ "localCustodian").writeNullable[LocalCustodian] and
      (JsPath \ "language").write[String]) (unlift(AddressRecord.unapply))

}
