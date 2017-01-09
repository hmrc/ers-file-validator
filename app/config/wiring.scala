/*
 * Copyright 2017 HM Revenue & Customs
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

import com.typesafe.config.Config
import play.api.Play
import play.api.Play.current
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.libs.ws.WSRequest
import play.api.mvc.LegacyI18nSupport
import uk.gov.hmrc.http.cache.client.SessionCache
import uk.gov.hmrc.play.audit.http.HttpAuditing
import uk.gov.hmrc.play.audit.http.config.LoadAuditingConfig
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.auth.controllers.AuthParamsControllerConfig
import uk.gov.hmrc.play.auth.microservice.connectors.AuthConnector
import uk.gov.hmrc.play.config.{AppName, RunMode, ServicesConfig}
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.http.ws._

import scala.concurrent.duration._

object ERSFileValidatorAuditConnector extends AuditConnector with AppName with RunMode {
  override lazy val auditingConfig = LoadAuditingConfig(s"$env.auditing")
}

object WSHttp extends WSGet with WSPut with WSPost with WSDelete with WSPatch with AppName with RunMode with HttpAuditing {
  override val hooks = Seq(AuditingHook)
  override val auditConnector = ERSFileValidatorAuditConnector
}

object WSHttpWithCustomTimeOut extends WSHttp with AppName with RunMode with HttpAuditing {
  override val hooks = Seq(AuditingHook)
  override val auditConnector = ERSFileValidatorAuditConnector
  private val twentySeconds: Int = 20

  override def buildRequest[A](url: String)(implicit hc: HeaderCarrier): WSRequest = {
    val ersTimeOut: Int = Play.configuration.getInt("ers-file-validator-timeout-seconds").getOrElse(twentySeconds)
    super.buildRequest[A](url).withRequestTimeout(Duration(ersTimeOut, SECONDS))
  }
}

object MicroserviceAuthConnector extends AuthConnector with ServicesConfig {
  override val authBaseUrl: String = baseUrl("auth")
}

object AuthParamsControllerConfiguration extends AuthParamsControllerConfig {
  lazy val controllerConfigs: Config = ControllerConfiguration.controllerConfigs
}

object ERSFileValidatorSessionCache extends SessionCache with AppName with ServicesConfig with LegacyI18nSupport {
  override lazy val http: WSHttp.type = WSHttp
  override lazy val defaultSource: String = "ers-returns-frontend"
  override lazy val baseUri: String = baseUrl("cachable.session-cache")
  override lazy val domain: String = getConfString("cachable.session-cache.domain", throw new Exception(Messages("ers.exceptions.wiring.noConfig")))
}
