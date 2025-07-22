/*
 * Copyright 2025 HM Revenue & Customs
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



sealed abstract class ErsError(message: String) extends Exception(message) {
  def message: String
  def context: String
}

sealed abstract class UserValidationError(message: String) extends ErsError(message)

case class HeaderValidationError(message: String, context: String) extends UserValidationError(message)

case class RowValidationError(
                               message: String,
                              context: String,
                              rowNumber: Int
                             ) extends UserValidationError(message)

case class SchemeTypeMismatchError(
                                    message: String,
                                    context: String,
                                    expectedSchemeType: String,
                                    requestSchemeType: String
                                  ) extends UserValidationError(message)

case class NoDataError(message: String, context: String) extends UserValidationError(message)

case class UnknownSheetError(message: String, context: String) extends UserValidationError(message)


sealed abstract class SystemError(message: String) extends ErsError(message)

case class ErsSystemError(message: String, context: String) extends SystemError(message)

case class ERSFileProcessingException(
                                       message: String,
                                       context: String,
                                       jsonSize: Option[Int] = None
                                     ) extends SystemError(message)
