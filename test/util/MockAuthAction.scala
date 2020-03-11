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

package util

import controllers.auth.AuthAction
import play.api.mvc.{Request, Result}
import uk.gov.hmrc.domain.EmpRef

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import uk.gov.hmrc.auth.core.AuthConnector
import org.scalatest.mockito.MockitoSugar

object MockAuthAction extends AuthAction with MockitoSugar {
  override val optionalEmpRef: Option[EmpRef] = None
  override implicit val ec: ExecutionContext = implicitly[ExecutionContext]
  override def authConnector: AuthConnector = mock[AuthConnector]

  override def invokeBlock[A](request: Request[A], block: Request[A] => Future[Result]): Future[Result] = {
    block(request)
  }
}
