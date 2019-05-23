import sbt._
import play.sbt.PlayImport._
import play.core.PlayVersion
import uk.gov.hmrc.SbtAutoBuildPlugin
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin
import uk.gov.hmrc.versioning.SbtGitVersioning

object FrontendBuild extends Build with MicroService {

  val appName = "address-lookup-frontend"

  override lazy val appDependencies: Seq[ModuleID] = compile ++ test() ++ itDependencies

  val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "frontend-bootstrap" % "12.8.0",
    "uk.gov.hmrc" %% "address-reputation-store" % "2.37.0",
    "uk.gov.hmrc" %% "http-caching-client" % "7.0.0",
    "io.spray" %% "spray-http" % "1.3.4"
  )

  def test(scope: String = "test") = Seq(
    "uk.gov.hmrc" %% "hmrctest" % "3.8.0-play-25" % scope,
    "org.scalatest" %% "scalatest" % "2.2.6" % scope,
    "org.pegdown" % "pegdown" % "1.6.0" % scope,
    "org.jsoup" % "jsoup" % "1.8.1" % scope,
    "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
    "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.1" % scope,

    "com.github.tomakehurst" % "wiremock" % "2.6.0" % "it"

  )

  def itDependencies = test("it")

}