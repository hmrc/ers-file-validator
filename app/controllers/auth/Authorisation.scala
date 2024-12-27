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

package controllers.auth

import play.api.Logging
import play.api.libs.json.JsValue
import play.api.mvc.Results.Unauthorized
import play.api.mvc._
import uk.gov.hmrc.auth.core.{AuthorisationException, AuthorisedFunctions, ConfidenceLevel, Enrolment}
import uk.gov.hmrc.domain.EmpRef
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

trait Authorisation extends AuthorisedFunctions with Logging {
  implicit val ec: ExecutionContext
  val cc: ControllerComponents
  val defaultActionBuilder: DefaultActionBuilder
  private type AsyncRequest = Request[AnyContent] => Future[Result]
  private type AsyncJsonRequest = Request[JsValue] => Future[Result]

  def authorisedAction(slashSeparatedEmpRef: String)(body: AsyncRequest): Action[AnyContent] = defaultActionBuilder.async { implicit request =>
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(request)
    Try(EmpRef.fromIdentifiers(slashSeparatedEmpRef)).toOption.map(empRef =>
      authorised(
        ConfidenceLevel.L50 and Enrolment("IR-PAYE")
          .withIdentifier("TaxOfficeNumber", empRef.taxOfficeNumber)
          .withIdentifier("TaxOfficeReference", empRef.taxOfficeReference)
          .withDelegatedAuthRule("ers-auth")
      ) {
        body(request)
      } recover {
        case exception: AuthorisationException =>
          logger.warn(s"[Authorisation][authorisedAction] user is unauthorised for ${request.uri} with " +
            s"exception ${exception.getMessage}", exception)
          Unauthorized
      }
    ).getOrElse {
      logger.warn(s"[Authorisation][authorisedAction] an invalid empRef was supplied when trying to hit ${request.uri}")
      Future.successful(Unauthorized)
    }
  }

  def authorisedActionWithBody(slashSeparatedEmpRef: String)(body: AsyncJsonRequest): Action[JsValue] = defaultActionBuilder.async(cc.parsers.json) {
    implicit request: Request[JsValue] =>
      implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(request)
      Try(EmpRef.fromIdentifiers(slashSeparatedEmpRef)).toOption.map(empRef =>
        authorised(
          ConfidenceLevel.L50 and Enrolment("IR-PAYE")
            .withIdentifier("TaxOfficeNumber", empRef.taxOfficeNumber)
            .withIdentifier("TaxOfficeReference", empRef.taxOfficeReference)
            .withDelegatedAuthRule("ers-auth")
        ) {
          body(request)
        } recover {
          case exception: AuthorisationException =>
            logger.warn(s"[Authorisation][authorisedAction] user is unauthorised for ${request.uri} with exception  ${exception.getMessage}", exception)
            Unauthorized
        }
      ).getOrElse {
        logger.warn(s"[Authorisation][authorisedAction] an invalid empRef was supplied when trying to hit ${request.uri}")
        Future.successful(Unauthorized)
      }
  }
}