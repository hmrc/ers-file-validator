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
import models.{ERSFileProcessingException, ERSFileProcessingExceptionWithSchemeTypes, SchemeData, SchemeInfo}
import play.api.Logging
import play.api.mvc.Request
import services.ERSTemplatesInfo.{ersSheetsWithCsopV4, ersSheetsWithCsopV5}
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
import com.typesafe.config.ConfigException

@Singleton
class DataGenerator @Inject()(auditEvents: AuditEvents,
                              applicationConfig: ApplicationConfig)(
  implicit val ec: ExecutionContext) extends DataParser with Metrics with Logging {

  private[services] def ersSheetsConf(schemeInfo: SchemeInfo): Map[String, SheetInfo] =
    if (applicationConfig.csopV5Enabled && csopV5required(schemeInfo)) ersSheetsWithCsopV5 else ersSheetsWithCsopV4

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
      if (rowData.isLeft) {
        checkForMissingHeaders(rowNum)
        sheetName = identifyAndDefineSheet(rowData.swap.getOrElse(""))
        logger.info(s"Sheetname = $sheetName (schemeRef: ${schemeInfo.schemeRef}) ******")
        logger.info(s"SCHEME TYPE = ${schemeInfo.schemeType} (schemeRef: ${schemeInfo.schemeRef}) ******")
        schemeData += SchemeData(schemeInfo, sheetName, None, ListBuffer())
        logger.info(s"SchemeData = ${schemeData.size} (schemeRef: ${schemeInfo.schemeRef}) ******")
        rowNum = 1
        validator = getValidator(sheetName) match {
          case Left(exception: ERSFileProcessingException) => throw exception
          case Right(value: DataValidator) => value
        }
      } else {
        rowData.map { rd =>
          (1 to rd._2).foreach { _ =>
            rowNum match {
              case count if count < 9 =>
                logger.debug("[DataGenerator][getErrors] GetData: incRowNum if count < 9: " + count + " RowNum: " + rowNum)
                incRowNum()
              case 9 =>
                logger.debug("[DataGenerator][getErrors] GetData: incRowNum if  9: " + rowNum + "sheetColSize: " + sheetColSize)
                logger.debug("[DataGenerator][getErrors] sheetName--->" + sheetName)
                sheetColSize = validateHeaderRow(rd._1, sheetName)
                incRowNum()
              case _ =>
                val foundData = rd._1
                val data = constructColumnData(foundData, sheetColSize)
                if (!isBlankRow(data)) {
                  schemeData.last.data += generateRowData(data, rowNum, validator)
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

  private def getValidatorException(errorMsg: String): ERSFileProcessingException =
    ERSFileProcessingException(
      ErrorResponseMessages.dataParserConfigFailure,
      errorMsg
    )

  def getValidator(sheetName: String)
                  (implicit schemeInfo: SchemeInfo, hc: HeaderCarrier, request: Request[_]): Either[ERSFileProcessingException, DataValidator] = {
      try {
        Right(ERSValidationConfigs.getValidator(ersSheetsConf(schemeInfo)(sheetName).configFileName))
      } catch {
        case _: ConfigException.Missing =>
          val errorMsg = "Could not set the validator due to a missing config"
          auditEvents.fileProcessingErrorAudit(schemeInfo, sheetName, errorMsg)
          logger.error(s"[getValidator] $errorMsg for sheet name: $sheetName and scheme type: ${schemeInfo.schemeType}.")
          Left(getValidatorException(errorMsg))
        case _: java.util.NoSuchElementException =>
          val errorMsg = s"Sheet name: $sheetName does not match any for scheme types."
          auditEvents.fileProcessingErrorAudit(schemeInfo, sheetName, errorMsg)
          logger.error(s"[getValidator] $errorMsg")
          Left(getValidatorException(errorMsg))
      }
  }

  def getSheetCsv(sheetName: String, schemeInfo: SchemeInfo)(
    implicit hc: HeaderCarrier, request: Request[_]): Either[Throwable, SheetInfo] = {
    ersSheetsConf(schemeInfo).get(sheetName) match {
      case Some(sheetInfo) => Right(sheetInfo)
      case _ =>
        auditEvents.fileProcessingErrorAudit(schemeInfo, sheetName, "Could not set the validator")
        logger.warn("[DataGenerator][getSheetCsv] Couldn't identify SheetName")
        Left(ERSFileProcessingException(
          s"${ErrorResponseMessages.dataParserIncorrectSheetName}",
          s"${ErrorResponseMessages.dataParserUnidentifiableSheetNameContext}"))
    }
  }

  def getValidatorAndSheetInfo(sheetName: String, schemeInfo: SchemeInfo)(
    implicit hc: HeaderCarrier, request: Request[_]): Either[Throwable, (DataValidator, SheetInfo)] = {
    (Try(ERSValidationConfigs.getValidator(ersSheetsConf(schemeInfo)(sheetName).configFileName)), getSheetCsv(sheetName, schemeInfo)) match {
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
    val schemeInfoSchemeType = schemeInfo.schemeType
    val requestSchemeType = res.schemeType
    if (requestSchemeType.toLowerCase == schemeInfoSchemeType.toLowerCase) {
      logger.debug("****5.1.1  data contains data:  *****" + data)
      data
    } else {
      auditEvents.fileProcessingErrorAudit(schemeInfo, data, s"${res.schemeType.toLowerCase} is not equal to ${schemeInfo.schemeType.toLowerCase}")
      logger.warn(ErrorResponseMessages.dataParserIncorrectSchemeType())
      throw ERSFileProcessingExceptionWithSchemeTypes(
        message = s"${ErrorResponseMessages.dataParserIncorrectSheetName}",
        context = s"${ErrorResponseMessages.dataParserIncorrectSchemeType(
          Some(schemeInfoSchemeType),
          Some(requestSchemeType)
        )}",
        expected = schemeInfoSchemeType,
        actual = requestSchemeType
      )
    }
  }

  def getSheet(sheetName: String)(implicit schemeInfo: SchemeInfo, hc: HeaderCarrier, request: Request[_]): SheetInfo = {
    ersSheetsConf(schemeInfo).getOrElse(sheetName, {
      auditEvents.fileProcessingErrorAudit(schemeInfo, sheetName, "Could not set the validator")
      logger.warn("[DataGenerator][getSheet] Couldn't identify SheetName")
      throw ERSFileProcessingException(
        s"${ErrorResponseMessages.dataParserIncorrectSheetName}",
        s"${ErrorResponseMessages.dataParserUnidentifiableSheetNameContext}")
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

  @throws[IllegalArgumentException]
  private def csopV5required(schemeInfo: SchemeInfo): Boolean = {
    Try(schemeInfo.taxYear.split("/")(0).toInt >= 2023)
      .getOrElse(throw new IllegalArgumentException(s"Invalid tax year format or conversion error: ${schemeInfo.taxYear}, expected format YYYY/YY"))
  }
}

