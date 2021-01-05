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

package services.headers

/**
 * Created by matt on 12/02/16.
 */
trait SAYEHeaders {

  /**SAYE Granted*/
  val sayeXMLHeaderSheet1 = <table:table-row table:style-name="ro5"><table:table-cell office:value-type="string" table:style-name="ce5"><text:p>1.</text:p><text:p>Date of grant</text:p><text:p>(yyyy-mm-dd)</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce5"><text:p>2.</text:p><text:p>Number of individuals granted options</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce5"><text:p>3.</text:p><text:p>Over how many shares in total were options granted?</text:p><text:p>e.g. 100.00</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce5"><text:p>4.</text:p><text:p>Unrestricted market value (UMV) of each share used to determine option exercise price</text:p><text:p>£</text:p><text:p>e.g. 10.1234</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce5"><text:p>5.</text:p><text:p>Option exercise price per share</text:p><text:p>£</text:p><text:p>e.g. 10.1234</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce5"><text:p>6.</text:p><text:p>Are the shares listed on a recognised stock exchange? (yes/no)</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce5"><text:p>7.</text:p><text:p>If no, was the market value agreed with HMRC?</text:p><text:p>(yes/no)</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce5"><text:p>8.</text:p><text:p>If yes enter the HMRC reference given</text:p></table:table-cell><table:table-cell table:number-columns-repeated="994" table:style-name="ce9"/><table:table-cell table:number-columns-repeated="4" table:style-name="ce10"/><table:table-cell table:style-name="ce9"/><table:table-cell table:style-name="ce10"/><table:table-cell table:number-columns-repeated="15376"/></table:table-row>
  val sayeHeaderSheet1Data = List(
    "1.Date of grant(yyyy-mm-dd)",
    "2.Number of individuals granted options",
    "3.Over how many shares in total were options granted?e.g. 100.00",
    "4.Unrestricted market value (UMV) of each share used to determine option exercise price£e.g. 10.1234",
    "5.Option exercise price per share£e.g. 10.1234",
    "6.Are the shares listed on a recognised stock exchange? (yes/no)",
    "7.If no, was the market value agreed with HMRC?(yes/no)",
    "8.If yes enter the HMRC reference given"
    )

  /**SAYE RCL*/
  val sayeXMLHeaderSheet2 = <table:table-row table:style-name="ro7"><table:table-cell office:value-type="string" table:style-name="ce5"><text:p>1.</text:p><text:p>Date of event</text:p><text:p>(yyyy-mm-dd)</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce5"><text:p>2.</text:p><text:p>Was money or value received by the option holder or anyone else when the option was released, exchanged, cancelled or lapsed?</text:p><text:p>(yes/no)</text:p><text:p>If yes go to question 3, otherwise no more information is needed for this event.</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce5"><text:p>3.</text:p><text:p>If yes, amount or value</text:p><text:p>£</text:p><text:p>e.g. 10.1234</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce5"><text:p>4.</text:p><text:p>Employee first name</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce5"><text:p>5.</text:p><text:p>Employee second name</text:p><text:p>(if applicable)</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce5"><text:p>6.</text:p><text:p>Employee last name</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce5"><text:p>7.</text:p><text:p>National Insurance number</text:p><text:p>(if applicable)</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce5"><text:p>8.</text:p><text:p>PAYE reference</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce5"><text:p>9.</text:p><text:p>Was PAYE operated?</text:p><text:p>(yes/no)</text:p></table:table-cell><table:table-cell table:number-columns-repeated="994" table:style-name="ce9"/><table:table-cell table:number-columns-repeated="5" table:style-name="ce10"/><table:table-cell table:number-columns-repeated="15376"/></table:table-row>
  val sayeHeaderSheet2Data = List(
    "1.Date of event(yyyy-mm-dd)",
    "2.Was money or value received by the option holder or anyone else when the option was released, exchanged, cancelled or lapsed?(yes/no)If yes go to question 3, otherwise no more information is needed for this event.",
    "3.If yes, amount or value£e.g. 10.1234",
    "4.Employee first name",
    "5.Employee second name(if applicable)",
    "6.Employee last name",
    "7.National Insurance number(if applicable)",
    "8.PAYE reference",
    "9.Was PAYE operated?(yes/no)"

  )

  /**SAYE Exercised*/
  val sayeXMLHeaderSheet3 = <table:table-row table:style-name="ro10"><table:table-cell office:value-type="string" table:style-name="ce5"><text:p>1.</text:p><text:p>Date of event</text:p><text:p>(yyyy-mm-dd)</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce5"><text:p>2.</text:p><text:p>Employee first name</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce5"><text:p>3.</text:p><text:p>Employee second name</text:p><text:p>(if applicable)</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce5"><text:p>4.</text:p><text:p>Employee last name</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce5"><text:p>5.</text:p><text:p>National Insurance number</text:p><text:p>(if applicable)</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce5"><text:p>6.</text:p><text:p>PAYE reference of employing company</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce5"><text:p>7.</text:p><text:p>Date of grant</text:p><text:p>(yyyy-mm-dd)</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce5"><text:p>8.</text:p><text:p>Total number of shares employee entitled to on exercise of the option e.g. 100.00</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce5"><text:p>9.</text:p><text:p>Were the shares subject to the option listed on a recognised stock exchange?</text:p><text:p>(yes/no)</text:p><text:p>If yes go to question 12</text:p><text:p>If no go to question 10</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce5"><text:p>10.</text:p><text:p>If no, was the market value agreed with HMRC?</text:p><text:p>(yes/no)</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce5"><text:p>11.</text:p><text:p>If yes enter the HMRC reference given</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce5"><text:p>12.</text:p><text:p>Actual market value (AMV) of a share on the date of exercise</text:p><text:p>£</text:p><text:p>e.g. 10.1234</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce5"><text:p>13.</text:p><text:p>Exercise price per share</text:p><text:p>£</text:p><text:p>e.g. 10.1234</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce5"><text:p>14.</text:p><text:p>Unrestricted market value (UMV) of a share on the date of exercise</text:p><text:p>£</text:p><text:p>e.g. 10.1234</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce5"><text:p>15.</text:p><text:p>Does the exercise qualify for tax relief?</text:p><text:p>(yes/no)</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce5"><text:p>16.</text:p><text:p>Were all shares resulting from the exercise sold? (yes/no). Answer yes if they were either sold on the same day as the exercise in connection with the exercise or sale instructions were given for all shares to be sold on exercise</text:p></table:table-cell><table:table-cell table:number-columns-repeated="998" table:style-name="ce5"/><table:table-cell table:number-columns-repeated="15370" table:style-name="ce19"/></table:table-row>
  val sayeHeaderSheet3Data = List(
    "1.Date of event(yyyy-mm-dd)",
    "2.Employee first name"	,
    "3.Employee second name(if applicable)",
    "4.Employee last name",
    "5.National Insurance number(if applicable)",
    "6.PAYE reference of employing company",
    "7.Date of grant(yyyy-mm-dd)",
    "8.Total number of shares employee entitled to on exercise of the option e.g. 100.00",
    "9.Were the shares subject to the option listed on a recognised stock exchange?(yes/no)If yes go to question 12If no go to question 10",
    "10.If no, was the market value agreed with HMRC?(yes/no)",
    "11.If yes enter the HMRC reference given",
    "12.Actual market value (AMV) of a share on the date of exercise£e.g. 10.1234",
    "13.Exercise price per share£e.g. 10.1234",
    "14.Unrestricted market value (UMV) of a share on the date of exercise£e.g. 10.1234",
    "15.Does the exercise qualify for tax relief?(yes/no)",
    "16.Were all shares resulting from the exercise sold? (yes/no). Answer yes if they were either sold on the same day as the exercise in connection with the exercise or sale instructions were given for all shares to be sold on exercise"

    )
}
