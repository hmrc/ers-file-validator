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

import _root_.services.audit.AuditEvents
import config.ApplicationConfig
import connectors.ERSFileValidatorConnector
import metrics.Metrics
import models._
import models.upscan.UpscanCallback
import play.api.Logging
import play.api.mvc.Request
import uk.gov.hmrc.http.{HeaderCarrier, SessionId}
import utils.{ErrorResponseMessages, ValidationUtils}

import java.io.InputStream
import java.util.UUID
import java.util.concurrent.TimeUnit
import java.util.zip.ZipInputStream
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

@Singleton
class ProcessOdsService @Inject()(dataGenerator: DataGenerator,
                                  auditEvents: AuditEvents,
                                  ersConnector: ERSFileValidatorConnector,
                                  sessionService: SessionCacheService,
                                  appConfig: ApplicationConfig,
                                  implicit val ec: ExecutionContext) extends Metrics with Logging {

  val splitSchemes: Boolean = appConfig.splitLargeSchemes
  val maxNumberOfRows: Int = appConfig.maxNumberOfRowsPerSubmission

  def processFile(callbackData: UpscanCallback, empRef: String)(implicit hc: HeaderCarrier, schemeInfo: SchemeInfo, request: Request[_]): Future[Either[ErsError, Int]] = {
    val startTime = System.currentTimeMillis()

    Try(readFile(callbackData.downloadUrl)) match {
      case Success(iterator) =>
        dataGenerator.getErrors(iterator) match {
          case Left(ersError) =>
            ersError match {
              case userError: UserValidationError =>
                logger.warn(s"[ProcessOdsService][processFile] User validation error: ${userError.message}, context: ${userError.context}, schemeRef: ${schemeInfo.schemeRef}")
                deliverBESMetrics(startTime)
                Future.successful(Left(userError))
              case systemError: SystemError =>
                logger.error(s"[ProcessOdsService][processFile] System error: ${systemError.message}, context: ${systemError.context}, schemeRef: ${schemeInfo.schemeRef}")
                deliverBESMetrics(startTime)
                Future.successful(Left(systemError))
            }

          case Right(result) =>
            logger.debug("2.1 result contains: " + result)
            deliverBESMetrics(startTime)
            val filesWithData = result.filter(_.data.nonEmpty)
            var totalRows = 0
            val res1 = filesWithData.foldLeft(0) {
              (res, el) => {
                totalRows += el.data.size
                res + sendScheme(el, empRef)
              }
            }
            val sessionId = hc.sessionId.getOrElse(SessionId(UUID.randomUUID().toString)).value
            sessionService.storeCallbackData(callbackData, totalRows)(RequestWithUpdatedSession(request, sessionId)).map {
              case Some(_) => {
                logger.info(s"[ProcessOdsService][processFile]: Total number of rows for ods file, schemeRef ${schemeInfo.schemeRef} (scheme type: ${schemeInfo.schemeType}): $totalRows")
                auditEvents.totalRows(totalRows, schemeInfo)
                Right(res1)
              }
              case None =>
                logger.error(s"storeCallbackData failed with Exception , timestamp: ${System.currentTimeMillis()}.")
                Left(ERSFileProcessingException("callback data storage in sessioncache failed ", "Exception storing callback data"))
            }
        }

      case Failure(exception) =>
        logger.error(s"[ProcessOdsService][processFile] Unexpected error reading file: ${exception.getMessage}", exception)
        deliverBESMetrics(startTime)
        Future.successful(Left(ERSFileProcessingException("Error reading ODS file", exception.getMessage)))
    }
  }

  // $COVERAGE-OFF$
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
  // $COVERAGE-ON$

  def sendSchemeData(ersSchemeData: SchemeData, empRef: String)(implicit hc: HeaderCarrier, request: Request[_]): Future[Boolean] = {
    logger.debug("Sheetdata sending to ers-submission " + ersSchemeData.sheetName)
    for (
      result <- ersConnector.sendToSubmissions(ersSchemeData, empRef)
    ) yield {
      result match {
        case Right(_) =>
          auditEvents.fileValidatorAudit(ersSchemeData.schemeInfo, ersSchemeData.sheetName)
        case Left(ex) =>
          auditEvents.auditRunTimeError(ex, ex.getMessage, ersSchemeData.schemeInfo, ersSchemeData.sheetName)
          logger.error(s"[ProcessOdsService][sendSchemeData] An exception occurred: ${ex.getMessage}", ex)
          throw ERSFileProcessingException(ex.toString, ex.getStackTrace.toString)
      }
    }
  }

  def sendScheme(schemeData: SchemeData, empRef: String)(implicit hc: HeaderCarrier, request: Request[_]): Int = {
    if (splitSchemes && (schemeData.data.size > maxNumberOfRows)) {

      val slices: Int = ValidationUtils.numberOfSlices(schemeData.data.size, maxNumberOfRows)
      for (i <- 0 until slices * maxNumberOfRows by maxNumberOfRows) {
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

  def deliverBESMetrics(startTime: Long): Unit =
    metrics.besTimer(System.currentTimeMillis() - startTime, TimeUnit.MILLISECONDS)
}