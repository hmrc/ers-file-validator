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
import services.ERSTemplatesInfo.{ersSheetsWithCsopV4, ersSheetsWithCsopV5}
import services.StaxProcessor.notFoundString
import services.audit.AuditEvents
import services.validation.ErsValidator
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.services.validation.DataValidator
import uk.gov.hmrc.services.validation.models.ValidationError
import utils.ErrorResponseMessages

import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.annotation.tailrec
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

  case class SchemeDataWithValidatorAndSheetColSize(
                                                    listOfSchemeData: Seq[SchemeData] = Seq.empty[SchemeData],
                                                    validator: DataValidator = ERSValidationConfigs.defValidator,
                                                    sheetColSize: Int = 0
                                                    )

  def processRow(rowNum: Int,
                 rowData: (Seq[String], Int),
                 schemeDataWithValidator: SchemeDataWithValidatorAndSheetColSize
                )(implicit sheetName: String, schemeInfo: SchemeInfo, hc: HeaderCarrier, request: Request[_]): SchemeDataWithValidatorAndSheetColSize = {
    if (rowNum == 9){
      logger.debug("[DataGenerator][getErrors] GetData: incRowNum if  9: " + rowNum + "sheetColSize: " + schemeDataWithValidator.sheetColSize)
      logger.debug("[DataGenerator][getErrors] sheetName--->" + sheetName)
      schemeDataWithValidator.copy(sheetColSize = validateHeaderRow(rowData._1, sheetName))
    }
    else if (rowNum > 9) {
      val data: Seq[String] = constructColumnData(rowData._1, schemeDataWithValidator.sheetColSize)
      if (!isBlankRow(data)) {
        val generatedRowData = generateRowData(data, rowNum, schemeDataWithValidator.validator)
        val generatedDataWithRepeats: Seq[Seq[String]] = List.fill(rowData._2)(generatedRowData)
        val updatedData: ListBuffer[Seq[String]] = schemeDataWithValidator.listOfSchemeData.last.data ++ generatedDataWithRepeats
        val updatedLastSchemeData: SchemeData = schemeDataWithValidator.listOfSchemeData.last.copy(data = updatedData)
        schemeDataWithValidator.copy(listOfSchemeData = schemeDataWithValidator.listOfSchemeData.dropRight(1).appended(updatedLastSchemeData))
      }
      else {
        schemeDataWithValidator
      }
    }
    else {
      logger.debug("[DataGenerator][processRow] RowNum: " + rowNum)
      schemeDataWithValidator
    }
  }

  private def checkForMissingHeaders(rowNum: Int): Unit = {
    if (rowNum > 0 && rowNum < 9) {
      throw ERSFileProcessingException(
        s"${ErrorResponseMessages.dataParserIncorrectHeader}",
        s"${ErrorResponseMessages.dataParserIncorrectHeader}")
    }
  }

  @tailrec
  private def processNextRow(staxProcessor: Iterator[String],
                             rowNum: Int = 0,
                             schemeDataWithValidator: SchemeDataWithValidatorAndSheetColSize
                    )(implicit schemeInfo: SchemeInfo, hc: HeaderCarrier, request: Request[_]): Seq[SchemeData] = {
    val listSchemeData: Seq[SchemeData] = schemeDataWithValidator.listOfSchemeData
    if (staxProcessor.hasNext) {
      val nextRowOfData: Either[String, (Seq[String], Int)] = parse(staxProcessor.next())
      nextRowOfData match {
        case Left(parsedData: String) =>
          checkForMissingHeaders(rowNum)
          if (parsedData == notFoundString){
            listSchemeData
          }
          else {
            val sheetName = identifyAndDefineSheet(nextRowOfData.swap.getOrElse(""))
            logger.info(s"Sheetname = $sheetName (schemeRef: ${schemeInfo.schemeRef}) ******")
            logger.info(s"SCHEME TYPE = ${schemeInfo.schemeType} (schemeRef: ${schemeInfo.schemeRef}) ******")
            val updatedSchemeData: SchemeData = SchemeData(schemeInfo, sheetName, None, ListBuffer())
            val updatedSchemeDataWithValidator = schemeDataWithValidator.copy(
              listOfSchemeData = schemeDataWithValidator.listOfSchemeData :+ updatedSchemeData,
              validator = setValidator(sheetName)
            )
            logger.info(s"SchemeData = ${updatedSchemeDataWithValidator.listOfSchemeData.size} (schemeRef: ${schemeInfo.schemeRef}) ******")
            processNextRow(staxProcessor, 1, updatedSchemeDataWithValidator)
          }
        case Right(passedRowData: (Seq[String], Int)) =>
          implicit val sheetName: String = listSchemeData.lastOption.map(_.sheetName).getOrElse("")
          val processedOtherRows: SchemeDataWithValidatorAndSheetColSize = processRow(rowNum, passedRowData, schemeDataWithValidator)
          processNextRow(staxProcessor, rowNum + 1, processedOtherRows)
      }
    }
    else {
      checkForMissingHeaders(rowNum)
      listSchemeData
    }
  }

  @throws(classOf[ERSFileProcessingException])
  def getErrors(staxProcessor: Iterator[String])
               (implicit schemeInfo: SchemeInfo, hc: HeaderCarrier, request: Request[_]): Seq[SchemeData] = {
    val startTime = System.currentTimeMillis()
    val processedSchemeData: Seq[SchemeData] =
      processNextRow(staxProcessor, 0, SchemeDataWithValidatorAndSheetColSize())
    if (processedSchemeData.foldLeft(0)((sum, obj) => sum + obj.data.size) == 0) {
      throw ERSFileProcessingException(
        s"${ErrorResponseMessages.dataParserNoData}",
        s"${ErrorResponseMessages.dataParserNoData}")
    }
    deliverDataIteratorMetrics(startTime)
    logger.debug("The SchemeData that GetData finally returns: " + processedSchemeData)
    processedSchemeData
  }

  def setValidator(sheetName: String)(implicit schemeInfo: SchemeInfo, hc: HeaderCarrier, request: Request[_]): DataValidator = {
    val setValidatorERSFileProcessingException: ERSFileProcessingException = ERSFileProcessingException(
      s"${ErrorResponseMessages.dataParserConfigFailure}",
      "Could not set the validator "
    )
    try {
      ERSValidationConfigs.getValidator(ersSheetsConf(schemeInfo)(sheetName).configFileName)
    } catch {
      case _: ConfigException.Missing =>
        auditEvents.fileProcessingErrorAudit(schemeInfo, sheetName, "Could not set the validator")
        logger.error(s"setValidator has thrown an exception, sheet name: $sheetName does match any for scheme type")
        throw setValidatorERSFileProcessingException
      case e: Exception =>
        auditEvents.fileProcessingErrorAudit(schemeInfo, sheetName, "Could not set the validator")
        logger.error(s"setValidator has thrown an exception, sheet name: $sheetName but scheme type: ${schemeInfo.schemeType} specified." +
          s" Exception message: " + e.getMessage)
        throw setValidatorERSFileProcessingException
    }
  }

  def getSheetCsv(sheetName: String, schemeInfo: SchemeInfo)(
    implicit hc: HeaderCarrier, request: Request[_]): Either[Throwable, SheetInfo] = {
    logger.debug(s"[DataGenerator][getSheetCsv] Looking for sheetName: $sheetName")
    ersSheetsConf(schemeInfo).get(sheetName) match {
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
    val schemeInfoSchemeType = schemeInfo.schemeType.toLowerCase
    if (res.schemeType.toLowerCase == schemeInfoSchemeType) {
      logger.debug("****5.1.1  data contains data:  *****" + data)
      data
    } else {
      auditEvents.fileProcessingErrorAudit(schemeInfo, data, s"${res.schemeType.toLowerCase} is not equal to ${schemeInfo.schemeType.toLowerCase}")
      logger.warn(ErrorResponseMessages.dataParserIncorrectSchemeType())
      throw ERSFileProcessingException(
        s"${ErrorResponseMessages.dataParserIncorrectSheetName}",
        s"${ErrorResponseMessages.dataParserIncorrectSchemeType(schemeInfoSchemeType)}")
    }
  }

  def getSheet(sheetName: String)(implicit schemeInfo: SchemeInfo, hc: HeaderCarrier, request: Request[_]): SheetInfo = {
    logger.info(s"Looking for sheetName: $sheetName (schemeRef: ${schemeInfo.schemeRef})")
    ersSheetsConf(schemeInfo).getOrElse(sheetName, {
      auditEvents.fileProcessingErrorAudit(schemeInfo, sheetName, "Could not set the validator")
      logger.warn("[DataGenerator][getSheet] Couldn't identify SheetName")
      throw ERSFileProcessingException(
        s"${ErrorResponseMessages.dataParserIncorrectSheetName}",
        s"${ErrorResponseMessages.dataParserUnidentifiableSheetName(sheetName)}")
    })
  }

  def validateHeaderRow(rowData: Seq[String], sheetName: String)(implicit schemeInfo: SchemeInfo, hc: HeaderCarrier, request: Request[_]): Int = {
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

