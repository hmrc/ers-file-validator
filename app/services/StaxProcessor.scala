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

package services

import services.StaxProcessor.notFoundString

import java.io.InputStream
import javax.xml.stream.events.XMLEvent
import javax.xml.stream.{XMLEventReader, XMLInputFactory}
import scala.annotation.tailrec

class StaxProcessor(inputStream: InputStream) extends Iterator[String] {

  val xif : XMLInputFactory = XMLInputFactory.newDefaultFactory();
  xif.setProperty(XMLInputFactory.SUPPORT_DTD, false)
  xif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false)
  xif.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false)
  xif.setProperty(XMLInputFactory.IS_VALIDATING, false)

  private val eventReader: XMLEventReader = xif.createXMLEventReader(inputStream)

  private val tableXmlElements: Seq[String] = Seq("table:table", "table:table-row")

  @tailrec
  private def checkForTableRow(xmlEvent: XMLEvent): Option[XMLEvent] = {
    if (xmlEvent.isStartElement && tableXmlElements
      .contains(xmlEvent.asStartElement().getName.getLocalPart)) {
      Some(xmlEvent)
    }
    else {
      if (eventReader.hasNext) {
        checkForTableRow(eventReader.nextEvent())
      }
      else {
        None
      }
    }
  }

  override def hasNext: Boolean = eventReader.hasNext

  override def next(): String = {
    val nextValue: Option[XMLEvent] = checkForTableRow(eventReader.nextEvent())
    nextValue match {
      case Some(xmlElement: XMLEvent) =>
        if (xmlElement.asStartElement().getName.getLocalPart == "table:table-row") {
          getStringToEndElement(Seq(xmlElement), "table:table-row").mkString
        }
        else {
          getName(xmlElement.toString)
        }
      case None => notFoundString
    }
  }

  def getName(message: String) : String = {
    val sheetNameRegEx = "(table:name=)\\'(\\w+)\\'".r
    sheetNameRegEx.findFirstMatchIn(message).map(_ group 2).getOrElse(notFoundString)
  }

  def foundelement(event: XMLEvent, elementName: String): Boolean =
    event.isEndElement && event.asEndElement().getName.getLocalPart == elementName

  @tailrec
  private def getStringToEndElement(xmlElements: Seq[XMLEvent], endElement: String): Seq[XMLEvent] = {
    if (eventReader.hasNext){
      val nextEvent = eventReader.nextEvent()
      val updatedXmlElements: Seq[XMLEvent] = xmlElements.appended(nextEvent)
      if (foundelement(nextEvent, endElement)){
        updatedXmlElements
      }
      else {
        getStringToEndElement(updatedXmlElements, endElement)
      }
    }
    else {
      Seq.empty[XMLEvent]
    }
  }
}

object StaxProcessor{
  val notFoundString: String = "--NOT-FOUND--"
}
