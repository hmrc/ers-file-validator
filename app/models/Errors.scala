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

sealed abstract class ErsException(message: String) extends Exception(message) {
  def message: String
  def context: String
}

sealed abstract class UserValidationException(message: String) extends ErsException(message)
case class HeaderValidationException(message: String, context: String) extends UserValidationException(message)

case class FileValidationException(message: String, context: String) extends UserValidationException(message)

case class SchemeTypeMismatchException(
  message: String,
  context: String,
  expectedSchemeType: String,
  requestSchemeType: String
) extends UserValidationException(message)

// to differentiate from lib exception
case class FileValidatorNoDataException(message: String, context: String) extends UserValidationException(message)

case class UnknownSheetException(message: String, context: String) extends UserValidationException(message)
case class InvalidTaxYearException(message: String, context: String) extends UserValidationException(message)

sealed abstract class SystemError(message: String) extends ErsException(message)
case class ErsSystemError(message: String, context: String) extends SystemError(message)
case class ErsFileProcessingException(message: String, context: String) extends SystemError(message)
