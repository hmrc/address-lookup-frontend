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
import model.v2.JourneyDataV2
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json._
import utils.TestConstants._

class JourneyDataV2Spec extends AnyWordSpecLike with Matchers with GuiceOneAppPerSuite {
  override implicit lazy val app: Application = {
    SharedMetricRegistries.clear()
    new GuiceApplicationBuilder().build()
  }

  "JourneyDataV2" should {
    "read successfully from full json" in {
      Json.fromJson[JourneyDataV2](journeyDataV2FullJson) mustBe JsSuccess(journeyDataV2Full)
    }
    "read successfully from minimal json" in {
      Json.fromJson[JourneyDataV2](journeyDataV2MinimalJson) mustBe JsSuccess(journeyDataV2MinimalExpected)
    }
    "fail to read when the journey config is missing from the json" in {
      Json.fromJson[JourneyDataV2](emptyJson) mustBe JsError(JsPath \ "config", JsonValidationError("error.path.missing"))
    }

    "write to json from full model" in {
      Json.toJson(journeyDataV2Full) mustBe journeyDataV2FullJson
    }
    "write to json from minimal model" in {
      Json.toJson(journeyDataV2MinimalExpected) mustBe journeyDataV2MinimalJson
    }
  }

  "welshEnabled" should {
    "return true" when {
      "there is welsh config provided" in {
        journeyDataV2Full.welshEnabled mustBe true
      }
      "there is no config provided" in {
        journeyDataV2Minimal.welshEnabled mustBe true
      }
    }
  }
}
