/*
 * Copyright 2021 HM Revenue & Customs
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

import org.joda.time.DateTime
import play.api.libs.json.{Format, JodaReads, JsResult, JsValue, Json, OFormat}
import play.api.libs.json.JodaWrites._
import models.upscan.UpscanCallback

import scala.collection.mutable.ListBuffer

case class SchemeData (schemeInfo: SchemeInfo, sheetName: String, numberOfParts: Option[Int], data: ListBuffer[Seq[String]])

object SchemeData {
  implicit val formatSchemeData: OFormat[SchemeData] = Json.format[SchemeData]
}

case class SubmissionsSchemeData(schemeInfo: SchemeInfo,
                                 sheetName: String,
                                 upscanCallback: UpscanCallback,
                                 numberOfRows: Int)

object SubmissionsSchemeData {
  implicit val formatSubmissionsSchemeData: OFormat[SubmissionsSchemeData] = Json.format[SubmissionsSchemeData]
}

case class SchemeInfo (
                       schemeRef:String,
                       timestamp: DateTime = DateTime.now,
                       schemeId: String,
                       taxYear: String,
                       schemeName: String,
                       schemeType: String
                     )

object SchemeInfo {

  implicit val dateFormatDefault: Format[DateTime] = new Format[DateTime] {
    override def reads(json: JsValue): JsResult[DateTime] = JodaReads.DefaultJodaDateTimeReads.reads(json)
    override def writes(o: DateTime): JsValue = JodaDateTimeNumberWrites.writes(o)
  }

  implicit val formatSchemeInfo: OFormat[SchemeInfo] = Json.format[SchemeInfo]
}
