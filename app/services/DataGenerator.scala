/*
 * Copyright 2021 HM Revenue & Customs
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

import java.util.concurrent.TimeUnit

import config.ApplicationConfig
import javax.inject.{Inject, Singleton}
import metrics.Metrics
import models.{ERSFileProcessingException, SchemeData, SchemeInfo}
import play.api.Logger
import play.api.mvc.Request
import services.ERSTemplatesInfo.ersSheets
import services.audit.AuditEvents
import services.validation.ErsValidator
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.services.validation.DataValidator
import uk.gov.hmrc.services.validation.models.ValidationError
import utils.ErrorResponseMessages

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success, Try}

@Singleton
class DataGenerator @Inject()(auditEvents: AuditEvents,
                              config: ApplicationConfig)(
  implicit val ec: ExecutionContext) extends DataParser with Metrics {

  val defaultChunkSize: Int = 10000
  private[services] val ersSheetsClone: Map[String, SheetInfo] = ersSheets

  def getErrors(iterator: Iterator[String])(implicit schemeInfo: SchemeInfo, hc: HeaderCarrier, request: Request[_]): ListBuffer[SchemeData] = {
    var rowNum = 0
    implicit var sheetName: String = ""
    var sheetColSize = 0
    val schemeData: ListBuffer[SchemeData] = ListBuffer()
    var validator: DataValidator = ERSValidationConfigs.defValidator

    def incRowNum() = rowNum = rowNum + 1

    val startTime = System.currentTimeMillis()

    def checkForMissingHeaders(rowNum: Int) = {
      if (rowNum > 0 && rowNum < 9) {
        throw ERSFileProcessingException(
          s"${ErrorResponseMessages.dataParserIncorrectHeader}",
          s"${ErrorResponseMessages.dataParserIncorrectHeader}")
      }
    }

    while (iterator.hasNext) {

      val row = iterator.next()
      //Logger.debug(" Data before  parsing ---> " + row)
      val rowData = parse(row)
      Logger.debug(" parsed data ---> " + rowData + " -- cursor --> " + rowNum)
      rowData.isLeft match {
        case true => {
          checkForMissingHeaders(rowNum)
          sheetName = identifyAndDefineSheet(rowData.left.get)
          Logger.debug("Sheetname = " + sheetName + "******")
          Logger.debug("SCHEME TYPE = " + schemeInfo.schemeType + "******")
          schemeData += SchemeData(schemeInfo, sheetName, None, ListBuffer())
          Logger.debug("SchemeData = " + schemeData.size + "******")
          rowNum = 1
          validator = setValidator(sheetName)
        }
        case _ =>
          for (i <- 1 to rowData.right.get._2) {
            rowNum match {
              case count if count < 9 => {
                Logger.debug("GetData: incRowNum if count < 9: " + count + " RowNum: " + rowNum)
                incRowNum()
              }
              case 9 => {
                Logger.debug("GetData: incRowNum if  9: " + rowNum + "sheetColSize: " + sheetColSize)
                sheetColSize = validateHeaderRow(rowData.right.get._1, sheetName)
                incRowNum()
              }
              case _ => {
                val foundData = rowData.right.get._1

                val data = constructColumnData(foundData, sheetColSize)

                if (!isBlankRow(data)) {
                  schemeData.last.data += generateRowData(data, rowNum, validator) //(schemeInfo,sheetName)
                }
                incRowNum()
              }
            }
          }
      }
    }

    checkForMissingHeaders(rowNum)
    if (schemeData.foldLeft(0)((sum, obj) => sum + obj.data.size) == 0) {
      throw ERSFileProcessingException(
        s"${ErrorResponseMessages.dataParserNoData}",
        s"${ErrorResponseMessages.dataParserNoData}")
    }
    deliverDataIteratorMetrics(startTime)
    Logger.debug("The SchemeData that GetData finally returns: " + schemeData)
    schemeData
  }

  def setValidator(sheetName: String)(implicit schemeInfo: SchemeInfo, hc: HeaderCarrier, request: Request[_]): DataValidator = {
    try {
      ERSValidationConfigs.getValidator(ersSheets(sheetName).configFileName)
    } catch {
      case e: Exception => {
        auditEvents.fileProcessingErrorAudit(schemeInfo, sheetName, "Could not set the validator")
        Logger.error("setValidator has thrown an exception, SheetName: " + sheetName + " Exception message: " + e.getMessage)
        throw ERSFileProcessingException(
          s"${ErrorResponseMessages.dataParserConfigFailure}",
          "Could not set the validator ")
      }
    }
  }

  def getSheetCsv(sheetName: String, schemeInfo: SchemeInfo)(
    implicit hc: HeaderCarrier, request: Request[_]): Either[Throwable, SheetInfo] = {
    Logger.debug(s"[DataGenerator][getSheetCsv] Looking for sheetName: $sheetName")
    ersSheetsClone.get(sheetName) match {
      case Some(sheetInfo) => Right(sheetInfo)
      case _ =>
        auditEvents.fileProcessingErrorAudit(schemeInfo, sheetName, "Could not set the validator")
        Logger.warn("[DataGenerator][getSheetCsv] Couldn't identify SheetName")
        Left(ERSFileProcessingException(
          s"${ErrorResponseMessages.dataParserIncorrectSheetName}",
          s"${ErrorResponseMessages.dataParserUnidentifiableSheetName(sheetName)}"))
    }
  }

  def getValidatorAndSheetInfo(sheetName: String, schemeInfo: SchemeInfo)(
    implicit hc: HeaderCarrier, request: Request[_]): Either[Throwable, (DataValidator, SheetInfo)] = {
    (Try(ERSValidationConfigs.getValidator(ersSheets(sheetName).configFileName)), getSheetCsv(sheetName, schemeInfo)) match {
      case (Success(validator), Right(value)) => Right((validator, value))
      case (Failure(e), _) =>
        auditEvents.fileProcessingErrorAudit(schemeInfo, sheetName, "Could not set the validator")
        Logger.error("setValidator has thrown an exception, SheetName: " + sheetName + " Exception message: " + e.getMessage)
        Left(ERSFileProcessingException(
          s"${ErrorResponseMessages.dataParserConfigFailure}",
          "Could not set the validator "))
      case (_, Left(e)) => Left(e)
    }
  }

  def identifyAndDefineSheet(data: String)(implicit schemeInfo: SchemeInfo, hc: HeaderCarrier, request: Request[_]): String = {
    Logger.debug("5.1  case 0 identifyAndDefineSheet  ")
    val res = getSheet(data)
    if (res.schemeType.toLowerCase == schemeInfo.schemeType.toLowerCase) {
      Logger.debug("****5.1.1  data contains data:  *****" + data)
      data
    } else {
      auditEvents.fileProcessingErrorAudit(schemeInfo, data, s"${res.schemeType.toLowerCase} is not equal to ${schemeInfo.schemeType.toLowerCase}")
      Logger.warn(s"${ErrorResponseMessages.dataParserIncorrectSchemeType(data)}")
      throw ERSFileProcessingException(
        s"${ErrorResponseMessages.dataParserIncorrectSchemeType()}",
        s"${ErrorResponseMessages.dataParserIncorrectSchemeType(data)}")
    }
  }

  def getSheet(sheetName: String)(implicit schemeInfo: SchemeInfo, hc: HeaderCarrier, request: Request[_]): SheetInfo = {
    Logger.debug(s"Looking for sheetName: ${sheetName}")
    ersSheets.getOrElse(sheetName, {
      auditEvents.fileProcessingErrorAudit(schemeInfo, sheetName, "Could not set the validator")
      Logger.warn(s"${ErrorResponseMessages.dataParserUnidentifiableSheetName(sheetName)}")
      throw ERSFileProcessingException(
        s"${ErrorResponseMessages.dataParserIncorrectSheetName}",
        s"${ErrorResponseMessages.dataParserUnidentifiableSheetName(sheetName)}")
    })
  }

  def validateHeaderRow(rowData: Seq[String], sheetName: String)(implicit schemeInfo: SchemeInfo, hc: HeaderCarrier, request: Request[_]) = {
    val headerFormat = "[^a-zA-Z0-9]"

    val header = getSheet(sheetName)(schemeInfo, hc, request).headerRow.map(_.replaceAll(headerFormat, ""))
    val data = rowData.take(header.size)
    val dataTrim = data.map(_.replaceAll(headerFormat, ""))

    Logger.debug("5.3  case 9 sheetName =" + sheetName + "data = " + dataTrim + "header == -> " + header)
    if(dataTrim == header) {
      header.size
    } else {
      auditEvents.fileProcessingErrorAudit(schemeInfo, sheetName, "Header row invalid")
      Logger.warn("Error while reading File + Incorrect ERS Template")
      throw ERSFileProcessingException(
        s"${ErrorResponseMessages.dataParserIncorrectHeader}",
        s"${ErrorResponseMessages.dataParserHeadersDontMatch}")
    }
  }

  def generateRowData(rowData: Seq[String], rowCount: Int, validator: DataValidator)(
    implicit schemeInfo: SchemeInfo, sheetName: String, hc: HeaderCarrier, request: Request[_]): Seq[String] = {

    Logger.debug("5.4  case _ rowData is " + rowData)
    ErsValidator.validateRow(rowData, rowCount, validator) match {
      case None => rowData
      case err: Option[List[ValidationError]] => {
        Logger.debug(s"Error while Validating Row num--> ${rowCount} ")
        Logger.debug(s"Rowdata is --> ${rowData.map(res => res)}")
        Logger.debug(s"Error column data is  ${err.get.map(_.cell.value)}")
        err.map {
          auditEvents.validationErrorAudit(_, schemeInfo, sheetName)
        }
        throw ERSFileProcessingException(
          s"${ErrorResponseMessages.dataParserFileInvalid}",
          s"${ErrorResponseMessages.dataParserValidationFailure}")
      }
      case _ => {
        auditEvents.fileProcessingErrorAudit(schemeInfo, sheetName, "Failure to validate")
        Logger.warn("Error while Validating File + Formatting errors present ")
        throw ERSFileProcessingException(
          s"${ErrorResponseMessages.dataParserFileInvalid}",
          s"${ErrorResponseMessages.dataParserValidationFailure}")
      }
    }
  }

  def constructColumnData(foundData: Seq[String], sheetColSize: Int): Seq[String] = {
    if (foundData.size < sheetColSize) {
      Logger.warn(s"Difference between amount of columns ${foundData.size} and amount of headers $sheetColSize")
      val additionalEmptyCells: Seq[String] = List.fill(sheetColSize - foundData.size)("")
      (foundData ++ additionalEmptyCells).take(sheetColSize)
    }
    else {
      foundData.take(sheetColSize)
    }
  }

  def deliverDataIteratorMetrics(startTime: Long): Unit =
    metrics.dataIteratorTimer(System.currentTimeMillis() - startTime, TimeUnit.MILLISECONDS)

}

