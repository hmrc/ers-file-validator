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

import fixtures.WithMockedAuthActions
import metrics.Metrics
import models._
import models.upscan.{UpscanCallback, UpscanCsvFileData, UpscanFileData}
import org.joda.time.DateTime
import org.mockito.ArgumentMatchers.{any, eq => argEq}
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Play
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc.{Action, AnyContent, DefaultActionBuilder}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{FileProcessingService, SessionService}
import uk.gov.hmrc.play.bootstrap.auth.DefaultAuthConnector

import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}

class DataUploadControllerSpec extends PlaySpec with MockitoSugar with GuiceOneAppPerSuite with WithMockedAuthActions {

  val empRef: String = "1234/ABCD"
  val mockSessionService: SessionService = mock[SessionService]
  val mockFileProcessingService: FileProcessingService = mock[FileProcessingService]
  val mockAuthConnector: DefaultAuthConnector = mock[DefaultAuthConnector]
  val metrics: Metrics = mock[Metrics]
  implicit val ec: ExecutionContextExecutor = ExecutionContext.global
  implicit def materializer = Play.materializer
  val defaultActionBuilder = app.injector.instanceOf(classOf[DefaultActionBuilder])

  val dataUploadController: DataUploadController = new DataUploadController(mockSessionService, mockFileProcessingService, mockAuthConnector, stubControllerComponents(), defaultActionBuilder, ec){
    override def authorisedAction(empRef: String)(body: AsyncRequest): Action[AnyContent] =
      mockAuthorisedAction(empRef: String)(body: AsyncRequest)
    override def authorisedActionWithBody(empRef: String)(body: AsyncRequestJson): Action[JsValue] =
      mockAuthorisedActionWithBody(empRef: String)(body: AsyncRequestJson)
  }

  val schemeInfo: SchemeInfo = SchemeInfo (
    schemeRef = "XA11000001231275",
    timestamp = DateTime.now,
    schemeId = "123PA12345678",
    taxYear = "2014/F15",
    schemeName = "MyScheme",
    schemeType = "EMI"
  )

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

  "processFileDataFromFrontend" must {
    "Successfully receive data" in {
      when(mockFileProcessingService.processFile(any[UpscanCallback](), argEq(empRef))(any(),any[SchemeInfo](),any())).thenReturn(l.size)
      val result = dataUploadController.processFileDataFromFrontend(empRef).apply(request.withJsonBody(Json.toJson(d)))
      status(result) mustBe OK
    }

    "return errors when an incorrect json object is sent to process-file" in {
      when(mockFileProcessingService.processFile(any[UpscanCallback](), argEq(empRef))(any(),any[SchemeInfo](),any())).thenReturn(l.size)
      val result = dataUploadController.processFileDataFromFrontend(empRef).apply(request.withJsonBody(Json.toJson(metaData)))
      status(result) mustBe BAD_REQUEST
      }

    "Throw exception when invalid data is sent" in {
      when(mockFileProcessingService.processFile(any[UpscanCallback](), argEq(empRef))(any(),any[SchemeInfo](),any())).thenThrow(new RuntimeException)
      val result = dataUploadController.processFileDataFromFrontend(empRef).apply(request.withJsonBody(Json.toJson(d)))
      status(result) mustBe INTERNAL_SERVER_ERROR
      }

    "Return ACCEPTED when ERSFileProcessingException is thrown" in {
      when(mockFileProcessingService.processFile(any[UpscanCallback](), argEq(empRef))(any(), any[SchemeInfo](), any())).thenThrow(new ERSFileProcessingException("Error", "Tests", None))
      val result = dataUploadController.processFileDataFromFrontend(empRef).apply(request.withJsonBody(Json.toJson(d)))
      status(result) mustBe ACCEPTED
    }
  }

  "calling processCsvFileDataFromFrontend" must {
    "Successfully receive data" in {
      when(mockSessionService.storeCallbackData(any(), any())(any(), any()))
        .thenReturn(Future.successful(Some(callbackData)))
      when(mockFileProcessingService.processCsvFile(any[UpscanCallback](), argEq(empRef))(any(), any[SchemeInfo](), any())).thenReturn(Future(l.size, 100))
      val result = dataUploadController.processCsvFileDataFromFrontend(empRef).apply(request.withBody(Json.toJson(csvData)))
      status(result) mustBe OK
    }
    "return errors when an incorrect json object is sent to process-file" in {
      when(mockFileProcessingService.processCsvFile(any[UpscanCallback](), argEq(empRef))(any(), any[SchemeInfo](), any())).thenReturn(Future(l.size, 100))
      val result = dataUploadController.processCsvFileDataFromFrontend(empRef).apply(request.withBody(metaData))
      status(result) mustBe BAD_REQUEST
    }
    "Throw exception when invalid data is sent" in {
      when(mockFileProcessingService.processCsvFile(any[UpscanCallback](), argEq(empRef))(any(), any[SchemeInfo](), any())).thenReturn(Future.failed(new RuntimeException))
      val result = dataUploadController.processCsvFileDataFromFrontend(empRef).apply(request.withBody(Json.toJson(csvData)))
      status(result) mustBe INTERNAL_SERVER_ERROR
    }
    "Return ACCEPTED when ERSFileProcessingException is thrown" in {
      when(mockSessionService.storeCallbackData(any(), any())(any(), any())).thenReturn(Future.successful(None))
      when(mockFileProcessingService.processCsvFile(any[UpscanCallback](), argEq(empRef))(any(), any[SchemeInfo](), any())).thenReturn(Future(l.size, 100))
      val result = dataUploadController.processCsvFileDataFromFrontend(empRef).apply(request.withBody(Json.toJson(csvData)))
      status(result) mustBe ACCEPTED
    }
  }
}
