import sbt._
import play.core.PlayVersion
import play.sbt.PlayImport._

object AppDependencies {
  
  lazy val scope: String = "test"

  val silencerVersion = "1.7.1"
  val bootstrapVersion = "5.24.0"
  val akkaVersion = "2.6.19"

  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc" %% "bootstrap-backend-play-28" % bootstrapVersion,
    "uk.gov.hmrc" %% "domain" % "8.1.0-play-28",
    "uk.gov.hmrc" %% "http-caching-client" % "9.6.0-play-28",
    "uk.gov.hmrc" %% "tabular-data-validator" % "1.4.0",
    "org.scala-lang.modules" %% "scala-xml" % "1.3.0",
    "org.scala-lang.modules" %% "scala-parser-combinators" % "1.1.2",
    "com.typesafe.play" %% "play-json-joda" % "2.9.2",
    "com.lightbend.akka" %% "akka-stream-alpakka-csv" % "3.0.3",
    "com.typesafe.akka" %% "akka-stream" % akkaVersion,
    "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
    "com.typesafe.akka" %% "akka-protobuf" % akkaVersion,
    "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
    "com.typesafe.akka" %% "akka-serialization-jackson" % akkaVersion,
    "com.typesafe.akka" %% "akka-http-spray-json" % "10.2.6",
      compilerPlugin("com.github.ghik" % "silencer-plugin" % silencerVersion cross CrossVersion.full),
    "com.github.ghik" % "silencer-lib" % silencerVersion % Provided cross CrossVersion.full
  )
  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% "bootstrap-test-play-28" % bootstrapVersion % scope,
    "org.jsoup" % "jsoup" % "1.14.3" % scope,
    "org.scalatestplus" %% "mockito-3-4" % "3.2.9.0" % scope,
    "com.typesafe.akka" %% "akka-testkit" % akkaVersion % scope
  )

  val all: Seq[ModuleID] = compile ++ test
}