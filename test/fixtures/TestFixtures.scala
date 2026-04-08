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

package fixtures

import config.ApplicationConfig
import connectors.ERSFileValidatorConnector
import models.SchemeInfo
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.mvc.Request
import services.SessionCacheService
import services.audit.AuditEvents
import uk.gov.hmrc.http.HeaderCarrier

import java.time.ZonedDateTime
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

trait TestFixtures {

  val schemeInfo: SchemeInfo = SchemeInfo(
    schemeRef = "XA11000001231275",
    timestamp = ZonedDateTime.now,
    schemeId = "123PA12345678",
    taxYear = "2014/F15",
    schemeName = "MyScheme",
    schemeType = "EMI"
  )

  implicit val request: Request[_]          = mock[Request[_]]
  implicit val headerCarrier: HeaderCarrier = mock[HeaderCarrier]
  val mockAuditEvents: AuditEvents          = mock[AuditEvents]

  implicit val ec: ExecutionContextExecutor = ExecutionContext.global

  val empRef: String = "1234/ABCD"

  val mockAppConfig: ApplicationConfig = mock[ApplicationConfig]

  val mockSessionService: SessionCacheService = mock[SessionCacheService]

  val mockErsFileValidatorConnector: ERSFileValidatorConnector = mock[ERSFileValidatorConnector]

}
