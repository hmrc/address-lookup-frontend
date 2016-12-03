package config

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper, SerializationFeature}
import com.fasterxml.jackson.module.scala.DefaultScalaModule

// this is temporary (replaced with a-r-store 2.7.0)
object PrettyMapper extends ObjectMapper {
  configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
  registerModule(DefaultScalaModule)
  setSerializationInclusion(JsonInclude.Include.NON_ABSENT)
  configure(SerializationFeature.INDENT_OUTPUT, true)
}
