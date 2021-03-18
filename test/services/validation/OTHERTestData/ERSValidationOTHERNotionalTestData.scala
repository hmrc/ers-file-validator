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

package services.validation.OTHERTestData


import uk.gov.hmrc.services.validation.models._
import models.ValidationErrorData
import org.apache.commons.lang3.StringUtils

trait ERSValidationOTHERNotionalTestData {

  val rowNumber:Int = 1

  def getDescriptions: List[String] = {
    val descriptions =
      List(
      //column A
        "When dateOfEvent is a correctly formatted date, no validation error should be raised",
        "Return an error message when dateOfEvent does not conform to the expected data format",
        "Return an error message when dateOfEvent has been left empty",
      //column B
        "When inRelationToASchemeWithADOTASRef is yes or no, no validation error should be raised",
        "Return an error message when inRelationToASchemeWithADOTASRef is not yes or no",
        "Return an error message when inRelationToASchemeWithADOTASRef has been left empty",
      //column C
        "When dotasRef is a correctly formatted RSN, no validation error should be raised",
        "Return an error message when dotasRef does not conform to the expected SRN format",
      //column D
        "When individualPAD\\firstName is a correctly formatted name, no validation error should be raised",
        "Return an error message when individualPAD\\firstName exceeds the maximum character length",
        "Return an error message when individualPAD\\firstName has been left empty",
      //column E
        "When individualPAD\\secondName is a correctly formatted name, no validation error should be raised",
        "Return an error message when individualPAD\\secondName exceeds the maximum character length",
      //column F
        "When individualPAD\\surname is a correctly formatted name, no validation error should be raised",
        "Return an error message when individualPAD\\surname exceeds the maximum character length",
        "Return an error message when individualPAD\\surname has been left empty",
      //column G
        "When individualPAD\\nino is a correctly formatted NINO, no validation error should be raised",
        "Return an error message when individualPAD\\nino does not conform to the expected NINO format",
      //column H
        "When individualPAD\\payeReference is a correctly formatted PAYE reference, no validation error should be raised",
        "Return an error message when nindividualPAD\\payeReference does not conform to the expected PAYE format",
      //column I
        "When dateSecuritiesOriginallyAcquired is a correctly formatted date, no validation error should be raised",
        "Return an error message when dateSecuritiesOriginallyAcquired does not conform to the expected data format",
        "Return an error message when dateSecuritiesOriginallyAcquired has been left empty",
      //column J
        "When numberOfSecuritiesOriginallyAcquired conforms to the expected number format, no validation error should be raised",
        "Return an error message when numberOfSecuritiesOriginallyAcquired does not have 2 numbers after the decimal point",
        "Return an error message when numberOfSecuritiesOriginallyAcquired is not a number",
        "Return an error message when numberOfSecuritiesOriginallyAcquired exceeds the maximum value",
      //column K
        "When amountOfNotionalLoanOutstanding conforms to the expected number format, no validation error should be raised",
        "Return an error message when amountOfNotionalLoanOutstanding does not have 4 numbers after the decimal point",
        "Return an error message when amountOfNotionalLoanOutstanding is not a number",
        "Return an error message when amountOfNotionalLoanOutstanding exceeds the maximum value",
      //column L
        "When payeOperatedApplied is a yes or no, no validation error should be raised",
        "Return an error message when payeOperatedApplied is not yes or no",
        "Return an error message when payeOperatedApplied is left empty",
        //column M
        "When adjusmentMadeForUKDuties is a yes or no, no validation error should be raised",
        "Return an error message when adjusmentMadeForUKDuties is not yes or no",
        "Return an error message when adjusmentMadeForUKDuties is left empty"
      )
    descriptions
  }

  def getTestData: List[Cell] = {
    val testData = List(
      Cell("A",rowNumber,"2014-08-30"),
      Cell("A",rowNumber,"2140830"),
      Cell("A",rowNumber,""),
      Cell("B",rowNumber,"Yes"),
      Cell("B",rowNumber,"Yess"),
      Cell("B",rowNumber,""),
      Cell("C",rowNumber,"12345678"),
      Cell("C",rowNumber,"123456723456789"),
      Cell("D",rowNumber,"John"),
      Cell("D",rowNumber,StringUtils.leftPad("", 36, "A")),
      Cell("D",rowNumber,""),
      Cell("E",rowNumber,"Jack"),
      Cell("E",rowNumber,StringUtils.leftPad("", 36, "A")),
      Cell("F",rowNumber,"Jackson"),
      Cell("F",rowNumber,StringUtils.leftPad("", 36, "A")),
      Cell("F",rowNumber,""),
      Cell("G",rowNumber,"AB123456A"),
      Cell("G",rowNumber,"AAB123456A"),
      Cell("H",rowNumber,"123/XZ55555555"),
      Cell("H",rowNumber,"AABaaa/123456A///"),
      Cell("I",rowNumber,"2014-08-30"),
      Cell("I",rowNumber,"2140830"),
      Cell("I",rowNumber,""),
      Cell("J",rowNumber,"120.00"),
      Cell("J",rowNumber,"120.123"),
      Cell("J",rowNumber,"oneTwoZero"),
      Cell("J",rowNumber,"123456789012.34"),
      Cell("K",rowNumber,"120.1234"),
      Cell("K",rowNumber,"120.12345"),
      Cell("K",rowNumber,"oneTwoZero"),
      Cell("K",rowNumber,"12345678901234.5678"),
      Cell("L",rowNumber,"Yes"),
      Cell("L",rowNumber,"Yess"),
      Cell("L",rowNumber,""),
      Cell("M",rowNumber,"Yes"),
      Cell("M",rowNumber,"Yess"),
      Cell("M",rowNumber,"")



    )
    testData
  }

  def getExpectedResults: List[Option[List[ValidationErrorData]]] = {
    val expectedResults = List(
      //column A
      None,
      Some(List(ValidationErrorData("error.1","001","Enter a date that matches the yyyy-mm-dd pattern."))),
      Some(List(ValidationErrorData("error.1","001","Enter a date that matches the yyyy-mm-dd pattern."))),
      //column B
      None,
      Some(List(ValidationErrorData("error.2","002","Enter 'yes' or 'no'."))),
      Some(List(ValidationErrorData("error.2","002","Enter 'yes' or 'no'."))),
      //column C
      None,
      Some(List(ValidationErrorData("error.3","003","Enter the scheme reference number (it should be an 8 digit number)."))),
      //column D
      None,
      Some(List(ValidationErrorData("error.4","004","Enter a first name (must be less than 36 characters and can only have letters, numbers, hyphens or apostrophes)."))),
      Some(List(ValidationErrorData("error.4","004","Enter a first name (must be less than 36 characters and can only have letters, numbers, hyphens or apostrophes)."))),
      //column E
      None,
      Some(List(ValidationErrorData("error.5","005","Must be less than 36 characters and can only have letters, numbers, hyphens or apostrophes."))),
      //column F
      None,
      Some(List(ValidationErrorData("error.6","006","Enter a last name (must be less than 36 characters and can only have letters, numbers, hyphens or apostrophes)."))),
      Some(List(ValidationErrorData("error.6","006","Enter a last name (must be less than 36 characters and can only have letters, numbers, hyphens or apostrophes)."))),
      //column G
      None,
      Some(List(ValidationErrorData("error.7","007","National Insurance number must be 2 letters followed by 6 number digits, with an optional final letter."))),
      //column H
      None,
      Some(List(ValidationErrorData("error.8","008","PAYE reference must be a 3 digit number followed by a forward slash and up to 10 more characters."))),
      //column I
      None,
      Some(List(ValidationErrorData("error.9","009","Enter a date that matches the yyyy-mm-dd pattern."))),
      Some(List(ValidationErrorData("error.9","009","Enter a date that matches the yyyy-mm-dd pattern."))),
      //column J
      None,
      Some(List(ValidationErrorData("error.10","010","Must be a number with 2 digits after the decimal point (and no more than 11 digits in front of it)."))),
      Some(List(ValidationErrorData("error.10","010","Must be a number with 2 digits after the decimal point (and no more than 11 digits in front of it)."))),
      Some(List(ValidationErrorData("error.10","010","Must be a number with 2 digits after the decimal point (and no more than 11 digits in front of it)."))),
      //column K
      None,
      Some(List(ValidationErrorData("error.11","011","Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
      Some(List(ValidationErrorData("error.11","011","Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
      Some(List(ValidationErrorData("error.11","011","Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
      //column L
      None,
      Some(List(ValidationErrorData("error.12","012","Enter 'yes' or 'no'."))),
      Some(List(ValidationErrorData("error.12","012","Enter 'yes' or 'no'."))),
      //column M
      None,
      Some(List(ValidationErrorData("error.13","013","Enter 'yes' or 'no'."))),
      Some(List(ValidationErrorData("error.13","013","Enter 'yes' or 'no'."))),
    )
    expectedResults
  }

  def getValidRowData:Seq[Cell] = {
    val rowData = Seq(
      Cell("A",rowNumber,"2014-08-30"),
      Cell("B",rowNumber,"Yes"),
      Cell("C",rowNumber,"12345678"),
      Cell("D",rowNumber,"John"),
      Cell("E",rowNumber,"Jack"),
      Cell("F",rowNumber,"Jackson"),
      Cell("G",rowNumber,"AB123456A"),
      Cell("H",rowNumber,"123/XZ55555555"),
      Cell("I",rowNumber,"2014-08-30"),
      Cell("J",rowNumber,"120.00"),
      Cell("K",rowNumber,"120.1234"),
      Cell("L",rowNumber,"Yes"),
      Cell("M",rowNumber,"Yes")
    )
    rowData
  }

  def getInvalidRowData:Seq[Cell] = {
    val rowData = Seq(
      Cell("A",rowNumber,"2140830"),
      Cell("B",rowNumber,"Yess"),
      Cell("C",rowNumber,"12323456456789"),
      Cell("D",rowNumber,StringUtils.leftPad("", 36, "A")),
      Cell("E",rowNumber,StringUtils.leftPad("", 36, "A")),
      Cell("F",rowNumber,StringUtils.leftPad("", 36, "A")),
      Cell("G",rowNumber,"AAB123456A"),
      Cell("H",rowNumber,"AABaaa/123456A///"),
      Cell("I",rowNumber,"2140830"),
      Cell("J",rowNumber,"120.123"),
      Cell("K",rowNumber,"120.12345"),
      Cell("L",rowNumber,"Yess"),
      Cell("M",rowNumber,"Yess")
    )
    rowData
  }

}
