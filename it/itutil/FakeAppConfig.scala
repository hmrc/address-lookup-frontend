package itutil

trait FakeAppConfig {

  val mockHost: String
  val mockPort: Int
  lazy val mockUrl = s"http://$mockHost:$mockPort"

  def fakeConfig(extraConfig: (String,String)*) = Map(
    "microservice.services.keystore.host" -> s"$mockHost",
    "microservice.services.keystore.port" -> s"$mockPort",
    "microservice.services.address-reputation.host" -> s"$mockHost",
    "microservice.services.address-reputation.port" -> s"$mockPort",
    "auditing.consumer.baseUri.host" -> s"$mockHost",
    "auditing.consumer.baseUri.port" -> s"$mockHost"
  ) ++ extraConfig

}