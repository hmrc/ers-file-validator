import sbt._
import play.core.PlayVersion
import play.sbt.PlayImport._

object AppDependencies {

  val silencerVersion = "1.7.1"

  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc" %% "bootstrap-backend-play-27" % "5.3.0",
    "uk.gov.hmrc" %% "domain" % "5.11.0-play-27",
    "uk.gov.hmrc" %% "http-caching-client" % "9.5.0-play-27",
    "uk.gov.hmrc" %% "tabular-data-validator" % "1.4.0",
    "org.scala-lang.modules" %% "scala-xml" % "1.0.6",
    "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.7",
    "com.typesafe.play" %% "play-json-joda" % "2.6.14",
    "com.lightbend.akka" %% "akka-stream-alpakka-csv" % "2.0.2",
    "com.typesafe.akka" %% "akka-stream" % "2.6.14",
    "com.typesafe.akka" %% "akka-slf4j" % "2.6.14",
    "com.typesafe.akka" %% "akka-protobuf" % "2.6.14",
    "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.14",
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
        "org.scalatest" %% "scalatest" % "3.0.9" % scope,
        "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.3" % scope,
        "org.pegdown" % "pegdown" % "1.6.0" % scope,
        "org.jsoup" % "jsoup" % "1.13.1" % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
        "com.github.tomakehurst" % "wiremock-jre8" % "2.28.0" % scope,
        "uk.gov.hmrc" %% "crypto" % "5.6.0",
        "org.mockito" % "mockito-core" % "3.3.3" % scope,
        "com.typesafe.akka" %% "akka-testkit" % "2.6.14" % scope

      )
    }.test
  }

  object IntegrationTest {
    def apply(): Seq[ModuleID] = new TestDependencies {
      override lazy val scope: String = "it"
      override lazy val test: Seq[ModuleID] = Seq(
        "org.scalatest" %% "scalatest" % "3.0.9" % scope,
        "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.3" % scope,
        "org.pegdown" % "pegdown" % "1.6.0" % scope,
        "org.jsoup" % "jsoup" % "1.13.1" % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
        "com.github.tomakehurst" % "wiremock-jre8" % "2.28.0" % scope
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
