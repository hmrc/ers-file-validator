import sbt._

object MicroServiceBuild extends Build with MicroService {
  val appName = "ers-file-validator"
  override lazy val appDependencies: Seq[ModuleID] = AppDependencies()
}

private object AppDependencies {

  import play.core.PlayVersion
  import play.sbt.PlayImport._

  val compile = Seq(
  ws,
  "uk.gov.hmrc" %% "bootstrap-backend-play-26" % "3.1.0",
  "uk.gov.hmrc" %% "domain" % "5.10.0-play-26",
  "uk.gov.hmrc" %% "http-caching-client" % "9.1.0-play-26",
  "uk.gov.hmrc" %% "bulk-entity-streaming" % "1.0.0",
  "uk.gov.hmrc" %% "tabular-data-validator" % "1.0.0",
  "org.scala-lang.modules" %% "scala-xml" % "1.0.6",
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.6",
  "com.codahale.metrics" % "metrics-graphite" % "3.0.2",
  "com.typesafe.play" %% "play-json-joda" % "2.6.10",
  "uk.gov.hmrc" %% "auth-client" % "3.2.0-play-26")

  trait TestDependencies {
    lazy val scope: String = "test"
    lazy val test: Seq[ModuleID] = ???
  }

  private val scalaTestVersion = "3.0.9"
  private val scalaTestPlusPlayVersion = "3.1.3"
  private val pegdownVersion = "1.6.0"
  private val jsoupVersion = "1.13.1"
  private val hmrcTestVersion = "3.3.0"
  private val wiremockVersion = "2.26.3"
  private val cryptoVersion = "5.6.0"
  private val mockitoCoreVerison = "3.3.3"

  object Test {
    def apply(): Seq[ModuleID] = new TestDependencies {
      override lazy val test = Seq(
        "uk.gov.hmrc" %% "hmrctest" % hmrcTestVersion % scope,
        "org.scalatest" %% "scalatest" % scalaTestVersion % scope,
        "org.scalatestplus.play" %% "scalatestplus-play" % scalaTestPlusPlayVersion % scope,
        "org.pegdown" % "pegdown" % pegdownVersion % scope,
        "org.jsoup" % "jsoup" % jsoupVersion % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
        "com.github.tomakehurst" % "wiremock-jre8" % wiremockVersion % scope,
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
        "com.github.tomakehurst" % "wiremock-jre8" % wiremockVersion % scope,
        "uk.gov.hmrc" %% "hmrctest" % hmrcTestVersion % scope
      )
    }.test
  }

  def apply(): Seq[ModuleID] = compile ++ Test() ++ IntegrationTest()
}
