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

package services

import com.typesafe.config.ConfigFactory
import hmrc.gsi.gov.uk.services.validation.DataValidator
import services.ersTemplatesInfo._

/**
 * Created by raghu on 26/01/16.
 */
case class SheetInfo (schemeType:String, sheetId: Int, sheetName:String, sheetTitle:String,configFileName: String, headerRow:List[String])

object ERSTemplatesInfo extends EMITemplateInfo with CsopTemplateInfo with SipTemplateInfo with OtherTemplateInfo with SayeTemplateInfo{

  val sheetNames = Seq(    "CSOP_OptionsGranted_V3",
    "CSOP_OptionsRCL_V3",
    "CSOP_OptionsExercised_V3",
    "EMI40_Adjustments_V3",
    "EMI40_Replaced_V3",
    "EMI40_RLC_V3",
    "EMI40_NonTaxable_V3",
    "EMI40_Taxable_V3",
    "Other_Grants_V3",
    "Other_Options_V3",
    "Other_Acquisition_V3",
    "Other_RestrictedSecurities_V3",
    "Other_OtherBenefits_V3",
    "Other_Convertible_V3",
    "Other_Notional_V3",
    "Other_Enhancement_V3",
    "Other_Sold_V3",
    "SAYE_Granted_V3",
    "SAYE_RCL_V3",
    "SAYE_Exercised_V3",
    "SIP_Awards_V3",
    "SIP_Out_V3"  )


  val ersSheets = Map(
    csopSheet1Name  -> SheetInfo(csop,  1, csopSheet1Name , csopSheet1title, csopSheet1ValConfig,  csopOptionsGrantedHeaderRow),
    csopSheet2Name  -> SheetInfo(csop,  2, csopSheet2Name,  csopSheet2Title, csopSheet2ValConfig,  csopOptionsRCLHeaderRow),
    csopSheet3Name  -> SheetInfo(csop,  3, csopSheet3Name,  csopSheet3Title, csopSheet3ValConfig,  csopOptionsExercisedHeaderRow),
    emiSheet1Name   -> SheetInfo(emi,   1, emiSheet1Name,   emiSheet1Desc,   emiSheet1ValConfig,   emiAdjustmentsHeaderRow),
    emiSheet2Name   -> SheetInfo(emi,   2, emiSheet2Name,   emiSheet2Desc,   emiSheet2ValConfig,   emiReplacedHeaderRow),
    emiSheet3Name   -> SheetInfo(emi,   3, emiSheet3Name,   emiSheet3Desc,   emiSheet3ValConfig,   emiRCLHeaderRow),
    emiSheet4Name   -> SheetInfo(emi,   4, emiSheet4Name,   emiSheet4Desc,   emiSheet4ValConfig,   emiNonTaxableHeaderRow),
    emiSheet5Name   -> SheetInfo(emi,   5, emiSheet5Name,   emiSheet5Desc,   emiSheet5ValConfig,   emiTaxableHeaderRow),
    otherSheet1Name -> SheetInfo(other, 1, otherSheet1Name, otherSheet1Desc, otherSheet1ValConfig, otherGrantsHeaderRow),
    otherSheet2Name -> SheetInfo(other, 2, otherSheet2Name, otherSheet2Desc, otherSheet2ValConfig, otherOptionsHeaderRow),
    otherSheet3Name -> SheetInfo(other, 3, otherSheet3Name, otherSheet3Desc, otherSheet3ValConfig, otherAcquisitionHeaderRow),
    otherSheet4Name -> SheetInfo(other, 4, otherSheet4Name, otherSheet4Desc, otherSheet4ValConfig, otherRestrictedSecuritiesHeaderRow),
    otherSheet5Name -> SheetInfo(other, 5, otherSheet5Name, otherSheet5Desc, otherSheet5ValConfig, otherBenefitsHeaderRow),
    otherSheet6Name -> SheetInfo(other, 6, otherSheet6Name, otherSheet6Desc, otherSheet6ValConfig, otherConvertibleHeaderRow),
    otherSheet7Name -> SheetInfo(other, 7, otherSheet7Name, otherSheet7Desc, otherSheet7ValConfig, otherNotionalHeaderRow),
    otherSheet8Name -> SheetInfo(other, 8, otherSheet8Name, otherSheet8Desc, otherSheet8ValConfig, otherEnhancementHeaderRow),
    otherSheet9Name -> SheetInfo(other, 9, otherSheet9Name, otherSheet9Desc, otherSheet9ValConfig, otherSoldHeaderRow),
    sipSheet1Name   -> SheetInfo(sip,   1, sipSheet1Name,   sipSheet1Desc,   sipSheet1ValConfig,   sipAwardsHeaderRow),
    sipSheet2Name   -> SheetInfo(sip,   2, sipSheet2Name,   sipSheet2Desc,   sipSheet2ValConfig,   sipOutHeaderRow),
    sayeSheet1Name  -> SheetInfo(saye,  1, sayeSheet1Name,  sayeSheet1Desc,  sayeSheet1ValConfig,  sayeGrantedHeaderRow),
    sayeSheet2Name  -> SheetInfo(saye,  2, sayeSheet2Name,  sayeSheet2Desc,  sayeSheet2ValConfig,  sayeRCLHeaderRow),
    sayeSheet3Name  -> SheetInfo(saye,  3, sayeSheet3Name,  sayeSheet3Desc,  sayeSheet3ValConfig,  sayeExercisedHeaderRow)
  )
}

object ERSValidationConfigs {

  val defValidator = DataValidator(getConfig(ERSTemplatesInfo.emiSheet1ValConfig))
def getValidator(configName:String) = DataValidator(getConfig(configName))

def getConfig(sheetConfig:String) = ConfigFactory.load.getConfig(sheetConfig) //load new config per sheet on iteration

  def getColNames(sheetConfig:String) = getConfig(sheetConfig).entrySet()

}
