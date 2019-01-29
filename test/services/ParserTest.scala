/*
 * Copyright 2019 HM Revenue & Customs
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

import java.util.concurrent.atomic.AtomicInteger

import models.{ERSFileProcessingException, SchemeInfo}
import org.joda.time.DateTime
import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.{ScalaFutures, Timeouts}
import org.scalatest.mock.MockitoSugar
import org.scalatest.time.{Millis, Span}
import org.scalatestplus.play.{OneServerPerSuite, PlaySpec}
import play.api.i18n.Messages
import play.api.mvc.Request
import services.XMLTestData._
import services.audit.AuditEvents
import play.api.i18n.Messages.Implicits._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.services.validation.{Cell, DataValidator, Row, ValidationError}

import scala.collection.mutable.ListBuffer
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.util.Success
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._
import java.io.FileNotFoundException
import javax.xml.parsers.SAXParserFactory
import scala.xml._


class ParserTest extends PlaySpec with OneServerPerSuite with ScalaFutures with MockitoSugar with BeforeAndAfter with Timeouts {

  object TestDataParser extends DataParser

  object TestDataGenerator extends DataGenerator {
    override val auditEvents: AuditEvents = mock[AuditEvents]
  }

  class MockDataValidator(rowValidator: () => Option[List[ValidationError]]) extends DataValidator {
    override def validateRows(rows: List[List[String]], contextObjectOpt: Option[AnyRef], firstRowNum: Int, ignoreBlankRows: Boolean): Option[List[ValidationError]] = ???
    override def validateRowsBuffered(rows: List[List[String]], errorBuffer: ListBuffer[ValidationError], contextObjectOpt: Option[AnyRef], firstRowNum: Int, ignoreBlankRows: Boolean): Unit = ???
    override def validateRows(rows: List[List[String]], contextObjectOpt: Option[AnyRef], zeroBased: Boolean): Option[List[ValidationError]] = ???
    override def validateCellBuffered(cell: Cell, errorBuffer: ListBuffer[ValidationError], contextObjectOpt: Option[AnyRef]): Unit = ???
    override def validateRowBuffered(row: Row, errorBuffer: ListBuffer[ValidationError], contextObjectOpt: Option[AnyRef]): Unit = ???
    override def validateCell(cell: Cell, contextObjectOpt: Option[AnyRef]): Option[List[ValidationError]] = ???
    override def validateRow(row: Row, contextObjectOpt: Option[AnyRef]): Option[List[ValidationError]] = rowValidator.apply()
  }

  private val nl: String = System.lineSeparator()
  private val timeout = 100
  private val awaitTimeout = Span(timeout, Millis)

  implicit val schemeInfo: SchemeInfo = SchemeInfo(
    schemeRef = "XA11999991234567",
    timestamp = DateTime.now,
    schemeId = "123PA12345678",
    taxYear = "2014/F15",
    schemeName = "MyScheme",
    schemeType = "EMI"
  )

  implicit val schemeName = schemeInfo.schemeName
  implicit val hc: HeaderCarrier = mock[HeaderCarrier]
  implicit val request: Request[_] = mock[Request[_]]

  val FileSystemReadXxePayload = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" +
    "  <!DOCTYPE foo [ " +
    "  <!ELEMENT foo ANY >" +
    "  <!ENTITY xxe SYSTEM \"file:///does/not/exist\" >]>" +
    "<foo>&xxe;</foo>"

  "parse row with duplicate column data 1" in {
    val result = TestDataParser.parse(emiAdjustmentsXMLRow1.toString)
    result.right.get._1.size must equal(17)
  }

  besParserTests.foreach(rec => {
    rec._1 in {
      val result = TestDataParser.parse(rec._2.toString)
      result.right.get._1.toList.take(rec._3.size) must be(rec._3)
    }
  })

  "parse row with repeats" in {
    val result = TestDataParser.parse(emiAdjustmentsRepeatXMLRow1.toString)
    result.right.get._1.size must equal(17)
    result.right.get._2 must equal(3)
  }

  "display incorrectSheetName exception in identifyAndDefineSheet method" in {
    def exceptionMessage: String = {
      try {
        TestDataGenerator.identifyAndDefineSheet("EMI40_Taxable")(schemeInfo, hc, request).toString
      }
      catch {
        case e: ERSFileProcessingException => e.message + ", " + e.context
      }
    }

    exceptionMessage must be("Incorrect ERS Template - Sheet Name isn't as expected, Couldn't identify SheetName EMI40_Taxable")
  }

  "display incorrectHeader exception in validateHeaderRow method" in {
    def exceptionMessage: String = {
      try {
        TestDataGenerator.validateHeaderRow(Seq("", ""), "CSOP_OptionsRCL_V3")(schemeInfo, hc, request).toString
      }
      catch {
        case e: ERSFileProcessingException => e.message + ", " + e.context
      }
    }

    exceptionMessage must be("Incorrect ERS Template - Header doesn't match, Header doesn't match")
  }

  "return sheetInfo given a valid sheet name" in {
    val sheet = TestDataGenerator.getSheet(ERSTemplatesInfo.emiSheet5Name)(schemeInfo, hc, request)
    sheet.schemeType mustBe "EMI"
    sheet.sheetId mustBe 5
  }

  "return sheetInfo for CSOP_OptionsGranted_V3" in {
    val sheet = TestDataGenerator.getSheet(ERSTemplatesInfo.csopSheet1Name)(schemeInfo, hc, request)
    sheet.schemeType mustBe "CSOP"
    sheet.sheetId mustBe 1
  }

  "return sheetInfo for CSOP_OptionsRCL_V3" in {
    val sheet = TestDataGenerator.getSheet(ERSTemplatesInfo.csopSheet2Name)(schemeInfo, hc, request)
    sheet.schemeType mustBe "CSOP"
    sheet.sheetId mustBe 2
  }

  "return sheetInfo for CSOP_OptionsExercised_V3" in {
    val sheet = TestDataGenerator.getSheet(ERSTemplatesInfo.csopSheet3Name)(schemeInfo, hc, request)
    sheet.schemeType mustBe "CSOP"
    sheet.sheetId mustBe 3
  }

  "throw an exception for an invalid sheetName" in {
    val result = intercept[ERSFileProcessingException] {
      TestDataGenerator.getSheet("abc")(schemeInfo, hc, request)
    }
    result.message mustBe Messages("ers.exceptions.dataParser.incorrectSheetName")
  }

  private def getRowsFromFileFixture = new {
    val content = s"field1, field2, field3${nl}field1${nl}field1, field2"
    val sheetName = "Other_Options_V3.csv"
  }

  "fetch all rows from the CSV file" in {
    val fixture = getRowsFromFileFixture
    val iterator = fixture.content.split(nl).iterator
    val actual = TestDataGenerator.getRowsFromFile(iterator)
    val expected: List[List[String]] = fixture.content.split(nl).toList.map(s => s.split(",").toList)
    actual mustBe expected
  }

  "handle empty CSV files" in {
    val fixture = getRowsFromFileFixture
    val actual = TestDataGenerator.getRowsFromFile(Iterator.empty)
    actual.isEmpty mustBe true
  }

  "return the correct number of chunks" in {
    // scalastyle:off magic.number
    TestDataGenerator.numberOfChunks(0, 1) mustBe 0
    TestDataGenerator.numberOfChunks(1, 2) mustBe 1
    TestDataGenerator.numberOfChunks(2, 2) mustBe 1
    TestDataGenerator.numberOfChunks(9, 2) mustBe 5
    // scalastyle:on magic.number
  }

  private def submitChunksFixture = new {
    val rows = List(
      List("field1", "field2"),
      List("field1", "field2"),
      List("field1", "field2"),
      List("field1", "field2"),
      List("field1", "field2")
    )
  }

  "submit the correct number of futures" in {
    val fixture = submitChunksFixture

    val rowValidator = () => Some(List.empty[ValidationError])
    val dataValidator = new MockDataValidator(rowValidator)

    val actual = TestDataGenerator.submitChunks(fixture.rows, 3, 2, 3, dataValidator)
    actual.length mustBe 3
  }

  "process all rows" in {
    val fixture = submitChunksFixture
    val count: AtomicInteger = new AtomicInteger(0)

    val rowValidator = () => {
      count.incrementAndGet()
      None
    }

    val dataValidator = new MockDataValidator(rowValidator)

    val actual = TestDataGenerator.submitChunks(fixture.rows, 3, 2, 3, dataValidator)

    for (future <- actual) {
      failAfter(awaitTimeout) {
        Await.ready(future, Duration.Inf)
      }
    }

    count.get() mustBe 5
  }

  private def processChunkFixture = new {
    val chunk = List(
      List("field1", "field2"),
      List("field1", "field2"),
      List("field1", "field2")
    )
  }

  "process each row in the chunk" in {
    val fixture = processChunkFixture
    var count: Int = 0

    val rowValidator = () => {
      count += 1
      None
    }

    val dataValidator = new MockDataValidator(rowValidator)

    TestDataGenerator.processChunk(fixture.chunk, 1, 3, dataValidator)
    count mustBe fixture.chunk.size
  }

  "throw exception when CSV file contains errors" in {
    val fixture = processChunkFixture

    val rowValidator = () => {
      Some(List(ValidationError(Cell("AA", 1, "bad"), "bad data", "bad cell AA", "cell AA is bad")))
    }

    val dataValidator = new MockDataValidator(rowValidator)

    intercept[ERSFileProcessingException] {
      TestDataGenerator.processChunk(fixture.chunk, 1, 3, dataValidator)
    }
  }

  "aggregate chunk results corectly" in {
    val row1 = List(Seq("field1", "field2"))
    val row2 = List.empty[Seq[String]]
    val row3 = List(Seq("field3", "field4", "field5"))

    val submissions: Array[Future[List[Seq[String]]]] = Array(
      Future(row1),
      Future(row2),
      Future(row3)
    )

    val expected = Some(Success(List(row1, row2, row3).flatten))
    val actual = TestDataGenerator.getResult(submissions)

    failAfter(awaitTimeout) {
      Await.ready(actual, Duration.Inf)
    }

    actual.value mustBe expected
  }

  "aggregate to an empty list when there are no chunk results" in {
    val submissions = Array.empty[Future[List[Seq[String]]]]

    val expected = Some(Success(List.empty[Seq[String]]))
    val actual = TestDataGenerator.getResult(submissions)

    failAfter(awaitTimeout) {
      Await.ready(actual, Duration.Inf)
    }

    actual.value mustBe expected
  }

  "Show that scala.xml.XML tries to access file system with malicious payload " in {
    intercept[FileNotFoundException] {
      XML.loadString(FileSystemReadXxePayload)
    }
  }

  "Show that scala.xml.XML can protect against file access when securely configured" in {
    intercept[SAXParseException] {
      XML.withSAXParser(TestDataParser.secureSAXParser).loadString(FileSystemReadXxePayload)
    }
  }
}
