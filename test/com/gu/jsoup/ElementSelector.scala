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

package com.gu.jsoup

import scala.jdk.CollectionConverters._
import org.jsoup.nodes.Element
import org.jsoup.select.Elements


sealed trait ElementSelector extends ElementSelectorBuilders {

  protected def self = this

  final def apply(elem: Element): Elements =
    fold[Element => Elements](
      (f, desc) => f,
      (l, r) => elem => new Elements(l(elem).asScala.flatMap(a => r.apply(a).asScala).asJava),
      (l, r) => elem => new Elements((l(elem).asScala ++ r(elem).asScala.distinct).asJava)
    )(elem)

  override final def toString: String =
    fold[List[String]](
      (f, desc) => desc.toList,
      (l, r) => l ::: r,
      (l, r) => l ::: List("or (" + r.mkString(" ") + ")")
    ).mkString(" ")

  final def fold[A](
                     select: (Element => Elements, Option[String]) => A,
                     and: (A, A) => A,
                     or: (A, A) => A
                   ): A = this match {
    case Select(f, desc) => select(f, desc)
    case And(left, right) => and(left.fold(select, and, or), right.fold(select, and, or))
    case Or(left, right) => or(left.fold(select, and, or), right.fold(select, and, or))
  }

  def and(right: ElementSelector): ElementSelector = And(self, right)

  def or(right: ElementSelector): ElementSelector = Or(self, right)

  def || (right: ElementSelector): ElementSelector = or(right)

  def && (right: ElementSelector): ElementSelector = and(right)

}

case class And(left: ElementSelector, right: ElementSelector) extends ElementSelector

case class Or(left: ElementSelector, Right: ElementSelector) extends ElementSelector

case class Select(f: Element => Elements, desc: Option[String]) extends ElementSelector


trait ElementSelectorBuilders {

  protected def self: ElementSelector

  private def selfAnd(f: Element => Elements, desc: String) = self.and(Select(f, Some(desc)))

  def withValue(value: String): ElementSelector =
    selfAnd(_.getElementsContainingText(value), s"""with value $value""")

  def withName(name: String): ElementSelector =
    selfAnd(_.getElementsByTag(name), """with name "%s"""".format(name))

  def withClass(cls: String): ElementSelector =
    selfAnd(_.getElementsByClass(cls), """with class "%s"""".format(cls))

  def withAttr(key: String): ElementSelector =
    selfAnd(_.getElementsByAttribute(key), "with attribute " + key)

  def withAttrValue(key: String, value: String): ElementSelector =
    selfAnd(_.getElementsByAttributeValue(key, value), """with attribute %s="%s"""".format(key, value))

  def withAttrValueMatching(key: String, pattern: String): ElementSelector =
    selfAnd(_.getElementsByAttributeValueMatching(key, pattern),
      "with attribute %s matching pattern %s".format(key, pattern))

  def withSrc(value: String): ElementSelector =
    self.and(withAttrValue("src", value))

  def withSrcMatching(pattern: String): ElementSelector =
    self.and(withAttrValueMatching("src", pattern))

}
