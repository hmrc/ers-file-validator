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

package services

import javax.xml.parsers.SAXParserFactory
import models._
import play.api.Logger
import utils.ErrorResponseMessages

import scala.util.Try
import scala.xml._

trait DataParser {

  val repeatColumnsAttr = "table:number-columns-repeated"
  val repeatTableAttr = "table:number-rows-repeated"

  def secureSAXParser = {
    val saxParserFactory = SAXParserFactory.newInstance()
    saxParserFactory.setFeature("http://xml.org/sax/features/external-general-entities", false)
    saxParserFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
    saxParserFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
    saxParserFactory.newSAXParser()
  }

  def parse(row: String): Either[String, (Seq[String], Int)] = {
    Logger.debug("DataParser: Parse: About to parse row: " + row)
    val xmlRow = Try(Option(XML.withSAXParser(secureSAXParser)loadString(row))).getOrElse(None)
    //    Logger.debug("DataParser: Parse: About to match xmlRow: " + xmlRow)
    xmlRow match {
      case None => Logger.debug("3.1 Parse row left "); Left(row)
      case elem: Option[Elem] => Logger.debug("3.2 Parse row right ")
        val cols = Try(Right(xmlRow.get.child.flatMap(parseColumn(_)))).getOrElse {
          Logger.warn(s"${ErrorResponseMessages.dataParserFileRetrievalFailed}");
          throw ERSFileProcessingException(
            s"${ErrorResponseMessages.dataParserFileRetrievalFailed}",
            s"${ErrorResponseMessages.dataParserParserFailure}")
        }

        cols match {
          case Right(r: Seq[String]) if !isBlankRow(r) => Right((r, repeated(xmlRow)))
          case Right(s: Seq[String]) => Right((s, 1))
        }
      case _ => {
        Logger.warn(s"${ErrorResponseMessages.dataParserFileParsingError}")
        throw ERSFileProcessingException(
          s"${ErrorResponseMessages.dataParserFileParsingError}",
          s"${ErrorResponseMessages.dataParserParsingOfFileData}")
      }
    }
  }

  def repeated(xmlRow: Option[Elem]): Int = {
    val rowsRepeated = xmlRow.get.attributes.asAttrMap.get(repeatTableAttr)
    if (rowsRepeated.isDefined) {
      rowsRepeated.get.toInt
    }
    else {
      1
    }
  }

  def parseColumn(col: scala.xml.Node): Seq[String] = {
    val colsRepeated = col.attributes.asAttrMap.get(repeatColumnsAttr)

    if (colsRepeated.nonEmpty && colsRepeated.get.toInt < 50) {
      val cols: scala.collection.mutable.MutableList[String] = scala.collection.mutable.MutableList()
      for (i <- 1 to colsRepeated.get.toInt) cols += col.text
      cols.toSeq
    }
    else Seq(col.text)
  }

  def isBlankRow(data: Seq[String]) = data.mkString("").trim.length == 0

}