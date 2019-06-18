
package forms

import controllers.Confirmed
import model.MessageConstants._
import model.{Edit, Lookup, MessageConstants, Select}
import play.api.data.{Form, Mapping}
import play.api.data.Forms._
import play.api.data.validation.{Constraint, Invalid, Valid}
import uk.gov.hmrc.address.uk.Postcode
import uk.gov.hmrc.play.mappers.StopOnFirstFail


object ALFForms {

  def messageConstants(isWelsh:Boolean): MessageConstants = if(isWelsh) WelshMessageConstants else EnglishMessageConstants

  val lookupForm = Form(
    mapping(
      "filter" -> optional(text.verifying("house.fewer.text", txt => txt.length < 256)),
      "postcode" -> text.verifying("postcode.incomplete.text", p => Postcode.cleanupPostcode(p).isDefined)
    )(Lookup.apply)(Lookup.unapply)
  )

  val minimumLength: Int = 1
  val maximumLength: Int = 255

  def minimumConstraint(isWelsh: Boolean): Constraint[String] = new Constraint[String](Some("length.min"), Seq.empty)(value =>
    if(value.length >= minimumLength) Valid else Invalid(messageConstants(isWelsh).errorMin(minimumLength))
  )

  def maximumConstraint(isWelsh: Boolean): Constraint[String] = new Constraint[String](Some("length.max"), Seq.empty)(value =>
    if(value.length <= maximumLength) Valid else Invalid(messageConstants(isWelsh).errorMax(maximumLength))
  )

  def nonEmptyConstraint(isWelsh: Boolean): Constraint[String] = new Constraint[String](Some("required"), Seq.empty)(value =>
    if(value.nonEmpty) Valid else Invalid(messageConstants(isWelsh).errorRequired)
  )

  def selectForm(isWelsh: Boolean = false) = Form(
    mapping(
      "addressId" -> default(text, "").verifying(StopOnFirstFail(
        nonEmptyConstraint(isWelsh),
        minimumConstraint(isWelsh),
        maximumConstraint(isWelsh)
      ))
    )(Select.apply)(Select.unapply)
  )

  val constraintString256 = (msg: String)  => new Constraint[String](Some("length.max"),Seq.empty)(s => if(s.length < 256) {
    Valid
  } else {
    Invalid(msg)
  } )

  val constraintMinLength = (msg: String) => new Constraint[String](Some("length.min"),Seq.empty)(s => if(s.nonEmpty) {
    Valid
  } else {
    Invalid(msg)
  } )

  def isValidPostcode(form: Form[Edit], isWelsh: Boolean = false): Form[Edit] = {
    val isGB: Boolean = form("countryCode").value.fold(true)(_ == "GB")
    val postcode = form("postcode").value.getOrElse("")

    (isGB, postcode) match {
      case (true, p) if p.nonEmpty && !Postcode.cleanupPostcode(postcode).isDefined => form.withError("postcode", messageConstants(isWelsh).editPagePostcodeErrorMessage)
      case _ => form
    }
  }

  def ukEditForm(isWelsh: Boolean = false): Form[Edit] = Form(
    mapping(
      "line1" -> text
        .verifying(constraintString256(messageConstants(isWelsh).editPageAddressLine1MaxErrorMessage))
        .verifying(constraintMinLength(messageConstants(isWelsh).editPageAddressLine1MinErrorMessage)),
      "line2" -> optional(text.verifying(constraintString256(messageConstants(isWelsh).editPageAddressLine2MaxErrorMessage))),
      "line3" -> optional(text.verifying(constraintString256(messageConstants(isWelsh).editPageAddressLine3MaxErrorMessage))),
      "town" -> text
        .verifying(constraintString256(messageConstants(isWelsh).editPageTownMaxErrorMessage))
        .verifying(constraintMinLength(messageConstants(isWelsh).editPageTownMinErrorMessage)),
      "postcode" -> default(text,""),
      "countryCode" -> ignored[Option[String]](Option("GB"))
    )(Edit.apply)(Edit.unapply)
  )

  def nonUkEditForm(isWelsh: Boolean = false) = Form(
    mapping(
      "line1" -> text
        .verifying(constraintString256(messageConstants(isWelsh).editPageAddressLine1MaxErrorMessage))
        .verifying(constraintMinLength(messageConstants(isWelsh).editPageAddressLine1MinErrorMessage)),
      "line2" -> optional(text.verifying(constraintString256(messageConstants(isWelsh).editPageAddressLine2MaxErrorMessage))),
      "line3" -> optional(text.verifying(constraintString256(messageConstants(isWelsh).editPageAddressLine3MaxErrorMessage))),
      "town" -> text
        .verifying(constraintString256(messageConstants(isWelsh).editPageTownMaxErrorMessage))
        .verifying(constraintMinLength(messageConstants(isWelsh).editPageTownMinErrorMessage)),
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