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

package connectors

import config.ApplicationConfig
import metrics.Metrics
import models.{ErsFileProcessingException, SchemeData, SchemeInfo, SubmissionsSchemeData}
import play.api.Logging
import services.audit.AuditEvents
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http._
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import utils.ErrorResponseMessages

import java.io.InputStream
import java.net.URL
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ERSFileValidatorConnector @Inject()(appConfig: ApplicationConfig,
                                          http: DefaultHttpClient,
                                          auditEvents: AuditEvents,
                                          implicit val ec: ExecutionContext)
  extends Metrics with Logging {

  // $COVERAGE-OFF$
  def upscanFileStream(downloadUrl: String): InputStream =
    new URL(downloadUrl).openStream()
  // $COVERAGE-ON$

  def sendToSubmissions(schemeData: SchemeData, empRef: String)(implicit hc: HeaderCarrier): Future[Either[Throwable, HttpResponse]] = {
    import java.net.URLEncoder
    val encodedEmpRef = URLEncoder.encode(empRef, "UTF-8")

    val startTime = System.currentTimeMillis()
    http.POST[SchemeData, HttpResponse](s"${appConfig.submissionsUrl}/ers/$encodedEmpRef/submit-presubmission", schemeData).map { response =>
      deliverSendToSubmissionsMetrics(startTime)
      Right(response)
    }.recover {
      case exception => Left(handleException(exception, startTime, schemeData.schemeInfo, schemeData.sheetName))
    }
  }

  def sendToSubmissionsNew(submissionsSchemeData: SubmissionsSchemeData, empRef: String)(
    implicit hc: HeaderCarrier): Future[Either[Throwable, HttpResponse]] = {

    import java.net.URLEncoder
    val encodedEmpRef = URLEncoder.encode(empRef, "UTF-8")

    val startTime = System.currentTimeMillis()

    http.POST[SubmissionsSchemeData, HttpResponse](s"${appConfig.submissionsUrl}/ers/v2/$encodedEmpRef/submit-presubmission", submissionsSchemeData).map { response =>
      deliverSendToSubmissionsMetrics(startTime)
      Right(response)
    }.recover {
      case exception =>
        Left(handleException(exception, startTime, submissionsSchemeData.schemeInfo, submissionsSchemeData.sheetName))
    }
  }

  def handleException(exception: Throwable, startTime: Long, schemeInfo: SchemeInfo, sheetName: String)
                     (implicit hc: HeaderCarrier): ErsFileProcessingException = exception match {
    case nf: BadRequestException =>
      deliverSendToSubmissionsMetrics(startTime)
      logger.error(s"${ErrorResponseMessages.fileValidatorConnectorBadRequest}", nf)
      auditEvents.auditRunTimeError(nf, nf.toString, schemeInfo, sheetName)
      ErsFileProcessingException(s"${ErrorResponseMessages.fileValidatorConnectorBadRequest}", nf.getMessage)
    case nf: NotFoundException =>
      deliverSendToSubmissionsMetrics(startTime)
      logger.error(s"${ErrorResponseMessages.fileValidatorConnectorNotFound}", nf)
      auditEvents.auditRunTimeError(nf, nf.toString, schemeInfo, sheetName)
      ErsFileProcessingException(s"${ErrorResponseMessages.fileValidatorConnectorNotFound}", nf.getMessage)
    case nf: ServiceUnavailableException =>
      deliverSendToSubmissionsMetrics(startTime)
      logger.error(s"${ErrorResponseMessages.fileValidatorConnectorServiceUnavailable}", nf)
      auditEvents.auditRunTimeError(nf, nf.toString, schemeInfo, sheetName)
      ErsFileProcessingException(s"${ErrorResponseMessages.fileValidatorConnectorServiceUnavailable}", nf.getMessage)
    case e =>
      logger.error(s"${ErrorResponseMessages.fileValidatorConnectorFailedSendingData}", e)
      deliverSendToSubmissionsMetrics(startTime)
      auditEvents.auditRunTimeError(e, e.toString, schemeInfo, sheetName)
      ErsFileProcessingException(s"${ErrorResponseMessages.fileValidatorConnectorFailedSendingData}", e.getMessage)
  }

  def deliverSendToSubmissionsMetrics(startTime: Long): Unit =
    metrics.sendToSubmissionsTimer(System.currentTimeMillis() - startTime, TimeUnit.MILLISECONDS)
}
