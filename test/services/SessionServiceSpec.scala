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

import models._

import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.{OneServerPerSuite, PlaySpec}
import play.api.libs.json._
import play.api.test.FakeRequest
import uk.gov.hmrc.http.cache.client.{CacheMap, SessionCache}

import uk.gov.hmrc.play.http.HeaderCarrier

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

    "successfully store arbitrary string" in {

      val foo = "bar"
      val json = Json.toJson[String](foo)
      when(TestSessionService.sessionCache.cache[String]
        (any[String], any[String])
        (any[Writes[String]], any[HeaderCarrier]))
        .thenReturn(Future.successful(CacheMap("sessionValue", Map("fookey" -> json))))

      val result = Await.result(TestSessionService.storeString("fookey", foo)(request, hc), 10 seconds)

      result.get must be("bar")
    }

    "fail to store string" in {
      val foo = "bar"
      val json = Json.toJson[String](foo)
      when(TestSessionService.sessionCache.cache[String]
        (any[String], any[String])
        (any[Writes[String]], any[HeaderCarrier]))
        .thenReturn(Future.failed(new RuntimeException))


      val result = Await.result(TestSessionService.storeString("fookey", foo)(request, hc), 10 seconds)
      result must be(None)
    }

    "successfully store attachments callback post data" in {
      val postData = CallbackData(id = "theid", collection = "thecollection", length = 1000L, name = Some("thefilename"), contentType = None, customMetadata
        = None)

      val json = Json.toJson[CallbackData](postData)
      when(TestSessionService.sessionCache.cache[CallbackData]
        (any[String], any[CallbackData])
        (any[Writes[CallbackData]], any[HeaderCarrier]))
        .thenReturn(Future.successful(CacheMap("sessionValue", Map(SessionService.CALLBACK_DATA_KEY -> json))))

      val result: Option[CallbackData] = Await.result(TestSessionService.storeCallbackData(postData)(request, hc), 10 seconds)

      result.get.length must be(1000L)

    }


    "successfully retrieve callback post data" in {
      val postData = CallbackData(id = "theid", collection = "thecollection", length = 1000L, name = Some("thefilename"), contentType = None, customMetadata
        = None)

      when(TestSessionService.sessionCache.fetchAndGetEntry[CallbackData](any())(any(), any())).thenReturn(Future.successful(Some
        (postData)))

      val result = Await.result(TestSessionService.retrieveCallbackData()(request, hc), 10 seconds)

      result.get.id must be("theid")
    }

     }
}
