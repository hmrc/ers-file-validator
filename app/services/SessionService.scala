/*
 * Copyright 2022 HM Revenue & Customs
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

import config.ERSFileValidatorSessionCache
import models.upscan.UpscanCallback
import play.api.Logging
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SessionService @Inject()(sessionCache: ERSFileValidatorSessionCache,
                               implicit val ec: ExecutionContext) extends Logging {

  val CALLBACK_DATA_KEY = "callback_data_key"

  def storeCallbackData(data: UpscanCallback, totalRows: Int)(implicit hc: HeaderCarrier): Future[Option[UpscanCallback]] = {
    val callbackData = data.copy(noOfRows = Some(totalRows))

    sessionCache.cache[UpscanCallback](CALLBACK_DATA_KEY, callbackData).map { _ =>
      Some(callbackData)
    }.recover {
      case e: Throwable =>
      logger.error("Failed to store callback data with no of rows: " + e)
      None
    }
  }
}
