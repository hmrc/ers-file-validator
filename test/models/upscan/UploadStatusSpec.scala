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

package models.upscan

import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.libs.json._

class UploadStatusSpec extends AnyWordSpecLike {

  "UploadStatus.readsUploadStatus" should {

    implicit val reads: Reads[UploadStatus] = UploadStatus.readsUploadStatus

    "read NotStarted" in {
      val json = Json.obj("_type" -> "NotStarted")

      json.as[UploadStatus] mustBe NotStarted
    }

    "read InProgress" in {
      val json = Json.obj("_type" -> "InProgress")

      json.as[UploadStatus] mustBe InProgress
    }

    "read Failed" in {
      val json = Json.obj("_type" -> "Failed")

      json.as[UploadStatus] mustBe Failed
    }

    "read UploadedSuccessfully" in {
      val json = Json.obj(
        "_type"       -> "UploadedSuccessfully",
        "name"        -> "file.csv",
        "downloadUrl" -> "http://example.com/file.csv",
        "noOfRows"    -> 12
      )

      json.as[UploadStatus] mustBe
        UploadedSuccessfully("file.csv", "http://example.com/file.csv", Some(12))
    }

    "return JsError for unexpected _type value" in {
      val json = Json.obj("_type" -> "SomethingElse")

      val result = reads.reads(json)

      result mustBe JsError("Unexpected value of _type: \"SomethingElse\"")
    }

    "return JsError when _type field is missing" in {
      val json = Json.obj("name" -> "file.csv")

      val result = reads.reads(json)

      result mustBe JsError("Missing _type field")
    }

    "return JsError when json is not an object" in {
      val json = JsString("not-an-object")

      val result = reads.reads(json)

      result mustBe JsError("Expected a JSON object")
    }
  }

  "UploadStatus.writesUploadStatus" should {

    implicit val writes: Writes[UploadStatus] = UploadStatus.writesUploadStatus

    "write NotStarted" in {
      Json.toJson(NotStarted: UploadStatus) mustBe
        Json.obj("_type" -> "NotStarted")
    }

    "write InProgress" in {
      Json.toJson(InProgress: UploadStatus) mustBe
        Json.obj("_type" -> "InProgress")
    }

    "write Failed" in {
      Json.toJson(Failed: UploadStatus) mustBe
        Json.obj("_type" -> "Failed")
    }

    "write UploadedSuccessfully" in {
      val status: UploadStatus =
        UploadedSuccessfully("file.csv", "http://example.com/file.csv", Some(12))

      Json.toJson(status) mustBe Json.obj(
        "name"        -> "file.csv",
        "downloadUrl" -> "http://example.com/file.csv",
        "noOfRows"    -> 12,
        "_type"       -> "UploadedSuccessfully"
      )
    }
  }

  "UploadedSuccessfully format" should {

    implicit val format: Format[UploadedSuccessfully] = Json.format[UploadedSuccessfully]

    "read and write correctly" in {
      val value = UploadedSuccessfully("file.csv", "http://example.com/file.csv", Some(12))

      val json = Json.toJson(value)
      json.as[UploadedSuccessfully] mustBe value
    }

    "handle missing optional noOfRows" in {
      val json = Json.obj(
        "name"        -> "file.csv",
        "downloadUrl" -> "http://example.com/file.csv"
      )

      json.as[UploadedSuccessfully] mustBe
        UploadedSuccessfully("file.csv", "http://example.com/file.csv", None)
    }
  }

}