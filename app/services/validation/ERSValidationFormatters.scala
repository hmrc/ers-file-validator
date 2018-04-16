/*
 * Copyright 2018 HM Revenue & Customs
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

package services.validation

import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}

trait ERSValidationFormatters {
  val ERSValidationFormatUpTo9 = """\\-[1-9]+|\\-[1-9]+\\.[1-9]+|[1-9]{1}|[1-9]{1}\\.[0-9]+"""
  val ERSValidationFormatNumberMax6 = """([0-9]{0,6}|0|[0-9]{0,6}\.[0-9]+)"""
  val ERSValidationFormatWholeNumber = """(\\-[0-9]+|[0-9]+)"""
  val ERSValidationFormatNumber8 = """([0-9]{8})"""
  val ERSValidationFormat2DecimalPlaces = """([0-9]+\.[0-9]{2})"""
  val ERSValidationFormatNumber = """([0-9]*|0|[0-9]*\.[0-9]+)"""
  val ERSValidationFormatNumberMax11 = """([0-9]{0,11}|[0-9]{0,11}\.[0-9]+)"""
  val ERSValidationFormat4DecimalPlaces = """([0-9]+\.[0-9]{4})""""
  val ERSValidationFormatNumberMax13 = """([0-9]{0,13}|[0-9]{0,13}\.[0-9]+)"""
  val ERSValidationFormatYesNo = """(?i)(\byes\b)|(\bno\b)"""
  val ERSValidationFormatHMRCRef = """([0-9a-zA-Z]{1,10})"""
  val ERSValidationFormat1To8 = """[1-8]{1}|[1-8]{1}\\.[0-9]+"""
  val ERSValidationFormat1To9 = """[1-9]{1}|[1-9]{1}\\.[0-9]+"""
  val ERSValidationFormatName = "(?!.*  )[a-zA-Z-' ]{1,35}"
  val ERSValidationFormatNino =  """[[A-Z]&&[^DFIQUV]][[A-Z]&&[^DFIQUVO]] ?\d{2} ?\d{2} ?\d{2} ?[A-Z]{1}"""
  val ERSValidationFormatPaye = """[a-zA-Z0-9/]{1,14}"""
  val ERSValidationFormatCompanyName = "(?!.*  )[a-zA-Z0-9-' ]{1,120}"
  val ERSValidationFormatCompanyAddress1to3 = "(?!.*  )[a-zA-Z0-9-' ]{1,27}"
  val ERSValidationFormatAddress4County = "(?!.*  )[a-zA-Z0-9-' ]{1,18}"
  val ERSValidationFormatPostcode = "[A-Z0-9 ]{6,8}"
  val ERSValidationFormatCRN = "[a-zA-Z0-9]{1,10}"
  val ERSValidationFormatCTRef = "[0-9]{10}"
  val ERSValidationFormatSrn = "[0-9]{8}"
  val ERSVAlidationFormatDropDown3Opt = "[1-3]{1}"
  val ERSValidationFormatAllSome = """(?i)(\ball\b)|(\bsome\b)"""
  val ERSValidationFormat6digits1decimalplaces = """([0-9]{0,6}\.[0-9]{1})"""
  val ERSValidationFormat11with2DecimalPlaces = """([0-9]{0,6}|[0-9]{0,6}\.[0-9]{2})"""

  val Srn = "[0-9]{8}"
  val ersDateFormatter: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-mm-dd")

  val ERSValidationFormatNumber1to4 = """([1-4]{1})"""
  val ERSValidationFormatMax1 = """([0-9]{1})"""
  val ERSValidationFormatRatio = """([0-9]+\:[0-9]+|[0-9]+\/[0-9]+)"""


}
