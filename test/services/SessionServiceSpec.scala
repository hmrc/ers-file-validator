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

package services

import repository.ERSFileValidatorSessionRepository
import models.upscan.UpscanCallback
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.libs.json._
import play.api.mvc.{AnyContent, AnyContentAsEmpty}
import play.api.test.FakeRequest
import uk.gov.hmrc.http.HeaderCarrier
import models.cache.CacheMap
import uk.gov.hmrc.mongo.cache.DataKey

import scala.language.postfixOps
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutor, Future}

class SessionServiceSpec extends PlaySpec with ScalaFutures with MockitoSugar {

  val mockSessionCache: ERSFileValidatorSessionRepository = mock[ERSFileValidatorSessionRepository]
  implicit val ec: ExecutionContextExecutor = ExecutionContext.global
  val sessionService = new SessionCacheService(mockSessionCache, ec)
  implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
  val hc: HeaderCarrier = HeaderCarrier()
  val CALLBACK_DATA_KEY = "callback_data_key"

  "storeCallbackData" must {
    "successfully store attachments callback post data" in {
      val postData: UpscanCallback = UpscanCallback("thefilename", "downloadUrl", Some(1000L))

      val json = Json.toJson[UpscanCallback](postData)
      when(mockSessionCache.cache[UpscanCallback]
        (any[String], any[UpscanCallback])
        (any[Writes[UpscanCallback]], any[HeaderCarrier], any()))
        .thenReturn(Future.successful(CacheMap("sessionValue", Map(sessionService.CALLBACK_DATA_KEY -> json))))

      val result: Option[UpscanCallback] = Await.result(sessionService.storeCallbackData(postData, 1000)(FakeRequest[AnyContent]()), 10 seconds)

      result.get.length must be(Some(1000L))
      result.get.noOfRows must be (Some(1000))
    }

    "return a None when cache can't be returned" in {
      val postData: UpscanCallback = UpscanCallback("thefilename", "downloadUrl", Some(1000L))

      when(mockSessionCache.cache[UpscanCallback]
        (any[String], any[UpscanCallback])
        (any[Writes[UpscanCallback]], any[HeaderCarrier], any()))
        .thenReturn(Future.failed(new Exception("")))

      val result: Option[UpscanCallback] = Await.result(sessionService.storeCallbackData(postData, 1000)(FakeRequest[AnyContent]), 10 seconds)
      result mustBe None
    }
  }
}
