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

package services.validation

import com.typesafe.config.ConfigFactory
import uk.gov.hmrc.services.validation.DataValidator
import uk.gov.hmrc.services.validation.models._
import org.scalatestplus.play.PlaySpec
import services.validation.SAYETestData.{ERSValidationSAYEExercisedTestData, ERSValidationSAYEGrantedTestData, ERSValidationSAYERCLTestData}

class ERSValidationConfig_SAYE_SayeGrantedTests extends PlaySpec with ERSValidationSAYEGrantedTestData with ValidationTestRunner {
  "SAYE Granted V3 scheme config validation" should {

    val validator = new DataValidator(ConfigFactory.load.getConfig("ers-saye-granted-validation-config"))
    runTests(validator, getDescriptions, getTestData, getExpectedResults)
    "make Q7 a mandatory field when Q6 is answered with no" in {
      val cellG = Cell("G", rowNumber, "")
      val cellF = Cell("F", rowNumber, "no")
      val row = Row(1, Seq(cellG, cellF))
      val resOpt: Option[List[ValidationError]] = validator.validateRow(row)
      resOpt mustBe Some(List(
        ValidationError(cellG, "mandatoryG", "G01", "Enter 'yes' or 'no'.")))
    }

    "make Q8 mandatory when Q6 is answered with yes" in {
      val cellH = Cell("H", rowNumber, "")
      val cellG = Cell("G", rowNumber, "yes")
      val row = Row(1,Seq(cellH,cellG))
      val resOpt: Option[List[ValidationError]] = validator.validateRow(row)
      resOpt mustBe Some(List(
        ValidationError(cellH,"mandatoryH","G02","Enter the HMRC reference (must be less than 11 characters).")
      ))
    }
  }
}

class ERSValidationConfig_SAYE_SayeRCLTests extends PlaySpec with ERSValidationSAYERCLTestData with ValidationTestRunner {
  "ERS SAYE RLC Validation Test" should {
    val validator = new DataValidator(ConfigFactory.load.getConfig("ers-saye-rcl-validation-config"))
    runTests(validator, getDescriptions, getTestData, getExpectedResults)

    "when Column B is answered yes, column C is a mandatory field" in {
      val cellC = Cell("C", rowNumber, "")
      val cellB = Cell("B", rowNumber, "yes")
      val row = Row(1, Seq(cellC, cellB))
      val resOpt: Option[List[ValidationError]] = validator.validateRow(row)
      resOpt.get must contain (
        ValidationError(cellC, "mandatoryC", "C01", "Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it).")
      )
    }
 }
}

class ERSValidationConfig_SAYE_ExercisedTests extends PlaySpec with ERSValidationSAYEExercisedTestData with ValidationTestRunner {
  "SAYE Exercised V3 scheme config validation" should {
    val validator = new DataValidator(ConfigFactory.load.getConfig("ers-saye-exercised-validation-config"))
    runTests(validator, getDescriptions, getTestData, getExpectedResults)

    "make Q10 a mandatory field when Q9 is answered with no" in {
      val cellJ = Cell("J", rowNumber, "")
      val cellI = Cell("I", rowNumber, "no")
      val row = Row(1, Seq(cellJ, cellI))
      val resOpt: Option[List[ValidationError]] = validator.validateRow(row)
      resOpt.get must contain (
        ValidationError(cellJ, "mandatoryJ", "J01", "Enter 'yes' or 'no'.")
      )
    }

    "make Q11 a mandatory field when Q9 is answered with yes" in {
      val cellK = Cell("K", rowNumber, "")
      val cellJ = Cell("J", rowNumber, "yes")
      val row = Row(1, Seq(cellK, cellJ))
      val resOpt: Option[List[ValidationError]] = validator.validateRow(row)
      resOpt.get must contain (
        ValidationError(cellK, "mandatoryK", "K01", "Enter the HMRC reference (must be less than 11 characters).")
      )
    }
   }
}
