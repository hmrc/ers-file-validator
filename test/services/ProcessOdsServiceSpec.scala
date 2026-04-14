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

import _root_.utils.ErrorResponseMessages._
import ch.qos.logback.classic.Level
import fixtures.{LogCapturePerTest, TestFixtures}
import models._
import models.upscan.UpscanCallback
import org.apache.pekko.util.Timeout
import org.mockito.ArgumentMatchers.{any, eq => argEq}
import org.mockito.Mockito._
import org.scalatest.EitherValues
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.Helpers.await
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, SessionId}
import uk.gov.hmrc.validator._

import java.io.InputStream
import scala.collection.mutable.ListBuffer
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

class ProcessOdsServiceSpec
    extends AnyFreeSpec with ScalaFutures with MockitoSugar with EitherValues with LogCapturePerTest with TestFixtures {

  implicit val timeout: Timeout = 5.seconds

  when(headerCarrier.sessionId).thenReturn(Some(SessionId("sessionId")))

  val callbackData: UpscanCallback = UpscanCallback("csop.ods", "downloadUrl", Some(1024), Some("ods"), None, None)

  def serviceWithReadFileException(ex: Throwable): ProcessOdsService =
    new ProcessOdsService(mockAuditEvents, mockErsFileValidatorConnector, mockSessionService, mockAppConfig, ec) {
      override def readFile(downloadUrl: String): InputStream = throw ex
    }

  override def beforeEach(): Unit = {
    reset(mockAuditEvents)
    reset(mockErsFileValidatorConnector)
    reset(mockSessionService)
    reset(mockAppConfig)
    super.beforeEach()
  }

  val processOdsService: ProcessOdsService =
    new ProcessOdsService(mockAuditEvents, mockErsFileValidatorConnector, mockSessionService, mockAppConfig, ec) {
      override val splitSchemes    = false
      override val maxNumberOfRows = 1

      override def readFile(downloadUrl: String): InputStream = XMLTestData.getEMIAdjustmentsTemplateLarge
    }

  def serviceWithOverrides(
    splitSchemesOverride: Boolean = false,
    maxNumberOfRowsOverride: Int = 1,
    readFileOverride: InputStream
  ): ProcessOdsService =

    new ProcessOdsService(mockAuditEvents, mockErsFileValidatorConnector, mockSessionService, mockAppConfig, ec) {
      override val splitSchemes                               = splitSchemesOverride
      override val maxNumberOfRows                            = maxNumberOfRowsOverride
      override def readFile(downloadUrl: String): InputStream = readFileOverride
    }

  "ProcessOdsService" - {

    "when calling the ers-file-validator-config library directly" - {

      "must successfully process valid EMI ODS data" in {
        when(
          mockErsFileValidatorConnector.sendToSubmissions(any[SchemeData](), any[String]())(any[HeaderCarrier])
        ).thenReturn(Future.successful(Right(HttpResponse(200, ""))))

        when(mockSessionService.storeCallbackData(any(), any())(any()))
          .thenReturn(Future.successful(Some(callbackData)))

        when(mockAuditEvents.totalRows(any(), argEq(schemeInfo))(any())).thenReturn(true)

        val result = await(processOdsService.processFile(callbackData, "")(headerCarrier, schemeInfo, request))
        result mustBe Right(1)

        verify(mockErsFileValidatorConnector, times(1)).sendToSubmissions(any(), any[String]())(any[HeaderCarrier])
        verify(mockAuditEvents, times(1)).fileValidatorAudit(any(), any())(any())
        verify(mockAuditEvents, times(1)).totalRows(any(), argEq(schemeInfo))(any())
      }

      "must successfully process valid EMI ODS data when no session id exists in header carrier" in {
        implicit val hcWithoutSession: HeaderCarrier = HeaderCarrier()

        when(
          mockErsFileValidatorConnector.sendToSubmissions(any[SchemeData](), any[String]())(any[HeaderCarrier])
        ).thenReturn(Future.successful(Right(HttpResponse(200, ""))))

        when(mockSessionService.storeCallbackData(any(), any())(any()))
          .thenReturn(Future.successful(Some(callbackData)))

        when(mockAuditEvents.totalRows(any(), argEq(schemeInfo))(any())).thenReturn(true)

        val result = await(processOdsService.processFile(callbackData, "")(hcWithoutSession, schemeInfo, request))
        result mustBe Right(1)

        verify(mockErsFileValidatorConnector, times(1)).sendToSubmissions(any(), any[String]())(any[HeaderCarrier])
        verify(mockSessionService, times(1)).storeCallbackData(any(), any())(any())
        verify(mockAuditEvents, times(1)).fileValidatorAudit(any(), any())(any())
        verify(mockAuditEvents, times(1)).totalRows(any(), argEq(schemeInfo))(any())
      }

      "must return Left when processFile reaches processSchemeData and sendScheme fails" in {
        when(
          mockErsFileValidatorConnector.sendToSubmissions(any[SchemeData](), any[String]())(any[HeaderCarrier])
        ).thenReturn(Future.successful(Left(new RuntimeException("submission failed during processFile"))))

        val result = await(processOdsService.processFile(callbackData, "")(headerCarrier, schemeInfo, request))

        result.left.value mustBe ErsFileProcessingException(
          message = "java.lang.RuntimeException: submission failed during processFile",
          context = "submission failed during processFile"
        )
      }

      "must return FileValidationException when ODS data contains ampersands" in {
        val service = serviceWithOverrides(readFileOverride = XMLTestData.getEMIAdjustmentsTemplateWithAmpersand)
        val result  = await(service.processFile(callbackData, "")(headerCarrier, schemeInfo, request))

        result.left.value mustBe FileValidationException("Must not contain ampersands.", "Must not contain ampersands.")
      }

      "must return FileValidatorNoDataException when ODS data is empty" in {
        val service = serviceWithOverrides(readFileOverride = XMLTestData.getEMIAdjustmentsTemplateWithNoData)
        val result  = await(service.processFile(callbackData, "")(headerCarrier, schemeInfo, request))

        result.left.value mustBe FileValidatorNoDataException(dataParserNoData, dataParserNoData)
      }

      "must return HeaderValidationException when ODS header is invalid" in {
        val service = serviceWithOverrides(readFileOverride = XMLTestData.getEMIAdjustmentsTemplateWithIncorrectHeader)
        val result  = await(service.processFile(callbackData, "")(headerCarrier, schemeInfo, request))

        result.left.value mustBe HeaderValidationException(
          "Incorrect ERS Template - Header doesn't match",
          "Incorrect header row"
        )
      }

      "must return FileValidationException when ODS data is invalid" in {
        val service = serviceWithOverrides(readFileOverride = XMLTestData.getEMIAdjustmentsTemplateWithInvalidData)
        val result  = await(service.processFile(callbackData, "")(headerCarrier, schemeInfo, request))

        result.left.value mustBe FileValidationException("Error when validating row", "Error when validating row")
      }

      "must return UnknownSheetException when ODS sheet name is unknown" in {
        val service =
          serviceWithOverrides(readFileOverride = XMLTestData.getEMIAdjustmentsTemplateWithIncorrectSheetName)

        val result = await(service.processFile(callbackData, "")(headerCarrier, schemeInfo, request))
        result.left.value mustBe UnknownSheetException(
          "Incorrect ERS Template - Sheet Name isn't as expected",
          "Couldn't find config for given SheetName: EMI40_Adjustments_V4_With_A_Typo"
        )
      }

      "must return SchemeTypeMismatchException when ODS sheet belongs to a different scheme type" in {
        val service = serviceWithOverrides(readFileOverride = XMLTestData.getEMIAdjustmentsTemplateWithCsopSchemeType)
        val result  = await(service.processFile(callbackData, "")(headerCarrier, schemeInfo, request))

        result.left.value mustBe SchemeTypeMismatchException(
          message = "Incorrect ERS Template - Sheet Name isn't as expected",
          context = "Incorrect ERS Template - Scheme Type isn't as expected, expected: CSOP parsed: EMI",
          expectedSchemeType = "CSOP",
          requestSchemeType = "EMI"
        )
      }

    }

    "when mocking library errors with readFile exceptions" - {

      "must return ErsFileProcessingException when a SystemErrorDuringValidationException is thrown" in {
        val service = serviceWithReadFileException(SystemErrorDuringValidationException("something went wrong"))
        val result  = await(service.processFile(callbackData, "")(headerCarrier, schemeInfo, request))

        result.left.value mustBe ErsFileProcessingException(
          message = "System error during validation: something went wrong",
          context = "System error during ODS processing, schemeRef: XA11000001231275"
        )
      }

      "must return ErsFileProcessingException when a ParserFailureException is thrown" in {
        val service = serviceWithReadFileException(ParserFailureException())
        val result  = await(service.processFile(callbackData, "")(headerCarrier, schemeInfo, request))

        result.left.value mustBe ErsFileProcessingException(
          message = "Failed to retrieve file",
          context = "System error during ODS processing, schemeRef: XA11000001231275"
        )
      }

      "must return ErsFileProcessingException when an unexpected Throwable is thrown" in {
        val service = serviceWithReadFileException(new RuntimeException("something unexpected"))
        val result  = await(service.processFile(callbackData, "")(headerCarrier, schemeInfo, request))

        result.left.value mustBe ErsFileProcessingException(
          message = "something unexpected",
          context = "Unexpected error processing file"
        )
      }
    }

    "sendSchemeData" - {

      "must return Right and audit success when submission succeeds" in {
        when(
          mockErsFileValidatorConnector.sendToSubmissions(any[SchemeData](), any[String]())(any[HeaderCarrier])
        ).thenReturn(Future.successful(Right(HttpResponse(200, ""))))

        val schemeData = SchemeData(
          schemeInfo,
          "sheetName",
          None,
          ListBuffer(
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
        )

        val result = await(processOdsService.sendSchemeData(schemeData, ""))

        result mustBe Right(())
        verify(mockAuditEvents, times(1)).fileValidatorAudit(argEq(schemeInfo), argEq("sheetName"))(any())
      }

      "must return Left and audit runtime error when submission fails" in {
        val ex = new RuntimeException("submission failed")

        when(
          mockErsFileValidatorConnector.sendToSubmissions(any[SchemeData](), any[String]())(any[HeaderCarrier])
        ).thenReturn(Future.successful(Left(ex)))

        val schemeData = SchemeData(
          schemeInfo,
          "sheetName",
          None,
          ListBuffer(
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
        )

        attachLogger(processOdsService)

        val result = await(processOdsService.sendSchemeData(schemeData, ""))

        result.left.value mustBe ErsFileProcessingException(
          message = "java.lang.RuntimeException: submission failed",
          context = "submission failed"
        )

        verify(mockAuditEvents, times(1))
          .auditRunTimeError(argEq(ex), argEq("submission failed"), argEq(schemeInfo), argEq("sheetName"))(any())

        logExistsContaining(Level.ERROR, "An exception occurred") mustBe true
      }

    }

    "sendScheme" - {

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

      "must return 1 and call sendSchemeData once when splitSchemes is set to false in config even if number of records > max number of rows/sub" in {
        when(
          mockErsFileValidatorConnector.sendToSubmissions(any[SchemeData](), any[String]())(any[HeaderCarrier])
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

      "must return 1 and call sendSchemeData once when splitSchemes is set to true but the number of records does not exceed max" in {
        val service =
          new ProcessOdsService(mockAuditEvents, mockErsFileValidatorConnector, mockSessionService, mockAppConfig, ec)

        when(
          mockErsFileValidatorConnector.sendToSubmissions(any[SchemeData](), any[String]())(any[HeaderCarrier])
        ).thenReturn(Future.successful(Right(HttpResponse(200, ""))))

        when(mockAppConfig.splitLargeSchemes).thenReturn(true)
        when(mockAppConfig.maxNumberOfRowsPerSubmission).thenReturn(200)
        // pass in 100 records and the number of records/sheet is 200

        val result = await(service.sendScheme(SchemeData(schemeInfo, "", None, oneHundredRecords), ""))

        result mustBe Right(1)

        verify(mockErsFileValidatorConnector, times(1))
          .sendToSubmissions(any[SchemeData](), any[String]())(any[HeaderCarrier])
      }

      "must return 2 slices and call sendSchemeData twice" in {
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

      "must return 3 slices and call sendSchemeData 3 times" in {
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

      "must return a Left when sendSchemeData fails with splitSchemes enabled" in {
        when(
          mockErsFileValidatorConnector.sendToSubmissions(any[SchemeData](), any[String]())(any[HeaderCarrier])
        ).thenReturn(Future.successful(Left(new RuntimeException("submission failed"))))

        when(mockAppConfig.splitLargeSchemes).thenReturn(true)
        when(mockAppConfig.maxNumberOfRowsPerSubmission).thenReturn(50)

        val service =
          new ProcessOdsService(mockAuditEvents, mockErsFileValidatorConnector, mockSessionService, mockAppConfig, ec)

        attachLogger(service)

        val result = await(service.sendScheme(SchemeData(schemeInfo, "", None, oneHundredRecords), ""))

        result.left.value mustBe ErsFileProcessingException(
          message = "java.lang.RuntimeException: submission failed",
          context = "submission failed"
        )

        logExistsContaining(Level.ERROR, "An exception occurred") mustBe true
      }

      "must return ErsFileProcessingException error when the callback data isn't stored correctly" in {
        when(mockAuditEvents.totalRows(any(), argEq(schemeInfo))(any())).thenReturn(true)

        when(
          mockErsFileValidatorConnector.sendToSubmissions(any[SchemeData](), any[String]())(any[HeaderCarrier])
        ).thenReturn(Future.successful(Right(HttpResponse(200, ""))))

        when(mockSessionService.storeCallbackData(any[UpscanCallback], any[Int])(any()))
          .thenReturn(Future.successful(None))

        val result = await(
          processOdsService.processFile(callbackData, "")(headerCarrier, schemeInfo, request)
        )

        result.left.value mustBe
          ErsFileProcessingException("callback data storage in sessioncache failed ", "Exception storing callback data")
      }

    }

    "readFile" - {

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

      "must return an InputStream when the zip contains content.xml" in {
        val xmlContent = "<document>test</document>".getBytes
        val zipStream  = createZipStream(Map("content.xml" -> xmlContent))

        when(mockErsFileValidatorConnector.upscanFileStream(any())).thenReturn(zipStream)

        val result      = service.readFile("http://test.com/file.ods")
        val resultBytes = result.readAllBytes()

        resultBytes mustBe xmlContent
      }

      "must return an InputStream when content.xml is not the first entry" in {
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

      "must throw ErsFileProcessingException when content.xml is not in the zip" in {

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

        exception mustBe ErsFileProcessingException(
          message = "Failed to stream the data from file",
          context = "Exception bulk entity streaming"
        )
      }

      "must close streams and rethrow when an exception occurs" in {
        val corruptStream = new java.io.ByteArrayInputStream("not a zip".getBytes)

        when(mockErsFileValidatorConnector.upscanFileStream(any())).thenReturn(corruptStream)

        val exception = intercept[ErsFileProcessingException] {
          service.readFile("http://test.com/file.ods")
        }

        exception mustBe ErsFileProcessingException(
          message = "Failed to stream the data from file",
          context = "Exception bulk entity streaming"
        )

      }
    }

  }

}
