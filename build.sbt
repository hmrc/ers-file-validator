import uk.gov.hmrc.*
import DefaultBuildSettings.*
import play.sbt.PlayImport.PlayKeys
import play.sbt.routes.RoutesKeys.{InjectedRoutesGenerator, routesGenerator}
import sbt.Keys.*
import sbt.*
import scoverage.ScoverageKeys
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin
import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion

lazy val IntegrationTest = config("it") extend Test

val appName: String = "ers-file-validator"

lazy val plugins: Seq[Plugins] = Seq(play.sbt.PlayScala, SbtDistributablesPlugin)

lazy val scoverageSettings = {
  Seq(
    ScoverageKeys.coverageExcludedPackages := "<empty>;app.*;config.*;testOnlyDoNotUseInAppConf.*;views.*;uk.gov.hmrc.*;prod.*;models.*;services.ERSRequest",
    ScoverageKeys.coverageMinimumStmtTotal := 94,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true,
    Test / parallelExecution := false
  )
}

lazy val microservice = Project(appName, file("."))
  .enablePlugins(plugins *)
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(scoverageSettings *)
  .settings(scalaSettings *)
  .settings(defaultSettings() *)
  .settings(
    scalaVersion := "2.13.12",
    libraryDependencies ++= AppDependencies(),
    Test / parallelExecution := false,
    Test / fork := true,
    retrieveManaged := true,
    routesGenerator := InjectedRoutesGenerator
  )
  .settings(resolvers += Resolver.jcenterRepo)
  .settings(majorVersion := 1)
  .settings(PlayKeys.playDefaultPort := 9226)
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(itSettings))


    scalacOptions ++= Seq(
  "-Wconf:src=routes/.*:s"
)

lazy val itSettings = integrationTestSettings() ++ Seq(
  unmanagedSourceDirectories   := Seq(
    baseDirectory.value / "it"
  ),
  parallelExecution            := false,
  fork                         := true
)

libraryDependencySchemes ++= Seq("org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always)

addCommandAlias("scalastyleAll", "all scalastyle Test/scalastyle")
