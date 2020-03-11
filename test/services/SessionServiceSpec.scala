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

import models._
import models.upscan.UpscanCallback
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.{OneServerPerSuite, PlaySpec}
import play.api.libs.json._
import play.api.test.FakeRequest
import uk.gov.hmrc.http.cache.client.{CacheMap, SessionCache}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class SessionServiceSpec extends PlaySpec with OneServerPerSuite with ScalaFutures with MockitoSugar {

  object TestSessionService extends SessionService {

    val mockSessionCache = mock[SessionCache]

    override def sessionCache: SessionCache = mockSessionCache

  }

  implicit val request = FakeRequest()
  val hc = HeaderCarrier()

  "Session service" must {
    "successfully store attachments callback post data" in {
      val postData: UpscanCallback = UpscanCallback("thefilename", "downloadUrl", Some(1000L))

      val json = Json.toJson[UpscanCallback](postData)
      when(TestSessionService.sessionCache.cache[UpscanCallback]
        (any[String], any[UpscanCallback])
        (any[Writes[UpscanCallback]], any[HeaderCarrier], any()))
        .thenReturn(Future.successful(CacheMap("sessionValue", Map(SessionService.CALLBACK_DATA_KEY -> json))))

      val result: Option[UpscanCallback] = Await.result(TestSessionService.storeCallbackData(postData, 1000)(request, hc), 10 seconds)

      result.get.length must be(Some(1000L))
      result.get.noOfRows must be (Some(1000))
    }
  }
}
