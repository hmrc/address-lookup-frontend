package address.uk

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.{JsPath, Reads, Writes}
import uk.gov.hmrc.address.v2.{Address, AddressRecord}


case class AddressRecordWithEdits(normativeAddress: Option[AddressRecord],
                                  userSuppliedAddress: Option[Address],
                                  noFixedAddress: Boolean)


object ResponseReadable {

  import osgb.outmodel.v2.AddressReadable._

  implicit val AddressRecordWEReads: Reads[AddressRecordWithEdits] = (
    (JsPath \ "normativeAddress").readNullable[AddressRecord] and
      (JsPath \ "userSuppliedAddress").readNullable[Address] and
      (JsPath \ "noFixedAddress").read[Boolean]) (AddressRecordWithEdits.apply _)

}


object ResponseWriteable {

  import osgb.outmodel.v2.AddressWriteable._

  implicit val AddressRecordWEWrites: Writes[AddressRecordWithEdits] = (
    (JsPath \ "normativeAddress").writeNullable[AddressRecord] and
      (JsPath \ "userSuppliedAddress").writeNullable[Address] and
      (JsPath \ "noFixedAddress").write[Boolean]) (unlift(AddressRecordWithEdits.unapply))

}
