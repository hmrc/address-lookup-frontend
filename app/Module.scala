import com.google.inject.AbstractModule
import config.AddressLookupFrontendSessionCache
import play.api.{Configuration, Environment}
import play.api.libs.concurrent.AkkaGuiceSupport
import uk.gov.hmrc.http.cache.client.HttpCaching

class Module(environment: Environment,
             playConfig: Configuration) extends AbstractModule with AkkaGuiceSupport {
  def configure(): Unit = {

    bind(classOf[HttpCaching]).to(classOf[AddressLookupFrontendSessionCache])

  }
}
