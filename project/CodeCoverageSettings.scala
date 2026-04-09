import sbt.Setting
import scoverage.ScoverageKeys.*

object CodeCoverageSettings {

  private val excludedPackages: Seq[String] = Seq("<empty>", ".*Routes.*")

  private val settings: Seq[Setting[_]] = Seq(
    coverageExcludedPackages := excludedPackages.mkString(";"),
    coverageMinimumStmtTotal := 100,
    coverageFailOnMinimum := true,
    coverageHighlighting := true
  )

  def apply(): Seq[Setting[_]] = settings

}
