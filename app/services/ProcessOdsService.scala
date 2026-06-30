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
import _root_.utils.{ErrorResponseMessages, SchemeResolver, ValidationUtils}
import config.ApplicationConfig
import connectors.ERSFileValidatorConnector
import metrics.Metrics
import models._
import models.upscan.UpscanCallback
import play.api.Logging
import play.api.mvc.Request
import uk.gov.hmrc.http.{HeaderCarrier, SessionId}
import uk.gov.hmrc.validator._
import uk.gov.hmrc.validator.models._
import uk.gov.hmrc.validator.ods.OdsValidator

import java.io.InputStream
import java.util.UUID
import java.util.concurrent.TimeUnit
import java.util.zip.ZipInputStream
import javax.inject.{Inject, Singleton}
import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

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
  ): Either[ValidatorFailure, ListBuffer[SchemeData]] =
    OdsValidator
      .generateSchemeData(
        schemeVersion,
        readFile(callbackData.downloadUrl),
        schemeInfo.schemeType,
        callbackData.name
      )
      .map(_.map(validDataSheet => SchemeData(schemeInfo, validDataSheet.sheetName, None, validDataSheet.data)))

  def processFile(callbackData: UpscanCallback, empRef: String)(implicit
    hc: HeaderCarrier,
    schemeInfo: SchemeInfo,
    request: Request[_]
  ): Future[Either[ErsException, Int]] = {
    val startTime = System.currentTimeMillis()

    val result: Try[Either[ErsException, ListBuffer[SchemeData]]] = Try {
      for {
        schemeVersion <- SchemeResolver.getSchemeVersion(schemeInfo.taxYear, appConfig)
        schemeData    <-
          generateSchemeData(callbackData, schemeVersion).left
            .map(failure => mapValidatorFailure(failure, startTime))
      } yield schemeData
    }

    result match {
      case Failure(e)     =>
        logger.error(s"Unexpected error processing file: ${e.getMessage}", e)
        Future.successful(Left(ErsFileProcessingException(e.getMessage, "Unexpected error processing file")))
      case Success(value) =>
        value match {
          case Left(error)       => Future.successful(Left(error))
          case Right(schemeData) => processSchemeData(callbackData, schemeData, startTime, empRef)
        }
    }
  }

  private def isSystemFailure(vf: ValidatorFailure): Boolean = vf match {
    case _: ParserFailure => true
    case _                => false
  }

  private def mapValidatorFailure(
    validatorFailure: ValidatorFailure,
    startTime: Long
  )(implicit schemeInfo: SchemeInfo): ErsException = {
    deliverBESMetrics(startTime)

    val logStart = "[ProcessOdsService][processFile]"

    validatorFailure match {
      case vf: IncorrectSchemeFailure                  =>
        logger.warn(s"$logStart Scheme type mismatch: ${vf.message}, schemeRef: ${schemeInfo.schemeRef}")

        SchemeTypeMismatchException(
          message = ErrorResponseMessages.dataParserIncorrectSheetName,
          context = ErrorResponseMessages
            .dataParserIncorrectSchemeType(Some(vf.uploadedFileSchemeType), Some(vf.selectedSchemeType)),
          expectedSchemeType = vf.uploadedFileSchemeType,
          requestSchemeType = vf.selectedSchemeType
        )
      case vf: IncorrectSheetNameFailure               =>
        logger.warn(s"$logStart Unknown sheet name: ${vf.sheetName}, schemeRef: ${schemeInfo.schemeRef}")

        UnknownSheetException(
          ErrorResponseMessages.dataParserIncorrectSheetName,
          s"Couldn't find config for given SheetName: ${vf.sheetName}"
        )
      case vf: IncorrectHeaderFailure                  =>
        logger.warn(s"$logStart Incorrect header: ${vf.message}, schemeRef: ${schemeInfo.schemeRef}")
        HeaderValidationException(ErrorResponseMessages.dataParserIncorrectHeader, vf.message)
      case _: NoDataFailure                            =>
        logger.warn(s"$logStart No data in file, schemeRef: ${schemeInfo.schemeRef}")
        FileValidatorNoDataException(ErrorResponseMessages.dataParserNoData, ErrorResponseMessages.dataParserNoData)
      case vf: ValidatorFailure if isSystemFailure(vf) =>
        logger.error(s"$logStart System error during validation: ${vf.message}, schemeRef: ${schemeInfo.schemeRef}")
        ErsFileProcessingException(
          vf.message,
          s"System error during ODS processing, schemeRef: ${schemeInfo.schemeRef}"
        )
      case vf: ValidatorFailure                        =>
        logger.warn(s"$logStart File validation error: ${vf.message}, schemeRef: ${schemeInfo.schemeRef}")
        FileValidationException(vf.message, vf.message)
    }
  }

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
          throw ErsFileProcessingException(
            s"${ErrorResponseMessages.fileProcessingServiceFailedStream}",
            s"${ErrorResponseMessages.fileProcessingServiceBulkEntity}"
          )
      }

    try
      findFileInZip(zipInputStream)
    catch {
      case e: Throwable =>
        Try(zipInputStream.close())
        Try(stream.close())
        throw e
    }
  }

  private def sendSchemeData(ersSchemeData: SchemeData, empRef: String)(implicit
    hc: HeaderCarrier
  ): Future[Either[ErsException, Unit]] = {
    logger.debug("Sheetdata sending to ers-submission " + ersSchemeData.sheetName)
    ersConnector.sendToSubmissions(ersSchemeData, empRef).map {
      case Right(_) =>
        auditEvents.fileValidatorAudit(ersSchemeData.schemeInfo, ersSchemeData.sheetName)
        Right(())
      case Left(ex) =>
        auditEvents.auditRunTimeError(ex, ex.getMessage, ersSchemeData.schemeInfo, ersSchemeData.sheetName)
        logger.error(s"[ProcessOdsService][sendSchemeData] An exception occurred: ${ex.getMessage}", ex)
        Left(ErsFileProcessingException(ex.toString, ex.getMessage))
    }
  }

  def sendScheme(schemeData: SchemeData, empRef: String)(implicit
    hc: HeaderCarrier
  ): Future[Either[ErsException, Int]] =

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
    } else { // data small enough to send in single slice
      sendSchemeData(schemeData, empRef).map((sendResult: Either[ErsException, Unit]) => sendResult.map(_ => 1))
    }

  private def processSchemeData(
    callbackData: UpscanCallback,
    result: ListBuffer[SchemeData],
    startTime: Long,
    empRef: String
  )(implicit hc: HeaderCarrier, schemeInfo: SchemeInfo, request: Request[_]): Future[Either[ErsException, Int]] = {
    logger.debug("2.1 result contains: " + result)
    deliverBESMetrics(startTime)

    val filesWithData = result.filter(_.data.nonEmpty)

    val totalRows = filesWithData.map(_.data.size).sum

    Future
      .sequence(
        filesWithData.map(schemeData => sendScheme(schemeData, empRef))
      )
      .flatMap { results: ListBuffer[Either[ErsException, Int]] =>
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
                    ErsFileProcessingException(
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
