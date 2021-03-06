/*
 * Copyright 2021 HM Revenue & Customs
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
import uk.gov.hmrc.play.audit.http.HttpAuditing
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient



@Singleton
class WSHttp @Inject()(httpAuditing: HttpAuditing, wsClient: WSClient, configuration: Configuration, appConfig: ApplicationConfig, actorSystem: ActorSystem)
  extends DefaultHttpClient(configuration, httpAuditing, wsClient, actorSystem) {


  def buildRequestWithTimeout(uri: String)(implicit hc: HeaderCarrier): WSRequest = {
    buildRequest(uri).withRequestTimeout(appConfig.ersTimeOut)
  }
}

@Singleton
class ERSFileValidatorSessionCache @Inject()(val http: DefaultHttpClient,
                                             appConfig: ApplicationConfig)
  extends SessionCache {

  lazy val defaultSource: String = "ers-returns-frontend"
  lazy val baseUri: String = appConfig.sessionCacheBaseUri
  lazy val domain: String = appConfig.sessionCacheDomain
}
