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

package connectors

import java.io.InputStream
import java.net.URL
import java.util.concurrent.TimeUnit

import config.ApplicationConfig
import javax.inject.{Inject, Singleton}
import metrics.Metrics
import models.{ERSFileProcessingException, SchemeData}
import play.api.Logger
import play.api.Play.current
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.mvc.Request
import services.audit.AuditEvents
import uk.gov.hmrc.http._
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ERSFileValidatorConnector @Inject()(appConfig: ApplicationConfig,
                                          http: DefaultHttpClient,
                                          auditEvents: AuditEvents,
                                          implicit val ec: ExecutionContext)
  extends Metrics {

  lazy val serviceURL: String = appConfig.fileValidatorBaseUrl

  def upscanFileStream(downloadUrl: String): InputStream =
    new URL(downloadUrl).openStream()

  def sendToSubmissions(schemeData: SchemeData, empRef: String)(implicit hc: HeaderCarrier, request: Request[_]): Future[HttpResponse] = {
    import java.net.URLEncoder
    val encodedEmpRef = URLEncoder.encode(empRef, "UTF-8")

    val startTime = System.currentTimeMillis()
    val result = http.POST(s"${appConfig.submissionsUrl}/ers/${encodedEmpRef}/submit-presubmission", schemeData).recover {
      case nf: BadRequestException => {
        deliverSendToSubmissionsMetrics(startTime)
        Logger.error(Messages("ers.exceptions.fileValidatorConnector.badRequest") + nf.getMessage)
        auditEvents.auditRunTimeError(nf, nf.toString, schemeData.schemeInfo, schemeData.sheetName)
        throw new ERSFileProcessingException(Messages("ers.exceptions.fileValidatorConnector.badRequest"), nf.getMessage)
      }
      case nf: NotFoundException => {
        deliverSendToSubmissionsMetrics(startTime)
        Logger.error(Messages("ers.exceptions.fileValidatorConnector.notFound") + nf.getMessage)
        auditEvents.auditRunTimeError(nf, nf.toString, schemeData.schemeInfo, schemeData.sheetName)
        throw new ERSFileProcessingException(Messages("ers.exceptions.fileValidatorConnector.notFound"), nf.getMessage)
      }
      case nf: ServiceUnavailableException => {
        deliverSendToSubmissionsMetrics(startTime)
        Logger.error(Messages("ers.exceptions.fileValidatorConnector.serviceUnavailable") + nf.getMessage)
        auditEvents.auditRunTimeError(nf, nf.toString, schemeData.schemeInfo, schemeData.sheetName)
        throw new ERSFileProcessingException(Messages("ers.exceptions.fileValidatorConnector.serviceUnavailable"), nf.getMessage)
      }
      case e => {
        Logger.error(Messages("ers.exceptions.fileValidatorConnector.failedSendingData") + e.getMessage)
        deliverSendToSubmissionsMetrics(startTime)
        auditEvents.auditRunTimeError(e, e.toString, schemeData.schemeInfo, schemeData.sheetName)
        throw new ERSFileProcessingException(Messages("ers.exceptions.fileValidatorConnector.failedSendingData"), e.getMessage)
      }
    }
    deliverSendToSubmissionsMetrics(startTime)
    result
  }

  def deliverSendToSubmissionsMetrics(startTime: Long): Unit =
    metrics.sendToSubmissionsTimer(System.currentTimeMillis() - startTime, TimeUnit.MILLISECONDS)
}
