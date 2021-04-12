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
import org.apache.commons.lang3.StringUtils

trait ERSValidationEMINonTaxableTestData {

  val rowNumber:Int = 1

  def getDescriptions: List[String] = {
    val descriptions =
      List(
        //Column A
        "validate exerciseDate without ValidationErrors for valid data",
        "validate exerciseDate with ValidationErrors for an invalid date",
        "validate exerciseDate with ValidationErrors for an empty field",
        //Column B
        "validate individualNTExercise\\firstName without ValidationErrors for an empty field",
        "validate individualNTExercise\\firstName with ValidationErrors for a string too long",
        "validate individualNTExercise\\firstName with ValidationErrors for an empty field",
        //Column C
        "validate individualNTExercise\\secondName without ValidationErrors for valid data",
        "validate individualNTExercise\\secondName with ValidationErrors for a string too long",
        //Column D
        "validate individualNTExercise\\surname without ValidationErrors for an empty field",
        "validate individualNTExercise\\surname with ValidationErrors for a string too long",
        "validate individualNTExercise\\surname with ValidationErrors for an empty field",
        //Column E
        "validate individualNTExercise\\nino without ValidationErrors for valid data",
        "validate individualNTExercise\\nino with ValidationErrors for an invalid nino",
        //Column F
        "validate individualNTExercise\\payeReference without ValidationErrors for a valid payeReference",
        "validate individualNTExercise\\payeReference with ValidationErrors for an invalid payeReference",
        //Column G
        "validate numberOfSharesAcquired without ValidationErrors for valid data",
        "validate numberOfSharesAcquired with ValidationErrors for a number with more than 2 decimal points",
        "validate numberOfSharesAcquired with ValidationErrors for an alphanumeric string",
        "validate numberOfSharesAcquired with ValidationErrors for a number too large",
        //Column H
        "validate actualMarketValueAtGrantDate without ValidationErrors for valid data",
        "validate actualMarketValueAtGrantDate with ValidationErrors for a number with more than 4 decimal points",
        "validate actualMarketValueAtGrantDate with ValidationErrors for an alphanumeric string",
        "validate actualMarketValueAtGrantDate with ValidationErrors for a number too large",
        //Column I
        "validate exercisePrice without ValidationErrors for valid data",
        "validate exercisePrice with ValidationErrors for a number with more than 4 decimal points",
        "validate exercisePrice with ValidationErrors for an alphanumeric string",
        "validate exercisePrice with ValidationErrors for a number too large",
        //Column J
        "validate actualMarketValuePerShareAtExerciseDate without ValidationErrors for valid data",
        "validate actualMarketValuePerShareAtExerciseDate with ValidationErrors for a number with more than 4 decimal points",
        "validate actualMarketValuePerShareAtExerciseDate with ValidationErrors for an alphanumeric string",
        "validate actualMarketValuePerShareAtExerciseDate with ValidationErrors for a number too large",
        //Column K
        "validate sharesListedOnSE without ValidationErrors for valid data",
        "validate sharesListedOnSE with ValidationErrors for invalid data",
        "validate sharesListedOnSE with ValidationErrors for an empty field",
        //Column L
        "validate marketValueAgreedHMRC without ValidationErrors for valid data",
        "validate marketValueAgreedHMRC with ValidationErrors for invalid data",
        //Column M
        "validate hmrcRef without ValidationErrors for valid data",
        "validate hmrcRef with ValidationErrors for invalid data",
        //Column N
        "validate totalAmountPaidToAcquireShares without ValidationErrors for valid data",
        "validate totalAmountPaidToAcquireShares with ValidationErrors for a number with more than 4 decimal points",
        "validate totalAmountPaidToAcquireShares with ValidationErrors for an alphanumeric string",
        "validate totalAmountPaidToAcquireShares with ValidationErrors for a number too large",
        //Column O
        "validate sDisposedOnSameDay without ValidationErrors for valid data",
        "validate sDisposedOnSameDay with ValidationErrors for invalid data",
        "validate sDisposedOnSameDay with ValidationErrors for an empty field"
      )
    descriptions
  }

  def getTestData: List[Cell] = {
    val testData = List(
      Cell("A", rowNumber, "2015-03-03"),
      Cell("A", rowNumber, "20150303"),
      Cell("A", rowNumber, ""),
      Cell("B", rowNumber, "Billy"),
      Cell("B", rowNumber, StringUtils.leftPad("", 45, "A")),
      Cell("B", rowNumber, ""),
      Cell("C", rowNumber, "Bob"),
      Cell("C", rowNumber, StringUtils.leftPad("", 45, "A")),
      Cell("D", rowNumber, "Thornton"),
      Cell("D", rowNumber, StringUtils.leftPad("", 45, "A")),
      Cell("D", rowNumber, ""),
      Cell("E", rowNumber, "AB123456C"),
      Cell("E", rowNumber, "abc"),
      Cell("F", rowNumber, "123/XZ55555555"),
      Cell("F", rowNumber, "abc???"),
      Cell("G", rowNumber, "10.12"),
      Cell("G", rowNumber, "10.1212"),
      Cell("G", rowNumber, "abc123"),
      Cell("G", rowNumber, StringUtils.leftPad("", 15, "1") + ".34"),
      Cell("H", rowNumber, "10.1234"),
      Cell("H", rowNumber, "10.12342323"),
      Cell("H", rowNumber, "102asbc"),
      Cell("H", rowNumber, StringUtils.leftPad("", 15, "1") + ".3434"),
      Cell("I", rowNumber, "10.1234"),
      Cell("I", rowNumber, "10.12342323"),
      Cell("I", rowNumber, "102asbc"),
      Cell("I", rowNumber, StringUtils.leftPad("", 15, "1") + ".3434"),
      Cell("J", rowNumber, "10.1234"),
      Cell("J", rowNumber, "10.12342323"),
      Cell("J", rowNumber, "102asbc"),
      Cell("J", rowNumber, StringUtils.leftPad("", 15, "1") + ".3434"),
      Cell("K", rowNumber, "yes"),
      Cell("K", rowNumber, "abv"),
      Cell("K", rowNumber, ""),
      Cell("L", rowNumber, "yes"),
      Cell("L", rowNumber, "abv"),
      Cell("M", rowNumber, "aa12345678"),
      Cell("M", rowNumber, "abc12345678901"),
      Cell("N", rowNumber, "10.1234"),
      Cell("N", rowNumber, "10.12342323"),
      Cell("N", rowNumber, "102asbc"),
      Cell("N", rowNumber, StringUtils.leftPad("", 15, "1") + ".3434"),
      Cell("O", rowNumber, "yes"),
      Cell("O", rowNumber, "abc"),
      Cell("O", rowNumber, "")
    )
    testData
  }

  def getExpectedResults: List[Option[List[ValidationErrorData]]] = {
    val expectedResults = List(
      None,
      Some(List(ValidationErrorData("error.1","001","Enter a date that matches the yyyy-mm-dd pattern."))),
      Some(List(ValidationErrorData("error.1","001","Enter a date that matches the yyyy-mm-dd pattern."))),
      None,
      Some(List(ValidationErrorData("error.2","002","Enter a first name (must be less than 36 characters and can only have letters, numbers, hyphens or apostrophes)."))),
      Some(List(ValidationErrorData("error.2","002","Enter a first name (must be less than 36 characters and can only have letters, numbers, hyphens or apostrophes)."))),
      None,
      Some(List(ValidationErrorData("error.3","003","Must be less than 36 characters and can only have letters, numbers, hyphens or apostrophes."))),
      None,
      Some(List(ValidationErrorData("error.4","004","Enter a last name (must be less than 36 characters and can only have letters, numbers, hyphens or apostrophes)."))),
      Some(List(ValidationErrorData("error.4","004","Enter a last name (must be less than 36 characters and can only have letters, numbers, hyphens or apostrophes)."))),
      None,
      Some(List(ValidationErrorData("error.5","005","National Insurance number must be 2 letters followed by 6 number digits, with an optional final letter."))),
      None,
      Some(List(ValidationErrorData("error.6","006","PAYE reference must be a 3 digit number followed by a forward slash and up to 10 more characters."))),
      None,
      Some(List(ValidationErrorData("error.7","007","Must be a number with 2 digits after the decimal point (and no more than 11 digits in front of it)."))),
      Some(List(ValidationErrorData("error.7","007","Must be a number with 2 digits after the decimal point (and no more than 11 digits in front of it)."))),
      Some(List(ValidationErrorData("error.7","007","Must be a number with 2 digits after the decimal point (and no more than 11 digits in front of it)."))),
      None,
      Some(List(ValidationErrorData("error.8","008","Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
      Some(List(ValidationErrorData("error.8","008","Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
      Some(List(ValidationErrorData("error.8","008","Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
      None,
      Some(List(ValidationErrorData("error.9","009","Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
      Some(List(ValidationErrorData("error.9","009","Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
      Some(List(ValidationErrorData("error.9","009","Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
      None,
      Some(List(ValidationErrorData("error.10","010","Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
      Some(List(ValidationErrorData("error.10","010","Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
      Some(List(ValidationErrorData("error.10","010","Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
      None,
      Some(List(ValidationErrorData("error.11","011","Enter 'yes' or 'no'."))),
      Some(List(ValidationErrorData("error.11","011","Enter 'yes' or 'no'."))),
      None,
      Some(List(ValidationErrorData("error.12","012","Enter 'yes' or 'no'."))),
      None,
      Some(List(ValidationErrorData("error.13","013","Enter the HMRC reference (must be less than 11 characters)."))),
      None,
      Some(List(ValidationErrorData("error.14","014","Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
      Some(List(ValidationErrorData("error.14","014","Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
      Some(List(ValidationErrorData("error.14","014","Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
      None,
      Some(List(ValidationErrorData("error.15","015","Enter 'yes' or 'no'."))),
      Some(List(ValidationErrorData("error.15","015","Enter 'yes' or 'no'."))),
    )
    expectedResults
  }

  def getValidRowData:Seq[Cell] = {
    val rowData = Seq(
      Cell("A", rowNumber, "2015-03-03"),
      Cell("B", rowNumber, "Billy"),
      Cell("C", rowNumber, "Bob"),
      Cell("D", rowNumber, "Thornton"),
      Cell("E", rowNumber, "AB123456C"),
      Cell("F", rowNumber, "123/XZ55555555"),
      Cell("G", rowNumber, "10.12"),
      Cell("H", rowNumber, "10.1234"),
      Cell("I", rowNumber, "10.1234"),
      Cell("J", rowNumber, "10.1234"),
      Cell("K", rowNumber, "yes"),
      Cell("L", rowNumber, "yes"),
      Cell("M", rowNumber, "aa12345678"),
      Cell("N", rowNumber, "10.1234"),
      Cell("O", rowNumber, "yes")
    )
    rowData
  }

  def getInvalidRowData:Seq[Cell] = {
    val rowData = Seq(
      Cell("A", rowNumber, "20150303"),
      Cell("B", rowNumber, StringUtils.leftPad("", 45, "A")),
      Cell("C", rowNumber, StringUtils.leftPad("", 45, "A")),
      Cell("D", rowNumber, StringUtils.leftPad("", 45, "A")),
      Cell("E", rowNumber, "abc"),
      Cell("F", rowNumber, "abc???"),
      Cell("G", rowNumber, "10.1212"),
      Cell("H", rowNumber, "10.12342323"),
      Cell("I", rowNumber, "10.12342323"),
      Cell("J", rowNumber, "10.12342323"),
      Cell("K", rowNumber, "abv"),
      Cell("L", rowNumber, "abv"),
      Cell("M", rowNumber, "abc12345678901"),
      Cell("N", rowNumber, "10.12342323"),
      Cell("O", rowNumber, "abc")
    )
    rowData
  }

}
