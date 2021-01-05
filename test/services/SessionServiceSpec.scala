/*
 * Copyright 2021 HM Revenue & Customs
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
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json._
import play.api.test.FakeRequest
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutor, Future}

class SessionServiceSpec extends PlaySpec with GuiceOneAppPerSuite with ScalaFutures with MockitoSugar {

  val mockSessionCache: ERSFileValidatorSessionCache = mock[ERSFileValidatorSessionCache]
  implicit val ec: ExecutionContextExecutor = ExecutionContext.global
  val sessionService = new SessionService(mockSessionCache, ec)
  implicit val request = FakeRequest()
  val hc = HeaderCarrier()

  "storeCallbackData" must {
    "successfully store attachments callback post data" in {
      val postData: UpscanCallback = UpscanCallback("thefilename", "downloadUrl", Some(1000L))

      val json = Json.toJson[UpscanCallback](postData)
      when(mockSessionCache.cache[UpscanCallback]
        (any[String], any[UpscanCallback])
        (any[Writes[UpscanCallback]], any[HeaderCarrier], any()))
        .thenReturn(Future.successful(CacheMap("sessionValue", Map(sessionService.CALLBACK_DATA_KEY -> json))))

      val result: Option[UpscanCallback] = Await.result(sessionService.storeCallbackData(postData, 1000)(request, hc), 10 seconds)

      result.get.length must be(Some(1000L))
      result.get.noOfRows must be (Some(1000))
    }

    "return a None when cache can't be returned" in {
      val postData: UpscanCallback = UpscanCallback("thefilename", "downloadUrl", Some(1000L))

      val json = Json.toJson[UpscanCallback](postData)
      when(mockSessionCache.cache[UpscanCallback]
        (any[String], any[UpscanCallback])
        (any[Writes[UpscanCallback]], any[HeaderCarrier], any()))
        .thenReturn(Future.failed(new Exception("")))

      val result: Option[UpscanCallback] = Await.result(sessionService.storeCallbackData(postData, 1000)(request, hc), 10 seconds)
      result mustBe None
    }
  }
}
