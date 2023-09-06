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

package config

import model.JourneyDataV2
import play.api.Configuration
import uk.gov.hmrc.mongo.cache.{DataKey, SessionCacheRepository}
import uk.gov.hmrc.mongo.{CurrentTimestampSupport, MongoComponent, TimestampSupport}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.Duration

@Singleton
class JourneyDataV2Repository @Inject()(
                                mongoComponent: MongoComponent,
                                timestampSupport: TimestampSupport,
                                config: Configuration
                              )(implicit ec: ExecutionContext
                              ) extends SessionCacheRepository (
  mongoComponent = mongoComponent,
  collectionName = "address-lookup-frontend",
  replaceIndexes = true,
  ttl = Duration("30 mins"),
  timestampSupport = new CurrentTimestampSupport(),
  sessionIdKey = "journey-data"
)
object JourneyDataV2Repository {
  val journeyDataV2DataKey: DataKey[JourneyDataV2] = DataKey("journey-data")
}
