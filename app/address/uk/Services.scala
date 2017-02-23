/*
 * Copyright 2016 HM Revenue & Customs
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

package address.uk

import address.uk.service.AddressLookupService
import config.FrontendGlobal
import config.ConfigHelper._
import keystore.{MemoMetrics, KeystoreServiceImpl}

object Services {

  import scala.concurrent.ExecutionContext.Implicits.global

  def configuredAddressLookupService(environment: play.api.Environment, configuration: play.api.Configuration ) = new AddressLookupService(
    mustGetConfigString(environment.mode, configuration, "addressReputation.endpoint"),
    FrontendGlobal.appName)

  private def configuredKeystoreService(environment: play.api.Environment, configuration: play.api.Configuration ) = new KeystoreServiceImpl(
    mustGetConfigString(environment.mode, configuration, "keystore.endpoint"),
    FrontendGlobal.appName,
    FrontendGlobal.logger)

  def metricatedKeystoreService(environment: play.api.Environment, configuration: play.api.Configuration ) = new MemoMetrics(configuredKeystoreService(environment,configuration),
    FrontendGlobal.logger)
}
