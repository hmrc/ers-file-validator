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

package controllers

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.http.scaladsl.Http
import org.apache.pekko.http.scaladsl.model.{HttpRequest, HttpResponse}
import org.apache.pekko.stream.scaladsl.Source
import controllers.auth.Authorisation
import metrics.Metrics
import models._
import models.scheme.SchemeMismatchError
import models.upscan.{UpscanCallback, UpscanCsvFileData, UpscanFileData}
import play.api.Logging
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import services.audit.AuditEvents
import services.{ProcessCsvService, ProcessOdsService, SessionCacheService}
import uk.gov.hmrc.http.SessionId
import uk.gov.hmrc.play.bootstrap.auth.DefaultAuthConnector
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DataUploadController @Inject()(auditEvents: AuditEvents,
                                     sessionService: SessionCacheService,
                                     processOdsService: ProcessOdsService,
                                     processCsvService: ProcessCsvService,
                                     val authConnector: DefaultAuthConnector,
                                     val cc: ControllerComponents,
                                     val defaultActionBuilder: DefaultActionBuilder
                                     )(implicit val ec: ExecutionContext, actorSystem: ActorSystem)
  extends BackendController(cc) with Metrics with Authorisation with Logging {

  def processFileDataFromFrontend(empRef: String): Action[AnyContent] = authorisedAction(empRef) {
    implicit request: Request[AnyContent] =>
      val startTime = System.currentTimeMillis()
      logger.debug("File Processing Request Received At: " + startTime)

      val json = request.body.asJson.get

      json.validate[UpscanFileData].fold(
        valid = res => {
          implicit val schemeInfo: SchemeInfo = res.schemeInfo
          processOdsService.processFile(res.callbackData, empRef).map {
            case Right(result) =>
              deliverFileProcessingMetrics(startTime)
              Ok(result.toString)

            case Left(error: ErsError) =>
              deliverFileProcessingMetrics(startTime)

              error match {
                case schemeError: SchemeTypeMismatchError =>
                  logger.warn(s"[DataUploadController][processFileDataFromFrontend] Scheme type mismatch: " +
                    s"${schemeError.message}, expected: ${schemeError.expectedSchemeType}, got: ${schemeError.requestSchemeType}, schemeRef: ${schemeInfo.schemeRef}")
                  val schemeMismatch = SchemeMismatchError(schemeError.message, schemeError.expectedSchemeType, schemeError.requestSchemeType)
                  BadRequest(Json.toJson(schemeMismatch))

                case userError: UserValidationError =>
                  logger.warn(s"[DataUploadController][processFileDataFromFrontend] User validation error: ${userError.message}, context: ${userError.context}, schemeRef: ${schemeInfo.schemeRef}")
                  BadRequest(userError.message)

                case systemError: SystemError =>
                  logger.error(s"[DataUploadController][processFileDataFromFrontend] Unexpected system error: ${systemError.message}")
                  InternalServerError
              }
          }
        },
        invalid = e => {
          val errorMessage = e.mkString(", ")
          logger.error(s"[DataUploadController][processFileDataFromFrontend] An exception occurred while validating file data :$errorMessage")
          deliverFileProcessingMetrics(startTime)
          Future.successful(BadRequest(e.toString))
        }
      )
  }

  def deliverFileProcessingMetrics(startTime: Long): Unit =
    metrics.fileProcessingTimer(System.currentTimeMillis() - startTime, TimeUnit.MILLISECONDS)

  def processCsvFileDataFromFrontendV2(empRef: String): Action[JsValue] = authorisedActionWithBody(empRef) {
    implicit request: Request[JsValue] =>

      val startTime = System.currentTimeMillis()
      request.body.validate[UpscanCsvFileData].fold(
        valid = res => {
          val schemeInfo: SchemeInfo = res.schemeInfo
          logger.debug("SCHEME TYPE: " + schemeInfo.schemeType)
          deliverFileProcessingMetrics(startTime)

          val processedFiles: List[Future[Either[ErsError, CsvFileSubmissions]]] = processCsvService.processFiles(res, streamFile)

          val extractedSchemeData: Seq[Future[Either[ErsError, CsvFileLengthInfo]]] = processedFiles.map { submission =>
            submission.flatMap(processCsvService.extractSchemeData(res.schemeInfo, empRef, _))
          }

          Future.sequence(extractedSchemeData).flatMap { allFilesResults =>
            allFilesResults.collectFirst {
              case Left(error: ErsError) =>
                error match {
                  case userError: UserValidationError =>
                    logger.warn(s"[DataUploadController][processCsvFileDataFromFrontendV2] User validation error: ${userError.message}, schemeRef: ${schemeInfo.schemeRef}")
                    deliverFileProcessingMetrics(startTime)
                    Future.successful(BadRequest(userError.message))
                  case systemError: SystemError =>
                    logger.error(s"[DataUploadController][processCsvFileDataFromFrontendV2] System error: ${systemError.message}, schemeRef: ${schemeInfo.schemeRef}")
                    deliverFileProcessingMetrics(startTime)
                    Future.successful(InternalServerError)
                }
            } match {
              case Some(futureResult) => futureResult
              case None =>
                val result: Seq[CsvFileLengthInfo] = allFilesResults.collect {
                  case Right(info) => info
                }
                val totalRowCount = result.foldLeft(0)((accum, inputTuple) => accum + inputTuple.fileLength)
                result.foreach((csvFileLengthInfo: CsvFileLengthInfo) =>
                  logger.info(s"[DataUploadController][processCsvFileDataFromFrontendV2]: Total number of rows for csv file, schemeRef ${schemeInfo.schemeRef} (scheme type: ${schemeInfo.schemeType}): ${csvFileLengthInfo.fileLength}")
                )

                auditEvents.totalRows(totalRowCount, schemeInfo)

                val sessionId = hc.sessionId.getOrElse(SessionId(UUID.randomUUID().toString)).value

                sessionService.storeCallbackData(res.callbackData.head, totalRowCount)(RequestWithUpdatedSession(request, sessionId)).map {
                  case callback: Option[UpscanCallback] if callback.isDefined =>
                    val numberOfSlices = result.map(_.noOfSlices).sum
                    logger.info(s"[DataUploadController][processCsvFileDataFromFrontendV2] File validated successfully, schemeRef: ${schemeInfo.schemeRef}")
                    Ok(numberOfSlices.toString)
                  case _ =>
                    logger.error(
                      s"[DataUploadController][processCsvFileDataFromFrontendV2] csv storeCallbackData failed" +
                        s" while storing data, schemeRef: ${schemeInfo.schemeRef}, timestamp: ${java.time.LocalTime.now()}.")
                    deliverFileProcessingMetrics(startTime)
                    Accepted("csv callback data storage in sessioncache failed")
                }
            }
          }
        },
        invalid = e => {
          logger.warn("[DataUploadController][processCsvFileDataFromFrontendV2] Invalid request body")
          deliverFileProcessingMetrics(startTime)
          Future.successful(BadRequest(e.toString))
        }
      )
  }


  private[controllers] def streamFile(downloadUrl: String): Source[HttpResponse, _] = {
    Source
      .single(HttpRequest(uri = downloadUrl))
      .mapAsync(parallelism = 1)(makeRequest)
  }

  // $COVERAGE-OFF$
  private[controllers] def makeRequest(request: HttpRequest): Future[HttpResponse] = Http()(actorSystem).singleRequest(request)
  // $COVERAGE-ON$
}
