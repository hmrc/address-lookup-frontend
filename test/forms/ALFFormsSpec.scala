/*
 * Copyright 2024 HM Revenue & Customs
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

import com.codahale.metrics.SharedMetricRegistries
import forms.ALFForms.editForm
import model.Edit
import model.v2.{MandatoryFieldsConfigModel, ManualAddressEntryConfig}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.data.validation.{Invalid, Valid}
import play.api.data.{Form, FormError}
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.inject.guice.GuiceApplicationBuilder

class ALFFormsSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {
  override implicit lazy val app: Application = {
    SharedMetricRegistries.clear()
    new GuiceApplicationBuilder().build()
  }

  val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit val messages: Messages = messagesApi.preferred(Seq(Lang("en")))

  val editFormNonUk: Form[Edit] = ALFForms.editForm(isUkMode = false)
  val editFormUk: Form[Edit] = ALFForms.editForm(isUkMode = true)

  val chars257: String = "A" * 257
  val chars256: String = "A" * 256
  val chars255: String = "A" * 255

  "ukEditForm" should {
    "return no errors with valid data" in {
      val data = Map(
        "organisation" -> "some-organisation",
        "line1" -> "foo1",
        "line2" -> "foo2",
        "line3" -> "foo3",
        "town" -> "twn",
        "postcode" -> "ZZ1 1ZZ",
        "countryCode" -> "GB")

      editFormUk.bind(data).hasErrors mustBe false
    }
    "retrieve organisation when present" in {
      val data = Map(
        "organisation" -> "some-organisation",
        "line1" -> "foo1",
        "line2" -> "foo2",
        "line3" -> "foo3",
        "town" -> "twn",
        "postcode" -> "ZZ1 1ZZ",
        "countryCode" -> "GB")

      editFormUk.bind(data).get.organisation mustBe Some("some-organisation")
    }
    "not retrieve organisation when it is not present" in {
      val data = Map(
        "line1" -> "foo1",
        "line2" -> "foo2",
        "line3" -> "foo3",
        "town" -> "twn",
        "postcode" -> "ZZ1 1ZZ",
        "countryCode" -> "GB")

      editFormUk.bind(data).get.organisation mustBe None
    }
    "should default country code if country code is different to GB but all data is valid" in {
      val data = Map(
        "line1" -> "foo1",
        "line2" -> "foo2",
        "line3" -> "foo3",
        "town" -> "twn",
        "postcode" -> "AA199ZZ",
        "countryCode" -> "FR")

      editFormUk.bind(data).get.countryCode mustBe "GB"
    }
    "should default country code if country code is not provided and all data is valid" in {
      val data = Map(
        "line1" -> "foo1",
        "line2" -> "foo2",
        "line3" -> "foo3",
        "town" -> "twn",
        "postcode" -> "AA199ZZ")

      editFormUk.bind(data).get.countryCode mustBe "GB"
    }
    "should return error if line 3 > 255 chars" in {
      val data = Map(
        "line1" -> "foo",
        "line2" -> "foo2",
        "line3" -> chars256,
        "town" -> "twn",
        "postcode" -> "AA199ZZ",
        "countryCode" -> "GB")

      editFormUk.bind(data).hasErrors mustBe true
    }

    "isvalidPostCode should accept international address with no postcode because country is defaulted to GB and postcode is optional" in {
      ALFForms.isValidPostcode(editFormUk.fill(Edit(None, None, None, None, None, "", "FR"))).hasErrors mustBe false
    }
    Seq(("case 1", "MN 99555"),
      ("case 2","A"),
      ("case 3","1"),
      ("case 4","999999999999"),
      ("case 5","ABC123XYZ123"),
      ("case 6", "SW778 2BH")).foreach{
      case (caseNum,postcode) =>
        s"$editFormUk NOT accept international address with invalid postcodes ($caseNum) because country code is defaulted to GB" in {
          ALFForms.isValidPostcode(editFormUk.fill(Edit(None, None, None, None, None, postcode, "FR"))).hasErrors mustBe true
        }
    }
    "isvalidPostCode should  accept international address with valid postcode because country is defaulted to GB" in {
      ALFForms.isValidPostcode(editFormUk.fill(Edit(None, None, None, None, None, "ZZ1 1ZZ", "FR"))).hasErrors mustBe false
    }
    "when custom ManualAddressEntryConfig JourneyOptions are supplied" when {

      val line1Limit = 50
      val line2Limit = 60
      val line3Limit = 70
      val townLimit = 80

      val config = ManualAddressEntryConfig(line1Limit, line2Limit, line3Limit, townLimit)

      "the limits are exceeded" should {
        "return an error for each field over the limit" in {

          val form: Form[Edit] = ALFForms.editForm(Some(config), isUkMode = true)

          val data = Map(
            "line1"       -> ("A" * (line1Limit + 1)),
            "line2"       -> ("A" * (line2Limit + 1)),
            "line3"       -> ("A" * (line3Limit + 1)),
            "town"        -> ("A" * (townLimit + 1)),
            "postcode"    -> "ZZ11ZZ",
            "countryCode" -> "GB"
          )

          val boundForm = form.bind(data)

          boundForm.hasErrors mustBe true
          boundForm.errors.head mustBe FormError("line1", s"The first address line needs to be fewer than ${line1Limit + 1} characters", Seq(line1Limit))
          boundForm.errors(1) mustBe FormError("line2", s"The second address line needs to be fewer than ${line2Limit + 1} characters", Seq(line2Limit))
          boundForm.errors(2) mustBe FormError("line3", s"The third address line needs to be fewer than ${line3Limit + 1} characters", Seq(line3Limit))
          boundForm.errors(3) mustBe FormError("town", s"The town or city needs to be fewer than ${townLimit + 1} characters", Seq(townLimit))
        }
      }

      "the limits are NOT exceeded (valid)" should {
        "return bound form with no errors" in {

          val form: Form[Edit] = ALFForms.editForm(Some(config), isUkMode = true)

          val data = Map(
            "line1"       -> ("A" * line1Limit),
            "line2"       -> ("A" * line2Limit),
            "line3"       -> ("A" * line3Limit),
            "town"        -> ("A" * townLimit),
            "postcode"    -> "ZZ11ZZ",
            "countryCode" -> "GB"
          )

          val boundForm = form.bind(data)

          boundForm.hasErrors mustBe false
          boundForm.value mustBe Some(Edit(
            organisation = None,
            line1 = Some("A" * line1Limit),
            line2 = Some("A" * line2Limit),
            line3 = Some("A" * line3Limit),
            town = Some("A" * townLimit),
            postcode = "ZZ11ZZ"
          ))
        }
      }
    }
  }

  "nonUkEditForm" should {
    "return no errors with valid data with invalid postcode provided because country is not GB" in {
      val data = Map(
        "line1" -> "foo1",
        "line2" -> "foo2",
        "town" -> "twn",
        "postcode" -> "fudgebarwizz123",
        "countryCode" -> "FR")

      editFormNonUk.bind(data).hasErrors mustBe false
    }
    "return errors with valid data that does not have country" in {
      val data = Map(
        "line1" -> "foo1",
        "line2" -> "foo2",
        "town" -> "twn",
        "postcode" -> "fudgebarwizz123")

      editFormNonUk.bind(data).hasErrors mustBe true
    }
    "when custom ManualAddressEntryConfig JourneyOptions are supplied" when {

      val line1Limit = 50
      val line2Limit = 60
      val line3Limit = 70
      val townLimit = 80

      val config = ManualAddressEntryConfig(line1Limit, line2Limit, line3Limit, townLimit)

      "the limits are exceeded" should {
        "return an error for each field over the limit" in {

          val form: Form[Edit] = ALFForms.editForm(Some(config), isUkMode = false)

          val data = Map(
            "line1"       -> ("A" * (line1Limit + 1)),
            "line2"       -> ("A" * (line2Limit + 1)),
            "line3"       -> ("A" * (line3Limit + 1)),
            "town"        -> ("A" * (townLimit + 1)),
            "countryCode" -> "FR"
          )

          val boundForm = form.bind(data)

          boundForm.hasErrors mustBe true
          boundForm.errors.head mustBe FormError("line1", s"The first address line needs to be fewer than ${line1Limit + 1} characters", Seq(line1Limit))
          boundForm.errors(1) mustBe FormError("line2", s"The second address line needs to be fewer than ${line2Limit + 1} characters", Seq(line2Limit))
          boundForm.errors(2) mustBe FormError("line3", s"The third address line needs to be fewer than ${line3Limit + 1} characters", Seq(line3Limit))
          boundForm.errors(3) mustBe FormError("town", s"The town or city needs to be fewer than ${townLimit + 1} characters", Seq(townLimit))
        }
      }

      "the limits are NOT exceeded (valid)" should {
        "return bound form with no errors" in {

          val form: Form[Edit] = ALFForms.editForm(Some(config), isUkMode = false)

          val data = Map(
            "line1"       -> ("A" * line1Limit),
            "line2"       -> ("A" * line2Limit),
            "line3"       -> ("A" * line3Limit),
            "town"        -> ("A" * townLimit),
            "countryCode" -> "FR"
          )

          val boundForm = form.bind(data)

          boundForm.hasErrors mustBe false
          boundForm.value mustBe Some(Edit(
            organisation = None,
            line1 = Some("A" * line1Limit),
            line2 = Some("A" * line2Limit),
            line3 = Some("A" * line3Limit),
            town = Some("A" * townLimit),
            postcode = "",
            countryCode = "FR"
          ))
        }
      }
    }
  }
  
  "editForm" should {
    
    "return no errors with valid data" when {
      
      "when none of the fields are mandatory and line 1 is given" in {
        val data = Map(
          "line1" -> "foo1"
        )

        val form = editForm(isUkMode = true)

        form.bind(data).hasErrors mustBe false
      }

      "none of the fields are mandatory and town is given" in {
        val data = Map(
          "town" -> "Some Town"
        )

        val form = editForm(isUkMode = true)

        form.bind(data).hasErrors mustBe false
      }
      
      "line 2 is set to mandatory and line 1 and town are not provided" in {
        val data = Map(
          "line2" -> "Some Line"
        )
        
        val form = editForm(Some(ManualAddressEntryConfig(
          mandatoryFields = Some(MandatoryFieldsConfigModel(
            addressLine2 = true
          ))
        )), isUkMode = true)
        
        form.bind(data).hasErrors mustBe false
        
      }
      
    }
    
    "return a single error for the mandatory line when one is set to mandatory and no data is provided" in {
      val data: Map[String, String] = Map()
      
      val form = editForm(Some(ManualAddressEntryConfig(
        mandatoryFields = Some(MandatoryFieldsConfigModel(
          addressLine3 = true
        ))
      )), isUkMode = true)
      val boundForm = form.bind(data)
      
      boundForm.hasErrors mustBe true
      boundForm.errors.length mustBe 1
      boundForm.errors.head.key mustBe "line3"
    }
    
    "return an error for each missing line when they are set to mandatory" which {
      case class Data(testingLine: String, data: Map[String, String], modelMap: MandatoryFieldsConfigModel => MandatoryFieldsConfigModel)
      
      val testData = Seq(
        Data("line1", Map("town" -> "Some Town"), _.copy(addressLine1 = true)),
        Data("line2", Map("line1" -> "line1"), _.copy(addressLine2 = true)),
        Data("line3", Map("line1" -> "line1"), _.copy(addressLine3 = true)),
        Data("postcode", Map("line1" -> "line1"), _.copy(postcode = true)),
        Data("town", Map("line1" -> "line1"), _.copy(town = true))
      )
      
      val allFalseModel = MandatoryFieldsConfigModel(
        addressLine1 = false, addressLine2 = false, addressLine3 = false, postcode = false, town = false
      )
      
      testData.foreach { data =>
        s"testing ${data.testingLine} errors if missing when set to mandatory" in {
          val settingsModel = Some(ManualAddressEntryConfig(
            mandatoryFields = Some(data.modelMap(allFalseModel))
          ))
          val form = editForm(settingsModel, isUkMode = true)
          val result = form.bind(data.data)
          
          result.hasErrors mustBe true
          result.errors.length mustBe 1
        }
      }
    }
  }

  "uk and non uk edit form" should {
    Map(editFormUk -> "uk", editFormNonUk -> "nonUk", editFormUk -> "uk Welsh", editFormNonUk -> "nonUk Welsh").foreach{ mapOfForms =>
      val formOfTest = mapOfForms._2
      val form = mapOfForms._1

      // #Scenario: UK Address no postcode
      s" accept a UK address with no postcode where for $formOfTest" in {
        ALFForms.isValidPostcode(form.fill(Edit(None, None, None, None, None, ""))).hasErrors mustBe false
      }
      // #Scenario Outline: UK Address with Invalid PostCode
      Seq(
        ("case 1","MN 99555"),
        ("case 2","A"),
        ("case 3","1"),
        ("case 4","999999999999"),
        ("case 5","ABC123XYZ123"),
        ("case 6","SW778 2BH")).foreach {
        case (caseNum, postcode) =>
          s"not accept a UK address with an invalid postcode ($caseNum) for $formOfTest" in {
            ALFForms.isValidPostcode(form.fill(Edit(None, None, None, None, None, postcode))).hasErrors mustBe true
          }
      }

      // #Scenario Outline: UK Address with Valid PostCode
      Seq(
        ("case 1","SW1A 1AA"),
        ("case 2","SW11 2BB"),
        ("case 3","SW7 9YY"),
        ("case 4","B1 1AA"),
        ("case 5","E1W 3CC"),
        ("case 6","B11 6HJ")).foreach{
        case  (caseNum,postcode) =>
          s"accept a UK address with a valid postcode ($caseNum) for $formOfTest" in {
            ALFForms.isValidPostcode(form.fill(Edit(None, None, None, None, None, postcode))).hasErrors mustBe false
          }
      }
      s"accept valid postcode and no CountryCode as country code is defaulted for $formOfTest" in {
        ALFForms.isValidPostcode(form.fill(Edit(None, None, None, None, None, "ZZ11ZZ", ""))).hasErrors mustBe false
      }

      s"$formOfTest accept input if only line 1 is present" in {
        val data = Map(
          "line1" -> "foo1",
          "line2" -> "",
          "line3" -> "",
          "town" -> "",
          "postcode" -> "ZZ11ZZ",
          "countryCode" -> "GB")

        form.bind(data).hasErrors mustBe false
      }

      s"$formOfTest accept input if only line 2 is present" in {
        val data = Map(
          "line1" -> "",
          "line2" -> "foo2",
          "line3" -> "",
          "town" -> "",
          "postcode" -> "ZZ11ZZ",
          "countryCode" -> "GB")

        form.bind(data).hasErrors mustBe false
      }

      s"$formOfTest accept input if only line 3 is present" in {
        val data = Map(
          "line1" -> "",
          "line2" -> "",
          "line3" -> "foo3",
          "town" -> "",
          "postcode" -> "ZZ11ZZ",
          "countryCode" -> "GB")

        form.bind(data).hasErrors mustBe false
      }

      s"$formOfTest accept input if only town is present" in {
        val data = Map(
          "line1" -> "",
          "line2" -> "",
          "line3" -> "",
          "town" -> "town",
          "postcode" -> "AA199ZZ",
          "countryCode" -> "GB")

        form.bind(data).hasErrors mustBe false
      }

      s"$formOfTest return error if all lines and town are empty" in {
        val data = Map(
          "line1" -> "",
          "line2" -> "",
          "line3" -> "",
          "town" -> "",
          "postcode" -> "AA199ZZ",
          "countryCode" -> "GB")

        form.bind(data).hasErrors mustBe true
      }

      s"$formOfTest return error if line 1 > 255 chars" in {
        val data = Map(
          "line1" -> chars256,
          "line2" -> "foo2",
          "line3" -> "foo3",
          "town" -> "twn",
          "postcode" -> "ZZ11ZZ",
          "countryCode" -> "GB")

        form.bind(data).hasErrors mustBe true
      }
      s"$formOfTest return error if line 2 > 255 chars" in {
        val data = Map(
          "line1" -> "foo",
          "line2" -> chars256,
          "line3" -> "foo3",
          "town" -> "twn",
          "postcode" -> "ZZ11ZZ",
          "countryCode" -> "GB")

        form.bind(data).hasErrors mustBe true
      }
      s"$formOfTest return error if line 3 > 255 chars" in {
        val data = Map(
          "line1" -> "foo",
          "line2" -> "foo2",
          "line3" -> chars256,
          "town" -> "twn",
          "postcode" -> "ZZ11ZZ",
          "countryCode" -> "GB")

        form.bind(data).hasErrors mustBe true
      }
      s"$formOfTest return error if town > 255 chars" in {
        val data = Map(
          "line1" -> "foo",
          "line2" -> "foo2",
          "line3" -> "foo3",
          "town" -> chars256,
          "postcode" -> "AA199ZZ",
          "countryCode" -> "GB")

        form.bind(data).hasErrors mustBe true
      }
    }
  }

  "constraintStringMaxLength" should {
    "return invalid for string > maxLength" in {
      ALFForms.constraintStringMaxLength("foo", 256)(chars257) mustBe Invalid("foo", 256)
    }
    "return valid for string = maxLength" in {
      ALFForms.constraintStringMaxLength("foo", 256)(chars256)  mustBe Valid
    }
    "return Valid for string < maxLength" in  {
      ALFForms.constraintStringMaxLength("foo", 256)(chars255)  mustBe Valid
    }
  }

  "constraintMinLength" should {
    "return invalid for empty string" in {
      ALFForms.constraintMinLength("foo")("") mustBe Invalid("foo")
    }
    "return valid for string > 0 chars" in {
      ALFForms.constraintMinLength("foo")("1") mustBe Valid
    }
  }
}
