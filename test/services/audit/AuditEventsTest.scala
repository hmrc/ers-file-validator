/*
 * Copyright 2018 HM Revenue & Customs
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

import uk.gov.hmrc.services.validation.{Cell, ValidationError}
import models.{SchemeData, SchemeInfo, ValidationErrorData}
import org.apache.commons.lang3.exception.ExceptionUtils
import org.joda.time.DateTime
import org.scalatest.{Matchers, WordSpec}
import play.api.test.FakeRequest
import services.audit.{AuditEvents, AuditService, AuditServiceConnector}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.model.DataEvent

import scala.collection.mutable.ListBuffer

class AuditEventsTest    extends WordSpec with Matchers {

  implicit val request = FakeRequest()
  implicit var hc = new HeaderCarrier()

  trait ObservableAuditConnector extends AuditServiceConnector {
    val events: ListBuffer[DataEvent] = new ListBuffer[DataEvent]

    def observedEvents: ListBuffer[DataEvent] = events

    def addEvent(dataEvent: DataEvent): Unit = {
      events += dataEvent
    }

    override def auditData(dataEvent: DataEvent)(implicit hc: HeaderCarrier): Unit = {
      addEvent(dataEvent)
    }
  }

  def createObservableAuditConnector = new ObservableAuditConnector {}

  def createAuditor(observableAuditConnector: ObservableAuditConnector) = {

    val testAuditService = new AuditService {
      override def auditConnector = observableAuditConnector
    }

    new AuditEvents {
      override def auditService: AuditService = testAuditService
    }
  }


  "its should audit runtime errors" in {
    val observableAuditConnector = createObservableAuditConnector
    val auditor = createAuditor(observableAuditConnector)

    var runtimeException : Throwable = null

    try {
      var divideByZero : Int = 0/0
    } catch {
      case e:Throwable => {
        runtimeException = e
        val dateTime = new DateTime
        val schemeInfo = new SchemeInfo("",dateTime,"","","","")
        val schemeData = new SchemeData(schemeInfo,"", None, ListBuffer(Seq("")))
        auditor.auditRunTimeError(e, "some context info", schemeData.schemeInfo, schemeData.sheetName)
      }
    }

    observableAuditConnector.events.length should equal(1)

    val event = observableAuditConnector.events.head

    event.auditType should equal("ERSRunTimeError")

    event.detail("ErrorMessage") should equal(runtimeException.getMessage)
    event.detail("StackTrace") should equal(ExceptionUtils.getStackTrace(runtimeException))
    event.detail("Context") should equal("some context info")
  }

  "get data for validationError audit when sending validationErrorAudit" in {
    def resultBuilder(cellData: Cell, expectedResultsMaybe: List[ValidationErrorData]): List[ValidationError] = {
      def populateValidationError(expRes: ValidationErrorData)(implicit cell: Cell) = {
        ValidationError(cell, expRes.id, expRes.errorId, expRes.errorMsg)
      }
      implicit val cell: Cell = cellData
      val validationErrors = expectedResultsMaybe.map(errorData => populateValidationError(errorData))
      validationErrors
    }

    val validationErrors = resultBuilder(
      Cell("B",11,"abc"),
      List(
        ValidationErrorData("error.2","002","This entry must be a number made up of digits."),
        ValidationErrorData("error.3","003","This entry is larger than the maximum number value allowed.")
      )
    )
    implicit val schemeInfo = SchemeInfo (
      schemeRef = "XA11000001231275",
      timestamp = DateTime.now,
      schemeId = "123PA12345678",
      taxYear = "2014/F15",
      schemeName = "MyScheme",
      schemeType = "EMI"
    )
    val schemeData = SchemeData(schemeInfo, "", None, new ListBuffer[Seq[String]])
    val ve = Map("Row" -> "11", "Column" -> "B", "timestamp" -> schemeInfo.timestamp.toString, "Value" -> "abc", "taxYear" -> "2014/F15", "sheetName" -> "", "schemeName" -> "MyScheme", "schemeId" -> "123PA12345678", "schemeType" -> "EMI", "schemeRef" -> "XA11000001231275", "ErrorMessage" -> "This entry must be a number made up of digits.")

    val observableAuditConnector = createObservableAuditConnector
    val auditor = createAuditor(observableAuditConnector)

    auditor.validationErrorAudit(validationErrors, schemeData.schemeInfo, schemeData.sheetName)

    observableAuditConnector.events.length should equal(1)

    val event = observableAuditConnector.events.head
    event.auditType should equal("ERSValidationError")
    event.detail("ErrorMessage") should equal("This entry must be a number made up of digits.")
    event.detail("Row") should equal("11")
    event.detail("Column") should equal("B")
    event.detail("Value") should equal("abc")
  }

  "submit transfer results audit event" in {
    val observableAuditConnector = createObservableAuditConnector
    val auditor = createAuditor(observableAuditConnector)
    val dateTime = new DateTime

    val schemeData = new SchemeData(new SchemeInfo("test",dateTime,"test","test","test","test"),"test", None, ListBuffer(Seq("test")))
    auditor.fileValidatorAudit(schemeData.schemeInfo, schemeData.sheetName)

    observableAuditConnector.events.length should equal(1)

    val event = observableAuditConnector.events.head
    event.auditType should equal("ERSFileValidatorAudit")
    event.detail("sheetName") should equal(schemeData.sheetName)
  }

  "submit audit for a file processing error" in {
    val observableAuditConnector = createObservableAuditConnector
    val auditor = createAuditor(observableAuditConnector)
    val dateTime = new DateTime
    val schemeData = new SchemeData(new SchemeInfo("test", dateTime, "test", "test", "test", "test"), "test", None, ListBuffer(Seq("test")))
    val msg = "Could not set the validator"
    auditor.fileProcessingErrorAudit(schemeData.schemeInfo, schemeData.sheetName, msg)

    observableAuditConnector.events.length should equal(1)

    val event = observableAuditConnector.events.head
    event.auditType should equal("ERSFileProcessingError")
    event.detail("schemeRef") should equal(schemeData.schemeInfo.schemeRef)
    event.detail("schemeType") should equal(schemeData.schemeInfo.schemeType)
    event.detail("schemeName") should equal(schemeData.schemeInfo.schemeName)
    event.detail("schemeId") should equal(schemeData.schemeInfo.schemeId)
    event.detail("taxYear") should equal(schemeData.schemeInfo.taxYear)
    event.detail("sheetName") should equal(schemeData.sheetName)
    event.detail("timestamp") should equal(schemeData.schemeInfo.timestamp.toString)
    event.detail("ErrorMessage") should equal(msg)

  }

  "totalRows should count the number of rows" in {
    val observableAuditConnector = createObservableAuditConnector
    val auditor = createAuditor(observableAuditConnector)
    val dateTime = new DateTime
    val schemeInfo = new SchemeInfo("test", dateTime, "test", "test", "test", "test")

    auditor.totalRows(5, schemeInfo)

    observableAuditConnector.events.length should equal(1)
    val event = observableAuditConnector.events.head
    event.auditType should equal("ERStotalRowCount")
    event.detail("schemeRef") should equal(schemeInfo.schemeRef)
    event.detail("schemeType") should equal(schemeInfo.schemeType)
    event.detail("schemeName") should equal(schemeInfo.schemeName)
    event.detail("schemeId") should equal(schemeInfo.schemeId)
    event.detail("taxYear") should equal(schemeInfo.taxYear)
    event.detail("timestamp") should equal(schemeInfo.timestamp.toString)
    event.detail("rows") should equal("5")
  }

}
