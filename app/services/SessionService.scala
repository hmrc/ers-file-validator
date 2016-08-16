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

package services

import config.SessionCacheWiring
import models.CallbackData
import play.api.Logger
import play.api.libs.json.{Json, Reads}
import play.api.mvc.Request

import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object SessionService extends SessionService

trait SessionService extends SessionCacheWiring {

  val CALCULATION_RESULTS_KEY: String = "calculation_results_key"
  val CALLBACK_DATA_KEY = "callback_data_key"

  val SCENARIO_KEY = "scenario"

//  val REVALUATION_DATE_KEY = "revaluation_date"
//  val REVALUATION_RATE_KEY = "revaluation_rate"

  def retrieveCallbackData()(implicit request: Request[_], hc: HeaderCarrier): Future[Option[CallbackData]] = {

    implicit val callbackDataReads: Reads[CallbackData] = Json.format[CallbackData]

    sessionCache.fetchAndGetEntry(CALLBACK_DATA_KEY)(hc, callbackDataReads)

  }

  def storeCallbackData(postData: CallbackData)(implicit request: Request[_], hc: HeaderCarrier): Future[Option[CallbackData]] = {

    val cacheMap = sessionCache.cache[CallbackData](CALLBACK_DATA_KEY, postData)

    cacheMap.map(cacheMap =>
      Some(postData)
    ).recover { case e =>
      Logger.warn("Failed to store attachments post data: " + e)
      None
    }
  }

  def storeString(dataPair:(String,String))(implicit request: Request[_], hc: HeaderCarrier): Future[Option[String]] = {
    val cacheMap = sessionCache.cache[String](dataPair._1, dataPair._2)

    cacheMap.map(cacheMap =>
      Some(dataPair._2)
    ).recover { case e =>
      Logger.warn("Failed to store attachments post data: " + e)
      None
    }
  }
}
