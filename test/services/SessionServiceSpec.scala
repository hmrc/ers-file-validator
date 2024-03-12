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

import models.upscan.{NotStarted, UploadStatus, UploadedSuccessfully, UpscanCallback}
import org.mockito.ArgumentMatchers.{any, anyString, eq => meq}
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import repository.ERSFileValidatorSessionRepository
import uk.gov.hmrc.http.{HeaderCarrier, SessionKeys}
import uk.gov.hmrc.mongo.cache.DataKey

import java.util.UUID
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutor, Future}
import scala.language.postfixOps

class SessionServiceSpec extends PlaySpec with ScalaFutures with MockitoSugar {

  val sessionPair: (String, String) = SessionKeys.sessionId -> UUID.randomUUID.toString
  val mockSessionCache: ERSFileValidatorSessionRepository = mock[ERSFileValidatorSessionRepository]
  implicit val ec: ExecutionContextExecutor = ExecutionContext.global
  val sessionService = new SessionCacheService(mockSessionCache, ec)
  implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
  val hc: HeaderCarrier = HeaderCarrier()
  val CALLBACK_DATA_KEY = "callback_data_key"

  "storeCallbackData" must {
    "successfully store attachments callback post data" in {
      val postData: UpscanCallback = UpscanCallback("thefilename", "downloadUrl", Some(1000L))

      when(mockSessionCache.putSession(DataKey(anyString()), any())
      (any(), any()))
        .thenReturn(Future.successful(sessionPair))

      val result: Option[UpscanCallback] = Await.result(sessionService.storeCallbackData(postData, 1000)(request), 10 seconds)

      result.get.length must be(Some(1000L))
      result.get.noOfRows must be (Some(1000))
    }

    "return a None when cache can't be returned" in {
      val postData: UpscanCallback = UpscanCallback("thefilename", "downloadUrl", Some(1000L))

      when(mockSessionCache.putSession
      (DataKey(anyString()), any())
        (any(), any()))
        .thenReturn(Future.failed(new Exception("")))

      val result: Option[UpscanCallback] = Await.result(sessionService.storeCallbackData(postData, 1000)(request), 10 seconds)
      result mustBe None
    }
  }

  "createCallbackRecord" must {
    "successfully store a NotStarted status in the session cache" in {
      when(mockSessionCache.putSession[UploadStatus](DataKey(meq(CALLBACK_DATA_KEY)), meq(NotStarted))(any(), any()))
        .thenReturn(Future.successful(sessionPair))

      val result: (String, String) = Await.result(sessionService.createCallbackRecord(request), 10 seconds)

      result mustBe sessionPair
      verify(mockSessionCache).putSession[UploadStatus](DataKey(meq(CALLBACK_DATA_KEY)), meq(NotStarted))(any(), any())
    }
  }

  "getCallbackRecord" must {
    "successfully retrieve the stored UploadStatus" in {
      val expectedStatus: UploadStatus = NotStarted

      when(mockSessionCache.getFromSession[UploadStatus](DataKey(meq(CALLBACK_DATA_KEY)))(any(), any()))
        .thenReturn(Future.successful(Some(expectedStatus)))

      val result: Option[UploadStatus] = Await.result(sessionService.getCallbackRecord(request), 10 seconds)

      result mustBe Some(expectedStatus)
      verify(mockSessionCache).getFromSession[UploadStatus](DataKey(meq(CALLBACK_DATA_KEY)))(any(), any())
    }
  }

  "updateCallbackRecord" must {
    "successfully update the UploadStatus in the session cache" in {
      val newStatus: UploadStatus = UploadedSuccessfully("fileId", "downloadUrl", Some(1))

      when(mockSessionCache.putSession[UploadStatus](DataKey(meq(CALLBACK_DATA_KEY)), meq(newStatus))(any(), any()))
        .thenReturn(Future.successful(sessionPair))

      val result: (String, String) = Await.result(sessionService.updateCallbackRecord(newStatus)(request), 10 seconds)

      result mustBe sessionPair
      verify(mockSessionCache).putSession[UploadStatus](DataKey(meq(CALLBACK_DATA_KEY)), meq(newStatus))(any(), any())
    }
  }
}
