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

import config.ApplicationConfig
import models.{ERSFileProcessingException, HeaderValidationError, SchemeInfo, UnknownSheetError}
import org.mockito.Mockito.when
import org.scalatest.concurrent.{ScalaFutures, TimeLimits}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.mvc.Request
import services.XMLTestData._
import services.audit.AuditEvents
import uk.gov.hmrc.http.HeaderCarrier
import org.scalatest.{BeforeAndAfter, EitherValues}

import java.time.ZonedDateTime
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}
import scala.xml._


class ParserTest extends PlaySpec with ScalaFutures with MockitoSugar with BeforeAndAfter with EitherValues with TimeLimits {

  object TestDataParser extends DataParser

  val mockAuditEvents: AuditEvents = mock[AuditEvents]
  val mockAppConfig: ApplicationConfig = mock[ApplicationConfig]
  implicit val ec: ExecutionContextExecutor = ExecutionContext.global
  def dataGenerator = new DataGenerator(mockAuditEvents, mockAppConfig)

  when(mockAppConfig.csopV5Enabled).thenReturn(true)

  implicit val schemeInfo: SchemeInfo = SchemeInfo(
    schemeRef = "XA11999991234567",
    timestamp = ZonedDateTime.now,
    schemeId = "123PA12345678",
    taxYear = "2014/F15",
    schemeName = "MyScheme",
    schemeType = "EMI"
  )

  implicit val schemeName: String = schemeInfo.schemeName
  implicit val hc: HeaderCarrier = mock[HeaderCarrier]
  implicit val request: Request[_] = mock[Request[_]]

  val FileSystemReadXxePayload = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" +
    "  <!DOCTYPE foo [ " +
    "  <!ELEMENT foo ANY >" +
    "  <!ENTITY xxe SYSTEM \"file:///does/not/exist\" >]>" +
    "<foo>&xxe;</foo>"

  "parse row with duplicate column data 1" in {
    val result = TestDataParser.parse(emiAdjustmentsXMLRow1.toString)
    result.value._1.size must equal(17)
  }

  besParserTests.foreach(rec => {
    rec._1 in {
      val result = TestDataParser.parse(rec._2.toString)
      result.value._1.toList.take(rec._3.size) must be(rec._3)
    }
  })

  "parse row with repeats" in {
    val result = TestDataParser.parse(emiAdjustmentsRepeatXMLRow1.toString)
    result.value._1.size must equal(17)
    result.value._2 must equal(3)
  }

    "display incorrectSheetName user validation error in identifyAndDefineSheet method" in {
      val result = dataGenerator.identifyAndDefineSheet("EMI40_Taxable")(schemeInfo, hc, request)
      val error = result.left.value

      error mustBe a[UnknownSheetError]
      error.message mustBe "Incorrect ERS Template - Sheet Name isn't as expected"
      error.context mustBe "Couldn't find config for given SheetName, sheet name may be incorrect"
    }

    "display incorrectHeader user validation error in validateHeaderRow method" in {
      val result = dataGenerator.validateHeaderRow(Seq("", ""), "CSOP_OptionsRCL_V4")(schemeInfo, hc, request)
      val error = result.left.value

      error mustBe a[HeaderValidationError]
      error.message mustBe "Incorrect ERS Template - Header doesn't match"
      error.context mustBe "Header doesn't match"
    }

    "return sheetInfo given a valid sheet name" in {
      val result = dataGenerator.getSheet(ERSTemplatesInfo.emiSheet5Name)(schemeInfo, hc, request)
      result.isRight mustBe true
      val sheet = result.value
      sheet.schemeType mustBe "EMI"
      sheet.sheetId mustBe 5
    }

    "return sheetInfo for CSOP_OptionsGranted_V4" in {
      val result = dataGenerator.getSheet(ERSTemplatesInfo.csopSheet1Name)(schemeInfo, hc, request)
      result.isRight mustBe true
      val sheet = result.value
      sheet.schemeType mustBe "CSOP"
      sheet.sheetId mustBe 1
    }

    "return sheetInfo for CSOP_OptionsRCL_V4" in {
      val result = dataGenerator.getSheet(ERSTemplatesInfo.csopSheet2Name)(schemeInfo, hc, request)
      result.isRight mustBe true
      val sheet = result.value
      sheet.schemeType mustBe "CSOP"
      sheet.sheetId mustBe 2
    }

    "return sheetInfo for CSOP_OptionsExercised_V4" in {
      val result = dataGenerator.getSheet(ERSTemplatesInfo.csopSheet3Name)(schemeInfo, hc, request)
      result.isRight mustBe true
      val sheet = result.value
      sheet.schemeType mustBe "CSOP"
      sheet.sheetId mustBe 3
    }

    "return Left for an invalid sheetName" in {
      val result = dataGenerator.getSheet("abc")(schemeInfo, hc, request)
      result.isLeft mustBe true
      val error = result.left.value
      error mustBe a[UnknownSheetError]
      error.message mustBe "Incorrect ERS Template - Sheet Name isn't as expected"
    }

    "Show that scala.xml.XML tries to access file system with malicious payload " in {
      intercept[SAXParseException] {
        XML.loadString(FileSystemReadXxePayload)
      }
    }

    "Show that scala.xml.XML can protect against file access when securely configured" in {
      intercept[SAXParseException] {
        XML.withSAXParser(TestDataParser.secureSAXParser).loadString(FileSystemReadXxePayload)
      }
    }

  "Throw ERSFileProcessingException if an exception occurs while parsing a column" in {
    object TestDataParser2 extends DataParser {
      override def parseColumn(col: Node): Seq[String] = throw new RuntimeException("Error")
    }
    intercept[ERSFileProcessingException] {
      TestDataParser2.parse(emiAdjustmentsRepeatXMLRow1.toString)
    }
  }
}
