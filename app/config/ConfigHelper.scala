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

import play.api.Mode._
import play.api.Play._
import play.api.{Application, Configuration, Play}
import uk.gov.hmrc.play.config.ServicesConfig

import scala.collection.JavaConversions._

object ConfigHelper extends ServicesConfig {

  def mustGetConfigString(config: Configuration, key: String): String = {
    getConfigString(config, key).getOrElse {
      throw new Exception("ERROR: Unable to find config item " + key)
    }
  }

  def mustGetConfigString(mode: Mode, config: Configuration, key: String): String = {
    getConfigString(mode, config, key).getOrElse {
      throw new Exception(s"ERROR: Unable to find config item $mode.$key or $key")
    }
  }

  def mustGetConfigString(app: Application, key: String): String = {
    mustGetConfigString(app.mode, app.configuration, key)
  }

  def getConfigString(config: Configuration, key: String): Option[String] = config.getString(key)

  def getConfigString(mode: Mode, config: Configuration, key: String): Option[String] = {
    val modeKey = s"$mode.$key"
    config.getString(modeKey).orElse(config.getString(key))
  }

  def getConfigString(app: Application, key: String): Option[String] = {
    getConfigString(app.mode, app.configuration, key)
  }

  def mustGetConfigMap(config: Configuration, key: String): Map[String, String] = {
    val modeKey = s"$mode.$key"
    config.getObject(modeKey).orElse(config.getObject(key)).getOrElse(
      throw new Exception(s"ERROR: Unable to find config item $mode.$key or $key")
    ).unwrapped().toMap[String, AnyRef].map(entry => entry._1 -> entry._2.toString)
  }

}
