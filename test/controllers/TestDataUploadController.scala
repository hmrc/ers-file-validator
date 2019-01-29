/*
 * Copyright 2019 HM Revenue & Customs
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

import metrics.Metrics
import models._
import org.joda.time.DateTime
import org.mockito.Matchers
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.Configuration
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{Request, AnyContentAsJson, Result}
import play.api.test.Helpers._
import play.api.test.{FakeApplication, FakeRequest}
import services.{FileProcessingService, SessionService}
import scala.collection.mutable.ListBuffer
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

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
    when(mockSessionService.storeCallbackData(Matchers.any(),Matchers.any())(Matchers.any(),Matchers.any())).thenReturn(Future.successful(Some(callbackData)))
  }

  def processFileDataFromFrontend(request: FakeRequest[AnyContentAsJson])(handler: Future[Result] => Any): Unit ={
    handler(TestDataUploadController.processFileDataFromFrontend("empRef").apply(request))
  }

  def processCsvFileDataFromFrontend(request: FakeRequest[AnyContentAsJson])(handler: Future[Result] => Any): Unit ={
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

  val callbackData = CallbackData(collection = "collection", id = "someid", length = 1000L, name = Some("John"), contentType = Some("content-type"), customMetadata = Some(metaData), None)
  val d: FileData = FileData(callbackData, schemeInfo)
  val csvData: CsvFileData = CsvFileData(
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
        when(mockFileProcessingService.processFile(any[CallbackData](), anyString())(any(),any[SchemeInfo](),Matchers.any())).thenReturn(l.size)
        processFileDataFromFrontend(request.withJsonBody(Json.toJson(d))) {
          result =>
            status(result) must be(OK)
        }
      }
    }

    "return errors when an incorrect json object is sent to process-file" in {
      reset(mockFileProcessingService)
      running(FakeApplication()){
        when(mockFileProcessingService.processFile(any[CallbackData](), anyString())(any(),any[SchemeInfo](),Matchers.any())).thenReturn(l.size)
          processFileDataFromFrontend(request.withJsonBody(metaData)){
            result =>
              status(result) must be (BAD_REQUEST)
          }
        }

      }

    "Throw exception when invalid data is sent" in {
      reset(mockFileProcessingService)
      running(FakeApplication()) {
        when(mockFileProcessingService.processFile(any[CallbackData](), anyString())(any(),any[SchemeInfo](),Matchers.any())).thenThrow(new RuntimeException)
        processFileDataFromFrontend(request.withJsonBody(Json.toJson(d))) {
          result =>
            status(result) must be(INTERNAL_SERVER_ERROR)
        }

      }
    }

    "Return ACCEPTED when ERSFileProcessingException is thrown" in {
      reset(mockFileProcessingService)
      running(FakeApplication()) {
        when(mockFileProcessingService.processFile(any[CallbackData](), anyString())(any(),any[SchemeInfo](),Matchers.any())).thenThrow(new ERSFileProcessingException("Error","Tests",None))
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
        when(mockFileProcessingService.processCsvFile(any[CallbackData](), anyString())(any(),any[SchemeInfo](),any[Request[_]])).thenReturn(Future(l.size, 100))
        processCsvFileDataFromFrontend(request.withJsonBody(Json.toJson(csvData))) {
          result =>
            status(result) must be(OK)
        }
      }
    }

    "return errors when an incorrect json object is sent to process-file" in {
      reset(mockFileProcessingService)
      running(FakeApplication()){
        when(mockFileProcessingService.processCsvFile(any[CallbackData](), anyString())(any(),any[SchemeInfo](),any[Request[_]])).thenReturn(Future(l.size, 100))
        processCsvFileDataFromFrontend(request.withJsonBody(metaData)){
          result =>
            status(result) must be (BAD_REQUEST)
        }
      }

    }

//    "Throw exception when invalid data is sent" in {
//      reset(mockFileProcessingService)
//      running(FakeApplication()) {
//        when(mockFileProcessingService.processCsvFile(any[CallbackData]())(any(),any[SchemeInfo]())).then(throw new ERSFileProcessingException("",""))
//        processCsvFileDataFromFrontend(request.withJsonBody(Json.toJson(csvData))) {
//          result =>
//            status(result) must be(INTERNAL_SERVER_ERROR)
//        }
//
//      }
//    }


  }
}
