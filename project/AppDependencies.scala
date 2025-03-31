import play.sbt.PlayImport.*
import sbt.*

object AppDependencies {
  private val bootstrapPlayVersion = "9.11.0"
  private val hmrcFrontendPlayVersion = "12.0.0"
  private val hmrcMongoPlayVersion = "2.6.0"

  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc"             %% "bootstrap-frontend-play-30"             % bootstrapPlayVersion,
    "uk.gov.hmrc"             %% "play-frontend-hmrc-play-30"             % hmrcFrontendPlayVersion,
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-play-30"                     % hmrcMongoPlayVersion,
    "uk.gov.hmrc"             %% "play-conditional-form-mapping-play-30"  % "3.2.0",
    "com.github.tototoshi"    %% "scala-csv"                              % "2.0.0",
    "net.ruippeixotog"        %% "scala-scraper"                          % "3.1.3",
    "uk.gov.hmrc.objectstore" %% "object-store-client-play-30"            % "2.1.0"
  ).map(_.withSources())

  def test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"       %% "bootstrap-test-play-30"   % bootstrapPlayVersion  % Test,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-test-play-30"  % hmrcMongoPlayVersion  % Test,
    "org.jsoup"          % "jsoup"                    % "1.19.1"              % Test,
  )

  def it: Seq[sbt.ModuleID] = Seq(
    "com.fasterxml.jackson.core"     % "jackson-databind"     % "2.18.3" % Test,
    "com.fasterxml.jackson.module"  %% "jackson-module-scala" % "2.18.3" % Test
  )

}
