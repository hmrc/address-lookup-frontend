import uk.gov.hmrc.DefaultBuildSettings

ThisBuild / majorVersion := 2
ThisBuild / scalaVersion := "2.13.12"

val appName = "address-lookup-frontend"

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .settings(
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
    TwirlKeys.templateImports ++= Seq(
      "uk.gov.hmrc.hmrcfrontend.views.html.components._",
      "uk.gov.hmrc.govukfrontend.views.html.components._"
    ),
    scalacOptions += "-Wconf:cat=unused-imports&src=html/.*:s",
    scalacOptions += "-Wconf:cat=unused-imports&src=routes/.*:s"
  )
  .settings(PlayKeys.playDefaultPort := 9028)
  .settings(resolvers += Resolver.jcenterRepo)

lazy val it = project.in(file("it"))
  .enablePlugins(play.sbt.PlayScala)
  .dependsOn(microservice % "test->test")
  .settings(
    libraryDependencies ++= AppDependencies.it,
    Test / javaOptions += "-Dlogger.resource=logback-test.xml",
    Test / parallelExecution := false
  )
  .settings(DefaultBuildSettings.itSettings())
