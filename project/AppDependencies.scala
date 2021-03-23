import sbt._
import play.core.PlayVersion
import play.sbt.PlayImport._
import uk.gov.hmrc._

object AppDependencies {

  val silencerVersion = "1.7.1"

  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc" %% "bootstrap-backend-play-26" % "3.2.0",
    "uk.gov.hmrc" %% "domain" % "5.10.0-play-26",
    "uk.gov.hmrc" %% "http-caching-client" % "9.2.0-play-26",
    "uk.gov.hmrc" % "bulk-entity-streaming_2.11" % "1.0.0",
    "uk.gov.hmrc" %% "tabular-data-validator" % "0.1.0-SNAPSHOT",
    "org.scala-lang.modules" %% "scala-xml" % "1.0.6",
    "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.6",
    "com.typesafe.play" %% "play-json-joda" % "2.6.10",
    "uk.gov.hmrc" %% "auth-client" % "3.2.0-play-26",
    "com.lightbend.akka" %% "akka-stream-alpakka-csv" % "2.0.2",
    "com.typesafe.akka" %% "akka-stream" % "2.6.12",
    "com.typesafe.akka" %% "akka-slf4j" % "2.6.12",
    "com.typesafe.akka" %% "akka-protobuf" % "2.6.12",
    "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.12",
      compilerPlugin("com.github.ghik" % "silencer-plugin" % silencerVersion cross CrossVersion.full),
    "com.github.ghik" % "silencer-lib" % silencerVersion % Provided cross CrossVersion.full
  )

  trait TestDependencies {
    lazy val scope: String = "test"
    lazy val test: Seq[ModuleID] = ???
  }

  object Test {
    def apply(): Seq[ModuleID] = new TestDependencies {
      override lazy val test: Seq[ModuleID] = Seq(
        "uk.gov.hmrc" %% "hmrctest" % "3.10.0-play-26" % scope,
        "org.scalatest" %% "scalatest" % "3.0.9" % scope,
        "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.3" % scope,
        "org.pegdown" % "pegdown" % "1.6.0" % scope,
        "org.jsoup" % "jsoup" % "1.13.1" % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
        "com.github.tomakehurst" % "wiremock-jre8" % "2.26.3" % scope,
        "uk.gov.hmrc" %% "crypto" % "5.6.0",
        "org.mockito" % "mockito-core" % "3.3.3" % scope,
        "com.typesafe.akka" %% "akka-testkit" % "2.6.12" % scope

      )
    }.test
  }

  object IntegrationTest {
    def apply(): Seq[ModuleID] = new TestDependencies {
      override lazy val scope: String = "it"
      override lazy val test: Seq[ModuleID] = Seq(
        "uk.gov.hmrc" %% "hmrctest" % "3.3.0" % scope,
        "org.scalatest" %% "scalatest" % "3.0.9" % scope,
        "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.3" % scope,
        "org.pegdown" % "pegdown" % "1.6.0" % scope,
        "org.jsoup" % "jsoup" % "1.13.1" % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
        "com.github.tomakehurst" % "wiremock-jre8" % "2.26.3" % scope
      )
    }.test
  }

  def apply(): Seq[ModuleID] = compile ++ Test() ++ IntegrationTest()

}

object TestPhases {

  val allPhases = "tt->test;test->test;test->compile;compile->compile"
  val allItPhases = "tit->it;it->it;it->compile;compile->compile"

  lazy val TemplateTest = config("tt") extend Test
  lazy val TemplateItTest = config("tit") extend IntegrationTest

}
