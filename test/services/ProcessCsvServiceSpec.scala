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

import ch.qos.logback.classic.Level
import fixtures.{LogCapturePerTest, TestFixtures}
import models._
import models.upscan.{UpscanCallback, UpscanCsvFileData}
import org.apache.pekko.NotUsed
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.http.scaladsl.model.{HttpResponse, StatusCodes}
import org.apache.pekko.stream.scaladsl.{Sink, Source}
import org.apache.pekko.testkit.TestKit
import org.apache.pekko.util.{ByteString, Timeout}
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.EitherValues
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.Helpers.await
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

class ProcessCsvServiceSpec
    extends TestKit(ActorSystem("Test"))
    with AnyWordSpecLike
    with Matchers
    with MockitoSugar
    with ScalaFutures
    with EitherValues
    with LogCapturePerTest
    with TestFixtures {

  implicit val defaultTimeout: Timeout                      = 5.seconds
  private def awaitSequence[T](seq: Seq[Future[T]]): Seq[T] = await(Future.sequence(seq))

  when(mockAppConfig.uploadFileSizeLimit).thenReturn(10000)
  when(mockAppConfig.maxNumberOfRowsPerSubmission).thenReturn(10000)

  private def stubSource(data: String): String => Source[HttpResponse, NotUsed] =
    _ => Source.single(HttpResponse(entity = data))

  type BodyExtractor   = Source[HttpResponse, _] => Source[Either[Throwable, List[ByteString]], _]
  type EntityExtractor = HttpResponse => Source[ByteString, _]

  private def processCsvService(
    extractBodyOverride: Option[BodyExtractor] = None,
    extractEntityOverride: Option[EntityExtractor] = None,
    sendSchemeOverride: Option[Future[Option[Throwable]]] = None
  ): ProcessCsvService =
    new ProcessCsvService(mockAuditEvents, mockAppConfig, mockErsFileValidatorConnector) {

      override def extractRequestBody: BodyExtractor =
        extractBodyOverride.getOrElse(super.extractRequestBody)

      override def extractEntityData(response: HttpResponse): Source[ByteString, _] =
        extractEntityOverride match {
          case Some(httpResponseToSource) => httpResponseToSource(response)
          case None                       => super.extractEntityData(response)
        }

      override def sendSchemeCsv(ersSchemeData: SubmissionsSchemeData, empRef: String)(implicit
        hc: HeaderCarrier
      ): Future[Option[Throwable]] =
        sendSchemeOverride.getOrElse(super.sendSchemeCsv(ersSchemeData, empRef)(hc))
    }

  val submissionsSchemeData: SubmissionsSchemeData = SubmissionsSchemeData(
    schemeInfo,
    "sheetName",
    UpscanCallback("CSOP_OptionsGranted_V4.csv", "no", noOfRows = Some(11)),
    numberOfRows = 11
  )

  val validData: String =
    "2015-09-23,250,123.12,12.1234,12.1234,no,yes,AB12345678,no\n" +
      "2015-09-23,250,123.12,12.1234,12.1234,no,yes,AB12345678,no\n" +
      "2015-09-23,250,123.12,12.1234,12.1234,no,yes,AB12345678,no"

  "processFiles" should {

    "return Right if all rows are valid" in {
      val upscanCallback = UpscanCallback("CSOP_OptionsGranted_V4", "no", noOfRows = Some(1))
      val callback       = UpscanCsvFileData(List(upscanCallback), schemeInfo)

      val results = awaitSequence(
        processCsvService().processFiles(callback, schemeInfo, stubSource(validData))
      )

      results mustBe Seq(Right(CsvFileSubmissions("CSOP_OptionsGranted_V4", 3, upscanCallback)))
    }

    "treat file extension as case-insensitive when processing a valid file" in {
      val upscanCallback = UpscanCallback("CSOP_OptionsGranted_V4.CSV", "no", noOfRows = Some(1))
      val callback       = UpscanCsvFileData(List(upscanCallback), schemeInfo)
      val data           = "2015-09-23,250,123.12,12.1234,12.1234,no,yes,AB12345678,no"

      val results = awaitSequence(
        processCsvService().processFiles(callback, schemeInfo, stubSource(data))
      )

      results mustBe Seq(Right(CsvFileSubmissions("CSOP_OptionsGranted_V4", 1, upscanCallback)))
    }

    "return a user validation error when file is empty" in {
      val callback = UpscanCsvFileData(
        List(UpscanCallback("CSOP_OptionsGranted_V4.csv", "no", noOfRows = Some(1))),
        schemeInfo
      )

      val results = awaitSequence(
        processCsvService(
          extractBodyOverride = Some(_ => Source.empty)
        ).processFiles(callback, schemeInfo, stubSource(validData))
      )

      results mustBe Seq(
        Left(
          FileValidatorNoDataException(
            "The file that you chose doesn’t contain any data.<br/>You won’t be able to upload CSOP_OptionsGranted_V4.csv as part of your annual return.",
            "The file that you chose doesn’t contain any data.<br/>You won’t be able to upload  as part of your annual return."
          )
        )
      )
    }

    "return a user validation error when an error occurs during the file validation" in {
      val callback = UpscanCsvFileData(
        List(UpscanCallback("CSOP_OptionsGranted_V4.csv", "no", noOfRows = Some(3))),
        schemeInfo
      )

      val dataWithError =
        "2015-09-23,250,123.12,12.1234,12.1234,no,yes,AB12345678,no\n" +
          "2015-09- 23,test,123.12,12.1234,12.1234,no,yes,AB12345678,no\n" +
          "2015-09-23,250,123.12,12.1234,12.1234,no,yes,AB12345678,no"

      val results = awaitSequence(
        processCsvService().processFiles(callback, schemeInfo, stubSource(dataWithError))
      )

      results mustBe Seq(
        Left(
          FileValidationException(
            "Found validation errors in CSV",
            "Error processing CSV file: CSOP_OptionsGranted_V4.csv, errors: column - A, error - 001 : Enter a date that matches the yyyy-mm-dd pattern" +
              "\ncolumn - B, error - 002 : Must be a whole number and be less than 1,000,000"
          )
        )
      )
    }

    "return a user validation error when an error occurs during the file processing" in {
      val callback = UpscanCsvFileData(
        List(UpscanCallback("NOT A VALID SHEET", "no", noOfRows = Some(1))),
        schemeInfo
      )

      val results = awaitSequence(
        processCsvService().processFiles(callback, schemeInfo, stubSource(validData))
      )

      results mustBe Seq(
        Left(
          ErsSystemError(
            "Sheet name: NOT A VALID SHEET not found for selected scheme version: V4",
            "Error processing CSV file: NOT A VALID SHEET"
          )
        )
      )
    }

    "return the original UserValidationException when the stream produces a Left(UserValidationException)" in {
      val userValidationError = FileValidationException("user error message", "user error context")
      val callback            = UpscanCsvFileData(
        List(UpscanCallback("CSOP_OptionsGranted_V4.csv", "no", noOfRows = Some(1))),
        schemeInfo
      )

      val results = awaitSequence(
        processCsvService(
          extractBodyOverride = Some(_ => Source.single(Left(userValidationError)))
        ).processFiles(callback, schemeInfo, stubSource(""))
      )

      results mustBe Seq(Left(userValidationError))
    }

    "return an ErsSystemError when the stream produces a Left(Throwable) that is not a UserValidationException" in {
      val exception = new RuntimeException("unexpected processing error")
      val callback  = UpscanCsvFileData(
        List(UpscanCallback("CSOP_OptionsGranted_V4.csv", "no", noOfRows = Some(1))),
        schemeInfo
      )

      val results = awaitSequence(
        processCsvService(
          extractBodyOverride = Some(_ => Source.single(Left(exception)))
        ).processFiles(callback, schemeInfo, stubSource(""))
      )

      results mustBe Seq(
        Left(
          ErsSystemError(
            "unexpected processing error",
            "Unexpected error processing CSV file: CSOP_OptionsGranted_V4.csv"
          )
        )
      )
    }

    "return a FileValidationException when the last row is a Right with non-empty validationErrors" in {
      val invalidRow: List[ByteString] = List(
        ByteString("bad-date"),
        ByteString("250"),
        ByteString("123.12"),
        ByteString("12.1234"),
        ByteString("12.1234"),
        ByteString("no"),
        ByteString("yes"),
        ByteString("AB12345678"),
        ByteString("no")
      )

      val callback = UpscanCsvFileData(
        List(UpscanCallback("CSOP_OptionsGranted_V4.csv", "no", noOfRows = Some(1))),
        schemeInfo
      )

      val futureResults = processCsvService(
        extractBodyOverride = Some(_ => Source.single(Right(invalidRow)))
      ).processFiles(callback, schemeInfo, stubSource(""))

      val results = awaitSequence(futureResults)

      results mustBe Seq(
        Left(
          FileValidationException(
            message = "Found validation errors in CSV",
            context = "Error processing CSV file: CSOP_OptionsGranted_V4.csv, errors: column - A, " +
              "error - 001 : Enter a date that matches the yyyy-mm-dd pattern"
          )
        )
      )
    }

    "return a Left containing the throwable when extractEntityData fails" in {
      val exception =
        ErsSystemError("stream failed", "Unexpected error processing CSV file: CSOP_OptionsGranted_V4.csv")

      val callback = UpscanCsvFileData(
        List(UpscanCallback("CSOP_OptionsGranted_V4.csv", "no", noOfRows = Some(1))),
        schemeInfo
      )

      val results = awaitSequence(
        processCsvService(
          extractEntityOverride = Some(_ => Source.failed(exception))
        ).processFiles(callback, schemeInfo, stubSource(""))
      )

      results mustBe Seq(Left(exception))
    }
  }

  "extractEntityData" should {

    "extract the data from an HttpResponse" in {
      val response = HttpResponse(entity = "Test response body")
      val result   = await(processCsvService().extractEntityData(response).runWith(Sink.head))

      result.utf8String mustBe "Test response body"
    }

    "return a failed source with an ErsFileProcessingException if response status is not Ok (200)" in {
      val response = HttpResponse(status = StatusCodes.InternalServerError)
      val result   = processCsvService().extractEntityData(response).runWith(Sink.head)

      result.failed.futureValue mustBe ErsFileProcessingException(
        "Failed to stream the data from file",
        "Exception bulk entity streaming"
      )
    }
  }

  "extractSchemeData" should {

    "pass on a Left if given a Left" in {
      val userError = ErsSystemError("hello there", "context")
      val result    = processCsvService().extractSchemeData(schemeInfo, empRef, Left(userError)).futureValue

      result.left.value mustBe ErsSystemError("hello there", "context")
    }

    "return system error if sendSchemeCsv finds errors" in {
      val result = await(
        processCsvService(
          sendSchemeOverride = Some(Future.successful(Some(new Exception("this was bad"))))
        ).extractSchemeData(
          schemeInfo,
          empRef,
          Right(
            CsvFileSubmissions("sheetName", 1, UpscanCallback("CSOP_OptionsGranted_V4.csv", "no", noOfRows = Some(1)))
          )
        )
      )

      result mustBe Left(ErsFileProcessingException("this was bad", "Error during CSV submission processing"))
    }

    "return a Right if sendSchemeCsv is happy" in {
      val testService = processCsvService(
        sendSchemeOverride = Some(Future.successful(None))
      )

      attachLogger(testService)

      val result = await(
        testService.extractSchemeData(
          schemeInfo,
          empRef,
          Right(CsvFileSubmissions("sheetName", 1, UpscanCallback("CSOP_OptionsGranted_V4.csv", "no")))
        )
      )

      result.value mustBe CsvFileLengthInfo(1, 1)

      logExistsContaining(Level.INFO, "[ProcessCsvService][extractSchemeData]: File length") mustBe true
    }
  }

  "sendSchemeCsv" should {

    "return a None if the submission was sent successfully" in {
      when(
        mockErsFileValidatorConnector
          .sendToSubmissionsNew(any[SubmissionsSchemeData], any[String])(any[HeaderCarrier])
      ).thenReturn(Future.successful(Right(uk.gov.hmrc.http.HttpResponse(StatusCodes.OK.intValue, "aBody"))))

      when(mockAuditEvents.fileValidatorAudit(any[SchemeInfo], any[String])(any[HeaderCarrier]))
        .thenReturn(true)

      val service = processCsvService()
      attachLogger(service)

      val result = await(service.sendSchemeCsv(submissionsSchemeData, empRef))

      result mustBe None

      logExistsContaining(
        Level.DEBUG,
        "[ProcessCsvService][sendSchemeCsv] Sheetdata sending to ers-submission"
      ) mustBe true
    }

    "return a throwable if the submission was not sent successfully" in {

      when(
        mockErsFileValidatorConnector
          .sendToSubmissionsNew(any[SubmissionsSchemeData], any[String])(any[HeaderCarrier])
      ).thenReturn(Future.successful(Left(new Exception("this failed"))))

      doNothing()
        .when(mockAuditEvents)
        .auditRunTimeError(any[Throwable], any[String], any[SchemeInfo], any[String])(any[HeaderCarrier])

      val result = await(processCsvService().sendSchemeCsv(submissionsSchemeData, empRef))

      result mustBe Some(ErsFileProcessingException(message = "java.lang.Exception: this failed", "this failed"))
    }
  }

  "stripExtension" should {

    val service = processCsvService()

    "Remove the extension" in {
      service.stripExtension("test.csv") mustBe "test"
    }

    "Remove the last extension if the file name contains multiple ." in {
      service.stripExtension("test.somethingelse.csv") mustBe "test.somethingelse"
    }

    "Return the original string if there is no extension" in {
      service.stripExtension("test") mustBe "test"
    }
  }

}
