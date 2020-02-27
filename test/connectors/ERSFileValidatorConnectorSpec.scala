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

import metrics.Metrics
import models._
import org.joda.time.DateTime
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfter
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.{OneServerPerSuite, PlaySpec}
import play.api.{Logger, Play}
import play.api.test.FakeRequest
import uk.gov.hmrc.play.http._
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import uk.gov.hmrc.http._

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class ERSFileValidatorConnectorSpec extends PlaySpec with OneServerPerSuite with MockitoSugar with BeforeAndAfter {

  val mockHttpPost = mock[HttpPost]
  val mockHttpGet = mock[HttpGet]
  val mockHttpPut = mock[HttpPut]
  val metrics = mock[Metrics]

  object TestERSFileValidatorConnector extends ERSFileValidatorConnector {
    protected def mode: play.api.Mode.Mode = Play.current.mode
    protected def runModeConfiguration: play.api.Configuration = Play.current.configuration
    override val httpPost: HttpPost = mockHttpPost
    override val httpGet: HttpGet = mockHttpGet
    override val httpPut: HttpPut = mockHttpPut
  }

  before {
    reset(mockHttpPost)
    reset(mockHttpGet)
    reset(mockHttpPut)
  }

  val data = ListBuffer[Seq[String]](Seq("abc"))

  val schemeInfo: SchemeInfo = SchemeInfo(
    schemeRef = "XA11000001231275",
    timestamp = DateTime.now,
    schemeId = "123PA12345678",
    taxYear = "2014/F15",
    schemeName = "MyScheme",
    schemeType = "EMI"
  )
  val submissionData = SchemeData(schemeInfo, "sheetOne", None, data: ListBuffer[Seq[String]])

  "The ERSFileValidator Connector" must {
    "return a positive response on sending sheet data" in {
      implicit val request = FakeRequest()
      implicit val hc: HeaderCarrier = new HeaderCarrier
      when(mockHttpPost.POST[SchemeData, HttpResponse](Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(HttpResponse(200)))
      TestERSFileValidatorConnector.sendToSubmissions(submissionData, "").map {
        response => response.body must equal(200)
      }
    }
    "throw the expected exception" in {

      val testData: List[(Throwable, String)] = List(
        (new Throwable(""), Messages("ers.exceptions.fileValidatorConnector.failedSendingData") + ", " + ""),
        (new BadRequestException(""), Messages("ers.exceptions.fileValidatorConnector.badRequest") + ", " + ""),
        (new NotFoundException(""), Messages("ers.exceptions.fileValidatorConnector.notFound") + ", " + ""),
        (new ServiceUnavailableException(""), Messages("ers.exceptions.fileValidatorConnector.serviceUnavailable") + ", " + "")
      )

      implicit val request = FakeRequest()
      implicit val hc: HeaderCarrier = new HeaderCarrier

      for (i <- 0 until testData.size) {
        when(mockHttpPost.POST[SchemeData, HttpResponse](Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.failed(testData(i)._1))

        def exceptionMessage: String = {
          try {
            val result = Await.result(TestERSFileValidatorConnector.sendToSubmissions(SchemeData(schemeInfo, "", None, data: ListBuffer[Seq[String]]), ""), 1 seconds)
            result.toString()
          }
          catch {
            case e: ERSFileProcessingException => {
              return e.message + ", " + e.context
            }
          }
        }
        Logger.debug(exceptionMessage)
        Logger.debug(testData(i)._2)
        exceptionMessage must be(testData(i)._2)
      }
    }
  }
}
