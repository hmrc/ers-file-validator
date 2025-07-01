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

package helpers

import config.ApplicationConfig
import connectors.ERSFileValidatorConnector
import models.{SchemeInfo, SubmissionsSchemeData, UserValidationError}
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.http.scaladsl.model.HttpResponse
import org.apache.pekko.stream.scaladsl.Source
import org.apache.pekko.util.ByteString
import play.api.mvc.Request
import services.audit.AuditEvents
import services.{DataGenerator, ProcessCsvService, SheetInfo}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.services.validation.DataValidator

import scala.concurrent.{ExecutionContext, Future}

class MockProcessCsvService(auditEvents: AuditEvents,
                            dataGenerator: DataGenerator,
                            appConfig: ApplicationConfig,
                            ersConnector: ERSFileValidatorConnector)(
                             formatDataToValidate: Option[Seq[String]] = None,
                             extractEntityData: Option[Source[ByteString, _]] = None,
                             mockExtractBodyOfRequest: Option[Source[HttpResponse, _] => Source[Either[Throwable, List[ByteString]], _]] = None,
                             processRow: Option[Either[UserValidationError, Seq[String]]] = None,
                             sendSchemeDataCsv: Option[Future[Option[Throwable]]] = None,
                             sendSchemeCsv: Option[Future[Either[Throwable, Int]]] = None,
                             sendSchemeCsvNew: Option[Future[Option[Throwable]]] = None
                           )(
                             implicit ec: ExecutionContext, ac: ActorSystem
                           ) extends ProcessCsvService(auditEvents, dataGenerator, appConfig, ersConnector) {

  override def formatDataToValidate(rowData: Seq[String], sheetInfo: SheetInfo): Seq[String] = formatDataToValidate match {
    case None => super.formatDataToValidate(rowData, sheetInfo)
    case Some(returner) => returner
  }

  override def extractEntityData(response: HttpResponse): Source[ByteString, _] = extractEntityData match {
    case None => super.extractEntityData(response)
    case Some(returner) => returner
  }

  override def extractBodyOfRequest: Source[HttpResponse, _] => Source[Either[Throwable, List[ByteString]], _] = mockExtractBodyOfRequest match {
    case None => super.extractBodyOfRequest
    case Some(returner) => returner
  }

  override def processRow(rowBytes: List[ByteString], sheetName: String, schemeInfo: SchemeInfo, validator: DataValidator, sheetInfo: SheetInfo)(
    implicit request: Request[_], hc: HeaderCarrier): Either[UserValidationError, Seq[String]] = processRow match {
    case None => super.processRow(rowBytes, sheetName, schemeInfo, validator, sheetInfo)
    case Some(returner) => returner
  }

  override def sendSchemeCsv(schemeData: SubmissionsSchemeData, empRef: String)(
    implicit hc: HeaderCarrier, request: Request[_]): Future[Option[Throwable]] = sendSchemeCsvNew match {
    case None => super.sendSchemeCsv(schemeData, empRef)
    case Some(returner) => returner
  }
}
