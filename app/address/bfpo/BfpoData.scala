package address.bfpo

import play.api.data.Form
import play.api.data.Forms.{optional, text, _}
import uk.gov.hmrc.address.v2.{Countries, International}

object BfpoForm {
  val bfpoForm = Form[BfpoData] {
    mapping(
      "guid" -> text,
      "continue-url" -> text,
      "address-lines" -> optional(text),
      "postcode" -> optional(text),
      "number" -> optional(text),
      "prev-postcode" -> optional(text),
      "prev-number" -> optional(text),
      "radio-inline-group" -> optional(text)
    )(BfpoData.apply)(BfpoData.unapply)
  }
}

case class BfpoData(
                     guid: String,
                     continue: String,
                     lines: Option[String] = None,
                     postcode: Option[String] = None,
                     number: Option[String] = None,
                     prevPostcode: Option[String] = None,
                     prevNumber: Option[String] = None,
                     uprn: Option[String] = None
                   ) {

  def hasBeenUpdated: Boolean = (prevNumber != number) || (prevPostcode != postcode)

  def toInternational: International = International(
    lines.toList.flatMap(_.split("\n").map(_.trim)),
    postcode,
    Some(Countries.UK)
  )
}
