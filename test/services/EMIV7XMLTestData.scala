/*
 * Copyright 2026 HM Revenue & Customs
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

package services

import services.XMLTestData.{closeTable, emiAdjustmentsXmlRowWithInvalidData, openTable, sheetName}

import java.io.ByteArrayInputStream

object EMIV7XMLTestData {

  val emiAdjustmentsV7SheetName = "EMI40_Adjustments_V7"

  val emiAdjustmentsV7Row1 =
    <table:table-row table:style-name='ro1'><table:table-cell table:style-name='ce1' calcext:value-type='string'><text:p>Enterprise Management Incentives (EMI) annual return template - Enter adjustment of options </text:p></table:table-cell><table:table-cell table:style-name='ce6' table:number-columns-repeated='13'></table:table-cell><table:table-cell table:style-name='ce9' table:number-columns-repeated='20'></table:table-cell><table:table-cell table:number-columns-repeated='990'></table:table-cell></table:table-row>

  val emiAdjustmentsV7Row2 =
    <table:table-row table:style-name='ro1'><table:table-cell table:style-name='ce2' calcext:value-type='string'><text:p>Use this worksheet to tell HMRC about options that have been adjusted in the tax year.</text:p></table:table-cell><table:table-cell table:style-name='ce7' table:number-columns-repeated='13'></table:table-cell><table:table-cell table:style-name='ce9' table:number-columns-repeated='20'></table:table-cell><table:table-cell table:number-columns-repeated='990'></table:table-cell></table:table-row>

  val emiAdjustmentsV7Row3 =
    <table:table-row table:style-name='ro2'><table:table-cell table:style-name='ce3' calcext:value-type='string'><text:p><text:a xlink:href='https://www.gov.uk/government/publications/enterprise-management-incentives-end-of-year-template' xlink:type='simple'>Check the notes and guidance for completing the end of year return for Enterprise Management Incentives schemes on GOV.UK</text:a></text:p></table:table-cell><table:table-cell table:style-name='ce8'></table:table-cell><table:table-cell table:style-name='ce9' table:number-columns-repeated='3'></table:table-cell><table:table-cell table:style-name='ce13' table:number-columns-repeated='2'></table:table-cell><table:table-cell table:style-name='ce13' table:content-validation-name='val1'></table:table-cell><table:table-cell table:style-name='ce16'></table:table-cell><table:table-cell table:style-name='ce16' table:content-validation-name='val1'></table:table-cell><table:table-cell table:style-name='ce18' table:content-validation-name='val1' table:number-columns-repeated='2'></table:table-cell><table:table-cell table:style-name='ce23' table:number-columns-repeated='2'></table:table-cell><table:table-cell table:style-name='ce9' table:number-columns-repeated='20'></table:table-cell><table:table-cell table:number-columns-repeated='990'></table:table-cell></table:table-row>

  val emiAdjustmentsV7Row4 =
    <table:table-row table:style-name='ro3'><table:table-cell table:style-name='ce4' calcext:value-type='string'><text:p>1. Has there been any adjustment of options following a variation in the share capital of the company?</text:p><text:p></text:p><text:p>Enter yes or no.</text:p></table:table-cell><table:table-cell table:style-name='ce4' calcext:value-type='string'><text:p>2. Has there been a change to the description of the shares under option?</text:p><text:p></text:p><text:p>Enter yes or no.</text:p></table:table-cell><table:table-cell table:style-name='ce4' calcext:value-type='string'><text:p>3. Is the adjustment a disqualifying event?</text:p><text:p></text:p><text:p>If it is a disqualifying event, enter yes and go to question 4. </text:p><text:p></text:p><text:p>If it is not a disqualifying event, enter no and go to question 5.</text:p></table:table-cell><table:table-cell table:style-name='ce4' calcext:value-type='string'><text:p>4. If yes, enter a number from 1 to 8 depending on the nature of the disqualifying event.</text:p><text:p></text:p><text:p>Follow the link at cell A3 for a list of disqualifying events.</text:p><text:p></text:p><text:p></text:p></table:table-cell><table:table-cell table:style-name='ce10' calcext:value-type='string'><text:p>5. Date option adjusted.</text:p><text:p></text:p><text:p>Enter the date in the correct format.</text:p><text:p></text:p><text:p>For example, 2023-10-31</text:p></table:table-cell><table:table-cell table:style-name='ce4' calcext:value-type='string'><text:p>6. Employee first name.</text:p></table:table-cell><table:table-cell table:style-name='ce4' calcext:value-type='string'><text:p>7. Employee second name (optional).</text:p><text:p></text:p><text:p></text:p><text:p></text:p></table:table-cell><table:table-cell table:style-name='ce4' calcext:value-type='string'><text:p>8. Employee last name.</text:p></table:table-cell><table:table-cell table:style-name='ce17' calcext:value-type='string'><text:p>9. National Insurance number.</text:p><text:p></text:p><text:p>Enter in the correct format. </text:p><text:p></text:p><text:p>For example, QQ123456C</text:p><text:p></text:p></table:table-cell><table:table-cell table:style-name='ce17' calcext:value-type='string'><text:p>10. PAYE reference of employing company.</text:p></table:table-cell><table:table-cell table:style-name='ce19' calcext:value-type='string'><text:p>11. Exercise price per share under option before adjustment.</text:p><text:p></text:p><text:p>Enter the value in pounds to 4 decimal places.</text:p><text:p></text:p><text:p>For example, 10.1234</text:p><text:p></text:p><text:p></text:p><text:p></text:p></table:table-cell><table:table-cell table:style-name='ce19' calcext:value-type='string'><text:p>12. Number of shares under the option after adjustment.</text:p><text:p></text:p><text:p>Enter the total number of shares to 2 decimal places. </text:p><text:p></text:p><text:p>For example, 100.00</text:p></table:table-cell><table:table-cell table:style-name='ce19' calcext:value-type='string'><text:p>13. Exercise price per share under option after the adjustment.</text:p><text:p></text:p><text:p>Enter the value in pounds to 4 decimal places.</text:p><text:p></text:p><text:p>For example, 10.1234</text:p></table:table-cell><table:table-cell table:style-name='ce19' calcext:value-type='string'><text:p>14. Actual market value of a share at the date of grant.</text:p><text:p></text:p><text:p>Enter the value in pounds to 4 decimal places.</text:p><text:p></text:p><text:p>For example, 10.1234</text:p></table:table-cell><table:table-cell table:style-name='ce15' table:number-columns-repeated='20'></table:table-cell><table:table-cell table:number-columns-repeated='990'></table:table-cell></table:table-row>

  val emiAdjustmentsV7Row5 =
    <table:table-row table:style-name='ro4'><table:table-cell table:style-name='ce5' calcext:value-type='string' table:number-columns-repeated='2'><text:p>no</text:p></table:table-cell><table:table-cell table:style-name='ce5' calcext:value-type='string'><text:p>yes</text:p></table:table-cell><table:table-cell table:style-name='ce5' calcext:value-type='string'><text:p>3</text:p></table:table-cell><table:table-cell office:date-value='2015-12-01' table:style-name='ce11' calcext:value-type='date'><text:p>2015-12-01</text:p></table:table-cell><table:table-cell table:style-name='ce14' calcext:value-type='string'><text:p>John</text:p></table:table-cell><table:table-cell table:style-name='ce15' calcext:value-type='string'><text:p>Barry</text:p></table:table-cell><table:table-cell table:style-name='ce14' calcext:value-type='string'><text:p>Doe</text:p></table:table-cell><table:table-cell table:style-name='ce14' calcext:value-type='string'><text:p>AA123456A</text:p></table:table-cell><table:table-cell table:style-name='ce14' calcext:value-type='string'><text:p>123/XZ55555555</text:p></table:table-cell><table:table-cell table:style-name='ce20' calcext:value-type='float' office:value='10.1234'><text:p>10.1234</text:p></table:table-cell><table:table-cell table:style-name='ce22' calcext:value-type='float' office:value='100.12'><text:p>100.12</text:p></table:table-cell><table:table-cell table:style-name='ce20' calcext:value-type='float' office:value='10.1234' table:number-columns-repeated='2'><text:p>10.1234</text:p></table:table-cell><table:table-cell table:number-columns-repeated='1010'></table:table-cell></table:table-row>

  val emiAdjustmentsV7Row6 =
    <table:table-row table:style-name='ro4' table:number-rows-repeated='995'><table:table-cell table:style-name='ce5' table:number-columns-repeated='4'></table:table-cell><table:table-cell table:style-name='ce12'></table:table-cell><table:table-cell table:style-name='ce5' table:number-columns-repeated='3'></table:table-cell><table:table-cell table:style-name='ce15' table:number-columns-repeated='2'></table:table-cell><table:table-cell table:style-name='ce21' table:number-columns-repeated='4'></table:table-cell><table:table-cell table:style-name='ce15' table:number-columns-repeated='20'></table:table-cell><table:table-cell table:number-columns-repeated='990'></table:table-cell></table:table-row>

  val emiAdjustmentsV7Row7 =
    <table:table-row table:style-name='ro5' table:number-rows-repeated='1047575'><table:table-cell table:number-columns-repeated='1024'></table:table-cell></table:table-row>

  val emiAdjustmentsV7Row8 =
    <table:table-row table:style-name='ro5'><table:table-cell table:number-columns-repeated='1024'></table:table-cell></table:table-row>

  def getEMIAdjustmentsV7TemplateTestData: ByteArrayInputStream = {
    val inputXml = openTable(emiAdjustmentsV7SheetName) + emiAdjustmentsV7Row1 + emiAdjustmentsV7Row2 +
      emiAdjustmentsV7Row3 + emiAdjustmentsV7Row4 + emiAdjustmentsV7Row5 + emiAdjustmentsV7Row6 + emiAdjustmentsV7Row7 + emiAdjustmentsV7Row8 + closeTable
    new ByteArrayInputStream(inputXml.getBytes("utf-8"))
  }

}
