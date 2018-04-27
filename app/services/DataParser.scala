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

package services

import java.util.concurrent.TimeUnit

import javax.xml.parsers.SAXParserFactory
import metrics.Metrics
import models._
import play.api.Logger
import play.api.Play.current
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.mvc.Request
import services.ERSTemplatesInfo._
import services.audit.AuditEvents
import services.validation.ErsValidator
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.services.validation.{DataValidator, ValidationError}

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success, Try}
import scala.xml._

trait DataParser {

  val repeatColumnsAttr = "table:number-columns-repeated"
  val repeatTableAttr = "table:number-rows-repeated"
  val auditEvents: AuditEvents = AuditEvents

  def secureSAXParser = {
    val saxParserFactory = SAXParserFactory.newInstance()
    saxParserFactory.setFeature("http://xml.org/sax/features/external-general-entities", false)
    saxParserFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
    saxParserFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
    saxParserFactory.newSAXParser()
  }

  def parse(row: String): Either[String, (Seq[String], Int)] = {
    Logger.debug("DataParser: Parse: About to parse row: " + row)
    val xmlRow = Try(Option(XML.withSAXParser(secureSAXParser)loadString(row))).getOrElse(None)
    //    Logger.debug("DataParser: Parse: About to match xmlRow: " + xmlRow)
    xmlRow match {
      case None => Logger.debug("3.1 Parse row left "); Left(row)
      case elem: Option[Elem] => Logger.debug("3.2 Parse row right ")
        val cols = Try(Right(xmlRow.get.child.flatMap(parseColumn(_)))).getOrElse {
          Logger.warn(Messages("ers.exceptions.dataParser.fileRetrievalFailed"));
          throw ERSFileProcessingException(Messages("ers.exceptions.dataParser.fileRetrievalFailed"), Messages("ers.exceptions.dataParser.parserFailure"))
        }

        cols match {
          case Right(r: Seq[String]) if !isBlankRow(r) => Right(r, repeated(xmlRow))
          case Right(s: Seq[String]) => Right((s, 1))
        }
      case _ => {
        Logger.warn(Messages("ers.exceptions.dataParser.fileParsingError"))
        throw ERSFileProcessingException(Messages("ers.exceptions.dataParser.fileParsingError"), Messages("ers.exceptions.dataParser.parsingOfFileData"))
      }
    }
  }

  def repeated(xmlRow: Option[Elem]): Int = {
    val rowsRepeated = xmlRow.get.attributes.asAttrMap.get(repeatTableAttr)
    if (rowsRepeated.isDefined) {
      rowsRepeated.get.toInt
    }
    else {
      1
    }
  }

  def parseColumn(col: scala.xml.Node): Seq[String] = {
    val colsRepeated = col.attributes.asAttrMap.get(repeatColumnsAttr)

    if (colsRepeated.nonEmpty && colsRepeated.get.toInt < 50) {
      val cols: scala.collection.mutable.MutableList[String] = scala.collection.mutable.MutableList()
      for (i <- 1 to colsRepeated.get.toInt) cols += col.text
      cols.toSeq
    }
    else Seq(col.text)
  }

  def isBlankRow(data: Seq[String]) = data.mkString("").trim.length == 0

}

trait DataGenerator extends DataParser with Metrics {

  val defaultChunkSize: Int = 10000

  def getData(iterator: Iterator[String])(implicit schemeInfo: SchemeInfo, hc: HeaderCarrier, request: Request[_]): ListBuffer[SchemeData] = {
    var rowNum = 0
    implicit var sheetName: String = ""
    var sheetColSize = 0
    val schemeData: ListBuffer[SchemeData] = ListBuffer()
    var validator: DataValidator = ERSValidationConfigs.defValidator

    def incRowNum() = rowNum = rowNum + 1

    val startTime = System.currentTimeMillis()

    def checkForMissingHeaders(rowNum: Int) = {
      if (rowNum > 0 && rowNum < 9) {
        throw ERSFileProcessingException(Messages("ers.exceptions.dataParser.incorrectHeader"), Messages("ers.exceptions.dataParser.incorrectHeader"))
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
      throw ERSFileProcessingException(Messages("ers.exceptions.dataParser.noData"), Messages("ers.exceptions.dataParser.noData"))
    }
    deliverDataIteratorMetrics(startTime)
    Logger.debug("The SchemeData that GetData finally returns: " + schemeData)
    schemeData
  }

  def getCsvData(iterator: Iterator[String])
                (implicit schemeInfo: SchemeInfo, sheetName: String, hc: HeaderCarrier, request: Request[_]): ListBuffer[Seq[String]] = {
    val start = System.currentTimeMillis()
    val chunkSize = current.configuration.getInt("validationChunkSize").getOrElse(defaultChunkSize)
    val cpus = Runtime.getRuntime.availableProcessors()

    Logger.info(s"Validating file ${sheetName} cpus: $cpus chunkSize: $chunkSize")

    val validator = setValidator(sheetName)
    val columnCount = getSheet(sheetName)(schemeInfo, hc, request).headerRow.size

    val rows = getRowsFromFile(iterator)
    val chunks = numberOfChunks(rows.size, chunkSize)
    val submissions = submitChunks(rows, chunks, chunkSize, columnCount, validator)
    val result = getResult(submissions)

    val data = checkResult(result) match {
      case Success(rows) => rows
      case Failure(ex) => throw ex
    }

    val timeTaken = System.currentTimeMillis() - start
    Logger.info(s"Validation of file ${sheetName} completed in $timeTaken ms")

    if (data.isEmpty) {
      throw ERSFileProcessingException(Messages("ers_check_csv_file.noData", sheetName + ".csv"), Messages("ers_check_csv_file.noData"))
    }
    data
  }

  def getRowsFromFile(iterator: Iterator[String]): List[List[String]] = {
    val rows: ListBuffer[List[String]] = new ListBuffer()
    while (iterator.hasNext) {
      val row = iterator.next().split(",").toList
      rows += row
    }
    rows.toList
  }

  def numberOfChunks(rows: Int, chunkSize: Int): Int = {
    val chunks: Int = (rows / chunkSize) + (if (rows % chunkSize == 0) 0 else 1)
    chunks
  }

  def submitChunks(
                    rows: List[List[String]],
                    chunks: Int,
                    chunkSize: Int,
                    columnCount: Int,
                    validator: DataValidator)
                  (implicit schemeInfo: SchemeInfo, sheetName: String, hc: HeaderCarrier, request: Request[_]): Array[Future[List[Seq[String]]]] = {

    val futures = new Array[Future[List[Seq[String]]]](chunks)

    for (chunk <- 1 to chunks) {
      val chunkStart = (chunk - 1) * chunkSize + 1
      val chunkEnd = (chunk * chunkSize).min(rows.size)

      futures(chunk - 1) = Future {
        val chunk = rows.slice(chunkStart - 1, chunkEnd)
        processChunk(chunk, chunkStart, columnCount, validator)
      }
    }
    futures
  }

  def processChunk(
                    chunk: List[List[String]],
                    chunkStart: Int,
                    columnCount: Int,
                    validator: DataValidator)
                  (implicit schemeInfo: SchemeInfo, sheetName: String, hc: HeaderCarrier, request: Request[_]): List[Seq[String]] = {

    val data: ListBuffer[Seq[String]] = new ListBuffer()
    var rowNo = chunkStart
    chunk.foreach(row => {
      val rowData: Seq[String] = constructColumnData(row, columnCount)
      if (!isBlankRow(rowData)) {
        data += generateRowData(rowData, rowNo, validator)
      }
      rowNo += 1
    })
    data.toList
  }

  def getResult(submissions: Array[Future[List[Seq[String]]]]): Future[ListBuffer[Seq[String]]] = {
    val data: ListBuffer[Seq[String]] = new ListBuffer()

    val result = Future.fold(submissions)(data)((a, b) => b match {
      case rows if rows.nonEmpty => a ++= rows
      case _ => a
    })

    result
  }

  def checkResult[T](result: Future[T]): Try[T] = {
    Await.ready(result, Duration.Inf)
    result.value match {
      case Some(Success(t)) => Success(t)
      case Some(Failure(t)) => Failure(t)
      case None => Failure(new RuntimeException("Unable to retrieve value of future CSV file validation."))
    }
  }

  def addSheetData(schemeInfo: SchemeInfo, sheetName: String, rowCount: Int, ersSchemeData: ListBuffer[Seq[String]], schemeData: ListBuffer[SchemeData]) = {
    if (!sheetName.isEmpty && rowCount >= 10) {
      schemeData += SchemeData(schemeInfo, sheetName, None, ersSchemeData.dropRight(0))
      ersSchemeData.clear
    }
    ersSchemeData
  }

  def setValidator(sheetName: String)(implicit schemeInfo: SchemeInfo, hc: HeaderCarrier, request: Request[_]) = {
    try {
      ERSValidationConfigs.getValidator(ersSheets(sheetName).configFileName)
    } catch {
      case e: Exception => {
        auditEvents.fileProcessingErrorAudit(schemeInfo, sheetName, "Could not set the validator")
        Logger.error("setValidator has thrown an exception, SheetName: " + sheetName + " Exception message: " + e.getMessage)
        throw new ERSFileProcessingException(Messages("ers.exceptions.dataParser.configFailure"), "Could not set the validator ")
      }
    }
  }

  def identifyAndDefineSheet(data: String)(implicit schemeInfo: SchemeInfo, hc: HeaderCarrier, request: Request[_]) = {
    Logger.debug("5.1  case 0 identifyAndDefineSheet  ")
    val res = getSheet(data)
    res.schemeType.toLowerCase == schemeInfo.schemeType.toLowerCase match {
      case true => {
        Logger.debug("****5.1.1  data contains data:  *****" + data)
        data
      }
      case _ => {
        auditEvents.fileProcessingErrorAudit(schemeInfo, data, s"${res.schemeType.toLowerCase} is not equal to ${schemeInfo.schemeType.toLowerCase}")
        Logger.warn(Messages("ers.exceptions.dataParser.incorrectSchemeType") + data)
        throw ERSFileProcessingException(Messages("ers.exceptions.dataParser.incorrectSchemeType"), Messages("ers.exceptions.dataParser.incorrectSchemeType") + data)
      }
    }
  }

  def getSheet(sheetName: String)(implicit schemeInfo: SchemeInfo, hc: HeaderCarrier, request: Request[_]) = {
    Logger.info(s"Looking for sheetName: ${sheetName}")
    ersSheets.getOrElse(sheetName, {
      auditEvents.fileProcessingErrorAudit(schemeInfo, sheetName, "Could not set the validator")
      Logger.warn(Messages("ers.exceptions.dataParser.unidentifiableSheetName") + sheetName)
      throw ERSFileProcessingException(Messages("ers.exceptions.dataParser.incorrectSheetName"), Messages("ers.exceptions.dataParser.unidentifiableSheetName") + " " + sheetName)
    })
  }

  def validateHeaderRow(rowData: Seq[String], sheetName: String)(implicit schemeInfo: SchemeInfo, hc: HeaderCarrier, request: Request[_]) = {
    val headerFormat = "[^a-zA-Z0-9]"

    val header = getSheet(sheetName)(schemeInfo, hc, request).headerRow.map(_.replaceAll(headerFormat, ""))
    val data = rowData.take(header.size)
    val dataTrim = data.map(_.replaceAll(headerFormat, ""))

    Logger.debug("5.3  case 9 sheetName =" + sheetName + "data = " + dataTrim + "header == -> " + header)
    dataTrim == header match {
      case true => header.size
      case _ => {
        auditEvents.fileProcessingErrorAudit(schemeInfo, sheetName, "Header row invalid")
        Logger.warn("Error while reading File + Incorrect ERS Template")
        throw ERSFileProcessingException(Messages("ers.exceptions.dataParser.incorrectHeader"), Messages("ers.exceptions.dataParser.headersDontMatch"))
      }
    }
  }

  def generateRowData(rowData: Seq[String], rowCount: Int, validator: DataValidator)(implicit schemeInfo: SchemeInfo, sheetName: String, hc: HeaderCarrier, request: Request[_]) = {
    // ignore case where check if row has data and all the cols are empty
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
        throw ERSFileProcessingException(Messages("ers.exceptions.dataParser.fileInvalid"), Messages("ers.exceptions.dataParser.validationFailure"))
      }
      case _ => {
        auditEvents.fileProcessingErrorAudit(schemeInfo, sheetName, "Failure to validate")
        Logger.warn("Error while Validating File + Formatting errors present ")
        throw ERSFileProcessingException(Messages("ers.exceptions.dataParser.fileInvalid"), Messages("ers.exceptions.dataParser.validationFailure"))
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
