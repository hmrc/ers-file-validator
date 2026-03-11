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

package services

import config.ApplicationConfig
import connectors.ERSFileValidatorConnector
import models._
import models.upscan.UpscanCallback
import org.mockito.ArgumentMatchers.{any, eq => argEq}
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.EitherValues
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.mvc.Request
import services.audit.AuditEvents
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, SessionId}
import uk.gov.hmrc.validator.models.ods.ValidDataRow
import uk.gov.hmrc.validator._
import java.io.InputStream
import java.time.ZonedDateTime
import scala.collection.mutable.ListBuffer
import scala.concurrent.duration.{Duration, SECONDS}
import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutor, Future}

class ProcessOdsServiceSpec extends PlaySpec with ScalaFutures with MockitoSugar with BeforeAndAfter with EitherValues {

  val mockSessionService: SessionCacheService                  = mock[SessionCacheService]
  val mockErsFileValidatorConnector: ERSFileValidatorConnector = mock[ERSFileValidatorConnector]
  val mockHeaderCarrier: HeaderCarrier                         = mock[HeaderCarrier]
  val mockAppConfig: ApplicationConfig                         = mock[ApplicationConfig]
  val mockAuditEvents: AuditEvents                             = mock[AuditEvents]
  implicit val ec: ExecutionContextExecutor                    = ExecutionContext.global
  implicit val request: Request[_]                             = mock[Request[_]]
  implicit val hc: HeaderCarrier                               = mock[HeaderCarrier]

  val schemeInfo: SchemeInfo = SchemeInfo(
    schemeRef = "XA11999991234567",
    timestamp = ZonedDateTime.now,
    schemeId = "123PA12345678",
    taxYear = "2014/F15",
    schemeName = "MyScheme",
    schemeType = "EMI"
  )

  def createListBuffer(listBuffer: ListBuffer[Seq[String]]): ListBuffer[ValidDataRow] =
    ListBuffer(ValidDataRow(data = listBuffer))

  val callbackData: UpscanCallback = UpscanCallback("csop.ods", "downloadUrl", Some(1024), Some("ods"), None, None)

  def serviceWithException(ex: Throwable): ProcessOdsService =
    new ProcessOdsService(mockAuditEvents, mockErsFileValidatorConnector, mockSessionService, mockAppConfig, ec) {
      override def readFile(downloadUrl: String): InputStream = throw ex
    }

  before {
    reset(mockErsFileValidatorConnector)
    reset(mockSessionService)
  }

  "The File Processing Service" must {
    def processOdsService(): ProcessOdsService =
      new ProcessOdsService(mockAuditEvents, mockErsFileValidatorConnector, mockSessionService, mockAppConfig, ec) {
        override val splitSchemes                               = false
        override val maxNumberOfRows                            = 1
        override def readFile(downloadUrl: String): InputStream = XMLTestData.getEMIAdjustmentsTemplateSTAX
      }

    when(hc.sessionId).thenReturn(Some(SessionId("sessionId")))

    "yield a list of scheme data from file data" in {
      when(
        mockErsFileValidatorConnector
          .sendToSubmissions(any[SchemeData](), any[String]())(any[HeaderCarrier], any[Request[_]])
      ).thenReturn(Future.successful(Right(HttpResponse(200, ""))))

      when(mockSessionService.storeCallbackData(any(), any())(any())).thenReturn(Future.successful(Some(callbackData)))
      when(mockAuditEvents.totalRows(any(), argEq(schemeInfo))(any(), any())).thenReturn(true)

      val result: Future[Either[ErsError, Int]] =
        processOdsService().processFile(callbackData, "")(hc, schemeInfo, request)
      Await.result(result, Duration(5, SECONDS)) mustBe Right(1)
    }

    "yield a list of scheme data from file data with large file" in {
      when(mockAuditEvents.totalRows(any(), argEq(schemeInfo))(any(), any())).thenReturn(true)

      when(
        mockErsFileValidatorConnector.sendToSubmissions(any[SchemeData](), any[String]())(
          any[HeaderCarrier],
          any[Request[_]]
        )
      ).thenReturn(Future.successful(Right(HttpResponse(200, ""))))

      when(mockSessionService.storeCallbackData(any[UpscanCallback], any[Int])(any()))
        .thenReturn(Future.successful(Some(callbackData)))

      val result = processOdsService().processFile(callbackData, "")(hc, schemeInfo, request)
      Await.result(result, Duration(5, SECONDS))
      verify(mockErsFileValidatorConnector, times(1))
        .sendToSubmissions(any(), any[String]())(any[HeaderCarrier], any[Request[_]])
    }

    "return system error when generateSchemeData throws a RuntimeException" in {
      val fileProcessingService: ProcessOdsService = spy(
        new ProcessOdsService(mockAuditEvents, mockErsFileValidatorConnector, mockSessionService, mockAppConfig, ec) {
          override val splitSchemes                               = false
          override val maxNumberOfRows                            = 1
          override def readFile(downloadUrl: String): InputStream = XMLTestData.getEMIAdjustmentsTemplateLarge
        }
      )

      doThrow(new RuntimeException("exception detail"))
        .when(fileProcessingService)
        .generateSchemeData(any(), any())(any())

      val result = Await.result(
        fileProcessingService.processFile(callbackData, "")(hc, schemeInfo, request),
        Duration(5, SECONDS)
      )

      result.swap.map(
        _ mustBe FileValidationError(
          message = "[ProcessOdsService][processFile] Error reading ODS file -> exception detail",
          context = "exception detail"
        )
      )
    }

    "return system error when the callback data isn't stored correctly" in {
      val fileProcessingService: ProcessOdsService =
        new ProcessOdsService(mockAuditEvents, mockErsFileValidatorConnector, mockSessionService, mockAppConfig, ec) {
          override val splitSchemes                               = false
          override val maxNumberOfRows                            = 1
          override def readFile(downloadUrl: String): InputStream = XMLTestData.getEMIAdjustmentsTemplateLarge
        }

      when(mockAuditEvents.totalRows(any(), argEq(schemeInfo))(any(), any())).thenReturn(true)

      when(
        mockErsFileValidatorConnector.sendToSubmissions(any[SchemeData](), any[String]())(
          any[HeaderCarrier],
          any[Request[_]]
        )
      ).thenReturn(Future.successful(Right(HttpResponse(200, ""))))

      when(mockSessionService.storeCallbackData(any[UpscanCallback], any[Int])(any()))
        .thenReturn(Future.successful(None))

      val result = Await.result(
        fileProcessingService.processFile(callbackData, "")(hc, schemeInfo, request),
        Duration(5, SECONDS)
      )
      result mustBe Left(
        ERSFileProcessingException("callback data storage in sessioncache failed ", "Exception storing callback data")
      )
    }

    "return FileValidationError for a generic ValidatorException" in {
      val genericValidatorException = DataContainsAmpersandException()

      val result = Await.result(
        serviceWithException(genericValidatorException)
          .processFile(callbackData, "")(hc, schemeInfo, request),
        Duration(5, SECONDS)
      )
      result.left.value mustBe FileValidationError("Must not contain ampersands.", "Must not contain ampersands.")
    }

    "throw an exception when sending data to ers-submissions fails" in {
      val listBuffer = ListBuffer(
        Seq(
          "yes",
          "yes",
          "yes",
          "4",
          "1989-10-20",
          "Anthony",
          "Joe",
          "Jones",
          "AA123456A",
          "123/XZ55555555",
          "10.1232",
          "100.00",
          "10.2585",
          "10.2544"
        )
      )
      when(
        mockErsFileValidatorConnector
          .sendToSubmissions(any[SchemeData](), any[String]())(any[HeaderCarrier], any[Request[_]])
      ).thenReturn(Future.successful(Left(new RuntimeException("Runtime error"))))

      val processOdsService: ProcessOdsService =
        new ProcessOdsService(mockAuditEvents, mockErsFileValidatorConnector, mockSessionService, mockAppConfig, ec)

      val result = Await.result(
        processOdsService.sendSchemeData(SchemeData(schemeInfo, "", None, listBuffer), ""),
        Duration(5, SECONDS)
      )
      result mustBe Left(ERSFileProcessingException("java.lang.RuntimeException: Runtime error", "Runtime error"))
    }

    "return ERSFileProcessingException when reading the file fails" in {
      val exceptionMessage                         = "Simulated file read failure"
      val fileProcessingService: ProcessOdsService =
        new ProcessOdsService(mockAuditEvents, mockErsFileValidatorConnector, mockSessionService, mockAppConfig, ec) {
          override def readFile(downloadUrl: String): InputStream = throw new RuntimeException(exceptionMessage)
        }

      val result = Await.result(
        fileProcessingService.processFile(callbackData, "")(hc, schemeInfo, request),
        Duration(5, SECONDS)
      )
      result mustBe Left(
        FileValidationError(
          message = "[ProcessOdsService][processFile] Error reading ODS file -> Simulated file read failure",
          context = exceptionMessage
        )
      )
    }
  }

  "mapValidatorException coverage" must {
    "return HeaderValidationError for IncorrectHeaderException" in {
      val result = Await.result(
        serviceWithException(new IncorrectHeaderException("sheetname", "filename"))
          .processFile(callbackData, "")(hc, schemeInfo, request),
        Duration(5, SECONDS)
      )
      result.left.value mustBe a[HeaderValidationError]
    }

    "return SchemeTypeMismatchError for IncorrectSchemeException" in {
      val result = Await.result(
        serviceWithException(new IncorrectSchemeException("EMI", "CSOP", "filename"))
          .processFile(callbackData, "")(hc, schemeInfo, request),
        Duration(5, SECONDS)
      )
      result.left.value mustBe a[SchemeTypeMismatchError]
    }

    "return UnknownSheetError for IncorrectSheetNameException" in {
      val result = Await.result(
        serviceWithException(new IncorrectSheetNameException("sheetName", "schemeName"))
          .processFile(callbackData, "")(hc, schemeInfo, request),
        Duration(5, SECONDS)
      )
      result.left.value mustBe a[UnknownSheetError]
    }

    "return NoDataError for NoDataException" in {
      val result = Await.result(
        serviceWithException(new NoDataException())
          .processFile(callbackData, "")(hc, schemeInfo, request),
        Duration(5, SECONDS)
      )
      result.left.value mustBe a[NoDataError]
    }

    "return ERSFileProcessingException for SystemErrorDuringValidationException" in {
      val result = Await.result(
        serviceWithException(new SystemErrorDuringValidationException("sys error"))
          .processFile(callbackData, "")(hc, schemeInfo, request),
        Duration(5, SECONDS)
      )
      result.left.value mustBe a[ERSFileProcessingException]
    }

    "return ERSFileProcessingException for ParserFailureException" in {
      val result = Await.result(
        serviceWithException(new ParserFailureException())
          .processFile(callbackData, "")(hc, schemeInfo, request),
        Duration(5, SECONDS)
      )
      result.left.value mustBe a[ERSFileProcessingException]
    }
  }

  "sendScheme method" must {
    val oneHundredRecords: ListBuffer[Seq[String]] = ListBuffer.fill(100)(
      Seq(
        "yes",
        "yes",
        "yes",
        "4",
        "1989-10-20",
        "Anthony",
        "Joe",
        "Jones",
        "AA123456A",
        "123/XZ55555555",
        "10.1232",
        "100.00",
        "10.2585",
        "10.2544"
      )
    )

    "return 1 and call sendSchemeData once when splitSchemes is set to false in config even if number of records > max number of rows/sub" in {
      when(
        mockErsFileValidatorConnector
          .sendToSubmissions(any[SchemeData](), any[String]())(any[HeaderCarrier], any[Request[_]])
      ).thenReturn(Future.successful(Right(HttpResponse(200, ""))))
      when(mockAppConfig.splitLargeSchemes).thenReturn(false)
      when(mockAppConfig.maxNumberOfRowsPerSubmission).thenReturn(50)

      val result = Await.result(
        new ProcessOdsService(mockAuditEvents, mockErsFileValidatorConnector, mockSessionService, mockAppConfig, ec)
          .sendScheme(SchemeData(schemeInfo, "", None, oneHundredRecords), ""),
        Duration(5, SECONDS)
      )
      result mustBe Right(1)
      verify(mockErsFileValidatorConnector, times(1))
        .sendToSubmissions(any[SchemeData](), any[String]())(any[HeaderCarrier], any[Request[_]])
    }

    "return 1 and call sendSchemeData once when splitSchemes is set to true but the number of records does not exceed max" in {
      when(
        mockErsFileValidatorConnector
          .sendToSubmissions(any[SchemeData](), any[String]())(any[HeaderCarrier], any[Request[_]])
      ).thenReturn(Future.successful(Right(HttpResponse(200, ""))))

      when(mockAppConfig.splitLargeSchemes).thenReturn(true)
      when(mockAppConfig.maxNumberOfRowsPerSubmission).thenReturn(
        200
      ) // pass in 100 records and the number of records/sheet is 200

      val result = Await.result(
        new ProcessOdsService(mockAuditEvents, mockErsFileValidatorConnector, mockSessionService, mockAppConfig, ec)
          .sendScheme(SchemeData(schemeInfo, "", None, oneHundredRecords), ""),
        Duration(5, SECONDS)
      )
      result mustBe Right(1)
      verify(mockErsFileValidatorConnector, times(1))
        .sendToSubmissions(any[SchemeData](), any[String]())(any[HeaderCarrier], any[Request[_]])
    }

    "return 2 slices and call sendSchemeData twice" in {
      when(
        mockErsFileValidatorConnector
          .sendToSubmissions(any[SchemeData](), any[String]())(any[HeaderCarrier], any[Request[_]])
      ).thenReturn(Future.successful(Right(HttpResponse(200, ""))))

      when(mockAppConfig.splitLargeSchemes).thenReturn(true)
      when(mockAppConfig.maxNumberOfRowsPerSubmission).thenReturn(50) // 100 / 50 = 2

      val result = Await.result(
        new ProcessOdsService(mockAuditEvents, mockErsFileValidatorConnector, mockSessionService, mockAppConfig, ec)
          .sendScheme(SchemeData(schemeInfo, "", None, oneHundredRecords), ""),
        Duration(5, SECONDS)
      )
      result mustBe Right(2)
      verify(mockErsFileValidatorConnector, times(2))
        .sendToSubmissions(any[SchemeData](), any[String]())(any[HeaderCarrier], any[Request[_]])
    }

    "return 3 slices and call sendSchemeData 3 times" in {
      when(
        mockErsFileValidatorConnector
          .sendToSubmissions(any[SchemeData](), any[String]())(any[HeaderCarrier], any[Request[_]])
      ).thenReturn(Future.successful(Right(HttpResponse(200, ""))))

      when(mockAppConfig.splitLargeSchemes).thenReturn(true)
      when(mockAppConfig.maxNumberOfRowsPerSubmission).thenReturn(40) // 100 / 40 = 3 slices

      val result = Await.result(
        new ProcessOdsService(mockAuditEvents, mockErsFileValidatorConnector, mockSessionService, mockAppConfig, ec)
          .sendScheme(SchemeData(schemeInfo, "", None, oneHundredRecords), ""),
        Duration(5, SECONDS)
      )
      result mustBe Right(3)
      verify(mockErsFileValidatorConnector, times(3))
        .sendToSubmissions(any[SchemeData](), any[String]())(any[HeaderCarrier], any[Request[_]])
    }
  }

}
