import play.sbt.PlayImport.ws
import sbt.*

object AppDependencies {

  private val bootstrapVersion = "10.7.0"
  private val pekkoVersion     = "1.0.2"
  private val mongoVersion     = "2.12.0"

  private val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc"       %% "bootstrap-backend-play-30" % bootstrapVersion,
    "uk.gov.hmrc"       %% "domain-play-30"            % "11.0.0",
    "commons-codec"      % "commons-codec"             % "1.21.0",
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"        % mongoVersion,
    "org.apache.pekko"  %% "pekko-connectors-csv"      % pekkoVersion,
    "uk.gov.hmrc"       %% "ers-file-validator-config" % "0.13.0",
    "uk.gov.hmrc"       %% "bootstrap-backend-play-30" % bootstrapVersion,
    "uk.gov.hmrc"       %% "domain-play-30"            % "11.0.0",
    "commons-codec"      % "commons-codec"             % "1.19.0",
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"        % mongoVersion,
    "org.apache.pekko"  %% "pekko-connectors-csv"      % pekkoVersion,
    "uk.gov.hmrc"       %% "ers-file-validator-config" % "1.0.0"
  )

  private val test: Seq[ModuleID] = Seq(
    "org.apache.pekko" %% "pekko-connectors-xml"   % pekkoVersion,
    "org.apache.pekko" %% "pekko-testkit"          % "1.0.3",
    "org.apache.pekko" %% "pekko-stream"           % pekkoVersion,
    "uk.gov.hmrc"      %% "bootstrap-test-play-30" % bootstrapVersion
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ test

}
