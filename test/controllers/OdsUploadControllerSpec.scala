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

package controllers

import fixtures.WithMockedAuthActions
import metrics.Metrics
import models._
import models.scheme.SchemeMismatchError
import models.upscan.{UpscanCallback, UpscanFileData}
import org.mockito.ArgumentMatchers.{any, eq => argEq}
import org.mockito.Mockito._
import org.scalatest.OptionValues
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{Action, AnyContent, DefaultActionBuilder, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.ProcessOdsService
import uk.gov.hmrc.play.bootstrap.auth.DefaultAuthConnector
import utils.ErrorResponseMessages

import java.time.ZonedDateTime
import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}

class OdsUploadControllerSpec
    extends AnyWordSpecLike
    with Matchers
    with OptionValues
    with MockitoSugar
    with GuiceOneAppPerSuite
    with WithMockedAuthActions {

  val empRef: String                           = "1234/ABCD"
  val mockProcessOdsService: ProcessOdsService = mock[ProcessOdsService]
  val mockAuthConnector: DefaultAuthConnector  = mock[DefaultAuthConnector]
  val metrics: Metrics                         = mock[Metrics]
  implicit val ec: ExecutionContextExecutor    = ExecutionContext.global

  implicit override lazy val app: Application =
    GuiceApplicationBuilder().configure("metrics.enabled" -> false).build()

  val defaultActionBuilder: DefaultActionBuilder =
    app.injector.instanceOf(classOf[DefaultActionBuilder])

  val odsUploadController: OdsUploadController = new OdsUploadController(
    mockProcessOdsService,
    mockAuthConnector,
    stubControllerComponents(),
    defaultActionBuilder
  ) {
    override def authorisedAction(empRef: String)(body: AsyncRequest): Action[AnyContent] =
      mockAuthorisedAction(empRef)(body)
  }

  val schemeInfo: SchemeInfo = SchemeInfo(
    schemeRef = "XA11000001231275",
    timestamp = ZonedDateTime.now,
    schemeId = "123PA12345678",
    taxYear = "2014/F15",
    schemeName = "MyScheme",
    schemeType = "EMI"
  )

  val request                      = FakeRequest()
  val l: ListBuffer[SchemeData]    = new ListBuffer()

  val metaData: JsObject           = Json.obj(
    "scon"                   -> "S1401234Z",
    "nino"                   -> "CB433298A",
    "surname"                -> "Smith",
    "firstForename"          -> "Bill",
    "calcType"               -> "1",
    "dualCalculationRequest" -> false,
    "contsEarningsRequest"   -> false,
    "inflationProofRequest"  -> false
  )

  val callbackData: UpscanCallback =
    UpscanCallback("John", "downloadUrl", Some(1000L), Some("content-type"), Some(metaData), None)

  val d: UpscanFileData            = UpscanFileData(callbackData, schemeInfo)

  "processOdsFile" must {

    "successfully receive data" in {
      when(mockProcessOdsService.processFile(any[UpscanCallback](), argEq(empRef))(any(), any[SchemeInfo](), any()))
        .thenReturn(Future.successful(Right(l.size)))

      val result = odsUploadController.processOdsFile(empRef).apply(request.withJsonBody(Json.toJson(d)))
      status(result) shouldBe OK
    }

    "return BAD_REQUEST when an incorrect JSON object is sent" in {
      when(mockProcessOdsService.processFile(any[UpscanCallback](), argEq(empRef))(any(), any[SchemeInfo](), any()))
        .thenReturn(Future.successful(Right(l.size)))

      val result = odsUploadController.processOdsFile(empRef).apply(request.withJsonBody(Json.toJson(metaData)))
      status(result) shouldBe BAD_REQUEST
    }

    "return BAD_REQUEST with no JSON body" in {
      val result = odsUploadController.processOdsFile(empRef).apply(request)
      status(result) shouldBe BAD_REQUEST
    }

    "return INTERNAL_SERVER_ERROR when a SystemError occurs" in {
      val systemError = ErsSystemError("System configuration error", "Config failure")
      when(mockProcessOdsService.processFile(any[UpscanCallback](), argEq(empRef))(any(), any[SchemeInfo](), any()))
        .thenReturn(Future.successful(Left(systemError)))

      val result = odsUploadController.processOdsFile(empRef).apply(request.withJsonBody(Json.toJson(d)))
      status(result) shouldBe INTERNAL_SERVER_ERROR
    }

    "return BAD_REQUEST when a SchemeTypeMismatchError occurs" in {
      val errorMessage       = ErrorResponseMessages.dataParserIncorrectSheetName
      val expectedSchemeType = "EMI"
      val requestSchemeType  = "CSOP"

      val userError = SchemeTypeMismatchError(
        errorMessage,
        ErrorResponseMessages.dataParserIncorrectSchemeType(Some(expectedSchemeType), Some(requestSchemeType)),
        expectedSchemeType,
        requestSchemeType
      )

      when(mockProcessOdsService.processFile(any[UpscanCallback](), argEq(empRef))(any(), any[SchemeInfo](), any()))
        .thenReturn(Future.successful(Left(userError)))

      val result = odsUploadController.processOdsFile(empRef).apply(request.withJsonBody(Json.toJson(d)))
      status(result) shouldBe BAD_REQUEST

      val mismatchError = contentAsJson(result).as[SchemeMismatchError]
      mismatchError.errorMessage       shouldBe errorMessage
      mismatchError.expectedSchemeType shouldBe expectedSchemeType
      mismatchError.requestSchemeType  shouldBe requestSchemeType
    }

    "return BAD_REQUEST when a UserValidationError occurs" in {
      val userError = HeaderValidationError("Header error", "Invalid header format")

      when(mockProcessOdsService.processFile(any[UpscanCallback](), argEq(empRef))(any(), any[SchemeInfo](), any()))
        .thenReturn(Future.successful(Left(userError)))

      val result: Future[Result] =
        odsUploadController.processOdsFile(empRef).apply(request.withJsonBody(Json.toJson(d)))

      status(result)          shouldBe BAD_REQUEST
      contentAsString(result) shouldBe "Header error"
    }
  }

}
