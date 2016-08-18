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

package services.validation.SIPTestData

import uk.gov.hmrc.services.validation.Cell
import models.ValidationErrorData


trait ERSValidationSIPOutTestData {
  val rowNumber:Int = 1

  def getDescriptions: List[String] = {
    val descriptions =
      List(
        //column A
        "When dateOfGrant conforms to the expected date format, no validation error should be raised",
        "Return an error message when dateOfGrant does not conform to the expect date format",
        "Return an error message when dateOfGrant has been left empty",
        //column B
        "When individualReleased\\firstName is a correctly formatted first name, no validation error should be raised",
        "Return an error message when individualReleased\\firstName exceeds the maximum character length",
        "Return an error message when individualReleased\\firstName is left empty",
        //column C
        "When individualReleased\\secondName is a correctly formatted first name, no validation error should be raised",
        "Return an error message when individualReleased\\secondName exceeds the maximum character length",
        //column D
        "When individualReleased\\surname is a correctly formatted first name, no validation error should be raised",
        "Return an error message when individualReleased\\surname exceeds the maximum character length",
        "Return an error message when individualReleased\\surname is left empty",
        //column E
        "When the national insurance number conforms to the expected format, no validation error should be raised",
        "Return an error message when the national insurance number is invalid",
        //column F
        "When the PAYE reference is valid, no validation error should be raised",
        "Return an error message when an invalid PAYE reference is provided",
        //coluimn G
        "When valid number of free shares is enterered, no validation error should be raised",
        "Return an error message when free shares does not contain two decimal places",
        "Return an error message when free shares exceeds 11 digits",
        //coluimn H
        "When valid number of partnership shares is enterered, no validation error should be raised",
        "Return an error message when partnership shares does not contain two decimal places",
        "Return an error message when partnership shares exceeds 11 digits",
        //coluimn I
        "When valid number of matching shares is enterered, no validation error should be raised",
        "Return an error message when matching shares does not contain two decimal places",
        "Return an error message when matching shares exceeds 11 digits",
        //coluimn J
        "When valid number of dividend shares is enterered, no validation error should be raised",
        "Return an error message when dividend shares does not contain two decimal places",
        "Return an error message when dividend shares exceeds 11 digits",
        //coluimn K
        "When valid number of market value per free share is enterered, no validation error should be raised",
        "Return an error message when market value per free share does not contain four decimal places",
        "Return an error message when market value per free share exceeds 13 digits",
        //column L
        "When column L conforms to the expected number format, no validation error should be raised",
        "Return an error message when column L does not have 2 digits after the decimal point",
        "Return an error message when column L is not a number",
        "Return an error message when column L is larger than the maximum value allowed",
        //column M
        "When column M conforms to the expected number format, no validation error should be raised",
        "Return an error message when column M does not have 2 digits after the decimal point",
        "Return an error message when column M is not a number",
        "Return an error message when column M is larger than the maximum value allowed",
        //column N
        "When awarded/totalValueOfSharesAwarded conforms to the expected number format, no validation error should be raised",
        "Return an error message when awarded/totalValueOfSharesAwarded does not have 2 digits after the decimal point",
        "Return an error message when awarded/totalValueOfSharesAwarded is not a number",
        "Return an error message when awarded/totalValueOfSharesAwarded is larger than the maximum value allowed",
        //column O
        "When awarded/sharesListedOnSE is yes or no, no validation error should be raised",
        "Return an error message when awarded/sharesListedOnSE is not yes or no",
        "Return an error message when awarded/sharesListedOnSE is left empty",
        //column P
        "When awarded/marketValueAgreedHMRC is yes or no, no validation error should be raised",
        "Return an error message when awarded/marketValueAgreedHMRC is not yes or no",
        //column Q
        "When column Q is yes or no, no validation error should be raised",
        "Return an error message when column Q is not yes or no"
      )
    descriptions
  }

  def getTestData: List[Cell] = {
    val testData = List(
      Cell("A", rowNumber, "2014-12-10"),
      Cell("A", rowNumber, "12-12-2014"),
      Cell("A", rowNumber, ""),
      Cell("B", rowNumber, "Guss"),
      Cell("B", rowNumber, "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz"),
      Cell("B", rowNumber, ""),
      Cell("C", rowNumber, "Jack"),
      Cell("C", rowNumber, "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz"),
      Cell("D", rowNumber, "Jackson"),
      Cell("D", rowNumber, "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz"),
      Cell("D", rowNumber, ""),
      Cell("E", rowNumber, "AB123456A"),
      Cell("E", rowNumber, "AB123456A.12"),
      Cell("F", rowNumber, "123/XZ55555555"),
      Cell("F", rowNumber, "123/XZ55555555.12"),
      Cell("G", rowNumber, "12.12"),
      Cell("G", rowNumber, "12.1234"),
      Cell("G", rowNumber, "123456789012.12"),
      Cell("H", rowNumber, "12.12"),
      Cell("H", rowNumber, "12.1234"),
      Cell("H", rowNumber, "123456789012.12"),
      Cell("I", rowNumber, "12.12"),
      Cell("I", rowNumber, "12.1234"),
      Cell("I", rowNumber, "123456789012.12"),
      Cell("J", rowNumber, "12.12"),
      Cell("J", rowNumber, "12.1234"),
      Cell("J", rowNumber, "123456789012.12"),
      Cell("K", rowNumber, "12.1234"),
      Cell("K", rowNumber, "12.12"),
      Cell("K", rowNumber, "12345678901234.1234"),
      Cell("L", rowNumber, "1234567890123.1234"),
      Cell("L", rowNumber, "1234.12345"),
      Cell("L", rowNumber, "oneTwoThree"),
      Cell("L", rowNumber, "123456789012345.1234"),
      Cell("M", rowNumber, "1234567890123.1234"),
      Cell("M", rowNumber, "1234.12345"),
      Cell("M", rowNumber, "oneTwoThree"),
      Cell("M", rowNumber, "123456789012345.1234"),
      Cell("N", rowNumber, "1234567890123.1234"),
      Cell("N", rowNumber, "1234.12345"),
      Cell("N", rowNumber, "oneTwoThree"),
      Cell("N", rowNumber, "123456789012345.1234"),
      Cell("O", rowNumber, "Yes"),
      Cell("O", rowNumber, "yess"),
      Cell("O", rowNumber, ""),
      Cell("P", rowNumber, "Yes"),
      Cell("P", rowNumber, "yess"),
      Cell("Q", rowNumber, "Yes"),
      Cell("Q", rowNumber, "yess")
    )
    testData
  }

  def getExpectedResults: List[Option[List[ValidationErrorData]]] = {
    val expectedResults = List(
      //column A
      None,
      Some(List(ValidationErrorData("error.1", "001", "Enter a date that matches the yyyy-mm-dd pattern."))),
      Some(List(ValidationErrorData("MANDATORY", "100", "Enter a date that matches the yyyy-mm-dd pattern."))),
      // column B
      None,
      Some(List(ValidationErrorData("error.2", "002", "Enter a first name (must be less than 36 characters and can only have letters, numbers, hyphens or apostrophes)."))),
      Some(List(ValidationErrorData("MANDATORY", "100", "Enter a first name (must be less than 36 characters and can only have letters, numbers, hyphens or apostrophes)."))),
      // column C
      None,
      Some(List(ValidationErrorData("error.3", "003", "Must be less than 36 characters and can only have letters, numbers, hyphens or apostrophes."))),
      // column D
      None,
      Some(List(ValidationErrorData("error.4", "004", "Enter a last name (must be less than 36 characters and can only have letters, numbers, hyphens or apostrophes)."))),
      Some(List(ValidationErrorData("MANDATORY", "100", "Enter a last name (must be less than 36 characters and can only have letters, numbers, hyphens or apostrophes)."))),
      //column E
      None,
      Some(List(ValidationErrorData("error.5", "005", "National Insurance number must be 2 letters followed by 6 number digits, with an optional final letter."))),
      //column F
      None,
      Some(List(ValidationErrorData("error.6", "006", "PAYE reference must be a 3 digit number followed by a forward slash and up to 10 more characters."))),
      //column G
      None,
      Some(List(ValidationErrorData("error.7", "007", "Must be a number with 2 digits after the decimal point (and no more than 11 digits in front of it)."))),
      Some(List(ValidationErrorData("error.7", "007", "Must be a number with 2 digits after the decimal point (and no more than 11 digits in front of it)."))),
      //column H
      None,
      Some(List(ValidationErrorData("error.8", "008", "Must be a number with 2 digits after the decimal point (and no more than 11 digits in front of it)."))),
      Some(List(ValidationErrorData("error.8", "008", "Must be a number with 2 digits after the decimal point (and no more than 11 digits in front of it)."))),
      //column I
      None,
      Some(List(ValidationErrorData("error.9", "009", "Must be a number with 2 digits after the decimal point (and no more than 11 digits in front of it)."))),
      Some(List(ValidationErrorData("error.9", "009", "Must be a number with 2 digits after the decimal point (and no more than 11 digits in front of it)."))),
      //column J
      None,
      Some(List(ValidationErrorData("error.10", "010", "Must be a number with 2 digits after the decimal point (and no more than 11 digits in front of it)."))),
      Some(List(ValidationErrorData("error.10", "010", "Must be a number with 2 digits after the decimal point (and no more than 11 digits in front of it)."))),
      //column K
      None,
      Some(List(ValidationErrorData("error.11", "011", "Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
      Some(List(ValidationErrorData("error.11", "011", "Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
      //column L
      None,
      Some(List(ValidationErrorData("error.12", "012", "Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
      Some(List(ValidationErrorData("error.12", "012", "Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
      Some(List(ValidationErrorData("error.12", "012", "Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
      //column M
      None,
      Some(List(ValidationErrorData("error.13", "013", "Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
      Some(List(ValidationErrorData("error.13", "013", "Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
      Some(List(ValidationErrorData("error.13", "013", "Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
      //column N
      None,
      Some(List(ValidationErrorData("error.14", "014", "Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
      Some(List(ValidationErrorData("error.14", "014", "Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
      Some(List(ValidationErrorData("error.14", "014", "Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
      //column O
      None,
      Some(List(ValidationErrorData("error.15", "015", "Enter 'yes' or 'no'."))),
      Some(List(ValidationErrorData("MANDATORY", "100", "Enter 'yes' or 'no'."))),
      //column P
      None,
      Some(List(ValidationErrorData("error.16", "016", "Enter 'yes' or 'no'."))),
      //column Q
      None,
      Some(List(ValidationErrorData("error.17", "017", "Enter 'yes' or 'no'.")))
    )
    expectedResults
  }

  def getValidRowData:Seq[Cell] = {
    val rowData = Seq(
      Cell("A", rowNumber, "2014-12-10"),
      Cell("B", rowNumber, "Guss"),
      Cell("C", rowNumber, "Jack"),
      Cell("D", rowNumber, "Jackson"),
      Cell("E", rowNumber, "AB123456A"),
      Cell("F", rowNumber, "123/XZ55555555"),
      Cell("G", rowNumber, "12.12"),
      Cell("H", rowNumber, "12.12"),
      Cell("I", rowNumber, "12.12"),
      Cell("J", rowNumber, "12.12"),
      Cell("K", rowNumber, "12.1234")
    )
    rowData
  }

  def getInvalidRowData:Seq[Cell] = {
    val rowData = Seq(
      Cell("A", rowNumber, "20-12-2011"),
      Cell("B", rowNumber, "1234567890.12"),
      Cell("C", rowNumber, "123.12"),
      Cell("D", rowNumber, "12345678901234.12"),
      Cell("E", rowNumber, "AB123456A.12"),
      Cell("F", rowNumber, "123/XZ55555555.12"),
      Cell("G", rowNumber, "12.1234"),
      Cell("H", rowNumber, "12.1234"),
      Cell("I", rowNumber, "12.1234"),
      Cell("J", rowNumber, "12.1234"),
      Cell("K", rowNumber, "12.12")
    )
    rowData
  }

}
