/*
 * Copyright 2019 HM Revenue & Customs
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

import java.io.{BufferedReader, InputStreamReader}
import java.util.concurrent.TimeUnit

import config.ApplicationConfig._
import connectors.ERSFileValidatorConnector
import metrics.Metrics
import models._
import play.api.{Configuration, Logger, Play}
import _root_.services.audit.AuditEvents
import play.api.Mode.Mode
import uk.gov.hmrc.play.config.ServicesConfig
import play.api.Play.current
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.mvc.Request
import uk.gov.hmrc.http.HeaderCarrier

import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

trait FileProcessingService extends ServicesConfig with DataGenerator with Metrics {

  val splitSchemes = splitLargeSchemes
  val maxNumberOfRows = maxNumberOfRowsPerSubmission
  val sessionService: SessionService = SessionService
  val ersConnector: ERSFileValidatorConnector = ERSFileValidatorConnector
  override val auditEvents:AuditEvents = AuditEvents

@throws(classOf[ERSFileProcessingException])
  def processFile(callbackData: CallbackData, empRef: String)(implicit hc: HeaderCarrier, schemeInfo: SchemeInfo, request : Request[_]) = {
    val startTime = System.currentTimeMillis()
    val fileData = readFile(callbackData.collection,callbackData.id)
    Logger.info(" 2. Invoke Data generator ")
    deliverBESMetrics(startTime)
    val result: ListBuffer[SchemeData] = getData(fileData)

    Logger.info("2.1 result contains: " + result)
    Logger.debug("No if SchemeData Objects " + result.size)
    val filesWithData = result.filter(_.data.nonEmpty)
    var totalRows = 0
    val res1 = filesWithData.foldLeft(0) {
      (res, el) => {
        totalRows+= el.data.size
        res + sendScheme(el,callbackData, empRef)}
    }
    sessionService.storeCallbackData(callbackData,totalRows).map {
      case callback: Option[CallbackData] if callback.isDefined => res1
      case _ => Logger.error(s"storeCallbackData failed with Exception , timestamp: ${System.currentTimeMillis()}.")
        throw new ERSFileProcessingException(("callback data storage in sessioncache failed "), "Exception storing callback data")
    } .recover {
      case e:Throwable => Logger.error(s"storeCallbackData failed with Exception ${e.getMessage}, timestamp: ${System.currentTimeMillis()}.")
        throw e
    }
    Logger.warn(s"Total rows for schemeRef ${schemeInfo.schemeRef}: $totalRows")
    auditEvents.totalRows(totalRows, schemeInfo)
    res1
  }

  def processCsvFile(callbackData: CallbackData, empRef: String)(implicit hc: HeaderCarrier, schemeInfo: SchemeInfo,request: Request[_])={
    val startTime = System.currentTimeMillis()
    readCSVFile(callbackData).map { fileData =>
      Logger.info(" 2. Invoke Data generator ")
      deliverBESMetrics(startTime)

      val sheetName = callbackData.name.getOrElse("").replace(".csv","")
      val result: ListBuffer[Seq[String]] = getCsvData(fileData)(schemeInfo, sheetName,hc,request)
      val schemeData: SchemeData = SchemeData(schemeInfo, sheetName, None, result)

      Logger.info("2.1 result contains: " + result)
      Logger.debug("No if SchemeData Objects " + result.size)
      (sendScheme(schemeData, callbackData, empRef), schemeData.data.size)
    }
  }

  def readFile(collection: String, id: String) : Iterator[String] = {
    Logger.debug("1. Read File ")
    try {
      val stream = ersConnector.readAttachmentUri(collection, id)
      new StaxProcessor(stream)
    }
    catch {
      case _: Throwable => throw new ERSFileProcessingException(Messages("ers.exceptions.fileProcessingService.failedStream"), Messages("ers.exceptions.fileProcessingService.stax"))
    }
  }

  val converter : (String) => Seq[String] = _.split(",")

def readCSVFile(callbackData: CallbackData) : Future[Iterator[String]] = {
  try {
    val reader = new BufferedReader(new InputStreamReader(ersConnector.readAttachmentUri(callbackData.collection, callbackData.id),"UTF-8"))
    Future(reader.lines().iterator().asScala)
  }catch {
    case e : Throwable => throw new ERSFileProcessingException(Messages("ers.exceptions.fileProcessingService.failedStream"), Messages("ers.exceptions.fileProcessingService.bulkEntity"))
  }
}


  def sendSchemeData(ersSchemeData: SchemeData, empRef: String)(implicit hc: HeaderCarrier, request: Request[_]) = {
    Logger.debug("Sheedata sending to ers-submission " + ersSchemeData.sheetName)
    val result = ersConnector.sendToSubmissions(ersSchemeData, empRef).onComplete {
      case Success(suc) => {
        auditEvents.fileValidatorAudit(ersSchemeData.schemeInfo, ersSchemeData.sheetName)
      }
      case Failure(ex) => {
        auditEvents.auditRunTimeError(ex, ex.getMessage, ersSchemeData.schemeInfo, ersSchemeData.sheetName)
        Logger.error(ex.getMessage)
        throw new ERSFileProcessingException(ex.toString, ex.getStackTrace.toString)
      }
    }
  }

  def sendScheme(schemeData: SchemeData, callbackData: CallbackData, empRef: String)(implicit hc: HeaderCarrier,request: Request[_]): Int = {
    // auditEvents.rowsPerSheet(callbackData, schemeData.data.size)

    if(splitSchemes && (schemeData.data.size > maxNumberOfRows)) {

      def numberOfSlices(sizeOfBuffer: Int): Int = {
        if(sizeOfBuffer%maxNumberOfRows > 0)
          sizeOfBuffer/maxNumberOfRows + 1
        else
          sizeOfBuffer/maxNumberOfRows
      }

      val slices: Int = numberOfSlices(schemeData.data.size)
      for(i <- 0 until slices * maxNumberOfRows by maxNumberOfRows) {
        val scheme = new SchemeData(schemeData.schemeInfo, schemeData.sheetName, Option(slices), schemeData.data.slice(i, (i + maxNumberOfRows)))
        Logger.debug("The size of the scheme data is " + scheme.data.size + " and i is " + i)
        sendSchemeData(scheme, empRef)
      }
      slices
    }
    else {
      sendSchemeData(schemeData, empRef)
      1
    }
  }

  def deliverBESMetrics(startTime:Long) =
    metrics.besTimer(System.currentTimeMillis() - startTime, TimeUnit.MILLISECONDS)
}


object FileProcessingService extends FileProcessingService{
  override protected def mode: Mode = Play.current.mode
  override protected def runModeConfiguration: Configuration = Play.current.configuration

}
