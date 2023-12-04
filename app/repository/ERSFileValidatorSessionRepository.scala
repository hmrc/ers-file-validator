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

package repository

import config.ApplicationConfig
import models.cache.CacheMap
import play.api.libs.json.Writes
import play.api.mvc.Request
import uk.gov.hmrc.http.SessionKeys
import uk.gov.hmrc.mongo.cache.{DataKey, SessionCacheRepository}
import uk.gov.hmrc.mongo.{MongoComponent, TimestampSupport}

import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ERSFileValidatorSessionRepository @Inject()(mongoComponent: MongoComponent,
                                                  appConfig: ApplicationConfig,
                                                  timestampSupport: TimestampSupport
                                         )(implicit ec: ExecutionContext) extends SessionCacheRepository(
  mongoComponent = mongoComponent,
  collectionName = appConfig.appName,
  ttl = Duration(appConfig.mongoTTLInSeconds, TimeUnit.SECONDS),
  timestampSupport = timestampSupport,
  sessionIdKey = SessionKeys.sessionId
)(ec) {

  def putInSession[T: Writes](dataKey: DataKey[T], data: T)
                             (implicit request: Request[_], ec: ExecutionContext): Future[CacheMap] = {
    cacheRepo
      .put[T](request)(dataKey, data)
      .map(res => CacheMap(res.id, res.data.value.toMap))
  }
}
