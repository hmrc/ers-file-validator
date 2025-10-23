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

import config.ApplicationConfig
import connectors.ERSFileValidatorConnector
import models._
import models.upscan.UpscanCsvFileData
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.http.scaladsl.model.HttpResponse
import org.apache.pekko.stream.connectors.csv.scaladsl.CsvParsing
import org.apache.pekko.stream.scaladsl.{Flow, Sink, Source}
import org.apache.pekko.util.ByteString
import play.api.Logging
import play.api.mvc.Request
import services.audit.AuditEvents
import services.validation.ErsValidator.getCells
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.services.validation.DataValidator
import uk.gov.hmrc.services.validation.models._
import utils.{ErrorResponseMessages, ValidationUtils}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

@Singleton
class ProcessCsvService @Inject()(auditEvents: AuditEvents,
                                  dataGenerator: DataGenerator,
                                  appConfig: ApplicationConfig,
                                  ersConnector: ERSFileValidatorConnector
                                 )(implicit executionContext: ExecutionContext,
                                   actorSystem: ActorSystem) extends Logging {

  private val uploadCsvSizeLimit: Int = appConfig.uploadFileSizeLimit

  def extractEntityData(response: HttpResponse): Source[ByteString, _] =
    response match {
      case HttpResponse(org.apache.pekko.http.scaladsl.model.StatusCodes.OK, _, entity, _) => entity.withSizeLimit(uploadCsvSizeLimit).dataBytes
      case notOkResponse =>
        logger.error(
          s"[ProcessCsvService][extractEntityData] Illegal response from Upscan: ${notOkResponse.status.intValue}, " +
            s"body: ${notOkResponse.entity.dataBytes}")
        Source.failed(
          ERSFileProcessingException(
            s"${ErrorResponseMessages.fileProcessingServiceFailedStream}",
            s"${ErrorResponseMessages.fileProcessingServiceBulkEntity}"
          )
        )
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
  ): List[Future[Either[ErsError, CsvFileSubmissions]]] =
    callback.callbackData map { successUpload =>

      val sheetName = successUpload.name.replace(".csv", "")

      dataGenerator.getValidatorAndSheetInfo(sheetName, callback.schemeInfo) match {

        case Right(validatorAndSheetInfo) =>

          val (validator, sheetInfo) = validatorAndSheetInfo

          val futureListOfErrors: Future[Seq[Either[Throwable, Seq[String]]]] =
            extractBodyOfRequest(source(successUpload.downloadUrl))
              .via(
                Flow.fromFunction((maybeRow: Either[Throwable, List[ByteString]]) =>
                  maybeRow.flatMap(row => processRow(row, successUpload.name, callback.schemeInfo, validator, sheetInfo))
                )
              )
              .takeWhile(_.isRight, inclusive = true)
              .runWith(Sink.seq[Either[Throwable, Seq[String]]])

          /* Because of the .takeWhile function above, if any row had an error in it, the 'creation' or futureListOfErrors will terminate.
             Since .takeWhile is set to be inclusive, if any row had an error in it, it will be the last object in the list.
             For example: if given an input of 3 rows, (valid, valid, valid), futureListOfErrors will be a List(Right, Right, Right).
             If given an input of 3 rows, (valid, invalid, valid), futureListOfErrors will be a List(Right, Left).
           */
          futureListOfErrors.map { sequenceOfEithers =>
            sequenceOfEithers.lastOption match {
              case None => Left(NoDataError(
                s"${ErrorResponseMessages.ersCheckCsvFileNoData(sheetName + ".csv")}",
                s"${ErrorResponseMessages.ersCheckCsvFileNoData()}"))
              case Some(lastRowValidation) => lastRowValidation match {
                case Right(_) => Right(CsvFileSubmissions(sheetName, sequenceOfEithers.length, successUpload))
                case Left(userError: UserValidationError) => Left(userError)
                case Left(exception: Throwable) => Left(ErsSystemError(exception.getMessage, s"Error processing CSV file: ${successUpload.name}"))
              }
            }
          }

        case Left(error) => Future.successful(Left(error))
      }
    }

  def extractSchemeData(schemeInfo: SchemeInfo, empRef: String, result: Either[ErsError, CsvFileSubmissions])(
    implicit request: Request[_], hc: HeaderCarrier
  ): Future[Either[ErsError, CsvFileLengthInfo]] = {
    result.fold(
      error => Future.successful(Left(error)),
      csvFileSubmissions => {
        logger.info("[ProcessCsvService][extractSchemeData]: File length " + csvFileSubmissions.fileLength)
        sendSchemeCsv(SubmissionsSchemeData(schemeInfo, csvFileSubmissions.sheetName, csvFileSubmissions.upscanCallback, csvFileSubmissions.fileLength), empRef)
          .map {
            case Some(throwable) =>
              Left(ERSFileProcessingException(throwable.getMessage, "Error during CSV submission processing"))
            case None =>
              val noOfSlices: Int = ValidationUtils.numberOfSlices(csvFileSubmissions.fileLength, appConfig.maxNumberOfRowsPerSubmission)
              Right(CsvFileLengthInfo(noOfSlices, csvFileSubmissions.fileLength))
          }
      }
    )
  }

  def formatDataToValidate(rowData: Seq[String], sheetInfo: SheetInfo): Seq[String] = {
    rowData.take(sheetInfo.headerRow.length)
  }

  def processRow(rowBytes: List[ByteString], sheetName: String, schemeInfo: SchemeInfo, validator: DataValidator, sheetInfo: SheetInfo)(
    implicit request: Request[_], hc: HeaderCarrier): Either[ErsError, Seq[String]] = {
    val rowStrings: Seq[String] = rowBytes.map(byteString => byteString.utf8String)
    val parsedRow = formatDataToValidate(rowStrings, sheetInfo)
    Try {
      validator.validateRow(Row(0, getCells(parsedRow, 0)))
    } match {
      case Failure(exception) =>
        logger.error(s"[ProcessCsvService][processRow] Validation failed: ${exception.getMessage}", exception)
        Left(RowValidationError("Invalid file format", s"Could not validate row due to unexpected structure. Error: ${exception.getMessage}", None))
      case Success(list) if list.isEmpty => Right(parsedRow)
      case Success(Some(errors)) =>
        val errorDetails = errors.map {
          case ValidationError(cell, _, errorId, errorMsg) => s"column - ${cell.column}, error - $errorId : $errorMsg"}
        logger.warn(s"Validation errors found for ${schemeInfo.schemeRef} : ${errorDetails.mkString(" | " )}" )
        auditEvents.fileProcessingErrorAudit(schemeInfo, sheetName, "Failure to validate")
        Left(RowValidationError(
          s"${ErrorResponseMessages.dataParserFileInvalid}",
          s"${ErrorResponseMessages.dataParserValidationFailure}",
          None))
      case Success(_) =>
        auditEvents.fileProcessingErrorAudit(schemeInfo, sheetName, "Failure to validate")
        logger.warn("[ProcessCsvService][processRow] Row validation found validation errors")
        Left(RowValidationError(
          s"${ErrorResponseMessages.dataParserFileInvalid}",
          s"${ErrorResponseMessages.dataParserValidationFailure}",
          None))
    }
  }

  def sendSchemeCsv(ersSchemeData: SubmissionsSchemeData, empRef: String)(implicit hc: HeaderCarrier, request: Request[_]): Future[Option[Throwable]] = {
    logger.debug("[ProcessCsvService][sendSchemeDataCsv] Sheetdata sending to ers-submission " + ersSchemeData.sheetName)
    ersConnector.sendToSubmissionsNew(ersSchemeData, empRef).map {
      case Right(_) =>
        auditEvents.fileValidatorAudit(ersSchemeData.schemeInfo, ersSchemeData.sheetName)
        None
      case Left(ex: Throwable) =>
        auditEvents.auditRunTimeError(ex, ex.getMessage, ersSchemeData.schemeInfo, ersSchemeData.sheetName)
        logger.error(s"[ProcessCsvService][sendSchemeDataCsv] Exception found when sending to submissions: ${ex.getMessage}", ex)
        Some(ERSFileProcessingException(ex.toString, ex.getStackTrace.toString))
    }
  }
}