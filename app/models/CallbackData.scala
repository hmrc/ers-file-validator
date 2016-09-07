/*
 * Copyright 2016 HM Revenue & Customs
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
import play.api.libs.json.{JsObject, Json}

case class FileInfo(errorMessage: String, fileName: String, fileID: String, fileType: String)

case class ERSFileProcessingException(message: String,
                                context: String,
                                jsonSize: Option[Int] = None) extends Exception(message)

case class CallbackData(collection: String, id: String, length: Long, name: Option[String], contentType: Option[String], customMetadata: Option[JsObject], noOfRows:Option[Int])

object CallbackData {
  implicit val format = Json.format[CallbackData]
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
  implicit val format = Json.format[SchemeInfo]
}

case class FileData(callbackData: CallbackData, schemeInfo: SchemeInfo)
object FileData{
  implicit val format = Json.format[FileData]
}

case class CsvFileData(callbackData: List[CallbackData], schemeInfo: SchemeInfo)
object CsvFileData{
  implicit val format = Json.format[CsvFileData]
}
