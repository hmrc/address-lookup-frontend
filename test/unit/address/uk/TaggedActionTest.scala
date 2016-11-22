package address.uk

import address.ViewConfig
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import org.scalatest.WordSpec
import play.api.test.FakeRequest
import play.api.mvc._
import play.api.test.Helpers._

class TaggedActionTest extends WordSpec with Results {

  implicit val ec = scala.concurrent.ExecutionContext.Implicits.global
  implicit val system = ActorSystem("TaggedActionTest")
  implicit def mat: Materializer = ActorMaterializer()

  val tag0: String = ViewConfig.cfg.toList.head._1

  "withTag" must {

    "return OK when tag is present and well-known" in {
      val req = FakeRequest("GET", "/")
      val h = TaggedAction.withTag(tag0).apply {
        Ok("foo")
      }
      val r = call(h, req)
      assert(status(r) === 200)
      assert(contentAsString(r) === "foo")
    }

    "return BadRequest when tag is unknown" in {
      val req = FakeRequest("GET", "/")
      val h = TaggedAction.withTag("z").apply {
        Ok("foo")
      }
      val r = call(h, req)
      assert(status(r) === 400)
      assert(contentAsString(r) === "z: unknown tag")
    }

  }
}
