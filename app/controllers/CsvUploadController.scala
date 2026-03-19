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

package controllers

import controllers.auth.Authorisation
import metrics.Metrics
import models._
import models.upscan.UpscanCsvFileData
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.http.scaladsl.Http
import org.apache.pekko.http.scaladsl.model.{HttpRequest, HttpResponse}
import org.apache.pekko.stream.scaladsl.Source
import play.api.Logging
import play.api.libs.json.JsValue
import play.api.mvc._
import services.audit.AuditEvents
import services.{ProcessCsvService, SessionCacheService}
import uk.gov.hmrc.http.SessionId
import uk.gov.hmrc.play.bootstrap.auth.DefaultAuthConnector
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import utils.LogUtils

import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CsvUploadController @Inject() (
  auditEvents: AuditEvents,
  sessionService: SessionCacheService,
  processCsvService: ProcessCsvService,
  val authConnector: DefaultAuthConnector,
  val cc: ControllerComponents,
  val defaultActionBuilder: DefaultActionBuilder
)(implicit val ec: ExecutionContext, actorSystem: ActorSystem)
    extends BackendController(cc) with Metrics with Authorisation with Logging {

  def processCsvFile(empRef: String): Action[JsValue] = authorisedActionWithBody(empRef) {
    implicit request: Request[JsValue] =>
      val startTime = System.currentTimeMillis()

      request.body
        .validate[UpscanCsvFileData]
        .fold(
          valid = upscanCsvFileData => processValidCsvRequest(empRef, upscanCsvFileData, startTime),
          invalid = jsonValidationErrors => {
            val parseErrors = LogUtils.formatErrorMessageFromJsonParseFailure(jsonValidationErrors)

            logger.warn(s"[CsvUploadController][processCsvFile] Invalid request body: $parseErrors")
            deliverFileProcessingMetrics(startTime)

            Future.successful(
              BadRequest(s"Invalid request body, parse errors: $parseErrors")
            )
          }
        )
  }

  private def processValidCsvRequest(
    empRef: String,
    upscanCsvFileData: UpscanCsvFileData,
    startTime: Long
  )(implicit request: Request[JsValue]): Future[Result] = {

    implicit val schemeInfo: SchemeInfo = upscanCsvFileData.schemeInfo

    logger.debug("SCHEME TYPE: " + schemeInfo.schemeType)
    deliverFileProcessingMetrics(startTime)

    val processedFiles: Seq[Future[Either[ErsError, CsvFileSubmissions]]] =
      processCsvService.processFiles(upscanCsvFileData, schemeInfo, streamFile)

    val extractedSchemeData: Seq[Future[Either[ErsError, CsvFileLengthInfo]]] =
      processedFiles.map(submissions =>
        submissions.flatMap(submissions => processCsvService.extractSchemeData(schemeInfo, empRef, submissions))
      )

    Future
      .sequence(extractedSchemeData)
      .flatMap(csvFileLengthInfo => handleCsvResults(upscanCsvFileData, startTime, csvFileLengthInfo))
  }

  private def handleCsvResults(
    res: UpscanCsvFileData,
    startTime: Long,
    allFilesResults: Seq[Either[ErsError, CsvFileLengthInfo]]
  )(implicit request: Request[JsValue]): Future[Result] = {
    implicit val schemeInfo: SchemeInfo = res.schemeInfo

    val maybeErrors: Option[ErsError] = allFilesResults.collectFirst { case Left(error) => error }

    maybeErrors match {
      case None                                 =>
        val successResults: Seq[CsvFileLengthInfo] = allFilesResults.collect { case Right(info) => info }
        storeCsvCallbackDataAndRespond(res, successResults, startTime)
      case Some(userError: UserValidationError) =>
        logger.warn(
          s"[CsvUploadController][handleCsvResults] User validation error: ${userError.message}, schemeRef: ${schemeInfo.schemeRef}"
        )

        deliverFileProcessingMetrics(startTime)
        Future.successful(BadRequest(userError.message))
      case Some(systemError: SystemError)       =>
        logger.error(
          s"[CsvUploadController][handleCsvResults] System error: ${systemError.message}, schemeRef: ${schemeInfo.schemeRef}"
        )

        deliverFileProcessingMetrics(startTime)
        Future.successful(InternalServerError)
    }
  }

  private def storeCsvCallbackDataAndRespond(
    res: UpscanCsvFileData,
    results: Seq[CsvFileLengthInfo],
    startTime: Long
  )(implicit request: Request[JsValue]): Future[Result] = {
    implicit val schemeInfo: SchemeInfo = res.schemeInfo

    results.foreach(info =>
      logger.info(
        s"[CsvUploadController][storeCsvCallbackDataAndRespond]: Total rows for ${schemeInfo.schemeRef} (${schemeInfo.schemeType}): ${info.fileLength}"
      )
    )

    val totalRowCount: Int = results.map(_.fileLength).sum
    auditEvents.totalRows(totalRowCount, schemeInfo)

    val sessionId = hc.sessionId.getOrElse(SessionId(UUID.randomUUID().toString)).value

    sessionService
      .storeCallbackData(res.callbackData.head, totalRowCount)(RequestWithUpdatedSession(request, sessionId))
      .map {
        case callback if callback.isDefined =>
          val numberOfSlices = results.map(_.noOfSlices).sum

          logger.info(
            s"[CsvUploadController][storeCsvCallbackDataAndRespond] File validated successfully, schemeRef: ${schemeInfo.schemeRef}"
          )

          Ok(numberOfSlices.toString)

        case _ =>
          logger.error(
            s"[CsvUploadController][storeCsvCallbackDataAndRespond] CSV storeCallbackData failed, schemeRef: ${schemeInfo.schemeRef}, timestamp: ${java.time.LocalTime.now()}."
          )
          deliverFileProcessingMetrics(startTime)
          Accepted("csv callback data storage in sessioncache failed")
      }
  }

  private[controllers] def streamFile(downloadUrl: String): Source[HttpResponse, _] =
    Source
      .single(HttpRequest(uri = downloadUrl))
      .mapAsync(parallelism = 1)(makeRequest)

  // $COVERAGE-OFF$
  private[controllers] def makeRequest(request: HttpRequest): Future[HttpResponse] =
    Http()(actorSystem).singleRequest(request)
  // $COVERAGE-ON$

  private def deliverFileProcessingMetrics(startTime: Long): Unit =
    metrics.fileProcessingTimer(System.currentTimeMillis() - startTime, TimeUnit.MILLISECONDS)

}
