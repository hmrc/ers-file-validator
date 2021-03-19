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

///*
// * Copyright 2021 HM Revenue & Customs
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package services
//
//import akka.NotUsed
//import akka.actor.ActorSystem
//import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
//import akka.stream.IOResult
//import akka.stream.alpakka.csv.scaladsl.CsvParsing
//import akka.stream.scaladsl.{FileIO, Sink, Source}
//import akka.testkit.TestKit
//import akka.util.ByteString
//import com.typesafe.config.ConfigFactory
//import config.ApplicationConfig
//import connectors.ERSFileValidatorConnector
//import models.ERSFileProcessingException
//import org.mockito.ArgumentMatchers._
//import org.mockito.Mockito._
//import org.scalatest.MustMatchers.convertToAnyMustWrapper
//import org.scalatest.concurrent.{ScalaFutures, TimeLimits}
//import org.scalatestplus.play.guice.GuiceOneAppPerSuite
//import play.api.i18n.Messages
//import play.api.libs.json.Json
//import play.api.mvc.{AnyContent, Request}
//import play.api.test.FakeRequest
//import services.audit.AuditEvents
//import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}
//import uk.gov.hmrc.http.cache.client.CacheMap
//import uk.gov.hmrc.play.test.UnitSpec
//import uk.gov.hmrc.services.validation.DataValidator
//import uk.gov.hmrc.services.validation.models.{Cell, ValidationError}
//
//import java.io.File
//import java.nio.file.Files
//import scala.collection.mutable.ListBuffer
//import scala.concurrent.duration.Duration
//import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutor, Future}
//import scala.concurrent.ExecutionContext.Implicits.global._
//
//class ProcessCsvServiceSpec extends TestKit(ActorSystem("Test")) with UnitSpec with GuiceOneAppPerSuite with TimeLimits with ScalaFutures {
//
//  def convertToAkkaSource(file: File): Source[List[ByteString], Future[IOResult]] = {
//    FileIO.fromPath(file.toPath)
//      .via(CsvParsing.lineScanner())
//  }
//
//  val mockDataGenerator: DataGenerator = mock[DataGenerator]
//  val mockAuditEvents: AuditEvents = mock[AuditEvents]
//  val mockErsFileValidatorConnector: ERSFileValidatorConnector = mock[ERSFileValidatorConnector]
//  val mockAppConfig: ApplicationConfig = mock[ApplicationConfig]
//
//  implicit val request : Request[_] = mock[Request[_]]
//  implicit val hc : HeaderCarrier = mock[HeaderCarrier]
//
//  def testProcessCsvService: ProcessCsvService = new ProcessCsvService(mockAuditEvents, mockDataGenerator, mockAppConfig, mockErsFileValidatorConnector)
//
//  "processRow" should {
//
//    "process a row and return the errors it contains" in {
//      val source = convertToAkkaSource(new File(System.getProperty("user.dir") + "/test/resources/copy/Other_Grants_V3.csv"))
//      val dataValidator = new DataValidator(ConfigFactory.load.getConfig("ers-other-grants-validation-config"))
//      val resultFuture = source
//        .runWith(Sink.seq).map { fileCopied =>
//        testProcessCsvService.processRow(fileCopied.flatten.toList, "Other_Grants_V3.csv", dataValidator)
//      }
//
//      val result = Await.result(resultFuture, Duration.Inf)
//      result match {
//        case Right(validationErrorList) =>
//          validationErrorList.head.cell mustBe Cell("A", 0, "25  2015")
//          validationErrorList.head.errorId mustBe "001"
//          validationErrorList.head.errorMsg mustBe "ers.upload.error.date"
//          validationErrorList.head.ruleId mustBe "error.1"
//        case Left(ex) => throw ex
//      }
//    }
//
//    "process a row and return an empty list if there are no errors" in {
//      val source = convertToAkkaSource(new File(System.getProperty("user.dir") + "/test/resources/copy/CSOP_OptionsGranted_V3.csv"))
//      val dataValidator = new DataValidator(ConfigFactory.load.getConfig("ers-csop-granted-validation-config"))
//      val resultFuture = source
//        .runWith(Sink.seq).map { fileCopied =>
//        testProcessCsvService.processRow(fileCopied.flatten.toList, "CSOP_OptionsGranted_V3.csv", dataValidator)
//      }
//
//      val result = Await.result(resultFuture, Duration.Inf)
//      result match {
//        case Right(validationErrorList) => validationErrorList.isEmpty mustBe true
//        case Left(ex) => throw ex
//      }
//    }
//  }
//
//  "processFiles" should {
//
//    implicit val request: RequestWithOptionalEmpRef[AnyContent] = RequestWithOptionalEmpRef(FakeRequest(), None)
//    when(mockDataGenerator.getSheetCsv(any(), any())(any())).thenReturn(Right((ERSTemplatesInfo.ersSheets("CSOP_OptionsGranted_V3"), "csop")))
//    when(mockDataGenerator.identifyAndDefineSheetEither(any())(any(), any(), any())).thenReturn(Right("CSOP_OptionsGranted_V3"))
//    when(mockDataGenerator.setValidatorCsv(any())(any(), any(), any())).thenReturn(Right(new DataValidator(ConfigFactory.load.getConfig("ers-csop-granted-validation-config"))))
//    when(mockErsUtil.SCHEME_ERROR_COUNT_CACHE).thenReturn("10")
//    when(mockErsUtil.cache[Any](any(), any())(any(), any(), any(), any())).thenReturn(Future(CacheMap("1", Map("test" -> Json.obj("test" -> "test")))))
//
//    def returnStubSource(x: String, data: String): Source[HttpResponse, NotUsed] = {
//      Source.single(HttpResponse(entity = data))
//    }
//
//    "return true if all rows are valid" in {
//      val callback = UpscanCsvFilesCallbackList(List(UpscanCsvFilesCallback(UploadId("1"), UploadedSuccessfully("CSOP_OptionsGranted_V3.csv", "no", Some(1)))))
//      val data = "2015-09-23,250,123.12,12.1234,12.1234,no,yes,AB12345678,no\n2015-09-23,250,123.12,12.1234,12.1234,no,yes,AB12345678,no\n2015-09-23,250,123.12,12.1234,12.1234,no,yes,AB12345678,no"
//
//      val resultFuture = testProcessCsvService.processFiles(Some(callback), "csop", returnStubSource(_ , data))
//
//      val result = Await.result(resultFuture, Duration.Inf)
//      val boolList = Await.result(Future.sequence(result), Duration.Inf)
//      assert(boolList.forall(_.isRight))
//      boolList mustBe List(Right(true))
//    }
//    "return false when the data contains at least one invalid row" in {
//      val callback = UpscanCsvFilesCallbackList(List(UpscanCsvFilesCallback(UploadId("1"), UploadedSuccessfully("CSOP_OptionsGranted_V3.csv", "no", Some(1)))))
//      val data = "2015-09-23,test,123.12,12.1234,12.1234,no,yes,AB12345678,no\n2015-09-23,250,123.12,12.1234,12.1234,no,yes,AB12345678,no\n2015-09-23,250,123.12,12.1234,12.1234,no,yes,AB12345678,no"
//
//      val resultFuture = testProcessCsvService.processFiles(Some(callback), "csop", returnStubSource(_, data))
//
//      val result = Await.result(resultFuture, Duration.Inf)
//      val boolList = Await.result(Future.sequence(result), Duration.Inf)
//      assert(boolList.forall(_.isRight))
//      boolList mustBe List(Right(false))
//    }
//
//    "return a throwable when an error occurs during the file validation" in {
//      val callback = UpscanCsvFilesCallbackList(List(UpscanCsvFilesCallback(UploadId("1"), UploadedSuccessfully("CSOP_OptionsGranted_V3.csv", "no", Some(1)))))
//      val data = ""
//      val resultFuture = testProcessCsvService.processFiles(Some(callback), "csop", returnStubSource(_, data))
//
//      val result = Await.result(resultFuture, Duration.Inf)
//      val boolList = Await.result(Future.sequence(result), Duration.Inf)
//      assert(boolList.forall(_.isLeft))
//      boolList.head match {
//        case Left(ex) => ex.getMessage mustBe "The file that you chose doesn’t contain any data.<br/><br/>You won’t be able to upload CSOP_OptionsGranted_V3.csv as part of your annual return."
//      }
//    }
//
//    "return a throwable when an error occurs during the file processing" in {
//      val callback = UpscanCsvFilesCallbackList(List(UpscanCsvFilesCallback(UploadId("1"), UploadedSuccessfully("CSOP_OptionsGranted_V3.csv", "no", Some(1)))))
//      val data = "2015-09-23,250,123.12,12.1234,12.1234,no,yes,AB12345678,no\n2015-09-23,250,123.12,12.1234,12.1234,no,yes,AB12345678,no\n2015-09-23,250,123.12,12.1234,12.1234,no,yes,AB12345678,no"
//      when(mockDataGenerator.setValidatorCsv(any())(any(), any(), any())).thenReturn(Left(ERSFileProcessingException(
//        "ers.exceptions.dataParser.configFailure",
//        Messages("ers.exceptions.dataParser.validatorError"),
//        optionalParams = Seq("test")
//      )))
//
//      val resultFuture = testProcessCsvService.processFiles(Some(callback), "csop", returnStubSource(_, data))
//
//      val result = Await.result(resultFuture, Duration.Inf)
//      val boolList = Await.result(Future.sequence(result), Duration.Inf)
//      assert(boolList.forall(_.isLeft))
//      boolList.head match {
//        case Left(ex) => ex.getMessage mustBe "ers.exceptions.dataParser.configFailure"
//      }
//    }
//
//  }
//
//  "extractEntityData" should {
//
//    "extract the data from an HttpResponse" in {
//      val response = HttpResponse(entity = "Test response body")
//      val resultFuture = testProcessCsvService.extractEntityData(response).runWith(Sink.head)
//
//      val result = Await.result(resultFuture, Duration.Inf)
//      result.utf8String mustBe "Test response body"
//    }
//
//    "return a failed source with an UpstreamErrorResponse if response status is not Ok (200)" in {
//      val response = HttpResponse(status = StatusCodes.InternalServerError)
//      val resultFuture = testProcessCsvService.extractEntityData(response).runWith(Sink.head)
//
//      assert(resultFuture.failed.futureValue.getMessage.contains("Illegal response from Upscan"))
//    }
//  }
//
//  "extractBodyOfRequest" should {
//
//    "extract the body from an Http response stream and read as csv" in {
//      val source = Source
//        .single(HttpResponse(entity = "0, 1, 2, 3\n4, 5"))
//
//      val resultFuture = testProcessCsvService.extractBodyOfRequest(source).runWith(Sink.seq)
//      val result = Await.result(resultFuture, Duration.Inf)
//
//      result.head match {
//        case Right(data) =>
//          data mustBe List(ByteString("0"), ByteString(" 1"), ByteString(" 2"), ByteString(" 3"))
//        case Left(ex) =>
//          throw ex
//      }
//      result(1) match {
//        case Right(data) =>
//          data mustBe List(ByteString("4"), ByteString(" 5"))
//        case Left(ex) =>
//          throw ex
//      }
//    }
//
//    "return a left containing a throwable when an error occurs" in {
//      val source = Source
//        .single(HttpResponse(status = StatusCodes.InternalServerError))
//
//      val resultFuture = testProcessCsvService.extractBodyOfRequest(source).runWith(Sink.seq)
//      val result = Await.result(resultFuture, Duration.Inf)
//      result.head.isLeft mustBe true
//      result.head.left.map { x =>
//        assert(x.isInstanceOf[UpstreamErrorResponse])
//        assert(x.asInstanceOf[UpstreamErrorResponse].getMessage().contains("Illegal response from Upscan"))
//      }
//    }
//  }
//
//  "checkFileType" should {
//    "check the file is a csv and remove the extension" in {
//      val result = testProcessCsvService.checkFileType("test.csv")
//      result.isRight mustBe true
//      result match {
//        case Right(value) => value mustBe "test"
//      }
//    }
//
//    "if the file is not a csv throw an ERSFileProcessingException" in {
//      val result = testProcessCsvService.checkFileType("test.ods")
//      result.isLeft mustBe true
//      result match {
//        case Left(ex) => ex.getMessage mustBe "You chose to check a CSV file, but test.ods isn’t a CSV file."
//      }
//    }
//  }
//
//  "processDisplayedErrors" should {
//    "correctly update the cells with the row number for as many rows as stated" in {
//      val errors = Seq((List(
//        ValidationError(Cell("A", 0, "test"), "001", "error.1", "ers.upload.error.date"),
//        ValidationError(Cell("B", 0, "test"), "001", "error.1", "ers.upload.error.date"),
//        ValidationError(Cell("C", 1, "test"), "001", "error.1", "ers.upload.error.date")
//      ), 0), (List(
//        ValidationError(Cell("A", 0, "test"), "001", "error.1", "ers.upload.error.date"),
//        ValidationError(Cell("B", 2, "test"), "001", "error.1", "ers.upload.error.date"),
//        ValidationError(Cell("C", 0, "test"), "001", "error.1", "ers.upload.error.date")
//      ), 0), (List
//        .empty[ValidationError],
//        0), (List(
//        ValidationError(Cell("A", 0, "test"), "001", "error.1", "ers.upload.error.date"),
//        ValidationError(Cell("B", 4, "test"), "001", "error.1", "ers.upload.error.date"),
//        ValidationError(Cell("C", 0, "test"), "001", "error.1", "ers.upload.error.date")
//      ), 0)
//      )
//      val result = testProcessCsvService.processDisplayedErrors(2, errors)
//      result.head mustBe (List(
//        ValidationError(Cell("A", 1, "test"), "001", "error.1", "ers.upload.error.date"),
//        ValidationError(Cell("B", 1, "test"), "001", "error.1", "ers.upload.error.date"),
//        ValidationError(Cell("C", 1, "test"), "001", "error.1", "ers.upload.error.date")
//      ), 0)
//
//      result(1) mustBe (List(
//        ValidationError(Cell("A", 2, "test"), "001", "error.1", "ers.upload.error.date"),
//        ValidationError(Cell("B", 2, "test"), "001", "error.1", "ers.upload.error.date"),
//        ValidationError(Cell("C", 2, "test"), "001", "error.1", "ers.upload.error.date")
//      ), 1)
//    }
//  }
//
//  "giveRowNumbers" should {
//
//    "return a seq of list of validation errors with correct row numbers" in {
//      val errors = Seq(List(
//        ValidationError(Cell("A", 0, "test"), "001", "error.1", "ers.upload.error.date"),
//        ValidationError(Cell("B", 0, "test"), "001", "error.1", "ers.upload.error.date"),
//        ValidationError(Cell("C", 1, "test"), "001", "error.1", "ers.upload.error.date")
//      ))
//      val result = testProcessCsvService.giveRowNumbers(errors)
//
//      result mustBe Seq(List(
//        ValidationError(Cell("A", 1, "test"), "001", "error.1", "ers.upload.error.date"),
//        ValidationError(Cell("B", 1, "test"), "001", "error.1", "ers.upload.error.date"),
//        ValidationError(Cell("C", 1, "test"), "001", "error.1", "ers.upload.error.date")
//      ))
//    }
//  }
//
//  "getRowsWithNumbers" should {
//
//    "return validation errors if there are no exceptions" in {
//      val errors = Seq(Right(List(
//        ValidationError(Cell("A", 0, "test"), "001", "error.1", "ers.upload.error.date"),
//        ValidationError(Cell("B", 0, "test"), "001", "error.1", "ers.upload.error.date"),
//        ValidationError(Cell("C", 1, "test"), "001", "error.1", "ers.upload.error.date")
//      )))
//      val result = testProcessCsvService.getRowsWithNumbers(errors, "test.csv")
//
//      result.isRight mustBe true
//      result.map { value =>
//        value mustBe Seq(List(
//          ValidationError(Cell("A", 1, "test"), "001", "error.1", "ers.upload.error.date"),
//          ValidationError(Cell("B", 1, "test"), "001", "error.1", "ers.upload.error.date"),
//          ValidationError(Cell("C", 1, "test"), "001", "error.1", "ers.upload.error.date")
//        ))
//      }
//    }
//
//    "return an exception if the file is empty" in {
//      val errors = Seq.empty[Either[Throwable, List[ValidationError]]]
//      val result = testProcessCsvService.getRowsWithNumbers(errors, "test.csv")
//
//      result.isLeft mustBe true
//      result match {
//        case Left(ex) => ex.getMessage mustBe "The file that you chose doesn’t contain any data.<br/><br/>You won’t be able to upload test.csv as part of your annual return."
//      }
//    }
//
//    "return the earliest previous exception if one exists" in {
//      val errors = Seq(
//        Left(ERSFileProcessingException("test error", "b")),
//        Right(List(ValidationError(Cell("A", 0, "test"), "001", "error.1", "ers.upload.error.date"))),
//        Left(ERSFileProcessingException("a", "b"))
//      )
//      val result = testProcessCsvService.getRowsWithNumbers(errors, "test.csv")
//
//      result.isLeft mustBe true
//      result match {
//        case Left(ex) => ex.getMessage mustBe "test error"
//      }
//    }
//  }
//
//  "checkValidityOfRows" should {
//
//    "return true if there are no validation errors in any row" in {
//      implicit val request: RequestWithOptionalEmpRef[AnyContent] = RequestWithOptionalEmpRef(FakeRequest(), None)
//      val errors = Seq(List.empty[ValidationError], List.empty[ValidationError], List.empty[ValidationError])
//      val callback = UpscanCsvFilesCallback(UploadId("1"), UploadedSuccessfully("CSOP_OptionsGranted_V3.csv", "no", Some(1)))
//
//      val resultFuture = testProcessCsvService.checkValidityOfRows(errors, "CSOP_OptionsGranted_V3.csv", callback)
//
//      val result = Await.result(resultFuture, Duration.Inf)
//
//      result.isRight mustBe true
//      result.map { value =>
//        value mustBe true
//      }
//    }
//
//    "return false and cache errors if there are validation errors in any row" in {
//      implicit val request: RequestWithOptionalEmpRef[AnyContent] = RequestWithOptionalEmpRef(FakeRequest(), None)
//      when(mockErsUtil.SCHEME_ERROR_COUNT_CACHE).thenReturn("10")
//      when(mockErsUtil.cache[Long](any(), any())(any(), any(), any(), any())).thenReturn(Future(CacheMap("1", Map("x" -> Json.obj("test" -> "test")))))
//      when(mockErsUtil.cache[ListBuffer[SheetErrors]](any(), any())(any(), any(), any(), any())).thenReturn(Future(CacheMap("1", Map("x" -> Json.obj("test" -> "test")))))
//      val errors = Seq(
//        List(ValidationError(Cell("A", 1, "test"), "001", "error.1", "ers.upload.error.date")),
//        List.empty[ValidationError],
//        List(ValidationError(Cell("A", 3, "test"), "001", "error.1", "ers.upload.error.date"))
//      )
//      val callback = UpscanCsvFilesCallback(UploadId("1"), UploadedSuccessfully("CSOP_OptionsGranted_V3.csv", "no", Some(1)))
//
//      val resultFuture = testProcessCsvService.checkValidityOfRows(errors, "CSOP_OptionsGranted_V3.csv", callback)
//
//      val result = Await.result(resultFuture, Duration.Inf)
//
//      result.isRight mustBe true
//      result.map { value =>
//        value mustBe false
//      }
//    }
//
//  }
//
//
//}
//
//
