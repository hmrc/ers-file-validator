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
 * Created by matt on 16/02/16.
 */
trait ERSValidationEMIReplacedTestData {
  val rowNumber:Int = 1

  def getDescriptions: List[String] ={
    val descriptions =
      List(
        "When grantDateOfOldOption conforms to the expected date format, no validation error should be raised",
        "Return an error message when grantDateOfOldOption does not conform to the expect date format",
        "Return an error message when grantDateOfOldOption has been left empty",
        "When grantDateOfNewOption conforms to the expected date format, no validation error should be raised",
        "Return an error message when grantDateOfNewOption does not conform to the expect date format",
        "Return an error message when grantDateOfNewOption has been left empty",
        "When individualReleased\\firstName conforms to the expected name format, no validation error should be raised",
        "Return an error message individualReleased\\firstName exceeds 35 characters",
        "Return an error message when individualReleased\\firstName is left empty",
        "When individualReleased\\secondName conforms to the expected name format, no validation error should be raised",
        "Return an error message when individualReleased\\secondName exceeds 35 characters",
        "When individualReleased\\surname conforms to the expected name format, no validation error should be raised",
        "Return an error message individualReleased\\surname exceeds 35 characters",
        "Return an error message when individualReleased\\surname is left empty",
        "When individualReleased\\nino is a correctly formatted NINO, no validation error should be raised",
        "Reurn an error message if individualReleased\\nino is no a correctly formatted NINO",
        "When individualReleased\\payeReference conforms to the expected PAYE ref format, no validation error should be raised",
        "Return an error message if individualReleased\\payeReference does not conform to the expected PAYE Ref format.",
        "When actualMarketValuePerShareReplacementAtDate conforms to the expected money format, no validation error should be raised",
        "Return an error message when actualMarketValuePerShareReplacementAtDate does not have 4 digits after the decimal point",
        "Return an error message when actualMarketValuePerShareReplacementAtDate is not a number",
        "Return an error message when actualMarketValuePerShareReplacementAtDate is larger than the maximum allowed value",
        "When snopCompany\\companyName is a correctly formatted company name, no validation errors should be returned",
        "Return an error message when snopCompany\\companyName exceeds the maximum amount of characters allowed",
        "Reurn an error message when snopCompany\\companyName is left empty",
        "When snopCompany\\companyAddress\\addressLine1 is a correctly formatted address, no validation error should be raised",
        "Return an error message when snopCompany\\companyAddress\\addressLine1 exceeds the maximum character limit",
        "Return an error message when snopCompany\\companyAddress\\addressLine1 is left empty",
        "When snopCompany\\companyAddress\\addressLine2 is a correctly formatted address, no validation error should be raised",
        "Return an error message when snopCompany\\companyAddress\\addressLine2 exceeds the maximum character limit",
        "When snopCompany\\companyAddress\\addressLine3 is a correctly formatted address, no validation error should be raised",
        "Return an error message when snopCompany\\companyAddress\\addressLine3 exceeds the maximum character limit",
        "When snopCompany\\companyAddress\\addressLine4 is a correctly formatted address, no validation error should be raised",
        "Return an error message when snopCompany\\companyAddress\\addressLine4 exceeds the maximum character limit",
        "When snopCompany\\companyAddress\\country is a correctly formatted country name, no validation error should be raised",
        "Return an error message when snopCompany\\companyAddress\\country exceeds the maximum character limit for countries",
        "When snopCompany\\companyAddress\\postCode is a correctly formatted postcode, no validation error should be raised",
        "Return an error when snopCompany\\companyAddress\\postCode exceeds the character limit for postcodes",
        "Return an error when snopCompany\\companyAddress\\postCode is left empty",
        "When snopCompany\\companyCTRef is a correctly formatted Company CT Ref, no validation error should be raised",
        "Return an error when snopCompany\\companyCTRef is not a 10 digit number",
        "When snopCompany\\companyCRN is a valid company reference number, no validation error should be raised",
        "Return an error when snopCompany\\companyCRN does not conform to the expected Company Reference Number format"
      )
    descriptions
  }

  def getTestData: List[Cell] ={
    val testData = List(
      Cell("A", rowNumber, "2014-12-10"),
      Cell("A", rowNumber, "12-2014-10"),
      Cell("A", rowNumber, ""),
      Cell("B", rowNumber, "2014-12-10"),
      Cell("B", rowNumber, "12-2014-10"),
      Cell("B", rowNumber, ""),
      Cell("C", rowNumber, "John"),
      Cell("C", rowNumber, "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz"),
      Cell("C", rowNumber, ""),
      Cell("D", rowNumber, "Jack"),
      Cell("D", rowNumber, "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz"),
      Cell("E", rowNumber, "Jackson"),
      Cell("E", rowNumber, "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz"),
      Cell("E", rowNumber, ""),
      Cell("F", rowNumber, "AA123456A"),
      Cell("F", rowNumber, "aaaAA12223456Aaaaa"),
      Cell("G", rowNumber, "123/XZ55555555"),
      Cell("G", rowNumber, "1a231/XZ555a555155"),
      Cell("H", rowNumber, "10.1234"),
      Cell("H", rowNumber, "123.12345"),
      Cell("H", rowNumber, "one point one"),
      Cell("H", rowNumber, "12345678901234567890.1234"),
      Cell("I", rowNumber, "company"),
      Cell("I", rowNumber, "abcdefghijabcdefghijabcdefghijabcdefghijabcdefghijabcdefghijabcdefghijabcdefghijabcdefghijabcdefghijabcdefghijabcdefghijabcdefghijabcdefghijabcdefghijabcdefghijabcdefghijabcdefghijabcdefghijabcdefghij"),
      Cell("I", rowNumber, ""),
      Cell("J", rowNumber, "1 Beth Street"),
      Cell("J", rowNumber, "abcdefghijabcdefghijabcdefghijabcdefghijabcdefghijabcdefgh"),
      Cell("J", rowNumber, ""),
      Cell("K", rowNumber, "Bucknall"),
      Cell("K", rowNumber, "abcdefghijabcdefghijabcdefghijabcdefghijabcdefghijabcdefgh"),
      Cell("L", rowNumber, "Stoke"),
      Cell("L", rowNumber, "abcdefghijabcdefghijabcdefghijabcdefghijabcdefghijabcdefgh"),
      Cell("M", rowNumber, "Staffordshire"),
      Cell("M", rowNumber, "abcdefghijabcdefghijabcdefghijabcdefghijabcdefghijabcdefgh"),
      Cell("N", rowNumber, "United Kingdom"),
      Cell("N", rowNumber, "abcdefghijabcdefghijabcdefghijabcdefghijabcdefghijabcdefgh"),
      Cell("O", rowNumber, "SE1 2AB"),
      Cell("O", rowNumber, "abcdefghijabcdefghijabcdefghijabcdefghijabcdefghijabcdefgh"),
      Cell("O", rowNumber, ""),
      Cell("P", rowNumber, "1234567890"),
      Cell("P", rowNumber, "12345678901"),
      Cell("Q", rowNumber, "AC097609"),
      Cell("Q", rowNumber, "AAC0A9976009")

    )
    testData
  }

  def getExpectedResults: List[Option[List[ValidationErrorData]]] = {
    val expectedResults = List(
      None,
      Some(List(ValidationErrorData("error.1", "001", "Enter a date that matches the yyyy-mm-dd pattern."))),
      Some(List(ValidationErrorData("error.1", "001", "Enter a date that matches the yyyy-mm-dd pattern."))),
      None,
      Some(List(ValidationErrorData("error.2", "002", "Enter a date that matches the yyyy-mm-dd pattern."))),
      Some(List(ValidationErrorData("error.2", "002", "Enter a date that matches the yyyy-mm-dd pattern."))),
      None,
      Some(List(ValidationErrorData("error.3", "003", "Enter a first name (must be less than 36 characters and can only have letters, numbers, hyphens or apostrophes)."))),
      Some(List(ValidationErrorData("error.3", "003", "Enter a first name (must be less than 36 characters and can only have letters, numbers, hyphens or apostrophes)."))),
      None,
      Some(List(ValidationErrorData("error.4", "004", "Must be less than 36 characters and can only have letters, numbers, hyphens or apostrophes."))),
      None,
      Some(List(ValidationErrorData("error.5", "005", "Enter a last name (must be less than 36 characters and can only have letters, numbers, hyphens or apostrophes)."))),
      Some(List(ValidationErrorData("error.5", "005", "Enter a last name (must be less than 36 characters and can only have letters, numbers, hyphens or apostrophes)."))),
      None,
      Some(List(ValidationErrorData("error.6", "006", "National Insurance number must be 2 letters followed by 6 number digits, with an optional final letter."))),
      None,
      Some(List(ValidationErrorData("error.7", "007", "PAYE reference must be a 3 digit number followed by a forward slash and up to 10 more characters."))),
      None,
      Some(List(ValidationErrorData("error.8", "008", "Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
      Some(List(ValidationErrorData("error.8", "008", "Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
      Some(List(ValidationErrorData("error.8", "008", "Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
      None,
      Some(List(ValidationErrorData("error.9", "009", "Enter the company name (must be less than 121 characters and can only have letters, numbers, hyphens or apostrophes)."))),
      Some(List(ValidationErrorData("error.9", "009", "Enter the company name (must be less than 121 characters and can only have letters, numbers, hyphens or apostrophes)."))),
      None,
      Some(List(ValidationErrorData("error.10", "010", "Enter the first line of the address (must be less than 28 characters and can only have letters, numbers, hyphens, apostrophes, forward slashes, commas or ampersands)."))),
      Some(List(ValidationErrorData("error.10", "010", "Enter the first line of the address (must be less than 28 characters and can only have letters, numbers, hyphens, apostrophes, forward slashes, commas or ampersands)."))),
      None,
      Some(List(ValidationErrorData("error.11", "011", "Must be less than 28 characters and can only have letters, numbers, hyphens, apostrophes, forward slashes, commas or ampersands." ))),
      None,
      Some(List(ValidationErrorData("error.12", "012", "Must be less than 28 characters and can only have letters, numbers, hyphens, apostrophes, forward slashes, commas or ampersands." ))),
      None,
      Some(List(ValidationErrorData("error.13", "013", "Must be less than 19 characters and can only have letters, numbers, hyphens, apostrophes, forward slashes, commas or ampersands."))),
      None,
      Some(List(ValidationErrorData("error.14", "014", "Must be less than 19 characters and can only have letters, numbers, hyphens, apostrophes, forward slashes, commas or ampersands."))),
      None,
      Some(List(ValidationErrorData("error.15", "015", "Enter the postcode (must be 6 to 8 characters and only have capital letters)."))),
      Some(List(ValidationErrorData("error.15", "015", "Enter the postcode (must be 6 to 8 characters and only have capital letters)."))),
      None,
      Some(List(ValidationErrorData("error.16", "016", "Corporation Tax reference must be a 10 digit number."))),
      None,
      Some(List(ValidationErrorData("error.17", "017", "Company Reference Number must be less than 11 characters (numbers and letters).")))

    )

    expectedResults
  }

  def getValidRowData:Seq[Cell] = {
    val rowData = Seq(
      Cell("A", rowNumber, "2014-12-10"),
      Cell("B", rowNumber, "2014-12-10"),
      Cell("C", rowNumber, "John"),
      Cell("D", rowNumber, "Jack"),
      Cell("E", rowNumber, "Jackson"),
      Cell("F", rowNumber, "AA123456A"),
      Cell("G", rowNumber, "123/XZ55555555"),
      Cell("H", rowNumber, "10.1234"),
      Cell("I", rowNumber, "company"),
      Cell("J", rowNumber, "1 Beth Street"),
      Cell("K", rowNumber, "Bucknall"),
      Cell("L", rowNumber, "Stoke"),
      Cell("M", rowNumber, "Staffordshire"),
      Cell("N", rowNumber, "United Kingdom"),
      Cell("O", rowNumber, "SE1 2AB"),
      Cell("Q", rowNumber, "AC097609")
    )
    rowData
  }

  def getInvalidRowData:Seq[Cell] = {
    val rowData = Seq(
      Cell("A", rowNumber, "20-12-2011"),
      Cell("A", rowNumber, ""),
      Cell("B", rowNumber, "20-12-2011"),
      Cell("B", rowNumber, ""),
      Cell("C", rowNumber, "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz"),
      Cell("C", rowNumber, ""),
      Cell("D", rowNumber, "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz"),
      Cell("E", rowNumber, "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz"),
      Cell("E", rowNumber, ""),
      Cell("F", rowNumber, "aaaAA12223456Aaaaa"),
      Cell("G", rowNumber, "1a231/XZ555a555155"),
      Cell("H", rowNumber, "123.12345"),
      Cell("H", rowNumber, "one point one"),
      Cell("H", rowNumber, "12345678901234567890.1234"),
      Cell("I", rowNumber, "abcdefghijabcdefghijabcdefghijabcdefghijabcdefghijabcdefghijabcdefghijabcdefghijabcdefghijabcdefghijabcdefghijabcdefghijabcdefghijabcdefghijabcdefghijabcdefghijabcdefghijabcdefghijabcdefghijabcdefghij"),
      Cell("I", rowNumber, ""),
      Cell("J", rowNumber, "abcdefghijabcdefghijabcdefghijabcdefghijabcdefghijabcdefgh"),
      Cell("J", rowNumber, ""),
      Cell("K", rowNumber, "abcdefghijabcdefghijabcdefghijabcdefghijabcdefghijabcdefgh"),
      Cell("L", rowNumber, "abcdefghijabcdefghijabcdefghijabcdefghijabcdefghijabcdefgh"),
      Cell("M", rowNumber, "abcdefghijabcdefghijabcdefghijabcdefghijabcdefghijabcdefgh"),
      Cell("N", rowNumber, "abcdefghijabcdefghijabcdefghijabcdefghijabcdefghijabcdefgh"),
      Cell("O", rowNumber, "abcdefghijabcdefghijabcdefghijabcdefghijabcdefghijabcdefgh"),
      Cell("O", rowNumber, ""),
      Cell("Q", rowNumber, "AAC0A9976009")
    )
    rowData
  }
}
