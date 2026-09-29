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
import org.mockito.Mockito.when
import org.scalatest.EitherValues
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.validator.SchemeVersion

class SchemeResolverSpec extends AnyWordSpecLike with Matchers with MockitoSugar with EitherValues {

  val mockAppConfig: ApplicationConfig = mock[ApplicationConfig]

  "SchemeResolver.getSchemeVersion" when {

    "useV4andV5Scheme is true and useV6andV7Scheme set to false" should {

      "return V4 when tax year is before 2023" in {
        when(mockAppConfig.useV6andV7Scheme).thenReturn(false)
        when(mockAppConfig.useV4andV5Scheme).thenReturn(true)

        val result = SchemeResolver.getSchemeVersion("2014/15", mockAppConfig)
        result mustBe Right(SchemeVersion.V4)
      }

      "return V5 for a tax year >= 2023" in {
        when(mockAppConfig.useV6andV7Scheme).thenReturn(false)
        when(mockAppConfig.useV4andV5Scheme).thenReturn(true)

        val result = SchemeResolver.getSchemeVersion("2025/26", mockAppConfig)
        result mustBe Right(SchemeVersion.V5)
      }

    }

    "useV6andV7Scheme is true and useV4andV5Scheme set to false" should {

      "return V7 for a tax year >=2023" in {
        when(mockAppConfig.useV6andV7Scheme).thenReturn(true)
        when(mockAppConfig.useV4andV5Scheme).thenReturn(false)

        val result = SchemeResolver.getSchemeVersion("2023/24", mockAppConfig)
        result mustBe Right(SchemeVersion.V7)
      }

      "return V6 for a tax year before 2023" in {
        when(mockAppConfig.useV6andV7Scheme).thenReturn(true)
        when(mockAppConfig.useV4andV5Scheme).thenReturn(false)

        val result = SchemeResolver.getSchemeVersion("2020/21", mockAppConfig)
        result mustBe Right(SchemeVersion.V6)
      }
    }

    "return InvalidTaxYearException when tax year has no slash separator" in {
      val result = SchemeResolver.getSchemeVersion("invalid", mockAppConfig)
      result.isLeft     mustBe true
      result.left.value mustBe a[InvalidTaxYearException]
    }

    "return InvalidTaxYearException when tax year start is not a number" in {
      val result = SchemeResolver.getSchemeVersion("ABCD/EF", mockAppConfig)
      result.isLeft mustBe true
      val error = result.left.value.asInstanceOf[InvalidTaxYearException]
      error.message mustBe "Invalid tax year format"
      error.context   must include("ABCD/EF")
      error.context   must include("expected format YYYY/YY")
    }

    "return InvalidTaxYearException for an empty string" in {
      val result = SchemeResolver.getSchemeVersion("", mockAppConfig)
      result.isLeft     mustBe true
      result.left.value mustBe a[InvalidTaxYearException]
    }

  }

}
