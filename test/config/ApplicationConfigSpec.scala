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

import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder

import scala.concurrent.duration.DurationInt

class ApplicationConfigSpec extends AnyWordSpecLike with GuiceOneAppPerSuite {

  private val appConfig: ApplicationConfig = app.injector.instanceOf[ApplicationConfig]

  private def buildAppWithOverrides(configOverrides: (String, Any)*): Application =
    new GuiceApplicationBuilder()
      .configure(configOverrides: _*)
      .build()

  "ApplicationConfig" when {

    "using the default application config" should {
      "return the correct values" in {
        appConfig.appName                      mustBe "ers-file-validator"
        appConfig.assetsPrefix                 mustBe "http://localhost:9032/assets/2.54.0"
        appConfig.analyticsHost                mustBe "auto"
        appConfig.ersTimeOut                   mustBe 70.seconds
        appConfig.maxNumberOfRowsPerSubmission mustBe 10000
        appConfig.sessionCacheBaseUri          mustBe "http://localhost:8400"
        appConfig.sessionCacheDomain           mustBe "keystore"
        appConfig.splitLargeSchemes            mustBe true
        appConfig.submissionsUrl               mustBe "http://localhost:9292"
        appConfig.validationChunkSize          mustBe 25000
        appConfig.uploadFileSizeLimit          mustBe 104857600
        appConfig.mongoTTLInSeconds            mustBe 3600
        appConfig.csopV5Enabled                mustBe true
      }
    }

    "csop v5 flag is false" should {
      "return false" in {
        val overriddenApp = buildAppWithOverrides(
          "microservice.services.features.csop-v5.enabled" -> false
        )

        val overriddenConfig = overriddenApp.injector.instanceOf[ApplicationConfig]

        overriddenConfig.csopV5Enabled mustBe false
      }
    }
  }

}
