/*
 * Copyright 2017 HM Revenue & Customs
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

package itutil

import java.net.{URLDecoder, URLEncoder}
import java.nio.charset.StandardCharsets
import java.util.UUID
import itutil.config.IntegrationTestConstants._
import play.api.libs.Crypto
import play.api.libs.ws.WSCookie
import uk.gov.hmrc.crypto.{CompositeSymmetricCrypto, Crypted, PlainText}
import uk.gov.hmrc.http.SessionKeys

trait LoginStub extends SessionCookieBaker {
  private val defaultUser = "/foo/bar"

  val SessionId         = s"stubbed-${UUID.randomUUID}"
  val invalidSessionId  = s"FAKE_PRF::NON-COMPSDOJ OMSDDf"

  private def cookieData(additionalData: Map[String, String], userId: String = defaultUser, sessionId: String = SessionId): Map[String, String] = {
    Map(
      SessionKeys.sessionId -> sessionId,
      SessionKeys.userId -> userId,
      SessionKeys.token -> "token",
      SessionKeys.authProvider -> "GGW",
      SessionKeys.lastRequestTimestamp -> new java.util.Date().getTime.toString
    ) ++ additionalData
  }

  def getSessionCookie(additionalData: Map[String, String] = Map(), userId: String = defaultUser, sessionId: String = SessionId): String =
    cookieValue(cookieData(additionalData, userId, sessionId))

  def sessionCookieWithCSRF: String = getSessionCookie(Map("csrfToken" -> testCsrfToken()))

  def sessionCookieWithCSRFAndLang(lang: Option[String] = Some("cy")): String = {
    getSessionCookie(Map("csrfToken" -> testCsrfToken())) + lang.fold("")(l => s";PLAY_LANG=$l;")
  }

  def sessionCookieWithWelshCookie(useWelsh: Boolean): String =
    sessionCookieWithCSRF + s";$useWelshCookieName=${useWelsh.toString}"
}

trait SessionCookieBaker {
  val cookieKey = "gvBoGdgzqG1AarzF1LY0zQ=="
  def cookieValue(sessionData: Map[String,String]): String = {
    def encode(data: Map[String, String]): PlainText = {
      val encoded = data.map {
        case (k, v) => URLEncoder.encode(k, "UTF-8") + "=" + URLEncoder.encode(v, "UTF-8")
      }.mkString("&")
      val key = "yNhI04vHs9<_HWbC`]20u`37=NGLGYY5:0Tg5?y`W<NoJnXWqmjcgZBec@rOxb^G".getBytes
      PlainText(Crypto.sign(encoded, key) + "-" + encoded)
    }

    val encodedCookie = encode(sessionData)
    val encrypted = CompositeSymmetricCrypto.aesGCM(cookieKey, Seq()).encrypt(encodedCookie).value

    s"""mdtp="$encrypted"; Path=/; HTTPOnly"; Path=/; HTTPOnly"""
  }

  def getCookieData(cookie: WSCookie): Map[String, String] = {
    getCookieData(cookie.value.get)
  }

  def getCookieData(cookieData: String): Map[String, String] = {
    val decrypted = CompositeSymmetricCrypto.aesGCM(cookieKey, Seq()).decrypt(Crypted(cookieData)).value

    decrypted.split("&")
      .map(_.split("="))
      .map { case Array(k, v) => (k, URLDecoder.decode(v, StandardCharsets.UTF_8.name()))}
      .toMap
  }
}