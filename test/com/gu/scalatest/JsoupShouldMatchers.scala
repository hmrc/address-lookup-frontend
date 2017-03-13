package com.gu.scalatest

import com.gu.jsoup.{ElementSelector, ElementSelectorBuilders, Select}
import org.jsoup.nodes.Element
import org.scalatest.MustMatchers
import org.scalatest.exceptions.TestFailedException
import org.scalatest.words.{IncludeWord, NotWord}

trait JsoupShouldMatchers extends MustMatchers with ElementSelectorBuilders with ElementWords {

  import org.jsoup.Jsoup.parseBodyFragment

  private[scalatest] def any: ElementSelector = Select(_.getAllElements, None)

  protected val self = any

  implicit def string2AsBodyFragment(s: String) = new {
    def asBodyFragment: Element = parseBodyFragment(s).body
  }

  implicit def jsoupElem2ShouldWrapper(elem: Element): JsoupElementShouldWrapper =
    new JsoupElementShouldWrapper(elem)

  private[scalatest] final class JsoupElementShouldWrapper(left: Element) {

    def should(includeWord: IncludeWord): ResultOfIncludeWord =
      new ResultOfIncludeWord(left)

    def should(notWord: NotWord): ResultOfNotWord =
      new ResultOfNotWord(left)
  }

  private[scalatest] final class ResultOfIncludeWord(left: Element) {

    def element(selector: ElementSelector) {
      if (selector(left).isEmpty)
        throw testFailedException(failureMessage('didNotIncludeElementMatchingSelector, left, selector))
    }

    def img(selector: ElementSelector = any) {
      element(selector.withName("img"))
    }

    def video(selector: ElementSelector = any) {
      element(selector.withName("video"))
    }

    def td(selector: ElementSelector = any) {
      element(selector.withName("td"))
    }

    def th(selector: ElementSelector = any) {
      element(selector.withName("th"))
    }
  }

  private[scalatest] final class ResultOfNotWord(left: Element) {

    def include(selector: ElementSelector) {
      if (! selector(left).isEmpty)
        throw testFailedException(failureMessage('includedElementMatchingSelector, left, selector))
    }
  }

  private[scalatest] def testFailedException(message: String): Throwable = {
    val fileNames = List("RicherHtmlTest.scala")
    val temp = new RuntimeException
    val stackDepth = temp.getStackTrace.takeWhile(sTraceElem =>
      fileNames.exists(_ == sTraceElem.getFileName) || sTraceElem.getMethodName == "testFailedException").length
    // TODO as far as my current understanding goes, stackDepth + 1 is just voodoo which appears to work
    new TestFailedException(message, stackDepth + 1)
  }

  private[scalatest] def failureMessage(key: Symbol, left: Element, expected: ElementSelector) =
    "%s %s %s".format(left, key match {
      case 'includedElementMatchingSelector => "included an element"
      case 'didNotIncludeElementMatchingSelector => "did not include an element"
    }, expected)

}


/*
 * Words for use after 'not' words
 */
trait ElementWords { this: JsoupShouldMatchers =>

  sealed trait ElementWord {
    def apply(selector: ElementSelector = any): ElementSelector
  }

  val element = new ElementWord {
    def apply(selector: ElementSelector) = selector
  }

  val img = new ElementWord {
    def apply(selector: ElementSelector) = selector.withName("img")
  }

  val video = new ElementWord {
    def apply(selector: ElementSelector) = selector.withName("video")
  }

}
