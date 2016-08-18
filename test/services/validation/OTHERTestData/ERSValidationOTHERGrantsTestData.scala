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

package services.validation.OTHERTestData

import uk.gov.hmrc.services.validation.Cell
import models.ValidationErrorData

trait ERSValidationOTHERGrantsTestData {

  val rowNumber:Int = 1

  def getDescriptions: List[String] = {
    val descriptions =
      List(
        "validate dateOfGrant without ValidationErrors for valid data",
        "validate dateOfGrant with ValidationErrors for invalid data",
        "validate dateOfGrant with ValidationErrors for no data",
        "validate numberOfEmployeesGrantedOptions without ValidationErrors for valid data",
        "validate numberOfEmployeesGrantedOptions with ValidationErrors for an alphanumeric string",
        "validate numberOfEmployeesGrantedOptions with ValidationErrors for an entry larger than that allowed",
        "validate umv without ValidationErrors for valid data",
        "validate umv with ValidationErrors for a number with more than 4 decimal places",
        "validate umv with ValidationErrors for an alphanumeric string",
        "validate umv with ValidationErrors for an entry larger than that allowed",
        "validate numberOfSharesOverWhichOptionsGranted without ValidationErrors valid data",
        "validate numberOfSharesOverWhichOptionsGranted with ValidationErrors a number with more than 2 decimal places",
        "validate numberOfSharesOverWhichOptionsGranted with ValidationErrors an alphanumeric string",
        "validate numberOfSharesOverWhichOptionsGranted with ValidationErrors a number larger than that allowed"
      )
    descriptions
  }

  def getTestData: List[Cell] = {
    val testData = List(
      Cell("A",rowNumber,"2014-08-30"),
      Cell("A",rowNumber,"2140830"),
      Cell("A",rowNumber,""),
      Cell("B",rowNumber,"123200"),
      Cell("B",rowNumber,"abc"),
      Cell("B",rowNumber,"12345678"),
      Cell("C",rowNumber,"10.1244"),
      Cell("C",rowNumber,"10.12441212"),
      Cell("C",rowNumber,"abc"),
      Cell("C",rowNumber,"12345678911231223.1234"),
      Cell("D",rowNumber,"100.00"),
      Cell("D",rowNumber,"100.0120"),
      Cell("D",rowNumber,"abc"),
      Cell("D",rowNumber,"12345678911231223.14")
    )
    testData
  }

  def getExpectedResults: List[Option[List[ValidationErrorData]]] = {
    val expectedResults = List(
      None,
      Some(List(ValidationErrorData("error.1","001","Enter a date that matches the yyyy-mm-dd pattern."))),
      Some(List(ValidationErrorData("MANDATORY","100","Enter a date that matches the yyyy-mm-dd pattern."))),
      None,
      Some(List(ValidationErrorData("error.2","002","Must be a whole number and be less than 1,000,000."))),
      Some(List(ValidationErrorData("error.2","002","Must be a whole number and be less than 1,000,000."))),
      None,
      Some(List(ValidationErrorData("error.3","003","Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
      Some(List(ValidationErrorData("error.3","003","Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
      Some(List(ValidationErrorData("error.3","003","Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
      None,
      Some(List(ValidationErrorData("error.4","004","Must be a number with 2 digits after the decimal point (and no more than 11 digits in front of it)."))),
      Some(List(ValidationErrorData("error.4","004","Must be a number with 2 digits after the decimal point (and no more than 11 digits in front of it)."))),
      Some(List(ValidationErrorData("error.4","004","Must be a number with 2 digits after the decimal point (and no more than 11 digits in front of it).")))
    )
    expectedResults
  }

  def getValidRowData:Seq[Cell] = {
    val rowData = Seq(
      Cell("A",rowNumber,"2014-08-30"),
      Cell("B",rowNumber,"12300"),
      Cell("C",rowNumber,"10.1244"),
      Cell("D",rowNumber,"100.00")
    )
    rowData
  }

  def getInvalidRowData:Seq[Cell] = {
    val rowData = Seq(
      Cell("A",rowNumber,"2140830"),
      Cell("B",rowNumber,"12345678"),
      Cell("C",rowNumber,"12345678911231223.1234"),
      Cell("D",rowNumber,"12345678911231223.14")
    )
    rowData
  }

}
