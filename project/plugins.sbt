resolvers += MavenRepository("HMRC-open-artefacts-maven2", "https://open.artefacts.tax.service.gov.uk/maven2")
resolvers += Resolver.url("HMRC-open-artefacts-ivy2", url("https://open.artefacts.tax.service.gov.uk/ivy2"))(Resolver.ivyStylePatterns)

ThisBuild / libraryDependencySchemes ++= Seq(
  "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
)

addSbtPlugin("uk.gov.hmrc"          % "sbt-auto-build"        % "3.20.0")
addSbtPlugin("com.github.gseitz"    % "sbt-release"           % "1.0.10")
addSbtPlugin("com.typesafe.play"    % "sbt-plugin"            % "2.8.21")
addSbtPlugin("org.scoverage"        % "sbt-scoverage"         % "2.0.9")
addSbtPlugin("org.scalastyle"       % "scalastyle-sbt-plugin" % "1.0.0")
addSbtPlugin("uk.gov.hmrc"          % "sbt-distributables"    % "2.4.0")
addSbtPlugin("com.timushev.sbt"     % "sbt-updates"           % "0.6.4")
