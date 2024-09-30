package services

import org.htmlunit.ScriptException
import org.htmlunit.html.HtmlPage
import org.htmlunit.javascript.DefaultJavaScriptErrorListener
import play.api.Logger

import java.net.{MalformedURLException, URL}

class WarnLoggingJavascriptErrorListener extends DefaultJavaScriptErrorListener {

  val logger = Logger("org.htmlunit.javascript.DefaultJavaScriptErrorListener")

  override def scriptException(page: HtmlPage, scriptException: ScriptException): Unit =
    logger.warn("Error during JavaScript execution", scriptException)

  override def timeoutError(page: HtmlPage, allowedTime: Long, executionTime: Long): Unit =
    logger.warn("Timeout during JavaScript execution after " + executionTime + "ms; allowed only " + allowedTime + "ms")

  override def malformedScriptURL(page: HtmlPage, url: String, malformedURLException: MalformedURLException): Unit =
    logger.warn("Unable to build URL for script src tag [" + url + "]", malformedURLException)

  override def loadScriptError(page: HtmlPage, scriptUrl: URL, exception: Exception): Unit =
    logger.warn("Error loading JavaScript from [" + scriptUrl + "].", exception)
}
