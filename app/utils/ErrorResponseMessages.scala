/*
 * Copyright 2022 HM Revenue & Customs
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

object ErrorResponseMessages {


  val fileProcessingServiceFailedStream = "Failed to stream the data from file"
  val fileProcessingServiceBulkEntity = "Exception bulk entity streaming"
  val fileValidatorConnectorFailedSendingData = "Failed sending data"
  val fileValidatorConnectorNotFound = "Submissions Service Not Found"
  val fileValidatorConnectorBadRequest = "Submissions Service Bad Request"
  val fileValidatorConnectorServiceUnavailable = "Submissions Service Service Unavailable"
  val dataParserFileRetrievalFailed = "Parser Exception couldn't retrieve row data"
  val dataParserParserFailure = "Parser Failure"
  val dataParserFileParsingError = "Error while Parsing File"
  val dataParserParsingOfFileData = "Parsing of File Data"
  val dataParserIncorrectSheetName = "Incorrect ERS Template - Sheet Name isn't as expected"
  def dataParserIncorrectSchemeType(data: String = "") = s"Incorrect ERS Template - Scheme Type isn't as expected $data"
  def dataParserUnidentifiableSheetName(sheetName: String = "") = s"Couldn't identify SheetName $sheetName"
  val dataParserIncorrectHeader = "Incorrect ERS Template - Header doesn't match"
  val dataParserHeadersDontMatch = "Header doesn't match"
  val dataParserFileInvalid = "File Invalid, formatting errors present"
  val dataParserValidationFailure = "Validation Failure"
  val dataParserConfigFailure = "Failed to find the config file"
  val dataParserNoData = """The file that you chose doesn’t have any data after row 9. The reportable events data must start in cell A10.<br/><a href="https://www.gov.uk/government/collections/employment-related-securities">Use the ERS guidance documents</a> to help you create error-free files."""
  def ersCheckCsvFileNoData(sheetName: String = "") = "The file that you chose doesn’t contain any data.<br/>You won’t be able to upload " +
    s"$sheetName as part of your annual return."


}
