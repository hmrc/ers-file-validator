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

package utils

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.libs.json.{JsPath, JsonValidationError}

class LogUtilsSpec extends AnyWordSpecLike with Matchers {

  "LogUtils.formatErrorMessageFromJsonParseFailure" should {

    "return an empty string when given an empty sequence of errors" in {
      val errors: Seq[(JsPath, Seq[JsonValidationError])] = Seq.empty

      LogUtils.formatErrorMessageFromJsonParseFailure(errors) shouldBe ""
    }

    "return a formatted error message for a single path with a single validation error" in {
      val errors: Seq[(JsPath, Seq[JsonValidationError])] = Seq(
        (JsPath \ "field1", Seq(JsonValidationError("error.path.missing")))
      )

      LogUtils.formatErrorMessageFromJsonParseFailure(errors) shouldBe "obj.field1: error.path.missing"
    }

    "return a formatted error message for a single path with multiple validation errors" in {
      val errors: Seq[(JsPath, Seq[JsonValidationError])] = Seq(
        (
          JsPath \ "field1",
          Seq(
            JsonValidationError("error.path.missing"),
            JsonValidationError("error.expected.jsstring")
          )
        )
      )

      LogUtils.formatErrorMessageFromJsonParseFailure(errors) shouldBe
        "obj.field1: error.path.missing, obj.field1: error.expected.jsstring"
    }

    "return a formatted error message for multiple paths each with a single validation error" in {
      val errors: Seq[(JsPath, Seq[JsonValidationError])] = Seq(
        (JsPath \ "field1", Seq(JsonValidationError("error.path.missing"))),
        (JsPath \ "field2", Seq(JsonValidationError("error.expected.jsstring")))
      )

      LogUtils.formatErrorMessageFromJsonParseFailure(errors) shouldBe
        "obj.field1: error.path.missing, obj.field2: error.expected.jsstring"
    }

    "return a formatted error message for multiple paths with multiple validation errors" in {
      val errors: Seq[(JsPath, Seq[JsonValidationError])] = Seq(
        (
          JsPath \ "field1",
          Seq(
            JsonValidationError("error.path.missing"),
            JsonValidationError("error.expected.jsnumber")
          )
        ),
        (JsPath \ "field2", Seq(JsonValidationError("error.expected.jsstring")))
      )

      LogUtils.formatErrorMessageFromJsonParseFailure(errors) shouldBe
        "obj.field1: error.path.missing, obj.field1: error.expected.jsnumber, obj.field2: error.expected.jsstring"
    }

    "handle a validation error with multiple messages" in {
      val errors: Seq[(JsPath, Seq[JsonValidationError])] = Seq(
        (JsPath \ "field1", Seq(JsonValidationError(Seq("error.one", "error.two"))))
      )

      LogUtils.formatErrorMessageFromJsonParseFailure(errors) shouldBe "obj.field1: error.one, error.two"
    }

    "handle nested paths" in {
      val errors: Seq[(JsPath, Seq[JsonValidationError])] = Seq(
        (JsPath \ "outer" \ "inner", Seq(JsonValidationError("error.path.missing")))
      )

      LogUtils.formatErrorMessageFromJsonParseFailure(errors) shouldBe "obj.outer.inner: error.path.missing"
    }
  }

}
