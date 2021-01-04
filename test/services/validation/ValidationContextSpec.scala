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

package services.validation

import org.apache.commons.lang3.StringUtils
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec

class ValidationContextSpec extends PlaySpec with MockitoSugar {

  object ersValidationFormatters extends ERSValidationFormatters

  "ERSValidationFormatNumberMax6" should {
    val validationRule:String = ersValidationFormatters.ERSValidationFormatNumberMax6
    "return true given string containing a number of 6 in length" in {
      val result = ValidationContext.verifyFormat(validationRule,"123456")
      result mustBe true
    }
    "return false given an alphanumeric string" in {
      val result = ValidationContext.verifyFormat(validationRule,"123xc6")
      result mustBe false
    }
    "return true given a string containing a sequence numbers less than six in length" in {
      ValidationContext.verifyFormat(validationRule,"1234") mustBe true
    }
    "return false given string containing a number of incorrect characters in length" in {
      ValidationContext.verifyFormat(validationRule, "1234567890") mustBe false
    }
  }

  "ERSValidationFormatNumber8" should {
    val validationRule:String = ersValidationFormatters.ERSValidationFormatNumber8
    "return true given string containing a number of 8 in length" in {
      val result = ValidationContext.verifyFormat(validationRule,"12345678")
      result mustBe true
    }
    "return false given an alphanumeric string" in {
      val result = ValidationContext.verifyFormat(validationRule,"123xc6")
      result mustBe false
    }
    "return false given a string containing a sequence numbers less than eight in length" in {
      ValidationContext.verifyFormat(validationRule,"1234567") mustBe false
    }
    "return false given a string containing a sequence numbers more than eight in length" in {
      ValidationContext.verifyFormat(validationRule, "123456789012") mustBe false
    }
  }

  "ERSValidationFormatWholeNumber" should {
    val validationRule:String = ersValidationFormatters.ERSValidationFormatWholeNumber
    "return true given string containing an integer" in {
      val result = ValidationContext.verifyFormat(validationRule,"12345678")
      result mustBe true
    }
    "return false given an alphanumeric string" in {
      val result = ValidationContext.verifyFormat(validationRule,"123xc6")
      result mustBe false
    }
    "return false given string containing a floating point number" in {
      ValidationContext.verifyFormat(validationRule,"12.34") mustBe false
    }
  }

  "ERSValidationFormatNumberMax11" should {
    val validationRule:String = ersValidationFormatters.ERSValidationFormatNumberMax11
    "return true given  a string containing a number of 11 in length with 2 decimal places" in {
      ValidationContext.verifyFormat(validationRule,"12445678901.01") mustBe true
    }
    "return true given a string containing a number of valid length and 1 decimal place" in {
      ValidationContext.verifyFormat(validationRule,"12445678901.1") mustBe true
    }
    "return false given an alphanumeric string" in {
      ValidationContext.verifyFormat(validationRule,"12345wwe901.02") mustBe false
    }
    "return false given a string containing a number of incorrect characters in length with the correct decimal places" in {
      ValidationContext.verifyFormat(validationRule,"124412124553233121.02") mustBe false
    }
    "return true given a string containing a number of fewer than 11 in length with the correct decimal places" in {
      ValidationContext.verifyFormat(validationRule,"12.02") mustBe true
    }
    "return true given a string containing 0" in {
      ValidationContext.verifyFormat(validationRule,"0") mustBe true
    }
  }

  "ERSValidationFormat2DecimalPlaces" should {
    val validationRule:String = ersValidationFormatters.ERSValidationFormat2DecimalPlaces
    "return true given a string containing a number of 11 in length with the 2 decimal places" in {
      ValidationContext.verifyFormat(validationRule,"12445678901.11") mustBe true
    }
    "return false given a string containing a number of 11 in length with no decimal places" in {
      ValidationContext.verifyFormat(validationRule,"12445678901.") mustBe false
    }
    "return false given a string containing a number of 11 in length with three decimal places" in {
      ValidationContext.verifyFormat(validationRule,"12445678901.111") mustBe false
    }
    "return false given a string containing a decimal point and trailing decimals" in {
      ValidationContext.verifyFormat(validationRule,".01") mustBe false
    }
  }

  "ERSValidationFormatNumberMax13" should {
    val validationRule:String = ersValidationFormatters.ERSValidationFormatNumberMax13
    "return true given a string containing a number of 13 in length with 4 decimal places" in {
      ValidationContext.verifyFormat(validationRule,"1244567890123.0123") mustBe true
    }
    "return true given a string containing a number of valid length and fewer than 4 decimal places" in {
      ValidationContext.verifyFormat(validationRule,"1244567890123.1") mustBe true
    }
    "return true given a string containing a number of fewer than 13 in length with the correct decimal places" in {
      ValidationContext.verifyFormat(validationRule,"12.1202") mustBe true
    }
    "return true given a string containing 0" in {
      ValidationContext.verifyFormat(validationRule,"0") mustBe true
    }
    "return false given an alphanumeric string" in {
      ValidationContext.verifyFormat(validationRule,"12345wwe901.0232") mustBe false
    }
    "return false given a string containing a number of incorrect characters in length with the correct decimal places" in {
      ValidationContext.verifyFormat(validationRule,"12441212455323323121.02") mustBe false
    }
  }

  "ERSValidationFormat4DecimalPlaces" should {
    val validationRule:String = ersValidationFormatters.ERSValidationFormat4DecimalPlaces

    "return true given a string containing a number with the correct amount of decimal places" in {
      ValidationContext.verifyFormat(validationRule,"12.3456") mustBe true
    }
    "return false given a string containing a number with the incorrect decimal places" in {
      ValidationContext.verifyFormat(validationRule,"23.12") mustBe false
    }
    "return false given a string containing a decimal point and trailing decimals" in {
      ValidationContext.verifyFormat(validationRule,".0211") mustBe false
    }
  }

  "ERSValidationFormatYesNo" should {
    val validationRule:String = ersValidationFormatters.ERSValidationFormatYesNo
    "return true given a string containing yes in lower or upper" in {
      ValidationContext.verifyFormat(validationRule,"yes") mustBe true
    }
    "return true given a string containing no in lower or upper" in {
      ValidationContext.verifyFormat(validationRule,"no") mustBe true
    }
    "return false given a string containing the same characters as yes in a scrambled order" in {
      ValidationContext.verifyFormat(validationRule,"sey") mustBe false
    }
    "return false given a string containing the same characters as no in a scrambled order" in {
      ValidationContext.verifyFormat(validationRule,"on") mustBe false
    }
    "return false given a string containing both yes and no" in {
      ValidationContext.verifyFormat(validationRule,"yesno") mustBe false
    }
  }

  "ERSValidationFormatHMRCRef" should {
    val validationRule:String = ersValidationFormatters.ERSValidationFormatHMRCRef
    "return true given a string containing 2 characters and 8 digits" in {
      ValidationContext.verifyFormat(validationRule, "aa12345678") mustBe true
    }
    "return true given a string containing 10 digits" in {
      ValidationContext.verifyFormat(validationRule, "1234567889") mustBe true
    }
    "return true given a string containing 10 characters" in {
      ValidationContext.verifyFormat(validationRule, "abcdefghij") mustBe true
    }
    "return true given a string containing up to 10 digits" in {
      ValidationContext.verifyFormat(validationRule, "189") mustBe true
    }
    "return flase given a string containing 10 characters including non-alphanumeric" in {
      ValidationContext.verifyFormat(validationRule, "1234 678aa") mustBe false
    }
    "return false given a string that exceeds 10 characters and numbers in length" in {
      ValidationContext.verifyFormat(validationRule, "1234567890abcd") mustBe false
    }
  }

  "ERSValidationFormat1To8" should {
    val validationRule:String = ersValidationFormatters.ERSValidationFormat1To8
    "return true given a string containing 1 number between 1 and 8" in {
      ValidationContext.verifyFormat(validationRule, "4") mustBe true
    }
    "return false given a string containing more than 1 digits" in {
      ValidationContext.verifyFormat(validationRule, "11") mustBe false
    }
    "return false given a string containing a value larger than 8" in {
      ValidationContext.verifyFormat(validationRule, "9") mustBe false
    }
    "return false given a string containing a value less than 1" in {
      ValidationContext.verifyFormat(validationRule, "0") mustBe false
    }
  }

  "ERSValidations.Name" should {
    val validationRule:String = ersValidationFormatters.ERSValidationFormatName
    "return true given a string containing any combination of letters" in {
      ValidationContext.verifyFormat(validationRule, "John") mustBe true
    }
    "return true given a string containing any combination of letters and a hyphen and apostrophe" in {
      ValidationContext.verifyFormat(validationRule, "John-Mc'Clane") mustBe true
    }
    "return true given a string containing a single white space between characters" in {
      ValidationContext.verifyFormat(validationRule, "John McClane") mustBe true
    }
    "return false given a string containing any more than a single white space" in {
      ValidationContext.verifyFormat(validationRule, "John    McClane") mustBe false
    }
    "return false given a string containing numbers" in {
      ValidationContext.verifyFormat(validationRule, "12345678aa") mustBe false
    }
    "return false given a string containing invalid symbols" in {
      ValidationContext.verifyFormat(validationRule, "asdas**A") mustBe false
    }
    "return true given string containing equal to 35 characters" in {
      characterLengthCheck(35, validationRule) mustBe true
    }
    "return false given string containing more than 35 characters" in {
      characterLengthCheck(40, validationRule) mustBe false
    }
  }

  "ERSValidations.Nino" should {
    val validationRule:String = ersValidationFormatters.ERSValidationFormatNino
    "return true given a string containing a valid NINO" in {
      ValidationContext.verifyFormat(validationRule, "AB123456C") mustBe true
    }
    "return false given a string containing only a prefix" in {
      ValidationContext.verifyFormat(validationRule, "AS1234564") mustBe false
    }
    "return false given a string containing only the suffix" in {
      ValidationContext.verifyFormat(validationRule, "12123456C") mustBe false
    }
  }

  "ERSValidations.paye" should {
    val validationRule:String = ersValidationFormatters.ERSValidationFormatPaye
    "return true given a string containing a valid PAYE ref" in {
      ValidationContext.verifyFormat(validationRule, "123/XZ55555555") mustBe true
    }
    "return true given a string containing a valid PAYE ref with trailing characters up to 10" in {
      ValidationContext.verifyFormat(validationRule, "120/GA87198") mustBe true
    }
    "return false given a string containing an invalid string" in {
      ValidationContext.verifyFormat(validationRule, "123???") mustBe false
    }
  }

  "ERSValidations.Srn" should {
    val validationRule:String = ersValidationFormatters.ERSValidationFormatSrn
    "return true given string containing a number of 8 in length" in {
      val result = ValidationContext.verifyFormat(validationRule,"12345678")
      result mustBe true
    }
    "return false given an alphanumeric string" in {
      val result = ValidationContext.verifyFormat(validationRule,"123xc678")
      result mustBe false
    }
    "return false given string containing a sequence of numbers more than 8 in length" in {
      ValidationContext.verifyFormat(validationRule, "123456789") mustBe false
    }
    "return false given string containing a sequence of numbers less than 8 in length" in {
      ValidationContext.verifyFormat(validationRule, "1234567") mustBe false
    }
  }

  "ERSValidations.CompanyName" should {
    val validationRule:String = ersValidationFormatters.ERSValidationFormatCompanyName
    "return true given string containing equal to 120 characters" in {
      characterLengthCheck(120, validationRule) mustBe true
    }
    "return true given string containing equal to 5 characters" in {
      characterLengthCheck(5, validationRule) mustBe true
    }
    "return false given a string containing more than 120 characters" in {
      characterLengthCheck(122, validationRule) mustBe false
    }
    "return false given a string containing less than 1 character" in {
      characterLengthCheck(0, validationRule) mustBe false
    }
  }

  "ERSValidations.CompanyAddress1to3" should {
    val validationRule:String = ersValidationFormatters.ERSValidationFormatCompanyAddress1to3
    "return true given a string containing equal to 27 characters" in {
      characterLengthCheck(27, validationRule) mustBe true
    }
    "return true given a string containing equal to 5 characters" in {
      characterLengthCheck(5, validationRule) mustBe true
    }
    "return false given a string containing more than 27 characters" in {
      characterLengthCheck(30, validationRule) mustBe false
    }
    "return false given a string containing less than 1 character" in {
      characterLengthCheck(0, validationRule) mustBe false
    }
  }

  "ERSValidationsSpec.CompanyAddress4County" should {
    val validationRule:String = ersValidationFormatters.ERSValidationFormatAddress4County
    "return true given a string containing equal to 18 characters" in {
      characterLengthCheck(18, validationRule) mustBe true
    }
    "return true given a string containing equal to 5 characters" in {
      characterLengthCheck(5, validationRule) mustBe true
    }
    "return false given a string containing more than 18 characters" in {
      characterLengthCheck(20, validationRule) mustBe false
    }
    "return false given a string containing less than 1 character" in {
      characterLengthCheck(0, validationRule) mustBe false
    }
  }

  "ERSValidations.Postcode" should {
    val validationRule:String = ersValidationFormatters.ERSValidationFormatPostcode
    "return true given a valid postcode 1" in {
      ValidationContext.verifyFormat(validationRule, "A1 1AA") mustBe true
    }
    "return true given a valid postcode 2" in {
      ValidationContext.verifyFormat(validationRule,"A12 1AA") mustBe true
    }
    "return true given a valid postcode 3" in {
      ValidationContext.verifyFormat(validationRule,"AB1 1AA") mustBe true
    }
    "return true given a valid postcode 4" in {
      ValidationContext.verifyFormat(validationRule,"AB12 1AA") mustBe true
    }
    "return true given a valid postcode 5" in {
      ValidationContext.verifyFormat(validationRule,"AB1A 1AA") mustBe true
    }
    "return true given a valid postcode 6" in {
      ValidationContext.verifyFormat(validationRule,"AB1A 1AA") mustBe true
    }
    "return false given a string containing an invalid format" in {
      ValidationContext.verifyFormat(validationRule,"12345") mustBe false
    }
  }

  "ERSValidations.Crn" should {
    val validationRule:String = ersValidationFormatters.ERSValidationFormatCRN
    "return true given a string containing a valid CRN" in {
      ValidationContext.verifyFormat(validationRule,"AC097609") mustBe true
    }
    "return true given a string containing a valid CRN 2" in {
      ValidationContext.verifyFormat(validationRule,"12334567") mustBe true
    }
    "return false given a string containing an invalid CRN" in {
      ValidationContext.verifyFormat(validationRule,"ACC9760%$") mustBe false
    }
    "return false given a string that exceeds 10 characters" in {
      characterLengthCheck(12, validationRule) mustBe false
    }
  }

  "ERSValidations.CorporationTaxRef" should {
    val validationRule:String = ersValidationFormatters.ERSValidationFormatCTRef
    "return true given string containing a number of 10 in length" in {
      ValidationContext.verifyFormat(validationRule,"1234567789") mustBe true
    }
    "return false given an alphanumeric string" in {
      ValidationContext.verifyFormat(validationRule,"123xc67654") mustBe false
    }
    "return false given an empty string" in {
      ValidationContext.verifyFormat(validationRule,"") mustBe false
    }
    "return false given string containing a number of more than 10" in {
      ValidationContext.verifyFormat(validationRule, "12345678901") mustBe false
    }
  }

  "ERSValidations.verifyDate" should {
    "return true given a valid date according to the format yyyy-mm-dd" in {
      ValidationContext.verifyDate("2015-12-31") mustBe true
    }
    "return false given an invalid date according to the format yyyy-mm-dd" in {
      ValidationContext.verifyDate("31-12-2015") mustBe false
    }
  }

  "ERSValidations.notEmpty" should {
    "return true given a populated string" in {
      ValidationContext.notEmpty("123") mustBe true
    }
    "return false given an empty string" in {
      ValidationContext.notEmpty("") mustBe false
    }
    "return false given a string of white space" in {
      ValidationContext.notEmpty("       ") mustBe false
    }
  }

  "ERSValidations.mandatoryBoolean" should {
    "pass when a field is required as another field is answered no and has data" in {
      val dataX = "no"
      val dataY = "data"
      val condition = "no"
      ValidationContext.mandatoryBoolean(condition, dataX, dataY) mustBe true
    }
    "pass when a field is required as another is answered no but the field it relies on is yes" in {
      val dataX = "yes"
      val dataY = ""
      val condition = "no"
      ValidationContext.mandatoryBoolean(condition, dataX, dataY) mustBe true
    }
    "fail when a field is required as another is answered no and has no data" in {
      val dataX = "no"
      val dataY = ""
      val condition = "no"
      ValidationContext.mandatoryBoolean(condition, dataX, dataY) mustBe false
    }
  }

  "ERSValidationFormat6digits2decimalplaces" should {

    val validationRule:String = ersValidationFormatters.ERSValidationFormat6digits1decimalplaces

    "return true for a number with two decimal places and less that six digits" in {
      ValidationContext.verifyFormat(validationRule,"12.1") mustBe true
    }

    "return false for a number with more than six digits" in {
      ValidationContext.verifyFormat(validationRule,"1234567.1") mustBe false
    }

    "return false when not given too many decimal points" in {
      ValidationContext.verifyFormat(validationRule,"12.12") mustBe false
    }

    "return false when not given a floating point value" in {
      ValidationContext.verifyFormat(validationRule,"12") mustBe false
    }

  }

  "ERSValidationFormatNumber1to4 " should {
    val validationRule:String = ersValidationFormatters.ERSValidationFormatNumber1to4
    "return true given a 1" in {
      ValidationContext.verifyFormat(validationRule, "1") mustBe true
    }
    "return true given a 2" in {
      ValidationContext.verifyFormat(validationRule, "2") mustBe true
    }
    "return true given a 3" in {
      ValidationContext.verifyFormat(validationRule, "3") mustBe true
    }
    "return true given a 4" in {
      ValidationContext.verifyFormat(validationRule, "4") mustBe true
    }

    "return false when given a floating point value" in {
      ValidationContext.verifyFormat(validationRule, "2.5") mustBe false
    }
    "return false when given a number is smaller than the allowed range" in {
      ValidationContext.verifyFormat(validationRule, "0") mustBe false
    }
    "return false when given a number is larger than the allowed range" in {
      ValidationContext.verifyFormat(validationRule, "5") mustBe false
    }
  }

  "ERSValidationFormatMax1" should {
    val validationRule:String = ersValidationFormatters.ERSValidationFormatMax1
    "return true given a single integer value" in{
      ValidationContext.verifyFormat(validationRule, "1") mustBe true
    }
    "return false given multiple values" in {
      ValidationContext.verifyFormat(validationRule, "55") mustBe false
    }
    "return false floating point values" in {
      ValidationContext.verifyFormat(validationRule, "2.5") mustBe false
    }
  }

  "ERSValidationFormatRatio " should {
    val validationRule:String = ersValidationFormatters.ERSValidationFormatRatio
    "return true given a valid ratio ':'" in {
      ValidationContext.verifyFormat(validationRule, "2:2") mustBe true
    }
    "return true given a valid ratio '/'" in {
      ValidationContext.verifyFormat(validationRule, "2/2") mustBe true
    }
    "return false given an invalid ratio, decimal point" in{
      ValidationContext.verifyFormat(validationRule, "2/2.5") mustBe false
    }
    "return false given an invalid ratio, single number" in{
      ValidationContext.verifyFormat(validationRule, "2") mustBe false
    }
    "return false given an invalid ratio, single number and decimal point" in{
      ValidationContext.verifyFormat(validationRule, "2.52") mustBe false
    }
  }

  def characterLengthCheck(length:Int, validationRule:String) = {
    val longString:String = StringUtils.leftPad("", length, 'A')
    ValidationContext.verifyFormat(validationRule,longString)
  }
}
