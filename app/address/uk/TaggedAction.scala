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

import play.api.mvc.{Request, _}

import scala.concurrent.{ExecutionContext, Future}


class TaggedAction(tag: String)(implicit ec: ExecutionContext) extends ActionBuilder[Request] {
  import address.ViewConfig._

  override def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]): Future[Result] = {
    if (cfg.contains(tag)) {
      block(request)
    } else {
      Future(Results.BadRequest(s"$tag: unknown tag"))
    }
  }
}


object TaggedAction {
  def withTag(tag: String)(implicit ec: ExecutionContext) = new TaggedAction(tag)(ec)
}

