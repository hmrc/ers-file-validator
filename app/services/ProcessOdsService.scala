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

import _root_.services.audit.AuditEvents
import config.ApplicationConfig
import connectors.ERSFileValidatorConnector
import metrics.Metrics
import models._
import models.upscan.UpscanCallback
import play.api.Logging
import play.api.mvc.Request
import uk.gov.hmrc.http.HeaderCarrier
import utils.{ErrorResponseMessages, ValidationUtils}

import java.io.InputStream
import java.util.concurrent.TimeUnit
import java.util.zip.ZipInputStream
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class ProcessOdsService @Inject()(dataGenerator: DataGenerator,
                                  auditEvents: AuditEvents,
                                  ersConnector: ERSFileValidatorConnector,
                                  sessionService: SessionService,
                                  appConfig: ApplicationConfig,
                                  implicit val ec: ExecutionContext) extends Metrics with Logging {

  val splitSchemes: Boolean = appConfig.splitLargeSchemes
  val maxNumberOfRows: Int = appConfig.maxNumberOfRowsPerSubmission

  @throws(classOf[ERSFileProcessingException])
  def processFile(callbackData: UpscanCallback, empRef: String)(implicit hc: HeaderCarrier, schemeInfo: SchemeInfo, request : Request[_]): Int = {
    val startTime = System.currentTimeMillis()
    logger.info("2.0 start: ")
    val result = dataGenerator.getErrors(readFile(callbackData.downloadUrl))
    logger.debug("2.1 result contains: " + result)
    deliverBESMetrics(startTime)
    logger.debug("No if SchemeData Objects " + result.size)
    val filesWithData = result.filter(_.data.nonEmpty)
    var totalRows = 0
    val res1 = filesWithData.foldLeft(0) {
      (res, el) => {
        totalRows += el.data.size
        res + sendScheme(el, empRef)
      }
    }
    sessionService.storeCallbackData(callbackData, totalRows).map {
      case callback: Option[UpscanCallback] if callback.isDefined => res1
      case _ => logger.error(s"storeCallbackData failed with Exception , timestamp: ${System.currentTimeMillis()}.")
        throw ERSFileProcessingException(("callback data storage in sessioncache failed "), "Exception storing callback data")
    }.recover {
      case e: Throwable => logger.error(s"storeCallbackData failed with Exception ${e.getMessage}, timestamp: ${System.currentTimeMillis()}.")
        throw e
    }

    logger.warn(s"Total rows for schemeRef ${schemeInfo.schemeRef}: $totalRows")
    auditEvents.totalRows(totalRows, schemeInfo)
    res1
  }

  private[services] def readFile(downloadUrl: String): Iterator[String] = {
    val stream = ersConnector.upscanFileStream(downloadUrl)
    val targetFileName = "content.xml"
    val zipInputStream = new ZipInputStream(stream)
    @scala.annotation.tailrec
    def findFileInZip(stream: ZipInputStream): InputStream = {
      Option(stream.getNextEntry) match {
        case Some(entry) if entry.getName == targetFileName =>
          stream
        case Some(_) =>
          findFileInZip(stream)
        case None =>
          throw ERSFileProcessingException(
            s"${ErrorResponseMessages.fileProcessingServiceFailedStream}",
            s"${ErrorResponseMessages.fileProcessingServiceBulkEntity}"
          )
      }
    }
    val contentInputStream = findFileInZip(zipInputStream)
    new StaxProcessor(contentInputStream)
  }

  def sendSchemeData(ersSchemeData: SchemeData, empRef: String)(implicit hc: HeaderCarrier, request: Request[_]): Unit = {
    logger.debug("Sheetdata sending to ers-submission " + ersSchemeData.sheetName)
    ersConnector.sendToSubmissions(ersSchemeData, empRef).map {
      case Right(_) =>
        auditEvents.fileValidatorAudit(ersSchemeData.schemeInfo, ersSchemeData.sheetName)
      case Left(ex) =>
        auditEvents.auditRunTimeError(ex, ex.getMessage, ersSchemeData.schemeInfo, ersSchemeData.sheetName)
        logger.error(ex.getMessage)
        throw ERSFileProcessingException(ex.toString, ex.getStackTrace.toString)
    }
  }

  def sendScheme(schemeData: SchemeData, empRef: String)(implicit hc: HeaderCarrier, request: Request[_]): Int = {
    if(splitSchemes && (schemeData.data.size > maxNumberOfRows)) {

      val slices: Int = ValidationUtils.numberOfSlices(schemeData.data.size, maxNumberOfRows)
      for(i <- 0 until slices * maxNumberOfRows by maxNumberOfRows) {
        val scheme = new SchemeData(schemeData.schemeInfo, schemeData.sheetName, Option(slices), schemeData.data.slice(i, (i + maxNumberOfRows)))
        logger.debug("The size of the scheme data is " + scheme.data.size + " and i is " + i)
        sendSchemeData(scheme, empRef)
      }
      slices
    }
    else {
      sendSchemeData(schemeData, empRef)
      1
    }
  }

  def deliverBESMetrics(startTime:Long): Unit =
    metrics.besTimer(System.currentTimeMillis() - startTime, TimeUnit.MILLISECONDS)
}
