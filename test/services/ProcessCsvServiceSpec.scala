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
import models.upscan.{UpscanCallback, UpscanCsvFileData}
import org.apache.pekko.NotUsed
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.http.scaladsl.model.{HttpResponse, StatusCodes}
import org.apache.pekko.stream.scaladsl.{Sink, Source}
import org.apache.pekko.testkit.TestKit
import org.apache.pekko.util.ByteString
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.concurrent.{ScalaFutures, TimeLimits}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.{EitherValues, OptionValues}
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.Request
import services.audit.AuditEvents
import uk.gov.hmrc.http.HeaderCarrier

import java.time.ZonedDateTime
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class ProcessCsvServiceSpec
    extends TestKit(ActorSystem("Test"))
    with AnyWordSpecLike
    with Matchers
    with OptionValues
    with MockitoSugar
    with TimeLimits
    with ScalaFutures
    with EitherValues {

  val mockAuditEvents: AuditEvents                             = mock[AuditEvents]
  val mockErsFileValidatorConnector: ERSFileValidatorConnector = mock[ERSFileValidatorConnector]
  val mockAppConfig: ApplicationConfig                         = mock[ApplicationConfig]
  when(mockAppConfig.uploadFileSizeLimit).thenReturn(10000)
  when(mockAppConfig.maxNumberOfRowsPerSubmission).thenReturn(10000)

  implicit val request: Request[_] = mock[Request[_]]
  implicit val hc: HeaderCarrier   = mock[HeaderCarrier]

  val schemeInfo: SchemeInfo = SchemeInfo(
    schemeRef = "XA11999991234567",
    timestamp = ZonedDateTime.now,
    schemeId = "123PA12345678",
    taxYear = "2014/F15",
    schemeName = "MyScheme",
    schemeType = "EMI"
  )

  val submissionsSchemeData: SubmissionsSchemeData = SubmissionsSchemeData(
    schemeInfo,
    "sheetName",
    UpscanCallback("CSOP_OptionsGranted_V4.csv", "no", noOfRows = Some(11)),
    numberOfRows = 11
  )

  def processCsvService(
    extractBodyOfRequestOverride: Option[Source[HttpResponse, _] => Source[Either[Throwable, List[ByteString]], _]] =
      None,
    extractEntityDataOverride: Option[HttpResponse => Source[ByteString, _]] = None,
    sendSchemeCsvNewOverride: Option[Future[Option[Throwable]]] = None
  ): ProcessCsvService = new ProcessCsvService(mockAuditEvents, mockAppConfig, mockErsFileValidatorConnector) {
    override def extractBodyOfRequest: Source[HttpResponse, _] => Source[Either[Throwable, List[ByteString]], _] =
      extractBodyOfRequestOverride.getOrElse(super.extractBodyOfRequest)

    override def extractEntityData(response: HttpResponse): Source[ByteString, _] =
      extractEntityDataOverride match {
        case Some(value) => value(response)
        case None        => super.extractEntityData(response)
      }

    override def sendSchemeCsv(ersSchemeData: SubmissionsSchemeData, empRef: String)(implicit
      hc: HeaderCarrier
    ): Future[Option[Throwable]] =
      sendSchemeCsvNewOverride.getOrElse(super.sendSchemeCsv(ersSchemeData, empRef))
  }

  "processFiles" should {
    def returnStubSource(x: String, data: String): Source[HttpResponse, NotUsed] =
      Source.single(HttpResponse(entity = data))

    "return Right if all rows are valid" in {
      val upscanCallback = UpscanCallback("CSOP_OptionsGranted_V4", "no", noOfRows = Some(1))
      val callback       = UpscanCsvFileData(List(upscanCallback), schemeInfo)
      val data           =
        "2015-09-23,250,123.12,12.1234,12.1234,no,yes,AB12345678,no\n" +
          "2015-09-23,250,123.12,12.1234,12.1234,no,yes,AB12345678,no\n" +
          "2015-09-23,250,123.12,12.1234,12.1234,no,yes,AB12345678,no"

      val resultFuture = processCsvService().processFiles(callback, schemeInfo, returnStubSource(_, data))
      val boolList     = Await.result(Future.sequence(resultFuture), Duration.Inf)

      boolList mustBe List(Right(CsvFileSubmissions("CSOP_OptionsGranted_V4", 3, upscanCallback)))
      assert(boolList.forall(_.isRight))
    }

    "treat file extension as case-insensitive when processing a valid file" in {
      val upscanCallback = UpscanCallback("CSOP_OptionsGranted_V4.CSV", "no", noOfRows = Some(1))
      val callback       = UpscanCsvFileData(List(upscanCallback), schemeInfo)
      val data           = "2015-09-23,250,123.12,12.1234,12.1234,no,yes,AB12345678,no"

      val resultFuture = processCsvService().processFiles(callback, schemeInfo, returnStubSource(_, data))
      val boolList     = Await.result(Future.sequence(resultFuture), Duration.Inf)

      boolList mustBe List(Right(CsvFileSubmissions("CSOP_OptionsGranted_V4", 1, upscanCallback)))
      assert(boolList.forall(_.isRight))
    }

    "return a user validation error when file is empty" in {
      val callback = UpscanCsvFileData(
        List(UpscanCallback("CSOP_OptionsGranted_V4.csv", "no", noOfRows = Some(1))),
        schemeInfo
      )
      val data     =
        "2015-09-23,250,123.12,12.1234,12.1234,no,yes,AB12345678,no\n" +
          "2015-09-23,250,123.12,12.1234,12.1234,no,yes,AB12345678,no\n" +
          "2015-09-23,250,123.12,12.1234,12.1234,no,yes,AB12345678,no"

      val resultFuture = processCsvService(
        extractBodyOfRequestOverride = Some(_ => Source.empty)
      ).processFiles(callback, schemeInfo, returnStubSource(_, data))

      val boolList = Await.result(Future.sequence(resultFuture), Duration.Inf)

      assert(boolList.head.isLeft)
      boolList.head.left.value mustBe a[FileValidatorNoDataException]
      val error = boolList.head.left.value.asInstanceOf[FileValidatorNoDataException]
      error.message mustBe "The file that you chose doesn’t contain any data.<br/>You won’t be able to upload CSOP_OptionsGranted_V4.csv as part of your annual return."
      assert(boolList.forall(_.isLeft))
    }

    "return a user validation error when an error occurs during the file validation" in {
      val callback = UpscanCsvFileData(
        List(UpscanCallback("CSOP_OptionsGranted_V4.csv", "no", noOfRows = Some(3))),
        schemeInfo
      )
      val data     =
        "2015-09-23,250,123.12,12.1234,12.1234,no,yes,AB12345678,no\n" +
          "2015-09- 23,test,123.12,12.1234,12.1234,no,yes,AB12345678,no\n" +
          "2015-09-23,250,123.12,12.1234,12.1234,no,yes,AB12345678,no"

      val resultFuture = processCsvService().processFiles(callback, schemeInfo, returnStubSource(_, data))

      val errorMessage = "[ProcessCsvService][processFiles]: Found validation errors in CSV"
      val context      =
        "Error processing CSV file: CSOP_OptionsGranted_V4.csv, errors: column - A, error - 001 : Enter a date that matches the yyyy-mm-dd pattern." +
          "\ncolumn - B, error - 002 : Must be a whole number and be less than 1,000,000."

      val result = Await.result(Future.sequence(resultFuture), Duration.Inf)
      result.size mustBe 1
      result.head.swap.map { (error: ErsException) =>
        error         mustBe a[FileValidationException]
        error.message mustBe errorMessage
        error.context mustBe context
      }
    }

    "return a user validation error when an error occurs during the file processing" in {
      val callback = UpscanCsvFileData(
        List(UpscanCallback("NOT A VALID SHEET", "no", noOfRows = Some(1))),
        schemeInfo
      )
      val data     =
        "2015-09-23,250,123.12,12.1234,12.1234,no,yes,AB12345678,no\n" +
          "2015-09-23,250,123.12,12.1234,12.1234,no,yes,AB12345678,no\n" +
          "2015-09-23,250,123.12,12.1234,12.1234,no,yes,AB12345678,no"

      val resultFuture = processCsvService().processFiles(callback, schemeInfo, returnStubSource(_, data))
      val boolList     = Await.result(Future.sequence(resultFuture), Duration.Inf)

      val error = boolList.head.left.value
      error         mustBe a[models.ErsSystemError]
      error.message mustBe "Sheet name: NOT A VALID SHEET not found for selected scheme version: V4"
      error.context mustBe "Error processing CSV file: NOT A VALID SHEET"
      assert(boolList.forall(_.isLeft))
    }

    "return the original UserValidationException when the stream produces a Left(UserValidationException)" in {
      val userValidationError = FileValidationException("user error message", "user error context")
      val callback            = UpscanCsvFileData(
        List(UpscanCallback("CSOP_OptionsGranted_V4.csv", "no", noOfRows = Some(1))),
        schemeInfo
      )

      val resultFuture = processCsvService(
        extractBodyOfRequestOverride = Some(_ => Source.single(Left(userValidationError)))
      ).processFiles(callback, schemeInfo, returnStubSource(_, ""))

      val result = Await.result(Future.sequence(resultFuture), Duration.Inf)

      result.head.isLeft     mustBe true
      result.head.left.value mustBe userValidationError
      result.head.left.value mustBe a[FileValidationException]
    }

    "return an ErsSystemError when the stream produces a Left(Throwable) that is not a UserValidationException" in {
      val exception = new RuntimeException("unexpected processing error")
      val callback  = UpscanCsvFileData(
        List(UpscanCallback("CSOP_OptionsGranted_V4.csv", "no", noOfRows = Some(1))),
        schemeInfo
      )

      val resultFuture = processCsvService(
        extractBodyOfRequestOverride = Some(_ => Source.single(Left(exception)))
      ).processFiles(callback, schemeInfo, returnStubSource(_, ""))

      val result = Await.result(Future.sequence(resultFuture), Duration.Inf)

      result.head.isLeft             mustBe true
      result.head.left.value         mustBe a[ErsSystemError]
      result.head.left.value.message mustBe "unexpected processing error"
      result.head.left.value.context mustBe "Unexpected error processing CSV file: CSOP_OptionsGranted_V4.csv"
    }

    "return a FileValidationException when the last row is a Right with non-empty validationErrors" in {
      val invalidRow: List[ByteString] = List(
        ByteString("bad-date"), // invalid date -> triggers validation error
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

      val resultFuture = processCsvService(
        extractBodyOfRequestOverride = Some(_ => Source.single(Right(invalidRow)))
      ).processFiles(callback, schemeInfo, returnStubSource(_, ""))

      val result = Await.result(Future.sequence(resultFuture), Duration.Inf)

      result.head.isLeft             mustBe true
      result.head.left.value         mustBe a[FileValidationException]
      result.head.left.value.message mustBe "Found validation errors in CSV"
      result.head.left.value.context mustBe
        "Error processing CSV file: CSOP_OptionsGranted_V4.csv, errors: column - A, " +
        "error - 001 : Enter a date that matches the yyyy-mm-dd pattern"
    }

  }

  "extractEntityData" should {
    "extract the data from an HttpResponse" in {
      val response     = HttpResponse(entity = "Test response body")
      val resultFuture = processCsvService().extractEntityData(response).runWith(Sink.head)
      val result       = Await.result(resultFuture, Duration.Inf)
      result.utf8String mustBe "Test response body"
    }

    "return a failed source with an ErsFileProcessingException if response status is not Ok (200)" in {
      val response     = HttpResponse(status = StatusCodes.InternalServerError)
      val resultFuture = processCsvService().extractEntityData(response).runWith(Sink.head)
      assert(resultFuture.failed.futureValue.getMessage.contains("Failed to stream the data from file"))
      assert(resultFuture.failed.futureValue.isInstanceOf[ErsFileProcessingException])
    }
  }

  "extractSchemeData" should {
    "pass on a Left if given a Left" in {
      val userError = ErsSystemError("hello there", "context")
      val result    = processCsvService().extractSchemeData(schemeInfo, "anEmpRef", Left(userError))
      assert(result.futureValue.isLeft)
      result.futureValue.left.value mustBe ErsSystemError("hello there", "context")
    }

    "return system error if sendSchemeCsv finds errors" in {
      val testService  = processCsvService(
        sendSchemeCsvNewOverride = Some(Future.successful(Some(new Exception("this was bad"))))
      )
      val futureResult = testService.extractSchemeData(
        schemeInfo,
        "anEmpRef",
        Right(
          CsvFileSubmissions("sheetName", 1, UpscanCallback("CSOP_OptionsGranted_V4.csv", "no", noOfRows = Some(1)))
        )
      )

      val result = Await.result(futureResult, Duration.Inf)
      result.isLeft     mustBe true
      result.left.value mustBe a[ErsFileProcessingException]
      val error = result.left.value.asInstanceOf[ErsFileProcessingException]
      error.message mustBe "this was bad"
      error.context mustBe "Error during CSV submission processing"
    }

    "return a Right if sendSchemeCsv is happy" in {
      val testService = processCsvService(
        sendSchemeCsvNewOverride = Some(Future.successful(None))
      )
      val result      = Await.result(
        testService.extractSchemeData(
          schemeInfo,
          "anEmpRef",
          Right(CsvFileSubmissions("sheetName", 1, UpscanCallback("CSOP_OptionsGranted_V4.csv", "no")))
        ),
        Duration.Inf
      )
      assert(result.isRight)
      result.value mustBe CsvFileLengthInfo(1, 1)
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

      val result = Await.result(processCsvService().sendSchemeCsv(submissionsSchemeData, "empRef"), Duration.Inf)
      assert(result.isEmpty)
    }

    "return a throwable if the submission was not sent successfully" in {
      val returnException = new Exception("this failed")
      when(
        mockErsFileValidatorConnector
          .sendToSubmissionsNew(any[SubmissionsSchemeData], any[String])(any[HeaderCarrier])
      ).thenReturn(Future.successful(Left(returnException)))

      doNothing()
        .when(mockAuditEvents)
        .auditRunTimeError(any[Throwable], any[String], any[SchemeInfo], any[String])(any[HeaderCarrier])

      val result = Await.result(processCsvService().sendSchemeCsv(submissionsSchemeData, "empRef"), Duration.Inf)
      assert(result.isDefined)
      assert(result.get.isInstanceOf[ErsFileProcessingException])
      result.get.getMessage mustBe returnException.toString
    }
  }

  "stripExtension" should {

    "Remove the extension" in {
      processCsvService().stripExtension("test.csv") mustBe "test"
    }

    "Remove the last extension if the file name contains multiple ." in {
      processCsvService().stripExtension("test.somethingelse.csv") mustBe "test.somethingelse"
    }

    "Return the original string if there is no extension" in {
      processCsvService().stripExtension("test") mustBe "test"
    }

  }

}
