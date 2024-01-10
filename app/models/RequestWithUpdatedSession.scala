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

package models

import play.api.libs.typedmap.TypedMap
import play.api.mvc.request.{RemoteConnection, RequestTarget}
import play.api.mvc.{Headers, Request, Session}

case class RequestWithUpdatedSession[A](originalRequest: Request[A], sessionId: String) extends Request[A] {
  override def body: A = originalRequest.body
  override def connection: RemoteConnection = originalRequest.connection
  override def method: String = originalRequest.method
  override def target: RequestTarget = originalRequest.target
  override def version: String = originalRequest.version
  override def headers: Headers = originalRequest.headers
  override def attrs: TypedMap = originalRequest.attrs
  override def session: Session = originalRequest.session + ("sessionId" -> sessionId)
}
