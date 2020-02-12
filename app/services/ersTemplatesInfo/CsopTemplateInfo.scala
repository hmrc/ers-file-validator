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

package services.ersTemplatesInfo

trait CsopTemplateInfo {

  val csop = "CSOP"

  val csopSheet1Name = "CSOP_OptionsGranted_V3"
  val csopSheet2Name =  "CSOP_OptionsRCL_V3"
  val csopSheet3Name = "CSOP_OptionsExercised_V3"


  val csopSheet1title = "CSOP scheme template – Options granted"
  val csopSheet1ValConfig = "ers-csop-granted-validation-config"
  val csopSheet2Title = "CSOP scheme template – Options released (including exchanges) cancelled or lapsed in year"
  val csopSheet2ValConfig = "ers-csop-rcl-validation"
  val csopSheet3Title ="CSOP scheme template – Options and replacement options exercised"
  val csopSheet3ValConfig = "ers-csop-exercised-validation"


  val csopOptionsGrantedHeaderRow = List("1.Date of grant(yyyy-mm-dd)",
    "2.Number of employees granted options",
    "3.Over how many shares in total were CSOP options grantede.g. 100.00",
    "4.Unrestricted market value (UMV) of each share used to determine option exercise price£e.g. 10.1234",
    "5.Option exercise price per share£e.g. 10.1234",
    "6.Are the shares under the CSOP option listed on a recognised stock exchange?(yes/no)",
    "7.If no, was the market value agreed with HMRC?(yes/no)",
    "8.If yes enter the HMRC reference given",
    "9.Using the UMV at the time of each relevant grant, does any employee hold unexercised CSOP options over shares totalling more than £30k, including this grant?(yes/no)"
  )//.map(_.replaceAll(csopHeaderFormat,""))

  val csopOptionsRCLHeaderRow =  List("1.Date of event(yyyy-mm-dd)",
    "2.Was money or value received by the option holder or anyone else when the option was released, exchanged, cancelled or lapsed? (yes/no) If yes go to question 3, otherwise no further information is needed for this event.",
    "3.If yes, amount or value £ e.g. 10.1234",
    "4.Employee first name",
    "5.Employee second name(if applicable)",
    "6.Employee last name",
    "7.National Insurance number(if applicable)",
    "8.PAYE reference of employing company",
    "9.Was PAYE operated?(yes/no)")//.map(_.replaceAll(csopHeaderFormat,""))

  val csopOptionsExercisedHeaderRow =
    List("1.Date of event(yyyy-mm-dd)",
      "2.Employee first name",
      "3.Employee second name(if applicable)",
      "4.Employee last name",
      "5.National Insurance number(if applicable)",
      "6.PAYE reference of employing company",
      "7.Date of grant(yyyy-mm-dd)",
      "8.Total number of shares employee entitled to on exercise of the option before any cashless exercise or other adjustmente.g. 100.00",
      "9.Are these shares part of the largest class of shares in that company?(yes/no)",
      "10.Are the shares subject to the option listed on a recognised stock exchange?(yes/no)",
      "11.Actual market value (AMV) of a share on the date of exerciseￂﾣe.g. 10.1234",
      "12.Exercise price per shareￂﾣe.g. 10.1234",
      "13.Unrestricted market value (UMV) of a share on the date of exerciseￂﾣe.g. 10.1234",
      "14.If the answer to question 10 is no, was the market value agreed with HMRC?(yes/no)",
      "15.If yes enter the HMRC reference given",
      "16.Does the exercise qualify for tax relief?(yes/no)",
      "17.Was PAYE operated?(yes/no)",
      "18.If yes, deductible amountￂﾣe.g. 10.1234",
      "19.Has a National Insurance Contributions election or agreement been operated?(yes/no)",
      "20.Were all shares resulting from the exercise sold? (yes/no). Answer yes if they were either sold on the same day as the exercise in connection with the exercise or sale instructions were given for all shares to be sold on exercise"
    )//.map(_.replaceAll(csopHeaderFormat,""))

}
