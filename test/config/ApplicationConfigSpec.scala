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

package config

import org.scalatest.matchers.must.Matchers.mustBe
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import scala.concurrent.duration.DurationInt

class ApplicationConfigSpec extends AnyWordSpecLike with GuiceOneAppPerSuite {

  private val appConfig: ApplicationConfig = app.injector.instanceOf[ApplicationConfig]
  private val baseConfig: Configuration    = app.injector.instanceOf[Configuration]

  "ApplicationConfig" when {

    "using the default application config" should {
      "return the correct values" in {
        appConfig.appName                      mustBe "ers-file-validator"
        appConfig.ersTimeOut                   mustBe 70.seconds
        appConfig.maxNumberOfRowsPerSubmission mustBe 10000
        appConfig.splitLargeSchemes            mustBe true
        appConfig.submissionsUrl               mustBe "http://localhost:9292"
        appConfig.validationChunkSize          mustBe 25000
        appConfig.uploadFileSizeLimit          mustBe 104857600
        appConfig.mongoTTLInSeconds            mustBe 3600
        appConfig.useV6andV7Scheme             mustBe false
        appConfig.useV4andV5Scheme             mustBe true
      }

      "fall back to the default values for useV4andV5Scheme and useV6andV7Scheme when values are missing" in {
        val stripped = Configuration(
          baseConfig.underlying
            .withoutPath("microservice.services.features.scheme-version.use-V4-and-V5")
            .withoutPath("microservice.services.features.scheme-version.use-V6-and-V7")
        )
        val config   = new ApplicationConfig(new ServicesConfig(stripped))
        config.useV4andV5Scheme mustBe true
        config.useV6andV7Scheme mustBe false

      }
    }

  }

}
