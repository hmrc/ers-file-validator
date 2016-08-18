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

import java.util.concurrent.TimeUnit

import uk.gov.hmrc.services.validation.{ValidationError,DataValidator}
import metrics.Metrics
import models._
import play.api.Logger
import play.api.i18n.Messages
import services.ERSTemplatesInfo._
import services.audit.AuditEvents
import services.validation.ErsValidator
import uk.gov.hmrc.play.http.HeaderCarrier
import play.api.mvc.Request
import scala.collection.mutable.ListBuffer
import scala.util.Try
import scala.xml._
/**
 * Created by raghu on 26/01/16.
 */
trait DataParser {

  val repeatAttr = "table:number-columns-repeated"
  val auditEvents:AuditEvents = AuditEvents


  def parse(row:String) = {
    Logger.debug("DataParser: Parse: About to parse row: " + row)
    val xmlRow = Try(Option(XML.loadString(row))).getOrElse(None)
//    Logger.debug("DataParser: Parse: About to match xmlRow: " + xmlRow)
    xmlRow match {
      case None => Logger.debug("3.1 Parse row left "); Left(row)
      case elem:Option[Elem] => Logger.debug("3.2 Parse row right ")
                      Try( Right(xmlRow.get.child.flatMap( parseColumn(_)))).getOrElse{
                        Logger.warn(Messages("ers.exceptions.dataParser.fileRetrievalFailed"));
                        throw ERSFileProcessingException (Messages("ers.exceptions.dataParser.fileRetrievalFailed"), Messages("ers.exceptions.dataParser.parserFailure"))
                      }
      case _  => {
        Logger.warn(Messages("ers.exceptions.dataParser.fileParsingError"))
        throw ERSFileProcessingException(Messages("ers.exceptions.dataParser.fileParsingError"), Messages("ers.exceptions.dataParser.parsingOfFileData"))
      }
    }
  }

  def parseColumn(col:scala.xml.Node) = {
    val colsRepeated =  col.attributes.asAttrMap.get(repeatAttr)

    if(colsRepeated.nonEmpty && colsRepeated.get.toInt < 50) {
      val cols:scala.collection.mutable.MutableList[String]= scala.collection.mutable.MutableList()
      for( i <- 1 to colsRepeated.get.toInt)  cols += col.text
      cols.toSeq
    }
    else  Seq(col.text)
  }

}

trait DataGenerator extends DataParser with Metrics{

  def getData(iterator:Iterator[String])(implicit schemeInfo: SchemeInfo, hc: HeaderCarrier, request : Request[_]) =

  {
    var rowNum = 0
    implicit var sheetName :String = ""
    var sheetColSize = 0
    val schemeData: ListBuffer[SchemeData] = ListBuffer()
    var validator:DataValidator = ERSValidationConfigs.defValidator
    def incRowNum() = rowNum =  rowNum + 1
    val startTime = System.currentTimeMillis()

    def checkForMissingHeaders(rowNum: Int) = {
      if(rowNum > 0 && rowNum < 9) {
        throw ERSFileProcessingException(Messages("ers.exceptions.dataParser.incorrectHeader"),Messages("ers.exceptions.dataParser.incorrectHeader"))
      }
    }

    while(iterator.hasNext){

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
        case _ => rowNum match {
          case count if count < 9 => {
            Logger.debug("GetData: incRowNum if count < 9: " + count + " RowNum: " + rowNum )
            incRowNum()
          }
          case 9 => {
            Logger.debug("GetData: incRowNum if  9: " + rowNum + "sheetColSize: " + sheetColSize )
            sheetColSize = validateHeaderRow(rowData.right.get, sheetName)
            incRowNum()
          }
          case _ => {
            val foundData = rowData.right.get

            val data = constructColumnData(foundData,sheetColSize)

            if(!isBlankRow(data)){
              schemeData.last.data += generateRowData(data, rowNum,validator)//(schemeInfo,sheetName)
            }
            incRowNum()
          }
        }
      }
    }

    checkForMissingHeaders(rowNum)
    if(schemeData.foldLeft(0)((sum, obj) => sum + obj.data.size) == 0) {
      throw ERSFileProcessingException(Messages("ers.exceptions.dataParser.noData"),Messages("ers.exceptions.dataParser.noData"))
    }
    deliverDataIteratorMetrics(startTime)
    Logger.debug("The SchemeData that GetData finally returns: " + schemeData)
    schemeData
  }

  def getCsvData(iterator:Iterator[String])(implicit schemeInfo: SchemeInfo, sheetName : String, hc: HeaderCarrier,request: Request[_]) = {
    val validator = setValidator(sheetName)
    val columnCount = getSheet(sheetName)(schemeInfo,hc,request).headerRow.size
    val sheetData: ListBuffer[Seq[String]] = ListBuffer()
    var rowCount = 0
    while (iterator.hasNext) {
      rowCount+=1
      val foundData = iterator.next.split(",")
      val rowData: Seq[String] = constructColumnData(foundData,columnCount)
      if(!isBlankRow(rowData)){
        Logger.debug("Row Num :- "+ rowCount +  " -- Data retrieved:-" + rowData.mkString)
        sheetData += generateRowData(rowData,rowCount,validator)
      }
    }
    if(sheetData.isEmpty) {
      throw ERSFileProcessingException(Messages("ers_check_csv_file.noData", sheetName + ".csv"), Messages("ers_check_csv_file.noData"))
    }
    sheetData
  }

  def addSheetData(schemeInfo: SchemeInfo, sheetName: String, rowCount: Int, ersSchemeData: ListBuffer[Seq[String]], schemeData: ListBuffer[SchemeData]) = {
    if(!sheetName.isEmpty && rowCount >= 10) {
      schemeData += SchemeData(schemeInfo,sheetName, None, ersSchemeData.dropRight(0))
      ersSchemeData.clear
    }
    ersSchemeData
  }

  def setValidator(sheetName:String)(implicit schemeInfo: SchemeInfo, hc: HeaderCarrier,request: Request[_]) = {
     try {
       ERSValidationConfigs.getValidator(ersSheets(sheetName).configFileName)
     }catch{
       case e:Exception => {
         auditEvents.fileProcessingErrorAudit(schemeInfo, sheetName, "Could not set the validator")
         Logger.error("setValidator has thrown an exception, SheetName: " + sheetName + " Exception message: " + e.getMessage)
         throw new ERSFileProcessingException(Messages("ers.exceptions.dataParser.configFailure"), "Could not set the validator ")
       }
     }
   }

  def identifyAndDefineSheet(data: String)(implicit schemeInfo: SchemeInfo, hc: HeaderCarrier,request: Request[_]) = {
    Logger.debug("5.1  case 0 identifyAndDefineSheet  ")
    val res = getSheet(data)
    res.schemeType.toLowerCase == schemeInfo.schemeType.toLowerCase   match {
      case true =>  {
        Logger.debug("****5.1.1  data contains data:  *****" + data)
        data }
      case _ => {
        auditEvents.fileProcessingErrorAudit(schemeInfo, data, s"${res.schemeType.toLowerCase} is not equal to ${schemeInfo.schemeType.toLowerCase}")
        Logger.warn(Messages("ers.exceptions.dataParser.incorrectSchemeType") + data)
        throw ERSFileProcessingException(Messages("ers.exceptions.dataParser.incorrectSchemeType"), Messages("ers.exceptions.dataParser.incorrectSchemeType") + data)
      }
    }
  }

  def getSheet(sheetName:String)(implicit schemeInfo: SchemeInfo, hc: HeaderCarrier,request: Request[_]) = {
    Logger.info(s"Looking for sheetName: ${sheetName}")
    ersSheets.getOrElse(sheetName, {
      auditEvents.fileProcessingErrorAudit(schemeInfo, sheetName, "Could not set the validator")
      Logger.warn(Messages("ers.exceptions.dataParser.unidentifiableSheetName") + sheetName)
      throw ERSFileProcessingException(Messages("ers.exceptions.dataParser.incorrectSheetName"), Messages("ers.exceptions.dataParser.unidentifiableSheetName") + " " +sheetName)
    })
  }

  def validateHeaderRow(rowData:Seq[String], sheetName:String)(implicit schemeInfo: SchemeInfo, hc: HeaderCarrier,request: Request[_]) =
  {
    val headerFormat = "[^a-zA-Z0-9]"

    val header = getSheet(sheetName)(schemeInfo,hc,request).headerRow.map(_.replaceAll(headerFormat,""))
    val data = rowData.take(header.size)
    val dataTrim = data.map(_.replaceAll(headerFormat,""))

    Logger.debug("5.3  case 9 sheetName =" + sheetName + "data = " + dataTrim + "header == -> " + header)
    dataTrim == header  match {
      case true=> header.size
      case _ => {
        auditEvents.fileProcessingErrorAudit(schemeInfo, sheetName, "Header row invalid")
        Logger.warn("Error while reading File + Incorrect ERS Template")
        throw ERSFileProcessingException(Messages("ers.exceptions.dataParser.incorrectHeader"),Messages("ers.exceptions.dataParser.headersDontMatch"))
      }
    }
  }

  def isBlankRow(data: Seq[String]) = data.mkString("").trim.length == 0

  def generateRowData(rowData:Seq[String], rowCount:Int, validator:DataValidator)(implicit schemeInfo: SchemeInfo,sheetName:String, hc: HeaderCarrier,request: Request[_]) = {
    // ignore case where check if row has data and all the cols are empty
    Logger.debug("5.4  case _ rowData is "+ rowData)
    ErsValidator.validateRow(rowData,rowCount,validator) match {
      case None => rowData
      case err:Option[List[ValidationError]] => {
        Logger.debug(s"Error while Validating Row num--> ${rowCount} ")
        Logger.debug(s"Rowdata is --> ${rowData.map(res=> res)}")
        Logger.debug(s"Error column data is  ${err.get.map(_.cell.value)}")
        err.map{
          auditEvents.validationErrorAudit(_, schemeInfo,sheetName)
        }
        throw ERSFileProcessingException (Messages("ers.exceptions.dataParser.fileInvalid"), Messages("ers.exceptions.dataParser.validationFailure"))
      }
      case _ => {
        auditEvents.fileProcessingErrorAudit(schemeInfo, sheetName, "Failure to validate")
        Logger.warn("Error while Validating File + Formatting errors present ")
        throw ERSFileProcessingException(Messages("ers.exceptions.dataParser.fileInvalid"), Messages("ers.exceptions.dataParser.validationFailure"))
      }
    }
  }

  def constructColumnData(foundData:Seq[String],sheetColSize:Int):Seq[String] = {
    if(foundData.size < sheetColSize) {
      Logger.warn(s"Difference between amount of columns ${foundData.size} and amount of headers ${sheetColSize}")
      val additionalEmptyCells: Seq[String] = List.fill(sheetColSize - foundData.size)("")
      (foundData ++ additionalEmptyCells).take(sheetColSize)
    }
    else {
      foundData.take(sheetColSize)
    }
  }

  def deliverDataIteratorMetrics(startTime:Long) =
    metrics.dataIteratorTimer(System.currentTimeMillis() - startTime, TimeUnit.MILLISECONDS)

}
