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

package connectors

import org.apache.pekko.stream.Materializer
import models._
import org.mockito.ArgumentMatchers.{any, eq => argEq}
import org.mockito.Mockito._
import org.scalatest.{BeforeAndAfter, EitherValues}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Play
import play.api.http.Status
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.http._
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient

import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.Application
import uk.gov.hmrc.validator.services.audit.AuditEvents
import uk.gov.hmrc.validator.services.config.ApplicationConfig
import uk.gov.hmrc.validator.services.connectors.ERSFileValidatorConnector
import uk.gov.hmrc.validator.services.models.{ERSFileProcessingException, SchemeData, SchemeInfo, SubmissionsSchemeData}
import uk.gov.hmrc.validator.services.models.upscan.UpscanCallback

import java.time.ZonedDateTime

class ERSFileValidatorConnectorSpec extends PlaySpec with MockitoSugar with BeforeAndAfter with EitherValues with GuiceOneAppPerSuite {

  override lazy implicit val app: Application = GuiceApplicationBuilder().configure("metrics.enabled" -> false).build()
  implicit def materializer: Materializer = Play.materializer
  implicit val ec: ExecutionContextExecutor = ExecutionContext.global
  implicit val hc: HeaderCarrier = new HeaderCarrier
  val mockAppConfig: ApplicationConfig = mock[ApplicationConfig]
  val mockHttpClient: DefaultHttpClient = mock[DefaultHttpClient]

  val mockAuditEvents: AuditEvents = mock[AuditEvents]

  val ersFileValidatorConnector = new ERSFileValidatorConnector(mockAppConfig,
    mockHttpClient,
    mockAuditEvents,
    ec)
  val data: ListBuffer[Seq[String]] = ListBuffer[Seq[String]](Seq("abc"))
  val schemeInfo: SchemeInfo = SchemeInfo(
    schemeRef = "XA11000001231275",
    timestamp = ZonedDateTime.now,
    schemeId = "123PA12345678",
    taxYear = "2014/F15",
    schemeName = "MyScheme",
    schemeType = "EMI"
  )
  val submissionData: SchemeData = SchemeData(schemeInfo, "sheetOne", None, data: ListBuffer[Seq[String]])
  val submissionSchemeData: SubmissionsSchemeData = SubmissionsSchemeData(
    schemeInfo, "sheetOne", UpscanCallback("name", "https://www.test.com/url"), numberOfRows = 1)
  val mockSubmissionsUrl = "/test-submissions-url"
  val mockEncodedSubmissionsUrl = "/test-submissions-url/ers/1234%2FABCD/submit-presubmission"
  val mockEncodedSubmissionsUrlV2 = "/test-submissions-url/ers/v2/1234%2FABCD/submit-presubmission"
  val empRef = "1234/ABCD"

  "The ERSFileValidator Connector" must {
    "return a positive response on sending sheet data" in {
      implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
      when(mockAppConfig.submissionsUrl).thenReturn(mockSubmissionsUrl)
      when(mockHttpClient.POST[SchemeData, HttpResponse](argEq[String](mockEncodedSubmissionsUrl), argEq[SchemeData](submissionData), any())(any(), any(), any[HeaderCarrier](), any[ExecutionContext]()))
        .thenReturn(Future.successful(HttpResponse(Status.OK, "Please check for me!")))
      val response = await(ersFileValidatorConnector.sendToSubmissions(submissionData, empRef))
      assert(response.isRight)
      response.value.status must equal(Status.OK)
      response.value.body must equal("Please check for me!")
    }

    "return a ERSFileProcessingException when receiving a BadRequestException" in {
      implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
      val badRequestException = new BadRequestException("This is a BadRequestException")
      when(mockAppConfig.submissionsUrl).thenReturn(mockSubmissionsUrl)
      when(mockHttpClient.POST[SchemeData, HttpResponse](argEq[String](mockEncodedSubmissionsUrl), argEq[SchemeData](submissionData), any())(any(), any(), any[HeaderCarrier](), any[ExecutionContext]()))
        .thenReturn(Future.failed(badRequestException))
      val result = await(ersFileValidatorConnector.sendToSubmissions(submissionData, empRef))
      result mustBe Left(ERSFileProcessingException("Submissions Service Bad Request", badRequestException.getMessage))
    }

    "return a ERSFileProcessingException when receiving a NotFoundException" in {
      implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
      val notFoundException = new NotFoundException("This is a NotFoundException")
      when(mockAppConfig.submissionsUrl).thenReturn(mockSubmissionsUrl)
      when(mockHttpClient.POST[SchemeData, HttpResponse](argEq[String](mockEncodedSubmissionsUrl), argEq[SchemeData](submissionData), any())(any(), any(), any[HeaderCarrier](), any[ExecutionContext]()))
        .thenReturn(Future.failed(notFoundException))
      val result = await(ersFileValidatorConnector.sendToSubmissions(submissionData, empRef))
      result mustBe Left(ERSFileProcessingException("Submissions Service Not Found", notFoundException.getMessage))
    }

    "return a ERSFileProcessingException when receiving a ServiceUnavailableException" in {
      implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
      val serviceUnavailableException = new ServiceUnavailableException("This is a ServiceUnavailableException")
      when(mockAppConfig.submissionsUrl).thenReturn(mockSubmissionsUrl)
      when(mockHttpClient.POST[SchemeData, HttpResponse](argEq[String](mockEncodedSubmissionsUrl), argEq[SchemeData](submissionData), any())(any(), any(), any[HeaderCarrier](), any[ExecutionContext]()))
        .thenReturn(Future.failed(serviceUnavailableException))
      val result = await(ersFileValidatorConnector.sendToSubmissions(submissionData, empRef))
      result mustBe Left(ERSFileProcessingException("Submissions Service Service Unavailable", serviceUnavailableException.getMessage))
    }

    "return a ERSFileProcessingException when receiving an Exception" in {
      implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
      val genericException = new MethodNotAllowedException("This is a MethodNotAllowedException")
      when(mockAppConfig.submissionsUrl).thenReturn(mockSubmissionsUrl)
      when(mockHttpClient.POST[SchemeData, HttpResponse](argEq[String](mockEncodedSubmissionsUrl), argEq[SchemeData](submissionData), any())(any(), any(), any[HeaderCarrier](), any[ExecutionContext]()))
        .thenReturn(Future.failed(genericException))
      val result = await(ersFileValidatorConnector.sendToSubmissions(submissionData, empRef))
      result mustBe Left(ERSFileProcessingException("Failed sending data", genericException.getMessage))
    }
  }

  "The ERSFileValidator Connector for new validation" must {
    "return a positive response on sending sheet data" in {
      implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
      when(mockAppConfig.submissionsUrl).thenReturn(mockSubmissionsUrl)
      when(mockHttpClient.POST[SubmissionsSchemeData, HttpResponse](argEq[String](mockEncodedSubmissionsUrlV2), argEq[SubmissionsSchemeData](submissionSchemeData), any())(any(), any(), any[HeaderCarrier](), any[ExecutionContext]()))
        .thenReturn(Future.successful(HttpResponse(Status.OK, "Please check for me!")))
      val response = await(ersFileValidatorConnector.sendToSubmissionsNew(submissionSchemeData, empRef))
      assert(response.isRight)
      response.value.status must equal(Status.OK)
      response.value.body must equal("Please check for me!")
    }

    "return a ERSFileProcessingException when receiving a BadRequestException" in {
      implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
      val badRequestException = new BadRequestException("This is a BadRequestException")
      when(mockAppConfig.submissionsUrl).thenReturn(mockSubmissionsUrl)
      when(mockHttpClient.POST[SubmissionsSchemeData, HttpResponse](argEq[String](mockEncodedSubmissionsUrlV2), argEq[SubmissionsSchemeData](submissionSchemeData), any())(any(), any(), any[HeaderCarrier](), any[ExecutionContext]()))
        .thenReturn(Future.failed(badRequestException))
      val result = await(ersFileValidatorConnector.sendToSubmissionsNew(submissionSchemeData, empRef))
      result mustBe Left(ERSFileProcessingException("Submissions Service Bad Request", badRequestException.getMessage))
    }

    "return a ERSFileProcessingException when receiving a NotFoundException" in {
      implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
      val notFoundException = new NotFoundException("This is a NotFoundException")
      when(mockAppConfig.submissionsUrl).thenReturn(mockSubmissionsUrl)
      when(mockHttpClient.POST[SubmissionsSchemeData, HttpResponse](argEq[String](mockEncodedSubmissionsUrlV2), argEq[SubmissionsSchemeData](submissionSchemeData), any())(any(), any(), any[HeaderCarrier](), any[ExecutionContext]()))
        .thenReturn(Future.failed(notFoundException))
      val result = await(ersFileValidatorConnector.sendToSubmissionsNew(submissionSchemeData, empRef))
      result mustBe Left(ERSFileProcessingException("Submissions Service Not Found", notFoundException.getMessage))
    }

    "return a ERSFileProcessingException when receiving a ServiceUnavailableException" in {
      implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
      val serviceUnavailableException = new ServiceUnavailableException("This is a ServiceUnavailableException")
      when(mockAppConfig.submissionsUrl).thenReturn(mockSubmissionsUrl)
      when(mockHttpClient.POST[SubmissionsSchemeData, HttpResponse](argEq[String](mockEncodedSubmissionsUrlV2), argEq[SubmissionsSchemeData](submissionSchemeData), any())(any(), any(), any[HeaderCarrier](), any[ExecutionContext]()))
        .thenReturn(Future.failed(serviceUnavailableException))
      val result = await(ersFileValidatorConnector.sendToSubmissionsNew(submissionSchemeData, empRef))
      result mustBe Left(ERSFileProcessingException("Submissions Service Service Unavailable", serviceUnavailableException.getMessage))
    }

    "return a ERSFileProcessingException when receiving an Exception" in {
      implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
      val genericException = new MethodNotAllowedException("This is a MethodNotAllowedException")
      when(mockAppConfig.submissionsUrl).thenReturn(mockSubmissionsUrl)
      when(mockHttpClient.POST[SubmissionsSchemeData, HttpResponse](argEq[String](mockEncodedSubmissionsUrlV2), argEq[SubmissionsSchemeData](submissionSchemeData), any())(any(), any(), any[HeaderCarrier](), any[ExecutionContext]()))
        .thenReturn(Future.failed(genericException))
      val result = await(ersFileValidatorConnector.sendToSubmissionsNew(submissionSchemeData, empRef))
      result mustBe Left(ERSFileProcessingException("Failed sending data", genericException.getMessage))
    }
  }
}
