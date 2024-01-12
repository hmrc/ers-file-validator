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

package controllers

import akka.stream.Materializer
import fixtures.WithMockedAuthActions
import models.upscan._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Play
import play.api.libs.json.{JsObject, JsString, Json}
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SessionCacheService
import uk.gov.hmrc.play.bootstrap.auth.DefaultAuthConnector

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}

class FileValidatorControllerSpec extends PlaySpec with MockitoSugar with GuiceOneAppPerSuite with WithMockedAuthActions with Results {

  val mockSessionService: SessionCacheService = mock[SessionCacheService]
  val controllerComponents: ControllerComponents = stubControllerComponents()
  val mockAuthConnector: DefaultAuthConnector = mock[DefaultAuthConnector]
  val defaultActionBuilder: DefaultActionBuilder = app.injector.instanceOf(classOf[DefaultActionBuilder])
  implicit val ec: ExecutionContextExecutor = ExecutionContext.global
  implicit def materializer: Materializer = Play.materializer

  val controller = new FileValidatorController(
    mockSessionService,
    controllerComponents,
    defaultActionBuilder)

  val empRef = "testEmpRef"
  val sessionId = "testSessionId"
  val request = FakeRequest()

  "FileValidatorController" should {
    "createCallbackRecord" should {
      "create a new callback record and return Created" in {
        when(mockSessionService.createCallbackRecord(any[Request[_]]))
          .thenReturn(Future.successful(("key", sessionId)))

        val result = controller.createCallbackRecord(sessionId)(request)

        status(result) mustBe CREATED
        contentAsJson(result) mustEqual Json.toJson(Map("sessionId" -> sessionId))
      }

      "return InternalServerError when exception occurred" in {
        when(mockSessionService.createCallbackRecord(any[Request[_]]))
          .thenReturn(Future.failed(new Exception()))

        val result = controller.createCallbackRecord(sessionId)(request)

        status(result) mustBe INTERNAL_SERVER_ERROR
      }
    }

    "getCallbackRecord" should {
      "return Ok with record data if found" in {
        when(mockSessionService.getCallbackRecord(any[Request[_]]))
          .thenReturn(Future.successful(Some(InProgress)))

        val result = controller.getCallbackRecord(sessionId)(request)

        status(result) mustBe OK
        contentAsJson(result) mustEqual JsObject(Map("_type" -> JsString("InProgress")))
      }

      "return NotFound when no record is found" in {
        when(mockSessionService.getCallbackRecord(any[Request[_]]))
          .thenReturn(Future.successful(None))

        val result = controller.getCallbackRecord(sessionId)(request)

        status(result) mustBe NOT_FOUND
        contentAsString(result) must include("No callback record found")
      }

      "return InternalServerError when exception occurred" in {
        when(mockSessionService.getCallbackRecord(any[Request[_]]))
          .thenReturn(Future.failed(new Exception()))

        val result = controller.getCallbackRecord(sessionId)(request)

        status(result) mustBe INTERNAL_SERVER_ERROR
      }
    }
  }

  "updateCallbackRecord" should {
    "update record and return NoContent" in {
      val uploadStatus = Json.toJson(Map("_type" -> JsString("InProgress")))
      when(mockSessionService.updateCallbackRecord(any[UploadStatus])(any[Request[_]]))
        .thenReturn(Future.successful(("key", sessionId)))

      val fakeRequest = request.withBody(uploadStatus)
      val result = controller.updateCallbackRecord(sessionId)(fakeRequest)

      status(result) mustBe NO_CONTENT
    }

    "return BadRequest for invalid request body" in {
      val invalidJson = Json.toJson(Map("_type" -> "invalid"))

      val fakeRequest = request.withBody(invalidJson)
      val result = controller.updateCallbackRecord(sessionId).apply(fakeRequest)

      status(result) mustBe BAD_REQUEST
    }

    "return InternalServerError when exception occurred" in {
      val uploadStatus = Json.toJson(Map("_type" -> JsString("InProgress")))

      when(mockSessionService.updateCallbackRecord(any[UploadStatus])(any[Request[_]]))
        .thenReturn(Future.failed(new Exception()))

      val fakeRequest = request.withBody(uploadStatus)
      val result = controller.updateCallbackRecord(sessionId)(fakeRequest)

      status(result) mustBe INTERNAL_SERVER_ERROR
    }
  }
}