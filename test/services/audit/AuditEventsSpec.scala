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

package services

import uk.gov.hmrc.services.validation.models._
import models.{SchemeData, SchemeInfo}
import org.apache.commons.lang3.exception.ExceptionUtils
import org.joda.time.DateTime
import org.mockito.ArgumentMatchers.{any, eq => argEq}
import org.mockito.Mockito.{times, verify}
import org.scalatest.{Matchers, WordSpec}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.FakeRequest
import services.audit.{AuditEvents, AuditService}
import uk.gov.hmrc.http.HeaderCarrier

import scala.collection.mutable.ListBuffer

class AuditEventsSpec extends WordSpec with Matchers with MockitoSugar with GuiceOneAppPerSuite {

  implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
  implicit var hc: HeaderCarrier = new HeaderCarrier()
  val mockAuditService: AuditService = mock[AuditService]
  val dateTime = new DateTime
  val schemeInfo = new SchemeInfo("schemeRef",dateTime,"schemeID","taxYear","schemeName","schemeType")
  val schemeData = new SchemeData(schemeInfo,"sheetName", None, ListBuffer(Seq("")))


  val auditEvents = new AuditEvents(mockAuditService)
  "sendEvent should audit runtime errors" in {

    val runtimeException = new AbstractMethodError("")
    val details = Map("ErrorMessage" -> runtimeException.getMessage,
      "Context" -> "some context info",
      "schemeData" -> Map(
        "schemeRef" -> schemeInfo.schemeRef,
        "schemeType" -> schemeInfo.schemeType,
        "schemeName" -> schemeInfo.schemeName,
        "schemeId" -> schemeInfo.schemeId,
        "taxYear" -> schemeInfo.taxYear,
        "sheetName" -> "sheetName",
        "timestamp" -> schemeInfo.timestamp.toString).toString,
      "StackTrace" -> ExceptionUtils.getStackTrace(runtimeException)
    )

    auditEvents.auditRunTimeError(runtimeException, "some context info", schemeData.schemeInfo, schemeData.sheetName)
    verify(mockAuditService, times(1)).sendEvent(argEq("ERSRunTimeError"), argEq(details))(any[Request[_]](), any[HeaderCarrier]())

  }

  "validationErrorAudit should audit the first validation error" in {
    val cell = Cell("B",11,"abc")
    val cell2 = Cell("C",0,"def")
    val details = Map(
      "Column" -> cell.column,
      "Row" -> cell.row.toString,
      "Value" -> cell.value,
      "ErrorMessage" -> "This entry must be a number made up of digits.",
      "schemeRef" -> schemeInfo.schemeRef,
      "schemeType" -> schemeInfo.schemeType,
      "schemeName" -> schemeInfo.schemeName,
      "schemeId" -> schemeInfo.schemeId,
      "taxYear" -> schemeInfo.taxYear,
      "sheetName" -> "sheetName",
      "timestamp" -> schemeInfo.timestamp.toString
    )

    val validationErrors = List(
      ValidationError(cell, "error.2","002","This entry must be a number made up of digits."),
      ValidationError(cell2,"error.3","003","This entry is larger than the maximum number value allowed.")
    )

    auditEvents.validationErrorAudit(validationErrors, schemeInfo, "sheetName")(hc, schemeInfo, request)
    verify(mockAuditService, times(1)).sendEvent(argEq("ERSValidationError"), argEq(details))(any[Request[_]](), any[HeaderCarrier]())

  }

  "ERSFileValidatorAudit should audit schemeInfo" in {
    val details = Map(
      "schemeRef" -> schemeInfo.schemeRef,
      "schemeType" -> schemeInfo.schemeType,
      "schemeName" -> schemeInfo.schemeName,
      "schemeId" -> schemeInfo.schemeId,
      "taxYear" -> schemeInfo.taxYear,
      "sheetName" -> "sheetName",
      "timestamp" -> schemeInfo.timestamp.toString
    )
    auditEvents.fileValidatorAudit(schemeInfo, "sheetName")
    verify(mockAuditService, times(1)).sendEvent(argEq("ERSFileValidatorAudit"), argEq(details))(any[Request[_]](), any[HeaderCarrier]())

  }

    "fileProcessingErrorAudit should audit the error" in {
      val errorMessage = "Could not set the validator"
      val details = Map(
        "schemeRef" -> schemeInfo.schemeRef,
        "schemeType" -> schemeInfo.schemeType,
        "schemeName" -> schemeInfo.schemeName,
        "schemeId" -> schemeInfo.schemeId,
        "taxYear" -> schemeInfo.taxYear,
        "sheetName" -> "sheetName",
        "timestamp" -> schemeInfo.timestamp.toString,
        "ErrorMessage" -> errorMessage
      )
      auditEvents.fileProcessingErrorAudit(schemeData.schemeInfo, schemeData.sheetName, errorMessage)
      verify(mockAuditService, times(1)).sendEvent(argEq("ERSFileProcessingError"), argEq(details))(any[Request[_]](), any[HeaderCarrier]())
    }

    "totalRows should audit the number of rows" in {
      val details = Map(
        "rows" -> "5",
        "schemeId" -> schemeInfo.schemeId,
        "schemeName" -> schemeInfo.schemeName,
        "schemeRef" -> schemeInfo.schemeRef,
        "schemeType" -> schemeInfo.schemeType,
        "taxYear" -> schemeInfo.taxYear,
        "timestamp" -> schemeInfo.timestamp.toString
      )

      auditEvents.totalRows(5, schemeInfo)
      verify(mockAuditService, times(1)).sendEvent(argEq("ERStotalRowCount"), argEq(details))(any[Request[_]](), any[HeaderCarrier]())
    }
}
