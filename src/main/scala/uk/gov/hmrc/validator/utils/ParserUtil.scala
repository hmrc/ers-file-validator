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

package uk.gov.hmrc.validator.utils

import play.api.Logging
import play.api.mvc.Request
import uk.gov.hmrc.validator.ERSTemplatesInfo
import uk.gov.hmrc.validator.models.SheetErrors

import javax.inject.{Inject, Singleton}
import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

object ParserUtil {

  def formatDataToValidate(rowData: Seq[String], sheetName: String): Seq[String] = {
    val sheetColSize = ERSTemplatesInfo.ersSheetsWithCsopV5(sheetName.replace(".csv", "")).headerRow.size
    if (rowData.size < sheetColSize) {
//      logger.debug(s"Difference between amount of columns ${rowData.size} and amount of headers $sheetColSize")
      val additionalEmptyCells: Seq[String] = Seq.fill(sheetColSize - rowData.size)("")
      (rowData ++ additionalEmptyCells).take(sheetColSize)
    }
    else {
      rowData.take(sheetColSize)
    }
  }

  def isFileValid(errorList: ListBuffer[SheetErrors]): Boolean = { // TODO: COME BACK TO
    isValid(errorList)
//    if (isValid(errorList)) {
//      Future.successful(Success(true))
//    }
//    else {
//      val updatedErrorCount = getTotalErrorCount(errorList)
//      val updatedErrorList = getSheetErrors(errorList)
//      val id = if (file.isDefined) file.get.uploadId else ""
//
//      val result = for {
//        _ <- sessionCacheService.cache[Long](s"${ersUtil.SCHEME_ERROR_COUNT_CACHE}$id", updatedErrorCount)
//        _ <- sessionCacheService.cache[ListBuffer[SheetErrors]](s"${ersUtil.ERROR_LIST_CACHE}$id", updatedErrorList)
//      } yield Success(false)
//
//      result recover {
//        case ex: Exception => Failure(ex)
//      }
//    }
  }

  def isValid(schemeErrors: ListBuffer[SheetErrors]): Boolean = {
    schemeErrors.map(_.errors.isEmpty).forall(identity)
  }

  def getTotalErrorCount(schemeErrors: ListBuffer[SheetErrors]): Long = {
    schemeErrors.map(_.errors.length).sum
  }

  def getSheetErrors(schemeErrors: ListBuffer[SheetErrors]): ListBuffer[SheetErrors] = {
    schemeErrors.map { schemeError =>
//      SheetErrors(schemeError.sheetName, schemeError.errors.take(appConfig.errorCount)) // TODO: Checl errorCount
      SheetErrors(schemeError.sheetName, schemeError.errors.take(0))
    }
  }
}
