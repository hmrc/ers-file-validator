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

package fixtures

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.classic.{Level, Logger => LogbackLogger}
import ch.qos.logback.core.read.ListAppender
import org.scalatest.{BeforeAndAfterEach, TestSuite}
import org.slf4j.LoggerFactory

import scala.collection.mutable
import scala.jdk.CollectionConverters._

trait LogCapturePerTest extends BeforeAndAfterEach { this: TestSuite =>

  private val logAppender     = new ListAppender[ILoggingEvent]()
  private val attachedLoggers = mutable.ListBuffer.empty[LogbackLogger]

  def logCaptureTargets: Seq[AnyRef] = Seq.empty

  protected def attachLogger(target: AnyRef): Unit = {
    val logger = LoggerFactory.getLogger(target.getClass).asInstanceOf[LogbackLogger]
    logger.setLevel(Level.DEBUG)
    logger.addAppender(logAppender)
    attachedLoggers += logger
  }

  def capturedLogs: List[ILoggingEvent] = logAppender.list.asScala.toList

  override def beforeEach(): Unit = {
    logAppender.list.clear()
    attachedLoggers.clear()
    logAppender.start()
    logCaptureTargets.foreach(attachLogger)
    super.beforeEach()
  }

  override def afterEach(): Unit = {
    attachedLoggers.foreach(_.detachAppender(logAppender))
    attachedLoggers.clear()
    logAppender.stop()
    super.afterEach()
  }

  def logExistsContaining(atLevel: Level, message: String): Boolean = capturedLogs.exists { event =>
    event.getLevel == atLevel &&
    event.getFormattedMessage.contains(message)
  }

}
