/*
 * Copyright 2023 HM Revenue & Customs
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

package services.headers

/**
 * Created by matt on 12/02/16.
 */
trait SIPHeaders {
  val sipXMLHeaderSheet1 = <table:table-row table:style-name="ro5"><table:table-cell office:value-type="string" table:style-name="ce12"><text:p>1.</text:p><text:p>Date of event</text:p><text:p>(yyyy-mm-dd)</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce12"><text:p>2.</text:p><text:p>Number of employees who acquired or were awarded shares</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce12"><text:p>3.</text:p><text:p>Type of shares awarded</text:p><text:p>Enter a number from 1 to 4 depending on the type of share awarded. Follow the link at cell A7 for a list of the types of share which can be awarded</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce12"><text:p>4.</text:p><text:p>If free shares, are performance conditions attached to their award?</text:p><text:p>(yes/no)</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce12"><text:p>5.</text:p><text:p>If matching shares, what is the ratio of matching shares to partnership shares?</text:p><text:p>Enter ratio for example 2:1; 2/1</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce12"><text:p>6.</text:p><text:p>Unrestricted market value (UMV) per share on acquisition or award</text:p><text:p>£</text:p><text:p>e.g. 10.1234</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce12"><text:p>7.</text:p><text:p>Total number of shares acquired or awarded</text:p><text:p>e.g. 100.00</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce12"><text:p>8.</text:p><text:p>Total value of shares acquired or awarded</text:p><text:p>£</text:p><text:p>e.g. 10.1234</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce12"><text:p>9.</text:p><text:p>Total number of employees whose award of free shares during the year exceeded the limit of £3,600</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce12"><text:p>10.</text:p><text:p>Total number of employees whose award of free shares during the year was at or below the limit of £3,600</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce12"><text:p>11.</text:p><text:p>Total number of employees whose award of partnership shares during the year exceeded the limit of £1,800</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce12"><text:p>12.</text:p><text:p>Total number of employees whose award of partnership shares during the year was at or below the limit of £1,800</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce12"><text:p>13.</text:p><text:p>Total number of employees whose award of matching shares during the year exceeded the limit of £3,600</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce12"><text:p>14.</text:p><text:p>Total number of employees whose award of matching shares during the year was at or below the limit of £3,600</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce12"><text:p>15.</text:p><text:p>Are the shares listed on a recognised stock exchange?</text:p><text:p>(yes/no)</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce12"><text:p>16.</text:p><text:p>If no, was the market value agreed with HMRC?</text:p><text:p>(yes/no)</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce12"><text:p>17.</text:p><text:p>If yes, enter the HMRC reference given</text:p></table:table-cell><table:table-cell table:number-columns-repeated="995" table:style-name="ce7"/><table:table-cell table:style-name="ce8"/><table:table-cell table:number-columns-repeated="15371"/></table:table-row>
  val sipXMLHeaderSheet2 = <table:table-row table:style-name="ro7"><table:table-cell office:value-type="string" table:style-name="ce12"><text:p>1.</text:p><text:p>Date of event</text:p><text:p>(yyyy-mm-dd)</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce12"><text:p>2.</text:p><text:p>Employee first name</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce12"><text:p>3.</text:p><text:p>Employee second name</text:p><text:p>(if applicable)</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce12"><text:p>4.</text:p><text:p>Employee last name</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce12"><text:p>5.</text:p><text:p>National Insurance number</text:p><text:p>(if applicable)</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce12"><text:p>6.</text:p><text:p>PAYE reference of the employing company</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce12"><text:p>7.</text:p><text:p>Number of free shares ceasing to be part of the plan</text:p><text:p>e.g. 100.00</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce12"><text:p>8.</text:p><text:p>Number of partnership shares ceasing to be part of the plan</text:p><text:p>e.g. 100.00</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce12"><text:p>9.</text:p><text:p>Number of matching shares ceasing to be part of the plan</text:p><text:p>e.g. 100.00</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce12"><text:p>10.</text:p><text:p>Number of dividend shares ceasing to be part of the plan</text:p><text:p>e.g. 100.00</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce12"><text:p>11.</text:p><text:p>Unrestricted market value per free share at date ceased to be part of the plan</text:p><text:p>£</text:p><text:p>e.g. 10.1234</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce12"><text:p>12.</text:p><text:p>Unrestricted market value per partnership share at date ceased to be part of the plan</text:p><text:p>£</text:p><text:p>e.g. 10.1234</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce12"><text:p>13.</text:p><text:p>Unrestricted market value per matching share at date ceased to be part of the plan</text:p><text:p>£</text:p><text:p>e.g. 10.1234</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce12"><text:p>14.</text:p><text:p>Unrestricted market value per dividend share at date ceased to be part of the plan</text:p><text:p>£</text:p><text:p>e.g. 10.1234</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce12"><text:p>15.</text:p><text:p>Have all the shares been held in the plan for 5 years or more at the date they ceased to be part of the plan?</text:p><text:p>(yes/no)</text:p><text:p>If yes, no more information is needed for this event.</text:p><text:p>If no, go to question 16</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce12"><text:p>16.</text:p><text:p>If no, for other than dividend shares, was PAYE operated? (yes/no)</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce12"><text:p>17.</text:p><text:p>If no, does this withdrawal of shares qualify for tax relief?</text:p><text:p>(yes/no)</text:p></table:table-cell><table:table-cell table:number-columns-repeated="16367" table:style-name="ce12"/></table:table-row>


  /**SIP Awards*/
  val sipHeaderSheet1Data = List(
    "1.Date of event(yyyy-mm-dd)",
    "2.Number of employees who acquired or were awarded shares",
    "3.Type of shares awardedEnter a number from 1 to 4 depending on the type of share awarded. Follow the link at cell A7 for a list of the types of share which can be awarded",
    "4.If free shares, are performance conditions attached to their award?(yes/no)",
    "5.If matching shares, what is the ratio of matching shares to partnership shares?Enter ratio for example 2:1; 2/1",
    "6.Unrestricted market value (UMV) per share on acquisition or award£e.g. 10.1234",
    "7.Total number of shares acquired or awardede.g. 100.00",
    "8.Total value of shares acquired or awarded£e.g. 10.1234"	,
    "9.Total number of employees whose award of free shares during the year exceeded the limit of £3,600",
    "10.Total number of employees whose award of free shares during the year was at or below the limit of £3,600",
    "11.Total number of employees whose award of partnership shares during the year exceeded the limit of £1,800"	,
    "12.Total number of employees whose award of partnership shares during the year was at or below the limit of £1,800"	,
    "13.Total number of employees whose award of matching shares during the year exceeded the limit of £3,600",
    "14.Total number of employees whose award of matching shares during the year was at or below the limit of £3,600"	,
    "15.Are the shares listed on a recognised stock exchange?(yes/no)"	,
    "16.If no, was the market value agreed with HMRC?(yes/no)"	,
   "17.If yes, enter the HMRC reference given"

    )

  /**SIP Out*/
  val sipHeaderSheet2Data = List(
    "1.Date of event(yyyy-mm-dd)",
    "2.Employee first name",
    "3.Employee second name(if applicable)",
    "4.Employee last name",
    "5.National Insurance number(if applicable)",
    "6.PAYE reference of the employing company",
    "7.Number of free shares ceasing to be part of the plane.g. 100.00",
    "8.Number of partnership shares ceasing to be part of the plane.g. 100.00",
    "9.Number of matching shares ceasing to be part of the plane.g. 100.00",
    "10.Number of dividend shares ceasing to be part of the plane.g. 100.00",
    "11.Unrestricted market value per free share at date ceased to be part of the plan£e.g. 10.1234",
    "12.Unrestricted market value per partnership share at date ceased to be part of the plan£e.g. 10.1234",
    "13.Unrestricted market value per matching share at date ceased to be part of the plan£e.g. 10.1234",
    "14.Unrestricted market value per dividend share at date ceased to be part of the plan£e.g. 10.1234",
    "15.Have all the shares been held in the plan for 5 years or more at the date they ceased to be part of the plan?(yes/no)If yes, no more information is needed for this event.If no, go to question 16",
    "16.If no, for other than dividend shares, was PAYE operated? (yes/no)",
    "17.If no, does this withdrawal of shares qualify for tax relief?(yes/no)"

  )
}
