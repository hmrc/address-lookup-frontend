import play.sbt.PlayImport.*
import sbt.*

object AppDependencies {
  private val bootstrapPlayVersion = "8.1.0"

  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc" %% "bootstrap-frontend-play-30" % bootstrapPlayVersion,
    "uk.gov.hmrc" %% "play-frontend-hmrc-play-30" % "8.2.0",
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30" % "1.6.0",
    "uk.gov.hmrc" %% "play-conditional-form-mapping-play-30" % "2.0.0",
    "com.github.tototoshi" %% "scala-csv" % "1.3.10"
  ).map(_.withSources())

  def test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% "bootstrap-test-play-30" % bootstrapPlayVersion % Test,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-test-play-30" % "1.6.0" % Test,
    "org.jsoup" % "jsoup" % "1.15.4" % Test,
  )

  def it: Seq[sbt.ModuleID] = Seq(
    "com.fasterxml.jackson.core" % "jackson-databind" % "2.15.1" % Test,
    "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.15.1" % Test
  )

}
