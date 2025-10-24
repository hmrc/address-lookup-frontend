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
import model.v2.TimeoutConfig
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json._
import utils.TestConstants._

class TimeoutConfigSpec extends AnyWordSpecLike with Matchers with GuiceOneAppPerSuite {
  override implicit lazy val app: Application = {
    SharedMetricRegistries.clear()
    new GuiceApplicationBuilder().build()
  }

  "TimeoutConfig" should {
    "fail to read from json with timeout amount missing" in {
      Json.fromJson[TimeoutConfig](timeoutConfigLessThanMinJson) mustBe JsError(JsPath \ "timeoutAmount", JsonValidationError("error.min", 120))
    }
    "fail to read from json with timeout url missing" in {
      Json.fromJson[TimeoutConfig](timeoutConfigMissingAmountJson) mustBe JsError(JsPath \ "timeoutAmount", JsonValidationError("error.path.missing"))
    }
    "fail to read from json when timeout amount is less than 120" in {
      Json.fromJson[TimeoutConfig](timeoutConfigMissingUrlJson) mustBe JsError(JsPath \ "timeoutUrl", JsonValidationError("error.path.missing"))
    }
  }
}
