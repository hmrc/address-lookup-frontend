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

import com.google.inject.ImplementedBy
import config.FrontendAppConfig
import model.v2.JourneyDataV2
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.mongo.cache.CacheIdType.SimpleCacheId
import uk.gov.hmrc.mongo.cache.{DataKey, MongoCacheRepository}
import uk.gov.hmrc.mongo.{CurrentTimestampSupport, MongoComponent}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[JourneyDataV2Cache])
trait JourneyRepository {
  def getV2(sessionId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[JourneyDataV2]]

  def putV2(sessionId: String, data: JourneyDataV2)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Boolean]
}

@Singleton
class JourneyDataV2Repository @Inject()(mongoComponent: MongoComponent, config: FrontendAppConfig)(implicit ec: ExecutionContext) extends MongoCacheRepository(
  mongoComponent = mongoComponent,
  collectionName = config.appName,
  replaceIndexes = true,
  ttl = config.cacheTtl,
  timestampSupport = new CurrentTimestampSupport(),
  cacheIdType = SimpleCacheId
)(ec) {
  val dataKey: DataKey[JourneyDataV2] = DataKey("journey-data")
}

@Singleton
class JourneyDataV2Cache @Inject()(repo: JourneyDataV2Repository)(implicit ec: ExecutionContext) extends JourneyRepository {
  override def getV2(sessionId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[JourneyDataV2]] =
    repo.get[JourneyDataV2](sessionId)(repo.dataKey)

  override def putV2(sessionId: String, data: JourneyDataV2)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Boolean] =
    repo.put[JourneyDataV2](sessionId)(repo.dataKey, data).map(_ => true) //TODO: Fix this!!!

}
