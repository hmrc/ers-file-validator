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

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.scaladsl.Source
import controllers.auth.Authorisation
import metrics.Metrics
import models._
import models.upscan.{UpscanCallback, UpscanCsvFileData, UpscanFileData}
import play.api.Logging
import play.api.libs.json.JsValue
import play.api.mvc._
import services.{ProcessCsvService, ProcessOdsService, SessionService}
import uk.gov.hmrc.play.bootstrap.auth.DefaultAuthConnector
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DataUploadController @Inject()(sessionService: SessionService,
                                     processOdsService: ProcessOdsService,
                                     processCsvService: ProcessCsvService,
                                     val authConnector: DefaultAuthConnector,
                                     val cc: ControllerComponents,
                                     val defaultActionBuilder: DefaultActionBuilder
                                     )(implicit val ec: ExecutionContext, actorSystem: ActorSystem)
  extends BackendController(cc) with Metrics with Authorisation with Logging {

  def processFileDataFromFrontend(empRef: String): Action[AnyContent] = authorisedAction(empRef) {
    implicit request: Request[AnyContent] =>
      val startTime =  System.currentTimeMillis()
      logger.debug("File Processing Request Received At: " + startTime)
      val json = request.body.asJson.get
      json.validate[UpscanFileData].fold(
        valid = res => {
          implicit val schemeInfo: SchemeInfo = res.schemeInfo
          processOdsService.processFile(res.callbackData, empRef).map { result =>
            deliverFileProcessingMetrics(startTime)
            Ok(result.toString)
          }.recover {
            case e: ERSFileProcessingException =>
              deliverFileProcessingMetrics(startTime)
              logger.warn(s"[DataUploadController][processFileDataFromFrontend] ERSFileProcessingException: ${e.getMessage}")
              Accepted(e.message)
            case er: Exception =>
              deliverFileProcessingMetrics(startTime)
              logger.error(s"[DataUploadController][processFileDataFromFrontend] An exception occurred: ${er.getMessage}")
              InternalServerError
          }
        },
        invalid = e => {
          logger.error(e.toString())
          deliverFileProcessingMetrics(startTime)
          Future.successful(BadRequest(e.toString))
        }
      )
  }

  def deliverFileProcessingMetrics(startTime:Long): Unit =
    metrics.fileProcessingTimer(System.currentTimeMillis() - startTime, TimeUnit.MILLISECONDS)

  // TODO Old version from before csv processing re-write, to be removed
  def processCsvFileDataFromFrontend(empRef:String): Action[JsValue] = authorisedActionWithBody(empRef) {
    implicit request: Request[JsValue] =>
      logger.debug("[DataUploadController][processCsvFileDataFromFrontend] receiving request on V1 processCsvFileDataFromFrontend")
      val startTime =  System.currentTimeMillis()
      request.body.validate[UpscanCsvFileData].fold(
        valid = res => {
          val schemeInfo: SchemeInfo = res.schemeInfo
          logger.debug("SCHEME TYPE: " + schemeInfo.schemeType)
          deliverFileProcessingMetrics(startTime)

          val processedFiles: List[Future[Either[Throwable, CsvFileContents]]] = processCsvService.processFiles(res, streamFile)

          val extractedSchemeData: Seq[Future[Either[Throwable, CsvFileLengthInfo]]] = processedFiles.map{ list =>
            list.flatMap(processCsvService.extractSchemeData(res.schemeInfo, empRef, _))}

          Future.sequence(extractedSchemeData).flatMap { oneFileResults =>
            oneFileResults.collectFirst {
              case Left(throwable: ERSFileProcessingException) =>
                logger.error(s"[DataUploadController][processCsvFileDataFromFrontend] ERS file processing exception: ${throwable.message}")
                deliverFileProcessingMetrics(startTime)
                Future.successful(Accepted(throwable.message))
              case Left(throwable) =>
                logger.error(s"[DataUploadController][processCsvFileDataFromFrontend] Unknown exception: ${throwable.getMessage}, full exception: $throwable")
                deliverFileProcessingMetrics(startTime)
                Future.successful(InternalServerError)
            } match {
              case Some(futureResult) => futureResult
              case None =>
                val result: Seq[CsvFileLengthInfo] = oneFileResults.flatMap {
                  case Left(exception) => throw exception
                  case Right(csvFileLengthInfo) => Some(csvFileLengthInfo)
                }
                val totalRowCount = result.foldLeft(0)((accum, inputTuple) => accum + inputTuple.fileLength)
                sessionService.storeCallbackData(res.callbackData.head, totalRowCount).map {
                  case callback: Option[UpscanCallback] if callback.isDefined =>
                    val numberOfSlices = result.map(_.noOfSlices).sum
                    logger.info("[DataUploadController][processCsvFileDataFromFrontend] File validated successfully")
                    Ok(numberOfSlices.toString)
                  case _ =>
                    logger.error(
                      s"[DataUploadController][processCsvFileDataFromFrontend] csv storeCallbackData failed" +
                        s" while storing data, timestamp: ${java.time.LocalTime.now()}.")
                    val exception = ERSFileProcessingException("csv callback data storage in sessioncache failed ", "Exception storing csv callback data")
                    deliverFileProcessingMetrics(startTime)
                    Accepted(exception.message)
              }
          }}
        },
        invalid = e => {
          logger.warn("[DataUploadController][processCsvFileDataFromFrontend] Invalid request body")
          deliverFileProcessingMetrics(startTime)
          Future(BadRequest(e.toString))
        }
      )
  }

  def processCsvFileDataFromFrontendV2(empRef: String): Action[JsValue] = authorisedActionWithBody(empRef) {
    implicit request: Request[JsValue] =>

      val startTime = System.currentTimeMillis()
      request.body.validate[UpscanCsvFileData].fold(
        valid = res => {
          val schemeInfo: SchemeInfo = res.schemeInfo
          logger.debug("SCHEME TYPE: " + schemeInfo.schemeType)
          deliverFileProcessingMetrics(startTime)

          val processedFiles: List[Future[Either[Throwable, CsvFileSubmissions]]] = processCsvService.processFilesNew(res, streamFile)

          val extractedSchemeData: Seq[Future[Either[Throwable, CsvFileLengthInfo]]] = processedFiles.map { submission =>
            submission.flatMap(processCsvService.extractSchemeDataNew(res.schemeInfo, empRef, _))
          }

          Future.sequence(extractedSchemeData).flatMap { allFilesResults =>
            allFilesResults.collectFirst {
              case Left(throwable: ERSFileProcessingException) =>
                logger.warn(s"[DataUploadController][processCsvFileDataFromFrontend] ERS file processing exception: ${throwable.message}")
                deliverFileProcessingMetrics(startTime)
                Future.successful(Accepted(throwable.message))
              case Left(throwable) =>
                logger.error(s"[DataUploadController][processCsvFileDataFromFrontend] Unknown exception: ${throwable.getMessage}, full exception: $throwable")
                deliverFileProcessingMetrics(startTime)
                Future.successful(InternalServerError)
            } match {
              case Some(futureResult) => futureResult
              case None =>
                val result: Seq[CsvFileLengthInfo] = allFilesResults.flatMap {
                  case Right(info) => Some(info)
                  case _ => None
                }
                val totalRowCount = result.foldLeft(0)((accum, inputTuple) => accum + inputTuple.fileLength)
                sessionService.storeCallbackData(res.callbackData.head, totalRowCount).map {
                  case callback: Option[UpscanCallback] if callback.isDefined =>
                    val numberOfSlices = result.map(_.noOfSlices).sum
                    logger.info("[DataUploadController][processCsvFileDataFromFrontendV2] File validated successfully")
                    Ok(numberOfSlices.toString)
                  case _ =>
                    logger.error(
                      s"[DataUploadController][processCsvFileDataFromFrontend] csv storeCallbackData failed" +
                        s" while storing data, timestamp: ${java.time.LocalTime.now()}.")
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
