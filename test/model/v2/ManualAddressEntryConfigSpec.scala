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

package model.v2

import com.codahale.metrics.SharedMetricRegistries
import model.v2.ManualAddressEntryConfig
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json._
import utils.TestConstants._

class ManualAddressEntryConfigSpec extends AnyWordSpecLike with Matchers with GuiceOneAppPerSuite {
  override implicit lazy val app: Application = {
    SharedMetricRegistries.clear()
    new GuiceApplicationBuilder().build()
  }

  "Manual Address Entry Config" when {

    "deserializing from JSON" should {

      "fail to read if a value is smaller than the minimum allowed (35)" in {
        Json.fromJson[ManualAddressEntryConfig](Json.obj(
          "line1MaxLength" -> 34,
          "line2MaxLength" -> 60,
          "line3MaxLength" -> 70,
          "townMaxLength" -> 80
        )) mustBe JsError(JsPath \ "line1MaxLength", JsonValidationError("error.min", 35))
      }

      "fail to read if a value is greater than the max allowed (255)" in {
        Json.fromJson[ManualAddressEntryConfig](Json.obj(
          "line1MaxLength" -> 256,
          "line2MaxLength" -> 60,
          "line3MaxLength" -> 70,
          "townMaxLength" -> 80
        )) mustBe JsError(JsPath \ "line1MaxLength", JsonValidationError("error.max", 255))
      }

      "read successfully from max json" in {
        Json.fromJson[ManualAddressEntryConfig](Json.obj(
          "line1MaxLength" -> 50,
          "line2MaxLength" -> 60,
          "line3MaxLength" -> 70,
          "townMaxLength" -> 80
        )) mustBe JsSuccess(ManualAddressEntryConfig(
          line1MaxLength = 50,
          line2MaxLength = 60,
          line3MaxLength = 70,
          townMaxLength = 80
        ))
      }

      "read successfully from minimal json" in {
        Json.fromJson[ManualAddressEntryConfig](emptyJson) mustBe JsSuccess(ManualAddressEntryConfig())
      }
    }

    "serialize to json" in {
      Json.toJson(ManualAddressEntryConfig()) mustBe Json.obj(
        "line1MaxLength" -> ManualAddressEntryConfig.defaultMax,
        "line2MaxLength" -> ManualAddressEntryConfig.defaultMax,
        "line3MaxLength" -> ManualAddressEntryConfig.defaultMax,
        "townMaxLength" -> ManualAddressEntryConfig.defaultMax
      )
    }
  }
}
