/*
 * Copyright 2016 HM Revenue & Customs
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

import com.kenshoo.play.metrics.MetricsRegistry

import java.util.concurrent.TimeUnit

trait ERSMetrics {
  def fileProcessingTimer(diff: Long, unit: TimeUnit): Unit
  def besTimer(diff: Long, unit: TimeUnit): Unit
  def sendToSubmissionsTimer(diff: Long, unit: TimeUnit): Unit
  def dataIteratorTimer(diff: Long, unit: TimeUnit): Unit
}

object ERSMetrics extends ERSMetrics {
  override def fileProcessingTimer(diff: Long, unit: TimeUnit) = MetricsRegistry.defaultRegistry.timer("file-processing-time").update(diff, unit)
  override def besTimer(diff: Long, unit: TimeUnit) = MetricsRegistry.defaultRegistry.timer("bes-processing-time").update(diff, unit)
  override def sendToSubmissionsTimer(diff: Long, unit: TimeUnit) = MetricsRegistry.defaultRegistry.timer("send-to-submissions-time").update(diff, unit)
  override def dataIteratorTimer(diff: Long, unit: TimeUnit) = MetricsRegistry.defaultRegistry.timer("data-iterator-time").update(diff, unit)
}

trait Metrics {
  val metrics:ERSMetrics = ERSMetrics
}
