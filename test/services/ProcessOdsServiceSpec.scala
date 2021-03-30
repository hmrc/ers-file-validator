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

import java.io.{FileInputStream, FileOutputStream}
import java.nio.file.Files
import java.util.zip.{ZipEntry, ZipOutputStream}

import config.ApplicationConfig
import connectors.ERSFileValidatorConnector
import models._
import models.upscan.UpscanCallback
import org.joda.time.DateTime
import org.mockito.ArgumentMatchers.{any, eq => argEq}
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.mvc.Request
import services.audit.AuditEvents
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}


class ProcessOdsServiceSpec extends PlaySpec with CSVTestData with GuiceOneAppPerSuite with ScalaFutures with MockitoSugar with BeforeAndAfter {

  val mockSessionService: SessionService = mock[SessionService]
  val mockErsFileValidatorConnector: ERSFileValidatorConnector = mock[ERSFileValidatorConnector]
  val mockDataGenerator: DataGenerator = mock[DataGenerator]
  val mockHeaderCarrier: HeaderCarrier = mock[HeaderCarrier]
  val mockAppConfig: ApplicationConfig = mock[ApplicationConfig]
  val mockAuditEvents: AuditEvents = mock[AuditEvents]

  implicit val ec: ExecutionContextExecutor = ExecutionContext.global
  implicit val request : Request[_] = mock[Request[_]]
  implicit val hc : HeaderCarrier = mock[HeaderCarrier]

  val schemeInfo: SchemeInfo = SchemeInfo (
    schemeRef = "XA11999991234567",
    timestamp = DateTime.now,
    schemeId = "123PA12345678",
    taxYear = "2014/F15",
    schemeName = "MyScheme",
    schemeType = "EMI"
  )

  def createListBuffer(schemeInfo: SchemeInfo, sheetName: String, listBuffer: ListBuffer[Seq[String]]): ListBuffer[SchemeData] = {
    ListBuffer(SchemeData(schemeInfo, sheetName, None, listBuffer))
  }

  val callbackData: UpscanCallback = UpscanCallback("csop.ods", "downloadUrl", Some(1024), Some("ods"), None, None)
  val callbackDataCSV: UpscanCallback = UpscanCallback("EMI40_Adjustments_V3", "downloadUrl", Some(1024), Some("csv"), None, None)

  val xmlData1 = <table:table table:name="EMI40_Adjustments_V3" table:style-name="ta1"> <table:table-row table:style-name="ro6"><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>1.</text:p><text:p>Date of event</text:p><text:p>(yyyy-mm-dd)</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>2.</text:p><text:p>Number of employees who acquired or were awarded shares</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>3.</text:p><text:p>Type of shares awarded</text:p><text:p>Enter a number from 1 to 4 depending on the type of share awarded. Follow the link at cell B10 for a list of the types of share which can be awarded</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>4.</text:p><text:p>If free shares, are performance conditions attached to their award?</text:p><text:p>(yes/no)</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>5.</text:p><text:p>If matching shares, what is the ratio of shares to partnership shares?</text:p><text:p>Enter ratio for example 2:1; 2/1</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>6.</text:p><text:p>Unrestricted market value (UMV) per share on acquisition or award</text:p><text:p>£</text:p><text:p>e.g. 10.1234</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>7.</text:p><text:p>Total number of shares acquired or awarded</text:p><text:p>e.g. 100.00</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>8.</text:p><text:p>Total value of shares acquired or awarded</text:p><text:p>£</text:p><text:p>e.g. 10.1234</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>9.</text:p><text:p>Total number of employees whose award of free shares during the year exceeded the limit of £3,600</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>10.</text:p><text:p>Total number of employees whose award of free shares during the year was at or below the limit of £3,600</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>11.</text:p><text:p>Total number of employees whose award of partnership shares during the year exceeded the limit of £1,800</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>12.</text:p><text:p>Total number of employees whose award of partnership shares during the year was at or below the limit of £1,800</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>13.</text:p><text:p>Total number of employees whose award of matching shares during the year exceeded the limit of £3,600</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>14.</text:p><text:p>Total number of employees whose award of matching shares during the year was at or below the limit of £3,600</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>15.</text:p><text:p>Are the shares listed on a recognised stock exchange?</text:p><text:p>(yes/no)</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>16.</text:p><text:p>If no, was the market value agreed with HMRC?</text:p><text:p>(yes/no)</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>17.</text:p><text:p>If yes, enter the HMRC reference given</text:p></table:table-cell><table:table-cell table:style-name="ce11"/><table:table-cell table:number-columns-repeated="1003"/><table:table-cell table:style-name="ce17"/><table:table-cell table:number-columns-repeated="2"/></table:table-row></table:table>
  val xmlData2 = <table:table-row table:style-name="ro6"><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>1.</text:p><text:p>Date of event</text:p><text:p>(yyyy-mm-dd)</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>2.</text:p><text:p>Number of employees who acquired or were awarded shares</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>3.</text:p><text:p>Type of shares awarded</text:p><text:p>Enter a number from 1 to 4 depending on the type of share awarded. Follow the link at cell B10 for a list of the types of share which can be awarded</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>4.</text:p><text:p>If free shares, are performance conditions attached to their award?</text:p><text:p>(yes/no)</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>5.</text:p><text:p>If matching shares, what is the ratio of shares to partnership shares?</text:p><text:p>Enter ratio for example 2:1; 2/1</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>6.</text:p><text:p>Unrestricted market value (UMV) per share on acquisition or award</text:p><text:p>£</text:p><text:p>e.g. 10.1234</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>7.</text:p><text:p>Total number of shares acquired or awarded</text:p><text:p>e.g. 100.00</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>8.</text:p><text:p>Total value of shares acquired or awarded</text:p><text:p>£</text:p><text:p>e.g. 10.1234</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>9.</text:p><text:p>Total number of employees whose award of free shares during the year exceeded the limit of £3,600</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>10.</text:p><text:p>Total number of employees whose award of free shares during the year was at or below the limit of £3,600</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>11.</text:p><text:p>Total number of employees whose award of partnership shares during the year exceeded the limit of £1,800</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>12.</text:p><text:p>Total number of employees whose award of partnership shares during the year was at or below the limit of £1,800</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>13.</text:p><text:p>Total number of employees whose award of matching shares during the year exceeded the limit of £3,600</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>14.</text:p><text:p>Total number of employees whose award of matching shares during the year was at or below the limit of £3,600</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>15.</text:p><text:p>Are the shares listed on a recognised stock exchange?</text:p><text:p>(yes/no)</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>16.</text:p><text:p>If no, was the market value agreed with HMRC?</text:p><text:p>(yes/no)</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>17.</text:p><text:p>If yes, enter the HMRC reference given</text:p></table:table-cell><table:table-cell table:style-name="ce11"/><table:table-cell table:number-columns-repeated="1003"/><table:table-cell table:style-name="ce17"/><table:table-cell table:number-columns-repeated="2"/></table:table-row>

  before {
    reset(mockErsFileValidatorConnector)
    reset(mockSessionService)
  }

  "The File Processing Service" must {
    val fileProcessingService: ProcessOdsService = new ProcessOdsService(mockDataGenerator, mockAuditEvents, mockErsFileValidatorConnector, mockSessionService, mockAppConfig, ec) {
      override val splitSchemes = false
      override val maxNumberOfRows = 1

      override def readFile(downloadUrl: String) = XMLTestData.getEMIAdjustmentsTemplateSTAX
    }

    "yield a list of scheme data from file data" in {
      val listBuffer = ListBuffer(
        Seq("yes", "yes", "yes", "4", "1989-10-20", "Anthony", "Joe", "Jones", "AA123456A", "123/XZ55555555", "10.1232", "100.00", "10.2585", "10.2544")
      )
      when(mockDataGenerator.getErrors(any())(any(),any(),any())).thenReturn(createListBuffer(schemeInfo, "EMI40_Adjustments_V3", listBuffer))
      when(mockErsFileValidatorConnector.sendToSubmissions(any[SchemeData](), any[String]())(any[HeaderCarrier],any[Request[_]])).thenReturn(Future.successful(Right(HttpResponse(200))))
      when(mockSessionService.storeCallbackData(any(),any())(any(),any())).thenReturn(Future.successful(Some(callbackData)))
      when(mockAuditEvents.totalRows(any(), argEq(schemeInfo))(any(), any())).thenReturn(true)
      val result = fileProcessingService.processFile(callbackData, "")(hc,schemeInfo, request)
      result mustBe 1
    }

    XMLTestData.staxIntegrationTests.foreach( rec => {
      val tempOdsPath = Files.createTempFile("file", ".ods").toFile
      val zip = new ZipOutputStream(new FileOutputStream(tempOdsPath))
      val entry = new ZipEntry("content.xml")
      entry.setExtra(rec._2.toString.getBytes())
      zip.putNextEntry(entry)
      zip.close()
      val inputStream = new FileInputStream(tempOdsPath)

      rec._1 in {
        when(mockErsFileValidatorConnector.upscanFileStream(argEq(callbackData.downloadUrl)))
          .thenReturn(inputStream)
        val result = fileProcessingService.readFile(callbackData.downloadUrl)
        result.map(_ must be (rec._3))
      }
    })
  }

  "yield a list of scheme data from file data with large file" in {
    val fileProcessingService: ProcessOdsService = new ProcessOdsService(mockDataGenerator, mockAuditEvents, mockErsFileValidatorConnector, mockSessionService, mockAppConfig, ec) {
      override val splitSchemes = true
      override val maxNumberOfRows = 1
      override def readFile(downloadUrl: String) = XMLTestData.getEMIAdjustmentsTemplateLarge
    }
    val listBuffer = ListBuffer(
      Seq("yes", "yes", "yes", "4", "1989-10-20", "Anthony", "Joe", "Jones", "AA123456A", "123/XZ55555555", "10.1232", "100.00", "10.2585", "10.2544"),
      Seq("yes", "yes", "yes", "4", "1989-10-20", "Anthony", "Joe", "Jones", "AA123456A", "123/XZ55555555", "10.1232", "100.00", "10.2585", "10.2544"),
      Seq("yes", "yes", "yes", "4", "1989-10-20", "Anthony", "Joe", "Jones", "AA123456A", "123/XZ55555555", "10.1232", "100.00", "10.2585", "10.2544")
    )
    when(mockDataGenerator.getErrors(any())(any(),any(),any())).thenReturn(createListBuffer(schemeInfo, "EMI40_Adjustments_V3", listBuffer))
    when(mockAuditEvents.totalRows(any(), argEq(schemeInfo))(any(), any())).thenReturn(true)
    when(mockErsFileValidatorConnector.sendToSubmissions(any[SchemeData](), any[String]())(any[HeaderCarrier],any[Request[_]])).thenReturn(Future.successful(Right(HttpResponse(200))))
    when(mockSessionService.storeCallbackData(any[UpscanCallback],any[Int])(any(), any[HeaderCarrier])).thenReturn(Future.successful(Some(callbackData)))
    val result = fileProcessingService.processFile(callbackData, "")(hc, schemeInfo, request)
    verify(mockErsFileValidatorConnector, times(3)).sendToSubmissions(any(), any[String]())(any[HeaderCarrier],any[Request[_]])
  }
//
//  "Csv files should be read successfully" in {
//    val fileProcessingService: ProcessOdsService = new ProcessOdsService(mockDataGenerator, mockAuditEvents, mockErsFileValidatorConnector, mockSessionService, mockAppConfig, ec) {
//      override val splitSchemes = false
//      override val maxNumberOfRows = 1
//      override def readFile(downloadUrl: String) = XMLTestData.getEMIAdjustmentsTemplateSTAX
//    }
//
//    when(mockErsFileValidatorConnector.sendToSubmissions(any[SchemeData](), any[String]())(any[HeaderCarrier],any[Request[_]])).thenReturn(
//      Future.successful(HttpResponse(200)))
//
//    for((csv,i) <- csvList.zipWithIndex){
//      when(mockErsFileValidatorConnector.upscanFileStream(argEq(callbackData.downloadUrl)))
//        .thenReturn(new ByteArrayInputStream(csv.getBytes))
//
//      val result = await(fileProcessingService.readCSVFile(callbackData.downloadUrl))
//      result.foreach{ seq =>
//        for((value,count) <- seq.zipWithIndex){
//          value mustBe expectedDataList(i)(count)
//        }
//      }
//    }
//  }
}
