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

package utils

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class ErrorResponseMessagesSpec extends AnyWordSpecLike with Matchers {

  "ErrorResponseMessages.dataParserIncorrectSchemeType" should {

    "return the default message if both arguments are None" in {
      val expectedMessage = "Incorrect ERS Template - Scheme Type isn't as expected"
      ErrorResponseMessages.dataParserIncorrectSchemeType() shouldBe expectedMessage
    }

    "return the default message if only one argument is not None" in {
      val expectedMessage = "Incorrect ERS Template - Scheme Type isn't as expected"
      ErrorResponseMessages.dataParserIncorrectSchemeType(Some("CSOP")) shouldBe expectedMessage
    }

    "return the correct message with the expected and parsed sheet names" in {
      val expectedMessage = "Incorrect ERS Template - Scheme Type isn't as expected, expected: CSOP parsed: EMI"
      ErrorResponseMessages.dataParserIncorrectSchemeType(Some("CSOP"), Some("EMI")) shouldBe expectedMessage
    }

  }

}
