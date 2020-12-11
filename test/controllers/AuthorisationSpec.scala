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

package controllers

import akka.stream.Materializer
import controllers.Assets.Ok
import controllers.auth.Authorisation
import org.mockito.ArgumentMatchers.{any, eq => argEq}
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Play
import play.api.http.Status
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents, DefaultActionBuilder, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.stubControllerComponents
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.EmptyRetrieval
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}


class AuthorisationSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach with GuiceOneAppPerSuite {

  val mockAuthConnector: AuthConnector = mock[AuthConnector]
  implicit def materializer: Materializer = app.materializer


  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockAuthConnector)
  }

  def getEnrolmentPredicate(taxOfficeNumber: String, taxOfficeReference: String): Predicate =
    ConfidenceLevel.L50 and Enrolment("IR-PAYE")
      .withIdentifier("TaxOfficeNumber", taxOfficeNumber)
      .withIdentifier("TaxOfficeReference", taxOfficeReference)
      .withDelegatedAuthRule("ers-auth")

  class AuthorisationTestController extends Authorisation {
    implicit val ec: ExecutionContext = ExecutionContext.global
    val cc: ControllerComponents = stubControllerComponents()
    val authConnector: AuthConnector = mockAuthConnector
    val defaultActionBuilder = app.injector.instanceOf(classOf[DefaultActionBuilder])


    def testAuthorisedAction(empRef: String): Action[AnyContent] = authorisedAction(empRef) { implicit request =>
      Future.successful(Ok("Successful"))
    }

    def testAuthorisedActionWithBody(empRef: String): Action[JsValue] = authorisedActionWithBody(empRef) { implicit request =>
      Future.successful(Ok("Successful"))
    }
  }

  val authorisationTestController = new AuthorisationTestController

  "authorisedAction" should {
    "return a perform the action if the user is authorised " in {
      when(
        mockAuthConnector
          .authorise(
            argEq(getEnrolmentPredicate("123", "2343234")),
            argEq(EmptyRetrieval)
          )(
            any(), any()
          )
      ).thenReturn(Future.successful(()))

      val result: Future[Result] = authorisationTestController.testAuthorisedAction("123/2343234")(FakeRequest())
      status(result) shouldBe Status.OK
      await(bodyOf(result).map(_ shouldBe "Successful"))
    }
  }

    "return a 401 if an Authorisation Exception is experienced" in {
      when(
        mockAuthConnector
          .authorise(
            argEq(getEnrolmentPredicate("123", "2343234")),
            argEq(EmptyRetrieval)
          )(
            any(), any()
          )
      ).thenReturn(Future.failed(InsufficientConfidenceLevel("failed")))

      val result: Future[Result] = authorisationTestController.testAuthorisedAction("123/2343234")(FakeRequest())
      status(result) shouldBe Status.UNAUTHORIZED
    }

    "return a 401 if an NoActiveSession Exception is experienced" in {
      when(
        mockAuthConnector
          .authorise(
            argEq(getEnrolmentPredicate("123", "2343234")),
            argEq(EmptyRetrieval)
          )(
            any(), any()
          )
      ).thenReturn(Future.failed(BearerTokenExpired("failed")))

      val result: Future[Result] = authorisationTestController.testAuthorisedAction("123/2343234")(FakeRequest())
      status(result) shouldBe Status.UNAUTHORIZED
    }

    "return a 401 if an invalid empref is passed in" in {
      val result: Future[Result] = authorisationTestController.testAuthorisedAction("1232343234")(FakeRequest())
      status(result) shouldBe Status.UNAUTHORIZED
    }

  "authorisedActionWithBody" should {

    val testBody = Json.obj("Example" -> "Payload")

    "return a perform the action if the user is authorised " in {
      when(
        mockAuthConnector
          .authorise(
            argEq(getEnrolmentPredicate("123", "2343234")),
            argEq(EmptyRetrieval)
          )(
            any(), any()
          )
      ).thenReturn(Future.successful(()))

      val result: Future[Result] = authorisationTestController.testAuthorisedActionWithBody("123/2343234")(FakeRequest().withBody(testBody))
      status(result) shouldBe Status.OK
      await(bodyOf(result).map(_ shouldBe "Successful"))
    }

    "return a 401 if an Authorisation Exception is experienced" in {
      when(
        mockAuthConnector
          .authorise(
            argEq(getEnrolmentPredicate("123", "2343234")),
            argEq(EmptyRetrieval)
          )(
            any(), any()
          )
      ).thenReturn(Future.failed(InsufficientConfidenceLevel("failed")))

      val result: Future[Result] = authorisationTestController.testAuthorisedActionWithBody("123/2343234")(FakeRequest().withBody(testBody))
      status(result) shouldBe Status.UNAUTHORIZED
    }

    "return a 401 if an NoActiveSession Exception is experienced" in {
      when(
        mockAuthConnector
          .authorise(
            argEq(getEnrolmentPredicate("123", "2343234")),
            argEq(EmptyRetrieval)
          )(
            any(), any()
          )
      ).thenReturn(Future.failed(BearerTokenExpired("failed")))

      val result: Future[Result] = authorisationTestController.testAuthorisedActionWithBody("123/2343234")(FakeRequest().withBody(testBody))
      status(result) shouldBe Status.UNAUTHORIZED
    }

    "return a 401 if an invalid empref is passed in" in {
      val result: Future[Result] = authorisationTestController.testAuthorisedActionWithBody("1232343234")(FakeRequest().withBody(testBody))
      status(result) shouldBe Status.UNAUTHORIZED
    }
  }
}
