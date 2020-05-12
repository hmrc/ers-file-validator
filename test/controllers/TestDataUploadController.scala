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

package controllers

import controllers.auth.AuthAction
import fixtures.WithMockedAuthActions
import metrics.Metrics
import models._
import models.upscan.{UpscanCallback, UpscanCsvFileData, UpscanFileData}
import org.joda.time.DateTime
import org.mockito.Matchers
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.Configuration
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc.{Action, AnyContent, AnyContentAsJson, BodyParser, BodyParsers, Request, Result}
import play.api.test.Helpers._
import play.api.test.{FakeApplication, FakeRequest}
import services.{FileProcessingService, SessionService}
import util.MockAuthAction

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class TestDataUploadController extends PlaySpec with MockitoSugar {

  val mockCurrentConfig = mock[Configuration]
  val mockSessionService = mock[SessionService]
  val mockFileProcessingService = mock[FileProcessingService]
  val metrics = mock[Metrics]

  val schemeInfo: SchemeInfo = SchemeInfo (
    schemeRef = "XA11000001231275",
    timestamp = DateTime.now,
    schemeId = "123PA12345678",
    taxYear = "2014/F15",
    schemeName = "MyScheme",
    schemeType = "EMI"
  )

  object TestDataUploadController extends DataUploadController {
    val currentConfig = mockCurrentConfig
    val sessionService = mockSessionService
    val fileProcessService = mockFileProcessingService
    override def authorisedAction(empRef: String): AuthAction = MockAuthAction
    when(mockSessionService.storeCallbackData(Matchers.any(),Matchers.any())(Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(Some(callbackData)))
  }

  def processFileDataFromFrontend(request: FakeRequest[AnyContentAsJson])(handler: Future[Result] => Any): Unit ={
    handler(TestDataUploadController.processFileDataFromFrontend("empRef").apply(request))
  }

  def processCsvFileDataFromFrontend(request: Request[JsValue])(handler: Future[Result] => Any): Unit ={
    handler(TestDataUploadController.processCsvFileDataFromFrontend("empRef").apply(request))
  }

  val request = FakeRequest()
  val l:ListBuffer[SchemeData] = new ListBuffer()

  val metaData: JsObject = Json.obj(
    "scon" -> "S1401234Z",
    "nino" ->"CB433298A",
    "surname" -> "Smith",
    "firstForename" ->"Bill",
    "calcType" -> "1",
    "dualCalculationRequest" -> false,
    "contsEarningsRequest" -> false,
    "inflationProofRequest" -> false
  )

  val callbackData: UpscanCallback = UpscanCallback("John", "downloadUrl", Some(1000L), Some("content-type"), Some(metaData), None)
  val d: UpscanFileData = UpscanFileData(callbackData, schemeInfo)
  val csvData: UpscanCsvFileData = UpscanCsvFileData(
    List(
      callbackData,
      callbackData
    ),
    schemeInfo
  )

  "DataUploadController" must {
    "Successfully receive data" in {
      reset(mockFileProcessingService)
      running(FakeApplication()) {
        when(mockFileProcessingService.processFile(any[UpscanCallback](), anyString())(any(),any[SchemeInfo](),Matchers.any())).thenReturn(l.size)
        processFileDataFromFrontend(request.withJsonBody(Json.toJson(d))) {
          result =>
            status(result) must be(OK)
        }
      }
    }

    "return errors when an incorrect json object is sent to process-file" in {
      reset(mockFileProcessingService)
      running(FakeApplication()){
        when(mockFileProcessingService.processFile(any[UpscanCallback](), anyString())(any(),any[SchemeInfo](),Matchers.any())).thenReturn(l.size)
          processFileDataFromFrontend(request.withJsonBody(metaData)){
            result =>
              status(result) must be (BAD_REQUEST)
          }
        }

      }

    "Throw exception when invalid data is sent" in {
      reset(mockFileProcessingService)
      running(FakeApplication()) {
        when(mockFileProcessingService.processFile(any[UpscanCallback](), anyString())(any(),any[SchemeInfo](),Matchers.any())).thenThrow(new RuntimeException)
        processFileDataFromFrontend(request.withJsonBody(Json.toJson(d))) {
          result =>
            status(result) must be(INTERNAL_SERVER_ERROR)
        }

      }
    }

    "Return ACCEPTED when ERSFileProcessingException is thrown" in {
      reset(mockFileProcessingService)
      running(FakeApplication()) {
        when(mockFileProcessingService.processFile(any[UpscanCallback](), anyString())(any(),any[SchemeInfo](),Matchers.any())).thenThrow(new ERSFileProcessingException("Error","Tests",None))
        processFileDataFromFrontend(request.withJsonBody(Json.toJson(d))) {
          result =>
            status(result) must be(ACCEPTED)
        }

      }
    }
  }

  "calling processCsvFileDataFromFrontend" must {
    "Successfully receive data" in {
      reset(mockFileProcessingService)
      running(FakeApplication()) {
        when(mockFileProcessingService.processCsvFile(any[UpscanCallback](), anyString())(any(),any[SchemeInfo](),any[Request[_]])).thenReturn(Future(l.size, 100))
        processCsvFileDataFromFrontend(request.withBody(Json.toJson(csvData))) {
          result =>
            status(result) must be(OK)
        }
      }
    }

    "return errors when an incorrect json object is sent to process-file" in {
      reset(mockFileProcessingService)
      running(FakeApplication()){
        when(mockFileProcessingService.processCsvFile(any[UpscanCallback](), anyString())(any(),any[SchemeInfo](),any[Request[_]])).thenReturn(Future(l.size, 100))
        processCsvFileDataFromFrontend(request.withBody(metaData)){
          result =>
            status(result) must be (BAD_REQUEST)
        }
      }

    }
  }
}
