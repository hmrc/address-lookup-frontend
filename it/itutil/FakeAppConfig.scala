package itutil

trait FakeAppConfig {

  val mockHost: String
  val mockPort: Int
  lazy val mockUrl = s"http://$mockHost:$mockPort"

  def fakeConfig(extraConfig: (String,String)*) = Map(
    "microservice.services.keystore.host" -> s"$mockHost",
    "microservice.services.keystore.port" -> s"$mockPort"
  ) ++ extraConfig

}