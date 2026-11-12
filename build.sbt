import play.sbt.PlayImport.PlayKeys
import sbt.*
import sbt.Keys.*
import uk.gov.hmrc.*
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin
import uk.gov.hmrc.DefaultBuildSettings.targetJvm

ThisBuild / majorVersion := 2
ThisBuild / scalaVersion := "3.9.0"
ThisBuild / targetJvm := "jvm-21"

lazy val microservice = Project("ers-file-validator", file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(CodeCoverageSettings())
  .settings(
    libraryDependencies ++= AppDependencies(),
    scalacOptions ++= Seq(
      "-feature",
      "-Wconf:msg=unused import&src=routes/.*:s"
    )
  )
  .settings(PlayKeys.playDefaultPort := 9226)

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test")
  .settings(DefaultBuildSettings.itSettings())
  .settings(Test / fork := true)

addCommandAlias("scalafmtAll", "all scalafmtSbt scalafmt Test/scalafmt it/Test/scalafmt")
