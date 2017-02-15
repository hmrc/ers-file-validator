/*
 * Copyright 2017 HM Revenue & Customs
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

import models.{ERSFileProcessingException, SchemeInfo}
import org.joda.time.DateTime
import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.{OneServerPerSuite, PlaySpec}
import play.api.i18n.Messages
import play.api.mvc.Request
import services.XMLTestData._
import services.audit.AuditEvents
import uk.gov.hmrc.play.http.HeaderCarrier
import play.api.i18n.Messages.Implicits._

/**
  * Created by raghu on 26/01/16.
  */
class ParserTest extends PlaySpec with OneServerPerSuite with ScalaFutures with MockitoSugar with BeforeAndAfter {

  object TestDataParser extends DataParser

  object TestDataGenerator extends DataGenerator {
    override val auditEvents: AuditEvents = mock[AuditEvents]
  }

  val schemeInfo: SchemeInfo = SchemeInfo(
    schemeRef = "XA11999991234567",
    timestamp = DateTime.now,
    schemeId = "123PA12345678",
    taxYear = "2014/F15",
    schemeName = "MyScheme",
    schemeType = "EMI"
  )

  implicit val hc: HeaderCarrier = mock[HeaderCarrier]
  implicit val request: Request[_] = mock[Request[_]]

  "parse row with duplicate column data 1" in {
    val result = TestDataParser.parse(emiAdjustmentsXMLRow1.toString)
    result.right.get.size must equal(17)
  }

  besParserTests.foreach(rec => {
    rec._1 in {
      val result = TestDataParser.parse(rec._2.toString)
      result.right.get.toList.take(rec._3.size) must be(rec._3)
    }
  })

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
}
