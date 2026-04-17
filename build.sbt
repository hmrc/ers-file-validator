import play.sbt.PlayImport.PlayKeys
import sbt.*
import sbt.Keys.*
import uk.gov.hmrc.*
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin

ThisBuild / majorVersion := 1
ThisBuild / scalaVersion := "2.13.18"

lazy val microservice = Project("ers-file-validator", file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(CodeCoverageSettings())
  .settings(
    libraryDependencies ++= AppDependencies(),
    Test / parallelExecution := false,
    Test / fork := true,
    scalacOptions ++= Seq(
      "-feature",
      "-Wconf:cat=unused-imports&src=routes/.*:s"
    )
  )
  .settings(PlayKeys.playDefaultPort := 9226)

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test")
  .settings(DefaultBuildSettings.itSettings())
  .settings(Test / fork := true)

addCommandAlias("scalafmtAll", "all scalafmtSbt scalafmt Test/scalafmt it/Test/scalafmt")
