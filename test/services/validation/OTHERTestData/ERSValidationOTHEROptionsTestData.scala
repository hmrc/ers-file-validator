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

trait ERSValidationOTHEROptionsTestData {

  val rowNumber:Int = 1

  def getDescriptions: List[String] = {
    val descriptions =
      List(
        //Column A
        "validate dateOfEvent without ValidationErrors for valid data",
        "validate dateOfEvent with ValidationErrors for invalid data",
        "validate dateOfEvent with ValidationErrors for an empty field",
        //Column B
        "validate inRelationToASchemeWithADOTASRef without ValidationErrors for valid data",
        "validate inRelationToASchemeWithADOTASRef with ValidationErrors for invalid data",
        "validate inRelationToASchemeWithADOTASRef with ValidationErrors for an empty field",
        //Column C
        "validate dotasRef without ValidationErrors for valid data",
        "validate dotasRef with ValidationErrors for invalid data",
        //Column D
        "validate individualOptions\\firstName without ValidationErrors for valid data",
        "validate individualOptions\\firstName with ValidationErrors for a string too long",
        "validate individualOptions\\firstName with ValidationErrors for an empty field",
        //Column E
        "validate individualOptions\\secondName without ValidationErrors for valid data",
        "validate individualOptions\\secondName with ValidationErrors for a string too long",
        //Column F
        "validate individualOptions\\surname without ValidationErrors for valid data",
        "validate individualOptions\\surname with ValidationErrors for a string too long",
        "validate individualOptions\\surname with ValidationErrors for an empty field",
        //Column G
        "validate individualOptions\\nino without ValidationErrors for valid data",
        "validate individualOptions\\nino with ValidationErrors for invalid data",
        //Column H
        "validate individualOptions\\payeReference without ValidationErrors for valid data",
        "validate individualOptions\\payeReference with ValidationErrors for invalid data",
        //Column I
        "validate dateOfGrant without ValidationErrors for valid data",
        "validate dateOfGrant with ValidationErrors for invalid data",
        "validate dateOfGrant with ValidationErrors for an empty field",
        //Column J
        "validate grantorCompany without ValidationErrors for valid data",
        "validate grantorCompany with ValidationErrors for a string too long",
        "validate grantorCompany with ValidationErrors for an empty field",
        //Column K
        "validate grantorCompany\\companyAddress\\addressLine1 without ValidationErrors for valid data",
        "validate grantorCompany\\companyAddress\\addressLine1 with ValidationErrors for a string too long",
        "validate grantorCompany\\companyAddress\\addressLine1 with ValidationErrors for an empty field",
        //Column L
        "validate grantorCompany\\companyAddress\\addressLine2 without ValidationErrors for valid data",
        "validate grantorCompany\\companyAddress\\addressLine2 with ValidationErrors for a string too long",
        //Column M
        "validate grantorCompany\\companyAddress\\addressLine3 without ValidationErrors for valid data",
        "validate grantorCompany\\companyAddress\\addressLine3 with ValidationErrors for a string too long",
        //Column N
        "validate grantorCompany\\companyAddress\\addressLine4 without ValidationErrors for valid data",
        "validate grantorCompany\\companyAddress\\addressLine4 with ValidationErrors for a string too long",
        //Column O
        "validate grantorCompany\\companyAddress\\country without ValidationErrors for valid data",
        "validate grantorCompany\\companyAddress\\country with ValidationErrors for a string too long",
        //Column P
        "validate grantorCompany\\companyAddress\\postCode without ValidationErrors for valid data",
        "validate grantorCompany\\companyAddress\\postCode with ValidationErrors for a string too long",
        //Column Q
        "validate grantorCompany\\companyCRN without ValidationErrors for valid data",
        "validate grantorCompany\\companyCRN with ValidationErrors for invalid data",
        "validate grantorCompany\\companyCRN with ValidationErrors for an empty field",
        //Column R
        "validate grantorCompany\\companyCTRef without ValidationErrors for valid data",
        "validate grantorCompany\\companyCTRef with ValidationErrors for invalid data",
        //Column S
        "validate grantorCompany\\companyPAYERef without ValidationErrors for valid data",
        "validate grantorCompany\\companyPAYERef with ValidationErrors for invalid data",
        //Column T
        "validate secUOPCompany\\companyName without ValidationErrors for valid data",
        "validate secUOPCompany\\companyName with ValidationErrors for a string too long",
        "validate secUOPCompany\\companyName with ValidationErrors for an empty field",
        //Column U
        "validate secUOPCompany\\companyAddress\\addressLine1 without ValidationErrors for valid data",
        "validate secUOPCompany\\companyAddress\\addressLine1 with ValidationErrors for a string too long",
        //Column V
        "validate secUOPCompany\\companyAddress\\addressLine2 without ValidationErrors for valid data",
        "validate secUOPCompany\\companyAddress\\addressLine2 with ValidationErrors for a string too long",
        //Column W
        "validate secUOPCompany\\companyAddress\\addressLine3 without ValidationErrors for valid data",
        "validate secUOPCompany\\companyAddress\\addressLine3 with ValidationErrors for a string too long",
        //Column X
        "validate secUOPCompany\\companyAddress\\addressLine4 without ValidationErrors for valid data",
        "validate secUOPCompany\\companyAddress\\addressLine4 with ValidationErrors for a string too long",
        //Column Y
        "validate secUOPCompany\\companyAddress\\country without ValidationErrors for valid data",
        "validate secUOPCompany\\companyAddress\\country with ValidationErrors for a string too long",
        //Column Z
        "validate secUOPCompany\\companyAddress\\postCode without ValidationErrors for valid data",
        "validate secUOPCompany\\companyAddress\\postCode with ValidationErrors for a string too long",
        //Column AA
        "validate secUOPCompany\\companyCRN without ValidationErrors for valid data",
        "validate secUOPCompany\\companyCRN with ValidationErrors for invalid data",
        //Column AB
        "validate secUOPCompany\\companyCTRef without ValidationErrors for valid data",
        "validate secUOPCompany\\companyCTRef with ValidationErrors for invalid data",
        //Column AC
        "validate secUOPCompany\\companyPAYERef without ValidationErrors for valid data",
        "validate secUOPCompany\\companyPAYERef with ValidationErrors for invalid data",
        //Column AD
        "validate optionsExercised without ValidationErrors for valid data",
        "validate optionsExercised with ValidationErrors for invalid data",
        //Column AE
        "validate numberOfsecuritiesAcquired without ValidationErrors for valid data",
        "validate numberOfsecuritiesAcquired with ValidationErrors for a number with more than 2 decimal places",
        "validate numberOfsecuritiesAcquired with ValidationErrors for an alphanumeric string",
        "validate numberOfsecuritiesAcquired with ValidationErrors for a number larger than that allowed",
        //Column AF
        "validate exercisePricePerSecurity without ValidationErrors for valid data",
        "validate exercisePricePerSecurity with ValidationErrors for a number with over 4 decimal places",
        "validate exercisePricePerSecurity with ValidationErrors for an alphanumeric value",
        "validate exercisePricePerSecurity with ValidationErrors for a number too large",
        //Column AG
        "validate marketValuePerSecurityAcquired without ValidationErrors for valid data",
        "validate marketValuePerSecurityAcquired with ValidationErrors for a number with over 4 decimal places",
        "validate marketValuePerSecurityAcquired with ValidationErrors for an alphanumeric value",
        "validate marketValuePerSecurityAcquired with ValidationErrors for a number too large",
        //Column AH
        "validate sharesListedOnSE without ValidationErrors for valid data",
        "validate sharesListedOnSE with ValidationErrors for invalid data",
        //Column AI
        "validate marketValueAgreedHMRC without ValidationErrors for valid data",
        "validate marketValueAgreedHMRC with ValidationErrors for invalid data",
        //Column AJ
        "validate hmrcRef without ValidationErrors for valid data",
        "validate hmrcRef with ValidationErrors for invalid data",
        //Column AK
        "validate amountDeductible without ValidationErrors for valid data",
        "validate amountDeductible with ValidationErrors for a number with over 4 decimal places",
        "validate amountDeductible with ValidationErrors for an alphanumeric value",
        "validate amountDeductible with ValidationErrors for a number too large",
        //Column AL
        "validate q38 without ValidationErrors for valid data",
        "validate q38 with ValidationErrors for invalid data",
        //Column AM
        "validate amountReceived without ValidationErrors for valid data",
        "validate amountReceived with ValidationErrors for a number with over 4 decimal places",
        "validate amountReceived with ValidationErrors for an alphanumeric value",
        "validate amountReceived with ValidationErrors for a number too large",
        //Column AN
        "validate nicsElectionAgreementEnteredInto without ValidationErrors for valid data",
        "validate nicsElectionAgreementEnteredInto with ValidationErrors for invalid data",
        //Column AO
        "validate payeOperatedApplied without ValidationErrors for valid data",
        "validate payeOperatedApplied with ValidationErrors for invalid data",
        //Column AP
        "validate adjusmentMadeForUKDuties without ValidationErrors for valid data",
        "validate adjusmentMadeForUKDuties with ValidationErrors for invalid data"
      )
    descriptions
  }

  def getTestData: List[Cell] = {
    val testData = List(
      Cell("A",rowNumber,"2014-08-09"),
      Cell("A",rowNumber,"2014/08/09"),
      Cell("A",rowNumber,""),
      Cell("B",rowNumber,"yes"),
      Cell("B",rowNumber,"abc"),
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
      Cell("H",rowNumber,"ab$%c"),
      Cell("I",rowNumber,"2014-08-09"),
      Cell("I",rowNumber,"2014/08/09"),
      Cell("I",rowNumber,""),
      Cell("J",rowNumber,"Company Name"),
      Cell("J",rowNumber,StringUtils.leftPad("",145, "A")),
      Cell("J",rowNumber,""),
      Cell("K",rowNumber,"Company Address 1"),
      Cell("K",rowNumber,StringUtils.leftPad("",30, "A")),
      Cell("K",rowNumber,""),
      Cell("L",rowNumber,"Company Address 2"),
      Cell("L",rowNumber,StringUtils.leftPad("",30, "A")),
      Cell("M",rowNumber,"Company Address 3"),
      Cell("M",rowNumber,StringUtils.leftPad("",30, "A")),
      Cell("N",rowNumber,"Company Address 4"),
      Cell("N",rowNumber,StringUtils.leftPad("",20, "A")),
      Cell("O",rowNumber,"Company Country"),
      Cell("O",rowNumber,StringUtils.leftPad("",20, "A")),
      Cell("P",rowNumber,"SR77BS"),
      Cell("P",rowNumber,"abc"),
      Cell("Q",rowNumber,"AC097609"),
      Cell("Q",rowNumber,"abv%"),
      Cell("Q",rowNumber,""),
      Cell("R",rowNumber,"1234567800"),
      Cell("R",rowNumber,"abv"),
      Cell("S",rowNumber,"123/XZ55555555"),
      Cell("S",rowNumber,"abv$£"),
      Cell("T",rowNumber,"Company Name"),
      Cell("T",rowNumber,StringUtils.leftPad("",145, "A")),
      Cell("T",rowNumber,""),
      Cell("U",rowNumber,"Company Address 1"),
      Cell("U",rowNumber,StringUtils.leftPad("",30, "A")),
      Cell("V",rowNumber,"Company Address 2"),
      Cell("V",rowNumber,StringUtils.leftPad("",30, "A")),
      Cell("W",rowNumber,"Company Address 3"),
      Cell("W",rowNumber,StringUtils.leftPad("",30, "A")),
      Cell("X",rowNumber,"Company Address 4"),
      Cell("X",rowNumber,StringUtils.leftPad("",20, "A")),
      Cell("Y",rowNumber,"Company Country"),
      Cell("Y",rowNumber,StringUtils.leftPad("",20, "A")),
      Cell("Z",rowNumber,"SR77BS"),
      Cell("Z",rowNumber,"abc"),
      Cell("AA",rowNumber,"AC097609"),
      Cell("AA",rowNumber,"abv£"),
      Cell("AB",rowNumber,"1234567800"),
      Cell("AB",rowNumber,"abv"),
      Cell("AC",rowNumber,"123/XZ55555555"),
      Cell("AC",rowNumber,"abv££"),
      Cell("AD",rowNumber,"no"),
      Cell("AD",rowNumber,"av"),
      Cell("AE",rowNumber,"100.00"),
      Cell("AE",rowNumber,"100.00121"),
      Cell("AE",rowNumber,"abc"),
      Cell("AE", rowNumber, StringUtils.leftPad("", 15, "1") + ".34"),
      Cell("AF", rowNumber, "10.1234"),
      Cell("AF", rowNumber, "10.1234567"),
      Cell("AF", rowNumber, "12nas"),
      Cell("AF", rowNumber, StringUtils.leftPad("", 15, "1") + ".3234"),
      Cell("AG", rowNumber, "10.1234"),
      Cell("AG", rowNumber, "10.1234567"),
      Cell("AG", rowNumber, "12nas"),
      Cell("AG", rowNumber, StringUtils.leftPad("", 15, "1") + ".3234"),
      Cell("AH",rowNumber,"no"),
      Cell("AH",rowNumber,"av"),
      Cell("AI",rowNumber,"yes"),
      Cell("AI",rowNumber,"123"),
      Cell("AJ", rowNumber, "aa12345678"),
      Cell("AJ", rowNumber, "abc12345678901"),
      Cell("AK", rowNumber, "10.1234"),
      Cell("AK", rowNumber, "10.1234567"),
      Cell("AK", rowNumber, "12nas"),
      Cell("AK", rowNumber, StringUtils.leftPad("", 15, "1") + ".3234"),
      Cell("AL",rowNumber,"yes"),
      Cell("AL",rowNumber,"123"),
      Cell("AM", rowNumber, "10.1234"),
      Cell("AM", rowNumber, "10.1234567"),
      Cell("AM", rowNumber, "12nas"),
      Cell("AM", rowNumber, StringUtils.leftPad("", 15, "1") + ".3234"),
      Cell("AN",rowNumber,"no"),
      Cell("AN",rowNumber,"av"),
      Cell("AO",rowNumber,"yes"),
      Cell("AO",rowNumber,"?!"),
      Cell("AP",rowNumber,"no"),
      Cell("AP",rowNumber,"av")
    )
    testData
  }

  def getExpectedResults: List[Option[List[ValidationErrorData]]] = {
    val expectedResults = List(
     //Column A
      None,
      Some(List(ValidationErrorData("error.1","001","Enter a date that matches the yyyy-mm-dd pattern."))),
      Some(List(ValidationErrorData("error.1","001","Enter a date that matches the yyyy-mm-dd pattern."))),
      //Column B
      None,
      Some(List(ValidationErrorData("error.2","002","Enter 'yes' or 'no'."))),
      Some(List(ValidationErrorData("error.2","002","Enter 'yes' or 'no'."))),
      //Column C
      None,
      Some(List(ValidationErrorData("error.3","003","Enter the scheme reference number (it should be an 8 digit number)."))),
      //Column D
      None,
      Some(List(ValidationErrorData("error.4","004","Enter a first name (must be less than 36 characters and can only have letters, numbers, hyphens or apostrophes)."))),
      Some(List(ValidationErrorData("error.4","004","Enter a first name (must be less than 36 characters and can only have letters, numbers, hyphens or apostrophes)."))),
      //Column E
      None,
      Some(List(ValidationErrorData("error.5","005","Must be less than 36 characters and can only have letters, numbers, hyphens or apostrophes."))),
      //Column F
      None,
      Some(List(ValidationErrorData("error.6","006","Enter a last name (must be less than 36 characters and can only have letters, numbers, hyphens or apostrophes)."))),
      Some(List(ValidationErrorData("error.6","006","Enter a last name (must be less than 36 characters and can only have letters, numbers, hyphens or apostrophes)."))),
      //Column G
      None,
      Some(List(ValidationErrorData("error.7","007","National Insurance number must be 2 letters followed by 6 number digits, with an optional final letter."))),
      //Column H
      None,
      Some(List(ValidationErrorData("error.8","008","PAYE reference must be a 3 digit number followed by a forward slash and up to 10 more characters."))),
      //Column I
      None,
      Some(List(ValidationErrorData("error.9","009","Enter a date that matches the yyyy-mm-dd pattern."))),
      Some(List(ValidationErrorData("error.9","009","Enter a date that matches the yyyy-mm-dd pattern."))),
      //Column J
      None,
      Some(List(ValidationErrorData("error.10","010","Enter the company name (must be less than 121 characters and can only have letters, numbers, hyphens or apostrophes)."))),
      Some(List(ValidationErrorData("error.10","010","Enter the company name (must be less than 121 characters and can only have letters, numbers, hyphens or apostrophes)."))),
      //Column K
      None,
      Some(List(ValidationErrorData("error.11","011","Enter the first line of the address (must be less than 28 characters and can only have letters, numbers, hyphens, apostrophes, forward slashes, commas or ampersands)."))),
      Some(List(ValidationErrorData("error.11","011","Enter the first line of the address (must be less than 28 characters and can only have letters, numbers, hyphens, apostrophes, forward slashes, commas or ampersands)."))),
      //Column L
      None,
      Some(List(ValidationErrorData("error.12","012","Must be less than 28 characters and can only have letters, numbers, hyphens, apostrophes, forward slashes, commas or ampersands." ))),
      //Column M
      None,
      Some(List(ValidationErrorData("error.13","013","Must be less than 28 characters and can only have letters, numbers, hyphens, apostrophes, forward slashes, commas or ampersands." ))),
      //Column N
      None,
      Some(List(ValidationErrorData("error.14","014","Must be less than 19 characters and can only have letters, numbers, hyphens, apostrophes, forward slashes, commas or ampersands."))),
      //Column O
      None,
      Some(List(ValidationErrorData("error.15","015","Must be less than 19 characters and can only have letters, numbers, hyphens, apostrophes, forward slashes, commas or ampersands."))),
      //Column P
      None,
      Some(List(ValidationErrorData("error.16","016","Must be 6 to 8 characters and only have capital letters."))),
      //Column Q
      None,
      Some(List(ValidationErrorData("error.17","017","Company Reference Number must be less than 11 characters (numbers and letters)."))),
      None,
      //Column R
      None,
      Some(List(ValidationErrorData("error.18","018","Corporation Tax reference must be a 10 digit number."))),
      //Column S
      None,
      Some(List(ValidationErrorData("error.19","019","PAYE reference must be a 3 digit number followed by a forward slash and up to 10 more characters."))),
      //Column T
      None,
      Some(List(ValidationErrorData("error.20","020","Enter the company name (must be less than 121 characters and can only have letters, numbers, hyphens or apostrophes)."))),
      Some(List(ValidationErrorData("error.20","020","Enter the company name (must be less than 121 characters and can only have letters, numbers, hyphens or apostrophes)."))),
      //Column U
      None,
      Some(List(ValidationErrorData("error.21","021","Enter the first line of the address (must be less than 28 characters and can only have letters, numbers, hyphens, apostrophes, forward slashes, commas or ampersands)."))),
      //Column V
      None,
      Some(List(ValidationErrorData("error.22","022","Must be less than 28 characters and can only have letters, numbers, hyphens, apostrophes, forward slashes, commas or ampersands." ))),
      //Column W
      None,
      Some(List(ValidationErrorData("error.23","023","Must be less than 28 characters and can only have letters, numbers, hyphens, apostrophes, forward slashes, commas or ampersands." ))),
      //Column X
      None,
      Some(List(ValidationErrorData("error.24","024","Must be less than 19 characters and can only have letters, numbers, hyphens, apostrophes, forward slashes, commas or ampersands."))),
      //Column Y
      None,
      Some(List(ValidationErrorData("error.25","025","Must be less than 19 characters and can only have letters, numbers, hyphens, apostrophes, forward slashes, commas or ampersands."))),
      //Column Z
      None,
      Some(List(ValidationErrorData("error.26","026","Must be 6 to 8 characters and only have capital letters."))),
      //Column AA
      None,
      Some(List(ValidationErrorData("error.27","027","Company Reference Number must be less than 11 characters (numbers and letters)."))),
      //Column AB
      None,
      Some(List(ValidationErrorData("error.28","028","Corporation Tax reference must be a 10 digit number."))),
      //Column AC
      None,
      Some(List(ValidationErrorData("error.29","029","PAYE reference must be a 3 digit number followed by a forward slash and up to 10 more characters."))),
      //Column AD
      None,
      Some(List(ValidationErrorData("error.30","030","Enter 'yes' or 'no'."))),
      //Column AE
      None,
      Some(List(ValidationErrorData("error.31","031","Must be a number with 2 digits after the decimal point (and no more than 11 digits in front of it)."))),
      Some(List(ValidationErrorData("error.31","031","Must be a number with 2 digits after the decimal point (and no more than 11 digits in front of it)."))),
      Some(List(ValidationErrorData("error.31","031","Must be a number with 2 digits after the decimal point (and no more than 11 digits in front of it)."))),
      //Column AF
      None,
      Some(List(ValidationErrorData("error.32","032","Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
      Some(List(ValidationErrorData("error.32","032","Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
      Some(List(ValidationErrorData("error.32","032","Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
      //Column AG
      None,
      Some(List(ValidationErrorData("error.33","033","Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
      Some(List(ValidationErrorData("error.33","033","Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
      Some(List(ValidationErrorData("error.33","033","Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
      //Column AH
      None,
      Some(List(ValidationErrorData("error.34","034","Enter 'yes' or 'no'."))),
      //Column AI
      None,
      Some(List(ValidationErrorData("error.35","035","Enter 'yes' or 'no'."))),
      //Column AJ
      None,
      Some(List(ValidationErrorData("error.36","036","Enter the HMRC reference (must be less than 11 characters)."))),
      //Column AK
      None,
      Some(List(ValidationErrorData("error.37","037","Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
      Some(List(ValidationErrorData("error.37","037","Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
      Some(List(ValidationErrorData("error.37","037","Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
      //Column AL
      None,
      Some(List(ValidationErrorData("error.38","038","Enter 'yes' or 'no'."))),
      //Column AM
      None,
      Some(List(ValidationErrorData("error.39","039","Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
      Some(List(ValidationErrorData("error.39","039","Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
      Some(List(ValidationErrorData("error.39","039","Must be a number with 4 digits after the decimal point (and no more than 13 digits in front of it)."))),
      //Column AN
      None,
      Some(List(ValidationErrorData("error.40","040","Enter 'yes' or 'no'."))),
      //Column AO
      None,
      Some(List(ValidationErrorData("error.41","041","Enter 'yes' or 'no'."))),
      //Column AP
      None,
      Some(List(ValidationErrorData("error.42","042","Enter 'yes' or 'no'.")))
    )
    expectedResults
  }

  def getValidRowData:Seq[Cell] = {
    val rowData = Seq(
      Cell("A",rowNumber,"2014-08-09"),
      Cell("B",rowNumber,"yes"),
      Cell("C",rowNumber,"12345678"),
      Cell("D",rowNumber,"Billy"),
      Cell("E",rowNumber,"Bob"),
      Cell("F",rowNumber,"Thornton"),
      Cell("G",rowNumber,"AB123456A"),
      Cell("H",rowNumber,"123/XZ55555555"),
      Cell("I",rowNumber,"2014-08-09"),
      Cell("J",rowNumber,"Company Name"),
      Cell("K",rowNumber,"Company Address 1"),
      Cell("L",rowNumber,"Company Address 2"),
      Cell("M",rowNumber,"Company Address 3"),
      Cell("N",rowNumber,"Company Address 4"),
      Cell("O",rowNumber,"Company Country"),
      Cell("P",rowNumber,"SR77BS"),
      Cell("Q",rowNumber,"AC097609"),
      Cell("R",rowNumber,"1234567800"),
      Cell("S",rowNumber,"123/XZ55555555"),
      Cell("T",rowNumber,"Company Name"),
      Cell("U",rowNumber,"Company Address 1"),
      Cell("V",rowNumber,"Company Address 2"),
      Cell("W",rowNumber,"Company Address 3"),
      Cell("X",rowNumber,"Company Address 4"),
      Cell("Y",rowNumber,"Company Country"),
      Cell("Z",rowNumber,"SR77BS"),
      Cell("AA",rowNumber,"AC097609"),
      Cell("AB",rowNumber,"1234567800"),
      Cell("AC",rowNumber,"123/XZ55555555"),
      Cell("AD",rowNumber,"no"),
      Cell("AE",rowNumber,"100.00"),
      Cell("AF", rowNumber,"10.1234"),
      Cell("AG",rowNumber,"10.1234"),
      Cell("AH",rowNumber,"no"),
      Cell("AI",rowNumber,"yes"),
      Cell("AJ", rowNumber,"aa12345678"),
      Cell("AK", rowNumber,"10.1234"),
      Cell("AL",rowNumber,"yes"),
      Cell("AM", rowNumber,"10.1234"),
      Cell("AN",rowNumber,"no"),
      Cell("AO",rowNumber,"yes"),
      Cell("AP",rowNumber,"no")
    )
    rowData
  }

  def getInvalidRowData:Seq[Cell] = {
    val rowData = Seq(
      Cell("A",rowNumber,"2014/08/09"),
      Cell("B",rowNumber,"abc"),
      Cell("C",rowNumber,"12345678901"),
      Cell("D",rowNumber,StringUtils.leftPad("",45, "A")),
      Cell("E",rowNumber,StringUtils.leftPad("",45, "A")),
      Cell("F",rowNumber,StringUtils.leftPad("",45, "A")),
      Cell("G",rowNumber,"abc"),
      Cell("H",rowNumber,"abc$%"),
      Cell("I",rowNumber,"2014/08/09"),
      Cell("J",rowNumber,StringUtils.leftPad("",145, "A")),
      Cell("K",rowNumber,StringUtils.leftPad("",30, "A")),
      Cell("L",rowNumber,StringUtils.leftPad("",30, "A")),
      Cell("M",rowNumber,StringUtils.leftPad("",30, "A")),
      Cell("N",rowNumber,StringUtils.leftPad("",20, "A")),
      Cell("O",rowNumber,StringUtils.leftPad("",20, "A")),
      Cell("P",rowNumber,"abc"),
      Cell("Q",rowNumber,"a$bv"),
      Cell("R",rowNumber,"abv"),
      Cell("S",rowNumber,"abv$£"),
      Cell("T",rowNumber,StringUtils.leftPad("",145, "A")),
      Cell("U",rowNumber,StringUtils.leftPad("",30, "A")),
      Cell("V",rowNumber,StringUtils.leftPad("",30, "A")),
      Cell("W",rowNumber,StringUtils.leftPad("",30, "A")),
      Cell("X",rowNumber,StringUtils.leftPad("",20, "A")),
      Cell("Y",rowNumber,StringUtils.leftPad("",20, "A")),
      Cell("Z",rowNumber,"abc"),
      Cell("AA",rowNumber,"ab$v"),
      Cell("AB",rowNumber,"abv"),
      Cell("AC",rowNumber,"abv$%"),
      Cell("AD",rowNumber,"av"),
      Cell("AE", rowNumber, StringUtils.leftPad("", 15, "1") + ".34"),
      Cell("AF", rowNumber, StringUtils.leftPad("", 15, "1") + ".3234"),
      Cell("AG", rowNumber, StringUtils.leftPad("", 15, "1") + ".3234"),
      Cell("AH",rowNumber,"av"),
      Cell("AI",rowNumber,"123"),
      Cell("AJ", rowNumber, "abc12345678901"),
      Cell("AK", rowNumber, StringUtils.leftPad("", 15, "1") + ".3234"),
      Cell("AL",rowNumber,"123"),
      Cell("AM", rowNumber, StringUtils.leftPad("", 15, "1") + ".3234"),
      Cell("AN",rowNumber,"av"),
      Cell("AO",rowNumber,"?!"),
      Cell("AP",rowNumber,"av")
    )
    rowData
  }

}
