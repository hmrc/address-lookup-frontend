/*
 * Copyright 2020 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

  def messageConstants(isWelsh:Boolean, isUkMode: Boolean = true): MessageConstants = if(isWelsh) WelshMessageConstants(isUkMode) else EnglishMessageConstants(isUkMode)

  def hasInvalidChars(chars: String) = !chars.replaceAll("\\s", "").forall(_.isLetterOrDigit)

  def isInvalidPostcode(postcode: String) = !Postcode.cleanupPostcode(postcode).isDefined

  def postcodeConstraint(isWelsh: Boolean, isUkMode: Boolean): Constraint[String] = Constraint[String](Some("constraints.postcode"), Seq.empty)({
    case empty if empty.isEmpty => Invalid(Seq(ValidationError(messageConstants(isWelsh, isUkMode).lookupPostcodeEmptyError)))
    case chars if hasInvalidChars(chars) => Invalid(Seq(ValidationError(messageConstants(isWelsh, isUkMode).lookupPostcodeInvalidError)))
    case postcode if isInvalidPostcode(postcode) => Invalid(Seq(ValidationError(messageConstants(isWelsh, isUkMode).lookupPostcodeError)))
    case _ => Valid
  })


  def lookupForm(isWelsh: Boolean = false, isUkMode: Boolean = false) = Form(
    mapping(
      "filter" -> optional(text.verifying(messageConstants(isWelsh, isUkMode).lookupFilterError, txt => txt.length < 256)),
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


  def isValidPostcode(form: Form[Edit], isWelsh: Boolean = false, isUKMode: Boolean = true): Form[Edit] = {
    val isGB: Boolean = form("countryCode").value.fold(true)(_ == "GB")
    val postcode = form("postcode").value.getOrElse("")

    (isGB, postcode) match {
      case (true, p) if p.nonEmpty && !Postcode.cleanupPostcode(postcode).isDefined => form.withError("postcode", messageConstants(isWelsh, isUKMode).editPagePostcodeErrorMessage)
      case _ => form
    }
  }


  def ukEditForm(isWelsh: Boolean = false, isUKMode: Boolean = true): Form[Edit] = Form(
    mapping(
      "line1" -> customErrorTextValidation(messageConstants(isWelsh, isUKMode).editPageAddressLine1MinErrorMessage)
        .verifying(constraintString256(messageConstants(isWelsh, isUKMode).editPageAddressLine1MaxErrorMessage)),
      "line2" -> optional(text.verifying(constraintString256(messageConstants(isWelsh, isUKMode).editPageAddressLine2MaxErrorMessage))),
      "line3" -> optional(text.verifying(constraintString256(messageConstants(isWelsh, isUKMode).editPageAddressLine3MaxErrorMessage))),
      "town" -> customErrorTextValidation(messageConstants(isWelsh, isUKMode).editPageTownMinErrorMessage)
        .verifying(constraintString256(messageConstants(isWelsh, isUKMode).editPageTownMaxErrorMessage)),
      "postcode" -> default(text,""),
      "countryCode" -> ignored[String]("GB")
    )(Edit.apply)(Edit.unapply)
  )

  def nonUkEditForm(isWelsh: Boolean = false, isUKMode: Boolean = false) = Form(
    mapping(
      "line1" -> customErrorTextValidation(messageConstants(isWelsh, isUKMode).editPageAddressLine1MinErrorMessage)
        .verifying(constraintString256(messageConstants(isWelsh, isUKMode).editPageAddressLine1MaxErrorMessage)),
      "line2" -> optional(text.verifying(constraintString256(messageConstants(isWelsh, isUKMode).editPageAddressLine2MaxErrorMessage))),
      "line3" -> optional(text.verifying(constraintString256(messageConstants(isWelsh, isUKMode).editPageAddressLine3MaxErrorMessage))),
      "town" -> customErrorTextValidation(messageConstants(isWelsh, isUKMode).editPageTownMinErrorMessage)
        .verifying(constraintString256(messageConstants(isWelsh, isUKMode).editPageTownMaxErrorMessage)),
      "postcode" -> default(text,""),
      "countryCode" -> customErrorTextValidation(messageConstants(isWelsh, isUKMode).editPageCountryErrorMessage)
    )(Edit.apply)(Edit.unapply)
  )

  val confirmedForm = Form(
    mapping(
      "id" -> text(1, 255)
    )(Confirmed.apply)(Confirmed.unapply)
  )
}