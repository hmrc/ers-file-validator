//import uk.gov.hmrc.*
//import uk.gov.hmrc.DefaultBuildSettings.{defaultSettings, scalaSettings}
////import DefaultBuildSettings.*
//import play.sbt.PlayImport.PlayKeys
//import play.sbt.routes.RoutesKeys.{InjectedRoutesGenerator, routesGenerator}
//import sbt.Keys.*
//import sbt.*
//import scoverage.ScoverageKeys
////import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin
//import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion
//
//lazy val IntegrationTest = config("it") extend Test
//
//val appName: String = "ers-file-validator"
//
//ThisBuild / majorVersion := 1
//ThisBuild / version := "1.0.0"
//ThisBuild / scalaVersion := "2.13.16"
//
//lazy val scoverageSettings = {
//  Seq(
//    ScoverageKeys.coverageExcludedPackages := "<empty>;app.*;config.*;testOnlyDoNotUseInAppConf.*;views.*;uk.gov.hmrc.*;prod.*;models.*;services.ERSRequest",
//    ScoverageKeys.coverageMinimumStmtTotal := 94,
//    ScoverageKeys.coverageFailOnMinimum := true,
//    ScoverageKeys.coverageHighlighting := true,
//    Test / parallelExecution := false
//  )
//}
//
////lazy val microservice = Project(appName, file("."))
////  .enablePlugins(plugins *)
////  .disablePlugins(JUnitXmlReportPlugin)
////  .settings(scoverageSettings *)
//////  .settings(scalaSettings *)
//////  .settings(defaultSettings() *)
////  .settings(
////    libraryDependencies ++= AppDependencies(),
////    Test / parallelExecution := false,
////    Test / fork := true,
////    retrieveManaged := true,
////    routesGenerator := InjectedRoutesGenerator
////  )
////  .settings(PlayKeys.playDefaultPort := 9226)
////  .settings(
////    publishTo := Some(Resolver.defaultLocal)
////  )
////    scalacOptions ++= Seq(
////      "-Wconf:cat=unused-imports&src=routes/.*:s"
////)
//
//scalaSettings
//defaultSettings()
//publishTo := Some(Resolver.defaultLocal)
//libraryDependencies ++= AppDependencies()
//
//Test / testOptions += Tests.Argument("-oD","-u", "target/test-reports", "-h", "target/test-reports/html-report")
//
////lazy val it = project
////  .enablePlugins(PlayScala)
////  .dependsOn(microservice % "test->test")
//////  .settings(DefaultBuildSettings.itSettings())
////  .settings(Test / fork := true)


import sbt.*
import uk.gov.hmrc.DefaultBuildSettings.{defaultSettings, scalaSettings}

val appName: String = "ers-file-validator"

lazy val project = Project(appName, file("."))

libraryDependencies ++= AppDependencies()
majorVersion := 1

scalaSettings
defaultSettings()
publishTo := Some(Resolver.defaultLocal)
scalaVersion := "2.13.16"

Test / testOptions += Tests.Argument("-oD","-u", "target/test-reports", "-h", "target/test-reports/html-report")
