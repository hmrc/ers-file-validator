/*
 * Copyright 2025 HM Revenue & Customs
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

///*
// * Copyright 2023 HM Revenue & Customs
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package uk.gov.hmrc.validator
//
//import org.apache.pekko.NotUsed
//import org.apache.pekko.actor.ActorSystem
//import org.apache.pekko.stream.connectors.csv.scaladsl.CsvParsing
//import org.apache.pekko.stream.scaladsl.{Flow, Sink, Source}
//import org.apache.pekko.util.ByteString
//import models.upscan.{UploadedSuccessfully, UpscanCsvFilesCallback, UpscanCsvFilesCallbackList}
//import models.{ERSFileProcessingException, RowValidationResults, SheetErrors}
//import org.apache.commons.io.FilenameUtils
//import play.api.Logging
//import play.api.i18n.Messages
//import play.api.mvc.Request
//import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}
//import uk.gov.hmrc.services.validation.DataValidator
//import uk.gov.hmrc.services.validation.models._
//import uk.gov.hmrc.validator.validation.ErsValidator
//import utils.{CsvParserUtil, ERSUtil}
//
//import javax.inject.{Inject, Singleton}
//import scala.annotation.tailrec
//import scala.collection.mutable.ListBuffer
//import scala.concurrent.{ExecutionContext, Future}
//import scala.util.{Failure, Success, Try}
//
//@Singleton
//class ProcessCsvService @Inject()(parserUtil: CsvParserUtil,
//                                  dataGenerator: DataGenerator,
//                                  appConfig: ApplicationConfig,
//                                  sessionCacheService: ErsCheckingFrontendSessionCacheRepository,
//                                  ersUtil: ERSUtil,
//                                  ersValidator: ErsValidator
//                                 )(implicit executionContext: ExecutionContext,
//                                   actorSystem: ActorSystem) extends Logging {
//
//  private val uploadCsvSizeLimit: Int = appConfig.upscanFileSizeLimit
//
//  def extractEntityData(response: HttpResponse): Source[ByteString, _] =
//    response match {
//      case HttpResponse(org.apache.pekko.http.scaladsl.model.StatusCodes.OK, _, entity, _) => entity.withSizeLimit(uploadCsvSizeLimit).dataBytes
//      case notOkResponse =>
//        Source.failed(
//          UpstreamErrorResponse(
//            s"[ProcessCsvService][extractEntityData] Illegal response from Upscan: ${notOkResponse.status.intValue}, body: ${notOkResponse.entity.dataBytes}",
//            notOkResponse.status.intValue))
//    }
//
//  def extractBodyOfRequest: Source[HttpResponse, _] => Source[Either[Throwable, List[ByteString]], _] =
//    _.flatMapConcat(extractEntityData)
//      .via(CsvParsing.lineScanner())
//      .via(Flow.fromFunction(Right(_)))
//      .recover {
//        case e => Left(e)
//      }
//
//  def processFiles(callback: Option[UpscanCsvFilesCallbackList], scheme: String, source: String => Source[HttpResponse, _])(
//    implicit request: Request[_], hc: HeaderCarrier, messages: Messages
//  ): List[Future[Either[Throwable, Boolean]]] =
//    callback.get.files map { file =>
//
//      val successUpload = file.uploadStatus.asInstanceOf[UploadedSuccessfully]
//
//      val validatorFuture: Future[Either[Throwable, DataValidator]] = Source(List(successUpload.name))
//        .via(Flow.fromFunction(checkFileType(_)(messages)))
//        .via(eitherFromFunction(dataGenerator.getSheetCsv(_, scheme)(messages)))
//        .via(eitherFromFunction(dataGenerator.identifyAndDefineSheetCsv(_)(hc, messages)))
//        .via(eitherFromFunction(dataGenerator.setValidatorCsv(_)(hc, messages)))
//        .runWith(Sink.head)
//
//      validatorFuture.flatMap {
//        _.fold(
//          throwable => Future.successful(Left(throwable)),
//          validator => {
//            val futureListOfErrors: Future[Seq[Either[Throwable, RowValidationResults]]] = extractBodyOfRequest(source(successUpload.downloadUrl))
//              .via(eitherFromFunction(processRow(_, successUpload.name, validator)))
//              .runWith(Sink.seq[Either[Throwable, RowValidationResults]])
//
//            futureListOfErrors.map {
//              getRowsWithNumbers(_, successUpload.name)(messages)
//            }.flatMap{
//              case Right(errorsFromRow) => checkValidityOfRows(errorsFromRow, successUpload.name, file)
//              case Left(exception) => Future(Left(exception))
//            }
//          }
//        )
//      }
//    }
//
//  def processRow(rowBytes: List[ByteString], sheetName: String, validator: DataValidator): Either[Throwable, RowValidationResults] = {
//    val rowStrings = rowBytes.map(byteString => byteString.utf8String)
//    val parsedRow = parserUtil.formatDataToValidate(rowStrings, sheetName)
//    val rowIsEmpty = parserUtil.rowIsEmpty(rowStrings)
//
//    Try {
//      validator.validateRow(Row(0, ersValidator.getCells(parsedRow, 0)))
//    } match {
//      case Failure(e) =>
//        logger.warn(e.toString)
//        Left(e)
//      case Success(list) => Right(RowValidationResults(list.getOrElse(List.empty), rowIsEmpty))
//    }
//  }
//
//  def checkFileType(name: String)(implicit messages: Messages): Either[Throwable, String] = if (name.endsWith(".csv")) {
//    Right(FilenameUtils.removeExtension(name))
//  } else {
//    Left(ERSFileProcessingException(
//      Messages("ers_check_csv_file.file_type_error", name)(messages),
//      Messages("ers_check_csv_file.file_type_error", name)(messages)))
//  }
//
//@tailrec
//private[services] final def processDisplayedErrors(errorsLeftToDisplay: Int,
//                                                   rowsWithIndex: Seq[(List[ValidationError], Int)]): Seq[(List[ValidationError], Int)] = {
//  if (errorsLeftToDisplay <= 0) {
//    rowsWithIndex
//  }
//  else {
//    val indexOfFirstOccurrence: Int = rowsWithIndex.indexWhere(errorsWithIndex => errorsWithIndex._1.nonEmpty &&
//      errorsWithIndex._1.exists(validationError => validationError.cell.row == 0))
//    if (indexOfFirstOccurrence != -1) {
//      val listOriginalReference = rowsWithIndex(indexOfFirstOccurrence)._1
//      val entryReplacement = (listOriginalReference.map(validationError => {
//        val cellReplaced = validationError.cell.copy(row = indexOfFirstOccurrence + 1)
//        validationError.copy(cell = cellReplaced)
//      }), indexOfFirstOccurrence)
//      processDisplayedErrors(errorsLeftToDisplay - entryReplacement._1.length, rowsWithIndex.updated(indexOfFirstOccurrence, entryReplacement))
//    } else {
//      rowsWithIndex
//    }
//  }
//}
//
//  def giveRowNumbers(list: Seq[List[ValidationError]]): Seq[List[ValidationError]] = {
//    val maximumNumberOfErrorsToDisplay: Int = appConfig.errorCount
//    processDisplayedErrors(maximumNumberOfErrorsToDisplay, list.zipWithIndex).map(_._1)
//  }
//
//  def getRowsWithNumbers(listOfErrors: Seq[Either[Throwable, RowValidationResults]], name: String)(
//    implicit messages: Messages): Either[Throwable, Seq[List[ValidationError]]] = listOfErrors match {
//    case allEmpty if allEmpty.isEmpty || allEmpty.filter(_.isRight).forall(_.map(_.rowWasEmpty).forall(identity)) =>
//      Left(ERSFileProcessingException(
//        "ers_check_csv_file.noData",
//        messages("ers_check_csv_file.noData", name),
//        needsExtendedInstructions = true,
//        optionalParams = Seq(name)))
//    case nonEmpty =>
//      nonEmpty.find(_.isLeft) match {
//      case Some(Left(issues)) => Left(issues)
//      case _ =>
//        val maybeErrors = nonEmpty.map(_.getOrElse(RowValidationResults(List())).validationErrors)
//        Right(giveRowNumbers(maybeErrors))
//    }
//  }
//
//  def checkValidityOfRows(listOfErrors: Seq[List[ValidationError]], name: String, file: UpscanCsvFilesCallback)(
//    implicit request: Request[_]): Future[Either[Throwable, Boolean]] = {
//    listOfErrors.filter(rowErrors => rowErrors.nonEmpty) match {
//      case allGood if allGood.isEmpty => Future.successful(Right(true))
//      case errors =>
//        val errorsToCache = ListBuffer(parserUtil.getSheetErrors(SheetErrors(FilenameUtils.removeExtension(name), errors.flatten.to(ListBuffer))))
//        for {
//          _ <- sessionCacheService.cache[Long](s"${ersUtil.SCHEME_ERROR_COUNT_CACHE}${file.uploadId.value}", errors.flatten.length)
//          _ <- sessionCacheService.cache[ListBuffer[SheetErrors]](s"${ersUtil.ERROR_LIST_CACHE}${file.uploadId.value}",
//            errorsToCache)
//        } yield Right(false)
//    }
//  }
//}
//
//object FlowOps {
//
//  def eitherFromFunction[A, B](input: A => Either[Throwable, B]): Flow[Either[Throwable, A], Either[Throwable, B], NotUsed] =
//    Flow.fromFunction(_.flatMap(input))
//
//}
