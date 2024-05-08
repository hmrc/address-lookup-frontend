/*
 * Copyright 2024 HM Revenue & Customs
 *
 */

package controllers

import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.Inject

class PingController @Inject()(controllerComponents: MessagesControllerComponents)
  extends FrontendController(controllerComponents) {

  def ping: Action[AnyContent] = Action {
    Ok
  }
}