/*
 * Copyright 2022 HM Revenue & Customs
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

import org.scalatest.OptionValues
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class ValidationUtilsSpec extends AnyWordSpecLike with Matchers with OptionValues{

  "numberOfSlices" should {
    "return the number of slices required to fit all rows based on the size of the buffer" in {
      ValidationUtils.numberOfSlices(2, 2) shouldBe 1
      ValidationUtils.numberOfSlices(1, 2) shouldBe 1
      ValidationUtils.numberOfSlices(1, 4) shouldBe 1
      ValidationUtils.numberOfSlices(4, 2) shouldBe 2
      ValidationUtils.numberOfSlices(10, 2) shouldBe 5
      ValidationUtils.numberOfSlices(10, 5) shouldBe 2

      ValidationUtils.numberOfSlices(3, 2) shouldBe 2
      ValidationUtils.numberOfSlices(5, 2) shouldBe 3
      ValidationUtils.numberOfSlices(5, 3) shouldBe 2
      ValidationUtils.numberOfSlices(7, 3) shouldBe 3
      ValidationUtils.numberOfSlices(16, 5) shouldBe 4
      ValidationUtils.numberOfSlices(19, 5) shouldBe 4

    }
  }
}
