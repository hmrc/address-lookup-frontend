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

package services

import org.htmlunit.ScriptException
import org.htmlunit.html.HtmlPage
import org.htmlunit.javascript.DefaultJavaScriptErrorListener
import play.api.Logger

import java.net.{MalformedURLException, URL}

class WarnLoggingJavascriptErrorListener extends DefaultJavaScriptErrorListener {

  val logger = Logger("org.htmlunit.javascript.DefaultJavaScriptErrorListener")

  override def scriptException(page: HtmlPage, scriptException: ScriptException): Unit =
    logger.warn("Error during JavaScript execution")

  override def timeoutError(page: HtmlPage, allowedTime: Long, executionTime: Long): Unit =
    logger.warn("Timeout during JavaScript execution after " + executionTime + "ms; allowed only " + allowedTime + "ms")

  override def malformedScriptURL(page: HtmlPage, url: String, malformedURLException: MalformedURLException): Unit =
    logger.warn("Unable to build URL for script src tag [" + url + "]")

  override def loadScriptError(page: HtmlPage, scriptUrl: URL, exception: Exception): Unit =
    logger.warn("Error loading JavaScript from [" + scriptUrl + "].")
}
