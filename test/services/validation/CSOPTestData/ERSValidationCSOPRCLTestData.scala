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


trait ERSValidationCSOPRCLTestData {

  val rowNumber:Int = 1

  def getDescriptions: List[String] ={
    val descriptions =
      List(
        //Column A
        "When dateOfEvent is correctly formatted no validation error should be raised",
        "Return The date must match the yyyy-mm-dd pattern. For dateOfEvent when an empty field is given",
        "Return The date must match the yyyy-mm-dd pattern. For dateOfEvent when an incorrect date is given",
        //Column B
        "When wasMoneyOrValueGiven is Yes or No then no validation error is raised",
        "Return This entry must be 'yes' or 'no'. For wasMoneyOrValueGiven when an empty value is given",
        "Return This entry must be 'yes' or 'no'. For wasMoneyOrValueGiven when any other value is given",
        //Column C
        "When amtOrValue is a correctly formatted number, no validation error should be raised.",
        "Return This entry must be a number with 4 digits after the decimal point. For amtOrValue when an incorrect amount of decimal places have been entered.",
        "Return This entry must be a number made up of digits. For amtOrValue when non-number values entered",
        "Return This entry is larger than the maximum number value allowed. For amtOrValue when to many characters",
        //Column D
        "When releasedindividualFirstName contains characters and is less the 35 characters, no validation errors should be raised",
        "Return This entry must contain 35 characters or less. For releasedindividualFirstName when the user inputs an empty string",
        "Return This entry must contain 35 characters or less. For releasedindividualFirstName when the user inputs too many characters",
        //Column E
        "When releasedindividualSecondName contains characters and is less the 35 characters, no validation errors should be raised",
        "Return This entry must contain 35 characters or less. For releasedindividualSecondName when the user inputs too many characters",
        //Column F
        "When releasedindividualLastName contains characters and is less the 35 characters, no validation errors should be raised",
        "Return This entry must contain 35 characters or less. For releasedindividualLastName when the user inputsan empty string",
        "Return This entry must contain 35 characters or less. For releasedindividualLastName when the user inputs too many characters",
        //Column G
        "When releasedindividualNino matches the expected Nino format, no validation error should be raised",
        "Return The National Insurance number must be 2 letters followed by 6 number digits, with an optional final letter. When the submitted text does not match a valid Nino ",
        //Column H
        "When releasedindividualPayeReference matches the expected PAYE reference format, no validation error should be raised",
        "Return The PAYE reference must be less than 15 characters. When the submitted text does not match the PAYE format.",
        //Column I
        "When payeOperatedApplied is Yes or No, no validation error should be raised",
        "Return This entry must be 'yes' or 'no'. When an empty string is given for payeOperatedApplied.",
        "Return This entry must be 'yes' or 'no'. When the characters entered do not match yes or no for payeOperatedApplied."
       )
    descriptions
  }

  def getTestData: List[Cell] ={
    val testData = List(
      Cell("A", rowNumber, "2014-12-10"),
      Cell("A", rowNumber, ""),
      Cell("A", rowNumber, "aaa"),
      Cell("B", rowNumber, "Yes"),
      Cell("B", rowNumber, ""),
      Cell("B",rowNumber, "yyYeesss"),
      Cell("C", rowNumber, "10.1234"),
      Cell("C", rowNumber, "10.123"),
      Cell("C", rowNumber, "Ten"),
      Cell("C", rowNumber, "123456789012345.1234"),
      Cell("D", rowNumber, "John"),
      Cell("D", rowNumber, " "),
      Cell("D", rowNumber, "AbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyz"),
      Cell("E", rowNumber, "John"),
      Cell("E", rowNumber, "AbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyz"),
      Cell("F", rowNumber, "John"),
      Cell("F", rowNumber, ""),
      Cell("F", rowNumber, "AbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyz"),
      Cell("G", rowNumber, "AB123456A"),
      Cell("G", rowNumber, "ABB25345BA1"),
      Cell("H", rowNumber, "123/XZ55555555"),
      Cell("H", rowNumber, "1234/12345/67890abcd"),
      Cell("I", rowNumber, "Yes"),
      Cell("I", rowNumber, " "),
      Cell("I", rowNumber, "YyEeSs")
    )
    testData
  }

  def getExpectedResults: List[Option[List[ValidationErrorData]]] = {
    val expectedResults = List(
      None,
      Some(List(ValidationErrorData("error.1", "001", "Enter a date that matches the yyyy-mm-dd pattern."))),
      Some(List(ValidationErrorData("error.1", "001", "Enter a date that matches the yyyy-mm-dd pattern."))),
      None,
      Some(List(ValidationErrorData("error.2", "002", "Enter 'yes' or 'no'."))),
      Some(List(ValidationErrorData("error.2", "002", "Enter 'yes' or 'no'."))),
      None,
      Some(List(ValidationErrorData("error.3", "003", "Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
      Some(List(ValidationErrorData("error.3", "003", "Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
      Some(List(ValidationErrorData("error.3", "003", "Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
      None,
      Some(List(ValidationErrorData("error.4", "004", "Enter a first name (must be less than 36 characters and can only have letters, numbers, hyphens or apostrophes)."))),
      Some(List(ValidationErrorData("error.4", "004", "Enter a first name (must be less than 36 characters and can only have letters, numbers, hyphens or apostrophes)."))),
      None,
      Some(List(ValidationErrorData("error.5", "005", "Must be less than 36 characters and can only have letters, numbers, hyphens or apostrophes."))),
      None,
      Some(List(ValidationErrorData("error.6", "006", "Enter a last name (must be less than 36 characters and can only have letters, numbers, hyphens or apostrophes)."))),
      Some(List(ValidationErrorData("error.6", "006", "Enter a last name (must be less than 36 characters and can only have letters, numbers, hyphens or apostrophes)."))),
      None,
      Some(List(ValidationErrorData("error.7", "007", "National Insurance number must be 2 letters followed by 6 number digits, with an optional final letter."))),
      None,
      Some(List(ValidationErrorData("error.8", "008", "PAYE reference must be a 3 digit number followed by a forward slash and up to 10 more characters."))),
      None,
      Some(List(ValidationErrorData("error.9", "009", "Enter 'yes' or 'no'."))),
      Some(List(ValidationErrorData("error.9", "009", "Enter 'yes' or 'no'.")))
    )

    expectedResults
  }

  def getValidRowData:Seq[Cell] = {
    val rowData = Seq(
      Cell("A", rowNumber, "2014-12-10")
    )
    rowData
  }

  def getInvalidRowData:Seq[Cell] = {
    val rowData = Seq(
      Cell("A", rowNumber, "20-12-2011")
    )
    rowData
  }

}
