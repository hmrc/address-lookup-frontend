/*
 * Copyright 2016 HM Revenue & Customs
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

package address

case class ViewConfig(pageTitle: String,
                      baseTitle: String,
                      prompt: String,
                      homeUrl: String,
                      allowManualEntry: Boolean = false,
                      allowNoFixedAddress: Boolean = true,
                      maxAddressesToShow: Int = 20,
                      indicator: Option[String] = None,
                      alpha: Boolean = false,
                      beta: Boolean = false) {
  def title = if (indicator.isDefined) baseTitle + " - " + indicator.get else baseTitle
}


object ViewConfig {

  val cfg = Map(
    "j0" -> ViewConfig(
      pageTitle = "Address lookup",
      baseTitle = "Your address",
      prompt = "Choose your location",
      homeUrl = "http://www.gov.uk/",
      allowManualEntry = true, allowNoFixedAddress = true, maxAddressesToShow = 20, alpha = true),

    "j1" -> ViewConfig(
      pageTitle = "Address lookup",
      baseTitle = "Address entry",
      prompt = "Enter the address",
      homeUrl = "http://www.gov.uk/",
      allowManualEntry = false, allowNoFixedAddress = false, maxAddressesToShow = 10, beta = true),

    "j2" -> ViewConfig(
      pageTitle = "Address lookup",
      baseTitle = "Address entry",
      prompt = "Enter the address",
      homeUrl = "http://www.gov.uk/",
      allowManualEntry = false, allowNoFixedAddress = false, maxAddressesToShow = 10),

    "bafe1" -> ViewConfig(
      pageTitle = "Bank Account Reputation",
      baseTitle = "Address Details",
      prompt = "Choose your location",
      homeUrl = "http://www.gov.uk/",
      allowManualEntry = true, allowNoFixedAddress = false, maxAddressesToShow = 20, alpha = true)

  )

  val defaultContinueUrl = "confirmation"
}
