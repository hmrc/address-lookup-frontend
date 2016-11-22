package address.uk

import play.api.mvc.{Request, _}

import scala.concurrent.{ExecutionContext, Future}


class TaggedAction(tag: String)(implicit ec: ExecutionContext) extends ActionBuilder[Request] {
  import address.ViewConfig._

  override def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]) = {
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

