/*
 * Copyright 2017 HM Revenue & Customs
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

trait SayeTemplateInfo {

  val saye = "SAYE"

  val sayeSheet1Name = "SAYE_Granted_V3"
  val sayeSheet2Name = "SAYE_RCL_V3"
  val sayeSheet3Name = "SAYE_Exercised_V3"

  val sayeSheet1Desc = "SAYE scheme template – Options granted"
  val sayeSheet1ValConfig = "ers-saye-granted-validation-config"
  val sayeSheet2Desc = "SAYE scheme template – Options released (including exchanges), cancelled or lapsed in year"
  val sayeSheet2ValConfig = "ers-saye-rcl-validation-config"
  val sayeSheet3Desc = "SAYE scheme template – Options exercised"
  val sayeSheet3ValConfig = "ers-saye-exercised-validation-config"


  val sayeGrantedHeaderRow = List(
    "1.Date of grant (yyyy-mm-dd)",
    "2.Number of individuals granted options",
    "3.Over how many shares in total were options granted? e.g. 100.00",
    "4.Unrestricted market value (UMV) of each share used to determine option exercise price £ e.g. 10.1234",
    "5.Option exercise price per share £ e.g. 10.1234",
    "6.Are the shares listed on a recognised stock exchange? (yes/no)",
    "7.If no, was the market value agreed with HMRC? (yes/no)",
    "8.If yes enter the HMRC reference given"
  )

  val sayeRCLHeaderRow = List(
    "1.Date of event (yyyy-mm-dd)",
    "2.Was money or value received by the option holder or anyone else when the option was released, exchanged, cancelled or lapsed? (yes/no) If yes go to question 3, otherwise no more information is needed for this event.",
    "3.If yes, amount or value £ e.g. 10.1234",
    "4.Employee first name",
    "5.Employee second name (if applicable)",
    "6.Employee last name",
    "7.National Insurance number (if applicable)",
    "8.PAYE reference",
    "9.Was PAYE operated? (yes/no)"
  )

  val sayeExercisedHeaderRow = List(
    "1.Date of event (yyyy-mm-dd)",
    "2.Employee first name",
    "3.Employee second name (if applicable)",
    "4.Employee last name",
    "5.National Insurance number (if applicable)",
    "6.PAYE reference of employing company",
    "7.Date of grant (yyyy-mm-dd)",
    "8.Total number of shares employee entitled to on exercise of the option e.g. 100.00",
    "9.Were the shares subject to the option listed on a recognised stock exchange? (yes/no) If yes go to question 12 If no go to question 10",
    "10.If no, was the market value agreed with HMRC? (yes/no)",
    "11.If yes enter the HMRC reference given",
    "12.Actual market value (AMV) of a share on the date of exercise £ e.g. 10.1234",
    "13.Exercise price per share £ e.g. 10.1234",
    "14.Unrestricted market value (UMV) of a share on the date of exercise £ e.g. 10.1234",
    "15.Does the exercise qualify for tax relief? (yes/no)",
    "16.Were all shares resulting from the exercise sold? (yes/no). Answer yes if they were either sold on the same day as the exercise in connection with the exercise or sale instructions were given for all shares to be sold on exercise"
  )
}
