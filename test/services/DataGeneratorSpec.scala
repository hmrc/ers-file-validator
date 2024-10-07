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

import com.typesafe.config.ConfigFactory
import config.ApplicationConfig
import models.{ERSFileProcessingException, SchemeInfo}
import org.mockito.ArgumentMatchers.{any, eq => argEq}
import org.mockito.Mockito._
import org.scalatest.{BeforeAndAfter, EitherValues}
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.mvc.Request
import services.audit.AuditEvents
import services.headers.HeaderData
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.services.validation.DataValidator
import uk.gov.hmrc.services.validation.models.{Cell, Row, ValidationError}
import utils.ErrorResponseMessages

import java.time.ZonedDateTime
import scala.collection.immutable.ArraySeq
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}
import scala.util.Try

class DataGeneratorSpec extends PlaySpec with CSVTestData with ScalaFutures with MockitoSugar with BeforeAndAfter with EitherValues with HeaderData {
  // scalastyle:off magic.number
  val mockAuditEvents: AuditEvents = mock[AuditEvents]
  val mockAppConfig: ApplicationConfig = mock[ApplicationConfig]
  implicit val ec: ExecutionContextExecutor = ExecutionContext.global
  def dataGenerator = new DataGenerator(mockAuditEvents, mockAppConfig)

  val schemeInfo: SchemeInfo = SchemeInfo(
    schemeRef = "XA11999991234567",
    timestamp = ZonedDateTime.now,
    schemeId = "123PA12345678",
    taxYear = "2014/F15",
    schemeName = "MyScheme",
    schemeType = "EMI"
  )

  implicit val request: Request[_] = mock[Request[_]]
  implicit val hc: HeaderCarrier = mock[HeaderCarrier]

  val testAct = List("", "", "", "")

  before {
    reset(mockAuditEvents)
  }

  "validateHeaderRow" should {
    "return with an error if the sheet name isn't recognised" in {
      val result = Try(dataGenerator.validateHeaderRow(XMLTestData.otherHeaderSheet1Data, "csopHeaderSheet1Data")(schemeInfo, hc, request))
      result.isFailure must be(true)
      verify(mockAuditEvents, times(1)).fileProcessingErrorAudit(argEq(schemeInfo), argEq("csopHeaderSheet1Data"), argEq("Could not set the validator"))(any(), any())
    }

    // FIXME: Potentially add validator tests for SAYE header
    Seq(
      // V4 CSOP schemes
      ("CSOP_OptionsGranted_V4", csopHeaderSheet1Data, 9, false),
      ("CSOP_OptionsRCL_V4", csopHeaderSheet2Data, 9, false),
      ("CSOP_OptionsExercised_V4", csopHeaderSheet3Data, 20, false),
      // V5 CSOP schemes
      ("CSOP_OptionsGranted_V5", csopHeaderSheet1DataV5, 9, true),
      ("CSOP_OptionsRCL_V5", csopHeaderSheet2Data, 9, true),
      ("CSOP_OptionsExercised_V5", csopHeaderSheet3Data, 20, true),
      // SIP schemes
      ("SIP_Awards_V4", sipHeaderSheet1Data, 17, false),
      ("SIP_Out_V4", sipHeaderSheet2Data, 17, false),
      // EMI schemes
      ("EMI40_Adjustments_V4", emiHeaderSheet1Data, 14, false),
      ("EMI40_Replaced_V4", emiHeaderSheet2Data, 17, false),
      ("EMI40_RLC_V4", emiHeaderSheet3Data, 12, false),
      ("EMI40_NonTaxable_V4", emiHeaderSheet4Data, 15, false),
      ("EMI40_Taxable_V4", emiHeaderSheet5Data, 20, false),
      // OTHER schemes
      ("Other_Grants_V4", otherHeaderSheet1Data, 4, false),
      ("Other_Options_V4", otherHeaderSheet2Data, 42, false),
      ("Other_Acquisition_V4", otherHeaderSheet3Data, 40, false),
      ("Other_RestrictedSecurities_V4", otherHeaderSheet4Data, 20, false),
      ("Other_OtherBenefits_V4", otherHeaderSheet5Data, 13, false),
      ("Other_Convertible_V4", otherHeaderSheet6Data, 15, false),
      ("Other_Notional_V4", otherHeaderSheet7Data, 13, false),
      ("Other_Enhancement_V4", otherHeaderSheet8Data, 14, false),
      ("Other_Sold_V4", otherHeaderSheet9Data, 14, false)
    ).foreach{
      case (schemeName, headerData, headerSize, v5Scheme) =>
        s"validate $schemeName headerRow as valid" in {
          when(mockAppConfig.csopV5Enabled).thenReturn(v5Scheme)
          val schemeInfoCorrectVersion = if (v5Scheme){
            schemeInfo.copy(taxYear = "2023/24")
          }
          else {
            schemeInfo
          }
          dataGenerator.validateHeaderRow(headerData, schemeName)(schemeInfoCorrectVersion, hc, request) must be(headerSize)
        }
    }
  }

  "setValidator" should {
    "return a DataValidator if the given sheet name is valid" in {
      val maybeDataValidator: Either[ERSFileProcessingException, DataValidator] =
        dataGenerator.getValidator("EMI40_Adjustments_V4")(SchemeInfo("", ZonedDateTime.now(), "", "2023/24", "", ""), hc, request)
      assert(maybeDataValidator.isRight)
    }

    "throw an exception if the given sheet name is not valid" in {
      dataGenerator.getValidator("Invalid")(SchemeInfo("", ZonedDateTime.now(), "" ,"" ,"", ""), hc, request)
        .left
        .map { (exception: ERSFileProcessingException) =>
          assert(exception.message === "Failed to find the config file")
          assert(exception.context === "Sheet name: Invalid does not match any for scheme types.")
      }
    }

    "throw an exception if the given sheet name maps to a config file which does not exist" in {
      dataGenerator.getValidator("CSOP_OptionsGranted_V4")(SchemeInfo("", ZonedDateTime.now(), "" ,"" ,"", ""), hc, request)
        .left
        .map { (exception: ERSFileProcessingException) =>
          assert(exception.message === "Failed to find the config file")
          assert(exception.context === "Could not set the validator due to a missing config.")
        }
    }
  }

  "getValidatorAndSheetInfo" should {
    "return a Right with a DataValidator if the given sheet name is valid" in {
      dataGenerator.getValidatorAndSheetInfo("EMI40_Adjustments_V4", SchemeInfo("", ZonedDateTime.now(), "", "2023/24", "", "")) match {
        case Left(_) => fail("Did not return validator")
        case Right(_) => succeed
      }
    }

    "return a left with an exception if the given sheet name is not valid" in {
      dataGenerator.getValidatorAndSheetInfo("Invalid", SchemeInfo("", ZonedDateTime.now(), "", "2023/24", "", "")) match {
        case Left(_) => succeed
        case Right(_) => fail("Did not return expected exception")
      }
    }
  }

  "identifyAndDefineSheet" should {
    "identify and define the sheet with correct scheme type" in {
      dataGenerator.identifyAndDefineSheet("EMI40_Adjustments_V4")(schemeInfo, hc, request) mustBe "EMI40_Adjustments_V4"
    }

    "return an error indicating the sheet name indicating the sheet name isn't as expected" in {
      val result: ERSFileProcessingException =
        intercept[ERSFileProcessingException](dataGenerator.identifyAndDefineSheet("EMI40_Adjustments")(schemeInfo, hc, request))
      assert(result.message === "Incorrect ERS Template - Sheet Name isn't as expected")
      assert(result.context === "Couldn't find config for given SheetName, sheet name may be incorrect")
      verify(mockAuditEvents, times(1))
        .fileProcessingErrorAudit(argEq(schemeInfo), argEq("EMI40_Adjustments"), argEq("Could not set the validator"))(any(), any())
    }

  "return an error indicating the sheet type is not as expected" in {
    val schemeInfoWithWrongSchemeType: SchemeInfo = schemeInfo.copy(schemeType = "CSOP")
    val result: ERSFileProcessingException =
      intercept[ERSFileProcessingException](dataGenerator.identifyAndDefineSheet("EMI40_Adjustments_V4")(schemeInfoWithWrongSchemeType, hc, request))
    assert(result.message === "Incorrect ERS Template - Sheet Name isn't as expected")
    assert(result.context === "Incorrect ERS Template - Scheme Type isn't as expected, expected: CSOP parsed: EMI")
    verify(mockAuditEvents, times(1))
      .fileProcessingErrorAudit(argEq(schemeInfoWithWrongSchemeType), argEq("EMI40_Adjustments_V4"), argEq("emi is not equal to csop"))(any(), any())
  }

    "return an error when scheme types do not match" in {
      val schemeInfo2: SchemeInfo = SchemeInfo(
        schemeRef = "XA11999991234567",
        timestamp = ZonedDateTime.now,
        schemeId = "123PA12345678",
        taxYear = "2014/F15",
        schemeName = "MyScheme",
        schemeType = "CSOP"
      )
      val result = Try(dataGenerator.identifyAndDefineSheet("EMI40_Adjustments_V4")(schemeInfo2, hc, request))
      result.isFailure must be(true)
      verify(mockAuditEvents, times(1)).fileProcessingErrorAudit(argEq(schemeInfo2), argEq("EMI40_Adjustments_V4"), argEq("emi is not equal to csop"))(any(), any())
    }
  }

  "isBlankRow" should {
    "return true when row is blank" in {
      dataGenerator.isBlankRow(testAct) must be(true)
    }

    "return true when row is blank and has white space" in {
      val testAct1 = List("  ", "  ", "   ", "  ")
      dataGenerator.isBlankRow(testAct1) must be(true)
    }

    "return false when row is not blank" in {
      val testAct1 = List("dfgdg", "", "", "")
      dataGenerator.isBlankRow(testAct1) must be(false)
    }
  }

  "generateRowData" should {
    val validator = new DataValidator(ConfigFactory.load.getConfig("ers-other-grants-validation-config"))

    "return the data when parsed correctly and no errors are found" in {
      val result = dataGenerator.generateRowData(XMLTestData.otherGrantsExpData, 10, validator)(schemeInfo, "Other_Grants_V4", hc, request)
      result must be(XMLTestData.otherGrantsExpData)
    }

    "return an error when an issue is found" in {
      val err = List(ValidationError(Cell("A",10,""),"error.1","001","Enter a date that matches the yyyy-mm-dd pattern."))
      val res1 = Try(dataGenerator.generateRowData(testAct, 10, validator)(schemeInfo, "Other_Grants_V4", hc, request))
      res1.isFailure mustBe true
      verify(mockAuditEvents, times(1)).validationErrorAudit(argEq(err), argEq(schemeInfo), argEq("Other_Grants_V4"))(any(), any())
    }

    "throw exception if ErsValidator.validateRow throws exception" in {
      val mockValidator = mock[DataValidator]
      when(mockValidator.validateRow(any[Row])).thenThrow(new RuntimeException("this is a runtime exception"))

     val caughtException = intercept[RuntimeException] {
       dataGenerator.generateRowData(testAct, 10, mockValidator)(schemeInfo, "Other_Grants_V4", hc, request)
     }

      caughtException.getMessage mustBe "this is a runtime exception"
    }
  }

  "getData" should {
    "get an exception if ods file has less than 9 rows and doesn't have header data" in {
      val schemeInfo: SchemeInfo = SchemeInfo (
        schemeRef = "XA11000001231275",
        timestamp = ZonedDateTime.now,
        schemeId = "123PA12345678",
        taxYear = "2014/F15",
        schemeName = "MyScheme",
        schemeType = "CSOP"
      )
      val result = intercept[ERSFileProcessingException] {
        dataGenerator.getErrors(XMLTestData.getInvalidCSOPWithoutHeaders)(schemeInfo, hc, request)
      }
      result.message mustBe "Incorrect ERS Template - Header doesn't match"
    }

    "get an exception if ods file has more than 1 sheet but 1 of the sheets has less than 9 rows and doesn't have header data" in {
      val schemeInfo: SchemeInfo = SchemeInfo (
        schemeRef = "XA11000001231275",
        timestamp = ZonedDateTime.now,
        schemeId = "123PA12345678",
        taxYear = "2014/F15",
        schemeName = "MyScheme",
        schemeType = "CSOP"
      )
      val result = intercept[ERSFileProcessingException] {
        dataGenerator.getErrors(XMLTestData.getInvalidCSOPWith2Sheets1WithoutHeaders)(schemeInfo, hc, request)
      }
      result.message mustBe "Incorrect ERS Template - Header doesn't match"
    }

    "get an exception if ods file doesn't contain any data" in {
      val schemeInfo: SchemeInfo = SchemeInfo (
        schemeRef = "XA11000001231275",
        timestamp = ZonedDateTime.now,
        schemeId = "123PA12345678",
        taxYear = "2014/F15",
        schemeName = "MyScheme",
        schemeType = "CSOP"
      )
      val result = intercept[ERSFileProcessingException] {
        dataGenerator.getErrors(XMLTestData.getCSOPWithoutData)(schemeInfo, hc, request)
      }
      result.message mustBe "The file that you chose doesn’t have any data after row 9. The reportable events data must start in cell A10.<br/><a href=\"https://www.gov.uk/government/collections/employment-related-securities\">Use the ERS guidance documents</a> to help you create error-free files."
    }

    "get Data for Iterator of Strings" in {
      val result = dataGenerator.getErrors(XMLTestData.getEMIAdjustmentsTemplate)(schemeInfo,hc,request)
      result.size must be (1)
      result.foreach(_.data.foreach(_ mustBe (XMLTestData.emiAdjustmentsExpData)))
      try {
        dataGenerator.getErrors(XMLTestData.getIncorrectsheetNameTemplate)(schemeInfo,hc,request)
      } catch {
        case e:Throwable => e.getMessage mustBe "Incorrect ERS Template - Sheet Name isn't as expected"
      }
      verify(mockAuditEvents, times(1)).fileProcessingErrorAudit(argEq(schemeInfo), argEq("EMI40_Adjustment"), argEq("Could not set the validator"))(any(), any())
    }

    "get mandatory Data for Iterator of Strings" in {
      val result = dataGenerator.getErrors(XMLTestData.getEMIReplacedTemplate)(schemeInfo,hc,request)
      result.size must be (1)
      result.foreach(_.data.foreach(_ must be (XMLTestData.emiReplacedExpMandatoryData)))
    }

    "expand repeated rows" in {
      val result = dataGenerator.getErrors(XMLTestData.getEMIAdjustmentsRepeatedTemplate)(schemeInfo,hc,request)
      result.size mustEqual(1)
      result.head.data.size mustEqual(4)
    }
  }

  "constructColumnData" should {
    val emiAdjustmentsColCount = 14

    "trim a column to return a dataset that corresponds with the header size" in {
      val result = ArraySeq.unsafeWrapArray(emiAdjustmentsTooLong.split(","))
      result.size mustBe emiAdjustmentsColCount
      result.size must be < emiAdjustmentsTooLong.size
    }

    "pad a column to return a dataset that corresponds with the header size" in {
      val emiAdjustmentsOptionalEndSeq = emiAdjustmentsOptionalEnd.split(",")
      val result = dataGenerator.constructColumnData(emiAdjustmentsOptionalEndSeq.toSeq,emiAdjustmentsColCount)
      result.size mustBe emiAdjustmentsColCount
      result.size must be > emiAdjustmentsOptionalEndSeq.size
    }

    "return the same sized data set if all columns are answered and present" in {
      val emiAdjustmentsCollectionSeq = emiAdjustmentsCollection.split(",")
      val result = dataGenerator.constructColumnData(emiAdjustmentsCollectionSeq.toSeq,emiAdjustmentsColCount)
      result.size mustBe emiAdjustmentsColCount
      result.size mustBe emiAdjustmentsCollectionSeq.size
    }

  }

  "getSheetCsv" should {
    def testServiceCreator(sheets: Map[String, SheetInfo]) = new DataGenerator(
      mockAuditEvents, mockAppConfig
    ) {
      override def ersSheetsConf(schemeInfo: SchemeInfo): Map[String, SheetInfo] = sheets
    }

    when(mockAuditEvents.fileProcessingErrorAudit(any(), any(), any())(any(), any())).thenReturn(true)

    "return a sheet info if sheetname is found in the sheets" in {
      val sheetTest: SheetInfo = SheetInfo("schemeType", 1, "sheetName", "sheetTitle", "configFileName", List("aHeader"))
      val testService: DataGenerator = testServiceCreator(Map("aName" -> sheetTest))

      val result = testService.getSheetCsv("aName", schemeInfo)
      assert(result.isRight)
      result.value mustBe sheetTest
    }

    "return a left if sheetname is not found in sheets" in {

      val sheetTest: SheetInfo = SheetInfo("schemeType", 1, "sheetName", "sheetTitle", "configFileName", List("aHeader"))
      val testService: DataGenerator = testServiceCreator(Map("anotherName" -> sheetTest))

      val result = testService.getSheetCsv("aWrongName", schemeInfo)
      assert(result.isLeft)
      result.swap.value mustBe ERSFileProcessingException(
        s"${ErrorResponseMessages.dataParserIncorrectSheetName}",
        s"${ErrorResponseMessages.dataParserUnidentifiableSheetNameContext}")

    }
  }

}
