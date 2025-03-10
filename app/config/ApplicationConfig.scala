/*
 * Copyright 2023 HM Revenue & Customs
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

import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.{DurationInt, FiniteDuration}

@Singleton
class ApplicationConfig @Inject()(config: ServicesConfig) {

  private def loadConfig(key: String) = config.getString(key)

  lazy val appName: String = config.getString("appName")

  lazy val assetsPrefix: String = loadConfig("assets.url") + loadConfig("assets.version")
  lazy val analyticsHost: String = config.getString("google-analytics.host")
  lazy val analyticsToken: String = config.getString("govuk-tax.google-analytics.token")
  lazy val ersTimeOut: FiniteDuration = config.getInt("ers-file-validator-timeout-seconds").seconds
  lazy val maxNumberOfRowsPerSubmission: Int = config.getInt("largefiles.maxrowspersheet")
  lazy val sessionCacheBaseUri: String = config.baseUrl("cachable.session-cache")
  lazy val sessionCacheDomain: String = config.getConfString("cachable.session-cache.domain", throw new Exception("Could not find config ''cachable.session-cache.domain''"))
  lazy val splitLargeSchemes: Boolean = config.getBoolean("largefiles.enabled")
  lazy val submissionsUrl: String = config.baseUrl("ers-submissions")
  lazy val validationChunkSize: Int = config.getInt("validationChunkSize")
  lazy val uploadFileSizeLimit: Int = config.getInt("file-size.uploadSizeLimit")
  lazy val mongoTTLInSeconds: Int = config.getInt("mongodb.timeToLiveInSeconds")
  lazy val csopV5Enabled: Boolean = config.getConfBool("features.csop-v5.enabled", defBool = false)
}
