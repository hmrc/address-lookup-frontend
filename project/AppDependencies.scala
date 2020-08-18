import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt.Tests.{Group, SubProcess}
import sbt._

object AppDependencies {
  val appName = "address-lookup-frontend"

  lazy val appDependencies: Seq[ModuleID] = compile ++ test("test") ++ itDependencies

  val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "bootstrap-play-26" % "1.13.0",
    "uk.gov.hmrc" %% "play-ui" % "8.11.0-play-26",
    "uk.gov.hmrc" %% "govuk-template" % "5.55.0-play-26",
    "uk.gov.hmrc" %% "play-frontend-govuk" % "0.50.0-play-26",
    "uk.gov.hmrc" %% "play-frontend-hmrc" % "0.18.0-play-26",
  //    "uk.gov.hmrc" %% "frontend-bootstrap" % "12.8.0",
    "uk.gov.hmrc" %% "address-reputation-store" % "2.40.0",
    "uk.gov.hmrc" %% "http-caching-client" % "9.1.0-play-26",
    "io.spray" %% "spray-http" % "1.3.4",
    "uk.gov.hmrc" %% "play-language" % "4.3.0-play-26"
  ).map(_.withSources())

  def test(scope: String = "test") = Seq(
    "org.scalatest" %% "scalatest" % "3.0.8" % scope,
    "org.pegdown" % "pegdown" % "1.6.0" % scope,
    "org.jsoup" % "jsoup" % "1.8.1" % scope,
    "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
    "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % scope,
    "org.mockito" % "mockito-all" % "2.0.2-beta" % scope
  ).map(_.withSources())

  def itDependencies = test("it") ++
  Seq("com.github.tomakehurst" % "wiremock-jre8" % "2.26.1" % "it")

}

private object TestPhases {
  def oneForkedJvmPerTest(tests: Seq[TestDefinition]): Seq[Group] =
    tests map {
      test => Group(test.name, Seq(test), SubProcess(ForkOptions(runJVMOptions = Seq("-Dtest.name=" + test.name))))
    }
}