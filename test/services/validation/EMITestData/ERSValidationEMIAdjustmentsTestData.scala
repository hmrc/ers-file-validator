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

package services.validation.EMITestData


import models.ValidationErrorData
import org.apache.commons.lang3.StringUtils
import uk.gov.hmrc.services.validation.models._

trait ERSValidationEMIAdjustmentsTestData {
  // scalastyle:off magic.number
  val rowNumber:Int = 1

  def getDescriptions: List[String] = {
    val descriptions =
      List(
        //A
        "validate column A (yes/no) without returning ValidationErrors for valid data",
        "validate column A returning ValidationErrors for empty data",
        "validate column A returning ValidationErrors for invalid data",
        //B
        "validate column B (yes/no) without returning ValidationErrors for valid data",
        "validate column B returning ValidationErrors for invalid data",
        //C
        "validate column C (yes/no) without returning ValidationErrors for valid data",
        "validate column C returning ValidationErrors for invalid data",
        //D
        "validate column D without returning ValidationErrors for valid data",
        "validate column D returning ValidationErrors for a number greater than 8",
        "validate column D returning ValidationErrors for a value not a number",
        "validate column D returning ValidationErrors for decimal number",
        //E
        "validate dateOptionAdjusted without returning ValidationErrors for valid data",
        "validate dateOptionAdjusted returning ValidationErrors for an empty date",
        "validate dateOptionAdjusted returning ValidationErrors for an invalid date",
        //F
        "validate firstName without returning ValidationErrors for valid data",
        "validate firstName without returning ValidationErrors for name with hyphen",
        "validate firstName returning ValidationErrors for a string over 35 characters in length",
        "validate firstName returning ValidationErrors for empty string",
        //G
        "validate secondName without returning ValidationErrors for valid data",
        "validate secondName returning ValidationErrors for a string over 35 characters in length",
        //H
        "validate surName without returning ValidationErrors for valid data",
        "validate surName returning ValidationErrors for a string over 35 characters in length",
        "validate surName returning ValidationErrors for an empty string",
        //I
        "validate NINO without returning ValidationErrors for a valid NINO",
        "validate NINO returning ValidationErrors for invalid data",
        "validate NINO returning ValidationErrors for missing a letter",
        "validate NINO returning ValidationErrors for no data",
        //J
        "validate PAYE ref without returning ValidationErrors for valid data",
        "validate PAYE ref returning ValidationErrors given invalid PAYE ref",
        "validate PAYE ref returning ValidationErrors given no PAYE ref",
        //K
        "validate exercisePricePerSUOPBeforeAdjustment without returning ValidationErrors given valid data",
        "validate exercisePricePerSUOPBeforeAdjustment returning ValidationErrors for a number without 4 digits",
        "validate exercisePricePerSUOPBeforeAdjustment returning ValidationErrors for an alphanumeric string",
        "validate exercisePricePerSUOPBeforeAdjustment returning ValidationErrors for a number large than that allowed",
        //L
        "validate numberOfSUOPAfterAdjustment without returning ValidationErrors given valid data",
        "validate numberOfSUOPAfterAdjustment returning ValidationErrors for a number without 2 digits",
        "validate numberOfSUOPAfterAdjustment returning ValidationErrors for an alphanumeric string",
        "validate numberOfSUOPAfterAdjustment returning ValidationErrors for a number large than that allowed",
        //M
        "validate exercisePricePerSUOPBeforeAdjustment2 without returning ValidationErrors given valid data",
        "validate exercisePricePerSUOPBeforeAdjustment2 returning ValidationErrors for a number without 4 digits",
        "validate exercisePricePerSUOPBeforeAdjustment2 returning ValidationErrors for an alphanumeric string",
        "validate exercisePricePerSUOPBeforeAdjustment2 returning ValidationErrors for a number large than that allowed",
        //N
        "validate actualMarketValueOASAtTheDateOfGrant without returning ValidationErrors given valid data",
        "validate actualMarketValueOASAtTheDateOfGrant returning ValidationErrors for a number without 4 digits",
        "validate actualMarketValueOASAtTheDateOfGrant returning ValidationErrors for an alphanumeric string",
        "validate actualMarketValueOASAtTheDateOfGrant returning ValidationErrors for a number large than that allowed"
      )
    descriptions
  }

  def getTestData: List[Cell] = {
    val testData = List(
      Cell("A", rowNumber, "yes"),
      Cell("A", rowNumber, ""),
      Cell("A", rowNumber, "123"),
      Cell("B", rowNumber, "yes"),
      Cell("B", rowNumber, "123"),
      Cell("C", rowNumber, "yes"),
      Cell("C", rowNumber, "123"),
      Cell("D", rowNumber, "2"),
      Cell("D", rowNumber, "123"),
      Cell("D", rowNumber, "abc"),
      Cell("D", rowNumber, "3.3"),
      Cell("E", rowNumber, "2011-10-13"),
      Cell("E", rowNumber, " "),
      Cell("E", rowNumber, "abc"),
      Cell("F", rowNumber, "Mia"),
      Cell("F", rowNumber, "Iam-Mia"),
      Cell("F", rowNumber, StringUtils.leftPad("", 38, 'A')),
      Cell("F", rowNumber, " "),
      Cell("G", rowNumber, "Mia"),
      Cell("G", rowNumber, StringUtils.leftPad("", 38, 'B')),
      Cell("H", rowNumber, "Mia"),
      Cell("H", rowNumber, StringUtils.leftPad("", 38, 'C')),
      Cell("H", rowNumber, " "),
      Cell("I", rowNumber, "AB123456C"),
      Cell("I", rowNumber, "123"),
      Cell("I", rowNumber, "AB123456"),
      Cell("I", rowNumber, ""),
      Cell("J", rowNumber, "123/XZ55555555"),
      Cell("J", rowNumber, "123abc///XZ55555555"),
      Cell("J", rowNumber, ""),
      Cell("K", rowNumber, "10.1234"),
      Cell("K", rowNumber, "10"),
      Cell("K", rowNumber, "10a"),
      Cell("K", rowNumber, StringUtils.leftPad("", 15, "1") + ".1234"),
      Cell("L", rowNumber, "10.14"),
      Cell("L", rowNumber, "10"),
      Cell("L", rowNumber, "10a"),
      Cell("L", rowNumber, StringUtils.leftPad("", 15, "1") + ".34"),
      Cell("M", rowNumber, "10.1234"),
      Cell("M", rowNumber, "10"),
      Cell("M", rowNumber, "10a"),
      Cell("M", rowNumber, StringUtils.leftPad("", 15, "1") + ".1234"),
      Cell("N", rowNumber, "10.1234"),
      Cell("N", rowNumber, "10"),
      Cell("N", rowNumber, "10a"),
      Cell("N", rowNumber, StringUtils.leftPad("", 15, "1") + ".1234")
    )
    testData
  }

  def getExpectedResults: List[Option[List[ValidationErrorData]]] = {
    val expectedResults = List(
      None,
      Some(List(ValidationErrorData("error.1","001","Enter 'yes' or 'no'."))),
      Some(List(ValidationErrorData("error.1","001","Enter 'yes' or 'no'."))),
      None,
      Some(List(ValidationErrorData("error.2","002","Enter 'yes' or 'no'."))),
      None,
      Some(List(ValidationErrorData("error.3","003","Enter 'yes' or 'no'."))),
      None,
      Some(List(ValidationErrorData("error.4","004","Enter '1', '2', '3', '4', '5', '6', '7' or '8'."))),
      Some(List(ValidationErrorData("error.4","004","Enter '1', '2', '3', '4', '5', '6', '7' or '8'."))),
      Some(List(ValidationErrorData("error.4","004","Enter '1', '2', '3', '4', '5', '6', '7' or '8'."))),
      None,
      Some(List(ValidationErrorData("error.5","005","Enter a date that matches the yyyy-mm-dd pattern."))),
      Some(List(ValidationErrorData("error.5","005","Enter a date that matches the yyyy-mm-dd pattern."))),
      None,
      None,
      Some(List(ValidationErrorData("error.6","006","Enter a first name (must be less than 36 characters and can only have letters, numbers, hyphens or apostrophes)."))),
      Some(List(ValidationErrorData("error.6","006","Enter a first name (must be less than 36 characters and can only have letters, numbers, hyphens or apostrophes)."))),
      None,
      Some(List(ValidationErrorData("error.7","007","Must be less than 36 characters and can only have letters, numbers, hyphens or apostrophes."))),
      None,
      Some(List(ValidationErrorData("error.8","008","Enter a last name (must be less than 36 characters and can only have letters, numbers, hyphens or apostrophes)."))),
      Some(List(ValidationErrorData("error.8","008","Enter a last name (must be less than 36 characters and can only have letters, numbers, hyphens or apostrophes)."))),
      None,
      Some(List(ValidationErrorData("error.9","009","Enter a National Insurance number (for example QQ123456C) or an ERS reference (for example TN010181Y)."))),
      Some(List(ValidationErrorData("error.9","009","Enter a National Insurance number (for example QQ123456C) or an ERS reference (for example TN010181Y)."))),
      Some(List(ValidationErrorData("error.9","009","Enter a National Insurance number (for example QQ123456C) or an ERS reference (for example TN010181Y)."))),
      None,
      Some(List(ValidationErrorData("error.10","010","Enter an employer PAYE reference. For example '123/AB456'."))),
      Some(List(ValidationErrorData("error.10","010","Enter an employer PAYE reference. For example '123/AB456'."))),
      None,
      Some(List(ValidationErrorData("error.11","011","Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
      Some(List(ValidationErrorData("error.11","011","Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
      Some(List(ValidationErrorData("error.11","011","Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
      None,
      Some(List(ValidationErrorData("error.12","012","Must be a number with 2 digits after the decimal point (and no more than 11 digits in front of it)."))),
      Some(List(ValidationErrorData("error.12","012","Must be a number with 2 digits after the decimal point (and no more than 11 digits in front of it)."))),
      Some(List(ValidationErrorData("error.12","012","Must be a number with 2 digits after the decimal point (and no more than 11 digits in front of it)."))),
      None,
      Some(List(ValidationErrorData("error.13", "013", "Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
      Some(List(ValidationErrorData("error.13", "013", "Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
      Some(List(ValidationErrorData("error.13", "013", "Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
      None,
      Some(List(ValidationErrorData("error.14", "014", "Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
      Some(List(ValidationErrorData("error.14", "014", "Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
      Some(List(ValidationErrorData("error.14", "014", "Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it).")))
    )
    expectedResults
  }

  def getValidRowData:Seq[Cell] = {
    val rowData = Seq(
      Cell("A", rowNumber, "yes"),
      Cell("B", rowNumber, "yes"),
      Cell("C", rowNumber, "yes"),
      Cell("D", rowNumber, "4"),
      Cell("E", rowNumber, "2011-10-13"),
      Cell("F", rowNumber, "Mia"),
      Cell("G", rowNumber, "Iam"),
      Cell("H", rowNumber, "Aim"),
      Cell("I", rowNumber, "AB123456C"),
      Cell("J", rowNumber, "123/XZ55555555"),
      Cell("K", rowNumber, "10.1234"),
      Cell("L", rowNumber, "10.14"),
      Cell("M", rowNumber, "10.1324"),
      Cell("N", rowNumber, "10.1244")
    )
    rowData
  }

  def getInvalidRowData:Seq[Cell] = {
    val rowData = Seq(
      Cell("A", rowNumber, "123"),
      Cell("B", rowNumber, "321"),
      Cell("C", rowNumber, "456"),
      Cell("D", rowNumber, "3.4"),
      Cell("E", rowNumber, "123"),
      Cell("F", rowNumber, "156$"),
      Cell("G", rowNumber, "789$"),
      Cell("H", rowNumber, "124$"),
      Cell("I", rowNumber, "123"),
      Cell("J", rowNumber, "125?XZ555555551222"),
      Cell("K", rowNumber, "10"),
      Cell("L", rowNumber, "12"),
      Cell("M", rowNumber, "14"),
      Cell("N", rowNumber, "14")
    )
    rowData
  }

}
