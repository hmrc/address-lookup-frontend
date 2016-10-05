package osgb.outmodel.v2

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import uk.gov.hmrc.address.v2.{Address, AddressRecord, Country, LocalCustodian}

object AddressReadable {

  implicit val CountryReads: Reads[Country] = (
    (JsPath \ "code").read[String](minLength[String](2) keepAnd maxLength[String](6)) and
      (JsPath \ "name").read[String]) (Country.apply _)

  implicit val AddressReads: Reads[Address] = (
    (JsPath \ "lines").read[List[String]] and
      (JsPath \ "town").readNullable[String] and
      (JsPath \ "county").readNullable[String] and
      (JsPath \ "postcode").read[String] and
      (JsPath \ "subdivision").readNullable[Country] and
      (JsPath \ "country").read[Country]) (Address.apply _)

  implicit val LocalCustodianReads: Reads[LocalCustodian] = (
    (JsPath \ "code").read[Int] and
      (JsPath \ "name").read[String]) (LocalCustodian.apply _)


  implicit val AddressRecordReads: Reads[AddressRecord] = (
    (JsPath \ "id").read[String] and
      (JsPath \ "uprn").readNullable[Long] and
      (JsPath \ "address").read[Address] and
      (JsPath \ "localCustodian").readNullable[LocalCustodian] and
      (JsPath \ "language").read[String]) (AddressRecord.apply _)

}
