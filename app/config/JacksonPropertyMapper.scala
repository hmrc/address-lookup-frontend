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

package config

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule

// Instead of converting to/from JSON, this converts to/from nested key/value properties.
object JacksonPropertyMapper extends JavaPropsMapper {
  configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
  registerModule(DefaultScalaModule)
  setSerializationInclusion(JsonInclude.Include.NON_ABSENT)

  def productToKeyVals(p: Product): List[(String, String)] = {
    // This is a naive two-stage algorithm that works simply. Maybe it could be improved later.
    val props = writeValueAsString(p)
    val lines = props.split('\n')
    val keyVals =
      for (line <- lines
           if line.length > 1) yield {
        divide(line, '=')
      }
    keyVals.toList
  }

  /** Divides at the first instance of c */
  private def divide(s: String, c: Char): (String, String) = {
    val i = s.indexOf(c)
    if (i < 0) s -> ""
    else {
      val w1 = s.substring(0, i)
      val rest = s.substring(i + 1)
      w1 -> rest
    }
  }
}
