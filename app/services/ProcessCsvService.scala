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

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpResponse
import akka.stream.alpakka.csv.scaladsl.CsvParsing
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.util.ByteString
import config.ApplicationConfig
import connectors.ERSFileValidatorConnector
import models.upscan.UpscanCsvFileData
import models.{CsvFileContents, ERSFileProcessingException, SchemeData, SchemeInfo}
import play.api.Logger
import play.api.mvc.Request
import services.ERSTemplatesInfo.ersSheets
import services.FlowOps.eitherFromFunction
import services.audit.AuditEvents
import services.validation.ErsValidator.getCells
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}
import uk.gov.hmrc.services.validation.DataValidator
import uk.gov.hmrc.services.validation.models._
import utils.{ErrorResponseMessages, ValidationUtils}

import javax.inject.{Inject, Singleton}
import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

@Singleton
class ProcessCsvService @Inject()(auditEvents: AuditEvents,
                                  dataGenerator: DataGenerator,
                                  appConfig: ApplicationConfig,
                                  ersConnector: ERSFileValidatorConnector
                                 )(implicit executionContext: ExecutionContext,
                                   actorSystem: ActorSystem) {

  private val uploadCsvSizeLimit: Int = appConfig.uploadCsvSizeLimit
  private[services] val ersSheetsClone: Map[String, SheetInfo] = ersSheets

  def extractEntityData(response: HttpResponse): Source[ByteString, _] =
    response match {
      case HttpResponse(akka.http.scaladsl.model.StatusCodes.OK, _, entity, _) => entity.withSizeLimit(uploadCsvSizeLimit).dataBytes
      case notOkResponse =>
        Logger.error(
          s"[ProcessCsvService][extractEntityData] Illegal response from Upscan: ${notOkResponse.status.intValue}, " +
            s"body: ${notOkResponse.entity.dataBytes}")
        Source.failed(
          ERSFileProcessingException(
            s"${ErrorResponseMessages.fileProcessingServiceFailedStream}",
            s"${ErrorResponseMessages.fileProcessingServiceBulkEntity}"))
    }

  def extractBodyOfRequest: Source[HttpResponse, _] => Source[Either[Throwable, List[ByteString]], _] =
    _.flatMapConcat(extractEntityData)
      .via(CsvParsing.lineScanner())
      .via(Flow.fromFunction(Right(_)))
      .recover {
        case e => Left(e)
      }

  def processFiles(callback: UpscanCsvFileData, source: String => Source[HttpResponse, _])(
    implicit request: Request[_], hc: HeaderCarrier
  ): List[Future[Either[Throwable, CsvFileContents]]] =
    callback.callbackData map { successUpload =>

      val sheetName = successUpload.name.replace(".csv", "")
      val tryValidator: Either[Throwable, DataValidator] = dataGenerator.setValidatorCsv(sheetName, callback.schemeInfo)

      tryValidator.fold(
        throwable => Future.successful(Left(throwable)),
        validator => {
          val futureListOfErrors: Future[Seq[Either[Throwable, Seq[String]]]] = extractBodyOfRequest(source(successUpload.downloadUrl))
            .via(eitherFromFunction(processRow(_, successUpload.name, callback.schemeInfo, validator)))
            .runWith(Sink.seq[Either[Throwable, Seq[String]]])

          futureListOfErrors.map { sequenceOfEithers =>
            sequenceOfEithers.collectFirst { case Left(x) => x } match {
              case Some(anError) => Left(anError)
              case None => Right(CsvFileContents(sheetName, sequenceOfEithers.map(_.right.get)))
            }
          }
        }
      )
    }

  def extractSchemeData(schemeInfo: SchemeInfo, empRef: String, result: Either[Throwable, CsvFileContents])(
    implicit request: Request[_], hc: HeaderCarrier
  ): Future[Either[Throwable, (Int, Int)]] = {

    result.fold(
      throwable => Future(Left(throwable)),
      csvFileContents => {
        val schemeData: SchemeData = SchemeData(schemeInfo, csvFileContents.sheetName, None, csvFileContents.contents.to[ListBuffer])
        Logger.info("2.1 result contains: " + csvFileContents)
        Logger.debug("No if SchemeData Objects " + csvFileContents.contents.size)
        sendSchemeCsv(schemeData, empRef).map { issues =>
          issues.fold(
            throwable => Left(throwable),
            noOfSlices => Right(noOfSlices, schemeData.data.size)
          )
        }
      }
    )
  }

  def getSheetCsv(sheetName: String, schemeInfo: SchemeInfo)(
    implicit hc: HeaderCarrier, request: Request[_]): Either[Throwable, SheetInfo] = {
    Logger.info(s"[DataGenerator][getSheetCsv] Looking for sheetName: $sheetName")
    ersSheetsClone.get(sheetName) match {
      case Some(sheetInfo) => Right(sheetInfo)
      case _ =>
        auditEvents.fileProcessingErrorAudit(schemeInfo, sheetName, "Could not set the validator")
        Logger.warn("[DataGenerator][getSheetCsv] Couldn't identify SheetName")
        Left(ERSFileProcessingException(
          s"${ErrorResponseMessages.dataParserIncorrectSheetName}",
          s"${ErrorResponseMessages.dataParserUnidentifiableSheetName(sheetName)}"))
    }
  }

  def formatDataToValidate(rowData: Seq[String], sheetName: String, schemeInfo: SchemeInfo)(
    implicit hc: HeaderCarrier, request: Request[_]
  ): Either[Throwable, Seq[String]] = {
    getSheetCsv(sheetName.replace(".csv", ""), schemeInfo) match {
      case Left(throwable) => Left(throwable)
      case Right(sheetInfo) =>
        val sheetColSize = sheetInfo.headerRow.length
        Right(rowData.take(sheetColSize))
    }
  }

  def processRow(rowBytes: List[ByteString], sheetName: String, schemeInfo: SchemeInfo, validator: DataValidator)(
    implicit request: Request[_], hc: HeaderCarrier
  ): Either[Throwable, Seq[String]] = {
    val rowStrings: Seq[String] = rowBytes.map(byteString => byteString.utf8String)
    formatDataToValidate(rowStrings, sheetName, schemeInfo) match {
      case Left(throwable) => Left(throwable)
      case Right(parsedRow) =>
        Try {
          validator.validateRow(Row(0, getCells(parsedRow, 0)))
        } match {
          case Failure(exception) =>
            Logger.error(s"[ProcessCsvService][processRow] Exception returned when attempting to validate row: ${exception.getMessage}")
            Left(exception)
          case Success(list) if list.isEmpty => Right(parsedRow)
          case Success(_) =>
            auditEvents.fileProcessingErrorAudit(schemeInfo, sheetName, "Failure to validate")
            Logger.warn("[ProcessCsvService][processRow] Row validation found validation errors")
            Left(ERSFileProcessingException(
              s"${ErrorResponseMessages.dataParserFileInvalid}",
              s"${ErrorResponseMessages.dataParserValidationFailure}"))
        }
    }
  }

  def sendSchemeDataCsv(ersSchemeData: SchemeData, empRef: String)(implicit hc: HeaderCarrier, request: Request[_]): Future[Option[Throwable]] = {
    Logger.debug("[ProcessCsvService][sendSchemeDataCsv] Sheetdata sending to ers-submission " + ersSchemeData.sheetName)
    ersConnector.sendToSubmissions(ersSchemeData, empRef).map {
      case Right(_) =>
        auditEvents.fileValidatorAudit(ersSchemeData.schemeInfo, ersSchemeData.sheetName)
        None
      case Left(ex) =>
        auditEvents.auditRunTimeError(ex, ex.getMessage, ersSchemeData.schemeInfo, ersSchemeData.sheetName)
        Logger.error(s"[ProcessCsvService][sendSchemeDataCsv] Exception found when sending to submissions: ${ex.getMessage}")
        Some(ERSFileProcessingException(ex.toString, ex.getStackTrace.toString))
    }
  }

  def sendSchemeCsv(schemeData: SchemeData, empRef: String)(implicit hc: HeaderCarrier, request: Request[_]): Future[Either[Throwable, Int]] = {
    val splitSchemes: Boolean = appConfig.splitLargeSchemes
    val maxNumberOfRows: Int = appConfig.maxNumberOfRowsPerSubmission
    if (splitSchemes && (schemeData.data.size > maxNumberOfRows)) {

      val slices: Int = ValidationUtils.numberOfSlices(schemeData.data.size, maxNumberOfRows)

      val sendSchemeDataCsvResults: Seq[Future[Option[Throwable]]] = (0 until slices * maxNumberOfRows by maxNumberOfRows).map { number =>
        val scheme = new SchemeData(schemeData.schemeInfo, schemeData.sheetName, Option(slices),
          schemeData.data.slice(number, number + maxNumberOfRows))
        Logger.debug("[ProcessCsvService][sendSchemeCsv] The size of the scheme data is " + scheme.data.size + " and number is " + number)
        sendSchemeDataCsv(scheme, empRef)
      }

      Future.sequence(sendSchemeDataCsvResults).map { failedSubmissions =>
        failedSubmissions.collectFirst {
          case Some(throwable) => throwable
        } match {
          case Some(throwable) => Left(throwable)
          case _ => Right(slices)
        }
      }
    }
    else {
      sendSchemeDataCsv(schemeData, empRef).map {
        case None => Right(1)
        case Some(exception) => Left(exception)
      }
    }
  }

}


object FlowOps {

  def eitherFromFunction[A, B](input: A => Either[Throwable, B]): Flow[Either[Throwable, A], Either[Throwable, B], NotUsed] =
    Flow.fromFunction(_.flatMap(input))

}