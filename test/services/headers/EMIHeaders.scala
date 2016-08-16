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

package services.headers

/**
 * Created by matt on 12/02/16.
 */
trait EMIHeaders {
  //EMI Adjustments
  val emiXMLHeaderSheet1 = <table:table-row table:style-name="ro5"><table:table-cell office:value-type="string" table:content-validation-name="val1" table:style-name="ce11"><text:p>1.</text:p><text:p>Has there been any adjustment of options following a variation in the share capital of the company?</text:p><text:p>(yes/no)</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce11"><text:p>2.</text:p><text:p>Has there been a change to the description of the shares under option?</text:p><text:p>(yes/no)</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce11"><text:p>3.</text:p><text:p>Is the adjustment a disqualifying event?</text:p><text:p>(yes/no). If YES go to question 4. If NO go to question 5.</text:p></table:table-cell><table:table-cell office:value-type="string" table:content-validation-name="val1" table:style-name="ce11"><text:p>4.</text:p><text:p>If yes, enter a number from 1 to 8 depending on the nature of the disqualifying event.</text:p><text:p>Follow the link at cell A7 for a list of disqualifying events</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce3"><text:p>5.</text:p><text:p>Date option adjusted</text:p><text:p>(yyyy-mm-dd)</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce3"><text:p>6.</text:p><text:p>Employee first name</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce3"><text:p>7.</text:p><text:p>Employee second name</text:p><text:p>(if applicable)</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce3"><text:p>8.</text:p><text:p>Employee last name</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce3"><text:p>9.</text:p><text:p>National Insurance number</text:p><text:p>(if applicable)</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce3"><text:p>10.</text:p><text:p>PAYE reference of employing company</text:p></table:table-cell><table:table-cell office:value-type="string" table:content-validation-name="val1" table:style-name="ce12"><text:p>11.</text:p><text:p>Exercise price per share under option before adjustment</text:p><text:p>£</text:p><text:p>e.g. 10.1234</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce13"><text:p>12.</text:p><text:p>Number of shares under the option after adjustment</text:p><text:p>e.g. 100.00</text:p></table:table-cell><table:table-cell office:value-type="string" table:content-validation-name="val1" table:style-name="ce12"><text:p>13.</text:p><text:p>Exercise price per share under option after the adjustment £</text:p><text:p>e.g. 10.1234</text:p></table:table-cell><table:table-cell office:value-type="string" table:content-validation-name="val1" table:style-name="ce12"><text:p>14.</text:p><text:p>Actual market value of a share at the date of grant</text:p><text:p>£</text:p><text:p>e.g. 10.1234</text:p></table:table-cell><table:table-cell table:number-columns-repeated="16370" table:style-name="ce3"/></table:table-row>
  val emiHeaderSheet1Data = List(
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

  )
  //EMI Replaced
  val emiXMLHeaderSheet2 = <table:table-row table:style-name="ro10"><table:table-cell office:value-type="string" table:content-validation-name="val3" table:style-name="ce3"><text:p>1.</text:p><text:p>Date of grant of old option</text:p><text:p>(yyyy-mm-dd)</text:p></table:table-cell><table:table-cell office:value-type="string" table:content-validation-name="val3" table:style-name="ce34"><text:p>2.</text:p><text:p>Date of grant of new option</text:p><text:p>(yyyy-mm-dd)</text:p></table:table-cell><table:table-cell office:value-type="string" table:content-validation-name="val3" table:style-name="ce3"><text:p>3.</text:p><text:p>Employee first name</text:p></table:table-cell><table:table-cell office:value-type="string" table:content-validation-name="val3" table:style-name="ce3"><text:p>4.</text:p><text:p>Employee second name</text:p><text:p>(if applicable)</text:p></table:table-cell><table:table-cell office:value-type="string" table:content-validation-name="val3" table:style-name="ce3"><text:p>5.</text:p><text:p>Employee last name</text:p></table:table-cell><table:table-cell office:value-type="string" table:content-validation-name="val3" table:style-name="ce3"><text:p>6.</text:p><text:p>National Insurance number</text:p><text:p>(if applicable)</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce3"><text:p>7.</text:p><text:p>PAYE reference of employing company</text:p></table:table-cell><table:table-cell office:value-type="string" table:content-validation-name="val3" table:style-name="ce12"><text:p>8.</text:p><text:p>Actual market value of original shares at the date the option(s) were replaced</text:p><text:p>£</text:p><text:p>e.g. 10.1234</text:p></table:table-cell><table:table-cell office:value-type="string" table:content-validation-name="val3" table:style-name="ce3"><text:p>9.</text:p><text:p>Name of the company whose shares are the subject of the new option</text:p></table:table-cell><table:table-cell office:value-type="string" table:content-validation-name="val3" table:style-name="ce12"><text:p>10.</text:p><text:p>Company address line 1</text:p></table:table-cell><table:table-cell office:value-type="string" table:content-validation-name="val3" table:style-name="ce12"><text:p>11.</text:p><text:p>Company address line 2</text:p></table:table-cell><table:table-cell office:value-type="string" table:content-validation-name="val3" table:style-name="ce12"><text:p>12.</text:p><text:p>Company address line 3</text:p></table:table-cell><table:table-cell office:value-type="string" table:content-validation-name="val3" table:style-name="ce12"><text:p>13.</text:p><text:p>Company address line 4</text:p></table:table-cell><table:table-cell office:value-type="string" table:content-validation-name="val3" table:style-name="ce12"><text:p>14.</text:p><text:p>Country</text:p></table:table-cell><table:table-cell office:value-type="string" table:content-validation-name="val3" table:style-name="ce12"><text:p>15.</text:p><text:p>Postcode</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce3"><text:p>16.</text:p><text:p>Corporation Tax reference</text:p><text:p>(Unique Taxpayer Reference)</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce3"><text:p>17.</text:p><text:p>Company Reference Number (CRN)</text:p></table:table-cell></table:table-row>
  val emiHeaderSheet2Data = List(
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

  )
  //EMI RLC
  val emiXMLHeaderSheet3 = <table:table-row table:style-name="ro12"><table:table-cell office:value-type="string" table:content-validation-name="val6" table:style-name="ce3"><text:p>1.</text:p><text:p>Date of event</text:p><text:p>(yyyy-mm-dd)</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce3"><text:p>2.</text:p><text:p>Is the release, lapse or cancellation the result of a disqualifying event?</text:p><text:p>(yes/no)</text:p></table:table-cell><table:table-cell office:value-type="string" table:content-validation-name="val6" table:style-name="ce3"><text:p>3.</text:p><text:p>If yes, enter a number from 1 to 8 depending on the nature of the disqualifying event.</text:p><text:p>Follow the link at cell A7 for a list of disqualifying events</text:p></table:table-cell><table:table-cell office:value-type="string" table:content-validation-name="val6" table:style-name="ce3"><text:p>4.</text:p><text:p>Employee first name</text:p></table:table-cell><table:table-cell office:value-type="string" table:content-validation-name="val6" table:style-name="ce3"><text:p>5.</text:p><text:p>Employee second name</text:p><text:p>(if applicable)</text:p></table:table-cell><table:table-cell office:value-type="string" table:content-validation-name="val6" table:style-name="ce3"><text:p>6.</text:p><text:p>Employee last name</text:p></table:table-cell><table:table-cell office:value-type="string" table:content-validation-name="val6" table:style-name="ce3"><text:p>7.</text:p><text:p>National Insurance number</text:p><text:p>(if applicable)</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce3"><text:p>8.</text:p><text:p>PAYE reference of employing company</text:p></table:table-cell><table:table-cell office:value-type="string" table:content-validation-name="val6" table:style-name="ce13"><text:p>9.</text:p><text:p>Number of shares subject to the option which have been released, lapsed or cancelled?</text:p><text:p>e.g. 100.00</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce3"><text:p>10.</text:p><text:p>Was money or value received?</text:p><text:p>(yes/no)</text:p><text:p>If yes go to question 11, otherwise no more information is needed for this event.</text:p></table:table-cell><table:table-cell office:value-type="string" table:content-validation-name="val6" table:style-name="ce12"><text:p>11.</text:p><text:p>If yes enter the amount</text:p><text:p>£</text:p><text:p>e.g. 10.1234</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce3"><text:p>12.</text:p><text:p>PAYE operated?</text:p><text:p>(yes/no)</text:p></table:table-cell></table:table-row>
  val emiHeaderSheet3Data = List(
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

  )
  //EMI NonTaxable
  val emiXMLHeaderSheet4 = <table:table-row table:style-name="ro15"><table:table-cell office:value-type="string" table:content-validation-name="val8" table:style-name="ce3"><text:p>1.</text:p><text:p>Date of option exercise</text:p><text:p>(yyyy-mm-dd)</text:p></table:table-cell><table:table-cell office:value-type="string" table:content-validation-name="val8" table:style-name="ce3"><text:p>2.</text:p><text:p>Employee first name</text:p></table:table-cell><table:table-cell office:value-type="string" table:content-validation-name="val8" table:style-name="ce3"><text:p>3.</text:p><text:p>Employee second name</text:p><text:p>(if applicable)</text:p></table:table-cell><table:table-cell office:value-type="string" table:content-validation-name="val8" table:style-name="ce3"><text:p>4.</text:p><text:p>Employee last name</text:p></table:table-cell><table:table-cell office:value-type="string" table:content-validation-name="val8" table:style-name="ce3"><text:p>5.</text:p><text:p>National Insurance number</text:p><text:p>(if applicable)</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce3"><text:p>6.</text:p><text:p>PAYE reference of employing company</text:p></table:table-cell><table:table-cell office:value-type="string" table:content-validation-name="val8" table:style-name="ce3"><text:p>7.</text:p><text:p>Total number of shares employee entitled to on exercise of the option before any cashless exercise or other adjustment</text:p><text:p>e.g. 100.00</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce3"><text:p>8.</text:p><text:p>Actual market value (AMV) of a share at the date of grant of the options exercised</text:p><text:p>£</text:p><text:p>e.g. 10.1234</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce3"><text:p>9.</text:p><text:p>Exercise price per share</text:p><text:p>£</text:p><text:p>e.g. 10.1234</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce3"><text:p>10.</text:p><text:p>AMV of a share at date of exercise</text:p><text:p>£</text:p><text:p>e.g. 10.1234</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce3"><text:p>11.</text:p><text:p>Are the shares subject to the option exercised listed on a recognised stock exchange?</text:p><text:p>(yes/no)</text:p><text:p>If yes go to question 14</text:p><text:p>If no go to next question 12</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce3"><text:p>12.</text:p><text:p>If no, was the market value agreed with HMRC?</text:p><text:p>(yes/no)</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce3"><text:p>13.</text:p><text:p>If yes, enter the HMRC reference given</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce3"><text:p>14.</text:p><text:p>Total amount paid for shares</text:p><text:p>£</text:p><text:p>e.g. 10.1234</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce3"><text:p>15.</text:p><text:p>Were all shares resulting from the exercise sold? (yes/no). Answer yes if they were either sold on the same day as the exercise in connection with the exercise or sale instructions were given for all shares to be sold on exercise</text:p></table:table-cell></table:table-row>
  val emiHeaderSheet4Data = List(
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

    )
  //EMI Taxable
  val emiXMLHeaderSheet5 = <table:table-row table:style-name="ro18"><table:table-cell office:value-type="string" table:content-validation-name="val11" table:style-name="ce3"><text:p>1.</text:p><text:p>Date option exercised</text:p><text:p>(yyyy-mm-dd)</text:p></table:table-cell><table:table-cell office:value-type="string" table:content-validation-name="val11" table:style-name="ce3"><text:p>2.</text:p><text:p>Is this as a result of a disqualifying event? (yes/no)</text:p></table:table-cell><table:table-cell office:value-type="string" table:content-validation-name="val11" table:style-name="ce3"><text:p>3.</text:p><text:p>If yes, enter a number from 1 to 8 depending on the nature of the disqualifying event.</text:p><text:p>Follow the link at cell A7 for a list of disqualifying events</text:p></table:table-cell><table:table-cell office:value-type="string" table:content-validation-name="val11" table:style-name="ce3"><text:p>4.</text:p><text:p>Employee first name</text:p></table:table-cell><table:table-cell office:value-type="string" table:content-validation-name="val11" table:style-name="ce3"><text:p>5.</text:p><text:p>Employee second name</text:p><text:p>(if applicable)</text:p></table:table-cell><table:table-cell office:value-type="string" table:content-validation-name="val11" table:style-name="ce3"><text:p>6.</text:p><text:p>Employee last name</text:p></table:table-cell><table:table-cell office:value-type="string" table:content-validation-name="val11" table:style-name="ce3"><text:p>7.</text:p><text:p>National Insurance number</text:p><text:p>(if applicable)</text:p></table:table-cell><table:table-cell office:value-type="string" table:content-validation-name="val11" table:style-name="ce3"><text:p>8.</text:p><text:p>PAYE reference</text:p></table:table-cell><table:table-cell office:value-type="string" table:content-validation-name="val11" table:style-name="ce3"><text:p>9.</text:p><text:p>Total number of shares employee entitled to on exercise of the option before any cashless exercise or other adjustment</text:p><text:p>e.g. 100.00</text:p></table:table-cell><table:table-cell office:value-type="string" table:content-validation-name="val11" table:style-name="ce3"><text:p>10.</text:p><text:p>Actual market value (AMV) of a share at date of grant</text:p><text:p>£</text:p><text:p>e.g. 10.1234</text:p></table:table-cell><table:table-cell office:value-type="string" table:content-validation-name="val11" table:style-name="ce3"><text:p>11.</text:p><text:p>Exercise price per share</text:p><text:p>£</text:p><text:p>e.g. 10.1234</text:p></table:table-cell><table:table-cell office:value-type="string" table:content-validation-name="val11" table:style-name="ce3"><text:p>12.</text:p><text:p>AMV of a share at date of exercise</text:p><text:p>£</text:p><text:p>e.g. 10.1234</text:p></table:table-cell><table:table-cell office:value-type="string" table:content-validation-name="val11" table:style-name="ce3"><text:p>13.</text:p><text:p>Unrestricted market value of a share at date of exercise</text:p><text:p>£</text:p><text:p>e.g. 10.1234</text:p></table:table-cell><table:table-cell office:value-type="string" table:content-validation-name="val11" table:style-name="ce3"><text:p>14.</text:p><text:p>Total amount paid for the shares</text:p><text:p>£</text:p><text:p>e.g. 10.1234</text:p></table:table-cell><table:table-cell office:value-type="string" table:content-validation-name="val11" table:style-name="ce3"><text:p>15.</text:p><text:p>Is the company listed on a recognised stock exchange? (yes/no)</text:p><text:p>If no go to question 16</text:p><text:p>If yes go to question 18</text:p><text:p/></table:table-cell><table:table-cell office:value-type="string" table:content-validation-name="val11" table:style-name="ce3"><text:p>16.</text:p><text:p>Has the market value been agreed with HMRC?</text:p><text:p>(yes/no)</text:p></table:table-cell><table:table-cell office:value-type="string" table:content-validation-name="val11" table:style-name="ce3"><text:p>17.</text:p><text:p>If yes, enter the HMRC reference given</text:p></table:table-cell><table:table-cell office:value-type="string" table:content-validation-name="val11" table:style-name="ce3"><text:p>18.</text:p><text:p>Has an election under Section 431(1) been made to disregard restrictions?</text:p><text:p>(yes/ no)</text:p></table:table-cell><table:table-cell office:value-type="string" table:content-validation-name="val11" table:style-name="ce3"><text:p>19.</text:p><text:p>Has a National Insurance Contribution election or agreement been operated (yes/no)</text:p></table:table-cell><table:table-cell office:value-type="string" table:content-validation-name="val11" table:style-name="ce3"><text:p>20.</text:p><text:p>Amount subjected to PAYE</text:p><text:p>£</text:p><text:p>e.g. 10.1234</text:p></table:table-cell></table:table-row>
  val emiHeaderSheet5Data = List(
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

  )

}
