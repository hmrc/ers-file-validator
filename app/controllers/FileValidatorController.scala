/*
 * Copyright 2024 HM Revenue & Customs
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

import models.RequestWithUpdatedSession
import models.upscan.UploadStatus
import play.api.Logging
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import services.SessionCacheService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FileValidatorController @Inject()(sessionService: SessionCacheService,
                                        val cc: ControllerComponents,
                                        val defaultActionBuilder: DefaultActionBuilder)
                                       (implicit val ec: ExecutionContext) extends BackendController(cc) with Logging {

  def createCallbackRecord(sessionId: String): Action[AnyContent] = Action.async {
    implicit request =>
      sessionService.createCallbackRecord(RequestWithUpdatedSession(request, sessionId))
        .map { case (_, sessionId) =>
          Created(Json.toJson(Map("sessionId" -> sessionId)))
        }.recover {
          case _: Exception =>
            InternalServerError("An error occurred while creating the callback record.")
        }
  }

  def getCallbackRecord(sessionId: String): Action[AnyContent] = Action.async { implicit request =>
    sessionService.getCallbackRecord(RequestWithUpdatedSession(request, sessionId))
      .map {
        case Some(record) => Ok(Json.toJson(record))
        case None => NotFound("No callback record found")
      }.recover {
        case _: Exception =>
          logger.error("An error occurred while retrieving the callback record.")
          InternalServerError("An error occurred while retrieving the callback record.")
      }
  }

  def updateCallbackRecord(sessionId: String): Action[JsValue] = Action.async(parse.json) {
    implicit request: Request[JsValue] =>
      request.body.validate[UploadStatus].fold(
        valid = uploadStatus => {
          sessionService.updateCallbackRecord(uploadStatus)(RequestWithUpdatedSession(request, sessionId))
            .map(_ => NoContent)
            .recover {
              case _: Exception =>
                logger.error("An error occurred while updating the callback record.")
                InternalServerError("An error occurred while updating the callback record.")
            }
        },
        invalid = errors => {
          Future.successful(BadRequest(s"Request contains errors: $errors"))
        }
      )
  }
}
