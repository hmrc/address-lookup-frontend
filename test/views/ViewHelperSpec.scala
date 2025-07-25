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

package views

import forms.ALFForms
import play.api.i18n
import play.api.i18n.{Lang, MessagesApi}
import uk.gov.hmrc.govukfrontend.views.Aliases.SelectItem
import uk.gov.hmrc.hmrcfrontend.views.viewmodels.language.{Cy, En, Language}

class ViewHelperSpec extends ViewSpec {

  sealed trait Messages {
    val selectCountry = "Select country or territory"
  }
  object EnglishMessages extends Messages
  object WelshMessages extends Messages {
    override val selectCountry = "Dewiswch gwlad neu diriogaeth"
  }

  val messagesMap: Map[Language, Messages] = Map(
    En -> EnglishMessages,
    Cy -> WelshMessages
  )

  val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

  ".countriesToSelectItems()" when {

    messagesMap.foreach { case (language, messagesForLanguage) =>

      s"being called with language code of '${language.code}'" should {

        implicit val messages: i18n.Messages = messagesApi.preferred(Seq(Lang(language.code)))

        s"return the correct message for the drop down of '${messagesForLanguage.selectCountry}'" in {
          val actual = ViewHelper.countriesToSelectItems(Seq(), ALFForms.countryPickerForm())
          val expected = Seq(SelectItem(Some(""), messagesForLanguage.selectCountry))

          actual shouldBe expected
        }
      }
    }
  }
}
