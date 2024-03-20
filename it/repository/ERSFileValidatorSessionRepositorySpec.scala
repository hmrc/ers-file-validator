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

package test.repository

import config.ApplicationConfig
import models.upscan.UpscanCallback
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.libs.json._
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import repository.ERSFileValidatorSessionRepository
import uk.gov.hmrc.mongo.cache.DataKey
import uk.gov.hmrc.mongo.{MongoComponent, TimestampSupport}

import scala.concurrent.ExecutionContext

  class ERSFileValidatorSessionRepositorySpec extends PlaySpec with ScalaFutures with MockitoSugar with GuiceOneServerPerSuite {

    private val sessionId = "sessionId"
    private implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global
    private implicit val request: Request[AnyRef] = FakeRequest().withSession("sessionId" -> sessionId)

    private val fakeAppConfig = app.injector.instanceOf[ApplicationConfig]
    private val mongoComponent = app.injector.instanceOf[MongoComponent]
    private val timestamp = app.injector.instanceOf[TimestampSupport]
    private val sessionRepository = new ERSFileValidatorSessionRepository(mongoComponent, fakeAppConfig, timestamp)

    "ERSFileValidatorSessionRepository" must {
      "store data" in {
        val dataKey = DataKey[UpscanCallback]("callback_data_key")
        val upscanCallback = UpscanCallback("someName", "someUrl")
        val expectedResult = Json.parse(
        """{"name":"someName","downloadUrl":"someUrl","_type":"UploadedSuccessfully"}""").as[UpscanCallback]
        await(sessionRepository.putSession(dataKey, upscanCallback))

        val result = await(sessionRepository.getFromSession(dataKey))
        result.value mustBe expectedResult
      }
    }
  }
