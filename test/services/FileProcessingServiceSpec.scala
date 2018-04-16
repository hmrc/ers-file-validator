/*
 * Copyright 2018 HM Revenue & Customs
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

import java.io.ByteArrayInputStream

import connectors.ERSFileValidatorConnector
import metrics.Metrics
import models._
import org.joda.time.DateTime
import org.mockito.Matchers
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.{OneServerPerSuite, PlaySpec}
import play.api.libs.iteratee.Enumerator
import play.api.libs.json.{Json, Writes}
import play.api.mvc.Request
import play.api.test.Helpers._
import services.audit.AuditEvents
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.stream.BulkEntityProcessor

import scala.concurrent.Future


class FileProcessingServiceSpec extends PlaySpec with CSVTestData with OneServerPerSuite with ScalaFutures with MockitoSugar with BeforeAndAfter{

  val mockBulkEntityProcessor = mock[BulkEntityProcessor[Array[String]]]
  val mockSessionService = mock[SessionService]
  val mockErsConnector = mock[ERSFileValidatorConnector]
  val mockDataGenerator = mock[DataGenerator]
  val mockHeaderCarrier = mock[HeaderCarrier]

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

  val callbackData = CallbackData("ers-files", "id1", 1024, Option("csop.ods"), Option("ods"), None,None)
  val callbackDataCSV = CallbackData("ers-files", "id1", 1024, Option("EMI40_Adjustments_V3"), Option("csv"), None,None)
  val metrics = mock[Metrics]

  def xmlSourceData() =  Future.successful(Enumerator(xmlData1.toString.getBytes()))

  val xmlData1 = <table:table table:name="EMI40_Adjustments_V3" table:style-name="ta1"> <table:table-row table:style-name="ro6"><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>1.</text:p><text:p>Date of event</text:p><text:p>(yyyy-mm-dd)</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>2.</text:p><text:p>Number of employees who acquired or were awarded shares</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>3.</text:p><text:p>Type of shares awarded</text:p><text:p>Enter a number from 1 to 4 depending on the type of share awarded. Follow the link at cell B10 for a list of the types of share which can be awarded</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>4.</text:p><text:p>If free shares, are performance conditions attached to their award?</text:p><text:p>(yes/no)</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>5.</text:p><text:p>If matching shares, what is the ratio of shares to partnership shares?</text:p><text:p>Enter ratio for example 2:1; 2/1</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>6.</text:p><text:p>Unrestricted market value (UMV) per share on acquisition or award</text:p><text:p>£</text:p><text:p>e.g. 10.1234</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>7.</text:p><text:p>Total number of shares acquired or awarded</text:p><text:p>e.g. 100.00</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>8.</text:p><text:p>Total value of shares acquired or awarded</text:p><text:p>£</text:p><text:p>e.g. 10.1234</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>9.</text:p><text:p>Total number of employees whose award of free shares during the year exceeded the limit of £3,600</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>10.</text:p><text:p>Total number of employees whose award of free shares during the year was at or below the limit of £3,600</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>11.</text:p><text:p>Total number of employees whose award of partnership shares during the year exceeded the limit of £1,800</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>12.</text:p><text:p>Total number of employees whose award of partnership shares during the year was at or below the limit of £1,800</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>13.</text:p><text:p>Total number of employees whose award of matching shares during the year exceeded the limit of £3,600</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>14.</text:p><text:p>Total number of employees whose award of matching shares during the year was at or below the limit of £3,600</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>15.</text:p><text:p>Are the shares listed on a recognised stock exchange?</text:p><text:p>(yes/no)</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>16.</text:p><text:p>If no, was the market value agreed with HMRC?</text:p><text:p>(yes/no)</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>17.</text:p><text:p>If yes, enter the HMRC reference given</text:p></table:table-cell><table:table-cell table:style-name="ce11"/><table:table-cell table:number-columns-repeated="1003"/><table:table-cell table:style-name="ce17"/><table:table-cell table:number-columns-repeated="2"/></table:table-row></table:table>
  val xmlData2 = <table:table-row table:style-name="ro6"><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>1.</text:p><text:p>Date of event</text:p><text:p>(yyyy-mm-dd)</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>2.</text:p><text:p>Number of employees who acquired or were awarded shares</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>3.</text:p><text:p>Type of shares awarded</text:p><text:p>Enter a number from 1 to 4 depending on the type of share awarded. Follow the link at cell B10 for a list of the types of share which can be awarded</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>4.</text:p><text:p>If free shares, are performance conditions attached to their award?</text:p><text:p>(yes/no)</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>5.</text:p><text:p>If matching shares, what is the ratio of shares to partnership shares?</text:p><text:p>Enter ratio for example 2:1; 2/1</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>6.</text:p><text:p>Unrestricted market value (UMV) per share on acquisition or award</text:p><text:p>£</text:p><text:p>e.g. 10.1234</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>7.</text:p><text:p>Total number of shares acquired or awarded</text:p><text:p>e.g. 100.00</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>8.</text:p><text:p>Total value of shares acquired or awarded</text:p><text:p>£</text:p><text:p>e.g. 10.1234</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>9.</text:p><text:p>Total number of employees whose award of free shares during the year exceeded the limit of £3,600</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>10.</text:p><text:p>Total number of employees whose award of free shares during the year was at or below the limit of £3,600</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>11.</text:p><text:p>Total number of employees whose award of partnership shares during the year exceeded the limit of £1,800</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>12.</text:p><text:p>Total number of employees whose award of partnership shares during the year was at or below the limit of £1,800</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>13.</text:p><text:p>Total number of employees whose award of matching shares during the year exceeded the limit of £3,600</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>14.</text:p><text:p>Total number of employees whose award of matching shares during the year was at or below the limit of £3,600</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>15.</text:p><text:p>Are the shares listed on a recognised stock exchange?</text:p><text:p>(yes/no)</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>16.</text:p><text:p>If no, was the market value agreed with HMRC?</text:p><text:p>(yes/no)</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>17.</text:p><text:p>If yes, enter the HMRC reference given</text:p></table:table-cell><table:table-cell table:style-name="ce11"/><table:table-cell table:number-columns-repeated="1003"/><table:table-cell table:style-name="ce17"/><table:table-cell table:number-columns-repeated="2"/></table:table-row>


  object testFileProcessingService extends FileProcessingService {
    override val sessionService = mockSessionService
    override val ersConnector = mockErsConnector
    override val auditEvents:AuditEvents = mock[AuditEvents]
  }

  "The File Processing Service" must {

    "yield a list of scheme data from file data" in {

      when(mockErsConnector.sendToSubmissions(Matchers.any[SchemeData](), anyString())(any[HeaderCarrier],any[Request[_]])).thenReturn(Future.successful(HttpResponse(200)))
      object testFileProcessingService extends FileProcessingService {
        override val splitSchemes = false
        override val maxNumberOfRows = 1
        override val sessionService = mockSessionService
        override val ersConnector = mockErsConnector
        override def readFile(collection: String, id: String) = XMLTestData.getEMIAdjustmentsTemplateSTAX
        override val auditEvents:AuditEvents = mock[AuditEvents]

        when(mockSessionService.storeCallbackData(Matchers.any(),Matchers.any())(Matchers.any(),Matchers.any())).thenReturn(Future.successful(Some(callbackData)))
      }
      val result = testFileProcessingService.processFile(callbackData, "")(hc,schemeInfo, request)
      result mustBe 1
    }

    XMLTestData.staxIntegrationTests.foreach( rec => {

      rec._1 in {
        when(mockErsConnector.readAttachmentUri(Matchers.any(),Matchers.any())).thenReturn(new ByteArrayInputStream(rec._2.toString.getBytes()))
        val result = testFileProcessingService.readFile(callbackData.collection,callbackData.id)
        result.map(_ must be (rec._3))
      }
    })


  }

  "yield a list of scheme data from file data with large file" in {
    when(mockErsConnector.sendToSubmissions(Matchers.any[SchemeData](), anyString())(any[HeaderCarrier],any[Request[_]])).thenReturn(Future.successful(HttpResponse(200)))

    object testFileProcessingService1 extends FileProcessingService {
      override val sessionService = mockSessionService
      override val ersConnector = mockErsConnector
      override val splitSchemes = true
      override val maxNumberOfRows = 1
      override def readFile(collection: String, id: String) = XMLTestData.getEMIAdjustmentsTemplateLarge
      override val auditEvents:AuditEvents = mock[AuditEvents]

      when(mockSessionService.storeCallbackData(any[CallbackData],any[Int])
        (Matchers.any(), any[HeaderCarrier])).thenReturn(Future.successful(Some(callbackData)))
    }

    val result = testFileProcessingService1.processFile(callbackData, "")(hc, schemeInfo, request)
    verify(mockErsConnector, times(3)).sendToSubmissions(Matchers.any(), anyString())(any[HeaderCarrier],any[Request[_]])
  }

  before {
    reset(mockErsConnector)
    reset(mockSessionService)
  }

  "Csv files should be read successfully" in {

    object testFileProcessingService extends FileProcessingService {
      override val splitSchemes = false
      override val maxNumberOfRows = 1
      override val sessionService = mockSessionService
      override val ersConnector = mockErsConnector
      override def readFile(collection: String, id: String) = XMLTestData.getEMIAdjustmentsTemplateSTAX
      override val auditEvents:AuditEvents = mock[AuditEvents]
    }

    when(mockErsConnector.sendToSubmissions(Matchers.any[SchemeData](), anyString())(any[HeaderCarrier],any[Request[_]])).thenReturn(
      Future.successful(HttpResponse(200)))


    for((csv,i) <- csvList.zipWithIndex){
      when(mockErsConnector.readAttachmentUri(Matchers.any(),Matchers.any())).thenReturn(
        new ByteArrayInputStream(csv.getBytes))

      val result = await(testFileProcessingService.readCSVFile(callbackData))
      result.foreach{ seq =>
        for((value,count) <- seq.zipWithIndex){
          value mustBe expectedDataList(i)(count)
        }
      }
    }

  }

}
