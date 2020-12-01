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

package metrics

import java.util.concurrent.TimeUnit

import com.codahale.metrics.MetricRegistry

trait ERSMetrics {
  def fileProcessingTimer(diff: Long, unit: TimeUnit): Unit

  def besTimer(diff: Long, unit: TimeUnit): Unit

  def sendToSubmissionsTimer(diff: Long, unit: TimeUnit): Unit

  def dataIteratorTimer(diff: Long, unit: TimeUnit): Unit
}

object ERSMetrics extends ERSMetrics {
  val registry = new MetricRegistry

  override def fileProcessingTimer(diff: Long, unit: TimeUnit): Unit = registry.timer("file-processing-time").update(diff, unit)

  override def besTimer(diff: Long, unit: TimeUnit): Unit = registry.timer("bes-processing-time").update(diff, unit)

  override def sendToSubmissionsTimer(diff: Long, unit: TimeUnit): Unit = registry.timer("send-to-submissions-time").update(diff, unit)

  override def dataIteratorTimer(diff: Long, unit: TimeUnit): Unit = registry.timer("data-iterator-time").update(diff, unit)
}

trait Metrics {
  val metrics: ERSMetrics = ERSMetrics
}
