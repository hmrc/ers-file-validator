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

trait ERSValidationSAYERCLTestData {
  val rowNumber:Int = 1

  def getDescriptions: List[String] = {
    val descriptions =
      List(
      //column A
          "When dateOfEvent conforms to the expected date format, no validation error should be raised",
          "Return an error message when dateOfEvent does not conform to the expect date format",
          "Return an error message when dateOfEvent has been left empty",
      //column B
          "When wasMoneyOrValueGiven is a correctly formatted yes or no answer, no validation error should be raised",
          "Return an error message when wasMoneyOrValueGiven does not match yes or no",
          "Return an error message when wasMoneyOrValueGiven is left empty",
      //column C
          "When amountReleased is a correctly formatted number, no validation error should be raised",
          "Return an error message when amountReleased does not have 4 digits after the decimal point",
          "Return an error message when amountReleased is not a number",
          "Return an error message when amountReleased is too large",
      //column D
          "When individualReleased\\firstName is a correctly formatted first name, no validation error should be raised",
          "Return an error message when individualReleased\\firstName exceeds the maximum character length",
          "Return an error message when individualReleased\\firstName is left empty",
      //column E
          "When individualReleased\\secondName is a correctly formatted first name, no validation error should be raised",
          "Return an error message when individualReleased\\secondName exceeds the maximum character length",
      //column F
          "When individualReleased\\surname is a correctly formatted first name, no validation error should be raised",
          "Return an error message when individualReleased\\surname exceeds the maximum character length",
          "Return an error message when individualReleased\\surname is left empty",
      //column G
          "When individualReleased\\nino is a correctly formatted NINO, no validation error should be raised",
          "Return an error message when individualReleased\\nino does not conform to the expected NINO format",
      //column H
        "When individualReleased\\payeReference is a correctly formatted PAYE ref, no validation errror should be raised",
        "Reutrn an error message when individualReleased\\payeReference does not conform to the expected PAYE format",
      //column I
        "When payeOperatedApplied is Yes or No, no validation error should be raised",
        "Return an error message when payeOperatedApplied is not yes or no",
        "Return an error message when payeOperatedApplied is left empty"
      )
    descriptions
  }

  def getTestData: List[Cell] ={
    val testData = List(
      Cell("A", rowNumber, "2014-12-10"),
      Cell("A", rowNumber, "12-2014-10"),
      Cell("A", rowNumber, ""),
      Cell("B", rowNumber, "yes"),
      Cell("B", rowNumber, "Yess"),
      Cell("B", rowNumber, ""),
      Cell("C", rowNumber, "123.1234"),
      Cell("C", rowNumber, "123.12345"),
      Cell("C", rowNumber, "abc"),
      Cell("C", rowNumber, "1234567890123456.1234"),
      Cell("D", rowNumber, "Guss"),
      Cell("D", rowNumber, "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz"),
      Cell("D", rowNumber, ""),
      Cell("E", rowNumber, "Jack"),
      Cell("E", rowNumber, "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz"),
      Cell("F", rowNumber, "Jackson"),
      Cell("F", rowNumber, "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz"),
      Cell("F", rowNumber, ""),
      Cell("G", rowNumber, "AA123456A"),
      Cell("G", rowNumber, "AAa123a4561Aa"),
      Cell("H", rowNumber, "123/XZ55555555"),
      Cell("H", rowNumber, "1a123/XZ533555a5555"),
      Cell("I", rowNumber, "Yes"),
      Cell("I", rowNumber, "Noooo"),
      Cell("I", rowNumber, "")
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
      Some(List(ValidationErrorData("error.9", "009", "Enter 'yes' or 'no'."))),
    )

    expectedResults
  }

  def getValidRowData:Seq[Cell] = {
    val rowData = Seq(
      Cell("A", rowNumber, "2014-12-10"),
      Cell("B", rowNumber, "yes"),
      Cell("C", rowNumber, "123.1234"),
      Cell("D", rowNumber, "Guss"),
      Cell("E", rowNumber, "Jack"),
      Cell("F", rowNumber, "Jackson"),
      Cell("G", rowNumber, "AA123456A"),
      Cell("H", rowNumber, "123/XZ55555555"),
      Cell("I", rowNumber, "Yes")
    )
    rowData
  }

  def getInvalidRowData:Seq[Cell] = {
    val rowData = Seq(
      Cell("A", rowNumber, "20-12-2011"),
      Cell("A", rowNumber, ""),
      Cell("B", rowNumber, "noooo"),
      Cell("B", rowNumber, ""),
      Cell("C", rowNumber, "123.12345"),
      Cell("C", rowNumber, "abc"),
      Cell("C", rowNumber, "1234567890123456.1234"),
      Cell("D", rowNumber, "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz"),
      Cell("D", rowNumber, ""),
      Cell("E", rowNumber, "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz"),
      Cell("F", rowNumber, "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz"),
      Cell("F", rowNumber, ""),
      Cell("G", rowNumber, "AAa123a4561Aa"),
      Cell("H", rowNumber, "1a123/XZ533555a5555"),
      Cell("I", rowNumber, "Noooo"),
      Cell("I", rowNumber, "")
    )
    rowData
  }

}
