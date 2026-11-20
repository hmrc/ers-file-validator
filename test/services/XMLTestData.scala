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

import java.io.ByteArrayInputStream
import scala.xml.Elem

object XMLTestData {

  val emiAdjustmentsXmlRow1: Elem =
    <table:table-row table:style-name="ro6"><table:table-cell table:number-columns-repeated="3" office:value-type="string" calcext:value-type="string"><text:p>yes</text:p></table:table-cell><table:table-cell table:style-name="ce15" office:value-type="string" calcext:value-type="string"><text:p>4</text:p></table:table-cell><table:table-cell table:style-name="ce12" office:value-type="date" office:date-value="1989-10-20" calcext:value-type="date"><text:p>1989-10-20</text:p></table:table-cell><table:table-cell table:style-name="ce15" office:value-type="string" calcext:value-type="string"><text:p>Anthony</text:p></table:table-cell><table:table-cell table:style-name="ce15" office:value-type="string" calcext:value-type="string"><text:p>Joe</text:p></table:table-cell><table:table-cell table:style-name="ce15" office:value-type="string" calcext:value-type="string"><text:p>Jones</text:p></table:table-cell><table:table-cell table:style-name="ce19" office:value-type="string" calcext:value-type="string"><text:p>AA123456A</text:p></table:table-cell><table:table-cell table:style-name="ce19" office:value-type="string" calcext:value-type="string"><text:p>123/XZ55555555</text:p></table:table-cell><table:table-cell table:style-name="ce22" office:value-type="float" office:value="10.1232" calcext:value-type="float"><text:p>10.1232</text:p></table:table-cell><table:table-cell table:style-name="ce25" office:value-type="float" office:value="100" calcext:value-type="float"><text:p>100.00</text:p></table:table-cell><table:table-cell table:style-name="ce22" office:value-type="float" office:value="10.2585" calcext:value-type="float"><text:p>10.2585</text:p></table:table-cell><table:table-cell table:style-name="ce22" office:value-type="float" office:value="10.2544" calcext:value-type="float"><text:p>10.2544</text:p></table:table-cell><table:table-cell table:number-columns-repeated="1008"/><table:table-cell table:style-name="ce28" table:number-columns-repeated="2"/></table:table-row>

  val emiAdjustmentsXmlRowWithAmpersand: Elem =
    <table:table-row table:style-name="ro6"><table:table-cell table:number-columns-repeated="3" office:value-type="string" calcext:value-type="string"><text:p>yes&amp;</text:p></table:table-cell><table:table-cell table:style-name="ce15" office:value-type="string" calcext:value-type="string"><text:p>4</text:p></table:table-cell><table:table-cell table:style-name="ce12" office:value-type="date" office:date-value="1989-10-20" calcext:value-type="date"><text:p>1989-10-20</text:p></table:table-cell><table:table-cell table:style-name="ce15" office:value-type="string" calcext:value-type="string"><text:p>Anthony</text:p></table:table-cell><table:table-cell table:style-name="ce15" office:value-type="string" calcext:value-type="string"><text:p>Joe</text:p></table:table-cell><table:table-cell table:style-name="ce15" office:value-type="string" calcext:value-type="string"><text:p>Jones</text:p></table:table-cell><table:table-cell table:style-name="ce19" office:value-type="string" calcext:value-type="string"><text:p>AA123456A</text:p></table:table-cell><table:table-cell table:style-name="ce19" office:value-type="string" calcext:value-type="string"><text:p>123/XZ55555555</text:p></table:table-cell><table:table-cell table:style-name="ce22" office:value-type="float" office:value="10.1232" calcext:value-type="float"><text:p>10.1232</text:p></table:table-cell><table:table-cell table:style-name="ce25" office:value-type="float" office:value="100" calcext:value-type="float"><text:p>100.00</text:p></table:table-cell><table:table-cell table:style-name="ce22" office:value-type="float" office:value="10.2585" calcext:value-type="float"><text:p>10.2585</text:p></table:table-cell><table:table-cell table:style-name="ce22" office:value-type="float" office:value="10.2544" calcext:value-type="float"><text:p>10.2544</text:p></table:table-cell><table:table-cell table:number-columns-repeated="1008"/><table:table-cell table:style-name="ce28" table:number-columns-repeated="2"/></table:table-row>

  val emiAdjustmentsXmlRowWithInvalidData: Elem =
    <table:table-row table:style-name="ro6"><table:table-cell table:number-columns-repeated="3" office:value-type="string" calcext:value-type="string"><text:p>123</text:p></table:table-cell><table:table-cell table:style-name="ce15" office:value-type="string" calcext:value-type="string"><text:p>canoe</text:p></table:table-cell><table:table-cell table:style-name="ce12" office:value-type="date" office:date-value="1989-10-20" calcext:value-type="date"><text:p>apples</text:p></table:table-cell><table:table-cell table:style-name="ce15" office:value-type="string" calcext:value-type="string"><text:p>Anthony</text:p></table:table-cell><table:table-cell table:style-name="ce15" office:value-type="string" calcext:value-type="string"><text:p>Joe</text:p></table:table-cell><table:table-cell table:style-name="ce15" office:value-type="string" calcext:value-type="string"><text:p>Jones</text:p></table:table-cell><table:table-cell table:style-name="ce19" office:value-type="string" calcext:value-type="string"><text:p>AA123456A</text:p></table:table-cell><table:table-cell table:style-name="ce19" office:value-type="string" calcext:value-type="string"><text:p>123/XZ55555555</text:p></table:table-cell><table:table-cell table:style-name="ce22" office:value-type="float" office:value="10.1232" calcext:value-type="float"><text:p>10.1232</text:p></table:table-cell><table:table-cell table:style-name="ce25" office:value-type="float" office:value="100" calcext:value-type="float"><text:p>100.00</text:p></table:table-cell><table:table-cell table:style-name="ce22" office:value-type="float" office:value="10.2585" calcext:value-type="float"><text:p>10.2585</text:p></table:table-cell><table:table-cell table:style-name="ce22" office:value-type="float" office:value="10.2544" calcext:value-type="float"><text:p>10.2544</text:p></table:table-cell><table:table-cell table:number-columns-repeated="1008"/><table:table-cell table:style-name="ce28" table:number-columns-repeated="2"/></table:table-row>

  def openTable(sheetName: String) = s"""<table:table table:name="$sheetName" table:style-name="ta1">"""
  val closeTable                   = """</table:table>"""

  val sheetName = "EMI40_Adjustments_V4"

  val row1: String =
    <table:table-row table:style-name="ro1"><table:table-cell office:value-type="string" table:number-columns-spanned="14" table:number-rows-spanned="1" table:style-name="ce26"><text:p>EMI template - Adjustment of options</text:p></table:table-cell><table:covered-table-cell table:number-columns-repeated="13"/><table:table-cell table:number-columns-repeated="16370" table:style-name="ce2"/></table:table-row>.toString

  val row2: String =
    <table:table-row table:style-name="ro1"><table:table-cell office:value-type="string" table:number-columns-spanned="14" table:number-rows-spanned="1" table:style-name="ce27"><text:p>How to complete this schedule:</text:p></table:table-cell><table:covered-table-cell table:number-columns-repeated="13"/><table:table-cell table:number-columns-repeated="16370" table:style-name="ce2"/></table:table-row>.toString

  val row3: String =
    <table:table-row table:style-name="ro2"><table:table-cell office:value-type="string" table:number-columns-spanned="14" table:number-rows-spanned="1" table:style-name="ce28"><text:p><text:span text:style-name="T1">ONLY complete this sheet if, following a variation in the share capital of the company whose shares are under option, there has been an adjustment of the option price or there has been a change in the description of shares under option.<text:s/></text:span><text:span text:style-name="T1"/></text:p><text:p><text:span text:style-name="T4">Please note</text:span>: Monetary values must be entered to 4 decimal places in pounds sterling.</text:p><text:p>Numbers of shares and securities must be entered to 2 decimal places in pounds sterling.</text:p></table:table-cell><table:covered-table-cell table:number-columns-repeated="13"/><table:table-cell table:number-columns-repeated="16370" table:style-name="ce2"/></table:table-row>.toString

  val row4: String =
    <table:table-row table:style-name="ro3"><table:table-cell office:value-type="string" table:number-columns-spanned="6" table:number-rows-spanned="1" table:style-name="ce28"><text:p>Please note: Monetary values must be entered to 4 decimal places in pounds sterling.</text:p><text:p>Numbers of shares and securities must be entered to 2 decimal places.</text:p></table:table-cell><table:covered-table-cell table:number-columns-repeated="5"/><table:table-cell table:number-columns-repeated="8" table:style-name="ce3"/><table:table-cell table:number-columns-repeated="16370" table:style-name="ce2"/></table:table-row>.toString

  val row5: String =
    <table:table-row table:style-name="ro4"><table:table-cell office:value-type="string" table:number-columns-spanned="14" table:number-rows-spanned="1" table:style-name="ce28"><text:p><text:span text:style-name="T2">Important note</text:span><text:span text:style-name="T2"/></text:p><text:p>You<text:s/><text:span text:style-name="T4">must not</text:span><text:s/>alter the structure or formatting of this schedule. If you do your schedule will be rejected.</text:p></table:table-cell><table:covered-table-cell table:number-columns-repeated="13"/><table:table-cell table:number-columns-repeated="16370" table:style-name="ce2"/></table:table-row>.toString

  val row6: String =
    <table:table-row table:style-name="ro2"><table:table-cell office:value-type="string" table:number-columns-spanned="12" table:number-rows-spanned="1" table:style-name="ce28"><text:p>For more information on completing this schedule, follow the link below. You must be connected to the internet to access the guide.</text:p></table:table-cell><table:covered-table-cell table:number-columns-repeated="11"/><table:table-cell table:number-columns-repeated="2" table:style-name="ce4"/><table:table-cell table:number-columns-repeated="16370" table:style-name="ce2"/></table:table-row>.toString

  val row7: String =
    <table:table-row table:style-name="ro1"><table:table-cell office:value-type="string" table:style-name="ce5"><text:p><text:a xlink:href="https://www.gov.uk/government/publications/enterprise-management-incentives-end-of-year-template">EMI guidance</text:a></text:p></table:table-cell><table:table-cell table:style-name="ce6"/><table:table-cell table:number-columns-spanned="3" table:number-rows-spanned="1" table:style-name="ce29"/><table:covered-table-cell table:number-columns-repeated="2"/><table:table-cell table:number-columns-repeated="2" table:style-name="ce7"/><table:table-cell table:content-validation-name="val2" table:style-name="ce8"/><table:table-cell table:style-name="ce9"/><table:table-cell table:content-validation-name="val2" table:style-name="ce8"/><table:table-cell table:content-validation-name="val2" table:style-name="ce8"/><table:table-cell table:content-validation-name="val2" table:style-name="ce8"/><table:table-cell table:number-columns-repeated="2" table:style-name="ce4"/><table:table-cell table:number-columns-repeated="16370" table:style-name="ce2"/></table:table-row>.toString

  val row8: String =
    <table:table-row table:style-name="ro1"><table:table-cell table:number-columns-repeated="4" table:style-name="ce10"/><table:table-cell table:number-columns-repeated="16380" table:style-name="ce2"/></table:table-row>.toString

  val row9EMIAdjustmentsHeader: String =
    <table:table-row table:style-name="ro5"><table:table-cell office:value-type="string" table:content-validation-name="val1" table:style-name="ce11"><text:p>1.</text:p><text:p>Has there been any adjustment of options following a variation in the share capital of the company?</text:p><text:p>(yes/no)</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce11"><text:p>2.</text:p><text:p>Has there been a change to the description of the shares under option?</text:p><text:p>(yes/no)</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce11"><text:p>3.</text:p><text:p>Is the adjustment a disqualifying event?</text:p><text:p>(yes/no). If YES go to question 4. If NO go to question 5.</text:p></table:table-cell><table:table-cell office:value-type="string" table:content-validation-name="val1" table:style-name="ce11"><text:p>4.</text:p><text:p>If yes, enter a number from 1 to 8 depending on the nature of the disqualifying event.</text:p><text:p>Follow the link at cell A7 for a list of disqualifying events</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce3"><text:p>5.</text:p><text:p>Date option adjusted</text:p><text:p>(yyyy-mm-dd)</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce3"><text:p>6.</text:p><text:p>Employee first name</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce3"><text:p>7.</text:p><text:p>Employee second name</text:p><text:p>(if applicable)</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce3"><text:p>8.</text:p><text:p>Employee last name</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce3"><text:p>9.</text:p><text:p>National Insurance number</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce3"><text:p>10.</text:p><text:p>PAYE reference of employing company</text:p></table:table-cell><table:table-cell office:value-type="string" table:content-validation-name="val1" table:style-name="ce12"><text:p>11.</text:p><text:p>Exercise price per share under option before adjustment</text:p><text:p>£</text:p><text:p>e.g. 10.1234</text:p></table:table-cell><table:table-cell office:value-type="string" table:style-name="ce13"><text:p>12.</text:p><text:p>Number of shares under the option after adjustment</text:p><text:p>e.g. 100.00</text:p></table:table-cell><table:table-cell office:value-type="string" table:content-validation-name="val1" table:style-name="ce12"><text:p>13.</text:p><text:p>Exercise price per share under option after the adjustment £</text:p><text:p>e.g. 10.1234</text:p></table:table-cell><table:table-cell office:value-type="string" table:content-validation-name="val1" table:style-name="ce12"><text:p>14.</text:p><text:p>Actual market value of a share at the date of grant</text:p><text:p>£</text:p><text:p>e.g. 10.1234</text:p></table:table-cell><table:table-cell table:number-columns-repeated="16370" table:style-name="ce3"/></table:table-row>.toString

  def getEMIAdjustmentsTemplate: ByteArrayInputStream = {
    val inputXml = openTable(
      sheetName
    ) + row1 + row2 + row3 + row4 + row5 + row6 + row7 + row8 + row9EMIAdjustmentsHeader + emiAdjustmentsXmlRow1
      .toString() + closeTable
    new ByteArrayInputStream(inputXml.getBytes("utf-8"))
  }

  def getEMIAdjustmentsTemplateLarge: ByteArrayInputStream = {
    val inputXml = openTable(
      sheetName
    ) + row1 + row2 + row3 + row4 + row5 + row6 + row7 + row8 + row9EMIAdjustmentsHeader + emiAdjustmentsXmlRow1
      .toString() + emiAdjustmentsXmlRow1.toString() + emiAdjustmentsXmlRow1.toString() + closeTable
    new ByteArrayInputStream(inputXml.getBytes("utf-8"))
  }

  // errors
  def getEMIAdjustmentsTemplateWithAmpersand: ByteArrayInputStream = {
    val inputXml = openTable(
      sheetName
    ) + row1 + row2 + row3 + row4 + row5 + row6 + row7 + row8 + row9EMIAdjustmentsHeader + emiAdjustmentsXmlRowWithAmpersand
      .toString() + closeTable
    new ByteArrayInputStream(inputXml.getBytes("utf-8"))
  }

  def getEMIAdjustmentsTemplateWithNoData: ByteArrayInputStream = {
    val inputXml = openTable(
      sheetName
    ) + row1 + row2 + row3 + row4 + row5 + row6 + row7 + row8 + row9EMIAdjustmentsHeader + closeTable
    new ByteArrayInputStream(inputXml.getBytes("utf-8"))
  }

  def getEMIAdjustmentsTemplateWithIncorrectHeader: ByteArrayInputStream = {
    val inputXml =
      openTable(sheetName) + row1 + row2 + row6 + row7 + row8 + row9EMIAdjustmentsHeader + emiAdjustmentsXmlRow1
        .toString() + closeTable
    new ByteArrayInputStream(inputXml.getBytes("utf-8"))
  }

  def getEMIAdjustmentsTemplateWithInvalidData: ByteArrayInputStream = {
    val inputXml = openTable(
      sheetName
    ) + row1 + row2 + row3 + row4 + row5 + row6 + row7 + row8 + row9EMIAdjustmentsHeader + emiAdjustmentsXmlRowWithInvalidData
      .toString() + closeTable
    new ByteArrayInputStream(inputXml.getBytes("utf-8"))
  }

  def getEMIAdjustmentsTemplateWithIncorrectSheetName: ByteArrayInputStream = {
    val inputXml = openTable(
      "EMI40_Adjustments_V4_With_A_Typo"
    ) + row1 + row2 + row3 + row4 + row5 + row6 + row7 + row8 + row9EMIAdjustmentsHeader + emiAdjustmentsXmlRow1
      .toString() + closeTable
    new ByteArrayInputStream(inputXml.getBytes("utf-8"))
  }

  def getEMIAdjustmentsTemplateWithCsopSchemeType: ByteArrayInputStream = {
    val inputXml = openTable(
      "CSOP_OptionsGranted_V4"
    ) + row1 + row2 + row3 + row4 + row5 + row6 + row7 + row8 + row9EMIAdjustmentsHeader + emiAdjustmentsXmlRow1
      .toString() + closeTable
    new ByteArrayInputStream(inputXml.getBytes("utf-8"))
  }
  
  val emiReplacedV7SheetName = "EMI40_Replaced_V7"

  val emiReplacedV7Row1 =
    <table:table-row table:style-name='ro6'>
      <table:table-cell table:style-name='ce1' calcext:value-type='string'>
        <text:p>Enterprise Management Incentives (EMI) annual return template - Enter replacement of options</text:p>
      </table:table-cell> <table:table-cell table:style-name='ce6' table:number-columns-repeated='16'></table:table-cell> <table:table-cell table:style-name='ce15' table:number-columns-repeated='20'></table:table-cell> <table:table-cell table:number-columns-repeated='987'></table:table-cell>
    </table:table-row>

  val emiReplacedV7Row2 =
    <table:table-row table:style-name='ro7'>
      <table:table-cell table:style-name='ce24' calcext:value-type='string'>
        <text:p>Use this worksheet to tell HMRC about options replaced because of a company re-organisation in the tax year.</text:p>
      </table:table-cell> <table:table-cell table:style-name='ce24' table:number-columns-repeated='11'></table:table-cell> <table:table-cell table:style-name='ce8' table:number-columns-repeated='3'></table:table-cell> <table:table-cell table:style-name='ce9' table:number-columns-repeated='22'></table:table-cell> <table:table-cell table:number-columns-repeated='987'></table:table-cell>
    </table:table-row>

  val emiReplacedV7Row3 =
    <table:table-row table:style-name='ro8'>
      <table:table-cell table:style-name='ce25' calcext:value-type='string'>
        <text:p>
          <text:a xlink:href='https://www.gov.uk/government/publications/enterprise-management-incentives-end-of-year-template' xlink:type='simple'>Check the notes and guidance for completing the end of year return for Enterprise Management Incentives schemes on GOV.UK</text:a>
        </text:p>
      </table:table-cell> <table:table-cell table:style-name='ce26'></table:table-cell> <table:table-cell table:style-name='ce8' table:number-columns-repeated='3'></table:table-cell> <table:table-cell table:style-name='ce9'></table:table-cell> <table:table-cell table:style-name='ce16'></table:table-cell> <table:table-cell table:style-name='ce18'></table:table-cell> <table:table-cell table:style-name='ce13' table:content-validation-name='val2' table:number-columns-repeated='7'></table:table-cell> <table:table-cell table:style-name='ce16' table:content-validation-name='val2'></table:table-cell> <table:table-cell table:style-name='ce16'></table:table-cell> <table:table-cell table:style-name='ce9' table:number-columns-repeated='20'></table:table-cell> <table:table-cell table:number-columns-repeated='987'></table:table-cell>
    </table:table-row>

  val emiReplacedV7Row4 =
    <table:table-row table:style-name='ro9'>
      <table:table-cell table:style-name='ce10' calcext:value-type='string'>
        <text:p>1. Date of grant of old option.</text:p> <text:p></text:p> <text:p>Enter the date in the correct format.</text:p> <text:p></text:p> <text:p>For example, 2023-10-31</text:p>
      </table:table-cell> <table:table-cell table:style-name='ce10' calcext:value-type='string'>
      <text:p>2. Date of grant of new option.</text:p> <text:p></text:p> <text:p>Enter the date in the correct format.</text:p> <text:p></text:p> <text:p>For example, 2023-10-31</text:p>
    </table:table-cell> <table:table-cell table:style-name='ce4' calcext:value-type='string'>
      <text:p>3. Employee first name.</text:p>
    </table:table-cell> <table:table-cell table:style-name='ce4' calcext:value-type='string'>
      <text:p>4. Employee second name (optional).</text:p> <text:p></text:p>
    </table:table-cell> <table:table-cell table:style-name='ce4' calcext:value-type='string'>
      <text:p>5. Employee last name.</text:p>
    </table:table-cell> <table:table-cell table:style-name='ce17' calcext:value-type='string'>
      <text:p>6. National Insurance number.</text:p> <text:p></text:p> <text:p>Enter in the correct format.</text:p> <text:p></text:p> <text:p>For example, QQ123456C</text:p> <text:p></text:p>
    </table:table-cell> <table:table-cell table:style-name='ce17' calcext:value-type='string'>
      <text:p>7. PAYE reference of employing company.</text:p>
    </table:table-cell> <table:table-cell table:style-name='ce19' calcext:value-type='string'>
      <text:p>8. Actual market value of original shares at the date the option(s) were replaced.</text:p> <text:p></text:p> <text:p>Enter the value in pounds to 4 decimal places.</text:p> <text:p></text:p> <text:p>For example, 10.1234</text:p>
    </table:table-cell> <table:table-cell table:style-name='ce4' calcext:value-type='string'>
      <text:p>9. Name of the company whose shares are the subject of the new option.</text:p>
    </table:table-cell> <table:table-cell table:style-name='ce27' calcext:value-type='string'>
      <text:p>10. Company address line 1.</text:p>
    </table:table-cell> <table:table-cell table:style-name='ce27' calcext:value-type='string'>
      <text:p>11. Company address line 2.</text:p>
    </table:table-cell> <table:table-cell table:style-name='ce27' calcext:value-type='string'>
      <text:p>12. Company address line 3.</text:p>
    </table:table-cell> <table:table-cell table:style-name='ce27' calcext:value-type='string'>
      <text:p>13. Company address line 4.</text:p>
    </table:table-cell> <table:table-cell table:style-name='ce27' calcext:value-type='string'>
      <text:p>14. Country.</text:p>
    </table:table-cell> <table:table-cell table:style-name='ce27' calcext:value-type='string'>
      <text:p>15. Postcode.</text:p>
    </table:table-cell> <table:table-cell table:style-name='ce27' calcext:value-type='string'>
      <text:p>16. Corporation Tax reference</text:p> <text:p>(Unique Taxpayer Reference).</text:p>
    </table:table-cell> <table:table-cell table:style-name='ce27' calcext:value-type='string'>
      <text:p>17. Company Reference Number (CRN).</text:p> <text:p></text:p> <text:p></text:p>
    </table:table-cell> <table:table-cell table:style-name='ce15' table:number-columns-repeated='20'></table:table-cell> <table:table-cell table:number-columns-repeated='987'></table:table-cell>
    </table:table-row>

  val emiReplacedV7Row5 =
    <table:table-row table:style-name='ro4'>
      <table:table-cell office:date-value='2015-12-09' table:style-name='ce11' calcext:value-type='date' table:number-columns-repeated='2'>
        <text:p>2015-12-09</text:p>
      </table:table-cell> <table:table-cell table:style-name='ce15' calcext:value-type='string'>
      <text:p>John</text:p>
    </table:table-cell> <table:table-cell table:style-name='ce15' calcext:value-type='string'>
      <text:p>Barry</text:p>
    </table:table-cell> <table:table-cell table:style-name='ce15' calcext:value-type='string'>
      <text:p>Smith</text:p>
    </table:table-cell> <table:table-cell table:style-name='ce14' calcext:value-type='string'>
      <text:p>AA123456A</text:p>
    </table:table-cell> <table:table-cell table:style-name='ce14' calcext:value-type='string'>
      <text:p>123/XZ55555555</text:p>
    </table:table-cell> <table:table-cell table:style-name='ce21' calcext:value-type='float' office:value='10.1234'>
      <text:p>10.1234</text:p>
    </table:table-cell> <table:table-cell table:style-name='ce15' calcext:value-type='float' office:value='10'>
      <text:p>10</text:p>
    </table:table-cell> <table:table-cell table:style-name='ce21' calcext:value-type='string'>
      <text:p>1 Example Street</text:p>
    </table:table-cell> <table:table-cell table:style-name='ce21' calcext:value-type='string'>
      <text:p>Bucknall</text:p>
    </table:table-cell> <table:table-cell table:style-name='ce21' calcext:value-type='string'>
      <text:p>Stoke</text:p>
    </table:table-cell> <table:table-cell table:style-name='ce21' calcext:value-type='string'>
      <text:p>Staffordshire</text:p>
    </table:table-cell> <table:table-cell table:style-name='ce21' calcext:value-type='string'>
      <text:p>United Kingdom</text:p>
    </table:table-cell> <table:table-cell table:style-name='ce21' calcext:value-type='string'>
      <text:p>SE1 2AB</text:p>
    </table:table-cell> <table:table-cell table:style-name='ce15' calcext:value-type='float' office:value='1234567890'>
      <text:p>1234567890</text:p>
    </table:table-cell> <table:table-cell table:style-name='ce15' calcext:value-type='string'>
      <text:p>AB123456</text:p>
    </table:table-cell> <table:table-cell table:number-columns-repeated='1007'></table:table-cell>
    </table:table-row>

  val emiReplacedV7Row6 =
    <table:table-row table:style-name='ro4'>
      <table:table-cell table:style-name='ce12' table:number-columns-repeated='2'></table:table-cell> <table:table-cell table:style-name='ce5' table:number-columns-repeated='3'></table:table-cell> <table:table-cell table:style-name='ce15' table:number-columns-repeated='2'></table:table-cell> <table:table-cell table:style-name='ce21'></table:table-cell> <table:table-cell table:style-name='ce5' table:number-columns-repeated='7'></table:table-cell> <table:table-cell table:style-name='ce15' table:number-columns-repeated='22'></table:table-cell> <table:table-cell table:number-columns-repeated='987'></table:table-cell>
    </table:table-row>

  val emiReplacedV7Row7 =
    <table:table-row table:style-name='ro4' table:number-rows-repeated='994'>
      <table:table-cell table:style-name='ce12' table:number-columns-repeated='2'></table:table-cell> <table:table-cell table:style-name='ce5' table:number-columns-repeated='3'></table:table-cell> <table:table-cell table:style-name='ce15' table:number-columns-repeated='2'></table:table-cell> <table:table-cell table:style-name='ce21'></table:table-cell> <table:table-cell table:style-name='ce5' table:number-columns-repeated='7'></table:table-cell> <table:table-cell table:style-name='ce15' table:number-columns-repeated='22'></table:table-cell> <table:table-cell table:number-columns-repeated='987'></table:table-cell>
    </table:table-row>

  val emiReplacedV7Row8 =
    <table:table-row table:style-name='ro5' table:number-rows-repeated='1047575'>
      <table:table-cell table:number-columns-repeated='1024'></table:table-cell>
    </table:table-row>

  val emiReplacedV7Row9 =
    <table:table-row table:style-name='ro5'>
      <table:table-cell table:number-columns-repeated='1024'></table:table-cell>
    </table:table-row>

  def getEMIAdjustmentsTemplates: ByteArrayInputStream = {
    val inputXml = openTable(emiReplacedV7SheetName) + emiReplacedV7Row1 + emiReplacedV7Row2 +
      emiReplacedV7Row3 + emiReplacedV7Row4 + emiReplacedV7Row5 + emiReplacedV7Row6 + emiReplacedV7Row7 + emiReplacedV7Row8 + emiReplacedV7Row9 + closeTable
    new ByteArrayInputStream(inputXml.getBytes("utf-8"))
  }

}
