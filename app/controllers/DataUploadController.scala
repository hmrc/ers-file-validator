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

package controllers

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.scaladsl.Source
import controllers.auth.Authorisation
import javax.inject.{Inject, Singleton}
import metrics.Metrics
import models._
import models.upscan.{UpscanCallback, UpscanCsvFileData, UpscanFileData}
import play.api.Logger
import play.api.libs.json.JsValue
import play.api.mvc.{Action, AnyContent, ControllerComponents, DefaultActionBuilder, PlayBodyParsers, Request}
import services.{FileProcessingService, ProcessCsvService, SessionService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.auth.DefaultAuthConnector
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DataUploadController @Inject()(sessionService: SessionService,
                                     fileProcessService: FileProcessingService,
                                     processCsvService: ProcessCsvService,
                                     val authConnector: DefaultAuthConnector,
                                     val cc: ControllerComponents,
                                     val defaultActionBuilder: DefaultActionBuilder,
                                     )(implicit val ec: ExecutionContext, actorSystem: ActorSystem)
  extends BackendController(cc) with Metrics with Authorisation {

  def processFileDataFromFrontend(empRef: String): Action[AnyContent] = authorisedAction(empRef) {
    implicit request =>
      val startTime =  System.currentTimeMillis()
      Logger.debug("File Processing Request Received At: " + startTime)
      val json = request.body.asJson.get
      json.validate[UpscanFileData].fold(
        valid = res => {
          implicit val schemeInfo: SchemeInfo = res.schemeInfo
          try {
            val result = fileProcessService.processFile(res.callbackData, empRef)
            deliverFileProcessingMetrics(startTime)
            Future.successful(Ok(result.toString))
          } catch {
            case e:ERSFileProcessingException =>
              deliverFileProcessingMetrics(startTime)
							Logger.warn(s"[DataUploadController][processFileDataFromFrontend] ERSFileProcessingException - ${e.getMessage}")
              Future.successful(Accepted(e.message))
            case er: Exception =>
              deliverFileProcessingMetrics(startTime)
              Logger.error(er.getMessage)
              Future.successful(InternalServerError)
          }
        },
        invalid = e => {
          Logger.error(e.toString())
          deliverFileProcessingMetrics(startTime)
          Future.successful(BadRequest(e.toString))
        }
      )
  }

  def deliverFileProcessingMetrics(startTime:Long): Unit =
    metrics.fileProcessingTimer(System.currentTimeMillis() - startTime, TimeUnit.MILLISECONDS)

  def processCsvFileDataFromFrontend(empRef:String): Action[JsValue] = authorisedActionWithBody(empRef) {
    implicit request: Request[JsValue] =>
      val startTime =  System.currentTimeMillis()
      request.body.validate[UpscanCsvFileData].fold(
        valid = res => {
          val schemeInfo: SchemeInfo = res.schemeInfo
          Logger.debug("SCHEME TYPE: " + schemeInfo.schemeType)
          deliverFileProcessingMetrics(startTime)
          val processedFiles: List[Future[Either[Throwable, CsvFileContents]]] = processCsvService.processFiles(res, readFileCsv)

          val extractedSchemeData: Seq[Future[Either[Throwable, (Int, Int)]]] = processedFiles.map{ list =>
            list.flatMap(processCsvService.extractSchemeData(res.schemeInfo, empRef, _))}

          Future.sequence(extractedSchemeData).flatMap{ oneFileResults => oneFileResults.find(_.isLeft) match {
            case Some(Left(throwable: ERSFileProcessingException)) =>
              Logger.error(throwable.message)
              deliverFileProcessingMetrics(startTime)
              Future(Accepted(throwable.message))
            case Some(Left(throwable)) =>
              Logger.error(throwable.getMessage, throwable)
              deliverFileProcessingMetrics(startTime)
              Future(InternalServerError)
            case None =>
              val result: Seq[(Int, Int)] = oneFileResults.map(_.right.get)
              val totalRowCount = result.foldLeft(0) ((accum,inputTuple) => accum + inputTuple._2)
              sessionService.storeCallbackData(res.callbackData.head, totalRowCount).map {
                case callback: Option[UpscanCallback] if callback.isDefined =>
                  val numberOfSlices = result.map(_._1).sum
                  Ok(numberOfSlices.toString)
                case _ =>
                  Logger.error(
                    s"[DataUploadController][processCsvFileDataFromFrontend] csv storeCallbackData failed" +
                      s" while storing data, timestamp: ${System.currentTimeMillis()}.")
                  val exception = ERSFileProcessingException("csv callback data storage in sessioncache failed ", "Exception storing csv callback data")
                  deliverFileProcessingMetrics(startTime)
                  Accepted(exception.message)
              }
          }}
        },
        invalid = e => {
          deliverFileProcessingMetrics(startTime)
          Future(BadRequest(e.toString))
        }
      )
  }

  private[controllers] def readFileCsv(downloadUrl: String): Source[HttpResponse, _] = {
    Source
      .single(HttpRequest(uri = downloadUrl))
      .mapAsync(parallelism = 1)(makeRequest)
  }

  private[controllers] def makeRequest(request: HttpRequest): Future[HttpResponse] = Http()(actorSystem).singleRequest(request)


  def process(res: List[UpscanCallback], empRef: String)
             (hc: HeaderCarrier, schemeInfo: SchemeInfo, request: Request[_]): List[Future[(Int, Int)]] = {
    for {
      callbackData <- res
    } yield fileProcessService.processCsvFile(callbackData, empRef)(hc, schemeInfo,request)
  }
}
