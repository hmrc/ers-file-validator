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

package services.audit

import org.mockito.ArgumentMatchers.{any, eq => argEq}
import org.mockito.Mockito._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.DefaultAuditConnector
import uk.gov.hmrc.play.audit.http.connector.AuditResult.Success
import uk.gov.hmrc.play.audit.model.DataEvent

import java.time.ZonedDateTime
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}

class AuditServiceSpec extends AnyWordSpecLike with MockitoSugar with Matchers {
  implicit val ec: ExecutionContextExecutor = ExecutionContext.global

  "auditService sendEvent should send the event" in {

    implicit val hc: HeaderCarrier = new HeaderCarrier

    val dateTime           = ZonedDateTime.now()
    val mockAuditConnector = mock[DefaultAuditConnector]

    val auditService = new AuditService(mockAuditConnector, ec) {
      override protected def getDateTime: ZonedDateTime = dateTime
    }

    val details: Map[String, String] = Map("details1" -> "randomDetail")

    val dataEvent = DataEvent(
      auditSource = "ers-file-validator",
      auditType = "source",
      tags = hc.otherHeaders.toMap ++ Map("dateTime" -> dateTime.toString),
      detail = details
    )

    when(mockAuditConnector.sendEvent(argEq(dataEvent))(any[HeaderCarrier](), any[ExecutionContext]()))
      .thenReturn(Future.successful(Success))
    auditService.sendEvent("source", details)
  }

  "getDateTime should return a ZonedDateTime representing the current time" in {
    val mockAuditConnector = mock[DefaultAuditConnector]

    val auditService = new AuditService(mockAuditConnector, ec) {
      def exposedGetDateTime: ZonedDateTime = getDateTime
    }

    val tolerance               = 10L // milliseconds
    val expected: ZonedDateTime = ZonedDateTime.now()
    val result                  = auditService.exposedGetDateTime

    assert(Math.abs(result.toInstant.toEpochMilli - expected.toInstant.toEpochMilli) < tolerance)
  }

}
