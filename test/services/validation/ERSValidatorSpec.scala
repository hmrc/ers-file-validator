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

package services.validation


import com.typesafe.config.ConfigFactory
import uk.gov.hmrc.services.validation.DataValidator
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import org.scalatestplus.play.PlaySpec
import services.validation.EMITestData.ERSValidationEMIAdjustmentsTestData

class ERSValidatorSpec extends PlaySpec with GuiceOneAppPerSuite with ScalaFutures with MockitoSugar with ERSValidationEMIAdjustmentsTestData {

  val validator: DataValidator = new DataValidator(ConfigFactory.load.getConfig("ers-emi-adjustments-validation-config"))
  val testData =  Seq("yes", "yes", "yes", "4", "2011-10-13", "Mia", "Iam", "Aim", "AB123456C", "123/XZ55555555", "10.1234", "10.14", "10.1324", "10.1244")

  "getCells" should {
    "should return valid cells" in {
      ErsValidator.getCells(testData, 1) mustBe getValidRowData
    }
  }

  "validateRow" should {
    "pass a row of valid EMI adjustments data without failure" in {
      ErsValidator.validateRow(getValidRowData.map(_.value),10,validator) mustBe None
    }
    "pass a row of invalid EMI adjustments data with failure" in {
      ErsValidator.validateRow(getInvalidRowData.map(_.value),10,validator).get.size mustBe 14
    }
  }
}
