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

import com.google.inject.{AbstractModule, Provides}
import controllers.RemoteMessagesApiProvider
import org.apache.pekko.stream.Materializer
import play.api.libs.concurrent.PekkoGuiceSupport
import play.api.{Configuration, Environment, Logger}
import services.{EnglishCountryNamesDataSource, GovWalesCacheUpdateScheduler, WelshCountryNamesDataSource, WelshCountryNamesObjectStoreDataSource}
import uk.gov.hmrc.objectstore.client.play.PlayObjectStoreClient

import javax.inject.Singleton
import scala.concurrent.ExecutionContext

class Module(env: Environment, playConfig: Configuration) extends AbstractModule with PekkoGuiceSupport {

  override def configure(): Unit = {
    bind(classOf[RemoteMessagesApiProvider])
    bind(classOf[GovWalesCacheUpdateScheduler]).asEagerSingleton()
  }

  @Provides
  @Singleton
  private def provideWelshCountryNamesDataSource(english: EnglishCountryNamesDataSource,
                                                 objectStore: PlayObjectStoreClient,
                                                 ec: ExecutionContext, mat: Materializer) = {
    val logger = Logger(this.getClass)

    val useLocal =
      playConfig.getOptional[Boolean]("microservice.services.gov-wales.useLocal").getOrElse(true)

    if (useLocal) {
      logger.info(s"Using local gov-wales country data")
      new WelshCountryNamesDataSource(english)
    } else {
      logger.info(s"Using gov-wales country data from object-store")
      new WelshCountryNamesObjectStoreDataSource(english, objectStore, ec, mat)
    }
  }
}
