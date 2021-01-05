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

package services

import java.io._
import org.scalatestplus.play.PlaySpec
import services.staxintegrationTestData.CSOPStaxIntegrationTestData
import scala.xml.Elem

class StaxProcessorSpec extends PlaySpec with CSOPStaxIntegrationTestData {

  "StaxProcessor" must {

    "return true for has next" in {
      val inputXml = xmlHeader + documentHeader + simpleXml.toString() + documentHeaderClosingTag
      val processor = new StaxProcessor(new ByteArrayInputStream(inputXml.getBytes("utf-8")))
      processor.hasNext must equal(true)
    }

    "say that the table name is CSOP_OptionsGranted_V3" in {
      val inputXml = xmlHeader + documentHeader + simpleXml.toString() + documentHeaderClosingTag
      val processor = new StaxProcessor(new ByteArrayInputStream(inputXml.getBytes("utf-8")))
      processor.hasNext
      processor.next must equal("CSOP_OptionsGranted_V3")
    }

    "information can be extracted from incoming string" in {
      val inputXml = xmlHeader + documentHeader + simpleXml.toString() + documentHeaderClosingTag
      val processor = new StaxProcessor(new ByteArrayInputStream(inputXml.getBytes("utf-8")))
      processor.getName("<['urn:oasis:names:tc:opendocument:xmlns:table:1.0']:table:table table:name='EMI40_Adjustments_V3' table:style-name='ta1'>") must equal("EMI40_Adjustments_V3")
    }

    def constructXmlDocument(elements : Elem*) : InputStream = {
      val inputXml = xmlHeader + documentHeader + elements.foldLeft("")(_ + _.toString) + documentHeaderClosingTag
      new ByteArrayInputStream(inputXml.getBytes("utf-8"))
    }

    "return the expected two table names" in {
      val processor = new StaxProcessor(constructXmlDocument(simpleXml, simpleXml2))

      processor.hasNext
      processor.next
      processor.hasNext
      processor.next must equal("table2")
    }

    "return the first row of the table as xml" in {
      val processor = new StaxProcessor(constructXmlDocument(simpleTableWithARow))
      processor.hasNext
      processor.next
      processor.next must equal("<table:table-row table:style-name='ro5'><table:table-cell table:style-name='ce6' calcext:value-type='string'><text:p>4.</text:p></table:table-cell></table:table-row>")
    }

    "return Nothing if xml does not contain table fields and hasNext not been called " in {
      val processor = new StaxProcessor(constructXmlDocument(withoutTableXml))
      processor.next must equal("--NOT-FOUND--")
    }
  }
}
