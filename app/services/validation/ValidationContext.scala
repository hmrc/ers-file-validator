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

package services.validation

import scala.util.matching.Regex
import uk.gov.hmrc.services.validation.{Row, Cell, DataValidator}
import play.api.Logger

object ValidationContext extends ERSValidationFormatters{

  def verifyFormat(formatter: String, data:String):Boolean =
    (!formatter.isEmpty) && new Regex(formatter).pattern.matcher(data).matches

  def verifyDate(data:String):Boolean = {
    try {
      ersDateFormatter.parseDateTime(data)
      true
    } catch {
      case e: IllegalArgumentException => false
    }
  }

  def mandatoryBoolean(condition:String, dataX:String, dataY:String) = {
    if(condition.toLowerCase.equals(dataX.toLowerCase)) notEmpty(dataY)
    else true
  }

  def notEmpty(data: String): Boolean = {
    !(data == null || data.trim.isEmpty)
  }
}

object ErsValidator {
  val colNames = List("A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z","AA","AB","AC","AD","AE","AF","AG","AH","AI","AJ","AK","AL","AM","AN","AO","AP")

  def validateRow(rowData:Seq[String],rowNumber:Int, validator: DataValidator) = {
  try {
    validator.validateRow(Row(rowNumber, getCells(rowData,rowNumber)), Some(ValidationContext))
    } catch {
      case e: Exception => {
        Logger.warn(e.toString)
        throw e
      }
    }
  }

  def getCells(rowData:Seq[String],rowNumber:Int) =
        (rowData zip colNames ).map{case(cellValue,col) => Cell(col.toString,rowNumber,cellValue)}

}
