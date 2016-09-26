import sbt._

object MicroServiceBuild extends Build with MicroService {
  import scala.util.Properties.envOrElse

  val appName = "ers-file-validator"

  override lazy val appDependencies: Seq[ModuleID] = AppDependencies()
}


private object AppDependencies {
  import play.PlayImport._
  import play.core.PlayVersion

  private val playHealthVersion = "1.1.0"
  private val playMicroServiceBootStrapVersion = "4.4.0"

  private val playUiVersion = "4.2.0"
  private val playConfigVersion = "2.0.1"
  private val metricsPlayVersion = "0.2.1"
  private val metricsGraphiteVersion = "3.0.2"
  private val domainVersion = "3.5.0"
  private val httpCachingVersion = "5.2.0"
  private val ersEntityStreamingVersion = "1.0.0"
  private val playAuthorisationVersion = "3.3.0"
  private val reactivemongoTestVersion = "1.2.0"

  val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "microservice-bootstrap" % playMicroServiceBootStrapVersion,
    "uk.gov.hmrc" %% "play-health" % playHealthVersion,
    "uk.gov.hmrc" %% "play-config" % playConfigVersion,
    "uk.gov.hmrc" %% "play-authorisation" % playAuthorisationVersion,
    "uk.gov.hmrc" %% "domain" % domainVersion,
    "uk.gov.hmrc" %% "http-caching-client" % httpCachingVersion,
    "com.kenshoo" %% "metrics-play" % metricsPlayVersion,
    "com.codahale.metrics" % "metrics-graphite" % metricsGraphiteVersion,
    "uk.gov.hmrc" %% "bulk-entity-streaming" % ersEntityStreamingVersion,
    "uk.gov.hmrc" %% "tabular-data-validator" % "1.0.0",
    "org.scala-lang.modules" %% "scala-xml" % "1.0.5",
    "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.3",
  "uk.gov.hmrc" %% "play-url-binders" % "1.0.0",
  "uk.gov.hmrc" %% "play-config" % "2.0.0",
  "uk.gov.hmrc" %% "play-json-logger" % "2.1.1",
  "uk.gov.hmrc" %% "domain" % "3.1.0" )

  trait TestDependencies {
    lazy val scope: String = "test"
    lazy val test : Seq[ModuleID] = ???
  }

  private val scalatestVersion = "2.2.5"
  private val scalatestPlusPlayVersion = "1.2.0"
  private val pegdownVersion = "1.6.0"
  private val jsoupVersion = "1.8.3"

  object Test {
    def apply() = new TestDependencies {
      override lazy val test = Seq(
        "org.scalatest" %% "scalatest" % scalatestVersion % scope,
        "org.scalatestplus" %% "play" % scalatestPlusPlayVersion % scope,
        "org.pegdown" % "pegdown" % pegdownVersion % scope,
        "org.jsoup" % "jsoup" % jsoupVersion % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
        "com.github.tomakehurst" % "wiremock" % "1.57" % scope,
        "uk.gov.hmrc" %% "crypto" % "3.0.0"
      )
    }.test
  }

  private val hmrcTestVersion = "1.6.0"

  object IntegrationTest {
    def apply() = new TestDependencies {

      override lazy val scope: String = "it"

      override lazy val test = Seq(
        "org.scalatest" %% "scalatest" % scalatestVersion % scope,
        "org.scalatestplus" %% "play" % scalatestPlusPlayVersion % scope,
        "org.pegdown" % "pegdown" % pegdownVersion % scope,
        "org.jsoup" % "jsoup" % jsoupVersion % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
        "com.github.tomakehurst" % "wiremock" % "1.57" % scope,
        "uk.gov.hmrc" %% "hmrctest" % hmrcTestVersion % scope
      )
    }.test
  }

  def apply() = compile ++ Test() ++ IntegrationTest()
}
