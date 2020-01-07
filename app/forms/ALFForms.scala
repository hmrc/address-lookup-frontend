
package forms

import controllers.Confirmed
import forms.Helpers.EmptyStringValidator
import model.MessageConstants._
import model.{Edit, Lookup, Select}
import play.api.data.{Form, FormError, Forms}
import play.api.data.Forms.{default, ignored, mapping, optional, text}
import play.api.data.format.Formatter
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}
import uk.gov.hmrc.address.uk.Postcode
import uk.gov.hmrc.play.mappers.StopOnFirstFail

object Helpers {

  trait EmptyStringValidator {
    def customErrorTextValidation(message: String) = Forms.of[String](stringFormat(message))
    def stringFormat(message: String): Formatter[String] = new Formatter[String] {

      private def getNonEmpty(key: String, data: Map[String, String]): Option[String] = data.getOrElse(key, "").trim match {
        case "" => None
        case entry => Some(entry)
      }
      def bind(key: String, data: Map[String, String]): Either[Seq[FormError],String] = getNonEmpty(key, data).toRight(Seq(FormError(key, message, Nil)))
      def unbind(key: String, value: String) = Map(key -> value)
    }

  }
}
object ALFForms extends EmptyStringValidator {

  def messageConstants(isWelsh:Boolean): MessageConstants = if(isWelsh) WelshMessageConstants else EnglishMessageConstants

  def hasInvalidChars(chars: String) = !chars.replaceAll("\\s", "").forall(_.isLetterOrDigit)

  def isInvalidPostcode(postcode: String) = !Postcode.cleanupPostcode(postcode).isDefined

  def postcodeConstraint(isWelsh: Boolean, isUkMode: Boolean): Constraint[String] = new Constraint[String](Some("constraints.postcode"), Seq.empty)({postcode =>
    val errors = postcode match {
      case empty if empty.isEmpty => Seq(ValidationError(messageConstants(isWelsh).lookupPostcodeEmptyError(isUkMode)))
      case chars if hasInvalidChars(chars) => Seq(ValidationError(messageConstants(isWelsh).lookupPostcodeInvalidError(isUkMode)))
      case postcode if isInvalidPostcode(postcode) => Seq(ValidationError(messageConstants(isWelsh).lookupPostcodeError(isUkMode)))
      case _ => Nil
    }

    if(errors.isEmpty) Valid else Invalid(errors)
  })

  def lookupForm(isWelsh: Boolean = false, isUkMode: Boolean = false) = Form(
    mapping(
      "filter" -> optional(text.verifying(messageConstants(isWelsh).lookupFilterError, txt => txt.length < 256)),
      "postcode" -> text.verifying(postcodeConstraint(isWelsh, isUkMode))
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
      "line1" -> customErrorTextValidation(messageConstants(isWelsh).editPageAddressLine1MinErrorMessage)
        .verifying(constraintString256(messageConstants(isWelsh).editPageAddressLine1MaxErrorMessage)),
      "line2" -> optional(text.verifying(constraintString256(messageConstants(isWelsh).editPageAddressLine2MaxErrorMessage))),
      "line3" -> optional(text.verifying(constraintString256(messageConstants(isWelsh).editPageAddressLine3MaxErrorMessage))),
      "town" -> customErrorTextValidation(messageConstants(isWelsh).editPageTownMinErrorMessage)
        .verifying(constraintString256(messageConstants(isWelsh).editPageTownMaxErrorMessage)),
      "postcode" -> default(text,""),
      "countryCode" -> ignored[Option[String]](Option("GB"))
    )(Edit.apply)(Edit.unapply)
  )

  def nonUkEditForm(isWelsh: Boolean = false) = Form(
    mapping(
      "line1" -> customErrorTextValidation(messageConstants(isWelsh).editPageAddressLine1MinErrorMessage)
        .verifying(constraintString256(messageConstants(isWelsh).editPageAddressLine1MaxErrorMessage)),
      "line2" -> optional(text.verifying(constraintString256(messageConstants(isWelsh).editPageAddressLine2MaxErrorMessage))),
      "line3" -> optional(text.verifying(constraintString256(messageConstants(isWelsh).editPageAddressLine3MaxErrorMessage))),
      "town" -> customErrorTextValidation(messageConstants(isWelsh).editPageTownMinErrorMessage)
        .verifying(constraintString256(messageConstants(isWelsh).editPageTownMaxErrorMessage)),
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