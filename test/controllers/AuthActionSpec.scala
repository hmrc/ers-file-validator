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
import controllers.auth.AuthAction
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import play.api.Play
import play.api.http.Status
import play.api.mvc.{Request, Result, Results}
import play.api.test.FakeRequest
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.EmptyRetrieval
import uk.gov.hmrc.auth.core.{AuthConnector, BearerTokenExpired, ConfidenceLevel, Enrolment, InsufficientConfidenceLevel}
import uk.gov.hmrc.domain.EmpRef
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try


class AuthActionSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach with WithFakeApplication {

  val mockAuthConnector: AuthConnector = mock[AuthConnector]

  implicit def materializer: Materializer = Play.materializer(fakeApplication)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockAuthConnector)
  }

  def authAction(empRef: String): AuthAction = new AuthAction {
    override val optionalEmpRef: Option[EmpRef] = Try(EmpRef.fromIdentifiers(empRef)).toOption
    override implicit val ec: ExecutionContext = ExecutionContext.global

    override def authConnector: AuthConnector = mockAuthConnector
  }

  def defaultAsyncBody: Request[_] => Result = _ => Results.Ok("Successful")

  def getEnrolmentPredicate(taxOfficeNumber: String, taxOfficeReference: String): Predicate =
    ConfidenceLevel.L50 and Enrolment("IR-PAYE")
      .withIdentifier("TaxOfficeNumber", taxOfficeNumber)
      .withIdentifier("TaxOfficeReference", taxOfficeReference)
      .withDelegatedAuthRule("ers-auth")


  "AuthAction" should {
    "return a perform the action if the user is authorised " in {
      when(
        mockAuthConnector
          .authorise(
            Matchers.eq(getEnrolmentPredicate("123", "2343234")),
            Matchers.eq(EmptyRetrieval)
          )(
            Matchers.any(), Matchers.any()
          )
      ).thenReturn(Future.successful(()))

      val result: Future[Result] = authAction("123/2343234")(defaultAsyncBody)(FakeRequest())
      status(result) shouldBe Status.OK
      await(
        bodyOf(result).map(
          _ shouldBe "Successful"
        )
      )
    }

    "return a 401 if an Authorisation Exception is experienced" in {
      when(
        mockAuthConnector
          .authorise(
            Matchers.eq(getEnrolmentPredicate("123", "2343234")),
            Matchers.eq(EmptyRetrieval)
          )(
            Matchers.any(), Matchers.any()
          )
      ).thenReturn(Future.failed(InsufficientConfidenceLevel("failed")))

      val result: Future[Result] = authAction("123/2343234")(defaultAsyncBody)(FakeRequest())
      status(result) shouldBe Status.UNAUTHORIZED
    }

    "return a 401 if an NoActiveSession Exception is experienced" in {
      when(
        mockAuthConnector
          .authorise(
            Matchers.eq(getEnrolmentPredicate("123", "2343234")),
            Matchers.eq(EmptyRetrieval)
          )(
            Matchers.any(), Matchers.any()
          )
      ).thenReturn(Future.failed(BearerTokenExpired("failed")))

      val result: Future[Result] = authAction("123/2343234")(defaultAsyncBody)(FakeRequest())
      status(result) shouldBe Status.UNAUTHORIZED
    }

    "return a 401 if an invalid empref is passed in" in {
      val result: Future[Result] = authAction("12343234")(defaultAsyncBody)(FakeRequest())
      status(result) shouldBe Status.UNAUTHORIZED
    }

    "return a 401 if an InsufficientConfidenceLevel Exception is experienced" in {
      when(
        mockAuthConnector
          .authorise(
            Matchers.eq(getEnrolmentPredicate("123", "2343234")),
            Matchers.eq(EmptyRetrieval)
          )(
            Matchers.any(), Matchers.any()
          )
      ).thenReturn(Future.failed(InsufficientConfidenceLevel("failed")))

      val result: Future[Result] = authAction("123/2343234")(defaultAsyncBody)(FakeRequest())
      status(result) shouldBe Status.UNAUTHORIZED
    }

  }

}
