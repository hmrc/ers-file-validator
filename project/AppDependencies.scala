import play.sbt.PlayImport.ws
import sbt.*

object AppDependencies {

  val bootstrapVersion  = "8.5.0"
  val pekkoVersion      = "1.0.2"
  val mongoVersion      = "1.8.0"

  val compile: Seq[ModuleID] = Seq(
    ws,
    "org.scala-lang.modules" %% "scala-xml"                  % "2.2.0",
    "uk.gov.hmrc"            %% "bootstrap-backend-play-30"  % bootstrapVersion,
    "uk.gov.hmrc"            %% "domain-play-30"             % "9.0.0",
    "uk.gov.hmrc"            %% "tabular-data-validator"     % "1.8.0",
    "commons-codec"           % "commons-codec"              % "1.16.1",
    "uk.gov.hmrc.mongo"      %% "hmrc-mongo-play-30"         % mongoVersion,
    "org.apache.pekko"       %% "pekko-connectors-csv"       % pekkoVersion
  )

  val test: Seq[ModuleID] = Seq(
    "org.apache.pekko"        %% "pekko-connectors-csv"     % pekkoVersion,
    "org.apache.pekko"        %% "pekko-connectors-xml"     % pekkoVersion,
    "org.apache.pekko"        %% "pekko-testkit"            % pekkoVersion,
    "org.apache.pekko"        %% "pekko-stream"             % pekkoVersion,
    "com.vladsch.flexmark"    %  "flexmark-all"             % "0.64.8",
    "org.jsoup"               %  "jsoup"                    % "1.17.2",
    "uk.gov.hmrc"             %% "bootstrap-test-play-30"   % bootstrapVersion,
    "org.scalatest"           %% "scalatest"                % "3.2.18",
    "org.scalatestplus"       %% "mockito-4-11"             % "3.2.18.0",
  ).map(_ % "test")

  def apply(): Seq[ModuleID] = compile ++ test
}
