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

package services.headers

/**
 * Created by matt on 12/02/16.
 */
trait CSOPHeaders {
  //OptionsGranted
  val csopXMLHeaderSheet1 = <table:table-row table:style-name="ro5"><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>1.</text:p><text:p>Date of grant</text:p><text:p>(yyyy-mm-dd)</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>2.</text:p><text:p>Number of employees granted options</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>3.</text:p><text:p>Over how many shares in total were CSOP options granted</text:p><text:p>e.g. 100.00</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>4.</text:p><text:p>Unrestricted market value (UMV) of each share used to determine option exercise price</text:p><text:p>£</text:p><text:p>e.g. 10.1234</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>5.</text:p><text:p>Option exercise price per share</text:p><text:p>£</text:p><text:p>e.g. 10.1234</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>6.</text:p><text:p>Are the shares under the CSOP option listed on a recognised stock exchange?</text:p><text:p>(yes/no)</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>7.</text:p><text:p>If no, was the market value agreed with HMRC?</text:p><text:p>(yes/no)</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>8.</text:p><text:p>If yes enter the HMRC reference given</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>9.</text:p><text:p>Using the UMV at the time of each relevant grant, does any employee hold unexercised CSOP options over shares totalling more than £30k, including this grant?</text:p><text:p>(yes/no)</text:p></table:table-cell><table:table-cell table:number-columns-repeated="1013"/><table:table-cell table:style-name="ce14" table:number-columns-repeated="2"/></table:table-row>
  val csopHeaderSheet1Data = List("1.Date of grant(yyyy-mm-dd)",
    "2.Number of employees granted options",
  "3.Over how many shares in total were CSOP options grantede.g. 100.00",
  "4.Unrestricted market value (UMV) of each share used to determine option exercise price£e.g. 10.1234",
  "5.Option exercise price per share£e.g. 10.1234",
  "6.Are the shares under the CSOP option listed on a recognised stock exchange?(yes/no)",
  "7.If no, was the market value agreed with HMRC?(yes/no)",
  "8.If yes enter the HMRC reference given",
  "9.Using the UMV at the time of each relevant grant, does any employee hold unexercised CSOP options over shares totalling more than £30k, including this grant?(yes/no)"
  )
  //OptionsRCL
  val csopXMLHeaderSheet2 = <table:table-row table:style-name="ro8"><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>1.</text:p><text:p>Date of event</text:p><text:p>(yyyy-mm-dd)</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>2.</text:p><text:p>Was money or value received by the option holder or anyone else when the option was released, exchanged, cancelled or lapsed?</text:p><text:p>(yes/no)</text:p><text:p>If yes go to question 3, otherwise no further information is needed for this event.</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>3.</text:p><text:p>If yes, amount or value</text:p><text:p>£</text:p><text:p>e.g. 10.1234</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>4.</text:p><text:p>Employee first name</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>5.</text:p><text:p>Employee second name</text:p><text:p>(if applicable)</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>6.</text:p><text:p>Employee last name</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>7.</text:p><text:p>National Insurance number</text:p><text:p>(if applicable)</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>8.</text:p><text:p>PAYE reference of employing company</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>9.</text:p><text:p>Was PAYE operated?</text:p><text:p>(yes/no)</text:p></table:table-cell><table:table-cell table:number-columns-repeated="1013"/><table:table-cell table:style-name="ce14" table:number-columns-repeated="2"/></table:table-row>
  val csopHeaderSheet2Data = List("1.Date of event(yyyy-mm-dd)",
    "2.Was money or value received by the option holder or anyone else when the option was released, exchanged, cancelled or lapsed?(yes/no)If yes go to question 3, otherwise no further information is needed for this event.",
    "3.If yes, amount or value£e.g. 10.1234",
    "4.Employee first name",
    "5.Employee second name(if applicable)",
    "6.Employee last name",
    "7.National Insurance number(if applicable)",
    "8.PAYE reference of employing company",
    "9.Was PAYE operated?(yes/no)"
  )
  //OptionsExercised
  val csopXMLHeaderSheet3 = <table:table-row table:style-name="ro12"><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>1.</text:p><text:p>Date of event</text:p><text:p>(yyyy-mm-dd)</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>2.</text:p><text:p>Employee first name</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>3.</text:p><text:p>Employee second name</text:p><text:p>(if applicable)</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>4.</text:p><text:p>Employee last name</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>5.</text:p><text:p>National Insurance number</text:p><text:p>(if applicable)</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>6.</text:p><text:p>PAYE reference of employing company</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>7.</text:p><text:p>Date of grant</text:p><text:p>(yyyy-mm-dd)</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>8.</text:p><text:p>Total number of shares employee entitled to on exercise of the option before any cashless exercise or other adjustment</text:p><text:p>e.g. 100.00</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>9.</text:p><text:p>Are these shares part of the largest class of shares in that company?</text:p><text:p>(yes/no)</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>10.</text:p><text:p>Are the shares subject to the option listed on a recognised stock exchange?</text:p><text:p>(yes/no)</text:p><text:p/></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>11.</text:p><text:p>Actual market value (AMV) of a share on the date of exercise</text:p><text:p>£</text:p><text:p>e.g. 10.1234</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>12.</text:p><text:p>Exercise price per share</text:p><text:p>£</text:p><text:p>e.g. 10.1234</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>13.</text:p><text:p>Unrestricted market value (UMV) of a share on the date of exercise</text:p><text:p>£</text:p><text:p>e.g. 10.1234</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>14.</text:p><text:p>If the answer to question 10 is no, was the market value agreed with HMRC?</text:p><text:p>(yes/no)</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>15.</text:p><text:p>If yes enter the HMRC reference given</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>16.</text:p><text:p>Does the exercise qualify for tax relief?</text:p><text:p>(yes/no)</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>17.</text:p><text:p>Was PAYE operated?</text:p><text:p>(yes/no)</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>18.</text:p><text:p>If yes, deductible amount</text:p><text:p>£</text:p><text:p>e.g. 10.1234</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>19.</text:p><text:p>Has a National Insurance Contributions election or agreement been operated?</text:p><text:p>(yes/no)</text:p></table:table-cell><table:table-cell table:style-name="ce6" office:value-type="string" calcext:value-type="string"><text:p>20.</text:p><text:p>Were all shares resulting from the exercise sold? (yes/no). Answer yes if they were either sold on the same day as the exercise in connection with the exercise or sale instructions were given for all shares to be sold on exercise</text:p></table:table-cell><table:table-cell table:style-name="ce19" table:number-columns-repeated="1004"/></table:table-row>
  val csopHeaderSheet3Data = List(
  "1.Date of event(yyyy-mm-dd)",
  "2.Employee first name"	,
  "3.Employee second name(if applicable)",
  "4.Employee last name",
  "5.National Insurance number(if applicable)"	,
  "6.PAYE reference of employing company"	,
  "7.Date of grant(yyyy-mm-dd)",
  "8.Total number of shares employee entitled to on exercise of the option before any cashless exercise or other adjustmente.g. 100.00",
  "9.Are these shares part of the largest class of shares in that company?(yes/no)",
  "10.Are the shares subject to the option listed on a recognised stock exchange?(yes/no)",
  "11.Actual market value (AMV) of a share on the date of exercise£e.g. 10.1234",
  "12.Exercise price per share£e.g. 10.1234",
  "13.Unrestricted market value (UMV) of a share on the date of exercise£e.g. 10.1234",
  "14.If the answer to question 10 is no, was the market value agreed with HMRC?(yes/no)",
  "15.If yes enter the HMRC reference given",
  "16.Does the exercise qualify for tax relief?(yes/no)",
  "17.Was PAYE operated?(yes/no)",
  "18.If yes, deductible amount£e.g. 10.1234",
  "19.Has a National Insurance Contributions election or agreement been operated?(yes/no)",
  "20.Were all shares resulting from the exercise sold? (yes/no). Answer yes if they were either sold on the same day as the exercise in connection with the exercise or sale instructions were given for all shares to be sold on exercise"
    )
}
