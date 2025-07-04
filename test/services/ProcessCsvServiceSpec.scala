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

package services

import com.typesafe.config.{Config, ConfigFactory}
import config.ApplicationConfig
import connectors.ERSFileValidatorConnector
import helpers.MockProcessCsvService
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
import org.scalatest.{EitherValues, OptionValues}
import org.scalatest.concurrent.{ScalaFutures, TimeLimits}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.Request
import services.audit.AuditEvents
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.services.validation.DataValidator
import uk.gov.hmrc.services.validation.models.{Cell, Row, ValidationError}
import utils.ErrorResponseMessages

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

  def testProcessCsvService: ProcessCsvService = new ProcessCsvService(mockAuditEvents, mockDataGenerator, mockAppConfig, mockErsFileValidatorConnector)

  class MockDataValidator(config: Config, returner: Option[List[ValidationError]]) extends DataValidator(config) {
    override def validateRow(row: Row): Option[List[ValidationError]] = returner
  }

  "processRow" should {
    "process a row and return the user validation error it contains" in {
      val sheetTest: SheetInfo = SheetInfo("schemeType", 1, "sheetName", "sheetTitle", "configFileName",
        (1 to 5).map(_.toString).toList)

      val testService = new MockProcessCsvService(
        mockAuditEvents, mockDataGenerator, mockAppConfig, mockErsFileValidatorConnector)(
        formatDataToValidate = Some(Seq("INVALIDROW", "With", "ManyValues"))
      )
      val dataValidator = new MockDataValidator(mock[Config], Some(List(ValidationError(Cell("A", 1, "value"), "ruleId", "errorId", "errorMessage"))))
      val result = testService.processRow(List(ByteString("INVALIDROW,With,ManyValues")),
        "Other_Grants_V4.csv", schemeInfo, dataValidator, sheetTest)

      result.isLeft mustBe true
      result.left.value mustBe a[RowValidationError]
      val error = result.left.value.asInstanceOf[RowValidationError]
      error.message mustBe s"${ErrorResponseMessages.dataParserFileInvalid}"
      error.context mustBe s"${ErrorResponseMessages.dataParserValidationFailure}"
    }

    "process a row and return the data if there are no errors" in {
      val sheetTest: SheetInfo = SheetInfo("schemeType", 1, "sheetName", "sheetTitle", "configFileName",
        (1 to 5).map(_.toString).toList)

      val testService = new MockProcessCsvService(
        mockAuditEvents, mockDataGenerator, mockAppConfig, mockErsFileValidatorConnector)(
        formatDataToValidate = Some(Seq("thisIsARow", "With", "ManyValues"))
      )
      val dataValidator = new MockDataValidator(mock[Config], None)
      val result = testService.processRow(List(ByteString("thisIsARow,With,ManyValues")),
        "CSOP_OptionsGranted_V4.csv", schemeInfo, dataValidator, sheetTest)

      assert(result.isRight)
      result.value mustBe List("thisIsARow", "With", "ManyValues")
    }

    "return Left when validateRow throws an exception" in {
      val testService = new MockProcessCsvService(
        mockAuditEvents, mockDataGenerator, mockAppConfig, mockErsFileValidatorConnector)(
        formatDataToValidate = Some(Seq("thisIsARow", "With", "ManyValues"))
      )
      val sheetTest: SheetInfo = SheetInfo("schemeType", 1, "sheetName", "sheetTitle", "configFileName",
        (1 to 5).map(_.toString).toList)

      val dataValidator = mock[DataValidator]
      when(dataValidator.validateRow(any())).thenThrow(new RuntimeException("this validation failed"))

      val result = testService.processRow(List(ByteString("INVALIDROW,With,ManyValues")),
        "Other_Grants_V4.csv", schemeInfo, dataValidator, sheetTest)

      result.isLeft mustBe true
      val error = result.swap.getOrElse(fail("Expected Left but got Right"))
      error mustBe a[RowValidationError]
      error.message mustBe "System error during validation"
      error.context mustBe "Validation failed: this validation failed"
    }
  }

  "processFiles" should {
    when(mockDataGenerator.getValidatorAndSheetInfo(any(), any[SchemeInfo])(any(), any()))
      .thenReturn(Right((new DataValidator(ConfigFactory.load.getConfig("ers-csop-granted-validation-config")), sheetTest)))

    def returnStubSource(x: String, data: String): Source[HttpResponse, NotUsed] = {
      Source.single(HttpResponse(entity = data))
    }

    "return Right if all rows are valid" in {
      val upscanCallback = UpscanCallback("CSOP_OptionsGranted_V4.csv", "no", noOfRows = Some(1))

      val callback = UpscanCsvFileData(List(upscanCallback), schemeInfo)
      val data = "2015-09-23,250,123.12,12.1234,12.1234,no,yes,AB12345678,no\n2015-09-23,250,123.12,12.1234,12.1234,no,yes,AB12345678,no\n2015-09-23,250,123.12,12.1234,12.1234,no,yes,AB12345678,no"

      val testService: ProcessCsvService = new MockProcessCsvService(
        mockAuditEvents, mockDataGenerator, mockAppConfig, mockErsFileValidatorConnector
      )(
        mockExtractBodyOfRequest = Some(_ => Source.single(Right(List(ByteString(data))))),
        processRow = Some(Right(Seq(data)))
      )

      when(mockDataGenerator.getValidatorAndSheetInfo(any(), any[SchemeInfo])(any(), any()))
        .thenReturn(Right((mock[DataValidator], sheetTest)))

      val resultFuture = testService.processFiles(callback, returnStubSource(_, data))

      val boolList = Await.result(Future.sequence(resultFuture), Duration.Inf)

      boolList mustBe List(Right(CsvFileSubmissions("CSOP_OptionsGranted_V4", 1, upscanCallback)))
      assert(boolList.forall(_.isRight))
    }

    "return a user validation error when file is empty" in {
      val testService: ProcessCsvService = new MockProcessCsvService(
        mockAuditEvents, mockDataGenerator, mockAppConfig, mockErsFileValidatorConnector
      )(
        mockExtractBodyOfRequest = Some(_ => Source.empty),
        processRow = Some(Right(Seq.empty))
      )
      val callback = UpscanCsvFileData(List(UpscanCallback("CSOP_OptionsGranted_V4.csv", "no", noOfRows = Some(1))), schemeInfo)
      val data = "2015-09-23,250,123.12,12.1234,12.1234,no,yes,AB12345678,no\n2015-09-23,250,123.12,12.1234,12.1234,no,yes,AB12345678,no\n2015-09-23,250,123.12,12.1234,12.1234,no,yes,AB12345678,no"

      val resultFuture = testService.processFiles(callback, returnStubSource(_, data))

      val boolList = Await.result(Future.sequence(resultFuture), Duration.Inf)

      assert(boolList.head.isLeft)
      boolList.head.left.value mustBe a[NoDataError]
      val error = boolList.head.left.value.asInstanceOf[NoDataError]
      error.message mustBe "The file that you chose doesn’t contain any data.<br/>You won’t be able to upload CSOP_OptionsGranted_V4.csv as part of your annual return."
      assert(boolList.forall(_.isLeft))
    }

    "return a user validation error when an error occurs during the file validation" in {
      val callback = UpscanCsvFileData(List(UpscanCallback("CSOP_OptionsGranted_V4.csv", "no", noOfRows = Some(1))), schemeInfo)
      val data = "2015-09-23,test,123.12,12.1234,12.1234,no,yes,AB12345678,no\n2015-09-23,250,123.12,12.1234,12.1234,no,yes,AB12345678,no\n2015-09-23,250,123.12,12.1234,12.1234,no,yes,AB12345678,no"

      val userError = RowValidationError("thisIsBad", "qualityContent", 1)
      val testService: ProcessCsvService = new MockProcessCsvService(
        mockAuditEvents, mockDataGenerator, mockAppConfig, mockErsFileValidatorConnector
      )(
        mockExtractBodyOfRequest = Some(_ => Source.single(Right(List(ByteString(data))))),
        processRow = Some(Left(userError))
      )
      when(mockDataGenerator.getValidatorAndSheetInfo(any(), any[SchemeInfo])(any(), any()))
        .thenReturn(Right((mock[DataValidator], sheetTest)))

      val resultFuture = testService.processFiles(callback, returnStubSource(_, data))

      val result = Await.result(Future.sequence(resultFuture), Duration.Inf)
      result mustBe List(Left(userError))
    }

    "return a user validation error when an error occurs during the file processing" in {
      val callback = UpscanCsvFileData(List(UpscanCallback("CSOP_OptionsGranted_V4.csv", "no", noOfRows = Some(1))), schemeInfo)
      val data = "2015-09-23,250,123.12,12.1234,12.1234,no,yes,AB12345678,no\n2015-09-23,250,123.12,12.1234,12.1234,no,yes,AB12345678,no\n2015-09-23,250,123.12,12.1234,12.1234,no,yes,AB12345678,no"
      when(mockDataGenerator.getValidatorAndSheetInfo(any(), any[SchemeInfo])(any(), any())).thenReturn(Left(ERSFileProcessingException(
        "ers.exceptions.dataParser.configFailure",
        "ers.exceptions.dataParser.validatorError"
      )))

      val resultFuture = testProcessCsvService.processFiles(callback, returnStubSource(_, data))

      val boolList = Await.result(Future.sequence(resultFuture), Duration.Inf)
      boolList.head match {
        case Left(userError: UnknownSheetError) =>
          userError.message mustBe s"${ErrorResponseMessages.dataParserIncorrectSheetName}"
        case _ => fail("Expected result to be a Left with UnknownSheetError")
      }
      assert(boolList.forall(_.isLeft))
    }

    "return UnknownSheetError when getValidatorAndSheetInfo returns ERSFileProcessingException" in {
      val callback = UpscanCsvFileData(List(UpscanCallback("CSOP_OptionsGranted_V4.csv", "no", noOfRows = Some(1))), schemeInfo)
      val data = "2015-09-23,250,123.12,12.1234,12.1234,no,yes,AB12345678,no"

      when(mockDataGenerator.getValidatorAndSheetInfo(any(), any[SchemeInfo])(any(), any()))
        .thenReturn(Left(ERSFileProcessingException("Config error", "Missing config")))

      val resultFuture = testProcessCsvService.processFiles(callback, returnStubSource(_, data))
      val result = Await.result(Future.sequence(resultFuture), Duration.Inf)

      result.head.isLeft mustBe true
      result.head.left.value mustBe a[UnknownSheetError]
      val error = result.head.left.value.asInstanceOf[UnknownSheetError]
      error.message mustBe s"${ErrorResponseMessages.dataParserIncorrectSheetName}"
      error.context mustBe s"${ErrorResponseMessages.dataParserUnidentifiableSheetNameContext}"
    }

    "return UnknownSheetError when getValidatorAndSheetInfo returns other throwable" in {
      val callback = UpscanCsvFileData(List(UpscanCallback("CSOP_OptionsGranted_V4.csv", "no", noOfRows = Some(1))), schemeInfo)
      val data = "2015-09-23,250,123.12,12.1234,12.1234,no,yes,AB12345678,no"

      when(mockDataGenerator.getValidatorAndSheetInfo(any(), any[SchemeInfo])(any(), any()))
        .thenReturn(Left(new RuntimeException("Some other error")))

      val resultFuture = testProcessCsvService.processFiles(callback, returnStubSource(_, data))
      val result = Await.result(Future.sequence(resultFuture), Duration.Inf)

      result.head.isLeft mustBe true
      result.head.left.value mustBe a[UnknownSheetError]
    }
  }

  "extractEntityData" should {

    "extract the data from an HttpResponse" in {
      val response = HttpResponse(entity = "Test response body")
      val resultFuture = testProcessCsvService.extractEntityData(response).runWith(Sink.head)

      val result = Await.result(resultFuture, Duration.Inf)
      result.utf8String mustBe "Test response body"
    }

    "return a failed source with an ERSFileProcessingException if response status is not Ok (200)" in {
      val response = HttpResponse(status = StatusCodes.InternalServerError)
      val resultFuture = testProcessCsvService.extractEntityData(response).runWith(Sink.head)

      assert(resultFuture.failed.futureValue.getMessage.contains("Failed to stream the data from file"))
      assert(resultFuture.failed.futureValue.isInstanceOf[ERSFileProcessingException])
    }
  }

  "extractBodyOfRequest" should {

    "extract the body from an Http response stream and read as csv" in {
      val source = Source
        .single(HttpResponse(entity = "0,1,2,3\n4,5"))

      val testService = new MockProcessCsvService(
        mockAuditEvents, mockDataGenerator, mockAppConfig, mockErsFileValidatorConnector)(
        extractEntityData = Some(Source.single(ByteString("0,1,2,3\n4,5")))
      )

      val resultFuture = testService.extractBodyOfRequest(source).runWith(Sink.seq)
      val result = Await.result(resultFuture, Duration.Inf)

      assert(result.head.isRight)
      result.head.value mustBe List(ByteString("0"), ByteString("1"), ByteString("2"), ByteString("3"))

      assert(result(1).isRight)
      result(1).value mustBe List(ByteString("4"), ByteString("5"))

      result.length mustBe 2
    }

    "return a left containing a throwable when an error occurs" in {
      val source = Source
        .single(HttpResponse(status = StatusCodes.InternalServerError))

      val testService = new MockProcessCsvService(
        mockAuditEvents, mockDataGenerator, mockAppConfig, mockErsFileValidatorConnector)(
        extractEntityData = Some(Source.failed(ERSFileProcessingException("a message!", "with contents!")))
      )

      val resultFuture = testService.extractBodyOfRequest(source).runWith(Sink.seq)
      val result = Await.result(resultFuture, Duration.Inf)
      result.head.isLeft mustBe true
      result.head.left.map { x =>
        assert(x.isInstanceOf[ERSFileProcessingException])
        assert(x.asInstanceOf[ERSFileProcessingException].getMessage.contains("a message!"))
      }
    }
  }

  "extractSchemeData" should {
    "pass on a Left if given a Left" in {
      val userError = RowValidationError("hello there", "context", 1)
      val result: Future[Either[UserValidationError, CsvFileLengthInfo]] = testProcessCsvService
        .extractSchemeData(schemeInfo, "anEmpRef", Left(userError))

      assert(result.futureValue.isLeft)
      result.futureValue.left.value mustBe userError
    }

    "throw system error if sendSchemeCsv finds errors" in {
      val testService = new MockProcessCsvService(
        mockAuditEvents, mockDataGenerator, mockAppConfig, mockErsFileValidatorConnector)(
        sendSchemeCsvNew = Some(Future.successful(Some(new Exception("this was bad")))))

      val futureResult = testService
        .extractSchemeData(schemeInfo, "anEmpRef", Right(CsvFileSubmissions("sheetName", 1, UpscanCallback("CSOP_OptionsGranted_V4.csv", "no", noOfRows = Some(1)))))

      val exception = intercept[Exception] {
        Await.result(futureResult, Duration.Inf)
      }
      exception.getMessage mustBe "this was bad"
    }

    "return a Right if sendSchemeCsv is happy" in {
      val testService = new MockProcessCsvService(
        mockAuditEvents, mockDataGenerator, mockAppConfig, mockErsFileValidatorConnector)(
        sendSchemeCsvNew = Some(Future.successful(None)))

      val result = Await.result(testService
        .extractSchemeData(schemeInfo, "anEmpRef",
          Right(CsvFileSubmissions("sheetName", 1, UpscanCallback("CSOP_OptionsGranted_V4.csv", "no")))),
        Duration.Inf
      )

      assert(result.isRight)
      result.value mustBe CsvFileLengthInfo(1, 1)

    }
  }


  "formatDataToValidate" should {

    "process the rowData" in {
      val sheetTest: SheetInfo = SheetInfo("schemeType", 1, "sheetName", "sheetTitle", "configFileName",
        (1 to 5).map(_.toString).toList)

      val testService = new MockProcessCsvService(
        mockAuditEvents, mockDataGenerator, mockAppConfig, mockErsFileValidatorConnector)()

      when(mockDataGenerator.getSheetCsv(any(), any())(any(), any())).thenReturn(Right(sheetTest))
      val result = testService.formatDataToValidate((11 to 20).map(_.toString), sheetTest)

      result mustBe (11 to 15).map(_.toString)
    }
  }


  "sendSchemeCsv New" should {
    "return a None if the submission was sent successfully" in {
      val testService = new MockProcessCsvService(
        mockAuditEvents, mockDataGenerator, mockAppConfig, mockErsFileValidatorConnector)(
      )
      when(mockErsFileValidatorConnector.sendToSubmissionsNew(any[SubmissionsSchemeData], any[String])(any[HeaderCarrier], any[Request[_]]))
        .thenReturn(Future.successful(Right(uk.gov.hmrc.http.HttpResponse(StatusCodes.OK.intValue, "aBody"))))
      when(mockAuditEvents.fileValidatorAudit(any[SchemeInfo], any[String])(any[HeaderCarrier], any[Request[_]]))
        .thenReturn(true)

      val result: Option[Throwable] = Await.result(
        testService.sendSchemeCsv(submissionsSchemeData, "empRef"),
        Duration.Inf
      )

      assert(result.isEmpty)
    }

    "return a throwable if the submission was not sent successfully" in {
      val testService = new MockProcessCsvService(
        mockAuditEvents, mockDataGenerator, mockAppConfig, mockErsFileValidatorConnector)(
      )
      val returnException = new Exception("this failed")
      when(mockErsFileValidatorConnector.sendToSubmissionsNew(any[SubmissionsSchemeData], any[String])(any[HeaderCarrier], any[Request[_]]))
        .thenReturn(Future.successful(Left(returnException)))
      doNothing().when(mockAuditEvents).auditRunTimeError(any[Throwable], any[String], any[SchemeInfo], any[String])(
        any[HeaderCarrier], any[Request[_]])

      val result: Option[Throwable] = Await.result(
        testService.sendSchemeCsv(submissionsSchemeData, "empRef"),
        Duration.Inf
      )

      assert(result.isDefined)
      assert(result.get.isInstanceOf[ERSFileProcessingException])
      result.get.getMessage mustBe returnException.toString
    }
  }


}


