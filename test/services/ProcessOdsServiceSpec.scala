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

import _root_.utils.ErrorResponseMessages
import ch.qos.logback.classic.Level
import fixtures.{LogCapturePerTest, TestFixtures}
import models._
import models.upscan.UpscanCallback
import org.apache.pekko.util.Timeout
import org.mockito.ArgumentMatchers.{any, eq => argEq}
import org.mockito.Mockito._
import org.scalatest.EitherValues
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.api.test.Helpers.await
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, SessionId}
import uk.gov.hmrc.validator._

import java.io.InputStream
import scala.collection.mutable.ListBuffer
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

class ProcessOdsServiceSpec
    extends PlaySpec with ScalaFutures with MockitoSugar with EitherValues with LogCapturePerTest with TestFixtures {

  implicit val timeout: Timeout = 5.seconds

  when(headerCarrier.sessionId).thenReturn(Some(SessionId("sessionId")))

  val callbackData: UpscanCallback = UpscanCallback("csop.ods", "downloadUrl", Some(1024), Some("ods"), None, None)

  def serviceWithReadFileException(ex: Throwable): ProcessOdsService =
    new ProcessOdsService(mockAuditEvents, mockErsFileValidatorConnector, mockSessionService, mockAppConfig, ec) {
      override def readFile(downloadUrl: String): InputStream = throw ex
    }

  override def beforeEach(): Unit = {
    reset(mockErsFileValidatorConnector)
    reset(mockSessionService)
    super.beforeEach()
  }

  val processOdsService: ProcessOdsService =
    new ProcessOdsService(mockAuditEvents, mockErsFileValidatorConnector, mockSessionService, mockAppConfig, ec) {
      override val splitSchemes    = false
      override val maxNumberOfRows = 1

      override def readFile(downloadUrl: String): InputStream = XMLTestData.getEMIAdjustmentsTemplateLarge
    }

  "The File Processing Service" must {

    "yield a list of scheme data from file data" in {
      when(
        mockErsFileValidatorConnector
          .sendToSubmissions(any[SchemeData](), any[String]())(any[HeaderCarrier])
      ).thenReturn(Future.successful(Right(HttpResponse(200, ""))))

      when(mockSessionService.storeCallbackData(any(), any())(any())).thenReturn(Future.successful(Some(callbackData)))
      when(mockAuditEvents.totalRows(any(), argEq(schemeInfo))(any())).thenReturn(true)

      val result: Future[Either[ErsException, Int]] =
        processOdsService.processFile(callbackData, "")(headerCarrier, schemeInfo, request)

      await(result) mustBe Right(1)
    }

    "yield a list of scheme data from file data when HeaderCarrier has no sessionId" in {
      val headerCarrierWithoutSession = HeaderCarrier()

      when(
        mockErsFileValidatorConnector
          .sendToSubmissions(any[SchemeData](), any[String]())(any[HeaderCarrier])
      ).thenReturn(Future.successful(Right(HttpResponse(200, ""))))

      when(mockSessionService.storeCallbackData(any(), any())(any()))
        .thenAnswer { invocation =>
          val updatedRequest = invocation.getArgument(2).asInstanceOf[Request[_]]
          updatedRequest.session.get("sessionId").isDefined mustBe true
          Future.successful(Some(callbackData))
        }

      when(mockAuditEvents.totalRows(any(), argEq(schemeInfo))(any())).thenReturn(true)

      val result: Future[Either[ErsException, Int]] =
        processOdsService.processFile(callbackData, "")(
          headerCarrierWithoutSession,
          schemeInfo,
          FakeRequest().withSession()
        )

      await(result) mustBe Right(1)
    }

    "yield a list of scheme data from file data with large file" in {
      when(mockAuditEvents.totalRows(any(), argEq(schemeInfo))(any())).thenReturn(true)

      when(mockErsFileValidatorConnector.sendToSubmissions(any[SchemeData](), any[String]())(any[HeaderCarrier]))
        .thenReturn(Future.successful(Right(HttpResponse(200, ""))))

      when(mockSessionService.storeCallbackData(any[UpscanCallback], any[Int])(any()))
        .thenReturn(Future.successful(Some(callbackData)))

      val result = processOdsService.processFile(callbackData, "")(headerCarrier, schemeInfo, request)

      await(result)

      verify(mockErsFileValidatorConnector, times(1))
        .sendToSubmissions(any(), any[String]())(any[HeaderCarrier])
    }

    "return system error when generateSchemeData throws a RuntimeException" in {
      val spiedService = spy(
        new ProcessOdsService(mockAuditEvents, mockErsFileValidatorConnector, mockSessionService, mockAppConfig, ec) {
          override val splitSchemes                               = false
          override val maxNumberOfRows                            = 1
          override def readFile(downloadUrl: String): InputStream = XMLTestData.getEMIAdjustmentsTemplateLarge
        }
      )

      doThrow(new RuntimeException("exception detail"))
        .when(spiedService)
        .generateSchemeData(any(), any())(any())

      val result = await(spiedService.processFile(callbackData, "")(headerCarrier, schemeInfo, request))

      result.swap.map(
        _ mustBe ErsFileProcessingException(
          message = "exception detail",
          context = "Unexpected error processing file"
        )
      )
    }

    "return system error when the callback data isn't stored correctly" in {
      when(mockAuditEvents.totalRows(any(), argEq(schemeInfo))(any())).thenReturn(true)

      when(
        mockErsFileValidatorConnector.sendToSubmissions(any[SchemeData](), any[String]())(
          any[HeaderCarrier]
        )
      ).thenReturn(Future.successful(Right(HttpResponse(200, ""))))

      when(mockSessionService.storeCallbackData(any[UpscanCallback], any[Int])(any()))
        .thenReturn(Future.successful(None))

      val result = await(
        processOdsService.processFile(callbackData, "")(headerCarrier, schemeInfo, request)
      )

      result mustBe Left(
        ErsFileProcessingException("callback data storage in sessioncache failed ", "Exception storing callback data")
      )
    }

    "return FileValidationException for a generic ValidatorException" in {
      val genericValidatorException = DataContainsAmpersandException()

      val result = await(
        serviceWithReadFileException(genericValidatorException)
          .processFile(callbackData, "")(headerCarrier, schemeInfo, request)
      )
      result.left.value mustBe FileValidationException("Must not contain ampersands.", "Must not contain ampersands.")
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
          .sendToSubmissions(any[SchemeData](), any[String]())(any[HeaderCarrier])
      ).thenReturn(Future.successful(Left(new RuntimeException("Runtime error"))))

      val processOdsService: ProcessOdsService =
        new ProcessOdsService(mockAuditEvents, mockErsFileValidatorConnector, mockSessionService, mockAppConfig, ec)

      val result = await(
        processOdsService.sendSchemeData(SchemeData(schemeInfo, "", None, listBuffer), "")(headerCarrier)
      )
      result mustBe Left(ErsFileProcessingException("java.lang.RuntimeException: Runtime error", "Runtime error"))
    }

    "return ErsFileProcessingException when reading the file fails" in {
      val exceptionMessage = "Simulated file read failure"

      val service = serviceWithReadFileException(new RuntimeException(exceptionMessage))

      val result = await(service.processFile(callbackData, "")(headerCarrier, schemeInfo, request))

      result mustBe Left(
        ErsFileProcessingException(
          message = exceptionMessage,
          context = "Unexpected error processing file"
        )
      )
    }

    "return Left when sendToSubmissions fails during processSchemeData" in {
      when(
        mockErsFileValidatorConnector.sendToSubmissions(any[SchemeData](), any[String]())(any[HeaderCarrier])
      ).thenReturn(Future.successful(Left(new RuntimeException("submission failed"))))

      attachLogger(processOdsService)

      val result = await(processOdsService.processFile(callbackData, "")(headerCarrier, schemeInfo, request))

      result.isLeft             mustBe true
      result.left.value         mustBe a[ErsFileProcessingException]
      result.left.value.message mustBe "java.lang.RuntimeException: submission failed"

      logExistsContaining(Level.DEBUG, "2.1 result contains:") mustBe true
    }

  }

  "mapValidatorException coverage" must {
    "return HeaderValidationException for IncorrectHeaderException" in {
      val result = await(
        serviceWithReadFileException(IncorrectHeaderException("sheetname", "filename"))
          .processFile(callbackData, "")(headerCarrier, schemeInfo, request)
      )
      result.left.value mustBe a[HeaderValidationException]
    }

    "return SchemeTypeMismatchException for IncorrectSchemeException" in {
      val result = await(
        serviceWithReadFileException(IncorrectSchemeException("EMI", "CSOP", "filename"))
          .processFile(callbackData, "")(headerCarrier, schemeInfo, request)
      )

      result.left.value mustBe a[SchemeTypeMismatchException]
    }

    "return UnknownSheetException for IncorrectSheetNameException" in {
      val result = await(
        serviceWithReadFileException(IncorrectSheetNameException("sheetName", "schemeName"))
          .processFile(callbackData, "")(headerCarrier, schemeInfo, request)
      )

      result.left.value mustBe a[UnknownSheetException]
    }

    "return NoDataException for NoDataException" in {
      val result = await(
        serviceWithReadFileException(NoDataException())
          .processFile(callbackData, "")(headerCarrier, schemeInfo, request)
      )

      result.left.value mustBe a[FileValidatorNoDataException]
    }

    "return ErsFileProcessingException for SystemErrorDuringValidationException" in {
      val result = await(
        serviceWithReadFileException(new SystemErrorDuringValidationException("sys error"))
          .processFile(callbackData, "")(headerCarrier, schemeInfo, request)
      )

      result.left.value mustBe a[ErsFileProcessingException]
    }

    "return ErsFileProcessingException for ParserFailureException" in {
      val result = await(
        serviceWithReadFileException(new ParserFailureException())
          .processFile(callbackData, "")(headerCarrier, schemeInfo, request)
      )

      result.left.value mustBe a[ErsFileProcessingException]
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
          .sendToSubmissions(any[SchemeData](), any[String]())(any[HeaderCarrier])
      ).thenReturn(Future.successful(Right(HttpResponse(200, ""))))

      when(mockAppConfig.splitLargeSchemes).thenReturn(false)
      when(mockAppConfig.maxNumberOfRowsPerSubmission).thenReturn(50)

      val service =
        new ProcessOdsService(mockAuditEvents, mockErsFileValidatorConnector, mockSessionService, mockAppConfig, ec)

      attachLogger(service)

      val result = await(service.sendScheme(SchemeData(schemeInfo, "", None, oneHundredRecords), ""))

      result mustBe Right(1)

      verify(mockErsFileValidatorConnector, times(1))
        .sendToSubmissions(any[SchemeData](), any[String]())(any[HeaderCarrier])

      logExistsContaining(Level.DEBUG, "Sheetdata sending to ers-submission") mustBe true
    }

    "return 1 and call sendSchemeData once when splitSchemes is set to true but the number of records does not exceed max" in {
      val service =
        new ProcessOdsService(mockAuditEvents, mockErsFileValidatorConnector, mockSessionService, mockAppConfig, ec)

      when(
        mockErsFileValidatorConnector
          .sendToSubmissions(any[SchemeData](), any[String]())(any[HeaderCarrier])
      ).thenReturn(Future.successful(Right(HttpResponse(200, ""))))

      when(mockAppConfig.splitLargeSchemes).thenReturn(true)
      when(mockAppConfig.maxNumberOfRowsPerSubmission).thenReturn(200)
      // pass in 100 records and the number of records/sheet is 200

      val result = await(service.sendScheme(SchemeData(schemeInfo, "", None, oneHundredRecords), ""))

      result mustBe Right(1)

      verify(mockErsFileValidatorConnector, times(1))
        .sendToSubmissions(any[SchemeData](), any[String]())(any[HeaderCarrier])
    }

    "return 2 slices and call sendSchemeData twice" in {
      when(
        mockErsFileValidatorConnector
          .sendToSubmissions(any[SchemeData](), any[String]())(any[HeaderCarrier])
      ).thenReturn(Future.successful(Right(HttpResponse(200, ""))))

      when(mockAppConfig.splitLargeSchemes).thenReturn(true)
      when(mockAppConfig.maxNumberOfRowsPerSubmission).thenReturn(50)
      // pass in 100 records, 100/50 = 2 -> call sendScheme twice

      val service =
        new ProcessOdsService(mockAuditEvents, mockErsFileValidatorConnector, mockSessionService, mockAppConfig, ec)

      val result = await(service.sendScheme(SchemeData(schemeInfo, "", None, oneHundredRecords), ""))

      result mustBe Right(2)

      verify(mockErsFileValidatorConnector, times(2))
        .sendToSubmissions(any[SchemeData](), any[String]())(any[HeaderCarrier])
    }

    "return 3 slices and call sendSchemeData 3 times" in {
      when(
        mockErsFileValidatorConnector.sendToSubmissions(any[SchemeData](), any[String]())(any[HeaderCarrier])
      ).thenReturn(Future.successful(Right(HttpResponse(200, ""))))

      when(mockAppConfig.splitLargeSchemes).thenReturn(true)
      when(mockAppConfig.maxNumberOfRowsPerSubmission).thenReturn(40)
      // pass in 100 records, 100/40 results in 3 calls to sendScheme

      val service =
        new ProcessOdsService(mockAuditEvents, mockErsFileValidatorConnector, mockSessionService, mockAppConfig, ec)

      attachLogger(service)

      val result = await(service.sendScheme(SchemeData(schemeInfo, "", None, oneHundredRecords), ""))

      result mustBe Right(3)

      verify(mockErsFileValidatorConnector, times(3)).sendToSubmissions(any[SchemeData](), any[String]())(
        any[HeaderCarrier]
      )

      logExistsContaining(Level.DEBUG, "The size of the scheme data is ")
    }

    "return a Left when sendSchemeData fails with splitSchemes enabled" in {
      when(
        mockErsFileValidatorConnector.sendToSubmissions(any[SchemeData](), any[String]())(any[HeaderCarrier])
      ).thenReturn(Future.successful(Left(new RuntimeException("submission failed"))))

      when(mockAppConfig.splitLargeSchemes).thenReturn(true)
      when(mockAppConfig.maxNumberOfRowsPerSubmission).thenReturn(50)

      val service =
        new ProcessOdsService(mockAuditEvents, mockErsFileValidatorConnector, mockSessionService, mockAppConfig, ec)

      attachLogger(service)

      val result = await(service.sendScheme(SchemeData(schemeInfo, "", None, oneHundredRecords), ""))

      result.isLeft     mustBe true
      result.left.value mustBe a[ErsFileProcessingException]

      logExistsContaining(Level.ERROR, "An exception occurred") mustBe true
    }
  }

  "readFile" must {

    def createZipStream(entries: Map[String, Array[Byte]]): InputStream = {
      val byteOutputStream = new java.io.ByteArrayOutputStream()
      val zipOutputStream  = new java.util.zip.ZipOutputStream(byteOutputStream)

      entries.foreach { case (name, content) =>
        zipOutputStream.putNextEntry(new java.util.zip.ZipEntry(name))
        zipOutputStream.write(content)
        zipOutputStream.closeEntry()
      }

      zipOutputStream.close()

      new java.io.ByteArrayInputStream(byteOutputStream.toByteArray)
    }

    val service = new ProcessOdsService(
      mockAuditEvents,
      mockErsFileValidatorConnector,
      mockSessionService,
      mockAppConfig,
      ec
    )

    "return an InputStream when the zip contains content.xml" in {
      val xmlContent = "<document>test</document>".getBytes
      val zipStream  = createZipStream(Map("content.xml" -> xmlContent))

      when(mockErsFileValidatorConnector.upscanFileStream(any())).thenReturn(zipStream)

      val result      = service.readFile("http://test.com/file.ods")
      val resultBytes = result.readAllBytes()

      resultBytes mustBe xmlContent
    }

    "return an InputStream when content.xml is not the first entry" in {
      val xmlContent = "<document>test</document>".getBytes
      val zipStream  = createZipStream(
        Map(
          "styles.xml"  -> "styles".getBytes,
          "meta.xml"    -> "meta".getBytes,
          "content.xml" -> xmlContent
        )
      )

      when(mockErsFileValidatorConnector.upscanFileStream(any())).thenReturn(zipStream)

      val result      = service.readFile("http://test.com/file.ods")
      val resultBytes = result.readAllBytes()

      resultBytes mustBe xmlContent
    }

    "throw ErsFileProcessingException when content.xml is not in the zip" in {

      val zipStream = createZipStream(
        Map(
          "styles.xml" -> "styles".getBytes,
          "meta.xml"   -> "meta".getBytes
        )
      )

      when(mockErsFileValidatorConnector.upscanFileStream(any())).thenReturn(zipStream)

      val exception = intercept[ErsFileProcessingException] {
        service.readFile("http://test.com/file.ods")
      }

      exception.message mustBe ErrorResponseMessages.fileProcessingServiceFailedStream
      exception.context mustBe ErrorResponseMessages.fileProcessingServiceBulkEntity
    }

    "close streams and rethrow when an exception occurs" in {
      val corruptStream = new java.io.ByteArrayInputStream("not a zip".getBytes)

      when(mockErsFileValidatorConnector.upscanFileStream(any())).thenReturn(corruptStream)

      val exception = intercept[ErsFileProcessingException] {
        service.readFile("http://test.com/file.ods")
      }

      exception.message mustBe "Failed to stream the data from file"
      exception.context mustBe "Exception bulk entity streaming"
    }
  }

}
