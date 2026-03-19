/*
 * Copyright 2026 HM Revenue & Customs
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
import uk.gov.hmrc.validator.models.ods.ValidDataRow
import uk.gov.hmrc.validator.ods.OdsValidator
import uk.gov.hmrc.validator._
import _root_.utils.{ErrorResponseMessages, SchemeResolver, ValidationUtils}

import java.io.InputStream
import java.util.UUID
import java.util.concurrent.TimeUnit
import java.util.zip.ZipInputStream
import javax.inject.{Inject, Singleton}
import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

@Singleton
class ProcessOdsService @Inject() (
  auditEvents: AuditEvents,
  ersConnector: ERSFileValidatorConnector,
  sessionService: SessionCacheService,
  appConfig: ApplicationConfig,
  implicit val ec: ExecutionContext
) extends Metrics with Logging {

  val splitSchemes: Boolean = appConfig.splitLargeSchemes
  val maxNumberOfRows: Int  = appConfig.maxNumberOfRowsPerSubmission

  def generateSchemeData(callbackData: UpscanCallback, schemeVersion: SchemeVersion)(implicit
    schemeInfo: SchemeInfo
  ): ListBuffer[SchemeData] =
    OdsValidator
      .generateSchemeData(
        schemeVersion,
        readFile(callbackData.downloadUrl),
        schemeInfo.schemeType,
        callbackData.name
      )
      .map((validDataRow: ValidDataRow) => SchemeData(schemeInfo, validDataRow.sheetName, None, validDataRow.data))

  def processFile(callbackData: UpscanCallback, empRef: String)(implicit
    hc: HeaderCarrier,
    schemeInfo: SchemeInfo,
    request: Request[_]
  ): Future[Either[ErsError, Int]] = {
    val startTime = System.currentTimeMillis()

    val result = for {
      schemeVersion <- SchemeResolver.getSchemeVersion(schemeInfo.taxYear, appConfig)
      schemeData    <-
        Try(generateSchemeData(callbackData, schemeVersion)).toEither.left
          .map(error => mapValidatorException(error, startTime))
    } yield schemeData

    result match {
      case Left(error)       => Future.successful(Left(error))
      case Right(schemeData) => processSchemeData(callbackData, schemeData, startTime, empRef)
    }
  }

  private def isSystemError(e: ValidatorException): Boolean = e match {
    case _: SystemErrorDuringValidationException | _: ParserFailureException => true
    case _                                                                   => false
  }

  private def mapValidatorException(
    e: Throwable,
    startTime: Long
  )(implicit schemeInfo: SchemeInfo): ErsError = {
    deliverBESMetrics(startTime)

    val logStart = "[ProcessOdsService][processFile]"

    e match {
      case e: IncorrectSchemeException               =>
        logger.warn(s"$logStart Scheme type mismatch: ${e.message}, schemeRef: ${schemeInfo.schemeRef}")

        SchemeTypeMismatchError(
          message = ErrorResponseMessages.dataParserIncorrectSheetName,
          context = ErrorResponseMessages
            .dataParserIncorrectSchemeType(Some(e.uploadedFileSchemeType), Some(e.selectedSchemeType)),
          expectedSchemeType = e.uploadedFileSchemeType,
          requestSchemeType = e.selectedSchemeType
        )
      case e: IncorrectSheetNameException            =>
        logger.warn(s"$logStart Unknown sheet name: ${e.sheetName}, schemeRef: ${schemeInfo.schemeRef}")

        UnknownSheetError(
          ErrorResponseMessages.dataParserIncorrectSheetName,
          s"Couldn't find config for given SheetName: ${e.sheetName}"
        )
      case e: IncorrectHeaderException               =>
        logger.warn(s"$logStart Incorrect header: ${e.message}, schemeRef: ${schemeInfo.schemeRef}")
        HeaderValidationError(ErrorResponseMessages.dataParserIncorrectHeader, e.message)
      case _: NoDataException                        =>
        logger.warn(s"$logStart No data in file, schemeRef: ${schemeInfo.schemeRef}")
        NoDataError(ErrorResponseMessages.dataParserNoData, ErrorResponseMessages.dataParserNoData)
      case e: ValidatorException if isSystemError(e) =>
        logger.error(s"$logStart System error during validation: ${e.message}, schemeRef: ${schemeInfo.schemeRef}")
        ERSFileProcessingException(e.message, s"System error during ODS processing, schemeRef: ${schemeInfo.schemeRef}")
      case e: ValidatorException                     =>
        logger.warn(s"$logStart File validation error: ${e.message}, schemeRef: ${schemeInfo.schemeRef}")
        FileValidationError(e.message, e.message)
      case e: Throwable                              =>
        logger.error(s"$logStart Unexpected error reading file: ${e.getMessage}", e)
        FileValidationError(s"$logStart Error reading ODS file -> ${e.getMessage}", e.getMessage)
    }
  }

  // $COVERAGE-OFF$
  private[services] def readFile(downloadUrl: String): InputStream = {
    val stream         = ersConnector.upscanFileStream(downloadUrl)
    val targetFileName = "content.xml"
    val zipInputStream = new ZipInputStream(stream)

    @scala.annotation.tailrec
    def findFileInZip(stream: ZipInputStream): InputStream =
      Option(stream.getNextEntry) match {
        case Some(entry) if entry.getName == targetFileName => stream
        case Some(_)                                        => findFileInZip(stream)
        case None                                           =>
          throw ERSFileProcessingException(
            s"${ErrorResponseMessages.fileProcessingServiceFailedStream}",
            s"${ErrorResponseMessages.fileProcessingServiceBulkEntity}"
          )
      }

    findFileInZip(zipInputStream)
  }
  // $COVERAGE-ON$

  def sendSchemeData(ersSchemeData: SchemeData, empRef: String)(implicit
    hc: HeaderCarrier
  ): Future[Either[ErsError, Unit]] = {
    logger.debug("Sheetdata sending to ers-submission " + ersSchemeData.sheetName)
    ersConnector.sendToSubmissions(ersSchemeData, empRef).map {
      case Right(_) => // todo, shouldn't we do something with the http response here?
        auditEvents.fileValidatorAudit(ersSchemeData.schemeInfo, ersSchemeData.sheetName)
        Right(())
      case Left(ex) =>
        auditEvents.auditRunTimeError(ex, ex.getMessage, ersSchemeData.schemeInfo, ersSchemeData.sheetName)
        logger.error(s"[ProcessOdsService][sendSchemeData] An exception occurred: ${ex.getMessage}", ex)
        Left(ERSFileProcessingException(ex.toString, ex.getMessage))
    }
  }

  def sendScheme(schemeData: SchemeData, empRef: String)(implicit
    hc: HeaderCarrier
  ): Future[Either[ErsError, Int]] =

    if (splitSchemes && schemeData.data.size > maxNumberOfRows) {

      val slices = ValidationUtils.numberOfSlices(schemeData.data.size, maxNumberOfRows)

      val sendSchemeResults = for (i <- 0 until slices * maxNumberOfRows by maxNumberOfRows) yield {
        val scheme = new SchemeData(
          schemeData.schemeInfo,
          schemeData.sheetName,
          Option(slices),
          schemeData.data.slice(i, i + maxNumberOfRows)
        )

        logger.debug("The size of the scheme data is " + scheme.data.size + " and i is " + i)

        sendSchemeData(scheme, empRef)
      }

      Future.sequence(sendSchemeResults).map { results =>
        results.find(_.isLeft) match {
          case Some(Left(err)) => Left(err)
          case _               => Right(slices)
        }
      }
    } else { // data enough to send in single slice
      sendSchemeData(schemeData, empRef).map((sendResult: Either[ErsError, Unit]) =>
        sendResult.map(_ => 1)
      )
    }

  private def processSchemeData(
    callbackData: UpscanCallback,
    result: ListBuffer[SchemeData],
    startTime: Long,
    empRef: String
  )(implicit hc: HeaderCarrier, schemeInfo: SchemeInfo, request: Request[_]): Future[Either[ErsError, Int]] = {
    logger.debug("2.1 result contains: " + result)
    deliverBESMetrics(startTime)

    val filesWithData = result.filter(_.data.nonEmpty)

    val totalRows = filesWithData.map(_.data.size).sum

    Future
      .sequence(
        filesWithData.map(schemeData => sendScheme(schemeData, empRef))
      )
      .flatMap { results: ListBuffer[Either[ErsError, Int]] =>
        results.find(_.isLeft) match {
          case Some(Left(err)) => Future.successful(Left(err))
          case _               =>
            val totalSlices = results.collect { case Right(n) => n }.sum
            val sessionId   = hc.sessionId.getOrElse(SessionId(UUID.randomUUID().toString)).value
            sessionService
              .storeCallbackData(callbackData, totalRows)(RequestWithUpdatedSession(request, sessionId))
              .map {
                case Some(_) =>
                  logger.info(
                    s"[ProcessOdsService][processFile]: Total number of rows for ods file, schemeRef ${schemeInfo.schemeRef} (scheme type: ${schemeInfo.schemeType}): $totalRows"
                  )
                  auditEvents.totalRows(totalRows, schemeInfo)
                  Right(totalSlices)
                case None    =>
                  logger.error(s"storeCallbackData failed, timestamp: ${System.currentTimeMillis()}.")
                  Left(
                    ERSFileProcessingException(
                      "callback data storage in sessioncache failed ",
                      "Exception storing callback data"
                    )
                  )
              }
        }
      }
  }

  def deliverBESMetrics(startTime: Long): Unit =
    metrics.besTimer(System.currentTimeMillis() - startTime, TimeUnit.MILLISECONDS)

}
