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
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.services.validation.DataValidator
import uk.gov.hmrc.services.validation.models.ValidationError
import uk.gov.hmrc.validator.CsvValidator.{setValidatorAndValidateCsvRow, validateCsvRow}
import uk.gov.hmrc.validator.models.RowValidationResults
import uk.gov.hmrc.validator.DataGenerator
import utils.{ErrorResponseMessages, ValidationUtils}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ProcessCsvService @Inject()(auditEvents: AuditEvents,
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

  def extractBodyOfRequestAndValidate(csopV5Enabled: Boolean, sheetName: String): Source[HttpResponse, _] => Source[Either[Throwable, RowValidationResults], _] =
    _.flatMapConcat(extractEntityData)
      .via(CsvParsing.lineScanner())
      .via(Flow.fromFunction(setValidatorAndValidateCsvRow(csopV5Enabled, _, sheetName)))
      .via(Flow.fromFunction(Right(_)))
      .map(_.flatMap(identity))
      .takeWhile(_.map(_.validationErrors.isEmpty).getOrElse(true), inclusive = true)
      .recover {
        case e => Left(e)
      }

  def stripExtension(name: String): String =
    name.lastIndexOf('.') match {
        case -1 => name
        case i  => name.substring(0, i)
    }

  def processFiles(callback: UpscanCsvFileData, source: String => Source[HttpResponse, _]): Seq[Future[Either[ErsError, CsvFileSubmissions]]] =
    callback.callbackData map { successUpload =>
      val sheetName = stripExtension(successUpload.name)
      val futureListOfErrors: Future[Seq[Either[Throwable, RowValidationResults]]] =
        extractBodyOfRequestAndValidate(appConfig.csopV5Enabled, sheetName)(
          source(successUpload.downloadUrl)
        )
          .runWith(Sink.seq[Either[Throwable, RowValidationResults]])

      /* Because of the .takeWhile function in extractBodyOfRequest method, if any row had an error in it, the 'creation' or futureListOfErrors will terminate.
         Since .takeWhile is set to be inclusive, if any row had an error in it, it will be the last object in the list.
         For example: if given an input of 3 rows, (valid, valid, valid), futureListOfErrors will be a List(Right, Right, Right).
         If given an input of 3 rows, (valid, invalid, valid), futureListOfErrors will be a List(Right, Left).
       */
      futureListOfErrors.map { sequenceOfEithers =>
        sequenceOfEithers.lastOption match {
          case None => Left(NoDataError(
            s"${ErrorResponseMessages.ersCheckCsvFileNoData(sheetName + ".csv")}",
            s"${ErrorResponseMessages.ersCheckCsvFileNoData()}"))
          case Some(lastRowValidation: Either[Throwable, RowValidationResults]) => lastRowValidation match {
            case Right(results) if results.validationErrors.isEmpty =>
              Right(CsvFileSubmissions(sheetName, sequenceOfEithers.length, successUpload))
            case Right(results: RowValidationResults) if results.validationErrors.nonEmpty =>
              val errorsToLog: String = results
                .validationErrors
                .map((error: ValidationError) => s"column - ${error.cell.column}, error - ${error.errorId} : ${error.errorMsg}")
                .mkString("\n")
              Left(
                RowValidationError(
                  message = s"[ProcessCSVService][processFiles]: Found validation errors in CSV",
                  context = s"Error processing CSV file: ${successUpload.name}, errors: $errorsToLog",
                  rowNumber = None
                )
              )
            case Left(userError: UserValidationError) =>
              Left(userError)
            case Left(exception: Throwable) =>
              Left(ErsSystemError(exception.getMessage, s"Error processing CSV file: ${successUpload.name}"))
          }
        }
      }
    }

  def extractSchemeData(schemeInfo: SchemeInfo, empRef: String, result: Either[Throwable, CsvFileSubmissions])(
    implicit request: Request[_], hc: HeaderCarrier
  ): Future[Either[ErsError, CsvFileLengthInfo]] = {
    result.fold(
      error => Future.successful(
        Left(
          ErsSystemError(error.getMessage, "[ProcessCsvService][extractSchemeData]: Error processing CSV file")
        )
      ),
      (csvFileSubmissions: CsvFileSubmissions) => {
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