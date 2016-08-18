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
import services.validation.CSOPTestData.{ERSValidationCSOPExercisedTestData, ERSValidationCSOPRCLTestData, ERSValidationCSOPGrantedTestData}

class CSOPOptionsGrantedV3ValidationCSOPTest extends PlaySpec with ERSValidationCSOPGrantedTestData with ValidationTestRunner{

  " ERS CSOP Granted Validation tests" should {
    val validator = DataValidator(ConfigFactory.load.getConfig("ers-csop-granted-validation-config"))
    runTests(validator, getDescriptions, getTestData, getExpectedResults)

    "when sharesListedOnSE is answered no, mvAgreedHMRC is a mandatory field" in {
      val cellG = Cell("G", rowNumber, "")
      val cellF = Cell("F", rowNumber, "no")
      val row = Row(1,Seq(cellG,cellF))
      val resOpt: Option[List[ValidationError]] = validator.validateRow(row, Some(ValidationContext))
      resOpt mustBe Some(List(
        ValidationError(cellG,"mandatoryG","G01","Enter 'yes' or 'no'.")))
    }

    "when sharesListedOnSE is answered yes, hmrcRef is a mandatory field" in {
      val cellH = Cell("H", rowNumber, "")
      val cellG = Cell("G", rowNumber, "yes")
      val row = Row(1,Seq(cellH,cellG))
      val resOpt: Option[List[ValidationError]] = validator.validateRow(row, Some(ValidationContext))
      resOpt mustBe Some(List(
        ValidationError(cellH,"mandatoryH","G02","Enter the HMRC reference (must be less than 11 characters).")
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
      resOpt.get.size mustBe 7
    }

  }


}

class CSOPOptionsRCLTest extends PlaySpec with ERSValidationCSOPRCLTestData with ValidationTestRunner{

  "ERS CSOP Options RCL Validation Test " should {
    val validator = DataValidator(ConfigFactory.load.getConfig("ers-csop-rcl-validation"))
    runTests(validator, getDescriptions, getTestData, getExpectedResults)

    "when colB is answered yes, colC is a mandatory field" in {
      val cellC = Cell("C", rowNumber, "")
      val cellB = Cell("B", rowNumber, "yes")
      val row = Row(1,Seq(cellB,cellC))
      val resOpt: Option[List[ValidationError]] = validator.validateRow(row, Some(ValidationContext))
      resOpt mustBe Some(List(
        ValidationError(cellC,"mandatoryC","C01","Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it).")
      ))
    }

  }

}

class CSOPOptionsExercisedTest extends PlaySpec with ERSValidationCSOPExercisedTestData with ValidationTestRunner{

  "ERS CROP Options Exercised Validation Test" should {

    val validator = DataValidator(ConfigFactory.load.getConfig("ers-csop-exercised-validation"))
    runTests(validator, getDescriptions, getTestData, getExpectedResults)

    "when mvAgreedHMRC is answered yes, hmrcRef is a mandatory field" in {
      val cellO = Cell("O", rowNumber, "")
      val cellN = Cell("N", rowNumber, "yes")
      val row = Row(1,Seq(cellO,cellN))
      val resOpt: Option[List[ValidationError]] = validator.validateRow(row, Some(ValidationContext))
      resOpt mustBe Some(List(
        ValidationError(cellO,"mandatoryO","O01","Enter the HMRC reference (must be less than 11 characters).")
      ))
    }

    "when payeOperatedApplied is answered yes, deductibleAmount must be answered" in {
      val cellR = Cell("R", rowNumber, "")
      val cellQ = Cell("Q", rowNumber, "yes")
      val row = Row(1,Seq(cellR,cellQ))
      val resOpt: Option[List[ValidationError]] = validator.validateRow(row, Some(ValidationContext))
      resOpt mustBe Some(List(
        ValidationError(cellR,"mandatoryR","R01","Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it).")
      ))
    }

  }

}
