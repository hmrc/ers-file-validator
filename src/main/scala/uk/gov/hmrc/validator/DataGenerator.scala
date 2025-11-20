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

package uk.gov.hmrc.validator

import play.api.Logger
import play.api.i18n.Messages
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.services.validation.DataValidator
import uk.gov.hmrc.validator.ERSTemplatesInfo.{ersSheets, ersSheetsWithCsopV5}
import uk.gov.hmrc.validator.models.{ERSFileProcessingException, SheetErrors}
import uk.gov.hmrc.validator.utils.ContentUtil
import uk.gov.hmrc.validator.utils.ParserUtil.formatDataToValidate
import uk.gov.hmrc.validator.validation.ErsValidator

import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success, Try}

@Singleton
class DataGenerator @Inject()(ersValidationConfigs: ERSValidationConfigs,
                              ersValidator: ErsValidator
                             ) extends DataParser with ContentUtil {

  val logger: Logger = Logger(getClass)

  val ersSheetsConfig: Map[String, SheetInfo] = if (true) ersSheetsWithCsopV5 else ersSheets // TODO: Pass in appConfigV5 bool

  def getErrors(iterator: Iterator[String], scheme: String, fileName: String)
               (implicit messages: Messages): ListBuffer[SheetErrors] = {

    var rowNum = 0
    implicit var sheetName: String = ""
    var sheetColSize = 0
    val schemeErrors: ListBuffer[SheetErrors] = ListBuffer()
    var validator: DataValidator = ersValidationConfigs.defValidator

    def incRowNum(): Unit = rowNum =  rowNum + 1
    var rowCount: Int = 0
    var rowsWithData: Int = 0
    val startTime = System.currentTimeMillis()

    while(iterator.hasNext) {
      val row = iterator.next()
      val rowData = parse(row, fileName)
      logger.debug("[DataGenerator][getErrors] parsed data ---> " + rowData + " -- cursor --> " + rowNum)
      if (rowData.isLeft) {
        checkForMissingHeaders(rowNum, sheetName, fileName)
        logger.debug("[DataGenerator][getErrors] data from the left --->" + rowData.swap.getOrElse(""))
        sheetName = identifyAndDefineSheet(rowData.swap.getOrElse(""), scheme)
        logger.debug("[DataGenerator][getErrors] Sheetname = " + sheetName + "******")
        schemeErrors += SheetErrors(sheetName, ListBuffer())
        logger.debug("[DataGenerator][getErrors] SchemeData = " + schemeErrors.size + "******")
        rowNum = 1
        validator = setValidator(sheetName)
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
                sheetColSize = validateHeaderRow(rd._1, sheetName, scheme, fileName)
                incRowNum()
              case _ =>
                val foundData = rd._1
                rowCount = foundData.size
                val data = formatDataToValidate(foundData, sheetName)
                if (!isBlankRow(data)) {
                  rowsWithData += 1
                  ersValidator.validateRow(validator)(data, rowNum) match {
                    case Some(errors) if errors.nonEmpty =>
                      logger.debug("[DataGenerator][getErrors] Error while Validating File + Formatting errors present " + errors.toString)
                      schemeErrors.last.errors ++= errors
                    case _ => schemeErrors.last.errors
                  }
                }
                incRowNum()
            }
          }
        }
      }
    }

    checkForMissingHeaders(rowNum, sheetName, fileName)
    if(rowsWithData == 0) {
      throw ERSFileProcessingException(
        "ers.exceptions.dataParser.noData",
        Messages("ers.exceptions.dataParser.noData"), needsExtendedInstructions = true)
    }
//    auditEvents.numRowsInSchemeData(scheme, rowsWithData)(hc, request, ec)
    logger.debug("[DataGenerator][getErrors] The SchemeData that GetData finally returns: " + schemeErrors)
    schemeErrors
  }

  def checkForMissingHeaders(rowNum: Int, sheetName: String, fileName: String)(implicit messages: Messages): Unit = {
    if(rowNum > 0 && rowNum < 9) {
      throw ERSFileProcessingException(
        "ers.exceptions.dataParser.incorrectHeader",
        Messages("ers.exceptions.dataParser.incorrectHeader", sheetName, fileName),
        needsExtendedInstructions = true,
        optionalParams = Seq(sheetName, fileName)
      )
    }
  }

  def setValidator(sheetName: String)(implicit messages: Messages): DataValidator = {
    try {
      getValidator(sheetName)
    } catch {
      case e: Exception =>
        logger.error("[DataGenerator][setValidator] setValidator has thrown an exception, SheetName: " + sheetName + " Exception message: " + e.getMessage)
        throw ERSFileProcessingException(
          "ers.exceptions.dataParser.configFailure",
          Messages("ers.exceptions.dataParser.validatorError"),
          optionalParams = Seq(sheetName)
        )
    }
  }

  def setValidatorCsv(sheetName: String)(implicit hc : HeaderCarrier, messages: Messages): Either[Throwable, DataValidator] = {
    Try {
      getValidator(sheetName)
    } match {
      case Success(validator) => Right(validator)
      case Failure(e) =>
        logger.error("[DataGenerator][setValidatorCsv] setValidator has thrown an exception, SheetName: " + sheetName + " Exception message: " + e.getMessage)
        Left(ERSFileProcessingException(
          "ers.exceptions.dataParser.configFailure",
          Messages("ers.exceptions.dataParser.validatorError"),
          optionalParams = Seq(sheetName)
        ))
    }
  }

  private def getValidator(sheetName: String): DataValidator =
    ersValidationConfigs.getValidator(ersSheetsConfig(sheetName).configFileName)

  def identifyAndDefineSheet(filename: String, scheme: String)(implicit messages: Messages): String = {
    logger.debug("5.1  case 0 identifyAndDefineSheet  " )
    val sheetInfo = getSheet(filename, scheme)
    val schemeName = getSchemeName(scheme)._2
    if (sheetInfo.schemeType.toLowerCase == schemeName.toLowerCase) {
      logger.debug("****5.1.1  data contains data:  *****" + filename)
      filename
    } else {
//      auditEvents.fileProcessingErrorAudit(sheetInfo.schemeType, sheetInfo.sheetName,
//        s"${sheetInfo.schemeType.toLowerCase} is not equal to ${schemeName.toLowerCase}")
      logger.warn(Messages("ers.exceptions.dataParser.incorrectSchemeType", sheetInfo.schemeType.toUpperCase, schemeName.toUpperCase))
      throw ERSFileProcessingException(
        "ers.exceptions.dataParser.incorrectSchemeType",
        Messages("ers.exceptions.dataParser.incorrectSchemeType", sheetInfo.schemeType.toLowerCase, schemeName.toLowerCase),
        optionalParams = Seq(withArticle(sheetInfo.schemeType.toUpperCase), withArticle(schemeName.toUpperCase), sheetInfo.sheetName))
    }
  }

   def identifyAndDefineSheetCsv(informationOnInput: (SheetInfo, String))(
     implicit hc: HeaderCarrier, messages: Messages): Either[Throwable, String] = {
     val (uploadedFileInfo, selectedSchemeName) = informationOnInput

     if (uploadedFileInfo.schemeType.toLowerCase == selectedSchemeName.toLowerCase) {
       Right(uploadedFileInfo.sheetName)
     } else {
       logger.warn(
         s"[DataGenerator][identifyAndDefineSheetEither] The user selected $selectedSchemeName but actually uploaded a ${uploadedFileInfo.schemeType} file"
       )
       Left(ERSFileProcessingException(
         "ers.exceptions.dataParser.incorrectSchemeType",
         Messages("ers.exceptions.dataParser.incorrectSchemeType", uploadedFileInfo.schemeType.toLowerCase, selectedSchemeName.toLowerCase),
         optionalParams = Seq(
           withArticle(uploadedFileInfo.schemeType.toUpperCase),
           withArticle(selectedSchemeName.toUpperCase),
           uploadedFileInfo.sheetName))
       )
     }
   }

  def getSheet(sheetName: String, scheme: String)(implicit messages: Messages): SheetInfo = {
    logger.info(s"[DataGenerator][getSheet] Looking for sheetName: $sheetName")
    ersSheetsConfig.getOrElse(sheetName, {
      logger.warn("[DataGenerator][getSheet] Couldn't identify SheetName")
      val schemeName = getSchemeName(scheme)._2
      throw ERSFileProcessingException(
        "ers.exceptions.dataParser.incorrectSheetName",
        Messages("ers.exceptions.dataParser.unidentifiableSheetName") + " " + sheetName,
        needsExtendedInstructions = true,
        optionalParams = Seq(sheetName, schemeName)
      )
    })
  }

  def getSheetCsv(sheetName: String, scheme: String)(implicit messages: Messages): Either[Throwable, (SheetInfo, String)] = {
    logger.info(s"[DataGenerator][getSheetCsv] Looking for sheetName: $sheetName")
    val selectedSchemeName = getSchemeName(scheme)._2
    ersSheetsConfig.get(sheetName) match {
      case Some(sheetInfo) => Right((sheetInfo, selectedSchemeName))
      case _ =>
        logger.warn("[DataGenerator][getSheetCsv] Couldn't identify SheetName")
        Left(ERSFileProcessingException(
          "ers.exceptions.dataParser.incorrectSheetName",
          Messages("ers.exceptions.dataParser.unidentifiableSheetName") + " " + sheetName,
          needsExtendedInstructions = true,
          optionalParams = Seq(sheetName, selectedSchemeName)))
    }
  }

  def validateHeaderRow(rowData:Seq[String], sheetName:String, scheme:String, fileName: String)(implicit messages: Messages): Int =
  {
    val headerFormat = "[^a-zA-Z0-9]"

    val header = getSheet(sheetName, scheme).headerRow.map(_.replaceAll(headerFormat,""))
    val data = rowData.take(header.size)
    val dataTrim = data.map(_.replaceAll(headerFormat,""))

    logger.debug("5.3  case 9 sheetName =" + sheetName + "data = " + dataTrim + "header == -> " + header)
    if (dataTrim == header) {
      header.size
    } else {
      logger.warn("[DataGenerator][validateHeaderRow] Error while reading File + Incorrect ERS Template")
      throw ERSFileProcessingException(
        "ers.exceptions.dataParser.incorrectHeader",
        Messages("ers.exceptions.dataParser.headersDontMatch"),
        needsExtendedInstructions = true,
        optionalParams = Seq(sheetName, fileName)
      )
    }
  }
}
