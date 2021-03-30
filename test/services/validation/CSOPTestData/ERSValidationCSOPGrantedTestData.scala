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

package services.validation.CSOPTestData


import uk.gov.hmrc.services.validation.models._
import models.ValidationErrorData

trait ERSValidationCSOPGrantedTestData {
  
  val rowNumber:Int = 1

 def getDescriptions: List[String] ={
   val descriptions =
    List(
      //Column A
      "when dateOfGrant contains a date formated yyy-mm-dd, no validationErrors will be raised",
      "when dateOfGrant contains an empty value a ValidationError will be raised",
      "when dateOfGrant contains anything but a valid date a ValidationError will be raised",
      //Column B
      "when numberOfIndividuals contains a string of up to 6 numbers",
      "return a error message provided in config given a number larger than that expected",
      "return a error message provided in config given a number with decimal places",
      "return error messages provided in config given a number larger than that expected with decimals",
      //Column C
      "return None after validating a string of 11 numbers and 2 decimal places for row C",
      "when numberOfSharesGrantedOver contains a number without 2 digits after the decimal point, a ValidationError will be raised",
      "when numberOfSharesGrantedOver contains a number greater than 11 in length, a ValidationError will be raised",
      "when numberOfSharesGrantedOver contains an alphanumeric string, a ValidationError will be raised",
      //Column D
      "when umvPerShareUsedToDetermineTheExPrice contains a number up to 13 in length with 4 decimal places, no ValidationErrors will be raised",
      "when umvPerShareUsedToDetermineTheExPrice contains a number with fewer than 4 decimal places, a ValidationError will be raised",
      "when umvPerShareUsedToDetermineTheExPrice contains an alphanumeric string, a ValidationError will be raised",
      "when umvPerShareUsedToDetermineTheExPrice contains a number greater than 11 in length, a ValidationError will be raised",
      //Column E
      "return None if the correct input of a number with 4 decimal places is received for exercisePricePerShare",
      "return an error when an invalid value for exercisePricePerShare is given",
      //Column F
      "return None if the correct input (yes/no) is received for sharesListedOnSE",
      "return an error when an empty value for sharesListedOnSE is given",
      "return an error when an invalid value for sharesListedOnSE is given",
      //Column G
      "return None if the correct input (yes/no) is received for mvAgreedHMRC",
      "return an error when an invalid value for mvAgreedHMRC is given",
      //Column H
      "return no errors when given a correct hmrcRef number",
      "return an error when an invalid hmrc reference number is given",
      //Column I
      "return no errors when given a correct input for the employeeHoldSharesGreaterThan30K question",
      "return a list[ValidationError] when given invalid data for the employeeHoldSharesGreaterThan30K question"
    )
    descriptions
 }

 def getTestData: List[Cell] ={
   val testData = List(
     Cell("A", rowNumber, "2014-12-10"),
     Cell("A", rowNumber, ""),
     Cell("A", rowNumber, "ABC Recruitment Ltd"),
     Cell("B", rowNumber, "123456"),
     Cell("B", rowNumber, "1234345675"),
     Cell("B", rowNumber, "10.20"),
     Cell("B", rowNumber, "1234345675.30"),
     Cell("C", rowNumber, "1244578901.01"),
     Cell("C", rowNumber, "11234345675"),
     Cell("C", rowNumber, "11234345232342323675.13"),
     Cell("C", rowNumber, "a1234"),
     Cell("D", rowNumber, "1234567890123.1234"),
     Cell("D", rowNumber, "1234567890123.14"),
     Cell("D", rowNumber, "a1234"),
     Cell("D", rowNumber, "11234345232342323675.1123"),
     Cell("E", rowNumber, "1890123.1234"),
     Cell("E", rowNumber, "0.111"),
     Cell("F", rowNumber, "Yes"),
     Cell("F", rowNumber, ""),
     Cell("F", rowNumber, "YEss"),
     Cell("G", rowNumber, "Yes"),
     Cell("G", rowNumber, "YEss"),
     Cell("H", rowNumber, "aa12345678"),
     Cell("H", rowNumber, "aaa123a456788"),
     Cell("I", rowNumber, "yes"),
     Cell("I", rowNumber, "yyeess")
   )
   testData
 }

 def getExpectedResults: List[Option[List[ValidationErrorData]]] = {
   val expectedResults = List(
     None,
     Some(List(ValidationErrorData("error.1","001","Enter a date that matches the yyyy-mm-dd pattern."))),
     Some(List(ValidationErrorData("error.1","001","Enter a date that matches the yyyy-mm-dd pattern."))),
     None,
     Some(List(ValidationErrorData("error.2","002","Must be a whole number and be less than 1,000,000."))),
     Some(List(ValidationErrorData("error.2","002","Must be a whole number and be less than 1,000,000."))),
     Some(List(ValidationErrorData("error.2","002","Must be a whole number and be less than 1,000,000."))),
     None,
     Some(List(ValidationErrorData("error.3","003","Must be a number with 2 digits after the decimal point (and no more than 11 digits in front of it)."))),
     Some(List(ValidationErrorData("error.3","003","Must be a number with 2 digits after the decimal point (and no more than 11 digits in front of it)."))),
     Some(List(ValidationErrorData("error.3","003","Must be a number with 2 digits after the decimal point (and no more than 11 digits in front of it)."))),
     None,
     Some(List(ValidationErrorData("error.4","004","Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
     Some(List(ValidationErrorData("error.4","004","Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
     Some(List(ValidationErrorData("error.4","004","Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
     None,
     Some(List(ValidationErrorData("error.5","005","Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
     None,
     Some(List(ValidationErrorData("error.6","006","Enter 'yes' or 'no'."))),
     Some(List(ValidationErrorData("error.6","006","Enter 'yes' or 'no'."))),
     None,
     Some(List(ValidationErrorData("error.7","007","Enter 'yes' or 'no'."))),
     None,
     Some(List(ValidationErrorData("error.8","008","Enter the HMRC reference (must be less than 11 characters)."))),
     None,
     Some(List(ValidationErrorData("error.9","009","Enter 'yes' or 'no'.")))
   )
   expectedResults
 }

  def getValidRowData:Seq[Cell] = {
    val rowData = Seq(
      Cell("A", rowNumber, "2014-12-10"),
      Cell("B", rowNumber, "123456"),
      Cell("C", rowNumber, "1244578901.01"),
      Cell("D", rowNumber, "1234567890123.1234"),
      Cell("E", rowNumber, "1890123.1234"),
      Cell("F", rowNumber, "Yes"),
      Cell("G", rowNumber, ""),
      Cell("H", rowNumber, "aa12345678"),
      Cell("I", rowNumber, "no")
    )
    rowData
  }

  def getInvalidRowData:Seq[Cell] = {
    val rowData = Seq(
      Cell("A", rowNumber, "20-12-2011"),
      Cell("B", rowNumber, "1245542456"),
      Cell("C", rowNumber, "1244578901.0121"),
      Cell("D", rowNumber, "1234567890123.14"),
      Cell("E", rowNumber, "1890123.14"),
      Cell("F", rowNumber, "sye"),
      Cell("G", rowNumber, "no"),
      Cell("H", rowNumber, ""),
      Cell("I", rowNumber, "on")
    )
    rowData
  }

}
