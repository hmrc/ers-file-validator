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

import services.XMLTestData.{closeTable, openTable}

import java.io.ByteArrayInputStream
import scala.xml.Elem

object SAYEV7XMLTestData {

  val sayeGrantedV7SheetName = "SAYE_Granted_V7"

  val sayeGrantedV7Row1: Elem =
    <table:table-row table:style-name='ro1'>
      <table:table-cell table:style-name='ce1' calcext:value-type='string'>
        <text:p>Save As You Earn (SAYE) annual return - Enter options granted</text:p>
      </table:table-cell> <table:table-cell table:style-name='ce1' table:number-columns-repeated='7'></table:table-cell> <table:table-cell table:style-name='ce22' table:number-columns-repeated='18'></table:table-cell> <table:table-cell table:number-columns-repeated='998'></table:table-cell>
    </table:table-row>

  val sayeGrantedV7Row2: Elem =
    <table:table-row table:style-name='ro2'>
      <table:table-cell table:style-name='ce2' calcext:value-type='string'>
        <text:p>Use this worksheet to tell HMRC about any options granted in the tax year.</text:p>
      </table:table-cell> <table:table-cell table:style-name='ce7' table:number-columns-repeated='7'></table:table-cell> <table:table-cell table:style-name='ce8' table:number-columns-repeated='18'></table:table-cell> <table:table-cell table:number-columns-repeated='998'></table:table-cell>
    </table:table-row>

  val sayeGrantedV7Row3: Elem =
    <table:table-row table:style-name='ro3'>
      <table:table-cell table:style-name='ce3' calcext:value-type='string'>
        <text:p>
          <text:a xlink:href='https://www.gov.uk/government/publications/save-as-you-earn-end-of-year-template' xlink:type='simple'>Check the notes and guidance for completing the Save As You Earn (SAYE) annual return on GOV.UK</text:a>
        </text:p>
      </table:table-cell> <table:table-cell table:style-name='ce8'></table:table-cell> <table:table-cell table:style-name='ce12'></table:table-cell> <table:table-cell table:style-name='ce15' table:number-columns-repeated='2'></table:table-cell> <table:table-cell table:style-name='ce18'></table:table-cell> <table:table-cell table:style-name='ce21'></table:table-cell> <table:table-cell table:style-name='ce8' table:number-columns-repeated='19'></table:table-cell> <table:table-cell table:number-columns-repeated='998'></table:table-cell>
    </table:table-row>

  val sayeGrantedV7Row4: Elem =
    <table:table-row table:style-name='ro4'><table:table-cell table:style-name='ce4' calcext:value-type='string'><text:p>1. Date of grant.</text:p> <text:p></text:p> <text:p>Enter the date in the correct format.</text:p> <text:p></text:p> <text:p>For example, 2023-10-31</text:p> <text:p></text:p></table:table-cell><table:table-cell table:style-name='ce9' calcext:value-type='string'>
      <text:p>2. Number of individuals granted options.</text:p>
    </table:table-cell><table:table-cell table:style-name='ce13' calcext:value-type='string'>
      <text:p>3. Over how many shares in total were options granted?</text:p> <text:p></text:p> <text:p>Enter the total number of shares to 2 decimal places.</text:p> <text:p></text:p> <text:p>For example, 100.00</text:p>
    </table:table-cell><table:table-cell table:style-name='ce16' calcext:value-type='string'>
      <text:p>4. Unrestricted market value (UMV) of each share used to determine option exercise price.</text:p> <text:p></text:p> <text:p>Enter the value in pounds to 4 decimal places.</text:p> <text:p></text:p> <text:p>For example, 10.1234</text:p>
    </table:table-cell><table:table-cell table:style-name='ce16' calcext:value-type='string'>
      <text:p>5. Option exercise price per share.</text:p> <text:p></text:p> <text:p>Enter the price at which the employee was granted the option to 4 decimal places.</text:p> <text:p></text:p> <text:p>For example, 10.1234</text:p>
    </table:table-cell><table:table-cell table:style-name='ce19' calcext:value-type='string'>
      <text:p>6. Are the shares listed on a recognised stock exchange?</text:p> <text:p></text:p> <text:p>Enter yes or no.</text:p> <text:p></text:p>
    </table:table-cell><table:table-cell table:style-name='ce19' calcext:value-type='string'>
      <text:p>7. If no, was the market value agreed with HMRC?</text:p> <text:p></text:p> <text:p>Enter yes or no.</text:p> <text:p></text:p>
    </table:table-cell><table:table-cell table:style-name='ce9' calcext:value-type='string'>
      <text:p>8. If yes, enter the HMRC valuation reference given.</text:p> <text:p></text:p>
    </table:table-cell><table:table-cell table:style-name='ce11' table:number-columns-repeated='18'></table:table-cell><table:table-cell table:number-columns-repeated='998'></table:table-cell>
    </table:table-row>

  val sayeGrantedV7Row5: Elem =
    <table:table-row table:style-name='ro5'><table:table-cell office:date-value='2016-04-14' table:style-name='ce5' calcext:value-type='date'><text:p>2016-04-14</text:p></table:table-cell><table:table-cell table:style-name='ce10' calcext:value-type='float' office:value='123456'><text:p>123456</text:p></table:table-cell><table:table-cell table:style-name='ce10' calcext:value-type='float' office:value='12345678911.23'><text:p>12345678911.23</text:p></table:table-cell><table:table-cell table:style-name='ce10' calcext:value-type='float' office:value='1234.5678'><text:p>1234.5678</text:p></table:table-cell><table:table-cell table:style-name='ce10' calcext:value-type='float' office:value='1234567.1234'><text:p>1234567.1234</text:p></table:table-cell><table:table-cell table:style-name='ce10' calcext:value-type='string' table:number-columns-repeated='2'><text:p>Yes</text:p></table:table-cell><table:table-cell table:style-name='ce10' calcext:value-type='string'><text:p>aa23678</text:p></table:table-cell><table:table-cell table:number-columns-repeated='1016'></table:table-cell></table:table-row>

  val sayeGrantedV7Row6: Elem =
    <table:table-row table:style-name='ro6'>
      <table:table-cell table:number-columns-repeated='1024'></table:table-cell>
    </table:table-row>

  def getSayeGrantedV7TemplateTestData: ByteArrayInputStream = {
    val inputXml = openTable(
      sayeGrantedV7SheetName
    ) + sayeGrantedV7Row1 + sayeGrantedV7Row2 + sayeGrantedV7Row3 + sayeGrantedV7Row4 + sayeGrantedV7Row5 + sayeGrantedV7Row6 + closeTable
    new ByteArrayInputStream(inputXml.getBytes("utf-8"))
  }

}
