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

package connectors

import akka.stream.Materializer
import config.ApplicationConfig
import models._
import org.joda.time.DateTime
import org.mockito.ArgumentMatchers.{any, eq => argEq}
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfter
import org.scalatest.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Play
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import services.audit.AuditEvents
import uk.gov.hmrc.http._
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient

import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}

class ERSFileValidatorConnectorSpec extends PlaySpec with MockitoSugar with BeforeAndAfter with GuiceOneAppPerSuite {

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
    timestamp = DateTime.now,
    schemeId = "123PA12345678",
    taxYear = "2014/F15",
    schemeName = "MyScheme",
    schemeType = "EMI"
  )
  val submissionData: SchemeData = SchemeData(schemeInfo, "sheetOne", None, data: ListBuffer[Seq[String]])
  val mockSubmissionsUrl = "/test-submissions-url"
  val mockEncodedSubmissionsUrl = "/test-submissions-url/ers/1234%2FABCD/submit-presubmission"
  val empRef = "1234/ABCD"

  "The ERSFileValidator Connector" must {
    "return a positive response on sending sheet data" in {
      implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
      when(mockAppConfig.submissionsUrl).thenReturn(mockSubmissionsUrl)
      when(mockHttpClient.POST[SchemeData, HttpResponse](argEq[String](mockEncodedSubmissionsUrl), argEq[SchemeData](submissionData), any())(any(), any(), any[HeaderCarrier](), any[ExecutionContext]()))
        .thenReturn(Future.successful(HttpResponse(200)))
      ersFileValidatorConnector.sendToSubmissions(submissionData, empRef).map {
        response => response.right.get.body must equal(200)
      }
    }

    "return a ERSFileProcessingException when receiving a BadRequestException" in {
      implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
      val badRequestException = new BadRequestException("This is a BadRequestException")
      when(mockAppConfig.submissionsUrl).thenReturn(mockSubmissionsUrl)
      when(mockHttpClient.POST[SchemeData, HttpResponse](argEq[String](mockEncodedSubmissionsUrl), argEq[SchemeData](submissionData), any())(any(), any(), any[HeaderCarrier](), any[ExecutionContext]()))
        .thenReturn(Future.failed(badRequestException))
      val result = try {
        Right(await(ersFileValidatorConnector.sendToSubmissions(submissionData, empRef)))
      } catch {
        case ex: Throwable => Left(ex)
      }
      result shouldBe Left(ERSFileProcessingException("Submissions Service Bad Request", badRequestException.getMessage))
    }

    "return a ERSFileProcessingException when receiving a NotFoundException" in {
      implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
      val notFoundException = new NotFoundException("This is a NotFoundException")
      when(mockAppConfig.submissionsUrl).thenReturn(mockSubmissionsUrl)
      when(mockHttpClient.POST[SchemeData, HttpResponse](argEq[String](mockEncodedSubmissionsUrl), argEq[SchemeData](submissionData), any())(any(), any(), any[HeaderCarrier](), any[ExecutionContext]()))
        .thenReturn(Future.failed(notFoundException))
      val result = try {
        Right(await(ersFileValidatorConnector.sendToSubmissions(submissionData, empRef)))
      } catch {
        case ex: Throwable => Left(ex)
      }
      result shouldBe Left(ERSFileProcessingException("Submissions Service Not Found", notFoundException.getMessage))
    }

    "return a ERSFileProcessingException when receiving a ServiceUnavailableException" in {
      implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
      val serviceUnavailableException = new ServiceUnavailableException("This is a ServiceUnavailableException")
      when(mockAppConfig.submissionsUrl).thenReturn(mockSubmissionsUrl)
      when(mockHttpClient.POST[SchemeData, HttpResponse](argEq[String](mockEncodedSubmissionsUrl), argEq[SchemeData](submissionData), any())(any(), any(), any[HeaderCarrier](), any[ExecutionContext]()))
        .thenReturn(Future.failed(serviceUnavailableException))
      val result = try {
        Right(await(ersFileValidatorConnector.sendToSubmissions(submissionData, empRef)))
      } catch {
        case ex: Throwable => Left(ex)
      }
      result shouldBe Left(ERSFileProcessingException("Submissions Service Service Unavailable", serviceUnavailableException.getMessage))
    }

    "return a ERSFileProcessingException when receiving an Exception" in {
      implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
      val genericException = new MethodNotAllowedException("This is a MethodNotAllowedException")
      when(mockAppConfig.submissionsUrl).thenReturn(mockSubmissionsUrl)
      when(mockHttpClient.POST[SchemeData, HttpResponse](argEq[String](mockEncodedSubmissionsUrl), argEq[SchemeData](submissionData), any())(any(), any(), any[HeaderCarrier](), any[ExecutionContext]()))
        .thenReturn(Future.failed(genericException))
      val result = try {
        Right(await(ersFileValidatorConnector.sendToSubmissions(submissionData, empRef)))
      } catch {
        case ex: Throwable => Left(ex)
      }
      result shouldBe Left(ERSFileProcessingException("Failed sending data", genericException.getMessage))
    }
  }
}
