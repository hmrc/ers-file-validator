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

package services.validation.OTHERTestData

import uk.gov.hmrc.services.validation.Cell
import models.ValidationErrorData
import org.apache.commons.lang3.StringUtils

trait ERSValidationOTHERRestrictedSecuritiesTestData {

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
        //D,
        "validate individualPAR\\firstName without ValidationErrors for valid data",
        "validate individualPAR\\firstName with ValidationErrors for a string too long",
        "validate individualPAR\\firstName with ValidationErrors for an empty field",
        //E
        "validate individualPAR\\secondName without ValidationErrors for valid data",
        "validate individualPAR\\secondName without ValidationErrors for a string too long",
        //F
        "validate individualPAR\\surname without ValidationErrors for valid data",
        "validate individualPAR\\surname with ValidationErrors for a string too long",
        "validate individualPAR\\surname with ValidationErrors for an empty field",
        //G
        "validate individualPAR\\nino without ValidationErrors for valid data",
        "validate individualPAR\\nino with ValidationErrors for invalid data",
        //H
        "validate individualPAR\\payeReference without ValidationErrors for valid data",
        "validate individualPAR\\payeReference with ValidationErrors for invalid data",
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
        "validate totalChargeableAmount without ValidationErrors for valid data",
        "validate totalChargeableAmount with ValidationErrors for a number with over 4 decimal places",
        "validate totalChargeableAmount with ValidationErrors for an alphanumeric value",
        "validate totalChargeableAmount with ValidationErrors for a number too large",
        //L
        "validate sharesListedOnSE without ValidationErrors for valid data",
        "validate sharesListedOnSE with ValidationErrors for invalid data",
        //M
        "validate marketValueAgreedHMRC without ValidationErrors for valid data",
        "validate marketValueAgreedHMRC with ValidationErrors for invalid data",
        //N
        "validate hmrcRef without ValidationErrors for valid data",
        "validate hmrcRef with ValidationErrors for invalid data",
        //O
        "validate dateOfVariation without ValidationErrors for valid data",
        "validate dateOfVariation with ValidationErrors for invalid data",
        //P
        "validate marketValuePerSecurityDirectlyBeforeVariation without ValidationErrors for valid data",
        "validate marketValuePerSecurityDirectlyBeforeVariation with ValidationErrors for a number with over 4 decimal places",
        "validate marketValuePerSecurityDirectlyBeforeVariation with ValidationErrors for an alphanumeric value",
        "validate marketValuePerSecurityDirectlyBeforeVariation with ValidationErrors for a number too large",
        //Q
        "validate marketValuePerSecuritiesDirectlyAfterVariation without ValidationErrors for valid data",
        "validate marketValuePerSecuritiesDirectlyAfterVariation with ValidationErrors for a number with over 4 decimal places",
        "validate marketValuePerSecuritiesDirectlyAfterVariation with ValidationErrors for an alphanumeric value",
        "validate marketValuePerSecuritiesDirectlyAfterVariation with ValidationErrors for a number too large",
        //R
        "validate nicsElectionAgreementEnteredInto without ValidationErrors for valid data",
        "validate nicsElectionAgreementEnteredInto with ValidationErrors for invalid data",
        "validate nicsElectionAgreementEnteredInto with ValidationErrors for an empty field",
        //S
        "validate payeOperatedApplied without ValidationErrors for valid data",
        "validate payeOperatedApplied with ValidationErrors for invalid data",
        "validate payeOperatedApplied with ValidationErrors for an empty field",
        //T
        "validate adjusmentMadeForUKDuties without ValidationErrors for valid data",
        "validate adjusmentMadeForUKDuties with ValidationErrors for invalid data",
        "validate adjusmentMadeForUKDuties with ValidationErrors for an empty field"
      )
    descriptions
  }

  def getTestData: List[Cell] = {
    val testData = List(
      Cell("A",rowNumber,"2015-08-19"),
      Cell("A",rowNumber,"20150819"),
      Cell("A",rowNumber,""),
      Cell("B",rowNumber,"yes"),
      Cell("B",rowNumber,"y"),
      Cell("B",rowNumber,""),
      Cell("C",rowNumber,"12345678"),
      Cell("C",rowNumber,"1235a678"),
      Cell("D",rowNumber,"Billy"),
      Cell("D",rowNumber,StringUtils.leftPad("",45, "A")),
      Cell("D",rowNumber,""),
      Cell("E",rowNumber,"Bob"),
      Cell("E",rowNumber,StringUtils.leftPad("",45, "A")),
      Cell("F",rowNumber,"Thornton"),
      Cell("F",rowNumber,StringUtils.leftPad("",45, "A")),
      Cell("F",rowNumber,""),
      Cell("G",rowNumber,"AB123456A"),
      Cell("G",rowNumber,"12123456A"),
      Cell("H",rowNumber,"123/XZ55555555"),
      Cell("H",rowNumber,"123/??"),
      Cell("I",rowNumber,"2018-09-12"),
      Cell("I",rowNumber,"20150918"),
      Cell("I",rowNumber,""),
      Cell("J",rowNumber,"100.00"),
      Cell("J",rowNumber,"100.00121"),
      Cell("J",rowNumber,"abc"),
      Cell("J", rowNumber, StringUtils.leftPad("", 15, "1") + ".34"),
      Cell("K", rowNumber, "10.1234"),
      Cell("K", rowNumber, "10.1234567"),
      Cell("K", rowNumber, "12nas"),
      Cell("K", rowNumber, StringUtils.leftPad("", 15, "1") + ".3234"),
      Cell("L",rowNumber,"yes"),
      Cell("L",rowNumber,"n"),
      Cell("M",rowNumber,"no"),
      Cell("M",rowNumber,"y"),
      Cell("N", rowNumber, "aa12345678"),
      Cell("N", rowNumber, "abc12345678901"),
      Cell("O", rowNumber, "2018-09-12"),
      Cell("O", rowNumber, "2018 09 12"),
      Cell("P", rowNumber, "10.1234"),
      Cell("P", rowNumber, "10.1234567"),
      Cell("P", rowNumber, "12nas"),
      Cell("P", rowNumber, StringUtils.leftPad("", 15, "1") + ".3234"),
      Cell("Q", rowNumber, "10.1234"),
      Cell("Q", rowNumber, "10.1234567"),
      Cell("Q", rowNumber, "12nas"),
      Cell("Q", rowNumber, StringUtils.leftPad("", 15, "1") + ".3234"),
      Cell("R",rowNumber,"no"),
      Cell("R",rowNumber,"o"),
      Cell("R",rowNumber,""),
      Cell("S",rowNumber,"no"),
      Cell("S",rowNumber,"o"),
      Cell("S",rowNumber,""),
      Cell("T",rowNumber,"no"),
      Cell("T",rowNumber,"o"),
      Cell("T",rowNumber,"")
    )
    testData
  }

  def getExpectedResults: List[Option[List[ValidationErrorData]]] = {
    val expectedResults = List(
      //A
      None,
      Some(List(ValidationErrorData("error.1","001","Enter a date that matches the yyyy-mm-dd pattern."))),
      Some(List(ValidationErrorData("MANDATORY","100","Enter a date that matches the yyyy-mm-dd pattern."))),
      //B
      None,
      Some(List(ValidationErrorData("error.2","002","Enter 'yes' or 'no'."))),
      Some(List(ValidationErrorData("MANDATORY","100","Enter 'yes' or 'no'."))),
      //C
      None,
      Some(List(ValidationErrorData("error.3","003","Enter the scheme reference number (it should be an 8 digit number)."))),
      //D
      None,
      Some(List(ValidationErrorData("error.4","004","Enter a first name (must be less than 36 characters and can only have letters, numbers, hyphens or apostrophes)."))),
      Some(List(ValidationErrorData("MANDATORY","100","Enter a first name (must be less than 36 characters and can only have letters, numbers, hyphens or apostrophes)."))),
      //E
      None,
      Some(List(ValidationErrorData("error.5","005","Must be less than 36 characters and can only have letters, numbers, hyphens or apostrophes."))),
      //F
      None,
      Some(List(ValidationErrorData("error.6","006","Enter a last name (must be less than 36 characters and can only have letters, numbers, hyphens or apostrophes)."))),
      Some(List(ValidationErrorData("MANDATORY","100","Enter a last name (must be less than 36 characters and can only have letters, numbers, hyphens or apostrophes)."))),
      //G
      None,
      Some(List(ValidationErrorData("error.7","007","National Insurance number must be 2 letters followed by 6 number digits, with an optional final letter."))),
      //H
      None,
      Some(List(ValidationErrorData("error.8","008","PAYE reference must be a 3 digit number followed by a forward slash and up to 10 more characters."))),
      //I
      None,
      Some(List(ValidationErrorData("error.9","009","Enter a date that matches the yyyy-mm-dd pattern."))),
      Some(List(ValidationErrorData("MANDATORY","100","Enter a date that matches the yyyy-mm-dd pattern."))),
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
      Some(List(ValidationErrorData("error.12","012","Enter 'yes' or 'no'."))),
      //M
      None,
      Some(List(ValidationErrorData("error.13","013","Enter 'yes' or 'no'."))),
      //N
      None,
      Some(List(ValidationErrorData("error.14","014","Enter the HMRC reference (must be less than 11 characters)."))),
      //O
      None,
      Some(List(ValidationErrorData("error.15","015","Enter a date that matches the yyyy-mm-dd pattern."))),
      //P
      None,
      Some(List(ValidationErrorData("error.16","016","Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
      Some(List(ValidationErrorData("error.16","016","Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
      Some(List(ValidationErrorData("error.16","016","Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
      //Q
      None,
      Some(List(ValidationErrorData("error.17","017","Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
      Some(List(ValidationErrorData("error.17","017","Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
      Some(List(ValidationErrorData("error.17","017","Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
      //R
      None,
      Some(List(ValidationErrorData("error.18","018","Enter 'yes' or 'no'."))),
      Some(List(ValidationErrorData("MANDATORY","100","Enter 'yes' or 'no'."))),
      //S
      None,
      Some(List(ValidationErrorData("error.19","019","Enter 'yes' or 'no'."))),
      Some(List(ValidationErrorData("MANDATORY","100","Enter 'yes' or 'no'."))),
      //T
      None,
      Some(List(ValidationErrorData("error.20","020","Enter 'yes' or 'no'."))),
      Some(List(ValidationErrorData("MANDATORY","100","Enter 'yes' or 'no'.")))
    )
    expectedResults
  }

  def getValidRowData:Seq[Cell] = {
    val rowData = Seq(
      Cell("A",rowNumber,"2015-08-19"),
      Cell("B",rowNumber,"yes"),
      Cell("C",rowNumber,"12345678"),
      Cell("D",rowNumber,"Billy"),
      Cell("E",rowNumber,"Bob"),
      Cell("F",rowNumber,"Thornton"),
      Cell("G",rowNumber,"AB123456A"),
      Cell("H",rowNumber,"123/XZ55555555"),
      Cell("I",rowNumber,"2018-09-12"),
      Cell("J",rowNumber,"100.00"),
      Cell("K", rowNumber, "10.1234"),
      Cell("L",rowNumber,"yes"),
      Cell("M",rowNumber,"no"),
      Cell("N", rowNumber, "aa12345678"),
      Cell("O", rowNumber, "2018-09-12"),
      Cell("P", rowNumber, "10.1234"),
      Cell("Q", rowNumber, "10.1234"),
      Cell("R",rowNumber,"no"),
      Cell("S",rowNumber,"no"),
      Cell("T",rowNumber,"no")
    )
    rowData
  }

  def getInvalidRowData:Seq[Cell] = {
    val rowData = Seq(
      Cell("A",rowNumber,"20150819"),
      Cell("B",rowNumber,"y"),
      Cell("C",rowNumber,"1235a678"),
      Cell("D",rowNumber,StringUtils.leftPad("",45, "A")),
      Cell("E",rowNumber,StringUtils.leftPad("",45, "A")),
      Cell("F",rowNumber,StringUtils.leftPad("",45, "A")),
      Cell("G",rowNumber,"12123456A"),
      Cell("H",rowNumber,"123XZ55555555??"),
      Cell("I",rowNumber,"20150918"),
      Cell("J", rowNumber, StringUtils.leftPad("", 15, "1") + ".34"),
      Cell("K", rowNumber, StringUtils.leftPad("", 15, "1") + ".3234"),
      Cell("L",rowNumber,"n"),
      Cell("M",rowNumber,"y"),
      Cell("N", rowNumber, "abc12345678901"),
      Cell("O", rowNumber, "2018 09 12"),
      Cell("P", rowNumber, StringUtils.leftPad("", 15, "1") + ".3234"),
      Cell("Q", rowNumber, StringUtils.leftPad("", 15, "1") + ".3234"),
      Cell("R",rowNumber,"o"),
      Cell("S",rowNumber,"o"),
      Cell("T",rowNumber,"")
    )
    rowData
  }

}
