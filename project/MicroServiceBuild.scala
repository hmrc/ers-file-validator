import sbt._

object MicroServiceBuild extends Build with MicroService {
  val appName = "ers-file-validator"
  override lazy val appDependencies: Seq[ModuleID] = AppDependencies()
}

private object AppDependencies {

  import play.core.PlayVersion
  import play.sbt.PlayImport._


  private val playMicroServiceBootStrapVersion = "6.18.0"
  private val metricsGraphiteVersion = "3.0.2"
  private val httpCachingVersion = "7.0.0"
  private val bulkEntityStreamingVersion = "1.0.0"
  private val domainVersion = "5.0.0"
  private val tabularDataValidatorVersion = "1.0.0"
  private val scalaXmlVersion = "1.0.6"
  private val scalaParserCombinatorsVersion = "1.0.6"
  private val scalaTestVersion = "2.2.6"
  private val scalaTestPlusPlayVersion = "1.5.1"
  private val pegdownVersion = "1.6.0"
  private val jsoupVersion = "1.10.3"
  private val hmrcTestVersion = "2.3.0"
  private val wiremockVersion = "1.58"
  private val cryptoVersion = "4.4.0"
  private val mockitoCoreVerison = "1.10.19"


  val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "microservice-bootstrap" % playMicroServiceBootStrapVersion,
    "uk.gov.hmrc" %% "domain" % domainVersion,
    "uk.gov.hmrc" %% "http-caching-client" % httpCachingVersion,
    "uk.gov.hmrc" %% "bulk-entity-streaming" % bulkEntityStreamingVersion,
    "uk.gov.hmrc" %% "tabular-data-validator" % tabularDataValidatorVersion,
    "org.scala-lang.modules" %% "scala-xml" % scalaXmlVersion,
    "org.scala-lang.modules" %% "scala-parser-combinators" % scalaParserCombinatorsVersion,
    "com.codahale.metrics" % "metrics-graphite" % metricsGraphiteVersion)

  trait TestDependencies {
    lazy val scope: String = "test"
    lazy val test: Seq[ModuleID] = ???
  }

  object Test {
    def apply(): Seq[ModuleID] = new TestDependencies {
      override lazy val test = Seq(
        "org.scalatest" %% "scalatest" % scalaTestVersion % scope,
        "org.scalatestplus.play" %% "scalatestplus-play" % scalaTestPlusPlayVersion % scope,
        "org.pegdown" % "pegdown" % pegdownVersion % scope,
        "org.jsoup" % "jsoup" % jsoupVersion % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
        "com.github.tomakehurst" % "wiremock" % wiremockVersion % scope,
        "uk.gov.hmrc" %% "crypto" % cryptoVersion,
        "org.mockito" % "mockito-core" % mockitoCoreVerison % scope
      )
    }.test
  }

  object IntegrationTest {
    def apply(): Seq[ModuleID] = new TestDependencies {
      override lazy val scope: String = "it"
      override lazy val test = Seq(
        "org.scalatest" %% "scalatest" % scalaTestVersion % scope,
        "org.scalatestplus.play" %% "scalatestplus-play" % scalaTestPlusPlayVersion % scope,
        "org.pegdown" % "pegdown" % pegdownVersion % scope,
        "org.jsoup" % "jsoup" % jsoupVersion % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
        "com.github.tomakehurst" % "wiremock" % wiremockVersion % scope,
        "uk.gov.hmrc" %% "hmrctest" % hmrcTestVersion % scope
      )
    }.test
  }

  def apply(): Seq[ModuleID] = compile ++ Test() ++ IntegrationTest()
}
