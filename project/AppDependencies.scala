import play.sbt.PlayImport.ws
import sbt.*

object AppDependencies {

  val bootstrapVersion  = "8.5.0"
//  val akkaVersion       = "2.6.21" //Current 'bobby rule' not to upgrade past 2.6.21
  val mongoVersion      = "1.7.0"

  val compile: Seq[ModuleID] = Seq(
    ws,
//    "com.lightbend.akka"     %% "akka-stream-alpakka-csv"    % "4.0.0",
//    "com.typesafe.akka"      %% "akka-stream"                % akkaVersion,
//    "com.typesafe.akka"      %% "akka-slf4j"                 % akkaVersion,
//    "com.typesafe.akka"      %% "akka-protobuf"              % akkaVersion,
//    "com.typesafe.akka"      %% "akka-actor-typed"           % akkaVersion,
//    "com.typesafe.akka"      %% "akka-serialization-jackson" % akkaVersion,
//    "com.typesafe.akka"      %% "akka-http-spray-json"       % "10.2.10",
    "org.scala-lang.modules" %% "scala-xml"                  % "2.2.0",
    "uk.gov.hmrc"            %% "bootstrap-backend-play-30"  % bootstrapVersion,
    "uk.gov.hmrc"            %% "domain-play-30"             % "9.0.0",
    "uk.gov.hmrc"            %% "tabular-data-validator"     % "1.8.0",
    "commons-codec"           % "commons-codec"              % "1.16.1",
    "uk.gov.hmrc.mongo"      %% "hmrc-mongo-play-30"         % mongoVersion,
    "org.apache.pekko"       %% "pekko-connectors-csv"       % "1.0.2"
  )

  val test: Seq[ModuleID] = Seq(
//    "com.typesafe.akka"       %% "akka-testkit"             % akkaVersion,
    "org.apache.pekko"       %% "pekko-connectors-csv"      % "1.0.2",
    "org.apache.pekko"       %% "pekko-connectors-xml"      % "1.0.2",
    "org.apache.pekko"       %% "pekko-testkit"             % "1.0.2",
    "org.apache.pekko"        %% "pekko-stream"             % "1.0.2",
    "com.vladsch.flexmark"    %  "flexmark-all"             % "0.64.8",
    "org.jsoup"               %  "jsoup"                    % "1.17.2",
    "uk.gov.hmrc"             %% "bootstrap-test-play-30"   % bootstrapVersion,
    "org.scalatest"           %% "scalatest"                % "3.2.18",
    "org.scalatestplus"       %% "mockito-4-11"             % "3.2.18.0",
  ).map(_ % "test")

  def apply(): Seq[ModuleID] = compile ++ test
}
