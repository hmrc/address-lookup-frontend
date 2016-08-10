package config

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule

object JacksonMapper extends ObjectMapper {
  configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
  registerModule(DefaultScalaModule)
  setSerializationInclusion(JsonInclude.Include.NON_ABSENT)
}


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
