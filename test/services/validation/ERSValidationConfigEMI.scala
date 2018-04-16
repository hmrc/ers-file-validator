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

package services.validation

import com.typesafe.config.ConfigFactory
import uk.gov.hmrc.services.validation.{Cell, DataValidator, Row, ValidationError}
import org.scalatestplus.play.PlaySpec

import services.validation.EMITestData.{ERSValidationEMIRLCTestData, ERSValidationEMINonTaxableTestData, ERSValidationEMIReplacedTestData, ERSValidationEMITaxableTestData, ERSValidationEMIAdjustmentsTestData}


class EMIAdjustmentsV3ValidationTest extends PlaySpec with ERSValidationEMIAdjustmentsTestData with ValidationTestRunner {

  "ERS Validation tests for EMI Adjustments" should {
    val validator = DataValidator(ConfigFactory.load.getConfig("ers-emi-adjustments-validation-config"))
    runTests(validator, getDescriptions, getTestData, getExpectedResults)

    "when Column A is answered yes, column B is a mandatory field" in {
      val cellB = Cell("B", rowNumber, "")
      val cellA = Cell("A", rowNumber, "yes")
      val row = Row(1,Seq(cellB,cellA))
      val resOpt: Option[List[ValidationError]] = validator.validateRow(row, Some(ValidationContext))
      resOpt mustBe Some(List(
        ValidationError(cellB,"mandatoryB","B01","Enter 'yes' or 'no'.")
      ))
    }

    "when Column B is answered yes, column C is a mandatory field" in {
      val cellC = Cell("C", rowNumber, "")
      val cellB = Cell("B", rowNumber, "yes")
      val row = Row(1,Seq(cellC,cellB))
      val resOpt: Option[List[ValidationError]] = validator.validateRow(row, Some(ValidationContext))
      resOpt mustBe Some(List(
        ValidationError(cellC,"mandatoryC","C01","Enter 'yes' or 'no'.")
      ))
    }

    "when Column C is answered yes, column D is a mandatory field" in {
      val cellD = Cell("D", rowNumber, "")
      val cellC = Cell("C", rowNumber, "yes")
      val row = Row(1,Seq(cellD,cellC))
      val resOpt: Option[List[ValidationError]] = validator.validateRow(row, Some(ValidationContext))
      resOpt mustBe Some(List(
        ValidationError(cellD,"mandatoryD","D01","Enter '1', '2', '3', '4', '5', '6', '7' or '8'.")
      ))
    }

    "when a valid row of data is provided, no ValidationErrors should be raised" in {
      val row = Row(1,getValidRowData)
      val resOpt: Option[List[ValidationError]] = validator.validateRow(row, Some(ValidationContext))
      resOpt mustBe None
    }

    "when a invalid row of data is provided, a list of ValidationErrors should be raised" in {
      val row = Row(1,getInvalidRowData)
      val resOpt: Option[List[ValidationError]] = validator.validateRow(row, Some(ValidationContext))
      resOpt.get.size mustBe getInvalidRowData.size
    }

  }

}

class EMIReplacedV3ValidationTest extends PlaySpec with ERSValidationEMIReplacedTestData with ValidationTestRunner{

  "ERS EMI Replaced Validation Test" should {
    val validator = DataValidator(ConfigFactory.load.getConfig("ers-emi-replaced-validation-config"))
    runTests(validator, getDescriptions, getTestData, getExpectedResults)
  }

}
class EMIRLCV3ValidationTest extends PlaySpec with ERSValidationEMIRLCTestData with ValidationTestRunner{


  "ERS EMI RLC Validation Test" should {
    val validator = DataValidator(ConfigFactory.load.getConfig("ers-emi-rlc-validation-config"))
    runTests(validator, getDescriptions, getTestData, getExpectedResults)

    "when Column K is answered yes, column L is a mandatory field" in {
      val cellC = Cell("C", rowNumber, "")
      val cellB = Cell("B", rowNumber, "yes")
      val row = Row(1, Seq(cellC, cellB))
      val resOpt: Option[List[ValidationError]] = validator.validateRow(row, Some(ValidationContext))
      resOpt mustBe Some(List(
        ValidationError(cellC, "mandatoryC", "C01", "Enter '1', '2', '3', '4', '5', '6', '7' or '8'.")
      ))
    }
    "when Column J is answered yes, column K is a mandatory field" in {
      val cellK = Cell("K", rowNumber, "")
      val cellJ = Cell("J", rowNumber, "yes")
      val row = Row(1, Seq(cellK, cellJ))
      val resOpt: Option[List[ValidationError]] = validator.validateRow(row, Some(ValidationContext))
      resOpt mustBe Some(List(
        ValidationError(cellK, "mandatoryK", "K01", "Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it).")
      ))
    }
    "when Column K is answered, column L is a mandatory field" in {
      val cellL = Cell("L", rowNumber, "")
      val cellK = Cell("K", rowNumber, "10.1234")
      val row = Row(1, Seq(cellL, cellK))
      val resOpt: Option[List[ValidationError]] = validator.validateRow(row, Some(ValidationContext))
      resOpt mustBe Some(List(
        ValidationError(cellL, "mandatoryL", "L01", "Enter 'yes' or 'no'.")
      ))
    }
  }
}

class EMINonTaxableV3ValidationTest extends PlaySpec with ERSValidationEMINonTaxableTestData with ValidationTestRunner{

  "ERS EMI Replaced Exercised Validation Test" should {

    val validator = DataValidator(ConfigFactory.load.getConfig("ers-emi-nontaxable-validation-config"))
    runTests(validator, getDescriptions, getTestData, getExpectedResults)

    "when Column K is answered no, column L is a mandatory field" in {
      val cellL = Cell("L", rowNumber, "")
      val cellK = Cell("K", rowNumber, "no")
      val row = Row(1,Seq(cellL,cellK))
      val resOpt: Option[List[ValidationError]] = validator.validateRow(row, Some(ValidationContext))
      resOpt mustBe Some(List(
        ValidationError(cellL,"mandatoryL","L01","Enter 'yes' or 'no'.")
      ))
    }

    "when Column L is answered yes, column M is a mandatory field" in {
      val cellM = Cell("M", rowNumber, "")
      val cellL = Cell("L", rowNumber, "yes")
      val row = Row(1,Seq(cellM,cellL))
      val resOpt: Option[List[ValidationError]] = validator.validateRow(row, Some(ValidationContext))
      resOpt mustBe Some(List(
        ValidationError(cellM,"mandatoryM","M01","Enter the HMRC reference (must be less than 11 characters).")
      ))
    }

    "when a valid row of data is provided, no ValidationErrors should be raised" in {
      val row = Row(1,getValidRowData)
      val resOpt: Option[List[ValidationError]] = validator.validateRow(row, Some(ValidationContext))
      resOpt mustBe None
    }

    "when a invalid row of data is provided, a list of ValidationErrors should be raised" in {
      val row = Row(1,getInvalidRowData)
      val resOpt: Option[List[ValidationError]] = validator.validateRow(row, Some(ValidationContext))
      resOpt.get.size mustBe getInvalidRowData.size
    }

  }

}


class EMITaxableV3ValidationTest extends PlaySpec with ERSValidationEMITaxableTestData with ValidationTestRunner {

  "ERS Validation tests for EMI Taxable" should {

    val validator = DataValidator(ConfigFactory.load.getConfig("ers-emi-taxable-validation-config"))
    runTests(validator, getDescriptions, getTestData, getExpectedResults)

    "when Column B is answered yes, column C is a mandatory field" in {
      val cellC = Cell("C", rowNumber, "")
      val cellB = Cell("B", rowNumber, "yes")
      val row = Row(1,Seq(cellC,cellB))
      val resOpt: Option[List[ValidationError]] = validator.validateRow(row, Some(ValidationContext))
      resOpt mustBe Some(List(
        ValidationError(cellC,"mandatoryC","C01","Enter '1', '2', '3', '4', '5', '6', '7' or '8'.")
      ))
    }

    "when Column O is answered yes, column P is a mandatory field" in {
      val cellP = Cell("P", rowNumber, "")
      val cellO = Cell("O", rowNumber, "no")
      val row = Row(1,Seq(cellP,cellO))
      val resOpt: Option[List[ValidationError]] = validator.validateRow(row, Some(ValidationContext))
      resOpt mustBe Some(List(
        ValidationError(cellP,"mandatoryP","P01","Enter 'yes' or 'no'.")
      ))
    }

    "when Column P is answered yes, column Q is a mandatory field" in {
      val cellQ = Cell("Q", rowNumber, "")
      val cellP = Cell("P", rowNumber, "yes")
      val row = Row(1,Seq(cellQ,cellP))
      val resOpt: Option[List[ValidationError]] = validator.validateRow(row, Some(ValidationContext))
      resOpt mustBe Some(List(
        ValidationError(cellQ,"mandatoryQ","Q01","Enter the HMRC reference (must be less than 11 characters).")
      ))
    }

    "when a valid row of data is provided, no ValidationErrors should be raised" in {
      val row = Row(1,getValidRowData)
      val resOpt: Option[List[ValidationError]] = validator.validateRow(row, Some(ValidationContext))
      resOpt mustBe None
    }

    "when a invalid row of data is provided, a list of ValidationErrors should be raised" in {
      val row = Row(1,getInvalidRowData)
      val resOpt: Option[List[ValidationError]] = validator.validateRow(row, Some(ValidationContext))
      resOpt.get.size mustBe getInvalidRowData.size
    }

  }

}
