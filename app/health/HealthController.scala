/*
 * Copyright 2024 HM Revenue & Customs
 *
 */

package health

import play.api.mvc.{Action, AnyContent, DefaultControllerComponents}
import play.api.{Configuration, Environment}
import services.WelshCountryNamesDataSource
import uk.gov.hmrc.play

import javax.inject.{Inject, Singleton}

@Singleton
class HealthController @Inject()(configuration: Configuration, environment: Environment,
     dataSource: WelshCountryNamesDataSource, controllerComponents: DefaultControllerComponents)
  extends play.health.HealthController(configuration, environment, controllerComponents) {

  override def ping: Action[AnyContent] = Action {
    if (dataSource.isCacheReady) Ok
    else ServiceUnavailable
  }
}
