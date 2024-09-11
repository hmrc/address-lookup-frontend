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

package services

import org.apache.pekko.actor.ActorSystem
import play.api.{Configuration, Logger}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.Try

@Singleton
class GovWalesCacheUpdateScheduler @Inject()(config: Configuration, dataSource: WelshCountryNamesDataSource)(implicit ec: ExecutionContext) {
  val logger = Logger(this.getClass)

  val delay: FiniteDuration = config.getOptional[Int]("microservice.services.gov-wales.cache-schedule.initial-delay").map(_.seconds)
    .getOrElse(throw new IllegalArgumentException("microservice.services.gov-wales.cache-schedule.initial-delay not set"))
  val interval: FiniteDuration = config.getOptional[Int]("microservice.services.gov-wales.cache-schedule.interval").map(_.seconds)
    .getOrElse(throw new IllegalArgumentException("microservice.services.gov-wales.cache-schedule.interval"))

  GovWalesCacheUpdateScheduler.system.getScheduler.scheduleAtFixedRate(delay, interval){
    () => Try {
      logger.info("Gov Wales data update started")

      for {
        _ <- dataSource.updateCache()
        _ <- dataSource.retrieveAndStoreData()
      } yield {
        logger.info("Gov Wales data update finished")
      }
    }
  }(ec)
}

object GovWalesCacheUpdateScheduler {
  val system: ActorSystem = ActorSystem("gov-wales-check-actor-system")
}
