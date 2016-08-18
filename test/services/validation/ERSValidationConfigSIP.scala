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
import services.validation.SIPTestData.{ERSValidationSIPAwardsTestData, ERSValidationSIPOutTestData}

class SIPAwardsV3ValidationTest extends PlaySpec with ERSValidationSIPAwardsTestData with ValidationTestRunner{

  val validator = DataValidator(ConfigFactory.load.getConfig("ers-sip-awards-validation-config"))

  "Ers Validation tests for SIP Awards" should {
    runTests(validator, getDescriptions, getTestData, getExpectedResults)

    "when awards Column C is answered with 2, column D is a mandatory field" in {
      val cellD = Cell("D", rowNumber, "")
      val cellC = Cell("C", rowNumber, "2")
      val row = Row(1,Seq(cellD,cellC))
      val resOpt: Option[List[ValidationError]] = validator.validateRow(row, Some(ValidationContext))
      resOpt mustBe Some(List(
        ValidationError(cellD,"mandatoryD","D01","Enter 'yes' or 'no'.")
      ))
    }

    "when awards Column C is answered 1, column E is a mandatory field" in {
      val cellE = Cell("E", rowNumber, "")
      val cellC= Cell("C", rowNumber, "1")
      val row = Row(1, Seq(cellE, cellC))
      val resOpt: Option[List[ValidationError]] = validator.validateRow(row, Some(ValidationContext))
      resOpt mustBe Some(List(
        ValidationError(cellE, "mandatoryE", "E01", "Enter the ratio of the matching shares (numbers must be separated by a ':' or '/', for example, 2:1 or 2/1).")
      ))
    }

    "when awards Column O is answered NO, column P is a mandatory field" in {
      val cellP = Cell("P", rowNumber, "")
      val cellO= Cell("O", rowNumber, "NO")
      val row = Row(1, Seq(cellP, cellO))
      val resOpt: Option[List[ValidationError]] = validator.validateRow(row, Some(ValidationContext))
      resOpt mustBe Some(List(
        ValidationError(cellP, "mandatoryP", "P01", "Enter 'yes' or 'no'.")
      ))
    }

    "when awards Column P is answered YES, column Q is a mandatory field" in {
      val cellQ = Cell("Q", rowNumber, "")
      val cellP= Cell("P", rowNumber, "YES")
      val row = Row(1, Seq(cellQ, cellP))
      val resOpt: Option[List[ValidationError]] = validator.validateRow(row, Some(ValidationContext))
      resOpt mustBe Some(List(
        ValidationError(cellQ, "mandatoryQ", "Q01", "Enter the HMRC reference (must be less than 11 characters).")
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
class SIPOutV3ValidationTest extends PlaySpec with ERSValidationSIPOutTestData with ValidationTestRunner{

  val validator = DataValidator(ConfigFactory.load.getConfig("ers-sip-out-validation-config"))

  "Ers Validation tests for SIP Out" should {
    runTests(validator, getDescriptions, getTestData, getExpectedResults)
  }

  "when awards Column O is answered NO, column P is a mandatory field" in {
    val cellP = Cell("P", rowNumber, "")
    val cellO= Cell("O", rowNumber, "NO")
    val row = Row(1, Seq(cellP, cellO))
    val resOpt: Option[List[ValidationError]] = validator.validateRow(row, Some(ValidationContext))
    resOpt mustBe Some(List(
      ValidationError(cellP, "mandatoryP", "P01", "Enter 'yes' or 'no'.")
    ))
  }

  "when awards Column P is answered NO, column Q is a mandatory field" in {
    val cellQ = Cell("Q", rowNumber, "")
    val cellP= Cell("P", rowNumber, "NO")
    val row = Row(1, Seq(cellQ, cellP))
    val resOpt: Option[List[ValidationError]] = validator.validateRow(row, Some(ValidationContext))
    resOpt mustBe Some(List(
      ValidationError(cellQ, "mandatoryQ", "Q01", "Enter 'yes' or 'no'.")
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
