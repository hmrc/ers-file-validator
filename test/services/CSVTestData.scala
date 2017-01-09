/*
 * Copyright 2017 HM Revenue & Customs
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

trait CSVTestData {

  val emiAdjustmentsCSV:String = "no,no,yes,3,2015-12-09,John,Barry,Doe,AB123456C,123/XZ55555555,10.1234,100.12,10.1234,10.1234"
  val emiAdjustmentsCollection:String = "no,no,yes,3,2015-12-09,John,Barry,Doe,AB123456C,123/XZ55555555,10.1234,100.12,10.1234,10.1234"
  val emiAdjustmentsOptionalEnd:String = "no,no,yes,3,2015-12-09,John,Barry,Doe,AB123456C,123/XZ55555555"
  val emiAdjustmentsTooLong:String = "no,no,yes,3,2015-12-09,John,Barry,Doe,AB123456C,123/XZ55555555,10.1234,100.12,10.1234,10.1234,,,,,"

  val csopOptionsExercisedCSV:String = "2015-07-23,Joe,Jim,Smith,AB123456C,123/CD1234,2015-08-29,123.12,yes,no,12.1234,12.1234,12.1234,yes,AB12345678,yes,yes,12.1234,yes,yes"
  val csopOptionsExercisedCollection:String = "2015-07-23,Joe,Jim,Smith,AB123456C,123/CD1234,2015-08-29,123.12,yes,no,12.1234,12.1234,12.1234,yes,AB12345678,yes,yes,12.1234,yes,yes"

  val csvList:List[String] = List(
    emiAdjustmentsCSV,
    csopOptionsExercisedCSV
  )

  val expectedDataList:List[String]= List(
    emiAdjustmentsCollection,
    csopOptionsExercisedCollection
  )

}
