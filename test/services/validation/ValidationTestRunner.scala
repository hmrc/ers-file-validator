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

import uk.gov.hmrc.services.validation.DataValidator
import uk.gov.hmrc.services.validation.models._
import models.ValidationErrorData
import org.scalatestplus.play.PlaySpec

trait ValidationTestRunner extends PlaySpec{


  def populateValidationError(expRes: ValidationErrorData)(implicit cell: Cell) = {
    ValidationError(cell, expRes.id, expRes.errorId, expRes.errorMsg)
  }

  def resultBuilder(cellData: Cell, expectedResultsMaybe: Option[List[ValidationErrorData]]): Option[ValidationError] = {
    if (expectedResultsMaybe.isDefined) {
      implicit val cell: Cell = cellData
      val validationErrors = expectedResultsMaybe.get.map(errorData => populateValidationError(errorData))
      Some(validationErrors.head)
    } else {
      None
    }
  }

  def runTests(validator:DataValidator, descriptions: List[String], testDatas:List[Cell], expectedResults:List[Option[List[ValidationErrorData]]]) = {
      for (x <- 0 until descriptions.length) {
        descriptions(x) in {
          validator.validateCell(testDatas(x)) mustBe resultBuilder(testDatas(x), expectedResults(x))
        }
      }
  }
}
