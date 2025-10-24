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
import model._
import model.v2.JourneyLabels
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json._
import utils.TestConstants._

class LabelsSpec extends AnyWordSpecLike with Matchers with GuiceOneAppPerSuite {
  override implicit lazy val app: Application = {
    SharedMetricRegistries.clear()
    new GuiceApplicationBuilder().build()
  }

  "JourneyLabels" should {
    "read successfully from minimal json" in {
      Json.fromJson[JourneyLabels](emptyJson) mustBe JsSuccess(journeyLabelsMinimal)
    }

    "write to json with minimal data" in {
      Json.toJson(journeyLabelsMinimal) mustBe emptyJson
    }
  }

  "LanguageLabels" should {
    import LanguageLabels.{languageLabelsReads, languageLabelsWrites}

    "read successfully from minimal json" in {
      Json.fromJson[LanguageLabels](emptyJson) mustBe JsSuccess(languageLabelsMinimal)
    }

    "write to json with minimal data" in {
      Json.toJson(languageLabelsMinimal) mustBe emptyJson
    }
  }

  "InternationalLanguageLabels" should {
    import InternationalLanguageLabels.{internationalLanguageLabelsReads, internationalLanguageLabelsWrites}

    "read successfully from minimal json" in {
      Json.fromJson[InternationalLanguageLabels](emptyJson) mustBe JsSuccess(internationalLanguageLabelsMinimal)
    }

    "write to json with minimal data" in {
      Json.toJson(internationalLanguageLabelsMinimal) mustBe emptyJson
    }
  }

  "AppLevelLabels" should {
    "read successfully from minimal json" in {
      Json.fromJson[AppLevelLabels](emptyJson) mustBe JsSuccess(appLevelLabelsMinimal)
    }

    "write to json with minimal data" in {
      Json.toJson(appLevelLabelsMinimal) mustBe emptyJson
    }
  }

  "SelectPageLabels" should {
    "read successfully from minimal json" in {
      Json.fromJson[SelectPageLabels](emptyJson) mustBe JsSuccess(selectPageLabelsMinimal)
    }

    "write to json with minimal data" in {
      Json.toJson(selectPageLabelsMinimal) mustBe emptyJson
    }
  }

  "LookupPageLabels" should {
    "read successfully from minimal json" in {
      Json.fromJson[LookupPageLabels](emptyJson) mustBe JsSuccess(lookupPageLabelsMinimal)
    }

    "write to json with minimal data" in {
      Json.toJson(lookupPageLabelsMinimal) mustBe emptyJson
    }
  }

  "EditPageLabels" should {
    "read successfully from minimal json" in {
      Json.fromJson[EditPageLabels](emptyJson) mustBe JsSuccess(editPageLabelsMinimal)
    }

    "write to json with minimal data" in {
      Json.toJson(editPageLabelsMinimal) mustBe emptyJson
    }
  }

  "ConfirmPageLabels" should {
    "read successfully from minimal json" in {
      Json.fromJson[ConfirmPageLabels](emptyJson) mustBe JsSuccess(confirmPageLabelsMinimal)
    }

    "write to json with minimal data" in {
      Json.toJson(confirmPageLabelsMinimal) mustBe emptyJson
    }
  }
}
