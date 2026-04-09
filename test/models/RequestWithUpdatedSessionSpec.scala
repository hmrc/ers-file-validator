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
import play.api.mvc.request.{RemoteConnection, RequestTarget}
import play.api.mvc.{Headers, Request, Session}

import java.net.InetAddress

class RequestWithUpdatedSessionSpec extends AnyWordSpec with Matchers {

  private def stubRequest(
    bodyValue: String = "test-body",
    remoteAddressValue: String = "127.0.0.1",
    methodValue: String = "GET",
    uriValue: String = "/test?x=1",
    pathValue: String = "/test",
    versionValue: String = "HTTP/1.1",
    headersValue: Headers = Headers("X-Test" -> "123"),
    attrsValue: TypedMap = TypedMap.empty,
    sessionValue: Session = Session()
  ): Request[String] =
    new Request[String] {
      override def body: String = bodyValue

      override def connection: RemoteConnection =
        RemoteConnection(
          remoteAddress = InetAddress.getByName(remoteAddressValue),
          secure = false,
          clientCertificateChain = None
        )

      override def method: String = methodValue

      override def target: RequestTarget =
        RequestTarget(
          uriString = uriValue,
          path = pathValue,
          queryString = Map("x" -> Seq("1"))
        )

      override def version: String = versionValue

      override def headers: Headers = headersValue

      override def attrs: TypedMap = attrsValue

      override def session: Session = sessionValue
    }

  "RequestWithUpdatedSession" should {

    "delegate all request fields to the original request" in {
      val attrs   = TypedMap.empty
      val headers = Headers("X-Test" -> "123", "X-Trace" -> "abc")

      val request = stubRequest(
        bodyValue = "my-body",
        remoteAddressValue = "10.0.0.1",
        methodValue = "POST",
        uriValue = "/path?x=1",
        pathValue = "/path",
        versionValue = "HTTP/2.0",
        headersValue = headers,
        attrsValue = attrs,
        sessionValue = Session(Map("foo" -> "bar"))
      )

      val updatedRequest = RequestWithUpdatedSession(request, "session-1")

      updatedRequest.body                     shouldBe "my-body"
      updatedRequest.connection.remoteAddress shouldBe InetAddress.getByName("10.0.0.1")
      updatedRequest.method                   shouldBe "POST"
      updatedRequest.target.uriString         shouldBe "/path?x=1"
      updatedRequest.target.path              shouldBe "/path"
      updatedRequest.version                  shouldBe "HTTP/2.0"
      updatedRequest.headers                  shouldBe headers
      updatedRequest.attrs                    shouldBe attrs
    }

    "add sessionId and preserve existing session values" in {
      val request = stubRequest(
        sessionValue = Session(Map("userId" -> "user-123"))
      )

      val updatedRequest = RequestWithUpdatedSession(request, "session-999")

      updatedRequest.session.get("userId")    shouldBe Some("user-123")
      updatedRequest.session.get("sessionId") shouldBe Some("session-999")
    }

    "add sessionId when original session is empty" in {
      val request = stubRequest(
        sessionValue = Session()
      )

      val updatedRequest = RequestWithUpdatedSession(request, "new-session")

      updatedRequest.session.data shouldBe Map("sessionId" -> "new-session")
    }

    "replace existing sessionId and preserve other values" in {
      val request = stubRequest(
        sessionValue = Session(Map("sessionId" -> "old", "foo" -> "bar"))
      )

      val updatedRequest = RequestWithUpdatedSession(request, "new")

      updatedRequest.session.get("sessionId") shouldBe Some("new")
      updatedRequest.session.get("foo")       shouldBe Some("bar")
    }
  }

}
