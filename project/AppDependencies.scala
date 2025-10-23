import play.sbt.PlayImport.ws
import sbt.*

object AppDependencies {

  val bootstrapVersion  = "9.19.0"
  val pekkoVersion      = "1.0.2"
  val mongoVersion      = "2.10.0"

  val compile: Seq[ModuleID] = Seq(
    ws,
    "org.scala-lang.modules" %% "scala-xml"                  % "2.4.0",
    "uk.gov.hmrc"            %% "bootstrap-backend-play-30"  % bootstrapVersion,
    "uk.gov.hmrc"            %% "domain-play-30"             % "11.0.0",
    "uk.gov.hmrc"            %% "tabular-data-validator"     % "1.9.0",
    "commons-codec"           % "commons-codec"              % "1.19.0",
    "uk.gov.hmrc.mongo"      %% "hmrc-mongo-play-30"         % mongoVersion,
    "org.apache.pekko"       %% "pekko-connectors-csv"       % pekkoVersion
  )

  val test: Seq[ModuleID] = Seq(
    "org.apache.pekko"        %% "pekko-connectors-csv"     % pekkoVersion,
    "org.apache.pekko"        %% "pekko-connectors-xml"     % pekkoVersion,
    "org.apache.pekko"        %% "pekko-testkit"            % "1.0.3",
    "org.apache.pekko"        %% "pekko-stream"             % pekkoVersion,
    "com.vladsch.flexmark"    %  "flexmark-all"             % "0.64.8",
    "org.jsoup"               %  "jsoup"                    % "1.21.1",
    "uk.gov.hmrc"             %% "bootstrap-test-play-30"   % bootstrapVersion,
    "org.scalatest"           %% "scalatest"                % "3.2.19",
    "org.scalatestplus"       %% "mockito-4-11"             % "3.2.18.0",
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ test
}
