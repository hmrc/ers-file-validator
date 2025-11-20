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

package uk.gov.hmrc.validator

import java.io.InputStream

import javax.xml.stream.events.XMLEvent
import javax.xml.stream.{XMLEventReader, XMLInputFactory}

class StaxProcessor(inputStream: InputStream) extends Iterator[String] {

  val xif : XMLInputFactory = XMLInputFactory.newInstance()
  xif.setProperty(XMLInputFactory.SUPPORT_DTD, false)
  xif.setProperty("javax.xml.stream.isSupportingExternalEntities", false)
  xif.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false)
  xif.setProperty(XMLInputFactory.IS_VALIDATING, false)

  val eventReader: XMLEventReader = xif.createXMLEventReader(inputStream)

  override def hasNext: Boolean = {
    while (eventReader.hasNext) {
      val xmlevent = eventReader.peek()
      if (xmlevent.isStartElement) {
        val name = xmlevent.asStartElement().getName.getLocalPart
        if (name == "table:table" || name == "table:table-row") {
          return true
        } else {
          eventReader.nextEvent()
        }
      } else {
          eventReader.nextEvent()
      }
    }
    false
  }

  override def next(): String = {
    val nextValue = eventReader.nextEvent()
    if(nextValue.isStartElement)
      if(nextValue.asStartElement().getName.getLocalPart == "table:table-row") {
        val a = getStringToEndElement("table:table-row")
        val b = nextValue.toString
        return b + a
      }
      else {
        return getName(nextValue.toString)
      }

    "--NOT-FOUND--"
  }

   def getName(message : String) : String = {
     val sheetNameRegEx = "(table:name=)\\'(\\w+)\\'".r
     sheetNameRegEx.findFirstMatchIn(message).map(_ group 2).getOrElse("--NOT-FOUND--")
   }

  def getStringToEndElement(endelement: String): String =
  {
    val buffer: StringBuilder = new StringBuilder

    def foundElement(event: XMLEvent, elementName: String): Boolean = {
      if(event.isEndElement) {
        event.asEndElement().getName.getLocalPart == elementName
      } else {
        false
      }
    }

    while(eventReader.hasNext)
      {
        val theNextEvent = eventReader.nextEvent()

        buffer.append(theNextEvent.toString)
        if(foundElement(theNextEvent, endelement)) {
          return buffer.toString()
        }
      }

    buffer.toString()
  }
}
