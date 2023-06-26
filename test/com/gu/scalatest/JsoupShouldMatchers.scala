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

package com.gu.scalatest

import com.gu.jsoup.{ElementSelector, ElementSelectorBuilders, Select}
import org.jsoup.nodes.Element
import org.scalatest.exceptions.TestFailedException
import org.scalatest.matchers.dsl.{IncludeWord, NotWord}
import org.scalatest.matchers.must.Matchers

trait JsoupShouldMatchers extends Matchers with ElementSelectorBuilders with ElementWords {

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
        throw testFailedException(failureMessage(Symbol("didNotIncludeElementMatchingSelector"), left, selector))
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
        throw testFailedException(failureMessage(Symbol("includedElementMatchingSelector"), left, selector))
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
