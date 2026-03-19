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
import models.scheme.SchemeMismatchError
import models.upscan.UpscanFileData
import play.api.Logging
import play.api.libs.json.Json
import play.api.mvc._
import services.ProcessOdsService
import uk.gov.hmrc.play.bootstrap.auth.DefaultAuthConnector
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import utils.LogUtils

import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class OdsUploadController @Inject() (
  processOdsService: ProcessOdsService,
  val authConnector: DefaultAuthConnector,
  val cc: ControllerComponents,
  val defaultActionBuilder: DefaultActionBuilder
)(implicit val ec: ExecutionContext)
    extends BackendController(cc) with Metrics with Authorisation with Logging {

  def processOdsFile(empRef: String): Action[AnyContent] = authorisedAction(empRef) {
    implicit request: Request[AnyContent] =>
      val startTime = System.currentTimeMillis()
      logger.debug("File Processing Request Received At: " + startTime)

      request.body.asJson match {
        case Some(json) =>
          json
            .validate[UpscanFileData]
            .fold(
              valid = upscanFileData => {

                implicit val schemeInfo: SchemeInfo = upscanFileData.schemeInfo

                processOdsService.processFile(upscanFileData.callbackData, empRef).map {
                  case Right(result)         =>
                    deliverFileProcessingMetrics(startTime)
                    Ok(result.toString)
                  case Left(error: ErsException) =>
                    deliverFileProcessingMetrics(startTime)
                    handleOdsError(error)
                }
              },
              invalid = jsonValidationErrors => {

                val parseErrors = LogUtils.formatErrorMessageFromJsonParseFailure(jsonValidationErrors)

                logger.error(
                  s"[OdsUploadController][processOdsFile] An exception occurred while validating file data :$parseErrors"
                )

                deliverFileProcessingMetrics(startTime)
                Future.successful(BadRequest(s"Invalid request body, parse errors: $parseErrors"))
              }
            )
        case None       =>
          logger.error(s"[OdsUploadController][processOdsFile] No JSON body in request")
          Future.successful(BadRequest("No JSON body in request"))
      }
  }

  private def handleOdsError(error: ErsException)(implicit schemeInfo: SchemeInfo): Result =

    error match {
      case schemeError: SchemeTypeMismatchException =>
        logger.warn(
          s"[OdsUploadController][handleOdsError] Scheme type mismatch: " +
            s"${schemeError.message}, expected: ${schemeError.expectedSchemeType}, got: ${schemeError.requestSchemeType}, schemeRef: ${schemeInfo.schemeRef}"
        )

        val error =
          SchemeMismatchError(schemeError.message, schemeError.expectedSchemeType, schemeError.requestSchemeType)

        BadRequest(Json.toJson(error))

      case userError: UserValidationException =>
        logger.warn(
          s"[OdsUploadController][handleOdsError] User validation error: ${userError.message}, context: ${userError.context}, schemeRef: ${schemeInfo.schemeRef}"
        )

        BadRequest(userError.message)

      case systemError: SystemError =>
        logger.error(s"[OdsUploadController][handleOdsError] Unexpected system error: ${systemError.message}")
        InternalServerError
    }

  private def deliverFileProcessingMetrics(startTime: Long): Unit =
    metrics.fileProcessingTimer(System.currentTimeMillis() - startTime, TimeUnit.MILLISECONDS)

}
