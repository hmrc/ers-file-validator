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

package services.validation

import com.typesafe.config.ConfigFactory
import uk.gov.hmrc.services.validation.{Cell, DataValidator, Row, ValidationError}
import org.scalatestplus.play.PlaySpec

import services.validation.OTHERTestData._

class OTHERGrantsV3ValidationTest extends PlaySpec with ERSValidationOTHERGrantsTestData with ValidationTestRunner {

  val validator = DataValidator(ConfigFactory.load.getConfig("ers-other-grants-validation-config"))

  "ERS Validation tests for OTHER Grants" should {
    runTests(validator, getDescriptions, getTestData, getExpectedResults)
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

class OTHEROptionsV3ValidationTest extends PlaySpec with ERSValidationOTHEROptionsTestData with ValidationTestRunner {

  val validator = DataValidator(ConfigFactory.load.getConfig("ers-other-options-validation-config"))

  "ERS Validation tests for OTHER Options" should {
    runTests(validator, getDescriptions, getTestData, getExpectedResults)
  }

  "when options Column B is answered yes, column C is a mandatory field" in {
    val cellC = Cell("C", rowNumber, "")
    val cellB = Cell("B", rowNumber, "yes")
    val row = Row(1,Seq(cellC,cellB))
    val resOpt: Option[List[ValidationError]] = validator.validateRow(row, Some(ValidationContext))
    resOpt mustBe Some(List(
      ValidationError(cellC,"mandatoryC","C01","Enter the scheme reference number (it should be an 8 digit number).")
    ))
  }



  "when options Column AL is answered yes, column AM is a mandatory field" in {
    val cellAM = Cell("AM", rowNumber, "")
    val cellAL = Cell("AL", rowNumber, "yes")
    val row = Row(1,Seq(cellAM,cellAL))
    val resOpt: Option[List[ValidationError]] = validator.validateRow(row, Some(ValidationContext))
    resOpt mustBe Some(List(
      ValidationError(cellAM,"mandatoryAM","AM01","Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it).")
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

class OTHERAcquisitionV3ValidationTest extends PlaySpec with ERSValidationOTHERAcquisitionTestData with ValidationTestRunner {

  val validator = DataValidator(ConfigFactory.load.getConfig("ers-other-acquisition-validation-config"))

  "ERS Validation tests for OTHER Acquisition" should {
    runTests(validator, getDescriptions, getTestData, getExpectedResults)

    "when Column B is answered YES, column C is a mandatory field" in {
      val cellC = Cell("C", rowNumber, "")
      val cellB = Cell("B", rowNumber, "yes")
      val row = Row(1,Seq(cellC,cellB))
      val resOpt: Option[List[ValidationError]] = validator.validateRow(row, Some(ValidationContext))
      resOpt mustBe Some(List(
        ValidationError(cellC,"mandatoryB","MB","Enter the scheme reference number (it should be an 8 digit number).")
      ))
    }

    "when Column T is answered YES, column U is a mandatory field" in {
      val cellU = Cell("U", rowNumber, "")
      val cellT = Cell("T", rowNumber, "yes")
      val row = Row(1,Seq(cellU,cellT))
      val resOpt: Option[List[ValidationError]] = validator.validateRow(row, Some(ValidationContext))
      resOpt mustBe Some(List(
        ValidationError(cellU,"mandatoryT","MT","Enter 'yes' or 'no'.")
      ))
    }

    "when Column U is answered NO, column V is a mandatory field" in {
      val cellV = Cell("V", rowNumber, "")
      val cellU = Cell("U", rowNumber, "no")
      val row = Row(1,Seq(cellV,cellU))
      val resOpt: Option[List[ValidationError]] = validator.validateRow(row, Some(ValidationContext))
      resOpt mustBe Some(List(
        ValidationError(cellV,"mandatoryU","MU","Enter 'yes' or 'no'.")
      ))
    }

    "when Column V is answered YES, column W is a mandatory field" in {
      val cellW = Cell("W", rowNumber, "")
      val cellV = Cell("V", rowNumber, "yes")
      val row = Row(1,Seq(cellW,cellV))
      val resOpt: Option[List[ValidationError]] = validator.validateRow(row, Some(ValidationContext))
      resOpt mustBe Some(List(
        ValidationError(cellW,"mandatoryV","MV","Enter the HMRC reference (must be less than 11 characters).")
      ))
    }

    "when Column Y is filled 1, column Z is a mandatory field" in {
      val cellZ = Cell("Z", rowNumber, "")
      val cellY = Cell("Y", rowNumber, "1")
      val row = Row(1,Seq(cellZ,cellY))
      val resOpt: Option[List[ValidationError]] = validator.validateRow(row, Some(ValidationContext))
      resOpt mustBe Some(List(
        ValidationError(cellZ,"mandatoryY","MY","Enter '1', '2' or '3'.")
      ))
    }

    "when Column Y is filled 1, column AD is a mandatory field" in {
      val cellAD = Cell("AD", rowNumber, "")
      val cellY = Cell("Y", rowNumber, "1")
      val row = Row(1,Seq(cellAD,cellY))
      val resOpt: Option[List[ValidationError]] = validator.validateRow(row, Some(ValidationContext))
      resOpt mustBe Some(List(
        ValidationError(cellAD,"mandatoryY2","MY2","Enter 'yes' or 'no'.")
      ))
    }

    "when Column AD is answered YES, column AE is a mandatory field" in {
      val cellAE = Cell("AE", rowNumber, "")
      val cellAD = Cell("AD", rowNumber, "yes")
      val row = Row(1,Seq(cellAE,cellAD))
      val resOpt: Option[List[ValidationError]] = validator.validateRow(row, Some(ValidationContext))
      resOpt mustBe Some(List(
        ValidationError(cellAE,"mandatoryD1","MD1","Enter 'all' or 'some'.")
      ))
    }

    "when Column AI is answered YES, column AJ is a mandatory field" in {
      val cellAJ = Cell("AJ", rowNumber, "")
      val cellAI = Cell("AI", rowNumber, "yes")
      val row = Row(1,Seq(cellAJ,cellAI))
      val resOpt: Option[List[ValidationError]] = validator.validateRow(row, Some(ValidationContext))
      resOpt mustBe Some(List(
        ValidationError(cellAJ,"mandatoryI1","MI1","Enter '1', '2' or '3'.")
      ))
    }

    "when Column AK is answered YES, column AL is a mandatory field" in {
      val cellAL = Cell("AL", rowNumber, "")
      val cellAK = Cell("AK", rowNumber, "yes")
      val row = Row(1,Seq(cellAL,cellAK))
      val resOpt: Option[List[ValidationError]] = validator.validateRow(row, Some(ValidationContext))
      resOpt mustBe Some(List(
        ValidationError(cellAL,"mandatoryK1","MK1","Enter 'yes' or 'no'.")
      ))
    }

  }
}

class OTHERRestrictedSecuritiesV3_ValidationTest extends PlaySpec with ERSValidationOTHERRestrictedSecuritiesTestData with ValidationTestRunner {

  val validator = DataValidator(ConfigFactory.load.getConfig("ers-other-restrictedsecurities-validation-config"))

  "ERS Validation tests for OTHER Restricted Securities" should {
    runTests(validator, getDescriptions, getTestData, getExpectedResults)
  }

  "when Column B is answered yes, column C is a mandatory field" in {
    val cellC = Cell("C", rowNumber, "")
    val cellB = Cell("B", rowNumber, "yes")
    val row = Row(1,Seq(cellC,cellB))
    val resOpt: Option[List[ValidationError]] = validator.validateRow(row, Some(ValidationContext))
    resOpt mustBe Some(List(
      ValidationError(cellC,"mandatoryC","C01","Enter the scheme reference number (it should be an 8 digit number).")
    ))
  }

  "when Column L is answered yes, column M is a mandatory field" in {
    val cellM = Cell("M", rowNumber, "")
    val cellL = Cell("L", rowNumber, "no")
    val row = Row(1,Seq(cellM,cellL))
    val resOpt: Option[List[ValidationError]] = validator.validateRow(row, Some(ValidationContext))
    resOpt mustBe Some(List(
      ValidationError(cellM,"mandatoryM","M01","Enter 'yes' or 'no'.")
    ))
  }

  "when Column L is answered yes, column N is a mandatory field" in {
    val cellN = Cell("N", rowNumber, "")
    val cellM = Cell("M", rowNumber, "yes")
    val row = Row(1,Seq(cellN,cellM))
    val resOpt: Option[List[ValidationError]] = validator.validateRow(row, Some(ValidationContext))
    resOpt mustBe Some(List(
      ValidationError(cellN,"mandatoryN","N01","Enter the HMRC reference (must be less than 11 characters).")
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

class OTHEROtherBenefitsV3ValidationTest extends PlaySpec with ERSValidationOTHEROtherBenefitsTestData with ValidationTestRunner {

  val validator = DataValidator(ConfigFactory.load.getConfig("ers-other-other-benefits-validation-config"))

  "ERS Validation tests for OTHER Other Benefits" should {
    runTests(validator, getDescriptions, getTestData, getExpectedResults)
  }

  "when Column B is answered yes, column C is a mandatory field" in {
    val cellC = Cell("C", rowNumber, "")
    val cellB = Cell("B", rowNumber, "yes")
    val row = Row(1,Seq(cellC,cellB))
    val resOpt: Option[List[ValidationError]] = validator.validateRow(row, Some(ValidationContext))
    resOpt mustBe Some(List(
      ValidationError(cellC,"mandatoryB","MB","Enter the scheme reference number (it should be an 8 digit number).")
    ))
  }
}

class OTHERConvertibleV3ValidationTest extends PlaySpec with ERSValidationOTHERConvertibleTestData with ValidationTestRunner {

  val validator = DataValidator(ConfigFactory.load.getConfig("ers-other-convertible-validation-config"))

  "ERS Validation tests for OTHER Convertible" should {
    runTests(validator, getDescriptions, getTestData, getExpectedResults)
  }

  "when Column B is answered yes, column C is a mandatory field" in {
    val cellC = Cell("C", rowNumber, "")
    val cellB = Cell("B", rowNumber, "yes")
    val row = Row(1, Seq(cellC, cellB))
    val resOpt: Option[List[ValidationError]] = validator.validateRow(row, Some(ValidationContext))
    resOpt mustBe Some(List(
      ValidationError(cellC, "mandatoryC", "C01", "Enter the scheme reference number (it should be an 8 digit number).")
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

class OTHERNotionalV3ValidationTest extends PlaySpec with ERSValidationOTHERNotionalTestData with ValidationTestRunner {

  val validator = DataValidator(ConfigFactory.load.getConfig("ers-other-notional-validation-config"))

  "ERS Validation tests for OTHER Notional" should {
    runTests(validator, getDescriptions, getTestData, getExpectedResults)
    "when notional Column B is answered yes, column C is a mandatory field" in {
      val cellC = Cell("C", rowNumber, "")
      val cellB = Cell("B", rowNumber, "yes")
      val row = Row(1, Seq(cellC, cellB))
      val resOpt: Option[List[ValidationError]] = validator.validateRow(row, Some(ValidationContext))
      resOpt mustBe Some(List(
        ValidationError(cellC, "mandatoryC", "C01", "Enter the scheme reference number (it should be an 8 digit number).")
      ))
    }
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

class OTHEREnhancementV3ValidationTest extends PlaySpec with ERSValidationOTHEREnhancementTestData with ValidationTestRunner {

  val validator = DataValidator(ConfigFactory.load.getConfig("ers-other-enhancement-validation-config"))

  "ERS Validation tests for OTHER Enhancement" should {
    runTests(validator, getDescriptions, getTestData, getExpectedResults)

    "when enhancement Column B is answered yes, column C is a mandatory field" in {
      val cellC = Cell("C", rowNumber, "")
      val cellB = Cell("B", rowNumber, "yes")
      val row = Row(1, Seq(cellC, cellB))
      val resOpt: Option[List[ValidationError]] = validator.validateRow(row, Some(ValidationContext))
      resOpt mustBe Some(List(
        ValidationError(cellC, "mandatoryC", "C01", "Enter the scheme reference number (it should be an 8 digit number).")
      ))
    }
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

class OTHERSoldV3ValidationTest extends PlaySpec with ERSValidationOTHERSoldTestData with ValidationTestRunner {

  val validator = DataValidator(ConfigFactory.load.getConfig("ers-other-sold-validation-config"))

  "ERS Validation tests for OTHER Sold" should {
    runTests(validator, getDescriptions, getTestData, getExpectedResults)

    "when sold Column B is answered yes, column C is a mandatory field" in {
      val cellC = Cell("C", rowNumber, "")
      val cellB = Cell("B", rowNumber, "yes")
      val row = Row(1, Seq(cellC, cellB))
      val resOpt: Option[List[ValidationError]] = validator.validateRow(row, Some(ValidationContext))
      resOpt mustBe Some(List(
        ValidationError(cellC, "mandatoryC", "C01", "Enter the scheme reference number (it should be an 8 digit number).")
      ))
    }
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
