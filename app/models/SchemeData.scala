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

import models.upscan.UpscanCallback

import java.time.{Instant, ZoneId, ZonedDateTime}
import play.api.libs.json._

import java.time.format.DateTimeFormatter
import scala.collection.mutable.ListBuffer
import scala.util.control.Exception.nonFatalCatch

case class SchemeData (schemeInfo: SchemeInfo, sheetName: String, numberOfParts: Option[Int], data: ListBuffer[Seq[String]])

object SchemeData {
  implicit val formatSchemeData: OFormat[SchemeData] = Json.format[SchemeData]
}

case class SubmissionsSchemeData(schemeInfo: SchemeInfo,
                                 sheetName: String,
                                 data: UpscanCallback,
                                 numberOfRows: Int)

object SubmissionsSchemeData {
  implicit val formatSubmissionsSchemeData: OFormat[SubmissionsSchemeData] = Json.format[SubmissionsSchemeData]
}

case class SchemeInfo (schemeRef:String,
                       timestamp: ZonedDateTime = ZonedDateTime.now,
                       schemeId: String,
                       taxYear: String,
                       schemeName: String,
                       schemeType: String)

object SchemeInfo {

  implicit val dateFormatDefault: Format[ZonedDateTime] = new Format[ZonedDateTime] {

    override def reads(json: JsValue): JsResult[ZonedDateTime] = json match {
      case JsNumber(d) => JsSuccess(ZonedDateTime.ofInstant(Instant.ofEpochMilli(d.toLong), ZoneId.of("UTC")))
      case _ => JsError(Seq(JsPath() -> Seq(JsonValidationError("error.expected.date"))))
    }

    override def writes(o: ZonedDateTime): JsValue = JsNumber(o.toInstant.toEpochMilli)
  }

  implicit val formatSchemeInfo: OFormat[SchemeInfo] = Json.format[SchemeInfo]
}
