/*
 * Copyright 2020 HM Revenue & Customs
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

import play.api.Logger
import play.api.mvc.Results.Unauthorized
import play.api.mvc.{ActionBuilder, ActionFunction, Request, Result}
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisationException, AuthorisedFunctions, ConfidenceLevel, Enrolment}
import uk.gov.hmrc.domain.EmpRef
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

case class AuthorisedAction(slashSeparatedEmpRef: String, authConnector: AuthConnector)
                           (implicit val ec: ExecutionContext)
  extends AuthAction {
  override val optionalEmpRef: Option[EmpRef] = Try(EmpRef.fromIdentifiers(slashSeparatedEmpRef)).toOption
}

trait AuthAction extends AuthorisedFunctions with ActionBuilder[Request] with ActionFunction[Request, Request] {
  val optionalEmpRef: Option[EmpRef]
  implicit val ec: ExecutionContext

  override def invokeBlock[A](request: Request[A], block: Request[A] => Future[Result]): Future[Result] = {
    implicit val hc: HeaderCarrier =
      HeaderCarrierConverter.fromHeadersAndSessionAndRequest(request.headers, request = Some(request))

    optionalEmpRef.map( empRef =>
      authorised(
        ConfidenceLevel.L50 and Enrolment("IR-PAYE")
          .withIdentifier("TaxOfficeNumber", empRef.taxOfficeNumber)
          .withIdentifier("TaxOfficeReference", empRef.taxOfficeReference)
          .withDelegatedAuthRule("ers-auth")
      ) {
        block(request)
      } recover {
        case exception: AuthorisationException =>
          Logger.warn(s"[AuthAction][invokeBlock] user is unauthorised for ${request.uri} with " +
            s"exception ${exception.getMessage}", exception)
          Unauthorized
      }
    ).getOrElse{
      Logger.warn(s"[AuthAction][invokeBlock] an invalid empRef was supplied when trying to hit ${request.uri}")
      Future.successful(Unauthorized)
    }
  }
}