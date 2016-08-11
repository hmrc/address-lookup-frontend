package address.uk

import config.ConfigHelper._
import play.api.Play

object Services {

  lazy val baseUrl = mustGetConfigString(Play.current, "addressLookup.server")
}
