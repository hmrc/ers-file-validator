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

package fixtures

import play.api.libs.json.JsValue
import play.api.mvc.{Action, AnyContent, ControllerComponents, Result, Request}
import play.api.test.Helpers.stubControllerComponents
import scala.concurrent.Future
import play.api.mvc.ActionBuilder

trait WithMockedAuthActions {

  type AsyncRequest = Request[AnyContent] => Future[Result]
  type AsyncRequestJson = Request[JsValue] => Future[Result]
  val cc: ControllerComponents = stubControllerComponents()
  val action: ActionBuilder[Request,AnyContent] = cc.actionBuilder

  def mockAuthorisedAction(empRef: String)(body: AsyncRequest): Action[AnyContent] = action.async { implicit request =>
    body(request)
  }

  def mockAuthorisedActionWithBody(empRef: String)(body: AsyncRequestJson): Action[JsValue] = action.async(cc.parsers.json) { implicit request =>
    body(request)
  }
}
