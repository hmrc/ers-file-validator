/*
 * Copyright 2019 HM Revenue & Customs
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

import akka.actor.ActorSystem
import com.typesafe.config.Config
import play.api.{Configuration, Play}
import play.api.Play.current
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.libs.ws.WSRequest
import play.api.mvc.LegacyI18nSupport
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.cache.client.SessionCache
import uk.gov.hmrc.play.audit.http.HttpAuditing
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.auth.controllers.AuthParamsControllerConfig
import uk.gov.hmrc.play.auth.microservice.connectors.AuthConnector
import uk.gov.hmrc.play.config.{AppName, ServicesConfig}
import uk.gov.hmrc.play.http.ws._
import uk.gov.hmrc.play.microservice.config.LoadAuditingConfig

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._




trait WSHttp extends WSGet with HttpGet with HttpPatch with HttpPut with HttpDelete with HttpPost with WSPut with WSPost with WSDelete with WSPatch with AppName with HttpAuditing {
  override val hooks = Seq(AuditingHook)
  override val auditConnector = ERSFileValidatorAuditConnector
}

object WSHttp extends WSHttp{
  override protected def configuration: Option[Config] = Some(Play.current.configuration.underlying)
  override protected def appNameConfiguration: Configuration = Play.current.configuration
  override protected def actorSystem : ActorSystem = akka.actor.ActorSystem()
}

object WSHttpWithCustomTimeOut extends WSHttp with AppName with HttpAuditing {
   protected def appNameConfiguration: play.api.Configuration = Play.current.configuration
  override protected def actorSystem : ActorSystem = akka.actor.ActorSystem()
    protected def configuration: Option[com.typesafe.config.Config] = Some(Play.current.configuration.underlying)

  override val hooks = Seq(AuditingHook)
  override val auditConnector = ERSFileValidatorAuditConnector

  override def buildRequest[A](url: String)(implicit hc: HeaderCarrier): WSRequest = {
    val ersTimeOut = Play.configuration.getInt("ers-file-validator-timeout-seconds").getOrElse(20).seconds
    super.buildRequest[A](url).withRequestTimeout(ersTimeOut)
  }
}

object MicroserviceAuthConnector extends AuthConnector with ServicesConfig with WSHttp{

  protected def appNameConfiguration: play.api.Configuration = Play.current.configuration
  override protected def actorSystem : ActorSystem = akka.actor.ActorSystem()
  protected def configuration: Option[com.typesafe.config.Config] = Some(Play.current.configuration.underlying)
  protected def mode: play.api.Mode.Mode = Play.current.mode
  protected def runModeConfiguration: play.api.Configuration = Play.current.configuration
  override val authBaseUrl: String = baseUrl("auth")
}

object AuthParamsControllerConfiguration extends AuthParamsControllerConfig {
  lazy val controllerConfigs: Config = ControllerConfiguration.controllerConfigs
}
object ERSFileValidatorAuditConnector extends AuditConnector with AppName {
  override lazy val auditingConfig = LoadAuditingConfig("auditing")
  override def appName : String = AppName (Play.current.configuration).appName
  protected def appNameConfiguration: play.api.Configuration = Play.current.configuration
}

object ERSFileValidatorSessionCache extends SessionCache with AppName with ServicesConfig with LegacyI18nSupport {
  override def appName : String = AppName (Play.current.configuration).appName
  protected def appNameConfiguration: play.api.Configuration = Play.current.configuration
  protected def mode: play.api.Mode.Mode = Play.current.mode
  protected def runModeConfiguration: play.api.Configuration = Play.current.configuration
  override lazy val http: WSHttp.type = WSHttp
  override lazy val defaultSource: String = "ers-returns-frontend"
  override lazy val baseUri: String = baseUrl("cachable.session-cache")
  override lazy val domain: String = getConfString("cachable.session-cache.domain", throw new Exception(Messages("ers.exceptions.wiring.noConfig")))
}
