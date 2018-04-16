/*
 * Copyright 2018 HM Revenue & Customs
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

trait OtherTemplateInfo {

  val other="OTHER"

  val otherSheet1Name = "Other_Grants_V3"
  val otherSheet2Name = "Other_Options_V3"
  val otherSheet3Name = "Other_Acquisition_V3"
  val otherSheet4Name = "Other_RestrictedSecurities_V3"
  val otherSheet5Name = "Other_OtherBenefits_V3"
  val otherSheet6Name = "Other_Convertible_V3"
  val otherSheet7Name = "Other_Notional_V3"
  val otherSheet8Name = "Other_Enhancement_V3"
  val otherSheet9Name = "Other_Sold_V3"


  val otherSheet1Desc = "Other ERS schemes or arrangements – Grant of securities options, including Restricted Stock Units (RSUs)"
  val otherSheet1ValConfig = "ers-other-grants-validation-config"
  val otherSheet2Desc = "Other ERS schemes or arrangements – Other option events, including conditional awards.  Options lapsed for nil consideration should not be included"
  val otherSheet2ValConfig = "ers-other-options-validation-config"
  val otherSheet3Desc = "Other ERS schemes or arrangements – Acquisition of securities"
  val otherSheet3ValConfig = "ers-other-acquisition-validation-config"
  val otherSheet4Desc = "Other ERS schemes or arrangements – Restricted securities post acquisition events"
  val otherSheet4ValConfig = "ers-other-restrictedsecurities-validation-config"
  val otherSheet5Desc = "Other ERS schemes or arrangements – Receipt of other benefits from securities post acquisition"
  val otherSheet5ValConfig = "ers-other-other-benefits-validation-config"
  val otherSheet6Desc = "Other ERS schemes or arrangements – Convertible securities post-acquisition"
  val otherSheet6ValConfig = "ers-other-convertible-validation-config"
  val otherSheet7Desc = "Other ERS schemes or arrangements – Discharge of notional loans post-acquisition"
  val otherSheet7ValConfig = "ers-other-notional-validation-config"
  val otherSheet8Desc = "Other ERS schemes or arrangements – Artificial enhancement of market value. Value of securities post acquisition"
  val otherSheet8ValConfig = "ers-other-enhancement-validation-config"
  val otherSheet9Desc = "Other ERS schemes and arrangements – Securities sold for more than market value post acquisition"
  val otherSheet9ValConfig = "ers-other-sold-validation-config"

  val otherGrantsHeaderRow = List(
    "1.Date of grant (yyyy-mm-dd)",
    "2.Number of employees granted options",
    "3.Unrestricted market value of a security at date of grant £ e.g. 10.1234",
    "4.Number of securities over which options grantede.g. 100.00"
  )//.map(_.replaceAll(otherHeaderFormat,""))


  val otherOptionsHeaderRow = List(
    "1.Date of event yyyy-mm-dd",
    "2.Is the event in relation to a disclosable tax avoidance scheme? (yes/no)",
    "3.If yes, enter the eight-digit scheme reference number (SRN)",
    "4.Employee first name",
    "5.Employee second name (if applicable)",
    "6.Employee last name",
    "7.National Insurance Number (if applicable)",
    "8.PAYE reference of employing company",
    "9.Date of grant of option subject to the reportable event yyyy-mm-dd",
    "10.Grantor company name",
    "11.Grantor company address line 1",
    "12.Grantor company address line 2",
    "13.Grantor company address line 3",
    "14.Grantor company address line 4",
    "15.Grantor company country",
    "16.Grantor company postcode",
    "17.Grantor Company Registration Number (CRN) , if applicable",
    "18.Grantor company Corporation Tax reference, if applicable",
    "19.Grantor company PAYE reference",
    "20.Name of the company whose securities under option",
    "21.Company whose securities under option – Address line 1",
    "22.Company whose securities under option – Address line 2",
    "23.Company whose securities under option – Address line 3",
    "24.Company whose securities under option – Address line 4",
    "25.Company whose securities under option – Country",
    "26.Company whose securities under option – Postcode",
    "27.Company Reference Number (CRN) of company whose securities under option",
    "28.Corporation Tax reference of company whose securities under option",
    "29.PAYE reference of company whose securities under option",
    "30.Were the options exercised? (yes/no).If yes go to next question If no go to question 38",
    "31.Total number of securities employee entitled to on exercise of the option before any cashless exercise or other adjustment e.g. 100.00",
    "32.If consideration was given for the securities, the amount given per security £ e.g. 10.1234",
    "33.If securities were acquired, Market Value (see note in guidance) of a security on the date of acquisition £ e.g. 10.1234",
    "34.If shares were acquired, are the shares listed on a recognised stock exchange? (yes/no).If yes go to question 37 If no go to next question",
    "35.If shares were not listed on a recognised stock exchange, was valuation agreed with HMRC? (yes/no)",
    "36.If yes, enter the HMRC reference given",
    "37.If the shares were acquired, total deductible amount excluding any consideration given for the securities £ e.g. 10.1234. Then go to question 40",
    "38.If securities were not acquired, was money or value received on the release, assignment, cancellation or lapse of the option? (yes/no) If yes go to next question If no, no further information required on this event.",
    "39.If yes, amount of money or value received £ e.g. 10.1234"	,
    "40.Was a NICs election or agreement operated? (yes/no)",
    "41.Was PAYE operated? (yes/no)",
    "42.Was any adjustment made for amounts subject to apportionment for residence or duties outside the UK (yes/no)"
  )//.map(_.replaceAll(otherHeaderFormat,""))

  val otherAcquisitionHeaderRow = List(
    "1.Date of event (yyyy-mm-dd)",
    "2.Is the event in relation to a disclosable tax avoidance scheme? (yes/no)",
    "3.If yes enter the eight-digit scheme reference number (SRN)",
    "4.Employee first name",
    "5.Employee second name (if applicable)",
    "6.Employee last name",
    "7.National Insurance number (if applicable)",
    "8.PAYE reference of employing company",
    "9.Name of the company whose securities acquired",
    "10.Company whose securities acquired – Address line 1",
    "11.Company whose securities acquired – Address line 2",
    "12.Company whose securities acquired – Address line 3",
    "13.Company whose securities acquired – Address line 4",
    "14.Company whose securities acquired – Country",
    "15.Company whose securities acquired – Postcode",
    "16.Company Reference Number (CRN) of company whose securities acquired",
    "17.Corporation Tax reference of company whose securities acquired",
    "18.PAYE reference of company whose securities acquired",
    "19.Description of security. Enter a number from 1 to 9. Follow the link in cell A7 for a list of security types",
    "20.If the securities are not shares enter ' no' and go to question 24 If the securities are shares, are they part of the largest class of shares in the company? (yes/no)",
    "21.If the securities are shares, are they listed on a recognised stock exchange? (yes/no) If no go to question 22, If yes go to question 24",
    "22.If shares were not listed on a recognised stock exchange, was valuation agreed with HMRC? (yes/no)",
    "23.If yes, enter the HMRC reference given",
    "24.Number of securities acquired e.g. 100.00",
    "25.Security type. Enter a number from 1 to 3, (follow the link at cell A7 for a list of security types). If restricted go to next question. If convertible go to question 32.If both restricted and convertible enter 1 and answer all questions 26 to 32. If neither restricted nor convertible go to question 29.",
    "26.If restricted, nature of restriction. Enter a number from 1-3, follow the link at cell A7 for a list of restrictions",
    "27.If restricted, length of time of restriction in years (if less than a whole year, enter as a decimal fraction, for example 0.6)",
    "28.If restricted, actual market value per security at date of acquisition £ e.g. 10.1234 (no entry should be made if an election to disregard ALL restrictions is operated)",
    "29.Unrestricted market value per security at date of acquisition £ e.g. 10.1234",
    "30.If restricted, has an election been operated to disregard restrictions? (yes/no)",
    "31.If an election has been operated to disregard restrictions, have all or some been disregarded? (enter all or some)",
    "32.If convertible, market value per security ignoring conversion rights £ e.g. 10.1234",
    "33.Total price paid for the securities £ e.g. 10.1234",
    "34.Was the price paid in pounds sterling? (yes/no)",
    "35.Was there an artificial reduction in value on acquisition? (yes/no) If 'yes' go to question 36, if 'No' go to question 37",
    "36.If there was an artificial reduction in value, nature of the artificial reduction Enter a number from 1 to 3. Follow the link in cell A7 for a list of types of artificial restriction",
    "37.Were shares acquired under an employee shareholder arrangement? (yes/no)",
    "38.If shares were acquired under an employee shareholder arrangement, was the total actual market value (AMV) of shares £2,000 or more? (yes/no)",
    "39.Was PAYE operated? (yes/no)",
    "40.Was any adjustment made for amounts subject to apportionment for residence or duties outside the UK (yes/no)"
  )//.map(_.replaceAll(otherHeaderFormat,""))

  val otherRestrictedSecuritiesHeaderRow = List(
    "1.Date of event (yyyy-mm-dd)",
    "2.Is the event in relation to a disclosable tax avoidance scheme? (yes/no)",
    "3.If yes, enter the eight-digit scheme reference number (SRN)",
    "4.Employee first name",
    "5.Employee second name (if applicable)",
    "6.Employee last name",
    "7.National Insurance Number (if applicable)",
    "8.PAYE reference of employing company",
    "9.Date securities originally acquired (yyyy-mm-dd)",
    "10.Number of securities originally acquired e.g. 100.00",
    "11.For disposals or lifting of restrictions, total chargeable amount £ e.g. 10.1234",
    "12.For lifting of restrictions, are the shares listed on a recognised stock exchange? (yes/no)",
    "13.If shares were not listed on a recognised stock exchange, was valuation agreed with HMRC? (yes/no)",
    "14.If yes, enter the HMRC reference given",
    "15.For variations, date of variation (yyyy-mm-dd)",
    "16.For variations, Actual Market Value (AMV) per security directly before variation £ e.g. 10.1234",
    "17.For variations, Actual Market Value (AMV) per security directly after variation £ e.g. 10.1234",
    "18.Has a National Insurance Contribution election or agreement been operated (yes/no)",
    "19.Was PAYE operated? (yes/no)",
    "20.Was any adjustment made for amounts subject to apportionment for residence or duties outside the UK (yes/no)"
  )//.map(_.replaceAll(otherHeaderFormat,""))

  val otherBenefitsHeaderRow = List(
    "1.Date of event (yyyy-mm-dd)",
    "2.Is the event in relation to a disclosable tax avoidance scheme? (yes/no)",
    "3.If yes enter the eight-digit scheme reference number (SRN)",
    "4.Employee first name",
    "5.Employee second name (if applicable)",
    "6.Employee last name",
    "7.National Insurance number (if applicable)",
    "8.PAYE reference of employing company",
    "9.Date securities originally acquired (yyyy-mm-dd)",
    "10.Number of securities originally acquired e.g. 100.00",
    "11.Amount or market value of the benefit £ e.g. 10.1234",
    "12.Was PAYE operated? (yes/no)",
    "13.Was any adjustment made for amounts subject to apportionment for residence or duties outside the UK (yes/no)"
  )//.map(_.replaceAll(otherHeaderFormat,""))

  val otherConvertibleHeaderRow = List(
    "1.Date of event (yyyy-mm-dd)",
    "2.Is the event in relation to a disclosable tax avoidance scheme? (yes/no)",
    "3.If yes, enter the eight-digit scheme reference number (SRN)",
    "4.Employee first name",
    "5.Employee second name (if applicable)",
    "6.Employee last name",
    "7.National Insurance number (if applicable)",
    "8.PAYE reference of employing company",
    "9.Date securities originally acquired (yyyy-mm-dd)",
    "10.Number of securities originally acquired e.g. 100.00",
    "11.For receipt of money or value, enter amount or market value of the benefit £ e.g. 10.1234 Then go to question 14",
    "12.For conversion, disposal or release of entitlement to convert, total chargeable amount £ e.g. 10.1234",
    "13.Has a National Insurance Contribution election or agreement been operated (yes/no)",
    "14.Was PAYE operated? (yes/no)",
    "15.Was any adjustment made for amounts subject to apportionment for residence or duties outside the UK? (yes/no)"
  )//.map(_.replaceAll(otherHeaderFormat,""))

  val otherNotionalHeaderRow = List(
    "1.Date of event (yyyy-mm-dd)",
    "2.Is the event in relation to a disclosable tax avoidance scheme? (yes/no)",
    "3.If yes, enter the eight-digit scheme reference number (SRN)",
    "4.Employee first name",
    "5.Employee second name (if applicable)",
    "6.Employee last name",
    "7.National Insurance number (if applicable)",
    "8.PAYE reference of employing company",
    "9.Date securities originally acquired (yyyy-mm-dd)",
    "10.Number of securities originally acquired e.g 100.00",
    "11.Amount of notional loan discharged £ e.g. 10.1234",
    "12.Was PAYE operated? (yes/no)",
    "13.Was any adjustment made for amounts subject to apportionment for residence or duties outside the UK? (yes/no)"
  )//.map(_.replaceAll(otherHeaderFormat,""))

  val otherEnhancementHeaderRow = List(
    "1.Date of event (yyyy-mm-dd)",
    "2.Is the event in relation to a disclosable tax avoidance scheme? (yes/no)",
    "3.If yes, enter the eight-digit scheme reference number (SRN)",
    "4.Employee first name",
    "5.Employee second name (if applicable)",
    "6.Employee last name",
    "7.National Insurance number (if applicable)",
    "8.PAYE reference of employing company",
    "9.Date securities originally acquired (yyyy-mm-dd)",
    "10.Number of securities originally acquired e.g. 100.00",
    "11.Total unrestricted market value (UMV) on 5th April or date of disposal if earlier £ e.g. 10.1234",
    "12.Total UMV ignoring effect of artificial increase on date of taxable event £ e.g. 10.1234",
    "13.Was PAYE operated? (yes/no)",
    "14.Was any adjustment made for amounts subject to apportionment for residence or duties outside the UK? (yes/no)"
  )//.map(_.replaceAll(otherHeaderFormat,""))

  val otherSoldHeaderRow = List(
    "1.Date of event (yyyy-mm-dd)",
    "2.Is the event in relation to a disclosable tax avoidance scheme? (yes/no)",
    "3.If yes, enter the eight-digit scheme reference number (SRN)",
    "4.Employee first name",
    "5.Employee second name (if applicable)",
    "6.Employee last name",
    "7.National Insurance number (if applicable)",
    "8.PAYE reference of employing company",
    "9.Number of securities originally acquired e.g. 100.00",
    "10.Amount received on disposal £ e.g. 10.1234",
    "11.Total market value on disposal £ e.g. 10.1234",
    "12.Expenses incurred £ e.g. 10.1234",
    "13.Was PAYE operated? (yes/no)",
    "14.Was any adjustment made for amounts subject to apportionment for residence or duties outside the UK? (yes/no)"
  )//.map(_.replaceAll(otherHeaderFormat,""))

}
