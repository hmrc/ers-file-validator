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

package services.validation.EMITestData


import uk.gov.hmrc.services.validation.models._
import models.ValidationErrorData

/**
 * Created by matt on 19/02/16.
 */
trait ERSValidationEMIRLCTestData {
  val rowNumber:Int = 1

  def getDescriptions: List[String] ={
    val descriptions =
      List(
        //column A
        "When dateOfEvent conforms to the expected date format, no validation error should be raised",
        "Return an error message when dateOfEvent does not conform to the expect date format",
        "Return an error message when dateOfEvent has been left empty",
        //column B
        "When disqualifyingEvent is a correctly formatted yes or no answer, no validation error should be raised",
        "Return an error message when disqualifyingEvent does not match yes or no",
        "Return an error message when disqualifyingEvent is left empty",
        //column C
        "When natureOfDisqualifyingEvent is a number between 1 & 8, no validation error should be raised",
        "Return an error message when natureOfDisqualifyingEvent is not within the specified range",
        "Return an error message when natureOfDisqualifyingEvent is not a number",
        "Return an error message when natureOfDisqualifyingEvent is larger than the maximum value",
        "Return an error message when natureOfDisqualifyingEvent contains a decimal point",
        "Return an error message when natureOfDisqualifyingEvent is a negative number",
        //column D
        "When individualRelLapsedCanc\\firstName is a correctly formatted first name, no validation error should be raised",
        "Return an error message when individualRelLapsedCanc\\firstName exceeds the maximum character length",
        "Return an error message when individualRelLapsedCanc\\firstName is left empty",
        //column E
        "When individualRelLapsedCanc\\secondName is a correctly formatted first name, no validation error should be raised",
        "Return an error message when individualRelLapsedCanc\\secondName exceeds the maximum character length",
        //column F
        "When individualRelLapsedCanc\\surname is a correctly formatted first name, no validation error should be raised",
        "Return an error message when individualRelLapsedCanc\\surname exceeds the maximum character length",
        "Return an error message when individualRelLapsedCanc\\surname is left empty",
        //column G
        "When individualRelLapsedCanc\\nino is a correctly formatted NINO, no validation error should be raised",
        "Return an error message when individualRelLapsedCanc\\nino does not conform to the expected NINO format",
        //column H
        "When individualRelLapsedCanc\\payeReference is a correctly formatted PAYE ref, no validation errror should be raised",
        "Reutrn an error message when individualRelLapsedCanc\\payeReference does not conform to the expected PAYE format",
        //column I
        "When numberOfSharesWhichCanNoLongerBeExercised is a valid number, no validation error should be raised",
        "Return an error message when numberOfSharesWhichCanNoLongerBeExercised does not have 2 numbers after the decimal point",
        "Return an error message when numberOfSharesWhichCanNoLongerBeExercised is not a number",
        "Return an error message when numberOfSharesWhichCanNoLongerBeExercised is larger than the maximum value allowed",
        //column J
        "When moneyValueReceived is yes or no, no validation error should be returned",
        "Return an error message when moneyValueReceived the entry does not match yes or no",
        "Return an error message when moneyValueReceived is left empty",
        //column K
        "When receivedAmount is a correctly formatted number, no validation error should be raised",
        "Return an error message when receivedAmount does not have 4 digits after the decimal point",
        "Return an error message when receivedAmount is not a number",
        "Return an error message when receivedAmount is too large",
        //column L
        "When payeOperatedApplied is Yes or No, no validation error should be raised",
        "Return an error message when payeOperatedApplied is not yes or no"

      )
    descriptions
  }

  def getTestData: List[Cell] ={
    val testData = List(
      Cell("A", rowNumber, "2014-12-10"),
      Cell("A", rowNumber, "12-2014-10"),
      Cell("A", rowNumber, ""),
      Cell("B", rowNumber, "Yes"),
      Cell("B", rowNumber, "Yess"),
      Cell("B", rowNumber, ""),
      Cell("C", rowNumber, "1"),
      Cell("C", rowNumber, "11"),
      Cell("C", rowNumber, "abc"),
      Cell("C", rowNumber, "9"),
      Cell("C", rowNumber, "3.14"),
      Cell("C", rowNumber, "-5"),
      Cell("D", rowNumber, "John"),
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
      Cell("I", rowNumber, "10.12"),
      Cell("I", rowNumber, "10.1234"),
      Cell("I", rowNumber, "AA123456A"),
      Cell("I", rowNumber, "12345678901234.12"),
      Cell("J", rowNumber, "yes"),
      Cell("J", rowNumber, "yesss"),
      Cell("J", rowNumber, ""),
      Cell("K", rowNumber, "123.1234"),
      Cell("K", rowNumber, "123.12345"),
      Cell("K", rowNumber, "abc"),
      Cell("K", rowNumber, "1234567890123456.1234"),
      Cell("L", rowNumber, "Yes"),
      Cell("L", rowNumber, "Noooo")

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
      Some(List(ValidationErrorData("error.3", "003", "Enter '1', '2', '3', '4', '5', '6', '7' or '8'."))),
      Some(List(ValidationErrorData("error.3", "003", "Enter '1', '2', '3', '4', '5', '6', '7' or '8'."))),
      Some(List(ValidationErrorData("error.3", "003", "Enter '1', '2', '3', '4', '5', '6', '7' or '8'."))),
      Some(List(ValidationErrorData("error.3", "003", "Enter '1', '2', '3', '4', '5', '6', '7' or '8'."))),
      Some(List(ValidationErrorData("error.3", "003", "Enter '1', '2', '3', '4', '5', '6', '7' or '8'."))),
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
      Some(List(ValidationErrorData("error.9", "009", "Must be a number with 2 digits after the decimal point (and no more than 11 digits in front of it)."))),
      Some(List(ValidationErrorData("error.9", "009", "Must be a number with 2 digits after the decimal point (and no more than 11 digits in front of it)."))),
      Some(List(ValidationErrorData("error.9", "009", "Must be a number with 2 digits after the decimal point (and no more than 11 digits in front of it)."))),
      None,
      Some(List(ValidationErrorData("error.10", "010", "Enter 'yes' or 'no'."))),
      Some(List(ValidationErrorData("error.10", "010", "Enter 'yes' or 'no'."))),
      None,
      Some(List(ValidationErrorData("error.11", "011", "Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
      Some(List(ValidationErrorData("error.11", "011", "Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
      Some(List(ValidationErrorData("error.11", "011", "Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
      None,
      Some(List(ValidationErrorData("error.12", "012", "Enter 'yes' or 'no'.")))
    )

    expectedResults
  }

  def getValidRowData:Seq[Cell] = {
    val rowData = Seq(
      Cell("A", rowNumber, "2014-12-10"),
      Cell("B", rowNumber, "yes"),
      Cell("C", rowNumber, "1"),
      Cell("D", rowNumber, "John"),
      Cell("E", rowNumber, "Jack"),
      Cell("F", rowNumber, "Jackson"),
      Cell("G", rowNumber, "AA123456A"),
      Cell("H", rowNumber, "123/XZ55555555"),
      Cell("I", rowNumber, "10.12"),
      Cell("J", rowNumber, "yes"),
      Cell("K", rowNumber, "123.1234"),
      Cell("L", rowNumber, "Yes")
    )
    rowData
  }

  def getInvalidRowData:Seq[Cell] = {
    val rowData = Seq(
      Cell("A", rowNumber, "20-12-2011"),
      Cell("A", rowNumber, ""),
      Cell("B", rowNumber, "noooo"),
      Cell("C", rowNumber, "abc"),
      Cell("C", rowNumber, "9"),
      Cell("C", rowNumber, "3.14"),
      Cell("C", rowNumber, "-5"),
      Cell("C", rowNumber, ""),
      Cell("D", rowNumber, "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz"),
      Cell("D", rowNumber, ""),
      Cell("E", rowNumber, "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz"),
      Cell("F", rowNumber, "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz"),
      Cell("F", rowNumber, ""),
      Cell("G", rowNumber, "AAa123a4561Aa"),
      Cell("H", rowNumber, "1a123/XZ533555a5555"),
      Cell("I", rowNumber, "10.1234"),
      Cell("I", rowNumber, "AA123456A"),
      Cell("I", rowNumber, "12345678901234.12"),
      Cell("J", rowNumber, "yesss"),
      Cell("J", rowNumber, ""),
      Cell("K", rowNumber, "123.12345"),
      Cell("K", rowNumber, "abc"),
      Cell("K", rowNumber, "1234567890123456.1234"),
      Cell("L", rowNumber, "Noooo")
    )
    rowData
  }
}
