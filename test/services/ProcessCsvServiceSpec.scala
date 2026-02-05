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

import com.typesafe.config.ConfigFactory
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
import org.eclipse.jetty.http2.generator.DataGenerator
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
import uk.gov.hmrc.validator.models.csv.RowValidationResults
import uk.gov.hmrc.validator.models.{Cell, ValidationError}
import uk.gov.hmrc.validator.{DataEngine, SheetInfo}

import java.time.ZonedDateTime
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class ProcessCsvServiceSpec extends TestKit(ActorSystem("Test")) with AnyWordSpecLike with Matchers with OptionValues with MockitoSugar with TimeLimits with ScalaFutures with EitherValues {

  val mockDataGenerator: DataGenerator = mock[DataGenerator]
  val mockAuditEvents: AuditEvents = mock[AuditEvents]
  val mockErsFileValidatorConnector: ERSFileValidatorConnector = mock[ERSFileValidatorConnector]
  val mockAppConfig: ApplicationConfig = mock[ApplicationConfig]
  when(mockAppConfig.uploadFileSizeLimit).thenReturn(10000)
  when(mockAppConfig.maxNumberOfRowsPerSubmission).thenReturn(10000)

  val sheetTest: SheetInfo = SheetInfo("schemeType", 1, "CSOP_OptionsGranted_V4", "sheetTitle", "configFileName", List("aHeader"))

  implicit val request: Request[_] = mock[Request[_]]
  implicit val hc: HeaderCarrier = mock[HeaderCarrier]

  val schemeInfo: SchemeInfo = SchemeInfo(
    schemeRef = "XA11999991234567",
    timestamp = ZonedDateTime.now,
    schemeId = "123PA12345678",
    taxYear = "2014/F15",
    schemeName = "MyScheme",
    schemeType = "EMI"
  )

  val submissionsSchemeData: SubmissionsSchemeData = SubmissionsSchemeData(schemeInfo, "sheetName",
    UpscanCallback("CSOP_OptionsGranted_V4.csv", "no", noOfRows = Some(11)), numberOfRows = 11)

  def processCsvService(extractBodyOfRequestAndValidateOverride: Option[Source[HttpResponse, _] => Source[Either[Throwable, RowValidationResults], _]] = None,
                        extractEntityDataOverride: Option[HttpResponse => Source[ByteString, _]] = None,
                        sendSchemeCsvNewOverride: Option[Future[Option[Throwable]]] = None): ProcessCsvService =
    new ProcessCsvService(mockAuditEvents, mockAppConfig, mockErsFileValidatorConnector) {
      override def extractBodyOfRequestAndValidate(csopV5Enabled: Boolean, sheetName: String): Source[HttpResponse, _] => Source[Either[Throwable, RowValidationResults], _] = { // TODO: COME BACK TO
        extractBodyOfRequestAndValidateOverride
          .getOrElse(super.extractBodyOfRequestAndValidate(csopV5Enabled, sheetName))
      }

      override def extractEntityData(response: HttpResponse): Source[ByteString, _] = {
        extractEntityDataOverride match {
          case Some(value: (HttpResponse => Source[ByteString, _])) => value(response)
          case None => super.extractEntityData(response)
        }
      }

      override def sendSchemeCsv(ersSchemeData: SubmissionsSchemeData, empRef: String)(implicit hc: HeaderCarrier, request: Request[_]): Future[Option[Throwable]] =
        sendSchemeCsvNewOverride.getOrElse(super.sendSchemeCsv(ersSchemeData, empRef))
    }

  "processFiles" should {

    def returnStubSource(x: String, data: String): Source[HttpResponse, NotUsed] = {
      Source.single(HttpResponse(entity = data))
    }

    "return Right if all rows are valid" in {
      when(mockAppConfig.csopV5Enabled).thenReturn(false)
      val upscanCallback = UpscanCallback("CSOP_OptionsGranted_V4", "no", noOfRows = Some(1))

      val callback = UpscanCsvFileData(List(upscanCallback), schemeInfo)
      val data = "2015-09-23,250,123.12,12.1234,12.1234,no,yes,AB12345678,no\n2015-09-23,250,123.12,12.1234,12.1234,no,yes,AB12345678,no\n2015-09-23,250,123.12,12.1234,12.1234,no,yes,AB12345678,no"

      val resultFuture = processCsvService().processFiles(callback, returnStubSource(_, data))

      val boolList: Seq[Either[ErsError, CsvFileSubmissions]] = Await.result(Future.sequence(resultFuture), Duration.Inf)

      boolList.head.swap.foreach(t => println(s"boolList.head.swap: ${t.context}"))
      boolList mustBe List(Right(CsvFileSubmissions("CSOP_OptionsGranted_V4", 3, upscanCallback)))
      assert(boolList.forall(_.isRight))
    }

    "treat file extension as case-insensitive when processing a valid file" in {
      val upscanCallback = UpscanCallback("CSOP_OptionsGranted_V4.CSV", "no", noOfRows = Some(1))

      val callback = UpscanCsvFileData(List(upscanCallback), schemeInfo)
      val data = "2015-09-23,250,123.12,12.1234,12.1234,no,yes,AB12345678,no"

      val resultFuture = processCsvService().processFiles(callback, returnStubSource(_, data))

      val boolList = Await.result(Future.sequence(resultFuture), Duration.Inf)

      boolList mustBe List(Right(CsvFileSubmissions("CSOP_OptionsGranted_V4", 1, upscanCallback)))
      assert(boolList.forall(_.isRight))
    }

    "return a user validation error when file is empty" in {
      val callback = UpscanCsvFileData(List(UpscanCallback("CSOP_OptionsGranted_V4.csv", "no", noOfRows = Some(1))), schemeInfo)
      val data = "2015-09-23,250,123.12,12.1234,12.1234,no,yes,AB12345678,no\n2015-09-23,250,123.12,12.1234,12.1234,no,yes,AB12345678,no\n2015-09-23,250,123.12,12.1234,12.1234,no,yes,AB12345678,no"

      val resultFuture = processCsvService(
        extractBodyOfRequestAndValidateOverride = Some(_ => Source.empty)
      ).processFiles(callback, returnStubSource(_, data))

      val boolList = Await.result(Future.sequence(resultFuture), Duration.Inf)

      assert(boolList.head.isLeft)
      boolList.head.left.value mustBe a[NoDataError]
      val error = boolList.head.left.value.asInstanceOf[NoDataError]
      error.message mustBe "The file that you chose doesn’t contain any data.<br/>You won’t be able to upload CSOP_OptionsGranted_V4.csv as part of your annual return."
      assert(boolList.forall(_.isLeft))
    }

    "return a user validation error when an error occurs during the file validation" in {
      val callback = UpscanCsvFileData(List(UpscanCallback("CSOP_OptionsGranted_V4.csv", "no", noOfRows = Some(3))), schemeInfo)
      val data = "2015-09-23,250,123.12,12.1234,12.1234,no,yes,AB12345678,no\n2015-09-  23,test,123.12,12.1234,12.1234,no,yes,AB12345678,no\n2015-09-23,250,123.12,12.1234,12.1234,no,yes,AB12345678,no"

      val resultFuture = processCsvService().processFiles(callback, returnStubSource(_, data))
      val errorMessage = "[ProcessCSVService][processFiles]: Found validation errors in CSV"
      val context = "Error processing CSV file: CSOP_OptionsGranted_V4.csv, errors: column - A, error - 001 : Enter a date that matches the yyyy-mm-dd pattern." +
        "\ncolumn - B, error - 002 : Must be a whole number and be less than 1,000,000."

      val result: Seq[Either[ErsError, CsvFileSubmissions]] = Await.result(Future.sequence(resultFuture), Duration.Inf)
      result.size mustBe 1
      result.head.swap.map { (error: ErsError) =>
        error mustBe a[RowValidationError]
        error.message mustBe errorMessage
        error.context mustBe context
      }
    }

    "return a user validation error when an error occurs during the file processing" in {
      val callback = UpscanCsvFileData(List(UpscanCallback("NOT A VALID SHEET", "no", noOfRows = Some(1))), schemeInfo)
      val data = "2015-09-23,250,123.12,12.1234,12.1234,no,yes,AB12345678,no\n2015-09-23,250,123.12,12.1234,12.1234,no,yes,AB12345678,no\n2015-09-23,250,123.12,12.1234,12.1234,no,yes,AB12345678,no"

      val resultFuture = processCsvService().processFiles(callback, returnStubSource(_, data))

      val boolList = Await.result(Future.sequence(resultFuture), Duration.Inf)
      val error = boolList.head.left.value

      error mustBe a[models.ErsSystemError]
      error.message mustBe "Failed to set validator"
      error.context mustBe "Error processing CSV file: NOT A VALID SHEET"
      assert(boolList.forall(_.isLeft))
    }
  }

  "extractEntityData" should {

    "extract the data from an HttpResponse" in {
      val response = HttpResponse(entity = "Test response body")
      val resultFuture = processCsvService().extractEntityData(response).runWith(Sink.head)

      val result = Await.result(resultFuture, Duration.Inf)
      result.utf8String mustBe "Test response body"
    }

    "return a failed source with an ERSFileProcessingException if response status is not Ok (200)" in {
      val response = HttpResponse(status = StatusCodes.InternalServerError)
      val resultFuture = processCsvService().extractEntityData(response).runWith(Sink.head)

      assert(resultFuture.failed.futureValue.getMessage.contains("Failed to stream the data from file"))
      assert(resultFuture.failed.futureValue.isInstanceOf[ERSFileProcessingException])
    }
  }

  "extractBodyOfRequestAndValidate" should {

    "extract the body from an Http response stream and validate each row of CSV" in {
      val source: Source[HttpResponse, NotUsed] = Source
        .single(
          HttpResponse(
            entity = "2015-09-23,250,123.12,12.1234,12.1234,no,yes,AB12345678,no\n" +
              "2015-09-23,250,123.12,12.1234,12.1234,no,yes,AB12345678,no\n" +
              "2015-09-23,250,123.12,12.1234,12.1234,no,yes,AB12345678,no"
          )
        )

      val resultFuture: Future[Seq[Either[Throwable, RowValidationResults]]] =
        processCsvService()
          .extractBodyOfRequestAndValidate(csopV5Enabled = false, sheetName = "CSOP_OptionsGranted_V4")(source)
          .runWith(Sink.seq)
      val result: Seq[Either[Throwable, RowValidationResults]] = Await.result(resultFuture, Duration.Inf)
      val expectedResult = List(
        Right(RowValidationResults(validationErrors = List(), rowWasEmpty = false)),
        Right(RowValidationResults(validationErrors = List(), rowWasEmpty = false)),
        Right(RowValidationResults(validationErrors = List(), rowWasEmpty = false))
      )
      result must contain theSameElementsAs expectedResult
    }

    val validRow = Right(RowValidationResults(validationErrors = List(), rowWasEmpty = false))
    val dataRow = "2015-09-23,250,123.12,12.1234,12.1234,no,yes,AB12345678,no"
    val invalidRow = Right(RowValidationResults(validationErrors = List(ValidationError(Cell("A", 0, "2015  -09-23"), "error.1", "001", "Enter a date that matches the yyyy-mm-dd pattern.")), rowWasEmpty = false))
    val invalidDataRow = "2015  -09-23,250,123.12,12.1234,12.1234,no,yes,AB12345678,no"

    val validationTestCases: Seq[(String, String, Seq[Either[Throwable, RowValidationResults]])] = Seq(
      ("1 valid row", dataRow, Seq(validRow)),
      ("2 valid rows", Seq.fill(2)(dataRow).mkString("\n"), Seq.fill(2)(validRow)),
      ("100 valid rows", Seq.fill(100)(dataRow).mkString("\n"), Seq.fill(100)(validRow)),
      ("1 invalid row", invalidDataRow, Seq(invalidRow)),
      ("1 valid row, 1 invalid row, 1 valid row", Seq(dataRow, invalidDataRow, dataRow).mkString("\n"), Seq(validRow, invalidRow)),
      ("1 invalid row, 1 valid rows", Seq(invalidDataRow, dataRow).mkString("\n"), Seq(invalidRow)),
      ("100 invalid rows", Seq.fill(100)(invalidDataRow).mkString("\n"), Seq(invalidRow))
    )

    validationTestCases.foreach {
      case (testDescription: String, sourceData: String, validationResult: Seq[Either[Throwable, RowValidationResults]]) =>
        s"when parsed $testDescription generate the correct validation result" in {
          val source: Source[HttpResponse, NotUsed] = Source.single(HttpResponse(entity = sourceData))

          val resultFuture: Future[Seq[Either[Throwable, RowValidationResults]]] =
            processCsvService()
              .extractBodyOfRequestAndValidate(csopV5Enabled = false, sheetName = "CSOP_OptionsGranted_V4")(source)
              .runWith(Sink.seq)

          val result: Seq[Either[Throwable, RowValidationResults]] = Await.result(resultFuture, Duration.Inf)

          result must contain theSameElementsAs validationResult
        }

    }

    "return a left containing a throwable when an error occurs" in {
      val source = Source
        .single(HttpResponse(status = StatusCodes.InternalServerError))

      val testService: ProcessCsvService = processCsvService(
        extractEntityDataOverride = Some(_ => Source.failed(ERSFileProcessingException("a message!", "with contents!")))
      )

      val dataValidator: DataEngine = new DataEngine(ConfigFactory.load.getConfig("ers-csop-granted-validation-config"))

      val resultFuture = testService
        .extractBodyOfRequestAndValidate(csopV5Enabled = true, sheetName = "CSOP_OptionsGranted_V4")(source)
        .runWith(Sink.seq)

      val result: Seq[Either[Throwable, RowValidationResults]] = Await.result(resultFuture, Duration.Inf)

      result.head.isLeft mustBe true
      result.head.left.map { x =>
        assert(x.isInstanceOf[ERSFileProcessingException])
        assert(x.asInstanceOf[ERSFileProcessingException].getMessage.contains("a message!"))
      }
    }
  }

  "extractSchemeData" should {
    "pass on a Left if given a Left" in {
      val userError = ErsSystemError("hello there", "context")
      val result: Future[Either[ErsError, CsvFileLengthInfo]] = processCsvService()
        .extractSchemeData(schemeInfo, "anEmpRef", Left(userError))

      assert(result.futureValue.isLeft)
      result.futureValue.left.value mustBe ErsSystemError("hello there", "[ProcessCsvService][extractSchemeData]: Error processing CSV file")
    }

    "return system error if sendSchemeCsv finds errors" in {
      val testService = processCsvService(
        sendSchemeCsvNewOverride = Some(Future.successful(Some(new Exception("this was bad"))))
      )

      val futureResult = testService
        .extractSchemeData(schemeInfo, "anEmpRef", Right(CsvFileSubmissions("sheetName", 1, UpscanCallback("CSOP_OptionsGranted_V4.csv", "no", noOfRows = Some(1)))))

      val result = Await.result(futureResult, Duration.Inf)

      result.isLeft mustBe true
      result.left.value mustBe a[ERSFileProcessingException]
      val error = result.left.value.asInstanceOf[ERSFileProcessingException]
      error.message mustBe "this was bad"
      error.context mustBe "Error during CSV submission processing"
    }

    "return a Right if sendSchemeCsv is happy" in {
      val testService = processCsvService(
        sendSchemeCsvNewOverride = Some(Future.successful(None))
      )

      val result = Await.result(testService
        .extractSchemeData(schemeInfo, "anEmpRef",
          Right(CsvFileSubmissions("sheetName", 1, UpscanCallback("CSOP_OptionsGranted_V4.csv", "no")))),
        Duration.Inf
      )

      assert(result.isRight)
      result.value mustBe CsvFileLengthInfo(1, 1)

    }
  }

  "sendSchemeCsv New" should {
    "return a None if the submission was sent successfully" in {
      when(mockErsFileValidatorConnector.sendToSubmissionsNew(any[SubmissionsSchemeData], any[String])(any[HeaderCarrier], any[Request[_]]))
        .thenReturn(Future.successful(Right(uk.gov.hmrc.http.HttpResponse(StatusCodes.OK.intValue, "aBody"))))
      when(mockAuditEvents.fileValidatorAudit(any[SchemeInfo], any[String])(any[HeaderCarrier], any[Request[_]]))
        .thenReturn(true)

      val result: Option[Throwable] = Await.result(
        processCsvService().sendSchemeCsv(submissionsSchemeData, "empRef"),
        Duration.Inf
      )

      assert(result.isEmpty)
    }

    "return a throwable if the submission was not sent successfully" in {
      val returnException = new Exception("this failed")
      when(mockErsFileValidatorConnector.sendToSubmissionsNew(any[SubmissionsSchemeData], any[String])(any[HeaderCarrier], any[Request[_]]))
        .thenReturn(Future.successful(Left(returnException)))
      doNothing().when(mockAuditEvents).auditRunTimeError(any[Throwable], any[String], any[SchemeInfo], any[String])(
        any[HeaderCarrier], any[Request[_]])

      val result: Option[Throwable] = Await.result(
        processCsvService().sendSchemeCsv(submissionsSchemeData, "empRef"),
        Duration.Inf
      )

      assert(result.isDefined)
      assert(result.get.isInstanceOf[ERSFileProcessingException])
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
