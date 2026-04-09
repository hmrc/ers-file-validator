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

import models.upscan.UpscanCallback
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.libs.json._

import java.time.{Instant, ZoneId, ZonedDateTime}
import scala.collection.mutable.ListBuffer

class SchemeDataSpec extends AnyWordSpecLike {

  private val fixedTimestamp =
    ZonedDateTime.ofInstant(Instant.ofEpochMilli(1710000000000L), ZoneId.of("UTC"))

  private val schemeInfo = SchemeInfo(
    schemeRef = "ref-123",
    timestamp = fixedTimestamp,
    schemeId = "scheme-id",
    taxYear = "2024",
    schemeName = "My Scheme",
    schemeType = "EMI"
  )

  private val upscanCallback = UpscanCallback(
    name = "test.csv",
    downloadUrl = "http://example.com/file.csv",
    length = Some(123L),
    contentType = Some("text/csv"),
    customMetadata = Some(Json.obj("foo" -> "bar")),
    noOfRows = Some(10)
  )

  "SchemeData format" should {
    "write and read SchemeData correctly" in {
      val schemeData = SchemeData(
        schemeInfo = schemeInfo,
        sheetName = "Sheet1",
        numberOfParts = Some(2),
        data = ListBuffer(Seq("a", "b"), Seq("c", "d"))
      )

      val json   = Json.toJson(schemeData)
      val result = json.as[SchemeData]

      result mustBe schemeData
    }
  }

  "SubmissionsSchemeData format" should {
    "write and read SubmissionsSchemeData correctly" in {
      val submissionsSchemeData = SubmissionsSchemeData(
        schemeInfo = schemeInfo,
        sheetName = "SheetA",
        data = upscanCallback,
        numberOfRows = 10
      )

      val json   = Json.toJson(submissionsSchemeData)
      val result = json.as[SubmissionsSchemeData]

      result mustBe submissionsSchemeData
    }
  }

  "SchemeInfo dateFormatDefault" should {
    "read a ZonedDateTime from JsNumber epoch millis" in {
      val epochMillis = 1710000000000L

      val result = SchemeInfo.dateFormatDefault.reads(JsNumber(epochMillis))

      result mustBe JsSuccess(
        ZonedDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneId.of("UTC"))
      )
    }

    "return JsError when reading a non-number value" in {
      val result = SchemeInfo.dateFormatDefault.reads(JsString("not-a-date"))

      result mustBe JsError(Seq(JsPath() -> Seq(JsonValidationError("error.expected.date"))))
    }

    "write a ZonedDateTime as JsNumber epoch millis" in {
      val result = SchemeInfo.dateFormatDefault.writes(fixedTimestamp)

      result mustBe JsNumber(1710000000000L)
    }
  }

  "SchemeInfo format" should {
    "write and read SchemeInfo correctly" in {
      val json   = Json.toJson(schemeInfo)
      val result = json.as[SchemeInfo]

      result mustBe schemeInfo
    }
  }

  "UpscanCallback format" should {
    "write callback with UploadedSuccessfully type" in {
      val json = Json.toJson(upscanCallback)

      (json \ "name").as[String]             mustBe "test.csv"
      (json \ "downloadUrl").as[String]      mustBe "http://example.com/file.csv"
      (json \ "length").as[Long]             mustBe 123L
      (json \ "contentType").as[String]      mustBe "text/csv"
      (json \ "customMetadata").as[JsObject] mustBe Json.obj("foo" -> "bar")
      (json \ "noOfRows").as[Int]            mustBe 10
      (json \ "_type").as[String]            mustBe "UploadedSuccessfully"
    }

    "read callback correctly" in {
      val json = Json.obj(
        "name"           -> "test.csv",
        "downloadUrl"    -> "http://example.com/file.csv",
        "length"         -> 123L,
        "contentType"    -> "text/csv",
        "customMetadata" -> Json.obj("foo" -> "bar"),
        "noOfRows"       -> 10
      )

      val result = json.as[UpscanCallback]

      result mustBe upscanCallback
    }

    "read callback correctly when optional fields are missing" in {
      val json = Json.obj(
        "name"        -> "test.csv",
        "downloadUrl" -> "http://example.com/file.csv"
      )

      val result = json.as[UpscanCallback]

      result mustBe UpscanCallback(
        name = "test.csv",
        downloadUrl = "http://example.com/file.csv",
        length = None,
        contentType = None,
        customMetadata = None,
        noOfRows = None
      )
    }
  }

}
