import play.sbt.PlayImport.*
import sbt.*

object AppDependencies {

  private val bootstrapPlayVersion = "10.4.0"
  private val hmrcFrontendPlayVersion = "12.24.0"
  private val hmrcMongoPlayVersion = "2.11.0"
  private val jacksonVersion = "3.0.2"

  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc"             %% "bootstrap-frontend-play-30"             % bootstrapPlayVersion,
    "uk.gov.hmrc"             %% "play-frontend-hmrc-play-30"             % hmrcFrontendPlayVersion,
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-play-30"                     % hmrcMongoPlayVersion,
    "uk.gov.hmrc"             %% "play-conditional-form-mapping-play-30"  % "3.4.0",
    "com.github.tototoshi"    %% "scala-csv"                              % "2.0.0",
    "net.ruippeixotog"        %% "scala-scraper"                          % "3.2.0",
    "uk.gov.hmrc.objectstore" %% "object-store-client-play-30"            % "2.5.0"
  ).map(_.withSources())

  def test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"       %% "bootstrap-test-play-30"   % bootstrapPlayVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-test-play-30"  % hmrcMongoPlayVersion,
    "org.jsoup"          % "jsoup"                    % "1.21.2"
  ).map(_ % Test)

  def it: Seq[sbt.ModuleID] = Seq(
    "tools.jackson.core"     % "jackson-databind"     % jacksonVersion,
    "tools.jackson.module"  %% "jackson-module-scala" % jacksonVersion
  ).map(_ % Test)

}
