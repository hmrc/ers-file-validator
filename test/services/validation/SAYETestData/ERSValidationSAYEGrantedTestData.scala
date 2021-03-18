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

package services.validation.SAYETestData


import uk.gov.hmrc.services.validation.models._
import models.ValidationErrorData

trait ERSValidationSAYEGrantedTestData {

  val rowNumber : Int =1

  def getDescriptions: List[String] = List(
      //A
      "when dateOfGrant contains a date formated yyyy-mm-dd, no validationErrors will be raised",
      "ValidationError will be raised when dateOfGrant is not the expected date format",
      "raise a ValidationError if the cell is empty",
      //B
      "when numberOfIndividuals contains a string up to 6 numbers",
      "provide an error message when a string number is larger than 6 digits",
      "provide an error message when a string number contain decimals",
      //C
      "return None for validating numberOfSharesGrantedOver contain 11 numbers plus 2 decimal places",
      "raise ValidationError when numberOfSharesGrantedOver contains a number without 2 digits after the decimal point",
      "raise ValidationError when numberOfSharesGRantedOver number is larger that 11 digits",
      "raise ValidationError when numberOfSharesGrantedOver contain none numeric digits",
      //D
      "return None for validating marketValuePerShareUsedToDetermineExercisePrice",
      "return ValidationError if entry does not have 4 decimal digits",
      "return ValidationError if entry contain is not a number",
      "return ValidationError if entry is larger than 13 digits",
      //E
      "return None for validating exercisePricePerShare",
      "return ValidationError when exercisePricePerShare entry does not have 4 decimal digits",
      "return ValidationError when exercisePricePerShare is not a number",
      "return ValidationError when exercisePricePerShare is larger than 13 digits",
      //F
      "return None when (yes/no) is received for shareListedOnSE",
      "return an error when an invalid value for sharesListedOnSE is given",
      "return an error when sharesListedOnSE is blank",
      //G
      "return None when (yes/no) is received for marketValueAgreedHMRC",
      "return an error when an invalid value for marketValueAgreedHMRC is given",
      //H
      "return None when (yes/no) is received for hmrcRef",
      "return an error when an invalid value for hmrcRef is given"
  )

  def getTestData: List[Cell] = List(
      Cell("A", rowNumber, "2016-04-14"),
      Cell("A", rowNumber, "garbage-date"),
      Cell("A", rowNumber, ""),
      Cell("B", rowNumber, "123456"),
      Cell("B", rowNumber, "1234345675"),
      Cell("B", rowNumber, "12.34"),
      Cell("C", rowNumber, "12345678911.23"),
      Cell("C", rowNumber, "12345678911"),
      Cell("C", rowNumber, "12345673562323.12"),
      Cell("C", rowNumber, "ab.12"),
      Cell("D", rowNumber, "1234.5678"),
      Cell("D", rowNumber, "12345.12"),
      Cell("D", rowNumber, "a.1234"),
      Cell("D", rowNumber, "12345678912345.1234"),
      Cell("E", rowNumber, "1234567.1234"),
      Cell("E", rowNumber, "12345.12"),
      Cell("E", rowNumber, "a.1234"),
      Cell("E", rowNumber, "12345678912345.1234"),
      Cell("F", rowNumber, "Yes"),
      Cell("F", rowNumber, "blah"),
      Cell("F", rowNumber, ""),
      Cell("G", rowNumber, "Yes"),
      Cell("G", rowNumber, "blah"),
      Cell("H", rowNumber, "aa23678"),
      Cell("H", rowNumber, "aaa123a43456789876543256788")
  )

  def getExpectedResults : List[Option[List[ValidationErrorData]]] = List(
    //A
    None,
    Some(List(ValidationErrorData("error.1", "001", "Enter a date that matches the yyyy-mm-dd pattern."))),
    Some(List(ValidationErrorData("error.1", "001", "Enter a date that matches the yyyy-mm-dd pattern."))),
    //B
    None,
    Some(List(ValidationErrorData("error.2", "002", "Must be a whole number and be less than 1,000,000."))),
    Some(List(ValidationErrorData("error.2", "002", "Must be a whole number and be less than 1,000,000."))),
    //C
    None,
    Some(List(ValidationErrorData("error.3", "003", "Must be a number with 2 digits after the decimal point (and no more than 11 digits in front of it)."))),
    Some(List(ValidationErrorData("error.3", "003", "Must be a number with 2 digits after the decimal point (and no more than 11 digits in front of it)."))),
    Some(List(ValidationErrorData("error.3", "003", "Must be a number with 2 digits after the decimal point (and no more than 11 digits in front of it)."))),
    //D
    None,
    Some(List(ValidationErrorData("error.4", "004", "Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
    Some(List(ValidationErrorData("error.4", "004", "Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
    Some(List(ValidationErrorData("error.4", "004", "Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
    //E
    None,
    Some(List(ValidationErrorData("error.5", "005", "Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
    Some(List(ValidationErrorData("error.5", "005", "Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
    Some(List(ValidationErrorData("error.5", "005", "Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
    //F
    None,
    Some(List(ValidationErrorData("error.6","006","Enter 'yes' or 'no'."))),
    Some(List(ValidationErrorData("error.6","006","Enter 'yes' or 'no'."))),
    //G
    None,
    Some(List(ValidationErrorData("error.7","007","Enter 'yes' or 'no'."))),
    //H
    None,
    Some(List(ValidationErrorData("error.8","008", "Enter the HMRC reference (must be less than 11 characters).")))
  )
}
