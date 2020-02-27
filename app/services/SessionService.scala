/*
 * Copyright 2020 HM Revenue & Customs
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

import config.SessionCacheWiring
import models.CallbackData
import play.api.Logger
import play.api.libs.json.{Json, Reads}
import play.api.mvc.Request
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object SessionService extends SessionService

trait SessionService extends SessionCacheWiring {

  val CALLBACK_DATA_KEY = "callback_data_key"

  def retrieveCallbackData()(implicit request: Request[_], hc: HeaderCarrier): Future[Option[CallbackData]] = {

    implicit val callbackDataReads: Reads[CallbackData] = Json.format[CallbackData]

    sessionCache.fetchAndGetEntry(CALLBACK_DATA_KEY)(hc, callbackDataReads, MdcLoggingExecutionContext.fromLoggingDetails(hc))

  }

  def storeCallbackData(data: CallbackData, totalRows: Int)(implicit request: Request[_], hc: HeaderCarrier): Future[Option[CallbackData]] = {
    val callbackData = data.copy(noOfRows = Some(totalRows))

    val cacheMap = sessionCache.cache[CallbackData](CALLBACK_DATA_KEY, callbackData)

    cacheMap.map(cacheMap =>
      Some(callbackData)
    ).recover { case e:Throwable =>
      Logger.error("Failed to store callback data with no of rows: " + e)
      None
    }
  }

  def storeString(dataPair:(String,String))(implicit request: Request[_], hc: HeaderCarrier): Future[Option[String]] = {
    val cacheMap = sessionCache.cache[String](dataPair._1, dataPair._2)

    cacheMap.map(cacheMap =>
      Some(dataPair._2)
    ).recover { case e =>
      Logger.error("Failed to store attachments post data: " + e)
      None
    }
  }
}
