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

package models.upscan

import models.SchemeInfo
import play.api.libs.json.{JsObject, JsString, JsValue, Json, OFormat, Reads, Writes}

case class UpscanCallback(name: String,
                          downloadUrl: String,
                          length: Option[Long] = None,
                          contentType: Option[String] = None,
                          customMetadata: Option[JsObject] = None,
                          noOfRows: Option[Int] = None
                         )

object UpscanCallback {
  implicit val upscanCallbackWrites: Writes[UpscanCallback] = Json.writes[UpscanCallback].transform(
    (js: JsValue) => js.as[JsObject] + ("_type" -> JsString("UploadedSuccessfully"))
  )

  implicit val upscanCallbackReads: Reads[UpscanCallback] = Json.reads[UpscanCallback]
}

case class UpscanFileData(callbackData: UpscanCallback, schemeInfo: SchemeInfo)
object UpscanFileData {
  implicit val formatUpscanFileData: OFormat[UpscanFileData] = Json.format[UpscanFileData]
}

case class UpscanCsvFilesList(files: List[UpscanCallback])
object UpscanCsvFilesList {
  implicit val formatUpscanCsvFilesList: OFormat[UpscanCsvFilesList] = Json.format[UpscanCsvFilesList]
}

case class UpscanCsvFileData(callbackData: List[UpscanCallback], schemeInfo: SchemeInfo)
object UpscanCsvFileData {
  implicit val formatUpscanCsvFileData: OFormat[UpscanCsvFileData] = Json.format[UpscanCsvFileData]
}

