package services

import org.htmlunit.ProxyConfig

class ExtendedProxyConfig(host: String, port: Int, scheme: String, val username: String,
                          val password: String) extends ProxyConfig(host, port, scheme) {
}
