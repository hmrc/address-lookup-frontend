import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt._

object AppDependencies {
  val appName = "address-lookup-frontend"

  lazy val appDependencies: Seq[ModuleID] = compile ++ test("test") ++ itDependencies
  val boostrapPlayVersion = "7.12.0"

  val compile = Seq(
    ws,
    "uk.gov.hmrc"           %% "bootstrap-frontend-play-28"      % boostrapPlayVersion,
    "uk.gov.hmrc"           %% "play-frontend-hmrc"              % "5.2.0-play-28",
    "uk.gov.hmrc"           %% "http-caching-client"             % "10.0.0-play-28",
    "uk.gov.hmrc.mongo"     %% "hmrc-mongo-play-28"              % "1.3.0",
    "uk.gov.hmrc"           %% "play-conditional-form-mapping"   % "1.12.0-play-28",
    "com.github.tototoshi"  %% "scala-csv"                       % "1.3.10"
  ).map(_.withSources())

  def test(scope: String = "test") = Seq(
    "uk.gov.hmrc" %% "bootstrap-test-play-28" % boostrapPlayVersion % scope,
    "org.jsoup" % "jsoup" % "1.15.4" % scope,
    "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
    "org.scalatestplus" %% "mockito-4-6" % "3.2.15.0" % scope,
    "com.vladsch.flexmark" % "flexmark-all" % "0.62.2" % scope
  )

  def itDependencies = test("it") ++ Seq(
    "com.github.tomakehurst" % "wiremock-jre8" % "2.35.0" % "it",
    "com.fasterxml.jackson.core" % "jackson-databind" % "2.15.1" % "it",
    "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.15.1" % "it"
  )

}
