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

package services.audit

import hmrc.gsi.gov.uk.services.validation.ValidationError
import models.{CallbackData, SchemeInfo}
import org.apache.commons.lang3.exception.ExceptionUtils
import uk.gov.hmrc.play.http.HeaderCarrier
import play.api.mvc.Request

object AuditEvents extends AuditEvents {
  override def auditService : AuditService = AuditService
}

trait AuditEvents {

  def auditService: AuditService

  def eventMap(schemeInfo: SchemeInfo, sheetName : String): Map[String, String] ={
    Map(
      "schemeRef" -> schemeInfo.schemeRef,
      "schemeType" -> schemeInfo.schemeType,
      "schemeName" -> schemeInfo.schemeName,
      "schemeId" -> schemeInfo.schemeId,
      "taxYear" -> schemeInfo.taxYear,
      "sheetName" -> sheetName,
      "timestamp" -> schemeInfo.timestamp.toString)
  }

  def auditRunTimeError(exception : Throwable, contextInfo : String, schemeInfo: SchemeInfo, sheetName : String) (implicit hc: HeaderCarrier,request: Request[_]) : Unit = {
    auditService.sendEvent("ERSRunTimeError",Map(
      "ErrorMessage" -> exception.getMessage,
      "Context" -> contextInfo,
      "schemeData" -> eventMap(schemeInfo, sheetName).toString,
      "StackTrace" -> ExceptionUtils.getStackTrace(exception)
    ))
  }

  def fileValidatorAudit(schemeInfo: SchemeInfo, sheetName : String)(implicit hc: HeaderCarrier,request: Request[_]): Boolean = {
    auditService.sendEvent("ERSFileValidatorAudit", eventMap(schemeInfo, sheetName))
    true
  }

  def fileProcessingErrorAudit(schemeInfo: SchemeInfo, sheetName : String,errorMsg:String)(implicit hc: HeaderCarrier, request: Request[_]): Boolean = {
    auditService.sendEvent("ERSFileProcessingError", eventMap(schemeInfo, sheetName) ++ Map("ErrorMessage" -> errorMsg))
    true
  }

  def validationErrorAudit(validationErrors:List[ValidationError],schemeInfo: SchemeInfo, sheetName : String)(implicit hc: HeaderCarrier,sc:SchemeInfo, request: Request[_]) = {
    auditService.sendEvent("ERSValidationError",Map(
        "Column" -> validationErrors.head.cell.column,
        "Row" -> validationErrors.head.cell.row.toString,
        "Value" -> validationErrors.head.cell.value,
        "ErrorMessage" -> validationErrors.head.errorMsg
      ) ++ eventMap(schemeInfo, sheetName))
    true
  }

  def totalRows(totalRows : Int,schemeInfo: SchemeInfo)(implicit hc: HeaderCarrier, request: Request[_]): Boolean = {

    auditService.sendEvent("ERStotalRowCount", Map(
      "rows" -> totalRows.toString,
      "schemeId"->schemeInfo.schemeId,
      "schemeName"->schemeInfo.schemeName,
      "schemeRef"->schemeInfo.schemeRef,
      "schemeType"->schemeInfo.schemeType,
      "taxYear"->schemeInfo.taxYear,
      "timestamp"->schemeInfo.timestamp.toString))
    true
  }

  def callbackResult(callbackData: CallbackData) (implicit hc: HeaderCarrier, request: Request[_]): Boolean = {
    val eventMap = Map("callbackData" -> callbackData.toString)

    auditService.sendEvent("ERSCallbackResult", eventMap)
    true
  }
}
