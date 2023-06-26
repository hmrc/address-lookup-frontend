/*
 * Copyright 2023 HM Revenue & Customs
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

package utils

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.{Environment, Mode}

class RelativeOrAbsoluteWithHostnameFromWhitelistTest extends AnyWordSpec with Matchers {
  "RelativeOrAbsoluteWithHostnameFromWhitelistTest" should {
    "return checked url" when {

      "absolute url is provided" in {
        val relativeOrAbsoluteWithHostnameFromWhitelist =
          new RelativeOrAbsoluteWithHostnameFromAllowlist(Set("some-host"), Environment.simple(mode = Mode.Test))

        val testUrl = "https://some-host/some/path/here"
        relativeOrAbsoluteWithHostnameFromWhitelist.url(testUrl) shouldBe testUrl
      }

      "relative url is provided" in {
        val relativeOrAbsoluteWithHostnameFromWhitelist =
          new RelativeOrAbsoluteWithHostnameFromAllowlist(Set("some-host"), Environment.simple(mode = Mode.Test))

        val testUrl = "/path/here"
        relativeOrAbsoluteWithHostnameFromWhitelist.url(testUrl) shouldBe testUrl
      }
    }

    "fail to return a checked url" when {
      "absolute url with host that is not whitelisted is provided" in {
        val relativeOrAbsoluteWithHostnameFromWhitelist =
          new RelativeOrAbsoluteWithHostnameFromAllowlist(Set("some-host"), Environment.simple(mode = Mode.Test))

        val testUrl = "https://other-host/some/path/here"
        val e = intercept[IllegalArgumentException] {
          relativeOrAbsoluteWithHostnameFromWhitelist.url(testUrl)
        }
        e.getMessage shouldBe "Provided URL [https://other-host/some/path/here] doesn't comply with redirect policy"
      }
    }
  }

}
