/*
 * Copyright 2016 HM Revenue & Customs
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

import java.io.File

import com.typesafe.config.Config

import net.ceedubs.ficus.Ficus._
import play.api.Mode._
import play.api.{Application, Configuration, Play}
import uk.gov.hmrc.play.audit.filters.{AuditFilter, FrontendAuditFilter}
import uk.gov.hmrc.play.audit.http.config.LoadAuditingConfig
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.auth.microservice.filters.AuthorisationFilter
import uk.gov.hmrc.play.config.{AppName, ControllerConfig, RunMode}
import uk.gov.hmrc.play.http.logging.filters.{LoggingFilter, FrontendLoggingFilter}
import uk.gov.hmrc.play.microservice.bootstrap.DefaultMicroserviceGlobal

object ApplicationGlobal extends DefaultMicroserviceGlobal with RunMode {

  override val auditConnector = MicroserviceAuditConnector

  override val loggingFilter = MicroserviceLoggingFilter

  override val microserviceAuditFilter = MicroserviceAuditFilter

  override val authFilter = Some(MicroserviceAuthFilter)

  override def onStart(app: Application) {
    super.onStart(app)
   // ApplicationCrypto.verifyConfiguration()
  }

  override def onLoadConfig(config: Configuration, path: File, classLoader: ClassLoader, mode: Mode): Configuration = {
    super.onLoadConfig(config, path, classLoader, mode)
  }

  override def microserviceMetricsConfig(implicit app: Application): Option[Configuration] = app.configuration.getConfig(s"$env.microservice.metrics")
}

object MicroserviceAuthFilter extends AuthorisationFilter {
  override lazy val authParamsConfig = AuthParamsControllerConfiguration
  override lazy val authConnector = MicroserviceAuthConnector
  override def controllerNeedsAuth(controllerName: String): Boolean = ControllerConfiguration.paramsForController(controllerName).needsAuth
}

object MicroserviceAuditConnector extends AuditConnector with RunMode {
  override lazy val auditingConfig = LoadAuditingConfig(s"$env.auditing")
}
object MicroserviceAuditFilter extends AuditFilter with AppName {
  override val auditConnector = MicroserviceAuditConnector
  override def controllerNeedsAuditing(controllerName: String) = ControllerConfiguration.paramsForController(controllerName).needsAuditing
}

object MicroserviceLoggingFilter extends LoggingFilter {
  override def controllerNeedsLogging(controllerName: String) = ControllerConfiguration.paramsForController(controllerName).needsLogging
}

object ControllerConfiguration extends ControllerConfig {
  lazy val controllerConfigs = Play.current.configuration.underlying.as[Config]("controllers")
}

object ERSFileValidatorLoggingFilter extends FrontendLoggingFilter {
  override def controllerNeedsLogging(controllerName: String): Boolean = ControllerConfiguration.paramsForController(controllerName).needsLogging
}

object ERSFileValidatorAuditFilter extends FrontendAuditFilter with RunMode with AppName {

  override lazy val maskedFormFields = Seq.empty[String]

  override lazy val applicationPort = None

  override lazy val auditConnector = ERSFileValidatorAuditConnector

  override def controllerNeedsAuditing(controllerName: String): Boolean = ControllerConfiguration.paramsForController(controllerName).needsAuditing
}
