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

import config.ApplicationConfig
import models.{ErsError, InvalidTaxYearError}
import uk.gov.hmrc.validator.SchemeVersion

import scala.util.{Failure, Success, Try}

object SchemeResolver {

  def getSchemeVersion(taxYear: String, appConfig: ApplicationConfig): Either[ErsError, SchemeVersion] =
    if (appConfig.csopV5Enabled) {
      Try(taxYear.split("/")(0).toInt >= 2023) match {
        case Success(v5Required) =>
          Right {
            if (v5Required) {
              SchemeVersion.V5
            } else {
              SchemeVersion.V4
            }
          }
        case Failure(_)          =>
          Left(
            InvalidTaxYearError(
              "Invalid tax year format",
              s"Invalid tax year format or conversion error: $taxYear, expected format YYYY/YY"
            )
          )
      }

    } else {
      Right(SchemeVersion.V4)
    }

}
