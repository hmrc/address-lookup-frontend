/*
 * Copyright 2025 HM Revenue & Customs
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

/*
 * Copyright 2025 HM Revenue & Customs
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

package model.v2

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.libs.json._

class MandatoryFieldsConfigModelSpec extends AnyWordSpecLike with Matchers {

  "MandatoryFieldsConfigModel" should {

    "convert to JSON" when {

      "all fields are true" in {
        val model = MandatoryFieldsConfigModel(
          addressLine1 = true,
          addressLine2 = true,
          addressLine3 = true,
          postcode = true,
          town = true
        )

        val expectedJson = Json.obj(
          "addressLine1" -> true,
          "addressLine2" -> true,
          "addressLine3" -> true,
          "postcode" -> true,
          "town" -> true
        )

        Json.toJson(model) mustBe expectedJson
      }

      "all fields are false" in {
        val model = MandatoryFieldsConfigModel(
          addressLine1 = false,
          addressLine2 = false,
          addressLine3 = false,
          postcode = false,
          town = false
        )

        val expectedJson = Json.obj(
          "addressLine1" -> false,
          "addressLine2" -> false,
          "addressLine3" -> false,
          "postcode" -> false,
          "town" -> false
        )

        Json.toJson(model) mustBe expectedJson
      }
    }

    "convert from JSON" when {

      "all fields are true" in {
        val json = Json.obj(
          "addressLine1" -> true,
          "addressLine2" -> true,
          "addressLine3" -> true,
          "postcode" -> true,
          "town" -> true
        )

        val expectedModel = MandatoryFieldsConfigModel(
          addressLine1 = true,
          addressLine2 = true,
          addressLine3 = true,
          postcode = true,
          town = true
        )

        Json.fromJson[MandatoryFieldsConfigModel](json) mustBe JsSuccess(expectedModel)
      }

      "all fields are false" in {
        val json = Json.obj(
          "addressLine1" -> false,
          "addressLine2" -> false,
          "addressLine3" -> false,
          "postcode" -> false,
          "town" -> false
        )

        val expectedModel = MandatoryFieldsConfigModel(
          addressLine1 = false,
          addressLine2 = false,
          addressLine3 = false,
          postcode = false,
          town = false
        )

        Json.fromJson[MandatoryFieldsConfigModel](json) mustBe JsSuccess(expectedModel)
      }
      
      "there are missing fields and the missing fields default to false" in {
        val json = Json.obj(
          "addressLine1" -> true,
          "addressLine3" -> true,
          "postcode" -> true
        )
        
        val expectedModel = MandatoryFieldsConfigModel(
          addressLine1 = true,
          addressLine2 = false,
          addressLine3 = true,
          postcode = true,
          town = false
        )
        
        Json.fromJson[MandatoryFieldsConfigModel](json) mustBe JsSuccess(expectedModel)
      }
    }
  }
}
