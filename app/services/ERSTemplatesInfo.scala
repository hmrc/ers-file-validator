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

package services

import com.typesafe.config.{Config, ConfigFactory}
import services.ersTemplatesInfo._
import uk.gov.hmrc.services.validation.DataValidator

/**
 * Created by raghu on 26/01/16.
 */
case class SheetInfo (schemeType:String, sheetId: Int, sheetName:String, sheetTitle:String,configFileName: String, headerRow:List[String])

object ERSTemplatesInfo extends EMITemplateInfo with CsopTemplateInfo with SipTemplateInfo with OtherTemplateInfo with SayeTemplateInfo{

  val csopSheets: Map[String, SheetInfo] = Map(
    csopSheet1Name  -> SheetInfo(csop,  1, csopSheet1Name , csopSheet1title, csopSheet1ValConfig,  csopOptionsGrantedHeaderRow),
    csopSheet2Name  -> SheetInfo(csop,  2, csopSheet2Name,  csopSheet2Title, csopSheet2ValConfig,  csopOptionsRCLHeaderRow),
    csopSheet3Name  -> SheetInfo(csop,  3, csopSheet3Name,  csopSheet3Title, csopSheet3ValConfig,  csopOptionsExercisedHeaderRow)
  )

  val csopSheetsV5: Map[String, SheetInfo] = Map(
    csopSheet1NameV5  -> SheetInfo(csop,  1, csopSheet1NameV5, csopSheet1title, csopSheet1ValConfigV5, csopOptionsGrantedHeaderRowV5),
    csopSheet2NameV5  -> SheetInfo(csop,  2, csopSheet2NameV5, csopSheet2Title, csopSheet2ValConfig, csopOptionsRCLHeaderRow),
    csopSheet3NameV5  -> SheetInfo(csop,  3, csopSheet3NameV5, csopSheet3Title, csopSheet3ValConfig, csopOptionsExercisedHeaderRow)
  )

  val emiSheets: Map[String, SheetInfo] = Map(
    emiSheet1Name   -> SheetInfo(emi,   1, emiSheet1Name,   emiSheet1Desc,   emiSheet1ValConfig,   emiAdjustmentsHeaderRow),
    emiSheet2Name   -> SheetInfo(emi,   2, emiSheet2Name,   emiSheet2Desc,   emiSheet2ValConfig,   emiReplacedHeaderRow),
    emiSheet3Name   -> SheetInfo(emi,   3, emiSheet3Name,   emiSheet3Desc,   emiSheet3ValConfig,   emiRCLHeaderRow),
    emiSheet4Name   -> SheetInfo(emi,   4, emiSheet4Name,   emiSheet4Desc,   emiSheet4ValConfig,   emiNonTaxableHeaderRow),
    emiSheet5Name   -> SheetInfo(emi,   5, emiSheet5Name,   emiSheet5Desc,   emiSheet5ValConfig,   emiTaxableHeaderRow)
  )

  val otherSheets: Map[String, SheetInfo] = Map(
    otherSheet1Name -> SheetInfo(other, 1, otherSheet1Name, otherSheet1Desc, otherSheet1ValConfig, otherGrantsHeaderRow),
    otherSheet2Name -> SheetInfo(other, 2, otherSheet2Name, otherSheet2Desc, otherSheet2ValConfig, otherOptionsHeaderRow),
    otherSheet3Name -> SheetInfo(other, 3, otherSheet3Name, otherSheet3Desc, otherSheet3ValConfig, otherAcquisitionHeaderRow),
    otherSheet4Name -> SheetInfo(other, 4, otherSheet4Name, otherSheet4Desc, otherSheet4ValConfig, otherRestrictedSecuritiesHeaderRow),
    otherSheet5Name -> SheetInfo(other, 5, otherSheet5Name, otherSheet5Desc, otherSheet5ValConfig, otherBenefitsHeaderRow),
    otherSheet6Name -> SheetInfo(other, 6, otherSheet6Name, otherSheet6Desc, otherSheet6ValConfig, otherConvertibleHeaderRow),
    otherSheet7Name -> SheetInfo(other, 7, otherSheet7Name, otherSheet7Desc, otherSheet7ValConfig, otherNotionalHeaderRow),
    otherSheet8Name -> SheetInfo(other, 8, otherSheet8Name, otherSheet8Desc, otherSheet8ValConfig, otherEnhancementHeaderRow),
    otherSheet9Name -> SheetInfo(other, 9, otherSheet9Name, otherSheet9Desc, otherSheet9ValConfig, otherSoldHeaderRow)
  )

  val sipSheets: Map[String, SheetInfo] = Map(
    sipSheet1Name   -> SheetInfo(sip,   1, sipSheet1Name,   sipSheet1Desc,   sipSheet1ValConfig,   sipAwardsHeaderRow),
    sipSheet2Name   -> SheetInfo(sip,   2, sipSheet2Name,   sipSheet2Desc,   sipSheet2ValConfig,   sipOutHeaderRow)
  )

  val sayeSheets: Map[String, SheetInfo] = Map(
    sayeSheet1Name  -> SheetInfo(saye,  1, sayeSheet1Name,  sayeSheet1Desc,  sayeSheet1ValConfig,  sayeGrantedHeaderRow),
    sayeSheet2Name  -> SheetInfo(saye,  2, sayeSheet2Name,  sayeSheet2Desc,  sayeSheet2ValConfig,  sayeRCLHeaderRow),
    sayeSheet3Name  -> SheetInfo(saye,  3, sayeSheet3Name,  sayeSheet3Desc,  sayeSheet3ValConfig,  sayeExercisedHeaderRow)
  )

  val ersSheets: Map[String, SheetInfo] = emiSheets ++ otherSheets ++ sipSheets ++ sayeSheets

  val ersSheetsWithCsopV4: Map[String, SheetInfo] = ersSheets ++ csopSheets
  val ersSheetsWithCsopV5: Map[String, SheetInfo] = ersSheets ++ csopSheetsV5
}

object ERSValidationConfigs {

  val defValidator = new DataValidator(getConfig(ERSTemplatesInfo.emiSheet1ValConfig))

  def getValidator(configName: String): DataValidator = new DataValidator(getConfig(configName))

  def getConfig(sheetConfig: String): Config = ConfigFactory.load.getConfig(sheetConfig) //load new config per sheet on iteration
}
