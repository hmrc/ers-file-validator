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

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, post, urlPathEqualTo}
import com.github.tomakehurst.wiremock.http.Fault
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import config.ApplicationConfig
import models._
import models.upscan.UpscanCallback
import org.apache.pekko.stream.Materializer
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.EitherValues
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.http.Status.{BAD_REQUEST, OK}
import play.api.inject.Injector
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import play.api.{Application, Play}
import services.audit.AuditEvents
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.test.WireMockSupport
import utils.ErrorResponseMessages.{
  fileValidatorConnectorBadRequest, fileValidatorConnectorFailedSendingData, fileValidatorConnectorNotFound,
  fileValidatorConnectorServiceUnavailable
}

import java.time.ZonedDateTime
import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

class ERSFileValidatorConnectorSpec extends PlaySpec with EitherValues with GuiceOneAppPerSuite with WireMockSupport {

  implicit override lazy val app: Application = GuiceApplicationBuilder()
    .configure(
      "metrics.enabled"                            -> false,
      "microservice.services.ers-submissions.port" -> wireMockPort,
      "microservice.services.ers-submissions.host" -> "localhost"
    )
    .build()

  lazy val injector: Injector = app.injector

  implicit def materializer: Materializer = Play.materializer

  implicit val ec: ExecutionContextExecutor = ExecutionContext.global
  implicit val hc: HeaderCarrier            = new HeaderCarrier
  val appConfig: ApplicationConfig          = app.injector.instanceOf[ApplicationConfig]

  val auditEvents: AuditEvents = app.injector.instanceOf[AuditEvents]
  val httpClient: HttpClientV2 = app.injector.instanceOf[HttpClientV2]

  val ersFileValidatorConnector: ERSFileValidatorConnector =
    spy(new ERSFileValidatorConnector(appConfig, httpClient, auditEvents, ec))

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

  val submissionSchemeData: SubmissionsSchemeData =
    SubmissionsSchemeData(schemeInfo, "sheetOne", UpscanCallback("name", "https://www.test.com/url"), numberOfRows = 1)

  val mockEncodedSubmissionsUrl   = "/ers/1234%2FABCD/submit-presubmission"
  val mockEncodedSubmissionsUrlV2 = "/ers/v2/1234%2FABCD/submit-presubmission"
  val empRef                      = "1234/ABCD"

  implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

  override protected def beforeEach(): Unit = {
    reset(ersFileValidatorConnector)
    super.beforeEach()
  }

  private def setupMockPost(statusCode: Int, url: String, body: String): StubMapping =

    wireMockServer.stubFor(
      post(urlPathEqualTo(url))
        .willReturn(
          aResponse()
            .withStatus(statusCode)
            .withBody(body)
        )
    )

  private def setupMockPostForFailure(url: String): StubMapping =

    wireMockServer.stubFor(
      post(urlPathEqualTo(url))
        .willReturn(
          aResponse()
            .withFault(Fault.EMPTY_RESPONSE)
        )
    )

  "The ERSFileValidator Connector" must {

    "return a positive response on sending sheet data" in {
      setupMockPost(OK, mockEncodedSubmissionsUrl, "Please check for me!")

      val response = await(ersFileValidatorConnector.sendToSubmissions(submissionData, empRef))
      assert(response.isRight)
      response.value.status must equal(Status.OK)
      response.value.body   must equal("Please check for me!")
      verify(ersFileValidatorConnector, times(1)).deliverSendToSubmissionsMetrics(any())
    }

    "return a Bad Request response on sending sheet data" in {
      setupMockPost(BAD_REQUEST, mockEncodedSubmissionsUrl, "Bad Request")

      val response = await(ersFileValidatorConnector.sendToSubmissions(submissionData, empRef))
      assert(response.isRight)
      response.value.status must equal(Status.BAD_REQUEST)
      response.value.body   must equal("Bad Request")
      verify(ersFileValidatorConnector, times(1)).deliverSendToSubmissionsMetrics(any())
    }

    "return a ErsFileProcessingException when receiving a BadRequestException" in {
      setupMockPostForFailure(mockEncodedSubmissionsUrl)
      val result = await(ersFileValidatorConnector.sendToSubmissions(submissionData, empRef))
      result.isLeft mustBe true

      verify(ersFileValidatorConnector, times(1)).deliverSendToSubmissionsMetrics(any())
    }

  }

  "The ERSFileValidator Connector for new validation" must {
    "return a positive response on sending sheet data" in {
      setupMockPost(OK, mockEncodedSubmissionsUrlV2, "Please check for me!")

      val response = await(ersFileValidatorConnector.sendToSubmissionsNew(submissionSchemeData, empRef))
      assert(response.isRight)
      response.value.status must equal(Status.OK)
      response.value.body   must equal("Please check for me!")
    }

    "return a Bad Request response on sending sheet data" in {
      setupMockPost(BAD_REQUEST, mockEncodedSubmissionsUrlV2, "Bad Request")

      val response = await(ersFileValidatorConnector.sendToSubmissionsNew(submissionSchemeData, empRef))
      assert(response.isRight)
      response.value.status must equal(Status.BAD_REQUEST)
      response.value.body   must equal("Bad Request")
      verify(ersFileValidatorConnector, times(1)).deliverSendToSubmissionsMetrics(any())
    }

    "return a ErsFileProcessingException when receiving a BadRequestException" in {
      setupMockPostForFailure(mockEncodedSubmissionsUrlV2)

      val result = await(ersFileValidatorConnector.sendToSubmissionsNew(submissionSchemeData, empRef))
      result.isLeft mustBe true

      verify(ersFileValidatorConnector, times(1)).deliverSendToSubmissionsMetrics(any())
    }

  }

  "When an error is returned, the ERSFileValidatorConnector" must {

    "handle BadRequestException" in {
      val exception = new BadRequestException("Submissions Service Bad Request")
      val result    =
        ersFileValidatorConnector.handleException(exception, System.currentTimeMillis(), schemeInfo, "sheetOne")
      result mustBe ErsFileProcessingException(fileValidatorConnectorBadRequest, exception.getMessage)
    }

    "handle NotFoundException" in {
      val exception = new NotFoundException("Submissions Service Not Found")
      val result    =
        ersFileValidatorConnector.handleException(exception, System.currentTimeMillis(), schemeInfo, "sheetOne")
      result mustBe ErsFileProcessingException(fileValidatorConnectorNotFound, exception.getMessage)
    }

    "handle ServiceUnavailableException" in {
      val exception = new ServiceUnavailableException("Submissions Service Service Unavailable")
      val result    =
        ersFileValidatorConnector.handleException(exception, System.currentTimeMillis(), schemeInfo, "sheetOne")
      result mustBe ErsFileProcessingException(fileValidatorConnectorServiceUnavailable, exception.getMessage)
    }

    "handle Exception" in {
      val exception = new Exception("Failed sending data")
      val result    =
        ersFileValidatorConnector.handleException(exception, System.currentTimeMillis(), schemeInfo, "sheetOne")
      result mustBe ErsFileProcessingException(fileValidatorConnectorFailedSendingData, exception.getMessage)
    }
  }

}
