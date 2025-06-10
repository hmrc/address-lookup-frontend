import play.sbt.PlayImport.*
import sbt.*

object AppDependencies {
  private val bootstrapPlayVersion = "9.13.0"
  private val hmrcFrontendPlayVersion = "12.5.0"
  private val hmrcMongoPlayVersion = "2.6.0"

  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc"             %% "bootstrap-frontend-play-30"             % bootstrapPlayVersion,
    "uk.gov.hmrc"             %% "play-frontend-hmrc-play-30"             % hmrcFrontendPlayVersion,
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-play-30"                     % hmrcMongoPlayVersion,
    "uk.gov.hmrc"             %% "play-conditional-form-mapping-play-30"  % "3.3.0",
    "com.github.tototoshi"    %% "scala-csv"                              % "2.0.0",
    "net.ruippeixotog"        %% "scala-scraper"                          % "3.2.0",
    "uk.gov.hmrc.objectstore" %% "object-store-client-play-30"            % "2.2.0"
  ).map(_.withSources())

  def test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"       %% "bootstrap-test-play-30"   % bootstrapPlayVersion  % Test,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-test-play-30"  % hmrcMongoPlayVersion  % Test,
    "org.jsoup"          % "jsoup"                    % "1.20.1"              % Test,
  )

  def it: Seq[sbt.ModuleID] = Seq(
    "com.fasterxml.jackson.core"     % "jackson-databind"     % "2.19.0" % Test,
    "com.fasterxml.jackson.module"  %% "jackson-module-scala" % "2.19.0" % Test
  )

}
