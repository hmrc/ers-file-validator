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
import org.apache.commons.lang3.StringUtils

trait ERSValidationSAYEExercisedTestData {
  val rowNumber : Int = 1
  def getDescriptions: List[String] = List(
    //A
     "not raise ValidationError if dateOfEvent contian yyyy-mm-dd date format",
     "raise ValidatoinError when dataOfEvent is not matching the date expected format",
     "raise ValidatoinError when cell is empty",
    //B
    "not raise ValidationError if individual\\firstName is a valid name format",
    "raise ValidationError if individual\\firstName entry is more than 35 characters",
    "raise a ValidationError if the cell is individual\\firstName is empty",
    //C
    "not raise ValidationError if individual\\secondName is a valid name format",
    "raise ValidationError if individual\\secondName entry is more than 35 characters",
    //D
    "not raise ValidationError if individual\\lastName is a valid format",
    "raise ValidationError if individual\\lastName entry is more than 35 characters",
    "raise a ValidationError if the cell individual\\lastName is empty",
    //E
    "not raise ValidationError if the national insurance number is valid",
    "raise a ValidationError if National Insurance Number is not conforming to the expected format",
    //F
    "not raise ValidationError individual\\payeReference is valid format",
    "raise a ValidationError if individual\\payeReference is not matching the expected format",
    //G
    "not raise a ValidationError if dataOfGrant is a valid format",
    "raise ValidationError when dataOfGrant in not matching the date expected format",
    "raise ValidatoinError when dateOfGrant cell is empty",
    //H
    "not raise ValidationError if numberOfSharesAcquired is valid",
    "raise a ValidationError if numberOfSharesAcquired has more than 2 decimal places",
    "raise a ValidationError if numberOfSharesAcquired has none alphaNumerics",
    "raise a ValidationError if numberOfSharesAcquired is more that 11.2 digits",
    //I
    "return None when (yes/no) is received for sharedListedOnSE",
    "return an error when an invalid value for sharesListedOnSE is given",
    "return an error when sharesListedOnSE is blank",
    //J
    "return None when (yes/no) is answered for marketValueAgreedHMRC",
    "raise ValidationError if marketValueAgreedHMRC answer is not valid",
    //K
    "return None when hmrcRef is a valid entry",
    "return ValidationErrors when hrmcRef is invalid",
    //L
    "return None when actualMarketValuePerShareAtAcquisitionDate is answered as expected",
    "Return ValidationErrors when actualMarketValuePerShareAtAcquisitionDate contains other than 4 decimal places",
    "Return ValidationErrors when actualMarketValuePerShareAtAcquisitionDate contains non-numeric characters",
    "Return ValidationErrors when actualMarketValuePerShareAtAcquisitionDate contains a number too large",
    //M
    "Validate valid exerciseValuePerShare data",
    "Return ValidationErrors when exerciseValuePerShare contains other than 4 decimal places",
    "Return ValidationErrors when exerciseValuePerShare contains non-numeric characters",
    "Return ValidationErrors when exerciseValuePerShare contains a number too large",
    //N
    "Validate valid unrestrictedMarketValuePerShareAtExerciseDate data",
    "Return ValidationErrors when unrestrictedMarketValuePerShareAtExerciseDate contains other than 4 decimal places",
    "Return ValidationErrors when unrestrictedMarketValuePerShareAtExerciseDate contains non-numeric characters",
    "Return ValidationErrors when unrestrictedMarketValuePerShareAtExerciseDate contains a number too large",
    //O
    "Validate qualifyForTaxRelief without ValidationErrors for valid data",
    "Return ValidationErrors when qualifyForTaxRelief is given invalid data",
    "Return ValidationErrors when qualifyForTaxRelief is given no data",
    //P
    "Validate valid sharesSoldInConnectionWithTheExercise data",
    "Return ValidationErrors when sharesSoldInConnectionWithTheExercise is not valid",
    "Return ValidationErrors when sharesSoldInConnectionWithTheExercise is empty"
  )

  def getTestData : List[Cell] = List(
    //A
    Cell("A", rowNumber, "2016-04-17"),
    Cell("A", rowNumber, "garbage-date"),
    Cell("A", rowNumber, ""),
    //B
    Cell("B", rowNumber, "Sam"),
    Cell("B", rowNumber, "SamSamSamSamSamSamSamSamSSamSamSamSamSAmSamSSamSa"),
    Cell("B", rowNumber, ""),
    //C
    Cell("C", rowNumber, "Smith"),
    Cell("C", rowNumber, "Smith Smithy blah blah blah blah blah bladdy blah"),
    //D
    Cell("D", rowNumber, "Smooth"),
    Cell("D", rowNumber, "Smooth Smoothy blah blah blah blah bladdy blah"),
    Cell("D", rowNumber, ""),
    //E
    Cell("E", rowNumber, "AA123456A"),
    Cell("E", rowNumber, "AAAA12341.135a"),
    //F
    Cell("F", rowNumber, "123/XZ55555555"),
    Cell("F", rowNumber, "1234/12345/12341234"),
    //G
    Cell("G", rowNumber, "2016-04-18"),
    Cell("G", rowNumber, "garbage-date"),
    Cell("G", rowNumber, ""),
    //H
    Cell("H", rowNumber, "11.22"),
    Cell("H", rowNumber, "11.2345"),
    Cell("H", rowNumber, "1d2.12"),
    Cell("H", rowNumber, "123235346435623.32"),
    //I
    Cell("I", rowNumber, "Yes"),
    Cell("I", rowNumber, "blah"),
    Cell("I", rowNumber, ""),
    //J
    Cell("J", rowNumber, "No"),
    Cell("J", rowNumber, "n"),
    //K
    Cell("K", rowNumber, "aa12345678"),
    Cell("K", rowNumber, "ab123ab45678901"),
    //L
    Cell("L", rowNumber, "10.1234"),
    Cell("L", rowNumber, "10.12"),
    Cell("L", rowNumber, "abc"),
    Cell("L", rowNumber, StringUtils.leftPad("", 15, "2") + ".3434"),
    //M
    Cell("M", rowNumber, "10.4444"),
    Cell("M", rowNumber, "10.312"),
    Cell("M", rowNumber, "#!"),
    Cell("M", rowNumber, StringUtils.leftPad("", 15, "3") + ".3434"),
    //N
    Cell("N", rowNumber, "10.3412"),
    Cell("N", rowNumber, "10.12122"),
    Cell("N", rowNumber, "abc"),
    Cell("N", rowNumber, StringUtils.leftPad("", 15, "4") + ".3434"),
    //O
    Cell("O", rowNumber, "yes"),
    Cell("O", rowNumber, "ys"),
    Cell("O", rowNumber, ""),
    //P
    Cell("P", rowNumber, "yes"),
    Cell("P", rowNumber, "garbage"),
    Cell("P", rowNumber, "")
  )

  def getExpectedResults: List[Option[List[ValidationErrorData]]] = List(
    //A
    None,
    Some(List(ValidationErrorData("error.1", "001", "Enter a date that matches the yyyy-mm-dd pattern."))),
    Some(List(ValidationErrorData("error.1", "001", "Enter a date that matches the yyyy-mm-dd pattern."))),
    //B
    None,
    Some(List(ValidationErrorData("error.2", "002", "Enter a first name (must be less than 36 characters and can only have letters, numbers, hyphens or apostrophes)."))),
    Some(List(ValidationErrorData("error.2", "002", "Enter a first name (must be less than 36 characters and can only have letters, numbers, hyphens or apostrophes)."))),
    //C
    None,
    Some(List(ValidationErrorData("error.3", "003", "Must be less than 36 characters and can only have letters, numbers, hyphens or apostrophes."))),
    //D
    None,
    Some(List(ValidationErrorData("error.4", "004", "Enter a last name (must be less than 36 characters and can only have letters, numbers, hyphens or apostrophes)."))),
    Some(List(ValidationErrorData("error.4", "004", "Enter a last name (must be less than 36 characters and can only have letters, numbers, hyphens or apostrophes)."))),
    //E
    None,
    Some(List(ValidationErrorData("error.5", "005", "National Insurance number must be 2 letters followed by 6 number digits, with an optional final letter."))),
    //F
    None,
    Some(List(ValidationErrorData("error.6", "006", "PAYE reference must be a 3 digit number followed by a forward slash and up to 10 more characters."))),
    //G
    None,
    Some(List(ValidationErrorData("error.7", "007", "Enter a date that matches the yyyy-mm-dd pattern."))),
    Some(List(ValidationErrorData("error.7", "007", "Enter a date that matches the yyyy-mm-dd pattern."))),
    //H
    None,
    Some(List(ValidationErrorData("error.8", "008", "Must be a number with 2 digits after the decimal point (and no more than 11 digits in front of it)."))),
    Some(List(ValidationErrorData("error.8", "008", "Must be a number with 2 digits after the decimal point (and no more than 11 digits in front of it)."))),
    Some(List(ValidationErrorData("error.8", "008", "Must be a number with 2 digits after the decimal point (and no more than 11 digits in front of it)."))),
    //I
    None,
    Some(List(ValidationErrorData("error.9", "009", "Enter 'yes' or 'no'."))),
    Some(List(ValidationErrorData("error.9", "009", "Enter 'yes' or 'no'."))),
    //J
    None,
    Some(List(ValidationErrorData("error.10", "010", "Enter 'yes' or 'no'."))),
    //K
    None,
    Some(List(ValidationErrorData("error.11", "011", "Enter the HMRC reference (must be less than 11 characters)."))),
    //L
    None,
    Some(List(ValidationErrorData("error.12", "012","Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
    Some(List(ValidationErrorData("error.12", "012","Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
    Some(List(ValidationErrorData("error.12", "012","Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
    //M
    None,
    Some(List(ValidationErrorData("error.13", "013","Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
    Some(List(ValidationErrorData("error.13", "013","Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
    Some(List(ValidationErrorData("error.13", "013","Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
    //N
    None,
    Some(List(ValidationErrorData("error.14", "014","Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
    Some(List(ValidationErrorData("error.14", "014","Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
    Some(List(ValidationErrorData("error.14", "014","Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
    //O
    None,
    Some(List(ValidationErrorData("error.15", "015", "Enter 'yes' or 'no'."))),
    Some(List(ValidationErrorData("error.15", "015", "Enter 'yes' or 'no'."))),
    //P
    None,
    Some(List(ValidationErrorData("error.16", "016", "Enter 'yes' or 'no'."))),
    Some(List(ValidationErrorData("error.16", "016", "Enter 'yes' or 'no'."))),
  )
}
