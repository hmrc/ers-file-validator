/*
 * Copyright 2023 HM Revenue & Customs
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

import play.api.mvc.{Request, Session}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.model.DataEvent

import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.play.audit.DefaultAuditConnector

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import scala.concurrent.ExecutionContext

@Singleton
class AuditService @Inject()(auditConnector: DefaultAuditConnector,
                             implicit val ec: ExecutionContext) {
  val auditSource = "ers-file-validator"

  def sendEvent(transactionName : String, details: Map[String, String])(implicit request: Request[_], hc: HeaderCarrier): Unit =
    auditConnector.sendEvent(buildEvent(transactionName, details))

  private def buildEvent( transactionName: String,  details: Map[String, String])(implicit request: Request[_], hc: HeaderCarrier) =
    DataEvent(
      auditSource = auditSource,
      auditType = transactionName,
      tags = generateTags(request.session, hc),
      detail = details
    )

  private def generateTags(session: Session, hc: HeaderCarrier): Map[String, String] =
    hc.otherHeaders.toMap ++
      hc.otherHeaders.toMap ++
      Map(
        "dateTime" ->  getDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ"))
      )

  protected def getDateTime: ZonedDateTime = ZonedDateTime.now()
}
