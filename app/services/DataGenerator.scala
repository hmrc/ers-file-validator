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

import com.typesafe.config.ConfigException
import config.ApplicationConfig
import metrics.Metrics
import models._
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

@Singleton
class DataGenerator @Inject()(auditEvents: AuditEvents,
                              applicationConfig: ApplicationConfig)(
                               implicit val ec: ExecutionContext) extends DataParser with Metrics with Logging {

  private[services] def ersSheetsConf(schemeInfo: SchemeInfo): Either[ErsError, Map[String, SheetInfo]] = {
    if (applicationConfig.csopV5Enabled) {
      csopV5required(schemeInfo).map(v5Required => {
        if (v5Required) {
          ersSheetsWithCsopV5
        } else {
          ersSheetsWithCsopV4
        }
      })
    } else {
      Right(ersSheetsWithCsopV4)
    }
  }

  def getErrors(iterator: Iterator[String])(implicit schemeInfo: SchemeInfo, hc: HeaderCarrier, request: Request[_]): Either[ErsError, ListBuffer[SchemeData]] = {
    var rowNum = 0
    implicit var sheetName: String = ""
    var sheetColSize = 0
    val schemeData: ListBuffer[SchemeData] = ListBuffer()
    var validator: DataValidator = ERSValidationConfigs.defValidator

    def incRowNum() = rowNum = rowNum + 1

    val startTime = System.currentTimeMillis()

    def hasMissingHeaders(rowNum: Int): Boolean = {
      rowNum > 0 && rowNum < 9
    }

    Try {
      while (iterator.hasNext) {
        val row = iterator.next()
        val rowData: Either[String, (Seq[String], Int)] = parse(row)
        logger.debug(" parsed data ---> " + rowData + " -- cursor --> " + rowNum)
        if (rowData.isLeft) {
          if (hasMissingHeaders(rowNum)) {
            return Left(HeaderValidationError(
              s"${ErrorResponseMessages.dataParserIncorrectHeader}",
              s"${ErrorResponseMessages.dataParserIncorrectHeader}"))
          }

          identifyAndDefineSheet(rowData.swap.getOrElse("")) match {
            case Left(userError) => return Left(userError)
            case Right(validSheetName) => sheetName = validSheetName
          }

          logger.info(s"Sheetname = $sheetName (schemeRef: ${schemeInfo.schemeRef}) ******")
          logger.info(s"SCHEME TYPE = ${schemeInfo.schemeType} (schemeRef: ${schemeInfo.schemeRef}) ******")
          schemeData += SchemeData(schemeInfo, sheetName, None, ListBuffer())
          logger.info(s"SchemeData = ${schemeData.size} (schemeRef: ${schemeInfo.schemeRef}) ******")
          rowNum = 1
          validator = getValidator(sheetName) match {
            case Left(error) => return Left(error)
            case Right(value) => value
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
                  validateHeaderRow(rd._1, sheetName) match {
                    case Left(userError) => return Left(userError)
                    case Right(size) => sheetColSize = size
                  }
                  incRowNum()
                case _ =>
                  val foundData = rd._1
                  val data = constructColumnData(foundData, sheetColSize)
                  if (!isBlankRow(data)) {
                    generateRowData(data, rowNum, validator) match {
                      case Left(userError) => return Left(userError)
                      case Right(validRowData) => schemeData.last.data += validRowData
                    }
                  }
                  incRowNum()
              }
            }
          }
        }
      }

      if (hasMissingHeaders(rowNum)) {
        return Left(HeaderValidationError(
          s"${ErrorResponseMessages.dataParserIncorrectHeader}",
          s"${ErrorResponseMessages.dataParserIncorrectHeader}"))
      }

      if (schemeData.foldLeft(0)((sum, obj) => sum + obj.data.size) == 0) {
        return Left(NoDataError(
          s"${ErrorResponseMessages.dataParserNoData}",
          s"${ErrorResponseMessages.dataParserNoData}"))
      }
      deliverDataIteratorMetrics(startTime)
      logger.debug("The SchemeData that GetData finally returns: " + schemeData)
      Right(schemeData)

    } match {
      case Success(result) => result
      case Failure(e) =>
        logger.error(s"[DataGenerator][getErrors] Unexpected system error: ${e.getMessage}", e)
        Left(ERSFileProcessingException(
          "System error during file processing",
          s"Unexpected error: ${e.getMessage}"))
    }
  }

  def getValidator(sheetName: String)(implicit schemeInfo: SchemeInfo, hc: HeaderCarrier, request: Request[_]): Either[ErsError, DataValidator] = {
    ersSheetsConf(schemeInfo).flatMap { sheetsConf =>
      try {
        sheetsConf.get(sheetName) match {
          case Some(sheetInfo) => Right(ERSValidationConfigs.getValidator(sheetInfo.configFileName))
          case None =>
            val errorMsg = s"Sheet name: $sheetName does not match any for scheme types."
            auditEvents.fileProcessingErrorAudit(schemeInfo, sheetName, errorMsg)
            logger.error(s"[getValidator] $errorMsg")
            Left(UnknownSheetError(errorMsg, "Invalid sheet configuration"))
        }
      } catch {
        case _: ConfigException.Missing =>
          val errorMsg = "Could not set the validator due to a missing config"
          auditEvents.fileProcessingErrorAudit(schemeInfo, sheetName, errorMsg)
          logger.error(s"[getValidator] $errorMsg for sheet name: $sheetName and scheme type: ${schemeInfo.schemeType}.")
          Left(ErsSystemError(errorMsg, "Config missing"))
      }
    }
  }

  def getValidatorAndSheetInfo(sheetName: String, schemeInfo: SchemeInfo)(
    implicit hc: HeaderCarrier, request: Request[_]): Either[ErsError, (DataValidator, SheetInfo)] = {
    ersSheetsConf(schemeInfo).flatMap { sheetsConf =>
      sheetsConf.get(sheetName) match {
        case Some(sheetInfo) =>
          Try(ERSValidationConfigs.getValidator(sheetInfo.configFileName)) match {
            case Success(validator) => Right((validator, sheetInfo))
            case Failure(e) =>
              auditEvents.fileProcessingErrorAudit(schemeInfo, sheetName, "Could not set the validator")
              logger.error("setValidator has thrown an exception, SheetName: " + sheetName + " Exception message: " + e.getMessage)
              Left(ErsSystemError(
                s"${ErrorResponseMessages.dataParserConfigFailure}",
                "Could not set the validator"))
          }
        case None =>
          auditEvents.fileProcessingErrorAudit(schemeInfo, sheetName, "Could not set the validator")
          logger.warn("[DataGenerator][getValidatorAndSheetInfo] Couldn't identify SheetName")
          Left(UnknownSheetError(
            s"${ErrorResponseMessages.dataParserIncorrectSheetName}",
            s"${ErrorResponseMessages.dataParserUnidentifiableSheetNameContext}"))
      }
    }
  }

  def identifyAndDefineSheet(data: String)(implicit schemeInfo: SchemeInfo, hc: HeaderCarrier, request: Request[_]): Either[ErsError, String] = {
    getSheet(data).flatMap { res =>
      val schemeInfoSchemeType = schemeInfo.schemeType
      val requestSchemeType = res.schemeType
      if (requestSchemeType.toLowerCase == schemeInfoSchemeType.toLowerCase) {
        logger.debug("****5.1.1  data contains data:  *****" + data)
        Right(data)
      } else {
        auditEvents.fileProcessingErrorAudit(schemeInfo, data, s"${res.schemeType.toLowerCase} is not equal to ${schemeInfo.schemeType.toLowerCase}")
        logger.warn(ErrorResponseMessages.dataParserIncorrectSchemeType())
        Left(SchemeTypeMismatchError(
          message = s"${ErrorResponseMessages.dataParserIncorrectSheetName}",
          context = s"${ErrorResponseMessages.dataParserIncorrectSchemeType(Some(schemeInfoSchemeType), Some(requestSchemeType))}",
          expectedSchemeType = schemeInfoSchemeType,
          requestSchemeType = requestSchemeType
        ))
      }
    }
  }

  def getSheet(sheetName: String)(implicit schemeInfo: SchemeInfo, hc: HeaderCarrier, request: Request[_]): Either[ErsError, SheetInfo] = {
    ersSheetsConf(schemeInfo).flatMap { sheetsConf =>
      sheetsConf.get(sheetName) match {
        case Some(sheetInfo) => Right(sheetInfo)
        case None =>
          auditEvents.fileProcessingErrorAudit(schemeInfo, sheetName, "Could not set the validator")
          logger.warn("[DataGenerator][getSheet] Couldn't identify SheetName")
          Left(UnknownSheetError(
            s"${ErrorResponseMessages.dataParserIncorrectSheetName}",
            s"${ErrorResponseMessages.dataParserUnidentifiableSheetNameContext}"))
      }
    }
  }

  def validateHeaderRow(rowData: Seq[String], sheetName: String)(implicit schemeInfo: SchemeInfo, hc: HeaderCarrier, request: Request[_]): Either[ErsError, Int] = {
    val headerFormat = "[^a-zA-Z0-9]"

    getSheet(sheetName).flatMap { sheetInfo =>
      val header = sheetInfo.headerRow.map(_.replaceAll(headerFormat, ""))
      val data = rowData.take(header.size)
      val dataTrim = data.map(_.replaceAll(headerFormat, ""))

      logger.debug("5.3  case 9 sheetName =" + sheetName + "data = " + dataTrim + "header == -> " + header)
      if (dataTrim == header) {
        Right(header.size)
      } else {
        auditEvents.fileProcessingErrorAudit(schemeInfo, sheetName, "Header row invalid")
        logger.warn("Error while reading File + Incorrect ERS Template")
        Left(HeaderValidationError(
          s"${ErrorResponseMessages.dataParserIncorrectHeader}",
          s"${ErrorResponseMessages.dataParserHeadersDontMatch}"))
      }
    }
  }

  def generateRowData(rowData: Seq[String], rowCount: Int, validator: DataValidator)(
    implicit schemeInfo: SchemeInfo, sheetName: String, hc: HeaderCarrier, request: Request[_]): Either[ErsError, Seq[String]] = {

    logger.debug("5.4  case _ rowData is " + rowData)

    Try {
      ErsValidator.validateRow(rowData, rowCount, validator)
    } match {
      case Success(None) => Right(rowData)
      case Success(err: Option[List[ValidationError]]) => {
        // $COVERAGE-OFF$
        logger.debug(s"Error while Validating Row num--> ${rowCount} ")
        logger.debug(s"Rowdata is --> ${rowData.map(res => res)}")
        logger.debug(s"Error column data is  ${err.get.map(_.cell.value)}")
        // $COVERAGE-ON$
        err match {
          case Some(errors) if errors.nonEmpty =>
            val errorDetails = errors.map {
              case ValidationError(cell, _, errorId, errorMsg) => s"column - ${cell.column}, error - $errorId : $errorMsg"}
            logger.warn(s"[DataGenerator][generateRowData] Validation errors found for ${schemeInfo.schemeRef} : ${errorDetails.mkString(" | " )}" )
        }
        err.map {
          auditEvents.validationErrorAudit(_, schemeInfo, sheetName)
        }
        Left(RowValidationError(
          s"${ErrorResponseMessages.dataParserFileInvalid}",
          s"${ErrorResponseMessages.dataParserValidationFailure}",
          Some(rowCount)))
      }
      case Failure(exception) =>
        logger.error(s"[DataGenerator][generateRowData] System error during row validation: ${exception.getMessage}", exception)
        Left(ErsSystemError(
          s"System error during validation",
          s"Validation system failure: ${exception.getMessage}"
        ))
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


  private def csopV5required(schemeInfo: SchemeInfo): Either[ErsError, Boolean] = {
    Try(schemeInfo.taxYear.split("/")(0).toInt >= 2023) match {
      case Success(result) => Right(result)
      case Failure(_) =>
        Left(InvalidTaxYearError(
          "Invalid tax year format",
          s"Invalid tax year format or conversion error: ${schemeInfo.taxYear}, expected format YYYY/YY"
        ))
    }
  }
}
