/*
 * Copyright 2019 HM Revenue & Customs
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

trait SipTemplateInfo {

  val sip = "SIP"

  val sipSheet1Name = "SIP_Awards_V3"
  val sipSheet2Name = "SIP_Out_V3"

  val sipSheet1Desc = "SIP template – Share awards"
  val sipSheet1ValConfig = "ers-sip-awards-validation-config"
  val sipSheet2Desc = "SIP template – Shares ceasing to be part of the plan"
  val sipSheet2ValConfig = "ers-sip-out-validation-config"


  val sipAwardsHeaderRow = List(
    "1.Date of event (yyyy-mm-dd)",
    "2.Number of employees who acquired or were awarded shares",
    "3.Type of shares awarded Enter a number from 1 to 4 depending on the type of share awarded. Follow the link at cell A7 for a list of the types of share which can be awarded",
    "4.If free shares, are performance conditions attached to their award? (yes/no)",
    "5.If matching shares, what is the ratio of matching shares to partnership shares? Enter ratio for example 2:1; 2/1",
    "6.Unrestricted market value (UMV) per share on acquisition or award £ e.g. 10.1234",
    "7.Total number of shares acquired or awarded e.g. 100.00",
    "8.Total value of shares acquired or awarded £ e.g. 10.1234",
    "9.Total number of employees whose award of free shares during the year exceeded the limit of £3,600",
    "10.Total number of employees whose award of free shares during the year was at or below the limit of £3,600",
    "11.Total number of employees whose award of partnership shares during the year exceeded the limit of £1,800",
    "12.Total number of employees whose award of partnership shares during the year was at or below the limit of £1,800",
    "13.Total number of employees whose award of matching shares during the year exceeded the limit of £3,600",
    "14.Total number of employees whose award of matching shares during the year was at or below the limit of £3,600",
    "15.Are the shares listed on a recognised stock exchange? (yes/no)",
    "16.If no, was the market value agreed with HMRC? (yes/no)",
    "17.If yes, enter the HMRC reference given"
  )//.map(_.replaceAll(sipHeaderFormat,""))

  val sipOutHeaderRow = List(
    "1.Date of event (yyyy-mm-dd)",
    "2.Employee first name",
    "3.Employee second name (if applicable)",
    "4.Employee last name",
    "5.National Insurance number (if applicable)",
    "6.PAYE reference of the employing company",
    "7.Number of free shares ceasing to be part of the plan e.g. 100.00",
    "8.Number of partnership shares ceasing to be part of the plan e.g. 100.00",
    "9.Number of matching shares ceasing to be part of the plan e.g. 100.00",
    "10.Number of dividend shares ceasing to be part of the plan e.g. 100.00",
    "11.Unrestricted market value per free share at date ceased to be part of the plan £ e.g. 10.1234",
    "12.Unrestricted market value per partnership share at date ceased to be part of the plan £ e.g. 10.1234",
    "13.Unrestricted market value per matching share at date ceased to be part of the plan £ e.g. 10.1234",
    "14.Unrestricted market value per dividend share at date ceased to be part of the plan £ e.g. 10.1234",
    "15.Have all the shares been held in the plan for 5 years or more at the date they ceased to be part of the plan? (yes/no) If yes, no more information is needed for this event.If no, go to question 16",
    "16.If no, for other than dividend shares, was PAYE operated? (yes/no)",
    "17.If no, does this withdrawal of shares qualify for tax relief? (yes/no)"
  )//.map(_.replaceAll(sipHeaderFormat,""))

}
