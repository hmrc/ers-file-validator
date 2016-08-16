/*
 * Copyright 2016 HM Revenue & Customs
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

import java.net.URL
import java.util.concurrent.TimeUnit

import config.WSHttpWithCustomTimeOut
import metrics.Metrics
import models.{ERSFileProcessingException, SchemeData}
import play.api.Logger
import play.api.i18n.Messages
import play.api.mvc.Request
import services.audit.AuditEvents
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


trait ERSFileValidatorConnector extends ServicesConfig with Metrics {
  val httpPost : HttpPost = WSHttpWithCustomTimeOut
  val httpGet: HttpGet = WSHttpWithCustomTimeOut
  val httpPut : HttpPut = WSHttpWithCustomTimeOut

  val auditEvents:AuditEvents = AuditEvents

  lazy val serviceURL = baseUrl("ers-file-validator")

  def readAttachmentUri(collection: String, id:String) = {
    val urladdress = s"${baseUrl("attachments")}/attachments-internal/${collection}/${id}"
    new URL(urladdress).openStream()
  }

  def sendToSubmissions(schemeData: SchemeData, empRef: String)(implicit hc: HeaderCarrier,request:Request[_]):Future[HttpResponse] = {
    import java.net.URLEncoder
    val encodedEmpRef = URLEncoder.encode(empRef, "UTF-8")

    val startTime = System.currentTimeMillis()
    val result = httpPost.POST(s"${baseUrl("ers-submissions")}/ers/${encodedEmpRef}/submit-presubmission", schemeData).recover {
      case nf : BadRequestException => {
        deliverSendToSubmissionsMetrics(startTime)
        Logger.error(Messages("ers.exceptions.fileValidatorConnector.badRequest") + nf.getMessage)
        auditEvents.auditRunTimeError(nf,nf.toString, schemeData.schemeInfo, schemeData.sheetName)
        throw new ERSFileProcessingException(Messages("ers.exceptions.fileValidatorConnector.badRequest"),nf.getMessage )
      }
      case nf : NotFoundException => {
        deliverSendToSubmissionsMetrics(startTime)
        Logger.error(Messages("ers.exceptions.fileValidatorConnector.notFound") + nf.getMessage)
        auditEvents.auditRunTimeError(nf,nf.toString, schemeData.schemeInfo, schemeData.sheetName)
        throw new ERSFileProcessingException(Messages("ers.exceptions.fileValidatorConnector.notFound"),nf.getMessage)
      }
      case nf : ServiceUnavailableException => {
        deliverSendToSubmissionsMetrics(startTime)
        Logger.error(Messages("ers.exceptions.fileValidatorConnector.serviceUnavailable") + nf.getMessage)
        auditEvents.auditRunTimeError(nf,nf.toString, schemeData.schemeInfo, schemeData.sheetName)
        throw new ERSFileProcessingException(Messages("ers.exceptions.fileValidatorConnector.serviceUnavailable"),nf.getMessage )
      }
      case e => {
        Logger.error(Messages("ers.exceptions.fileValidatorConnector.failedSendingData") + e.getMessage)
        deliverSendToSubmissionsMetrics(startTime)
        auditEvents.auditRunTimeError(e,e.toString, schemeData.schemeInfo, schemeData.sheetName)
        throw new ERSFileProcessingException(Messages("ers.exceptions.fileValidatorConnector.failedSendingData"),e.getMessage)
      }
    }
    deliverSendToSubmissionsMetrics(startTime)
    result
  }

  def deliverSendToSubmissionsMetrics(startTime:Long) =
    metrics.sendToSubmissionsTimer(System.currentTimeMillis() - startTime, TimeUnit.MILLISECONDS)

  //def source6() = play.api.libs.ws.WS.url("http://localhost:9410/file-stream/ABCDEF123456").getStream().map { response => response._2}
}

object ERSFileValidatorConnector extends ERSFileValidatorConnector
