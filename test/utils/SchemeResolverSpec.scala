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
import models.InvalidTaxYearError
import org.scalatest.EitherValues
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.Mockito.when
import uk.gov.hmrc.validator.SchemeVersion

class SchemeResolverSpec extends AnyWordSpecLike with Matchers with MockitoSugar with EitherValues {

  val mockAppConfig: ApplicationConfig = mock[ApplicationConfig]

  "SchemeResolver.getSchemeVersion" when {

    "csopV5Enabled is false" should {

      "always return V4 regardless of tax year" in {
        when(mockAppConfig.csopV5Enabled).thenReturn(false)
        val result = SchemeResolver.getSchemeVersion("2023/24", mockAppConfig)
        result mustBe Right(SchemeVersion.V4)
      }

      "return V4 even for a tax year that would otherwise require V5" in {
        when(mockAppConfig.csopV5Enabled).thenReturn(false)
        val result = SchemeResolver.getSchemeVersion("2025/26", mockAppConfig)
        result mustBe Right(SchemeVersion.V4)
      }

      "return V4 even for an invalid tax year format" in {
        when(mockAppConfig.csopV5Enabled).thenReturn(false)
        val result = SchemeResolver.getSchemeVersion("invalid", mockAppConfig)
        result mustBe Right(SchemeVersion.V4)
      }
    }

    "csopV5Enabled is true" should {

      "return V5 when tax year start is 2023" in {
        when(mockAppConfig.csopV5Enabled).thenReturn(true)
        val result = SchemeResolver.getSchemeVersion("2023/24", mockAppConfig)
        result mustBe Right(SchemeVersion.V5)
      }

      "return V5 when tax year start is after 2023" in {
        when(mockAppConfig.csopV5Enabled).thenReturn(true)
        val result = SchemeResolver.getSchemeVersion("2024/25", mockAppConfig)
        result mustBe Right(SchemeVersion.V5)
      }

      "return V4 when tax year start is before 2023" in {
        when(mockAppConfig.csopV5Enabled).thenReturn(true)
        val result = SchemeResolver.getSchemeVersion("2022/23", mockAppConfig)
        result mustBe Right(SchemeVersion.V4)
      }

      "return V4 when tax year start is 2014" in {
        when(mockAppConfig.csopV5Enabled).thenReturn(true)
        val result = SchemeResolver.getSchemeVersion("2014/15", mockAppConfig)
        result mustBe Right(SchemeVersion.V4)
      }

      "return InvalidTaxYearError when tax year has no slash separator" in {
        when(mockAppConfig.csopV5Enabled).thenReturn(true)
        val result = SchemeResolver.getSchemeVersion("invalid", mockAppConfig)
        result.isLeft mustBe true
        result.left.value mustBe a[InvalidTaxYearError]
      }

      "return InvalidTaxYearError when tax year start is not a number" in {
        when(mockAppConfig.csopV5Enabled).thenReturn(true)
        val result = SchemeResolver.getSchemeVersion("ABCD/EF", mockAppConfig)
        result.isLeft mustBe true
        val error = result.left.value.asInstanceOf[InvalidTaxYearError]
        error.message mustBe "Invalid tax year format"
        error.context must include("ABCD/EF")
        error.context must include("expected format YYYY/YY")
      }

      "return InvalidTaxYearError for an empty string" in {
        when(mockAppConfig.csopV5Enabled).thenReturn(true)
        val result = SchemeResolver.getSchemeVersion("", mockAppConfig)
        result.isLeft mustBe true
        result.left.value mustBe a[InvalidTaxYearError]
      }
    }
  }
}