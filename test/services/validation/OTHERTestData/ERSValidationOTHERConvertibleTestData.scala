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

trait ERSValidationOTHERConvertibleTestData {

  val rowNumber:Int = 1

  def getDescriptions: List[String] = {
    val descriptions =
      List(
        //A
        "validate dateOfEvent without ValidationErrors for valid data",
        "validate dateOfEvent with ValidationErrors for invalid data",
        "validate dateOfEvent with ValidationErrors for an empty field",
        //B
        "validate inRelationToASchemeWithADOTASRef without ValidationErrors for valid data",
        "validate inRelationToASchemeWithADOTASRef with ValidationErrors for invalid data",
        "validate inRelationToASchemeWithADOTASRef with ValidationErrors for an empty field",
        //C
        "validate dotasRef without ValidationErrors for valid data",
        "validate dotasRef with ValidationErrors for invalid data",
        //D
        "validate individualPAC\\firstName without ValidationErrors for valid data",
        "validate individualPAC\\firstName with ValidationErrors for a string too long",
        "validate individualPAC\\firstName with ValidationErrors for an empty field",
        //E
        "validate individualPAC\\secondName without ValidationErrors for valid data",
        "validate individualPAC\\secondName with ValidationErrors for a string too long",
        //F
        "validate individualPAC\\surname without ValidationErrors for valid data",
        "validate individualPAC\\surname with ValidationErrors for a string too long",
        "validate individualPAC\\surname with ValidationErrors for an empty field",
        //G
        "validate individualPAC\\nino without ValidationErrors for valid data",
        "validate individualPAC\\nino with ValidationErrors for invalid data",
        //H
        "validate individualPAC\\payeReference without ValidationErrors for valid data",
        "validate individualPAC\\payeReference with ValidationErrors for invalid data",
        //I
        "validate dateSecuritiesOriginallyAcquired without ValidationErrors for valid data",
        "validate dateSecuritiesOriginallyAcquired with ValidationErrors for invalid data",
        "validate dateSecuritiesOriginallyAcquired with ValidationErrors for an empty field",
        //J
        "validate numberOfSecuritiesOriginallyAcquired without ValidationErrors for valid data",
        "validate numberOfSecuritiesOriginallyAcquired with ValidationErrors for a number with more than 2 decimal places",
        "validate numberOfSecuritiesOriginallyAcquired with ValidationErrors for an alphanumeric string",
        "validate numberOfSecuritiesOriginallyAcquired with ValidationErrors for a number larger than that allowed",
        //K
        "validate amountOrMarketValueOfTheBenefit without ValidationErrors for valid data",
        "validate amountOrMarketValueOfTheBenefit with ValidationErrors for a number with over 4 decimal places",
        "validate amountOrMarketValueOfTheBenefit with ValidationErrors for an alphanumeric value",
        "validate amountOrMarketValueOfTheBenefit with ValidationErrors for a number too large",
        //L
        "validate totalChargeableAmount without ValidationErrors for valid data",
        "validate totalChargeableAmount with ValidationErrors for a number with over 4 decimal places",
        "validate totalChargeableAmount with ValidationErrors for an alphanumeric value",
        "validate totalChargeableAmount with ValidationErrors for a number too large",
        //M
        "validate nicsElectionAgreementEnteredInto without ValidationErrors for valid data",
        "validate nicsElectionAgreementEnteredInto with ValidationErrors for invalid data",
        "validate nicsElectionAgreementEnteredInto with ValidationErrors for an empty field",
        //N
        "validate payeOperatedApplied without ValidationErrors for valid data",
        "validate payeOperatedApplied with ValidationErrors for invalid data",
        "validate payeOperatedApplied with ValidationErrors for an empty field",
        //O
        "validate adjusmentMadeForUKDuties without ValidationErrors for valid data",
        "validate adjusmentMadeForUKDuties with ValidationErrors for invalid data",
        "validate adjusmentMadeForUKDuties with ValidationErrors for an empty field"
      )
    descriptions
  }

  def getTestData: List[Cell] = {
    val testData = List(
      Cell("A",rowNumber,"2012-02-22"),
      Cell("A",rowNumber,"20120222"),
      Cell("A",rowNumber,""),
      Cell("B",rowNumber,"yes"),
      Cell("B",rowNumber,"ys"),
      Cell("B",rowNumber,""),
      Cell("C",rowNumber,"12345678"),
      Cell("C",rowNumber,"12345678901"),
      Cell("D",rowNumber,"Billy"),
      Cell("D",rowNumber,StringUtils.leftPad("",45, "A")),
      Cell("D",rowNumber,""),
      Cell("E",rowNumber,"Bob"),
      Cell("E",rowNumber,StringUtils.leftPad("",45, "A")),
      Cell("F",rowNumber,"Thornton"),
      Cell("F",rowNumber,StringUtils.leftPad("",45, "A")),
      Cell("F",rowNumber,""),
      Cell("G",rowNumber,"AB123456A"),
      Cell("G",rowNumber,"abc"),
      Cell("H",rowNumber,"123/XZ55555555"),
      Cell("H",rowNumber,"abcXZ55555555////"),
      Cell("I",rowNumber,"2012-02-22"),
      Cell("I",rowNumber,"20120222"),
      Cell("I",rowNumber,""),
      Cell("J",rowNumber,"100.00"),
      Cell("J",rowNumber,"100.00121"),
      Cell("J",rowNumber,"abc"),
      Cell("J", rowNumber, StringUtils.leftPad("", 15, "1") + ".34"),
      Cell("K", rowNumber, "10.1234"),
      Cell("K", rowNumber, "10.1234567"),
      Cell("K", rowNumber, "12nas"),
      Cell("K", rowNumber, StringUtils.leftPad("", 15, "1") + ".3234"),
      Cell("L", rowNumber, "10.1234"),
      Cell("L", rowNumber, "10.1234567"),
      Cell("L", rowNumber, "12nas"),
      Cell("L", rowNumber, StringUtils.leftPad("", 15, "1") + ".3234"),
      Cell("M",rowNumber,"yes"),
      Cell("M",rowNumber,"ys"),
      Cell("M",rowNumber,""),
      Cell("N",rowNumber,"yes"),
      Cell("N",rowNumber,"ys"),
      Cell("N",rowNumber,""),
      Cell("O",rowNumber,"yes"),
      Cell("O",rowNumber,"ys"),
      Cell("O",rowNumber,"")
    )
    testData
  }

  def getExpectedResults: List[Option[List[ValidationErrorData]]] = {
    val expectedResults = List(
      //A
      None,
      Some(List(ValidationErrorData("error.1","001","Enter a date that matches the yyyy-mm-dd pattern."))),
      Some(List(ValidationErrorData("error.1","001","Enter a date that matches the yyyy-mm-dd pattern."))),
      //B
      None,
      Some(List(ValidationErrorData("error.2","002","Enter 'yes' or 'no'."))),
      Some(List(ValidationErrorData("error.2","002","Enter 'yes' or 'no'."))),
      //C
      None,
      Some(List(ValidationErrorData("error.3","003","Enter the scheme reference number (it should be an 8 digit number)."))),
      //D
      None,
      Some(List(ValidationErrorData("error.4","004","Enter a first name (must be less than 36 characters and can only have letters, numbers, hyphens or apostrophes)."))),
      Some(List(ValidationErrorData("error.4","004","Enter a first name (must be less than 36 characters and can only have letters, numbers, hyphens or apostrophes)."))),
      //E
      None,
      Some(List(ValidationErrorData("error.5","005","Must be less than 36 characters and can only have letters, numbers, hyphens or apostrophes."))),
      //F
      None,
      Some(List(ValidationErrorData("error.6","006","Enter a last name (must be less than 36 characters and can only have letters, numbers, hyphens or apostrophes)."))),
      Some(List(ValidationErrorData("error.6","006","Enter a last name (must be less than 36 characters and can only have letters, numbers, hyphens or apostrophes)."))),
      //G
      None,
      Some(List(ValidationErrorData("error.7","007","National Insurance number must be 2 letters followed by 6 number digits, with an optional final letter."))),
      //H
      None,
      Some(List(ValidationErrorData("error.8","008","PAYE reference must be a 3 digit number followed by a forward slash and up to 10 more characters."))),
      //I
      None,
      Some(List(ValidationErrorData("error.9","009","Enter a date that matches the yyyy-mm-dd pattern."))),
      Some(List(ValidationErrorData("error.9","009","Enter a date that matches the yyyy-mm-dd pattern."))),
      //J
      None,
      Some(List(ValidationErrorData("error.10","010","Must be a number with 2 digits after the decimal point (and no more than 11 digits in front of it)."))),
      Some(List(ValidationErrorData("error.10","010","Must be a number with 2 digits after the decimal point (and no more than 11 digits in front of it)."))),
      Some(List(ValidationErrorData("error.10","010","Must be a number with 2 digits after the decimal point (and no more than 11 digits in front of it)."))),
      //K
      None,
      Some(List(ValidationErrorData("error.11","011","Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
      Some(List(ValidationErrorData("error.11","011","Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
      Some(List(ValidationErrorData("error.11","011","Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
      //L
      None,
      Some(List(ValidationErrorData("error.12","012","Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
      Some(List(ValidationErrorData("error.12","012","Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
      Some(List(ValidationErrorData("error.12","012","Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
      //M
      None,
      Some(List(ValidationErrorData("error.13","013","Enter 'yes' or 'no'."))),
      Some(List(ValidationErrorData("error.13","013","Enter 'yes' or 'no'."))),
      //N
      None,
      Some(List(ValidationErrorData("error.14","014","Enter 'yes' or 'no'."))),
      Some(List(ValidationErrorData("error.14","014","Enter 'yes' or 'no'."))),
      //O
      None,
      Some(List(ValidationErrorData("error.15","015","Enter 'yes' or 'no'."))),
      Some(List(ValidationErrorData("error.15","015","Enter 'yes' or 'no'."))),
    )
    expectedResults
  }

  def getValidRowData:Seq[Cell] = {
    val rowData = Seq(
      Cell("A",rowNumber,"2012-02-22"),
      Cell("B",rowNumber,"yes"),
      Cell("C",rowNumber,"12345678"),
      Cell("D",rowNumber,"Billy"),
      Cell("E",rowNumber,"Bob"),
      Cell("F",rowNumber,"Thornton"),
      Cell("G",rowNumber,"AB123456A"),
      Cell("H",rowNumber,"123/XZ55555555"),
      Cell("I",rowNumber,"2012-02-22"),
      Cell("J",rowNumber,"100.00"),
      Cell("K",rowNumber,"10.1200"),
      Cell("L",rowNumber,"10.1200"),
      Cell("M",rowNumber,"yes"),
      Cell("N",rowNumber,"yes"),
      Cell("O",rowNumber,"yes")
    )
    rowData
  }

  def getInvalidRowData:Seq[Cell] = {
    val rowData = Seq(
      Cell("A",rowNumber,"20120222"),
      Cell("B",rowNumber,"ys"),
      Cell("C",rowNumber,"12345678901"),
      Cell("D",rowNumber,StringUtils.leftPad("",45, "A")),
      Cell("E",rowNumber,StringUtils.leftPad("",45, "A")),
      Cell("F",rowNumber,StringUtils.leftPad("",45, "A")),
      Cell("G",rowNumber,"abc"),
      Cell("H",rowNumber,"abcXZ55555555////"),
      Cell("I",rowNumber,"20120222"),
      Cell("J", rowNumber, StringUtils.leftPad("", 15, "1") + ".34"),
      Cell("K", rowNumber, StringUtils.leftPad("", 15, "1") + ".3234"),
      Cell("L", rowNumber, StringUtils.leftPad("", 15, "1") + ".3234"),
      Cell("M",rowNumber,"ys"),
      Cell("N",rowNumber,"ys"),
      Cell("O",rowNumber,"ys")
    )
    rowData
  }

}
