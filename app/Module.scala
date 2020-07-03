import com.google.inject.AbstractModule
import play.api.{Configuration, Environment}
import play.api.libs.concurrent.AkkaGuiceSupport

class Module(environment: Environment,
             playConfig: Configuration) extends AbstractModule with AkkaGuiceSupport {
  def configure(): Unit = {


  }
}
