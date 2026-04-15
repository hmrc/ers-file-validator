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

    "delegate all request fields to the original request" in {
      val attrs   = TypedMap.empty
      val headers = Headers("X-Test" -> "123", "X-Trace" -> "abc")

      val request = FakeRequest("POST", "/path?x=1")
        .withBody("my-body")
        .withHeaders(headers.headers: _*)
        .withAttrs(attrs)
        .withSession("foo" -> "bar")

      val updatedRequest = RequestWithUpdatedSession(request, "session-1")

      updatedRequest.body             shouldBe "my-body"
      updatedRequest.method           shouldBe "POST"
      updatedRequest.target.uriString shouldBe "/path?x=1"
      updatedRequest.target.path      shouldBe "/path"
      updatedRequest.version          shouldBe request.version
      updatedRequest.headers          shouldBe headers
      updatedRequest.attrs            shouldBe attrs
    }

    "add sessionId and preserve existing session values" in {
      val request = FakeRequest("GET", "/test")
        .withBody("test-body")
        .withSession("userId" -> "user-123")

      val updatedRequest = RequestWithUpdatedSession(request, "session-999")

      updatedRequest.session.get("userId")    shouldBe Some("user-123")
      updatedRequest.session.get("sessionId") shouldBe Some("session-999")
    }

    "add sessionId when original session is empty" in {
      val request = FakeRequest("GET", "/test")
        .withBody("test-body")

      val updatedRequest = RequestWithUpdatedSession(request, "new-session")

      updatedRequest.session.data shouldBe Map("sessionId" -> "new-session")
    }

    "replace existing sessionId and preserve other values" in {
      val request = FakeRequest("GET", "/test")
        .withBody("test-body")
        .withSession("sessionId" -> "old", "foo" -> "bar")

      val updatedRequest = RequestWithUpdatedSession(request, "new")

      updatedRequest.session.get("sessionId") shouldBe Some("new")
      updatedRequest.session.get("foo")       shouldBe Some("bar")
    }
  }

}