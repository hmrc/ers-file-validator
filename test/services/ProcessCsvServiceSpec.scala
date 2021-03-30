/*
 * Copyright 2021 HM Revenue & Customs
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

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.stream.scaladsl.{Sink, Source}
import akka.testkit.TestKit
import akka.util.ByteString
import com.typesafe.config.{Config, ConfigFactory}
import config.ApplicationConfig
import connectors.ERSFileValidatorConnector
import helpers.MockProcessCsvService
import models.upscan.{UpscanCallback, UpscanCsvFileData}
import models.{CsvFileContents, CsvFileSubmissions, ERSFileProcessingException, SchemeData, SchemeInfo, SubmissionsSchemeData}
import org.joda.time.DateTime
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.MustMatchers.convertToAnyMustWrapper
import org.scalatest.concurrent.{ScalaFutures, TimeLimits}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.mvc.Request
import services.audit.AuditEvents
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.services.validation.DataValidator
import uk.gov.hmrc.services.validation.models.{Cell, Row, ValidationError}
import utils.ErrorResponseMessages

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class ProcessCsvServiceSpec extends TestKit(ActorSystem("Test")) with UnitSpec with MockitoSugar with GuiceOneAppPerSuite with TimeLimits with ScalaFutures {

  val mockDataGenerator: DataGenerator = mock[DataGenerator]
  val mockAuditEvents: AuditEvents = mock[AuditEvents]
  val mockErsFileValidatorConnector: ERSFileValidatorConnector = mock[ERSFileValidatorConnector]
  val mockAppConfig: ApplicationConfig = mock[ApplicationConfig]
  when(mockAppConfig.uploadCsvSizeLimit).thenReturn(10000)

  val sheetTest: SheetInfo = SheetInfo("schemeType", 1, "CSOP_OptionsGranted_V3", "sheetTitle", "configFileName", List("aHeader"))

  implicit val request: Request[_] = mock[Request[_]]
  implicit val hc: HeaderCarrier = mock[HeaderCarrier]

  val schemeInfo: SchemeInfo = SchemeInfo(
    schemeRef = "XA11999991234567",
    timestamp = DateTime.now,
    schemeId = "123PA12345678",
    taxYear = "2014/F15",
    schemeName = "MyScheme",
    schemeType = "EMI"
  )

  val submissionsSchemeData: SubmissionsSchemeData = SubmissionsSchemeData(schemeInfo, "sheetName",
    UpscanCallback("CSOP_OptionsGranted_V3.csv", "no", noOfRows = Some(11)), numberOfRows = 11)

  def testProcessCsvService: ProcessCsvService = new ProcessCsvService(mockAuditEvents, mockDataGenerator, mockAppConfig, mockErsFileValidatorConnector)

  class MockDataValidator(config: Config, returner: Option[List[ValidationError]]) extends DataValidator(config) {
    override def validateRow(row: Row): Option[List[ValidationError]] = returner
  }

  "processRow" should {
    "process a row and return the errors it contains" in {
      val sheetTest: SheetInfo = SheetInfo("schemeType", 1, "sheetName", "sheetTitle", "configFileName",
        (1 to 5).map(_.toString).toList)

      val testService = new MockProcessCsvService(
        mockAuditEvents, mockDataGenerator, mockAppConfig, mockErsFileValidatorConnector)(
        formatDataToValidate = Some(Seq("INVALIDROW", "With", "ManyValues"))
      )
      val dataValidator = new MockDataValidator(mock[Config], Some(List(ValidationError(Cell("A", 1, "value"), "ruleId", "errorId", "errorMessage"))))
      val result = testService.processRow(List(ByteString("INVALIDROW,With,ManyValues")),
        "Other_Grants_V3.csv", schemeInfo, dataValidator, sheetTest)

      result.left.get mustBe ERSFileProcessingException(
        s"${ErrorResponseMessages.dataParserFileInvalid}",
        s"${ErrorResponseMessages.dataParserValidationFailure}")
      assert(result.isLeft)
    }

    "process a row and return an empty list if there are no errors" in {
      val sheetTest: SheetInfo = SheetInfo("schemeType", 1, "sheetName", "sheetTitle", "configFileName",
        (1 to 5).map(_.toString).toList)

      val testService = new MockProcessCsvService(
        mockAuditEvents, mockDataGenerator, mockAppConfig, mockErsFileValidatorConnector)(
        formatDataToValidate = Some(Seq("thisIsARow", "With", "ManyValues"))
      )
      val dataValidator = new MockDataValidator(mock[Config], None)
      val result = testService.processRow(List(ByteString("thisIsARow,With,ManyValues")),
        "CSOP_OptionsGranted_V3.csv", schemeInfo, dataValidator, sheetTest)

      assert(result.isRight)
      result.right.get mustBe List("thisIsARow", "With", "ManyValues")
    }

    "return throwable if validateRow returned a throwable" in {
      val testService = new MockProcessCsvService(
        mockAuditEvents, mockDataGenerator, mockAppConfig, mockErsFileValidatorConnector)(
        formatDataToValidate = Some(Seq("thisIsARow", "With", "ManyValues"))
      )
      val sheetTest: SheetInfo = SheetInfo("schemeType", 1, "sheetName", "sheetTitle", "configFileName",
        (1 to 5).map(_.toString).toList)

      val dataValidator = mock[DataValidator]
      when(dataValidator.validateRow(any())).thenThrow(new RuntimeException("this validation failed"))
      val result = testService.processRow(List(ByteString("INVALIDROW,With,ManyValues")),
        "Other_Grants_V3.csv", schemeInfo, dataValidator, sheetTest)

      assert(result.isLeft)
      result.left.get.getMessage mustBe "this validation failed"
    }
  }

  "processFiles" should {
    when(mockDataGenerator.getValidatorAndSheetInfo(any(), any[SchemeInfo])(any(), any()))
      .thenReturn(Right((new DataValidator(ConfigFactory.load.getConfig("ers-csop-granted-validation-config")), sheetTest)))

    def returnStubSource(x: String, data: String): Source[HttpResponse, NotUsed] = {
      Source.single(HttpResponse(entity = data))
    }

    "return true if all rows are valid" in {
      val callback = UpscanCsvFileData(List(UpscanCallback("CSOP_OptionsGranted_V3.csv", "no", noOfRows = Some(1))), schemeInfo)
      val data = "2015-09-23,250,123.12,12.1234,12.1234,no,yes,AB12345678,no\n2015-09-23,250,123.12,12.1234,12.1234,no,yes,AB12345678,no\n2015-09-23,250,123.12,12.1234,12.1234,no,yes,AB12345678,no"

      val testService: ProcessCsvService = new MockProcessCsvService(
        mockAuditEvents, mockDataGenerator, mockAppConfig, mockErsFileValidatorConnector
      )(
        mockExtractBodyOfRequest = Some(_ => Source.single(Right(List(ByteString(data))))),
        processRow = Some(Right(Seq(data)))
      )

      val resultFuture = testService.processFiles(callback, returnStubSource(_, data))

      val boolList = Await.result(Future.sequence(resultFuture), Duration.Inf)

      boolList mustBe List(Right(CsvFileContents("CSOP_OptionsGranted_V3", Seq(Seq(data)))))
      assert(boolList.forall(_.isRight))
    }
    "return a throwable when an error occurs during the file validation" in {
      val callback = UpscanCsvFileData(List(UpscanCallback("CSOP_OptionsGranted_V3.csv", "no", noOfRows = Some(1))), schemeInfo)
      val data = "2015-09-23,test,123.12,12.1234,12.1234,no,yes,AB12345678,no\n2015-09-23,250,123.12,12.1234,12.1234,no,yes,AB12345678,no\n2015-09-23,250,123.12,12.1234,12.1234,no,yes,AB12345678,no"

      val testService: ProcessCsvService = new MockProcessCsvService(
        mockAuditEvents, mockDataGenerator, mockAppConfig, mockErsFileValidatorConnector
      )(
        mockExtractBodyOfRequest = Some(_ => Source.single(Right(List(ByteString(data))))),
        processRow = Some(Left(ERSFileProcessingException("thisIsBad", "qualityContent")))
      )

      val resultFuture = testService.processFiles(callback, returnStubSource(_, data))

      val result = Await.result(Future.sequence(resultFuture), Duration.Inf)
      result mustBe List(Left(ERSFileProcessingException("thisIsBad", "qualityContent")))
    }

    "return a throwable when an error occurs during the file processing" in {
      val callback = UpscanCsvFileData(List(UpscanCallback("CSOP_OptionsGranted_V3.csv", "no", noOfRows = Some(1))), schemeInfo)
      val data = "2015-09-23,250,123.12,12.1234,12.1234,no,yes,AB12345678,no\n2015-09-23,250,123.12,12.1234,12.1234,no,yes,AB12345678,no\n2015-09-23,250,123.12,12.1234,12.1234,no,yes,AB12345678,no"
      when(mockDataGenerator.getValidatorAndSheetInfo(any(), any[SchemeInfo])(any(), any())).thenReturn(Left(ERSFileProcessingException(
        "ers.exceptions.dataParser.configFailure",
        "ers.exceptions.dataParser.validatorError",
      )))

      val resultFuture = testProcessCsvService.processFiles(callback, returnStubSource(_, data))

      val boolList = Await.result(Future.sequence(resultFuture), Duration.Inf)
      boolList.head match {
        case Left(ex) => ex.getMessage mustBe "ers.exceptions.dataParser.configFailure"
        case Right(_) => fail("Expected result to be a Left")
      }
      assert(boolList.forall(_.isLeft))
    }

  }

  "processFilesNew" should {

    def returnStubSource(x: String, data: String): Source[HttpResponse, NotUsed] = {
      Source.single(HttpResponse(entity = data))
    }

    "return true if all rows are valid" in {
      val upscanCallback = UpscanCallback("CSOP_OptionsGranted_V3.csv", "no", noOfRows = Some(1))

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

      val resultFuture = testService.processFilesNew(callback, returnStubSource(_, data))

      val boolList = Await.result(Future.sequence(resultFuture), Duration.Inf)

      boolList mustBe List(Right(CsvFileSubmissions("CSOP_OptionsGranted_V3", 1, upscanCallback)))
      assert(boolList.forall(_.isRight))
    }

    "return a throwable when an error occurs during the file validation" in {
      val callback = UpscanCsvFileData(List(UpscanCallback("CSOP_OptionsGranted_V3.csv", "no", noOfRows = Some(1))), schemeInfo)
      val data = "2015-09-23,test,123.12,12.1234,12.1234,no,yes,AB12345678,no\n2015-09-23,250,123.12,12.1234,12.1234,no,yes,AB12345678,no\n2015-09-23,250,123.12,12.1234,12.1234,no,yes,AB12345678,no"

      val testService: ProcessCsvService = new MockProcessCsvService(
        mockAuditEvents, mockDataGenerator, mockAppConfig, mockErsFileValidatorConnector
      )(
        mockExtractBodyOfRequest = Some(_ => Source.single(Right(List(ByteString(data))))),
        processRow = Some(Left(ERSFileProcessingException("thisIsBad", "qualityContent")))
      )
      when(mockDataGenerator.getValidatorAndSheetInfo(any(), any[SchemeInfo])(any(), any()))
        .thenReturn(Right((mock[DataValidator], sheetTest)))

      val resultFuture = testService.processFilesNew(callback, returnStubSource(_, data))

      val result = Await.result(Future.sequence(resultFuture), Duration.Inf)
      result mustBe List(Left(ERSFileProcessingException("thisIsBad", "qualityContent")))
    }

    "return a throwable when an error occurs during the file processing" in {
      val callback = UpscanCsvFileData(List(UpscanCallback("CSOP_OptionsGranted_V3.csv", "no", noOfRows = Some(1))), schemeInfo)
      val data = "2015-09-23,250,123.12,12.1234,12.1234,no,yes,AB12345678,no\n2015-09-23,250,123.12,12.1234,12.1234,no,yes,AB12345678,no\n2015-09-23,250,123.12,12.1234,12.1234,no,yes,AB12345678,no"
      when(mockDataGenerator.getValidatorAndSheetInfo(any(), any[SchemeInfo])(any(), any())).thenReturn(Left(ERSFileProcessingException(
        "ers.exceptions.dataParser.configFailure",
        "ers.exceptions.dataParser.validatorError",
      )))

      val resultFuture = testProcessCsvService.processFilesNew(callback, returnStubSource(_, data))

      val boolList = Await.result(Future.sequence(resultFuture), Duration.Inf)
      boolList.head match {
        case Left(ex) => ex.getMessage mustBe "ers.exceptions.dataParser.configFailure"
        case Right(_) => fail("Expected result to be a Left")
      }
      assert(boolList.forall(_.isLeft))
    }
  }

  "extractEntityData" should {

    "extract the data from an HttpResponse" in {
      val response = HttpResponse(entity = "Test response body")
      val resultFuture = testProcessCsvService.extractEntityData(response).runWith(Sink.head)

      val result = Await.result(resultFuture, Duration.Inf)
      result.utf8String mustBe "Test response body"
    }

    "return a failed source with an UpstreamErrorResponse if response status is not Ok (200)" in {
      val response = HttpResponse(status = StatusCodes.InternalServerError)
      val resultFuture = testProcessCsvService.extractEntityData(response).runWith(Sink.head)

      assert(resultFuture.failed.futureValue.getMessage.contains("Failed to stream the data from file"))
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
      result.head.right.get mustBe List(ByteString("0"), ByteString("1"), ByteString("2"), ByteString("3"))

      assert(result(1).isRight)
      result(1).right.get mustBe List(ByteString("4"), ByteString("5"))

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
      val result: Future[Either[Throwable, (Int, Int)]] = testProcessCsvService
        .extractSchemeData(schemeInfo, "anEmpRef", Left(new Exception("hello there")))

      assert(result.isLeft)
      result.left.get.getMessage mustBe "hello there"
    }

    "return a Left if sendSchemeCsv finds errors" in {
      val testService = new MockProcessCsvService(
        mockAuditEvents, mockDataGenerator, mockAppConfig, mockErsFileValidatorConnector)(
        sendSchemeCsv = Some(Future.successful(Left(new Exception("this was bad")))))

      val result = Await.result(testService
        .extractSchemeData(schemeInfo, "anEmpRef", Right(CsvFileContents("aSheet", Seq(Seq("a row"))))),
        Duration.Inf
      )

      assert(result.isLeft)
      result.left.get.getMessage mustBe "this was bad"
    }

    "return a Right if sendSchemeCsv is happy" in {
      val testService = new MockProcessCsvService(
        mockAuditEvents, mockDataGenerator, mockAppConfig, mockErsFileValidatorConnector)(
        sendSchemeCsv = Some(Future.successful(Right(458))))

      val result = Await.result(testService
        .extractSchemeData(schemeInfo, "anEmpRef", Right(CsvFileContents("aSheet", Seq(Seq("a row"))))),
        Duration.Inf
      )

      assert(result.isRight)
      result.right.get mustBe((458, 1))

    }
  }

  "extractSchemeDataNew" should {
    "pass on a Left if given a Left" in {
      val result: Future[Either[Throwable, (Int, Int)]] = testProcessCsvService
        .extractSchemeDataNew(schemeInfo, "anEmpRef", Left(new Exception("hello there")))

      assert(result.isLeft)
      result.left.get.getMessage mustBe "hello there"
    }

    "return a Left if sendSchemeCsv finds errors" in {
      val testService = new MockProcessCsvService(
        mockAuditEvents, mockDataGenerator, mockAppConfig, mockErsFileValidatorConnector)(
        sendSchemeCsvNew = Some(Future.successful(Left(new Exception("this was bad")))))

      val result = Await.result(testService
        .extractSchemeDataNew(schemeInfo, "anEmpRef", Right(CsvFileSubmissions("sheetName", 1, UpscanCallback("CSOP_OptionsGranted_V3.csv", "no", noOfRows = Some(1))))),
        Duration.Inf
      )

      assert(result.isLeft)
      result.left.get.getMessage mustBe "this was bad"
    }

    "return a Right if sendSchemeCsv is happy" in {
      val testService = new MockProcessCsvService(
        mockAuditEvents, mockDataGenerator, mockAppConfig, mockErsFileValidatorConnector)(
        sendSchemeCsvNew = Some(Future.successful(Right(458))))

      val result = Await.result(testService
        .extractSchemeDataNew(schemeInfo, "anEmpRef", Right(CsvFileSubmissions("sheetName", 1, UpscanCallback("CSOP_OptionsGranted_V3.csv", "no", noOfRows = Some(1))))),
        Duration.Inf
      )

      assert(result.isRight)
      result.right.get mustBe((458, 1))

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

  "sendSchemeDataCsv" should {
    "return a None if the submission was sent successfully" in {
      val testService = new MockProcessCsvService(
        mockAuditEvents, mockDataGenerator, mockAppConfig, mockErsFileValidatorConnector)(
      )
      when(mockErsFileValidatorConnector.sendToSubmissions(any[SchemeData], any[String])(any[HeaderCarrier], any[Request[_]]))
        .thenReturn(Future.successful(Right(uk.gov.hmrc.http.HttpResponse(StatusCodes.OK.intValue, "aBody"))))
      when(mockAuditEvents.fileValidatorAudit(any[SchemeInfo], any[String])(any[HeaderCarrier], any[Request[_]]))
        .thenReturn(true)

      val result: Option[Throwable] = Await.result(
        testService.sendSchemeDataCsv(SchemeData(schemeInfo, "sheetName", None, ListBuffer(Seq("aaa"))), "empRef"),
        Duration.Inf
      )

      assert(result.isEmpty)
    }

    "return a throwable if the submission was not sent successfully" in {
      val testService = new MockProcessCsvService(
        mockAuditEvents, mockDataGenerator, mockAppConfig, mockErsFileValidatorConnector)(
      )
      val returnException = new Exception("this failed")
      when(mockErsFileValidatorConnector.sendToSubmissions(any[SchemeData], any[String])(any[HeaderCarrier], any[Request[_]]))
        .thenReturn(Future.successful(Left(returnException)))
      doNothing().when(mockAuditEvents).auditRunTimeError(any[Throwable], any[String], any[SchemeInfo], any[String])(
        any[HeaderCarrier], any[Request[_]])

      val result: Option[Throwable] = Await.result(
        testService.sendSchemeDataCsv(SchemeData(schemeInfo, "sheetName", None, ListBuffer(Seq("aaa"))), "empRef"),
        Duration.Inf
      )

      assert(result.isDefined)
      assert(result.get.isInstanceOf[ERSFileProcessingException])
      result.get.getMessage mustBe returnException.toString
    }
  }

  "sendSchemeDataCsvNew" should {
    "return a None if the submission was sent successfully" in {
      val testService = new MockProcessCsvService(
        mockAuditEvents, mockDataGenerator, mockAppConfig, mockErsFileValidatorConnector)(
      )
      when(mockErsFileValidatorConnector.sendToSubmissionsNew(any[SubmissionsSchemeData], any[String])(any[HeaderCarrier], any[Request[_]]))
        .thenReturn(Future.successful(Right(uk.gov.hmrc.http.HttpResponse(StatusCodes.OK.intValue, "aBody"))))
      when(mockAuditEvents.fileValidatorAudit(any[SchemeInfo], any[String])(any[HeaderCarrier], any[Request[_]]))
        .thenReturn(true)

      val result: Option[Throwable] = Await.result(
        testService.sendSchemeDataCsv(submissionsSchemeData, "empRef"),
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
        testService.sendSchemeDataCsv(submissionsSchemeData, "empRef"),
        Duration.Inf
      )

      assert(result.isDefined)
      assert(result.get.isInstanceOf[ERSFileProcessingException])
      result.get.getMessage mustBe returnException.toString
    }
  }

  "sendSchemeCsvNew" when {
    "sending data" should {
      "succeed if sendSchemeDataCsvNew succeeded" in {
        val testService = new MockProcessCsvService(
          mockAuditEvents, mockDataGenerator, mockAppConfig, mockErsFileValidatorConnector)(
          sendSchemeDataCsvNew = Some(Future.successful(None))
        )
        when(mockAppConfig.maxNumberOfRowsPerSubmission).thenReturn(10)

        val result = Await.result(testService.sendSchemeCsv(submissionsSchemeData, "empRef"),
          Duration.Inf
        )

        assert(result.isRight)
        result.right.get shouldBe 2
      }

      "fail if sendSchemeDataCsvNew failed" in {
        val testService = new MockProcessCsvService(
          mockAuditEvents, mockDataGenerator, mockAppConfig, mockErsFileValidatorConnector)(
          sendSchemeDataCsvNew = Some(Future.successful(Some(new RuntimeException("take that"))))
        )
        when(mockAppConfig.maxNumberOfRowsPerSubmission).thenReturn(10)

        val result = Await.result(testService.sendSchemeCsv(submissionsSchemeData, "empRef"),
          Duration.Inf
        )

        assert(result.isLeft)
        result.left.get.getMessage shouldBe "take that"
      }
    }
  }

  "sendSchemeCsv" when {
    "sending whole data if splitSchemes is false" should {
      "follow a happy path" in {
        val testService = new MockProcessCsvService(
          mockAuditEvents, mockDataGenerator, mockAppConfig, mockErsFileValidatorConnector)(
          sendSchemeDataCsv = Some(Future.successful(None))
        )
        when(mockAppConfig.splitLargeSchemes).thenReturn(false)

        val result: Either[Throwable, Int] = Await.result(
          testService.sendSchemeCsv(SchemeData(schemeInfo, "sheetName", None, ListBuffer(Seq("aString"))), "empRef"),
          Duration.Inf
        )
        assert(result.isRight)
        result.right.get mustBe 1
      }
      "follow an unhappy path" in {
        val testService = new MockProcessCsvService(
          mockAuditEvents, mockDataGenerator, mockAppConfig, mockErsFileValidatorConnector)(
          sendSchemeDataCsv = Some(Future.successful(Some(new Exception("we did not succeed"))))
        )
        when(mockAppConfig.splitLargeSchemes).thenReturn(false)

        val result: Either[Throwable, Int] = Await.result(
          testService.sendSchemeCsv(SchemeData(schemeInfo, "sheetName", None, ListBuffer(Seq("aString"))), "empRef"),
          Duration.Inf
        )
        assert(result.isLeft)
        result.left.get.getMessage mustBe "we did not succeed"
      }
    }

    "splitting into slices" should {
      "follow a happy path" in {
        val testService = new MockProcessCsvService(
          mockAuditEvents, mockDataGenerator, mockAppConfig, mockErsFileValidatorConnector)(
          sendSchemeDataCsv = Some(Future.successful(None))
        )
        when(mockAppConfig.splitLargeSchemes).thenReturn(true)
        when(mockAppConfig.maxNumberOfRowsPerSubmission).thenReturn(10)
        val result: Either[Throwable, Int] = Await.result(
          testService.sendSchemeCsv(SchemeData(schemeInfo, "sheetName", None,
            (1 to 50).map(int => Seq(int.toString)).to[ListBuffer]),
            "empRef"),
          Duration.Inf
        )

        assert(result.isRight)
        result.right.get shouldBe 5
      }

      "follow an unhappy path" in {
        val testService = new MockProcessCsvService(
          mockAuditEvents, mockDataGenerator, mockAppConfig, mockErsFileValidatorConnector)(
          sendSchemeDataCsv = Some(Future.successful(Some(new Exception("this is bad"))))
        )
        when(mockAppConfig.splitLargeSchemes).thenReturn(true)
        when(mockAppConfig.maxNumberOfRowsPerSubmission).thenReturn(10)
        val result: Either[Throwable, Int] = Await.result(
          testService.sendSchemeCsv(SchemeData(schemeInfo, "sheetName", None,
            (1 to 50).map(int => Seq(int.toString)).to[ListBuffer]),
            "empRef"),
          Duration.Inf
        )

        assert(result.isLeft)
        result.left.get.getMessage shouldBe "this is bad"
      }
    }
  }

}


