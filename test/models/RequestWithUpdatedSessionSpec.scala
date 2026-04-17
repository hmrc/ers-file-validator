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

package models

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.typedmap.TypedMap
import play.api.mvc.Headers
import play.api.test.FakeRequest

class RequestWithUpdatedSessionSpec extends AnyWordSpec with Matchers {

  "RequestWithUpdatedSession" should {

    "preserve the original request fields and add or replace the sessionId" in {
      val attrs   = TypedMap.empty
      val headers = Headers("X-Test" -> "123", "X-Trace" -> "abc")

      val request = FakeRequest("POST", "/path?x=1")
        .withBody("my-body")
        .withHeaders(headers.headers: _*)
        .withAttrs(attrs)
        .withSession("foo" -> "bar", "sessionId" -> "old-session")

      val updatedRequest = RequestWithUpdatedSession(request, "new-session")

      updatedRequest.body       shouldBe request.body
      updatedRequest.connection shouldBe request.connection
      updatedRequest.method     shouldBe request.method
      updatedRequest.target     shouldBe request.target
      updatedRequest.version    shouldBe request.version
      updatedRequest.headers    shouldBe request.headers
      updatedRequest.attrs      shouldBe request.attrs

      updatedRequest.session.get("foo")       shouldBe Some("bar")
      updatedRequest.session.get("sessionId") shouldBe Some("new-session")
    }
  }

}
