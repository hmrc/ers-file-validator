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

import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.DefaultAuditConnector
import uk.gov.hmrc.play.audit.model.DataEvent

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class AuditService @Inject() (auditConnector: DefaultAuditConnector, implicit val ec: ExecutionContext) {
  val auditSource = "ers-file-validator"

  def sendEvent(transactionName: String, details: Map[String, String])(implicit hc: HeaderCarrier): Unit =
    auditConnector.sendEvent(buildEvent(transactionName, details))

  private def buildEvent(transactionName: String, details: Map[String, String])(implicit hc: HeaderCarrier) =
    DataEvent(
      auditSource = auditSource,
      auditType = transactionName,
      tags = generateTags(hc),
      detail = details
    )

  private def generateTags(hc: HeaderCarrier): Map[String, String] =
    hc.otherHeaders.toMap ++
      Map(
        "dateTime" -> getDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"))
      )

  protected def getDateTime: ZonedDateTime = ZonedDateTime.now()
}
