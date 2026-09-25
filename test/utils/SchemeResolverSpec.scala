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
import models.InvalidTaxYearException
import org.scalatest.EitherValues
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.Mockito.when
import uk.gov.hmrc.validator.SchemeVersion

class SchemeResolverSpec extends AnyWordSpecLike with Matchers with MockitoSugar with EitherValues {

  val mockAppConfig: ApplicationConfig = mock[ApplicationConfig]

  "SchemeResolver.getSchemeVersion" when {

    "useV4andV5Scheme is true" should {

      "always return V4 regardless of tax year for scheme type other than CSOP" in {
        when(mockAppConfig.useV6andV7Scheme).thenReturn(false)
        when(mockAppConfig.useV4andV5Scheme).thenReturn(true)
        val result = SchemeResolver.getSchemeVersion("2023/24", mockAppConfig, "EMI")
        result mustBe Right(SchemeVersion.V4)
      }

      "return V5 for a tax year >= 2023 for CSOP scheme" in {
        when(mockAppConfig.useV6andV7Scheme).thenReturn(false)
        when(mockAppConfig.useV4andV5Scheme).thenReturn(true)
        val result = SchemeResolver.getSchemeVersion("2025/26", mockAppConfig, "CSOP")
        result mustBe Right(SchemeVersion.V5)
      }

    }

    "useV6andV7Scheme is true" should {

      "return V7 when tax year start is >=2023 for CSOP scheme" in {
        when(mockAppConfig.useV6andV7Scheme).thenReturn(true)
        when(mockAppConfig.useV4andV5Scheme).thenReturn(false)
        val result = SchemeResolver.getSchemeVersion("2023/24", mockAppConfig, "CSOP")
        result mustBe Right(SchemeVersion.V7)
      }

      "return V7 regardless of tax year for scheme type other than CSOP" in {
        when(mockAppConfig.useV6andV7Scheme).thenReturn(true)
        when(mockAppConfig.useV4andV5Scheme).thenReturn(false)
        val result = SchemeResolver.getSchemeVersion("2024/25", mockAppConfig, "EMI")
        result mustBe Right(SchemeVersion.V7)
      }

      "return V6 when tax year start is < 2023 for CSOP scheme" in {
        when(mockAppConfig.useV6andV7Scheme).thenReturn(true)
        when(mockAppConfig.useV4andV5Scheme).thenReturn(false)
        val result = SchemeResolver.getSchemeVersion("2020/21", mockAppConfig, "CSOP")
        result mustBe Right(SchemeVersion.V6)
      }
    }
  }

}
