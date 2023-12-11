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

package controllers

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import akka.testkit.TestKit
import fixtures.WithMockedAuthActions
import metrics.Metrics
import models._
import models.upscan.{UpscanCallback, UpscanCsvFileData, UpscanFileData}
import org.joda.time.DateTime
import org.mockito.ArgumentMatchers.{any, eq => argEq}
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.OptionValues
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Play
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc.{Action, AnyContent, DefaultActionBuilder, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{ProcessCsvService, ProcessOdsService, SessionService}
import uk.gov.hmrc.play.bootstrap.auth.DefaultAuthConnector

import scala.collection.mutable.ListBuffer
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutor, Future}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.Application
import org.scalatest.wordspec.AnyWordSpecLike

class DataUploadControllerSpec extends TestKit(ActorSystem("DataUploadControllerSpec"))
  with AnyWordSpecLike with Matchers with OptionValues with MockitoSugar with GuiceOneAppPerSuite with WithMockedAuthActions with ScalaFutures {

  val empRef: String = "1234/ABCD"
  val mockSessionService: SessionService = mock[SessionService]
  val mockProcessOdsService: ProcessOdsService = mock[ProcessOdsService]
  val mockProcessCsvService: ProcessCsvService = mock[ProcessCsvService]
  val mockAuthConnector: DefaultAuthConnector = mock[DefaultAuthConnector]
  val metrics: Metrics = mock[Metrics]
  implicit val ec: ExecutionContextExecutor = ExecutionContext.global
  override implicit lazy val app: Application = GuiceApplicationBuilder().configure("metrics.enabled" -> false).build()
  implicit def materializer: Materializer = Play.materializer
  val defaultActionBuilder: DefaultActionBuilder = app.injector.instanceOf(classOf[DefaultActionBuilder])

  val dataUploadController: DataUploadController = new DataUploadController(mockSessionService,
    mockProcessOdsService, mockProcessCsvService, mockAuthConnector,
    stubControllerComponents(), defaultActionBuilder) {
    override def authorisedAction(empRef: String)(body: AsyncRequest): Action[AnyContent] =
      mockAuthorisedAction(empRef: String)(body: AsyncRequest)

    override def authorisedActionWithBody(empRef: String)(body: AsyncRequestJson): Action[JsValue] =
      mockAuthorisedActionWithBody(empRef: String)(body: AsyncRequestJson)

    val mockSource: Source[HttpResponse, NotUsed] = Source.fromIterator(() => List(HttpResponse(StatusCodes.OK)).iterator)

    override private[controllers] def streamFile(downloadUrl: String): Source[HttpResponse, _] = mockSource
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
      when(mockProcessOdsService.processFile(any[UpscanCallback](), argEq(empRef))(any(),any[SchemeInfo](),any())).thenReturn(Future.successful(l.size))
      val result = dataUploadController.processFileDataFromFrontend(empRef).apply(request.withJsonBody(Json.toJson(d)))
      status(result) shouldBe OK
    }

    "return errors when an incorrect json object is sent to process-file" in {
      when(mockProcessOdsService.processFile(any[UpscanCallback](), argEq(empRef))(any(),any[SchemeInfo](),any())).thenReturn(Future.successful(l.size))
      val result = dataUploadController.processFileDataFromFrontend(empRef).apply(request.withJsonBody(Json.toJson(metaData)))
      status(result) shouldBe BAD_REQUEST
      }

    "Throw exception when invalid data is sent" in {
      when(mockProcessOdsService.processFile(any[UpscanCallback](), argEq(empRef))(any(),any[SchemeInfo](),any()))
        .thenReturn(Future.failed(new RuntimeException))
      val result = dataUploadController.processFileDataFromFrontend(empRef).apply(request.withJsonBody(Json.toJson(d)))
      status(result) shouldBe INTERNAL_SERVER_ERROR
      }

    "Return ACCEPTED when ERSFileProcessingException is thrown" in {
      when(mockProcessOdsService.processFile(any[UpscanCallback](), argEq(empRef))(any(), any[SchemeInfo](), any()))
        .thenReturn(Future.failed(ERSFileProcessingException("Error", "Tests", None)))
      val result = dataUploadController.processFileDataFromFrontend(empRef).apply(request.withJsonBody(Json.toJson(d)))
      status(result) shouldBe ACCEPTED
    }
  }

  "calling processCsvFileDataFromFrontendV2" must {
    "Successfully receive data" in {
      when(mockSessionService.storeCallbackData(any(), any())(any()))
        .thenReturn(Future.successful(Some(callbackData)))
      when(mockProcessCsvService.processFilesNew(any[UpscanCsvFileData](), any())(any(), any()))
        .thenReturn(List(Future(Right(CsvFileSubmissions("sheetName", 1, callbackData)))))
      when(mockProcessCsvService.extractSchemeDataNew(any(), any(), any())(any(), any()))
        .thenReturn(Future(Right(CsvFileLengthInfo(1,1))))

      val result = dataUploadController.processCsvFileDataFromFrontendV2(empRef).apply(request.withBody(Json.toJson(csvData)))
      status(result) shouldBe OK
    }

    "return an ERSFileProcessingException if one occurs" in {
      when(mockProcessCsvService.processFilesNew(any[UpscanCsvFileData](), any())(any(), any()))
        .thenReturn(List(Future(Right(CsvFileSubmissions("sheetName", 1, callbackData)))))
      when(mockProcessCsvService.extractSchemeDataNew(any(), any(), any())(any(), any()))
        .thenReturn(Future(Left(ERSFileProcessingException("Error processing file",""))))

      val result = dataUploadController.processCsvFileDataFromFrontendV2(empRef).apply(request.withBody(Json.toJson(csvData)))
      status(result) shouldBe ACCEPTED
      contentAsString(result) shouldBe "Error processing file"
    }

    "return a 500 if any other kind of exception occurs" in {
      when(mockProcessCsvService.processFilesNew(any[UpscanCsvFileData](), any())(any(), any()))
        .thenReturn(List(Future(Right(CsvFileSubmissions("sheetName", 1, callbackData)))))
      when(mockProcessCsvService.extractSchemeDataNew(any(), any(), any())(any(), any()))
        .thenReturn(Future(Left(new RuntimeException("Oh boy"))))

      val result = dataUploadController.processCsvFileDataFromFrontendV2(empRef).apply(request.withBody(Json.toJson(csvData)))
      status(result) shouldBe INTERNAL_SERVER_ERROR
    }

    "return an error when failing to store callback data" in {
      when(mockSessionService.storeCallbackData(any(), any())(any()))
        .thenReturn(Future.successful(None))
      when(mockProcessCsvService.processFilesNew(any[UpscanCsvFileData](), any())(any(), any()))
        .thenReturn(List(Future(Right(CsvFileSubmissions("sheetName", 1, callbackData)))))
      when(mockProcessCsvService.extractSchemeDataNew(any(), any(), any())(any(), any()))
        .thenReturn(Future(Right(CsvFileLengthInfo(1,1))))

      val result: Future[Result] = dataUploadController.processCsvFileDataFromFrontendV2(empRef).apply(request.withBody(Json.toJson(csvData)))
      status(result) shouldBe ACCEPTED
      contentAsString(result) shouldBe "csv callback data storage in sessioncache failed"
    }

    "return a 400 if the body cannot be parsed into an UpscanCsvFileData object" in {
      val result: Future[Result] = dataUploadController.processCsvFileDataFromFrontendV2(empRef).apply(request.withBody(Json.toJson("bad json")))
      status(result) shouldBe BAD_REQUEST
    }
  }

  val secondDataUploadController: DataUploadController = new DataUploadController(mockSessionService,
    mockProcessOdsService, mockProcessCsvService, mockAuthConnector,
    stubControllerComponents(), defaultActionBuilder) {
    override def authorisedAction(empRef: String)(body: AsyncRequest): Action[AnyContent] =
      mockAuthorisedAction(empRef: String)(body: AsyncRequest)

    override def authorisedActionWithBody(empRef: String)(body: AsyncRequestJson): Action[JsValue] =
      mockAuthorisedActionWithBody(empRef: String)(body: AsyncRequestJson)

    override private[controllers] def makeRequest(request: HttpRequest): Future[HttpResponse] = Future.successful(HttpResponse(StatusCodes.OK))
  }

  "Calling streamFile" should {
    "process the response" in {
      val result: Future[Seq[HttpResponse]] = secondDataUploadController.streamFile("http://www.test.com").runWith(Sink.seq)

      val responses = Await.result(result, Duration.Inf)
      responses.length shouldBe 1
      responses.head shouldBe HttpResponse(StatusCodes.OK)

    }
  }
}
