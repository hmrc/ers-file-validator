import uk.gov.hmrc.*
import DefaultBuildSettings.*
import play.sbt.PlayImport.PlayKeys
import play.sbt.routes.RoutesKeys.{InjectedRoutesGenerator, routesGenerator}
import sbt.Keys.*
import sbt.*
import scoverage.ScoverageKeys
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin
import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion


val appName: String = "ers-file-validator"

lazy val appDependencies: Seq[ModuleID] = AppDependencies.all
lazy val plugins: Seq[Plugins] = Seq(play.sbt.PlayScala, SbtDistributablesPlugin)

lazy val scoverageSettings = {
  Seq(
    ScoverageKeys.coverageExcludedPackages := "<empty>;app.*;config.*;testOnlyDoNotUseInAppConf.*;views.*;uk.gov.hmrc.*;prod.*;models.*;services.ERSRequest",
    ScoverageKeys.coverageMinimumStmtTotal := 92,
    ScoverageKeys.coverageFailOnMinimum := false,
    ScoverageKeys.coverageHighlighting := true,
    Test / parallelExecution := false
  )
}

lazy val microservice = Project(appName, file("."))
  .enablePlugins(plugins *)
  .settings(scoverageSettings *)
  .settings(scalaSettings *)
  .settings(defaultSettings() *)
  .settings(
    scalaVersion := "2.13.10",
    libraryDependencies ++= appDependencies,
    Test / parallelExecution := false,
    Test / fork := true,
    retrieveManaged := true,
    routesGenerator := InjectedRoutesGenerator
  )
  .settings(resolvers += Resolver.jcenterRepo)
  .settings(majorVersion := 1)
  .settings(PlayKeys.playDefaultPort := 9226)

scalacOptions ++= Seq(
  "-Wconf:src=routes/.*:s"
)

libraryDependencySchemes ++= Seq("org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always)

addCommandAlias("scalastyleAll", "all scalastyle test:scalastyle")
