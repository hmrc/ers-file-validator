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
import models.{ErsException, InvalidTaxYearException}
import uk.gov.hmrc.validator.SchemeVersion

import scala.util.{Failure, Success, Try}

object SchemeResolver {

  def getSchemeVersion(
    taxYear: String,
    appConfig: ApplicationConfig,
    schemeType: String
  ): Either[ErsException, SchemeVersion] = {
    val isCsop  = schemeType.equalsIgnoreCase("CSOP")
    val version =
      (
        appConfig.useV4andV5Scheme,
        appConfig.useV6andV7Scheme,
        isCsop,
        taxYear.split("/")(0).toInt >= 2023
      ) match {
        case (true, false, true, true) => SchemeVersion.V5
        case (true, false, _, _)       => SchemeVersion.V4

        case (false, true, true, false) => SchemeVersion.V6
        case (false, true, _, _)        => SchemeVersion.V7

        case _ =>
          SchemeVersion.V4
      }

    Right(version)
  }

}
