import play.sbt.PlayImport.ws
import sbt._

object AppDependencies {

  val silencerVersion = "1.7.12"
  val bootstrapVersion = "7.13.0"
  val akkaVersion = "2.6.20"

  val compile: Seq[ModuleID] = Seq(
    ws,
    "com.lightbend.akka"     %% "akka-stream-alpakka-csv"    % "3.0.4",
    "com.typesafe.play"      %% "play-json-joda"             % "2.9.4",
    "com.typesafe.akka"      %% "akka-stream"                % akkaVersion,
    "com.typesafe.akka"      %% "akka-slf4j"                 % akkaVersion,
    "com.typesafe.akka"      %% "akka-protobuf"              % akkaVersion,
    "com.typesafe.akka"      %% "akka-actor-typed"           % akkaVersion,
    "com.typesafe.akka"      %% "akka-serialization-jackson" % akkaVersion,
    "com.typesafe.akka"      %% "akka-http-spray-json"       % "10.2.10",
    "org.scala-lang.modules" %% "scala-xml"                  % "1.3.0",
    "uk.gov.hmrc"            %% "bootstrap-backend-play-28"  % bootstrapVersion,
    "uk.gov.hmrc"            %% "domain"                     % "8.1.0-play-28",
    "uk.gov.hmrc"            %% "http-caching-client"        % "10.0.0-play-28",
    "uk.gov.hmrc"            %% "tabular-data-validator"     % "1.5.0",
      compilerPlugin("com.github.ghik" % "silencer-plugin" % silencerVersion cross CrossVersion.full),
    "com.github.ghik" % "silencer-lib" % silencerVersion % Provided cross CrossVersion.full
  )
  val test: Seq[ModuleID] = Seq(
    "com.typesafe.akka"     %% "akka-testkit"             % akkaVersion,
    "com.vladsch.flexmark"  %  "flexmark-all"             % "0.62.2",
    "org.jsoup"             %  "jsoup"                    % "1.15.3",
    "org.scalatestplus"     %% "mockito-3-4"              % "3.2.10.0",
    "uk.gov.hmrc"           %% "bootstrap-test-play-28"   % bootstrapVersion
  ).map(_ % "test")

  val all: Seq[ModuleID] = compile ++ test
  
}