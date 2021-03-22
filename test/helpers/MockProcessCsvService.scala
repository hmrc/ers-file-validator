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

package helpers

import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpResponse
import akka.stream.scaladsl.Source
import akka.util.ByteString
import config.ApplicationConfig
import connectors.ERSFileValidatorConnector
import models.{SchemeData, SchemeInfo}
import play.api.mvc.Request
import services.{DataGenerator, ProcessCsvService, SheetInfo}
import services.audit.AuditEvents
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.services.validation.DataValidator

import scala.concurrent.{ExecutionContext, Future}

class MockProcessCsvService(auditEvents: AuditEvents,
                            dataGenerator: DataGenerator,
                            appConfig: ApplicationConfig,
                            ersConnector: ERSFileValidatorConnector)(
                             formatDataToValidate: Option[Either[Throwable, Seq[String]]] = None,
                             extractEntityData: Option[Source[ByteString, _]] = None,
                             mockExtractBodyOfRequest: Option[Source[HttpResponse, _] => Source[Either[Throwable, List[ByteString]], _]] = None,
                             getSheetCsv: Option[Either[Throwable, SheetInfo]] = None,
                             processRow: Option[Either[Throwable, Seq[String]]] = None,
                             sendSchemeDataCsv: Option[Future[Option[Throwable]]] = None,
                             sendSchemeCsv: Option[Future[Either[Throwable, Int]]] = None
                           )(
                             implicit ec: ExecutionContext, ac: ActorSystem
                           ) extends ProcessCsvService(auditEvents, dataGenerator, appConfig, ersConnector) {

  override def formatDataToValidate(rowData: Seq[String], sheetName: String, schemeInfo: SchemeInfo)(
    implicit hc: HeaderCarrier, request: Request[_]): Either[Throwable, Seq[String]] = formatDataToValidate match {
    case None => super.formatDataToValidate(rowData, sheetName, schemeInfo)
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

  override def getSheetCsv(sheetName: String, schemeInfo: SchemeInfo)(
    implicit hc: HeaderCarrier, request: Request[_]): Either[Throwable, SheetInfo] = getSheetCsv match {
    case None => super.getSheetCsv(sheetName, schemeInfo)
    case Some(returner) => returner
  }

  override def processRow(rowBytes: List[ByteString], sheetName: String, schemeInfo: SchemeInfo, validator: DataValidator)(
    implicit request: Request[_], hc: HeaderCarrier): Either[Throwable, Seq[String]] = processRow match {
    case None => super.processRow(rowBytes, sheetName, schemeInfo, validator)
    case Some(returner) => returner
  }

  override def sendSchemeDataCsv(ersSchemeData: SchemeData, empRef: String)(
    implicit hc: HeaderCarrier, request: Request[_]): Future[Option[Throwable]] = sendSchemeDataCsv match {
    case None => super.sendSchemeDataCsv(ersSchemeData, empRef)
    case Some(returner) => returner
  }

  override def sendSchemeCsv(schemeData: SchemeData, empRef: String)(
    implicit hc: HeaderCarrier, request: Request[_]): Future[Either[Throwable, Int]] = sendSchemeCsv match {
    case None => super.sendSchemeCsv(schemeData, empRef)
    case Some(returner) => returner
  }
}
