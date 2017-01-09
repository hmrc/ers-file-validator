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

package services

import org.scalatest.{Matchers, WordSpec}
import play.api.test.FakeRequest
import services.audit.{AuditService, AuditServiceConnector}
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.audit.model.DataEvent

class AuditServiceTest   extends WordSpec with Matchers {
  "auditer should send message" in {

    implicit val request = FakeRequest()

    implicit var hc = new HeaderCarrier

    val auditConnectorObj = new AuditServiceConnector  {
      var lastDataEvent : Option[DataEvent]  = None
      override def auditData(dataEvent : DataEvent)(implicit hc : HeaderCarrier) : Unit = {
        lastDataEvent = Some(dataEvent)
      }
    }

    val auditTest = new AuditService {
      override def auditConnector = auditConnectorObj
    }

    auditTest.sendEvent("source",  Map("details1" -> "randomDetail"))

    val dataEvent : DataEvent = auditConnectorObj.lastDataEvent.get
    dataEvent should not equal(Nil)
    dataEvent.auditSource should equal("ers-file-validator")
    dataEvent.auditType should equal("source")

    val tags = dataEvent.tags
    tags("dateTime") should not equal(Nil)

    val details = dataEvent.detail
    details("details1") should equal("randomDetail")
  }
}
