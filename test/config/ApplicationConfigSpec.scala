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
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import scala.concurrent.duration.DurationInt

class ApplicationConfigSpec extends AnyWordSpecLike {

  private class FakeServicesConfig extends ServicesConfig(null) {

    override def getString(key: String): String = key match {
      case "appName"                          => "ers-frontend"
      case "assets.url"                       => "http://assets/"
      case "assets.version"                   => "v1"
      case "google-analytics.host"            => "http://analytics"
      case "govuk-tax.google-analytics.token" => "token-123"
    }

    override def getInt(key: String): Int = key match {
      case "ers-file-validator-timeout-seconds" => 30
      case "largefiles.maxrowspersheet"         => 1000
      case "validationChunkSize"                => 250
      case "file-size.uploadSizeLimit"          => 5000
      case "mongodb.timeToLiveInSeconds"        => 3600
    }

    override def getBoolean(key: String): Boolean = key match {
      case "largefiles.enabled" => true
    }

    override def baseUrl(serviceName: String): String = serviceName match {
      case "cachable.session-cache" => "http://session-cache"
      case "ers-submissions"        => "http://ers-submissions"
    }

    override def getConfString(key: String, default: => String): String = key match {
      case "cachable.session-cache.domain" => "session-cache-domain"
      case _                               => default
    }

    override def getConfBool(key: String, defBool: => Boolean): Boolean = key match {
      case "features.csop-v5.enabled" => true
      case _                          => defBool
    }

  }

  private class FakeServicesConfigCsopFalse extends FakeServicesConfig {

    override def getConfBool(key: String, defBool: => Boolean): Boolean = key match {
      case "features.csop-v5.enabled" => false
      case _                          => defBool
    }

  }

  "ApplicationConfig" when {

    "all config values are present" should {
      "return the correct values" in {
        val appConfig = new ApplicationConfig(new FakeServicesConfig)

        appConfig.appName                      mustBe "ers-frontend"
        appConfig.assetsPrefix                 mustBe "http://assets/v1"
        appConfig.analyticsHost                mustBe "http://analytics"
        appConfig.analyticsToken               mustBe "token-123"
        appConfig.ersTimeOut                   mustBe 30.seconds
        appConfig.maxNumberOfRowsPerSubmission mustBe 1000
        appConfig.sessionCacheBaseUri          mustBe "http://session-cache"
        appConfig.sessionCacheDomain           mustBe "session-cache-domain"
        appConfig.splitLargeSchemes            mustBe true
        appConfig.submissionsUrl               mustBe "http://ers-submissions"
        appConfig.validationChunkSize          mustBe 250
        appConfig.uploadFileSizeLimit          mustBe 5000
        appConfig.mongoTTLInSeconds            mustBe 3600
        appConfig.csopV5Enabled                mustBe true
      }
    }

    "csop v5 flag is false" should {
      "return false" in {
        val appConfig = new ApplicationConfig(new FakeServicesConfigCsopFalse)

        appConfig.csopV5Enabled mustBe false
      }
    }
  }

}
