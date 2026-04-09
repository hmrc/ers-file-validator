/*
 * Copyright 2026 HM Revenue & Customs
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

import ch.qos.logback.classic.Level
import fixtures.{LogCapturePerTest, TestFixtures, WithMockedAuthActions}
import metrics.Metrics
import models._
import models.upscan.{UpscanCallback, UpscanCsvFileData}
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.http.scaladsl.Http
import org.apache.pekko.http.scaladsl.model.{HttpResponse, StatusCodes}
import org.apache.pekko.stream.scaladsl.Sink
import org.apache.pekko.testkit.TestKit
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.OptionValues
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc.{Action, DefaultActionBuilder, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.ProcessCsvService
import uk.gov.hmrc.play.bootstrap.auth.DefaultAuthConnector

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class CsvUploadControllerSpec
    extends TestKit(ActorSystem("CsvUploadControllerSpec"))
    with AnyWordSpecLike
    with Matchers
    with OptionValues
    with MockitoSugar
    with GuiceOneAppPerSuite
    with WithMockedAuthActions
    with ScalaFutures
    with LogCapturePerTest
    with TestFixtures {

  val mockProcessCsvService: ProcessCsvService = mock[ProcessCsvService]
  val mockAuthConnector: DefaultAuthConnector  = mock[DefaultAuthConnector]
  val metrics: Metrics                         = mock[Metrics]

  implicit override lazy val app: Application =
    GuiceApplicationBuilder().configure("metrics.enabled" -> false).build()

  val defaultActionBuilder: DefaultActionBuilder =
    app.injector.instanceOf(classOf[DefaultActionBuilder])

  val csvUploadController: CsvUploadController = new CsvUploadController(
    mockAuditEvents,
    mockSessionService,
    mockProcessCsvService,
    mockAuthConnector,
    stubControllerComponents(),
    defaultActionBuilder
  ) {
    override def authorisedActionWithBody(empRef: String)(body: AsyncRequestJson): Action[JsValue] =
      mockAuthorisedActionWithBody(empRef)(body)
  }

  override def logCaptureTargets: Seq[CsvUploadController] = Seq(csvUploadController)

  override val request = FakeRequest()

  val metaData: JsObject = Json.obj(
    "scon"                   -> "S1401234Z",
    "nino"                   -> "CB433298A",
    "surname"                -> "Smith",
    "firstForename"          -> "Bill",
    "calcType"               -> "1",
    "dualCalculationRequest" -> false,
    "contsEarningsRequest"   -> false,
    "inflationProofRequest"  -> false
  )

  val callbackData: UpscanCallback =
    UpscanCallback("John", "downloadUrl", Some(1000L), Some("content-type"), Some(metaData), None)

  val csvData: UpscanCsvFileData = UpscanCsvFileData(List(callbackData, callbackData), schemeInfo)

  "processCsvFile" must {

    when(mockProcessCsvService.processFiles(any[UpscanCsvFileData](), any[SchemeInfo], any()))
      .thenReturn(List(Future(Right(CsvFileSubmissions("sheetName", 1, callbackData)))))

    "successfully receive data" in {
      when(mockSessionService.storeCallbackData(any(), any())(any()))
        .thenReturn(Future.successful(Some(callbackData)))

      when(mockProcessCsvService.extractSchemeData(any(), any(), any())(any()))
        .thenReturn(Future(Right(CsvFileLengthInfo(1, 1))))

      val result = csvUploadController.processCsvFile(empRef).apply(request.withBody(Json.toJson(csvData)))
      status(result) shouldBe OK
    }

    "return BAD_REQUEST when a UserValidationException occurs in extractSchemeData" in {
      val userError = FileValidatorNoDataException("No data found", "File contains no data")

      when(mockProcessCsvService.extractSchemeData(any(), any(), any())(any()))
        .thenReturn(Future(Left(userError)))

      val result = csvUploadController.processCsvFile(empRef).apply(request.withBody(Json.toJson(csvData)))
      status(result)          shouldBe BAD_REQUEST
      contentAsString(result) shouldBe "No data found"

      logExistsContaining(Level.WARN, "[CsvUploadController][handleCsvResults] User validation error:") shouldBe true

    }

    "return INTERNAL_SERVER_ERROR when a SystemError occurs" in {
      when(mockProcessCsvService.extractSchemeData(any(), any(), any())(any()))
        .thenReturn(Future.successful(Left(ErsSystemError("System configuration error", "Config failure"))))

      val result = csvUploadController.processCsvFile(empRef).apply(request.withBody(Json.toJson(csvData)))
      status(result) shouldBe INTERNAL_SERVER_ERROR

      logExistsContaining(Level.ERROR, "[CsvUploadController][handleCsvResults] System error:") shouldBe true
    }

    "return ACCEPTED when failing to store callback data" in {
      when(mockSessionService.storeCallbackData(any(), any())(any()))
        .thenReturn(Future.successful(None))

      when(mockProcessCsvService.extractSchemeData(any(), any(), any())(any()))
        .thenReturn(Future(Right(CsvFileLengthInfo(1, 1))))

      val result: Future[Result] =
        csvUploadController.processCsvFile(empRef).apply(request.withBody(Json.toJson(csvData)))
      status(result)          shouldBe ACCEPTED
      contentAsString(result) shouldBe "csv callback data storage in sessioncache failed"

      logExistsContaining(
        Level.ERROR,
        "[CsvUploadController][storeCsvCallbackDataAndRespond] CSV storeCallbackData failed"
      ) shouldBe true

    }

    "return BAD_REQUEST if the body cannot be parsed into an UpscanCsvFileData object" in {
      val result: Future[Result] =
        csvUploadController.processCsvFile(empRef).apply(request.withBody(Json.toJson("bad json")))

      status(result)          shouldBe BAD_REQUEST
      contentAsString(result) shouldBe
        "Invalid request body, parse errors: obj: error.expected.jsobject"

      logExistsContaining(Level.WARN, "[CsvUploadController][processCsvFile] Invalid request body") shouldBe true
    }

    "streamFile function passed into processFiles should be usable" in {

      var capturedStream: String => org.apache.pekko.stream.scaladsl.Source[HttpResponse, _] = null

      when(mockProcessCsvService.processFiles(any[UpscanCsvFileData](), any[SchemeInfo], any()))
        .thenAnswer { invocation =>
          capturedStream = invocation.getArgument(2)
          List(Future.successful(Right(CsvFileSubmissions("sheet", 1, callbackData))))
        }

      when(mockProcessCsvService.extractSchemeData(any(), any(), any())(any()))
        .thenReturn(Future.successful(Right(CsvFileLengthInfo(1, 1))))

      when(mockSessionService.storeCallbackData(any(), any())(any()))
        .thenReturn(Future.successful(Some(callbackData)))

      val result =
        csvUploadController.processCsvFile(empRef).apply(request.withBody(Json.toJson(csvData)))

      status(result) shouldBe OK

      val binding = Await.result(
        Http().newServerAt("localhost", 0).bindSync(_ => HttpResponse(StatusCodes.OK)),
        Duration.Inf
      )
      val port    = binding.localAddress.getPort

      val responses = Await.result(
        capturedStream(s"http://localhost:$port").runWith(Sink.seq),
        Duration.Inf
      )

      responses.head.status shouldBe StatusCodes.OK

      Await.result(binding.unbind(), Duration.Inf)
    }
  }

  "streamFile" should {

    val binding = Await.result(
      Http().newServerAt("localhost", 0).bindSync(_ => HttpResponse(StatusCodes.OK)),
      Duration.Inf
    )
    val port    = binding.localAddress.getPort

    val streamController = spy(
      new CsvUploadController(
        mockAuditEvents,
        mockSessionService,
        mockProcessCsvService,
        mockAuthConnector,
        stubControllerComponents(),
        defaultActionBuilder
      ) {
        override def authorisedActionWithBody(empRef: String)(body: AsyncRequestJson): Action[JsValue] =
          mockAuthorisedActionWithBody(empRef)(body)
      }
    )

    "process the response" in {
      val result    = streamController.streamFile(s"http://localhost:$port").runWith(Sink.seq)
      val responses = Await.result(result, Duration.Inf)

      verify(streamController, times(1)).makeRequest(any())
      responses.length      shouldBe 1
      responses.head.status shouldBe StatusCodes.OK

      Await.result(binding.unbind(), Duration.Inf)
    }
  }

}
