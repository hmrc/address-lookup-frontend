
package forms

import controllers.Confirmed
import model.{Edit, Lookup, Select}
import play.api.data.{Form, Mapping}
import play.api.data.Forms.{default, mapping, optional, text,ignored}
import play.api.data.validation.{Constraint, Invalid, Valid}
import uk.gov.hmrc.address.uk.Postcode

object ALFForms {

  val lookupForm = Form(
    mapping(
      "filter" -> optional(text.verifying("Your house name/number needs to be fewer than 256 characters", txt => txt.length < 256)),
      "postcode" -> text.verifying("The postcode you entered appears to be incomplete or invalid. Please check and try again.", p => Postcode.cleanupPostcode(p).isDefined)
    )(Lookup.apply)(Lookup.unapply)
  )

  val selectForm = Form(
    mapping(
      "addressId" -> text(1, 255)
    )(Select.apply)(Select.unapply)
  )
    val constraintString256 = (msg: String)  => new Constraint[String](Some("length.max"),Seq.empty)(s => if(s.length < 256) {
    Valid
  } else {
    Invalid(s"Enter a $msg using 256 characters or less")
  } )

  val constraintMinLength = (msg: String) => new Constraint[String](Some("length.min"),Seq.empty)(s => if(s.nonEmpty) {
    Valid
  } else {
    Invalid(msg)
  } )

  def isValidPostcode(form: Form[Edit]): Form[Edit] = {
    val isGB: Boolean = form("countryCode").value.fold(true)(_ == "GB")
    val postcode = form("postcode").value.getOrElse("")

    (isGB, postcode) match {
      case (true, p) if p.nonEmpty && !Postcode.cleanupPostcode(postcode).isDefined => form.withError("postcode", "Enter a valid UK postcode")
      case _ => form
    }
  }

  def ukEditForm: Form[Edit] = Form(
    mapping(
      "line1" -> text
        .verifying(constraintString256("first address line"))
        .verifying(constraintMinLength("Enter first line of address")),
      "line2" -> default[Option[String]](optional(text.verifying(constraintString256("second address line"))), Some("")),
      "line3" -> optional(text.verifying(constraintString256("third address line"))),
      "town" -> text
        .verifying(constraintString256("town or city"))
        .verifying(constraintMinLength("Enter a town or city")),
      "postcode" -> default(text,""),
      "countryCode" -> ignored[Option[String]](Option("GB"))
    )(Edit.apply)(Edit.unapply)
  )

  def nonUkEditForm = Form(
    mapping(
      "line1" -> text
        .verifying(constraintString256("first address line"))
        .verifying(constraintMinLength("Enter first line of address")),
      "line2" -> optional(text.verifying(constraintString256("second address line"))),
      "line3" ->  optional(text.verifying(constraintString256("third address line"))),
      "town" -> text
        .verifying(constraintString256("town or city"))
        .verifying(constraintMinLength("Enter a town or city")),
      "postcode" -> default(text,""),
      "countryCode" -> optional(text(2))
    )(Edit.apply)(Edit.unapply)
  )

  val confirmedForm = Form(
    mapping(
      "id" -> text(1, 255)
    )(Confirmed.apply)(Confirmed.unapply)
  )
}