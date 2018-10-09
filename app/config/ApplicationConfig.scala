/*
 * Copyright 2018 HM Revenue & Customs
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

import play.api.Play._
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.mvc.LegacyI18nSupport
import uk.gov.hmrc.play.config.ServicesConfig

trait ApplicationConfig {
  val assetsPrefix: String
  val betaFeedbackUrl: String
  val betaFeedbackUnauthenticatedUrl: String
  val analyticsToken: Option[String]
  val analyticsHost: String

  val submissionsUrl: String

  val splitLargeSchemes: Boolean
  val maxNumberOfRowsPerSubmission: Int
}

object ApplicationConfig extends ApplicationConfig with ServicesConfig with LegacyI18nSupport {

  private def loadConfig(key: String) = configuration.getString(key).getOrElse(throw new Exception(Messages("ers.exceptions.applicationConfig.missingKey", key)))

  private val contactHost = configuration.getString("contact-frontend.host").getOrElse("")

  override lazy val assetsPrefix: String = loadConfig("assets.url") + loadConfig("assets.version")
  override lazy val betaFeedbackUrl = s"$contactHost/contact/beta-feedback"
  override lazy val betaFeedbackUnauthenticatedUrl = s"$contactHost/contact/beta-feedback-unauthenticated"
  override lazy val analyticsToken: Option[String] = configuration.getString("google-analytics.token")
  override lazy val analyticsHost: String = configuration.getString("google-analytics.host").getOrElse("auto")

  override lazy val submissionsUrl: String = baseUrl("ers-submissions") + "/ers-submissions/" + "submit-presubmission"

  override lazy val splitLargeSchemes: Boolean = configuration.getBoolean("largefiles.enabled").getOrElse(false)
  override lazy val maxNumberOfRowsPerSubmission: Int = configuration.getInt("largefiles.maxrowspersheet").getOrElse(10000)

}
