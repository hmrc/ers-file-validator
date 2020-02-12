/*
 * Copyright 2020 HM Revenue & Customs
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

package services.ersTemplatesInfo

trait EMITemplateInfo {

  val emi="EMI"

  val emiSheet1Name = "EMI40_Adjustments_V3"
  val emiSheet2Name = "EMI40_Replaced_V3"
  val emiSheet3Name = "EMI40_RLC_V3"
  val emiSheet4Name = "EMI40_NonTaxable_V3"
  val emiSheet5Name = "EMI40_Taxable_V3"

  val emiSheet1Desc = "EMI template - Adjustment of options"
  val emiSheet1ValConfig = "ers-emi-adjustments-validation-config"
  val emiSheet2Desc = "EMI template – Replacement of options"
  val emiSheet2ValConfig = "ers-emi-replaced-validation-config"
  val emiSheet3Desc = "EMI template - Options released, lapsed or cancelled"
  val emiSheet3ValConfig = "ers-emi-rlc-validation-config"
  val emiSheet4Desc = "EMI template - Non-taxable exercise of options"
  val emiSheet4ValConfig = "ers-emi-nontaxable-validation-config"
  val emiSheet5Desc = "EMI template - Taxable exercise of options"

  val emiSheet5ValConfig = "ers-emi-taxable-validation-config"
  val emiAdjustmentsHeaderRow = List(
    "1.Has there been any adjustment of options following a variation in the share capital of the company?(yes/no)",
    "2.Has there been a change to the description of the shares under option?(yes/no)",
    "3.Is the adjustment a disqualifying event?(yes/no). If YES go to question 4. If NO go to question 5.",
    "4.If yes, enter a number from 1 to 8 depending on the nature of the disqualifying event.Follow the link at cell A7 for a list of disqualifying events"	,
    "5.Date option adjusted(yyyy-mm-dd)",
    "6.Employee first name"	,
    "7.Employee second name(if applicable)"	,
    "8.Employee last name",
    "9.National Insurance number(if applicable)",
    "10.PAYE reference of employing company"	,
    "11.Exercise price per share under option before adjustment£e.g. 10.1234"	,
    "12.Number of shares under the option after adjustmente.g. 100.00",
    "13.Exercise price per share under option after the adjustment £e.g. 10.1234",
    "14.Actual market value of a share at the date of grant£e.g. 10.1234"
  )//.map(_.replaceAll(headerFormat,""))

  val emiReplacedHeaderRow = List(
    "1.Date of grant of old option(yyyy-mm-dd)",
    "2.Date of grant of new option(yyyy-mm-dd)",
    "3.Employee first name"	,
    "4.Employee second name(if applicable)",
    "5.Employee last name"	,
    "6.National Insurance number(if applicable)"	,
    "7.PAYE reference of employing company",
    "8.Actual market value of original shares at the date the option(s) were replaced£e.g. 10.1234",
    "9.Name of the company whose shares are the subject of the new option"	,
    "10.Company address line 1",
    "11.Company address line 2",
    "12.Company address line 3"	,
    "13.Company address line 4"	,
    "14.Country"	,
    "15.Postcode"	,
    "16.Corporation Tax reference(Unique Taxpayer Reference)"	,
    "17.Company Reference Number (CRN)"
  )//.map(_.replaceAll(headerFormat,""))

  val emiRCLHeaderRow = List(
    "1.Date of event(yyyy-mm-dd)",
    "2.Is the release, lapse or cancellation the result of a disqualifying event?(yes/no)",
    "3.If yes, enter a number from 1 to 8 depending on the nature of the disqualifying event.Follow the link at cell A7 for a list of disqualifying events",
    "4.Employee first name",
    "5.Employee second name(if applicable)",
    "6.Employee last name",
    "7.National Insurance number(if applicable)",
    "8.PAYE reference of employing company",
    "9.Number of shares subject to the option which have been released, lapsed or cancelled?e.g. 100.00",
    "10.Was money or value received?(yes/no)If yes go to question 11, otherwise no more information is needed for this event.",
    "11.If yes enter the amount£e.g. 10.1234",
    "12.PAYE operated?(yes/no)"
  )//.map(_.replaceAll(headerFormat,""))

  val emiNonTaxableHeaderRow = List(
    "1.Date of option exercise(yyyy-mm-dd)",
    "2.Employee first name",
    "3.Employee second name(if applicable)",
    "4.Employee last name",
    "5.National Insurance number(if applicable)",
    "6.PAYE reference of employing company",
    "7.Total number of shares employee entitled to on exercise of the option before any cashless exercise or other adjustmente.g. 100.00",
    "8.Actual market value (AMV) of a share at the date of grant of the options exercised£e.g. 10.1234",
    "9.Exercise price per share£e.g. 10.1234",
    "10.AMV of a share at date of exercise£e.g. 10.1234",
    "11.Are the shares subject to the option exercised listed on a recognised stock exchange?(yes/no)If yes go to question 14If no go to next question 12",
    "12.If no, was the market value agreed with HMRC?(yes/no)",
    "13.If yes, enter the HMRC reference given",
    "14.Total amount paid for shares£e.g. 10.1234",
    "15.Were all shares resulting from the exercise sold? (yes/no). Answer yes if they were either sold on the same day as the exercise in connection with the exercise or sale instructions were given for all shares to be sold on exercise"
  )//.map(_.replaceAll(headerFormat,""))

  val emiTaxableHeaderRow = List(
    "1.Date option exercised(yyyy-mm-dd)",
    "2.Is this as a result of a disqualifying event? (yes/no)",
    "3.If yes, enter a number from 1 to 8 depending on the nature of the disqualifying event.Follow the link at cell A7 for a list of disqualifying events",
    "4.Employee first name",
    "5.Employee second name(if applicable)",
    "6.Employee last name",
    "7.National Insurance number(if applicable)"	,
    "8.PAYE reference",
    "9.Total number of shares employee entitled to on exercise of the option before any cashless exercise or other adjustmente.g. 100.00",
    "10.Actual market value (AMV) of a share at date of grant£e.g. 10.1234",
    "11.Exercise price per share£e.g. 10.1234",
    "12.AMV of a share at date of exercise£e.g. 10.1234",
    "13.Unrestricted market value of a share at date of exercise£e.g. 10.1234",
    "14.Total amount paid for the shares£e.g. 10.1234",
    "15.Is the company listed on a recognised stock exchange? (yes/no)If no go to question 16If yes go to question 18",
    "16.Has the market value been agreed with HMRC?(yes/no)",
    "17.If yes, enter the HMRC reference given",
    "18.Has an election under Section 431(1) been made to disregard restrictions?(yes/ no)",
    "19.Has a National Insurance Contribution election or agreement been operated (yes/no)",
    "20.Amount subjected to PAYE£e.g. 10.1234"
  )//.map(_.replaceAll(headerFormat,""))

}
