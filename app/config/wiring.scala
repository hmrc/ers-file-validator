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

import akka.actor.ActorSystem
import javax.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.libs.ws.{WSClient, WSRequest}
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.cache.client.SessionCache
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient

import scala.concurrent.duration._



@Singleton
class WSHttp @Inject()(auditConnector: AuditConnector, wsClient: WSClient, configuration: Configuration, actorSystem: ActorSystem)
  extends DefaultHttpClient(configuration, auditConnector, wsClient, actorSystem) {


  def buildRequestWithTimeout(uri: String)(implicit hc: HeaderCarrier): WSRequest = {
    val ersTimeOut = configuration.getInt("ers-file-validator-timeout-seconds").getOrElse(20).seconds
    buildRequest(uri).withRequestTimeout(ersTimeOut)
  }
}

@Singleton
class ERSFileValidatorSessionCache @Inject()(val http: DefaultHttpClient,
                                             appConfig: ApplicationConfig)
  extends SessionCache {

//  override def appName : String
//  protected def appNameConfiguration: play.api.Configuration = Play.current.configuration
//  protected def mode: play.api.Mode.Mode = Play.current.mode
//  protected def runModeConfiguration: play.api.Configuration = Play.current.configuration
  lazy val defaultSource: String = "ers-returns-frontend"
  lazy val baseUri: String = appConfig.sessionCacheBaseUri
  lazy val domain: String = appConfig.sessionCacheDomain
}
