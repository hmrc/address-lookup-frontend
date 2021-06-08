/*
 * Copyright 2021 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.google.inject.AbstractModule
import config.AddressLookupFrontendSessionCache
import controllers.RemoteMessagesApiProvider
import play.api.libs.concurrent.AkkaGuiceSupport
import play.api.{Configuration, Environment}
import uk.gov.hmrc.http.cache.client.HttpCaching

class Module(environment: Environment, playConfig: Configuration)
  extends AbstractModule
    with AkkaGuiceSupport {
  override def configure(): Unit = {
    bind(classOf[HttpCaching]).to(classOf[AddressLookupFrontendSessionCache])
    bind(classOf[RemoteMessagesApiProvider])
  }
}
