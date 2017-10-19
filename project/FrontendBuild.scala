import sbt._
import play.sbt.PlayImport._
import play.core.PlayVersion
import uk.gov.hmrc.SbtAutoBuildPlugin
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin
import uk.gov.hmrc.versioning.SbtGitVersioning

object FrontendBuild extends Build with MicroService {

  val appName = "address-lookup-frontend"

  override lazy val appDependencies: Seq[ModuleID] = compile ++ test()

  val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "frontend-bootstrap" % "8.8.0",
    "uk.gov.hmrc" %% "address-reputation-store" % "2.22.0",
    "uk.gov.hmrc" %% "http-caching-client" % "7.0.0",
    "io.spray" %% "spray-http" % "1.3.4"
  )

  def test(scope: String = "test") = Seq(
    //"uk.gov.hmrc" %% "hmrctest" % "3.0.0" % scope,
    "uk.gov.hmrc" %% "hmrctest" % "2.3.0" % scope,
    "org.scalatest" %% "scalatest" % "2.2.6" % scope,
    "org.pegdown" % "pegdown" % "1.6.0" % scope,
    "org.jsoup" % "jsoup" % "1.8.1" % scope,
    "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
    "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % scope,
    "com.pyruby" % "java-stub-server" % "0.14" % scope
  )

}
