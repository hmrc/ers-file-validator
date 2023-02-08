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

import config.ApplicationConfig
import metrics.Metrics
import models.{ERSFileProcessingException, SchemeData, SchemeInfo}
import play.api.Logging
import play.api.mvc.Request
import services.ERSTemplatesInfo.ersSheets
import services.audit.AuditEvents
import services.validation.ErsValidator
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.services.validation.DataValidator
import uk.gov.hmrc.services.validation.models.ValidationError
import utils.ErrorResponseMessages

import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success, Try}

@Singleton
class DataGenerator @Inject()(auditEvents: AuditEvents,
                              config: ApplicationConfig)(
  implicit val ec: ExecutionContext) extends DataParser with Metrics with Logging {

  val defaultChunkSize: Int = 10000
  private[services] val ersSheetsClone: Map[String, SheetInfo] = ersSheets

  @throws(classOf[ERSFileProcessingException])
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
      val rowData = parse(row)
      logger.debug(" parsed data ---> " + rowData + " -- cursor --> " + rowNum)
      rowData.isLeft match {
        case true => {
          checkForMissingHeaders(rowNum)
          sheetName = identifyAndDefineSheet(rowData.left.get)
          logger.debug("Sheetname = " + sheetName + "******")
          logger.debug("SCHEME TYPE = " + schemeInfo.schemeType + "******")
          schemeData += SchemeData(schemeInfo, sheetName, None, ListBuffer())
          logger.debug("SchemeData = " + schemeData.size + "******")
          rowNum = 1
          validator = setValidator(sheetName)
        }
        case _ =>
          for (i <- 1 to rowData.right.get._2) {
            rowNum match {
              case count if count < 9 => {
                logger.debug("GetData: incRowNum if count < 9: " + count + " RowNum: " + rowNum)
                incRowNum()
              }
              case 9 => {
                logger.debug("GetData: incRowNum if  9: " + rowNum + "sheetColSize: " + sheetColSize)
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
    logger.debug("The SchemeData that GetData finally returns: " + schemeData)
    schemeData
  }

  def setValidator(sheetName: String)(implicit schemeInfo: SchemeInfo, hc: HeaderCarrier, request: Request[_]): DataValidator = {
    try {
      ERSValidationConfigs.getValidator(ersSheets(sheetName).configFileName)
    } catch {
      case e: Exception => {
        auditEvents.fileProcessingErrorAudit(schemeInfo, sheetName, "Could not set the validator")
        // Adjusting this log until this investigation is complete https://jira.tools.tax.service.gov.uk/browse/DDCE-3208
        //logger.error("setValidator has thrown an exception, SheetName: " + sheetName + " Exception message: " + e.getMessage)
        logger.error("setValidator has thrown an exception. Exception message: " + e.getMessage)
        throw ERSFileProcessingException(
          s"${ErrorResponseMessages.dataParserConfigFailure}",
          "Could not set the validator ")
      }
    }
  }

  def getSheetCsv(sheetName: String, schemeInfo: SchemeInfo)(
    implicit hc: HeaderCarrier, request: Request[_]): Either[Throwable, SheetInfo] = {
    logger.debug(s"[DataGenerator][getSheetCsv] Looking for sheetName: $sheetName")
    ersSheetsClone.get(sheetName) match {
      case Some(sheetInfo) => Right(sheetInfo)
      case _ =>
        auditEvents.fileProcessingErrorAudit(schemeInfo, sheetName, "Could not set the validator")
        logger.warn("[DataGenerator][getSheetCsv] Couldn't identify SheetName")
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
        logger.error("setValidator has thrown an exception, SheetName: " + sheetName + " Exception message: " + e.getMessage)
        Left(ERSFileProcessingException(
          s"${ErrorResponseMessages.dataParserConfigFailure}",
          "Could not set the validator "))
      case (_, Left(e)) => Left(e)
    }
  }

  def identifyAndDefineSheet(data: String)(implicit schemeInfo: SchemeInfo, hc: HeaderCarrier, request: Request[_]): String = {
    logger.debug("5.1  case 0 identifyAndDefineSheet  ")
    val res = getSheet(data)
    if (res.schemeType.toLowerCase == schemeInfo.schemeType.toLowerCase) {
      logger.debug("****5.1.1  data contains data:  *****" + data)
      data
    } else {
      auditEvents.fileProcessingErrorAudit(schemeInfo, data, s"${res.schemeType.toLowerCase} is not equal to ${schemeInfo.schemeType.toLowerCase}")
      // Adjusting this log until this investigation is complete https://jira.tools.tax.service.gov.uk/browse/DDCE-3208
      //logger.warn(s"${ErrorResponseMessages.dataParserIncorrectSchemeType(data)}")
      logger.warn(ErrorResponseMessages.dataParserIncorrectSchemeType())
      throw ERSFileProcessingException(
        s"${ErrorResponseMessages.dataParserIncorrectSchemeType()}",
        s"${ErrorResponseMessages.dataParserIncorrectSchemeType(data)}")
    }
  }

  def getSheet(sheetName: String)(implicit schemeInfo: SchemeInfo, hc: HeaderCarrier, request: Request[_]): SheetInfo = {
    logger.debug(s"Looking for sheetName: ${sheetName}")
    ersSheets.getOrElse(sheetName, {
      auditEvents.fileProcessingErrorAudit(schemeInfo, sheetName, "Could not set the validator")
      logger.warn("[DataGenerator][getSheet] Couldn't identify SheetName")
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

    logger.debug("5.3  case 9 sheetName =" + sheetName + "data = " + dataTrim + "header == -> " + header)
    if(dataTrim == header) {
      header.size
    } else {
      auditEvents.fileProcessingErrorAudit(schemeInfo, sheetName, "Header row invalid")
      logger.warn("Error while reading File + Incorrect ERS Template")
      throw ERSFileProcessingException(
        s"${ErrorResponseMessages.dataParserIncorrectHeader}",
        s"${ErrorResponseMessages.dataParserHeadersDontMatch}")
    }
  }

  def generateRowData(rowData: Seq[String], rowCount: Int, validator: DataValidator)(
    implicit schemeInfo: SchemeInfo, sheetName: String, hc: HeaderCarrier, request: Request[_]): Seq[String] = {

    logger.debug("5.4  case _ rowData is " + rowData)
    ErsValidator.validateRow(rowData, rowCount, validator) match {
      case None => rowData
      case err: Option[List[ValidationError]] => {
        // $COVERAGE-OFF$
        logger.debug(s"Error while Validating Row num--> ${rowCount} ")
        logger.debug(s"Rowdata is --> ${rowData.map(res => res)}")
        logger.debug(s"Error column data is  ${err.get.map(_.cell.value)}")
        // $COVERAGE-ON$
        err.map {
          auditEvents.validationErrorAudit(_, schemeInfo, sheetName)
        }
        throw ERSFileProcessingException(
          s"${ErrorResponseMessages.dataParserFileInvalid}",
          s"${ErrorResponseMessages.dataParserValidationFailure}")
      }
    }
  }

  def constructColumnData(foundData: Seq[String], sheetColSize: Int): Seq[String] = {
    if (foundData.size < sheetColSize) {
      logger.warn(s"Difference between amount of columns ${foundData.size} and amount of headers $sheetColSize")
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

