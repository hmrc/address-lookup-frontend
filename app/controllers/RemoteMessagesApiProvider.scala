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

package controllers

import javax.inject.{Inject, Singleton}
import play.api.http.HttpConfiguration
import play.api.i18n.{DefaultMessagesApi, DefaultMessagesApiProvider, Langs}
import play.api.libs.json.JsObject
import play.api.{Configuration, Environment}

@Singleton
class RemoteMessagesApiProvider @Inject()(environment: Environment, config: Configuration, langs: Langs,
                                          httpConfiguration: HttpConfiguration)
  extends DefaultMessagesApiProvider(environment, config, langs, httpConfiguration) {

  lazy val defaultMessages: Map[String, Map[String, String]] = loadAllMessages

  def getRemoteMessagesApi(remoteMessages: Option[JsObject]) = {
    val english = remoteMessages.flatMap(js => (js \ "en").asOpt[Map[String, String]]).getOrElse(Map())
    val welsh = remoteMessages.flatMap(js => (js \ "cy").asOpt[Map[String, String]]).getOrElse(Map())

    val allMessages = defaultMessages.map {
      case (s, m) if s == "en" => s -> (m ++ english)
      case (s, m) if s == "cy" => s -> (m ++ welsh)
      case (s, m) => s -> m
    }

    new DefaultMessagesApi(
      allMessages,
      langs,
      langCookieName = langCookieName,
      langCookieSecure = langCookieSecure,
      langCookieHttpOnly = langCookieHttpOnly,
      httpConfiguration = httpConfiguration
    )
  }
}
