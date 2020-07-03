import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt.Tests.{Group, SubProcess}
import sbt._

object AppDependencies {
  val appName = "address-lookup-frontend"

  lazy val appDependencies: Seq[ModuleID] = compile ++ test("test") ++ itDependencies

  val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "bootstrap-play-25" % "5.3.0",
    "uk.gov.hmrc" %% "play-ui" % "8.11.0-play-25",
    "uk.gov.hmrc" %% "govuk-template" % "5.55.0-play-25",
  //    "uk.gov.hmrc" %% "frontend-bootstrap" % "12.8.0",
    "uk.gov.hmrc" %% "address-reputation-store" % "2.37.0",
    "uk.gov.hmrc" %% "http-caching-client" % "7.0.0",
    "io.spray" %% "spray-http" % "1.3.4",
    "uk.gov.hmrc" %% "play-language" % "3.4.0"
  ).map(_.withSources())

  def test(scope: String = "test") = Seq(
    "uk.gov.hmrc" %% "hmrctest" % "3.8.0-play-25" % scope,

    "org.pegdown" % "pegdown" % "1.6.0" % scope,
    "org.jsoup" % "jsoup" % "1.8.1" % scope,
    "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
    "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.1" % scope,
    "org.mockito" % "mockito-all" % "2.0.2-beta" % scope
  ).map(_.withSources())

  def itDependencies = test("it") ++
  Seq("com.github.tomakehurst" % "wiremock" % "2.6.0" % "it")

}

private object TestPhases {
  def oneForkedJvmPerTest(tests: Seq[TestDefinition]): Seq[Group] =
    tests map {
      test => Group(test.name, Seq(test), SubProcess(ForkOptions(runJVMOptions = Seq("-Dtest.name=" + test.name))))
    }
}