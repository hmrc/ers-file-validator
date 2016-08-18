/*
 * Copyright 2016 HM Revenue & Customs
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
import uk.gov.hmrc.services.validation.DataValidator
import models.{ERSFileProcessingException, SchemeData, SchemeInfo}
import org.joda.time.DateTime
import org.scalatest.{Matchers, BeforeAndAfter}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.{OneServerPerSuite, PlaySpec}
import play.api.mvc.Request
import services.audit.AuditEvents
import services.headers.HeaderData
import uk.gov.hmrc.play.http.HeaderCarrier
import play.api.i18n.Messages
import scala.collection.mutable.ListBuffer
import scala.util.Try

/**
 * Created by raghu on 03/02/16.
 */
class DataGeneratorSpec extends PlaySpec with CSVTestData with OneServerPerSuite with ScalaFutures with MockitoSugar with BeforeAndAfter with HeaderData{

  object dataGeneratorObj extends DataGenerator {
    override val auditEvents:AuditEvents = mock[AuditEvents]
  }

  val schemeInfo: SchemeInfo = SchemeInfo (
    schemeRef = "XA11999991234567",
    timestamp = DateTime.now,
    schemeId = "123PA12345678",
    taxYear = "2014/F15",
    schemeName = "MyScheme",
    schemeType = "EMI"
  )

  implicit val request : Request[_] = mock[Request[_]]
  implicit val hc:HeaderCarrier = mock[HeaderCarrier]

  val testAct = List("","","","")
  "The File Processing Service" must {

    "validateHeaderRow " in {
      dataGeneratorObj.validateHeaderRow(XMLTestData.otherHeaderSheet1Data, "Other_Grants_V3")(schemeInfo,hc,request) must be (4)
      val result = Try(dataGeneratorObj.validateHeaderRow(XMLTestData.otherHeaderSheet1Data,"csopHeaderSheet1Data")(schemeInfo,hc,request))
      result.isFailure must be (true)
    }

    "validate CSOP_OptionsGranted_V3 headerRow as valid" in {
      dataGeneratorObj.validateHeaderRow(csopHeaderSheet1Data, "CSOP_OptionsGranted_V3")(schemeInfo,hc,request) must be (9)
    }
    "validate CSOP_OptionsRCL_V3 headerRow as valid" in {
      dataGeneratorObj.validateHeaderRow(csopHeaderSheet2Data, "CSOP_OptionsRCL_V3")(schemeInfo,hc,request) must be (9)
    }
    "validate CSOP_OptionsExercised_V3 headerRow as valid" in {
      dataGeneratorObj.validateHeaderRow(csopHeaderSheet3Data, "CSOP_OptionsExercised_V3")(schemeInfo,hc,request) must be (20)
    }

    "validate SIP_Awards_V3 headerRow as valid" in {
      dataGeneratorObj.validateHeaderRow(sipHeaderSheet1Data, "SIP_Awards_V3")(schemeInfo,hc,request) must be (17)
    }
    "validate SIP_Out_V3 headerRow as valid" in {
      dataGeneratorObj.validateHeaderRow(sipHeaderSheet2Data, "SIP_Out_V3")(schemeInfo,hc,request) must be (17)
    }

    "validate EMI40_Adjustments_V3 headerRow as valid" in {
      dataGeneratorObj.validateHeaderRow(emiHeaderSheet1Data, "EMI40_Adjustments_V3")(schemeInfo,hc,request) must be (14)
    }
    "validate EMI40_Replaced_V3 headerRow as valid" in {
      dataGeneratorObj.validateHeaderRow(emiHeaderSheet2Data, "EMI40_Replaced_V3")(schemeInfo,hc,request) must be (17)
    }
    "validate EMI40_RLC_V3 headerRow as valid" in {
      dataGeneratorObj.validateHeaderRow(emiHeaderSheet3Data, "EMI40_RLC_V3")(schemeInfo,hc,request) must be (12)
    }
    "validate EMI40_NonTaxable_V3 headerRow as valid" in {
      dataGeneratorObj.validateHeaderRow(emiHeaderSheet4Data, "EMI40_NonTaxable_V3")(schemeInfo,hc,request) must be (15)
    }
    "validate EMI40_Taxable_V3 headerRow as valid" in {
      dataGeneratorObj.validateHeaderRow(emiHeaderSheet5Data, "EMI40_Taxable_V3")(schemeInfo,hc,request) must be (20)
    }

    "validate Other_Grants_V3 headerRow as valid" in {
      dataGeneratorObj.validateHeaderRow(otherHeaderSheet1Data, "Other_Grants_V3")(schemeInfo,hc,request) must be (4)
    }
    "validate Other_Options_V3 headerRow as valid" in {
      dataGeneratorObj.validateHeaderRow(otherHeaderSheet2Data, "Other_Options_V3")(schemeInfo,hc,request) must be (42)
    }
    "validate Other_Acquisition_V3 headerRow as valid" in {
      dataGeneratorObj.validateHeaderRow(otherHeaderSheet3Data, "Other_Acquisition_V3")(schemeInfo,hc,request) must be (40)
    }
    "validate Other_RestrictedSecurities_V3 headerRow as valid" in {
      dataGeneratorObj.validateHeaderRow(otherHeaderSheet4Data, "Other_RestrictedSecurities_V3")(schemeInfo,hc,request) must be (20)
    }
    "validate Other_OtherBenefits_V3 headerRow as valid" in {
      dataGeneratorObj.validateHeaderRow(otherHeaderSheet5Data, "Other_OtherBenefits_V3")(schemeInfo,hc,request) must be (13)
    }
    "validate Other_Convertible_V3 headerRow as valid" in {
      dataGeneratorObj.validateHeaderRow(otherHeaderSheet6Data, "Other_Convertible_V3")(schemeInfo,hc,request) must be (15)
    }
    "validate Other_Notional_V3 headerRow as valid" in {
      dataGeneratorObj.validateHeaderRow(otherHeaderSheet7Data, "Other_Notional_V3")(schemeInfo,hc,request) must be (13)
    }
    "validate Other_Enhancement_V3 headerRow as valid" in {
      dataGeneratorObj.validateHeaderRow(otherHeaderSheet8Data, "Other_Enhancement_V3")(schemeInfo,hc,request) must be (14)
    }
    "validate Other_Sold_V3 headerRow as valid" in {
      dataGeneratorObj.validateHeaderRow(otherHeaderSheet9Data, "Other_Sold_V3")(schemeInfo,hc,request) must be (14)
    }

    "identifyAndDefineSheet with correct scheme type" in  {
      dataGeneratorObj.identifyAndDefineSheet("EMI40_Adjustments_V3")(schemeInfo,hc,request) must be ("EMI40_Adjustments_V3")
      val result = Try(dataGeneratorObj.identifyAndDefineSheet("EMI40_Adjustments")(schemeInfo,hc,request))
      result.isFailure must be (true)
    }

    "isBlankRow" in {
      dataGeneratorObj.isBlankRow(testAct) must be (true)
      val testAct1 = List("dfgdg","","","")
      dataGeneratorObj.isBlankRow(testAct1) must be (false)
    }

    "generateRowData" in {
      val validator = DataValidator(ConfigFactory.load.getConfig("ers-emi-adjustments-validation-config"))
      val result = dataGeneratorObj.generateRowData(XMLTestData.emiAdjustmentsExpData, 10,validator)(schemeInfo,"Other_Grants_V3",hc,request)

      result must be (XMLTestData.emiAdjustmentsExpData)
      val res1 = Try(dataGeneratorObj.generateRowData(testAct,10,validator)(schemeInfo,"Other_Grants_V3",hc,request))
      res1.isFailure mustBe true

    }

    "get an exception if ods file has less than 9 rows and doesn't have header data" in {
      val schemeInfo: SchemeInfo = SchemeInfo (
        schemeRef = "XA11000001231275",
        timestamp = DateTime.now,
        schemeId = "123PA12345678",
        taxYear = "2014/F15",
        schemeName = "MyScheme",
        schemeType = "CSOP"
      )
      val result = intercept[ERSFileProcessingException] {
        dataGeneratorObj.getData(XMLTestData.getInvalidCSOPWithoutHeaders)(schemeInfo, hc, request)
      }
      result.message mustBe Messages("ers.exceptions.dataParser.incorrectHeader")
    }

    "get an exception if ods file has more than 1 sheet but 1 of the sheets has less than 9 rows and doesn't have header data" in {
      val schemeInfo: SchemeInfo = SchemeInfo (
        schemeRef = "XA11000001231275",
        timestamp = DateTime.now,
        schemeId = "123PA12345678",
        taxYear = "2014/F15",
        schemeName = "MyScheme",
        schemeType = "CSOP"
      )
      val result = intercept[ERSFileProcessingException] {
        dataGeneratorObj.getData(XMLTestData.getInvalidCSOPWith2Sheets1WithoutHeaders)(schemeInfo, hc, request)
      }
      result.message mustBe Messages("ers.exceptions.dataParser.incorrectHeader")
    }

    "get an exception if ods file doesn't contain any data" in {
      val schemeInfo: SchemeInfo = SchemeInfo (
        schemeRef = "XA11000001231275",
        timestamp = DateTime.now,
        schemeId = "123PA12345678",
        taxYear = "2014/F15",
        schemeName = "MyScheme",
        schemeType = "CSOP"
      )
      val result = intercept[ERSFileProcessingException] {
        dataGeneratorObj.getData(XMLTestData.getCSOPWithoutData)(schemeInfo, hc, request)
      }
      result.message mustBe Messages("ers.exceptions.dataParser.noData")
    }

    "addSheetData " in {
      val dataList :ListBuffer[SchemeData] = ListBuffer()
      val result = dataGeneratorObj.addSheetData(schemeInfo, "EMI40_Adjustments_V3", 12,ListBuffer(XMLTestData.emiAdjustmentsExpData),dataList)
      result.size mustEqual(0)
    }

    "get Data for Iterator of Strings" in {
      object dataGenObj extends DataGenerator {
        override val auditEvents:AuditEvents = mock[AuditEvents]
      }
      val result = dataGenObj.getData(XMLTestData.getEMIAdjustmentsTemplate)(schemeInfo,hc,request)
      result.size must be (1)
      result.foreach(_.data.foreach(_ must be (XMLTestData.emiAdjustmentsExpData)))
        try {
          dataGenObj.getData(XMLTestData.getIncorrectsheetNameTemplate)(schemeInfo,hc,request)
        } catch {
          case e:Throwable => e.getMessage must be (Messages("ers.exceptions.dataParser.incorrectSheetName"))
        }
    }

    "get mandatory Data for Iterator of Strings" in {
      object dataGenObj extends DataGenerator {
       override val auditEvents:AuditEvents = mock[AuditEvents]
      }
      val result = dataGenObj.getData(XMLTestData.getEMIReplacedTemplate)(schemeInfo,hc,request)
      result.size must be (1)
      result.foreach(_.data.foreach(_ must be (XMLTestData.emiReplacedExpMandatoryData)))
    }

  }

  "getCsvData" must {
    "validate csv from the ninth row" in {
      object dataGenObj extends DataGenerator {
        override val auditEvents:AuditEvents = mock[AuditEvents]
      }

      val x = Iterator(
        "no,no,yes,3,2015-12-09,John,Barry,Doe,AA123456A,123/XZ55555555,10.1234,100.12,10.1234,10.1234",
        "no,no,yes,3,2015-12-09,John,Barry,Doe,AA123456A,123/XZ55555555,10.1234,100.12,10.1234,10.1234",
        "no,no,yes,3,2015-12-09,John,Barry,Doe,AA123456A,123/XZ55555555,10.1234,100.12,10.1234,10.1234"
      )

      val result = dataGenObj.getCsvData(x)(schemeInfo, "EMI40_Adjustments_V3",hc,request)
      result.size must be (3)
    }

    "throw an exception if csv file is empty" in {
      object dataGenObj extends DataGenerator {
        override val auditEvents:AuditEvents = mock[AuditEvents]
      }

      val x = Iterator()

      val result = intercept[ERSFileProcessingException] {
        dataGenObj.getCsvData(x)(schemeInfo, "EMI40_Adjustments_V3",hc,request)
      }
      result.getMessage must be (Messages("ers_check_csv_file.noData", "EMI40_Adjustments_V3.csv"))
    }
  }

  "constructColumnData" must {

    object dataGenObj extends DataGenerator {
      override val auditEvents:AuditEvents = mock[AuditEvents]
    }

    val emiAdjustmentsColCount = 14

    "trim a column to return a dataset that corresponds with the header size" in {
      val result = dataGenObj.constructColumnData(emiAdjustmentsTooLong.split(","),emiAdjustmentsColCount)
      result.size mustBe emiAdjustmentsColCount
      result.size must be < emiAdjustmentsTooLong.size
    }

    "pad a column to return a dataset that corresponds with the header size" in {
      val emiAdjustmentsOptionalEndSeq = emiAdjustmentsOptionalEnd.split(",")
      val result = dataGenObj.constructColumnData(emiAdjustmentsOptionalEndSeq,emiAdjustmentsColCount)
           result.size mustBe emiAdjustmentsColCount
      result.size must be > emiAdjustmentsOptionalEndSeq.size
    }

    "return the same sized data set if all columns are answered and present" in {
      val emiAdjustmentsCollectionSeq = emiAdjustmentsCollection.split(",")
      val result = dataGenObj.constructColumnData(emiAdjustmentsCollectionSeq,emiAdjustmentsColCount)
      result.size mustBe emiAdjustmentsColCount
      result.size mustBe emiAdjustmentsCollectionSeq.size
    }

  }

}
