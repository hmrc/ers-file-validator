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

package services.besIntegrationTestData

trait EMIBESIntegrationTestData {
  val emiAdjustmentsXML = <table:table table:name="EMI40_Adjustments_V3" table:style-name="ta1"><table:table-row table:style-name="ro6"><table:table-cell table:number-columns-repeated="4" office:value-type="string" calcext:value-type="string"><text:p>yes</text:p></table:table-cell><table:table-cell table:style-name="ce12" office:value-type="date" office:date-value="1989-10-20" calcext:value-type="date"><text:p>1989-10-20</text:p></table:table-cell><table:table-cell table:style-name="ce15" office:value-type="string" calcext:value-type="string"><text:p>Anthony</text:p></table:table-cell><table:table-cell table:style-name="ce15" office:value-type="string" calcext:value-type="string"><text:p>Joe</text:p></table:table-cell><table:table-cell table:style-name="ce15" office:value-type="string" calcext:value-type="string"><text:p>Jones</text:p></table:table-cell><table:table-cell table:style-name="ce19" office:value-type="string" calcext:value-type="string"><text:p>AA123456A</text:p></table:table-cell><table:table-cell table:style-name="ce19" office:value-type="string" calcext:value-type="string"><text:p>123/XZ55555555</text:p></table:table-cell><table:table-cell table:style-name="ce22" office:value-type="float" office:value="10.1232" calcext:value-type="float"><text:p>10.1232</text:p></table:table-cell><table:table-cell table:style-name="ce25" office:value-type="float" office:value="100" calcext:value-type="float"><text:p>100.00</text:p></table:table-cell><table:table-cell table:style-name="ce22" office:value-type="float" office:value="10.2585" calcext:value-type="float"><text:p>10.2585</text:p></table:table-cell><table:table-cell table:style-name="ce22" office:value-type="float" office:value="10.2544" calcext:value-type="float"><text:p>10.2544</text:p></table:table-cell><table:table-cell table:number-columns-repeated="1008"/><table:table-cell table:style-name="ce28" table:number-columns-repeated="2"/></table:table-row></table:table>
  val emiAdjustmentsXMLRow1 = <table:table-row table:style-name="ro6"><table:table-cell table:number-columns-repeated="3" office:value-type="string" calcext:value-type="string"><text:p>yes</text:p></table:table-cell><table:table-cell table:style-name="ce15" office:value-type="string" calcext:value-type="string"><text:p>4</text:p></table:table-cell><table:table-cell table:style-name="ce12" office:value-type="date" office:date-value="1989-10-20" calcext:value-type="date"><text:p>1989-10-20</text:p></table:table-cell><table:table-cell table:style-name="ce15" office:value-type="string" calcext:value-type="string"><text:p>Anthony</text:p></table:table-cell><table:table-cell table:style-name="ce15" office:value-type="string" calcext:value-type="string"><text:p>Joe</text:p></table:table-cell><table:table-cell table:style-name="ce15" office:value-type="string" calcext:value-type="string"><text:p>Jones</text:p></table:table-cell><table:table-cell table:style-name="ce19" office:value-type="string" calcext:value-type="string"><text:p>AA123456A</text:p></table:table-cell><table:table-cell table:style-name="ce19" office:value-type="string" calcext:value-type="string"><text:p>123/XZ55555555</text:p></table:table-cell><table:table-cell table:style-name="ce22" office:value-type="float" office:value="10.1232" calcext:value-type="float"><text:p>10.1232</text:p></table:table-cell><table:table-cell table:style-name="ce25" office:value-type="float" office:value="100" calcext:value-type="float"><text:p>100.00</text:p></table:table-cell><table:table-cell table:style-name="ce22" office:value-type="float" office:value="10.2585" calcext:value-type="float"><text:p>10.2585</text:p></table:table-cell><table:table-cell table:style-name="ce22" office:value-type="float" office:value="10.2544" calcext:value-type="float"><text:p>10.2544</text:p></table:table-cell><table:table-cell table:number-columns-repeated="1008"/><table:table-cell table:style-name="ce28" table:number-columns-repeated="2"/></table:table-row>
  val emiAdjustmentsRepeatXMLRow1 = <table:table-row table:number-rows-repeated="3" table:style-name="ro6"><table:table-cell table:number-columns-repeated="3" office:value-type="string" calcext:value-type="string"><text:p>yes</text:p></table:table-cell><table:table-cell table:style-name="ce15" office:value-type="string" calcext:value-type="string"><text:p>4</text:p></table:table-cell><table:table-cell table:style-name="ce12" office:value-type="date" office:date-value="1989-10-20" calcext:value-type="date"><text:p>1989-10-20</text:p></table:table-cell><table:table-cell table:style-name="ce15" office:value-type="string" calcext:value-type="string"><text:p>Anthony</text:p></table:table-cell><table:table-cell table:style-name="ce15" office:value-type="string" calcext:value-type="string"><text:p>Joe</text:p></table:table-cell><table:table-cell table:style-name="ce15" office:value-type="string" calcext:value-type="string"><text:p>Jones</text:p></table:table-cell><table:table-cell table:style-name="ce19" office:value-type="string" calcext:value-type="string"><text:p>AA123456A</text:p></table:table-cell><table:table-cell table:style-name="ce19" office:value-type="string" calcext:value-type="string"><text:p>123/XZ55555555</text:p></table:table-cell><table:table-cell table:style-name="ce22" office:value-type="float" office:value="10.1232" calcext:value-type="float"><text:p>10.1232</text:p></table:table-cell><table:table-cell table:style-name="ce25" office:value-type="float" office:value="100" calcext:value-type="float"><text:p>100.00</text:p></table:table-cell><table:table-cell table:style-name="ce22" office:value-type="float" office:value="10.2585" calcext:value-type="float"><text:p>10.2585</text:p></table:table-cell><table:table-cell table:style-name="ce22" office:value-type="float" office:value="10.2544" calcext:value-type="float"><text:p>10.2544</text:p></table:table-cell><table:table-cell table:number-columns-repeated="1008"/><table:table-cell table:style-name="ce28" table:number-columns-repeated="2"/></table:table-row>
  val emiAdjustmentsExpData = List("yes","yes","yes","4","1989-10-20","Anthony","Joe","Jones","AA123456A","123/XZ55555555","10.1232","100.00","10.2585","10.2544")

  val emiReplacedXML = <table:table table:name="EMI40_Replaced_V3" table:style-name="ta2"><table:table-row table:style-name="ro6"><table:table-cell table:style-name="ce13" office:value-type="date" office:date-value="2011-10-11" calcext:value-type="date"><text:p>2011-10-11</text:p></table:table-cell><table:table-cell table:style-name="ce13" office:value-type="date" office:date-value="2011-11-13" calcext:value-type="date"><text:p>2011-11-13</text:p></table:table-cell><table:table-cell table:style-name="ce16" office:value-type="string" calcext:value-type="string"><text:p>Anthony</text:p></table:table-cell><table:table-cell table:style-name="ce16" office:value-type="string" calcext:value-type="string"><text:p>Joe</text:p></table:table-cell><table:table-cell table:style-name="ce16" office:value-type="string" calcext:value-type="string"><text:p>Jones</text:p></table:table-cell><table:table-cell table:style-name="ce19" office:value-type="string" calcext:value-type="string"><text:p>AA123456A</text:p></table:table-cell><table:table-cell table:style-name="ce19" office:value-type="string" calcext:value-type="string"><text:p>123/XZ55555555</text:p></table:table-cell><table:table-cell office:value-type="float" office:value="10.2332" calcext:value-type="float"><text:p>10.2332</text:p></table:table-cell><table:table-cell table:style-name="ce16" office:value-type="string" calcext:value-type="string"><text:p>CompanyA</text:p></table:table-cell><table:table-cell table:style-name="ce19" office:value-type="string" calcext:value-type="string"><text:p>A Cottage</text:p></table:table-cell><table:table-cell table:style-name="ce19" office:value-type="string" calcext:value-type="string"><text:p>55 Buckingham Rd</text:p></table:table-cell><table:table-cell table:style-name="ce19" office:value-type="string" calcext:value-type="string"><text:p>THORNBURY</text:p></table:table-cell><table:table-cell table:style-name="ce16" office:value-type="string" calcext:value-type="string"><text:p>London</text:p></table:table-cell><table:table-cell table:style-name="ce16" office:value-type="string" calcext:value-type="string"><text:p>UK</text:p></table:table-cell><table:table-cell table:style-name="ce19" office:value-type="string" calcext:value-type="string"><text:p>HR7 1XX</text:p></table:table-cell><table:table-cell table:style-name="ce19" office:value-type="float" office:value="1234567890" calcext:value-type="float"><text:p>1234567890</text:p></table:table-cell><table:table-cell table:style-name="ce19" office:value-type="string" calcext:value-type="string"><text:p>AC097609</text:p></table:table-cell><table:table-cell table:number-columns-repeated="1007"/></table:table-row></table:table>
  val emiReplacedXMLRow1 = <table:table-row table:style-name="ro6"><table:table-cell table:style-name="ce13" office:value-type="date" office:date-value="2011-10-11" calcext:value-type="date"><text:p>2011-10-11</text:p></table:table-cell><table:table-cell table:style-name="ce13" office:value-type="date" office:date-value="2011-11-13" calcext:value-type="date"><text:p>2011-11-13</text:p></table:table-cell><table:table-cell table:style-name="ce16" office:value-type="string" calcext:value-type="string"><text:p>Anthony</text:p></table:table-cell><table:table-cell table:style-name="ce16" office:value-type="string" calcext:value-type="string"><text:p>Joe</text:p></table:table-cell><table:table-cell table:style-name="ce16" office:value-type="string" calcext:value-type="string"><text:p>Jones</text:p></table:table-cell><table:table-cell table:style-name="ce19" office:value-type="string" calcext:value-type="string"><text:p>AA123456A</text:p></table:table-cell><table:table-cell table:style-name="ce19" office:value-type="string" calcext:value-type="string"><text:p>123/XZ55555555</text:p></table:table-cell><table:table-cell office:value-type="float" office:value="10.2332" calcext:value-type="float"><text:p>10.2332</text:p></table:table-cell><table:table-cell table:style-name="ce16" office:value-type="string" calcext:value-type="string"><text:p>CompanyA</text:p></table:table-cell><table:table-cell table:style-name="ce19" office:value-type="string" calcext:value-type="string"><text:p>A Cottage</text:p></table:table-cell><table:table-cell table:style-name="ce19" office:value-type="string" calcext:value-type="string"><text:p>55 Buckingham Rd</text:p></table:table-cell><table:table-cell table:style-name="ce19" office:value-type="string" calcext:value-type="string"><text:p>THORNBURY</text:p></table:table-cell><table:table-cell table:style-name="ce16" office:value-type="string" calcext:value-type="string"><text:p>London</text:p></table:table-cell><table:table-cell table:style-name="ce16" office:value-type="string" calcext:value-type="string"><text:p>UK</text:p></table:table-cell><table:table-cell table:style-name="ce19" office:value-type="string" calcext:value-type="string"><text:p>HR7 1XX</text:p></table:table-cell><table:table-cell table:style-name="ce19" office:value-type="float" office:value="1234567890" calcext:value-type="float"><text:p>1234567890</text:p></table:table-cell><table:table-cell table:style-name="ce19" office:value-type="string" calcext:value-type="string"><text:p>AC097609</text:p></table:table-cell><table:table-cell table:number-columns-repeated="1007"/></table:table-row>
  val emiReplacedExpData = List("2011-10-11","2011-11-13","Anthony","Joe","Jones","AA123456A","123/XZ55555555","10.2332","CompanyA","A Cottage","55","Buckingham Rd","THORNBURY","London","UK","HR7 1XX","1234567890","AC097609")
  val emiReplacedXMLRowMandatoryCells = <table:table-row table:style-name="ro1"><table:table-cell table:number-columns-repeated="2" table:style-name="ce6" office:value-type="date" office:date-value="2015-12-09" calcext:value-type="date"><text:p>2015-12-09</text:p></table:table-cell><table:table-cell office:value-type="string" calcext:value-type="string"><text:p>John</text:p></table:table-cell><table:table-cell/><table:table-cell office:value-type="string" calcext:value-type="string"><text:p>Smith</text:p></table:table-cell><table:table-cell office:value-type="string" calcext:value-type="string"><text:p>AB123456A</text:p></table:table-cell><table:table-cell office:value-type="string" calcext:value-type="string"><text:p>123/XZ66666666</text:p></table:table-cell><table:table-cell table:number-columns-repeated="1"/><table:table-cell office:value-type="string" calcext:value-type="string"><text:p>Some Company Name</text:p></table:table-cell><table:table-cell office:value-type="string" calcext:value-type="string"><text:p>1 Example Street</text:p></table:table-cell><table:table-cell table:number-columns-repeated="4"/><table:table-cell office:value-type="string" calcext:value-type="string"><text:p>TE12 3ST</text:p></table:table-cell><table:table-cell table:number-columns-repeated="1009"/></table:table-row>
  val emiReplacedExpMandatoryData = List("2015-12-09","2015-12-09","John","","Smith","AB123456A","123/XZ66666666","","Some Company Name","1 Example Street","", "", "", "", "TE12 3ST", "", "")

  val emiRLCXML = <table:table table:name="EMI40_RLC_V3" table:style-name="ta3"><table:table-row table:style-name="ro6"><table:table-cell table:style-name="ce13" office:value-type="date" office:date-value="2001-12-10" calcext:value-type="date"><text:p>2001-12-10</text:p></table:table-cell><table:table-cell table:style-name="ce35" office:value-type="string" calcext:value-type="string"><text:p>yes</text:p></table:table-cell><table:table-cell office:value-type="float" office:value="2" calcext:value-type="float"><text:p>2</text:p></table:table-cell><table:table-cell table:style-name="ce16" office:value-type="string" calcext:value-type="string"><text:p>Anthony</text:p></table:table-cell><table:table-cell table:style-name="ce16" office:value-type="string" calcext:value-type="string"><text:p>Joe</text:p></table:table-cell><table:table-cell table:style-name="ce16" office:value-type="string" calcext:value-type="string"><text:p>Jones</text:p></table:table-cell><table:table-cell table:style-name="ce19" office:value-type="string" calcext:value-type="string"><text:p>AA123456A</text:p></table:table-cell><table:table-cell table:style-name="ce19" office:value-type="string" calcext:value-type="string"><text:p>123/XZ55555555</text:p></table:table-cell><table:table-cell office:value-type="float" office:value="100" calcext:value-type="float"><text:p>100.00</text:p></table:table-cell><table:table-cell table:style-name="ce36" office:value-type="string" calcext:value-type="string"><text:p>yes</text:p></table:table-cell><table:table-cell office:value-type="float" office:value="10.2584" calcext:value-type="float"><text:p>10.2584</text:p></table:table-cell><table:table-cell table:style-name="ce36" office:value-type="string" calcext:value-type="string"><text:p>yes</text:p></table:table-cell><table:table-cell table:number-columns-repeated="1012"/></table:table-row></table:table>
  val emiRLCXMLRow1 = <table:table-row table:style-name="ro6"><table:table-cell table:style-name="ce13" office:value-type="date" office:date-value="2001-12-10" calcext:value-type="date"><text:p>2001-12-10</text:p></table:table-cell><table:table-cell table:style-name="ce35" office:value-type="string" calcext:value-type="string"><text:p>yes</text:p></table:table-cell><table:table-cell office:value-type="float" office:value="2" calcext:value-type="float"><text:p>2</text:p></table:table-cell><table:table-cell table:style-name="ce16" office:value-type="string" calcext:value-type="string"><text:p>Anthony</text:p></table:table-cell><table:table-cell table:style-name="ce16" office:value-type="string" calcext:value-type="string"><text:p>Joe</text:p></table:table-cell><table:table-cell table:style-name="ce16" office:value-type="string" calcext:value-type="string"><text:p>Jones</text:p></table:table-cell><table:table-cell table:style-name="ce19" office:value-type="string" calcext:value-type="string"><text:p>AA123456A</text:p></table:table-cell><table:table-cell table:style-name="ce19" office:value-type="string" calcext:value-type="string"><text:p>123/XZ55555555</text:p></table:table-cell><table:table-cell office:value-type="float" office:value="100" calcext:value-type="float"><text:p>100.00</text:p></table:table-cell><table:table-cell table:style-name="ce36" office:value-type="string" calcext:value-type="string"><text:p>yes</text:p></table:table-cell><table:table-cell office:value-type="float" office:value="10.2584" calcext:value-type="float"><text:p>10.2584</text:p></table:table-cell><table:table-cell table:style-name="ce36" office:value-type="string" calcext:value-type="string"><text:p>yes</text:p></table:table-cell><table:table-cell table:number-columns-repeated="1012"/></table:table-row>
  val emiRLCExpData = List("2001-12-10","yes","2","Anthony","Joe","Jones","AA123456A","123/XZ55555555","100.00","yes","10.2584","yes")

  val emiNonTaxableXML = <table:table table:name="EMI40_NonTaxable_V3" table:style-name="ta3"><table:table-row table:style-name="ro17"><table:table-cell table:style-name="ce12" office:value-type="date" office:date-value="1908-10-19" calcext:value-type="date"><text:p>1908-10-19</text:p></table:table-cell><table:table-cell table:style-name="ce22" office:value-type="string" calcext:value-type="string"><text:p>Anthony</text:p></table:table-cell><table:table-cell table:style-name="ce22" office:value-type="string" calcext:value-type="string"><text:p>Joe</text:p></table:table-cell><table:table-cell table:style-name="ce15" office:value-type="string" calcext:value-type="string"><text:p>Jones</text:p></table:table-cell><table:table-cell table:style-name="ce15" office:value-type="string" calcext:value-type="string"><text:p>AA123456A</text:p></table:table-cell><table:table-cell table:style-name="ce12" office:value-type="string" calcext:value-type="string"><text:p>123/XZ55555555</text:p></table:table-cell><table:table-cell table:style-name="ce25" office:value-type="float" office:value="100.01" calcext:value-type="float"><text:p>100.01</text:p></table:table-cell><table:table-cell table:style-name="ce22" office:value-type="float" office:value="10.1235" calcext:value-type="float"><text:p>10.1235</text:p></table:table-cell><table:table-cell table:style-name="ce22" office:value-type="float" office:value="10.1232" calcext:value-type="float"><text:p>10.1232</text:p></table:table-cell><table:table-cell table:style-name="ce22" office:value-type="float" office:value="10.1245" calcext:value-type="float"><text:p>10.1245</text:p></table:table-cell><table:table-cell table:style-name="ce15" office:value-type="string" calcext:value-type="string"><text:p>yes</text:p></table:table-cell><table:table-cell table:style-name="ce15"/><table:table-cell table:style-name="ce27" office:value-type="string" calcext:value-type="string"><text:p>aa123456</text:p></table:table-cell><table:table-cell table:style-name="ce22" office:value-type="float" office:value="10.1235" calcext:value-type="float"><text:p>10.1235</text:p></table:table-cell><table:table-cell table:style-name="ce15" office:value-type="string" calcext:value-type="string"><text:p>yes</text:p></table:table-cell><table:table-cell table:number-columns-repeated="1009"/></table:table-row></table:table>
  val emiNonTaxableXMLRow1 = <table:table-row table:style-name="ro17"><table:table-cell table:style-name="ce12" office:value-type="date" office:date-value="1908-10-19" calcext:value-type="date"><text:p>1908-10-19</text:p></table:table-cell><table:table-cell table:style-name="ce22" office:value-type="string" calcext:value-type="string"><text:p>Anthony</text:p></table:table-cell><table:table-cell table:style-name="ce22" office:value-type="string" calcext:value-type="string"><text:p>Joe</text:p></table:table-cell><table:table-cell table:style-name="ce15" office:value-type="string" calcext:value-type="string"><text:p>Jones</text:p></table:table-cell><table:table-cell table:style-name="ce15" office:value-type="string" calcext:value-type="string"><text:p>AA123456A</text:p></table:table-cell><table:table-cell table:style-name="ce12" office:value-type="string" calcext:value-type="string"><text:p>123/XZ55555555</text:p></table:table-cell><table:table-cell table:style-name="ce25" office:value-type="float" office:value="100.01" calcext:value-type="float"><text:p>100.01</text:p></table:table-cell><table:table-cell table:style-name="ce22" office:value-type="float" office:value="10.1235" calcext:value-type="float"><text:p>10.1235</text:p></table:table-cell><table:table-cell table:style-name="ce22" office:value-type="float" office:value="10.1232" calcext:value-type="float"><text:p>10.1232</text:p></table:table-cell><table:table-cell table:style-name="ce22" office:value-type="float" office:value="10.1245" calcext:value-type="float"><text:p>10.1245</text:p></table:table-cell><table:table-cell table:style-name="ce15" office:value-type="string" calcext:value-type="string"><text:p>yes</text:p></table:table-cell><table:table-cell table:style-name="ce15"/><table:table-cell table:style-name="ce27" office:value-type="string" calcext:value-type="string"><text:p>aa123456</text:p></table:table-cell><table:table-cell table:style-name="ce22" office:value-type="float" office:value="10.1235" calcext:value-type="float"><text:p>10.1235</text:p></table:table-cell><table:table-cell table:style-name="ce15" office:value-type="string" calcext:value-type="string"><text:p>yes</text:p></table:table-cell><table:table-cell table:number-columns-repeated="1009"/></table:table-row>
  val emiNonTaxableExpData = List("1908-10-19","Anthony","Joe","Jones","AA123456A","123/XZ55555555","100.01","10.1235","10.1232","10.1245","yes","","aa123456","10.1235","yes")

  val emiTaxableXML = <table:table table:name="EMI40_Taxable_V3" table:style-name="ta3"><table:table-row table:style-name="ro17"><table:table-cell table:style-name="ce12" office:value-type="date" office:date-value="1908-10-19" calcext:value-type="date"><text:p>1908-10-19</text:p></table:table-cell><table:table-cell table:style-name="ce15" office:value-type="string" calcext:value-type="string"><text:p>yes</text:p></table:table-cell><table:table-cell table:style-name="ce15" office:value-type="string" calcext:value-type="string"><text:p>2</text:p></table:table-cell><table:table-cell table:style-name="ce15" office:value-type="string" calcext:value-type="string"><text:p>Anthony</text:p></table:table-cell><table:table-cell table:style-name="ce15" office:value-type="string" calcext:value-type="string"><text:p>Joe</text:p></table:table-cell><table:table-cell table:style-name="ce15" office:value-type="string" calcext:value-type="string"><text:p>Jones</text:p></table:table-cell><table:table-cell table:style-name="ce15" office:value-type="string" calcext:value-type="string"><text:p>AA123456A</text:p></table:table-cell><table:table-cell table:style-name="ce15" office:value-type="string" calcext:value-type="string"><text:p>123/XZ55555555</text:p></table:table-cell><table:table-cell table:style-name="ce25" office:value-type="float" office:value="100.01" calcext:value-type="float"><text:p>100.01</text:p></table:table-cell><table:table-cell table:style-name="ce22" office:value-type="float" office:value="10.1225" calcext:value-type="float"><text:p>10.1225</text:p></table:table-cell><table:table-cell table:style-name="ce22" office:value-type="float" office:value="10.1221" calcext:value-type="float"><text:p>10.1221</text:p></table:table-cell><table:table-cell table:style-name="ce22" office:value-type="float" office:value="10.1224" calcext:value-type="float"><text:p>10.1224</text:p></table:table-cell><table:table-cell table:style-name="ce22" office:value-type="float" office:value="10.1334" calcext:value-type="float"><text:p>10.1334</text:p></table:table-cell><table:table-cell table:style-name="ce22" office:value-type="float" office:value="10.1434" calcext:value-type="float"><text:p>10.1434</text:p></table:table-cell><table:table-cell table:style-name="ce15" office:value-type="string" calcext:value-type="string"><text:p>yes</text:p></table:table-cell><table:table-cell table:style-name="ce15" table:number-columns-repeated="2"/><table:table-cell table:style-name="ce15" office:value-type="string" calcext:value-type="string"><text:p>yes</text:p></table:table-cell><table:table-cell table:style-name="ce15" office:value-type="string" calcext:value-type="string"><text:p>no</text:p></table:table-cell><table:table-cell table:style-name="ce22" office:value-type="float" office:value="10.9862" calcext:value-type="float"><text:p>10.9862</text:p></table:table-cell><table:table-cell table:number-columns-repeated="1004"/></table:table-row></table:table>
  val emiTaxableXMLRow1 = <table:table-row table:style-name="ro17"><table:table-cell table:style-name="ce12" office:value-type="date" office:date-value="1908-10-19" calcext:value-type="date"><text:p>1908-10-19</text:p></table:table-cell><table:table-cell table:style-name="ce15" office:value-type="string" calcext:value-type="string"><text:p>yes</text:p></table:table-cell><table:table-cell table:style-name="ce15" office:value-type="string" calcext:value-type="string"><text:p>2</text:p></table:table-cell><table:table-cell table:style-name="ce15" office:value-type="string" calcext:value-type="string"><text:p>Anthony</text:p></table:table-cell><table:table-cell table:style-name="ce15" office:value-type="string" calcext:value-type="string"><text:p>Joe</text:p></table:table-cell><table:table-cell table:style-name="ce15" office:value-type="string" calcext:value-type="string"><text:p>Jones</text:p></table:table-cell><table:table-cell table:style-name="ce15" office:value-type="string" calcext:value-type="string"><text:p>AA123456A</text:p></table:table-cell><table:table-cell table:style-name="ce15" office:value-type="string" calcext:value-type="string"><text:p>123/XZ55555555</text:p></table:table-cell><table:table-cell table:style-name="ce25" office:value-type="float" office:value="100.01" calcext:value-type="float"><text:p>100.01</text:p></table:table-cell><table:table-cell table:style-name="ce22" office:value-type="float" office:value="10.1225" calcext:value-type="float"><text:p>10.1225</text:p></table:table-cell><table:table-cell table:style-name="ce22" office:value-type="float" office:value="10.1221" calcext:value-type="float"><text:p>10.1221</text:p></table:table-cell><table:table-cell table:style-name="ce22" office:value-type="float" office:value="10.1224" calcext:value-type="float"><text:p>10.1224</text:p></table:table-cell><table:table-cell table:style-name="ce22" office:value-type="float" office:value="10.1334" calcext:value-type="float"><text:p>10.1334</text:p></table:table-cell><table:table-cell table:style-name="ce22" office:value-type="float" office:value="10.1434" calcext:value-type="float"><text:p>10.1434</text:p></table:table-cell><table:table-cell table:style-name="ce15" office:value-type="string" calcext:value-type="string"><text:p>yes</text:p></table:table-cell><table:table-cell table:style-name="ce15" table:number-columns-repeated="2"/><table:table-cell table:style-name="ce15" office:value-type="string" calcext:value-type="string"><text:p>yes</text:p></table:table-cell><table:table-cell table:style-name="ce15" office:value-type="string" calcext:value-type="string"><text:p>no</text:p></table:table-cell><table:table-cell table:style-name="ce22" office:value-type="float" office:value="10.9862" calcext:value-type="float"><text:p>10.9862</text:p></table:table-cell><table:table-cell table:number-columns-repeated="1004"/></table:table-row>
  val emiTaxableExpData = List("1908-10-19","yes","2","Anthony","Joe","Jones","AA123456A","123/XZ55555555","100.01","10.1225","10.1221","10.1224","10.1334","10.1434","yes","","","yes","no","10.9862")


}
