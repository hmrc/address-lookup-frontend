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
                      allowManualEntry: Boolean,
                      allowNoFixedAddress: Boolean,
                      allowInternationalAddress: Boolean,
                      allowBfpo: Boolean,
                      maxAddressesToShow: Int,
                      indicator: Option[String] = None, // augments the title in the title bar
                      alpha: Boolean = false,
                      beta: Boolean = false) {

  def title: String = if (indicator.isDefined) baseTitle + " - " + indicator.get else baseTitle
}


object ViewConfig {

  val live1 = ViewConfig(
    pageTitle = "Address lookup",
    baseTitle = "Your address",
    prompt = "Choose your location",
    homeUrl = "http://www.gov.uk/",
    allowManualEntry = false,
    allowNoFixedAddress = false,
    allowInternationalAddress = false,
    allowBfpo = false,
    maxAddressesToShow = 20)

  val alpha1 = live1.copy(alpha = true)

  val beta1 = live1.copy(beta = true)

  //---------------------------------------------------------------------------

  val cfg = Map(
    "j0" -> alpha1.copy(allowManualEntry = true, allowNoFixedAddress = true),

    "j1" -> beta1.copy(allowInternationalAddress = true, allowBfpo = true),

    "j2" -> live1.copy(baseTitle = "Address entry", prompt = "Enter the address", maxAddressesToShow = 100),

    "bafe1" -> ViewConfig(
      pageTitle = "Bank Account Reputation",
      baseTitle = "Address Details",
      prompt = "Choose your location",
      homeUrl = "http://www.gov.uk/",
      allowManualEntry = true,
      allowNoFixedAddress = false,
      allowInternationalAddress = true,
      allowBfpo = true,
      maxAddressesToShow = 20,
      alpha = true)
  )

  val defaultContinueUrl = "confirmation"
}
