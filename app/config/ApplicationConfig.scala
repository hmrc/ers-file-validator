/*
 * Copyright 2020 HM Revenue & Customs
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

import javax.inject.{Inject, Singleton}
import play.api.Mode.Mode
import play.api.{Configuration, Environment, Play}
import uk.gov.hmrc.play.config.ServicesConfig

@Singleton
class ApplicationConfig @Inject()(val runModeConfiguration: Configuration, val environment: Environment)
  extends ServicesConfig {
  override protected def mode: Mode = Play.current.mode


  private def loadConfig(key: String) = runModeConfiguration.getString(key).getOrElse(throw new Exception(s"Missing Key: ${key}"))
  private val contactHost = runModeConfiguration.getString("contact-frontend.host").getOrElse("")

  lazy val assetsPrefix: String = loadConfig("assets.url") + loadConfig("assets.version")

  lazy val betaFeedbackUrl = s"$contactHost/contact/beta-feedback"
  lazy val betaFeedbackUnauthenticatedUrl = s"$contactHost/contact/beta-feedback-unauthenticated"
  lazy val analyticsToken: Option[String] = runModeConfiguration.getString("google-analytics.token")
  lazy val analyticsHost: String = runModeConfiguration.getString("google-analytics.host").getOrElse("auto")
  lazy val fileValidatorBaseUrl: String = baseUrl("ers-file-validator")
  lazy val maxNumberOfRowsPerSubmission: Int = runModeConfiguration.getInt("largefiles.maxrowspersheet").getOrElse(10000)
  lazy val submissionsUrl: String = baseUrl("ers-submissions")
  lazy val splitLargeSchemes: Boolean = runModeConfiguration.getBoolean("largefiles.enabled").getOrElse(false)
  lazy val sessionCacheBaseUri: String = baseUrl("cachable.session-cache")
  lazy val sessionCacheDomain: String = getConfString("cachable.session-cache.domain", throw new Exception("Could not find config ''cachable.session-cache.domain''"))

}
