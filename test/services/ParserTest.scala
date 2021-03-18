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

import config.ApplicationConfig
import models.{ERSFileProcessingException, SchemeInfo}
import org.joda.time.DateTime
import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.{ScalaFutures, TimeLimits}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatest.time.{Millis, Span}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import org.scalatestplus.play.PlaySpec
import play.api.mvc.Request
import services.XMLTestData._
import services.audit.AuditEvents
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}
import java.io.FileNotFoundException

import scala.xml._


class ParserTest extends PlaySpec with GuiceOneAppPerSuite with ScalaFutures with MockitoSugar with BeforeAndAfter with TimeLimits {

  object TestDataParser extends DataParser

  val mockAuditEvents: AuditEvents = mock[AuditEvents]
  val mockAppConfig: ApplicationConfig = mock[ApplicationConfig]
  implicit val ec: ExecutionContextExecutor = ExecutionContext.global
  val dataGenerator = new DataGenerator(mockAuditEvents, mockAppConfig)

  private val nl: String = System.lineSeparator()
  private val timeout = 200
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
          dataGenerator.identifyAndDefineSheet("EMI40_Taxable")(schemeInfo, hc, request).toString
        }
        catch {
          case e: ERSFileProcessingException => e.message + ", " + e.context
        }
      }

      exceptionMessage mustBe "Incorrect ERS Template - Sheet Name isn't as expected, Couldn't identify SheetName EMI40_Taxable"
    }

    "display incorrectHeader exception in validateHeaderRow method" in {
      def exceptionMessage: String = {
        try {
          dataGenerator.validateHeaderRow(Seq("", ""), "CSOP_OptionsRCL_V3")(schemeInfo, hc, request).toString
        }
        catch {
          case e: ERSFileProcessingException => e.message + ", " + e.context
        }
      }
      exceptionMessage mustBe "Incorrect ERS Template - Header doesn't match, Header doesn't match"
    }

    "return sheetInfo given a valid sheet name" in {
      val sheet = dataGenerator.getSheet(ERSTemplatesInfo.emiSheet5Name)(schemeInfo, hc, request)
      sheet.schemeType mustBe "EMI"
      sheet.sheetId mustBe 5
    }

    "return sheetInfo for CSOP_OptionsGranted_V3" in {
      val sheet = dataGenerator.getSheet(ERSTemplatesInfo.csopSheet1Name)(schemeInfo, hc, request)
      sheet.schemeType mustBe "CSOP"
      sheet.sheetId mustBe 1
    }

    "return sheetInfo for CSOP_OptionsRCL_V3" in {
      val sheet = dataGenerator.getSheet(ERSTemplatesInfo.csopSheet2Name)(schemeInfo, hc, request)
      sheet.schemeType mustBe "CSOP"
      sheet.sheetId mustBe 2
    }

    "return sheetInfo for CSOP_OptionsExercised_V3" in {
      val sheet = dataGenerator.getSheet(ERSTemplatesInfo.csopSheet3Name)(schemeInfo, hc, request)
      sheet.schemeType mustBe "CSOP"
      sheet.sheetId mustBe 3
    }

    "throw an exception for an invalid sheetName" in {
      val result = intercept[ERSFileProcessingException] {
        dataGenerator.getSheet("abc")(schemeInfo, hc, request)
      }
      result.message mustBe "Incorrect ERS Template - Sheet Name isn't as expected"
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
